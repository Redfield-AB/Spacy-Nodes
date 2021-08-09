/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.redfield.textprocessing.SpacyPlugin;
import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.data.dto.SpacyDocument;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

public class SpacyTokenizerNodeModel extends NodeModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyTokenizerNodeModel.class);

	private final SpacyNodeSettings settings = new SpacyNodeSettings();

	protected SpacyTokenizerNodeModel() {
		super(1, 1);
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { null };
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		SpacyPlugin.checkLicense();

		BufferedDataTable inTable = inData[0];
		settings.getSpacyModel().ensureDownloaded();

		try (PythonContext context = new PythonContext()) {
			context.putDataTable(inTable, exec);
			context.executeInKernel(getScript(), exec);
			BufferedDataTable result = context.getDataTable(exec, exec);
			BufferedDataTable joined = exec.createJoinedTable(inTable, result, exec);

			ColumnRearranger r = new ColumnRearranger(joined.getDataTableSpec());
			int colIndex = joined.getDataTableSpec().findColumnIndex(settings.getColumn());
			int jsonColIdx = joined.getDataTableSpec().getNumColumns() - 1;
			r.replace(new DocumentCellFactory(createTextContainerFactory(exec), settings.getColumn(), jsonColIdx),
					colIndex);
			r.remove(jsonColIdx);

			return new BufferedDataTable[] { exec.createColumnRearrangeTable(joined, r, exec) };
		}
	}

	private static TextContainerDataCellFactory createTextContainerFactory(ExecutionContext exec) {
		TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
		docFactory.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
		return docFactory;
	}

	private String getScript() {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from SpacyNlp import SpacyNlp");
		b.a(PythonContext.VAR_OUTPUT_TABLE).a(" = SpacyNlp.run(").asr(settings.getSpacyModelPath()).a(",")
				.a(PythonContext.VAR_INPUT_TABLE).a(", ").as(settings.getColumn()).a(")").n();
		return b.toString();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettings(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettings(settings);
	}

	@Override
	protected void reset() {
		// nothing to do
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	private static class DocumentCellFactory extends SingleCellFactory {

		private final int columnIndex;
		private final TextContainerDataCellFactory factory;

		public DocumentCellFactory(TextContainerDataCellFactory factory, String columnName, int columnIndex) {
			super(createSpec(columnName));
			this.columnIndex = columnIndex;
			this.factory = factory;
		}

		private static DataColumnSpec createSpec(String columnName) {
			return new DataColumnSpecCreator(columnName, DocumentCell.TYPE).createSpec();
		}

		@Override
		public DataCell getCell(DataRow row) {
			String json = row.getCell(columnIndex).toString();
			try {
				return fromJson(json);
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				return new MissingCell(e.getMessage());
			}
		}

		@SuppressWarnings("deprecation")
		private DataCell fromJson(String json) throws JsonProcessingException {
			List<Sentence> sentences = new ArrayList<>();
			SpacyDocument spacyDoc = SpacyDocument.fromJson(json);

			for (SpacySentence spacySent : spacyDoc.getSentences()) {
				List<Term> terms = new ArrayList<>();

				for (SpacyWord w : spacySent.getWords()) {
					terms.add(fromSpacyWord(w));
				}

				sentences.add(new Sentence(terms));
			}

			DocumentBuilder b = new DocumentBuilder();
			b.addParagraph(new Paragraph(sentences));
			b.createNewSection(SectionAnnotation.CHAPTER);

			return factory.createDataCell(b.createDocument());
		}

		private static Term fromSpacyWord(SpacyWord word) {
			return new Term(Arrays.asList(new Word(word.getText(), " ")), Collections.emptyList(), false);
		}

	}
}

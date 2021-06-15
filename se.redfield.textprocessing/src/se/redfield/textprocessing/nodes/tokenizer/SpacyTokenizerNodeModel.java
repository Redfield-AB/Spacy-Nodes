/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.util.DocumentDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;

import se.redfield.textprocessing.SpacyPlugin;
import se.redfield.textprocessing.core.PythonContext;

public class SpacyTokenizerNodeModel extends NodeModel {

	private final SpacyTokenizerNodeSettings settings = new SpacyTokenizerNodeSettings();

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

			ColumnRearranger r = new ColumnRearranger(result.getDataTableSpec());
			int colIndex = result.getDataTableSpec().findColumnIndex(settings.getColumn());
			r.replace(new DocumentCellFactory(settings.getColumn(), colIndex), colIndex);

			return new BufferedDataTable[] { exec.createColumnRearrangeTable(result, r, exec) };
		}
	}

	private String getScript() {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from SpacyTokenizer import tokenize");
		b.a(PythonContext.VAR_OUTPUT_TABLE).a(" = tokenize(").a(PythonContext.VAR_INPUT_TABLE).a(", ")
				.as(settings.getColumn()).a(",").asr(settings.getSpacyModelPath()).a(")").n();
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

	private static class DocumentCellFactory extends AbstractCellFactory {

		private final int columnIndex;
		private final TextContainerDataCellFactory factory;
		private final JSONParser parser;

		public DocumentCellFactory(String columnName, int columnIndex) {
			super(createSpec(columnName));
			this.columnIndex = columnIndex;
			this.factory = new DocumentDataCellFactory();
			this.parser = new JSONParser();
		}

		private static DataColumnSpec createSpec(String columnName) {
			return new DataColumnSpecCreator(columnName, DocumentCell.TYPE).createSpec();
		}

		@Override
		public DataCell[] getCells(DataRow row) {
			String json = row.getCell(columnIndex).toString();
			try {
				return new DataCell[] { fromJson(json) };
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("deprecation")
		private DataCell fromJson(String json) throws ParseException {
			JSONArray jsonSenteces = (JSONArray) parser.parse(json);
			List<Sentence> sentences = new ArrayList<>();
			for (Object obj : jsonSenteces) {
				JSONArray jsonWords = (JSONArray) obj;
				List<Term> terms = new ArrayList<>();
				for (Object word : jsonWords) {
					terms.add(fromJson((JSONObject) word));
				}
				sentences.add(new Sentence(terms));
			}

			DocumentBuilder b = new DocumentBuilder();
			b.addParagraph(new Paragraph(sentences));
			b.createNewSection(SectionAnnotation.CHAPTER);

			return factory.createDataCell(b.createDocument());
		}

		private static Term fromJson(JSONObject obj) {
			String tag = (String) obj.get("tag");
			List<Tag> tags = Arrays.asList(PartOfSpeechTag.stringToTag(tag.toUpperCase()));
			Term res = new Term(Arrays.asList(new Word((String) obj.get("text"), " ")), tags, false);
			return res;
		}
	}
}

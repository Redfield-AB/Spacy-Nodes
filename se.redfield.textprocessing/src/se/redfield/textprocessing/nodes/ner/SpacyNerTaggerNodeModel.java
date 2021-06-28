/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.ner;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import se.redfield.textprocessing.SpacyPlugin;
import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.SpacyNerTagger;
import se.redfield.textprocessing.core.SpacyNlp;
import se.redfield.textprocessing.nodes.tokenizer.SpacyTokenizerNodeSettings;

public class SpacyNerTaggerNodeModel extends NodeModel {

	private final SpacyTokenizerNodeSettings settings = new SpacyTokenizerNodeSettings();

	protected SpacyNerTaggerNodeModel() {
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

		try (PythonContext context = new PythonContext()) {
			SpacyNlp nlp = new SpacyNlp(context, exec, settings.getSpacyModelPath());
			SpacyNerTagger tagger = new SpacyNerTagger(nlp);
			TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
			docFactory.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));

			ColumnRearranger r = new ColumnRearranger(inTable.getDataTableSpec());
			int colIndex = inTable.getDataTableSpec().findColumnIndex(settings.getColumn());
			r.replace(new SpacyNerCellFactory(docFactory, tagger, settings.getColumn(), colIndex), colIndex);

			return new BufferedDataTable[] { exec.createColumnRearrangeTable(inTable, r, exec) };
		}
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

	private static class SpacyNerCellFactory extends SingleCellFactory {
		private TextContainerDataCellFactory factory;
		private SpacyNerTagger tagger;
		private int columnIdx;

		public SpacyNerCellFactory(TextContainerDataCellFactory factory, SpacyNerTagger tagger, String columnName,
				int columnIdx) {
			super(createSpec(factory, columnName));
			this.factory = factory;
			this.tagger = tagger;
			this.columnIdx = columnIdx;
		}

		private static DataColumnSpec createSpec(TextContainerDataCellFactory factory, String columnName) {
			return new DataColumnSpecCreator(columnName, factory.getDataType()).createSpec();
		}

		@Override
		public DataCell getCell(DataRow row) {
			final Document d = ((DocumentValue) row.getCell(columnIdx)).getDocument();
			return factory.createDataCell(tagger.tag(d));
		}
	}
}

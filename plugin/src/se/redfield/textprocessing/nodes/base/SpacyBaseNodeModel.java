/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
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
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.redfield.textprocessing.SpacyPlugin;
import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.SpacyNlp;
import se.redfield.textprocessing.data.dto.SpacyDocument;

public abstract class SpacyBaseNodeModel extends NodeModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyBaseNodeModel.class);

	protected final SpacyNodeSettings settings;

	protected SpacyBaseNodeModel(SpacyNodeSettings settings) {
		super(1, 1);
		this.settings = settings;
	}

	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		return new DataTableSpec[] { createSpec() };
	}

	private static DataTableSpec createSpec() {
		return null;
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec) throws Exception {
		SpacyPlugin.checkLicense();

		BufferedDataTable inTable = inData[0];

		try (PythonContext ctx = new PythonContext()) {
			SpacyNlp nlp = new SpacyNlp(ctx, exec, settings.getSpacyModelPath());
			BufferedDataTable res = nlp.processDocuments(prepareInputTable(inTable, exec), settings.getColumn(),
					getSpacyMethod());
			BufferedDataTable joined = exec.createJoinedTable(inTable, res, exec);

			int docColIdx = joined.getDataTableSpec().findColumnIndex(settings.getColumn());
			int jsonColIdx = joined.getDataTableSpec().getNumColumns() - 1;
			SpacyDocumentCellFactory fac = new SpacyDocumentCellFactory(createTextContainerFactory(exec),
					createSpacyDocumentProcessor(), settings.getColumn(), docColIdx, jsonColIdx);

			ColumnRearranger r = new ColumnRearranger(joined.getDataTableSpec());
			r.replace(fac, docColIdx);
			r.remove(jsonColIdx);

			return new BufferedDataTable[] { exec.createColumnRearrangeTable(joined, r, exec) };
		}
	}

	private BufferedDataTable prepareInputTable(BufferedDataTable inTable, ExecutionContext exec)
			throws CanceledExecutionException {
		int idx = inTable.getDataTableSpec().findColumnIndex(settings.getColumn());

		SingleCellFactory fac = new SingleCellFactory(
				new DataColumnSpecCreator(settings.getColumn(), StringCell.TYPE).createSpec()) {

			@Override
			public DataCell getCell(DataRow row) {
				return new StringCell(((DocumentValue) row.getCell(idx)).getDocument().getDocumentBodyText());
			}
		};

		ColumnRearranger r = new ColumnRearranger(inTable.getDataTableSpec());
		r.replace(fac, idx);
		r.keepOnly(idx);
		return exec.createColumnRearrangeTable(inTable, r, exec);
	}

	private static TextContainerDataCellFactory createTextContainerFactory(ExecutionContext exec) {
		TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
		docFactory.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
		return docFactory;
	}

	protected abstract String getSpacyMethod();

	protected abstract SpacyDocumentProcessor createSpacyDocumentProcessor();

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
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void reset() {
		// nothing to do
	}

	private static class SpacyDocumentCellFactory extends SingleCellFactory {
		private final TextContainerDataCellFactory factory;
		private final SpacyDocumentProcessor processor;

		private final int docColumnIdx;
		private final int jsonColumnIdx;

		public SpacyDocumentCellFactory(TextContainerDataCellFactory factory, SpacyDocumentProcessor processor,
				String newColumnName, int docColumnIdx, int jsonColumnIdx) {
			super(createSpec(factory, newColumnName));
			this.factory = factory;
			this.processor = processor;

			this.docColumnIdx = docColumnIdx;
			this.jsonColumnIdx = jsonColumnIdx;
		}

		private static DataColumnSpec createSpec(TextContainerDataCellFactory factory, String columnName) {
			return new DataColumnSpecCreator(columnName, factory.getDataType()).createSpec();
		}

		@Override
		public DataCell getCell(DataRow row) {
			final Document d = ((DocumentValue) row.getCell(docColumnIdx)).getDocument();

			try {
				SpacyDocument spacyDoc = SpacyDocument.fromJson(row.getCell(jsonColumnIdx).toString());
				return factory.createDataCell(processor.process(spacyDoc, d));
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				return new MissingCell(e.getMessage());
			}
		}

	}

}

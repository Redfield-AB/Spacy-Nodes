/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;
import org.knime.python2.kernel.PythonIOException;

import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.nodes.port.ISpacyModelPortObject;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObjectSpec;

/**
 * Base {@link NodeModel} implementation for other SpaCy nodes.
 * 
 * @author Alexander Bondaletov
 *
 */
public abstract class SpacyBaseNodeModel extends NodeModel {
	/**
	 * The input port containing SpaCy model.
	 */
	public static final int PORT_MODEL = 0;

	protected static final String PYTHON_RES_COLUMN_NAME = "result";

	protected final SpacyNodeSettings settings;
	
	private final boolean acceptStringColumn;
	
	private final boolean hasModelPorts;
	
	private final int tablePortIdx;

	protected SpacyBaseNodeModel(SpacyNodeSettings settings, boolean acceptStringColumn, boolean hasModelPorts) {
		super(createPorts(hasModelPorts), createPorts(hasModelPorts));
		this.settings = settings;
		this.acceptStringColumn = acceptStringColumn;
		this.hasModelPorts = hasModelPorts;
		this.tablePortIdx = hasModelPorts ? 1 : 0;
	}
	
	private static PortType[] createPorts(boolean hasModelPorts) {
		if (hasModelPorts) {
			return new PortType[] { ISpacyModelPortObject.TYPE, BufferedDataTable.TYPE };
		} else {
			return new PortType[] { BufferedDataTable.TYPE };
		}
	}
	
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec inSpec = (DataTableSpec) inSpecs[this.tablePortIdx];

		if (settings.getColumn().isEmpty()) {
			attemptAutoconfigureColumn(inSpec);
		}
		settings.validate();
		validateSpec(inSpecs);

		final var outputSpec = createSpec(inSpec);
		
		if (this.hasModelPorts) {
			return new PortObjectSpec[] { inSpecs[PORT_MODEL], outputSpec };
		} else {
			return new PortObjectSpec[] { outputSpec };
		}
	}

	private void attemptAutoconfigureColumn(DataTableSpec spec) {
		DataColumnSpec defColumn = null;
		Class<? extends DataValue> valueType = acceptStringColumn ? StringValue.class : DocumentValue.class;

		for (int i = 0; i < spec.getNumColumns() && defColumn == null; i++) {
			if (spec.getColumnSpec(i).getType().isCompatible(valueType)) {
				defColumn = spec.getColumnSpec(i);
			}
		}

		if (defColumn != null) {
			settings.getColumnModel().setStringValue(defColumn.getName());
			setWarningMessage(String.format("Column '%s' is selected automatically", defColumn.getName()));
		}
	}

	protected DataTableSpec createSpec(DataTableSpec inSpec) {
		int idx = inSpec.findColumnIndex(settings.getColumn());

		DataTableSpecCreator c = new DataTableSpecCreator(inSpec);
		DataColumnSpec outColumn = createOutputColumnSpec();
		if (settings.getReplaceColumn()) {
			c.replaceColumn(idx, outColumn);
		} else {
			c.addColumns(outColumn);
		}

		return c.createSpec();
	}

	private void validateSpec(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec tableSpec = (DataTableSpec) inSpecs[this.tablePortIdx];
		int idx = tableSpec.findColumnIndex(settings.getColumn());
		if (idx < 0) {
			throw new InvalidSettingsException(
					String.format("Column '%s' is not found in the table", settings.getColumn()));
		}

		DataColumnSpec column = tableSpec.getColumnSpec(idx);
		Class<? extends DataValue> valueType = acceptStringColumn ? StringValue.class : DocumentValue.class;
		if (!column.getType().isCompatible(valueType)) {
			throw new InvalidSettingsException(String.format("Column '%s' has unsupported type", column.getName()));
		}

		if (hasModelPorts) {
			SpacyModelPortObjectSpec modelSpec = (SpacyModelPortObjectSpec) inSpecs[PORT_MODEL];
			Set<SpacyFeature> features = modelSpec.getModel().getFeatures();

			if (features == null) {
				throw new InvalidSettingsException(
						"Model features are not available. Please execute the model selector node first.");
			}

			if (!features.contains(getFeature())) {
				throw new InvalidSettingsException(getFeature() + " is not supported by the provided model.");
			}
		}
	}

	protected DataColumnSpec createOutputColumnSpec() {
		return new DataColumnSpecCreator(settings.getOutputColumnName(), DocumentCell.TYPE).createSpec();
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {
		BufferedDataTable inTable = (BufferedDataTable) inData[this.tablePortIdx];
		exec.setMessage("spaCy");

		try (PythonContext ctx = new PythonContext(settings.getPythonCommand().getCommand(), 2)) {
			BufferedDataTable inputTable = prepareInputTable(inTable, exec.createSubExecutionContext(0.05));
			ctx.putDataTable(0, inputTable, exec.createSubProgress(0.05));
			final var applyModelProgress = exec.createSubProgress(0.8);
			applyModelProgress.setMessage(() -> "Applying the pipeline.");
			ctx.executeInKernel(createExecuteScript(getModelPath(inData)), applyModelProgress);

			BufferedDataTable result = buildOutputTable(inTable, ctx, exec.createSubExecutionContext(0.1),
					getModelName(inData));
			if (this.hasModelPorts) {
				return new PortObject[] { inData[PORT_MODEL], result };
			} else {
				return new PortObject[] { result };
			}
		}
	}
	
	private String getModelName(final PortObject[] inData) {
		if (this.hasModelPorts) {
			var model = (ISpacyModelPortObject) inData[PORT_MODEL];
			return model.getSpec().getModel().getName();
		} else {
			var oldSettings = (OldSpacyNodeSettings)settings;
			return Path.of(oldSettings.getSpacyModelPath()).getFileName().toString();
		}
	}
	
	private String getModelPath(final PortObject[] inData) throws IOException {
		if (this.hasModelPorts) {
			var model = (ISpacyModelPortObject) inData[PORT_MODEL];
			return model.getModelPath();
		} else {
			var oldSettings = (OldSpacyNodeSettings)settings;
			oldSettings.getSpacyModel().ensureDownloaded();
			return oldSettings.getSpacyModelPath();
		}
	}

	protected BufferedDataTable buildOutputTable(BufferedDataTable inTable, PythonContext ctx, ExecutionContext exec, String modelName)
			throws CanceledExecutionException, PythonIOException {
		exec.setMessage(() -> "Retrieving the output table.");
		BufferedDataTable res = ctx.getDataTable(0, exec.createSubExecutionContext(0.05));
		BufferedDataTable meta = ctx.getDataTable(1, exec.createSubExecutionContext(0.05));

		BufferedDataTable joined = exec.createJoinedTable(inTable, res, exec.createSubProgress(0.05));

		int inColIdx = joined.getDataTableSpec().findColumnIndex(settings.getColumn());
		int resColIdx = joined.getDataTableSpec().getNumColumns() - 1;
		CellFactory fac = createCellFactory(inColIdx, resColIdx, joined.getDataTableSpec(), meta,
				exec.createSubExecutionContext(0.05), modelName);

		ColumnRearranger r = new ColumnRearranger(joined.getDataTableSpec());
		if (settings.getReplaceColumn()) {
			r.replace(fac, inColIdx);
		} else {
			r.append(fac);
		}
		r.remove(resColIdx);

		BufferedDataTable result = exec.createColumnRearrangeTable(joined, r, exec.createSubProgress(0.80));
		exec.setProgress(1.0);
		return result;
	}

	private String createExecuteScript(String modelPath) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from SpacyNlp import " + getSpacyMethod());
		b.a(getSpacyMethod()).a(".run(").n();
		b.a("model_handle = ").asr(modelPath).a(",").n();
		PythonContext.putInputTableArgs(b, "input_table", 0);
		b.a("column = ").as(settings.getColumn()).a(")").n();
		return b.toString();
	}

	private BufferedDataTable prepareInputTable(BufferedDataTable inTable, ExecutionContext exec)
			throws CanceledExecutionException {
		exec.setMessage(() -> "Preparing the input data.");
		final var spec = inTable.getDataTableSpec();
		ColumnRearranger r = new ColumnRearranger(spec);
		int idx = spec.findColumnIndex(settings.getColumn());

		if (spec.getColumnSpec(idx).getType().isCompatible(DocumentValue.class)) {
			SingleCellFactory fac = new SingleCellFactory(
					new DataColumnSpecCreator(settings.getColumn(), StringCell.TYPE).createSpec()) {

				@Override
				public DataCell getCell(DataRow row) {
					return new StringCell(((DocumentValue) row.getCell(idx)).getDocument().getDocumentBodyText());
				}
			};

			r.replace(fac, idx);
		}

		r.keepOnly(idx);
		BufferedDataTable result = exec.createColumnRearrangeTable(inTable, r, exec);
		exec.setProgress(1.0);
		return result;
	}

	protected TextContainerDataCellFactory createTextContainerFactory(ExecutionContext exec) {
		TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
		docFactory.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
		return docFactory;
	}

	protected abstract CellFactory createCellFactory(int inputColumn, int resultColumn, DataTableSpec inSpec,
			BufferedDataTable metaTable, ExecutionContext exec, String modelName) throws CanceledExecutionException;

	protected abstract String getSpacyMethod();

	protected abstract SpacyFeature getFeature();

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettingsFrom(settings);
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

}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import java.io.File;
import java.io.IOException;
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

import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObject;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObjectSpec;

public abstract class SpacyBaseNodeModel extends NodeModel {
	public static final int PORT_MODEL = 0;
	public static final int PORT_TABLE = 1;

	protected final SpacyNodeSettings settings;
	private final boolean acceptStringColumn;

	protected SpacyBaseNodeModel(SpacyNodeSettings settings, boolean acceptStringColumn) {
		super(new PortType[] { SpacyModelPortObject.TYPE, BufferedDataTable.TYPE },
				new PortType[] { SpacyModelPortObject.TYPE, BufferedDataTable.TYPE });
		this.settings = settings;
		this.acceptStringColumn = acceptStringColumn;
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec inSpec = (DataTableSpec) inSpecs[PORT_TABLE];

		if (settings.getColumn().isEmpty()) {
			attemptAutoconfigureColumn(inSpec);
		}
		settings.validate();
		validateSpec(inSpecs);

		return new PortObjectSpec[] { inSpecs[PORT_MODEL], createSpec(inSpec) };
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

	protected DataTableSpec createSpec(DataTableSpec inSpec) throws InvalidSettingsException {
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
		DataTableSpec tableSpec = (DataTableSpec) inSpecs[PORT_TABLE];
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

	protected DataColumnSpec createOutputColumnSpec() {
		return new DataColumnSpecCreator(settings.getOutputColumnName(), DocumentCell.TYPE).createSpec();
	}

	@Override
	protected PortObject[] execute(PortObject[] inData, ExecutionContext exec) throws Exception {
		SpacyModelPortObject model = (SpacyModelPortObject) inData[PORT_MODEL];
		BufferedDataTable inTable = (BufferedDataTable) inData[PORT_TABLE];

		try (PythonContext ctx = new PythonContext(settings.getPythonCommand().getCommand())) {
			ctx.putDataTable(prepareInputTable(inTable, exec), exec);
			ctx.executeInKernel(createExecuteScript(model.getModelPath()), exec);
			BufferedDataTable res = ctx.getDataTable(exec, exec);

			BufferedDataTable joined = exec.createJoinedTable(inTable, res, exec);

			int inColIdx = joined.getDataTableSpec().findColumnIndex(settings.getColumn());
			int resColIdx = joined.getDataTableSpec().getNumColumns() - 1;
			CellFactory fac = createCellFactory(inColIdx, resColIdx, exec);

			ColumnRearranger r = new ColumnRearranger(joined.getDataTableSpec());
			if (settings.getReplaceColumn()) {
				r.replace(fac, inColIdx);
			} else {
				r.append(fac);
			}
			r.remove(resColIdx);

			return new PortObject[] { model, exec.createColumnRearrangeTable(joined, r, exec) };
		}
	}

	private String createExecuteScript(String modelPath) {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("from SpacyNlp import " + getSpacyMethod());
		b.a(PythonContext.VAR_OUTPUT_TABLE).a(" = ").a(getSpacyMethod()).a(".run(").n();
		b.a("model_handle = ").asr(modelPath).a(",").n();
		b.a("input_table = ").a(PythonContext.VAR_INPUT_TABLE).a(",").n();
		b.a("column = ").as(settings.getColumn()).a(")").n();
		return b.toString();
	}

	private BufferedDataTable prepareInputTable(BufferedDataTable inTable, ExecutionContext exec)
			throws CanceledExecutionException {
		ColumnRearranger r = new ColumnRearranger(inTable.getDataTableSpec());
		int idx = inTable.getDataTableSpec().findColumnIndex(settings.getColumn());

		if (inTable.getDataTableSpec().getColumnSpec(idx).getType().isCompatible(DocumentValue.class)) {
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
		return exec.createColumnRearrangeTable(inTable, r, exec);
	}

	protected TextContainerDataCellFactory createTextContainerFactory(ExecutionContext exec) {
		TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
		docFactory.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
		return docFactory;
	}

	protected abstract CellFactory createCellFactory(int inputColumn, int resultColumn, ExecutionContext exec);;

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

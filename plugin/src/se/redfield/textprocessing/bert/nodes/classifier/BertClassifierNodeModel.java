/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.classifier;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.filestore.FileStore;
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
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.bert.core.BertCommands;
import se.redfield.bert.core.ClassesToFeaturesConverter;
import se.redfield.bert.core.ClassesToFeaturesConverter.ClassifierInput;
import se.redfield.bert.nodes.port.BertClassifierPortObject;
import se.redfield.bert.nodes.port.BertClassifierPortObjectSpec;
import se.redfield.bert.nodes.port.BertModelConfig;
import se.redfield.bert.nodes.port.BertModelPortObject;
import se.redfield.bert.nodes.port.BertModelPortObjectSpec;

/**
 * BERT Classifier node. Takes BERT model and constructs classifier that then
 * trainined on the provided data table.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeModel extends NodeModel {

	/**
	 * {@link BertModelPortObject} input port index.
	 */
	public static final int PORT_BERT_MODEL = 0;
	/**
	 * Data table input port index.
	 */
	public static final int PORT_DATA_TABLE = 1;

	/**
	 * Validation table input port index.
	 */
	public static final int PORT_VALIDATION_TABLE = 2;

	private final BertClassifierSettings settings = new BertClassifierSettings();

	protected BertClassifierNodeModel() {
		super(new PortType[] { BertModelPortObject.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL },
				new PortType[] { BertClassifierPortObject.TYPE, BufferedDataTable.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		BertModelPortObject bertModel = (BertModelPortObject) inObjects[PORT_BERT_MODEL];
		FileStore fileStore = exec.createFileStore("model");

		ClassesToFeaturesConverter converter = new ClassesToFeaturesConverter(settings.getSentenceColumn(),
				settings.getClassColumn(), true, settings.getClassSeparator());
		ClassifierInput input = converter.process((BufferedDataTable) inObjects[PORT_DATA_TABLE],
				(BufferedDataTable) inObjects[PORT_VALIDATION_TABLE], exec);

		BufferedDataTable statsTable = runTrain(bertModel.getModel(), fileStore, input, exec);

		return new PortObject[] {
				new BertClassifierPortObject(createSpec(bertModel.getSpec()), fileStore, input.getClasses()),
				statsTable };
	}

	private BufferedDataTable runTrain(BertModelConfig bertModel, FileStore fileStore, ClassifierInput input,
			ExecutionContext exec) throws PythonKernelCleanupException, DLInvalidEnvironmentException,
			PythonIOException, CanceledExecutionException {
		try (BertCommands commands = new BertCommands()) {
			commands.putDataTable(input.getTrainingTable(),
					exec.createSubProgress(input.hasValidationTable() ? 0.05 : 0.1));
			if (input.hasValidationTable()) {
				commands.putDataTable("validation_table", input.getValidationTable(), exec.createSubProgress(0.05));
			}

			commands.executeInKernel(getTrainScript(bertModel, fileStore, input), exec.createSubProgress(0.9));
			return commands.getDataTable(BertCommands.VAR_OUTPUT_TABLE, exec, exec.createSubProgress(0));
		}
	}

	private String getTrainScript(BertModelConfig bertModel, FileStore fileStore, ClassifierInput input) {
		DLPythonSourceCodeBuilder b = DLPythonUtils
				.createSourceCodeBuilder("from BertClassifier import BertClassifier");
		b.a(BertCommands.VAR_OUTPUT_TABLE).a(" = BertClassifier.run_train(").n();

		BertCommands.putInputTableArgs(b);
		BertCommands.putBertModelArgs(b, bertModel);
		BertCommands.putSentenceColumArg(b, settings.getSentenceColumn());
		BertCommands.putMaxSeqLengthArg(b, settings.getMaxSeqLength());
		BertCommands.putFileStoreArgs(b, fileStore);
		BertCommands.putBatchSizeArgs(b, settings.getBatchSize());

		b.a("class_column = ").as(settings.getClassColumn()).a(",").n();
		b.a("class_count = ").a(input.getClassesCount()).a(",").n();
		b.a("epochs = ").a(settings.getEpochs()).a(",").n();
		b.a("fine_tune_bert = ").a(settings.getFineTuneBert()).a(",").n();
		b.a("optimizer = " + settings.getOptimizer()).a(",").n();
		if (input.hasValidationTable()) {
			b.a("validation_table = validation_table,").n();
			b.a("validation_batch_size = ").a(settings.getValidationBatchSize()).a(",").n();
		}
		b.a("multi_label = True,").n();
		b.a(")").n();

		return b.toString();
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.validate((DataTableSpec) inSpecs[PORT_DATA_TABLE], (DataTableSpec) inSpecs[PORT_VALIDATION_TABLE]);
		return new PortObjectSpec[] { createSpec((BertModelPortObjectSpec) inSpecs[PORT_BERT_MODEL]), null };
	}

	private BertClassifierPortObjectSpec createSpec(BertModelPortObjectSpec modelSpec) {
		return new BertClassifierPortObjectSpec(settings.getMaxSeqLength(), true, settings.getClassSeparator(),
				modelSpec.getModel().getType());
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
	protected void reset() {
		// nothing to reset
	}

}

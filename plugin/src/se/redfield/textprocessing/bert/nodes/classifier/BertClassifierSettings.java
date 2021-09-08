/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.classifier;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.setting.OptimizerSettings;

/**
 * 
 * Settings for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierSettings {
	/**
	 * Default class separator character
	 */
	public static final String DEFAULT_CLASS_SEPARATOR = ";";

	private static final String KEY_SENTENCE_COLUMN = "sentenceColumn";
	private static final String KEY_MAX_SEQ_LENGTH = "maxSeqLength";
	private static final String KEY_CLASS_COLUMN = "classColumn";
	private static final String KEY_EPOCHS = "epochs";
	private static final String KEY_BATCH_SIZE = "batchSize";
	private static final String KEY_VALIDATION_BATCH_SIZE = "validationBatchSize";
	private static final String KEY_FINE_TUNE_BERT = "fineTuneBert";
	private static final String KEY_OPTIMIZER = "optimizer";
	private static final String KEY_CLASS_SEPARATOR = "classSeparator";

	private final SettingsModelString sentenceColumn;
	private final SettingsModelIntegerBounded maxSeqLength;
	private final SettingsModelString classColumn;
	private final SettingsModelIntegerBounded epochs;
	private final SettingsModelIntegerBounded batchSize;
	private final SettingsModelIntegerBounded validationBatchSize;
	private final SettingsModelBoolean fineTuneBert;
	private OptimizerSettings optimizer;
	private final SettingsModelString classSeparator;

	/**
	 * Creates new instance
	 */
	public BertClassifierSettings() {
		sentenceColumn = new SettingsModelString(KEY_SENTENCE_COLUMN, "");
		maxSeqLength = new SettingsModelIntegerBounded(KEY_MAX_SEQ_LENGTH, 128, 3, 512);
		classColumn = new SettingsModelString(KEY_CLASS_COLUMN, "");
		epochs = new SettingsModelIntegerBounded(KEY_EPOCHS, 1, 1, Integer.MAX_VALUE);
		batchSize = new SettingsModelIntegerBounded(KEY_BATCH_SIZE, 20, 1, Integer.MAX_VALUE);
		validationBatchSize = new SettingsModelIntegerBounded(KEY_VALIDATION_BATCH_SIZE, 20, 1, Integer.MAX_VALUE);
		fineTuneBert = new SettingsModelBoolean(KEY_FINE_TUNE_BERT, false);
		optimizer = new OptimizerSettings(KEY_OPTIMIZER);
		classSeparator = new SettingsModelString(KEY_CLASS_SEPARATOR, DEFAULT_CLASS_SEPARATOR);
	}

	/**
	 * Saves current settings into the given {@link NodeSettingsWO}.
	 * 
	 * @param settings
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		sentenceColumn.saveSettingsTo(settings);
		maxSeqLength.saveSettingsTo(settings);
		classColumn.saveSettingsTo(settings);
		epochs.saveSettingsTo(settings);
		validationBatchSize.saveSettingsTo(settings);
		batchSize.saveSettingsTo(settings);
		fineTuneBert.saveSettingsTo(settings);
		optimizer.saveSettingsTo(settings);
		classSeparator.saveSettingsTo(settings);
	}

	/**
	 * Validates settings in the provided {@link NodeSettingsRO}.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.validateSettings(settings);
		maxSeqLength.validateSettings(settings);
		classColumn.validateSettings(settings);
		epochs.validateSettings(settings);
		batchSize.validateSettings(settings);
		validationBatchSize.validateSettings(settings);
		fineTuneBert.validateSettings(settings);
		classColumn.validateSettings(settings);

		BertClassifierSettings temp = new BertClassifierSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	/**
	 * Validates internal consistency of the current settings
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		if (sentenceColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Sentence column is not selected");
		}

		if (classColumn.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Class column is not selected");
		}

		if (classSeparator.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Class separator is required");
		}
	}

	/**
	 * Validates the settings against input table spec.
	 * 
	 * @param spec           Input table spec.
	 * @param validationSpec Optional validation table spec.
	 * @throws InvalidSettingsException
	 */
	public void validate(DataTableSpec spec, DataTableSpec validationSpec) throws InvalidSettingsException {
		validate();

		String sc = sentenceColumn.getStringValue();
		if (!spec.containsName(sc)) {
			throw new InvalidSettingsException("Input table doesn't contain column: " + sc);
		}
		if (validationSpec != null && !validationSpec.containsName(sc)) {
			throw new InvalidSettingsException("Validation table doesn't contain sentence column: " + sc);
		}

		String cc = classColumn.getStringValue();
		if (!spec.containsName(cc)) {
			throw new InvalidSettingsException("Input table doesn't contain column: " + cc);
		}
		if (validationSpec != null && !validationSpec.containsName(cc)) {
			throw new InvalidSettingsException("Validation table doesn't contain class column: " + cc);
		}
	}

	/**
	 * Loads settings from the provided {@link NodeSettingsRO}
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		sentenceColumn.loadSettingsFrom(settings);
		maxSeqLength.loadSettingsFrom(settings);
		classColumn.loadSettingsFrom(settings);
		epochs.loadSettingsFrom(settings);
		batchSize.loadSettingsFrom(settings);
		validationBatchSize.loadSettingsFrom(settings);
		fineTuneBert.loadSettingsFrom(settings);
		optimizer.loadSettingsFrom(settings);
		classSeparator.loadSettingsFrom(settings);
	}

	/**
	 * @return the sentenceColumn model.
	 */
	public SettingsModelString getSentenceColumnModel() {
		return sentenceColumn;
	}

	/**
	 * @return the sentence column
	 */
	public String getSentenceColumn() {
		return sentenceColumn.getStringValue();
	}

	/**
	 * @return the maxSeqLenght model.
	 */
	public SettingsModelIntegerBounded getMaxSeqLengthModel() {
		return maxSeqLength;
	}

	/**
	 * @return the max sequence length
	 */
	public int getMaxSeqLength() {
		return maxSeqLength.getIntValue();
	}

	/**
	 * @return the classColumn model
	 */
	public SettingsModelString getClassColumnModel() {
		return classColumn;
	}

	/**
	 * @return the class column
	 */
	public String getClassColumn() {
		return classColumn.getStringValue();
	}

	/**
	 * @return the epochs model
	 */
	public SettingsModelIntegerBounded getEpochsModel() {
		return epochs;
	}

	/**
	 * @return the number of training epochs
	 */
	public int getEpochs() {
		return epochs.getIntValue();
	}

	/**
	 * @return the batch size model
	 */
	public SettingsModelIntegerBounded getBatchSizeModel() {
		return batchSize;
	}

	/**
	 * @return the batch size
	 */
	public int getBatchSize() {
		return batchSize.getIntValue();
	}

	/**
	 * @return the validation table batch size model
	 */
	public SettingsModelIntegerBounded getValidationBatchSizeModel() {
		return validationBatchSize;
	}

	/**
	 * @return the validation table batch size
	 */
	public int getValidationBatchSize() {
		return validationBatchSize.getIntValue();
	}

	/**
	 * @return the fineTuneBert model
	 */
	public SettingsModelBoolean getFineTuneBertModel() {
		return fineTuneBert;
	}

	/**
	 * @return the fine tune BERT option
	 */
	public boolean getFineTuneBert() {
		return fineTuneBert.getBooleanValue();
	}

	/**
	 * @return the optimizer settings.
	 */
	public OptimizerSettings getOptimizerSettings() {
		return optimizer;
	}

	/**
	 * @return the Python representation of the current optimizer
	 */
	public String getOptimizer() {
		return optimizer.getOptimizer().getBackendRepresentation();
	}

	/**
	 * @return the classSeparator model.
	 */
	public SettingsModelString getClassSeparatorModel() {
		return classSeparator;
	}

	/**
	 * @return the class separator character to be used for multi-label
	 *         classification
	 */
	public String getClassSeparator() {
		return classSeparator.getStringValue();
	}
}

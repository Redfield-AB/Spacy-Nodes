/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.bert.setting.PythonNodeSettings;
import se.redfield.textprocessing.core.SpacyModel;

public class SpacyNodeSettings extends PythonNodeSettings {

	private static final String KEY_COLUMN = "column";
	private static final String KEY_MODEL = "model";
	private static final String KEY_LOCAL_PATH = "localPath";
	private static final String KEY_REPLACE_COLUMN = "replaceColumn";
	private static final String KEY_APPENDED_COLUMN_NAME = "appendedColumnName";

	private final SettingsModelString column;
	private SpacyModel spacyModel;
	private final SettingsModelString localModelPath;
	private final SettingsModelBoolean replaceColumn;
	private final SettingsModelString appendedColumnName;

	public SpacyNodeSettings() {
		this(true, "Processed document");
	}

	public SpacyNodeSettings(boolean defReplaceColumn, String defAppendedColumnName) {
		column = new SettingsModelString(KEY_COLUMN, "");
		spacyModel = SpacyModel.getDefault();
		localModelPath = new SettingsModelString(KEY_LOCAL_PATH, "");
		replaceColumn = new SettingsModelBoolean(KEY_REPLACE_COLUMN, defReplaceColumn);
		appendedColumnName = new SettingsModelString(KEY_APPENDED_COLUMN_NAME, defAppendedColumnName);

		appendedColumnName.setEnabled(!defReplaceColumn);
		replaceColumn.addChangeListener(e -> appendedColumnName.setEnabled(!replaceColumn.getBooleanValue()));
	}

	public SettingsModelString getColumnModel() {
		return column;
	}

	public String getColumn() {
		return column.getStringValue();
	}

	public SpacyModel getSpacyModel() {
		return spacyModel;
	}

	public void setSpacyModel(SpacyModel spacyModel) {
		this.spacyModel = spacyModel;
	}

	public String getSpacyModelPath() {
		if (spacyModel == SpacyModel.LOCAL_DIR) {
			return localModelPath.getStringValue();
		} else {
			return spacyModel.getPackageDir().getAbsolutePath();
		}
	}

	public SettingsModelString getLocalModelPathModel() {
		return localModelPath;
	}

	public SettingsModelBoolean getReplaceColumnModel() {
		return replaceColumn;
	}

	public boolean getReplaceColumn() {
		return replaceColumn.getBooleanValue();
	}

	public SettingsModelString getAppendedColumnNameModel() {
		return appendedColumnName;
	}

	public String getAppendedColumnName() {
		return appendedColumnName.getStringValue();
	}

	public String getOutputColumnName() {
		if (getReplaceColumn()) {
			return getColumn();
		} else {
			return appendedColumnName.getStringValue();
		}
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		column.saveSettingsTo(settings);
		settings.addString(KEY_MODEL, spacyModel.getPackageName());
		localModelPath.saveSettingsTo(settings);
		replaceColumn.saveSettingsTo(settings);
		appendedColumnName.saveSettingsTo(settings);
	}

	@Override
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		column.loadSettingsFrom(settings);
		spacyModel = SpacyModel.fromPackageName(settings.getString(KEY_MODEL));
		localModelPath.loadSettingsFrom(settings);
		appendedColumnName.loadSettingsFrom(settings);
		replaceColumn.loadSettingsFrom(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyNodeSettings temp = new SpacyNodeSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	public void validate() throws InvalidSettingsException {
		if (column.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Column is not selected");
		}

		if (spacyModel == SpacyModel.LOCAL_DIR && localModelPath.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Local path is not specified");
		}

		if (!replaceColumn.getBooleanValue() && appendedColumnName.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Appended column name cannot be empty");
		}
	}

}

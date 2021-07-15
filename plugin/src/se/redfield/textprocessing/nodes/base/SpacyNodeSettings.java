/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.textprocessing.core.SpacyModel;

public class SpacyNodeSettings {

	private static final String KEY_COLUMN = "column";
	private static final String KEY_MODEL = "model";
	private static final String KEY_LOCAL_PATH = "localPath";

	private final SettingsModelString column;
	private SpacyModel spacyModel;
	private final SettingsModelString localModelPath;

	public SpacyNodeSettings() {
		column = new SettingsModelString(KEY_COLUMN, "");
		spacyModel = SpacyModel.getDefault();
		localModelPath = new SettingsModelString(KEY_LOCAL_PATH, "");
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

	public void saveSettings(NodeSettingsWO settings) {
		column.saveSettingsTo(settings);
		settings.addString(KEY_MODEL, spacyModel.getPackageName());
		localModelPath.saveSettingsTo(settings);
	}

	public void loadSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		column.loadSettingsFrom(settings);
		spacyModel = SpacyModel.fromPackageName(settings.getString(KEY_MODEL));
		localModelPath.loadSettingsFrom(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyNodeSettings temp = new SpacyNodeSettings();
		temp.loadSettings(settings);
		temp.validate();
	}

	private void validate() throws InvalidSettingsException {
		if (column.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Column is not selected");
		}

		if (spacyModel == SpacyModel.LOCAL_DIR && localModelPath.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Local path is not specified");
		}
	}
}

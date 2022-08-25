/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.textprocessing.core.SpacyModel;

@Deprecated
public final class OldSpacyNodeSettings extends SpacyNodeSettings {

	private static final String KEY_MODEL = "model";
	private static final String KEY_LOCAL_PATH = "localPath";
	
	private SpacyModel spacyModel;
	private final SettingsModelString localModelPath = new SettingsModelString(KEY_LOCAL_PATH, "");
	
	public OldSpacyNodeSettings() {
		super();
	}
	
	public OldSpacyNodeSettings(boolean defReplaceColumn, String defAppendColumnName) {
		super(defReplaceColumn, defAppendColumnName);
	}
	
	public SpacyModel getSpacyModel() {
		return spacyModel;
	}
	
	public String getSpacyModelPath() {
		if (spacyModel == SpacyModel.LOCAL_DIR) {
			return localModelPath.getStringValue();
		} else {
			return spacyModel.getPackageDir().getAbsolutePath();
		}
	}
	
	public void setSpacyModel(SpacyModel spacyModel) {
		this.spacyModel = spacyModel;
	}
	
	public SettingsModelString getLocalModelPathModel() {
		return localModelPath;
	}
	
	@Override
	protected SpacyNodeSettings newInstance() {
		return new OldSpacyNodeSettings();
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		settings.addString(KEY_MODEL, spacyModel.getPackageName());
		localModelPath.saveSettingsTo(settings);
	}
	
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsFrom(settings);
		spacyModel = SpacyModel.fromPackageName(settings.getString(KEY_MODEL));
		localModelPath.loadSettingsFrom(settings);
	}
	
	@Override
	public void validate() throws InvalidSettingsException {
		super.validate();
		if (spacyModel == SpacyModel.LOCAL_DIR && localModelPath.getStringValue().isEmpty()) {
				throw new InvalidSettingsException("Local path is not specified");
		}
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.conda.prefs.CondaPreferences;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.PythonCommandConfig;

import se.redfield.textprocessing.prefs.SpacyPreferences;

/**
 * The node settings for the different SpaCy nodes.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyNodeSettings {

	private static final String KEY_COLUMN = "column";
	private static final String KEY_REPLACE_COLUMN = "replaceColumn";
	private static final String KEY_APPENDED_COLUMN_NAME = "appendedColumnName";
	private static final String KEY_PYTHON_COMMAND = "pythonCommand";

	private final SettingsModelString column;
	private final SettingsModelBoolean replaceColumn;
	private final SettingsModelString appendedColumnName;
	private final PythonCommandConfig pythonCommand;
	
	/**
	 * Default constructor
	 */
	public SpacyNodeSettings() {
		this(true, "Processed document");
	}

	/**
	 * @param defReplaceColumn      The default value of the replaceColumn option.
	 * @param defAppendedColumnName The default appended column name.
	 */
	public SpacyNodeSettings(boolean defReplaceColumn, String defAppendedColumnName) {
		column = new SettingsModelString(KEY_COLUMN, "");
		replaceColumn = new SettingsModelBoolean(KEY_REPLACE_COLUMN, defReplaceColumn);
		appendedColumnName = new SettingsModelString(KEY_APPENDED_COLUMN_NAME, defAppendedColumnName);
		pythonCommand = new PythonCommandConfig(KEY_PYTHON_COMMAND, PythonVersion.PYTHON3,
				CondaPreferences::getCondaInstallationDirectory, SpacyPreferences::getPythonCommandPreference);

		appendedColumnName.setEnabled(!defReplaceColumn);
		replaceColumn.addChangeListener(e -> appendedColumnName.setEnabled(!replaceColumn.getBooleanValue()));
	}
	
	/**
	 * @return the column settings model
	 */
	public SettingsModelString getColumnModel() {
		return column;
	}

	/**
	 * @return the selected column.
	 */
	public String getColumn() {
		return column.getStringValue();
	}

	/**
	 * @return the replaceColumn settings model.
	 */
	public SettingsModelBoolean getReplaceColumnModel() {
		return replaceColumn;
	}

	/**
	 * @return whether to replace selected column or append the new one.
	 */
	public boolean getReplaceColumn() {
		return replaceColumn.getBooleanValue();
	}

	/**
	 * @return the appendedColumnName settings model.
	 */
	public SettingsModelString getAppendedColumnNameModel() {
		return appendedColumnName;
	}

	/**
	 * @return the name for the appended column.
	 */
	public String getAppendedColumnName() {
		return appendedColumnName.getStringValue();
	}

	/**
	 * @return the name of the output column
	 */
	public String getOutputColumnName() {
		if (getReplaceColumn()) {
			return getColumn();
		} else {
			return appendedColumnName.getStringValue();
		}
	}

	/**
	 * @return the python command option.
	 */
	public PythonCommandConfig getPythonCommand() {
		return pythonCommand;
	}

	/**
	 * Saves the current settings into provided {@link NodeSettingsWO} object.
	 * 
	 * @param settings the settings object
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		column.saveSettingsTo(settings);
		replaceColumn.saveSettingsTo(settings);
		appendedColumnName.saveSettingsTo(settings);
		pythonCommand.saveSettingsTo(settings);
	}

	/**
	 * Loads the settings form the given {@link NodeSettingsRO} object.
	 * 
	 * @param settings the settings object.
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		column.loadSettingsFrom(settings);
		appendedColumnName.loadSettingsFrom(settings);
		replaceColumn.loadSettingsFrom(settings);
		pythonCommand.loadSettingsFrom(settings);
	}

	/**
	 * Validates the settings stored in the given {@link NodeSettingsRO} object.
	 * 
	 * @param settings the settings object.
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyNodeSettings temp = newInstance();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}
	
	protected SpacyNodeSettings newInstance() {
		return new SpacyNodeSettings();
	}

	/**
	 * Validates the current settings.
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		if (column.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Column is not selected");
		}

		if (!replaceColumn.getBooleanValue() && appendedColumnName.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("Appended column name cannot be empty");
		}
		
	}
	
}

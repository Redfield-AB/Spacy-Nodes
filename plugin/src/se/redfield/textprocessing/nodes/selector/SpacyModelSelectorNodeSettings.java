/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.util.EnumSet;
import java.util.function.Consumer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.connections.FSCategory;
import org.knime.filehandling.core.defaultnodesettings.EnumConfig;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filtermode.SettingsModelFilterMode.FilterMode;
import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage;

import se.redfield.textprocessing.core.model.SpacyModelDefinition;

/**
 * The SpaCy model selector node settings.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelSelectorNodeSettings {

	private static final String KEY_MODEL_DEF = "model";
	private static final String KEY_SELECTION_MODE = "selectionMode";
	private static final String KEY_LOCAL_PATH = "localPath";

	private SpacyModelDefinition modelDef;
	private SpacyModelSelectionMode selectionMode;
	private SettingsModelReaderFileChooser localPath;

	private final PortsConfiguration portsConfig;

	/**
	 * @param portsConfig the ports configuration.
	 */
	public SpacyModelSelectorNodeSettings(PortsConfiguration portsConfig) {
		this.portsConfig = portsConfig;
		modelDef = null;
		selectionMode = SpacyModelSelectionMode.SPACY;
		localPath = new SettingsModelReaderFileChooser(KEY_LOCAL_PATH, portsConfig,
				SpacyModelSelectorNodeFactory.FILE_SYSTEM_CONNECTION_PORT_NAME, EnumConfig.create(FilterMode.FOLDER),
				EnumSet.of(FSCategory.CONNECTED, FSCategory.LOCAL, FSCategory.MOUNTPOINT, FSCategory.RELATIVE));
	}

	/**
	 * @return the selected model definition
	 */
	public SpacyModelDefinition getModelDef() {
		return modelDef;
	}

	/**
	 * @param modelDef the model definition.
	 */
	public void setModelDef(SpacyModelDefinition modelDef) {
		this.modelDef = modelDef;
	}

	/**
	 * @return the model selection mode
	 */
	public SpacyModelSelectionMode getSelectionMode() {
		return selectionMode;
	}

	/**
	 * @param selectionMode the model selection mode.
	 */
	public void setSelectionMode(SpacyModelSelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	/**
	 * @return the path of the selected local model
	 */
	public SettingsModelReaderFileChooser getLocalPathModel() {
		return localPath;
	}

	/**
	 * @param settings the settings object.
	 */
	public void saveSettingsTo(NodeSettingsWO settings) {
		if (modelDef != null) {
			settings.addString(KEY_MODEL_DEF, modelDef.getId());
		}
		settings.addString(KEY_SELECTION_MODE, selectionMode.getKey());
		localPath.saveSettingsTo(settings);
	}

	/**
	 * Validates the settings stored in the given settings object.
	 * 
	 * @param settings the settings object.
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyModelSelectorNodeSettings temp = new SpacyModelSelectorNodeSettings(portsConfig);
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	/**
	 * Validates the current settings.
	 * 
	 * @throws InvalidSettingsException
	 */
	public void validate() throws InvalidSettingsException {
		if (selectionMode == SpacyModelSelectionMode.SPACY && modelDef == null) {
			throw new InvalidSettingsException("The Spacy model is not selected");
		}
	}

	/**
	 * Loads the settings from the given settings object.
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		String modelId = settings.getString(KEY_MODEL_DEF, null);
		if (modelId != null) {
			modelDef = SpacyModelDefinition.list().stream().filter(m -> m.getId().equals(modelId)).findFirst()
					.orElseThrow(() -> new InvalidSettingsException(
							"Could not find a model definition for the model: " + modelId));
		} else {
			modelDef = null;
		}
		selectionMode = SpacyModelSelectionMode.fromKey(settings.getString(KEY_SELECTION_MODE));
		localPath.loadSettingsFrom(settings);
	}

	/**
	 * @param inSpecs     the port object specs
	 * @param msgConsumer the message consumer
	 * @throws InvalidSettingsException
	 */
	public void configure(PortObjectSpec[] inSpecs, Consumer<StatusMessage> msgConsumer)
			throws InvalidSettingsException {
		if (selectionMode == SpacyModelSelectionMode.LOCAL) {
			localPath.configureInModel(inSpecs, msgConsumer);
		}
	}

	/**
	 * The model selector mode.
	 *
	 */
	public enum SpacyModelSelectionMode {
		/**
		 * The model from the official repository
		 */
		SPACY("SpaCy official model"),
		/**
		 * The model stored locally
		 */
		LOCAL("Local model");

		private String title;

		private SpacyModelSelectionMode(String title) {
			this.title = title;
		}

		/**
		 * @return the key stored in the settings
		 */
		public String getKey() {
			return name();
		}

		@Override
		public String toString() {
			return title;
		}

		/**
		 * @param key the key stored in the settings
		 * @return the selection mode object.
		 * @throws InvalidSettingsException
		 */
		public static SpacyModelSelectionMode fromKey(String key) throws InvalidSettingsException {
			try {
				return SpacyModelSelectionMode.valueOf(key);
			} catch (IllegalArgumentException e) {
				throw new InvalidSettingsException("Invalid selection mode: " + key);
			}
		}
	}
}

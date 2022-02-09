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

public class SpacyModelSelectorNodeSettings {

	private static final String KEY_MODEL_DEF = "model";
	private static final String KEY_SELECTION_MODE = "selectionMode";
	private static final String KEY_LOCAL_PATH = "localPath";

	private SpacyModelDefinition modelDef;
	private SpacyModelSelectionMode selectionMode;
	private SettingsModelReaderFileChooser localPath;

	private final PortsConfiguration portsConfig;

	public SpacyModelSelectorNodeSettings(PortsConfiguration portsConfig) {
		this.portsConfig = portsConfig;
		modelDef = null;// SpacyModelDefinition.list().get(0);
		selectionMode = SpacyModelSelectionMode.SPACY;
		localPath = new SettingsModelReaderFileChooser(KEY_LOCAL_PATH, portsConfig,
				SpacyModelSelectorNodeFactory.FILE_SYSTEM_CONNECTION_PORT_NAME, EnumConfig.create(FilterMode.FOLDER),
				EnumSet.of(FSCategory.CONNECTED, FSCategory.LOCAL, FSCategory.MOUNTPOINT, FSCategory.RELATIVE));
	}

	public SpacyModelDefinition getModelDef() {
		return modelDef;
	}

	public void setModelDef(SpacyModelDefinition modelDef) {
		this.modelDef = modelDef;
	}

	public SpacyModelSelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(SpacyModelSelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	public SettingsModelReaderFileChooser getLocalPathModel() {
		return localPath;
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_MODEL_DEF, modelDef.getId());
		settings.addString(KEY_SELECTION_MODE, selectionMode.getKey());
		localPath.saveSettingsTo(settings);
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyModelSelectorNodeSettings temp = new SpacyModelSelectorNodeSettings(portsConfig);
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	public void validate() throws InvalidSettingsException {
		if (selectionMode == SpacyModelSelectionMode.SPACY && modelDef == null) {
			throw new InvalidSettingsException("The Spacy model is not selected");
		}
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		String modelId = settings.getString(KEY_MODEL_DEF);
		modelDef = SpacyModelDefinition.list().stream().filter(m -> m.getId().equals(modelId)).findFirst().orElseThrow(
				() -> new InvalidSettingsException("Could not find a model definition for the model: " + modelId));
		selectionMode = SpacyModelSelectionMode.fromKey(settings.getString(KEY_SELECTION_MODE));
		localPath.loadSettingsFrom(settings);
	}

	public void configure(PortObjectSpec[] inSpecs, Consumer<StatusMessage> msgConsumer)
			throws InvalidSettingsException {
		localPath.configureInModel(inSpecs, msgConsumer);
	}

	public enum SpacyModelSelectionMode {
		SPACY("SpaCy official model"), LOCAL("Local model");

		private String title;

		private SpacyModelSelectionMode(String title) {
			this.title = title;
		}

		public String getKey() {
			return name();
		}

		@Override
		public String toString() {
			return title;
		}

		public static SpacyModelSelectionMode fromKey(String key) throws InvalidSettingsException {
			try {
				return SpacyModelSelectionMode.valueOf(key);
			} catch (IllegalArgumentException e) {
				throw new InvalidSettingsException("Invalid selection mode: " + key);
			}
		}
	}
}
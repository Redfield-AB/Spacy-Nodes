/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

public class SpacyModelSelectorNodeSettings {

	private static final String KEY_MODEL_DEF = "model";

	private SpacyModelDefinition modelDef;

	public SpacyModelSelectorNodeSettings() {
		modelDef = null;// SpacyModelDefinition.list().get(0);
	}

	public SpacyModelDefinition getModelDef() {
		return modelDef;
	}

	public void setModelDef(SpacyModelDefinition modelDef) {
		this.modelDef = modelDef;
	}

	public void saveSettingsTo(NodeSettingsWO settings) {
		settings.addString(KEY_MODEL_DEF, modelDef.getId());
	}

	public void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		SpacyModelSelectorNodeSettings temp = new SpacyModelSelectorNodeSettings();
		temp.loadSettingsFrom(settings);
		temp.validate();
	}

	public void validate() throws InvalidSettingsException {
		if (modelDef == null) {
			throw new InvalidSettingsException("The Spacy model is not selected");
		}
	}

	public void loadSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		String modelId = settings.getString(KEY_MODEL_DEF);
		modelDef = SpacyModelDefinition.list().stream().filter(m -> m.getId().equals(modelId)).findFirst().orElseThrow(
				() -> new InvalidSettingsException("Could not find a model definition for the model: " + modelId));
	}
}

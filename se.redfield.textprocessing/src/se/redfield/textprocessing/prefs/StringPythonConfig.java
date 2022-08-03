/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.config.PythonConfigStorage;

final class StringPythonConfig implements PythonConfig {
	
	private final SettingsModelString m_model;
	
	StringPythonConfig(final String configKey, final String defaultValue) {
		m_model = new SettingsModelString(configKey, defaultValue);
	}

	String getValue() {
		return m_model.getStringValue();
	}
	
	SettingsModelString getModel() {
		return m_model;
	}
	
	@Override
	public void saveConfigTo(PythonConfigStorage storage) {
		storage.saveStringModel(m_model);
	}

	@Override
	public void loadConfigFrom(PythonConfigStorage storage) {
		storage.loadStringModel(m_model);
	}

}

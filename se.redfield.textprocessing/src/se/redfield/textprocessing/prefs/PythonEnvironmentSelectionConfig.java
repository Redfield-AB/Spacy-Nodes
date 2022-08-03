/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.util.List;

import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.ManualEnvironmentsConfig;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.config.PythonConfigStorage;
import org.knime.python2.config.PythonEnvironmentTypeConfig;
import org.knime.python3.scripting.nodes.prefs.BundledCondaEnvironmentConfig;

final class PythonEnvironmentSelectionConfig implements PythonConfig {

	private final PythonEnvironmentTypeConfig m_environmentTypeConfig;

	private final CondaEnvironmentsConfig m_condaEnvironmentsConfig;

	private final ManualEnvironmentsConfig m_manualEnvironmentsConfig;

	private final BundledCondaEnvironmentConfig m_bundledCondaEnvironmentConfig;

	private final List<PythonConfig> m_configs;

	PythonEnvironmentSelectionConfig(final PythonEnvironmentTypeConfig envTypeConfig, CondaEnvironmentsConfig condaEnvConfig,
			ManualEnvironmentsConfig manualEnvConfig, BundledCondaEnvironmentConfig bundledEnvConfig) {
		m_environmentTypeConfig = envTypeConfig;
		m_condaEnvironmentsConfig = condaEnvConfig;
		m_manualEnvironmentsConfig = manualEnvConfig;
		m_bundledCondaEnvironmentConfig = bundledEnvConfig;
		m_configs = List.of(m_environmentTypeConfig, m_condaEnvironmentsConfig, m_manualEnvironmentsConfig,
				m_bundledCondaEnvironmentConfig);
	}
	
	List<PythonConfig> getConfigs() {
		return m_configs;
	}
	
	PythonEnvironmentTypeConfig getEnvironmentTypeConfig() {
		return m_environmentTypeConfig;
	}

	CondaEnvironmentsConfig getCondaEnvironmentsConfig() {
		return m_condaEnvironmentsConfig;
	}

	ManualEnvironmentsConfig getManualEnvironmentsConfig() {
		return m_manualEnvironmentsConfig;
	}

	BundledCondaEnvironmentConfig getBundledCondaEnvironmentConfig() {
		return m_bundledCondaEnvironmentConfig;
	}

	@Override
	public void saveConfigTo(PythonConfigStorage storage) {
		m_configs.forEach(c -> c.saveConfigTo(storage));
	}
	
	@Override
	public void saveDefaultsTo(PythonConfigStorage storage) {
		m_configs.forEach(c -> c.saveConfigTo(storage));
	}

	@Override
	public void loadConfigFrom(PythonConfigStorage storage) {
		m_configs.forEach(c -> c.loadConfigFrom(storage));
	}

}

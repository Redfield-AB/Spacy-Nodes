/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.util.List;

import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver;

final class MultiOptionEnvironmentCreator extends AbstractCondaEnvironmentCreationObserver {

	private final String m_description;

	private final String m_defaultEnvironmentName;
	
	private final List<CondaEnvironmentCreationOption> m_options;

	public MultiOptionEnvironmentCreator(String description, String defaultEnvironmentName,
			CondaEnvironmentCreationOption... options) {
		super(PythonVersion.PYTHON3);
		m_description = description;
		m_defaultEnvironmentName = defaultEnvironmentName;
		m_options = List.of(options);
	}

	String getDescription() {
		return m_description;
	}

	String getDefaultEnvironmentName() {
		return m_defaultEnvironmentName;
	}

	void startEnvironmentCreation(String environmentName, CondaEnvironmentCreationStatus status,
			CondaEnvironmentCreationOption creationOption) {
		startEnvironmentCreation(environmentName, creationOption.m_pathToEnvYml, null, status);
	}

	List<CondaEnvironmentCreationOption> getCreationOptions() {
		return m_options;
	}

	// TODO disable GPU on Mac

	static final class CondaEnvironmentCreationOption {

		private final String m_name;

		private final boolean m_enable;
		
		private final String m_pathToEnvYml;

		CondaEnvironmentCreationOption(String name, boolean enable, String pathToEnvYml) {
			m_name = name;
			m_enable = enable;
			m_pathToEnvYml = pathToEnvYml;
		}

		String getName() {
			return m_name;
		}

		boolean isEnabled() {
			return m_enable;
		}
	}

}

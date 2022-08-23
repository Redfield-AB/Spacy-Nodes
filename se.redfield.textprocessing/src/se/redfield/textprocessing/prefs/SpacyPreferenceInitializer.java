/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.knime.conda.Conda;
import org.knime.conda.prefs.CondaPreferences;
import org.knime.core.node.NodeLogger;
import org.knime.python2.config.PythonConfig;

/**
 * Preferences initializer.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyPreferenceInitializer extends AbstractPreferenceInitializer {
	@SuppressWarnings("unused")
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyPreferenceInitializer.class);

	/**
	 * The cache directory setting key
	 */
	static final String PREF_CACHE_DIR = "redfield.textprocessing.cachedir";
	
	

	@Override
	public void initializeDefaultPreferences() {
		saveToDefault(//
				SpacyPreferences.createBundledEnvConfig(), //
				SpacyPreferences.getDefaultPythonEnvironmentTypeConfig(), //
				SpacyPreferences.getDefaultCondaEnvironmentsConfig(), //
				SpacyPreferences.getDefaultManualEnvironmentsConfig(), //
				SpacyPreferences.getDefaultCacheDirConfig()//
		);
	}

	private static void saveToDefault(final PythonConfig... configs) {
		for (var config : configs) {
			config.saveConfigTo(SpacyPreferences.DEFAULT);
		}
	}

	static boolean isCondaConfigured() {
		try {
			final var condaDir = CondaPreferences.getCondaInstallationDirectory();
			final var conda = new Conda(condaDir);
			conda.testInstallation();
			return true;
		} catch (IOException ex) { // NOSONAR: we handle the exception by returning false, no need to rethrow
			return false;
		}
	}
}

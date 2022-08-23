/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.io.File;
import java.util.function.Supplier;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.knime.python2.PythonCommand;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.ManualEnvironmentsConfig;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.config.PythonConfigStorage;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.config.PythonEnvironmentTypeConfig;
import org.knime.python2.config.PythonEnvironmentsConfig;
import org.knime.python2.prefs.PreferenceStorage;
import org.knime.python2.prefs.PreferenceWrappingConfigStorage;
import org.knime.python3.scripting.nodes.prefs.BundledCondaEnvironmentConfig;

/**
 * Convenience front-end of the Spacy preferences.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class SpacyPreferences {

	private static final String PLUGIN_ID = "se.redfield.textprocessing";

	private static final PreferenceStorage DEFAULT_SCOPE_PREFS = new PreferenceStorage(PLUGIN_ID,
			DefaultScope.INSTANCE);

	private static final PreferenceStorage CURRENT_SCOPE_PREFS = new PreferenceStorage(PLUGIN_ID,
			InstanceScope.INSTANCE, DefaultScope.INSTANCE);

	/**
	 * Accessed by preference page.
	 */
	static final PythonConfigStorage CURRENT = new PreferenceWrappingConfigStorage(CURRENT_SCOPE_PREFS);

	/**
	 * Accessed by preference page and preferences initializer.
	 */
	static final PythonConfigStorage DEFAULT = new PreferenceWrappingConfigStorage(DEFAULT_SCOPE_PREFS);

	static final String BUNDLED_ENV_ID = "se_redfield_textprocessing";

	private static final String DEFAULT_CACHE_DIR = System.getProperty("java.io.tmpdir") + File.separator
			+ "spacy-cache";

	private static PythonEnvironmentType getEnvironmentTypePreference() {
		var config = createAndLoadCurrent(PythonEnvironmentTypeConfig::new);
		return PythonEnvironmentType.fromId(config.getEnvironmentType().getStringValue());
	}

	private static <T extends PythonConfig> T createAndLoadCurrent(final Supplier<T> supplier) {
		var config = supplier.get();
		config.loadConfigFrom(CURRENT);
		return config;
	}

	static BundledCondaEnvironmentConfig createBundledEnvConfig() {
		return new BundledCondaEnvironmentConfig(BUNDLED_ENV_ID);
	}

	static PythonEnvironmentTypeConfig getDefaultPythonEnvironmentTypeConfig() {
		return new PythonEnvironmentTypeConfig(PythonEnvironmentType.BUNDLED);
	}

	static CondaEnvironmentsConfig getDefaultCondaEnvironmentsConfig() {
		return new CondaEnvironmentsConfig();
	}

	static ManualEnvironmentsConfig getDefaultManualEnvironmentsConfig() {
		return new ManualEnvironmentsConfig();
	}

	static StringPythonConfig getDefaultCacheDirConfig() {
		return new StringPythonConfig("cachedir", DEFAULT_CACHE_DIR);
	}

	private static PythonEnvironmentsConfig getCurrentEnvironmentConfig() {
		var envType = getEnvironmentTypePreference();
		switch (envType) {
		case BUNDLED:
			return createAndLoadCurrent(SpacyPreferences::createBundledEnvConfig);
		case CONDA:
			return createAndLoadCurrent(CondaEnvironmentsConfig::new);
		case MANUAL:
			return createAndLoadCurrent(ManualEnvironmentsConfig::new);
		default:
			throw new IllegalStateException("Unknown environment type encountered: " + envType);
		}
	}

	/**
	 * @return the PythonCommand configured on the preference page
	 */
	public static PythonCommand getPythonCommandPreference() {
		return getCurrentEnvironmentConfig()//
				.getPython3Config()//
				.getPythonCommand();
	}

	/**
	 * @return the absolute path to the cache directory
	 */
	public static String getCacheDir() {
		return createAndLoadCurrent(SpacyPreferences::getDefaultCacheDirConfig).getValue();
	}

	private SpacyPreferences() {
	}
}

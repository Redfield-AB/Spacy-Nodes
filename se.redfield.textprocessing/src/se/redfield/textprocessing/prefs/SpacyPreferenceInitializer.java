/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.node.NodeLogger;

import se.redfield.textprocessing.SpacyPlugin;

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
	public static final String PREF_CACHE_DIR = "redfield.textprocessing.cachedir";

	private static final String DEFAULT_CACHE_DIR = System.getProperty("java.io.tmpdir") + File.separator
			+ "spacy-cache";

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SpacyPlugin.getDefault().getPreferenceStore();

		store.setDefault(PREF_CACHE_DIR, DEFAULT_CACHE_DIR);
	}

	/**
	 * @return the cache directory for the downloaded spacy models.
	 */
	public static String getCacheDir() {
		final IPreferenceStore pStore = SpacyPlugin.getDefault().getPreferenceStore();
		if (!pStore.contains(PREF_CACHE_DIR)) {
			return DEFAULT_CACHE_DIR;
		}
		return pStore.getString(PREF_CACHE_DIR);
	}
}

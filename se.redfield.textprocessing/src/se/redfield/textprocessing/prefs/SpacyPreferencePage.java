/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.knime.core.util.Version;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.config.PythonConfig;
import org.knime.python2.prefs.AbstractPythonPreferencePage;
import org.knime.python3.PythonSourceDirectoryLocator;
import org.knime.python3.scripting.nodes.prefs.BundledCondaEnvironmentConfig;
import se.redfield.textprocessing.prefs.MultiOptionEnvironmentCreator.CondaEnvironmentCreationOption;

/**
 * The preference page.
 * 
 * @author Alexander Bondaletov
 *
 */
public final class SpacyPreferencePage extends AbstractPythonPreferencePage {

	private static final String FEATURE_NAME = "Redfield NLP Nodes";

	private final MultiOptionEnvironmentCreator m_condaEnvironmentCreator = new MultiOptionEnvironmentCreator(
			FEATURE_NAME, "redfield_nlp", getCondaEnvOptions());
	
	private static CondaEnvironmentCreationOption[] getCondaEnvOptions() {
		var options = Stream.of(new CondaEnvironmentCreationOption("CPU", true, getEnvPath("cpu")));
		if (Platform.OS_LINUX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
			options = Stream.concat(options,
					Stream.of(new CondaEnvironmentCreationOption("GPU", true, getEnvPath("gpu"))));
		}
		return options.toArray(CondaEnvironmentCreationOption[]::new);
	}

	private final PythonEnvironmentSelectionConfig m_pyEnvSelectConfig = new PythonEnvironmentSelectionConfig(
			SpacyPreferences.getDefaultPythonEnvironmentTypeConfig(),
			SpacyPreferences.getDefaultCondaEnvironmentsConfig(),
			SpacyPreferences.getDefaultManualEnvironmentsConfig(),
			new BundledCondaEnvironmentConfig(SpacyPreferences.BUNDLED_ENV_ID));

	private PythonEnvironmentSelectionPanel m_pyEnvSelectPanel;

	private final StringPythonConfig m_cacheDirConfig = SpacyPreferences.getDefaultCacheDirConfig();

	/**
	 * Creates new instance.
	 */
	public SpacyPreferencePage() {
		super(SpacyPreferences.CURRENT, SpacyPreferences.DEFAULT);
	}

	@Override
	protected void populatePageBody(Composite container, List<PythonConfig> configs) {
		configs.add(m_pyEnvSelectConfig);
		var configObserver = new BundledEnvironmentConfigsObserver(m_pyEnvSelectConfig, m_condaEnvironmentCreator,
				FEATURE_NAME, List.of(//
						new PythonModuleSpec("py4j"), //
						new PythonModuleSpec("pyarrow", new Version(6, 0, 0), true), //
						new PythonModuleSpec("spacy"))//
		);
		m_pyEnvSelectPanel = new PythonEnvironmentSelectionPanel(container, m_pyEnvSelectConfig,
				m_condaEnvironmentCreator, configObserver);

		// Cache dir:
		configs.add(m_cacheDirConfig);
		addCacheDirChooser(container);
	}

	private void addCacheDirChooser(Composite container) {
		var cacheDirGroup = PreferenceUtils.createGroup(container, "Cache Directory");
		var cacheDirModel = m_cacheDirConfig.getModel();
		new DirectoryChooser(SpacyPreferenceInitializer.PREF_CACHE_DIR, "", cacheDirGroup, cacheDirModel);
	}

	private static String getEnvPath(final String tag) {
		return PythonSourceDirectoryLocator.getPathFor(SpacyPreferencePage.class, "config/spacy_" + tag + ".yml")//
				.toAbsolutePath()//
				.toString();
	}

	@Override
	protected void reflectLoadedConfigurations() {
		String warning = m_pyEnvSelectPanel.reflectLoadedConfigurations();
		setMessage(FEATURE_NAME, NONE);
		updateDisplayMinSize();
		if (warning != null) {
			setMessage(warning, WARNING);
		}
	}

	@Override
	protected void setupHooks() {
		m_pyEnvSelectConfig.getEnvironmentTypeConfig().getEnvironmentType()
				.addChangeListener(e -> setMessage(FEATURE_NAME, NONE));
		m_pyEnvSelectPanel.setupHooksAfterInitialization(this::updateDisplayMinSize);
	}
}

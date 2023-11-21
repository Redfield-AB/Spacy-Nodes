/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.knime.conda.prefs.CondaPreferences;
import org.knime.python2.PythonKernelTester.PythonKernelTestResult;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractPythonConfigsObserver.PythonConfigsInstallationTestStatusChangeListener;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.prefs.ManualEnvironmentsPreferencePanel;
import org.knime.python2.prefs.PythonBundledEnvironmentTypePreferencePanel;

final class PythonEnvironmentSelectionPanel {

	private final PythonEnvironmentSelectionConfig m_config;

	private final StackLayout m_envConfigLayout;

	private final MultiOptionEnvironmentCreator m_condaEnvironmentCreator;

	private final CustomCondaEnvironmentsPreferencePanel m_condaEnvironmentPanel;

	private final ManualEnvironmentsPreferencePanel m_manualEnvironmentPanel;

	private final Composite m_bundledCondaEnvironmentPanel;

	private final BundledEnvironmentConfigsObserver m_configObserver;

	private final IPropertyChangeListener m_condaDirPropertyChangeListener;

	PythonEnvironmentSelectionPanel(Composite parent, final PythonEnvironmentSelectionConfig config,
			final MultiOptionEnvironmentCreator envCreator, BundledEnvironmentConfigsObserver configObserver) {
		m_config = config;
		m_condaEnvironmentCreator = envCreator;
		m_configObserver = configObserver;
		m_condaDirPropertyChangeListener = event -> {
			if ("condaDirectoryPath".equals(event.getProperty()) && m_configObserver != null) {
				m_configObserver.testCurrentPreferences();
			}
		};
		final Group envConfigGroup = PreferenceUtils.createGroup(parent, "Python environment configuration");
		new PythonBundledEnvironmentTypePreferencePanel(m_config.getEnvironmentTypeConfig(), envConfigGroup,
				m_config.getBundledCondaEnvironmentConfig().isAvailable());
		final Label separator = new Label(envConfigGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Composite envConfigPanel = new Composite(envConfigGroup, SWT.NONE);
		m_envConfigLayout = new StackLayout();
		envConfigPanel.setLayout(m_envConfigLayout);
		envConfigPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		m_condaEnvironmentPanel = new CustomCondaEnvironmentsPreferencePanel(envConfigPanel,
				m_config.getCondaEnvironmentsConfig(), "Spacy", m_condaEnvironmentCreator,
				shell -> new MultiOptionCondaEnvironmentCreationDialog(m_condaEnvironmentCreator, shell, //
						m_condaEnvironmentCreator.getDescription(), //
						String.format("This will create a new preconfigured Python 3 Conda environment for the %s. "
								+ "Creating the Conda environment may take several minutes and requires "
								+ "an active internet connection.", m_condaEnvironmentCreator.getDescription()))
										.open());

		// Manual environment:
		m_manualEnvironmentPanel = new ManualEnvironmentsPreferencePanel(m_config.getManualEnvironmentsConfig(),
				envConfigPanel, false);

		// Bundled Conda environment:
		m_bundledCondaEnvironmentPanel = createBundledEnvPanel(envConfigPanel);
	}

	private Composite createBundledEnvPanel(final Composite parent) {
		final Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		var featureName = m_condaEnvironmentCreator.getDescription();
		final String bundledEnvDescription = String.format("The %s extension provides its own Python environment.\n"
				+ "If you select this option, then all its nodes that are configured to use\n"
				+ " the settings from the preference page will make use of this bundled Python environment.\n\n\n"
				+ "This bundled Python environment can not be extended.\n"
				+ "If you need additional packages for your nodes (e.g. because you use third-party models),\n"
				+ " use the \"Conda\" option above to change the environment for all %s \n"
				+ "or use the Conda Environment Propagation node to set a Conda environment for selected nodes.\n",
				featureName, featureName);

		final Label environmentSelectionLabel = new Label(panel, SWT.NONE);
		final var gridData = new GridData();
		environmentSelectionLabel.setLayoutData(gridData);
		environmentSelectionLabel.setText(bundledEnvDescription);
		return panel;
	}

	String reflectLoadedConfigurations() {
		final var envType = m_config.getEnvironmentTypeConfig().getEnvironmentType();
		var pythonEnvType = PythonEnvironmentType.fromId(envType.getStringValue());

		String warning = null;
		final var bundledEnvAvailable = m_config.getBundledCondaEnvironmentConfig().isAvailable();
		if (PythonEnvironmentType.BUNDLED == pythonEnvType && !bundledEnvAvailable) {
			warning = "You had previously selected the 'Bundled' option, but no bundled Python environment is available."
					+ " Switched to 'Conda'.";
			envType.setStringValue(PythonEnvironmentType.CONDA.getId());
		} else if (PythonEnvironmentType.CONDA == pythonEnvType //
				&& bundledEnvAvailable //
				&& !SpacyPreferenceInitializer.isCondaConfigured()) {
			warning = "You had previously selected the 'Conda' option, but Conda is not configured properly. "
					+ "Switched to 'Bundled'.";
			envType.setStringValue(PythonEnvironmentType.BUNDLED.getId());
		}

		displayPanelForEnvironmentType(envType.getStringValue());
		return warning;
	}

	private void displayPanelForEnvironmentType(final String environmentTypeId) {
		final var environmentType = PythonEnvironmentType.fromId(environmentTypeId);
		if (PythonEnvironmentType.CONDA == environmentType) {
			m_envConfigLayout.topControl = m_condaEnvironmentPanel.getPanel();
		} else if (PythonEnvironmentType.MANUAL == environmentType) {
			m_envConfigLayout.topControl = m_manualEnvironmentPanel.getPanel();
		} else if (PythonEnvironmentType.BUNDLED == environmentType) {
			m_envConfigLayout.topControl = m_bundledCondaEnvironmentPanel;
		} else {
			throw new IllegalStateException(
					"Selected Python environment type is neither Bundled, Conda, nor Manual. This is an implementation error.");
		}
	}

	void setupHooksAfterInitialization(Runnable resizeRunnable) {
		m_config.getEnvironmentTypeConfig().getEnvironmentType().addChangeListener(e -> displayPanelForEnvironmentType(
				m_config.getEnvironmentTypeConfig().getEnvironmentType().getStringValue()));

		// Displaying installation test results may require resizing the scroll view.
		m_configObserver.addConfigsTestStatusListener(new PythonConfigsInstallationTestStatusChangeListener() {

			@Override
			public void condaInstallationTestStarting() {
				resizeRunnable.run();
			}

			@Override
			public void condaInstallationTestFinished(final String errorMessage) {
				resizeRunnable.run();
			}

			@Override
			public void environmentInstallationTestStarting(final PythonEnvironmentType environmentType,
					final PythonVersion pythonVersion) {
				resizeRunnable.run();
			}

			@Override
			public void environmentInstallationTestFinished(final PythonEnvironmentType environmentType,
					final PythonVersion pythonVersion, final PythonKernelTestResult testResult) {
				resizeRunnable.run();
			}
		});

		// Trigger installation test if the Conda directory path changes
		CondaPreferences.addPropertyChangeListener(m_condaDirPropertyChangeListener);

		// Trigger initial installation test.
		m_configObserver.testCurrentPreferences();
	}

}

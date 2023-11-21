/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jan 25, 2019 (marcel): created
 */
package se.redfield.textprocessing.prefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.knime.conda.Conda;
import org.knime.conda.CondaEnvironmentIdentifier;
import org.knime.conda.prefs.CondaPreferences;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.python2.PythonCommand;
import org.knime.python2.PythonKernelTester;
import org.knime.python2.PythonKernelTester.PythonKernelTestResult;
import org.knime.python2.PythonModuleSpec;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatus;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatusListener;
import org.knime.python2.config.AbstractCondaEnvironmentsPanel;
import org.knime.python2.config.AbstractPythonConfigsObserver;
import org.knime.python2.config.BundledCondaEnvironmentConfig;
import org.knime.python2.config.CondaEnvironmentConfig;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.config.ManualEnvironmentConfig;
import org.knime.python2.config.ManualEnvironmentsConfig;
import org.knime.python2.config.PythonConfigsObserver;
import org.knime.python2.config.PythonEnvironmentConfig;
import org.knime.python2.config.PythonEnvironmentType;
import org.knime.python2.config.PythonEnvironmentTypeConfig;
import org.knime.python2.config.PythonEnvironmentsConfig;
import org.knime.python2.config.SerializerConfig;

/**
 * Specialization of the {@link PythonConfigsObserver} for nodes that come with
 * a bundled environment.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class BundledEnvironmentConfigsObserver extends AbstractPythonConfigsObserver {

	private final BundledCondaEnvironmentConfig m_bundledCondaEnvironmentConfig;

	private final String m_featureName;

	private final List<PythonModuleSpec> m_additionalRequiredModules;

	private final PythonEnvironmentTypeConfig m_environmentTypeConfig;

	private final CondaEnvironmentsConfig m_condaEnvironmentsConfig;

	private final AbstractCondaEnvironmentCreationObserver m_condaEnvironmentCreator;

	private final ManualEnvironmentsConfig m_manualEnvironmentsConfig;

	private final PythonEnvironmentSelectionConfig m_config;

	BundledEnvironmentConfigsObserver(//
			PythonEnvironmentSelectionConfig config,
			final AbstractCondaEnvironmentCreationObserver condaEnvironmentCreator, //
			String featureName, //
			Collection<PythonModuleSpec> additionalRequirements//
	) {
		m_config = config;
		m_bundledCondaEnvironmentConfig = config.getBundledCondaEnvironmentConfig();
		m_condaEnvironmentsConfig = config.getCondaEnvironmentsConfig();
		m_manualEnvironmentsConfig = config.getManualEnvironmentsConfig();
		m_environmentTypeConfig = config.getEnvironmentTypeConfig();
		m_condaEnvironmentCreator = condaEnvironmentCreator;
		m_featureName = featureName;
		m_additionalRequiredModules = List.copyOf(additionalRequirements);

		// Initialize view-model of default Python environment (since this was/is not
		// persisted):

		updateDefaultPythonEnvironment();

		// Test all environments of the respective type on environment type change:
		m_config.getEnvironmentTypeConfig().getEnvironmentType().addChangeListener(e -> {
			updateDefaultPythonEnvironment();
			testCurrentPreferences();
		});

		// Test Conda environments on change:
		m_config.getCondaEnvironmentsConfig().getPython3Config().getEnvironmentDirectory()
				.addChangeListener(e -> testPythonEnvironment(true));

		// Disable Conda environment creation by default, updated when Conda
		// installation is tested.
		condaEnvironmentCreator.getIsEnvironmentCreationEnabled().setBooleanValue(false);

		// Handle finished Conda environment creation processes:
		observeEnvironmentCreation(condaEnvironmentCreator);

		// Test manual environments on change:
		m_config.getManualEnvironmentsConfig().getPython3Config().getExecutablePath()
				.addChangeListener(e -> testPythonEnvironment(false));

		// Test required external modules of serializer on change:
	}

	/**
	 * @return The currently selected PythonEnvironmentType
	 */
	protected PythonEnvironmentType getEnvironmentType() {
		return PythonEnvironmentType.fromId(m_environmentTypeConfig.getEnvironmentType().getStringValue());
	}

	/**
	 * @return The environments of the current type. Can be overloaded to support
	 *         bundled conda environments.
	 */
	protected PythonEnvironmentsConfig getEnvironmentsOfCurrentType() {
		final var environmentType = getEnvironmentType();
		if (PythonEnvironmentType.BUNDLED == environmentType) {
			return m_bundledCondaEnvironmentConfig;
		} else if (PythonEnvironmentType.CONDA == environmentType) {
			return m_condaEnvironmentsConfig;
		} else if (PythonEnvironmentType.MANUAL == environmentType) {
			return m_manualEnvironmentsConfig;
		} else {
			throw unknownEnvType(environmentType);
		}
	}

	private static IllegalStateException unknownEnvType(final PythonEnvironmentType environmentType) {
		return new IllegalStateException("Selected environment type '" + environmentType.getName() + "' is neither "
				+ "bundled nor conda nor manual. This is an implementation error.");
	}

	private void updateDefaultPythonEnvironment() {
		if (PythonEnvironmentType.BUNDLED.equals(getEnvironmentType())) {
			// We do not configure a default environment if bundling is selected,
			// that will happen once the user selects "Conda" or "Manual" for the first
			// time.
			return;
		}

		final List<PythonEnvironmentConfig> notDefaultEnvironments = new ArrayList<>(4);
		Collections.addAll(notDefaultEnvironments, m_condaEnvironmentsConfig.getPython2Config(),
				m_condaEnvironmentsConfig.getPython3Config(), m_manualEnvironmentsConfig.getPython2Config(),
				m_manualEnvironmentsConfig.getPython3Config());

		final PythonEnvironmentsConfig environmentsOfCurrentType = getEnvironmentsOfCurrentType();
		final PythonEnvironmentConfig defaultEnvironment = environmentsOfCurrentType.getPython3Config();
		notDefaultEnvironments.remove(defaultEnvironment);

		for (final var notDefaultEnvironment : notDefaultEnvironments) {
			notDefaultEnvironment.getIsDefaultPythonEnvironment().setBooleanValue(false);
		}
		defaultEnvironment.getIsDefaultPythonEnvironment().setBooleanValue(true);
	}

	/**
	 * Initiates installation tests for all environments of the currently selected
	 * {@link PythonEnvironmentType} as well as for the currently selected
	 * serializer. Depending on the selected environment type, the status of each of
	 * these tests is published to all installation status models in either the
	 * observed {@link CondaEnvironmentConfig} or the observed
	 * {@link ManualEnvironmentConfig}. Also to the installation error model of the
	 * observed {@link SerializerConfig}.
	 */
	public void testCurrentPreferences() {
		final PythonEnvironmentType environmentType = getEnvironmentType();
		switch (environmentType) {
		case BUNDLED:
			if (!m_bundledCondaEnvironmentConfig.isAvailable()) {
				m_bundledCondaEnvironmentConfig.getPythonInstallationError()
						.setStringValue(String.format(
								"Bundled Conda environment is not available please reinstall the '%s' feature.",
								m_featureName));
			}
			break;
		case CONDA:
			refreshAndTestCondaConfig();
			break;
		case MANUAL:
			testPythonEnvironment(false);
			break;
		default:
			throw unknownEnvType(environmentType);
		}
	}

	private void refreshAndTestCondaConfig() {
		new Thread(() -> {
			final Conda conda;
			try {
				conda = testCondaInstallation();
			} catch (final Exception ex) {
				return;
			}
			final List<CondaEnvironmentIdentifier> availableEnvironments;
			try {
				availableEnvironments = getAvailableCondaEnvironments(conda, true);
			} catch (final Exception ex) {
				return;
			}

			try {
				setAvailableCondaEnvironments(availableEnvironments);
				testPythonEnvironment(true);
			} catch (Exception ex) {
				// Ignore
			}
		}).start();
	}

	private Conda testCondaInstallation() throws Exception {
		final SettingsModelString condaInfoMessage = m_condaEnvironmentsConfig.getCondaInstallationInfo();
		final SettingsModelString condaErrorMessage = m_condaEnvironmentsConfig.getCondaInstallationError();
		try {
			condaInfoMessage.setStringValue("Testing Conda installation...");
			condaErrorMessage.setStringValue("");
			onCondaInstallationTestStarting();
			final String condaDir = CondaPreferences.getCondaInstallationDirectory();
			final Conda conda = new Conda(condaDir);
			conda.testInstallation();
			String condaVersionString = conda.getVersionString();
			try {
				condaVersionString = "Conda version: "
						+ Conda.condaVersionStringToVersion(condaVersionString).toString();
			} catch (final IllegalArgumentException ex) {
				// Ignore and use raw version string.
			}
			condaInfoMessage.setStringValue("Using Conda at '" + condaDir + "'. " + condaVersionString);
			condaErrorMessage.setStringValue("");
			m_condaEnvironmentCreator.getIsEnvironmentCreationEnabled().setBooleanValue(true);
			onCondaInstallationTestFinished("");
			return conda;
		} catch (final Exception ex) {
			condaInfoMessage.setStringValue("");
			condaErrorMessage.setStringValue(ex.getMessage());
			clearAvailableCondaEnvironments();
			setCondaEnvironmentStatusMessages("", "");
			m_condaEnvironmentCreator.getIsEnvironmentCreationEnabled().setBooleanValue(false);
			onCondaInstallationTestFinished(ex.getMessage());
			throw ex;
		}
	}

	private List<CondaEnvironmentIdentifier> getAvailableCondaEnvironments(final Conda conda,
			final boolean updatePython3StatusMessage) throws Exception {
		try {
			final String determiningEnvironmentsMessage = "Collecting available environments...";
			if (updatePython3StatusMessage) {
				setCondaEnvironmentStatusMessages(determiningEnvironmentsMessage, "");
			}
			return conda.getEnvironments();
		} catch (final Exception ex) {
			m_condaEnvironmentsConfig.getCondaInstallationError().setStringValue(ex.getMessage());
			final String environmentsNotDetectedMessage = "Available environments could not be detected.";
			clearAvailableCondaEnvironments();
			setCondaEnvironmentStatusMessages("", environmentsNotDetectedMessage);
			throw ex;
		}
	}

	private void clearAvailableCondaEnvironments() {
		setAvailableCondaEnvironments(Collections.emptyList());
	}

	private void setCondaEnvironmentStatusMessages(final String infoMessage, final String errorMessage) {
		final CondaEnvironmentConfig condaEnvironmentConfig = m_condaEnvironmentsConfig.getPython3Config();
		condaEnvironmentConfig.getPythonInstallationInfo().setStringValue(infoMessage);
		condaEnvironmentConfig.getPythonInstallationError().setStringValue(errorMessage);
	}

	private void setAvailableCondaEnvironments(List<CondaEnvironmentIdentifier> availableEnvironments) {
		final CondaEnvironmentConfig condaConfig = m_condaEnvironmentsConfig.getPython3Config();
		if (availableEnvironments.isEmpty()) {
			availableEnvironments = Arrays.asList(CondaEnvironmentIdentifier.PLACEHOLDER_CONDA_ENV);
		}
		condaConfig.getAvailableEnvironments()
				.setValue(availableEnvironments.toArray(new CondaEnvironmentIdentifier[0]));
		final String currentlySelectedEnvironment = condaConfig.getEnvironmentDirectory().getStringValue();
		if (availableEnvironments.stream()
				.noneMatch(env -> Objects.equals(env.getDirectoryPath(), currentlySelectedEnvironment))) {
			condaConfig.getEnvironmentDirectory().setStringValue(availableEnvironments.get(0).getDirectoryPath());
		}
	}

	private void testPythonEnvironment(final boolean isConda) {
		final PythonEnvironmentsConfig environmentsConfig;
		final PythonEnvironmentType environmentType;
		boolean isCondaPlaceholder = false;
		if (isConda) {
			if (isPlaceholderEnvironmentSelected()) {
				// We don't want to test the placeholder but just clear the installation status
				// messages and return.
				isCondaPlaceholder = true;
			}
			environmentsConfig = m_condaEnvironmentsConfig;
			environmentType = PythonEnvironmentType.CONDA;
		} else {
			environmentsConfig = m_manualEnvironmentsConfig;
			environmentType = PythonEnvironmentType.MANUAL;
		}
		final PythonEnvironmentConfig environmentConfig;
		final PythonVersion pythonVersion;
		environmentConfig = environmentsConfig.getPython3Config();
		pythonVersion = PythonVersion.PYTHON3;
		final SettingsModelString infoMessage = environmentConfig.getPythonInstallationInfo();
		final SettingsModelString errorMessage = environmentConfig.getPythonInstallationError();
		final String environmentCreationInfo;
		if (isConda) {
			environmentCreationInfo = String.format(
					"\nNote: You can create a new %s Conda environment that contains all packages\nrequired by "
							+ "the %s by clicking the '%s' button\nabove.",
					pythonVersion.getName(), m_featureName,
					AbstractCondaEnvironmentsPanel.CREATE_NEW_ENVIRONMENT_BUTTON_TEXT);
		} else {
			environmentCreationInfo = String.format(
					"\nNote: An easy way to create a new Conda environment that contains all packages\n"
							+ "required by the %s can be found on the '%s' tab of this preference page.",
					m_featureName, PythonEnvironmentType.CONDA.getName());
		}
		if (isCondaPlaceholder) {
			infoMessage.setStringValue("");
			errorMessage.setStringValue("No environment available. Please create a new one to be able to use "
					+ pythonVersion.getName() + "." + environmentCreationInfo);
			return;
		}
		final Collection<PythonModuleSpec> requiredSerializerModules = getAdditionalRequiredModules();
		infoMessage.setStringValue("Testing " + pythonVersion.getName() + " environment...");
		errorMessage.setStringValue("");
		new Thread(() -> {
			onEnvironmentInstallationTestStarting(environmentType, pythonVersion);
			final PythonCommand pythonCommand = environmentConfig.getPythonCommand();
			final PythonKernelTestResult testResult = PythonKernelTester.testPython3Installation(pythonCommand,
					requiredSerializerModules, true);
			infoMessage.setStringValue(testResult.getVersion());
			String errorLog = testResult.getErrorLog();
			if (errorLog != null && !errorLog.isEmpty()) {
				errorLog += environmentCreationInfo;
			}
			errorMessage.setStringValue(errorLog);
			onEnvironmentInstallationTestFinished(environmentType, pythonVersion, testResult);
		}).start();
	}

	/**
	 * Provides any additional modules that are required for the environment to be
	 * valid. By default these are the requirements of the selected serialization
	 * library.
	 *
	 * @return any additional modules that have to be present
	 */
	protected Collection<PythonModuleSpec> getAdditionalRequiredModules() {
		return m_additionalRequiredModules;
	}

	private boolean isPlaceholderEnvironmentSelected() {
		final SettingsModelString condaEnvironmentDirectory = m_condaEnvironmentsConfig.getPython3Config()
				.getEnvironmentDirectory();
		return CondaEnvironmentIdentifier.PLACEHOLDER_CONDA_ENV.getDirectoryPath()
				.equals(condaEnvironmentDirectory.getStringValue());
	}

	private void observeEnvironmentCreation(final AbstractCondaEnvironmentCreationObserver creationStatus) {
		creationStatus.addEnvironmentCreationStatusListener(new CondaEnvironmentCreationStatusListener() {

			@Override
			public void condaEnvironmentCreationStarting(final CondaEnvironmentCreationStatus status) {
				// no-op
			}

			@Override
			public void condaEnvironmentCreationFinished(final CondaEnvironmentCreationStatus status,
					final CondaEnvironmentIdentifier createdEnvironment) {
				final Conda conda;
				try {
					conda = testCondaInstallation();
				} catch (final Exception ex) {
					return;
				}
				final List<CondaEnvironmentIdentifier> availableEnvironments;
				try {
					availableEnvironments = getAvailableCondaEnvironments(conda, true);
				} catch (final Exception ex) {
					return;
				}
				try {
					setAvailableCondaEnvironments(availableEnvironments);
					final CondaEnvironmentConfig environmentConfig = m_condaEnvironmentsConfig.getPython3Config();
					environmentConfig.getEnvironmentDirectory().setStringValue(createdEnvironment.getDirectoryPath());
					testPythonEnvironment(true);
				} catch (Exception ex) {
					// Ignore, we still want to configure and test the second environment.
				}
			}

			@Override
			public void condaEnvironmentCreationCanceled(final CondaEnvironmentCreationStatus status) {
				// no-op
			}

			@Override
			public void condaEnvironmentCreationFailed(final CondaEnvironmentCreationStatus status,
					final String errorMessage) {
				// no-op
			}
		}, false);
	}
}

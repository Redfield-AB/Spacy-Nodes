/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.knime.python2.PythonVersion;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver;
import org.knime.python2.config.CondaEnvironmentsConfig;
import org.knime.python2.prefs.CondaDirectoryPathStatusPanel;
import org.knime.python2.prefs.CondaEnvironmentSelectionBox;

final class CustomCondaEnvironmentsPreferencePanel {

	private final Composite m_panel;

	private final CondaEnvironmentsConfig m_config;

	CustomCondaEnvironmentsPreferencePanel(final Composite parent, CondaEnvironmentsConfig config,
			String envConsumerName, AbstractCondaEnvironmentCreationObserver environmentCreator,
			Consumer<Shell> openCreationDialog) {
		m_panel = new Composite(parent, SWT.NONE);
		m_panel.setLayout(new GridLayout());
		m_config = config;
		addCondaDirPathStatusWidget();
		addPythonEnvWidget(envConsumerName, environmentCreator, openCreationDialog);
	}

	private void addCondaDirPathStatusWidget() {
		final CondaDirectoryPathStatusPanel statusPanel = new CondaDirectoryPathStatusPanel(
				m_config.getCondaInstallationInfo(), m_config.getCondaInstallationError(), m_panel);
		final GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		statusPanel.setLayoutData(gridData);
	}

	private void addPythonEnvWidget(String envConsumerName, AbstractCondaEnvironmentCreationObserver envCreator,
			Consumer<Shell> openCreationDialog) {
		var envConfig = m_config.getPython3Config();
		var envSelectionBox = new CondaEnvironmentSelectionBox(//
				PythonVersion.PYTHON3, //
				envConfig.getEnvironmentDirectory(), //
				envConfig.getAvailableEnvironments(), //
				envConsumerName, //
				"Name of the " + envConsumerName + " Conda environment", //
				envConfig.getPythonInstallationInfo(), //
				envConfig.getPythonInstallationWarning(), //
				envConfig.getPythonInstallationError(), //
				envCreator, //
				m_panel, //
				openCreationDialog//
		);
		final GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
//        gridData.horizontalIndent = 20;
        gridData.grabExcessHorizontalSpace = true;
        envSelectionBox.setLayoutData(gridData);
	}

	Control getPanel() {
		return m_panel;
	}

}

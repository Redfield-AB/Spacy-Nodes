/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import static org.knime.python2.prefs.PythonPreferenceUtils.performActionOnWidgetInUiThread;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.event.ChangeEvent;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.knime.conda.CondaEnvironmentIdentifier;
import org.knime.python2.config.CondaEnvironmentCreationDialog;

import se.redfield.textprocessing.prefs.MultiOptionEnvironmentCreator.CondaEnvironmentCreationOption;

import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatus;
import org.knime.python2.config.AbstractCondaEnvironmentCreationObserver.CondaEnvironmentCreationStatusListener;

final class MultiOptionCondaEnvironmentCreationDialog extends Dialog implements CondaEnvironmentCreationDialog {
	
	private static final String ENVIRONMENT_NAME_PLACEHOLDER = "Collecting existing environment names...";

	private static final int DESCRIPTION_LABEL_WIDTH_HINTS = 300;

	private final MultiOptionEnvironmentCreator m_environmentCreator;

	// UI components: Initialized by #createContents().

	private final Shell m_shell;

	private Text m_environmentNameTextBox;

	private Label m_statusLabel;

	private StackLayout m_progressBarStackLayout;

	private ProgressBar m_indeterminateProgressBar;

	private ProgressBar m_determinateProgressBar;

	private Label m_errorTextBoxLabel;

	private Composite m_errorTextBoxContainer;

	private Text m_errorTextBox;

	private Button m_cancelButton;
	
	private final Map<Button, CondaEnvironmentCreationOption> m_buttonMap = new LinkedHashMap<>();

	/**
	 * Initialized when the {@link #switchToStartingOrRetryingState() create button
	 * is clicked}.
	 */
	private volatile CondaEnvironmentCreationStatus m_status;

	/**
	 * Initialized by {@link #registerExternalHooks()}.
	 */
	private CondaEnvironmentCreationStatusListener m_statusChangeListener;

	private volatile boolean m_environmentCreationTerminated = false;
	
	public MultiOptionCondaEnvironmentCreationDialog(final MultiOptionEnvironmentCreator environmentCreator,
        final Shell parent, String name, String description) {
        super(parent, SWT.NONE);
        m_environmentCreator = environmentCreator;
        m_shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.SHEET | SWT.RESIZE);
        m_shell.setText(String.format("New %s Conda environment", name));
        createContents(description);
        m_shell.pack();
    }

	private void createContents(String description) {
		m_shell.setLayout(new GridLayout());
		final Label descriptionText = new Label(m_shell, SWT.WRAP);
		descriptionText.setText(description);
		descriptionText.setFont(JFaceResources.getFontRegistry().getItalic(""));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = DESCRIPTION_LABEL_WIDTH_HINTS;
		descriptionText.setLayoutData(gridData);

		final Label separator = new Label(m_shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Composite environmentNameContainer = new Composite(m_shell, SWT.NONE);
		environmentNameContainer.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		environmentNameContainer.setLayout(new GridLayout(2, false));

		// Environment name:

		final Label environmentNameTextBoxLabel = new Label(environmentNameContainer, SWT.NONE);
		environmentNameTextBoxLabel.setText("New environment's name");
		environmentNameTextBoxLabel.setLayoutData(new GridData());

		m_environmentNameTextBox = new Text(environmentNameContainer, SWT.BORDER);
		m_environmentNameTextBox.setEnabled(false);
		m_environmentNameTextBox.setText(ENVIRONMENT_NAME_PLACEHOLDER);
		m_environmentNameTextBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// Progress monitoring widgets:

		final Composite installationMonitorContainer = new Composite(m_shell, SWT.NONE);
		installationMonitorContainer.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		installationMonitorContainer.setLayout(new GridLayout());

		m_statusLabel = new Label(installationMonitorContainer, SWT.WRAP);
		m_statusLabel.setText("Please click one of the create buttons to start. "
				+ "You can specify a custom environment name using the text field above before starting.");
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.widthHint = DESCRIPTION_LABEL_WIDTH_HINTS;
		gridData.verticalIndent = 10;
		m_statusLabel.setLayoutData(gridData);

		final Composite progressBarContainer = new Composite(installationMonitorContainer, SWT.NONE);
		progressBarContainer.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		m_progressBarStackLayout = new StackLayout();
		progressBarContainer.setLayout(m_progressBarStackLayout);
		m_determinateProgressBar = new ProgressBar(progressBarContainer, SWT.SMOOTH);
		m_indeterminateProgressBar = new ProgressBar(progressBarContainer, SWT.SMOOTH | SWT.INDETERMINATE);
		m_progressBarStackLayout.topControl = m_determinateProgressBar;

		m_errorTextBoxLabel = new Label(installationMonitorContainer, SWT.NONE);
		m_errorTextBoxLabel.setText("Conda error log");
		gridData = new GridData();
		gridData.verticalIndent = 10;
		m_errorTextBoxLabel.setLayoutData(gridData);

		m_errorTextBoxContainer = new Composite(installationMonitorContainer, SWT.NONE);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 80;
		m_errorTextBoxContainer.setLayoutData(gridData);
		m_errorTextBoxContainer.setLayout(new FillLayout());
		m_errorTextBox = new Text(m_errorTextBoxContainer,
				SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		final Color red = new Color(m_errorTextBoxContainer.getDisplay(), 255, 0, 0);
		m_errorTextBox.setForeground(red);
		m_errorTextBox.addDisposeListener(e -> red.dispose());

		m_determinateProgressBar.setEnabled(false);
		m_errorTextBox.setEnabled(false);

		// Hide error log initially.
		m_errorTextBoxLabel.setVisible(false);
		((GridData) m_errorTextBoxLabel.getLayoutData()).exclude = true;
		m_errorTextBoxContainer.setVisible(false);
		((GridData) m_errorTextBoxContainer.getLayoutData()).exclude = true;

		// --

		final Composite buttonContainer = new Composite(m_shell, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.verticalIndent = 15;
		buttonContainer.setLayoutData(gridData);
		buttonContainer.setLayout(new RowLayout());
		
		for (var option : m_environmentCreator.getCreationOptions()) {
			var button = new Button(buttonContainer, SWT.NONE);
			button.setText(String.format("Create new %s environment", option.getName()));
			button.setEnabled(false);
			button.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					switchToStartingOrRetryingState(option);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				
			});
			m_buttonMap.put(button, option);
		}
		
		m_cancelButton = new Button(buttonContainer, SWT.NONE);
		m_cancelButton.setText("Cancel");

		// Initialize environment name: we're calling Conda here, so we should do this
		// asynchronously.
		new Thread(() -> {
			final String defaultEnvironmentName = m_environmentCreator.getDefaultEnvironmentName();
			switchToDefaultEnvironmentNameAvailableState(defaultEnvironmentName);
		}).start();

		// Internal hooks:


		m_cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (m_status != null && !m_environmentCreationTerminated) {
					m_environmentCreator.cancelEnvironmentCreation(m_status);
					// Shell will be closed by the environment status listener's cancellation
					// handler.
				} else {
					m_shell.close();
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				widgetSelected(e);
			}
		});
		m_shell.addShellListener(new ShellListener() {

			@Override
			public void shellIconified(final ShellEvent e) {
				// no-op
			}

			@Override
			public void shellDeiconified(final ShellEvent e) {
				// no-op
			}

			@Override
			public void shellDeactivated(final ShellEvent e) {
				// no-op
			}

			@Override
			public void shellClosed(final ShellEvent e) {
				if (m_status != null && !m_environmentCreationTerminated) {
					m_environmentCreator.cancelEnvironmentCreation(m_status);
					// Shell will be closed by the environment status listener's cancellation/finish
					// handlers.
					e.doit = false;
				}
			}

			@Override
			public void shellActivated(final ShellEvent e) {
				// no-op
			}
		});
	}

	public void open() {
		try {
			m_shell.open();
			final Display display = getParent().getDisplay();
			while (!m_shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} finally {
			unregisterExternalHooks();
		}
	}

	/**
	 * May be followed by starting state.
	 */
	private void switchToDefaultEnvironmentNameAvailableState(final String defaultEnvironmentName) {
		performActionOnWidgetInUiThread(m_environmentNameTextBox, () -> {
			m_environmentNameTextBox.setText(defaultEnvironmentName);
			m_environmentNameTextBox.setEnabled(true);
			m_environmentNameTextBox.setFocus();
			m_environmentNameTextBox.getParent().layout();
			for (var entry : m_buttonMap.entrySet()) {
				entry.getKey().setEnabled(entry.getValue().isEnabled());
			}
			return null;
		}, false);
	}

	/**
	 * May be followed by finished, canceled, or failed state.
	 */
	private void switchToStartingOrRetryingState(CondaEnvironmentCreationOption creationOption) {
		// If retrying.
		m_environmentCreationTerminated = false;
		m_statusLabel.setForeground(null);
		m_errorTextBox.setText("");
		unregisterExternalHooks();

		m_environmentNameTextBox.setEnabled(false);
		m_determinateProgressBar.setEnabled(true);
		m_progressBarStackLayout.topControl = m_indeterminateProgressBar;
		m_errorTextBox.setEnabled(true);
		m_buttonMap.keySet().forEach(b -> b.setEnabled(false));
		String environmentName = m_environmentNameTextBox.getText();
		if (environmentName.isEmpty() || environmentName.equals(ENVIRONMENT_NAME_PLACEHOLDER)) {
			// While the creator can handle empty environments names, we should also sync
			// the dialog to let the
			// user know which name we chose to overrule their empty text field input.
			environmentName = m_environmentCreator.getDefaultEnvironmentName();
			m_environmentNameTextBox.setText(environmentName);
		}
		m_shell.layout(true, true);
		m_status = new CondaEnvironmentCreationStatus();
		registerExternalHooks();
		m_environmentCreator.startEnvironmentCreation(environmentName, m_status, creationOption);
	}

	/**
	 * May be followed by starting state ("retry") or no state (terminal state).
	 */
	private void switchToFailedState() {
		m_environmentCreationTerminated = true;
		performActionOnWidgetInUiThread(m_shell, () -> {
			if (!m_statusLabel.isDisposed()) {
				final Color red = new Color(m_statusLabel.getDisplay(), 255, 0, 0);
				m_statusLabel.setForeground(red);
				m_statusLabel.addDisposeListener(e -> red.dispose());
			}
			if (!m_determinateProgressBar.isDisposed()) {
				m_determinateProgressBar.setSelection(0);
				m_determinateProgressBar.setEnabled(false);
				m_progressBarStackLayout.topControl = m_determinateProgressBar;
			}
			if (!m_indeterminateProgressBar.isDisposed()) {
				m_indeterminateProgressBar.setEnabled(false);
			}
			// Prepare for retry.
			if (!m_environmentNameTextBox.isDisposed()) {
				m_environmentNameTextBox.setEnabled(true);
			}
			
			for (var entry : m_buttonMap.entrySet()) {
				var button = entry.getKey();
				if (!button.isDisposed()) {
					var option = entry.getValue();
					button.setText(String.format("Retry creating new %s environment", option.getName()));
					button.setEnabled(option.isEnabled());
				}
			}
			
			m_shell.layout(true, true);
			return null;
		}, false);
	}

	/**
	 * Terminal state.
	 */
	private void switchToFinishedState() {
		m_environmentCreationTerminated = true;
		performActionOnWidgetInUiThread(m_shell, () -> {
			m_shell.close();
			return null;
		}, false);
	}

	/**
	 * Terminal state.
	 */
	private void switchToCanceledState() {
		switchToFinishedState();
	}

	private void registerExternalHooks() {
		m_status.getStatusMessage().addChangeListener(this::updateStatusMessage);
		m_status.getProgress().addChangeListener(this::updateProgress);
		m_status.getErrorLog().addChangeListener(this::updateErrorLog);
		m_statusChangeListener = new CondaEnvironmentCreationStatusListener() {

			@Override
			public void condaEnvironmentCreationStarting(final CondaEnvironmentCreationStatus status) {
				// no-op
			}

			@Override
			public void condaEnvironmentCreationFinished(final CondaEnvironmentCreationStatus status,
					final CondaEnvironmentIdentifier createdEnvironment) {
				if (status == m_status) {
					switchToFinishedState();
				}
			}

			@Override
			public void condaEnvironmentCreationCanceled(final CondaEnvironmentCreationStatus status) {
				if (status == m_status) {
					switchToCanceledState();
				}
			}

			@Override
			public void condaEnvironmentCreationFailed(final CondaEnvironmentCreationStatus status,
					final String errorMessage) {
				if (status == m_status) {
					switchToFailedState();
				}
			}
		};
		// Prepend to close dialog before installation tests on the preference page are
		// triggered.
		m_environmentCreator.addEnvironmentCreationStatusListener(m_statusChangeListener, true);
	}

	private void unregisterExternalHooks() {
		if (m_status != null) {
			m_status.getStatusMessage().removeChangeListener(this::updateStatusMessage);
			m_status.getProgress().removeChangeListener(this::updateProgress);
			m_status.getErrorLog().removeChangeListener(this::updateErrorLog);
		}
		m_environmentCreator.removeEnvironmentCreationStatusListener(m_statusChangeListener);
	}

	private void updateStatusMessage(@SuppressWarnings("unused") final ChangeEvent e) {
		performActionOnWidgetInUiThread(m_statusLabel, () -> {
			m_statusLabel.setText(m_status.getStatusMessage().getStringValue());
			m_statusLabel.requestLayout();
			return null;
		}, true);
	}

	private void updateProgress(@SuppressWarnings("unused") final ChangeEvent e) {
		final int progress = m_status.getProgress().getIntValue();
		Control newVisibleProgressBar;
		if (progress < 100) {
			performActionOnWidgetInUiThread(m_determinateProgressBar, () -> {
				m_determinateProgressBar.setSelection(progress);
				m_determinateProgressBar.requestLayout();
				return null;
			}, true);
			newVisibleProgressBar = m_determinateProgressBar;
		} else {
			newVisibleProgressBar = m_indeterminateProgressBar;
		}
		if (m_progressBarStackLayout.topControl != newVisibleProgressBar) {
			m_progressBarStackLayout.topControl = newVisibleProgressBar;
			performActionOnWidgetInUiThread(m_shell, () -> {
				m_shell.layout(true, true);
				return null;
			}, true);
		}
	}

	private void updateErrorLog(@SuppressWarnings("unused") final ChangeEvent e) {
		performActionOnWidgetInUiThread(m_shell, () -> {
			if (!m_errorTextBox.isDisposed()) {
				m_errorTextBox.setText(m_status.getErrorLog().getStringValue());
				m_errorTextBox.requestLayout();
				if (!m_errorTextBoxContainer.isDisposed() && !m_errorTextBoxContainer.getVisible()) {
					// Show error log if it is hidden (which is the initial state).
					if (!m_errorTextBoxLabel.isDisposed()) {
						m_errorTextBoxLabel.setVisible(true);
						((GridData) m_errorTextBoxLabel.getLayoutData()).exclude = false;
					}
					m_errorTextBoxContainer.setVisible(true);
					final GridData errorTextBoxContainerGridData = (GridData) m_errorTextBoxContainer.getLayoutData();
					errorTextBoxContainerGridData.exclude = false;
					m_shell.layout(true, true);
					// Manually resize shell to show error log as if it wasn't hidden in the first
					// place.
					final Point oldSize = m_shell.getSize();
					final Point newSize = m_shell.computeSize(oldSize.x,
							oldSize.y + errorTextBoxContainerGridData.heightHint, true);
					m_shell.setSize(newSize);
				}
			}
			return null;
		}, true);
	}
}

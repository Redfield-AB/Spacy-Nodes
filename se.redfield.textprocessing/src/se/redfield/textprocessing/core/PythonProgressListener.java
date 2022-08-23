/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.node.ExecutionMonitor;
import org.knime.python2.kernel.PythonOutputListener;

/**
 * {@link PythonOutputListener} intended to track the progress of the script
 * execution. It is done by parsing the output looking for decimal numbers
 * followed by the '%' character.
 * 
 * @author Alexander Bondaletov
 *
 */
public class PythonProgressListener implements PythonOutputListener {
	private static final Pattern PATTERN = Pattern.compile("(\\d+\\.?\\d*)\\%");

	private ExecutionMonitor monitor;
	private boolean disabled = false;

	/**
	 * Sets the monitor to report progress to.
	 * 
	 * @param monitor The execution monitor.
	 */
	public void setMonitor(ExecutionMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public void messageReceived(String message, boolean isWarningMessage) {
		if (monitor != null && !disabled) {
			Matcher m = PATTERN.matcher(message);
			if (m.find()) {
				try {
					double progress = Double.parseDouble(m.group(1));
					if (progress > 0 && progress <= 100) {
						monitor.setProgress(progress / 100.0);
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

}

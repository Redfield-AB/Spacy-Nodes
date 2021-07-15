/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.Arrays;
import java.util.Collections;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.prefs.DLPythonPreferences;
import org.knime.python2.PythonCommand;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;

import com.google.common.base.Strings;

public class PythonContext implements AutoCloseable {

	public static final String VAR_INPUT_TABLE = "input_table";
	public static final String VAR_OUTPUT_TABLE = "output_table";

	private final PythonKernel kernel;

	public PythonContext() throws DLInvalidEnvironmentException {
		kernel = createKernel();
	}

	private static PythonKernel createKernel() throws DLInvalidEnvironmentException {
		PythonKernelOptions options = getKernelOptions();
		PythonCommand command = getCommand();
		try {
			PythonKernel kernel = PythonKernelQueue.getNextKernel(command, Collections.emptySet(),
					Collections.emptySet(), options, PythonCancelable.NOT_CANCELABLE);
			return kernel;
		} catch (PythonIOException e) {
			final String msg = !Strings.isNullOrEmpty(e.getMessage())
					? "An error occurred while trying to launch Python: " + e.getMessage()
					: "An unknown error occurred while trying to launch Python. See log for details.";
			throw new DLInvalidEnvironmentException(msg, e);
		} catch (PythonCanceledExecutionException e) {
			throw new IllegalStateException("Implementation error", e);
		}
	}

	private static PythonKernelOptions getKernelOptions() {
		return new PythonKernelOptions().forAddedAdditionalRequiredModuleNames(Arrays.asList("spacy"));
	}

	private static PythonCommand getCommand() {
		return DLPythonPreferences.getPythonTF2CommandPreference();
	}

	@Override
	public void close() throws Exception {
		kernel.close();
	}

	public void putDataTable(BufferedDataTable table, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.putDataTable(VAR_INPUT_TABLE, table, exec);
	}

	public BufferedDataTable getDataTable(ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		return getDataTable(VAR_OUTPUT_TABLE, exec, monitor);
	}

	public BufferedDataTable getDataTable(String name, ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		return kernel.getDataTable(name, exec, monitor);
	}

	public void executeInKernel(String code, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.execute(code, new PythonExecutionMonitorCancelable(exec));
	}
}

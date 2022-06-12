/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.dl.core.DLInvalidEnvironmentException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.PythonCommand;
import org.knime.python2.kernel.PythonCancelable;
import org.knime.python2.kernel.PythonCanceledExecutionException;
import org.knime.python2.kernel.PythonExecutionMonitorCancelable;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernel;
import org.knime.python2.kernel.PythonKernelBackendRegistry.PythonKernelBackendType;
import org.knime.python2.kernel.PythonKernelOptions;
import org.knime.python2.kernel.PythonKernelQueue;
import org.knime.python3.PythonSourceDirectoryLocator;

public class PythonContext implements AutoCloseable {

	private static final String KNIO_INPUT_TABLE = "knio.input_tables[%d]";
	private static final String KNIO_OUTPUT_TABLE = "knio.output_tables[%d]";

	private final PythonKernel kernel;

	public PythonContext(PythonCommand command, int numOutputTables) throws DLInvalidEnvironmentException {
		kernel = createKernel(command);
		kernel.setExpectedOutputTables(new String[numOutputTables]);
	}

	private static PythonKernel createKernel(PythonCommand command) throws DLInvalidEnvironmentException {
		PythonKernelOptions options = getKernelOptions();
		try {
			PythonKernel kernel = PythonKernelQueue.getNextKernel(command, PythonKernelBackendType.PYTHON3,
					Collections.emptySet(), Collections.emptySet(), options, PythonCancelable.NOT_CANCELABLE);
			kernel.execute("import knime_io as knio");
			kernel.execute(setupPythonPath());
			return kernel;
		} catch (PythonIOException e) {
			final String msg = e.getMessage() != null && e.getMessage().isEmpty()
					? "An error occurred while trying to launch Python: " + e.getMessage()
					: "An unknown error occurred while trying to launch Python. See log for details.";
			throw new DLInvalidEnvironmentException(msg, e);
		} catch (PythonCanceledExecutionException e) {
			throw new IllegalStateException("Implementation error", e);
		}
	}

	private static String setupPythonPath() {
		DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("import sys");
		Path path = PythonSourceDirectoryLocator.getPathFor(PythonContext.class, "py");
		b.a("sys.path.append(").asr(path.toString()).a(")").n();
		return b.toString();
	}

	private static PythonKernelOptions getKernelOptions() {
		return new PythonKernelOptions().forAddedAdditionalRequiredModuleNames(Arrays.asList("spacy"));
	}

	@Override
	public void close() throws Exception {
		kernel.close();
	}

	public void putDataTable(int idx, BufferedDataTable table, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		String name = String.format(KNIO_INPUT_TABLE, idx);
		kernel.putDataTable(name, table, exec);
	}

	public BufferedDataTable getDataTable(ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		return getDataTable(0, exec, monitor);
	}

	public BufferedDataTable getDataTable(int idx, ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		String name = String.format(KNIO_OUTPUT_TABLE, idx);
		return kernel.getDataTable(name, exec, monitor);
	}

	public void executeInKernel(String code, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.execute(code, new PythonExecutionMonitorCancelable(exec));
	}

	public static void putInputTableArgs(DLPythonSourceCodeBuilder b, String argName, int idx) {
		String tableName = String.format(KNIO_INPUT_TABLE, idx);
		b.a(argName).a(" = ").a(tableName).a(".to_pyarrow(),").n();
	}
}

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

/**
 * Gateway class for interacting with the python code.
 * 
 * @author Alexander Bondaletov
 *
 */
public class PythonContext implements AutoCloseable {

	private static final String KNIO_INPUT_TABLE = "knio.input_tables[%d]";
	private static final String KNIO_OUTPUT_TABLE = "knio.output_tables[%d]";

	private final PythonKernel kernel;

	/**
	 * @param command         The python command.
	 * @param numOutputTables The expected number of output tables.
	 * @throws DLInvalidEnvironmentException
	 */
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

	/**
	 * Puts provided data table into the python context.
	 * 
	 * @param idx   The table index.
	 * @param table The table.
	 * @param exec  The execution monitor.
	 * @throws PythonIOException
	 * @throws CanceledExecutionException
	 */
	public void putDataTable(int idx, BufferedDataTable table, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		String name = String.format(KNIO_INPUT_TABLE, idx);
		kernel.putDataTable(name, table, exec);
	}

	/**
	 * Gets the data table from the python context.
	 * 
	 * @param exec    Execution context.
	 * @param monitor Execution monitor.
	 * @return the data table.
	 * @throws PythonIOException
	 * @throws CanceledExecutionException
	 */
	public BufferedDataTable getDataTable(ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		return getDataTable(0, exec, monitor);
	}

	/**
	 * Gets the data table with the given index from the python context.
	 * 
	 * @param idx     The table index.
	 * @param exec    Execution context.
	 * @param monitor Execution monitor.
	 * @return The data table.
	 * @throws PythonIOException
	 * @throws CanceledExecutionException
	 */
	public BufferedDataTable getDataTable(int idx, ExecutionContext exec, ExecutionMonitor monitor)
			throws PythonIOException, CanceledExecutionException {
		String name = String.format(KNIO_OUTPUT_TABLE, idx);
		return kernel.getDataTable(name, exec, monitor);
	}

	/**
	 * Executes the given python code.
	 * 
	 * @param code The python code.
	 * @param exec Execution monitor.
	 * @throws PythonIOException
	 * @throws CanceledExecutionException
	 */
	public void executeInKernel(String code, ExecutionMonitor exec)
			throws PythonIOException, CanceledExecutionException {
		kernel.execute(code, new PythonExecutionMonitorCancelable(exec));
	}

	/**
	 * Puts input table argument into provided code builder.
	 * 
	 * @param b       The code builder.
	 * @param argName The python function argument name.
	 * @param idx     The table index.
	 */
	public static void putInputTableArgs(DLPythonSourceCodeBuilder b, String argName, int idx) {
		String tableName = String.format(KNIO_INPUT_TABLE, idx);
		b.a(argName).a(" = ").a(tableName).a(".to_pyarrow(),").n();
	}
}

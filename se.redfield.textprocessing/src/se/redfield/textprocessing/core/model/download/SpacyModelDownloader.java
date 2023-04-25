/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.dl.python.util.DLPythonSourceCodeBuilder;
import org.knime.dl.python.util.DLPythonUtils;
import org.knime.python2.kernel.PythonIOException;
import org.knime.python2.kernel.PythonKernelCleanupException;

import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.model.SpacyModelDescription;
import se.redfield.textprocessing.prefs.SpacyPreferences;

/**
 * Abstract class for downloading SpaCy models.
 * 
 * @author Alexander Bondaletov
 *
 */
public abstract class SpacyModelDownloader {

	/**
	 * Checks if the model is already downloaded and downloads it, if necessary.
	 * 
	 * @param exec Execution monitor
	 * @throws IOException
	 * @throws InvalidSettingsException
	 * @throws CanceledExecutionException
	 */
	public void ensureDownloaded(ExecutionMonitor exec)
			throws IOException, InvalidSettingsException, CanceledExecutionException {
		if (!isDownloaded(exec.createSilentSubProgress(0))) {
			download(exec);
			validateModel(exec.createSilentSubProgress(0));
		}
	}

	private boolean isDownloaded(ExecutionMonitor exec)
			throws InvalidSettingsException, PythonKernelCleanupException, CanceledExecutionException {
		if (Files.isDirectory(getModelDownloadDir().toPath())) {
			try {
				validateModel(exec);
				return true;
			} catch (PythonIOException e) {
				return false;
			}
		} else {
			return false;
		}
	}

	private void validateModel(ExecutionMonitor exec) throws PythonKernelCleanupException, PythonIOException,
			CanceledExecutionException, InvalidSettingsException {
		try (PythonContext ctx = new PythonContext(SpacyPreferences.getPythonCommandPreference(), 0)) {
			DLPythonSourceCodeBuilder b = DLPythonUtils.createSourceCodeBuilder("import spacy");
			b.a("spacy.load(").asr(getModelDownloadDir().getAbsolutePath()).a(")");
			ctx.executeInKernel(b.toString(), exec);
		}
	}

	protected abstract File getModelDownloadDir() throws InvalidSettingsException;

	protected abstract void download(ExecutionMonitor exec)
			throws IOException, InvalidSettingsException, CanceledExecutionException;

	/**
	 * 
	 * @param configure Whether the call is performed during the configure stage.
	 * @return The spacy model description.
	 * @throws InvalidSettingsException
	 */
	public abstract SpacyModelDescription getModelDescription(boolean configure) throws InvalidSettingsException;
}

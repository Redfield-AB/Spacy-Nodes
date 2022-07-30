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

import se.redfield.textprocessing.core.model.SpacyModelDescription;

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
		if (!Files.isDirectory(getModelDownloadDir().toPath())) {
			download(exec);
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

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;

import se.redfield.textprocessing.core.model.SpacyModelDescription;

public abstract class SpacyModelDownloader {

	public void ensureDownloaded(ExecutionMonitor exec) throws IOException, InvalidSettingsException {
		if (!Files.isDirectory(getModelDownloadDir().toPath())) {
			download(exec);
		}
	}

	protected abstract File getModelDownloadDir() throws InvalidSettingsException;

	protected abstract void download(ExecutionMonitor exec) throws IOException, InvalidSettingsException;

	public abstract SpacyModelDescription getModelDescription(boolean configure) throws InvalidSettingsException;
}

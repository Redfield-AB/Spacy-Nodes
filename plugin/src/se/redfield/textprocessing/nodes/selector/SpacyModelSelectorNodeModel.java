/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import se.redfield.textprocessing.nodes.port.SpacyModelDescription;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObject;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObjectSpec;
import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

public class SpacyModelSelectorNodeModel extends NodeModel {

	private final SpacyModelSelectorNodeSettings settings = new SpacyModelSelectorNodeSettings();

	protected SpacyModelSelectorNodeModel() {
		super(new PortType[] {}, new PortType[] { SpacyModelPortObject.TYPE });
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.validate();
		return new PortObjectSpec[] { createSpec() };
	}

	private SpacyModelPortObjectSpec createSpec() {
		SpacyModelDescription model = new SpacyModelDescription(
				settings.getModelDef().getModelDownloadDir().getAbsolutePath());
		return new SpacyModelPortObjectSpec(model);
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		ensureModelDownloaded(exec);
		return new PortObject[] { new SpacyModelPortObject(createSpec()) };
	}

	private void ensureModelDownloaded(ExecutionContext exec) throws IOException {
		if (!Files.isDirectory(settings.getModelDef().getModelDownloadDir().toPath())) {
			downloadModel(exec);
		}
		exec.setProgress(1);
	}

	private void downloadModel(ExecutionContext exec) throws IOException {
		File cacheDir = new File(SpacyPreferenceInitializer.getCacheDir());
		File archive = new File(cacheDir, settings.getModelDef().getId() + ".tar.gz");

		exec.setProgress("Downloading the model");
		FileUtils.copyURLToFile(new URL(settings.getModelDef().getUrl()), archive);
		exec.setProgress("Unpacking the model");

		String packagePrefix = findPackagePrefix(archive);

		try (InputStream fi = Files.newInputStream(archive.toPath());
				BufferedInputStream bi = new BufferedInputStream(fi);
				GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
				TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().startsWith(packagePrefix)) {
					String name = settings.getModelDef().getId() + "/"
							+ entry.getName().substring(packagePrefix.length() + 1);
					Path newPath = cacheDir.toPath().resolve(name);

					Path parent = newPath.getParent();
					if (parent != null && Files.notExists(parent)) {
						Files.createDirectories(parent);
					}

					Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		Files.delete(archive.toPath());
	}

	private static String findPackagePrefix(File archive) throws IOException {
		try (InputStream fi = Files.newInputStream(archive.toPath());
				BufferedInputStream bi = new BufferedInputStream(fi);
				GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
				TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				if (entry.getName().endsWith("config.cfg")) {
					return entry.getName().substring(0, entry.getName().lastIndexOf('/'));
				}
			}
		}
		throw new IOException("config.cfg is not found in the archive");
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettingsFrom(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void reset() {
		// nothing to do
	}

}

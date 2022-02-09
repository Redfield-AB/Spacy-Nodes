/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

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
import org.knime.core.node.ExecutionMonitor;

import se.redfield.textprocessing.core.model.SpacyModelDefinition;
import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

public class RepositoryModelDownloader extends SpacyModelDownloader {

	private SpacyModelDefinition def;

	public RepositoryModelDownloader(SpacyModelDefinition def) {
		this.def = def;
	}

	@Override
	protected File getModelDownloadDir() {
		return def.getModelDownloadDir();
	}

	@Override
	protected void download(ExecutionMonitor exec) throws IOException {
		File cacheDir = new File(SpacyPreferenceInitializer.getCacheDir());
		File archive = new File(cacheDir, def.getId() + ".tar.gz");

		exec.setProgress("Downloading the model");
		FileUtils.copyURLToFile(new URL(def.getUrl()), archive);
		exec.setProgress("Unpacking the model");

		String packagePrefix = findPackagePrefix(archive);

		try (InputStream fi = Files.newInputStream(archive.toPath());
				BufferedInputStream bi = new BufferedInputStream(fi);
				GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
				TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().startsWith(packagePrefix)) {
					String name = def.getId() + "/" + entry.getName().substring(packagePrefix.length() + 1);
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
}

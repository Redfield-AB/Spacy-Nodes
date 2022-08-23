/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

import se.redfield.textprocessing.core.model.SpacyModelDefinition;
import se.redfield.textprocessing.core.model.SpacyModelDescription;
import se.redfield.textprocessing.prefs.SpacyPreferences;

/**
 * {@link SpacyModelDownloader} for downloading models from the official SpaCy
 * repository.
 * 
 * @author Alexander Bondaletov
 *
 */
public class RepositoryModelDownloader extends SpacyModelDownloader {
	private static final int BUFFER_SIZE = 10 * 1024;// 10 KB
	private static final int TIMEOUT = 10 * 1000; // 10s

	private SpacyModelDefinition def;

	/**
	 * @param def The SpaCy model definition.
	 */
	public RepositoryModelDownloader(SpacyModelDefinition def) {
		this.def = def;
	}

	@Override
	public SpacyModelDescription getModelDescription(boolean configure) {
		return new SpacyModelDescription(getModelDownloadDir().getAbsolutePath(), def.getFeatures());
	}

	@Override
	protected File getModelDownloadDir() {
		return def.getModelDownloadDir();
	}

	@Override
	protected void download(ExecutionMonitor exec) throws IOException, CanceledExecutionException {
		File archive = downloadArchive(exec);
		unpackArchive(archive);
	}

	private File downloadArchive(ExecutionMonitor exec) throws IOException, CanceledExecutionException {
		File cacheDir = new File(SpacyPreferences.getCacheDir());
		Files.createDirectories(cacheDir.toPath());
		File archive = new File(cacheDir, def.getId() + ".tar.gz");

		URL url = new URL(def.getUrl());
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(TIMEOUT);
		connection.setReadTimeout(TIMEOUT);

		byte[] buffer = new byte[BUFFER_SIZE];
		long contentLength = connection.getContentLengthLong();
		long readCount = 0;

		try (InputStream in = connection.getInputStream(); //
				OutputStream out = new FileOutputStream(archive)) {
			int n;
			while ((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);

				readCount += n;
				exec.checkCanceled();
				exec.setProgress((double) readCount / contentLength);
			}
		}

		return archive;
	}

	private void unpackArchive(File archive) throws IOException {
		File cacheDir = new File(SpacyPreferences.getCacheDir());
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

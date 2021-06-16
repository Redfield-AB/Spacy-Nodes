/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.knime.core.node.NodeLogger;
import org.knime.dl.util.DLUtils;

import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

public class SpacyModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyModel.class);

	private static final String MODELS_FILE = "config/spacy_models.csv";
	private static final String DEFAULT_MODEL = "en_core_web_sm";

	public static final SpacyModel LOCAL_DIR = new SpacyModel("Load from local directory...", "local-dir", "") {
		@Override
		public void ensureDownloaded() throws IOException {
		}
	};

	private static Map<String, SpacyModel> models;

	private final String title;
	private final String packageName;
	private final String url;

	private SpacyModel(String title, String packageName, String url) {
		this.title = title;
		this.packageName = packageName;
		this.url = url;
	}

	public String getPackageName() {
		return packageName;
	}

	public void ensureDownloaded() throws IOException {
		if (!isDownloaded()) {
			download();
		}
	}

	public File getPackageDir() {
		File cacheDir = new File(SpacyPreferenceInitializer.getCacheDir());
		return new File(cacheDir, packageName);
	}

	private boolean isDownloaded() {
		return Files.isDirectory(getPackageDir().toPath());
	}

	private void download() throws IOException {
		File cacheDir = new File(SpacyPreferenceInitializer.getCacheDir());
		File archive = new File(cacheDir, packageName + ".tar.gz");
		FileUtils.copyURLToFile(new URL(url), archive);

		String packagePrefix = findPackagePrefix(archive);

		try (InputStream fi = Files.newInputStream(archive.toPath());
				BufferedInputStream bi = new BufferedInputStream(fi);
				GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
				TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().startsWith(packagePrefix)) {
					String name = packageName + "/" + entry.getName().substring(packagePrefix.length() + 1);
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

	private String findPackagePrefix(File archive) throws IOException {
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
	public String toString() {
		return title;
	}

	public static SpacyModel fromPackageName(String packageName) {
		if (models == null) {
			loadModels();
		}
		return models.get(packageName);
	}

	public static Collection<SpacyModel> list() {
		ArrayList<SpacyModel> list = new ArrayList<>(models.values());
		list.sort((m1, m2) -> {
			if (m1 == LOCAL_DIR) {
				return Integer.MIN_VALUE;
			} else if (m2 == LOCAL_DIR) {
				return Integer.MAX_VALUE;
			} else {
				return m1.title.compareTo(m2.title);
			}
		});
		return list;
	}

	public static SpacyModel getDefault() {
		return fromPackageName(DEFAULT_MODEL);
	}

	private static void loadModels() {
		models = new HashMap<>();
		try {
			List<String> lines = Files
					.readAllLines(DLUtils.Files.getFileFromSameBundle(SpacyModel.class, MODELS_FILE).toPath());

			for (String line : lines) {
				String[] parts = line.split(";");
				String title = parts[0];
				String packageName = parts[1];
				String url = parts[2];

				SpacyModel m = new SpacyModel(title, packageName, url);
				models.put(packageName, m);
			}

			models.put(LOCAL_DIR.packageName, LOCAL_DIR);
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
}

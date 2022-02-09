/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.knime.core.node.NodeLogger;
import org.knime.dl.util.DLUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

public class SpacyModelDefinition {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyModelDefinition.class);
	private static final String MODELS_FILE = "config/spacy-models.json";

	private static List<SpacyModelDefinition> models;

	private String name;
	private String size;
	private String version;
	private String lang;
	private String url;
	private String[] components;

	public String getName() {
		return name;
	}

	public String getSize() {
		return size;
	}

	public String getVersion() {
		return version;
	}

	public String getLang() {
		return lang;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return name + "-" + version;
	}

	public File getModelDownloadDir() {
		File cacheDir = new File(SpacyPreferenceInitializer.getCacheDir());
		return new File(cacheDir, getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SpacyModelDefinition) {
			return getId().equals(((SpacyModelDefinition) obj).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getId());
	}

	public static List<SpacyModelDefinition> list() {
		if (models == null) {
			try {
				models = loadModels();
				models.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
				models = Collections.emptyList();
			}
		}
		return models;
	}

	private static List<SpacyModelDefinition> loadModels() throws IOException {
		Gson gson = new GsonBuilder().create();
		try (FileReader reader = new FileReader(
				DLUtils.Files.getFileFromSameBundle(SpacyModelDefinition.class, MODELS_FILE))) {
			Type typeOf = new TypeToken<List<SpacyModelDefinition>>() {
			}.getType();
			return gson.fromJson(reader, typeOf);
		}
	}

}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.knime.python3.PythonSourceDirectoryLocator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

/**
 * Class holding different information about available official SpaCy models.
 * The list of models is stored in the config file.
 * 
 * @author Alexander Bondaletov
 *
 */
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
	private Set<SpacyFeature> features;

	/**
	 * @return The model name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The model size.
	 */
	public String getSize() {
		return size;
	}

	/**
	 * @return The SpaCy version required for the model.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return The model language.
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @return The model download URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return The model unique identifier. Constructed from the model name and
	 *         version.
	 */
	public String getId() {
		return name + "-" + version;
	}

	/**
	 * @return The set of features supported by the model.
	 */
	public Set<SpacyFeature> getFeatures() {
		if (features == null) {
			features = SpacyFeature.fromPipeline(components);
		}
		return features;
	}

	/**
	 * @return The directory the model is (or will be) downloaded.
	 */
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

	/**
	 * @return The list of available SpaCy models.
	 */
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
		var path = PythonSourceDirectoryLocator.getPathFor(SpacyModelDefinition.class, MODELS_FILE);
		try (var reader = Files.newBufferedReader(path)) {
			Type typeOf = new TypeToken<List<SpacyModelDefinition>>() {
			}.getType();
			return gson.fromJson(reader, typeOf);
		}
	}

}

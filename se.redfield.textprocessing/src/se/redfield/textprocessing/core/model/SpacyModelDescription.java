/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

/**
 * Class for storing different information about the SpaCy model passed through
 * the port object.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelDescription {

	private static final String KEY_PATH = "path";
	private static final String KEY_FEATURES = "features";

	private String path;
	private Set<SpacyFeature> features;

	/**
	 * Default constructor.
	 */
	public SpacyModelDescription() {
		this("", null);
	}

	/**
	 * @param path     The model path.
	 * @param features The model features.
	 */
	public SpacyModelDescription(String path, Set<SpacyFeature> features) {
		this.path = path;
		this.features = features;
	}

	/**
	 * Saves the data into provided model.
	 * 
	 * @param model To save to.
	 */
	public void save(ModelContentWO model) {
		model.addString(KEY_PATH, path);
		if (features != null) {
			model.addStringArray(KEY_FEATURES,
					features.stream().map(SpacyFeature::name).collect(Collectors.toList()).toArray(new String[] {}));
		}
	}

	/**
	 * Loads the data from the provided model.
	 * 
	 * @param model To load from.
	 */
	public void load(ModelContentRO model) {
		path = model.getString(KEY_PATH, "");
		String[] arr = model.getStringArray(KEY_FEATURES, (String[]) null);
		if (arr != null) {
			features = Arrays.stream(arr).map(SpacyFeature::valueOf).collect(Collectors.toSet());
		} else {
			features = null;
		}
	}
	
	/**
	 * @return The model download path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return The set of features supported by the model.
	 */
	public Set<SpacyFeature> getFeatures() {
		return features;
	}
	
	/**
	 * @return the name of the model
	 */
	public String getName() {
		return Path.of(path).getFileName().toString();
	}
}

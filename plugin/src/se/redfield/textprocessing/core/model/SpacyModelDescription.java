/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

public class SpacyModelDescription {

	private static final String KEY_PATH = "path";
	private static final String KEY_FEATURES = "features";

	private String path;
	private Set<SpacyFeature> features;

	public SpacyModelDescription() {
		this("", null);
	}

	public SpacyModelDescription(String path, Set<SpacyFeature> features) {
		this.path = path;
		this.features = features;
	}

	public void save(ModelContentWO model) {
		model.addString(KEY_PATH, path);
		if (features != null) {
			model.addStringArray(KEY_FEATURES,
					features.stream().map(SpacyFeature::name).collect(Collectors.toList()).toArray(new String[] {}));
		}
	}

	public void load(ModelContentRO model) {
		path = model.getString(KEY_PATH, "");
		String[] arr = model.getStringArray(KEY_FEATURES, null);
		if (arr != null) {
			features = Arrays.stream(arr).map(SpacyFeature::valueOf).collect(Collectors.toSet());
		} else {
			features = null;
		}
	}

	public String getPath() {
		return path;
	}

	public Set<SpacyFeature> getFeatures() {
		return features;
	}
}

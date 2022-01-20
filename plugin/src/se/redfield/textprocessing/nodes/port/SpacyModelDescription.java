/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

public class SpacyModelDescription {

	private static final String KEY_PATH = "path";

	private String path;

	public SpacyModelDescription() {
		this("");
	}

	public SpacyModelDescription(String path) {
		this.path = path;
	}

	public void save(ModelContentWO model) {
		model.addString(KEY_PATH, path);
	}

	public void load(ModelContentRO model) {
		path = model.getString(KEY_PATH, "");
	}

	public String getPath() {
		return path;
	}
}

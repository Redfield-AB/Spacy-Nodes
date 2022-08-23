/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The class representing the SpaCy model data fetched from the meta.json file.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelMeta {

	private String[] components;

	/**
	 * @return The pipeline components.
	 */
	public String[] getComponents() {
		return components;
	}

	/**
	 * @param reader The reader to read meta.json from.
	 * @return The meta object
	 */
	public static SpacyModelMeta read(Reader reader) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(reader, SpacyModelMeta.class);
	}
}

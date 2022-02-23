/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SpacyModelMeta {

	private String[] components;

	public String[] getComponents() {
		return components;
	}

	public static SpacyModelMeta read(Reader reader) {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(reader, SpacyModelMeta.class);
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SpacyDocument {

	private SpacySentence[] sentences;

	public SpacySentence[] getSentences() {
		return sentences;
	}

	public static SpacyDocument fromJson(String json) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, SpacyDocument.class);
	}
}

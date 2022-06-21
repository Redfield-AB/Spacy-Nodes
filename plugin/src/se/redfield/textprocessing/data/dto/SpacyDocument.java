/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The result of processing document by SpaCy.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyDocument {

	private SpacySentence[] sentences;

	/**
	 * @return The sentences.
	 */
	public SpacySentence[] getSentences() {
		return sentences;
	}

	/**
	 * Deserealizes the {@link SpacyDocument} object from the provided json string.
	 * 
	 * @param json The json string.
	 * @return The document object.
	 * @throws JsonProcessingException
	 */
	public static SpacyDocument fromJson(String json) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, SpacyDocument.class);
	}
}

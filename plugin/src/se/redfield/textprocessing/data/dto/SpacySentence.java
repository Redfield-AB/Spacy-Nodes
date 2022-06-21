/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import java.util.Arrays;

/**
 * The class representing a sentence from the SpaCy processing result.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacySentence {

	private SpacyWord[] words;

	/**
	 * @return The list of words.
	 */
	public SpacyWord[] getWords() {
		return words;
	}

	@Override
	public String toString() {
		return Arrays.toString(words);
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import java.util.Arrays;

public class SpacySentence {

	private SpacyWord[] words;

	public SpacyWord[] getWords() {
		return words;
	}

	@Override
	public String toString() {
		return Arrays.toString(words);
	}
}

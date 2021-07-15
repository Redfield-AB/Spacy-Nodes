/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

public class SpacyWord {

	private String text;
	private String lemma;
	private String tag;

	public String getText() {
		return text;
	}

	public String getLemma() {
		return lemma;
	}

	public String getLemmaOrText() {
		if (lemma == null || lemma.isEmpty()) {
			return text;
		}
		return lemma;
	}

	public String getTag() {
		return tag;
	}
}

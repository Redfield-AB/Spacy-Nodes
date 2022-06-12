/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import java.util.List;

public class SpacyWord {

	private String text;
	private String lemma;
	private String tag;
	private String entity;
	private int iob;
	private List<String> morph;

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

	public String getEntity() {
		return entity;
	}

	public int getIob() {
		return iob;
	}

	public List<String> getMorph() {
		return morph;
	}

	@Override
	public String toString() {
		return text;
	}
}

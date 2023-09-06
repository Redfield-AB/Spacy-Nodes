/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.dto;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.knime.ext.textprocessing.data.Word;

/**
 * The word with the associated tags.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyWord {

	private String text;
	private String lemma;
	private String tag;
	private String entity;
	private int iob;
	private List<String> morph;

	/**
	 * @return The word text.
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return The lemma.
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * @return The lemma (if available) or the word text.
	 */
	public String getLemmaOrText() {
		if (lemma == null || lemma.isEmpty()) {
			return text;
		}
		return lemma;
	}

	/**
	 * @return The POS tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @return The NER entity.
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * @return The iob(inside-outside-boundary) attribute of the NER entity.
	 */
	public int getIob() {
		return iob;
	}

	/**
	 * @return The list of morphology tags.
	 */
	public List<String> getMorph() {
		return morph;
	}

	@Override
	public String toString() {
		return text;
	}

	/**
	 * Tests whether the text of this word is equal to the text of the provided
	 * {@link Word} object. Any two blank string are considered to be equal in this
	 * case.
	 * 
	 * @param word The word to test.
	 * @return <code>true</code> if the words are the same.
	 */
	public boolean isSame(Word word) {
		if (text == null) {
			return word.getText() == null;
		}

		return text.equals(word.getText()) || StringUtils.isAllBlank(text, word.getText());
	}
}

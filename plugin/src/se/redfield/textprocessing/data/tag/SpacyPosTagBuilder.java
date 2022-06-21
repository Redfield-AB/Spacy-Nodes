/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

/**
 * Tag builder for the SpaCy POS tags.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyPosTagBuilder extends ResourceFileTagBuilder {

	private static final SpacyPosTagBuilder instance = new SpacyPosTagBuilder();

	/**
	 * @return the tag builder instance.
	 */
	public static SpacyPosTagBuilder getInstance() {
		return instance;
	}

	/**
	 * Creates new instance.
	 */
	public SpacyPosTagBuilder() {
		super("SPACY_POS", "config/pos_tags.txt");
	}

}

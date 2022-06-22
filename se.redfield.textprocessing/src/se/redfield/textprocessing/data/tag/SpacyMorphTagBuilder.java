/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

/**
 * Tag builder for the Spacy morphology tags. Tags are loaded from the config
 * file.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyMorphTagBuilder extends ResourceFileTagBuilder {
	private static final String TAG_TYPE = "SPACY_MORPH";
	private static final String TAGS_FILE = "config/morph_tags.txt";

	private static final SpacyMorphTagBuilder instance = new SpacyMorphTagBuilder();

	/**
	 * @return The tag builder instance.
	 */
	public static SpacyMorphTagBuilder getInstance() {
		return instance;
	}

	/**
	 * Creates new instance
	 */
	public SpacyMorphTagBuilder() {
		super(TAG_TYPE, TAGS_FILE);
	}

}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

public class SpacyNerTagBuilder extends ResourceFileTagBuilder {

	private static final SpacyNerTagBuilder instance = new SpacyNerTagBuilder();

	/**
	 * @return The tag builder instance.
	 */
	public static SpacyNerTagBuilder getInstance() {
		return instance;
	}

	public SpacyNerTagBuilder() {
		super("SPACY_NE", "config/ner_tags.txt");
	}

}

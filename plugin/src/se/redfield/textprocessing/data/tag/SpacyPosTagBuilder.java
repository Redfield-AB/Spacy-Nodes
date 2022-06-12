/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

public class SpacyPosTagBuilder extends ResourceFileTagBuilder {

	private static final SpacyPosTagBuilder instance = new SpacyPosTagBuilder();

	public static SpacyPosTagBuilder getInstance() {
		return instance;
	}

	public SpacyPosTagBuilder() {
		super("SPACY_POS", "config/pos_tags.txt");
	}

}

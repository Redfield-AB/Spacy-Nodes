/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum SpacyFeature {
	LEMMATIZATION("Lemmatization", "lemmatizer"), //
	MORPHOLOGY("Morphology", "morphologizer"), //
	NER("NER", "ner"), //
	POS("POS", "tagger", "morphologizer"), //
	TOKENIZATION("Tonenization"), //
	VECTORIZATION("Vectorization", "tok2vec", "transformer");

	private String title;
	private Set<String> components;

	private SpacyFeature(String title, String... components) {
		this.title = title;
		this.components = new HashSet<>(Arrays.asList(components));
	}

	@Override
	public String toString() {
		return title;
	}

	private boolean check(String[] pipeline) {
		return components.isEmpty() || Arrays.stream(pipeline).anyMatch(s -> components.contains(s));
	}

	public static Set<SpacyFeature> fromPipeline(String[] pipeline) {
		return Arrays.stream(SpacyFeature.values()).filter(f -> f.check(pipeline)).collect(Collectors.toSet());
	}
}

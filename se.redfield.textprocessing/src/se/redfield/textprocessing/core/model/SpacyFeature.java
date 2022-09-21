/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enum representing different SpaCy model capabilities.
 * 
 * @author Alexander Bondaletov
 *
 */
public enum SpacyFeature {
	/**
	 * Lemmatization
	 */
	LEMMATIZATION("Lemmatization", "lemmatizer"),
	/**
	 * Morphology
	 */
	MORPHOLOGY("Morphology", "morphologizer"),
	/**
	 * Named Entity Recognition
	 */
	NER("NER", "ner"),
	/**
	 * Part-of-Speech tagging
	 */
	POS("POS", "tagger", "morphologizer"),
	/**
	 * Tokenization
	 */
	TOKENIZATION("Tonenization"),
	/**
	 * Vectorizaton
	 */
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

	/**
	 * @param pipeline The model pipeline components.
	 * @return The set of supported features
	 */
	public static Set<SpacyFeature> fromPipeline(String[] pipeline) {
		return Arrays.stream(SpacyFeature.values()).filter(f -> f.check(pipeline)).collect(Collectors.toSet());
	}
}

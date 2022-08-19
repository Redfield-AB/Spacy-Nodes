/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Set;

import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.tag.TagSet;

/**
 * The {@link TagBuilder} implementation for the dynamically created tag set.
 * Tag type is assigned to be unique among the already registered dynamic tag
 * sets.
 * 
 * @author Alexander Bondaletov
 *
 */
public class DynamicTagBuilder extends GenericTagBuilder {
	/**
	 * @param registeredTagSets Tag sets already registered.
	 * @param tags              The list of tags.
	 * @param modelName         The name of the model that produced the tags
	 * @param tagType           The type of tag (e.g. POS)
	 */
	public DynamicTagBuilder(Set<TagSet> registeredTagSets, Set<String> tags, String modelName, String tagType) {
		super(String.format("SPACY_%s_%s", tagType, modelName), tags);
	}

}

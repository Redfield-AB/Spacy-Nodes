/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.tag.TagSet;

public class DynamicTagBuilder extends GenericTagBuilder {
	private static final String TAG_TYPE_PREFIX = "SPACY_";

	public DynamicTagBuilder(Set<TagSet> registeredTagSets, Set<String> tags) {
		super(buildTagType(registeredTagSets), tags);
	}

	private static String buildTagType(Set<TagSet> registeredTagSets) {
		Set<String> registeredNames = registeredTagSets.stream().map(TagSet::getType).collect(Collectors.toSet());

		int idx = 0;
		String type = null;
		do {
			type = TAG_TYPE_PREFIX + idx++;
		} while (registeredNames.contains(type));

		return type;
	}

}

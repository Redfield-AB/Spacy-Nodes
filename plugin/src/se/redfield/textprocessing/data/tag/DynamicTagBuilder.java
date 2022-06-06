/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.tag.TagSet;

public class DynamicTagBuilder implements TagBuilder {
	private static final String TAG_TYPE_PREFIX = "SPACY_";

	private String type;
	private Map<String, Tag> tags;

	public DynamicTagBuilder(Set<TagSet> registeredTagSets, Set<String> tags) {
		Set<String> registeredNames = registeredTagSets.stream().map(TagSet::getType).collect(Collectors.toSet());

		int idx = 0;
		do {
			type = TAG_TYPE_PREFIX + idx++;
		} while (registeredNames.contains(type));

		this.tags = tags.stream().collect(Collectors.toMap(val -> val, val -> new Tag(val, type)));
	}

	@Override
	public Tag buildTag(String value) {
		Tag tag = tags.get(value);
		if (tag == null) {
			throw new IllegalStateException(value + " not found in " + Arrays.toString(tags.values().toArray()));
		}
		return tag;
	}

	@Override
	public List<String> asStringList() {
		return tags.values().stream().map(Tag::getTagValue).collect(Collectors.toList());
	}

	@Override
	public Set<Tag> getTags() {
		return new HashSet<>(tags.values());
	}

	@Override
	public String getType() {
		return type;
	}
}

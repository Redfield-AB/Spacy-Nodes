/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.tag.TagSet;

public class TagSetTagBuilder implements TagBuilder {
	private TagSet tagSet;
	private Map<String, Tag> tags;

	public TagSetTagBuilder(TagSet tagSet) {
		this.tagSet = tagSet;
		tags = tagSet.getTags().stream().collect(Collectors.toMap(Tag::getTagValue, t -> t));
	}

	@Override
	public Tag buildTag(String value) {
		return tags.get(value);
	}

	@Override
	public List<String> asStringList() {
		return tagSet.asStringList();
	}

	@Override
	public Set<Tag> getTags() {
		return tagSet.getTags();
	}

	@Override
	public String getType() {
		return tagSet.getType();
	}
}
/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

public class SpacyNerTagBuilder implements TagBuilder {

	public SpacyNerTagBuilder() {
		// default constructor
	}

	@Override
	public Tag buildTag(String value) {
		return SpacyNerTag.fromString(value).getTag();
	}

	@Override
	public List<String> asStringList() {
		return Arrays.stream(SpacyNerTag.values()).map(t -> t.getTag().getTagValue()).collect(Collectors.toList());
	}

	@Override
	public Set<Tag> getTags() {
		return Arrays.stream(SpacyNerTag.values()).map(SpacyNerTag::getTag).collect(Collectors.toSet());
	}

	@Override
	public String getType() {
		return SpacyNerTag.TAG_TYPE;
	}

}

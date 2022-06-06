/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

public class SpacyPosTagBuilder implements TagBuilder {

	private static final SpacyPosTagBuilder instance = new SpacyPosTagBuilder();

	public static SpacyPosTagBuilder getInstance() {
		return instance;
	}

	@Override
	public Tag buildTag(String value) {
		Tag tag = PartOfSpeechTag.stringToTag(value);
		if (!tag.equals(PartOfSpeechTag.UNKNOWN.getTag())) {
			return tag;
		}
		return null;
	}

	@Override
	public List<String> asStringList() {
		return PartOfSpeechTag.getDefault().asStringList();
	}

	@Override
	public Set<Tag> getTags() {
		return PartOfSpeechTag.getDefault().getTags();
	}

	@Override
	public String getType() {
		return PartOfSpeechTag.getDefault().getType();
	}

}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.UniversalDependenciesPOSTag;

public class TagFactory {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(TagFactory.class);

	private static TagFactory pos;
	private static TagFactory ner;

	public static TagFactory pos() {
		if (pos == null) {
			pos = new TagFactory(TagSet.POS, TagSet.UDPOS);
		}
		return pos;
	}

	public static TagFactory ner() {
		if (ner == null) {
			ner = new TagFactory(TagSet.NE);
		}
		return ner;
	}

	private List<TagSet> tagSets = new ArrayList<>();

	private TagFactory(TagSet... tagSets) {
		this.tagSets = Arrays.asList(tagSets);
	}

	public List<Tag> fromString(String tag) {
		if (tag == null || tag.isEmpty()) {
			return Collections.emptyList();
		}

		List<Tag> tags = new ArrayList<>();

		for (TagSet t : tagSets) {
			t.getTag(tag).ifPresent(tags::add);
		}

		if (tags.isEmpty()) {
			LOGGER.debug("Unknown tag: " + tag);
		}

		return tags;
	}

	enum TagSet {
		POS(PartOfSpeechTag.getDefault(), PartOfSpeechTag.UNKNOWN.getTag()),
		UDPOS(new UniversalDependenciesPOSTag(), PartOfSpeechTag.UNKNOWN.getTag()),
		NE(NamedEntityTag.getDefault(), NamedEntityTag.UNKNOWN.getTag());

		private final TagBuilder builder;
		private final Tag unknownTag;

		private TagSet(TagBuilder builder, Tag unknownTag) {
			this.builder = builder;
			this.unknownTag = unknownTag;
		}

		public Optional<Tag> getTag(String tag) {
			Tag result = builder.buildTag(tag);

			if (result.equals(unknownTag)) {
				return Optional.empty();
			} else {
				return Optional.of(result);
			}
		}
	}
}

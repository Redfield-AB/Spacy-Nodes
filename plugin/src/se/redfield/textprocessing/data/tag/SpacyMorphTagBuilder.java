/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.node.NodeLogger;
import org.knime.dl.util.DLUtils;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

/**
 * Tag builder for the Spacy morphology tags. Tags are loaded from the config
 * .csv file.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyMorphTagBuilder implements TagBuilder {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyMorphTagBuilder.class);

	private static final String TAG_TYPE = "SPACY_MORPH";
	private static final Tag UNKNOWN = new Tag("unknown", TAG_TYPE);
	private static final String TAGS_FILE = "config/morph_tags.csv";

	private static final SpacyMorphTagBuilder instance = new SpacyMorphTagBuilder();

	/**
	 * @return The tag builder instance.
	 */
	public static SpacyMorphTagBuilder getInstance() {
		return instance;
	}

	private Map<String, Tag> tags;
	private List<String> stringList;

	/**
	 * Creates new instance
	 */
	public SpacyMorphTagBuilder() {
		try {
			readTags(Files.readAllLines(DLUtils.Files.getFileFromSameBundle(this, TAGS_FILE).toPath()));
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void readTags(List<String> lines) {
		tags = new HashMap<>();

		for (String line : lines) {
			String[] parts = line.split(";");
			String group = parts[0];
			String[] values = parts[1].split(",");

			for (String val : values) {
				String tagValue = buildTagValue(group, val);
				tags.put(tagValue, new Tag(tagValue, TAG_TYPE));
			}
		}

		stringList = tags.values().stream().map(Tag::getTagValue).sorted().collect(Collectors.toList());
	}

	/**
	 * @param group The morphology group
	 * @param value The value of the tag
	 * @return The tag
	 */
	public Tag getTag(String group, String value) {
		return buildTag(buildTagValue(group, value));
	}

	private static String buildTagValue(String group, String value) {
		return group + ":" + value;
	}

	@Override
	public Tag buildTag(String value) {
		return tags.getOrDefault(value, UNKNOWN);
	}

	@Override
	public List<String> asStringList() {
		return stringList;
	}

	@Override
	public Set<Tag> getTags() {
		return new HashSet<>(tags.values());
	}

	@Override
	public String getType() {
		return TAG_TYPE;
	}

}

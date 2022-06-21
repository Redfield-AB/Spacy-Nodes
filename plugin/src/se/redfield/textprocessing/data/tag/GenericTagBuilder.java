/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

/**
 * Generic implementation of the {@link TagBuilder} interface.
 * 
 * @author Alexander Bondaletov
 *
 */
public class GenericTagBuilder implements TagBuilder {

	private final Map<String, Tag> tags;
	private final String type;

	/**
	 * @param type      The tag type.
	 * @param tagValues The list of tags.
	 */
	public GenericTagBuilder(String type, Collection<String> tagValues) {
		this.type = type;
		this.tags = tagValues.stream().collect(Collectors.toUnmodifiableMap(v -> v, v -> new Tag(v, type)));
	}

	@Override
	public Tag buildTag(String value) {
		return tags.get(value);
	}

	@Override
	public List<String> asStringList() {
		return List.copyOf(tags.keySet());
	}

	@Override
	public Set<Tag> getTags() {
		return Set.copyOf(tags.values());
	}

	@Override
	public String getType() {
		return type;
	}

}

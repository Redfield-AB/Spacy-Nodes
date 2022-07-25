/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.python3.PythonSourceDirectoryLocator;

/**
 * {@link TagBuilder} implementation that loads the tags from the resource text
 * file.
 * 
 * @author Alexander Bondaletov
 *
 */
public class ResourceFileTagBuilder extends GenericTagBuilder {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ResourceFileTagBuilder.class);

	/**
	 * @param type    The tag type.
	 * @param tagFile The path of the file containing tags.
	 */
	public ResourceFileTagBuilder(String type, String tagFile) {
		super(type, readTags(tagFile));
	}

	private static List<String> readTags(String tagFile) {
		try {
			return Files.readAllLines(PythonSourceDirectoryLocator.getPathFor(ResourceFileTagBuilder.class, tagFile));
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return Collections.emptyList();
	}
}

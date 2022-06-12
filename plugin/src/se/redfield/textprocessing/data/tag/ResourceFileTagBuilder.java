/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.data.tag;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.dl.util.DLUtils;

public class ResourceFileTagBuilder extends GenericTagBuilder {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(ResourceFileTagBuilder.class);

	public ResourceFileTagBuilder(String type, String tagFile) {
		super(type, readTags(tagFile));
	}

	private static List<String> readTags(String tagFile) {
		try {
			return Files
					.readAllLines(DLUtils.Files.getFileFromSameBundle(ResourceFileTagBuilder.class, tagFile).toPath());
		} catch (IllegalArgumentException | IOException e) {
			LOGGER.error(e.getMessage(), e);
		}

		return Collections.emptyList();
	}
}

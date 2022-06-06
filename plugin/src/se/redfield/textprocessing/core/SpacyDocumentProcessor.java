/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.TagBuilder;

import se.redfield.textprocessing.data.dto.SpacyDocument;

public interface SpacyDocumentProcessor {

	public Document process(SpacyDocument spacyDoc, Document doc);

	public TagBuilder getTagBuilder();

	public void setTagBuilder(TagBuilder builder);
}

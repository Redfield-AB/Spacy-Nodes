/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.TagBuilder;

import se.redfield.textprocessing.data.dto.SpacyDocument;

/**
 * Interface of the spacy document processor object intended to merge existing
 * {@link Document} object with the {@link SpacyDocument}.
 * 
 * @author Alexander Bondaletov
 *
 */
public interface SpacyDocumentProcessor {

	/**
	 * @param spacyDoc The spacy document.
	 * @param doc      The original document.
	 * @return The result document.
	 */
	public Document process(SpacyDocument spacyDoc, Document doc);

	/**
	 * @return The tag builder associated with the processor.
	 */
	public TagBuilder getTagBuilder();

	/**
	 * @param builder The tag builder.
	 */
	public void setTagBuilder(TagBuilder builder);
}

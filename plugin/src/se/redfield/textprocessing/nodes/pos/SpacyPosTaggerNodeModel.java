/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.pos;

import java.util.List;

import org.knime.ext.textprocessing.data.Tag;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.TagFactory;
import se.redfield.textprocessing.core.TaggerDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * POS (Part of Speech) tagger node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyPosTaggerNodeModel extends SpacyBaseNodeModel {

	protected SpacyPosTaggerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyPosTagger";
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new SpacyPosDocumentProcessor();
	}

	private class SpacyPosDocumentProcessor extends TaggerDocumentProcessor {

		@Override
		protected List<Tag> getTags(SpacyWord word) {
			return TagFactory.pos().fromString(word.getTag());
		}
	}
}
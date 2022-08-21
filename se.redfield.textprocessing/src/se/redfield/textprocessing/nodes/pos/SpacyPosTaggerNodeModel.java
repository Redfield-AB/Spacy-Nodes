/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.pos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.ext.textprocessing.data.Tag;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.TaggerDocumentProcessor;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.data.tag.SpacyPosTagBuilder;
import se.redfield.textprocessing.nodes.base.SpacyDocumentProcessorNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * POS (Part of Speech) tagger node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyPosTaggerNodeModel extends SpacyDocumentProcessorNodeModel {

	protected SpacyPosTaggerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyPosTagger";
	}

	@Override
	protected SpacyFeature getFeature() {
		return SpacyFeature.POS;
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new SpacyPosDocumentProcessor();
	}

	private class SpacyPosDocumentProcessor extends TaggerDocumentProcessor {

		protected SpacyPosDocumentProcessor() {
			super(SpacyPosTagBuilder.getInstance());
		}

		@Override
		protected List<Tag> getTags(SpacyWord word) {
			if (word.getTag() != null && !word.getTag().isEmpty()) {
				return Arrays.asList(getTagBuilder().buildTag(word.getTag()));
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public String getTagType() {
			return "POS";
		}
	}
}

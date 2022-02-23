/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.morph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.knime.ext.textprocessing.data.Tag;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.TaggerDocumentProcessor;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.data.tag.SpacyMorphTagBuilder;
import se.redfield.textprocessing.nodes.base.SpacyDocumentProcessorNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * Morphologizer node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyMorphologizerNodeModel extends SpacyDocumentProcessorNodeModel {

	protected SpacyMorphologizerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyMorphonogizer";
	}

	@Override
	protected SpacyFeature getFeature() {
		return SpacyFeature.MORPHOLOGY;
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new SpacyMorphDocumentProcessor();
	}

	private class SpacyMorphDocumentProcessor extends TaggerDocumentProcessor {

		@Override
		protected List<Tag> getTags(SpacyWord word) {
			List<Tag> tags = new ArrayList<>();

			for (Entry<String, String> e : word.getMorph().entrySet()) {
				String[] values = e.getValue().split(",");
				for (String val : values) {
					tags.add(SpacyMorphTagBuilder.getInstance().getTag(e.getKey(), val));
				}
			}

			return tags;
		}

	}
}

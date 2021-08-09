/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.core.AbstractSpacyDocumentProcessor;
import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.TagFactory;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

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

	private class SpacyPosDocumentProcessor extends AbstractSpacyDocumentProcessor {

		@Override
		protected Sentence mergeSentence(SpacySentence spacySent) {
			List<Term> terms = new ArrayList<>();
			Iterator<SpacyWord> wordsIter = Arrays.asList(spacySent.getWords()).iterator();

			while (wordsIter.hasNext()) {
				SpacyWord word = wordsIter.next();
				Term orig = getNextTerm();

				if (orig.getWords().size() == 1) {
					if (!orig.getWords().get(0).getText().equals(word.getText())) {
						throw new DocumentProcessingException();
					}

					List<Tag> tags = new ArrayList<>(orig.getTags());
					tags.addAll(TagFactory.pos().fromString(word.getTag()));
					terms.add(new Term(orig.getWords(), tags, false));
				} else {
					terms.add(createTerm(word));

					int count = orig.getWords().size() - 1;
					while (wordsIter.hasNext() && count > 0) {
						terms.add(createTerm(wordsIter.next()));
						count--;
					}

					if (count > 0) {
						throw new DocumentProcessingException();
					}
				}
			}

			return new Sentence(terms);
		}

		@Override
		protected Sentence processSentence(SpacySentence spacySent) {
			List<Term> terms = new ArrayList<>();
			for (SpacyWord w : spacySent.getWords()) {
				terms.add(createTerm(w));
			}
			return new Sentence(terms);
		}

		private Term createTerm(SpacyWord word) {
			return new Term(Arrays.asList(new Word(word.getText(), " ")), TagFactory.pos().fromString(word.getTag()),
					false);
		}
	}
}

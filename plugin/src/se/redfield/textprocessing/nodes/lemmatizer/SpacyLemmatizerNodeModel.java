/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.lemmatizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.core.AbstractSpacyDocumentProcessor;
import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

public class SpacyLemmatizerNodeModel extends SpacyBaseNodeModel {

	protected SpacyLemmatizerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyLemmatizer";
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new LemmatizerDocumentProcessor();
	}

	private class LemmatizerDocumentProcessor extends AbstractSpacyDocumentProcessor {

		@Override
		protected Sentence mergeSentence(SpacySentence spacySent) {
			List<Term> terms = new ArrayList<>();
			int idx = 0;
			while (idx < spacySent.getWords().length) {
				if (!hasNextTerm()) {
					throw new DocumentProcessingException();
				}

				Term orig = getNextTerm();
				List<Word> words = new ArrayList<>();

				for (Word w : orig.getWords()) {
					if (idx >= spacySent.getWords().length) {
						throw new DocumentProcessingException();
					}

					SpacyWord sw = spacySent.getWords()[idx++];

					if (!w.getText().equals(sw.getText())) {
						throw new DocumentProcessingException(w.getText() + "!=" + sw.getText());
					}

					words.add(new Word(sw.getLemmaOrText(), " "));
				}

				terms.add(new Term(words, orig.getTags(), orig.isUnmodifiable()));
			}
			return new Sentence(terms);
		}

		@Override
		protected Sentence processSentence(SpacySentence spacySent) {
			List<Term> terms = new ArrayList<>();
			for (SpacyWord sw : spacySent.getWords()) {
				terms.add(new Term(Arrays.asList(new Word(sw.getLemmaOrText(), " ")), Collections.emptyList(), false));
			}
			return new Sentence(terms);
		}

	}
}

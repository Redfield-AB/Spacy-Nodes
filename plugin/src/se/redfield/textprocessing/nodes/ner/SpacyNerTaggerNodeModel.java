/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.ner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.core.AbstractSpacyDocumentProcessor;
import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.data.tag.SpacyNerTag;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

public class SpacyNerTaggerNodeModel extends SpacyBaseNodeModel {

	protected SpacyNerTaggerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyNerTagger";
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new SpacyNerDocumentProcessor();
	}

	private class SpacyNerDocumentProcessor extends AbstractSpacyDocumentProcessor {

		private List<Term> terms;
		private int idx;

		@Override
		protected Sentence mergeSentence(SpacySentence spacySent) {
			SpacyWord[] words = spacySent.getWords();

			terms = new ArrayList<>();
			idx = 0;

			while (idx < spacySent.getWords().length) {
				if (words[idx].getIob() == 2) {
					skipNonNeWords(words);
				} else if (words[idx].getIob() == 3) {
					processNe(words);
				}
			}

			return new Sentence(terms);
		}

		private void skipNonNeWords(SpacyWord[] words) {
			int toSkip = 0;
			while (idx < words.length && words[idx].getIob() == 2) {
				toSkip += 1;
				idx += 1;
			}

			skipTerms(toSkip, false);
		}

		private void skipTerms(int toSkip, boolean discard) {
			while (toSkip > 0) {
				if (!hasNextTerm()) {
					throw new DocumentProcessingException();
				}

				Term term = getNextTerm();

				if (term.getWords().size() > toSkip) {
					throw new DocumentProcessingException();
				}

				toSkip -= term.getWords().size();

				if (!discard) {
					terms.add(term);
				}
			}

			if (toSkip < 0) {
				throw new DocumentProcessingException();
			}
		}

		private void processNe(SpacyWord[] words) {
			String entity = words[idx].getEntity();
			List<Word> neWords = collectNe(words);

			if (!hasNextTerm()) {
				throw new DocumentProcessingException();
			}

			Term orig = getNextTerm();
			List<Tag> tags = new ArrayList<>();

			if (orig.getWords().size() == neWords.size()) {
				tags.addAll(orig.getTags());
			} else if (orig.getWords().size() < neWords.size()) {
				skipTerms(neWords.size() - orig.getWords().size(), true);
			} else {
				throw new DocumentProcessingException();
			}

			tags.add(SpacyNerTag.fromString(entity).getTag());
			terms.add(new Term(neWords, tags, false));
		}

		private List<Word> collectNe(SpacyWord[] words) {
			List<String> neWords = new ArrayList<>();
			neWords.add(words[idx].getText());

			idx += 1;

			while (idx < words.length && words[idx].getIob() == 1) {
				neWords.add(words[idx].getText());
				idx += 1;
			}

			return neWords.stream().map(w -> new Word(w, " ")).collect(Collectors.toList());
		}

		@Override
		protected Sentence processSentence(SpacySentence spacySent) {
			SpacyWord[] words = spacySent.getWords();

			terms = new ArrayList<>();
			idx = 0;

			while (idx < words.length) {
				SpacyWord w = words[idx];

				if (w.getIob() == 2) {
					terms.add(new Term(Arrays.asList(new Word(w.getText(), " ")), Collections.emptyList(), false));
					idx += 1;
				} else {
					String entity = w.getEntity();
					List<Word> neWords = collectNe(words);
					terms.add(new Term(neWords, Arrays.asList(SpacyNerTag.fromString(entity).getTag()), false));
				}
			}

			return new Sentence(terms);
		}

	}
}

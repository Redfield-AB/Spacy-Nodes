/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;

/**
 * Base class for simple 'tagger' document processors, i.e. when a tags are
 * assigned to individual words.
 * 
 * @author Alexander Bondaletov
 *
 */
public abstract class TaggerDocumentProcessor extends AbstractSpacyDocumentProcessor {

	protected TaggerDocumentProcessor(TagBuilder builder) {
		super(builder);
	}

	@Override
	protected Sentence mergeSentence(SpacySentence spacySent) {
		List<Term> terms = new ArrayList<>();
		Iterator<SpacyWord> wordsIter = Arrays.asList(spacySent.getWords()).iterator();

		while (wordsIter.hasNext()) {
			SpacyWord word = wordsIter.next();
			Term orig = getNextTerm();

			if (orig.getWords().size() == 1) {
				if (!word.isSame(orig.getWords().get(0))) {
					throw new DocumentProcessingException();
				}

				List<Tag> tags = new ArrayList<>(orig.getTags());
				tags.addAll(getTags(word));
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

	protected Term createTerm(SpacyWord word) {
		return new Term(Arrays.asList(new Word(word.getText(), " ")), getTags(word), false);
	}

	protected abstract List<Tag> getTags(SpacyWord word);

}

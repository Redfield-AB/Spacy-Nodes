/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.stopword;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.core.AbstractSpacyDocumentProcessor;
import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyDocumentProcessorNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * The SpaCy stop word filter node model.
 * 
 * @author Adrian Nembach
 *
 */
public class SpacyStopWordFilterNodeModel extends SpacyDocumentProcessorNodeModel {

	protected SpacyStopWordFilterNodeModel(SpacyNodeSettings settings, boolean hasModelPorts) {
		super(settings, hasModelPorts);
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyStopWordFilter";
	}

	@Override
	protected SpacyFeature getFeature() {
		return SpacyFeature.TOKENIZATION;
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new StopWordFilterDocProcessor();
	}

	private static final class StopWordFilterDocProcessor extends AbstractSpacyDocumentProcessor {

		protected StopWordFilterDocProcessor() {
			super(null);
		}

		@Override
		public String getTagType() {
			return null;
		}

		@Override
		protected Sentence mergeSentence(SpacySentence spacySent) {
			var spacyWords = spacySent.getWords();
			var knimeSent = getNextSentence();
			var terms = new ArrayList<Term>();
			int spacyWordIdx = 0;
			for (var term : knimeSent.getTerms()) {
				var words = new ArrayList<Word>();
				for (var word : term.getWords()) {
					if (spacyWordIdx >= spacyWords.length) {
						break;
					}
					if (spacyWords[spacyWordIdx].isSame(word)) {
						words.add(word);
						spacyWordIdx++;
					}
				}
				if (!words.isEmpty()) {
					terms.add(new Term(words, term.getTags(), term.isUnmodifiable()));
				}
			}
			if (spacyWordIdx != spacyWords.length && !theRestIsBlank(spacyWordIdx, spacyWords)) {
				throw new DocumentProcessingException(
						"There are fewer words in the input sentence than in the sentence returned by spacy. "
								+ "This is most likely a coding error.");
			}
			return new Sentence(terms);
		}

		private static boolean theRestIsBlank(int idx, SpacyWord[] words) {
			for (int i = idx; i < words.length; i++) {
				if (!StringUtils.isBlank(words[i].getText())) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected Sentence processSentence(SpacySentence spacySent) {
			return new Sentence(Stream.of(spacySent.getWords())//
					.map(w -> new Term(List.of(new Word(w.getLemmaOrText(), " ")), List.of(), false))//
					.collect(Collectors.toList())//
			);
		}

	}

}

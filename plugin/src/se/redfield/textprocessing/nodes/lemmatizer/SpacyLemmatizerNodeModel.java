/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.lemmatizer;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacyDocument;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

public class SpacyLemmatizerNodeModel extends SpacyBaseNodeModel {

	protected SpacyLemmatizerNodeModel() {
		super(new SpacyNodeSettings());
	}

	@Override
	protected String getSpacyMethod() {
		return "lemmatize_table";
	}

	@Override
	protected SpacyDocumentProcessor createSpacyDocumentProcessor() {
		return new LemmatizerDocumentProcessor();
	}

	private class LemmatizerDocumentProcessor implements SpacyDocumentProcessor {

		@Override
		public Document process(SpacyDocument spacyDoc, Document doc) {
			DocumentBuilder db = new DocumentBuilder(doc);
			int idx = 0;

			for (Section s : doc.getSections()) {
				for (Paragraph p : s.getParagraphs()) {
					List<Sentence> sents = new ArrayList<>();
					for (Sentence sent : p.getSentences()) {
						SpacySentence spacySent = spacyDoc.getSentences()[idx++];
						sents.add(buildSentence(sent, spacySent));
					}
					db.addParagraph(new Paragraph(sents));
				}
				db.createNewSection(s.getAnnotation());
			}

			return db.createDocument();
		}

		private Sentence buildSentence(Sentence orig, SpacySentence spacySent) {
			List<Term> terms = new ArrayList<>();
			int idx = 0;

			for (Term origTerm : orig.getTerms()) {
				List<Word> newWords = new ArrayList<>();

				for (int i = 0; i < origTerm.getWords().size(); i++) {
					newWords.add(new Word(spacySent.getWords()[idx++].getLemmaOrText(), " "));
				}

				terms.add(new Term(newWords, origTerm.getTags(), false));
			}

			return new Sentence(terms);
		}

	}
}

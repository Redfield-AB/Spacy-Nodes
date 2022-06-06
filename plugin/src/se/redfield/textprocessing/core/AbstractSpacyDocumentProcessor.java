/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.Term;

import se.redfield.textprocessing.data.dto.SpacyDocument;
import se.redfield.textprocessing.data.dto.SpacySentence;

public abstract class AbstractSpacyDocumentProcessor implements SpacyDocumentProcessor {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(AbstractSpacyDocumentProcessor.class);

	private Document doc;
	private SpacyDocument spacyDoc;
	private DocumentBuilder builder;

	private Iterator<SpacySentence> spacySentenceIter;
	private SpacySentence curSpacySent;

	private Iterator<Section> sectionIter;
	private Section curSection;
	private Section nextSection;

	private Iterator<Paragraph> paragraphIter;
	private Paragraph curParagraph;

	private Iterator<Sentence> sentenceIter;
	private Sentence curSentence;

	private Iterator<Term> termIter;
	private Term curTerm;

	private List<Sentence> outSentences;

	private TagBuilder tagBuilder;

	protected AbstractSpacyDocumentProcessor(TagBuilder builder) {
		tagBuilder = builder;
	}

	@Override
	public TagBuilder getTagBuilder() {
		return tagBuilder;
	}

	@Override
	public void setTagBuilder(TagBuilder builder) {
		tagBuilder = builder;
	}

	@Override
	public Document process(SpacyDocument spacyDoc, Document doc) {
		init(spacyDoc, doc);

		try {
			while (hasMoreSpacySentences()) {
				commitSentence(mergeSentence(getNextSpacySentence()));
			}
		} catch (DocumentProcessingException | NoSuchElementException e) {
			LOGGER.debug("Invalid document tokenization", e);
			commitSentence(processSentence(curSpacySent));
		}

		while (hasMoreSpacySentences()) {
			commitSentence(processSentence(getNextSpacySentence()));
		}
		commitParagraph();
		commitSection();

		return builder.createDocument();
	}

	protected abstract Sentence mergeSentence(SpacySentence spacySent);

	protected abstract Sentence processSentence(SpacySentence spacySent);

	private void init(SpacyDocument spacyDoc, Document doc) {
		this.doc = doc;
		this.spacyDoc = spacyDoc;
		builder = new DocumentBuilder(doc);

		spacySentenceIter = Arrays.asList(spacyDoc.getSentences()).iterator();
		sectionIter = doc.getSections().iterator();

		curSpacySent = null;
		curSection = null;
		nextSection = null;
		paragraphIter = null;
		curParagraph = null;
		sentenceIter = null;
		curSentence = null;
		termIter = null;
		curTerm = null;
		outSentences = new ArrayList<>();
	}

	protected boolean hasMoreSpacySentences() {
		return spacySentenceIter.hasNext();
	}

	protected SpacySentence getNextSpacySentence() {
		curSpacySent = spacySentenceIter.next();
		return curSpacySent;
	}

	protected void commitSentence(Sentence sent) {
		outSentences.add(sent);
	}

	protected void commitParagraph() {
		builder.addParagraph(new Paragraph(outSentences));
		outSentences = new ArrayList<>();
	}

	protected void commitSection() {
		if (curSection != null) {
			builder.createNewSection(curSection.getAnnotation());
		}
	}

	protected boolean hasNextSection() {
		if (nextSection == null && sectionIter.hasNext()) {
			nextSection = queryNextSection();
		}
		return nextSection != null;
	}

	private Section queryNextSection() {
		while (sectionIter.hasNext()) {
			Section sec = sectionIter.next();
			if (!isBodySection(sec)) {
				skipSection(sec);
			} else {
				return sec;
			}
		}

		return null;
	}

	private static boolean isBodySection(Section section) {
		return section.getAnnotation().equals(SectionAnnotation.UNKNOWN)
				|| section.getAnnotation().equals(SectionAnnotation.CHAPTER)
				|| section.getAnnotation().equals(SectionAnnotation.ABSTRACT);
	}

	private void skipSection(Section section) {
		for (Paragraph p : section.getParagraphs()) {
			builder.addParagraph(p);
		}
		builder.createNewSection(section.getAnnotation());
	}

	protected Section getNextSection() {
		commitSection();
		curSection = nextSection;
		nextSection = queryNextSection();
		paragraphIter = curSection.getParagraphs().iterator();
		return curSection;
	}

	protected boolean hasNextParagraph() {
		return (paragraphIter != null && paragraphIter.hasNext()) || hasNextSection();
	}

	protected Paragraph getNextParagraph() {
		if (paragraphIter == null || !paragraphIter.hasNext()) {
			if (hasNextSection()) {
				getNextSection();
			} else {
				throw new NoSuchElementException();
			}
		}
		if (curParagraph != null) {
			commitParagraph();
		}
		curParagraph = paragraphIter.next();
		sentenceIter = curParagraph.getSentences().iterator();
		return curParagraph;
	}

	protected boolean hasNextSentence() {
		return (sentenceIter != null && sentenceIter.hasNext()) || hasNextParagraph();
	}

	protected Sentence getNextSentence() {
		if (sentenceIter == null || !sentenceIter.hasNext()) {
			if (hasNextParagraph()) {
				getNextParagraph();
			} else {
				throw new NoSuchElementException();
			}
		}
		curSentence = sentenceIter.next();
		termIter = curSentence.getTerms().iterator();
		return curSentence;
	}

	protected boolean hasNextTerm() {
		return (termIter != null && termIter.hasNext()) || hasNextSentence();
	}

	protected Term getNextTerm() {
		if (termIter == null || !termIter.hasNext()) {
			if (hasNextSentence()) {
				getNextSentence();
			} else {
				throw new NoSuchElementException();
			}
		}
		curTerm = termIter.next();
		return curTerm;
	}

	public static class DocumentProcessingException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public DocumentProcessingException() {
			super();
		}

		public DocumentProcessingException(String message) {
			super(message);
		}

	}
}

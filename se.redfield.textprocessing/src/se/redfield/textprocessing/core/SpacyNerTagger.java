/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core;

import java.util.List;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
import org.knime.python2.kernel.PythonIOException;

public class SpacyNerTagger extends AbstractDocumentTagger {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyNerTagger.class);

	private SpacyNlp nlp;

	public SpacyNerTagger(SpacyNlp nlp) {
		super(false, "OpenNLP WhitespaceTokenizer");
		this.nlp = nlp;
	}

	@Override
	protected List<Tag> getTags(String tag) {
		return TagFactory.ner().fromString(tag);
	}

	@Override
	protected List<TaggedEntity> tagEntities(Sentence sentence) {
		try {
			return nlp.computeNeTags(sentence.getText());
		} catch (PythonIOException | CanceledExecutionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	protected void preprocess(Document doc) {

	}

}

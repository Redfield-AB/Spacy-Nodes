/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.data.dto.SpacyDocument;
import se.redfield.textprocessing.data.dto.SpacySentence;
import se.redfield.textprocessing.data.dto.SpacyWord;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * The tokenizer node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyTokenizerNodeModel extends SpacyBaseNodeModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyTokenizerNodeModel.class);

	protected SpacyTokenizerNodeModel(SpacyNodeSettings settings, boolean hasModelPorts) {
		super(settings, true, hasModelPorts);
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyNlp";
	}

	@Override
	protected SpacyFeature getFeature() {
		return SpacyFeature.TOKENIZATION;
	}

	@Override
	protected CellFactory createCellFactory(int inputColumn, int resultColumn, DataTableSpec inSpec,
			BufferedDataTable metaTable, ExecutionContext exec, String modelName) {
		return new DocumentCellFactory(createOutputColumnSpec(), createTextContainerFactory(exec), resultColumn);
	}

	@Override
	protected CellFactory createInputProcessingFactory(DataTableSpec spec, int columnIdx) {
		return new InputTextCleanupFactory(columnIdx);
	}

	private static class DocumentCellFactory extends SingleCellFactory {

		private final int columnIndex;
		private final TextContainerDataCellFactory factory;

		public DocumentCellFactory(DataColumnSpec spec, TextContainerDataCellFactory factory, int columnIndex) {
			super(spec);
			this.columnIndex = columnIndex;
			this.factory = factory;
		}

		@Override
		public DataCell getCell(DataRow row) {
			String json = row.getCell(columnIndex).toString();
			try {
				return fromJson(json);
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				return new MissingCell(e.getMessage());
			}
		}

		@SuppressWarnings("deprecation")
		private DataCell fromJson(String json) throws JsonProcessingException {
			List<Sentence> sentences = new ArrayList<>();
			SpacyDocument spacyDoc = SpacyDocument.fromJson(json);

			for (SpacySentence spacySent : spacyDoc.getSentences()) {
				List<Term> terms = new ArrayList<>();

				for (SpacyWord w : spacySent.getWords()) {
					terms.add(fromSpacyWord(w));
				}

				sentences.add(new Sentence(terms));
			}

			DocumentBuilder b = new DocumentBuilder();
			b.addParagraph(new Paragraph(sentences));
			b.createNewSection(SectionAnnotation.CHAPTER);

			return factory.createDataCell(b.createDocument());
		}

		private static Term fromSpacyWord(SpacyWord word) {
			return new Term(Arrays.asList(new Word(word.getText(), " ")), Collections.emptyList(), false);
		}

	}

	private class InputTextCleanupFactory extends SingleCellFactory {

		private final int columnIdx;

		public InputTextCleanupFactory(int columnIdx) {
			super(new DataColumnSpecCreator(settings.getColumn(), StringCell.TYPE).createSpec());
			this.columnIdx = columnIdx;
		}

		@Override
		public DataCell getCell(DataRow row) {
			var result = getText(row) //
					.strip() // remove all leading and trailing whitespace chars
					.replaceAll("(\\s)\\s+", "$1"); // remove consecutive whitespace chars
			return StringCellFactory.create(result);
		}

		private String getText(DataRow row) {
			var cell = row.getCell(columnIdx);
			if (cell instanceof DocumentValue) {
				return ((DocumentValue) cell).getDocument().getDocumentBodyText();
			} else {
				return ((StringValue) cell).getStringValue();
			}
		}
	}
}

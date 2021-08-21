/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacyDocument;

public abstract class SpacyDocumentProcessorNodeModel extends SpacyBaseNodeModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyDocumentProcessorNodeModel.class);

	protected SpacyDocumentProcessorNodeModel(SpacyNodeSettings settings) {
		super(settings);
	}

	@Override
	protected CellFactory createCellFactory(int inputColumn, int resultColumn, ExecutionContext exec) {
		return new SpacyDocumentCellFactory(createTextContainerFactory(exec), createSpacyDocumentProcessor(),
				settings.getColumn(), inputColumn, resultColumn);
	}

	protected abstract SpacyDocumentProcessor createSpacyDocumentProcessor();

	private static class SpacyDocumentCellFactory extends SingleCellFactory {
		private final TextContainerDataCellFactory factory;
		private final SpacyDocumentProcessor processor;

		private final int docColumnIdx;
		private final int jsonColumnIdx;

		public SpacyDocumentCellFactory(TextContainerDataCellFactory factory, SpacyDocumentProcessor processor,
				String newColumnName, int docColumnIdx, int jsonColumnIdx) {
			super(createSpec(factory, newColumnName));
			this.factory = factory;
			this.processor = processor;

			this.docColumnIdx = docColumnIdx;
			this.jsonColumnIdx = jsonColumnIdx;
		}

		private static DataColumnSpec createSpec(TextContainerDataCellFactory factory, String columnName) {
			return new DataColumnSpecCreator(columnName, factory.getDataType()).createSpec();
		}

		@Override
		public DataCell getCell(DataRow row) {
			final Document d = ((DocumentValue) row.getCell(docColumnIdx)).getDocument();

			try {
				SpacyDocument spacyDoc = SpacyDocument.fromJson(row.getCell(jsonColumnIdx).toString());
				return factory.createDataCell(processor.process(spacyDoc, d));
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage(), e);
				return new MissingCell(e.getMessage());
			}
		}

	}
}

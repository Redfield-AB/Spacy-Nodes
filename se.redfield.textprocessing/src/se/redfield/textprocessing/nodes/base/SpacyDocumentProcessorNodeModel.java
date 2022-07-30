/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.tag.TagSet;
import org.knime.ext.textprocessing.data.tag.TagSets;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import se.redfield.textprocessing.core.SpacyDocumentProcessor;
import se.redfield.textprocessing.data.dto.SpacyDocument;
import se.redfield.textprocessing.data.tag.DynamicTagBuilder;
import se.redfield.textprocessing.data.tag.GenericTagBuilder;

/**
 * The base {@link NodeModel} for the nodes that outputs the document as a
 * result.
 * 
 * @author Alexander Bondaletov
 *
 */
public abstract class SpacyDocumentProcessorNodeModel extends SpacyBaseNodeModel {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(SpacyDocumentProcessorNodeModel.class);

	protected SpacyDocumentProcessorNodeModel(SpacyNodeSettings settings) {
		super(settings, false);
	}

	@Override
	protected CellFactory createCellFactory(int inputColumn, int resultColumn, DataTableSpec inSpec,
			BufferedDataTable metaTable, ExecutionContext exec) throws CanceledExecutionException {
		TextContainerDataCellFactory textContainerFactory = createTextContainerFactory(exec);

		DataColumnSpecCreator creator = new DataColumnSpecCreator(settings.getOutputColumnName(),
				textContainerFactory.getDataType());
		Set<TagSet> dynamicTagSets = TagSets.getDynamicTagSets(inSpec.getColumnSpec(inputColumn));
		TagSets.addTagSetsToMetaData(creator, dynamicTagSets);

		SpacyDocumentProcessor docProcessor = createSpacyDocumentProcessor();

		if (docProcessor.getTagBuilder() != null) {
			Set<String> tags = collectAssignedTags(metaTable, exec);
			TagBuilder builder = selectTagBuilder(tags, docProcessor.getTagBuilder(), dynamicTagSets);
			docProcessor.setTagBuilder(builder);

			if (builder instanceof DynamicTagBuilder) {
				TagSets.addTagBuildersToMetaData(creator, Set.of(builder));
			}
		}

		return new SpacyDocumentCellFactory(textContainerFactory, docProcessor, creator.createSpec(), inputColumn,
				resultColumn);
	}

	private static Set<String> collectAssignedTags(BufferedDataTable metaTable, ExecutionMonitor exec)
			throws CanceledExecutionException {
		Set<String> tags = new HashSet<>();
		for (DataRow row : metaTable) {
			tags.add(row.getCell(0).toString());

			exec.checkCanceled();
			exec.setProgress((double) tags.size() / metaTable.size());
		}
		return tags;
	}

	private static TagBuilder selectTagBuilder(Set<String> tags, TagBuilder staticBuilder,
			Set<TagSet> availableTagSets) {
		if (tags.stream().allMatch(t -> staticBuilder.buildTag(t) != null)) {
			return staticBuilder;
		}

		for (TagSet tagSet : availableTagSets) {
			List<String> tagSetTags = tagSet.asStringList();
			if (tagSetTags.containsAll(tags)) {
				return new GenericTagBuilder(tagSet.getType(), tagSetTags);
			}
		}

		return new DynamicTagBuilder(availableTagSets, tags);
	}

	protected abstract SpacyDocumentProcessor createSpacyDocumentProcessor();

	private static class SpacyDocumentCellFactory extends SingleCellFactory {
		private final TextContainerDataCellFactory factory;
		private final SpacyDocumentProcessor processor;

		private final int docColumnIdx;
		private final int jsonColumnIdx;

		public SpacyDocumentCellFactory(TextContainerDataCellFactory factory, SpacyDocumentProcessor processor,
				DataColumnSpec newColumnSpec, int docColumnIdx, int jsonColumnIdx) {
			super(newColumnSpec);
			this.factory = factory;
			this.processor = processor;

			this.docColumnIdx = docColumnIdx;
			this.jsonColumnIdx = jsonColumnIdx;
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

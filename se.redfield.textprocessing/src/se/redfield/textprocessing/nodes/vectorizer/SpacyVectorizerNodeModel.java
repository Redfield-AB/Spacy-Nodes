/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.vectorizer;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.python2.kernel.PythonIOException;

import se.redfield.textprocessing.core.PythonContext;
import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.nodes.base.SpacyBaseNodeModel;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * The vectorizer node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyVectorizerNodeModel extends SpacyBaseNodeModel {

	protected SpacyVectorizerNodeModel(SpacyNodeSettings settings, boolean hasModelPorts) {
		super(settings, true, hasModelPorts);
	}

	@Override
	protected String getSpacyMethod() {
		return "SpacyVectorizer";
	}

	@Override
	protected SpacyFeature getFeature() {
		return SpacyFeature.VECTORIZATION;
	}

	@Override
	protected DataColumnSpec createOutputColumnSpec() {
		return new DataColumnSpecCreator(settings.getOutputColumnName(), ListCell.getCollectionType(DoubleCell.TYPE))
				.createSpec();
	}

	@Override
	protected BufferedDataTable buildOutputTable(BufferedDataTable inTable, PythonContext ctx, ExecutionContext exec,
			String modelName) throws CanceledExecutionException, PythonIOException {
		BufferedDataTable res = ctx.getDataTable(exec.createSubExecutionContext(0.5));
		BufferedDataTable joined = joinResultTable(inTable, res, exec.createSubExecutionContext(0.5));

		DataTableSpecCreator c = new DataTableSpecCreator(joined.getDataTableSpec());
		c.replaceColumn(joined.getDataTableSpec().findColumnIndex(PYTHON_RES_COLUMN_NAME), createOutputColumnSpec());
		BufferedDataTable result = exec.createSpecReplacerTable(joined, c.createSpec());
		exec.setProgress(1.0);
		return result;
	}

	private BufferedDataTable joinResultTable(BufferedDataTable inTable, BufferedDataTable resTable,
			ExecutionContext exec) throws CanceledExecutionException {
		BufferedDataTable joined = exec.createJoinedTable(inTable, resTable, exec);

		if (settings.getReplaceColumn()) {
			ColumnRearranger r = new ColumnRearranger(joined.getDataTableSpec());
			int colIdx = joined.getDataTableSpec().findColumnIndex(settings.getColumn());
			r.remove(colIdx);
			r.move(PYTHON_RES_COLUMN_NAME, colIdx);
			return exec.createColumnRearrangeTable(joined, r, exec);
		} else {
			return joined;
		}
	}

	@Override
	protected CellFactory createCellFactory(int inputColumn, int resultColumn, DataTableSpec inSpec,
			BufferedDataTable metaTable, ExecutionContext exec, String modelName) {
		return null;
	}

}

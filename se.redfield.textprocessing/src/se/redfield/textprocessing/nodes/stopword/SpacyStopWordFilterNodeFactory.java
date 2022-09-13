/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.stopword;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.SpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * The node factory for the {@link SpacyStopWordFilterNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyStopWordFilterNodeFactory extends NodeFactory<SpacyStopWordFilterNodeModel> {

	@Override
	public SpacyStopWordFilterNodeModel createNodeModel() {
		return new SpacyStopWordFilterNodeModel(new SpacyNodeSettings(), true);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyStopWordFilterNodeModel> createNodeView(int viewIndex, SpacyStopWordFilterNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new SpacyNodeDialog();
	}

}

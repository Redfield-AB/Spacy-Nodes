/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.pos;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.SpacyNodeDialog;

public class SpacyPosTaggerNodeFactory extends NodeFactory<SpacyPosTaggerNodeModel> {

	@Override
	public SpacyPosTaggerNodeModel createNodeModel() {
		return new SpacyPosTaggerNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyPosTaggerNodeModel> createNodeView(int viewIndex, SpacyPosTaggerNodeModel nodeModel) {
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

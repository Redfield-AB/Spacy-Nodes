/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.vectorizer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.SpacyNodeDialog;

/**
 * The factory class for the {@link SpacyVectorizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyVectorizerNodeFactory extends NodeFactory<SpacyVectorizerNodeModel> {

	@Override
	public SpacyVectorizerNodeModel createNodeModel() {
		return new SpacyVectorizerNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyVectorizerNodeModel> createNodeView(int viewIndex, SpacyVectorizerNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new SpacyNodeDialog(true);
	}

}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * The factory class for the {@link SpacyModelSelectorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelSelectorNodeFactory extends NodeFactory<SpacyModelSelectorNodeModel> {

	@Override
	public SpacyModelSelectorNodeModel createNodeModel() {
		return new SpacyModelSelectorNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyModelSelectorNodeModel> createNodeView(int viewIndex, SpacyModelSelectorNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new SpacyModelSelectorNodeDialog();
	}

}

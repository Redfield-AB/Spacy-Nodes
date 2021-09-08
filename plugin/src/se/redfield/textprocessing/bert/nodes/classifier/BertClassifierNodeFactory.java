/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.classifier;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory class for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeFactory extends NodeFactory<BertClassifierNodeModel> {

	@Override
	public BertClassifierNodeModel createNodeModel() {
		return new BertClassifierNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<BertClassifierNodeModel> createNodeView(int viewIndex, BertClassifierNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new BertClassifierNodeDialog();
	}

}

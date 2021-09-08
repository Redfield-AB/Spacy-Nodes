/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.embedder;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Factory class for the {@link BertEmbedderNodeModel}.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertEmbedderNodeFactory extends NodeFactory<BertEmbedderNodeModel> {

	@Override
	public BertEmbedderNodeModel createNodeModel() {
		return new BertEmbedderNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<BertEmbedderNodeModel> createNodeView(int viewIndex, BertEmbedderNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new BertEmbedderNodeDialog();
	}

}

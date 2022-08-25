/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.tokenizer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.OldSpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.OldSpacyNodeSettings;

/**
 * Factory class for the {@link SpacyTokenizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
@Deprecated
public class SpacyTokenizerNodeFactory extends NodeFactory<SpacyTokenizerNodeModel> {

	@Override
	public SpacyTokenizerNodeModel createNodeModel() {
		return new SpacyTokenizerNodeModel(new OldSpacyNodeSettings(), false);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyTokenizerNodeModel> createNodeView(int viewIndex, SpacyTokenizerNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new OldSpacyNodeDialog(new OldSpacyNodeSettings(), true);
	}

}

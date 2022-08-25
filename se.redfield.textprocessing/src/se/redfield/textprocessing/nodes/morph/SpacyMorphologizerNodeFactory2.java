/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.morph;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.SpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.SpacyNodeSettings;

/**
 * Node factory for the {@link SpacyMorphologizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyMorphologizerNodeFactory2 extends NodeFactory<SpacyMorphologizerNodeModel> {

	@Override
	public SpacyMorphologizerNodeModel createNodeModel() {
		return new SpacyMorphologizerNodeModel(new SpacyNodeSettings(), true);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyMorphologizerNodeModel> createNodeView(int viewIndex, SpacyMorphologizerNodeModel nodeModel) {
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

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.morph;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.OldSpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.OldSpacyNodeSettings;

/**
 * Node factory for the {@link SpacyMorphologizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
@Deprecated
public class SpacyMorphologizerNodeFactory extends NodeFactory<SpacyMorphologizerNodeModel> {

	@Override
	public SpacyMorphologizerNodeModel createNodeModel() {
		return new SpacyMorphologizerNodeModel(new OldSpacyNodeSettings(), false);
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
		return new OldSpacyNodeDialog(new OldSpacyNodeSettings());
	}

}

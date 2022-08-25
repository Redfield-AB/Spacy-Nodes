/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.lemmatizer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.OldSpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.OldSpacyNodeSettings;

/**
 * The node factory for the {@link SpacyLemmatizerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
@Deprecated
public class SpacyLemmatizerNodeFactory extends NodeFactory<SpacyLemmatizerNodeModel> {

	@Override
	public SpacyLemmatizerNodeModel createNodeModel() {
		return new SpacyLemmatizerNodeModel(new OldSpacyNodeSettings(), false);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyLemmatizerNodeModel> createNodeView(int viewIndex, SpacyLemmatizerNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new OldSpacyNodeDialog(new OldSpacyNodeSettings(), false);
	}

}

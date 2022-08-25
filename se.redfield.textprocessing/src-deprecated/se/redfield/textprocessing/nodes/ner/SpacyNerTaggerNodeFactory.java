/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.ner;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import se.redfield.textprocessing.nodes.base.OldSpacyNodeDialog;
import se.redfield.textprocessing.nodes.base.OldSpacyNodeSettings;

/**
 * The factory class for the {@link SpacyNerTaggerNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
@Deprecated
public class SpacyNerTaggerNodeFactory extends NodeFactory<SpacyNerTaggerNodeModel> {

	@Override
	public SpacyNerTaggerNodeModel createNodeModel() {
		return new SpacyNerTaggerNodeModel(new OldSpacyNodeSettings(), false);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<SpacyNerTaggerNodeModel> createNodeView(int viewIndex, SpacyNerTaggerNodeModel nodeModel) {
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

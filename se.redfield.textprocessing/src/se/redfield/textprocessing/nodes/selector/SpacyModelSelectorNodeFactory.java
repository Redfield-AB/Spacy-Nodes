/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.util.Optional;

import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;

import se.redfield.textprocessing.nodes.port.SpacyModelPortObject;

/**
 * The factory class for the {@link SpacyModelSelectorNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelSelectorNodeFactory extends ConfigurableNodeFactory<SpacyModelSelectorNodeModel> {

	/**
	 * File System Connection port name.
	 */
	public static final String FILE_SYSTEM_CONNECTION_PORT_NAME = "File System Connection";

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder builder = new PortsConfigurationBuilder();
		builder.addOptionalInputPortGroup(FILE_SYSTEM_CONNECTION_PORT_NAME, FileSystemPortObject.TYPE);
		builder.addFixedOutputPortGroup("Spacy Model", SpacyModelPortObject.TYPE);
		return Optional.of(builder);
	}

	@Override
	protected SpacyModelSelectorNodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		return new SpacyModelSelectorNodeModel(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
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
	protected NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
		return new SpacyModelSelectorNodeDialog(creationConfig.getPortConfig().orElseThrow(IllegalStateException::new));
	}

}

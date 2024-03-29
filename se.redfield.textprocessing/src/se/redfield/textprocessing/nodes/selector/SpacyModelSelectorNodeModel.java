/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.defaultnodesettings.status.NodeModelStatusConsumer;
import org.knime.filehandling.core.defaultnodesettings.status.StatusMessage.MessageType;

import se.redfield.textprocessing.core.model.SpacyModelDescription;
import se.redfield.textprocessing.core.model.download.FsModelDownloader;
import se.redfield.textprocessing.core.model.download.RepositoryModelDownloader;
import se.redfield.textprocessing.core.model.download.SpacyModelDownloader;
import se.redfield.textprocessing.nodes.port.SpacyModelFileStorePortObject;
import se.redfield.textprocessing.nodes.port.SpacyModelPortObjectSpec;
import se.redfield.textprocessing.nodes.selector.SpacyModelSelectorNodeSettings.SpacyModelSelectionMode;

/**
 * The SpaCy model selector node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelSelectorNodeModel extends NodeModel {

	private final SpacyModelSelectorNodeSettings settings;
	private final NodeModelStatusConsumer statusConsumer = new NodeModelStatusConsumer(
			EnumSet.of(MessageType.ERROR, MessageType.WARNING));

	protected SpacyModelSelectorNodeModel(PortsConfiguration portsConfig) {
		super(portsConfig.getInputPorts(), portsConfig.getOutputPorts());
		settings = new SpacyModelSelectorNodeSettings(portsConfig);
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) throws InvalidSettingsException {
		settings.validate();
		settings.configure(inSpecs, statusConsumer);
		statusConsumer.setWarningsIfRequired(this::setWarningMessage);
		SpacyModelDownloader downloader = createDownloader();
		return new PortObjectSpec[] { createSpec(downloader.getModelDescription(true)) };
	}

	private static SpacyModelPortObjectSpec createSpec(SpacyModelDescription desc) {
		return new SpacyModelPortObjectSpec(desc);
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec) throws Exception {
		SpacyModelDownloader downloader = createDownloader();
		downloader.ensureDownloaded(exec);
		final var spec = createSpec(downloader.getModelDescription(false));
		var model = SpacyModelFileStorePortObject.create(spec, FileStoreFactory.createFileStoreFactory(exec));
		return new PortObject[] { model };
	}

	private SpacyModelDownloader createDownloader() {
		if (settings.getSelectionMode() == SpacyModelSelectionMode.SPACY) {
			return new RepositoryModelDownloader(settings.getModelDef());
		} else {
			return new FsModelDownloader(settings.getLocalPathModel());
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		this.settings.loadSettingsFrom(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no internals
	}

	@Override
	protected void reset() {
		// nothing to do
	}

}

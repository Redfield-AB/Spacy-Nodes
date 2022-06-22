/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.python2.config.PythonFixedVersionExecutableSelectionPanel;

/**
 * The node dialog for different SpaCy nodes.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyNodeDialog extends DefaultNodeSettingsPane {
	private SpacyNodeSettings settings = new SpacyNodeSettings();
	private final PythonFixedVersionExecutableSelectionPanel selector;

	/**
	 * Default constructor
	 */
	public SpacyNodeDialog() {
		this(false);
	}

	/**
	 * @param acceptStringsColumns if <code>true</code> string columns will be
	 *                             accepted for selection. Otherwise, only Document
	 *                             columns are accepted.
	 */
	@SuppressWarnings("unchecked")
	public SpacyNodeDialog(boolean acceptStringsColumns) {
		selector = new PythonFixedVersionExecutableSelectionPanel(this, settings.getPythonCommand());

		Class<? extends DataValue> classFilter = acceptStringsColumns ? StringValue.class : DocumentValue.class;

		addDialogComponent(new DialogComponentColumnNameSelection(settings.getColumnModel(), "Select column:",
				SpacyBaseNodeModel.PORT_TABLE, true, classFilter));
		addDialogComponent(new DialogComponentBoolean(settings.getReplaceColumnModel(), "Replace column"));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentString(settings.getAppendedColumnNameModel(), "Append Column"));

		addTab("Python", selector);
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		selector.loadSettingsFrom(settings);
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}
}

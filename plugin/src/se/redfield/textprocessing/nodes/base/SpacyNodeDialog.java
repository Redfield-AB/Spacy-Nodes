/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.base;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.DocumentValue;

import se.redfield.textprocessing.core.SpacyModel;

public class SpacyNodeDialog extends DefaultNodeSettingsPane {
	private SpacyNodeSettings settings = new SpacyNodeSettings();
	private JComboBox<SpacyModel> cbModels;

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
		Class<? extends DataValue> classFilter = acceptStringsColumns ? StringValue.class : DocumentValue.class;

		addDialogComponent(new DialogComponentColumnNameSelection(settings.getColumnModel(), "Select column:", 0, true,
				classFilter));
		addDialogComponent(new DialogComponentBoolean(settings.getReplaceColumnModel(), "Replace column"));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentString(settings.getAppendedColumnNameModel(), "Append Column"));
		addTab("Model", createModelSelector());
	}

	private Component createModelSelector() {
		JPanel panel = new JPanel();

		DialogComponentFileChooser localPath = new DialogComponentFileChooser(settings.getLocalModelPathModel(),
				"spacy-model", JFileChooser.OPEN_DIALOG, true);

		cbModels = new JComboBox<>(SpacyModel.list().toArray(new SpacyModel[] {}));
		cbModels.addActionListener(e -> {
			settings.setSpacyModel((SpacyModel) cbModels.getSelectedItem());
			localPath.getComponentPanel().setVisible(settings.getSpacyModel() == SpacyModel.LOCAL_DIR);
		});

		JLabel label = new JLabel("spaCy model:");
		panel.add(label);
		panel.add(cbModels);

		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(panel);
		box.add(localPath.getComponentPanel());

		return box;
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			this.settings.loadSettings(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		cbModels.setSelectedItem(this.settings.getSpacyModel());
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettings(settings);
	}
}

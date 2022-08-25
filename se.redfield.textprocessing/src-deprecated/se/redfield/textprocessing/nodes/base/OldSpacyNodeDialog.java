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

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.port.PortObjectSpec;

import se.redfield.textprocessing.core.SpacyModel;

@Deprecated
public final class OldSpacyNodeDialog extends SpacyNodeDialog {
	
	private JComboBox<SpacyModel> cbModels = new JComboBox<>(SpacyModel.list().toArray(new SpacyModel[] {}));
	
	private final OldSpacyNodeSettings settings;

	public OldSpacyNodeDialog(OldSpacyNodeSettings spacySettings, boolean acceptStringsColumns) {
		super(spacySettings, 0, acceptStringsColumns);
		settings = spacySettings;
		addTabAt(1, "Model", createModelSelector());
	}
	
	public OldSpacyNodeDialog(OldSpacyNodeSettings spacySettings) {
		this(spacySettings, false);
	}

	private Component createModelSelector() {
		JPanel panel = new JPanel();

		DialogComponentFileChooser localPath = new DialogComponentFileChooser(settings.getLocalModelPathModel(),
				"spacy-model", JFileChooser.OPEN_DIALOG, true);

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
		super.loadAdditionalSettingsFrom(settings, specs);
		cbModels.setSelectedItem(this.settings.getSpacyModel());
	}
}

/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.embedder;

import javax.swing.JLabel;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.setting.InputSettings;

/**
 * 
 * Dialog for {@link BertEmbedderNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertEmbedderNodeDialog extends NodeDialogPane {
	private final BertEmbedderSettings settings;
	private InputSettingsEditor inputSettings;

	/**
	 * Creates new instance.
	 */
	public BertEmbedderNodeDialog() {
		settings = new BertEmbedderSettings();
		inputSettings = new InputSettingsEditor(settings.getInputSettings(), BertEmbedderNodeModel.PORT_DATA_TABLE);

		addTab("Settings", inputSettings.getComponentGroupPanel());
		addTab("Advanced", new AdvancedTabGroup().getComponentGroupPanel());
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}
		inputSettings.loadSettings(settings, specs);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

	private class AdvancedTabGroup extends AbstractGridBagDialogComponentGroup {
		public AdvancedTabGroup() {
			addNumberSpinnerRowComponent(settings.getBatchSizeModel(), "Batch size", 1);
		}
	}

	private class InputSettingsEditor extends AbstractGridBagDialogComponentGroup {

		private DialogComponentColumnNameSelection sentenceColumn;

		@SuppressWarnings("unchecked")
		public InputSettingsEditor(InputSettings settings, int specIndex) {
			sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "", specIndex,
					true, StringValue.class);

			addDoubleColumnRow(new JLabel("Sentence column"),
					getFirstComponent(sentenceColumn, ColumnSelectionPanel.class));
			addNumberSpinnerRowComponent(settings.getMaxSeqLengthModel(), "Max sequence length", 1);
		}

		public void loadSettings(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
			sentenceColumn.loadSettingsFrom(settings, specs);
		}
	}
}
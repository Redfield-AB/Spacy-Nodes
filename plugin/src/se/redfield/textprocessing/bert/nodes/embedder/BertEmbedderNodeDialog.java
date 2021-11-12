/*
 * Copyright (c) 2020 Redfield AB.
 *
 */
package se.redfield.textprocessing.bert.nodes.embedder;

import javax.swing.JLabel;

import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.dl.base.nodes.AbstractGridBagDialogComponentGroup;

import se.redfield.bert.setting.InputSettings;
import se.redfield.bert.setting.ui.PythonNodeDialog;

/**
 * 
 * Dialog for {@link BertEmbedderNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertEmbedderNodeDialog extends PythonNodeDialog<BertEmbedderSettings> {
	private InputSettingsEditor inputSettings;

	/**
	 * Creates new instance.
	 */
	public BertEmbedderNodeDialog() {
		super(new BertEmbedderSettings());
		inputSettings = new InputSettingsEditor(settings.getInputSettings(), BertEmbedderNodeModel.PORT_DATA_TABLE);

		addTab("Settings", inputSettings.getComponentGroupPanel());
		addTab("Advanced", new AdvancedTabGroup().getComponentGroupPanel());
		addPythonTab();
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadSettingsFrom(settings, specs);
		inputSettings.loadSettings(settings, specs);
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

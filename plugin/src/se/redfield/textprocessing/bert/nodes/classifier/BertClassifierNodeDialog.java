/*
 * Copyright (c) 2020 Redfield AB.
 * 
 */
package se.redfield.textprocessing.bert.nodes.classifier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
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

import se.redfield.bert.setting.ui.OptimizerSettingsEditor;

/**
 * 
 * Dialog for the {@link BertClassifierNodeModel} node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class BertClassifierNodeDialog extends NodeDialogPane {

	private final BertClassifierSettings settings;

	private DialogComponentColumnNameSelection sentenceColumn;
	private DialogComponentColumnNameSelection classColumn;
	private OptimizerSettingsEditor optimizer;

	/**
	 * Creates new instance
	 */
	@SuppressWarnings("unchecked")
	public BertClassifierNodeDialog() {
		settings = new BertClassifierSettings();

		sentenceColumn = new DialogComponentColumnNameSelection(settings.getSentenceColumnModel(), "Sentence column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);
		classColumn = new DialogComponentColumnNameSelection(settings.getClassColumnModel(), "Class column",
				BertClassifierNodeModel.PORT_DATA_TABLE, StringValue.class);

		addTab("Settings", new SettingsTabGroup().getComponentGroupPanel());
		addTab("Advanced", createAdvancedSettingsTab());
	}

	private JComponent createAdvancedSettingsTab() {
		optimizer = new OptimizerSettingsEditor(settings.getOptimizerSettings());
		optimizer.setBorder(BorderFactory.createTitledBorder("Optimizer"));

		Box box = new Box((BoxLayout.Y_AXIS));
		box.add(new TrainingSettingsGroup().getComponentGroupPanel());
		box.add(optimizer);
		return box;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		sentenceColumn.loadSettingsFrom(settings, specs);
		classColumn.loadSettingsFrom(settings, specs);
		optimizer.settingsLoaded();

		this.settings.getValidationBatchSizeModel()
				.setEnabled(specs[BertClassifierNodeModel.PORT_VALIDATION_TABLE] != null);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

	private class SettingsTabGroup extends AbstractGridBagDialogComponentGroup {
		public SettingsTabGroup() {
			addDoubleColumnRow(new JLabel("Sentence column"),
					getFirstComponent(sentenceColumn, ColumnSelectionPanel.class));
			addDoubleColumnRow(new JLabel("Class column"), getFirstComponent(classColumn, ColumnSelectionPanel.class));
			addNumberSpinnerRowComponent(settings.getMaxSeqLengthModel(), "Max sequence length", 1);
			addStringEditRowComponent(settings.getClassSeparatorModel(), "Class separator");
		}
	}

	private class TrainingSettingsGroup extends AbstractGridBagDialogComponentGroup {
		public TrainingSettingsGroup() {
			addNumberSpinnerRowComponent(settings.getEpochsModel(), "Number of epochs", 1);
			addNumberSpinnerRowComponent(settings.getBatchSizeModel(), "Batch size", 1);
			addNumberSpinnerRowComponent(settings.getValidationBatchSizeModel(), "Validation batch size", 1);
			addCheckboxRow(settings.getFineTuneBertModel(), "Fine tune BERT", true);
			getComponentGroupPanel().setBorder(BorderFactory.createTitledBorder("Training settings"));
		}
	}
}

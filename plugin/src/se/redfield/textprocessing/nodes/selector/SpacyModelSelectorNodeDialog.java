/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

public class SpacyModelSelectorNodeDialog extends NodeDialogPane {

	private final SpacyModelSelectorNodeSettings settings;

	private JTable modelsTable;

	public SpacyModelSelectorNodeDialog() {
		settings = new SpacyModelSelectorNodeSettings();

		addTab("Settings", createSettingsTab());
	}

	private JComponent createSettingsTab() {
		JScrollPane pane = new JScrollPane(createModelsTable());
		pane.setBorder(BorderFactory.createTitledBorder("Spacy Model Selection"));
		pane.setPreferredSize(new Dimension(400, 200));
		return pane;
	}

	private JTable createModelsTable() {
		ModelsTableModel model = new ModelsTableModel();
		modelsTable = new JTable(model);
		modelsTable.setRowSelectionAllowed(true);
		modelsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelsTable.getSelectionModel().addListSelectionListener(e -> onTableSelectionChanged());
		modelsTable.setAutoCreateRowSorter(true);

		return modelsTable;
	}

	private void onTableSelectionChanged() {
		int row = modelsTable.getSelectedRow();
		if (row == -1) {
			settings.setModelDef(null);
		} else {
			int idx = modelsTable.convertRowIndexToModel(row);
			settings.setModelDef(SpacyModelDefinition.list().get(idx));
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		this.settings.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			this.settings.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// ignore
		}

		onSettingsLoaded();
	}

	private void onSettingsLoaded() {
		if (settings.getModelDef() != null) {
			int idx = SpacyModelDefinition.list().indexOf(settings.getModelDef());
			if (idx > -1) {
				int row = modelsTable.convertRowIndexToView(idx);
				modelsTable.setRowSelectionInterval(row, row);
				modelsTable.scrollRectToVisible(modelsTable.getCellRect(row, 0, true));
			}
		}
	}

	private static class ModelsTableModel extends AbstractTableModel {

		private static final int COLUMN_NAME = 0;
		private static final int COLUMN_LANG = 1;
		private static final int COLUMN_SIZE = 2;
		private static final int COLUMN_VERSION = 3;
		private static final String[] COLUMN_NAMES = { "Name", "Language", "Size", "Version" };

		@Override
		public int getRowCount() {
			return SpacyModelDefinition.list().size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			SpacyModelDefinition row = SpacyModelDefinition.list().get(rowIndex);
			switch (columnIndex) {
			case COLUMN_NAME:
				return row.getName();
			case COLUMN_LANG:
				return row.getLang();
			case COLUMN_SIZE:
				return row.getSize();
			case COLUMN_VERSION:
				return row.getVersion();
			default:
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}

	}
}

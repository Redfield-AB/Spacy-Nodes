/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.selector;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.data.location.variable.FSLocationVariableType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.DialogComponentReaderFileChooser;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;

import se.redfield.textprocessing.core.model.SpacyModelDefinition;
import se.redfield.textprocessing.nodes.selector.SpacyModelSelectorNodeSettings.SpacyModelSelectionMode;

/**
 * The node dialog for the SpaCy model selector node.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelSelectorNodeDialog extends NodeDialogPane {

	private final SpacyModelSelectorNodeSettings settings;

	private JPanel cards;

	private Map<SpacyModelSelectionMode, JRadioButton> modeButtons;

	private JTable modelsTable;
	private TableRowSorter<ModelsTableModel> tableRowSorter;

	private DialogComponentReaderFileChooser fileChooser;

	private ModelsRowFilter langFilter;
	private ModelsRowFilter versionFilter;

	/**
	 * @param portsConfig the ports configuration.
	 */
	public SpacyModelSelectorNodeDialog(PortsConfiguration portsConfig) {
		settings = new SpacyModelSelectorNodeSettings(portsConfig);

		addTab("Settings", createSettingsTab());
	}

	private JComponent createSettingsTab() {
		GridBagConstraints c = new GridBagConstraints();
		JPanel panel = new JPanel(new GridBagLayout());

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;

		panel.add(createRadioButtonsPanel(), c);

		c.gridy += 1;
		panel.add(createCardsPanel(), c);

		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridy += 1;
		panel.add(Box.createVerticalGlue(), c);
		return panel;
	}

	private JComponent createRadioButtonsPanel() {
		modeButtons = new EnumMap<>(SpacyModelSelectionMode.class);
		ButtonGroup group = new ButtonGroup();
		JPanel panel = new JPanel();

		for (SpacyModelSelectionMode mode : SpacyModelSelectionMode.values()) {
			JRadioButton rb = createModeRadiobutton(group, mode);
			panel.add(rb);
		}

		return panel;
	}

	private JRadioButton createModeRadiobutton(ButtonGroup group, SpacyModelSelectionMode mode) {
		JRadioButton rb = new JRadioButton(mode.toString());
		rb.addActionListener(e -> onSelectionModeChanged(mode));

		group.add(rb);
		modeButtons.put(mode, rb);
		return rb;
	}

	private void onSelectionModeChanged(SpacyModelSelectionMode mode) {
		settings.setSelectionMode(mode);
		((CardLayout) cards.getLayout()).show(cards, mode.name());
	}

	private JComponent createCardsPanel() {
		cards = new JPanel(new CardLayout());
		cards.add(createModelSelectorPanel(), SpacyModelSelectionMode.SPACY.name());
		cards.add(createFileChooserPanel(), SpacyModelSelectionMode.LOCAL.name());
		return cards;
	}

	private JComponent createModelSelectorPanel() {
		Box box = new Box(BoxLayout.Y_AXIS);

		JScrollPane pane = new JScrollPane(createModelsTable());
		pane.setBorder(BorderFactory.createTitledBorder("Spacy Model Selection"));
		pane.setPreferredSize(new Dimension(400, 200));

		box.add(createFiltersPanel());
		box.add(pane);

		return box;
	}

	private JComponent createFiltersPanel() {
		langFilter = new ModelsRowFilter(collectValues(SpacyModelDefinition::getLang), ModelsTableModel.COLUMN_LANG);
		versionFilter = new ModelsRowFilter(collectValues(SpacyModelDefinition::getVersion),
				ModelsTableModel.COLUMN_VERSION);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(langFilter.getComponent());
		panel.add(versionFilter.getComponent());
		return panel;
	}

	private static List<String> collectValues(Function<SpacyModelDefinition, String> func) {
		return SpacyModelDefinition.list().stream().map(func).distinct().collect(Collectors.toList());
	}

	private JTable createModelsTable() {
		ModelsTableModel model = new ModelsTableModel();
		modelsTable = new JTable(model);
		modelsTable.setRowSelectionAllowed(true);
		modelsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelsTable.getSelectionModel().addListSelectionListener(e -> onTableSelectionChanged());
		modelsTable.setAutoCreateRowSorter(true);

		tableRowSorter = new TableRowSorter<>(model);
		modelsTable.setRowSorter(tableRowSorter);

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

	private JComponent createFileChooserPanel() {
		SettingsModelReaderFileChooser pathModel = settings.getLocalPathModel();
		final FlowVariableModel fvm = createFlowVariableModel(pathModel.getKeysForFSLocation(),
				FSLocationVariableType.INSTANCE);

		fileChooser = new DialogComponentReaderFileChooser(settings.getLocalPathModel(), "spacy-model", fvm);
		return fileChooser.getComponentPanel();
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

		fileChooser.loadSettingsFrom(settings, specs);
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

		SpacyModelSelectionMode mode = settings.getSelectionMode();
		modeButtons.get(mode).setSelected(true);
		onSelectionModeChanged(mode);
	}

	private static class ModelsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

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

	private class ModelsRowFilter extends RowFilter<ModelsTableModel, Integer> {
		private static final String ALL_FILTER = "All";

		private final JComboBox<String> combo;
		private final int column;

		public ModelsRowFilter(Collection<String> values, int column) {
			this.column = column;

			List<String> items = new ArrayList<>();
			items.add(ALL_FILTER);
			items.addAll(values);
			combo = new JComboBox<>(items.toArray(String[]::new));
			combo.addActionListener(e -> applyFilters());
		}

		private void applyFilters() {
			tableRowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(langFilter, versionFilter)));
		}

		@Override
		public boolean include(Entry<? extends ModelsTableModel, ? extends Integer> entry) {
			String selected = (String) combo.getSelectedItem();
			if (ALL_FILTER.equals(selected)) {
				return true;
			}

			String value = (String) entry.getModel().getValueAt(entry.getIdentifier(), column);

			return selected == null || selected.equals(value);
		}

		public JComponent getComponent() {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel label = new JLabel(ModelsTableModel.COLUMN_NAMES[column] + ":");
			panel.add(label);
			panel.add(combo);
			return panel;
		}
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

final class DirectoryChooser {

	private final DirectoryFieldEditor m_cacheDirChooser;
	
	DirectoryChooser(final String name, String label, Composite parent, SettingsModelString model) {
		m_cacheDirChooser = new DirectoryFieldEditor(name, label, parent);
		m_cacheDirChooser.setStringValue(model.getStringValue());
		model.addChangeListener(e -> m_cacheDirChooser.setStringValue(model.getStringValue()));
		m_cacheDirChooser.getTextControl(parent).addListener(SWT.Traverse, event -> {
			model.setStringValue(m_cacheDirChooser.getStringValue());
			if (event.detail == SWT.TRAVERSE_RETURN) {
				event.doit = false;
			}
		});
		m_cacheDirChooser
				.setPropertyChangeListener(event -> model.setStringValue(m_cacheDirChooser.getStringValue()));
	}
}

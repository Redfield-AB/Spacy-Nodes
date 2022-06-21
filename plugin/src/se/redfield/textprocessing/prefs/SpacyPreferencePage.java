/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import se.redfield.textprocessing.SpacyPlugin;

/**
 * The preference page.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Creates new instance.
	 */
	public SpacyPreferencePage() {
		super();
		setPreferenceStore(SpacyPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

	@Override
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(SpacyPreferenceInitializer.PREF_CACHE_DIR, "Cache dir",
				getFieldEditorParent()));
	}
}

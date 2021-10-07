/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nodepit.licensing.LicensableProduct;
import com.nodepit.licensing.LicenseManager;
import com.nodepit.licensing.ui.LicenseFileFieldEditor;

import se.redfield.textprocessing.SpacyPlugin;

public class SpacyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
		LicensableProduct product = LicenseManager.getProduct("se.redfield.product");

		addField(new DirectoryFieldEditor(SpacyPreferenceInitializer.PREF_CACHE_DIR, "Cache dir",
				getFieldEditorParent()));
		addField(new LicenseFileFieldEditor(product, getFieldEditorParent()));
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nodepit.licensing.LicensableProduct;
import com.nodepit.licensing.LicenseManager;
import com.nodepit.licensing.ui.LicenseFileFieldEditor;

public class LicensePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	@Override
	protected void createFieldEditors() {
		LicensableProduct product = LicenseManager.getProduct("se.redfield.product");
		addField(new LicenseFileFieldEditor(product, getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing to do
	}

}

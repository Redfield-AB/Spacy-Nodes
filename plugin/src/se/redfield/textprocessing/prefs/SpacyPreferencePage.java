/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import se.redfield.textprocessing.SpacyPlugin;

public class SpacyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor tfCacheDir;

	public SpacyPreferencePage() {
		super();
		setPreferenceStore(SpacyPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.LEFT);
		mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl = new GridLayout(1, true);
		gl.verticalSpacing = 15;
		mainComposite.setLayout(gl);

		Group cacheDirGroup = new Group(mainComposite, SWT.SHADOW_ETCHED_IN);
		cacheDirGroup.setText("spaCy models cache dir");
		cacheDirGroup.setLayout(new GridLayout(2, false));
		cacheDirGroup.setLayoutData(getGridData());

		tfCacheDir = new DirectoryFieldEditor(SpacyPreferenceInitializer.PREF_CACHE_DIR, "Cache dir", cacheDirGroup);
		tfCacheDir.setPage(this);
		tfCacheDir.setPreferenceStore(getPreferenceStore());
		tfCacheDir.load();

		return mainComposite;
	}

	private static final GridData getGridData() {
		return new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
	}

	@Override
	protected void performDefaults() {
		tfCacheDir.loadDefault();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		tfCacheDir.store();
		return super.performOk();
	}
}

/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.prefs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

final class PreferenceUtils {

	static Group createGroup(Composite container, final String groupName) {
		final Group envConfigGroup = new Group(container, SWT.NONE);
		envConfigGroup.setText(groupName);
		final var layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		envConfigGroup.setLayout(layout);
		envConfigGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return envConfigGroup;
	}

	private PreferenceUtils() {

	}
}

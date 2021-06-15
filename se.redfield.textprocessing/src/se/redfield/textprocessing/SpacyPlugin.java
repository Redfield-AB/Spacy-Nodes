/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.nodepit.licensing.LicenseException;
import com.nodepit.licensing.LicenseInformation;
import com.nodepit.licensing.LicenseManager;

public class SpacyPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static SpacyPlugin plugin;

	/**
	 * The constructor.
	 */
	public SpacyPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called when the plug-in is stopped.
	 * 
	 * @param context The OSGI bundle context
	 * @throws Exception If this plugin could not be stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return Singleton instance of the Plugin
	 */
	public static SpacyPlugin getDefault() {
		return plugin;
	}

	public static void checkLicense() {
		LicenseInformation license;
		try {
			license = LicenseManager.getProduct("se.redfield.product").getLicense();
		} catch (LicenseException e) {
			throw new IllegalStateException(e.getMessage());
		}

	}
}

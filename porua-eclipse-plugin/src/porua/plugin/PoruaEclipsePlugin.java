package porua.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginUtility;

/**
 * The activator class controls the plug-in life cycle
 */
public class PoruaEclipsePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "porua.plugin.PoruaEclipsePlugin"; //$NON-NLS-1$

	// The shared instance
	private static PoruaEclipsePlugin plugin;

	/**
	 * The constructor
	 */
	public PoruaEclipsePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		try {
			IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
			if ((pref.get(PluginConstants.KEY_PORUA_HOME, null) == null)
					|| (pref.get(PluginConstants.KEY_MAVEN_HOME, null) == null)) {
				MessageDialog.openInformation(getWorkbench().getActiveWorkbenchWindow().getShell(), "Message",
						"Please configure the plugin in Preferences->Porua");
				;
			} else {
				PluginUtility.configurePlugin();
			}

		} catch (Exception e) {
			// PluginTagUtility.clearMaps();
			PluginUtility.pluginLogger(IStatus.ERROR, e.getMessage());
			MessageDialog.openError(getWorkbench().getActiveWorkbenchWindow().getShell(), "Error", e.getMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PoruaEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative
	 * path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}

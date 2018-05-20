package porua.plugin.utility;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import porua.plugin.PoruaEclipsePlugin;

public class PluginClassLoader {

	private static URLClassLoader poruaClassLoader = null;
	private static URLClassLoader paletteClassLoader = null;

	/**
	 * Classloader for Porua related libs.
	 * 
	 * @return
	 */
	public static ClassLoader getPoruaClassloader() throws Exception {
		try {
			if (poruaClassLoader == null) {
				IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
				String poruaHome = pref.get(PluginConstants.KEY_PORUA_HOME, null);

				List<URL> rootUrls = getJarUrls(poruaHome);
				List<URL> libUrls = getJarUrls(poruaHome.concat("/lib"));
				rootUrls.addAll(libUrls);
				poruaClassLoader = URLClassLoader.newInstance(rootUrls.toArray(new URL[rootUrls.size()]));
			}
		} catch (Exception e) {
			throw new Exception("Invalid Porua home");
		}
		return poruaClassLoader;
	}

	public static void configureClassLoaders() throws Exception {
		poruaClassLoader = null;
		paletteClassLoader = null;
		getPoruaClassloader();
		getPaletteClassLoader();
	}

	/**
	 * Classloader for palette libs.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static URLClassLoader getPaletteClassLoader() throws Exception {
		try {
			if (paletteClassLoader == null) {
				IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
				String poruaHome = pref.get(PluginConstants.KEY_PORUA_HOME, null);

				String palleteJarPath = poruaHome.concat("/plugin");
				List<URL> urls = getJarUrls(palleteJarPath);
				paletteClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
			}
		} catch (Exception e) {
			throw new Exception("Invalid Porua home");
		}
		return paletteClassLoader;
	}

	private static List<URL> getJarUrls(String libDir) throws Exception {
		File[] jars = new File(libDir).listFiles();
		List<URL> urls = new ArrayList<>();
		for (File jar : jars) {
			if (jar.getName().endsWith(".jar")) {
				urls.add(jar.toURI().toURL());
			}
		}
		return urls;
	}
}

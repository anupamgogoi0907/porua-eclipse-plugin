package porua.plugin.utility;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class PluginClassLoader {

	public static String PORUA_HOME = "";
	public static String LIB_PATH = "";
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
				List<URL> rootUrls = getJarUrls(PORUA_HOME);
				List<URL> libUrls = getJarUrls(PORUA_HOME.concat("/lib"));
				rootUrls.addAll(libUrls);
				poruaClassLoader = URLClassLoader.newInstance(rootUrls.toArray(new URL[rootUrls.size()]));
			}
		} catch (Exception e) {
			throw new Exception("Invalid Porua home");
		}
		return poruaClassLoader;
	}

	public static void configureClassLoaders(String poruaHome) throws Exception {
		poruaClassLoader = null;
		paletteClassLoader = null;
		PORUA_HOME = poruaHome;
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
				LIB_PATH = PORUA_HOME.concat("/plugin");
				List<URL> urls = getJarUrls(LIB_PATH);
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

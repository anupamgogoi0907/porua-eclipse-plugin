package porua.plugin.utility;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.osgi.framework.Bundle;

import porua.plugin.PoruaEclipsePlugin;

@SuppressWarnings("restriction")
public class PluginUtility {
	public static String projectName = "";

	public static void configurePlugin(String poruaHome) throws Exception {
		PluginClassLoader.configureClassLoaders(poruaHome);
		PluginTagUtility.loadTags();
	}

	public static void setProject(Object obj) throws Exception {
		org.eclipse.core.internal.resources.File file = (org.eclipse.core.internal.resources.File) obj;
		projectName = file.getProject().getName();
	}

	public static File readBundleResource(String filePath) {
		File file = null;
		try {
			Bundle bundle = Platform.getBundle(PoruaEclipsePlugin.PLUGIN_ID);
			URL fileURL = bundle.getEntry(filePath);
			fileURL = FileLocator.toFileURL(fileURL);
			file = URIUtil.toFile(URIUtil.toURI(fileURL));
			System.out.println(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static Properties readPropertyFile() {
		Properties props = new Properties();
		try {
			FileInputStream is = new FileInputStream(readBundleResource("resources/config/init.properties"));
			props.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}

}

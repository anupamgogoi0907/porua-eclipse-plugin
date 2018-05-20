package porua.plugin.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.framework.Bundle;

import porua.plugin.PoruaEclipsePlugin;

@SuppressWarnings("restriction")
public class PluginUtility {

	public static IProject project = null;

	public static void configurePlugin() throws Exception {
		PluginClassLoader.configureClassLoaders();
		PluginTagUtility.loadTags();
	}

	public static void setProject(Object obj) throws Exception {
		if (obj instanceof Project) {
			Project p = (Project) obj;
			project = p.getProject();
		} else if (obj instanceof JavaProject) {
			JavaProject jp = (JavaProject) obj;
			project = jp.getProject();
		} else if (obj instanceof org.eclipse.core.internal.resources.File) {
			org.eclipse.core.internal.resources.File file = (org.eclipse.core.internal.resources.File) obj;
			project = file.getProject();
		}
	}

	public static void buildProject(String root) throws Exception {
		IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
		String mavenHome = pref.get(PluginConstants.KEY_MAVEN_HOME, null);

		try {
			ProcessBuilder builder = new ProcessBuilder(mavenHome.concat("/bin/mvn"), "clean", "install");
			builder.directory(new File(root));
			Process process = builder.start();
			copy(process.getInputStream(), System.out);
			process.waitFor();
			executeApp(root);
		} catch (Exception e) {
			throw new Exception("Could not build the project.Maven error.");
		}

	}

	private static void executeApp(String root) throws Exception {
		File[] files = new File(root).listFiles();
		for (File file : files) {
			if (!file.isDirectory() && file.getName().endsWith(".jar")) {
				ClassLoader loader = PluginClassLoader.getPoruaClassloader();
				Class<?> clazz = loader.loadClass("com.porua.container.PoruaContainer");
				Method mehtod = clazz.getDeclaredMethod("scanSingleApp");
				mehtod.invoke(null, file);
				break;
			}
		}

	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		while (true) {
			int c = in.read();
			if (c == -1)
				break;
			out.write((char) c);
		}
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

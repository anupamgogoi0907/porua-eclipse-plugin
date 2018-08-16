package porua.plugin.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import porua.plugin.PoruaEclipsePlugin;

public class PluginUtility {

	/**
	 * Condifure plugin.
	 * 
	 * @throws Exception
	 */
	public static void configurePlugin() throws Exception {
		PluginClassLoader.configureClassLoaders();
		PluginTagUtility.clearMaps();
		PluginTagUtility.loadTags();
	}

	/**
	 * Build/Compile project using Maven.
	 * 
	 * @param root
	 * @param action
	 * @throws Exception
	 */
	public static void buildProject(String root, String action) throws Exception {
		try {
			IEclipsePreferences pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
			String mavenHome = pref.get(PluginConstants.KEY_MAVEN_HOME, null);

			ProcessBuilder builder = new ProcessBuilder(mavenHome.concat("/bin/mvn"), "clean", action);
			builder.directory(new File(root));
			Process process = builder.start();
			copy(process.getInputStream(), System.out);
			process.waitFor();
			// executeApp(root);
		} catch (Exception e) {
			PluginUtility.pluginLogger(IStatus.ERROR, e.getMessage());
		}
	}

	/**
	 * Execute app from eclipse.
	 * 
	 * @param root
	 * @throws Exception
	 */
	public static void executeApp(String root) throws Exception {
		File[] files = new File(root).listFiles();
		for (File file : files) {
			if (!file.isDirectory() && file.getName().endsWith(".jar")) {
				ClassLoader loader = PluginClassLoader.getPoruaClassloader();
				Class<?> clazz = loader.loadClass("com.porua.container.PoruaContainer");
				Method mehtod = clazz.getDeclaredMethod("scanSingleApp", File.class);
				mehtod.invoke(null, file);
				break;
			}
		}

	}

	/**
	 * Log Maven.
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private static void copy(InputStream in, OutputStream out) throws IOException {
		String result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
		pluginLogger(IStatus.INFO, result);
		PluginUtility.pluginLogger(IStatus.INFO, result);
	}

	/**
	 * Eclipse plugin logger.
	 * 
	 * @param level
	 * @param msg
	 */
	public static void pluginLogger(int level, String msg) {
		Status status = new Status(level, PoruaEclipsePlugin.PLUGIN_ID, msg);
		PoruaEclipsePlugin.getDefault().getLog().log(status);
	}

	/**
	 * Read bundle resource.
	 * 
	 * @param filePath
	 * @return
	 */
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

	/**
	 * Read property file.
	 * 
	 * @return
	 */
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

	/**
	 * Get current project.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static IProject getCurrentProject() throws Exception {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		if (activeEditor != null) {
			FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
			IProject project = input.getFile().getProject();
			return project;
		} else {
			return null;
		}
	}

}

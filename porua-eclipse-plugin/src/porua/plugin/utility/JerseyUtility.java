package porua.plugin.utility;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

public class JerseyUtility {

	/**
	 * Generate Jaxrs api class.
	 */
	public static void generateApiClass() {
		try {
			IProject project = PluginUtility.getCurrentProject();
			String apiPath = project.getLocation().toString().concat(PluginConstants.RESOURCE_FOLDER);
			File apiFile = new File(apiPath).listFiles()[0];
			if (apiFile != null) {
				String srcPath = project.getLocation().toString().concat(PluginConstants.SRC_FOLDER);
				ClassLoader loader = PluginClassLoader.getPoruaClassloader();
				Class<?> clazz = loader.loadClass("com.porua.api.utility.ApiGen");
				Method m = clazz.getDeclaredMethod("generateApiClass", String.class, String.class);
				m.invoke(null, apiFile.getAbsolutePath(), srcPath);
			}
		} catch (Exception e) {
			PluginUtility.pluginLogger(IStatus.ERROR, e.getMessage());
		}
	}

	/**
	 * This method executes only for API Router.
	 */
	public static List<String> loadJerseyPaths() {
		try {
			IProject project = PluginUtility.getCurrentProject();
			URLClassLoader poruaClassLoader = (URLClassLoader) PluginClassLoader.getPoruaClassloader();
			List<URL> listUrl = new ArrayList<>();
			Stream.of(poruaClassLoader.getURLs()).forEach(url -> {
				listUrl.add(url);
			});
			File target = new File(project.getLocation().toString().concat("/target/classes"));
			listUrl.add(target.toURI().toURL());
			URLClassLoader loader = URLClassLoader.newInstance(listUrl.toArray(new URL[listUrl.size()]));
			Class<?> clazz = loader.loadClass("api.generated.MyAPI");
			List<String> list = getJerseyPaths(clazz);
			return list;
		} catch (Exception e) {
			PluginUtility.pluginLogger(IStatus.ERROR, e.getMessage());
			return null;
		}
	}

	/**
	 * Returns Jersey paths in the format GET:/somepath
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<String> getJerseyPaths(Class<?> clazz) {
		List<String> listPath = new ArrayList<>();
		Stream.of(clazz.getDeclaredMethods()).forEach(m -> {
			String[] arr = new String[2];
			Annotation[] annotations = m.getDeclaredAnnotations();
			Stream.of(annotations).forEach(annot -> {
				try {
					Class<? extends Annotation> type = annot.annotationType();
					if (PluginConstants.HTTP_METHODS.contains(type.getSimpleName())) {
						arr[0] = type.getSimpleName();
					}
					if (type.getSimpleName().equalsIgnoreCase("Path")) {
						String value = (String) type.getDeclaredMethod("value").invoke(annot);
						arr[1] = value;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			});
			listPath.add(arr[0] + ":" + arr[1]);

		});
		return listPath;
	}
}

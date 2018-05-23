package porua.plugin.utility;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.graphics.ImageData;

import porua.plugin.PoruaEclipsePlugin;
import porua.plugin.pojos.TagData;

@SuppressWarnings({ "deprecation" })
public class PluginTagUtility {

	public static Map<String, List<TagData>> mapNsTags = new HashMap<>();
	public static Map<String, List<TagData>> mapPrefixTags = new HashMap<>();

	public static ImageData getImageByTag(String tagComponent) {
		ImageData imgData = null;
		try {
			TagData pojo = PluginTagUtility.getTagDataByTagName(tagComponent);
			imgData = new ImageData(PluginClassLoader.getPaletteClassLoader().getResourceAsStream(pojo.getImageName()));
			imgData.scaledTo(100, 50);
		} catch (Exception e) {
			imgData = PoruaEclipsePlugin.getImageDescriptor(PluginConstants.ICONS_PATH.concat("default.png"))
					.getImageData();
		}
		return imgData;
	}

	public static ImageData getImageByTag(TagData tagData) {
		ImageData imgData = null;
		try {
			imgData = new ImageData(
					PluginClassLoader.getPaletteClassLoader().getResourceAsStream(tagData.getImageName()));
			imgData.scaledTo(100, 50);
		} catch (Exception e) {
			imgData = PoruaEclipsePlugin.getImageDescriptor(PluginConstants.ICONS_PATH.concat("default.png"))
					.getImageData();
		}
		return imgData;
	}

	public static TagData getTagDataByTagName(String tagComponent) {
		String[] arr = tagComponent.split(":"); // prefix:component
		List<TagData> listTag = mapPrefixTags.get(arr[0]);
		TagData tagData = listTag.stream().filter(t -> t.getTag().equals(arr[1])).findFirst().get();
		return tagData;
	}

	public static List<TagData> getAllTags() {
		List<TagData> listAllTags = new ArrayList<>();
		for (String ns : mapNsTags.keySet()) {
			List<TagData> listTag = mapNsTags.get(ns);
			listAllTags.addAll(listTag);
		}
		return listAllTags;
	}

	public static void loadTags() throws Exception {
		URLClassLoader loader = PluginClassLoader.getPaletteClassLoader();
		for (URL url : loader.getURLs()) {
			JarFile jarFile = new JarFile(new File(url.toURI()));
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry en = entries.nextElement();
				if (en.getName().endsWith(".class")) {
					String className = en.getName().substring(0, en.getName().length() - 6);
					className = className.replace('/', '.');
					Class<?> clazz = loader.loadClass(className);
					TagData tagData = makeTagData(clazz);
					if (tagData != null) {
						mapNamespaceToTag(tagData);
					}
				}
			}
			jarFile.close();
		}
		generatePrefixForTag();
	}

	public static TagData makeTagData(Class<?> clazz) throws Exception {
		Map<String, Object> mapConnector = checkForConnector(clazz);
		if (mapConnector != null) {
			String connTagName = (String) mapConnector.get("tagName");
			String connTagNamespace = (String) mapConnector.get("tagNamespace");
			String connTagSchemaLocation = (String) mapConnector.get("tagSchemaLocation");
			String connImageName = (String) mapConnector.get("imageName");
			TagData tagData = new TagData(connTagName, connTagNamespace, connTagSchemaLocation, connImageName);

			// Fields
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				Map<String, Object> mapConfig = checkForConnectorConfig(field);
				if (mapConfig == null) {
					tagData.getProps().add(field.getName());
				} else {
					String configTagName = (String) mapConfig.get("tagName");
					String configName = (String) mapConfig.get("configName");
					TagData tagConfigData = new TagData(configTagName, connTagNamespace, connTagSchemaLocation, "");
					for (Field f : field.getType().getDeclaredFields()) {
						tagConfigData.getProps().add(f.getName());
					}
					tagData.getConfig().put(configName, tagConfigData);
				}
			}
			return tagData;
		} else {
			return null;
		}
	}

	public static void mapNamespaceToTag(TagData tagData) {
		List<TagData> listTag = mapNsTags.get(tagData.getTagNamespace());
		listTag = (listTag == null) ? new ArrayList<>() : listTag;
		listTag.add(tagData);
		mapNsTags.put(tagData.getTagNamespace(), listTag);

		if (tagData.getConfig() != null && tagData.getConfig().size() != 0) {
			mapNamespaceToTag((TagData) tagData.getConfig().values().toArray()[0]);
		}
	}

	public static void generatePrefixForTag() {
		int nsCount = 0;
		for (String ns : mapNsTags.keySet()) {
			String prefix = "ns" + (++nsCount);
			List<TagData> list = mapNsTags.get(ns);
			list.stream().forEach(t -> t.setTagNamespacePrefix(prefix));

			mapPrefixTags.put(prefix, list);
		}
	}

	/**
	 * I am not sure why I'm doing this. Inside Eclipse I could not use
	 * clazz.getAnnotation(Connector.class)
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> checkForConnector(Class<?> clazz) throws Exception {
		Map<String, Object> map = null;
		Annotation[] arr = clazz.getAnnotations();
		if (arr.length != 0) {
			Annotation annot = arr[0];
			Class<? extends Annotation> type = annot.annotationType();
			if (type.getName().equals("com.porua.core.tag.Connector")) {
				map = new HashMap<>();
				String tagName = (String) type.getMethod("tagName").invoke(annot);
				String tagNamespace = (String) type.getMethod("tagNamespace").invoke(annot);
				String tagSchemaLocation = (String) type.getMethod("tagSchemaLocation").invoke(annot);
				String imageName = (String) type.getMethod("imageName").invoke(annot);

				map.put("tagName", tagName);
				map.put("tagNamespace", tagNamespace);
				map.put("tagSchemaLocation", tagSchemaLocation);
				map.put("imageName", imageName);
				return map;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static Map<String, Object> checkForConnectorConfig(Field field) throws Exception {
		Map<String, Object> map = null;
		Annotation[] arr = field.getAnnotations();
		if (arr.length != 0) {
			Annotation annot = arr[0];
			Class<? extends Annotation> type = annot.annotationType();
			if (type.getName().equals("com.porua.core.tag.ConnectorConfig")) {
				map = new HashMap<>();
				String tagName = (String) type.getMethod("tagName").invoke(annot);
				String configName = (String) type.getMethod("configName").invoke(annot);

				map.put("tagName", tagName);
				map.put("configName", configName);
				return map;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static void clearMaps() {
		mapNsTags = new HashMap<>();
		mapPrefixTags = new HashMap<>();
	}
}

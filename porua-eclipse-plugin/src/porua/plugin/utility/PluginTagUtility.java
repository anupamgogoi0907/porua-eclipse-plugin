package porua.plugin.utility;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.ImageData;
import org.w3c.dom.Node;

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
			imgData = PoruaEclipsePlugin.getImageDescriptor(PluginConstants.ICONS_PATH.concat("default.png")).getImageData();
		}
		return imgData;
	}

	public static ImageData getImageByTag(TagData tagData) {
		ImageData imgData = null;
		try {
			imgData = new ImageData(PluginClassLoader.getPaletteClassLoader().getResourceAsStream(tagData.getImageName()));
			imgData.scaledTo(100, 50);
		} catch (Exception e) {
			imgData = PoruaEclipsePlugin.getImageDescriptor(PluginConstants.ICONS_PATH.concat("default.png")).getImageData();
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

	/**
	 * Load tags from jar files under $PORUA_HOME/plugin
	 * 
	 * @throws Exception
	 */
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
					TagData tagData = makeTagDataForConnector(clazz);
					if (tagData != null) {
						mapNamespaceToTag(tagData);
					}
				}
			}
			jarFile.close();
		}
		generatePrefixForTag();
	}

	/**
	 * Make tags for the connector.
	 * 
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static TagData makeTagDataForConnector(Class<?> clazz) throws Exception {
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
				Map<String, Object> mapConfigProp = checkForConfigProperty(field);

				// Simple property.
				if (mapConfigProp != null) {
					Class<?> classEnum = (Class<?>) mapConfigProp.get("enumClass");
					if (Void.class == classEnum) {
						tagData.getAttributes().add(field.getName());
					} else {
						Enum<?>[] values = (Enum<?>[]) classEnum.getEnumConstants();
						List<Object> listValue = Arrays.asList(values).stream().map(e -> e.name()).collect(Collectors.toList());
						tagData.getAttributeValues().put(field.getName(), listValue);
					}
				}
				// Separate configuration class.
				Map<String, TagData> mapConnectorConfig = makeTagDataForConnectorConfig(field, mapConnector);
				if (mapConnectorConfig != null) {
					tagData.getConfig().put(mapConnectorConfig.keySet().iterator().next(), mapConnectorConfig.values().iterator().next());
				}
			}
			return tagData;
		} else {
			return null;
		}
	}

	/**
	 * Create Tag for connector configuration.
	 * 
	 * @param field
	 * @param mapConnector
	 * @return
	 * @throws Exception
	 */
	public static Map<String, TagData> makeTagDataForConnectorConfig(Field field, Map<String, Object> mapConnector)
			throws Exception {
		// Check if field is annonated with @ConnectorConfig.
		Map<String, Object> mapConnectorConfig = checkForConnectorConfig(field);

		if (mapConnectorConfig != null) {
			// Connector.
			String connTagNamespace = (String) mapConnector.get("tagNamespace");
			String connTagSchemaLocation = (String) mapConnector.get("tagSchemaLocation");

			// Connector Config.
			String configTagName = (String) mapConnectorConfig.get("tagName");
			String configName = (String) mapConnectorConfig.get("configName");

			// Config tag.
			TagData tagConfigData = new TagData(configTagName, connTagNamespace, connTagSchemaLocation, "");
			for (Field f : field.getType().getDeclaredFields()) {
				Map<String, Object> mapConfigProp = checkForConfigProperty(f);
				if (mapConfigProp != null) {
					Class<?> classEnum = (Class<?>) mapConfigProp.get("enumClass");
					if (Void.class == classEnum) {
						tagConfigData.getAttributes().add(f.getName());
					} else {
						Enum<?>[] values = (Enum<?>[]) classEnum.getEnumConstants();
						List<Object> listValue = Arrays.asList(values).stream().map(e -> e.name()).collect(Collectors.toList());
						tagConfigData.getAttributeValues().put(f.getName(), listValue);
					}
				}
			}
			Map<String, TagData> map = new HashMap<>();
			map.put(configName, tagConfigData);
			return map;
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
	 * Check for @Connector annotation.
	 * 
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

	/**
	 * Check for @ConnectorConfig annotation.
	 * 
	 * @param field
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * Check for @ConfigProperty configuration.
	 * 
	 * @param field
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> checkForConfigProperty(Field field) throws Exception {
		Map<String, Object> map = null;
		Annotation[] arr = field.getAnnotations();
		if (arr.length != 0) {
			Annotation annot = arr[0];
			Class<? extends Annotation> type = annot.annotationType();
			if (type.getName().equals("com.porua.core.tag.ConfigProperty")) {
				map = new HashMap<>();
				Class<?> classEnum = (Class<?>) type.getMethod("enumClass").invoke(annot);
				map.put("enumClass", classEnum);
			}
		}
		return map;
	}

	public static void clearMaps() {
		mapNsTags = new HashMap<>();
		mapPrefixTags = new HashMap<>();
	}

	/**
	 * The Case tag is not included in the plugin jars. It has to be created
	 * manually from the Node information we have.
	 * 
	 * @param nodeCase
	 * @return
	 */
	public static TagData makeSwitchCaseTag(Node nodeCase) {
		TagData tagData = new TagData();
		tagData = new TagData();
		tagData.setTag(nodeCase.getNodeName().split(":")[1]);
		tagData.setTagNamespacePrefix(nodeCase.getNodeName().split(":")[0]);
		tagData.getAttributes().add(PluginConstants.ATTRIBUTE_TAG_SWITCH_CASE);
		return tagData;
	}
}

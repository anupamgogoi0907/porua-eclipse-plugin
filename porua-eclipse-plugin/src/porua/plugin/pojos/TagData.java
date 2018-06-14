package porua.plugin.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagData implements Serializable {
	private static final long serialVersionUID = 1L;

	private String tag;
	private String tagNamespace;
	private String tagNamespacePrefix;
	private String tagSchemaLocation;
	private String imageName;
	private List<String> attributes;
	private Map<String, TagData> config;
	private Map<String, List<Object>> attributeValues;

	public TagData() {
		this.attributes = new ArrayList<>();
		this.config = new HashMap<>();
		this.setAttributeValues(new HashMap<>());
	}

	public TagData(String tag, String tagNamespace, String tagSchemaLocation, String imageName) {
		this();
		this.tag = tag;
		this.tagNamespace = tagNamespace;
		this.tagSchemaLocation = tagSchemaLocation;
		this.imageName = imageName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> props) {
		this.attributes = props;
	}

	public Map<String, TagData> getConfig() {
		return config;
	}

	public void setConfig(Map<String, TagData> config) {
		this.config = config;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTagNamespace() {
		return tagNamespace;
	}

	public void setTagNamespace(String tagNamespace) {
		this.tagNamespace = tagNamespace;
	}

	public String getTagNamespacePrefix() {
		return tagNamespacePrefix;
	}

	public void setTagNamespacePrefix(String tagNamespacePrefix) {
		this.tagNamespacePrefix = tagNamespacePrefix;
	}

	public String getTagSchemaLocation() {
		return tagSchemaLocation;
	}

	public void setTagSchemaLocation(String tagSchemaLocation) {
		this.tagSchemaLocation = tagSchemaLocation;
	}

	public Map<String, List<Object>> getAttributeValues() {
		return attributeValues;
	}

	public void setAttributeValues(Map<String, List<Object>> propValues) {
		this.attributeValues = propValues;
	}

}

package porua.plugin.views.templates;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;
import porua.plugin.utility.PluginConstants;

public class ConnectorConfigAttributeView extends ViewTemplate {

	public ConnectorConfigAttributeView(PoruaXMLEditor poruaXmlEditor, ComponentData compData, Composite parent) {
		super(poruaXmlEditor, compData, parent);

	}

	@Override
	public void renderAttributes() {

	}

	@Override
	public void renderAttributes(TagData tagData, Object... param) {
		Node nodeConfig = null;

		// Check for selected configuration name.
		if (param != null && param.length != 0 && param[0] != null) {
			nodeConfig = getNode(tagData.getTagNamespacePrefix() + ":" + tagData.getTag(), PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG, (String) param[0]);
		}
		// Text Attributes ## Label & Text.
		if (tagData.getAttributes() != null && tagData.getAttributes().size() != 0) {

			makeLabelAndText(populateAttribueAndSelectedValue(nodeConfig, tagData.getAttributes()));
		}

		// Combo Attributes ## Label & Combo.
		if (tagData.getAttributeValues() != null && tagData.getAttributeValues().size() != 0) {
			makeLabelAndCombo(tagData.getAttributeValues(), populateAttribueAndSelectedValue(nodeConfig, tagData.getAttributeValues().keySet()));
		}
	}

	private Map<String, String> populateAttribueAndSelectedValue(Node node, Iterable<String> attributes) {
		Map<String, String> mapAttributeSelectedVal = new HashMap<>();
		for (String attributeName : attributes) {
			if (node == null) {
				mapAttributeSelectedVal.put(attributeName, "");
			} else {
				NamedNodeMap mapNodeConfigAtt = node.getAttributes();
				Node nodeAtt = mapNodeConfigAtt.getNamedItem(attributeName);
				mapAttributeSelectedVal.put(attributeName, nodeAtt == null ? "" : nodeAtt.getNodeValue());
			}
		}
		return mapAttributeSelectedVal;

	}
}

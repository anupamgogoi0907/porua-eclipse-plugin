package porua.plugin.views.templates;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;

public class ConnectorConfigAttributeView extends ViewTemplate {

	public ConnectorConfigAttributeView(PoruaXMLEditor poruaXmlEditor, ComponentData compData, Composite parent) {
		super(poruaXmlEditor, compData, parent);

	}

	@Override
	public void renderAttributes() {

	}

	@Override
	public void renderAttributes(TagData tagData, Object... param) {
		Map<String, String> mapAttributeSelectedVal = null;
		if (param != null && param.length != 0 && param[0] != null) {
			mapAttributeSelectedVal = getSelectedAttributes(tagData.getTagNamespacePrefix() + ":" + tagData.getTag(), (String) param[0]);
		}
		// Text Attributes ## Label & Text.
		if (tagData.getAttributes() != null && tagData.getAttributes().size() != 0) {
			Map<String, String> map = new HashMap<>();
			for (String attributeName : tagData.getAttributes()) {
				map.put(attributeName, mapAttributeSelectedVal == null ? "" : mapAttributeSelectedVal.get(attributeName));
			}
			makeLabelAndText(map);
		}

		// Combo Attributes ## Label & Combo.
		if (tagData.getAttributeValues() != null && tagData.getAttributeValues().size() != 0) {
			makeLabelAndCombo(tagData.getAttributeValues(), mapAttributeSelectedVal);
		}
	}

	/**
	 * Get the selected attributes of the Connector Configuration. It searches for
	 * the configuration tag in the xml. There might be more than one node with the
	 * same tag i.e multiple configuration. So, it compares with the
	 * {@param attributeConfigName}
	 * 
	 * @param tag
	 * @param attributeConfigName
	 * @return
	 */
	private Map<String, String> getSelectedAttributes(String tag, String attributeConfigName) {
		Map<String, String> mapSelectedAttribute = null;

		// Get nodes for the configuration tag.
		NodeList nl = poruaXmlEditor.getXmlDoc().getElementsByTagName(tag);

		// Look for the selected config-name if any.
		if (nl != null && nl.getLength() != 0) {
			for (int iNode = 0; iNode < nl.getLength(); iNode++) {
				Node nodeConfigSelected = getNode(tag, "name", attributeConfigName);

				if (nodeConfigSelected != null && nodeConfigSelected.getAttributes() != null) {
					mapSelectedAttribute = new HashMap<>();
					for (int iAtt = 0; iAtt < nodeConfigSelected.getAttributes().getLength(); iAtt++) {
						Node nodeConfigSelectedAtt = nodeConfigSelected.getAttributes().item(iAtt);
						mapSelectedAttribute.put(nodeConfigSelectedAtt.getNodeName(), nodeConfigSelectedAtt.getNodeValue());
					}
				}
			}
		}
		return mapSelectedAttribute;
	}

	
}

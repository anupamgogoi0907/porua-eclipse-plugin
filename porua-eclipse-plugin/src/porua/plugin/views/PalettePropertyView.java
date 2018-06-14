package porua.plugin.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;
import porua.plugin.utility.PluginTagUtility;
import porua.plugin.views.templates.ConnectorAttributeView;

public class PalettePropertyView extends ViewPart implements IViewData {
	public static final String ID = "porua.plugin.views.PalettePropertyView";

	private PoruaXMLEditor poruaXmlEditor;
	private Composite parent;
	private ComponentData compData;
	

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
	}

	@Override
	public void setFocus() {
		this.parent.setFocus();
	}

	@Override
	public void setViewData(PoruaXMLEditor poruaXmlEditor, Object data) {
		this.poruaXmlEditor = poruaXmlEditor;
		this.compData = (ComponentData) data;

		// init
		deleteControls();
		getTagData();
	}

	private void deleteControls() {
		for (Control c : this.parent.getChildren()) {
			c.dispose();
		}
		this.parent.pack();
	}

	/**
	 * Get TagData for the component selected in the editor.
	 */
	private void getTagData() {
		Node nodeComp = poruaXmlEditor.findNodeInFlow(compData.getGroupName(), compData.getIndex());
		TagData tagData = PluginTagUtility.getTagDataByTagName(nodeComp.getNodeName());
		if (tagData != null) {
			drawTagAttributes(nodeComp, tagData);
		}
	}


	/**
	 * Represet tag visually.
	 * 
	 * @param nodeComp
	 * @param tagData
	 */
	private void drawTagAttributes(Node nodeComp, TagData tagData) {
//		RowLayout layout = new RowLayout();
//		layout.type = SWT.VERTICAL;
//		layout.spacing = 10;
//		parent.setLayout(layout);
//
//		NamedNodeMap mapNodeCompAtt = nodeComp.getAttributes();
//
//		// Connector Text Attributes ## Label & Text.
//		if (tagData.getAttributes() != null && tagData.getAttributes().size() != 0) {
//			for (int i = 0; i < tagData.getAttributes().size(); i++) {
//				String attribute = tagData.getAttributes().get(i);
//
//				Node nodeCompAtt = null;
//				if (mapNodeCompAtt != null && mapNodeCompAtt.getLength() != 0) {
//					nodeCompAtt = mapNodeCompAtt.getNamedItem(attribute);
//				}
//				renderConnectorAttribute(attribute, nodeCompAtt == null ? "" : nodeCompAtt.getTextContent());
//			}
//
//		}
//		// Connector Enum Attributes ## Label & Combo.
//		if (tagData.getAttributeValues() != null && tagData.getAttributeValues().size() != 0) {
//			Map<String, String> mapPropOldSelVal = new HashMap<>();
//			for (String attribute : tagData.getAttributeValues().keySet()) {
//				Node nodeCompAtt = mapNodeCompAtt.getNamedItem(attribute);
//				if (nodeCompAtt != null) {
//					mapPropOldSelVal.put(attribute, nodeCompAtt.getNodeValue());
//				}
//			}
//			renderConnectorAttribute(tagData.getAttributeValues(), mapPropOldSelVal);
//		}
//
//		// Connector Configuration Attributes ## Label & Combo.
//		String configPropName = getConfigName(tagData);
//		if (configPropName != null) {
//			Node nodeConfigAtt = mapNodeCompAtt.getNamedItem(configPropName);
//			renderConnectorAttribute(tagData.getConfig(), nodeConfigAtt == null ? null : nodeConfigAtt.getTextContent());
//		}
//
//		// Save
//		if (tagData.getAttributes() != null || tagData.getConfig() != null) {
//			Button btnSave = new Button(parent, SWT.NONE);
//			btnSave.setText("Save");
//			btnSave.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					// Need to reload the Node. Otherwise reference is lost.
//					Node nc = poruaXmlEditor.findNodeInFlow(compData.getGroupName(), compData.getIndex());
//
//					// Set attribute values.
//					if (mapAttributeSelectedValue != null && mapAttributeSelectedValue.size() != 0) {
//						for (String prop : mapAttributeSelectedValue.keySet()) {
//							((Element) nc).setAttribute(prop, mapAttributeSelectedValue.get(prop));
//						}
//					}
//					poruaXmlEditor.redrawComposite();
//				}
//
//			});
//		}
//		parent.pack();
		new ConnectorAttributeView(poruaXmlEditor, compData, parent).renderAttributes();
	}

}

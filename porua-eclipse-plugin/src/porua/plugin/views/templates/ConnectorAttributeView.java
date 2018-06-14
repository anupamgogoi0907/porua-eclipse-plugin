package porua.plugin.views.templates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import porua.plugin.components.AddConfigurationDialog;
import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;
import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginTagUtility;

public class ConnectorAttributeView extends ViewTemplate {

	public ConnectorAttributeView(PoruaXMLEditor poruaXmlEditor, ComponentData compData, Composite parent) {
		super(poruaXmlEditor, compData, parent);

	}

	@Override
	public void renderAttributes(TagData data, Object... param) {
	}

	@Override
	public void renderAttributes() {
		Node nodeComp = poruaXmlEditor.findNodeInFlow(compData.getGroupName(), compData.getIndex());
		TagData tagData = PluginTagUtility.getTagDataByTagName(nodeComp.getNodeName());

		// Text Attributes ## Label & Text.
		if (tagData.getAttributes() != null && tagData.getAttributes().size() != 0) {
			makeLabelAndText(populateAttribueAndSelectedValue(nodeComp, tagData.getAttributes()));
		}

		// Enum Attributes ## Label & Combo.
		if (tagData.getAttributeValues() != null && tagData.getAttributeValues().size() != 0) {
			makeLabelAndCombo(tagData.getAttributeValues(), populateAttribueAndSelectedValue(nodeComp, tagData.getAttributeValues().keySet()));
		}

		// Configuration Attribute ## Label & Combo & Button.
		if (tagData.getConfig() != null && tagData.getConfig().size() != 0) {
			// Single configuration per connector.
			String attributeConfigName = tagData.getConfig().keySet().iterator().next();
			TagData tagDataConfig = tagData.getConfig().values().iterator().next();

			// Check for existing configuration.
			Map<String, String> mapAttributeSelectedVal = new HashMap<>();
			NamedNodeMap mapNodeCompAtt = nodeComp.getAttributes();
			Node nodeCompAtt = mapNodeCompAtt.getNamedItem(attributeConfigName);
			mapAttributeSelectedVal.put(attributeConfigName, nodeCompAtt == null ? "" : nodeCompAtt.getTextContent());

			// Render attributes.
			renderConfigAttributes(tagDataConfig, mapAttributeSelectedVal);
		}
		// Save
		if (tagData.getAttributes() != null || tagData.getConfig() != null) {
			Button btnSave = new Button(parent, SWT.NONE);
			btnSave.setText("Save");
			btnSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Need to reload the Node. Otherwise reference is lost.
					Node nc = poruaXmlEditor.findNodeInFlow(compData.getGroupName(), compData.getIndex());

					// Set attribute values.
					if (getSelectedAttValues() != null && getSelectedAttValues().size() != 0) {
						for (String prop : getSelectedAttValues().keySet()) {
							((Element) nc).setAttribute(prop, getSelectedAttValues().get(prop));
						}
					}
					poruaXmlEditor.redrawComposite();
				}

			});
		}
		// pack all.
		parent.pack();
	}

	/**
	 * Render Configuration attribute of connector.
	 * 
	 * @param tagDataConfig
	 *            TagData of Configuration.
	 * @param mapAttributeSelectedVal
	 *            Configuration name and its value.
	 */
	private void renderConfigAttributes(TagData tagDataConfig, Map<String, String> mapAttributeSelectedVal) {
		String tagConfig = tagDataConfig.getTagNamespacePrefix() + ":" + tagDataConfig.getTag();

		// Possible values for the attribute.
		List<Object> listValues = loadValuesByTagAndAttribute(tagConfig, PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG);

		for (String attributeName : mapAttributeSelectedVal.keySet()) {

			// Label and Combo.
			Group group = makeLabelAndCombo(attributeName, listValues, mapAttributeSelectedVal);
			Combo comboDropDown = (Combo) getControlByData(attributeName, group);

			// Add
			Button btnAdd = new Button(group, SWT.PUSH);
			btnAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					AddConfigurationDialog dlg = new AddConfigurationDialog(parent.getShell(), poruaXmlEditor, tagDataConfig, null);
					dlg.open();

					// Reload the combo.
					populateComboAndSelect(comboDropDown, loadValuesByTagAndAttribute(tagConfig, PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG), attributeName, mapAttributeSelectedVal.get(attributeName));

				}
			});
			btnAdd.setText("+");
			btnAdd.pack();

			// Edit.
			String selectedConfigName = comboDropDown.getSelectionIndex() == -1 ? "" : comboDropDown.getItem(comboDropDown.getSelectionIndex());
			Button btnEdit = new Button(group, SWT.PUSH);
			btnEdit.setText("..");
			btnEdit.setEnabled((selectedConfigName == null || selectedConfigName.equals("")) ? false : true);
			btnEdit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String selectedConfigName = getSelectedAttValues() == null ? "" : getSelectedAttValues().get(attributeName) == null ? "" : getSelectedAttValues().get(attributeName);
					AddConfigurationDialog dlg = new AddConfigurationDialog(parent.getShell(), poruaXmlEditor, tagDataConfig, selectedConfigName);
					dlg.open();

					// Reload the combo.
					populateComboAndSelect(comboDropDown, loadValuesByTagAndAttribute(tagConfig, PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG), attributeName, mapAttributeSelectedVal.get(attributeName));
				}
			});
		}

	}

}

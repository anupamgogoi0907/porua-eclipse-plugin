package porua.plugin.views.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;

public abstract class ViewTemplate {
	protected Composite parent;
	protected PoruaXMLEditor poruaXmlEditor;
	protected ComponentData compData;
	private Map<String, String> mapAttributeSelectedValue;

	public ViewTemplate(PoruaXMLEditor poruaXmlEditor, ComponentData compData, Composite parent) {
		this.poruaXmlEditor = poruaXmlEditor;
		this.compData = compData;
		this.parent = parent;
		this.parent.setLayout(makeVerticalLayout());

		// Init.
		this.mapAttributeSelectedValue = new HashMap<>();
	}

	public abstract void renderAttributes();

	public abstract void renderAttributes(TagData tagData, Object... param);

	/**
	 * Renders multiple label and text.
	 * 
	 * @param mapAttributeSelectedVal
	 *            Map of attribute -> selected value
	 */
	protected void makeLabelAndText(Map<String, String> mapAttributeSelectedVal) {
		for (String attributeName : mapAttributeSelectedVal.keySet()) {
			makeLabelAndText(attributeName, mapAttributeSelectedVal.get(attributeName));
		}
	}

	/**
	 * Renders single label and text.
	 * 
	 * @param attributeName
	 * @param attributeValue
	 */
	protected void makeLabelAndText(String attributeName, String attributeValue) {
		Group group = makeHorizontalGroup();

		// Label.
		Label label = new Label(group, SWT.NONE);
		label.setText(attributeName);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		label.pack();

		// Text.
		Text textInput = new Text(group, SWT.NONE);
		textInput.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		textInput.setData(attributeName);
		textInput.setText(attributeValue);
		textInput.setLayoutData(new RowData(400, 20));
		textInput.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text t = (Text) e.widget;
				mapAttributeSelectedValue.put(t.getData().toString(), t.getText());
			}
		});
		textInput.pack();

		group.pack();
	}

	/**
	 * Renders multiple label and combo.
	 * 
	 * @param mapAttributeValues
	 *            Map of attribute -> enum constants
	 * @param mapAttributeSelectedVal
	 *            Map of attribute -> selected value
	 */
	protected void makeLabelAndCombo(Map<String, List<Object>> mapAttributeValues, Map<String, String> mapAttributeSelectedVal) {
		for (String attribute : mapAttributeValues.keySet()) {
			makeLabelAndCombo(attribute, mapAttributeValues.get(attribute), mapAttributeSelectedVal);
		}
	}

	/**
	 * Renders single label and combo.
	 * 
	 * @param attributeName
	 * @param attributeValues
	 * @param mapAttributeSelectedVal
	 *            Map of attribute -> selected value
	 */
	protected Group makeLabelAndCombo(String attributeName, List<Object> attributeValues, Map<String, String> mapAttributeSelectedVal) {
		Group group = makeHorizontalGroup();

		// Label.
		Label label = new Label(group, SWT.NONE);
		label.setText(attributeName);
		label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		label.pack();

		// Combo.
		Combo comboDropDown = new Combo(group, SWT.DROP_DOWN | SWT.BORDER);
		comboDropDown.setData(attributeName);
		populateCombo(comboDropDown, attributeValues);
		comboDropDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				mapAttributeSelectedValue.put(attributeName, c.getItem(c.getSelectionIndex()));
			}
		});

		// Set previously selected value (if any)
		if (mapAttributeSelectedVal != null && mapAttributeSelectedVal.size() != 0) {
			String value = mapAttributeSelectedVal.get(attributeName);
			selectComboValue(comboDropDown, attributeName, value);
		}
		return group;
	}

	/**
	 * Select combo based on previous selection.
	 * 
	 * @param comboDropDown
	 * @param attributeName
	 * @param attributeValue
	 */
	public void selectComboValue(Combo comboDropDown, String attributeName, String attributeValue) {
		if (attributeValue != null) {
			int iIndex = 0;
			for (String item : comboDropDown.getItems()) {
				if (attributeValue.equals(item)) {
					comboDropDown.select(iIndex);
					break;
				}
				iIndex++;
			}
		}
	}

	/**
	 * Returns a node from a list of nodes for that tag whose attributevalue matches
	 * with {@param attributeValue}
	 * 
	 * @param tag
	 *            e.g <ns:hello msg="hello"/>
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public Node getNode(String tag, String attributeName, String attributeValue) {
		// Get nodes for the configuration tag.
		NodeList nl = poruaXmlEditor.getXmlDoc().getElementsByTagName(tag);
		if (nl != null && nl.getLength() != 0) {
			for (int iNode = 0; iNode < nl.getLength(); iNode++) {
				Node node = nl.item(iNode);
				NamedNodeMap mapNodeAtt = node.getAttributes();
				Node nodeAtt = mapNodeAtt.getNamedItem(attributeName);
				if (attributeValue.equals(nodeAtt.getNodeValue())) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * Loads nodes by tag and searches for values for specific attribute of the each
	 * node and return the list of values.
	 * 
	 * @param tag
	 *            e.g <ns:hello msg="hello world" />
	 * @param attributeName
	 * @return
	 */
	protected List<Object> loadValuesByTagAndAttribute(String tag, String attributeName) {
		List<Object> list = null;
		NodeList nl = poruaXmlEditor.getXmlDoc().getElementsByTagName(tag);
		if (nl != null && nl.getLength() != 0) {
			list = new ArrayList<>();
			for (int i = 0; i < nl.getLength(); i++) {
				NamedNodeMap mapConfigNodeAtt = nl.item(i).getAttributes();
				Node nodeAtt = mapConfigNodeAtt.getNamedItem(attributeName);
				if (nodeAtt != null) {
					list.add(nodeAtt.getNodeValue());
				}
			}
		}
		return list;
	}

	/**
	 * Populates combo with given values.
	 * 
	 * @param comboDropDown
	 * @param listValue
	 */
	protected void populateCombo(Combo comboDropDown, List<Object> listValue) {
		if (listValue != null) {
			comboDropDown.setItems(new String[] {});
			listValue.stream().forEach(v -> {
				comboDropDown.add(v.toString());
			});
		}
	}

	/**
	 * Populates combo and select value .
	 * 
	 * @param comboDropDown
	 * @param listValue
	 * @param attributeName
	 * @param attributeValue
	 */
	protected void populateComboAndSelect(Combo comboDropDown, List<Object> listValue, String attributeName, String attributeValue) {
		populateCombo(comboDropDown, listValue);
		if (attributeName != null && attributeValue != null) {
			selectComboValue(comboDropDown, attributeName, attributeValue);
		}
	}

	/**
	 * Get the selected attribute values.
	 * 
	 * @return
	 */
	public Map<String, String> getSelectedAttValues() {
		return mapAttributeSelectedValue;
	}

	/**
	 * Make horizontal group for attributes.
	 * 
	 * @return
	 */
	private Group makeHorizontalGroup() {
		RowLayout layout = new RowLayout();
		layout.type = SWT.HORIZONTAL;
		layout.spacing = 10;
		layout.center = true;

		Group group = new Group(parent, SWT.NONE);
		group.setLayout(layout);
		return group;
	}

	/**
	 * Make vertical layout for Parent.
	 * 
	 * @return
	 */
	private Layout makeVerticalLayout() {
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 10;
		return layout;
	}

	protected Control getControlByData(String data, Composite parent) {
		for (Control control : parent.getChildren()) {
			if (data.equals(control.getData())) {
				return control;
			}
		}
		return null;
	}
}

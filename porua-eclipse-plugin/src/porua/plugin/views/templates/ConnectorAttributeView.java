package porua.plugin.views.templates;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import porua.plugin.PoruaEclipsePlugin;
import porua.plugin.components.AddConfigurationDialog;
import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;
import porua.plugin.utility.JerseyUtility;
import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginDomUtility;
import porua.plugin.utility.PluginTagUtility;
import porua.plugin.utility.PluginUtility;

@SuppressWarnings("unchecked")
public class ConnectorAttributeView extends ViewTemplate {

	private static ILog logger = PoruaEclipsePlugin.getDefault().getLog();

	public ConnectorAttributeView(PoruaXMLEditor poruaXmlEditor, ComponentData compData, Composite parent) {
		super(poruaXmlEditor, compData, parent);

	}

	@Override
	public void renderAttributes(Node nodeComp) {
		TagData tagData = null;

		// Check for SwithCase.
		if (PluginConstants.TAG_SWITCH_CASE.equals(nodeComp.getNodeName().split(":")[1])) {
			tagData = PluginTagUtility.makeSwitchCaseTag(nodeComp);
		} else {
			tagData = PluginTagUtility.getTagDataByTagName(nodeComp.getNodeName());
		}

		// Text Attributes ## Label & Text.
		if (tagData.getAttributes() != null && tagData.getAttributes().size() != 0) {
			makeLabelAndText(populateAttribueAndSelectedValue(nodeComp, tagData.getAttributes()));
		}

		// Enum Attributes ## Label & Combo.
		if (tagData.getAttributeValues() != null && tagData.getAttributeValues().size() != 0) {
			makeLabelAndCombo(tagData.getAttributeValues(),
					populateAttribueAndSelectedValue(nodeComp, tagData.getAttributeValues().keySet()));
		}

		// Configuration Attribute ## Label & Combo & Button.
		if (tagData.getConfig() != null && tagData.getConfig().size() != 0) {
			TagData tagDataConfig = tagData.getConfig().values().iterator().next();
			renderConfigAttributes(tagDataConfig,
					populateAttribueAndSelectedValue(nodeComp, tagData.getConfig().keySet()));
		}
		// Action group.
		makeActionGroup(tagData);

		// Jersey Path (if connector is API Router)
		makeJerseyPathTable(tagData);

		// pack all.
		parent.pack();

	}

	/**
	 * Render table cintaining Jersey paths.
	 * 
	 * @param tagData
	 */
	private void makeJerseyPathTable(TagData tagData) {
		if (PluginConstants.TAG_API_ROUTER.equals(tagData.getTag())) {
			List<String> listPath = JerseyUtility.loadJerseyPaths();
			if (listPath != null) {
				Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				// On click copy the row value.
				table.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						Table t = (Table) event.widget;
						TableItem ti = t.getItem(t.getSelectionIndex());
						System.out.println(ti.getText());
						Clipboard clipBoard = new Clipboard(parent.getDisplay());
						TextTransfer textTransfer = TextTransfer.getInstance();
						clipBoard.setContents(new String[] { ti.getText() }, new Transfer[] { textTransfer });
						clipBoard.dispose();
					}
				});

				// Column
				TableColumn tcPath = new TableColumn(table, SWT.NONE);
				tcPath.setText("Path");
				table.setHeaderVisible(true);

				// Items
				listPath.forEach((value) -> {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(value);
				});
				tcPath.pack();
				table.pack();
			}
		}
	}

	/**
	 * Button group.
	 * 
	 * @param tagData
	 * @return
	 */
	private Group makeActionGroup(TagData tagData) {
		Group btnGroup = new Group(parent, SWT.NONE);
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		hLayout.spacing = 10;
		btnGroup.setLayout(hLayout);

		// Generate.
		if (PluginConstants.TAG_API_ROUTER.equals(tagData.getTag())) {
			Button btnGenerate = new Button(btnGroup, SWT.NONE);
			btnGenerate.setText("Generate");
			btnGenerate.addSelectionListener(handleBtnGenerate);
		}
		// Save
		if (tagData.getAttributes() != null || tagData.getConfig() != null) {
			Button btnSave = new Button(btnGroup, SWT.NONE);
			btnSave.setText("Save");
			btnSave.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					// Need to reload the Node. Otherwise reference is lost.
					Node nc = null;
					if (Collection.class.isAssignableFrom(compData.getHierarchy().getClass())) {
						List<Integer> hierarchy = ((List<Integer>) compData.getHierarchy());
						Node p = poruaXmlEditor.findNodeInFlow(compData.getFlowId(), hierarchy.get(0));
						nc = PluginDomUtility.iterate(p, hierarchy, 1);
					} else {
						nc = poruaXmlEditor.findNodeInFlow(compData.getFlowId(), (Integer) compData.getHierarchy());
					}

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

		btnGroup.pack();
		return btnGroup;
	}

	/**
	 * Handle Generate event.
	 */
	private SelectionAdapter handleBtnGenerate = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			try {

				logger.log(new Status(0, PoruaEclipsePlugin.PLUGIN_ID, "Tststst"));
				// Generate Jaxrs api class.
				JerseyUtility.generateApiClass();

				// Compile the project.
				PluginUtility.buildProject(PluginUtility.getCurrentProject().getLocation().toString(), "compile");

				// Load the Jersey Paths.
				JerseyUtility.loadJerseyPaths();
				MessageDialog.openInformation(parent.getShell(), "Message", "Jax-rs class created.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Render Configuration attribute of connector.
	 * 
	 * @param tagDataConfig           TagData of Configuration.
	 * @param mapAttributeSelectedVal Configuration name and its value.
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
					AddConfigurationDialog dlg = new AddConfigurationDialog(parent.getShell(), poruaXmlEditor,
							tagDataConfig, null);
					dlg.open();

					// Reload the combo.
					populateComboAndSelect(comboDropDown,
							loadValuesByTagAndAttribute(tagConfig, PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG),
							attributeName, mapAttributeSelectedVal.get(attributeName));

				}
			});
			btnAdd.setText("+");
			btnAdd.pack();

			// Edit.
			String selectedConfigName = comboDropDown.getSelectionIndex() == -1 ? ""
					: comboDropDown.getItem(comboDropDown.getSelectionIndex());
			Button btnEdit = new Button(group, SWT.PUSH);
			btnEdit.setText("..");
			btnEdit.setEnabled((selectedConfigName == null || selectedConfigName.equals("")) ? false : true);
			btnEdit.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String selectedConfigName = getSelectedAttValues() == null ? ""
							: getSelectedAttValues().get(attributeName) == null ? ""
									: getSelectedAttValues().get(attributeName);
					AddConfigurationDialog dlg = new AddConfigurationDialog(parent.getShell(), poruaXmlEditor,
							tagDataConfig, selectedConfigName);
					dlg.open();

					// Reload the combo.
					populateComboAndSelect(comboDropDown,
							loadValuesByTagAndAttribute(tagConfig, PluginConstants.ATTRIBUTE_CONNECTOR_CONFIG),
							attributeName, mapAttributeSelectedVal.get(attributeName));
				}
			});
		}

	}

}

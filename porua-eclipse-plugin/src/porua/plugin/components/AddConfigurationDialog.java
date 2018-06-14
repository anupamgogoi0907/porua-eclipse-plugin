package porua.plugin.components;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.TagData;
import porua.plugin.views.templates.ConnectorConfigAttributeView;
import porua.plugin.views.templates.ViewTemplate;

public class AddConfigurationDialog extends Dialog {
	private PoruaXMLEditor poruaXmlEditor;
	private TagData tagData;
	private String selectedConfig;
	private ViewTemplate viewTemplate;

	public AddConfigurationDialog(Shell parentShell, PoruaXMLEditor poruaXmlEditor, TagData tagData, String selectedConfig) {
		super(parentShell);
		this.poruaXmlEditor = poruaXmlEditor;
		this.tagData = tagData;

		// init
		this.selectedConfig = selectedConfig;

	}

	@Override
	protected Control createDialogArea(Composite container) {
		Composite parent = (Composite) super.createDialogArea(container);
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 10;
		parent.setLayout(layout);

		// Init view template.
		viewTemplate = new ConnectorConfigAttributeView(poruaXmlEditor, null, parent);
		viewTemplate.renderAttributes(tagData, selectedConfig);
		return parent;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configuration");
	}

	// @Override
	// protected Point getInitialSize() {
	// return new Point(450, 300);
	// }

	@Override
	protected void okPressed() {
		String tag = tagData.getTagNamespacePrefix() + ":" + tagData.getTag();
		Element elm = null;
		if (selectedConfig == null) {
			poruaXmlEditor.modifyNamespaces(tagData);
			elm = poruaXmlEditor.getXmlDoc().createElement(tag);

		} else {
			Node node = viewTemplate.getNode(tag, "name", selectedConfig);
			if (node != null) {
				elm = (Element) node;
			}
		}
		// Set attribute values.
		if (elm != null) {
			for (String attribute : viewTemplate.getSelectedAttValues().keySet()) {
				elm.setAttribute(attribute, viewTemplate.getSelectedAttValues().get(attribute));
			}
			poruaXmlEditor.getXmlDoc().getFirstChild().appendChild(elm);
			poruaXmlEditor.redrawComposite();
		}
		this.close();
	}

}

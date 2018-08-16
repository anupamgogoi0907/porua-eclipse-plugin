package porua.plugin.views;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.utility.PluginDomUtility;
import porua.plugin.views.templates.ConnectorAttributeView;

@SuppressWarnings("unchecked")
public class PalettePropertyView extends ViewPart implements IViewData {
	public static final String ID = "porua.plugin.views.PalettePropertyView";

	private PoruaXMLEditor poruaXmlEditor;
	private Composite view;
	private ComponentData compData;
	private ScrolledComposite scrollComposite;

	@Override
	public void createPartControl(Composite parent) {
		this.scrollComposite = new ScrolledComposite(parent, SWT.VERTICAL);
		this.view = new Group(this.scrollComposite, SWT.VERTICAL | SWT.BORDER_SOLID);
		this.scrollComposite.setLayout(new FillLayout());
		this.scrollComposite.setContent(this.view);
	}

	@Override
	public void setFocus() {
		this.scrollComposite.setFocus();
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
		for (Control c : this.view.getChildren()) {
			c.dispose();
		}
		this.view.pack();
	}

	/**
	 * Get TagData for the component selected in the editor.
	 */
	private void getTagData() {
		Node nodeComp = null;

		// For SwitchComponent.
		if (Collection.class.isAssignableFrom(compData.getHierarchy().getClass())) {
			List<Integer> hierarchy = ((List<Integer>) compData.getHierarchy());
			Node p = poruaXmlEditor.findNodeInFlow(compData.getFlowId(), hierarchy.get(0));
			nodeComp = PluginDomUtility.iterate(p, hierarchy, 1);
		} else {
			nodeComp = poruaXmlEditor.findNodeInFlow(compData.getFlowId(), (Integer) compData.getHierarchy());

		}

		// Draw attributes.
		drawTagAttributes(nodeComp);
	}

	/**
	 * Represet tag visually.
	 * 
	 * @param nodeComp
	 * @param tagData
	 */
	private void drawTagAttributes(Node nodeComp) {
		new ConnectorAttributeView(poruaXmlEditor, compData, this.view).renderAttributes(nodeComp);
	}

}

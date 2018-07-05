package porua.plugin.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginTagUtility;
import porua.plugin.views.IViewData;
import porua.plugin.views.PalettePropertyView;

public class PaletteComponent extends Button {
	private String tagComponent;
	private Integer index;
	private FlowComponent parent;
	private PoruaXMLEditor poruaXmlEditor;

	public PaletteComponent(FlowComponent parent, PoruaXMLEditor poruaXmlEditor, String tagComponent, Integer index) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.poruaXmlEditor = poruaXmlEditor;
		this.tagComponent = tagComponent;
		this.index = index;

		initComponent();

	}

	/**
	 * Initialize a palette component.
	 */
	private void initComponent() {
		RowData rowData = new RowData(PluginConstants.PALETTE_COMPONENT_SIZE);
		this.setLayoutData(rowData);

		// Set image and data
		this.setImage(new Image(parent.getDisplay(), PluginTagUtility.getImageByTag(tagComponent)));
		this.setData(new ComponentData(parent.getText(), index));

		makeContextMenu();
		showPropertyView();
		pack();
	}

	/**
	 * When clicked on a palette component Eclipse opens the property view.
	 */
	public void showPropertyView() {
		this.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					ComponentData compData = (ComponentData) e.widget.getData();
					if (compData != null) {
						IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PalettePropertyView.ID);
						((IViewData) view).setViewData(poruaXmlEditor, compData);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}
		});
	}

	/**
	 * Right click context menu on a palette component.
	 */
	public void makeContextMenu() {
		Menu popupMenu = new Menu(this);

		MenuItem editItem = new MenuItem(popupMenu, SWT.CASCADE);
		editItem.setText("Edit");
		editItem.setData(this.getData());
		editItem.addListener(SWT.Selection, menuItemListener);

		MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
		deleteItem.setText("Delete");
		deleteItem.setData(this.getData());
		deleteItem.addListener(SWT.Selection, menuItemListener);

		this.setMenu(popupMenu);
	}

	/**
	 * Context menu listener.
	 */
	private Listener menuItemListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			MenuItem item = (MenuItem) event.widget;
			ComponentData data = (ComponentData) item.getData();
			Node nodeFlow = poruaXmlEditor.findFlowNodeInDom(data.getFlowId());
			Node nodeComp = nodeFlow.getChildNodes().item((Integer) data.getHierarchy());
			if (item.getText().equals("Delete")) {
				nodeFlow.removeChild(nodeComp);
			}
			poruaXmlEditor.redrawComposite();

		}
	};

	@Override
	protected void checkSubclass() {
	}

	public String getTagComponent() {
		return tagComponent;
	}

	public void setTagComponent(String tagComponent) {
		this.tagComponent = tagComponent;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

}

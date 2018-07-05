package porua.plugin.controls.switchcontrol;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginTagUtility;
import porua.plugin.views.IViewData;
import porua.plugin.views.PalettePropertyView;

/**
 * Switch Case child.
 * 
 * @author ac-agogoi
 *
 */
public class SwitchCaseChild extends Button {
	SwitchCase parent;
	PoruaXMLEditor poruaXmlEditor;
	String tagComponent;
	Integer iCaseChild;

	public SwitchCaseChild(SwitchCase parent, PoruaXMLEditor poruaXmlEditor, String tagComponent, Integer iCaseChild) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.poruaXmlEditor = poruaXmlEditor;
		this.tagComponent = tagComponent;
		this.iCaseChild = iCaseChild;

		initComponent();
	}

	/**
	 * Initialize a palette component.
	 */
	private void initComponent() {
		this.setImage(new Image(parent.getDisplay(), PluginTagUtility.getImageByTag(tagComponent)));
		this.setLayoutData(new RowData(PluginConstants.PALETTE_COMPONENT_SIZE));
		makeContextMenu();
		showPropertyView();
	}

	/**
	 * Right click context menu on a palette component.
	 */
	public void makeContextMenu() {
		Menu popupMenu = new Menu(this);

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

		}
	};

	private void showPropertyView() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				try {
					ComponentData compData = new ComponentData(parent.parent.parent.getText(), Arrays.asList(parent.parent.iSwitch, parent.iCase, iCaseChild));
					IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PalettePropertyView.ID);
					((IViewData) view).setViewData(poruaXmlEditor, compData);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	/**
	 * Enable subclassing.
	 */
	@Override
	protected void checkSubclass() {
	}
}

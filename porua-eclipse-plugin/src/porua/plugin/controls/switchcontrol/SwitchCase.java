package porua.plugin.controls.switchcontrol;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.ComponentData;
import porua.plugin.pojos.TagData;
import porua.plugin.transfer.TagDataTransfer;
import porua.plugin.utility.PluginConstants;
import porua.plugin.views.IViewData;
import porua.plugin.views.PalettePropertyView;

/**
 * Switch Case component.E.g <ns:case condition="" />
 * 
 * @author ac-agogoi
 *
 */
public class SwitchCase extends Group {
	SwitchComponent parent;
	private PoruaXMLEditor poruaXmlEditor;
	Integer iCase;

	public SwitchCase(SwitchComponent parent, PoruaXMLEditor poruaXmlEditor, Integer iCase) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.poruaXmlEditor = poruaXmlEditor;
		this.iCase = iCase;
		initComponent();
	}

	/**
	 * Init SwitchCase.
	 */
	private void initComponent() {
		makeLayout();
		makeDropTarget();
		showPropertyView();
		this.pack();
	}

	private void makeLayout() {
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		// hLayout.marginWidth = 50;
		// hLayout.marginHeight = 50;
		hLayout.spacing = 5;

		this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		this.setLayout(hLayout);
	}

	/**
	 * Accept Palette component. Currently it does not accept SwitchComponent.
	 */
	private void makeDropTarget() {
		DropTarget dtGroup = new DropTarget(this, DND.DROP_MOVE);
		dtGroup.setTransfer(new Transfer[] { TagDataTransfer.getInstance() });
		dtGroup.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				// Palette component.Currently SwitchComponent can not be added to Switch case.
				TagData tagData = (TagData) event.data;
				if (!SwitchComponent.restrictedComps.contains(tagData.getTag())) {
					// Get Flow id. SwitchComponent->FlowComponent.getText()
					Node nodeSwitch = poruaXmlEditor.findNodeInFlow(parent.parent.getText(), parent.iSwitch);

					if (nodeSwitch != null && nodeSwitch.getChildNodes().getLength() != 0) {
						// Search for the Case node by its index i.e iCase.
						Node nodeCase = nodeSwitch.getChildNodes().item(iCase);

						// If it's a Switch add case tag. <ns:case condition="" />
						if (PluginConstants.TAG_SWITCH.equals(tagData.getTag())) {
							String tagCase = nodeSwitch.getNodeName().split(":")[0] + ":" + PluginConstants.TAG_SWITCH_CASE;
							Element elmCase = poruaXmlEditor.getXmlDoc().createElement(tagCase);
							elmCase.setAttribute("condition", "");
							nodeCase.appendChild(elmCase);
						} else {
							// Add the Component tag.
							Element elmComp = poruaXmlEditor.getXmlDoc().createElement(tagData.getTagNamespacePrefix() + ":" + tagData.getTag());
							nodeCase.appendChild(elmComp);
						}
					}
					// Update the Xml.
					poruaXmlEditor.modifyNamespaces(tagData);
					poruaXmlEditor.redrawComposite();
				}
			}
		});
	}

	/**
	 * Opens IDE's property view window on clicking.
	 */
	private void showPropertyView() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent event) {
				try {
					ComponentData compData = new ComponentData(parent.parent.getText(), Arrays.asList(parent.iSwitch, iCase));
					IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PalettePropertyView.ID);
					((IViewData) view).setViewData(poruaXmlEditor, compData);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	public Integer getiCase() {
		return iCase;
	}

	/**
	 * Enable subclassing.
	 */
	@Override
	protected void checkSubclass() {
	}
}

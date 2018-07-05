package porua.plugin.controls.switchcontrol;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Group;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import porua.plugin.components.FlowComponent;
import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.TagData;
import porua.plugin.transfer.TagDataTransfer;
import porua.plugin.utility.PluginConstants;

public class SwitchComponent extends Group {
	private String tagSwitch;
	Integer iSwitch;
	FlowComponent parent;
	private PoruaXMLEditor poruaXmlEditor;

	// Restriced components.
	public static List<String> restrictedComps = Arrays.asList(PluginConstants.TAG_FLOW, PluginConstants.TAG_SUB_FLOW, PluginConstants.TAG_SWITCH);

	/**
	 * 
	 * @param parent
	 *            Flow or Subflow.
	 * @param poruaXmlEditor
	 *            The main Editor.
	 * @param iSwitch
	 *            Index of this component in the Flow or Subflow.
	 */
	public SwitchComponent(FlowComponent parent, PoruaXMLEditor poruaXmlEditor, String tagSwitch, Integer iSwitch) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.poruaXmlEditor = poruaXmlEditor;
		this.tagSwitch = tagSwitch;
		this.iSwitch = iSwitch;

		initComponent();
	}

	/**
	 * Init everything.
	 */
	private void initComponent() {
		initSwitchCases();
		makeLayout();
		makeDropTarget();
		parent.pack();
	}

	/**
	 * Init switch cases. Switch -> Cases -> Components.
	 */
	private void initSwitchCases() {
		Node nodeSwitch = poruaXmlEditor.findNodeInFlow(parent.getText(), iSwitch);

		// Switch Case components. Vertical group.
		NodeList nlCase = nodeSwitch.getChildNodes();
		for (int iCase = 0; iCase < nlCase.getLength(); iCase++) {

			// Each case.Horizontal group.
			Node nodeCase = nlCase.item(iCase);
			if (nodeCase.getNodeType() == Node.ELEMENT_NODE) {
				SwitchCase caseComp = new SwitchCase(this, poruaXmlEditor, iCase);
				// Children of each case.
				NodeList nlCaseChildren = nodeCase.getChildNodes();
				for (int iCaseChild = 0; iCaseChild < nlCaseChildren.getLength(); iCaseChild++) {
					Node nodeCaseChild = nlCaseChildren.item(iCaseChild);
					if (nodeCaseChild.getNodeType() == Node.ELEMENT_NODE) {
						new SwitchCaseChild(caseComp, poruaXmlEditor, nodeCaseChild.getNodeName(), iCaseChild);
					}
				}
				caseComp.pack();
			}
		}
	}

	/**
	 * Drop components to Switch. It should create <ns:case condition=""/> and add
	 * the component/s inside the tag.
	 */
	private void makeDropTarget() {
		DropTarget dtGroup = new DropTarget(this, DND.DROP_MOVE);
		dtGroup.setTransfer(new Transfer[] { TagDataTransfer.getInstance() });
		dtGroup.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				// Palette component.Currently Switch can't be dropped to a Switch component.
				TagData tagData = (TagData) event.data;
				if (!restrictedComps.contains(tagData.getTag())) {
					// Get the Switch node. Parent is the Flow to which it belongs.
					Node nodeSwitch = poruaXmlEditor.findNodeInFlow(parent.getText(), iSwitch);

					// Add a Case.
					String tagCase = tagSwitch.split(":")[0] + ":" + PluginConstants.TAG_SWITCH_CASE;
					Element elmCase = poruaXmlEditor.getXmlDoc().createElement(tagCase);
					elmCase.setAttribute("condition", "");
					nodeSwitch.appendChild(elmCase);

					// Add the Palette component to the Case.
					Element elmComp = poruaXmlEditor.getXmlDoc().createElement(tagData.getTagNamespacePrefix() + ":" + tagData.getTag());
					elmCase.appendChild(elmComp);
					poruaXmlEditor.modifyNamespaces(tagData);

					poruaXmlEditor.redrawComposite();
				}
			}
		});
	}

	/**
	 * Vertical layout for Switch.
	 */
	private void makeLayout() {
		RowLayout vLayout = new RowLayout(SWT.VERTICAL);
		vLayout.marginWidth = 40;
		vLayout.marginHeight = 40;
		vLayout.spacing = 10;
		this.setLayout(vLayout);
	}

	/**
	 * Enable subclassing.
	 */
	@Override
	protected void checkSubclass() {
	}

}

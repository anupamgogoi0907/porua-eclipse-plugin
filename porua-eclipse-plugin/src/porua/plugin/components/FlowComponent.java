package porua.plugin.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import porua.plugin.editors.PoruaXMLEditor;
import porua.plugin.pojos.TagData;
import porua.plugin.transfer.TagDataTransfer;
import porua.plugin.utility.PluginConstants;

public class FlowComponent extends Group {
	private PoruaXMLEditor poruaXmlEditor;
	private Composite parent;

	public FlowComponent(Composite parent, PoruaXMLEditor poruaXmlEditor, String name) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.poruaXmlEditor = poruaXmlEditor;
		setText(name);
		initComponent();
	}

	private void initComponent() {
		makeLayout();
		makeDropTarget();
		makeContextMenu();
		pack();
	}

	private void makeDropTarget() {
		DropTarget dtGroup = new DropTarget(this, DND.DROP_MOVE);
		dtGroup.setTransfer(new Transfer[] { TagDataTransfer.getInstance() });
		dtGroup.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				TagData tagData = (TagData) event.data;
				if (!PluginConstants.TAG_FLOW.equals(tagData.getTag())) {
					poruaXmlEditor.modifyNamespaces(tagData);
					poruaXmlEditor.makeChangesToXml(getText(),
							tagData.getTagNamespacePrefix() + ":" + tagData.getTag());
					parent.pack();
				}
			}
		});
	}

	private void makeLayout() {
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		hLayout.marginWidth = 100;
		hLayout.marginHeight = 100;
		hLayout.spacing = 100;

		setLayout(hLayout);
	}

	public void makeContextMenu() {
		Menu popupMenu = new Menu(this);

		MenuItem runItem = new MenuItem(popupMenu, SWT.CASCADE);
		runItem.setText("Run");
		runItem.setData(this.getData());
		runItem.addListener(SWT.Selection, menuItemListener);

		this.setMenu(popupMenu);
	}

	private Listener menuItemListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			System.out.println("Running...");

		}
	};

	@Override
	protected void checkSubclass() {
	}

}

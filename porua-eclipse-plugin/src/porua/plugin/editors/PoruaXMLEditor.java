package porua.plugin.editors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import porua.plugin.components.FlowCanvas;
import porua.plugin.components.FlowComponent;
import porua.plugin.components.PaletteComponent;
import porua.plugin.pojos.TagData;
import porua.plugin.utility.PluginDomUtility;
import porua.plugin.utility.PluginUtility;
import porua.plugin.utility.PluginConstants;
import porua.plugin.views.PalettePropertyView;

public class PoruaXMLEditor extends MultiPageEditorPart implements IResourceChangeListener {
	public static int flowCount = 0;

	private TextEditor editor;

	private ScrolledComposite scrollComposite;
	private FlowCanvas composite;
	private Document xmlDoc = null;

	/**
	 * Creates a multi-page editor example.
	 */
	public PoruaXMLEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		setCurrentProject();
	}

	private void setCurrentProject() {
		try {
			ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getSelection();
			if (selection != null) {
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				PluginUtility.setProject(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Editor tab of the multi-page editor.Index 0.
	 */
	private void createEditor() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, "xml");
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
	 * Preview tab of the multi-page editor. Index 1.
	 */
	private void createPreview() {
		initComposite();
		xmlToVisualComponent(readEditorData());

		// After adding components pack it.
		composite.pack();

		// Add page
		int index = addPage(scrollComposite);
		setPageText(index, "Preview");
	}

	/**
	 * Initialize everything.
	 */
	private void initComposite() {
		scrollComposite = new ScrolledComposite(getContainer(), SWT.V_SCROLL | SWT.H_SCROLL);
		scrollComposite.setLayout(new FillLayout());
		composite = new FlowCanvas(scrollComposite, this);
		scrollComposite.setContent(composite);
	}

	/**
	 * Render xml tags to visual component.
	 * 
	 * @param editorData
	 *            The xml data.
	 */
	private void xmlToVisualComponent(String editorData) {
		if (editorData != null && !editorData.equals("")) {
			xmlDoc = PluginDomUtility.xmlToDom(editorData);
			NodeList listFlow = null;
			String tag = "";

			// Flows.
			tag = findTagByName(PluginConstants.TAG_FLOW);
			if (!"".equals(tag)) {
				listFlow = xmlDoc.getElementsByTagName(tag);
				renderTags(listFlow);
			}
			// Sub-flows.
			tag = findTagByName(PluginConstants.TAG_SUB_FLOW);
			if (!"".equals(tag)) {
				listFlow = xmlDoc.getElementsByTagName(tag);
				renderTags(listFlow);
			}

		}
	}

	/**
	 * Renders items of the listFlow to SWT components.
	 * 
	 * @param listFlow
	 *            List of Flow or Subflow.
	 */
	private void renderTags(NodeList listFlow) {
		for (int i = 0; i < listFlow.getLength(); i++) {
			// Flow/Subflow
			Node nodeFlow = listFlow.item(i);

			// Process Flow/Subflow and its children.
			if (nodeFlow.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap mapAtt = nodeFlow.getAttributes();
				Node nodeAtt = mapAtt.getNamedItem("id");
				if (nodeAtt != null) {
					// Add flow/subflow to canvas.
					Group group = addFlowGroupToComposite(nodeAtt.getTextContent());

					// Extract childern (processors) of the flow/subflow.
					NodeList listFlowElm = nodeFlow.getChildNodes();
					for (int j = 0; j < listFlowElm.getLength(); j++) {
						Node nodeFlowElm = listFlowElm.item(j);

						// Add children/processors to the flow/subflow.
						if (nodeFlowElm.getNodeType() == Node.ELEMENT_NODE) {
							addPaletteComponentToGroup(group, nodeFlowElm.getNodeName(), j);
						}
					}
				}
			}

		}
	}

	/**
	 * Check if tag exists.
	 * 
	 * @param name
	 * @return Returns complete tag, <ns:tag/>
	 */
	private String findTagByName(String name) {
		Node root = xmlDoc.getFirstChild();
		NodeList listNode = root.getChildNodes();
		for (int i = 0; i < listNode.getLength(); i++) {
			Node n = listNode.item(i);
			String tag = n.getNodeName();
			if (tag.contains(name)) {
				return tag;
			}
		}
		return "";
	}

	/**
	 * Add flow or subflow to the canvas.
	 * 
	 * @param name
	 *            Name (id) of the Flow or Subflow
	 * @return Flow or Subflow group.
	 */
	private Group addFlowGroupToComposite(String name) {
		Group group = new FlowComponent(composite, this, name);
		flowCount++;
		return group;
	}

	/**
	 * Add a component to a flow or subflow.
	 * 
	 * @param parent
	 *            Flow or Subflow to which the Palette component is being dropped.
	 * @param tagComponent
	 *            Tag of the Palette component.
	 * @param index
	 *            Index of the Palette component.
	 */
	private void addPaletteComponentToGroup(Group parent, String tagComponent, Integer index) {
		new PaletteComponent(parent, this, tagComponent, index);
	}

	/**
	 * Search for a flow or sublfow in the DOM.
	 * 
	 * @param id
	 *            Id of the Flow/Subflow. Id is also known as the groupName of the
	 *            Flow or Subflow.
	 * @return Flow or Subflow Node.
	 */
	public Node findFlowNodeInDom(String id) {
		Node nodeFlowOrSubflow = null;
		NodeList listFlow = null;
		String tag = "";

		// Search in Flows.
		tag = findTagByName(PluginConstants.TAG_FLOW);
		if (!"".equals(tag)) {
			listFlow = xmlDoc.getElementsByTagName(tag);
			// Search for the specific Flow node in the list of Flow nodes.
			nodeFlowOrSubflow = findFlowNodeInNodeList(id, listFlow);
		}

		// Search in SubFlows.
		if (nodeFlowOrSubflow == null) {
			tag = findTagByName(PluginConstants.TAG_SUB_FLOW);
			if (!"".equals(tag)) {
				listFlow = xmlDoc.getElementsByTagName(tag);
				// Search for the specific Sub-Flow node in the list of Sub-Flow nodes.
				nodeFlowOrSubflow = findFlowNodeInNodeList(id, listFlow);
			}
		}
		return nodeFlowOrSubflow;
	}

	/**
	 * Search for a particular Flow/Subflow in the NodeList.
	 * 
	 * @param id
	 *            Id of the Flow/Subflow
	 * @param listFlow
	 *            List of Flow/Subflow
	 * @return
	 */
	private Node findFlowNodeInNodeList(String id, NodeList listFlow) {
		for (int i = 0; i < listFlow.getLength(); i++) {
			Node nodeFlow = listFlow.item(i);
			NamedNodeMap mapAtt = nodeFlow.getAttributes();
			Node attId = mapAtt.getNamedItem("id");
			if (id.equals(attId.getTextContent())) {
				return nodeFlow;
			}
		}
		return null;
	}

	/**
	 * Search for a component (processor) in the flow by its index.
	 * 
	 * @param groupName
	 *            The Id of the Flow or Subflow.
	 * @param index
	 *            Index of the Palette component in the Flow or Subflow.
	 * @return
	 */
	public Node findNodeInFlow(String groupName, int index) {
		Node nodeFlow = findFlowNodeInDom(groupName);
		if (nodeFlow != null && nodeFlow.getChildNodes() != null && nodeFlow.getChildNodes().getLength() != 0) {
			return nodeFlow.getChildNodes().item(index);
		} else {
			return null;
		}
	}

	/**
	 * Update xml every time Flows or Subflows are added.
	 * 
	 * @param tagFlow
	 */
	public void makeChangesToXml(String tagFlow) {
		Element elm = xmlDoc.createElement(tagFlow);
		elm.setAttribute("id", "flow-" + (++flowCount));
		xmlDoc.getFirstChild().appendChild(elm);
		redrawComposite();
	}

	/**
	 * Update xml every time Palettes are added to Flow or Subflow are added.
	 * 
	 * @param targetFlowName
	 *            Name of target Flow or Subflow to which Palettes are dropped.
	 * @param tagComponent
	 *            Tag of the Pallete.
	 */
	public void makeChangesToXml(String targetFlowName, String tagComponent) {
		Node nodeFlow = findFlowNodeInDom(targetFlowName);
		if (nodeFlow != null) {
			Element elm = xmlDoc.createElement(tagComponent);
			nodeFlow.appendChild(elm);
			redrawComposite();
		}
	}

	/**
	 * Redraw the whole canvas.
	 */
	public void redrawComposite() {
		redrawComposite(PluginDomUtility.domToXml(xmlDoc));
	}

	/**
	 * Redraw the whole canvas.
	 * 
	 * @param The
	 *            updated xml. xml is mpdified somewhere else.
	 */
	public void redrawComposite(String xml) {
		deleteCompositeControls();
		xmlToVisualComponent(xml);
		writeEditorData(xml);
		composite.layout();
		composite.pack();
	}

	/**
	 * Read editor data i.e xml.
	 * 
	 * @return
	 */
	private String readEditorData() {
		IEditorInput input = editor.getEditorInput();
		IDocument doc = editor.getDocumentProvider().getDocument(input);
		String content = doc.get();
		return content;
	}

	/**
	 * Write data to editor.
	 * 
	 * @param xml
	 */
	private void writeEditorData(String xml) {
		IEditorInput input = editor.getEditorInput();
		IDocument doc = editor.getDocumentProvider().getDocument(input);
		doc.set(xml);
	}

	/**
	 * Delete all swt components from the composite.
	 */
	private void deleteCompositeControls() {
		for (Control c : composite.getChildren()) {
			c.dispose();
		}
	}

	/**
	 * Modify namespace every time components are added.
	 * 
	 * @param tagData
	 */
	public void modifyNamespaces(TagData tagData) {
		NamedNodeMap mapAtt = xmlDoc.getFirstChild().getAttributes();
		boolean nsFound = false;
		for (int i = 0; i < mapAtt.getLength(); i++) {
			Node n = mapAtt.item(i);
			if (tagData.getTagNamespace().equals(n.getTextContent())) {
				nsFound = true;
				break;
			}
		}
		if (!nsFound) {
			((Element) xmlDoc.getFirstChild()).setAttribute("xmlns:" + tagData.getTagNamespacePrefix(), tagData.getTagNamespace());
			String schemaLoc = xmlDoc.getFirstChild().getAttributes().getNamedItem("xsi:schemaLocation").getTextContent();
			schemaLoc = schemaLoc + " " + tagData.getTagNamespace() + " " + tagData.getTagSchemaLocation();
			xmlDoc.getFirstChild().getAttributes().getNamedItem("xsi:schemaLocation").setTextContent(schemaLoc);
		}

	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void createPages() {
		initComposite();
		createEditor();
		createPreview();
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors. Subclasses
	 * may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the text
	 * for page 0's tab, and updates this multi-page editor's input to correspond to
	 * the nested editor's.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method checks
	 * that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 0) {
			IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PalettePropertyView.ID);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(part);
		} else if (newPageIndex == 1) {
			redrawComposite(readEditorData());
		}
	}

	/**
	 * Closes all project files on project close.
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) editor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	public Document getXmlDoc() {
		return xmlDoc;
	}

}

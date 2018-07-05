package porua.plugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import porua.plugin.utility.PluginDomUtility;

public class Test {

	static Document xmlDoc;

	public static void main(String[] args) throws Exception {

		display();
	}

	/**
	 * Vertical.RED
	 * 
	 * @author ac-agogoi
	 *
	 */
	public static class SwitchComponent extends Group {
		public SwitchComponent(Composite parent) {
			super(parent, SWT.NONE);
			makeLayout();
			pack();
		}

		private void makeLayout() {
			RowLayout vLayout = new RowLayout(SWT.VERTICAL);
			vLayout.marginWidth = 100;
			vLayout.marginHeight = 100;
			vLayout.spacing = 10;
			vLayout.center = true;

			this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_RED));
			this.setLayout(vLayout);
		}

		@Override
		protected void checkSubclass() {
		}
	}

	/**
	 * Horizontal. GREEN
	 * 
	 * @author ac-agogoi
	 *
	 */
	public static class SwitchCase extends Group {

		public SwitchCase(SwitchComponent parent) {
			super(parent, SWT.NONE);
			makeLayout();
		}

		private void makeLayout() {
			RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
			hLayout.spacing = 5;

			this.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			this.setLayout(hLayout);
		}

		@Override
		protected void checkSubclass() {
		}
	}

	/**
	 * Button.
	 * 
	 * @author ac-agogoi
	 *
	 */
	public static class SwitchCaseChild extends Button {
		public SwitchCaseChild(SwitchCase parent, String name) {
			super(parent, SWT.NONE);
			this.setText(name);
			initComponent();

		}

		private void initComponent() {
			RowData rowData = new RowData(20, 20);
			this.setLayoutData(rowData);

		}

		@Override
		protected void checkSubclass() {
		}
	}

	/**
	 * Main diplay code.
	 * 
	 * @throws Exception
	 */
	public static void display() throws Exception {

		Display display = new Display();

		// Shell
		Shell shell = new Shell(display);
		ScrolledComposite sc = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new FillLayout());
		sc.setContent(shell);

		// Process.
		xmlDoc = PluginDomUtility.xmlToDom(read());
		for (int iSwitch = 0; iSwitch < xmlDoc.getFirstChild().getChildNodes().getLength(); iSwitch++) {
			Node nodeSwitch = xmlDoc.getFirstChild().getChildNodes().item(iSwitch);
			if (nodeSwitch.getNodeType() == Node.ELEMENT_NODE) {
				SwitchComponent parent = new SwitchComponent(shell);
				iterate(nodeSwitch, parent);
				parent.pack();
			}
		}

		// Open
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static void iterate(Node nodeSwitch, SwitchComponent parent) throws Exception {
		NodeList nlCase = nodeSwitch.getChildNodes();
		for (int iCase = 0; iCase < nlCase.getLength(); iCase++) {
			Node nodeCase = nlCase.item(iCase);

			if (nodeCase.getNodeType() == Node.ELEMENT_NODE) {
				// Case.Horizontal element added vartically.
				SwitchCase sc = new SwitchCase(parent);
				NodeList nlCaseChildren = nodeCase.getChildNodes();
				for (int iCaseChild = 0; iCaseChild < nlCaseChildren.getLength(); iCaseChild++) {

					// Case child.
					Node nodeCaseChild = nlCaseChildren.item(iCaseChild);
					if (nodeCaseChild.getNodeType() == Node.ELEMENT_NODE) {
						if (nodeCaseChild.getNodeName().contains("switch")) {
							SwitchComponent temp = new SwitchComponent(sc);
							iterate(nodeCaseChild, temp);
						} else {
							new SwitchCaseChild(sc, nodeCaseChild.getNodeName());
						}
						sc.pack();
					}
				}
			}
		}
	}

	public static String read() throws Exception {
		String fileName = "./resources/config/multi-level-switch.xml";
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		StringBuilder sb = new StringBuilder("");
		for (String s : lines) {
			sb.append(s);
		}
		return sb.toString();
	}

}

package porua.plugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import porua.plugin.components.AddConfigurationDialog;
import porua.plugin.pojos.TagData;

public class Test {
	public static Layout makeLayout() {
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		hLayout.center = true;
		hLayout.spacing = 10;
		return hLayout;
	}

	public static Group makeGroups(Composite parent) {

		Group g1 = new Group(parent, SWT.NONE);
		g1.setLayout(makeLayout());

		Label l1 = new Label(g1, SWT.NONE);
		l1.setText("Porua Home");
		l1.setData("hello");
		new Text(g1, SWT.NONE);
		g1.pack();
		return g1;
	}

	public static void display() {
		Display display = new Display();

		// Shell
		Shell shell = new Shell(display);

		// Component
		Composite parent = new Composite(shell, SWT.NONE);
		parent.setLayout(new RowLayout(SWT.VERTICAL));

		Group g = makeGroups(parent);
		for(Control control:g.getChildren()) {
			System.out.println(control.getData());
		}
		parent.pack();

		// Open
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public static void makeDialog(Shell shell) {
		TagData pojo = new TagData();
		pojo.setAttributes(Arrays.asList("a", "b"));
		AddConfigurationDialog cd = new AddConfigurationDialog(shell, null, pojo, null);
		cd.open();
	}

	public static void makeImage(Shell shell) {
		ImageData imgData = new ImageData("/Users/ac-agogoi/git/ant-esb/porua-eclipse-plugin/resources/icons/ant_16.png");
		Image im = new Image(shell.getDisplay(), imgData);
		Label label = new Label(shell, SWT.NONE);
		label.setSize(100, 100);
		label.setImage(im);
		label.pack();
	}

	public static String read() throws Exception {
		String fileName = "/Users/ac-agogoi/git/ant-esb/porua-eclipse-plugin/resources/config/init.xml";
		List<String> lines = Files.readAllLines(Paths.get(fileName));
		StringBuilder sb = new StringBuilder("");
		for (String s : lines) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		display();
	}

}

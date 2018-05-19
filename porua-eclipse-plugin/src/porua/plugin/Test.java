package porua.plugin;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
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

	public static void display() {
		Display display = new Display();

		// Shell
		Shell shell = new Shell(display);

		// Component
		Group group = new Group(shell, SWT.NONE);
		group.setLayout(makeLayout());

		Label label = new Label(group, SWT.NONE);
		label.setText("Palettes Path:");

		Text txt = new Text(group, SWT.NONE);
		RowData rd = new RowData();
		rd.width = 200;
		txt.setLayoutData(rd);

		group.pack();
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
		pojo.setProps(Arrays.asList("a", "b"));
		AddConfigurationDialog cd = new AddConfigurationDialog(shell, null, pojo);
		cd.open();
	}

	public static void makeImage(Shell shell) {
		ImageData imgData = new ImageData(
				"/Users/ac-agogoi/git/ant-esb/porua-eclipse-plugin/resources/icons/ant_16.png");
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

	}

}

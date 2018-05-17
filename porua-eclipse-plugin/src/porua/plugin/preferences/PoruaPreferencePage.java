package porua.plugin.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PoruaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(makeLayout());

		Label label = new Label(group, SWT.NONE);
		label.setText("Palettes Path:");

		Text txt = new Text(group, SWT.NONE);
		RowData rd = new RowData();
		rd.width = 300;
		txt.setLayoutData(rd);

		group.pack();
		return group;

	}

	@Override
	public boolean performOk() {
		return super.performOk();
	}

	private Layout makeLayout() {
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		hLayout.spacing = 10;
		return hLayout;
	}
}

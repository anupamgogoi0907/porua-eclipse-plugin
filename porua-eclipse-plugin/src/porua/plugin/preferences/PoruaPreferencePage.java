package porua.plugin.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.osgi.service.prefs.BackingStoreException;

import porua.plugin.Activator;
import porua.plugin.utility.PluginUtility;

public class PoruaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IEclipsePreferences pref;
	private String libPath;
	private boolean isChanged;

	@Override
	public void init(IWorkbench workbench) {
		pref = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
	}

	@Override
	protected Control createContents(Composite parent) {

		libPath = pref.get("LIB_PATH", null);

		Group group = new Group(parent, SWT.NONE);
		group.setLayout(makeLayout());

		Label label = new Label(group, SWT.NONE);
		label.setText("Palettes Path:");

		Text txt = new Text(group, SWT.NONE);
		txt.setText(libPath == null ? "" : libPath);
		RowData rd = new RowData();
		rd.width = 300;
		txt.setLayoutData(rd);
		txt.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent event) {
				Text txt = (Text) event.widget;
				if (!txt.getText().equals(libPath)) {
					libPath = txt.getText();
					isChanged = true;
				}
			}
		});

		group.pack();
		return group;

	}

	@Override
	public boolean performOk() {
		try {
			if (isChanged && libPath != null && !libPath.equals("")) {
				pref.put("LIB_PATH", libPath);
				PluginUtility.LIB_PATH = libPath;
				PluginUtility.loadTags();
			}
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
		} finally {
			try {
				pref.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		return super.performOk();
	}

	private Layout makeLayout() {
		RowLayout hLayout = new RowLayout(SWT.HORIZONTAL);
		hLayout.spacing = 10;
		return hLayout;
	}
}

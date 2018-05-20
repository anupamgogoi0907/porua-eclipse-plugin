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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;

import porua.plugin.PoruaEclipsePlugin;
import porua.plugin.utility.PluginConstants;
import porua.plugin.utility.PluginTagUtility;
import porua.plugin.utility.PluginUtility;
import porua.plugin.views.PoruaPaletteView;

public class PoruaPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IEclipsePreferences pref;
	private String poruaHome = "";
	private String mavenHome = "";
	private boolean isChanged;

	@Override
	public void init(IWorkbench workbench) {
		pref = ConfigurationScope.INSTANCE.getNode(PoruaEclipsePlugin.PLUGIN_ID);
	}

	@Override
	protected Control createContents(Composite composite) {

		poruaHome = pref.get(PluginConstants.KEY_PORUA_HOME, null);
		mavenHome = pref.get(PluginConstants.KEY_MAVEN_HOME, null);

		// Make a new composite.
		Composite parent = new Composite(composite, SWT.NONE);
		parent.setLayout(new RowLayout(SWT.VERTICAL));
		makeGroup("Porua Home:", "txtPoruaHome", (poruaHome == null ? "" : poruaHome), parent);
		makeGroup("Maven Home:", "txtMavenHome", (mavenHome == null ? "" : mavenHome), parent);

		parent.pack();
		return parent;

	}

	private Group makeGroup(String lblName, String txtId, String txtValue, Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new RowLayout(SWT.HORIZONTAL));

		// Text layout data.
		RowData rd = new RowData();
		rd.width = 300;

		// Porua Home
		Label lbl = new Label(group, SWT.NONE);
		lbl.setText(lblName);

		Text txt = new Text(group, SWT.NONE);
		txt.setData(txtId);
		txt.setText(txtValue);
		txt.setLayoutData(rd);
		txt.addModifyListener(textModifyListener);

		group.pack();
		return group;
	}

	private ModifyListener textModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent event) {
			Text txt = (Text) event.widget;
			String id = txt.getData().toString();
			if (id.equals("txtPoruaHome")) {
				if (!txt.getText().equals(poruaHome)) {
					poruaHome = txt.getText();
					isChanged = true;
				}

			} else if (id.equals("txtMavenHome")) {
				mavenHome = txt.getText();
				isChanged = true;
			}
		}
	};

	@Override
	public boolean performOk() {
		try {
			if ((poruaHome != null && !poruaHome.equals("")) && (mavenHome != null && !mavenHome.equals(""))) {
				if (isChanged) {
					pref.put(PluginConstants.KEY_PORUA_HOME, poruaHome);
					pref.put(PluginConstants.KEY_MAVEN_HOME, mavenHome);
					PluginUtility.configurePlugin();
					reopenPalleteView();
				}
			} else {
				MessageDialog.openInformation(getShell(), "Message", "Please set the required values.");
			}

		} catch (Exception e) {
			PluginTagUtility.clearMaps();
			reopenPalleteView();
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

	private void reopenPalleteView() {
		hideView();
		showView();
	}

	private void showView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PoruaPaletteView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void hideView() {
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart myView = wp.findView(PoruaPaletteView.ID);
		wp.hideView(myView);
	}

}

package porua.plugin.utility;

import org.eclipse.swt.graphics.Point;

public interface PluginConstants {

	public static final String TAG_FLOW = "porua-flow";
	public static final String TAG_SUB_FLOW = "sub-flow";
	public static final String TAG_SWITCH = "switch";
	public static final String TAG_SWITCH_CASE = "case";

	public static final String ATTRIBUTE_TAG_SWITCH_CASE = "condition";
	public static final String ATTRIBUTE_CONNECTOR_CONFIG = "name";

	public static final String ICONS_PATH = "resources/icons/";
	public static final String KEY_PORUA_HOME = "PORUA_HOME";
	public static final String KEY_MAVEN_HOME = "MAVEN_HOME";
	public static final String APP_FOLDER = "/src/main/app";

	public static final Point PALETTE_COMPONENT_SIZE = new Point(80, 80);

}

package porua.plugin.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import porua.plugin.pojos.TagData;

public interface PluginConstants {

	public static final String TAG_FLOW = "porua-flow";
	public static final String ICONS_PATH = "resources/icons/";

	public static final Map<String, List<TagData>> mapNsTags = new HashMap<>();
	public static final Map<String, List<TagData>> mapPrefixTags = new HashMap<>();

}

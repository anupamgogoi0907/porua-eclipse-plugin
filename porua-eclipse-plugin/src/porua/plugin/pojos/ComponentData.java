package porua.plugin.pojos;

/**
 * 
 * @author ac-agogoi
 *
 */
public class ComponentData {

	private String flowId;
	private Object hierarchy;

	public ComponentData(String flowId, Object hierarchy) {
		super();
		this.flowId = flowId;
		this.hierarchy = hierarchy;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public Object getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(Object hierarchy) {
		this.hierarchy = hierarchy;
	}

}

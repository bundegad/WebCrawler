package configuration;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractBaseConfig {
	
	private String[] actions;
	public String path;
	
	public AbstractBaseConfig(JSONObject data) {
		
		if (data.has(ConfigurationUtils.PATH_KEY)) {
			this.path = data.getString(ConfigurationUtils.PATH_KEY);
		}
		
		if (data.has(ConfigurationUtils.ACTIONS_KEY)) {
			JSONArray actionJson = data.getJSONArray(ConfigurationUtils.ACTIONS_KEY);
			this.actions = ConfigurationUtils.JsonArrayToStringArray(actionJson);
		}
	}
	
	public abstract boolean isValid();
	
	public String[] getActions() {
		return actions;
	}
	
	public String getFullPathForFile(String file) {
		return this.path + file;
	}
}

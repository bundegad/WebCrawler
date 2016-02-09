package configuration;

import org.json.JSONObject;


public class StaticConfig extends AbstractBaseConfig {

	
	public StaticConfig(JSONObject data) {
		super(data);
	}
	
	public boolean isValid() {
		return true;
	}
	
	@Override
	public String getFullPathForFile(String file) {
		file = file.replace("static/", "");
		return super.getFullPathForFile(file);
	}
}

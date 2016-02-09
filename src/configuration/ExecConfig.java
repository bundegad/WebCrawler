package configuration;

import org.json.JSONObject;

public class ExecConfig extends AbstractBaseConfig {

	private String templateFile;
	
	public ExecConfig(JSONObject execJson) {
		super(execJson);
		if (execJson.has(ConfigurationUtils.EXTRAS_KEY)) {
			JSONObject extras = execJson.getJSONObject(ConfigurationUtils.EXTRAS_KEY);
			templateFile = extras.getString(ConfigurationUtils.EXEC_TEMPLATE__PAGE_KEY);
		}
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	public boolean hasTemplatePage() {
		return templateFile != null && io.Utils.isValidFile(getFullPathForFile(templateFile));
	}
	
	public String getTemplateFile() {
		return templateFile;
	}
	
	@Override
	public String getFullPathForFile(String file) {
		return super.getFullPathForFile(file);
	}
}

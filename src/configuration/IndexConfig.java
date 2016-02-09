package configuration;


import org.json.JSONObject;



public class IndexConfig extends AbstractBaseConfig {
	
	private static final String DEFAULT_PAGE = "index.html";
	
	private String indexRunningPage;

	public IndexConfig(JSONObject data) {
		super(data);
		
		if (data.has(ConfigurationUtils.EXTRAS_KEY)) {
			JSONObject extras = data.getJSONObject(ConfigurationUtils.EXTRAS_KEY);
			if (extras.has(ConfigurationUtils.INDEX_RUNNING_KEY)) {
				indexRunningPage = extras.getString(ConfigurationUtils.INDEX_RUNNING_KEY);
			}
		}
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public String getFullPathForFile(String file) {
		file = file.equals("") ? DEFAULT_PAGE : file; 
		return super.getFullPathForFile(file);
	}
	
	public boolean hasRunningFile() {
		return indexRunningPage != null;
	}
	
	public String getRunningFile() {
		return getFullPathForFile(indexRunningPage);
	}

}

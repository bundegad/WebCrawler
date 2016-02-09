package configuration;

import org.json.JSONObject;

public class CrawlerConfig extends AbstractBaseConfig  {

	
	public CrawlerConfig(JSONObject data) {
		super(data);
	}

	@Override
	public boolean isValid() {		
		return true;
	}
	
}

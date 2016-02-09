package configuration;

import java.util.HashMap;

import org.json.JSONObject;

import http.HTTPResponseCode;

public class ErrorConfig extends AbstractBaseConfig {

	
	public HashMap<HTTPResponseCode, String> errorPages;
	
	
	public ErrorConfig(JSONObject data) {
		super(data);
		
		errorPages = new HashMap<>();
		if (data.has(ConfigurationUtils.EXTRAS_KEY)) {
			JSONObject extras = data.getJSONObject(ConfigurationUtils.EXTRAS_KEY);
			setErrorPages(extras);
		}
	}

	private void setErrorPages(JSONObject extras) {
		errorPages.put(HTTPResponseCode.NOT_FOUND, extras.getString(ConfigurationUtils.NOT_FOUND_KEY));
		errorPages.put(HTTPResponseCode.BAD_REQUEST, extras.getString(ConfigurationUtils.BAD_REQUEST_KEY));
		errorPages.put(HTTPResponseCode.FORBIDDEN, extras.getString(ConfigurationUtils.FORBIDDEN_KEY));
		errorPages.put(HTTPResponseCode.INTERNAL_ERROR, extras.getString(ConfigurationUtils.INTERNAL_KEY));
		errorPages.put(HTTPResponseCode.NOT_IMPLEMENTEED, extras.getString(ConfigurationUtils.NOT_IMPLEMENTED_KEY));	
	}

	public boolean isErrorFileExists(HTTPResponseCode code) {
		return errorPages.containsKey(code);
	}
	

	@Override
	public boolean isValid() {
		return path != null;
	}	
}

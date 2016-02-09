package configuration;

import org.json.JSONArray;

public class ConfigurationUtils {

	//App configuration keys
	public static final String PORT_KEY =  "port";
	public static final String MAX_THREADS_KEY = "maxThreads";
	public static final String PATH_KEY = "path";
	public static final String ACTIONS_KEY = "router-actions";
	public static final String HANDLERS_KEY = "handlers";
	public static final String EXTRAS_KEY = "extras";
	
	
	//Static handler keys
	public static final String STATIC_HANDLER_KEY = "staticHandler";
	
	//Errors handler keys
	public static final String ERROR_HANDLER_KEY = "errorsHandler";
	public static final String BAD_REQUEST_KEY = "bad-request";
	public static final String FORBIDDEN_KEY = "forbidden";
	public static final String NOT_FOUND_KEY = "not-found";
	public static final String INTERNAL_KEY = "internal";
	public static final String NOT_IMPLEMENTED_KEY = "not-implemented";
	
	//Crawler Handler config keys
	public static final String CRAWLER_HANDLER_KEY = "crawlerHandler";
	
	//Index Handler config keys
	public static final String INDEX_HANDLER_KEY = "indexHandler";
	public static final String INDEX_RUNNING_KEY = "running-page";
	
	//exec Handler config keys
	public static final String EXEC_HANDLER_KEY = "execHandler";
	public static final String EXEC_TEMPLATE__PAGE_KEY = "template-page";
	
	
	//Crawler Manager keys
	public static final String 	CRAWLER_MANAGER_KEY = "crawlerManager";
	public static final String MAX_DOWNLOADERS_KEY = "maxDownloaders";
	public static final String MAX_ANALYZERS_KEY = "maxAnalyzers";
	public static final String DIRECTORY_KEY = "directory";
	public static final String EXTENSIONS_KEY = "extnesions";
	
	//crawler extensions key
	public static final String IMAGE_EXTENSIOSN_KEY = "imageExtensions";
	public static final String VIDEO_EXTENSIONS_KEY = "videoExtenstions";
	public static final String DOCUMENT_EXTENSIONS_KEY = "documentExtensions";
	
	
	public static String[] JsonArrayToStringArray(JSONArray jsonArr) {
		
		if (jsonArr == null) {
			return null;
		}
		
		String[] result = new String[jsonArr.length()];
		for (int i = 0; i < jsonArr.length(); i++) {
			result[i] = jsonArr.getString(i);
		}
		
		return result;
	} 
}

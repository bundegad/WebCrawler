import java.io.File;
import java.io.IOException;


import org.json.JSONException;
import org.json.JSONObject;

import configuration.*;
import http.HTTPListener;
import http.Router;
import synchronization.ThreadPool;
import synchronization.ThreadPoolManager;

public class WebCrawler {
	
	public static final boolean TEST_HTML = true;
	public static final String CONFIG_INI = "config.ini";
	public static final String WORKING_DIR = System.getProperty("user.dir");
	public static final String FILE_SEPERATOR = File.separator;
	private static final int MAX_THREADS = 10;

	public static void main(String[] args)  {
		
		AppConfig appConfig = getAppConfig();
		
		if ( appConfig == null || !appConfig.isValid()) {
			System.out.println("Please check config.ini");
			return;
		}
		
		Router router = new Router();
		router.register(appConfig.getConfigurations());
		
		
		//Create and start the handlers pool.
		int maxThreads = appConfig.maxThreads > MAX_THREADS ? MAX_THREADS : appConfig.maxThreads;
		ThreadPool<Runnable> pool = new ThreadPool<>(maxThreads);
		ThreadPoolManager.getInstance().add(pool, HTTPListener.REQUEST_HANDLERS_POOL_KEY);
		
		HTTPListener listerner = new HTTPListener(8080 , router);
		try {
			listerner.start();
		} catch (IOException e) {
			System.out.println("Could not create server");
		}
	}
	
	
	public static AppConfig getAppConfig() {
		
		String configFile = String.format("%s%s%s", WORKING_DIR, FILE_SEPERATOR, CONFIG_INI);
		AppConfig configuration = null;
	
		try {
			String configFileContent = io.Utils.readFile(configFile);
			JSONObject jsonConfig = new JSONObject(configFileContent);
			configuration = new AppConfig(jsonConfig);
		} catch (IOException e) {
			System.out.println("Could not read file" + configFile);
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			System.out.println("Could not parse file to json");
			return null;
		}
		
		return configuration;
	}
	
			
}

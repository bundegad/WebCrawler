package configuration;


import org.json.JSONObject;

import crawler.CrawlerManager;

public class AppConfig extends AbstractBaseConfig {

	AbstractBaseConfig[]  configurations;
	public final int maxThreads;
	public final int port;

	public AppConfig(JSONObject data) {
		super(data);
		this.port = data.getInt(ConfigurationUtils.PORT_KEY);
		this.maxThreads = data.getInt(ConfigurationUtils.MAX_THREADS_KEY);
		
		if (data.has(ConfigurationUtils.HANDLERS_KEY)) {
			JSONObject handlers = data.getJSONObject(ConfigurationUtils.HANDLERS_KEY);
			setConfigurations(handlers);
		}
		
		//Crawler Manager
		JSONObject crawlerManagerConfig = data.getJSONObject(ConfigurationUtils.CRAWLER_MANAGER_KEY);
		CrawlerManager.getInstance().setConfiguration(path, crawlerManagerConfig);
	}

	@Override
	public boolean isValid() {
		
		if (this.port <= 0 || this.port > 56000) {
			System.out.println("Illegal port : " + port);
			return false;
		}
		
		if (maxThreads <= 0) {
			System.out.println("Illegal number of max threads");
			return false;
		}
		
		for (AbstractBaseConfig config : configurations) {
			if (!config.isValid()) {
				return false;
			}
		}
		
		//TODO add validation to configuration manager.
		
		return true;
	}
	
	private void setConfigurations(JSONObject data) {
		
		
		//Static handler configuration
		JSONObject staticJson = data.getJSONObject(ConfigurationUtils.STATIC_HANDLER_KEY);
		StaticConfig staticConfig = new StaticConfig(staticJson);
		staticConfig.path = path + staticConfig.path;
		
		//errors handler configuration
		JSONObject errorJson = data.getJSONObject(ConfigurationUtils.ERROR_HANDLER_KEY);
		ErrorConfig errorConfig = new ErrorConfig(errorJson);
		errorConfig.path = path + errorConfig.path;
		
		//Crawler handler configuration
		JSONObject crawlerJson = data.getJSONObject(ConfigurationUtils.CRAWLER_HANDLER_KEY);
		CrawlerConfig crawlerConfig = new CrawlerConfig(crawlerJson);
		crawlerConfig.path = path + crawlerConfig.path;
		
		//Crawler configuration
		JSONObject indexJson = data.getJSONObject(ConfigurationUtils.INDEX_HANDLER_KEY);
		IndexConfig indexConfig = new IndexConfig(indexJson);
		indexConfig.path = path + indexConfig.path;
		
		//Exec handler configuration
		JSONObject execJson = data.getJSONObject(ConfigurationUtils.EXEC_HANDLER_KEY);
		ExecConfig execConfig = new ExecConfig(execJson);
		execConfig.path = path + execConfig.path;
		
		
		
		configurations = new AbstractBaseConfig[] {staticConfig,
				errorConfig,
				crawlerConfig,
				indexConfig,
				execConfig};
	}
	
	public AbstractBaseConfig[] getConfigurations() {
		return configurations;
	}

	@Override
	public String[] getActions() {
		throw new UnsupportedOperationException();
	}
	
	
}

package handlers;

import configuration.AbstractBaseConfig;
import configuration.CrawlerConfig;
import configuration.ErrorConfig;
import configuration.ExecConfig;
import configuration.IndexConfig;
import configuration.StaticConfig;
import http.Router;

public class HandlersFactory {
	
	public static AbstractBaseHandler createHandler(AbstractBaseConfig configuration, Router router) {
		
		if (configuration instanceof StaticConfig) {
			return new StaticHandler((StaticConfig) configuration, router);
		}
		
		if (configuration instanceof CrawlerConfig) {
			return new CrawlerHandler((CrawlerConfig) configuration, router);
		}
		
		if (configuration instanceof ErrorConfig) {
			return new ErrorsHandler((ErrorConfig) configuration, router);
		}
		
		if (configuration instanceof IndexConfig) {
			return new IndexHandler((IndexConfig) configuration, router);
		}
		
		if (configuration instanceof ExecConfig) {
			return new ExecHandler((ExecConfig) configuration, router);
		}
 		
		return null;
	}
}

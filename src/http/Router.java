package http;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import configuration.AbstractBaseConfig;
import handlers.AbstractBaseHandler;
import handlers.HandlersFactory;

public class Router {
	
	private HashMap<String, AbstractBaseConfig> table;
	
	public Router() {
		table = new HashMap<>();
	}
	
	public void  register(AbstractBaseConfig configuration) {
		for (String action : configuration.getActions()) {
			table.put(action, configuration);
		}
	}
	
	public void register(AbstractBaseConfig[] configurations) {
		for (AbstractBaseConfig iConfiguration : configurations) {
			register(iConfiguration);
		}
	}
	
	
	public AbstractBaseHandler route(String path) {
		AbstractBaseConfig configuration = null;
		Pattern pattern = null;
		Matcher matcher = null;
		
		for (String action : table.keySet()) {
			pattern = Pattern.compile(action);
			matcher = pattern.matcher(path);
			if (matcher.matches()) {
				configuration = table.get(action);
				break;
			}
		}
		
		return HandlersFactory.createHandler(configuration, this);
	}
}

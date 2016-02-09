package handlers;


import org.json.JSONArray;
import org.json.JSONObject;

import configuration.IndexConfig;
import crawler.CrawlerManager;
import crawler.ExecutionFileConstants;
import crawler.CrawlerManager.State;
import exceptions.ServerException;
import http.HTTPRequest;
import http.HTTPResponse;
import http.Router;

public class IndexHandler extends AbstractBaseHandler {
	public static final String INDEX_REFIRECT_PATH = "index.html";
	public static final String INDEX_ERROR_KEY = "error";
	
	public static final String PROGRESS_KEY = "progress";
	public static final String RECENTS_KEY = "recents";
	public static final String NAME_KEY = "name";
	public static final String HREF_KEY = "href";
	
	IndexConfig configuration;
	
	public IndexHandler(IndexConfig configuration, Router router) {
		super(configuration, router);
		this.configuration = configuration;
	}

	@Override
	public HTTPResponse handle(HTTPRequest request) throws ServerException {
		
		this.request = request;
		String file = null;
		JSONObject data = new JSONObject();
		
		CrawlerManager manager = CrawlerManager.getInstance();
		if (manager.getState() == State.RUNNING && configuration.hasRunningFile()) {
			file = configuration.getRunningFile();
			data.put(PROGRESS_KEY, Double.toString(manager.getProgress()));
			
		} else {
			file = getRequiredPath(request.path);
		}
		
		JSONArray pastExecutions = manager.getPastExecutions();
		JSONArray recents = new JSONArray();
		
		for (int i = 0; i < pastExecutions.length(); i++) {
			JSONObject pastExecution = pastExecutions.getJSONObject(i);
			recents.put(getRecentResult(pastExecution));
		}
		
		data.put(RECENTS_KEY, recents);
		
		if (request.UrlParams != null && request.UrlParams.containsKey(INDEX_ERROR_KEY)) {
			data.put(INDEX_ERROR_KEY, request.UrlParams.get(INDEX_ERROR_KEY));
		}
		
		return generateHtmlResponse(file, data);
	}
	
	private JSONObject getRecentResult(JSONObject execution) {
		JSONObject recentResult = new JSONObject();
		
		String name = String.format("%s - %s",
				execution.getString(ExecutionFileConstants.DOMAIN),
				execution.getString(ExecutionFileConstants.DATE_TIME));
		String id = execution.getString(ExecutionFileConstants.ID);
		
		recentResult.put(NAME_KEY, name);
		recentResult.put(HREF_KEY, String.format("/exec?id=%s", id));
		return recentResult;
	}
}

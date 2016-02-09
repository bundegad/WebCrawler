package handlers;


import java.net.URISyntaxException;
import java.util.HashMap;

import configuration.CrawlerConfig;
import crawler.CrawlerManager;
import crawler.CrawlerManager.State;
import exceptions.ServerException;
import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.Router;

public class CrawlerHandler extends AbstractBaseHandler {
	public static final String CRAWLER_START_KEY = "crawler-start";
	public static final String CRAWLER_STOP_KEY = "crawler-stop";
	
	public static final String CRAWLER_COULD_NOT_STOP_MESSAGE = "Could not stop crawler, please try again";
	public static final String CRAWLER_COULD_NOT_START_MESSAGE = "Could not start crawler, please try again";
	public static final String CRAWLER_START_NO_DOMAIN_MESSAGE = "Could not start crawler without domain";
	public static final String CRAWLER_START_INVALID_DOMAIN_MESSAGE = "Could not start crawler, invalid domain";
	
	private static final String DOMAIN_KEY = "domain";
	private static final String TCP_KEY = "fullTcp";
	private static final String DISRESPECT_ROBOT_KEY = "disrespectRobot";

	
	public CrawlerHandler(CrawlerConfig configuration, Router router) {
		super(configuration, router);
	}

	@Override
	public HTTPResponse handle(HTTPRequest request) throws ServerException {
		System.out.println("handling crawling request");
		this.request = request;
		
		switch (request.path) {
			case CRAWLER_START_KEY: {
				return handleStartRequest();
			}
			
			case CRAWLER_STOP_KEY: {
				return handleStopRequest();
			}
			
			default: {
				throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
			}
		}	
	}
	
	public HTTPResponse handleStartRequest() throws ServerException {
		
		//validation
		HashMap<String, String> params = request.UrlParams;
		String domain = params.get(DOMAIN_KEY);
		if (domain == null || domain.isEmpty()) {
			params.put(IndexHandler.INDEX_ERROR_KEY, CRAWLER_START_NO_DOMAIN_MESSAGE);
			return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, params);
		}
		
		if (domain.endsWith("+")) {
			//This is a hack remove it.
			System.out.println("domain ends with +++++++++++++++++++++");
			domain = domain.substring(0, domain.length() -1 );
		}
		
		String disRobotValue = params.get(DISRESPECT_ROBOT_KEY);
		boolean shouldDisRobot =  disRobotValue != null &&
				disRobotValue.equals(HTTPConstants.HTTP_ON_PARAM_VALUE);
		
		String fullTcpValue = params.get(TCP_KEY);
		boolean shouldFullTcp =  fullTcpValue != null &&
				fullTcpValue.equals(HTTPConstants.HTTP_ON_PARAM_VALUE);
		
		CrawlerManager manager = CrawlerManager.getInstance();
		if (manager.getState() != State.WAITING) {
			params.put(IndexHandler.INDEX_ERROR_KEY, CRAWLER_COULD_NOT_START_MESSAGE);
			return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, params);
		}
		
		try {
			manager.Start(domain, shouldFullTcp, shouldDisRobot);
			return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, params);
		} catch (URISyntaxException e) {
			params.put(IndexHandler.INDEX_ERROR_KEY, CRAWLER_START_INVALID_DOMAIN_MESSAGE);
			return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, params);
		}
	}
	
	public HTTPResponse handleStopRequest() throws ServerException {
		
		CrawlerManager manager = CrawlerManager.getInstance();
		
		if (manager.getState() != State.RUNNING) {
			HashMap<String, String> params = new HashMap<>();
			params.put(IndexHandler.INDEX_ERROR_KEY, CRAWLER_COULD_NOT_STOP_MESSAGE);
			
			return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, params);
		}
		
		manager.Stop();
		return redirect(IndexHandler.INDEX_REFIRECT_PATH, request.headers, request.UrlParams);
	}
}

package handlers;


import org.json.JSONObject;

import configuration.ExecConfig;
import crawler.CrawlerManager;
import exceptions.ServerException;
import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.Router;

public class ExecHandler extends AbstractBaseHandler {

	private static String ID_PARAM_KEY = "id";
	ExecConfig configuration;
	
	public ExecHandler(ExecConfig configuration, Router router) {
		super(configuration, router);
		this.configuration = configuration;
	}

	@Override
	public HTTPResponse handle(HTTPRequest request) throws ServerException {
		System.out.println("exec was called");
		this.request = request;
	
		
		String referer = request.headers.get(HTTPConstants.HTTP_REFERER_KEY);
		if (referer == null || !referer.trim().startsWith(HTTPConstants.HTTP_LOCALHOST_PREFIX)) {
			throw new ServerException(HTTPResponseCode.FORBIDDEN);
		}
		
		String fileId = request.UrlParams.get(ID_PARAM_KEY);
		if (fileId == null) {
			throw new ServerException(HTTPResponseCode.NOT_FOUND);
		}
		
		JSONObject data = CrawlerManager.getInstance().getPastExecution(fileId);
		if (data == null) {
			System.out.println("data is null for id");
			throw new ServerException(HTTPResponseCode.NOT_FOUND);
		}
		
		String file = getTemplatePath();
		return generateHtmlResponse(file, data);
	}
	
	protected String getTemplatePath() throws ServerException {
		return configuration.getFullPathForFile(configuration.getTemplateFile());
	}
	

}

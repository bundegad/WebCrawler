package handlers;

import java.io.IOException;

import configuration.StaticConfig;
import exceptions.ServerException;
import http.HTTPRequest;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.Router;

public class StaticHandler extends AbstractBaseHandler {

	public StaticHandler(StaticConfig configuration, Router router) {
		super(configuration, router);
	}
	
	public HTTPResponse handle(HTTPRequest request) throws ServerException {
		
		this.request = request;
		HTTPResponse response = null;
		
		try {
			String file = getRequiredPath(request.path);
			response  = generateFileResponse(file);
		}  catch (IOException e) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		return response;
	}
}

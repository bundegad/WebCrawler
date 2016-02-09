package handlers;

import java.io.IOException;

import configuration.ErrorConfig;
import exceptions.ServerException;
import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPRequestType;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.Router;

public class ErrorsHandler extends AbstractBaseHandler {

	
	public static final String ROUTING_KEY = "errors";
	
	private ErrorConfig configuration;
	private HTTPResponseCode code;

	
	public ErrorsHandler(ErrorConfig configuration, Router router) {
		super(configuration, router);
		this.configuration = configuration;
	}
	
	@Override
	public HTTPResponse handle(HTTPRequest request) throws ServerException {
		
		if (code == null) {
			throw new ServerException(HTTPResponseCode.NOT_FOUND);
		}
		
		System.out.println("Errors handler called with code " + code);
		String version = request.version;
		HTTPResponse response = new HTTPResponse(code, version);

		if (configuration.isErrorFileExists(code)) {
			String errorFile = configuration.errorPages.get(code);
			String errorFileFullPath = configuration.getFullPathForFile(errorFile);

			String fileContent = "";
			try {
				fileContent = io.Utils.readFile(errorFileFullPath);
			} catch (IOException e) {
				//ignore the error in reading the file.
				System.out.println("Error in reading error file: " + errorFile);
			}

			http.FileType type = http.FileType.getTypeForFile(errorFile);
			response.addHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, Integer.toString(fileContent.length()));
			response.addHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY, type.toString());
			response.attachFileContent(fileContent.getBytes());
		}
		
		if (request.type == HTTPRequestType.GET || request.type == HTTPRequestType.POST) {
			response.shouldAttachFile = true;
		}
		
		return response;
	}

	public void setCode(HTTPResponseCode code) {
		this.code = code;
	}

}

package handlers;


import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import configuration.AbstractBaseConfig;
import exceptions.ServerException;
import html.HtmlGenerator;
import http.FileType;
import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPRequestType;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.Router;

public abstract class AbstractBaseHandler {
	
	protected AbstractBaseConfig configuration;
	protected HTTPRequest request;
	protected Router router;
	
	public AbstractBaseHandler(AbstractBaseConfig configuration, Router router) {
		this.configuration = configuration;
		this.router = router;
	}
	
	public abstract HTTPResponse handle(HTTPRequest request) throws ServerException;
	
	
	protected HTTPResponse generateFileResponse(String file) throws IOException, ServerException  {

		try {	
			if (!io.Utils.isValidFile(file)) {
				throw new ServerException(HTTPResponseCode.NOT_FOUND);
			}
		} catch (SecurityException e) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}

		HTTPResponse response = new HTTPResponse(HTTPResponseCode.OK, request.version);

		http.FileType contentType = http.FileType.getTypeForFile(file);
		byte[] fileContent = contentType.isImage() ? io.Utils.readImageFile(file) :
				io.Utils.readFile(file).getBytes();
		
		

		if (request.isChunked()){
			response.addHeader(HTTPConstants.HTTP_TRANSFER_ENCODING, HTTPConstants.HTTP_CHUNKED_KEY);
		} else{
			int contentLength = fileContent.length;
			response.addHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, Integer.toString(contentLength));	
		}

	
		response.addHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY, contentType.toString());
		response.attachFileContent(fileContent);
		
		if (request.type == HTTPRequestType.POST || request.type == HTTPRequestType.GET) {
			response.shouldAttachFile = true;
		}
 		
		
		return response;
	}
	
	protected HTTPResponse generateJsonResponse(JSONObject object) throws ServerException {
		
		
		if (object == null || request == null) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		HTTPResponse response = new HTTPResponse(HTTPResponseCode.OK, request.version);
		
		if (request.isChunked()){
			response.addHeader(HTTPConstants.HTTP_TRANSFER_ENCODING, HTTPConstants.HTTP_CHUNKED_KEY);
		} else{
			response.addHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, Integer.toString(object.toString().length()));	
		}
		
		response.addHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY, FileType.json.toString());
		response.attachFileContent(object.toString().getBytes());
		
		if (request.type == HTTPRequestType.POST || request.type == HTTPRequestType.GET) {
			response.shouldAttachFile = true;
		}
		
		
		return response;
	}
	
	protected HTTPResponse generateHtmlResponse(String file, JSONObject data) throws ServerException {
		
		try {	
			if (!io.Utils.isValidFile(file)) {
				throw new ServerException(HTTPResponseCode.NOT_FOUND);
			}
		} catch (SecurityException e) {
			System.out.println("file is not valid");
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		HTTPResponse response = new HTTPResponse(HTTPResponseCode.OK, request.version);
		
		http.FileType contentType = http.FileType.getTypeForFile(file);
		if (contentType != http.FileType.html) {
			System.out.println("file is not html");
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		response.addHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY, contentType.toString());
		
		
		
		HtmlGenerator generator = new HtmlGenerator(data, file);
		byte[] fileContent = null;
		
		try {
			fileContent = generator.generate();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		if (request.isChunked()){
			response.addHeader(HTTPConstants.HTTP_TRANSFER_ENCODING, HTTPConstants.HTTP_CHUNKED_KEY);
		} else {
			int contentLength = fileContent.length;
			response.addHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, Integer.toString(contentLength));	
		}

		response.attachFileContent(fileContent);
		
		if (request.type == HTTPRequestType.POST || request.type == HTTPRequestType.GET) {
			response.shouldAttachFile = true;
		}
 		
		
		return response;
	}
	
	protected HTTPResponse redirect(String path, HashMap<String, String> headers, HashMap<String, String> params) throws ServerException {
		HTTPRequest request = new HTTPRequest(this.request.originRequest, path, this.request.type);
		request.setHeaders(headers);
		request.setParams(params);		
		AbstractBaseHandler nextHandler = router.route(path);
		if (nextHandler == null) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		return nextHandler.handle(request);
	}
	
	protected String getRequiredPath(String path) throws ServerException {
		return configuration.getFullPathForFile(path);
	}
	
	
}

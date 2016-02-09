package http;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.ServerException;
import io.Utils;

public class HTTPUtils {

	public static HTTPRequest parseRawHttpRequest(InputStream in) throws ServerException {
		
		String httpheaders = Utils.readHeadersFromInputStream(in);
		HTTPRequest request = parseRawHttpRequest(httpheaders);
		String body = null;
		boolean validateBody = false;
		
		String contentLengthString = request.headers.get(HTTPConstants.HTTP_CONTENT_LENGTH_KEY);
		if (contentLengthString != null) {
			
			int contentLength = Integer.parseInt(contentLengthString);
			body = io.Utils.readDataFromInputStream
					(in, contentLength);
			validateBody = true;
			
		} else if (request.headers.containsKey(HTTPConstants.HTTP_TRANSFER_ENCODING)) {	
			
			String transferEncoding = request.headers.get(HTTPConstants.HTTP_TRANSFER_ENCODING);
			if (!transferEncoding.equals(HTTPConstants.HTTP_CHUNKED_KEY)) {
				throw new ServerException(HTTPResponseCode.BAD_REQUEST);
			}
			
			body = io.Utils.readChunkedDataFromInputStream(in, HTTPConstants.SOCKET_DEFAULT_TIMEOUT_MS);
			
		}
		
		request.body = body;
		if (validateBody && !HTTPUtils.validateBody(request)) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}
		
		return request;
	}
	
	public static HTTPRequest parseRawHttpRequest(String rawRequest) throws ServerException {

		//Check null or empty
		if (rawRequest == null || rawRequest.isEmpty()) {
			return null;
		}

		//Parse lines
		String[] requestLines = rawRequest.split(HTTPConstants.CRLF);

		//clean first line
		String firstLine = removeSpaces(requestLines[0]);


		//Parse first line
		Pattern firstLinePattern = Pattern.compile(HTTPConstants.REQUEST_FIRSTLINE_PATTEN_STRING);
		Matcher matcher = firstLinePattern.matcher(firstLine);


		//Check if valid first line
		if (!matcher.matches()) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}

		//Extract type, path and version
		HTTPRequestType type = HTTPRequestType.convertFromString(matcher.group(1));
		String version = matcher.group(3);
		String path =  null;

		try {
			path = URLDecoder.decode(matcher.group(2), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}

		//Validate path
		if (path.trim().contains(" ")) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}


		//Parse headers
		int endHeadersIndex = getEndHeadersIndex(requestLines);
		HashMap<String, String> headers = parseRawHeaders(requestLines, endHeadersIndex);


		//Construct request
		HTTPRequest request = new HTTPRequest(rawRequest, path, type);
		request.version = version;
		request.setHeaders(headers);

		

		//Parse url params in case its get or head request.
		if (request.type == HTTPRequestType.GET || request.type == HTTPRequestType.HEAD) {
			parseURLParams(request);
		}

		return request;
	}
	
	public static HTTPResponse parseRawHttpResponse(InputStream in, boolean shouldReadBody) throws ServerException {
		
		String httpheaders = Utils.readHeadersFromInputStream(in);
		HTTPResponse response = parseRawHttpResponse(httpheaders);
		
		if (!shouldReadBody) {
			return response;
		}
		
		boolean validateBody = false;
		String body = null;
		
		if (response.isChunked()) {
			body = io.Utils.readChunkedDataFromInputStream(in, HTTPConstants.SOCKET_DEFAULT_TIMEOUT_MS);
		} 
		else {
			
			int contentLength = Integer.parseInt(response.getContentLength());
			body = io.Utils.readDataFromInputStream(in, contentLength);
			validateBody = true;
		}
		
		response.fileContent = body.getBytes();
//		if (validateBody && !HTTPUtils.validateBody(response)) {
//			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
//		}
		
		return response;
	}
	

	public static HTTPResponse parseRawHttpResponse(String rawResponse) throws ServerException  {
		//Check null or empty
		if (rawResponse == null || rawResponse.isEmpty()) {
			return null;
		}

		//Parse lines
		String[] responseLines = rawResponse.split(HTTPConstants.CRLF);

		//clean first line
		String firstLine = removeSpaces(responseLines[0]);


		//Parse first line
		Pattern firstLinePattern = Pattern.compile(HTTPConstants.RESPONSE_FIRSTLINE_PATTEN_STRING);
		Matcher matcher = firstLinePattern.matcher(firstLine);

		if (!matcher.matches()) {
			return null;
		}

		//Create Response
		String version = matcher.group(1);
		HTTPResponseCode code = HTTPResponseCode.convertFromInt(Integer.parseInt(matcher.group(2)));
		if (code == null) {
			return null;
		}

		HTTPResponse response = new HTTPResponse(code, version); 

		//Parse headers
		int endHeadersIndex = getEndHeadersIndex(responseLines);
		HashMap<String, String> headers = parseRawHeaders(responseLines, endHeadersIndex);
		response.addHeaders(headers);

		return response;
	}

	public static URLParsedObject parsedRawURL(String url) throws URISyntaxException  {
		
		try {
			url = URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			
		}
		
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = String.format("http://%s", url);
		}

		//Create URI object
		URI uri = new URI(url);

		//Get host
		String host = uri.getHost();

		//Get port
		int port = uri.getPort();
		port = port != -1 ? port : HTTPConstants.DEFAULT_PORT;

		String path = null;
		if (host == null) {
			host = uri.getRawPath();
			path = "/";
			return new URLParsedObject(host, path, port);
		} 
		
		
		path = uri.getRawPath();
		path = path == null || path.length() == 0 ? "/" : path;
		String query = uri.getRawQuery();
		if (query != null && query.length() > 0) {
			path = String.format("%s?%s", path, query);
		}
		
		host = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
		return new URLParsedObject(host, path, port);
	}

	private static HashMap<String, String> parseRawHeaders(String[] requestLines, int endHeadersIndex) throws ServerException {

		HashMap<String, String> headers = new HashMap<>();
		Pattern headerlinePattern = Pattern.compile(HTTPConstants.REQUEST_HEADERLINE_PATTERN_STRING);


		for (int i = 1; i < endHeadersIndex; i++) {

			Matcher matcher = headerlinePattern.matcher(requestLines[i]);
			if (!matcher.matches()) {
				throw new ServerException(HTTPResponseCode.BAD_REQUEST); 
			} 

			headers.put(matcher.group(1).toLowerCase().trim(),
					matcher.group(2).trim());
		}

		return headers;
	}

	private static void parseURLParams(HTTPRequest request) throws ServerException {
		String[] pathParts = request.path.split("\\?");

		if (pathParts.length > 2) {
			throw new ServerException(HTTPResponseCode.BAD_REQUEST);
		}


		if (pathParts.length == 2) {
			request.path = pathParts[0];
			request.setParams(parseRawParams(pathParts[1]));
		}

	}

	public static boolean validateBody(HTTPRequest request) {

		String contentLength = request.headers.get(HTTPConstants.HTTP_CONTENT_LENGTH_KEY);
		if (contentLength == null) {
			contentLength = "0";
		}

		try {
			if (Integer.parseInt(contentLength) != request.body.length()) {
				return false;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static boolean validateBody(HTTPResponse response) {
		
		String contentLength = response.getHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY);
		if (contentLength == null) {
			contentLength = "0";
		}

		try {
			if (Integer.parseInt(contentLength) != response.fileContent.length) {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	private static HashMap<String, String> parseRawParams(String rawParams) throws ServerException {

		HashMap<String, String> params = new HashMap<>();
		if (rawParams == null) {
			return params;
		}

		String[] paramsParts = rawParams.split("&");
		for (int  i = 0; i < paramsParts.length; i++) {
			String[] keyValue = paramsParts[i].split("=");
			if (keyValue.length == 1) {
				params.put(keyValue[0], "");
			} else if (keyValue.length == 2) {
				params.put(keyValue[0], keyValue[1]);
			} else {
				return params;
			}
		}

		return params;
	}

	private static int getEndHeadersIndex(String[] requestLines) {

		int i = 1;
		while (i < requestLines.length && !requestLines[i].isEmpty()) {
			i++;
		}

		return i;
	}

	//Done
	private static String removeSpaces(String str) {
		return str.replaceAll("\\s*", "");
	}



	public static class URLParsedObject {

		public final String host;
		public final String path;
		public final int port;

		private URLParsedObject(String host, String path, int port) {
			this.host = host;
			this.path = path;
			this.port = port;
		}
	}
}

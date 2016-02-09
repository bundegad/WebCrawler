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

public class HTTPUtils {

	public static HTTPRequest parseRawHttpRequest(InputStream in) throws ServerException {

		HttpParsedMessageObject httpMessageObject = io.Utils.readHttpMessageFromInputStream(in);

		//Parse first line
		Pattern firstLinePattern = Pattern.compile(HTTPConstants.REQUEST_FIRSTLINE_PATTEN_STRING);
		Matcher matcher = firstLinePattern.matcher(removeSpaces(httpMessageObject.firstLine));


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

		//Construct request
		HTTPRequest request = new HTTPRequest("", path, type);
		request.version = version;
		request.setHeaders(httpMessageObject.headers);


		//Parse url params in case its get or head request.
		if (request.type == HTTPRequestType.GET || request.type == HTTPRequestType.HEAD) {
			parseURLParams(request);
		}

		request.body = new String(httpMessageObject.body);
		
		return request;
	}

	public static HTTPResponse parseRawHttpResponse(InputStream in) throws ServerException {

		HttpParsedMessageObject httpMessageObject = io.Utils.readHttpMessageFromInputStream(in);
		
		//clean first line
		String firstLine = removeSpaces(httpMessageObject.firstLine);


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
		response.addHeaders(httpMessageObject.headers);
		
		response.fileContent = httpMessageObject.body;
		
		
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

	public static boolean equalDomains(String domain1, String domain2) {
		return domain1.contains(domain2) || domain2.contains(domain1);
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

	public static class HttpParsedMessageObject {
		public final String firstLine;
		public final HashMap<String, String> headers;
		public final byte[] body;

		public HttpParsedMessageObject(String firstLine, HashMap<String, String> headers, byte[] body) {
			this.firstLine = firstLine;
			this.headers = headers;
			this.body = body;
		}
	}
}

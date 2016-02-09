package http;
import java.util.HashMap;


public class HTTPRequest {
	
	
	public String originRequest;
	public HTTPRequestType type;
	public String path;
	public String version;
	public HashMap<String, String> UrlParams;
	public HashMap<String, String> headers;
	public String body;
	
	public HTTPRequest(String originRequest, String path, HTTPRequestType type) {
		this(path, type);
		this.originRequest = originRequest;
	}
	
	public HTTPRequest(String path, HTTPRequestType type) {
		this.path = path;
		this.type = type;
	}
	
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	public void setParams(HashMap<String, String> params) {
		this.UrlParams = params;
	}

	public boolean shouldCloseConnection() {

		String connectionValue = this.headers.get(HTTPConstants.HTTP_CONNECTION_KEY);
		if (this.version.equals(HTTPConstants.HTTP_TYPE_1_0)) {
			return !HTTPConstants.HTTP_CONNECTION_KEEP_ALIVE.equals(connectionValue);
		} else {
			return HTTPConstants.HTTP_CONNECTION_CLOSE.equals(connectionValue);
		}
	} 
	
	public boolean isChunked(){	
		return HTTPConstants.HTTP_CHUNKED_KEY_YES.equals(headers.get(HTTPConstants.HTTP_CHUNKED_KEY));
	}


	public String toHTTPString() {
		
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		
		StringBuilder request = new StringBuilder(String.format("%s %s HTTP/1.1%s", type, path, HTTPConstants.CRLF));
		for (String key : headers.keySet() ) { 
		request.append(String.format("%s: %s%s", key, headers.get(key), HTTPConstants.CRLF)); 
		}
		request.append(HTTPConstants.CRLF);
		return request.toString();
	}
}



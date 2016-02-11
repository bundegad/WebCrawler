package http;

public class HTTPConstants {

	//Static Patterns Strings.
	public static String REQUEST_FIRSTLINE_PATTEN_STRING = "(.*?)/(.*)HTTP/(1[.][0-1])";
	public static String RESPONSE_FIRSTLINE_PATTEN_STRING = "HTTP/(1[.][0-1])([1-5][0-9]{2})(.*)";
	public static String REQUEST_HEADERLINE_PATTERN_STRING = "(.*?):(.*?)";

	//Constants Symbols/Tokens
	public static final String CRLF = "\r\n";
	
	
	//HTTP protocol constants
	public static final String HTTP_TYPE_1_0 = "1.0";
	public static final String HTTP_TYPE_1_1 = "1.1";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	
	public static final String HTTP_CONNECTION_KEY = "connection";
	public static final String HTTP_CONNECTION_CLOSE = "closed";
	public static final String HTTP_CONNECTION_KEEP_ALIVE = "keep-alive";
	
	public static final String HTTP_CONTENT_LENGTH_KEY = "content-length";
	public static final String HTTP_CONTENT_TYPE_KEY = "content-type";
	public static final String HTTP_CONTENT_TYPE_HTML = "text/html";
	
	public static final String HTTP_REFERER_KEY = "referer";
	public static final String HTTP_LOCALHOST_PREFIX = "http://localhost";
	
	public static final String HTTP_HOST_KEY = "host";
	
	public static final String HTTP_LOCATION_KEY = "location";
	public static final String HTTP_USER_AGENT_KEY = "user-agent";
	
	public static final String HTTP_CONTENT_MESSAGE_TYPE = "message/http";
	public static final String HTTP_TRANSFER_ENCODING = "transfer-encoding";
	public static final String HTTP_CHUNKED_KEY = "chunked";
	public static final String HTTP_CHUNKED_KEY_YES = "yes";
	
	public static final String HTTP_ON_PARAM_VALUE =  "on"; 
	
	public static final String DISALLOW = "disallow";
	public static final String ALLOW = "allow";
	
	public static final int SOCKET_DEFAULT_TIMEOUT_MS = 15000;
	public static final  int DEFAULT_PORT = 80;
}
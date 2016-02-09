package crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;

import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPRequestType;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.HTTPUtils;
import synchronization.ThreadPoolManager;


public class CrawlerDownloader implements Runnable {
	
	private enum ResourceType {
		IMAGE, VIDEO, DOCUMENT, PAGE, UNKNOWN
	}
	
	
	private String host;
	private int port;
	private String path;
	private ResourceType resourceType;
	private Socket socket;

	public CrawlerDownloader(String host, String path, int port)  {
		this.host = host;
		this.path = path;
		this.port = port;
		setType();
	}
	
	private void setType() {
		CrawlerManager manager = CrawlerManager.getInstance();
		
		if (manager.isImage(path)) {
			resourceType = ResourceType.IMAGE;
			return;
		}
		
		if (manager.isDocument(path)) {
			resourceType = ResourceType.DOCUMENT;
			return;
		}
		
		if (manager.isVideo(path)) {
			resourceType = ResourceType.VIDEO;
			return;
		}
		
		resourceType = ResourceType.PAGE;
		System.out.println("Path is " + path + "and resource type is " + resourceType + "==========");
	}


	@Override
	public void run() {
		
		
		System.out.println(String.format("downliading form host: %s, path: %s, and port: %s", host, path, port));
		
		HTTPRequest request = getRequest();
		
		try {
			
			//Connect to host.
			socket = new Socket();
			socket.setSoTimeout(HTTPConstants.SOCKET_DEFAULT_TIMEOUT_MS);
			socket.connect(new InetSocketAddress(host, port));
			CrawlerManager.getInstance().getExecutionRecord().addPort(port);
			
			//Write the request
			io.Utils.writeOutputStream(socket.getOutputStream(), request.toHTTPString().getBytes());
			
			
			//Parse response.
			boolean shouldReadBody = request.type == HTTPRequestType.GET;
			HTTPResponse response = HTTPUtils.parseRawHttpResponse(socket.getInputStream(), shouldReadBody);
			
			//Handle response
			if (response != null) {
				System.out.println(String.format("Received response with code : %s", response.code));
				handleResponse(response);
			}
			
			
			System.out.println(String.format("finish downliading form host: %s, path: %s, and port: %s", host, path, port));
			
		} catch (Exception e) {
			System.out.println(String.format("Error, could not get repsonse from Host: %s,"
					+ " on port: %s, and path: %s and message: %s", host, port, path, e.getMessage()));
			e.printStackTrace();
		} finally {
			
			if (socket != null) {
				
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Error, could not close socket");
				}
			}
		}
	}
	
	
	private void handleResponse(HTTPResponse response) throws UnsupportedEncodingException, URISyntaxException {
		
		//Handle redirect
		if (response.code == HTTPResponseCode.REDIRECT) {
			
			String redirectPath = response.getHeader(HTTPConstants.HTTP_LOCATION_KEY);
			if (redirectPath != null) {
				redirect(redirectPath);
				return;
			}
		}
		
//		if(response.getHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY)
//				.equals(HTTPConstants.HTTP_CONTENT_TYPE_HTML)) {
//			resourceType = ResourceType.UNKNOWN;
//		}
		
		
		//Handle OK
		if (response.code == HTTPResponseCode.OK) {
			
			int contentLength =  Integer.parseInt(response.getHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY));
			
			switch(this.resourceType) {
				case IMAGE:
					CrawlerManager.getInstance().getExecutionRecord().addImage(contentLength);
					break;
				case VIDEO:
					CrawlerManager.getInstance().getExecutionRecord().addVideo(contentLength);
					break;
				case DOCUMENT:
					CrawlerManager.getInstance().getExecutionRecord().addDocument(contentLength);
					break;
				case PAGE:
					CrawlerManager.getInstance().getExecutionRecord().addPage(contentLength);
					if (response.fileContent != null) {
						sendToAnalyzer(new String(response.fileContent));
					}
					break;
				case UNKNOWN:
					return;
			}
		}
		
	}

	private HTTPRequest getRequest() {
		
		HTTPRequestType type =  resourceType == ResourceType.PAGE ? HTTPRequestType.GET : HTTPRequestType.HEAD;
		HTTPRequest request = new HTTPRequest(path, type);
		
		HashMap<String, String> headers = getHeaders();
		request.setHeaders(headers);
		
		return request;
	}
	

	private HashMap<String, String> getHeaders() {
		HashMap<String, String> headers = new HashMap<>();
		headers.put(HTTPConstants.HTTP_CONNECTION_KEY, HTTPConstants.HTTP_CONNECTION_CLOSE);
		headers.put(HTTPConstants.HTTP_HOST_KEY, host);
		headers.put(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, "0");
		return headers;
	}

	private void redirect(String url) throws URISyntaxException {
		
		if (!url.startsWith("http://") && !url.startsWith(host)) {
			url = String.format("http://%s%s", host, url);
		}
		
		System.out.println("Redirecting to " + url);
		
		//parsed url then create downloader
		HTTPUtils.URLParsedObject urlParsedObject = HTTPUtils.parsedRawURL(url);
		CrawlerDownloader redirectDownloader  = new CrawlerDownloader(urlParsedObject.host,
				urlParsedObject.path, urlParsedObject.port);
		
		//Send the downloader to pool.
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(redirectDownloader);
	}
	
	private void sendToAnalyzer(String content) {
		System.out.println("Sending to anyalyzer content for path " + path);
		CrawlerAnalyzer redirectDownloader  = new CrawlerAnalyzer(host, path, content);
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.get(CrawlerExecuter.ANALYZERS_POOL_KEY).execute(redirectDownloader);
	}
	
}

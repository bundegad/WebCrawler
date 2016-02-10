package crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;

import exceptions.ServerException;
import http.HTTPConstants;
import http.HTTPRequest;
import http.HTTPRequestType;
import http.HTTPResponse;
import http.HTTPResponseCode;
import http.HTTPUtils;
import synchronization.ThreadPoolManager;


public class CrawlerDownloader implements Runnable {

	private enum ResourceType {
		IMAGE, VIDEO, DOCUMENT, PAGE
	}


	private String host;
	private int port;
	private String path;
	private ResourceType resourceType;
	private Socket socket;
	private boolean isCheckRobot;
	public static final String ROBOT_PATH = "/robots.txt";
	
	CrawlerExecutionRecord record = CrawlerManager.getInstance().getExecutionRecord();

	public CrawlerDownloader(String host, String path, int port)  {
		this.host = host;
		this.path = path;
		this.port = port;
		this.isCheckRobot = false;
		setType();
	}

	public void enableRobot() {
		this.isCheckRobot = true;
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
			System.out.println(path);
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
			
			long rtt = connectSocketWithRtt();
			CrawlerManager.getInstance().getExecutionRecord().addPort(port);
			CrawlerManager.getInstance().getExecutionRecord().addRTT(rtt);
			
			//Check if robot 
			if (isCheckRobot) {
				handleRobot();
				socket.close();
				return;
			}

			//Write the request
			io.Utils.writeOutputStream(socket.getOutputStream(), request.toHTTPString().getBytes());


			//Parse response.
			boolean shouldReadBody = request.type != HTTPRequestType.HEAD;
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


	private void handleRobot() throws IOException, ServerException {
		
		HTTPRequest request = new HTTPRequest(ROBOT_PATH, HTTPRequestType.GET);
		request.setHeaders(new HashMap<String, String>());
		
		//Write the request
		io.Utils.writeOutputStream(socket.getOutputStream(), request.toHTTPString().getBytes());


		//Parse response.
		boolean shouldReadBody = request.type != HTTPRequestType.HEAD;
		HTTPResponse response = HTTPUtils.parseRawHttpResponse(socket.getInputStream(), shouldReadBody);
		
		if (response.code != HTTPResponseCode.OK) {
			System.out.println("Could not get robot.txt");
		}
		
		CrawlerDownloader redirectDownloader = new CrawlerDownloader(this.host, this.path, this.port);
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(redirectDownloader);
		
	}

	private void handleResponse(HTTPResponse response) throws UnsupportedEncodingException, URISyntaxException {


		//Handle redirect
		if (response.code == HTTPResponseCode.REDIRECT) {

			String redirectPath = response.getHeader(HTTPConstants.HTTP_LOCATION_KEY);
			if (redirectPath == null) {
				return;
			}

			if (redirectPath.startsWith("/")) {
				redirectPath = String.format("%s%s", host, redirectPath);
			}


			redirect(redirectPath);
			return;
		}

		if (response.code != HTTPResponseCode.OK) {
			return;
		}

		
		//Handle OK
		int contentLength =  Integer.parseInt(response.getHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY));

		switch(this.resourceType) {
		case IMAGE:
			System.out.println("add an image");
			CrawlerManager.getInstance().getExecutionRecord().addImage(contentLength);
			break;
		case VIDEO:
			System.out.println("add an video");
			CrawlerManager.getInstance().getExecutionRecord().addVideo(contentLength);
			break;
		case DOCUMENT:
			System.out.println("add an document");
			CrawlerManager.getInstance().getExecutionRecord().addDocument(contentLength);
			break;
		case PAGE:
			System.out.println("add an page");
			CrawlerManager.getInstance().getExecutionRecord().addPage(contentLength);
			if (shouldAnalyze(response)) {
				sendToAnalyzer(new String(response.fileContent));
			} else {
				System.out.println("Not html ignoring anlyzer");
				System.out.println(response.toString());
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

		//parsed url then create downloader
		HTTPUtils.URLParsedObject urlParsedObject = HTTPUtils.parsedRawURL(url);
		if (urlParsedObject == null) {
			System.out.println("Not supported http schema,  stopping crawler");
			return;
		}

		if (!HTTPUtils.equalDomains(this.host, urlParsedObject.host)) {
			System.out.println("domain not equal ignoring redirect");
		}

		CrawlerDownloader redirectDownloader  = new CrawlerDownloader(urlParsedObject.host,
				urlParsedObject.path, urlParsedObject.port);
		if (isCheckRobot) {
			redirectDownloader.enableRobot();
		}

		//Send the downloader to pool.
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(redirectDownloader);
	}

	private boolean shouldAnalyze(HTTPResponse response) {
		if (response.getHeader(HTTPConstants.HTTP_CONNECTION_KEY) == null) {
			return true;
		}

		return response.getHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY).contains(HTTPConstants.HTTP_CONTENT_TYPE_HTML)
				&& response.fileContent != null;
	}

	private void sendToAnalyzer(String content) {
		System.out.println("Sending to anyalyzer content for path " + path);
		CrawlerAnalyzer redirectDownloader  = new CrawlerAnalyzer(host, path, content);
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.get(CrawlerExecuter.ANALYZERS_POOL_KEY).execute(redirectDownloader);
	}

	private long connectSocketWithRtt() throws IOException {
		long startTime = System.currentTimeMillis();
		socket.connect(new InetSocketAddress(host, port));
		return System.currentTimeMillis() - startTime;
	}
}

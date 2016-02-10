package crawler;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import http.HTTPUtils;
import synchronization.ThreadPoolManager;

public class CrawlerAnalyzer implements Runnable {

	//private static final Pattern LINK_TAG_PATTERN = Pattern.compile("(src|href)\\s*=\\s*(\"[^\"]+\"|'[^']+')");
	
	private static final Pattern IMG_OR_VIDEO_TAG_PATTERN = Pattern.compile("<(img|source|script).* (src)\\s*=\\s*(\"[^\"]+\"|'[^']+')");
	private static final Pattern A_OR_LINK_TAG_PATTERN = Pattern.compile("<(a|link).* (href)\\s*=\\s*(\"[^\"]+\"|'[^']+')");
	
	private String fileContent;
	private String host;
	private String path;

	public CrawlerAnalyzer(String host, String path, String fileContent) {
		this.fileContent = fileContent;
		this.host = host;
		this.path = path;
	}


	@Override
	public void run() {

		System.out.println(String.format("Analyzing file from host : %s, and path : %s", host, path));

		Matcher matcher1 = IMG_OR_VIDEO_TAG_PATTERN.matcher(fileContent);
		while (matcher1.find()) {
			try {
				String url = matcher1.group(3).replace("\"", "").replace("\'", "");
				System.out.println("url that regex found is " + url);
				url = getAbsoulutePath(url);
				if (url == null) {
					System.out.println("found unsupported url schema for url " + url);
					continue;
				}
				System.out.println("found url " + url);

				foundLink(url);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				System.out.println("Cannot parsed url");
			} 

		}
		Matcher matcher2 = A_OR_LINK_TAG_PATTERN.matcher(fileContent);
		while (matcher2.find()) {
			try {
				String url = matcher2.group(3).replace("\"", "").replace("\'", "");
				System.out.println("url that regex found is LINK " + url);
				url = getAbsoulutePath(url);
				if (url == null) {
					System.out.println("found unsupported url schema for url " + url);
					continue;
				}
				System.out.println("found url " + url);

				foundLink(url);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				System.out.println("Cannot parsed url");
			} 

		}


		System.out.println("finish analyzer");	
	}

	private String getAbsoulutePath(String url) throws URISyntaxException, UnsupportedEncodingException {

		if (url.startsWith("/")) {
			String absoluteInternalUrl = String.format("%s%s", host, url);
			absoluteInternalUrl = URLDecoder.decode(absoluteInternalUrl, "UTF-8");
			return absoluteInternalUrl;
		}

		HTTPUtils.URLParsedObject parseObj = HTTPUtils.parsedRawURL(url);
		
		//Check if Url starts with https and is not supported
		if (parseObj == null) {
			
			//Remove https
			url = url.substring(8);
			if (!isInternal(url)) {
				String domain  = extractDomain(url);
				CrawlerManager.getInstance().getExecutionRecord().addDomain(domain);
				return null;
			}
		}
		
		String parsedUrl =  String.format("%s%s", parseObj.host, parseObj.path);
		return parsedUrl;
	}

	private boolean isInternal(String url) {
		return HTTPUtils.equalDomains(url, host);
	}

	private String extractDomain(String url) {
		int indexOfSlash = url.indexOf("/");
		return indexOfSlash == -1 ? url : url.substring(0, indexOfSlash);
	}

	private void foundLink(String url) throws URISyntaxException {

		CrawlerExecutionRecord record = CrawlerManager.getInstance().getExecutionRecord();
		if (!record.shouldAddResource(url)) {
			return;
		}

		if (!isInternal(url)) {
			String domain  = extractDomain(url);
			record.addDomain(domain);
			return;
		}

		
		HTTPUtils.URLParsedObject parsedObject  =  HTTPUtils.parsedRawURL(url);
		record.addResource(parsedObject.path);
		CrawlerDownloader downloader = new CrawlerDownloader(parsedObject.host, parsedObject.path, parsedObject.port);
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(downloader);

	}
}

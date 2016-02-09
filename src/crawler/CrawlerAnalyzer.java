package crawler;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import http.HTTPUtils;
import synchronization.ThreadPoolManager;

public class CrawlerAnalyzer implements Runnable {


	private static final Pattern LINK_TAG_PATTERN = Pattern.compile("(src|href)\\s*=\\s*(\"[^\"]+\"|'[^']+')");



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

		Matcher matcher = LINK_TAG_PATTERN.matcher(fileContent);
		while (matcher.find()) {
			try {
				String url = matcher.group(2).replace("\"", "").replace("\'", "");
				System.out.println("url that regex found is " + url);
				url = getAbsoulutePath(url);
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
		String parsedUrl =  String.format("%s%s", parseObj.host, parseObj.path);
		return parsedUrl;
	}

	private boolean isInternal(String url) {
		return url.startsWith(host);
	}

	private String extractDomain(String url) {
		int indexOfSlash = url.indexOf("/");
		return indexOfSlash == -1 ? url : url.substring(0, indexOfSlash);
	}

	private void foundLink(String url) throws URISyntaxException {

		CrawlerExecutionRecord record = CrawlerManager.getInstance().getExecutionRecord();
		if (record.hasResouce(url)) {
			return;
		}

		if (!isInternal(url)) {
			String domain  = extractDomain(url);
			record.addDomain(domain);
			return;
		}

		HTTPUtils.URLParsedObject parsedObject  =  HTTPUtils.parsedRawURL(url);
		CrawlerDownloader downloader = new CrawlerDownloader(parsedObject.host, parsedObject.path, parsedObject.port);
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(downloader);

	}
}

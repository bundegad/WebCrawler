package crawler;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import http.HTTPUtils;
import synchronization.ThreadPoolManager;

public class CrawlerAnalyzer implements Runnable {
	
	private static final Pattern IMG_TAG_PATTERN = Pattern.compile("(?i)<(img)\\s[^>]*(src)\\s*=\\s*(\"[^\"]+\"|'[^']+')");
	private static final Pattern A_TAG_PATTERN = Pattern.compile("(?i)<(a)\\s[^>]*(href)\\s*=\\s*(\"[^\"]+\"|'[^']+')");
	
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

		//HTML with no Comments
		String fileContentNoComments = fileContent.replaceAll("(?s)<!--(.*?)-->", "");

		System.out.println(String.format("Analyzing file from host : %s, and path : %s", host, path));
	
		Matcher matcher1 = IMG_TAG_PATTERN.matcher(fileContentNoComments);
		while (matcher1.find()) {
			try {
				String url = matcher1.group(3).replace("\"", "").replace("\'", "");
				url = getAbsoulutePath(url);
				if (url == null) {
					System.out.println("found unsupported url schema for url " + url);
					continue;
				}

				foundLink(url);
			} catch (URISyntaxException | UnsupportedEncodingException e) {
				System.out.println("Cannot parsed url");
			} 

		}
		Matcher matcher2 = A_TAG_PATTERN.matcher(fileContentNoComments);
		while (matcher2.find()) {
			try {
				String url = matcher2.group(3).replace("\"", "").replace("\'", "");
				url = getAbsoulutePath(url);
				if (url == null) {
					System.out.println("found unsupported url schema for url " + url);
					continue;
				}

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
			return cleanWWW(absoluteInternalUrl);
		}

		
		//Check if Url starts with https and is not supported
		if (url.startsWith("https://")) {
			
			//Remove https
			url = url.substring(8);
			if (!isInternal(url)) {
				String domain  = cleanWWW(extractDomain(url));
				CrawlerManager.getInstance().getExecutionRecord().addDomain(domain);
				System.out.println("adding domain for " + url);
			}
			
			return null;
		}
		
		if (url.startsWith("http://")) {
			url = url.substring(7);
		}
		
		if (!url.startsWith("www.")) {
			String path = this.path.endsWith("/") ? this.path : this.path + "/";
			return cleanWWW(String.format("%s%s%s",host, path, url));
		}
		
		
		return cleanWWW(url);
	}

	private boolean isInternal(String url) throws URISyntaxException {
		int indexofSpliter = url.indexOf("/");
		url = indexofSpliter == -1 ? url : url.substring(0, indexofSpliter);
		return HTTPUtils.equalDomains(HTTPUtils.parsedRawURL(url).host, host);

	}

	private String extractDomain(String url) {
		int indexOfSlash = url.indexOf("/");
		return indexOfSlash == -1 ? url : url.substring(0, indexOfSlash);
	}

	private void foundLink(String url) throws URISyntaxException {
		
		CrawlerExecutionRecord record = CrawlerManager.getInstance().getExecutionRecord();	
		if (!record.shouldAddResource(url)) {
			System.out.println("ignoring resource " + url);
			return;
		}

		if (!isInternal(url)) {
			System.out.println("adding external link " + url);
			record.addExternalLink();
			String domain  = extractDomain(url);
			record.addDomain(domain);
			return;
		}
		

		System.out.println("adding internal link " + url);
		record.addInternalLink();
		HTTPUtils.URLParsedObject parsedObject  =  HTTPUtils.parsedRawURL(url);
		record.addResource(url);
		CrawlerDownloader downloader = new CrawlerDownloader(parsedObject.host, parsedObject.path, parsedObject.port);
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.get(CrawlerExecuter.DONWLOADERS_POOL_KEY).execute(downloader);

	}
	
	private String cleanWWW(String url) {
		return url.startsWith("www.") ? url.substring(4) : url;
	}
}

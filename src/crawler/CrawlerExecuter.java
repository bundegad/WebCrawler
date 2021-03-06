package crawler;

import java.net.URISyntaxException;

import crawler.CrawlerTcpScanner.IFoundPortCallback;
import exceptions.ServerException;
import http.HTTPUtils;
import synchronization.ThreadPool;
import synchronization.ThreadPoolManager;
import synchronization.ThreadPoolManager.IOnEmptyCallback;

public class CrawlerExecuter {
	
	
	public static final String ANALYZERS_POOL_KEY = "analyzers_pool_key";
	public static final String DONWLOADERS_POOL_KEY = "downloaders_pool_key";
	public static final String SCANNERS_POOL_KEY = "scanners_pool_key";
	
	private final IOnEmptyCallback ON_PORT_SCANNED_CB = new IOnEmptyCallback() {
		
		@Override
		public void onEmpty() {
			System.out.println("finished port scanning");
			ThreadPoolManager.getInstance().get(SCANNERS_POOL_KEY).stop();
			CrawlerManager.getInstance().setProgress(40);
			CrawlerManager.getInstance().setProgressBuffer(95);
			try {
				startCrawling();
			} catch (URISyntaxException e) {
				System.out.println("Error, Could not get the parsed url");
			}
		}
	};
	
	private final IOnEmptyCallback ON_DOWNLOADED_AND_ANALYZED_CB =  new IOnEmptyCallback() {
		
		@Override
		public void onEmpty() {
			
			
			System.out.println("finish crawling");
			stop();
			CrawlerManager  manager = CrawlerManager.getInstance();
			manager.createFile();
			
			try {
				manager.Stop();
			} catch (ServerException e) {
				System.out.println("could not stop crawling manager");
			}
			
		}
	};
	
	
	private CrawlerExecutionRecord record;
	
	public CrawlerExecuter(CrawlerExecutionRecord record) {
		this.record = record;
	}


	public void start() throws URISyntaxException {
		if (record.isFullTcp) {
			CrawlerManager.getInstance().setProgressBuffer(40);
			scanPorts();
		} else {
			CrawlerManager.getInstance().setProgressBuffer(85);
			startCrawling();
		}
	}
	
	private void scanPorts() {
		
		System.out.println("Start port scanning");
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.bind(new String[] {SCANNERS_POOL_KEY}, ON_PORT_SCANNED_CB);
		ThreadPool<Runnable> scannersPool = manager.get(SCANNERS_POOL_KEY);
		
		int numScanners = scannersPool.getSize();
		CrawlerTcpScanner[] scanners = getScanners(numScanners);
		scannersPool.start();
		
		scannersPool.executeAll(scanners);
	}
	
	private CrawlerTcpScanner[] getScanners(int size) {
		
		CrawlerTcpScanner[] scanners = new CrawlerTcpScanner[size];
		
		int numPortsPerScanner = CrawlerTcpScanner.MAX_PORT / size;
		int startPort = 0;
		int currentScanner = 0;
		String host = record.domain;
		if (host.startsWith("https://")) {
			host = host.substring(8);
		} else if (host.startsWith("http://")) {
			host = host.substring(7);
		}
		
		if (!host.startsWith("www.")) {
			host = "www." + host;
		}
		
		int indexOfSeperator = host.indexOf("/");
		host = indexOfSeperator == -1 ? host : host.substring(0, indexOfSeperator);
		
		while (startPort <= CrawlerTcpScanner.MAX_PORT) {
			
			IFoundPortCallback cb = new IFoundPortCallback() {
				
				@Override
				public void onFoundPort(int port) {
					System.out.println("found port " + port);
					addPort(port);
				}
			};
			
			int endPort = startPort + numPortsPerScanner;
			scanners[currentScanner] = new CrawlerTcpScanner(host, startPort, endPort, cb);
			
			startPort += numPortsPerScanner + 1;
			currentScanner++;
			
		}
		
		return scanners;
	}

	private void startCrawling() throws URISyntaxException {
		
		System.out.println("Start Crawling");
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.bind(new String[] {ANALYZERS_POOL_KEY, DONWLOADERS_POOL_KEY}, ON_DOWNLOADED_AND_ANALYZED_CB);
		
		ThreadPool<Runnable> downloadersPool = manager.get(DONWLOADERS_POOL_KEY);
		ThreadPool<Runnable> analyzersPool = manager.get(ANALYZERS_POOL_KEY);
		
		downloadersPool.start();
		analyzersPool.start();
					
		HTTPUtils.URLParsedObject urlObject = HTTPUtils.parsedRawURL(record.domain);
		if (urlObject == null) {
			System.out.println("Not supported http schema stopping crawler");
			return;
		}
		
		record.addResource(urlObject.path);
		CrawlerDownloader downloader = new CrawlerDownloader(urlObject.host, urlObject.path, urlObject.port);
		
		if (!record.isDisrespectRobot) {
			downloader.enableRobot();
		}
		
		downloadersPool.execute(downloader);

		
	}

	public void stop() {
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.get(ANALYZERS_POOL_KEY).stop();
		manager.get(DONWLOADERS_POOL_KEY).stop();
	}
	
	public void addPort(int port) {
		record.addPort(port);
	}
}

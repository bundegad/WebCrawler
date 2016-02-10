package crawler;

import java.net.URISyntaxException;

import crawler.CrawlerTcpScanner.IFoundPortCallback;
import exceptions.ServerException;
import http.HTTPUtils;
import synchronization.ThreadPool;
import synchronization.ThreadPoolManager;
import synchronization.ThreadPoolManager.IOnEmptyCallback;

public class CrawlerExecuter {
	
	private static boolean IGNORE_FULL_TCP = true;
	
	public static final String ANALYZERS_POOL_KEY = "analyzers_pool_key";
	public static final String DONWLOADERS_POOL_KEY = "downloaders_pool_key";
	public static final String SCANNERS_POOL_KEY = "scanners_pool_key";
	
	
	private final IOnEmptyCallback ON_PORT_SCANNED_CB = new IOnEmptyCallback() {
		
		@Override
		public void onEmpty() {
			System.out.println("finished port scanning");
			ThreadPoolManager.getInstance().get(SCANNERS_POOL_KEY).stop();
			CrawlerManager.getInstance().setProgress(40);
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
		if (record.isFullTcp && !IGNORE_FULL_TCP) {
			scanPorts();
		} else {
			startCrawling();
		}
	}
	
	private void scanPorts() {
		
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
		
		while (startPort <= CrawlerTcpScanner.MAX_PORT) {
			
			IFoundPortCallback cb = new IFoundPortCallback() {
				
				@Override
				public void onFoundPort(int port) {
					System.out.println("found port " + port);
					addPort(port);		
				}
			};
			
			int endPort = startPort + numPortsPerScanner;
			scanners[currentScanner] = new CrawlerTcpScanner(record.domain, startPort, endPort, cb);
			
			startPort += numPortsPerScanner + 1;
			currentScanner++;
			
		}
		
		return scanners;
	}

	private void startCrawling() throws URISyntaxException {
		
		ThreadPoolManager manager = ThreadPoolManager.getInstance();
		manager.bind(new String[] {ANALYZERS_POOL_KEY, DONWLOADERS_POOL_KEY}, ON_DOWNLOADED_AND_ANALYZED_CB);
		
		ThreadPool<Runnable> downloadersPool = manager.get(DONWLOADERS_POOL_KEY);
		ThreadPool<Runnable> analyzersPool = manager.get(ANALYZERS_POOL_KEY);
		
		downloadersPool.start();
		analyzersPool.start();
	
		CrawlerDownloader downloader;
				
		HTTPUtils.URLParsedObject urlObject = HTTPUtils.parsedRawURL(record.domain);
		if (urlObject == null) {
			System.out.println("Not supported http schema stopping crawler");
			return;
		}
		
		downloader = new CrawlerDownloader(urlObject.host, urlObject.path, urlObject.port);
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

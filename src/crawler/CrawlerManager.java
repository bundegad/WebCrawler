package crawler;


import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

import configuration.ConfigurationUtils;
import exceptions.ServerException;
import http.HTTPResponseCode;
import synchronization.ThreadPool;
import synchronization.ThreadPoolManager;

public class CrawlerManager {
	
	
	public enum State {
		WAITING, RUNNING, STOPPING
	}
	
	private static CrawlerManager manager;
	
	private String directory;
	
	private String[] imageExtensions;
	private String[] videoExtensions;
	private String[] documentExtensions;
	
	private State state;
	private double progress;
	
	private CrawlerExecutionRecord executionRecord;
	private CrawlerExecuter executor;
	
	public static  CrawlerManager getInstance() {
		if (manager == null) {
			manager = new CrawlerManager();
		}
		
		return manager;
	}
	
	private CrawlerManager() {
		state = State.WAITING;
		progress = 0;
	}
	
	public void setConfiguration(String path, JSONObject configuration)  {
		
		int maxAnalyzers = configuration.getInt(ConfigurationUtils.MAX_ANALYZERS_KEY);
		int maxDownloaders = configuration.getInt(ConfigurationUtils.MAX_DOWNLOADERS_KEY);
		
		ThreadPoolManager poolManager = ThreadPoolManager.getInstance();
		poolManager.add(new ThreadPool<Runnable>(maxAnalyzers), CrawlerExecuter.ANALYZERS_POOL_KEY);
		poolManager.add(new ThreadPool<Runnable>(maxDownloaders), CrawlerExecuter.DONWLOADERS_POOL_KEY);
		poolManager.add(new ThreadPool<>(maxDownloaders), CrawlerExecuter.SCANNERS_POOL_KEY);
		
		//Get the directory for crawler file.
		directory = path + configuration.getString(ConfigurationUtils.DIRECTORY_KEY);
		
		//Get Extensions
		JSONObject extensions = configuration.getJSONObject(ConfigurationUtils.EXTENSIONS_KEY);
		imageExtensions = ConfigurationUtils.JsonArrayToStringArray(
				extensions.getJSONArray(ConfigurationUtils.IMAGE_EXTENSIOSN_KEY));
		videoExtensions = ConfigurationUtils.JsonArrayToStringArray(
				extensions.getJSONArray(ConfigurationUtils.VIDEO_EXTENSIONS_KEY));
		documentExtensions = ConfigurationUtils.JsonArrayToStringArray(
				extensions.getJSONArray(ConfigurationUtils.DOCUMENT_EXTENSIONS_KEY));
		
	}
		
	public synchronized void Start(final String domain, final boolean shouldFullTcp, 
		final boolean shouldDisrespectRobot) throws ServerException, URISyntaxException {
		
		if (state != State.WAITING) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		
		executionRecord = new CrawlerExecutionRecord(domain, shouldFullTcp, shouldDisrespectRobot);
		executor = new CrawlerExecuter(executionRecord);	
		executor.start();
		
		state  = State.RUNNING;
		progress = 10;
		
		System.out.println(String.format("crawler manager started with domain:%s, full-tcp:%s, disRobot:%s",
				domain, shouldFullTcp, shouldDisrespectRobot));
	}
	
	public synchronized void Stop() throws ServerException {
		
		if (state != State.RUNNING) {
			throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
		}
		
		createFile();
		state = State.STOPPING;
		progress = 0;
		executor.stop();
		executor = null;
		
		System.out.println("crawler manager stopped");
		state = State.WAITING;
	}
	
	public synchronized State getState() {
		return state;
	}
	
	public synchronized void setProgress(double progress) {
		this.progress = progress;
	}
	public synchronized double getProgress() {
		return progress;
	}
	
	public CrawlerExecutionRecord getExecutionRecord() {
		return this.executionRecord;
	}
	
	public boolean isImage(String file) {
		file = removeQuery(file);
		for (String extension : imageExtensions) {
			if (file.endsWith(extension)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isDocument(String file) {
		file = removeQuery(file);
		for (String extension : documentExtensions) {
			if (file.endsWith(extension)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isVideo(String file) {
		file = removeQuery(file);
		for (String extension : videoExtensions) {
			if (file.endsWith(extension)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public JSONObject getPastExecution(String id) {
		String path = String.format("%s%s.json", directory, id);
		return FileManager.getInstance().getJsonFile(path);
	}
	
	public JSONArray getPastExecutions() {
		return FileManager.getInstance().getAllJsonFiles(directory);
	}
	
	public void createFile() {

		
		if (executionRecord == null) {
			return;
		}
		
		JSONObject fileObject = executionRecord.toJson();
		String id = executionRecord.getId();
		FileManager manager = FileManager.getInstance();
		
		boolean isFileWritten = manager.createJsonFile(directory, id, fileObject);
		if (!isFileWritten) {
			//do stuff.
		}
	}
	
	private String removeQuery(String file) {
		return file.replace("\\?.*", "");
	}
}

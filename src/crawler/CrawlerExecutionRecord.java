package crawler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class CrawlerExecutionRecord {

	private static final String VALUE_KEY = "value";

	private final String id;
	public final String domain;
	public final boolean isFullTcp;
	public final boolean isDisrespectRobot;
	private String disrespectPath;
	private final ArrayList<Integer> ports;
	private final ArrayList<String> domains;
	private final String dateTime;

	private long sumRTT;
	private long numRTT;

	private int numDocuments;
	private int sizeDocuments;

	private int numPages;
	private int sizePages;

	private int numImages;
	private int sizeImages;

	private int numVideos;
	private int sizeVideos;

	private int numInternalLinks;
	private int numExternalLinks;

	private ArrayList<String> resources;

	public CrawlerExecutionRecord(String domain, boolean isFullTcp, boolean isDisrespectRobot) {
		this.domain = domain;
		this.isFullTcp = isFullTcp;
		this.isDisrespectRobot = isDisrespectRobot;
		this.ports = new ArrayList<>();
		this.domains = new ArrayList<>();
		this.dateTime = new Date(System.currentTimeMillis()).toString();
		this.id  = UUID.randomUUID().toString();
		this.resources = new ArrayList<>();
	}
	
	public void addDisrespectPath(String path) {
		this.disrespectPath = path;
	}

	public String getId() {
		return this.id;
	}

	public JSONObject toJson() {

		JSONObject jsonResult = new JSONObject();
		jsonResult.put(ExecutionFileConstants.DOMAIN, domain);
		jsonResult.put(ExecutionFileConstants.IS_FULL_TCP_SCAN, Boolean.toString(isFullTcp));
		jsonResult.put(ExecutionFileConstants.IS_DISRESPECT_ROBOTS, Boolean.toString(isDisrespectRobot));

		JSONArray portsJson = new JSONArray();
		for (Integer port : ports) {
			JSONObject portJsonObject = new JSONObject();
			portJsonObject.put(VALUE_KEY, port.toString());
			portsJson.put(portJsonObject);
		}

		jsonResult.put(ExecutionFileConstants.OPEN_PORTS, portsJson);
		jsonResult.put(ExecutionFileConstants.NUMBER_OPEN_PORTS, Integer.toString(portsJson.length()));
		numRTT = numRTT == 0 ? 1 : numRTT;
		jsonResult.put(ExecutionFileConstants.AVERAGE_RTT, Double.toString(sumRTT/(double)numRTT));


		JSONArray domainsJson = new JSONArray();
		for (String connectedDomain : domains) {
			JSONObject domainJsonObject = new JSONObject();
			domainJsonObject.put(VALUE_KEY, connectedDomain);
			domainsJson.put(domainJsonObject);
		}

		jsonResult.put(ExecutionFileConstants.CONNECTED_DOMAINS, domainsJson);
		jsonResult.put(ExecutionFileConstants.NUMBER_CONNECTED_DOMAINS, Integer.toString(domainsJson.length()));

		jsonResult.put(ExecutionFileConstants.NUMBER_DOCUMENTS, Integer.toString(numDocuments));
		jsonResult.put(ExecutionFileConstants.SIZE_DOCUMENTS, Integer.toString(sizeDocuments));

		jsonResult.put(ExecutionFileConstants.NUMBER_PAGES, Integer.toString(numPages));
		jsonResult.put(ExecutionFileConstants.SIZE_PAGES, Integer.toString(sizePages));

		jsonResult.put(ExecutionFileConstants.NUMBER_IMAGES, Integer.toString(numImages));
		jsonResult.put(ExecutionFileConstants.SIZE_IMAGES, Integer.toString(sizeImages));

		jsonResult.put(ExecutionFileConstants.NUMBER_VIDEOS, Integer.toString(numVideos));
		jsonResult.put(ExecutionFileConstants.SIZE_VIDEOS, Integer.toString(sizeVideos));

		jsonResult.put(ExecutionFileConstants.NUMBER_INTERNAL_LINKS, Integer.toString(numInternalLinks));
		jsonResult.put(ExecutionFileConstants.NUMBER_EXTERNAL_LINKS, Integer.toString(numExternalLinks));

		jsonResult.put(ExecutionFileConstants.DATE_TIME, this.dateTime);
		jsonResult.put(ExecutionFileConstants.ID, this.id);


		return jsonResult;
	}

	public synchronized void addPort(int port) {
		if (!this.ports.contains(port)) {
			this.ports.add(port);
		}
	}

	public synchronized void addImage(int size) {
		this.numImages++;
		this.sizeImages += size;
	}

	public synchronized void addDocument(int size) {
		this.numDocuments++;
		this.sizeDocuments += size;
	}

	public synchronized void addVideo(int size) {
		this.numVideos++;
		this.sizeVideos += size;
	}

	public synchronized void addPage(int size) {
		this.numPages++;
		this.sizePages += size;
	}

	private boolean hasResouce(String resource) {
		return resources.contains(resource);
	}
	
	public synchronized  boolean shouldAddResource(String resource) {
		return !hasResouce(resource) && (disrespectPath == null || resource.startsWith(disrespectPath));
	}
	
	public synchronized void addResource(String resource) {
		this.resources.add(resource);
	}
	
	public synchronized void addDomain(String domain) {
		if (!domains.contains(domain)) {
			domains.add(domain);
		}
	}
	
	public synchronized void addExternalLink() {
		this.numExternalLinks++;
	} 
	
	public synchronized void addInternalLink() {
		this.numInternalLinks++;
	}
	
	public synchronized void addRTT(long rtt) {
		sumRTT += rtt;
		numRTT++;
	}
}

package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileManager {
	
	private static FileManager manager;
	
	
	private FileManager() {}
	
	public  void setConfiguration(JSONObject comfiguration) {
		
	}
	
	public static FileManager getInstance() {
		if (manager == null) {
			manager = new FileManager();
		}
		
		return manager;
	}
	
	public boolean createJsonFile(String directory, String id,  JSONObject data) {
		
		if (data == null || id == null) {
			return false;
		}
		
		data.put(ExecutionFileConstants.ID, id);
		String file = String.format("%s%s.json", directory, id);
		byte[] bytes = data.toString().getBytes();
		
		FileOutputStream writer = null;
		
		try {
			writer = new FileOutputStream(file);
			writer.write(bytes);
			writer.close();
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	public JSONObject getJsonFile(String path) {
		
		if (path == null || !io.Utils.isValidFile(path)) {
			return null;
		}
		
		String data = null;
		try {
			data = io.Utils.readFile(path);
		} catch (IOException e) {
			return null;
		}
		
		JSONObject object  = null;
		try {
			object = new JSONObject(data);
		} catch (JSONException e) {
			return null;
		}
		
		return object;
	}
	
	public JSONArray getAllJsonFiles(String directory) {
		
		if (directory == null || !io.Utils.isValidDirectory(directory)) {
			return null;
		}
		
		File folder = new File(directory);
		File[] filesList = folder.listFiles();
		
		JSONArray objectsLists = new JSONArray();
		for (File file : filesList) { 
			
			if (file.isDirectory() || !file.getName().endsWith(".json")) {
				continue;
			}
			
			JSONObject object = getJsonFile(file.getAbsolutePath());
			if (object == null) {
				continue;
			}
			
			objectsLists.put(object);
		}
		
		return objectsLists;
	}
}

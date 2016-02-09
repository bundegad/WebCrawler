package html;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HtmlGenerator {
	
	//Patterns constant strings
	public static final String OPEN_SINGLE_PATTERN_STRING = "[@][{]\\s*";
	public static final String OPEN_FOR_PATTERN_STRING = "[@]for[{]\\s*";
	public static final String CLOSE_SINGLE_PATTERN_STRING = "\\s*[}]\\s*";
	public static final String CLOSE_FOR_PATTERN_STRING = "\\s*[@]for[}]";
	public static final String FOR_SEPERATOR_PATTERN_STRING = "\\s*[=][>]\\s*";
	
	public static final String VARIABLE_PATTERN_STRING = "\\s*[a-zA-Z0-9]+\\s*";
	
	public static final String OBJECT_VALUE_KEY = "value"; 
	
	public static final String SINGLE_REPLACEMENT_PATTERN_STRING = String.format(
			"%s(%s)%s",
			OPEN_SINGLE_PATTERN_STRING,
			VARIABLE_PATTERN_STRING,
			CLOSE_SINGLE_PATTERN_STRING);
	
	public static final String FOR_REPLACEMENT_PATTERN_STRING = String.format(
			"%s(%s)%s(.*?)%s",
			OPEN_FOR_PATTERN_STRING,
			VARIABLE_PATTERN_STRING,
			FOR_SEPERATOR_PATTERN_STRING,
			CLOSE_FOR_PATTERN_STRING);
	
	
	
	
	private final JSONObject data;
	private final String filePath;
	
	public HtmlGenerator(JSONObject data, String filePath) {
		this.data = data;
		this.filePath = filePath;
	}
	
	public byte[] generate() throws IOException {
		String fileContent = readFile();
		if (data == null) {
			return fileContent.getBytes();
		}
		
		String result = replaceFor(fileContent, data);
		result =  replaceSingle(result, data);
		return result.getBytes();
	}
	
	private String replaceSingle(String file, JSONObject data) throws JSONException {
		
		Pattern pattern = Pattern.compile(SINGLE_REPLACEMENT_PATTERN_STRING);
		Matcher matcher = pattern.matcher(file);
		StringBuffer stringBuffer = new StringBuffer();
		
		while (matcher.find()) {
			String objectValue = data.has(matcher.group(1)) ? 
					data.getString(matcher.group(1).trim()) : "";
			matcher.appendReplacement(stringBuffer, objectValue);
		}
		
		matcher.appendTail(stringBuffer);
		return stringBuffer.toString();
	}
	
	private String replaceFor(String file, JSONObject data) throws JSONException {
		
		Pattern pattern = Pattern.compile(FOR_REPLACEMENT_PATTERN_STRING);
		Matcher matcher = pattern.matcher(file);
		StringBuffer stringBuffer = new StringBuffer();
		
		while (matcher.find()) {
			JSONArray entities = data.getJSONArray(matcher.group(1).trim());
			String htmlEntity = matcher.group(2);
			
			StringBuilder replacementBuilder = new StringBuilder();
			for (int i = 0; i < entities.length(); i++) {
				JSONObject entity = entities.getJSONObject(i);
				replacementBuilder.append(replaceSingle(htmlEntity, entity));
			}
			
			matcher.appendReplacement(stringBuffer, replacementBuilder.toString());
		}
		
		matcher.appendTail(stringBuffer);
		return stringBuffer.toString();
	}
	

	private String readFile() throws IOException {
		return io.Utils.readFile(filePath);
	}
}

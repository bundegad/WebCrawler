package http;
import java.util.HashMap;


public class HTTPResponse {
	
	
	public HTTPResponseCode code;
	private HashMap<String, String> headears;
	private String version;
	public boolean shouldAttachFile;
	public byte[] fileContent;
	
	
	public HTTPResponse(HTTPResponseCode code, String version) {
		this.code = code;
		this.version = version;
		this.headears = new HashMap<>();
		this.fileContent = null;
	}
	
	public HTTPResponse(String rawResponse) {
		
	}
	
	public void addHeader(String headerName, String headerValue) {		
		this.headears.put(headerName, headerValue);
	}
	
	public void addHeaders(HashMap<String, String> headers) {
		for (String header : headers.keySet()) {
			addHeader(header, headers.get(header));
		}
	}
	
	public String getHeader(String key) {
		return headears.get(key);
	}
	
	public void attachFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("HTTP/%s %s%s", this.version,
					this.code.toString(), HTTPConstants.CRLF));
		
		for (String headerName : this.headears.keySet()) {
			builder.append(String.format("%s : %s%s", headerName, this.headears.get(headerName), HTTPConstants.CRLF));
		}
		
		builder.append(HTTPConstants.CRLF);
		return builder.toString();
	}
	
	public boolean isChunked() {
		return HTTPConstants.HTTP_CHUNKED_KEY.equals(headears.get(HTTPConstants.HTTP_TRANSFER_ENCODING));
	}
	
	public String getContentLength() {
		if (!headears.containsKey(HTTPConstants.HTTP_CONTENT_LENGTH_KEY)) {
			return "0";
		}
		
		return headears.get(HTTPConstants.HTTP_CONTENT_LENGTH_KEY);
	}
	

}

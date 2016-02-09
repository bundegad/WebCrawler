package exceptions;

import http.HTTPResponseCode;

public class ServerException extends Exception {
	
	private static final long serialVersionUID = 3043868578737035149L;
	public final HTTPResponseCode code;
	
	public ServerException(HTTPResponseCode code) {
		super();
		this.code = code;
	}
}

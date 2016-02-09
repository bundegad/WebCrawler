package http;


public enum HTTPRequestType {
		GET, POST, TRACE, HEAD, NOT_SUPPORTED;
		
		public static HTTPRequestType convertFromString(String str) {
			
			switch (str.toUpperCase()) {
			case "GET":
				return GET;
			case "POST":
				return POST;
			case "HEAD":
				return HEAD;
			case "TRACE":
				return TRACE;
			default:
				return NOT_SUPPORTED;
			}
		}
	}
	
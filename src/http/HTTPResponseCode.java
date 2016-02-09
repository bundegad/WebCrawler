package http;

public enum HTTPResponseCode {
		OK,
		NOT_FOUND,
		NOT_IMPLEMENTEED,
		BAD_REQUEST,
		FORBIDDEN,
		INTERNAL_ERROR,
		REQUEST_RECEIVED,
		REDIRECT;
		
		public String toString() {	
			switch(this) {	
			case REQUEST_RECEIVED:
				return "100 Request Received";
			case OK:
				return "200 OK";
			case REDIRECT:
				return "300 Redirect";
			case BAD_REQUEST: 
				return "400 Bad Request";
			case FORBIDDEN:
				return "403 Forbidden";
			case NOT_FOUND: 
				return "404 Not Found";
			case NOT_IMPLEMENTEED:
				return "501 Not Implemented";
			case INTERNAL_ERROR:
				return "500 Internal Server Error";
			default: 
				return "Unknown";
			}

		}
		
		
		
		public static HTTPResponseCode convertFromInt(int code) {
			
			switch (code) {
				
			case 100:
			case 101:
				return REQUEST_RECEIVED;
			case 200:
			case 201:
			case 202:
			case 203:
			case 204:
			case 205:
				return OK;
			case 300:
			case 301:
			case 302:
			case 303:
			case 304:
			case 305:
			case 307:
				return REDIRECT;
			case 400:
			case 401:
			case 402:
			case 405:
			case 406:
			case 407:
			case 408:
			case 409:
			case 410:
			case 411:
			case 412:
			case 413:
			case 414:
			case 415:
			case 416:
			case 417:
				return BAD_REQUEST;
			case 403:
				return FORBIDDEN;
			case 404:
				return NOT_FOUND;
			case 500:
			case 502:
			case 503:
			case 504:
			case 505:
				return INTERNAL_ERROR;
			case 501:
				return NOT_IMPLEMENTEED;
			default:
				return null;
			}
		}
}
	
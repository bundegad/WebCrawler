package http;
import java.io.IOException;
import java.net.Socket;
import exceptions.ServerException;
import handlers.AbstractBaseHandler;
import handlers.ErrorsHandler;


public class HTTPRequestHandler implements Runnable {

	
	private final Socket connection;
	private  HTTPRequest request;
	private HTTPResponse response;
	private Router router;

	public HTTPRequestHandler(Socket connection,  Router router)  {
		this.connection = connection;
		this.router = router;
	}


	public void run() {
		try {

			//Parse the message
			request = HTTPUtils.parseRawHttpRequest(connection.getInputStream());
			
			//Validate not null
			if (request == null) {
				return;
			}
			
			routeRequest();
			if (this.response == null) {
				throw new ServerException(HTTPResponseCode.INTERNAL_ERROR);
			}
			
			response.addHeader(HTTPConstants.HTTP_CONNECTION_KEY, getConnectionHeaderValue());
			sendResponse();
			

		} catch (IOException e) {
			generateError(HTTPResponseCode.INTERNAL_ERROR);
		} catch (ServerException e) {
			generateError(e.code);
		} catch (Exception e) {
			e.printStackTrace();
			generateError(HTTPResponseCode.INTERNAL_ERROR);
		} finally {		
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (IOException e) {
					System.out.println("Could not close socket.");
				}
			}
		}
	}
	
	private void sendResponse() throws ServerException, IOException {
	
		io.Utils.writeOutputStream(this.connection.getOutputStream(), this.response.toString());
//		System.out.println(String.format("Sent Response:\n%s", this.response));
		

		if (shouldAttachFile()) {
			writeAttachedFile();
		}
	}


	private void generateError(HTTPResponseCode code) {
		ErrorsHandler errorHandler = (ErrorsHandler) router.route(ErrorsHandler.ROUTING_KEY);
		errorHandler.setCode(code);
		try {
			this.response = errorHandler.handle(request);
			sendResponse();
		} catch (ServerException | IOException e) {
			System.out.println(String.format("Could generate error response for", code));
		}
		
		errorHandler.setCode(null);
		
	}


	private void routeRequest() throws ServerException {
		
		if (request.type == HTTPRequestType.TRACE) {
			handleTrace();
			return;
		}
		
		if (request.type == HTTPRequestType.NOT_SUPPORTED) {
			generateError(HTTPResponseCode.NOT_IMPLEMENTEED);
		}
		
		String path = request.path;
		
		AbstractBaseHandler handler = router.route(path);
		
		if (handler == null) {
			throw new ServerException(HTTPResponseCode.NOT_FOUND);
		}
		
		this.response = handler.handle(request);
	}


	private void handleTrace() {
		response = new HTTPResponse(HTTPResponseCode.OK, getConnectionVersion());
		String responseContent = request.originRequest;
		
		if (request.isChunked()){
			response.addHeader(HTTPConstants.HTTP_TRANSFER_ENCODING, HTTPConstants.HTTP_CHUNKED_KEY);
		} else {
			response.addHeader(HTTPConstants.HTTP_CONTENT_LENGTH_KEY, Integer.toString(responseContent.length()));
		}
		
		response.addHeader(HTTPConstants.HTTP_CONTENT_TYPE_KEY, HTTPConstants.HTTP_CONTENT_MESSAGE_TYPE);
		response.attachFileContent(responseContent.getBytes());

		String connectionString = getConnectionHeaderValue();
		response.addHeader(HTTPConstants.HTTP_CONNECTION_KEY, connectionString);
	}

	private String getConnectionHeaderValue() {
		return  request == null || request.shouldCloseConnection() ? 
				HTTPConstants.HTTP_CONNECTION_CLOSE : HTTPConstants.HTTP_CONNECTION_KEEP_ALIVE;
	}

	private String getConnectionVersion() {
		return this.request != null ? this.request.version : HTTPConstants.HTTP_TYPE_1_0;
	}

	private boolean shouldAttachFile() {
		return response != null && response.shouldAttachFile;
	}
	
	private void writeAttachedFile() throws ServerException, IOException {
		if (request.isChunked()) {
			io.Utils.writeOutputStreamChunked(this.connection.getOutputStream(), response.fileContent);
		}else{
			io.Utils.writeOutputStream(this.connection.getOutputStream(), response.fileContent);
		}
	}
	
}


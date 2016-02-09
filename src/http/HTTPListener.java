package http;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import synchronization.ThreadPoolManager;



public class HTTPListener {
	public static final String REQUEST_HANDLERS_POOL_KEY = "request_handlers_pool_key";
	private final int port;
	private Router router;

	public HTTPListener(int port, Router router) {
		this.port = port;
		this.router = router;
	}

	public void start() throws IOException {


		// Establish the listen socket.
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);
		ThreadPoolManager.getInstance().get(REQUEST_HANDLERS_POOL_KEY).start();

		System.out.println("start listeneing on port: " + port);

		// Process HTTP service requests in an infinite loop.
		while (true) {
			
			// Listen for a TCP connection request.
			Socket connection = socket.accept();

			// Construct an object to process the HTTP request message.
			HTTPRequestHandler requestHandler = new HTTPRequestHandler(connection, router);
			ThreadPoolManager.getInstance().get(REQUEST_HANDLERS_POOL_KEY).execute(requestHandler);
		}
	}
}

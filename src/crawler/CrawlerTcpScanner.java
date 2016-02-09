package crawler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class CrawlerTcpScanner implements Runnable {
	
	public interface IFoundPortCallback {
		void onFoundPort(int port);
	}
	
	public static  final int MAX_PORT = 1000; // should change to 65553.
	private static final int TIMOEOUT = 200;
	
	private final int startPort;
	private final int endPort;
	private final IFoundPortCallback callback;
	private final String domain;
	
	
	public CrawlerTcpScanner(String domain, int startPort, int endPort, IFoundPortCallback callback) {
		this.domain = domain;
		this.startPort = startPort;
		this.endPort = endPort;
		this.callback = callback;
	}
	
	@Override
	public void run()  {
		
		if (!validDomain(domain)) {
			return;
		}
		
		int currentPort = startPort;
		Socket socket = null;
		
		while (currentPort <=  endPort) {
			try {
				
				socket = new Socket();
				socket.connect(new InetSocketAddress(domain, currentPort), TIMOEOUT);
				callback.onFoundPort(currentPort);
				socket.close();
				
			} catch (IOException e) {
//				System.out.println(String.format("could not create socket, message: %s, port: %s", e.getMessage(), currentPort));
			} finally {
				currentPort++;
			}	
		}
			
	}

	private boolean validDomain(String domain) {
		
		 try {
		      InetAddress ipaddress = InetAddress.getByName(domain);
		      System.out.println("IP address: " + ipaddress.getHostAddress());
		 } catch ( UnknownHostException e ) {
		      System.out.println("Could not find IP address for: " + domain);
		      return false;
		 }
		 
		return true;
	}
	
}

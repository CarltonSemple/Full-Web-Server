package server;
import helpers.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.logging.*;

// SSL 
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import dataModel.User;

public class ServerMain {

	public static final Logger logger = Logger.getLogger(
			ServerMain.class.getCanonicalName());
	private static final String INDEX_FILE = "index.html";

	private final File rootDirectory;
	private final int port; // https requires port 443
	
	public static final String userFilePath = "users.ser";
	
	// SSL
	ServerSocketFactory serveSockFact;	

	public ServerMain(File rootDirectory, int port) throws IOException {

		if (!rootDirectory.isDirectory()) {
			throw new IOException(rootDirectory 
					+ " does not exist as a directory"); 
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
		
		this.serveSockFact = SSLServerSocketFactory.getDefault();
		
		// Initialize the user list by attempting to load it if it exists
		User.userList = FileAccess.loadUsers(userFilePath);
		FileAccess.saveUsers("users.ser", User.userList);
	}

	public void start() throws IOException {
		
		ExecutorService pool = Executors.newCachedThreadPool();
		try (ServerSocket server = new ServerSocket(port)) //(SSLServerSocket)serveSockFact.createServerSocket(port)) // 
		{
			logger.info("Accepting connections on port " + server.getLocalPort());
			//logger.info("Document Root: " + rootDirectory);
			
			// Add supported cipher suites
			//server.setEnabledCipherSuites(server.getSupportedCipherSuites());

			while (true) {
				try {
					Socket request = server.accept(); // (SSLSocket)
					//request.startHandshake();	// handshake
					logger.info("accepted client");
					Runnable r = new RequestProcessor(
							rootDirectory, INDEX_FILE, request);
					pool.submit(r);
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Error accepting connection", ex);
				}   
			}
		}
	}
			
	public static void main(String[] args) {

		// get the Document root
		File docroot;
		try {
			docroot = new File(args[0]);
		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println("Usage: java JHTTP docroot port");
			return;
		}

		// set the port to listen on
		int port;
		try {
			port = Integer.parseInt(args[1]);
			if (port < 0 || port > 65535) port = 80;
		} catch (RuntimeException ex) {
			port = 80;
		}  

		try {            
			ServerMain webserver = new ServerMain(docroot, port);
			webserver.start();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Server could not start", ex);
		}
	}
}
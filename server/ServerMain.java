package server;

import helpers.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.Date;

import dataModel.User;


public class ServerMain {
	public static final Logger logger = Logger.getLogger(ServerMain.class
			.getCanonicalName());
	private static final String INDEX_FILE = "index.html";

	private final File rootDirectory;
	private final int port; // https requires port 443

	public static final String userFilePath = "users.ser";
	
	public static int requestCount = 0, startTime = 0, endTime = 0;

	public static java.util.logging.FileHandler fh;
	
	public ServerMain(File rootDirectory, int port) throws IOException {

		if (!rootDirectory.isDirectory()) {
			throw new IOException(rootDirectory
					+ " does not exist as a directory");
		}
		this.rootDirectory = rootDirectory;
		this.port = port;

		// Initialize the user list by attempting to load it if it exists
		User.userList = FileAccess.loadUsers(userFilePath);
	}

	public void start() throws IOException, InterruptedException{
		ExecutorService pool = Executors.newCachedThreadPool();
		Date date = new Date();
		try (ServerSocket server = new ServerSocket(port)) // (SSLServerSocket)serveSockFact.createServerSocket(port)) //
		{
			logger.info("Accepting connections on port "
					+ server.getLocalPort());
			
			while (true) {
				
				try {
					Socket request = server.accept(); // (SSLSocket)
					if (requestCount == 0)
					{
						date = new Date();
						startTime = (int) date.getTime();
					}
					logger.info("accepted client");
					Runnable r = new RequestProcessor(rootDirectory,
							INDEX_FILE, request);
					pool.submit(r);
					date = new Date();
					endTime = (int) date.getTime();
					requestCount++;
					
					// Every 3 seconds, check if there are over 3 requests
					// If so, sleep for 5 seconds for bandwidth throttling
					if (endTime-startTime > 3000)
					{
						if (requestCount > 3)
							Thread.sleep(5000);
						requestCount = 0;
					}
				} catch (IOException ex) {
					logger.log(Level.WARNING, "Error accepting connection", ex);
				}
			}
		}
	}

	public static void main(String[] args) {

		// Send the logging information to a log file	
		try {
			fh = new java.util.logging.FileHandler("servermain.log");
			logger.addHandler(fh);
		} catch (SecurityException ex) {
			logger.log(Level.WARNING, "Could not add logger handler", ex);
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Could not add logger handler", ex);
		}
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
			if (port < 0 || port > 65535)
				port = 80;
		} catch (RuntimeException ex) {
			port = 80;
		}

		try {
			ServerMain webserver = new ServerMain(docroot, port);
			webserver.start();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Server could not start", ex);
		} catch (InterruptedException ie){
			logger.log(Level.SEVERE, "Server could not start", ie);
		}
	}
}
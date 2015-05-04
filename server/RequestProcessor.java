package server;
import helpers.Authenticator;
import helpers.FileAccess;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;

import dataModel.User;

public class RequestProcessor implements Runnable {

	private final static Logger logger = Logger
			.getLogger(RequestProcessor.class.getCanonicalName());

	private File rootDirectory;
	private String indexFileName = "index.html";
	private Socket connection;
	
	public static java.util.logging.FileHandler fh;

	public RequestProcessor(File rootDirectory, String indexFileName,
			Socket connection) {

		if (rootDirectory.isFile()) {
			throw new IllegalArgumentException(
					"rootDirectory must be a directory, not a file");
		}
		try {
			rootDirectory = rootDirectory.getCanonicalFile();
		} catch (IOException ex) {
		}
		this.rootDirectory = rootDirectory;

		if (indexFileName != null)
			this.indexFileName = indexFileName;
		this.connection = connection;
		
		// Send the logging information to a log file
		try{
			fh = new java.util.logging.FileHandler("servermain.log");
			logger.addHandler(fh);
		}catch(SecurityException ex){
			logger.log(Level.WARNING, "Could not add logger handler", ex);
		}catch(IOException ex){
			logger.log(Level.WARNING, "Could not add logger handler", ex);
		}
	}

	@Override
	public void run() {
		// for security checks
		String root = rootDirectory.getPath();
		try {
			OutputStream raw = new BufferedOutputStream(
					connection.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
			Reader in = new InputStreamReader(new BufferedInputStream(
					connection.getInputStream()), "US-ASCII");
			StringBuilder requestLine = new StringBuilder();
			StringBuilder headerFull = new StringBuilder();
			// Read first line of request
			while (true) {
				int c = in.read();
				if (c == '\r' || c == '\n') {
					break;
				}
				requestLine.append((char) c);
				headerFull.append((char) c);
			}

			// Get the rest of the HTTP header
			while (in.ready()) {
				int c = in.read();
				headerFull.append((char) c);
			}

			String get = requestLine.toString();

			logger.info(connection.getRemoteSocketAddress() + " " + get);

			String[] tokens = get.split("\\s+");
			String method = tokens[0];
			String version = "";

			// GET
			if (method.equals("GET")) {
				String fileName = tokens[1];
				if (fileName.endsWith("/"))
					fileName += indexFileName;
				String contentType = URLConnection.getFileNameMap()
						.getContentTypeFor(fileName);
				if (tokens.length > 2) {
					version = tokens[2];
				}

				
				// Get rid of variables from file name
				String partwhole = new String(fileName);
								
				System.out.println("before file: " + fileName);
				
				if(partwhole.contains("?")){
					if(partwhole.length() > 1){
						System.out.println("partswhole > 1");
						fileName = partwhole.toString().substring(0, partwhole.toString().indexOf("?"));
						//System.out.println("parts size: " + parts.length);
						//if(parts.length > 1)
						//	fileName = parts[0];
						System.out.println("file: " + fileName);
					}
				}
				
				
				File theFile = new File(rootDirectory, fileName.substring(1,
						fileName.length()));

				if (theFile.canRead()
				// Don't let clients outside the document root
						&& theFile.getCanonicalPath().startsWith(root)) {
					byte[] theData = Files.readAllBytes(theFile.toPath());
					if (version.startsWith("HTTP/")) { // send a MIME header
						sendHeader(out, "HTTP/1.0 200 OK", contentType,
								theData.length);
					}

					// send the file; it may be an image or other binary data
					// so use the underlying output stream
					// instead of the writer
					raw.write(theData, 0, theData.length);
					raw.flush();
				} else { // can't find the file
					logger.info("file not found");
					String body = new StringBuilder("<HTML>\r\n")
							.append("<HEAD><TITLE>File Not Found</TITLE>\r\n")
							.append("</HEAD>\r\n")
							.append("<BODY>")
							.append("<H1>HTTP Error 404: File Not Found</H1>\r\n")
							.append("</BODY></HTML>\r\n").toString();
					if (version.startsWith("HTTP/")) { // send a MIME header
						sendHeader(out, "HTTP/1.0 404 File Not Found",
								"text/html; charset=utf-8", body.length());
					}
					out.write(body);
					out.flush();
				}
			}else if(method.equals("HEAD")){
				logger.info("head request");
				System.out.println("head request");

				sendHeader(out, "HTTP/1.0 200 OK", "header", 0);
			}else if (method.equals("POST")) {
				logger.info("POST method");
				logger.info(headerFull.toString());

				// Check for which function to execute
				if(requestLine.toString().contains("login")){
					login(headerFull, out);
				}else if(requestLine.toString().contains("createuser")){
					createUser(headerFull, out);
				}

			} else { // method does not equal "GET"
				logger.info("not a GET method");
				System.out.println("not a method");
				String body = new StringBuilder("<HTML>\r\n")
						.append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
						.append("</HEAD>\r\n").append("<BODY>")
						.append("<H1>HTTP Error 501: Not Implemented</H1>\r\n")
						.append("</BODY></HTML>\r\n").toString();
				if (version.startsWith("HTTP/")) { // send a MIME header
					sendHeader(out, "HTTP/1.0 501 Not Implemented",
							"text/html; charset=utf-8", body.length());
				}
				out.write(body);
				out.flush();
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING,
					"Error talking to " + connection.getRemoteSocketAddress(),
					ex);
		} finally {
			try {
				connection.close();
			} catch (IOException ex) {
			}
		}
	}
	
	/**
	 * Create a user without any admin permissions
	 * @param headerFull
	 * @param out
	 * @throws IOException
	 */
	private void createUser(StringBuilder headerFull, Writer out) throws IOException{
		// Parse the HTTP header for just the POST variables
		String[] lines = headerFull.toString().split("\n");
		System.out.println("variables: " + lines[lines.length - 1]);
		String[] variables = lines[lines.length - 1].split("&");

		String username = "";
		String password = "";
		
		// Create a user to check against the database
		System.out.println("here come the variables");
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].contains("username")) {
				username = variables[i].substring(
						variables[i].indexOf("username"),
						variables[i].length());
				String[] te = username.split("=");
				username = te[1];
			} else if (variables[i].contains("password")) {
				password = variables[i].substring(
						variables[i].indexOf("password"),
						variables[i].length());
				String[] te = password.split("=");
				password = te[1];
			}
		}
		
		// Create a new user without admin rights
		User user = new User(username, password, "none");
		System.out.println(user.toString());
		
		User.printUsers();
		if(User.AddUser(user) == 1){
			// Save & Reload user list
			System.out.println("users before saving");
			User.printUsers();
			FileAccess.saveUsers(ServerMain.userFilePath, User.userList);
			User.userList = FileAccess.loadUsers(ServerMain.userFilePath);
			System.out.println("users after saving and loading");
			User.printUsers();
			

			// Send response to the client
			if(Authenticator.verifyCredentials(user) == 2)
				out.write("username=" + user.name() + "&key=" + "admin");
			else if(Authenticator.verifyCredentials(user) == 1)
				out.write("username=" + user.name() + "&key=" + "none");
			else
				out.write("Either the username or password are incorrect. Please try again");
		}else{
			out.write("A user with that name already exists");
		}		
		
		out.flush();
	}
	
	/**
	 * Check to see if a user can log in
	 * @param headerFull
	 * @param out
	 * @throws IOException
	 */
	private void login(StringBuilder headerFull, Writer out) throws IOException{
		// Parse the HTTP header for just the POST variables
		String[] lines = headerFull.toString().split("\n");
		System.out.println("variables: " + lines[lines.length - 1]);
		String[] variables = lines[lines.length - 1].split("&");

		String username = "";
		String password = "";
		
		// Create a user to check against the database
		System.out.println("here come the variables");
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].contains("username")) {
				username = variables[i].substring(
						variables[i].indexOf("username"),
						variables[i].length());
				String[] te = username.split("=");
				username = te[1];
			} else if (variables[i].contains("password")) {
				password = variables[i].substring(
						variables[i].indexOf("password"),
						variables[i].length());
				String[] te = password.split("=");
				password = te[1];
			}
		}
		User user = new User(username, password, "none");
		System.out.println(user.toString());
		
		User.printUsers();
		
		User.getAuthority(user);

		// Send response to the client
		if(Authenticator.verifyCredentials(user) == 2){
			out.write("username=" + user.name() + "&key=" + "admin");
		}
		else if(Authenticator.verifyCredentials(user) == 1){
			out.write("username=" + user.name() + "&key=" + "none");
		}
		else
			out.write("Either the username or password are incorrect. Please try again");
		out.flush();
	}

	private void sendHeader(Writer out, String responseCode,
			String contentType, int length) throws IOException {
		out.write(responseCode + "\r\n");
		//System.out.println(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		//System.out.println("Date: " + now + "\r\n");
		out.write("Server: JHTTP 2.0\r\n");
		//System.out.println("Server: JHTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		//System.out.println("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n\r\n");
		//System.out.println("Content-type: " + contentType + "\r\n\r\n");
		out.flush();
	}
}
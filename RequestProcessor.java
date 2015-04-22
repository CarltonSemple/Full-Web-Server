import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
    
public class RequestProcessor implements Runnable {
  
  private final static Logger logger = Logger.getLogger(
      RequestProcessor.class.getCanonicalName());

  private File rootDirectory;
  private String indexFileName = "index.html";
  private Socket connection;
  
  public RequestProcessor(File rootDirectory, 
      String indexFileName, Socket connection) {
        
    if (rootDirectory.isFile()) {
      throw new IllegalArgumentException(
          "rootDirectory must be a directory, not a file");   
    }
    try {
      rootDirectory = rootDirectory.getCanonicalFile();
    } catch (IOException ex) {
    }
    this.rootDirectory = rootDirectory;

    if (indexFileName != null) this.indexFileName = indexFileName;
    this.connection = connection;
  }
  
  @Override
  public void run() {
    // for security checks
    String root = rootDirectory.getPath();
    try {              
      OutputStream raw = new BufferedOutputStream(
                          connection.getOutputStream()
                         );         
      Writer out = new OutputStreamWriter(raw);
      Reader in = new InputStreamReader(
                   new BufferedInputStream(
                    connection.getInputStream()
                   ),"US-ASCII"
                  );
      StringBuilder requestLine = new StringBuilder();
      StringBuilder headerFull = new StringBuilder();
      // Read first line of request
      while (true) {
        int c = in.read();
        if (c == '\r' || c == '\n') {
        	break;
        }
        requestLine.append((char) c);
        headerFull.append((char)c);
      }
      
      String get = requestLine.toString();
      
      logger.info(connection.getRemoteSocketAddress() + " " + get);
      
      String[] tokens = get.split("\\s+");
      String method = tokens[0];
      String version = "";
      if (method.equals("GET")) {
        String fileName = tokens[1];
        if (fileName.endsWith("/")) fileName += indexFileName;
        String contentType = 
            URLConnection.getFileNameMap().getContentTypeFor(fileName);
        if (tokens.length > 2) {
          version = tokens[2];
        }

        File theFile = new File(rootDirectory, 
            fileName.substring(1, fileName.length()));
        
        if (theFile.canRead() 
            // Don't let clients outside the document root
            && theFile.getCanonicalPath().startsWith(root)) {
          byte[] theData = Files.readAllBytes(theFile.toPath());
          if (version.startsWith("HTTP/")) { // send a MIME header
            sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
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
      } else if(method.equals("POST"))
      {
    	  logger.info("POST method");
          
    	  // Get the rest of the HTTP header
          while(true){
        	  int c = in.read(); 
        	  headerFull.append((char) c);
        	  if(headerFull.toString().contains("end-variables"))
        		  break;
          }
          logger.info(headerFull.toString());
          
          // Parse the HTTP header for just the POST variables
          int startIndex = headerFull.indexOf("start-variables");
          startIndex += "start-variables".length();
          int endIndex = headerFull.indexOf("end-variables");
          
          String varSetString = headerFull.substring(startIndex, endIndex);
          String[] variables = varSetString.split("&");
          
          String username = "";
          String password = "";
          
          System.out.println("here come the variables");
          for(int i = 0; i < variables.length; i++)
          {
        	  if(variables[i].contains("username")){
        		  username = variables[i].substring(variables[i].indexOf("username"), variables[i].length());
        		  String[] te = username.split("=");
        		  username = te[1];
        	  }
        	  else if(variables[i].contains("password")){
        		  password = variables[i].substring(variables[i].indexOf("password"), variables[i].length());
        		  String[] te = password.split("=");
        		  password = te[1];
        	  }
          }
                   
          
          User user = new User(username, password);
          System.out.println("user: " + user.username);
          System.out.println("password: " + user.password);
          
          
      } else { // method does not equal "GET"
    	  logger.info("not a GET method");
        String body = new StringBuilder("<HTML>\r\n")
            .append("<HEAD><TITLE>Not Implemented</TITLE>\r\n")
            .append("</HEAD>\r\n")
            .append("<BODY>")
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
          "Error talking to " + connection.getRemoteSocketAddress(), ex);
    } finally {
      try {
        connection.close();        
      }
      catch (IOException ex) {} 
    }
  }

  private void sendHeader(Writer out, String responseCode,
      String contentType, int length)
      throws IOException {
    out.write(responseCode + "\r\n");
    Date now = new Date();
    out.write("Date: " + now + "\r\n");
    out.write("Server: JHTTP 2.0\r\n");
    out.write("Content-length: " + length + "\r\n");
    out.write("Content-type: " + contentType + "\r\n\r\n");
    out.flush();
  }
}
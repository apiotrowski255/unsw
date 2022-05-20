/*
 *
 * TCPServer from Kurose and Ross
 * Compile: javac TCPServer.java
 * Run: java TCPServer
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {

	public static void main(String[] args)throws Exception {

      if (args.length != 1) {
         System.out.println("Usage: java TCPServer <port>");
         System.exit(-1);
      }

		int port = Integer.parseInt(args[0]);
		
		ServerSocket socket = null; 
      try {
         socket = new ServerSocket(port); 
      } catch (IOException e) {
         System.err.println("Could not start server: " + e);
         System.exit(-1);
      }
      
      System.out.println("TCPServer accepting connections on port " + port);

      while (true){

         Socket connection = null;
         connection = socket.accept();
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         OutputStream out = new BufferedOutputStream(connection.getOutputStream());
         PrintStream pout = new PrintStream(out);

		   String request = in.readLine();
         if (request==null) {
            continue;
         }
         

         String req = request.substring(4, request.length()-9).trim();
         String path = "./" + req;
         if (req.equals("/")) {
            path = "./index.html";
         }
         File f = new File(path);
         try {
            InputStream file = new FileInputStream(f);
            pout.print("HTTP/1.0 200 OK\r\n" +
                                        "Content-Type: " + guessContentType(path) + "\r\n" +
                                        "Date: " + new Date() + "\r\n" +
                                        "Server: FileServer 1.0\r\n\r\n");
            sendFile(file, out); // send raw file 
            log(connection, "200 OK");
         } catch (FileNotFoundException e) {
            errorReport(pout, connection, "404", "Not Found",
                                         "The requested URL was not found on this server.");
         } 
         out.flush();
         connection.close();                                    
         
         
         
	   } // end of while (true)

   } // end of main()
   
   private static void sendFile(InputStream file, OutputStream out) {
      try {
         byte[] buffer = new byte[1000];
         while (file.available()>0) {
            out.write(buffer, 0, file.read(buffer));
         }
      } catch (IOException e) {
         System.err.println(e);
      }
   }
   
   private static void errorReport(PrintStream pout, Socket connection, String code, String title, String msg) {
      pout.print("HTTP/1.0 " + code + " " + title + "\r\n" +
                   "\r\n" +
                   "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" +
                   "<TITLE>" + code + " " + title + "</TITLE>\r\n" +
                   "</HEAD><BODY>\r\n" +
                   "<H1>" + title + "</H1>\r\n" + msg + "<P>\r\n" +
                   "<HR><ADDRESS>FileServer 1.0 at " + 
                   connection.getLocalAddress().getHostName() + 
                   " Port " + connection.getLocalPort() + "</ADDRESS>\r\n" +
                   "</BODY></HTML>\r\n");
   }
   
   private static void log(Socket connection, String msg)
   {
      System.err.println(new Date() + " [" + connection.getInetAddress().getHostAddress() + ":" + connection.getPort() + "] " + msg);
   }
   
   private static String guessContentType(String path) {
      if (path.endsWith(".html") || path.endsWith(".htm")) 
         return "text/html";
      else if (path.endsWith(".gif")) 
         return "image/gif";
      else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
         return "image/jpeg";
      else    
         return "text/plain";   
   }

} // end of class TCPServer


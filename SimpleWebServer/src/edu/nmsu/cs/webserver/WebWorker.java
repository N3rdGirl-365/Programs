package edu.nmsu.cs.webserver;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class WebWorker implements Runnable
{

	private Socket socket;
   private String filePath = "null";
   private String filePathAbsolute = "null";

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
         filePathAbsolute = filePath;
         writeHTTPHeader(os, "text/html");
			writeContent(os, filePathAbsolute);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**   
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is)
	{
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				System.err.println("Request line: (" + line + ")");
            //System.out.println("!!!" + line.substring(0,3));
            if (line.substring(0,3).equals("GET")){
               String directoryDraft = "null";
               filePath = getFileName(line);
               System.out.println("FILENAME: " + getFileName(line));
            }//end if for "GET"
            
				if (line.length() == 0)
					break;
			}
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		return;
	}


// Get the file name from input stream
     private String getFileName(String line){
		String fileName;
		fileName = line.substring(5,line.length()-8); //modify to remove 'GET' and "HTTP/1.1"
      File myFile = new File(fileName);
      String fileNameAbsolute = myFile.getAbsolutePath();
      return fileNameAbsolute;
   }//end method


	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
	{
      try(FileInputStream myInput = new FileInputStream(filePath)){
   		Date d = new Date();
   		DateFormat df = DateFormat.getDateTimeInstance();
   		df.setTimeZone(TimeZone.getTimeZone("GMT"));
   		os.write("HTTP/1.1 200 OK\n".getBytes());
   		os.write("Date: ".getBytes());
   		os.write((df.format(d)).getBytes());
   		os.write("\n".getBytes());
   		os.write("Server: Jon's very own server\n".getBytes());
   		// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   		// os.write("Content-Length: 438\n".getBytes());
   		os.write("Connection: close\n".getBytes());
   		os.write("Content-Type: ".getBytes());
   		os.write(contentType.getBytes());
   		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
      }//end try
      catch (Exception e)
			{
            System.err.println("\nHOUSTAN WE HAVE A PROBLEM! " + e);  
            os.write("HTTP/1.0 404 Not Found\n".getBytes());
            os.write("Connection: close\n".getBytes());
   		   os.write("Content-Type: ".getBytes());
   		   os.write(contentType.getBytes());
            os.write("\n\n".getBytes());//end HTTP header
			}//end catch  
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os, String fileNameAbsolute) throws Exception
	{
      try (FileInputStream myInput = new FileInputStream(fileNameAbsolute);) {
      InputStreamReader myReader = new InputStreamReader(myInput);
		BufferedReader b = new BufferedReader(myReader);
      
      
      
      os.write("<html><head></head><body>\n".getBytes());
		os.write("<h3>Well Howdy do!</h3>\n".getBytes());
      os.write("".getBytes());
      String line;

      while ((line = b.readLine()) != null) {
        int indexOfDate = line.indexOf("<cs371date>");
        int lengthOfDate = "<cs371date>".length();
        int indexOfServer = line.indexOf("<cs371server>");
        int lengthOfServer = "<cs371server>".length();
                  
        Date d = new Date();
   	  DateFormat df = DateFormat.getDateTimeInstance();
        String dateLong = df.format(d);
        String dateShort = dateLong.substring(0, 12);
                  
        if (indexOfDate != -1){
           line = line.substring(0, indexOfDate) + dateShort + line.substring(indexOfDate + lengthOfDate); 
           System.out.println("MODIFIED: " + line);
           //update indeces, since line just changed!
           indexOfServer = line.indexOf("<cs371server>");
           indexOfDate = line.indexOf("<cs371date>");
        }//end if for date
        if (indexOfServer != -1){
           line = line.substring(0, indexOfServer) + "Rachel's Server" + line.substring(indexOfServer + lengthOfServer);
           System.out.println("MODIFIED: " + line);
           //update indeces, since line just changed!
           indexOfServer = line.indexOf("<cs371server>");
           indexOfDate = line.indexOf("<cs371date>");
        }//end if for server

        os.write(line.getBytes());
      }//end while

      os.write("</body></html>\n".getBytes());//close HTML
   }//end try
   
   catch (Exception e){
      System.err.println("\nHOUSTAN WE HAVE A PROBLEM! " + e);
      os.write("<html>".getBytes());
      os.write("<head>".getBytes());
      os.write("</head>".getBytes());
      os.write("<body>".getBytes());
      os.write("<h3>Well partner,</h3>".getBytes());
      os.write("<p>Looks like you got lost...</p>".getBytes());
      os.write("</body>".getBytes());
      os.write("</html>".getBytes());
      //close write
      os.flush();
		socket.close();
    }//end catch  
   
	}//end method

} // end class

package org.placelab.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A simple HTTP response.
 */
public class HTTPResponse {

   public static int RESPONSE_OK = 200;
   public static int RESPONSE_NOT_FOUND = 404;
   public static int RESPONSE_MOVED_TEMPORARILY = 302;
   public static int RESPONSE_SERVER_ERROR = 500;

    public static String RESPONSE_OK_STR = "OK";
    public static String RESPONSE_NOT_FOUND_STR = "Not found";
    public static String RESPONSE_MOVED_TEMPORARILY_STR = "Moved temporarily";
    public static String RESPONSE_SERVER_ERROR_STR = "Server error";

   public int     	responseCode;
   public String    	responseString;
   public Hashtable  	headers=null;
   public String    	contentType;
   public int       	contentLength;
   public byte    	content[];
   
   public HTTPResponse(int responseCode, String contentType, 
                       int contentLength, byte content[]) {
       this(responseCode, null, contentType, content, contentLength);
   }
   public HTTPResponse(int responseCode, String contentType, byte content[]) {
       this(responseCode, null, contentType, content, content.length);
   }
   public HTTPResponse(int responseCode, String responseString, 
                       String contentType, byte content[]) {
       this(responseCode, responseString, contentType, content,content.length);
   }
   
   public HTTPResponse(int responseCode, String responseString, 
                       String contentType, byte content[], int contentLength) {
   	this.responseCode = responseCode;
   	this.responseString = responseString;
   	this.contentType = contentType;
   	this.contentLength = contentLength;
   	this.content = content;
   }

    public void addHeader(String key, String value) {
        if (headers==null) {
            headers = new Hashtable();
        }
        headers.put(key.toLowerCase(), value);
    }
    private boolean hasHeader(String key) {
        return (headers != null && headers.get(key.toLowerCase()) != null);
    }
    private void appendHeaders(StringBuffer sb) {
        if (headers==null) return;
        for (Enumeration keys=headers.keys(); keys.hasMoreElements(); ) {
            String key = (String) keys.nextElement();
            String value = (String) headers.get(key);

            key = key.substring(0,1).toUpperCase() + key.substring(1);
            sb.append(key + ": " + value + "\r\n");
        }
    }
    private void appendIfNoSuchHeader(StringBuffer sb,String key,String value){
        if (hasHeader(key)) return;
        key = key.substring(0,1).toUpperCase() + key.substring(1);
        sb.append(key + ": " + value + "\r\n");
    }
    public byte[] getBytes() throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 " + responseCode + 
                  (responseString != null ? " "+responseString :"") + "\r\n");
        appendHeaders(sb);
        appendIfNoSuchHeader(sb, "Date", new Date().toString());
        appendIfNoSuchHeader(sb, "Server", "PlacelabServletEngine 1.0");
        appendIfNoSuchHeader(sb, "Content-Length", ""+contentLength);
        appendIfNoSuchHeader(sb, "Content-Type", contentType);
        
        // ensure that this sucker isn't cached
        sb.append("Pragma: no-cache\r\n");
        sb.append("Cache-Control: no-cache\r\n");
        sb.append("Expires: " + new Date().toString() + "\r\n");

        // mark the end of headers
        sb.append("\r\n");

        // write the headers to a byte stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(sb.toString().getBytes());

        // write the content to a byte stream
        if (contentLength > 0) {
            bos.write(content,0,contentLength);
        }
        bos.close();
        return bos.toByteArray();
    }
}

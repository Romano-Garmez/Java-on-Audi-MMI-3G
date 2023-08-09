package org.placelab.stumbler.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.placelab.core.PlacelabProperties;
import org.placelab.util.Logger;

/**
 * Used by PlacelabStumblerGUI to send log files to placelab.org
 */
public class HTTPTransport implements IRunnableWithProgress {

	protected static  String urlString = "http://www.placelab.org/data/do-submit.php";
	public URL url;
	public String username;
	public String passwd;
	public String device;
	public String description;
	public String file;
	public String header;
	
	public static class PlacelabOrgFailure extends Exception {
	    public PlacelabOrgFailure(String msg) {
	        super(msg);
	    }
	}

	public HTTPTransport(String file, String header) {
	    this(urlString, file, header);
	}
	
	/**
	 * Constructor.
	 **/
	public HTTPTransport(String urlString, String file, String header) {
		try {
			this.url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		username = PlacelabProperties.get("placelab.uploadLogs_username");
		passwd = PlacelabProperties.get("placelab.uploadLogs_password");
		device = "";
		description = "";
		this.file = file;
		this.header = header;
	}

	/**
	 * Send a file to a URL.
	 **/
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	    try {
		    URL                url; 
		    URLConnection      urlConn; 
		    DataOutputStream   dos; 
		    DataInputStream    dis; 
	
		    monitor.beginTask("Uploading log to placelab.org", 100);
		    
		    // build date string
		    StringBuffer dfsb = new SimpleDateFormat("M/dd/yyyy").format(new java.util.Date(), new StringBuffer(), new FieldPosition(0));
		    String dateStr = dfsb.toString();
	
		    monitor.subTask("Connecting");
		    if(monitor.isCanceled()) throw new InterruptedException();
		    
		    url = new URL(urlString);
		    urlConn = url.openConnection();
		    urlConn.setDoInput(true); 
		    urlConn.setDoOutput(true); 
		    urlConn.setUseCaches(false); 
		    urlConn.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded"); 
		    dos = new DataOutputStream (urlConn.getOutputStream());
		    
		    monitor.worked(10);
		    monitor.subTask("Encoding headers");
		    if(monitor.isCanceled()) throw new InterruptedException();
		    
		    String args = "username=" + URLEncoder.encode(username) + "&" + 
		                  "passwd=" + URLEncoder.encode(passwd) + "&" +
		                  "readDisclaimer=agree&" +
		                  "cvt_to_ns=true&" +
				  "trace_device=" + URLEncoder.encode(device) + "&" +
				  "trace_descr=" + URLEncoder.encode(description) + "&" +
		                  "mailBack=on&" +
		                  "simple_output=true&" +
		                  "trace_date=" + URLEncoder.encode(dateStr) + "&" +
		                  "trace_data=";
	
		    if ( header != null ) {
			args = args + URLEncoder.encode(header);
		    }
	
		    System.out.println("upload args = " + args);
		    dos.writeBytes(args);
	
		    monitor.worked(5);
		    monitor.subTask("Sending log");
		    if(monitor.isCanceled()) throw new InterruptedException();
		    
		    File f = new File(file);
		    long numBytes = f.length();
			FileInputStream is = new FileInputStream(file);
			boolean done = false;
		    byte[] buf = new byte[1024];
			while ( !done ) {
				int cnt = is.read(buf,0,buf.length);
				if (cnt == -1) {
					done = true;
				} else {
				    if(monitor.isCanceled()) throw new InterruptedException();
				    dos.writeBytes(URLEncoder.encode(new String(buf,0,cnt)));
					Logger.println(URLEncoder.encode(new String(buf,0,cnt)), Logger.HIGH);
					monitor.worked((int) (((double)cnt / (double)numBytes) * 80));
				}
			}
			is.close();
		    dos.flush(); 
		    dos.close(); 
		
		    // the server responds by saying 
		    // "SUCCESS" or "FAILURE" 
		
		    monitor.subTask("getting response from placelab.org");
		    if(monitor.isCanceled()) throw new InterruptedException();
		    
		    dis = new DataInputStream(urlConn.getInputStream());
		    StringBuffer sb = new StringBuffer();
		    done = false;
		    while (!done) {
		    	int read = dis.read(buf,0,buf.length);
		    	if (read == -1) {
		    		done = true;
		    	} else {
		    		sb.append(new String(buf,0,read));
		    	}
		    	
		    	
		    }
		    String s = sb.toString();
		    dis.close(); 
		    Logger.println("Got back " + s, Logger.LOW);
		  
		    if (s.equals("SUCCESS")) { 
		    	Logger.println("Whoo!!!", Logger.HIGH);
		    } else { 
		        Logger.println("Post Error!", Logger.HIGH); 
		        throw new InvocationTargetException(new PlacelabOrgFailure(s));
		    } 
		    monitor.worked(5);
		    monitor.done();
	    } catch (InterruptedException ie) {
	        throw new InterruptedException();
	    } catch (Exception e) {
	        throw new InvocationTargetException(e);
	    }
	}

	/***
	 * Is the web server reachable? 
	 * @return true if yes false if no
	 **/
	public boolean isReachable() {
		try {
			url.openConnection().getInputStream().close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Test driver
	 **/
	public static void main(String[] args) {
		String urlString = "http://www.placelab.org/data/do-submit.php";
		if ( args.length > 0 ) urlString = args[0];
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("test"));
			pw.println("line 1");
			pw.println("line 2");
			pw.close();
		} catch (IOException e) { e.printStackTrace(); System.exit(1); }
		HTTPTransport http = new HTTPTransport(urlString, "test", "my test header");
		http.username = "anthony.lamarca@intel.com";
		http.passwd = "insecure";
		try {
            http.run(new IProgressMonitor() {

                public void beginTask(String arg0, int arg1) {
                }

                public void done() {
                }

                public void internalWorked(double arg0) {
                    
                    
                }

                public boolean isCanceled() {
                    
                    return false;
                }

                public void setCanceled(boolean arg0) {
                    
                    
                }

                public void setTaskName(String arg0) {
                    
                    
                }

                public void subTask(String arg0) {
                    
                    
                }

                public void worked(int arg0) {
                    
                    
                }
                
            });
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
	}
}

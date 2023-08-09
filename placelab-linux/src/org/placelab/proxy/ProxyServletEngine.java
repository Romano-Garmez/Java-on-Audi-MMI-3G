package org.placelab.proxy;

import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.placelab.core.ShutdownListener;

import at.jps.proxy.CfgFileReader;
import at.jps.proxy.ProxyServer;

/**
 * 
 * Register Servlets with this class to have them intercept requests through the web proxy. By default the server is started on port 2080.
 */
public class ProxyServletEngine {
	public static String SERVLET_PREFIX = "http://placelab.api";
	private Hashtable servlets;
	private ProxyServer proxy = null;
	private static ProxyServletEngine theEngine = null;
	public Vector lastRefs;
	public int refBufferLimit = 100;
	private ShutdownListener shutdownListener = null;
	
	public long requests = 0;
	public String startTime;

	private ProxyServletEngine() {
		servlets = new Hashtable();
		lastRefs = new Vector();
		startTime = new Date().toString();
	}
	
	public static ProxyServletEngine getServletEngine() {
		if (theEngine == null) {
			theEngine = new ProxyServletEngine();
		}
		return theEngine;
	}
	
	public static Hashtable addHeaders(HTTPRequest req) {
		ProxyServletEngine e = getServletEngine();
		Hashtable t = new Hashtable();
		Enumeration i = e.servlets.keys();
		while(i.hasMoreElements()) {
			Servlet s = (Servlet)i.nextElement();
			Hashtable tPrime = s.injectHeaders(req);
			if(tPrime != null) {
				t.putAll(tPrime);
			}
		}
		return t;
	}
	
	public static HTTPResponse serviceRequest(HTTPRequest req) {
		// see if any servlets are interested in servicing this request
		ProxyServletEngine e = getServletEngine();
		String urlStr = req.url.toString();
		e.lastRefs.insertElementAt(urlStr,0);
		e.requests++;
		if (e.lastRefs.size() > e.refBufferLimit) {
			e.lastRefs.removeElementAt(e.lastRefs.size()-1);
		}
		for (Enumeration en = e.servlets.keys(); en.hasMoreElements();) {
			Servlet s = (Servlet)en.nextElement();
			String pattern = (String)e.servlets.get(s);
			
                        /* only accept if the url starts with this pattern */
			if (urlStr.indexOf(pattern) == 0) {
				try {
					HTTPResponse res = s.serviceRequest(req);
					if (res != null) {
//                                            getConfig().logAccess(0, "Servlet",
//                                                                  urlStr);
                                            return res;
					}
				} catch (Exception ex) {;} // if the servlet is messed up, ignore it
			}
		}
		return null;
	}
	
	public static void removeServlet(Servlet s) {
		getServletEngine().servlets.remove(s);
	}
	
	/**
	 * Add a new servlet to the system. This servlet will be given all requests that start with the string urlPattern
	 */
	public static boolean addServlet(String urlPattern, Servlet servlet) {
		ProxyServletEngine e = getServletEngine();
		if (e.servlets.get(servlet.getName()) != null) {
			return false;
		}

                if (urlPattern.charAt(0)=='/') {
                    /* append the SERVLET_PREFIX */
                    urlPattern = ProxyServletEngine.SERVLET_PREFIX +urlPattern;
                }
		e.servlets.put(servlet,urlPattern);
		return true;
	}
	
	/**
	 * Starts up the web proxy and the servlet engine
	 */
	public static void startProxy(boolean useDaemonThread)  {
		ProxyServletEngine e = getServletEngine();
		if (e.proxy == null) {
			// lets try to shut down any old servers on this port
			CfgFileReader cfg = new CfgFileReader();
			String proxyPortStr = 
				System.getProperty("http.proxyPort");
			String proxyHost = 
				System.getProperty("http.proxyHost");
			if (proxyHost != null && proxyPortStr != null) {
				int proxyPort=0;
				try {
					proxyPort = 
						Integer.parseInt(proxyPortStr);
					cfg.remote_port = proxyPort;
					cfg.remote_ip = proxyHost;
				} catch (NumberFormatException ne) {
					System.out.println("Invalid proxy port: "+proxyPortStr);
				}
			}
			e.proxy = new ProxyServer( cfg, useDaemonThread);
			shutdownOtherProxies(e.proxy.config.local_port);
			e.proxy.getConfiguration().log_level = 0;
		}
    	e.proxy.startServer();
	}

	public static void setShutdownListener(ShutdownListener shutdownListener) {
		ProxyServletEngine e = getServletEngine();
		e.shutdownListener = shutdownListener;
	}

	public static void stopProxy() {
		ProxyServletEngine e = getServletEngine();
		if (e.proxy != null) {
			e.proxy.stopServer();
		}
	}
	
	public static void killProxy() {
		ProxyServletEngine e = getServletEngine();
		if (e.proxy != null) {
			e.proxy.stopServer();
			e.proxy.serverExit();
			e.proxy = null;
			if (e.shutdownListener != null) {
				e.shutdownListener.shutdown();
			}
		}
	}
	
	/**
	 * This attempts to shut down any other Place Lab proxies that are running on the same machine
	 */
	private static void shutdownOtherProxies(int port) {
		try {
			URL u = new URL("http",         // protocol,
                  "localhost",            // host name or IP of proxy server to use
                  port,             // proxy port
                  "http://placelab.api/core/shutdown");		
			URLConnection uc = u.openConnection();
			uc.getInputStream().close();
		} catch (Exception ex) {
			; // not a problem, it'll break if an old server ISNT running...
		}
	}	
	
	public static Vector recentAccesses() {
		return getServletEngine().lastRefs;
	}
	
	public static CfgFileReader getConfig() {
		return getServletEngine().proxy.config;
	}
	
}


package org.placelab.proxy;

import java.util.Hashtable;

/**
 * 
 *
 * Implement one of these and register it with the ServletEngine to intercept
 * requests coming in to the proxy
 */
public interface Servlet {
	/**
	 * Servlets can have human readable names. This is just for convenience.
	 */
	public String getName();
	
	/** A serlvet must reply to passed in http requests
	 */
	public HTTPResponse serviceRequest(HTTPRequest req);
	/**
	 * lets a servlet inject headers into a request that's handled by anybody
	 * @param req
	 * @return null if you don't want to inject headers, or a Hashtable of headers/values if you do
	 */
	public Hashtable injectHeaders(HTTPRequest req);
}

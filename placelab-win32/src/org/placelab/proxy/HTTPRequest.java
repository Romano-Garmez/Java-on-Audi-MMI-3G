package org.placelab.proxy;

import java.net.URL;
import java.util.Hashtable;

/**
 * A simplifies HTTPrequest with a URL and a set of HTTP headers
  */

public class HTTPRequest {
	public URL url;
	public Hashtable headers;
	
	public HTTPRequest(URL url, Hashtable headers) {
		this.url = url;
		this.headers = headers;
	}
	
	public String toString() {
		return "<" + url.toString() + ">";
	}
}

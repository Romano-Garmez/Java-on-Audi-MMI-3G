package org.placelab.example;

import java.util.Hashtable;

import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.Measurement;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;
import org.placelab.proxy.ProxyServletEngine;
import org.placelab.proxy.Servlet;
import org.placelab.spotter.LogSpotter;

/**
 * This sample creates a placelab object and installs a servlet
 * in it.
 */
public class ServletExample implements Servlet, EstimateListener {
	private Estimate lastEstimate = null;

	public static void main(String[] args) {
		try {
			ServletExample ss = new ServletExample();
			// Install the samples as a servlet
			ProxyServletEngine.addServlet("/sample", ss);

			PlacelabWithProxy placelab;			
			if (args.length == 0) {
				// Create a default placelab (with a live spotter)
				placelab = new PlacelabWithProxy();
			} else {
				// Create a placelab (with an explicit log spotter)
				placelab = new PlacelabWithProxy(LogSpotter.newSpotter(args[0]),
				                        null, // use default tracker
										null, // use default mapper
				                        2000  // poll spotter every 2s
				                        );
			}
			placelab.addEstimateListener(ss);
			
			placelab.createProxy();
			System.out.println("The proxy and servlet are running. Change your broswer's http proxy to:");
			System.out.println("locahost:2080 and the load the page: http://placelab.api/sample");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
		lastEstimate = e;
	}

	// A servlet has a name	
	public String getName() {
		return "A sample servlet";
	}
	
	// print the last estimate we got from the tracker
	public HTTPResponse serviceRequest(HTTPRequest req) {
		String text;
		if (lastEstimate == null) {
			text = "This is the sample servlet!\nNo estimate yet...";
		} else {
			text = "This is the sample servlet!\nLast estimate: " + 
			       lastEstimate.getCoord();
		}
		
		return new HTTPResponse(
						HTTPResponse.RESPONSE_OK,
						"text/plain", // mime type
						text.length(),
						text.getBytes());
		
	}

	// servlets can add headers to outgoing http requests.
	public Hashtable injectHeaders(HTTPRequest req) {
		Hashtable t = new Hashtable();
		t.put("X-SillyHeader", "banana");
		return t;
	}
}

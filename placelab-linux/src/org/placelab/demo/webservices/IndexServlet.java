/*
 * Created on Jul 7, 2004
 *
 */
package org.placelab.demo.webservices;

import java.util.Hashtable;

import org.placelab.core.TwoDCoordinate;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;

/**
 * 
 *
 */
public class IndexServlet extends WebServlet {
    public IndexServlet(WebService webService) {
        super(webService, "/index", "all-placelab-services");
    }
	
    // print the last estimate we got from the tracker
    public HTTPResponse serviceRequest(HTTPRequest req) {
        TwoDCoordinate position = getEstimatedPosition();
        String url = "http://www.placelab.org/services/all.php?url="+ getDaemonURL();
        return createResponse("An index of all Place Lab web services", url);
    }
    
    // servlets can add headers to outgoing http requests.
    public Hashtable injectHeaders(HTTPRequest req) {
        return null;
    }
}

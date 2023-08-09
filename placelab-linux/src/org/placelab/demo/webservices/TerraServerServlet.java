package org.placelab.demo.webservices;

import java.util.Hashtable;

import org.placelab.core.TwoDCoordinate;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;


public class TerraServerServlet extends WebServlet {
    public TerraServerServlet(WebService webService) {
        super(webService, "/terraserver", "terraserver");
    }
	
    // print the last estimate we got from the tracker
    public HTTPResponse serviceRequest(HTTPRequest req) {
        TwoDCoordinate position = getEstimatedPosition();
        String url = "http://terraserver.microsoft.com/image.aspx?Lon=" +
            position.getLongitude() + "&Lat=" + position.getLatitude() + 
            "&w=2&ref=G|" + position.getLongitude() + "," + 
            position.getLatitude() + "&s=10"; 
        return createResponse("An aerial shot of your current location (via TerraServer)", url);
    }
    
    // servlets can add headers to outgoing http requests.
    public Hashtable injectHeaders(HTTPRequest req) {
        return null;
    }
}

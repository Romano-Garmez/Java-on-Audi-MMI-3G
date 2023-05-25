package org.placelab.demo.webservices;

import java.util.Hashtable;

import org.placelab.core.TwoDCoordinate;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;

public class MapquestServlet extends WebServlet {
    public MapquestServlet(WebService webService) {
        super(webService, "/mapquest", "www.mapquest.com");
    }
	
    // print the last estimate we got from the tracker
    public HTTPResponse serviceRequest(HTTPRequest req) {
        TwoDCoordinate position = getEstimatedPosition();
        String url = "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude=" + position.getLatitude() + "&longitude=" + position.getLongitude() + "&zoom=10";
        return createResponse("Your current location (via MapQuest)", url);
    }
    
    // servlets can add headers to outgoing http requests.
    public Hashtable injectHeaders(HTTPRequest req) {
        return null;
    }
}

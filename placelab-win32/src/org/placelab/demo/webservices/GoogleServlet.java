package org.placelab.demo.webservices;

import java.util.Hashtable;

import org.placelab.core.TwoDCoordinate;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;


public class GoogleServlet extends WebServlet {
    public GoogleServlet(WebService webService) {
        super(webService, "/google", "local.google.com");
    }
	
    // print the last estimate we got from the tracker
    public HTTPResponse serviceRequest(HTTPRequest req) {
        String url = req.url.getPath();
        if (url.indexOf("/google/query")==0) {
            return serviceQueryRequest(req);
        } else {
            return serviceFormRequest(req);
        }
    }

    private HTTPResponse serviceFormRequest(HTTPRequest req) {
        return createResponse("Location-based search (local.google.com)",
                              "http://www.placelab.org/services/google-form.php?url=" + getDaemonURL() + "/google/query");
    }

    private HTTPResponse serviceQueryRequest(HTTPRequest req) {
        TwoDCoordinate position = getEstimatedPosition();
        String queryUrl = req.url.getQuery();
        int index = (queryUrl != null ? queryUrl.indexOf("query=") : -1);
        if (index < 0) {
            return serviceFormRequest(req);
        }
        index += 6; // skip over the "query=" part

        int end = queryUrl.indexOf("&", index);
        if (end < index) end = queryUrl.length();

        String query = queryUrl.substring(index, end);
        String url = "http://local.google.com/local?sc=1&q="+query+"&near="+
            position.getLatitude() + "%2C" + position.getLongitude() + 
            "&btnG=Google+Search";
        return createResponse("Google Search: " + query + " near you (powered by Place Lab)", url);
    }
    
    // servlets can add headers to outgoing http requests.
    public Hashtable injectHeaders(HTTPRequest req) {
        return null;
    }
}

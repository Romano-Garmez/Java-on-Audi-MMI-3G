package org.placelab.demo.webservices;

import java.util.Hashtable;

import org.placelab.core.TwoDCoordinate;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;
import org.placelab.proxy.ProxyServletEngine;
import org.placelab.proxy.Servlet;

/**
 * This sample creates a placelab object and installs a servlet
 * in it.
 */
abstract public class WebServlet implements Servlet {
    private WebService webService;
    private String name;

    public WebServlet(WebService webService, String urlPrefix, String name) {
        this.webService = webService;
        this.name = name;
        ProxyServletEngine.addServlet(urlPrefix, this);
    }

    // A servlet has a name	
    public String getName() {
        return name;
    }

    protected TwoDCoordinate getEstimatedPosition() {
        return webService.getEstimatedPosition();
    }
    protected String getDaemonURL() {
        return "http://localhost:" + 
            ProxyServletEngine.getConfig().getLocalProxyPort();
    }

    // print the last estimate we got from the tracker
    protected HTTPResponse createResponse(String title, String url) {
        String html = "<HTML>\n" +
            "<TITLE>"+title+"</TITLE>\n" +
            "<FRAMESET ROWS=\"40,100%\" BORDER=0>\n" +
            "<FRAME NAME=\"placelab_poweredby\" \n" +
            "    SRC=\"http://www.placelab.org/services/powered-by.html\"/>\n"+
            "<FRAME NAME=\"placelab_main\" SRC=\"" + url + "\"/>\n" +
            "<NOFRAMES>\n" +
            "<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;url=" + url + "\"/>\n"+
            "<BODY>This page is located at <A HREF=\""+url+"\">"+url+"</A>\n"+
            "</BODY>\n"+
            "</NOFRAMES>\n"+
            "</FRAMESET>\n"+
            "</HTML>\n";

        return new HTTPResponse(HTTPResponse.RESPONSE_OK,
                                HTTPResponse.RESPONSE_OK_STR,
                                "text/html", // mime type
                                html.getBytes());
    }
    
    // servlets can add headers to outgoing http requests.
    public Hashtable injectHeaders(HTTPRequest req) {
        return null;
    }
}

package org.placelab.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.placelab.core.Coordinate;
import org.placelab.core.Types;
import org.placelab.test.ConsoleTestResult;
import org.placelab.test.Harness;
import org.placelab.test.Regression;
import org.placelab.util.StringUtil;

/*
 * This main servlet handles much of the functionality for the 
 */

/* hacked by James to allow location data to be set from a page for testing */

public class CoreServlet implements Servlet {
	
	public static String SERVLET_PREFIX = "/core";
	
	protected Coordinate pos;
	protected String cityName;
	protected String stateName;
	protected String countryName;
	protected String zipcode;
	protected String street;

	protected boolean beenPoked;
	
	public void resetDefaults() {
		pos = Types.newCoordinate("47.6636333", "-122.3083683");
		cityName = "Seattle";
		stateName = "Washington";
		countryName = "United States";
		zipcode = "98105";
		street = "45th";
		beenPoked = false;
	}
	
	public CoreServlet() {
		resetDefaults();
	}
	
	public String getName() {
		return "Core Servlet";
	}
	
	public static CoreServlet createAndRegister() {
		CoreServlet cs = new CoreServlet();
		ProxyServletEngine.addServlet(SERVLET_PREFIX,cs);
		return cs;
	}
	
	public Hashtable injectHeaders(HTTPRequest req) {
		Hashtable t = new Hashtable();
		t.put("X-PlaceLab.Location", pos.getLatitudeAsString() + "," + pos.getLongitudeAsString());
		return t;
	}
	
	public HTTPResponse serviceRequest(HTTPRequest req) {
		StringBuffer sb = new StringBuffer();
		String s = req.url.getPath().substring(SERVLET_PREFIX.length());
		String contentType = null;
		if (s.startsWith("/status")) {
			contentType = showStatus(sb);
		} else if (s.startsWith("/shutdown")) {
			ProxyServletEngine.killProxy();
			sb.append("The Place Lab daemon is shutting down, goodbye!");
			contentType = "text/html";
		} else if (s.startsWith("/harness")) {
			contentType = runTests(sb);
		} else if (s.startsWith("/viewlog")) {
			contentType = showProxyLog(sb);
		} else if (s.startsWith("/query")) {
			contentType = serviceQuery(sb,s);
		} else if (s.startsWith("/a2bcc")) {
			contentType = serviceA2bcc(sb,s);
		} else if (s.startsWith("/poke")) {
			contentType = pokeLocation(sb, s);
		} else {
			contentType = genFrontPage(sb);
		}
		String tStr = sb.toString();
		return new HTTPResponse(HTTPResponse.RESPONSE_OK,
                                        HTTPResponse.RESPONSE_OK_STR,
                                        contentType, tStr.getBytes());
	}
	
	public String genFrontPage(StringBuffer sb) {
		sb.append("<table cellpadding=10><tr>\n");
		sb.append("<td colspan=3 align=center><font face=arial size=+2><b>Place Lab Daemon</b></font></td>\n");
		sb.append("<tr></tr>\n");
		sb.append("<td><font face=arial><a href=/core/status>Status</a></font></td>\n");
		sb.append("<td><font face=arial><a href=/core/viewlog>HTTP Log</a></font></td>\n");
		sb.append("<td><font face=arial><a href=/core/query>Query</a></font></td>\n");
		sb.append("<tr></tr>\n");
		sb.append("<td><font face=arial><a href=/core/harness>Run Tests</a></font></td>\n");
		sb.append("<td><font face=arial><a href=/core/shutdown>Shutdown</a></font></td>\n");
		sb.append("<tr></tr>\n");
		sb.append("<td colspan=3><font face=arial><a href=/core/a2bcc>Nearby Websites from a2b.cc</a></font></td>\n");
		sb.append("</tr></table>\n");
		return "text/html";
	}
	
	public String runTests(StringBuffer sb) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(os);
			Harness.clearTests();
			Regression.setupAllKnownTests(new String[0]);
			Harness.runTheTests(new ConsoleTestResult(pw, true,true));
			pw.close();
			os.close();
			sb.append(new String(os.toByteArray()));
			sb.append("<p><font face=arial><a href=/core>Back to top</a></font>");
		} catch (IOException ex) {
			sb.append(ex.getMessage());
		}
		return "text/html";
	}
	
	public String showProxyLog(StringBuffer sb) {
		try {
			sb.append("<p><font face=arial size=+2>Recent HTTP Requests Through the Proxy</a><p>");
			Vector l = ProxyServletEngine.recentAccesses();
			for (Enumeration it = l.elements(); it.hasMoreElements();) {
				sb.append("<font face=arial size=-1>" + it.nextElement() + "</font><br>\n");
			}
			sb.append("<p><font face=arial size=-1><a href=/core>Back to top</a></font>");
		} catch (Exception ex) {
			sb.append(ex.getMessage());
		}
		return "text/html";
	}

	public String showStatus(StringBuffer sb) {
		try {
			sb.append("<p><font face=arial size=+2>Place Lab Daemon Status</a><p>");
	
			sb.append("<font face=arial size>Proxy Port: " + ProxyServletEngine.getConfig().local_port + "</font><br>\n");
			sb.append("<font face=arial size>Proxy Requests served: " + ProxyServletEngine.getServletEngine().requests + "</font><br>\n");
			sb.append("<font face=arial size>Proxy start time: " + ProxyServletEngine.getServletEngine().startTime + "</font><br>\n");

			sb.append("<p><font face=arial size=-1><a href=/core>Back to top</a></font>");
		} catch (Exception ex) {
			sb.append(ex.getMessage());
		}
		return "text/html";
	}

	public String serviceQuery(StringBuffer sb, String suffix) {
		String s = suffix.substring("proxy".length()+1);
		if (s.startsWith("/location")) {
			sb.append("" + pos.getLatitudeAsString() + "\n" + pos.getLongitudeAsString());
			return "text/plain";
		} else if (s.startsWith("/place")) {
			//### Bogus for now
			sb.append("At a computer");
			return "text/plain";
		} else if (s.startsWith("/city")) { 
			sb.append(cityName);
			return "text/plain";
		} else if (s.startsWith("/state")) {
			sb.append(stateName);
			return "text/plain";
		} else if (s.startsWith("/country")) {
			sb.append(countryName);
			return "text/plain";
		} else if (s.startsWith("/zipcode")) {
			sb.append(zipcode);
			return "text/plain";
		} else if (s.startsWith("/street")) {
			sb.append(street);
			return "text/plain";
		} else {
			sb.append("<table cellpadding=10><tr>\n");
			sb.append("<td colspan=3 align=center><font face=arial size=+2><b>Query Place Lab</b></font></td>\n");
			sb.append("<tr></tr>\n");
			sb.append("<td><font face=arial><a href=/core/query/location>Location</a></font></td>\n");
			sb.append("</tr></table>\n");
			sb.append("<p><font face=arial size=-1><a href=/core>Back to top</a></font>");
			return "text/html";
		}
	}

	public String serviceA2bcc(StringBuffer sb, String suffix) {
		try {
			sb.append("<head><title>Nearby Web Sites from A2b.cc</title></head></body>");
			sb.append("<p><font face=arial size=+3>Nearby Web Sites, brought to you by <a href=http://a2b.cc><img border=0 src=\"http://www.a2b.cc/images/A2B-white-184X41.gif\" height=40</a></a></font><p>");
			URL u = new URL("http://www.a2b.cc/setloc/rmtsearch.a2b?username=placelab1&pass=placelab&lat=" +
			pos.getLatitudeAsString()+ "&long=" + pos.getLongitudeAsString() + "&resnum=0");
			URLConnection uc = u.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader( uc.getInputStream()));
			reader.readLine(); // burn the ok line
			sb.append("<font face=arial size=+2>Your location: Latitude = " + pos.getLatitudeAsString().substring(0,pos.getLatitudeAsString().indexOf(".") + 6) 
			     + "  Longitude = " + pos.getLongitudeAsString().substring(0,pos.getLongitudeAsString().indexOf(".") + 6) + "<p></font>"); 
			sb.append("<table cellpadding=5 cellspacing=0 >" 
			+ "<tr><td><font face=arial><b>URL</b></font></td><td align=right><font face=arial><b>Distance</b></font></td></tr>");
			while (true) {
			  String str = reader.readLine();
			  if (str == null) {
			  	break;
			  }
			  String sarr[] = StringUtil.split(str,'|');
			  if (sarr.length > 1) {
				  sb.append("<tr><td><font face=arial><a href=\"" + sarr[0] + "\" target=_>" + sarr[1] + "</a></font>&nbsp;&nbsp;&nbsp;&nbsp;</td><td align=right>" + (Integer.parseInt(sarr[2])/1000) + "k " + "</td></tr>");
			  }
			}

			sb.append("</table><p>&nbsp;<p><font face=arial size=-1><a href=/core>Back to top</a></font>");
		} catch (Exception ex) {
			sb.append(ex.getMessage());
		}
		return "text/html";
	}
	
	
	public void trackerPoke(Coordinate pos) {
		if(beenPoked) return;
		this.pos = pos.createClone();
	}
	
	protected String pokeLocation(StringBuffer sb, String suffix) {
		String s = suffix.substring("poke".length()+1);
		
		// some very (very) basic url encoded value parsing
		if(s.startsWith("?")) {
			beenPoked = true;
			String values = URLDecoder.decode(s.substring("?".length()));
			// between latitude = and & ought to be the latitude double
			// can't use regular expressions since I can't use java 1.4
			int start, end;
			String latStr=null, lonStr=null;
			start = values.indexOf('=', 0);
			end = values.indexOf('&', start);
			latStr = values.substring(start+1, end);
			start = values.indexOf('=', end);
			end = values.indexOf('&', start);
			lonStr = values.substring(start+1, end);
			
			// XXX: (yatin says: got rid of this if statement and added checks for null instead) if(lat <= -1000.0 && lng <= -1000.0)
			if (latStr == null && lonStr == null) {
				this.resetDefaults();
			} else {
				pos = Types.newCoordinate(latStr, lonStr);

				start = values.indexOf('=', end);
				end = values.indexOf('&', start);
				cityName = values.substring(start+1, end);
				start = values.indexOf('=', end);
				end = values.indexOf('&', start);
				stateName = values.substring(start+1, end);
				start = values.indexOf('=', end);
				end = values.indexOf('&', start);
				countryName = values.substring(start+1, end);
				start = values.indexOf('=', end);
				end = values.indexOf('&', start);
				zipcode = values.substring(start+1, end);
				start = values.indexOf('=', end);
				end = values.indexOf('&', start);
				street = values.substring(start+1, end);
				sb.append("Thank you.  No, No, seriously, Thank <b>you</b> for your submission<p>");
			}
		}

		sb.append("<form method=\"get\" action=\"/core/poke\" enctype=\"application/x-www-form-urlencoded\">\n");
		sb.append("Latitude: <input type=\"text\" name=\"latitude\"/ value=\"" + pos.getLatitudeAsString() + "\">\n");
		sb.append("<p>Longitude: <input type=\"text\" name=\"longitude\"/ value=\"" + pos.getLongitudeAsString() + "\">\n");
		sb.append("<p>City: <input type=\"text\" name=\"city\"/ value=\"" + cityName + "\">\n");
		sb.append("<p>State: <input type=\"text\" name=\"state\"/ value=\"" + stateName + "\">\n");
		sb.append("<p>Country: <input type=\"text\" name=\"country\"/ value=\"" + countryName + "\">\n");
		sb.append("<p>Zipcode: <input type=\"text\" name=\"zipcode\"/ value=\"" + zipcode + "\">\n");
		sb.append("<p>Street: <input type=\"text\" name=\"street\"/ value=\"" + street + "\">\n");
		sb.append("<input type=\"submit\" name=\"submit\" value=\"Submit\" />");
		sb.append("<p>if you put in values of < -1000.0 for lat and long, I will resume using the values from the spotter.");
		sb.append("</form>");
		return "text/html";

	}

}

package org.placelab.proxy;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;

import org.placelab.test.TestResult;
import org.placelab.test.Testable;

import at.jps.proxy.ProxyServer;
/**
 * 
 *
 */
public class ServletEngineProxyTest implements Testable {
	
	public int testPort = 6543;
	public String SUCCESS_STR = "Test was successful";

	public String getName() { return "ServletEngineProxyTest";}
	
	public void runTests(TestResult result) {
		ProxyServer ps = null;
		try {
			ps = new ProxyServer();
			ps.config.local_port = testPort;
			ProxyServletEngine.addServlet("http://placelab.test",new TestServlet());
			ps.startServer();
			
			if (!result.skipTestBecauseNoNetwork(2)) {
				testProxyFetch(ps,result);
				test404(ps,result);
			}
			testServletOk(ps,result);
			testServletNotOk(ps,result);
		} catch (Exception ex) {
			result.errorCaught(this,ex);
		} finally {
			if (ps != null) {
				ps.stopServer();
				ps.serverExit();
			}
		}
	}

	private void testProxyFetch(ProxyServer ps,TestResult result) throws Exception {
		
			URL u = new URL("http",         // protocol,
    	              "localhost",            // host name or IP of proxy server to use
        	          testPort,             // proxy port
            	      "http://www.yahoo.com");  // the original URL
		
			URLConnection uc = u.openConnection();
			InputStream is = uc.getInputStream();
			byte[] buf = new byte[1024];
			StringBuffer page = new StringBuffer();
			boolean done = false;
			while (!done) {
			  int read = is.read(buf);
			  if (read == -1) {
			  	done = true;
			  } else {
			  	page.append(new String(buf,0,read));
			  }
			}
			String str = page.toString();
			int idx = str.indexOf("Yahoo!");
			result.assertTrue(this,true,idx != -1,"Ensure that the webpage was properly fetched");	
	
	}
	
	private void test404(ProxyServer ps,TestResult result) throws Exception {
		
		URL u = new URL("http",         // protocol,
                  "localhost",            // host name or IP of proxy server to use
                  testPort,             // proxy port
                  "http://www.yahoo.com/globglumglue");  // a bogus URL
		
		try {
			URLConnection uc = u.openConnection();
			uc.getInputStream();
		} catch (Exception ex) {
			result.assertTrue(this,true,true,"Ensure the 404 failed properly");
			return;
		}
		result.fail(this,"404 was unrecognized");
		
	}
	
	private void testServletOk(ProxyServer ps,TestResult result) throws Exception {
		
		URL u = new URL("http",         // protocol,
                  "localhost",            // host name or IP of proxy server to use
                  testPort,             // proxy port
                  "http://placelab.test/good");  // a bogus URL
		
		URLConnection uc = u.openConnection();
		InputStream is = uc.getInputStream();
		byte[] buf = new byte[1024];
		StringBuffer page = new StringBuffer();
		boolean done = false;
		while (!done) {
		  int read = is.read(buf);
		  if (read == -1) {
		  	done = true;
		  } else {
		  	page.append(new String(buf,0,read));
		  }
		}
		String str = page.toString();
		result.assertTrue(this,SUCCESS_STR,str,"Ensure that the servlet was properly invoked");	
	}
	
	private void testServletNotOk(ProxyServer ps,TestResult result) throws Exception {
		
		URL u = new URL("http",         // protocol,
                  "localhost",            // host name or IP of proxy server to use
                  testPort,             // proxy port
                  "http://placelab.test/bad");  // a bogus URL
		
		try {
			URLConnection uc = u.openConnection();
			uc.getInputStream();
		} catch (Exception ex) {
			result.assertTrue(this,true,true,"Ensure the Servlet 404 failed properly");
			return;
		}
		result.fail(this,"Servlet 404 was unrecognized");
	}
	
	public class TestServlet implements Servlet {
		public String getName() { 
			return "Test Servlet"; 
		}
		
		public HTTPResponse serviceRequest(HTTPRequest req) {
			if (req.url.toString().indexOf("/bad") != -1) {
				// send back a 404
				return new HTTPResponse(HTTPResponse.RESPONSE_NOT_FOUND,
			                         "text/html",
			                         -1,
			                         null);
			} else {
				// send back good content
				return new HTTPResponse(HTTPResponse.RESPONSE_OK,
			                         "text/html",
			                         SUCCESS_STR.getBytes());
			}
		}
		
		public Hashtable injectHeaders(HTTPRequest req) {
			return null;
		}
	}




}

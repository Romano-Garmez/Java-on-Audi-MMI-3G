/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * Copyright (c) 1999 / 2000  Johannes Plachy
 *
 * JProxySrv@jps.at
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * If you modify this file, please send us a copy.
 */


package at.jps.proxy;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;
import org.placelab.proxy.ProxyServletEngine;


/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class ProxyConnection extends Thread
{

   CfgFileReader                 ivCfgFileReader;

   static final String           _CRLF = "\r\n";
   static final String           _CRLF2 = "\r\n\r\n";
   static final String           _LF = "\n";
   static final String           _LF2 = "\n\n";

   static final boolean          debug_mode = false;

   public static final int       _LOG_LEVEL_MINIMAL = 2;
   public static final int       _LOG_LEVEL_NORMAL = 1;
   public static final int       _LOG_LEVEL_MAXIMAL = 0;

   int                           _RECV_TIMEOUT;

   Socket                        sock_c;
   Socket                        sock_s;

   Object                        parent;

   // DebugTracer tracer;

   Hashtable                     MIMETypes;
   HTTPRequestHeader             c_header;
   HTTPResponseHeader            s_header;

   boolean                       using_proxy = false;


   /**
    * Constructor declaration
    *
    *
    * @param p
    *
    * @param s
    *
    * @param cfgFileReader
    *
    *
    */
   ProxyConnection( Object p, Socket s, CfgFileReader cfgFileReader )
   {
      ivCfgFileReader = cfgFileReader;

      _RECV_TIMEOUT = 15000;

      // tracer = new DebugTracer();

      parent = p;
      sock_c = s;

      setPriority( 6 );

      _RECV_TIMEOUT = cfgFileReader.getReceiveTimeout() * 1000;

      String   remoteProxy = cfgFileReader.getRemoteProxyIP();

      if ( remoteProxy != null && ( remoteProxy.length() > 0 ) && getConfiguration().getRemoteProxyPort() >= 0 )
      {
         using_proxy = true;

      }

      Runtime  r;

      try
      {
         sock_c.setSoTimeout( _RECV_TIMEOUT );

         start();

         return;
      }
      catch ( SocketException se )
      {
         System.out.println( se );

         return;
      }
      catch ( OutOfMemoryError _ex )
      {
         r = Runtime.getRuntime();

         System.out.println( "Out of Memory!!\nFree memory is: " + r.freeMemory() );
         System.out.println( "Total memory is: " + r.totalMemory() );
         System.gc();

      }

      try
      {
         Thread.sleep( 5000L );

         return;
      }
      catch ( InterruptedException _ex )
      {
         return;
      }
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   CfgFileReader getConfiguration()
   {
      return ivCfgFileReader;
   }


   /**
    * Method declaration
    *
    *
    * @param s
    *
    *
    * @return
    *
    */
   private boolean closeSock( Socket s )
   {
      try
      {
         s.close();
      }
      catch ( IOException _ex )
      {
         return false;
      }

      return true;
   }

    private void returnError(OutputStream out_c, String error) {
        String errorHtml = "<HTML><TITLE>Error!</TITLE><BODY><H1>Server or proxy error</H1><UL>"+error+"</UL></BODY></HTML>";
        HTTPResponse response = 
            new HTTPResponse(HTTPResponse.RESPONSE_SERVER_ERROR,
                             HTTPResponse.RESPONSE_SERVER_ERROR_STR,
                             "text/html",
                             errorHtml.getBytes());

      	try {
            byte[] buf = response.getBytes();
            out_c.write(buf, 0, buf.length);
            out_c.flush();
      	} catch (IOException ex) {
            return; // Badness happened, not sure what to do...
      	}
    }


   /**
    * Method declaration
    *
    *
    */
   public void run()
   {
      StringBuffer   sb = new StringBuffer();
      boolean        txn_ok = false;
      String         remoteAddress = "";
      String         localAddress = "";
      String         ip_info = "";

      InputStream    in_c = null;
      InputStream    in_s = null;
      OutputStream   out_c = null;
      OutputStream   out_s = null;


      try
      {
         in_c = sock_c.getInputStream();
         out_c = sock_c.getOutputStream();
      }
      catch ( IOException ie )
      {
      	System.out.println("Uh Oh...");
         getConfiguration().logError( "ProxyConnectionx1", ie.toString() );

         closeSock( sock_c );

         return;
      }

      byte     onebyte[] = new byte[ 1 ];
      boolean  EOH = false;

      try
      {
         while ( !EOH )
         {
            if ( in_c.read( onebyte ) <= 0 )
            {
               EOH = true;
            }
            else
            {
               sb.append( new String( onebyte, "8859_1" ) );

               if ( sb.toString().endsWith( "\r\n\r\n" ) || sb.toString().endsWith( "\n\n" ) )
               {
                  EOH = true;
               }
            }

         }

      }
      catch ( IOException ie )
      {
         getConfiguration().logError( "ProxyConnectionx2", ie.toString() );

         closeSock( sock_c );

         return;
      }

      getConfiguration().showClientRequest( sb.toString() );
      
      c_header = new HTTPRequestHeader( sb.toString() );
      
      // Just an example of what a header might be
      //c_header.headerfields.put("X-.Location","47.6544567,-122.3076600");
      //c_header.headerfields.put("X-PlaceLab.Place","Coffee Shop");
      
      /* lamarca added this bad code */
      String tstr = c_header.primeheader;
      HTTPResponse servletRes = null;
      
      // james added this even worse code
      int idx1 = tstr.indexOf(' ');
      int idx2 = tstr.indexOf(' ',idx1+1);
      String urlStr = tstr.substring(idx1+1,idx2);
      if (urlStr.charAt(0) == '/') {
          /* if the proxy was accessed directly as if it were a web server
           * assume we are requesting a proxy servlet URL and attach the 
           * SERVLET_PREFIX */
          urlStr = ProxyServletEngine.SERVLET_PREFIX + urlStr;
      }
      HTTPRequest req;
      try {
      	req = new HTTPRequest(new URL(urlStr),c_header.headerfields);
      	Hashtable sHeaders = ProxyServletEngine.addHeaders(req);
      	if (sHeaders != null) {
            c_header.headerfields.putAll(sHeaders);
        }
      } catch (Exception e) {
      	e.printStackTrace();
      }
      
      try {
      	// re-initialize req with the new headers
      	req = new HTTPRequest(new URL(urlStr),c_header.headerfields);
      	// i actually considered injecting the headers as a side effect of this
      	// but i checked myself -j
      	servletRes = ProxyServletEngine.serviceRequest(req);
      } catch (Exception ex) {
      	ex.printStackTrace();
      }
      if (servletRes != null) {
      	try {
	      	byte[] buf = servletRes.getBytes();
	        out_c.write( buf, 0, buf.length);
	        out_c.flush();
      	} catch (IOException ex) {
      		return; // Badness happened, not sure what to do...
      	}

      // hacked by james. just keep on trucking, even if the servlet handled it
      // that way the servlet can affect the header for an arbitrary page, if it so wishes
      }  else {

		  /* Monkey business start */
		      String   server_adpt = "";
	      String   server_addr = "";
	      String   doc = "/";
	      int      server_port = 80;
	      int      loc = 0;
	
	
	      if ( c_header.URI == null ||!c_header.URI.startsWith( "http://" ) )
	      {
                  returnError(out_c, "Invalid url: "+c_header.URI);
	         closeSock( sock_c );
	
	         return;
	      }
	
	      getConfiguration().logAccess( 0, "Proxy", c_header.getPrimeHeader() );
	      getConfiguration().logAccess( 2, "Proxy", sb.toString() );
	
	      if ( ( loc = c_header.URI.indexOf( "/", 8 ) ) <= 0 )
	      {
	         server_adpt = c_header.URI.substring( 7 );
	      }
	      else
	      {
	         server_adpt = c_header.URI.substring( 7, loc );
	         doc = c_header.URI.substring( loc );
	      }
	
	      if ( ( loc = server_adpt.indexOf( ":" ) ) <= 0 )
	      {
	         server_addr = server_adpt;
	         server_port = 80;
	      }
	      else
	      {
	         server_addr = server_adpt.substring( 0, loc );
	         server_port = Integer.parseInt( server_adpt.substring( loc + 1 ) );
	      }
	
	      byte  resp[] = new byte[ 2048 ];
	      int   content_len = 0;
	      int   total_len = 0;
	      int   len = 0;
	
	      content_len = c_header.getContentLength();
	      EOH = false;
	
	      try
	      {
	         if ( using_proxy )
	         {
	            sock_s = new Socket( getConfiguration().getRemoteProxyIP(), getConfiguration().getRemoteProxyPort() );
	
	            sock_s.setSoTimeout( _RECV_TIMEOUT );
	
	            in_s = sock_s.getInputStream();
	            out_s = sock_s.getOutputStream();
	         }
	         else
	         {
	            sock_s = new Socket( server_addr, server_port );
	
	            sock_s.setSoTimeout( _RECV_TIMEOUT );
	
	            in_s = sock_s.getInputStream();
	            out_s = sock_s.getOutputStream();
	            sb = new StringBuffer();
	
	            sb.append( c_header.Method ).append( " " ).append( doc ).append( " " ).append( c_header.Version ).append( "\r\n" );
	
	            Hashtable   ht = c_header.getHeaderFields();
	
	            for ( Enumeration enu = ht.keys(); enu.hasMoreElements();  )
	            {
	               String   key = ( String )enu.nextElement();
	               String   value = ( String )ht.get( key );
	
	               if ( !key.equalsIgnoreCase( "Proxy-Connection" ) )
	               {
	                  sb.append( key ).append( ": " ).append( value ).append( "\r\n" );
	               }
	
	            }
	
	            sb.append( "\r\n" );
	         }
	
                 byte[] bytes=sb.toString().getBytes();
	         out_s.write(bytes, 0, bytes.length);
	         out_s.flush();
	
	         if ( content_len > 0 )
	         {
	            while ( content_len > total_len )
	            {
	               if ( ( len = in_c.read( resp ) ) <= 0 )
	               {
	                  break;
	               }
	
	               total_len += len;
	
	               if ( getConfiguration().getShowContent() && byteArrayOperator.isPrintable( resp, len ) )
	               {
	                  getConfiguration().showClientRequest( new String( resp, 0, len ) );
	               }
	
	               out_s.write( resp, 0, len );
	               out_s.flush();
	            }
	         }
	
	         sb = new StringBuffer();
	
	         while ( !EOH )
	         {
	            if ( in_s.read( onebyte ) < 0 )
	            {
	               EOH = true;
	            }
	            else
	            {
	               sb.append( new String( onebyte, "8859_1" ) );
	
	               if ( sb.toString().endsWith( "\r\n\r\n" ) || sb.toString().endsWith( "\n\n" ) )
	               {
	                  EOH = true;
	               }
	            }
	
	         }
	
	         getConfiguration().showServerResponse( sb.toString() );
	
                 bytes = sb.toString().getBytes();
	         out_c.write(bytes, 0, bytes.length);
	         out_c.flush();
	
	         s_header = new HTTPResponseHeader( sb.toString() );
	
	         if ( getConfiguration().getTrackIP() )
	         {
	            remoteAddress = sock_s.getInetAddress().getHostAddress();
	            localAddress = sock_s.getLocalAddress().getHostAddress();
	            ip_info = " (" + remoteAddress + "/" + localAddress + ")";
	         }
	
	         getConfiguration().logAccess( 1, "ProxyConnectionx3", s_header.getPrimeHeader() + ip_info );
	         getConfiguration().logAccess( 2, "ProxyConnectionx4", sb.toString() );
	         content_len = 0;
	         total_len = 0;
	         len = 0;
	         content_len = s_header.getContentLength();
	
	         if ( content_len > 0 )
	         {
	            while ( content_len > total_len )
	            {
	               if ( ( len = in_s.read( resp ) ) <= 0 )
	               {
	                  break;
	               }
	
	               total_len += len;
	               out_c.write( resp, 0, len );
	               out_c.flush();
	            }
	
	         }
	         else
	         {
	            while ( ( len = in_s.read( resp ) ) >= 0 )
	            {
	               out_c.write( resp, 0, len );
	               out_c.flush();
	
	            }
	
	         }
	         txn_ok = true;
	      }
	      catch ( SocketException se )
	      {
	
	         getConfiguration().logError( "Error: SocketException, ", c_header.URI );
	         getConfiguration().logError( "ProxyConnectionx6", se.toString() );
	
	         System.out.println( c_header.URI + " " + se );
	      }
	      catch ( IOException ie )
	      {
	         getConfiguration().logError( "Error: IOException, ", c_header.URI );
	         getConfiguration().logError( "ProxyConnectionx7", ie.toString() );
	
	         System.out.println( c_header.URI + " " + ie );
	      }
		 /* End of Monkey business */
	  }
	      


      closeSock( sock_c );

      if ( !txn_ok )
      {
         return;

      }
   }


}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


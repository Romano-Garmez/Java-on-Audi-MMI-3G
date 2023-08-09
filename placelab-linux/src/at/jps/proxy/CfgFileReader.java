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
 * 
 * This file was severly hacked by lamarca
 */

package at.jps.proxy;

import java.util.Enumeration;
import java.util.Vector;


final public class CfgFileReader
{


   private Log                   ivAccessLogFile = null;
   private Log                   ivErrorLogFile = null;
   private Vector                ivListeners = null;
   
   public int receive_timeout = 300;
   public int max_connections = 48;
   public int cgi_timeout = 5;
   public int send_timeout = 30;
   public int log_level = -1;
   public int local_port = 2080;
   public String access_log = null;
   public String error_log = null;
   public String proxy_ip = null;
   public int remote_port = -1;
   public String remote_ip = null;
   public boolean track_ip = true;
   public boolean showcontent = true;

   public CfgFileReader()
   {
      ivListeners = new Vector();
   }


   int getReceiveTimeout()
   {
      return receive_timeout;
   }


   int getMaxConnections()
   {
      return max_connections;
   }

   int getCGITimeout()
   {
      return cgi_timeout;
   }

   int getSendTimeout()
   {
      return send_timeout;
   }

   int getlogLevel()
   {
      return log_level;
   }

   public int getLocalProxyPort()
   {
      return local_port;
   }


   String getAccessLogfile()
   {
      return access_log;
   }


   String getErrorLogfile()
   {
      return error_log;
   }

   String getRemoteProxyIP()
   {
      return remote_ip;
   }

   int getRemoteProxyPort()
   {
      return remote_port;
   }

   String getIP()
   {
      return proxy_ip;
   }


   boolean getTrackIP()
   {
   	
      return track_ip;
   }


   boolean getShowContent()
   {
      return showcontent;
   }


   public void showServerResponse( String s )
   {
      for ( Enumeration e = cloneListeners().elements(); e.hasMoreElements();  )
      {
         ( ( ServerInterface )e.nextElement() ).showServerResponse( s );
      }

   }


   public void showClientRequest( String s )
   {
      for ( Enumeration e = cloneListeners().elements(); e.hasMoreElements();  )
      {
         ( ( ServerInterface )e.nextElement() ).showClientRequest( s );
      }

   }


   public void updateHTTPCounter() {}


   public synchronized void logAccess( int level, String source, String message )
   {
      if ( getlogLevel() >= level )
      {
         if ( ivAccessLogFile == null )
         {
            ivAccessLogFile = new Log( getAccessLogfile() );
         }

      ivAccessLogFile.addMessage( source, message );
      }

      for ( Enumeration e = cloneListeners().elements(); e.hasMoreElements();  )
      {
         ( ( ServerInterface )e.nextElement() ).showAccessLog( source + " " + message );
      }

   }

   public void logError( String source, String message )
   {
      if ( ivErrorLogFile == null )
      {
         ivErrorLogFile = new Log( getErrorLogfile() );

      ivErrorLogFile.addMessage( source, message );
      }

      for ( Enumeration e = cloneListeners().elements(); e.hasMoreElements();  )
      {
         ( ( ServerInterface )e.nextElement() ).showErrorLog( source + " " + message );
      }


   }


   public boolean re_initialization()
   {
      return true;
   }


   public synchronized void addListener( ServerInterface listener )
   {
      ivListeners.addElement( listener );
   }

   private synchronized Vector cloneListeners()
   {
      return ( Vector )ivListeners.clone();
   }

}




/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


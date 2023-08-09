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
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class ProxyServer implements Runnable
{
   String                  server_ip;
   int                     server_port;
   int                     max_connections;
   private Thread          myThread;
   private ServerSocket    ss;
   private boolean         server_running;
   private boolean         server_exit;

   static final String     localhost = "127.0.0.1";
   static final boolean    debug_mode = false;


   public CfgFileReader   config;

   /**
    * Constructor declaration
    *
    *
    * @param cfg
    *
    *
    */
   public ProxyServer() throws Exception {
   	this(new CfgFileReader(),true);
   }
   
   public ProxyServer( CfgFileReader cfg, boolean daemonThraed )
   {
      config = cfg;
      max_connections = 8;
      server_running = false;
      server_exit = false;

      server_ip = cfg.getIP();

      myThread = new Thread( this );

      if ( daemonThraed )
      {
         myThread.setDaemon( true );
      }

      myThread.start();
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public CfgFileReader getConfiguration()
   {
      return config;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public boolean isServerRunning()
   {
      return server_running;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public synchronized boolean startServer()
   {
      server_port = getConfiguration().getLocalProxyPort();
      if ( server_running )
      {
         return true;
      }

      try
      {
         if ( server_ip == null || server_ip.equals( "" ) || server_ip.equals( "*" ) )
         {
            ss = new ServerSocket( server_port, max_connections );
         }
         else
         {
            ss = new ServerSocket( server_port, max_connections, InetAddress.getByName( server_ip ) );
         }

         ss.setSoTimeout( 1000 );
      }
      catch ( IOException e )
      {
         ss = null;

         return false;
      }

	  if (getConfiguration().log_level >= 0) {
	      System.out.println( "*** HTTP Proxy running on port " + server_port );
	  }

      server_running = true;

      return true;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public synchronized boolean stopServer()
   {
      server_running = false;

      try
      {
      	if (ss==null) {
      		return true;
      	}
      	//XXX ANTHONY HACK
         ss.close();
      }
      catch ( IOException _ex ) {}

      ss = null;

      return true;
   }


   /**
    * Method declaration
    *
    *
    */
   public void serverExit()
   {
      server_exit = true;
   }


   /**
    * Method declaration
    *
    *
    */
   public void run()
   {
      while ( !server_exit )
      {
         if ( server_running )
         {
            try
            {
               java.net.Socket   s = ss.accept();

               new ProxyConnection( this, s, getConfiguration() );

               getConfiguration().updateHTTPCounter();

            }
            catch ( IOException _ex ) {}
         }
         else
         {
            try
            {
               Thread.sleep( 500L );
            }
            catch ( InterruptedException _ex ) {}

         }
      }
   }

}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


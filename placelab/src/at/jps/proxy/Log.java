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


import java.io.*;
import java.util.*;

/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class Log
{

   /**
    * Constructor declaration
    *
    *
    * @param filename
    *
    *
    */
   public Log( String filename )
   {
   	  if (filename == null) { // assume stdout
   	  	fos = null;
   	  	logopened = true;
   	  	return;
   	  }
      logopened = false;
      newline[ 0 ] = 13;   // '\r'
      newline[ 1 ] = 10;   // '\n'
      log = new File( filename );

      try
      {
         fos = new FileOutputStream( log );
      }
      catch ( IOException e )
      {
         logopened = false;

         System.out.println( "Error: Can't open " + filename + " for writing" );
         System.out.println( e.toString() );
      }

      logopened = true;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public boolean isLogopened()
   {
      return logopened;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public boolean rotateLog()
   {
      return true;
   }


   /**
    * Method declaration
    *
    *
    * @param message
    *
    *
    * @return
    *
    */
   public boolean addMessage( String message )
   {
      return addMessage( "N/A", message );
   }


   /**
    * Method declaration
    *
    *
    * @param source
    *
    * @param message
    *
    *
    * @return
    *
    */
   public boolean addMessage( String source, String message )
   {
      if ( !logopened )
      {
         return false;

      }

      Calendar       calendar = new GregorianCalendar( TimeZone.getDefault() );
      StringBuffer   sb_now = new StringBuffer();

      sb_now.append( calendar.get( 2 ) ).append( "/" );
      sb_now.append( calendar.get( 5 ) ).append( "/" );
      sb_now.append( calendar.get( 1 ) ).append( " " );
      sb_now.append( calendar.get( 11 ) ).append( ":" );
      sb_now.append( calendar.get( 12 ) ).append( ":" );
      sb_now.append( calendar.get( 13 ) ).append( " " );

      String   now = sb_now.toString();
		
		
      try
      {
		  if (fos == null) {
	         System.out.write( now.getBytes());
	         System.out.write("  ".getBytes());
	         System.out.write( source.getBytes());
	         System.out.write("  ".getBytes());
	         System.out.write( message.getBytes());
	         System.out.println("");
		  	return true;
		  }
         fos.write( now.getBytes() );
         fos.write( 9 );
         fos.write( source.getBytes() );
         fos.write( 9 );
         fos.write( message.getBytes() );
         fos.write( newline );
         fos.flush();
      }
      catch ( IOException _ex )
      {
         return false;
      }

      return true;
   }




   private File               log;
   private boolean            logopened;
   private FileOutputStream   fos;
   private static byte        newline[] = new byte[ 2 ];

}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


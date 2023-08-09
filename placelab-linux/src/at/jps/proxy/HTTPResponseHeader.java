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

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class HTTPResponseHeader
{

   /**
    * Constructor declaration
    *
    *
    */
   public HTTPResponseHeader()
   {
      Version = "";
      ResponseCode = "";
      Reason = "";
      primeheader = "";
      headerfields = new Hashtable();
      MalFormedHeader = false;
      content_type = "";
   }


   /**
    * Constructor declaration
    *
    *
    * @param in
    *
    *
    */
   public HTTPResponseHeader( String in )
   {
      Version = "";
      ResponseCode = "";
      Reason = "";
      primeheader = "";
      headerfields = new Hashtable();
      MalFormedHeader = false;
      content_type = "";

      if ( !parseHeader( in ) )
      {
         MalFormedHeader = true;
      }

      header_content = in;
   }


   /**
    * Constructor declaration
    *
    *
    * @param in
    *
    *
    */
   public HTTPResponseHeader( byte in[] )
   {
      this( new String( in ) );
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String toString()
   {
      return header_content;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String getPrimeHeader()
   {
      return primeheader;
   }


   /**
    * Method declaration
    *
    *
    * @param name
    *
    *
    * @return
    *
    */
   public String getHeader( String name )
   {
      return ( String )headerfields.get( name );
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String getResponseCode()
   {
      return ResponseCode;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String getReason()
   {
      return Reason;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String getVersion()
   {
      return Version;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public int getContentLength()
   {
      return content_length;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public String getContentType()
   {
      return content_type;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public Hashtable getHeaderFields()
   {
      return headerfields;
   }


   /**
    * Method declaration
    *
    *
    * @param input
    *
    *
    * @return
    *
    */
   public boolean parseHeader( String input )
   {
      if ( input == null || input.equals( "" ) )
      {
         return MalFormedHeader = true;
      }

      MalFormedHeader = false;

      String   delimiter;

      if ( input.endsWith( "\r\n" ) )
      {
         delimiter = "\r\n";
      }
      else
      {
         delimiter = "\n";
      }

      int   pos;

      if ( ( pos = input.indexOf( delimiter ) ) < 0 )
      {
         MalFormedHeader = true;

         return false;
      }

      primeheader = input.substring( 0, pos );

      StringTokenizer   st = new StringTokenizer( primeheader, " " );

      for ( int i = 0; st.hasMoreTokens(); i++ )
      {
         switch ( i )
         {

         case 0:     // '\0'
            Version = st.nextToken();

            break;

         case 1:     // '\001'
            ResponseCode = st.nextToken();

            break;

         case 2:     // '\002'

         default:
            Reason = Reason + st.nextToken() + " ";

            break;

         }
      }

      if ( Version == null || ResponseCode == null || Reason == null )
      {
         MalFormedHeader = true;

         return false;
      }

      String   name;
      String   value;

      for ( st = new StringTokenizer( input.substring( pos ), delimiter ); st.hasMoreTokens(); headerfields.put( name, value ) )
      {
         String   token = st.nextToken();

         if ( ( pos = token.indexOf( ": " ) ) < 0 )
         {
            return false;
         }

         name = token.substring( 0, pos );
         value = token.substring( pos + 2 );

         if ( name.equalsIgnoreCase( "Content-Length" ) )
         {
            try
            {
               value.replace( '\r', ' ' );

               value = value.trim();
               content_length = Integer.parseInt( value );
            }
            catch ( NumberFormatException _ex )
            {
               System.out.println( "Error: Bad Content-Length. [" + value + "]" );
            }
         }

         if ( name.equalsIgnoreCase( "Content-Type" ) )
         {
            value.replace( '\r', ' ' );

            value = value.trim();
            content_type = value;
         }
      }

      return true;
   }


   /**
    * Method declaration
    *
    *
    * @return
    *
    */
   public boolean isMalFormedHeader()
   {
      return MalFormedHeader;
   }


   public String     Version;
   public String     ResponseCode;
   public String     Reason;
   public String     primeheader;
   public Hashtable  headerfields;
   private boolean   MalFormedHeader;
   private int       content_length;
   private String    content_type;
   private String    header_content;
}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


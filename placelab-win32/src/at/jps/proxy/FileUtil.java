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

/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class FileUtil
{

   /**
    * Constructor declaration
    *
    *
    */
   public FileUtil() {}


   /**
    * Method declaration
    *
    *
    * @param filename
    *
    *
    * @return
    *
    */
   public static String readFile( String filename )
   {
      StringBuffer   content = new StringBuffer();

      try
      {
         FileInputStream   fs = new FileInputStream( filename );
         byte              buf[] = null;

         for ( int len = 0; ( len = fs.available() ) > 0;  )
         {
            buf = new byte[ len ];

            fs.read( buf );
            content.append( new String( buf ) );
         }

         fs.close();
      }
      catch ( IOException _ex ) {}

      return content.toString();
   }


   /**
    * Method declaration
    *
    *
    * @param filename
    *
    * @param array
    *
    *
    * @return
    *
    */
   public static boolean writeFile( String filename, byte array[] )
   {
      try
      {
         FileOutputStream  fs = new FileOutputStream( filename );

         fs.write( array );
         fs.close();
      }
      catch ( IOException _ex )
      {
         return false;
      }

      return true;
   }


}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


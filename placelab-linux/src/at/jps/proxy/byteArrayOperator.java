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

   
/**
 * Class declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public class byteArrayOperator
{

   /**
    * Constructor declaration
    *
    *
    */
   byteArrayOperator() {}


   /**
    * Method declaration
    *
    *
    * @param target
    *
    * @param last_byte
    *
    * @param source
    *
    * @param length
    *
    *
    */
   public static void copy( byte target[], int last_byte, byte source[], int length )
   {
      for ( int i = 0; i < length; i++ )
      {
         target[ last_byte + i ] = source[ i ];
      }

   }


   /**
    * Method declaration
    *
    *
    * @param in
    *
    *
    * @return
    *
    */
   public static boolean isPrintable( byte in[] )
   {
      if ( in == null )
      {
         return false;
      }
      else
      {
         return isPrintable( in, in.length );
      }
   }


   /**
    * Method declaration
    *
    *
    * @param in
    *
    * @param length
    *
    *
    * @return
    *
    */
   public static boolean isPrintable( byte in[], int length )
   {
      if ( in == null || length <= 0 )
      {
         return false;
      }

      int   pcount = 0;
      int   npcount = 0;
      int   dif = 0;

      if ( length < 10 )
      {
         for ( int i = 0; i < length; i++ )
         {
            if ( in[ i ] > 31 && in[ i ] < 127 )
            {
               pcount++;
            }
            else
            {
               npcount++;
            }
         }

      }
      else
      {
         dif = length / 10;

         for ( int i = 0; i < length; i += dif )
         {
            if ( in[ i ] > 31 && in[ i ] < 127 )
            {
               pcount++;
            }
            else
            {
               npcount++;
            }
         }

      }

      return npcount == 0 || pcount / npcount >= 3;
   }


}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


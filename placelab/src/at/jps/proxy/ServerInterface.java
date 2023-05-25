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
 * Interface declaration
 *
 *
 * 
 * @version %I%, %G%
 */
public interface ServerInterface
{

   /**
    * Method declaration
    *
    *
    * @param s
    *
    *
    */
   public void showServerResponse( String s );

   /**
    * Method declaration
    *
    *
    * @param s
    *
    *
    */
   public void showClientRequest( String s );

   /**
    * Method declaration
    *
    *
    * @param s
    *
    *
    */
   public void showAccessLog( String s );

   /**
    * Method declaration
    *
    *
    * @param s
    *
    *
    */
   public void showErrorLog( String s );


   public void showFTPTraffic( String s);

}





/*--- formatting done in "JPS Convention" style on 03-17-2000 ---*/


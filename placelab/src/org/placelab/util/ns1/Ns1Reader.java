// -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*-
// vim:ts=2:sw=2:tw=80:et

// $Id: Ns1Reader.java,v 1.3 2004/06/04 22:00:51 jhoward Exp $
/* 
 * Copyright (c) 2003-2004, Hugh Kennedy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the WiGLE.net nor Mimezine nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.placelab.util.ns1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.DataFormatException;

import org.apache.poi.hpsf.Util;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * decode .ns1 binary files.
 * see http://jakarta.apache.org/poi/hpsf/internals.html
 * for some info on what we're up to.
 * 
 * NS1 is Copyright (c) Marius Milner, April 2003, be nice.
 */
public class Ns1Reader {
  
  /** magic number for ns1 files */
  private static final int NS_MAGIC_NUMBER = 0x5374654e; // "NetS"
  
  /** the highest version we can decode */
  private static final int NS_VERSION_MAX  = 12;
  
  /** MAC length in bytes */
  private static final int MAC_LEN = 6;
  
  /** dBm */
  private static final int MIN_DBM = -149;
  
  /**
   * read an ns1 file.
   *
   * @param in input stream to read from
   * @throws IOException if there is an error reading from in
   * @throws DataFormatException if in dosen't contain an understandable NS1 format
   */
  public static Ns1 readNs1FromStream( InputStream in ) throws IOException, DataFormatException {
    Ns1ErrorState nes = new Ns1ErrorState();
    Ns1 n = readNs1FromStream( in, nes );
    if ( nes.getError() ) {
      throw new DataFormatException( nes.getReason() );
    }
    return n;
  }
  
  /**
   * Say if a file looks like an ns1 by looking at the beginning
   */
  public static boolean isNs1(String file) throws IOException, DataFormatException
  {
  	InputStream in = new FileInputStream(file);
  	byte[] buff = new byte[512];
    long pos = 0;

    // check the magic number
    int magix = readInt(in, buff, "magic number"); 
    if ( magix != NS_MAGIC_NUMBER ) {
      return false;
    }
    return true;
  }

  /**
   * read an ns1 file.
   *
   * if there is a data format error after the ns1 reading has begun, 
   * the existing ns1 will be returned, and err will be set to an error state.
   * errors before the AP reading begins will except.
   *
   * @param in input stream to read from
   * @param err the Ns1ErrorState to note errors to.
   * @throws IOException if there is an error reading from in
   * @throws DataFormatException if in dosen't contain an understandable NS1 format
   */
  public static Ns1 readNs1FromStream( InputStream in, Ns1ErrorState err ) 
    throws IOException, DataFormatException {
    
    byte[] buff = new byte[512];
    long pos = 0;

    // check the magic number
    int magix = readInt(in, buff, "magic number"); 
    if ( magix != NS_MAGIC_NUMBER ) {
      throw new DataFormatException("not an ns1");
    }
    pos += LittleEndianConsts.INT_SIZE;

    int version = readInt(in, buff, "version");
    if ( ( version > NS_VERSION_MAX ) || ( version < 0 ) ) {
      throw new DataFormatException("unsupported file version:"+version);
    }
    pos += LittleEndianConsts.INT_SIZE;

    Ns1 ns1 = new Ns1(version);
    
    int count = readInt(in, buff, "AP count"); 
    pos += LittleEndianConsts.INT_SIZE;

    try {
      while ( count > 0 ) {
        AP ap = new AP();
      
        // read the ssid
        StringInfo ssidInfo = readString(in, buff, "ssid");
        String ssid = ssidInfo.str;
        pos += ssidInfo.bytes_read;
        
        ap.setSsid(ssid);
      
        // read the mac
        int rv = in.read( buff, 0, MAC_LEN );
        if ( rv != MAC_LEN ) {
          throw new DataFormatException("couldn't read MAC (wrong len, wanted "+MAC_LEN+", read"+rv+")");
        }
        pos += MAC_LEN;

        StringBuffer mac = new StringBuffer( MAC_LEN * 3 );
        for ( int i = 0; i < MAC_LEN; i++ ) {
          mac.append(convertDigit((int) (buff[i] >> 4)));
          mac.append(convertDigit((int) (buff[i] & 0x0f)));
          if ( i < ( MAC_LEN - 1 ) ) {
            mac.append(':');
          }
        }
        
        ap.setMac( mac.toString() );
      
        // signal
        int max_signal = readInt(in, buff, "AP max signal");
        pos += LittleEndianConsts.INT_SIZE;

        ap.setMaxSignal( max_signal );
      
        // noise
        int max_noise = readInt(in, buff, "AP max noise");
        pos += LittleEndianConsts.INT_SIZE;

        ap.setMaxNoise( max_noise );
      
        // snr
        int snr = readInt(in, buff, "AP max SNR");
        pos += LittleEndianConsts.INT_SIZE;

        ap.setMaxSnr( snr );
      
      
        if ( version < 6 ) {
          int chan = readInt(in, buff, "AP channel version < 6");
          pos += LittleEndianConsts.INT_SIZE;

          ap.setLastChannel( chan );
          ap.setChannels( chan );
        } else if ( version == 6 ) {
          int chan = readInt(in, buff, "AP channel version 6"); 
          pos += LittleEndianConsts.INT_SIZE;

          ap.setChannels( chan );
        }
        
        int flags = readInt(in, buff, "AP flags");
        pos += LittleEndianConsts.INT_SIZE;

        ap.setFlags( flags );
        
        int interval = readInt(in, buff, "AP beacon interval");
        pos += LittleEndianConsts.INT_SIZE;

        ap.setBeaconInterval( interval );
      
        Date first, last;
        
        if ( version < 5 ) {
          // read in as time_t
          int first_seen = readInt(in, buff, "AP first seen time_t");
          pos += LittleEndianConsts.INT_SIZE;

          first = new Date( first_seen * 1000 );
          
          int last_seen = readInt(in, buff, "AP last seen time_t");
          pos += LittleEndianConsts.INT_SIZE;

          last = new Date( last_seen * 1000 );
        } else {
          // read in as filetime
          int ft_low = readInt(in, buff, "AP first seen filetime low");
          pos += LittleEndianConsts.INT_SIZE;

          int ft_high = readInt(in, buff, "AP first seen filetime high");
          pos += LittleEndianConsts.INT_SIZE;
          
          first = Util.filetimeToDate( ft_high, ft_low );
        
          ft_low = readInt(in, buff, "AP last seen filetime low");
          pos += LittleEndianConsts.INT_SIZE;

          ft_high = readInt(in, buff, "AP last seen filetime high");
          pos += LittleEndianConsts.INT_SIZE;
          
          last = Util.filetimeToDate( ft_high, ft_low );
        }
      
      
        ap.setFirstSeen( first );
        ap.setLastSeen( last );
        
        if ( version >= 2 ) {
          double best_lat = readDouble(in, buff, "AP best lat"); 
          pos += LittleEndianConsts.DOUBLE_SIZE;

          ap.setBestLat( best_lat );
          
          double best_lon = readDouble(in, buff, "AP best long");
          pos += LittleEndianConsts.DOUBLE_SIZE;
          ap.setBestLon( best_lon );
          
          ap.setBestGnr( 0 );
        }
      
      
        if ( version >= 3 ) {
          // do encounter slurping
          int encount = readInt(in, buff, "AP encounter count");
          pos += LittleEndianConsts.INT_SIZE;
        
          int prevsig = 0;
        
          while ( encount > 0 ) {
          
            Encounter enc = new Encounter();
            Date seen;
          
            if ( version < 5 ) {
              // read in as time_t
              int seen_t = readInt(in, buff, "encounter seen time_t");
              pos += LittleEndianConsts.INT_SIZE;
            
              seen = new Date( seen_t * 1000 );
            } else {
              // read in as filetime
              int ft_low = readInt(in, buff, "encounter seen filetime low");
              pos += LittleEndianConsts.INT_SIZE;
              int ft_high = readInt(in, buff, "encounter seen filetime high");
              pos += LittleEndianConsts.INT_SIZE;
            
              seen = Util.filetimeToDate( ft_high, ft_low );
            }
          
            enc.setTime( seen );
          
            // signal
            int signal = readInt(in, buff, "encounter signal");
            pos += LittleEndianConsts.INT_SIZE;
          
            enc.setSignal( signal );
          
            // noise
            int noise = readInt(in, buff, "encounter noise ");
            pos += LittleEndianConsts.INT_SIZE;

            enc.setNoise( noise );
          
            // source
            int source_id = readInt(in, buff, "encounter source id"); 
            pos += LittleEndianConsts.INT_SIZE;

            enc.setSource( Encounter.Source.getSourceFor( source_id ) );
            
            if ( ! Encounter.Source.NONE.equals( enc.getSource() ) ) {
              double lat = readDouble(in, buff, "encounter lat");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setLat( lat );
              
              double lon = readDouble(in, buff, "encounter long");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setLon( lon );
              
              double alt = readDouble(in, buff, "encounter alt");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setAlt( alt );
              
              // sat count
              int satc = readInt(in, buff, "encounter sat count");
              pos += LittleEndianConsts.INT_SIZE;
              enc.setSatCount( satc );
              
              double speed = readDouble(in, buff, "encounter speed");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setSpeed( speed );
              
              double track = readDouble(in, buff, "encounter track");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setTrack( track );
              
              double var = readDouble(in, buff, "encounter variation");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setVariation( var );
              
              double hdop = readDouble(in, buff, "encounter hdop");
              pos += LittleEndianConsts.DOUBLE_SIZE;
              enc.setHdop( hdop );
            }
          
            if ( ( enc.getSignal() == MIN_DBM ) && ( prevsig == MIN_DBM ) ) {
              continue;                       
            }
            
            prevsig = enc.getSignal();
            
            if ( ( enc.getLat() != 0 ) && ( enc.getLon() != 0 ) && 
                 ( enc.getLat() == ap.getBestLat() ) && ( enc.getLon() == ap.getBestLon() ) ) {
              if ( ap.getBestGnr() < ( enc.getSignal() - enc.getNoise() ) ) {
                ap.setBestGnr( enc.getSignal() - enc.getNoise() );
              }
            }
          
            ap.addEncounter( enc );
            encount--;
          }
          
        }
      
        if ( version >= 4 ) {
          // read the name 
          StringInfo nameInfo = readString(in, buff, "name");
          String name = nameInfo.str;
          pos += nameInfo.bytes_read;
          ap.setName( name );
        }
      
        if ( version >= 7 ) {
          // channels
          int mask = readInt(in, buff, "ap low channel mask");
          pos += LittleEndianConsts.INT_SIZE;
          long channels = mask;
          
          mask = readInt(in, buff, "ap high channel mask"); 
          pos += LittleEndianConsts.INT_SIZE;
          
          channels |= (mask << 32);
          
          ap.setChannels( channels ); 
          
          int lastchan = readInt(in, buff, "ap last channel"); 
          pos += LittleEndianConsts.INT_SIZE;
          ap.setLastChannel( lastchan );
        }
        
        if ( version >= 8 ) {
          // ip addr
          int ipaddr = readInt(in, buff, "ap IP");
          pos += LittleEndianConsts.INT_SIZE;
          // throw it away.
        }
        
        if ( version >= 11 ) {
            //  min_sig, max_noise, rate, ip_subnet, ip_mask
            int min_sig = readInt(in, buff, "min sig");
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.
            
            int other_max_noise = readInt(in, buff, "max noise");
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.

            int data_rate = readInt(in, buff, "data rate"); // in 100kbps units
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.
            
            int ip_sub = readInt(in, buff, "ip subnet");
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.

            int ip_mask = readInt(in, buff, "ip mask");
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.            
        }
        
        if ( version >= 12 ) {
            int ap_flags = readInt(in, buff, "ap flags");
            pos += LittleEndianConsts.INT_SIZE;
            // throw it away.            

            // some extra data.
            int info_elem_len = readInt(in, buff, "802.11 info elements len");
            pos += LittleEndianConsts.INT_SIZE;

            // eat the 802.11 information elements:
            for ( long n = info_elem_len; n > 0 ; ) {
                long lrv = in.skip( n );
                if ( lrv > 0 ) {
                    n -= lrv; 
                } else {
                    throw new DataFormatException("couldn't read 802.11 info elements, got "+
                                                  rv+" of "+info_elem_len);
                }
            }
        }
        
        
        ns1.addAP( ap );
        count--;
      }
    } catch ( DataFormatException ex ) {
      err.setError( true );
      err.setReason( ex.toString() );
      err.setOffset( pos );
    }

    return ns1;
  }


  /**
   * read a CArchive short from in.
   * will always consume LittleEndianConsts.SHORT_SIZE bytes.
   *
   * @param in the stream to read from
   * @param buff the buffer to use for reading
   * @param name the name of the field for excepting
   * @return a short
   * @throws IOException for problems with in
   * @throws DataFormatException if an short cannot be read.
   */
  private static short readShort( InputStream in, byte[] buff, String name) 
    throws IOException, DataFormatException {
    int rv = in.read( buff, 0, LittleEndianConsts.SHORT_SIZE );
    if ( rv != LittleEndianConsts.SHORT_SIZE ) {
      throw new DataFormatException("couldn't read "+name+", got "+rv+" of "+LittleEndianConsts.SHORT_SIZE);
    }
    return LittleEndian.getShort( buff, 0 );
  }
  
  /**
   * read a CArchive int from in.
   * will always consume LittleEndianConsts.INT_SIZE bytes.
   *
   * @param in the stream to read from
   * @param buff the buffer to use for reading
   * @param name the name of the field for excepting
   * @return an int
   * @throws IOException for problems with in
   * @throws DataFormatException if an int cannot be read.
   */
  private static int readInt( InputStream in, byte[] buff, String name) 
    throws IOException, DataFormatException {
    int rv = in.read( buff, 0, LittleEndianConsts.INT_SIZE );
    if ( rv != LittleEndianConsts.INT_SIZE ) {
      throw new DataFormatException("couldn't read "+name+", got "+rv+" of "+LittleEndianConsts.INT_SIZE);
    }
    return LittleEndian.getInt( buff, 0 );
  }

  /**
   * read a CArchive double from in.
   * will always consume LittleEndianConsts.DOUBLE_SIZE bytes.
   *
   * @param in the stream to read from
   * @param buff the buffer to use for reading
   * @param name the name of the field for excepting
   * @return a double 
   * @throws IOException for problems with in
   * @throws DataFormatException if a double cannot be read.
   */
  private static double readDouble( InputStream in, byte[] buff, String name)
    throws IOException, DataFormatException {
    int rv = in.read( buff, 0, LittleEndianConsts.DOUBLE_SIZE );
    if ( rv != LittleEndianConsts.DOUBLE_SIZE ) {
      throw new DataFormatException("couldn't read "+name+", got "+rv+" of "+LittleEndianConsts.DOUBLE_SIZE);
    }
    return LittleEndian.getDouble( buff, 0 );
  }


  /**
   * read a CArchive String from in.
   * @param in the stream to read from
   * @param buff the buffer to use for reading
   * @param name the name of the field for excepting
   * @return a StringInfo with the string and count of bytes consumed.
   * @throws IOException for problems with in
   * @throws DataFormatException if a String cannot be read.
   */
  private static StringInfo readString(InputStream in, byte[] buff, String name) 
    throws IOException, DataFormatException {
    int len = in.read();

    String result = null;
    int rv = -1;
    int offset = 0;
    if ( 255 == len ) { // wide string indicator?

      short bom = readShort( in, buff, name );
      String enc = "UTF-16LE";
      if        ( (short)0xfffe == bom ) {
         enc = "UTF-16LE";
      } else if ( (short)0xfeff == bom ) {
         enc = "UTF-16BE";
      } else {
         throw new DataFormatException( "unknown byte order mark:" + bom );
      }
      
      len = in.read(); // length in chars
      len *= 2;        // 2 byte encoding
      offset = 4;      // 4 bytes lost to indicator, byte order and length.
      
      rv = in.read( buff, 0, len );
      if ( rv != len ) {
        throw new DataFormatException("couldn't read "+name+" (wrong len, wanted "+len+", read"+rv+")");
      }

      result = new String(buff, 0, rv, "UTF-16LE");
    
    } else { 
      offset = 1; // 1 byte lost to length
      rv = in.read( buff, 0, len );
      if ( rv != len ) {
        throw new DataFormatException("couldn't read "+name+" (wrong len, wanted "+len+", read"+rv+")");
      }
      
      result = new String(buff, 0, rv, "ISO-8859-1");
    }

    return new StringInfo( result, rv + offset );
  }
  
  /** additional information on strings read in. */
  static class StringInfo {
     /** the string read in */
     final String str;
     /** the number of bytes consumed reading in str. */
     final int bytes_read;
     /**
      * build a StringInfo.
      * @param string the string
      * @param byte_count the byte count
     */
     StringInfo( String string, int byte_count ) {
        str = string;
        bytes_read = byte_count;
     }
  }

  /**
   * From org.apache.soap.encoding.Hex
   * [Private] Convert the specified value (0 .. 15) to the corresponding
   * hexadecimal digit.
   *
   * @param value Value to be converted
   */
  private static char convertDigit(int value) {
    
    value &= 0x0f;
    if (value >= 10)
      return ((char) (value - 10 + 'a'));
    else
      return ((char) (value + '0'));
  }
}

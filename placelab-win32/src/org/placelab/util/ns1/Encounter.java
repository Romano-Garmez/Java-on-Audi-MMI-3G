// -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*-
// vim:ts=2:sw=2:tw=80:et

// $Id: Encounter.java,v 1.1 2004/05/29 01:32:00 jhoward Exp $
/* 
 * Copyright (c) 2003, Hugh Kennedy
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

import java.util.Date;

/**
 * data object for holding single observational data about observed networks.
 * this is the first kind of Encounter.
 * should consider dropping the fields we're not interested in into a subclass.
 */
public class Encounter {
  /** you got a bad altitude, boy. */
  public static final double BAD_ALT = -20000000;
  
  
  private Date time;
  private int signal;
  private int noise;
  private Source source;
  private double lat;
  private double lon;
  private double alt = BAD_ALT;
  private int sat_count;
  private double speed;
  private double track;
  private double variation;
  private double hdop;
        
    
  /**
   * Get the value of time.
   * @return value of time.
   */
  public Date getTime() {return time;}
  
  /**
   * Set the value of time.
   * @param v  Value to assign to time.
   */
  public void setTime(Date  v) {this.time = v;}

    
  /**
   * Get the value of signal.
   * @return value of signal.
   */
  public int getSignal() {return signal;}
  
  /**
   * Set the value of signal.
   * @param v  Value to assign to signal.
   */
  public void setSignal(int  v) {this.signal = v;}
  
  
  /**
   * Get the value of noise.
   * @return value of noise.
   */
  public int getNoise() {return noise;}
  
  /**
   * Set the value of noise.
   * @param v  Value to assign to noise.
   */
  public void setNoise(int  v) {this.noise = v;}
    
    
  /**
   * Get the value of source.
   * @return value of source.
   */
  public Source getSource() {return source;}
  
  /**
   * Set the value of source.
   * @param v  Value to assign to source.
   */
  public void setSource(Source  v) {this.source = v;}
  
    
  /**
   * Get the value of lat.
   * @return value of lat.
   */
  public double getLat() {return lat;}
  
  /**
   * Set the value of lat.
   * @param v  Value to assign to lat.
   */
  public void setLat(double  v) {this.lat = v;}
  

  /**
   * Get the value of lon.
   * @return value of lon.
   */
  public double getLon() {return lon;}
    
  /**
   * Set the value of lon.
   * @param v  Value to assign to lon.
   */
  public void setLon(double  v) {this.lon = v;}
    
    
  /**
   * Get the value of alt.
   * @return value of alt.
   */
  public double getAlt() {return alt;}
    
  /**
   * Set the value of alt.
   * @param v  Value to assign to alt.
   */
  public void setAlt(double  v) {this.alt = v;}
    

  /**
   * Get the value of sat_count.
   * @return value of sat_count.
   */
  public int getSatCount() {return sat_count;}
    
  /**
   * Set the value of sat_count.
   * @param v  Value to assign to sat_count.
   */
  public void setSatCount(int  v) {this.sat_count = v;}
    
    
  /**
   * Get the value of speed.
   * @return value of speed.
   */
  public double getSpeed() {return speed;}
    
  /**
   * Set the value of speed.
   * @param v  Value to assign to speed.
   */
  public void setSpeed(double  v) {this.speed = v;}
    
  
  /**
   * Get the value of track.
   * @return value of track.
   */
  public double getTrack() {return track;}
  
  /**
   * Set the value of track.
   * @param v  Value to assign to track.
   */
  public void setTrack(double  v) {this.track = v;}
  
    
  /**
   * Get the value of variation.
   * @return value of variation.
   */
  public double getVariation() {return variation;}
    
  /**
   * Set the value of variation.
   * @param v  Value to assign to variation.
   */
  public void setVariation(double  v) {this.variation = v;}
    
    
  /**
   * Get the value of hdop.
   * @return value of hdop.
   */
  public double getHdop() {return hdop;}
  
  /**
   * Set the value of hdop.
   * @param v  Value to assign to hdop.
   */
  public void setHdop(double  v) {this.hdop = v;}

    
  static class Source {
    private final int id;
    
    public static final Source NONE = new Source(0);
    public static final Source GPS = new Source( NONE.id + 1 );
    public static final Source INTERPOLATED_GPS = new Source( GPS.id + 1 );
    public static final Source DGPS = new Source( INTERPOLATED_GPS.id + 1 );
    public static final Source INTERPOLATED_DGPS = new Source( DGPS.id + 1 );
    public static final Source MANUAL = new Source( INTERPOLATED_DGPS.id + 1 );
    public static final Source INTERPOLATED_MANUAL = new Source( MANUAL.id + 1 );       
    public static final Source BOGUS = new Source( -1 );
    

    private static Source[] sources = {
      NONE, GPS, INTERPOLATED_GPS, DGPS, INTERPOLATED_DGPS, MANUAL, INTERPOLATED_MANUAL };
    
    private Source(int v) { id = v; }
    
    public static Source getSourceFor(int id) {
      try {
        return sources[id];
      } catch ( ArrayIndexOutOfBoundsException e) {
        return BOGUS;
      }
    }
  }
  
}


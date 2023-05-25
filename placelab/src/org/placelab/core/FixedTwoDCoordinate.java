/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import org.placelab.collections.HashMap;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;

/**
 * The Coordinate class used on systems that do not support
 * floating point math.  If you write an application that
 * only runs on fixed point hardware (some phones) you can cast
 * any Coordinate into a FixedTwoDCoordinate.
 */
public class FixedTwoDCoordinate implements Coordinate {
    private long /*flong*/ lat, lon; 

    /// CONSTRUCTORS ///
    
	/** Create a null FixedTwoDCoordinate */
	public FixedTwoDCoordinate() {
	    this.lat = NULL.lat;
	    this.lon = NULL.lon;
	}
	
	public FixedTwoDCoordinate(String lat, String lon) {
		constructFromStrings(lat, lon);
	}
	public FixedTwoDCoordinate(String latNMEA, String latHem, String lonNMEA, String lonHem) {
		constructFromNMEA(latNMEA, latHem, lonNMEA, lonHem);
	}
	public FixedTwoDCoordinate(FixedTwoDCoordinate c) {
		lat = c.lat;
		lon = c.lon;
	}
	public FixedTwoDCoordinate(long flongLat, long flongLon) {
	    lat=flongLat;
	    lon=flongLon;
	}
	
	public boolean isNull() {
		return (lat == NULL.lat && lon == NULL.lon);
	}
	private static FixedTwoDCoordinate NULL=new FixedTwoDCoordinate("0", "0");
	
	public Coordinate createClone() {
	    return new FixedTwoDCoordinate(this);
	}
	
    public void constructFromMap(HashMap map) {
        constructFromStrings((String)map.get(Types.LATITUDE), (String)map.get(Types.LONGITUDE));
    }
    
	public void constructFromStrings(String lat, String lon) {
		this.lat = fromCoord(lat);
		this.lon = fromCoord(lon);
	}
	public void constructFromNMEA(String latNMEA, String latHem, String lonNMEA, String lonHem) {
		lat = fromNMEA(latNMEA, latHem);
		lon = fromNMEA(lonNMEA, lonHem);
	}	

	public long getLatitudeFlong() {
	    return lat;
	}
	public long getLongitudeFlong() {
	    return lon;
	}
	public String getLatitudeAsString() {
	    if(isNull()) return "NULL";
	    try {
	        return FixedPointLong.flongToString(lat);
	    } catch(FixedPointLongException fple) {
	        return "ERR";
	    }
	        
	}
	public String getLongitudeAsString() {
	    if(isNull()) return "NULL";
	    try {
	        return FixedPointLong.flongToString(lon);
	    } catch(FixedPointLongException fple) {
	        return "ERR";
	    }
	}
	public String toString() {
	    if(isNull()) return "unknown";
	    try {
	        return FixedPointLong.flongToString(lat,6)+", "+FixedPointLong.flongToString(lon,6);
	    } catch(FixedPointLongException fple) {
	        return "Coordinate error: " + fple;
	    }
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof FixedTwoDCoordinate)) {
			return super.equals(o);
		} else {
			FixedTwoDCoordinate c = (FixedTwoDCoordinate) o;
			if(isNull() || c.isNull()) return false;
			return lat == c.lat && lon == c.lon;
		}
	}
	
	
	private long fromNMEA(String numS, String hemisphere) {
	    long number = 0L;
	    try {
	        number = FixedPointLong.stringToFlong(numS) / 100; 
	    } catch (Exception e) {
	        // its common for the lat and lon fields to be empty from nmea when the
	        // device isn't getting good data.  0.0 is fine in this case
	        return 0L;
	    }
	    long left = FixedPointLong.intPart(number);
	    long right = number - left;
	    return (left + ((right * 10) / 6)) * 
	    	(hemisphere.toLowerCase().equals("s") ||
				hemisphere.toLowerCase().equals("w") ? -1 : 1);
	}
	
	private long fromCoord(String num) {
		long val=0L;
		if (Character.isLowerCase(num.charAt(0)) || Character.isUpperCase(num.charAt(0))) {
			char hemisphere = num.charAt(0);
			try {
				val = FixedPointLong.stringToFlong(num);
			} catch(FixedPointLongException ex) {
				return 0L;
			}
			if (hemisphere == 's' || hemisphere == 'S' ||
					hemisphere == 'w' || hemisphere == 'W') {
				val = -val;
			}
		} else {
			try {
				val = FixedPointLong.stringToFlong(num);
			} catch(FixedPointLongException ex) {
			    System.err.println("Error constructing coord: " + ex);
				return 0L;
			}
		}
		return val;
	}

    public String getLatitudeHemisphereNMEA() {
        return lat > 0 ? "N" : "S";
    }

    public String getLongitudeHemisphereNMEA() {
        return lon > 0 ? "E" : "W";
    }
 
    private String toNMEA(long value) {
        long hours = FixedPointLong.intPart(value);
        long minutes = ((value-hours)* 6) / 10;
        long ans = (hours + minutes) * 100;
        try {
            return FixedPointLong.flongToString(ans,3);
        } catch(FixedPointLongException fple) {
            return ""; // evil but can't really do anything inline with data
        }
    }
    
    public String getLatitudeNMEA() {
        return toNMEA(lat);
    }

    public String getLongitudeNMEA() {
        return toNMEA(lon);
    }

    ///////////
    // DISTANCE
    
    /**
	 * Returns the distance between points in meters
	 */
	public long distanceFromFlong(FixedTwoDCoordinate c2) throws FixedPointLongException {
	    if(isNull() || c2.isNull()) throw new FixedPointLongException("distanceFromFlong: null coord!");
	    return CoordinateModel.distance(this,c2);
	}
	
	public int distanceFromInMeters(Coordinate c2) {
	    int l;
	    try {
	        l = FixedPointLong.intValue(distanceFromFlong((FixedTwoDCoordinate)c2));
	    } catch(FixedPointLongException fple) {
	        throw new ArithmeticException("FixedTwoDCoordinate.distanceFromInMeters FixedPointLong error: " + fple);
	    }
	    return l;
	}
	
	private static final int DISTANCEFROM_DPS = 3;
	public String distanceFromAsString(Coordinate c2) {
	    String ret;
	    try {
	        ret = FixedPointLong.flongToString(distanceFromFlong((FixedTwoDCoordinate)c2),DISTANCEFROM_DPS);
	    } catch(FixedPointLongException fple) {
	        ret = "distanceFromAsString: err " + fple;
	        //throw new ArithmeticException("FixedTwoDCoordinate.distanceFromAsString FixedPointLong error: " + fple);
	    }
	    return ret;
	}
	
	////////////
	// WITHIN
	
	public boolean within(Coordinate coord1, Coordinate coord2) {
	    if(isNull() || coord1.isNull() || coord2.isNull()) return false; 
		FixedTwoDCoordinate c1 = (FixedTwoDCoordinate) coord1;
	    FixedTwoDCoordinate c2 = (FixedTwoDCoordinate) coord2;
	    long latMin, latMax, lonMin, lonMax;
	    if (c1.lat < c2.lat) {
	    	latMin = c1.lat;
	    	latMax = c2.lat;
	    } else {
	    	latMin = c2.lat;
	    	latMax = c1.lat;
	    }
	    if (c1.lon < c2.lon) {
	    	lonMin = c1.lon;
	    	lonMax = c2.lon;
	    } else {
	    	lonMin = c2.lon;
	    	lonMax = c1.lon;
	    }
	    return lat >= latMin && lon >= lonMin && lat <= latMax && lon <= lonMax;
	}
    
   
	/**
     * @param northFlong the distance in meters to move north
     * @param eastFlong the distance in meters to move east
     * @return a translated FixedTwoDCoordinate
     */
    public FixedTwoDCoordinate translateFixed(final long northFlong, final long eastFlong) throws FixedPointLongException {
        if(isNull()) throw new FixedPointLongException("Cannot translate a null coordinate");
        return CoordinateModel.translate(this,northFlong,eastFlong);        
    }
    
	/**
     * @param north the distance in meters to move north
     * @param east the distance in meters to move east
     * @return a translated Coordinate
     */
    public Coordinate translate(final int north, final int east) {
        Coordinate ret;
        try {
            ret = translateFixed(FixedPointLong.intToFlong(north),FixedPointLong.intToFlong(east));
        } catch(FixedPointLongException fple) {
            throw new ArithmeticException("FixedTwoDCoordinate.translate: FixedPointLongException " + fple);
        }
        return ret;
        
    }
    
    /** This private class contains the coordinate model, e.g. Spherical Earth */
    private static class CoordinateModel {        
        // Circumference of the earth: 40075160m equatorial, 40008000m polar
        // Dividing by 360 degrees, we get the following:
        private static final long LAT_TO_METERS = 111320L;
        private static final long LON_TO_METERS = 111133L;
        
        private static long distance(FixedTwoDCoordinate c1, FixedTwoDCoordinate c2) throws FixedPointLongException {
            // FLAT EARTH
            //long latDist = (c1.lat-c2.lat) * LAT_TO_METERS;
            //long lonDist = (c1.lon-c2.lon) * LON_TO_METERS;

            // SPHERICAL EARTH, LOCALLY FLAT
            long latDist = LAT_TO_METERS * (c1.lat-c2.lat);
            long lonDist = FixedPointLong.mult(LON_TO_METERS * (c1.lon-c2.lon),FixedPointLong.cos((c1.lat+c2.lat)/2L));
            long ret = FixedPointLong.pythagoras(latDist,lonDist);
            //System.err.println("Distance: latDist " + FixedPointLong.flongToString(latDist) + ", lonDist " + FixedPointLong.flongToString(lonDist) + " pythag " + FixedPointLong.flongToString(ret));
            
            return ret;
        }

    	private static FixedTwoDCoordinate translate(FixedTwoDCoordinate c, long northFlong, long eastFlong) throws FixedPointLongException {
    	    FixedTwoDCoordinate ret = new FixedTwoDCoordinate(c);
	        // FLAT EARTH
	        //ret.lat += northFlong / LAT_METER;
	        //ret.lon += eastFlong / LON_METER;
	
	        // SPHERICAL EARTH, LOCALLY FLAT
	        ret.lat+= northFlong / LAT_TO_METERS;
	        final long ninety = FixedPointLong.intToFlong(90);
	        if(ret.lat > ninety || ret.lat < -ninety) throw new FixedPointLongException("translate: can't handle poles");
	        
	        ret.lon+= FixedPointLong.div(eastFlong,LON_TO_METERS*FixedPointLong.cos((ret.lat+c.lat)/2L));
	        if(ret.lon > ninety*2) ret.lon -= ninety * 4;
	        if(ret.lon < -ninety*2)ret.lon += ninety * 4;
	        return ret;
    	}
    }
}

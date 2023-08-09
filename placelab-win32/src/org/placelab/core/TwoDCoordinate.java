/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import org.placelab.collections.HashMap;
import org.placelab.util.NumUtil;

/**
 * The standard coordinate class used on systems with floating
 * point math support.  If you write an application that only
 * runs on systems that support floats, you can cast any 
 * Coordinate into a TwoDCoordinate.
 */
public class TwoDCoordinate implements Coordinate {
	private static final double PRECISION = 1E-6;
    private double lat, lon;

	/** Create a null TwoDCoordinate */
	public TwoDCoordinate() {
	    this.lat = NULL.lat;
	    this.lon = NULL.lon;
	}
	
	/**
	 * Create a new TwoDCoordinate.
	 */
	public TwoDCoordinate(double lat, double lon) {
	    this.lat = lat;
	    this.lon = lon;
	}
	public TwoDCoordinate(String lat, String lon) {
		constructFromStrings(lat, lon);
	}
	public TwoDCoordinate(String latNMEA, String latHem, String lonNMEA, String lonHem) {
		constructFromNMEA(latNMEA, latHem, lonNMEA, lonHem);
	}
	public TwoDCoordinate(TwoDCoordinate c) {
		lat = c.lat;
		lon = c.lon;
	}
	public void constructFromStrings(String lat, String lon) {
		this.lat = fromString(lat);
		this.lon = fromString(lon);
		if (this.lat == Double.NaN || this.lon == Double.NaN) {
			// if either is NaN, make sure they are both set to NaN
			this.lat = this.lon = Double.NaN;
		}
	}
	public void constructFromMap(HashMap map) {
		constructFromStrings((String)map.get(Types.LATITUDE), (String)map.get(Types.LONGITUDE));
	}
	public void constructFromNMEA(String latNMEA, String latHem, String lonNMEA, String lonHem) {
		lat = fromNMEA(latNMEA, latHem);
		lon = fromNMEA(lonNMEA, lonHem);
	}

	public void moveBy(double xMeters, double yMeters) {
		CoordinateTranslator.T.move(this, xMeters, yMeters);
	}
	
	public void moveTo(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public String getLatitudeAsString() {
	    return ""+getLatitude(); 
	}
	public String getLongitudeAsString() {
	    return ""+getLongitude();
	}
	public String toString() {
		return ""+lat+","+lon;
	}

	public double getLatitude() {
		return lat;
	}

	protected void setLatitude(double lat) {
		this.lat = lat;
	}

	protected void setLongitude(double lon) {
		this.lon = lon;
	}

	public double getLongitude() {
		return lon;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof TwoDCoordinate)) {
			return super.equals(o);
		} else {
			TwoDCoordinate c = (TwoDCoordinate) o;
			return lat == c.lat && lon == c.lon;
		}
	}

	/**
	 * Returns the distance between points in meters
	 */
	public int distanceFromInMeters(Coordinate c2) {
	    return (int)distanceFrom((TwoDCoordinate)c2);
	}
	public String distanceFromAsString(Coordinate c2) {
	    return ""+distanceFrom((TwoDCoordinate)c2);
	}
	public double distanceFrom(TwoDCoordinate c2) {
        return (CoordinateTranslator.T.distance(this, c2));
	}
	public double xDistanceFrom(TwoDCoordinate c2) {
		return CoordinateTranslator.T.xDistance(this, c2);
	}
	public double yDistanceFrom(TwoDCoordinate c2) {
		return CoordinateTranslator.T.yDistance(this, c2);
	}
	/* the following two methods give an estimate of how much the latitude (or longitude) 
	 * would change by for the given number of meters around this coordinate
	 */ 
	public double metersToLatitudeUnits(double meters) {
		return CoordinateTranslator.T.metersToLatitudeUnits(this, meters);
	}
	public double metersToLongitudeUnits(double meters) {
		return CoordinateTranslator.T.metersToLongitudeUnits(this, meters);
	}
	public Coordinate translate(int north, int east) {
	    TwoDCoordinate c = new TwoDCoordinate(this);
	    CoordinateTranslator.T.move(c,north,east);
	    return c;
	}
	public boolean within(Coordinate coord1, Coordinate coord2) {
	    TwoDCoordinate c1 = (TwoDCoordinate) coord1;
	    TwoDCoordinate c2 = (TwoDCoordinate) coord2;
	    double latMin, latMax, lonMin, lonMax;
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
	    return lat >= latMin - PRECISION && lon >= lonMin - PRECISION && lat <= latMax + PRECISION && lon <= lonMax + PRECISION;
	}

	private double fromNMEA(String numS, String hemisphere) {
	    /*double number = Double.parseDouble(numS);
		double whole = Math.floor(number / 100.0);
		
		whole += (number - (whole * 100.0)) / 60.0;
		
		return whole * (hemisphere.equalsIgnoreCase("s") ||
				hemisphere.equalsIgnoreCase("w") ? -1 : 1);*/
	    double number = 0.0;
	    try {
	        number = Double.parseDouble(numS) / 100.0;
	    } catch (NumberFormatException nfe) {
	        // its common for the lat and lon fields to be empty from nmea when the
	        // device isn't getting good data.  0.0 is fine in this case
	        return 0.0;
	    } catch (NullPointerException npe) {
	        // this happens sometimes too...
	        return 0.0;
	    }
	    double left = Math.floor(number);
	    double right = number - left;
	    return (left + (right * (10.0 / 6.0))) * 
	    	(hemisphere.equalsIgnoreCase("s") || 
	    	        hemisphere.equalsIgnoreCase("w") ? -1 : 1);
	}
	private double fromString(String num) {
		double val=Double.NaN;
		if (Character.isLetter(num.charAt(0))) {
			char hemisphere = num.charAt(0);
			if (num.length() < 3) {
				return val; // bad bad bad
			}
			try {
				val = Double.parseDouble(num.substring(2));
			} catch(NumberFormatException ex) {
				return Double.NaN;
			}
			if (hemisphere == 's' || hemisphere == 'S' ||
					hemisphere == 'w' || hemisphere == 'W') {
				val = -val;
			}
		} else {
			try {
				val = Double.parseDouble(num);
			} catch(NumberFormatException ex) {
				return Double.NaN;
			}
		}
		return val;
	}
	
	public boolean isNull() {
		return ((Double.isNaN(lat) && Double.isNaN(lon)) || 
		        ((lat == 0.0) && (lon == 0.0)));
	}
	public static final TwoDCoordinate NULL=new TwoDCoordinate(Double.NaN, Double.NaN);

    public Coordinate createClone() {
        return new TwoDCoordinate(this);
    }
    public TwoDCoordinate createCloneAndMove(double xMeters, double yMeters) {
    	TwoDCoordinate c = new TwoDCoordinate(this);
    	c.moveBy(xMeters, yMeters);
    	return c;
    }

    private String toNMEA(double value) {
        double hours = Math.floor(value);
        double minutes = (value - hours) * (6.0 / 10.0);
        double ans = (hours + minutes) * 100.0;
        return NumUtil.doubleToString(ans, 3);
    }
    
    /* (non-Javadoc)
     * @see org.placelab.core.Coordinate#getLatitudeNMEA()
     */
    public String getLatitudeNMEA() {
        return toNMEA(lat);
    }

    /* (non-Javadoc)
     * @see org.placelab.core.Coordinate#getLongitudeNMEA()
     */
    public String getLongitudeNMEA() {
        return toNMEA(lon);
    }

    /* (non-Javadoc)
     * @see org.placelab.core.Coordinate#getLatitudeHemisphereNMEA()
     */
    public String getLatitudeHemisphereNMEA() {
        return lat > 0 ? "N" : "S";
    }

    /* (non-Javadoc)
     * @see org.placelab.core.Coordinate#getLongitudeHemisphereNMEA()
     */
    public String getLongitudeHemisphereNMEA() {
        return lon > 0 ? "E" : "W";
    }
}

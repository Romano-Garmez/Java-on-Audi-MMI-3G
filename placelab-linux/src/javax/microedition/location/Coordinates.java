package javax.microedition.location;


/**
 * 
 * The Coordinates class represents coordinates as latitude-longitude-altitude
 * values. The latitude and longitude values are expressed in degrees using
 * floating point values. The degrees are in decimal values (rather than
 * minutes/seconds). The coordinates are given using the WGS84 datum.
 * 
 * This class also provides convenience methods for converting between a string
 * coordinate representation and the double representation used in this class.
 *  
 */

public class Coordinates {

	/**
	 * Identifier for string coordinate representation Degrees, Minutes, decimal
	 * fractions of a minute.
	 */
	public static final int DD_MM = 2;

	/**
	 * Identifier for string coordinate representation Degrees, Minutes, Seconds
	 * and decimal fractions of a second.
	 */
	public static final int DD_MM_SS = 1;
	
	private double latitude;
	private double longitude;
	private float altitude;

	/**
	 * Constructs a new Coordinates object with the values specified. The
	 * latitude and longitude parameters are expressed in degrees using floating
	 * point values. The degrees are in decimal values (rather than
	 * minutes/seconds).
	 * 
	 * The coordinate values always apply to the WGS84 datum.
	 * 
	 * The Float.NaN value can be used for altitude to indicate that altitude is
	 * not known.
	 * 
	 * @param latitude
	 *            the latitude of the location. Valid range: [-90.0, 90.0].
	 *            Positive values indicate northern latitude and negative values
	 *            southern latitude.
	 * @param longitude
	 *            the longitude of the location. Valid range: [-180.0, 180.0).
	 *            Positive values indicate eastern longitude and negative values
	 *            western longitude.
	 * @param altitude
	 *            the altitude of the location in meters, defined as height
	 *            above the WGS84 ellipsoid. Float.NaN can be used to indicate
	 *            that altitude is not known.
	 * @throws java.lang.IllegalArgumentException
	 *             if an input parameter is out of the valid range
	 */
	public Coordinates(double latitude, double longitude, float altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	
	/**
	 * Returns the latitude component of this coordinate. Positive values indicate northern latitude and  negative values southern latitude.
	 * The latitude is given in WGS84 datum.
	 * @return the latitude in degrees
	 * @see #setLatitude(double) 
	 */
	public double getLatitude () {
		return latitude;
	}
	
	/**
	 * Returns the longitude component of this coordinate.  Positive values indicate eastern longitude and  negative values western longitude.

 The longitude is given in WGS84 datum.
	 * @return the longitude in degrees
	 * @see #setLongitude(double)
	 */
	public double getLongitude () {
		return longitude;
	}
	
	/**
	 * Returns the altitude component of this coordinate. Altitude is defined  to mean height above the WGS84 reference ellipsoid. 0.0 means a location at the ellipsoid surface, negative values mean the location is below the ellipsoid surface, Float.NaN that the altitude is not available.
	 * @return the altitude in meters above the reference ellipsoid
	 * @see #gsetAltitude(float)
	 */
	public float getAltitude () {
		return altitude;		
	}
	
	/**
	 * 
	 * Sets the geodetic altitude for this point.
	 * 
	 * @param altitude
	 *            the altitude of the location in meters, defined as height
	 *            above the WGS84 ellipsoid. 0.0 means a location at the
	 *            ellipsoid surface, negative values mean the location is below
	 *            the ellipsoid surface, Float.NaN that the altitude is not
	 *            available
	 * @see getAltitude()
	 */
	public void setAltitude (float altitude) {
		this.altitude = altitude;
	}
	
	/**
	 * Sets the geodetic latitude for this point. Latitude is given as a double
	 * expressing the latitude in degrees in the WGS84 datum.
	 * 
	 * @param latitude
	 *            the latitude component of this location in degrees. Valid
	 *            range: [-90.0, 90.0].
	 * @throws java.lang.IllegalArgumentException
	 *             if latitude is out of the valid range
	 * @see #getLatitude()
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Sets the geodetic longitude for this point. Longitude is given as a
	 * double expressing the longitude in degrees in the WGS84 datum.
	 * 
	 * @param longitude
	 *            the longitude of the location in degrees. Valid range:
	 *            [-180.0, 180.0)
	 * @throws java.lang.IllegalArgumentException
	 *             if longitude is out of the valid range
	 * @see #getLongitude()
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	/**
	 * 
	 * Converts a String representation of a coordinate into the double
	 * representation as used in this API.
	 * 
	 * There are two string syntaxes supported:
	 * 
	 * 1. Degrees, minutes, seconds and decimal fractions of seconds. This is
	 * expressed as a string complying with the following BNF definition where
	 * the degrees are within the range [-179, 179] and the minutes and seconds
	 * are within the range [0, 59], or the degrees is -180 and the minutes,
	 * seconds and decimal fractions are 0:
	 * 
	 * <code>
	 coordinate = degrees ":" minutes ":" seconds "." decimalfrac
	 | degrees ":" minutes ":" seconds
	 | degrees ":" minutes
	 degrees = degreedigits | "-" degreedigits
	 degreedigits = digit | nonzerodigit digit | "1" digit digit 
	 minutes = minsecfirstdigit digit
	 seconds = minsecfirstdigit digit
	 decimalfrac = 1*3digit 
	 digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
	 nonzerodigit = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
	 minsecfirstdigit = "0" | "1" | "2" | "3" | "4" | "5" 
	 </code>
	 * 
	 * 2. Degrees, minutes and decimal fractions of minutes. This is expressed
	 * as a string complying with the following BNF definition where the degrees
	 * are within the range [-179, 179] and the minutes are within the range [0,
	 * 59], or the degrees is -180 and the minutes and decimal fractions are 0:
	 * 
	 * 
	 * <code>
	 coordinate = degrees ":" minutes "." decimalfrac
	 | degrees ":" minutes
	 degrees = degreedigits | "-" degreedigits
	 degreedigits = digit | nonzerodigit digit | "1" digit digit 
	 minutes = minsecfirstdigit digit
	 decimalfrac = 1*5digit
	 digit = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
	 nonzerodigit = "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
	 minsecfirstdigit = "0" | "1" | "2" | "3" | "4" | "5" 
	 </code>
	 * 
	 * For example, for the double value of the coordinate 61.51d, the
	 * corresponding syntax 1 string is "61:30:36" and the corresponding syntax
	 * 2 string is "61:30.6".
	 * 
	 * @param coordinate
	 *            a String in either of the two representation specified above
	 * 
	 * @return a double value with decimal degrees that matches the string
	 *         representation given as the parameter
	 * @throws java.lang.IllegalArgumentException
	 *             if the coordinate input parameter does not comply with the
	 *             defined syntax for the specified types
	 * @throws java.lang.NullPointerException
	 *             if the coordinate string is null
	 */
	public static double convert (String coordinate) {
		
		try {
			
			int degrees;
			int minutes;
			int seconds;
			int decimalFrac;
			
			//System.out.println("coor: " + coordinate);
			
			if (coordinate.indexOf(':') == -1)
				throw new IllegalArgumentException();
			
			if (coordinate.indexOf('.') == -1)
				coordinate = coordinate + ".0";
			
			degrees = Integer.parseInt(coordinate.substring(0, coordinate.indexOf(':')));
			//System.out.println("degrees: " + degrees);
			
			if ( (degrees < -179) || (degrees > 179) )
				throw new IllegalArgumentException();
			
			//System.out.println("a: " + coordinate.indexOf(':', coordinate.indexOf(':')));
			//System.out.println("b: " + coordinate.indexOf('.', coordinate.indexOf(':')));
			
			
			
			if (
					(coordinate.indexOf(':', coordinate.indexOf(':')+1) == -1) &&
					(coordinate.indexOf('.', coordinate.indexOf(':')) != -1) ) {
				// decimal mode
				minutes = Integer.parseInt(coordinate.substring(coordinate.indexOf(':')+1, coordinate.indexOf('.', coordinate.indexOf(':'))));
				
				if ( (minutes < 0) || (minutes > 59) )
					throw new IllegalArgumentException();
				
				
				decimalFrac = Integer.parseInt(coordinate.substring(coordinate.indexOf('.', coordinate.indexOf(':'))+1, coordinate.length()));
				if (decimalFrac < 0)
					throw new IllegalArgumentException();
				
				//System.out.println("minutes: " + minutes);
				//System.out.println("decimalFrac: " + decimalFrac);
				
				return degrees + (Double.parseDouble(minutes+"."+decimalFrac)/60D);
								
			} else if ( (coordinate.indexOf(':', coordinate.indexOf(':')) != -1) &&
					(coordinate.indexOf('.', coordinate.indexOf(':', coordinate.indexOf(':'))) != -1) ) {
				
				minutes = Integer.parseInt(coordinate.substring(coordinate.indexOf(':')+1, coordinate.indexOf(':', coordinate.indexOf(':')+1)));
				//System.out.println("here: " + coordinate.substring(coordinate.indexOf(':')+1, coordinate.indexOf(':', coordinate.indexOf(':')+1)));
				//System.out.println("here2: " + coordinate.substring(coordinate.indexOf(':', coordinate.indexOf(':')+1)+1, coordinate.indexOf('.', coordinate.indexOf(':', coordinate.indexOf(':')))));
				
				seconds = Integer.parseInt(coordinate.substring(coordinate.indexOf(':', coordinate.indexOf(':')+1)+1, coordinate.indexOf('.', coordinate.indexOf(':', coordinate.indexOf(':')))));
				decimalFrac = Integer.parseInt(coordinate.substring( coordinate.indexOf('.', coordinate.indexOf(':', coordinate.indexOf(':')))+1, coordinate.length()));
				
				return degrees + (minutes/60D) + (Double.parseDouble(seconds+"."+decimalFrac) / 3600D);
			} else {
				throw new IllegalArgumentException();
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Incorrect format: " + coordinate);
		}
		
	}
	
	/**
	 * Converts a double representation of a coordinate with decimal degrees
	 * into a string representation.
	 * 
	 * There are string syntaxes supported are the same as for the
	 * convert(String) method. The implementation shall provide as many
	 * significant digits for the decimal fractions as are allowed by the string
	 * syntax definition.
	 * 
	 * @param coordinate
	 *            a double reprentation of a coordinate
	 * @param outputType
	 *            identifier of the type of the string representation wanted for
	 *            output The constant DD_MM_SS identifies the syntax 1 and the
	 *            constant DD_MM identifies the syntax 2.
	 * @return a string representation of the coordinate in a representation
	 *         indicated by the parameter
	 * @throws java.lang.IllegalArgumentException
	 *             if the outputType is not one of the two costant values
	 *             defined in this class or if the coordinate value is not
	 *             within the range [-180.0, 180.0) or is Double.NaN
	 * @see #convert(string)
	 */
	public static String convert(double coordinate, int outputType) {
		if (outputType == DD_MM) {
			int deg = (int) Math.floor(coordinate);
			float min = (float) ((coordinate - deg) * 60);
			return deg + ":" + min;
		} else if (outputType == DD_MM_SS) {
			int deg = (int) Math.floor(coordinate);
			float min = (float) ((coordinate - deg) * 60);
			float sec = (float) ((min - Math.floor(min)) * 60);
			return deg + ":" + ((int) min) + ":" + sec;
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Calculates the azimuth between the two points according to the ellipsoid
	 * model of WGS84. The azimuth is relative to true north. The Coordinates
	 * object on which this method is called is considered the origin for the
	 * calculation and the Coordinates object passed as a parameter is the
	 * destination which the azimuth is calculated to. When the origin is the
	 * North pole and the destination is not the North pole, this method returns
	 * 180.0. When the origin is the South pole and the destination is not the
	 * South pole, this method returns 0.0. If the origin is equal to the
	 * destination, this method returns 0.0. The implementation shall calculate
	 * the result as exactly as it can. However, it is required that the result
	 * is within 1 degree of the correct result.
	 * 
	 * @param to
	 *            the Coordinates of the destination
	 * @return the azimuth to the destination in degrees. Result is within the
	 *         range [0.0 ,360.0).
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 */
	public float azimuthTo(Coordinates to) {
		double lat1 = getLatitude();
		double lon1 = getLongitude();
		double lat2 = to.getLatitude();
		double lon2 = to.getLongitude();

		double dtor = Math.PI / 180.0;
		double rtod = 180.0 / Math.PI;

		double distance = rtod
				* Math.acos(Math.sin(lat1 * dtor) * Math.sin(lat2 * dtor)
						+ Math.cos(lat1 * dtor) * Math.cos(lat2 * dtor)
						* Math.cos((lon2 - lon1) * dtor));

		double cosAzimuth = (Math.cos(lat1 * dtor) * Math.sin(lat2 * dtor) - Math
				.sin(lat1 * dtor)
				* Math.cos(lat2 * dtor) * Math.cos((lat2 - lon1) * dtor))
				/ Math.sin(distance * dtor);

		double sinAzimuth = Math.cos(lat2 * dtor)
				* Math.sin((lat2 - lon1) * dtor) / Math.sin(distance * dtor);

		return (float) (rtod * Math.atan2(sinAzimuth, cosAzimuth));
	}

	/**
	 * Calculates the geodetic distance between the two points according to the
	 * ellipsoid model of WGS84. Altitude is neglected from calculations.
	 * 
	 * The implementation shall calculate this as exactly as it can. However, it
	 * is required that the result is within 0.35% of the correct result.
	 * 
	 * @param to
	 *            the Coordinates of the destination
	 * @return the distance to the destination in meters
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 */
	public float distance(Coordinates to) {
		
		/*
		 * Haversine Formula (from R.W. Sinnott, "Virtues of the Haversine", Sky
		 * and Telescope, vol. 68, no. 2, 1984, p. 159):
		 * 
		 * See the following URL for more info on calculating distances:
		 * http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1
		 */
		
		double earthRadius = 6371; //km

		double lat1 = Math.toRadians(to.getLatitude());
		double lon1 = Math.toRadians(to.getLongitude());
		double lat2 = Math.toRadians(getLatitude());
		double lon2 = Math.toRadians(getLongitude());

		double dlon = (lon2 - lon1);
		double dlat = (lat2 - lat1);

		double a = (Math.sin(dlat / 2)) * (Math.sin(dlat / 2))
				+ (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)))
				* (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)));

		double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));

		double km = earthRadius * c;

		return (float) (km * 1000);
	}
	
}

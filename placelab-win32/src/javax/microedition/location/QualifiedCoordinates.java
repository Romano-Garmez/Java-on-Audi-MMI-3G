
package javax.microedition.location;

/**
 * The QualifiedCoordinates class represents coordinates as
 * latitude-longitude-altitude values that are associated with an accuracy
 * value.
 */

public class QualifiedCoordinates extends Coordinates {
	

	/**
	 * Constructs a new QualifiedCoordinates object with the values specified.
	 * The latitude and longitude parameters are expressed in degrees using
	 * floating point values. The degrees are in decimal values (rather than
	 * minutes/seconds).
	 * 
	 * The coordinate values always apply to the WGS84 datum.
	 * 
	 * The Float.NaN value can be used for altitude to indicate that altitude is
	 * not known.
	 * 
	 * @param latitude
	 *            he latitude of the location. Valid range: [-90.0, 90.0]
	 * @param longitude
	 *            the longitude of the location. Valid range: [-180.0, 180.0)
	 * @param altitude
	 *            the altitude of the location in meters, defined as height
	 *            above WGS84 ellipsoid. Float.NaN can be used to indicate that
	 *            altitude is not known.
	 * @param horizontalAccuracy
	 *            the horizontal accuracy of this location result in meters.
	 *            Float.NaN can be used to indicate that the accuracy is not
	 *            known. Must be greater or equal to 0.
	 * @param verticalAccuracy
	 *            the vertical accuracy of this location result in meters.
	 *            Float.NaN can be used to indicate that the accuracy is not
	 *            known. Must be greater or equal to 0.
	 * @throws java.lang.IllegalArgumentException
	 *             if an input parameter is out of the valid range
	 */
	public QualifiedCoordinates(double latitude, double longitude,
			float altitude, float horizontalAccuracy, float verticalAccuracy) {
		super(latitude, longitude, altitude);
	}
	
	
	/**
	 * Returns the horizontal accuracy of the location in meters (1-sigma
	 * standard deviation). A value of Float.NaN means the horizontal accuracy
	 * could not be determined.
	 * 
	 * The horizontal accuracy is the RMS (root mean square) of east accuracy
	 * (latitudinal error in meters, 1-sigma standard deviation), north accuracy
	 * (longitudinal error in meters, 1-sigma).
	 * 
	 * @return the horizontal accuracy in meters. Float.NaN if this is not known
	 */
	public float getHorizontalAccuracy() {
		return 0F;
	}

	/**
	 * Returns the accuracy of the location in meters in vertical direction
	 * (orthogonal to ellipsoid surface, 1-sigma standard deviation). A value of
	 * Float.NaN means the vertical accuracy could not be determined.
	 * 
	 * @return the vertical accuracy in meters. Float.NaN if this is not known.
	 */
	public float getVerticalAccuracy() {
		return 0F;
	}

	/**
	 * Sets the horizontal accuracy of the location in meters (1-sigma standard
	 * deviation). A value of Float.NaN means the horizontal accuracy could not
	 * be determined.
	 * 
	 * The horizontal accuracy is the RMS (root mean square) of east accuracy
	 * (latitudinal error in meters, 1-sigma standard deviation), north accuracy
	 * (longitudinal error in meters, 1-sigma).
	 * 
	 * @param horizontalAccuracy
	 *            the horizontal accuracy of this location result in meters.
	 *            Float.NaN means the horizontal accuracy could not be
	 *            determined. Must be greater or equal to 0.
	 * @throws java.lang.IllegalArgumentException
	 *             if the parameter is less than 0
	 */
	public void setHorizontalAccuracy(float horizontalAccuracy) {

	}

	/**
	 * Sets the accuracy of the location in meters in vertical direction
	 * (orthogonal to ellipsoid surface, 1-sigma standard deviation). A value of
	 * Float.NaN means the vertical accuracy could not be determined.
	 * 
	 * @param verticalAccuracy
	 *            the vertical accuracy of this location result in meters.
	 *            Float.NaN means the horizontal accuracy could not be
	 *            determined. Must be greater or equal to 0.
	 * @throws java.lang.IllegalArgumentException
	 *             if the parameter is less than 0
	 */
	public void setVerticalAccuracy(float verticalAccuracy) {

	}
}

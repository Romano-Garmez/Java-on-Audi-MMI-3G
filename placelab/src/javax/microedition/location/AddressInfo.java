

package javax.microedition.location;

public class AddressInfo {
	
	/** Address field noting a building floor. */
	public static final int BUILDING_FLOOR = 11;
	
	/** Address field denoting a building name. */
	public static final int BUILDING_NAME = 10;
	
	/** Address field denoting a building room. */
	public static final int BUILDING_ROOM = 12;
	
	/** Address field denoting a building zone. */
	public static final int BUILDING_ZONE = 13;
	
	/** Address field denoting town or city name. */
	public static final int CITY = 4;
	
	/** Address field denoting a county, which is an entity between a state and a city. */ 
	public static final int COUNTY = 5;
	
	/** Address field denoting country as a two-letter ISO 3166-1 code. */
	public static final int COUNTRY_CODE = 8;
	
	/** Address field denoting a street in a crossing. */
	public static final int CROSSING1 = 14;

	/** Address field denoting a street in a crossing. */
	public static final int CROSSING2 = 15;
	
	/** Address field denoting a municipal district. */
	public static final int DISTRICT = 9;
	
	/** Address field denoting address extension, e.g. flat number. */
	public static final int EXTENSION = 1;
	
	/** Address field denoting a phone number for this place. */
	public static final int PHONE_NUMBER = 17;
	
	/** Address field denoting zip or postal code. */
	public static final int POSTAL_CODE = 3;
	
	/** Address field denoting state or province. */
	public static final int STATE = 6;
	
	/** Address field denoting street name and number. */
	public static final int STREET = 2;
	/** Address field denoting a URL for this place. */
	public static final int URL = 16;
	
	private String[] data = new String[18];
	
	/**
	 * Returns the value of an address field. If the field is not available  null is returned.
	 *  Example: <code>getField(AddressInfo.STREET)</code> might return  "113 Broadway" if the location is on Broadway, New York, or  null if not available.
	 * @param field the ID of the field to be retrieved.
	 * @return the address field string. If the field is not set, returns null
	 * @see #setField(int, String)
	 */
	public String getField (int field) {
		return data[field];
	}
	
	/**
	 * Sets the value of an address field.
	 * @param field the ID of the field to be set
	 * @param value the new value for the field. null  is used to indicate that the field has no content.
	 * @throws java.lang.IllegalArgumentException if the parameter field ID is not one of the constant values defined in this class.
	 * @see #getField(int)
	 */
	public void setField (int field, String value) {
		data[field] = value;
	}
}

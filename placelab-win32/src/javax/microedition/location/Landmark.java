
package javax.microedition.location;

/**
 * The Landmark class represents a landmark, i.e. a known location with a name.
 * A landmark has a name by which it is known to the end user, a textual
 * description, QualifiedCoordinates and optionally AddressInfo.
 * 
 * This class is only a container for the information. The constructor does not
 * validate the parameters passed in but just stores the values, except the name
 * field is never allowed to be null. The get* methods return the values passed
 * in the constructor. When the platform implementation returns Landmark
 * objects, it MUST ensure that it only returns objects where the parameters
 * have values set as described for their semantics in this class.
 *  
 */

public class Landmark {
	
	private String name;
	private String desc;
	private QualifiedCoordinates qualifiedCoordinates;
	private AddressInfo addressInfo;
	
	public Landmark(String name, String description,
			QualifiedCoordinates coordinates, AddressInfo addressInfo) {
		this.name = name;
		this.desc = description;
		this.qualifiedCoordinates = coordinates;
		this.addressInfo = addressInfo;
	}

	/**
	 * Gets the AddressInfo of the landmark
	 * @return the AddressInfo of the landmark
	 * @see #setAddressInfo(AddressInfo)
	 */
	public AddressInfo getAddressInfo() {
		return addressInfo;
	}

	/**
	 * Sets the name of the landmark.
	 * @param name name for the landmark
	 * @throws java.lang.NullPointerException if the parameter is null
	 * @see #getName()
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * Sets the description of the landmark.
	 * @param description description for the landmark, null  may be passed in to indicate that description  is not available.
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		this.desc = description;
	}

	/**
	 * Sets the QualifiedCoordinates of the landmark.
	 * @param coordinates the qualified coordinates of the landmark
	 * @see #getQualifiedCoordinates()
	 */
	public void setQualifiedCoordinates(QualifiedCoordinates coordinates) {
		this.qualifiedCoordinates = coordinates;
	}

	/**
	 * Sets the AddressInfo of the landmark.
	 * @param addressInfo the AddressInfo of the landmark
	 * @see #getAddressInfo()
	 */
	public void setAddressInfo(AddressInfo addressInfo) {
		this.addressInfo = addressInfo;
	}
	
	/**
	 * Gets the landmark name.
	 * @return the name of the landmark
	 * @see #setName(String)
	 */
	public String getName() {
		return name;
	}	
	
	/**
	 * Gets the landmark description.
	 * @return returns the description of the landmark, null  if not available
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return desc;
	}
	
	/**
	 * Gets the QualifiedCoordinates of the landmark.
	 * @return QualifiedCoordinates of the  landmark. null
	 * @see #setQualifiedCoordinates(QualifiedCoordinates)
	 */
	public QualifiedCoordinates getQualifiedCoordinates() {
		return qualifiedCoordinates;
	}
}



package javax.microedition.location;

import java.io.IOException;
import java.util.Enumeration;

import org.placelab.jsr0179.FSLandmarkStore;

/**
 * 
 * <P>
 * The <code>LandmarkStore</code> class provides methods to store, delete and
 * retrieve landmarks from a persistent landmark store. There is one default
 * landmark store and there may be multiple other named landmark stores. The
 * implementation may support creating and deleting landmark stores by the
 * application. All landmark stores MUST be shared between all J2ME applications
 * and MAY be shared with native applications in the terminal. Named landmark
 * stores have unique names in this API. If the underlying implementation allows
 * multiple landmark stores with the same name, it must present them with unique
 * names in the API e.g. by adding some postfix to those names that have
 * multiple instances in order to differentiate them.
 * <p>
 * The <code>Landmark</code> s have a name and may be placed in a category or
 * several categories. The category is intended to group landmarks that are of
 * similar type to the end user, e.g. restaurants, museums, etc. The landmark
 * names are strings that identify the landmark to the end user. The category
 * names describe the category to the end user. The language used in the names
 * may be any and depends on the preferences of the end user. The names of the
 * categories are unique within a <code>LandmarkStore</code>. However, the
 * names of the landmarks are not guaranteed to be unique. <code>Landmark</code>
 * s with the same name can appear in multiple categories or even several
 * <code>Landmark</code> s with the same name in the same category.
 * </p>
 * <p>
 * The <code>Landmark</code> objects returned from the
 * <code>getLandmarks</code> methods in this class shall guarantee that the
 * application can read a consistent set of the landmark data valid at the time
 * of obtaining the object instance, even if the landmark information in the
 * store is modified subsequently by this or some other application.
 * </p>
 * <p>
 * The <code>Landmark</code> object instances can be in two states: <br>
 * <ul>
 * <li>initially constructed by an application</li>
 * <li>belongs to a <code>LandmarkStore</code></li>
 * </ul>
 * A <code>Landmark</code> object belongs to a <code>LandmarkStore</code> if
 * it has been obtained from the <code>LandmarkStore</code> using
 * <code>getLandmarks</code> or if it has been added to the
 * <code>LandmarkStore</code> using <code>addLandmark</code>. A
 * <code>Landmark</code> object is initially constructed by an application
 * when it has been constructed using the constructor but has not been added to
 * a <code>LandmarkStore</code> using <code>addLandmark</code>.
 * </p>
 * <p>
 * The landmark stores created by an application and landmarks added in landmark
 * stores persist even if the application itself is deleted from the terminal.
 * </p>
 * <p>
 * Accessing the landmark store may cause a <code>SecurityException</code>,
 * if the calling application does not have the required permissions. The
 * permissions to read and write (including add and delete) landmarks are
 * distinct. An application having e.g. a permission to read landmarks wouldn't
 * necessarily have the permission to delete them. The permissions (names etc.)
 * for the MIDP 2.0 security framework are defined elsewhere in this
 * specification.
 * </p>
 * <P>
 *  
 */

public class LandmarkStore {
	
	/*
	 * Removed for now until we decide to implement.
	*/
	private static final Object[][] stores = new Object[][]{
			{
				"fs",
				FSLandmarkStore.class,
				null
			}
	};
	
	
	// We're currently not supporting any landmark stores
	//private static final Object[][] stores = new Object[][]{};
	
	/**
	 * Adds a category to this LandmarkStore.
	 * 
	 * All implementations must support names that have length up to and
	 * including 32 characters. If the provided name is longer it may be
	 * truncated by the implementation if necessary.
	 * 
	 * @param categoryName
	 *            name for the category to be added
	 * @throws java.lang.IllegalArgumentException
	 *             if a category with the specified name already exists
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 * @throws LandmarkException
	 *             if this LandmarkStore does not support adding new categories
	 * @throws java.io.IOException
	 *             if an I/O error occurs or there are no resources to add a new
	 *             category
	 * @throws java.lang.SecurityException
	 *             if the application does not have the permission to manage
	 *             categories
	 *  
	 */
	public void addCategory(String categoryName) throws LandmarkException, IOException {

	}
	
	/**
	 * Removes a category from this LandmarkStore. The category will be removed
	 * from all landmarks that are in that category. However, this method will
	 * not remove any of the landmarks, only the associated category information
	 * from the landmarks. If a category with the supplied name does not exist
	 * in this LandmarkStore, the method returns silently with no error.
	 * 
	 * @param categoryName
	 *            name for the category to be removed
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 * @throws LandmarkException
	 *             if this LandmarkStore does not support deleting categories
	 * @throws java.io.IOException
	 *             if an I/O error occurs
	 * @throws java.lang.SecurityException
	 *             if the application does not have the permission to manage
	 *             categories
	 */
	public void deleteCategory(String categoryName) throws LandmarkException, IOException {

	}
	
	/**
	 * Adds a landmark to the specified group in the landmark store.
	 * 
	 * If some textual String field inside the landmark object is set to a value
	 * that is too long to be stored, the implementation is allowed to
	 * automatically truncate fields that are too long.
	 * 
	 * However, the name field MUST NOT be truncated. Every implementation shall
	 * be able to support name fields that are 32 characters or shorter.
	 * Implementations may support longer names but are not required to. If an
	 * application tries to add a Landmark with a longer name field than the
	 * implementation can support, IllegalArgumentException is thrown.
	 * 
	 * When the landmark store is empty, every implementation is required to be
	 * able to store a landmark where each String field is set to a 30 character
	 * long string.
	 * 
	 * If the Landmark object that is passed as a parameter is an instance that
	 * belongs to this LandmarkStore, the same landmark instance will be added
	 * to the specified category in addition to the category/categories which it
	 * already belongs to. If the landmark already belongs to the specified
	 * category, this method returns with no effect. If the landmark has been
	 * deleted after obtaining it from getLandmarks, it will be added back when
	 * this method is called.
	 * 
	 * If the Landmark object that is passed as a parameter is an instance
	 * initially constructed by the application using the constructor or an
	 * instance that belongs to a different LandmarkStore, a new landmark will
	 * be created in this LandmarkStore and it will belong initially to only the
	 * category specified in the category parameter. After this method call, the
	 * Landmark object that is passed as a parameter belongs to this
	 * LandmarkStore.
	 * 
	 * @param landmark
	 *            the landmark to be added
	 * @param category
	 *            category where the landmark is added. null can be used to
	 *            indicate that the landmark does not belong to a category
	 * @throws IOException
	 *             if an I/O error happened when accessing the landmark store or
	 *             if there are no resources available to store this landmark
	 * @throws java.lang.SecurityException
	 *             if the application is not allowed to add landmarks
	 * @throws java.lang.IllegalArgumentException
	 *             if the landmark has a longer name field than the
	 *             implementation can support or if the category is not null or
	 *             one of the categories defined in this LandmarkStore
	 * @throws java.io.IOException
	 *             if an I/O error happened when accessing the landmark store or
	 *             if there are no resources available to store this landmark
	 * @throws java.lang.NullPointerException
	 *             if the landmark parameter is null
	 */
	public void addLandmark(Landmark landmark, String category) throws IOException {
		
	}
	
	/**
	 * Creates a new landmark store with a specified name.
	 * 
	 * All LandmarkStores are shared between all J2ME applications and may be
	 * shared with native applications. Implementations may support creating
	 * landmark stores on a removable media. However, the Java application is
	 * not able to directly choose where the landmark store is stored, if the
	 * implementation supports several storage media. The implementation of this
	 * method may e.g. prompt the end user to make the choice if the
	 * implementation supports several storage media. If the landmark store is
	 * stored on a removable media, this media might be removed by the user
	 * possibly at any time causing it to become unavailable.
	 * 
	 * A newly created landmark store does not contain any landmarks.
	 * 
	 * @param storeName
	 *            the name of the landmark store to create
	 * @throws IOException
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 * @throws java.lang.IllegalArgumentException
	 *             if the name is too long or if a landmark store with the
	 *             specified name already exists
	 * @throws java.io.IOException
	 *             if the landmark store couldn't be created due to an I/O error
	 * @throws java.lang.SecurityException
	 *             if the application does not have permissions to create a new
	 *             landmark store
	 * @throws LandmarkException
	 *             if the implementation does not support creating new landmark
	 *             stores
	 */
	public static void createLandmarkStore(String storeName) throws IOException, LandmarkException {
		throw new LandmarkException("Not supported");
	}
	
	/**
	 * Delete a landmark store with a specified name. All the landmarks and
	 * categories defined in the named landmark store are irrevocably removed.
	 * 
	 * If a landmark store with the specified name does not exist, this method
	 * returns silently without any error.
	 * 
	 * @param storeName
	 *            the name of the landmark store to create
	 * @throws IOException
	 *             if the landmark store couldn't be deleted due to an I/O error
	 * @throws LandmarkException
	 *             if the implementation does not support deleting landmark
	 *             stores
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null (the default landmark store can't be
	 *             deleted)
	 * 
	 * @throws java.lang.SecurityException
	 *             if the appliction does not have permissions to delete a
	 *             landmark store
	 */
	public static void deleteLandmarkStore(String storeName) throws IOException, LandmarkException {
		throw new LandmarkException("Not supported.");
	}
	
	/**
	 * Lists the names of all the available landmark stores.
	 * 
	 * The default landmark store is obtained from getInstance by passing null
	 * as the parameter. The null name for the default landmark store is not
	 * included in the list returned by this method. If there are no named
	 * landmark stores, other than the default landmark store, this method
	 * returns null.
	 * 
	 * Returns:
	 * 
	 * @return an array of landmark store names
	 * @throws java.lang.SecurityException
	 *             if the application does not have the permission to access
	 *             landmark stores
	 * @throws java.io.IOException
	 *             if an I/O error occurred when trying to access the landmark
	 *             stores
	 */
	public static String[] listLandmarkStores() throws IOException {
		String[] names = new String[stores.length];
		
		for (int i=0;i<stores.length;i++) {
			names[i] = (String) stores[i][0];
		}
		
		return names;
	}
	
	/**
	 * Gets the Landmarks from the storage where the category and/or name
	 * matches the given parameters.
	 * 
	 * @param category
	 *            the category of the landmark. null implies a wildcard that
	 *            matches all categories
	 * @param name
	 *            the name of the desired landmark. null implies a wildcard that
	 *            matches all the names within
	 * @return an Enumeration containing all the matching Landmarks or null if
	 *         no Landmark matched the given parameters
	 * @throws IOException
	 *             if an I/O error happened when accessing the landmark store
	 */
	public Enumeration getLandmarks(String category, String name)
			throws IOException {
		return null;
	}
	
	/**
	 * Lists all landmarks stored in the store.
	 * 
	 * @return a java.util.Enumeration object containing Landmark objects
	 *         representing all the landmarks stored in this LandmarkStore or
	 *         null if there are no landmarks in the store
	 * @throws IOException
	 *             if an I/O error happened when accessing the landmark store
	 */
	public Enumeration getLandmarks() throws IOException {
		return null;
	}
	
	/**
	 * Lists all the landmarks that are within an area defined by bounding
	 * minimum and maximum latitude and longitude and belong to the defined
	 * category, if specified. The bounds are considered to belong to the area.
	 * 
	 * If minLongitude <= maxLongitude, this area covers the longitude range
	 * [minLongitude, maxLongitude]. If minLongitude > maxLongitude, this area
	 * covers the longitude range [-180.0, maxLongitude] and [minLongitude,
	 * 180.0).
	 * 
	 * For latitude, the area covers the latitude range [minLatitude,
	 * maxLatitude].
	 * 
	 * @param category
	 *            the category of the landmark. null implies a wildcard that
	 *            matches all categories
	 * @param minLatitude
	 *            minimum latitude of the area. Must be within the range [-90.0,
	 *            90.0]
	 * @param maxLatitude
	 *            maximum latitude of the area. Must be within the range
	 *            [minLatitude, 90.0]
	 * @param minLongitude
	 *            minimum longitude of the area. Must be within the range
	 *            [-180.0, 180.0)
	 * @param maxLongitude
	 *            maximum longitude of the area. Must be within the range
	 *            [-180.0, 180.0)
	 * @return an Enumeration containing all the matching Landmarks or null if
	 *         no Landmark matched the given parameters
	 * @throws IOException
	 *             if an I/O error happened when accessing the landmark store
	 * @throws java.lang.IllegalArgumentException
	 *             if the minLongitude or maxLongitude is out of the range
	 *             [-180.0, 180.0), or minLatitude or maxLatitude is out of the
	 *             range [-90.0,90.0], or if minLatitude > maxLatitude
	 */
	public Enumeration getLandmarks(String category, double minLatitude,
			double maxLatitude, double minLongitude, double maxLongitude)
			throws IOException {
		return null;
	}
	

	/**
	 * Removes the named landmark from the specified category.
	 * 
	 * The Landmark instance passed in as the parameter must be an instance that
	 * belongs to this LandmarkStore.
	 * 
	 * If the Landmark is not found in this LandmarkStore in the specified
	 * category or if the parameter is a Landmark instance that does not belong
	 * to this LandmarkStore, then the request is silently ignored and the
	 * method call returns with no error. The request is also silently ignored
	 * if the specified category does not exist in this LandmarkStore.
	 * 
	 * The landmark is only removed from the specified category but the landmark
	 * information is retained in the store. If the landmark no longer belongs
	 * to any category, it can still be obtained from the store by passing null
	 * as the category to getLandmarks.
	 * 
	 * @param lm
	 *            the landmark to be removed
	 * @param category
	 *            the category from which it will be removed.
	 * @throws IOException
	 *             if an I/O error happened when accessing the landmark store
	 * @throws java.lang.SecurityException
	 *             if the application is not allowed to delete the landmark
	 * @throws java.lang.NullPointerException
	 *             if either parameter is null
	 */
	public void removeLandmarkFromCategory(Landmark lm, String category)
			throws IOException {

	}
	

	/**
	 * Updates the information about a landmark. This method only updates the
	 * information about a landmark and does not modify the categories the
	 * landmark belongs to.
	 * 
	 * The Landmark instance passed in as the parameter must be an instance that
	 * belongs to this LandmarkStore.
	 * 
	 * This method can't be used to add a new landmark to the store.
	 * 
	 * @param lm
	 *            the landmark to be updated
	 * @throws java.lang.SecurityException
	 *             if the application is not allowed to update the landmark
	 * @throws LandmarkException
	 *             if the landmark instance passed as the parameter does not
	 *             belong to this LandmarkStore or does not exist in the store
	 *             any more
	 * @throws java.io.IOException
	 *             if an I/O error happened when accessing the landmark store
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 */
	public void updateLandmark(Landmark lm) throws IOException,
			LandmarkException {

	}
	
	/**
	 * Deletes a landmark from this LandmarkStore. This method removes the
	 * specified landmark from all categories and deletes the information from
	 * this LandmarkStore.
	 * 
	 * The Landmark instance passed in as the parameter must be an instance that
	 * belongs to this LandmarkStore.
	 * 
	 * If the Landmark is not found in this LandmarkStore, then the request is
	 * silently ignored and the method call returns with no error.
	 * 
	 * @param lm
	 *            the landmark to be deleted
	 * @throws java.lang.SecurityException
	 *             if the application is not allowed to delete the landmark
	 * @throws LandmarkException
	 *             if the landmark instance passed as the parameter does not
	 *             belong to this LandmarkStore
	 * @throws java.io.IOException
	 *             if an I/O error happened when accessing the landmark store
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 */
	public void deleteLandmark(Landmark lm) throws IOException,
			LandmarkException {

	}
	
	/**
	 * Returns the category names that are defined in this LandmarkStore. The
	 * language and locale used for these names depends on the implementation
	 * and end user settings. The names shall be such that they can be displayed
	 * to the end user and have a meaning to the end user
	 * 
	 * @return an java.util.Enumeration containing Strings representing the
	 *         category names. If there are no categories defined in this
	 *         LandmarkStore, an Enumeration with no entries is returned.
	 */
	public Enumeration getCategories() {
		return null;
	}
	
	
	public static LandmarkStore getInstance (String storeName) {
		for (int i=0;i<stores.length;i++) {
			String name = (String) stores[i][0];
			if (name.equals(storeName)) {
				try {
					if (stores[i][2] == null)
						stores[i][2] =(((Class)stores[i][1]).newInstance());
					return (LandmarkStore) stores[i][2];
				} catch (InstantiationException e) {
					throw new SecurityException(e.toString());
				} catch (IllegalAccessException e) {
					throw new SecurityException(e.toString());
				}
			}
		}
		return null;
	}
	
	
}

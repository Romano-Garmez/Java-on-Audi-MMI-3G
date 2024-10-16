package javax.microedition.location;

import org.placelab.jsr0179.LocationProviderImpl;

/**
 * This is the starting point for applications using this API and represents a
 * source of the location information. A LocationProvider represents a
 * location-providing module, generating Locations.
 * 
 * Applications obtain LocationProvider instances (classes implementing the
 * actual functionality by extending this abstract class) by calling the factory
 * method. It is the responsibility of the implementation to return the correct
 * LocationProvider-derived object.
 * 
 * Applications that need to specify criteria for the location provider
 * selection, must first create a Criteria object, and pass it to the factory
 * method. The methods that access the location related information shall throw
 * SecurityException if the application does not have the relevant permission to
 * access the location information.
 * 
 *  
 */
public abstract class LocationProvider {
	/** Availability status code: the location provider is available. */
	public static final int AVAILABLE = 1;

	/**
	 * Availability status code: the location provider is temporarily
	 * unavailable. Temporary unavailability means that the method is
	 * unavailable due to reasons that can be expected to possibly change in the
	 * future and the provider to become available. An example is not being able
	 * to receive the signal because the signal used by the location method is
	 * currently being obstructed, e.g. when deep inside a building for
	 * satellite based methods. However, a very short transient obstruction of
	 * the signal should not cause the provider to toggle quickly between
	 * TEMPORARILY_UNAVAILABLE and AVAILABLE.
	 */
	public static final int TEMPORARILY_UNAVAILABLE = 2;

	/**
	 * Availability status code: the location provider is out of service. Being
	 * out of service means that the method is unavailable and the
	 * implementation is not able to expect that this situation would change in
	 * the near future. An example is when using a location method implemented
	 * in an external device and the external device is detached.
	 */
	public static final int OUT_OF_SERVICE = 3;

	
	/**
	 * LocationProvider is currently hard-wired to work with only one provider, ours.
	 */
	private static LocationProviderImpl provider = null;
	
	
	/**
	 * Empty constructor to help implementations and extensions. This is not
	 * intended to be used by applications. Applications should not make
	 * subclasses of this class and invoke this constructor from the subclass.
	 */
	protected LocationProvider() {

	}

	/**
	 * Returns the current state of this LocationProvider. The return value
	 * shall be one of the availability status code constants defined in this
	 * class.
	 * 
	 * @return the availability state of this LocationProvider
	 */
	public abstract int getState();

	/**
	 * This factory method is used to get an actual LocationProvider
	 * implementation based on the defined criteria. The implementation chooses
	 * the LocationProvider so that it best fits the defined criteria, taking
	 * into account also possible implementation dependent preferences of the
	 * end user. If no concrete LocationProvider could be created that typically
	 * can match the defined criteria but there are other location providers not
	 * meeting the criteria that could be returned for a more relaxed criteria,
	 * null is returned to indicate this. The LocationException is thrown, if
	 * all supported location providers are out of service.
	 * 
	 * A LocationProvider instance is returned if there is a location provider
	 * meeting the criteria in either the available or temporarily unavailable
	 * state. Implementations should try to select providers in the available
	 * state before providers in temporarily unavailable state, but this can't
	 * be always guaranteed because the implementation may not always know the
	 * state correctly at this point in time. If a LocationProvider meeting the
	 * criteria can be supported but is currently out of service, it shall not
	 * be returned.
	 * 
	 * When this method is called with a Criteria that has all fields set to the
	 * default values (i.e. the least restrictive criteria possible), the
	 * implementation shall return a LocationProvider if there is any provider
	 * that isn't in the out of service state. Passing null as the parameter is
	 * equal to passing a Criteria that has all fields set to the default
	 * values, i.e. the least restrictive set of criteria.
	 * 
	 * This method only makes the selection of the provider based on the
	 * criteria and is intended to return it quickly to the application. Any
	 * possible initialization of the provider is done at an implementation
	 * dependent time and MUST NOT block the call to this method.
	 * 
	 * This method may, depending on the implementation, return the same
	 * LocationProvider instance as has been returned previously from this
	 * method to the calling application, if the same instance can be used to
	 * fulfil both defined criteria. Note that there can be only one
	 * LocationListener associated with a LocationProvider instance.
	 * 
	 * @param criteria
	 *            the criteria for provider selection or null to indicate the
	 *            least restrictive criteria with default values
	 * @return a LocationProvider meeting the defined criteria or null if a
	 *         LocationProvider that meets the defined criteria can't be
	 *         returned but there are other supported available or temporarily
	 *         unavailable providers that do not meet the criteria.
	 * @throws LocationException
	 *             LocationException if all LocationProviders are currently out
	 *             of service
	 * @see Criteria
	 */
	public static LocationProvider getInstance(Criteria criteria)
			throws LocationException {
		
		if (provider != null) 
			return provider;
		
		provider = new LocationProviderImpl();
		return provider;
	}
	
	/**
	 * Retrieves a Location with the constraints given by the Criteria
	 * associated with this class. If no result could be retrieved, a
	 * LocationException is thrown. If the location can't be determined within
	 * the timeout period specified in the parameter, the method shall throw a
	 * LocationException.
	 * 
	 * If the provider is temporarily unavailable, the implementation shall wait
	 * and try to obtain the location until the timeout expires. If the provider
	 * is out of service, then the LocationException is thrown immediately.
	 * 
	 * Note that the individual Location returned might not fulfil exactly the
	 * criteria used for selecting this LocationProvider. The Criteria is used
	 * to select a location provider that typically is able to meet the defined
	 * criteria, but not necessarily for every individual location measurement.
	 * 
	 * @param timeout
	 *            a timeout value in seconds. -1 is used to indicate that the
	 *            implementation shall use its default timeout value for this
	 *            provider.
	 * @return a Location object
	 * @throws LocationException
	 *             if the location couldn't be retrieved or if the timeout
	 *             period expired
	 * @throws java.lang.InterruptedException
	 *             if the operation is interrupted by calling reset() from
	 *             another thread
	 * @throws java.lang.SecurityException
	 *             if the calling application does not have a permission to
	 *             query the location information
	 * @throws java.lang.IllegalArgumentException
	 *             if the timeout = 0 or timeout < -1
	 */
	public abstract Location getLocation(int timeout) throws LocationException,
			java.lang.InterruptedException;
	
	/**
	 * Adds a LocationListener for updates at the defined interval. The listener
	 * will be called with updated location at the defined interval. The
	 * listener also gets updates when the availablilty state of the
	 * LocationProvider changes.
	 * 
	 * Passing in -1 as the interval selects the default interval which is
	 * dependent on the used location method. Passing in 0 as the interval
	 * registers the listener to only receive provider status updates and not
	 * location updates at all.
	 * 
	 * Only one listener can be registered with each LocationProvider instance.
	 * Setting the listener replaces any possibly previously set listener.
	 * Setting the listener to null cancels the registration of any previously
	 * set listener.
	 * 
	 * The implementation shall initiate obtaining the first location result
	 * immediately when the listener is registered and provide the location to
	 * the listener as soon as it is available. Subsequent location updates will
	 * happen at the defined interval after the first one. If the specified
	 * update interval is smaller than the time it takes to obtain the first
	 * result, the listener shall receive location updates with invalid
	 * Locations at the defined interval until the first location result is
	 * available.
	 * 
	 * The timeout parameter determines a timeout that is used if it's not
	 * possible to obtain a new location result when the update is scheduled to
	 * be provided. This timeout value indicates how many seconds the update is
	 * allowed to be provided late compared to the defined interval. If it's not
	 * possible to get a new location result (interval + timeout) seconds after
	 * the previous update, the update will be made and an invalid Location
	 * instance is returned. This is also done if the reason for the inability
	 * to obtain a new location result is due to the provider being temporarily
	 * unavailable or out of service. For example, if the interval is 60 seconds
	 * and the timeout is 10 seconds, the update must be delivered at most 70
	 * seconds after the previous update and if no new location result is
	 * available by that time the update will be made with an invalid Location
	 * instance.
	 * 
	 * The maxAge parameter defines how old the location result is allowed to be
	 * provided when the update is made. This allows the implementation to reuse
	 * location results if it has a recent location result when the update is
	 * due to be delivered. This parameter can only be used to indicate a larger
	 * value than the normal time of obtaining a location result by a location
	 * method. The normal time of obtaining the location result means the time
	 * it takes normally to obtain the result when a request is made. If the
	 * application specifies a time value that is less than what can be realized
	 * with the used location method, the implementation shall provide as recent
	 * location results as are possible with the used location method. For
	 * example, if the interval is 60 seconds, the maxAge is 20 seconds and
	 * normal time to obtain the result is 10 seconds, the implementation would
	 * normally start obtaining the result 50 seconds after the previous update.
	 * If there is a location result otherwise available that is more recent
	 * than 40 seconds after the previous update, then the maxAge setting to 20
	 * seconds allows to return this result and not start obtaining a new one.
	 * 
	 * @param listener
	 *            the listener to be registered. If set to null the registration
	 *            of any previously set listener is cancelled.
	 * @param interval
	 *            the interval in seconds. -1 is used for the default interval
	 *            of this provider. 0 is used to indicate that the application
	 *            wants to receive only provider status updates and not location
	 *            updates at all.
	 * @param timeout
	 *            timeout value in seconds, must be greater than 0. if the value
	 *            is -1, the default timeout for this provider is used. Also, if
	 *            the interval is -1 to indicate the default, the value of this
	 *            parameter has no effect and the default timeout for this
	 *            provider is used. If the interval is 0, this parameter has no
	 *            effect.
	 * @param maxAge
	 *            maximum age of the returned location in seconds, must be
	 *            greater than 0 or equal to -1 to indicate that the default
	 *            maximum age for this provider is used. Also, if the interval
	 *            is -1 to indicate the default, the value of this parameter has
	 *            no effect and the default maximum age for this provider is
	 *            used. If the interval is 0, this parameter has no effect.
	 * @throws java.lang.IllegalArgumentException
	 *             if interval < -1, or if (interval != -1) and (timeout >
	 *             interval or maxAge > interval or (timeout < 1 and timeout !=
	 *             -1) or (maxAge < 1 and maxAge != -1))
	 * @throws java.lang.SecurityException
	 *             if the calling application does not have a permission to
	 *             query the location information
	 */
	public abstract void setLocationListener(LocationListener listener,
			int interval, int timeout, int maxAge);
	
	
	/**
	 * Resets the LocationProvider.
	 * 
	 * All pending synchronous location requests will be aborted and any blocked
	 * getLocation method calls will terminate with InterruptedException.
	 * 
	 * Applications can use this method e.g. when exiting to have its threads
	 * freed from blocking synchronous operations.
	 */
	public abstract void reset();
	
	
	/**
	 * Returns the last known location that the implementation has. This is the
	 * best estimate that the implementation has for the previously known
	 * location.
	 * 
	 * Applications can use this method to obtain the last known location and
	 * check the timestamp and other fields to determine if this is recent
	 * enough and good enough for the application to use without needing to make
	 * a new request for the current location.
	 * 
	 * @return a location object. null is returned if the implementation doesn't
	 *         have any previous location information.
	 * @throws java.lang.SecurityException
	 *             if the calling application does not have a permission to
	 *             query the location information
	 */
	public static Location getLastKnownLocation() {
		if (provider == null)
			return null;
		
		return provider.getLastKnownLocationImpl();	
	}
	
	/**
	 * Adds a ProximityListener for updates when proximity to the specified
	 * coordinates is detected.
	 * 
	 * If this method is called with a ProximityListener that is already
	 * registered, the registration to the specified coordinates is added in
	 * addition to the set of coordinates it has been previously registered for.
	 * A single listener can handle events for multiple sets of coordinates.
	 * 
	 * If the current location is known to be within the proximity radius of the
	 * specified coordinates, the listener shall be called immediately.
	 * 
	 * Detecting the proximity to the defined coordinates is done on a best
	 * effort basis by the implementation. Due to the limitations of the methods
	 * used to implement this, there are no guarantees that the proximity is
	 * always detected; especially in situations where the terminal briefly
	 * enters the proximity area and exits it shortly afterwards, it is possible
	 * that the implementation misses this. It is optional to provide this
	 * feature as it may not be reasonably implementable with all methods used
	 * to implement this API.
	 * 
	 * If the implementation is capable of supporting the proximity monitoring
	 * and has resources to add the new listener and coordinates to be monitored
	 * but the monitoring can't be currently done due to the current state of
	 * the method used to implement it, this method shall succeeed and the
	 * monitoringStateChanged method of the listener shall be immediately called
	 * to notify that the monitoring is not active currently.
	 * 
	 * @param listener
	 *            the listener to be registered
	 * @param coordinates
	 *            the coordinates to be registered
	 * @param proximityRadius
	 *            the radius in meters that is considered to be the threshold
	 *            for being in the proximity of the specified coordinates
	 * @throws LocationException
	 *             if the platform does not have resources to add a new listener
	 *             and coordinates to be monitored or does not support proximity
	 *             monitoring at all
	 * @throws java.lang.IllegalArgumentException
	 *             if the proximity radius is 0 or negative* or Float.NaN
	 * @throws java.lang.NullPointerException
	 *             if the listener or coordinates parameter is null
	 * @throws java.lang.SecurityException
	 *             if the application does not have the permission to register a
	 *             proximity listener
	 */
	public static void addProximityListener(ProximityListener listener,
			Coordinates coordinates, float proximityRadius)
			throws LocationException {
		
		if ((proximityRadius == Float.NaN) || (proximityRadius <= 0F)) 
			throw new IllegalArgumentException();
		
		if ((listener == null) || (coordinates == null)) 
			throw new NullPointerException();
		
		provider.addProximityListenerImpl(listener, coordinates, proximityRadius);
	}
	
	/**
	 * Removes a ProximityListener from the list of recipients for updates. If
	 * the specified listener is not registered or if the parameter is null,
	 * this method silently returns with no action.
	 * 
	 * @param listener
	 *            the listener to remove
	 * @throws java.lang.NullPointerException
	 *             if the parameter is null
	 */
	public static void removeProximityListener(ProximityListener listener) {
		if (provider != null) 
			provider.removeProximityListenerImpl(listener);
	}
}

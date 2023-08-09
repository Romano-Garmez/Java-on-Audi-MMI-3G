/*
 * Created on Oct 14, 2004
 *
 */
package org.placelab.mapper.loader;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.Map;
import org.placelab.core.TwoDCoordinate;


public class EuropeRegions {
	private static Map one, two;
	
	public static TwoDCoordinate northEast(String region) {
		return (TwoDCoordinate)one.get(region);
	}

	public static TwoDCoordinate southWest(String region) {
		return (TwoDCoordinate)two.get(region);
	}
	
	public static Iterator regions() {
		return one.keySet().iterator();
	}

	static {
		// load all this shit
		one = new HashMap();
		two = new HashMap();
		
		one.put("United Kingdom, Ireland", new TwoDCoordinate(59.50300, 3.42251));
		two.put("United Kingdom, Ireland", new TwoDCoordinate(49.09007, -10.23846));
		one.put("Spain, Portugal", new TwoDCoordinate(43.80572, 9.62768));
		two.put("Spain, Portugal", new TwoDCoordinate(35.83357, -4.13928));
		one.put("France", new TwoDCoordinate(51.13956, 8.26144));
		two.put("France", new TwoDCoordinate(42.41141, -4.94009));
		one.put("Belgium, Germany, Holland", new TwoDCoordinate(51.13956, 8.26144));
		two.put("Belgium, Germany, Holland", new TwoDCoordinate(42.41141, -4.94009));
		one.put("Austria, Italy, Switzerland", new TwoDCoordinate(48.69744, 18.54906));
		two.put("Austria, Italy, Switzerland", new TwoDCoordinate(36.66042, 6.02277));
		one.put("Eastern Europe", new TwoDCoordinate(54.87738, 40.32211));
		two.put("Eastern Europe", new TwoDCoordinate(34.67268, 12.02408));
		one.put("Scandinavia", new TwoDCoordinate(71.32001, 31.56877));
		two.put("Scandinavia", new TwoDCoordinate(54.40720, 4.08139));
	}
}

package org.placelab.test;


import java.io.File;

import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.WiFiBeacon;
/**
 * 
 *
 */
public class JDBMMapperTest implements Testable {

	public String getName() { return "JDBMMapperTest";}
	
	public void runTests(TestResult result) {
		try {
			JDBMMapper map = new JDBMMapper(PlacelabProperties.get("placelab.datadir") + File.separator + "testwifimap");
			map.deleteAll();
			testSizeMap(map,0,result);	
			add(map,"1234");	
			testSizeMap(map,1,result);		
			add(map,"5678");	
			testSizeMap(map,2,result);
			testMapGet(map,result,"5678",true);
			testMapGet(map,result,"9999",false);
			testMapEmpty(map,2,result);
			map.deleteAll();
			
		} catch (Exception ex) {
			result.errorCaught(this,ex);
		} finally {
		}
	}

	private void testMapGet(
		JDBMMapper map,
		TestResult result,
		String id,
		boolean found) {
		Beacon b = map.findBeacon(id);
		result.assertTrue(this,found,b != null,"Ensure beacons are in the map");	
	}


	private void add(JDBMMapper map, String id) {
		WiFiBeacon b = new WiFiBeacon();
		b.setPosition(new TwoDCoordinate(47.6636333,-122.3083683));
		b.setSsid("blahblah");
		b.setId(id);
		map.putBeacon(b.getId(), b);
	}


	private void testMapEmpty(JDBMMapper map, int n, TestResult result) {
		int size = map.size();
		map.deleteAll();
		result.assertTrue(this,n,size,"Make sure the right number of beacons are removed");	
	}



	private void testSizeMap(JDBMMapper map, int n, TestResult result) {
		result.assertTrue(this,n,map.size(),"Make sure table is correct size");	
	}

}

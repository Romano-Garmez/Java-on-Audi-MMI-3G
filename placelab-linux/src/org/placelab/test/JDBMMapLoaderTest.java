package org.placelab.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.placelab.core.PlacelabProperties;
import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.MapLoader;
/**
 * 
 *
 */
public class JDBMMapLoaderTest implements Testable {

	public String getName() { return "MapLoaderTest";}
	
	public void runTests(TestResult result) {
		try {
			MapLoader ml = new MapLoader(new JDBMMapper(PlacelabProperties.get("placelab.datadir") + File.separator + "testmap"));
			testLoadMap(ml,result);		
			testEmptyMap(ml,result);
		} catch (Exception ex) {
			result.errorCaught(this,ex);
		}
	}
	
	private void testLoadMap(MapLoader ml, TestResult result) {
		StringBuffer sb = new StringBuffer();
		sb.append("47.6636333\t-122.3083683\tlambda\t004005b45c85\n"); // a piece of a real log
		sb.append("47.6636333\t-122.3083683\tlinksys\t000c41424432\n");
		sb.append("47.6637783\t-122.30837\t4714\t00095b5322ec\n");
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		try {
			result.assertTrue(this,3,ml.loadMap(is),"Make sure the right number of beacons are read");	
		} catch (Exception ex) {
			result.errorCaught(this,ex);
		}
	}
	
	private void testEmptyMap(MapLoader ml, TestResult result) throws IOException {
		int size = ((JDBMMapper)ml.getMapper()).size();
		
		((JDBMMapper)ml.getMapper()).deleteAll();
		result.assertTrue(this,3,size,"Make sure the right number of beacons are deleted");		
	}
}

package org.placelab.test;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.WiFiReading;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.WiFiBeacon;
import org.placelab.spotter.SpotterException;

/**
 * 
 *
 */
public class MapperTest implements Testable {

	public void testme() {
		Mapper mapper;
		
		//Beacon b=mapper.get(s);
		
	}
	public String getName() { return "MapperTest";}
	
	public void runTests(TestResult result) {
		testGetSingleBeacon(result);		
		testGetMultipleBeacons(result);		
	}
	
	private BeaconMeasurement getFakeMeasurement() {
		FakeSpotter fake=new FakeSpotter();
		fake.open();
		fake.fakeLab();
		try {
			return (BeaconMeasurement) fake.getMeasurement();
		} catch (SpotterException e) {
			// wont happen
			return null;
		}		
	}
	
	private void testGetSingleBeacon(TestResult result) {
		BeaconMeasurement meas=getFakeMeasurement();
		WiFiReading lab1100 = (WiFiReading) meas.getReading(0);
		
		result.assertTrue(this,"1100",lab1100.getSsid(),
			"sanity check to make sure we have the right AP for this test");
		Mapper mapper=new FakeMapper();
		WiFiBeacon b=(WiFiBeacon)mapper.findBeacon(lab1100.getId());
		TwoDCoordinate c = (TwoDCoordinate) b.getPosition();
		
		result.assertTrueDouble(this,FakeMapper.LAB_LATITUDE_AP_0,c.getLatitude(), //###
			"should be at the lab1");
		result.assertTrueDouble(this,FakeMapper.LAB_LONGITUDE_AP_0,c.getLongitude(), //###
			"should be at the lab2");
		
		
	}
	
	private void testGetMultipleBeacons(TestResult result) {
		BeaconMeasurement meas= getFakeMeasurement();
		Mapper mapper=new FakeMapper();
		WiFiBeacon beacon1100_0 = (WiFiBeacon)mapper.findBeacon(meas.getReading(0).getId());
		WiFiBeacon beacon1100_1 = (WiFiBeacon)mapper.findBeacon(meas.getReading(2).getId());
		WiFiBeacon beaconIntelGuest = (WiFiBeacon)mapper.findBeacon(meas.getReading(1).getId());
		
		result.sameObject(this,null,beaconIntelGuest,
			"We don't understand this beacon");
			
		result.differentObjects(this,null,beacon1100_0,
			"We better know that one");
			
		result.assertTrueDouble(this,FakeMapper.LAB_LATITUDE_AP_0,
			((TwoDCoordinate)beacon1100_0.getPosition()).getLatitude(), "lat of beacon 0");	
		result.assertTrueDouble(this,FakeMapper.LAB_LONGITUDE_AP_0,
			((TwoDCoordinate)beacon1100_0.getPosition()).getLongitude(), "long of beacon 0");	

		result.assertTrueDouble(this,FakeMapper.LAB_LATITUDE_AP_1,
			((TwoDCoordinate)beacon1100_1.getPosition()).getLatitude(), "lat of beacon 1");	
		result.assertTrueDouble(this,FakeMapper.LAB_LONGITUDE_AP_1,
		        ((TwoDCoordinate)beacon1100_1.getPosition()).getLongitude(), "long of beacon_1");	
	}
}


package org.placelab.jsr0179;


import org.placelab.test.TestResult;
import org.placelab.test.Testable;

import javax.microedition.location.AddressInfo;
import javax.microedition.location.Landmark;
import javax.microedition.location.QualifiedCoordinates;

public class FSLandmarkStoreTest implements Testable {

	public String getName() {
		return "FSLandmarkStoreTest";
	}
	
	public void runTests(TestResult result) throws Throwable {
		reconstructLandmarks(result);		
	}
	
	public void reconstructLandmarks (TestResult result) {
		
		String name = "landmark's name";
		String desc = "a detailed description of the landmark";
		QualifiedCoordinates coordinates = new QualifiedCoordinates(36.012, -122.3333, 57F, 1.0F, 2.0F);
		AddressInfo ai = new AddressInfo();
		
		ai.setField(AddressInfo.BUILDING_FLOOR, "floor");
		ai.setField(AddressInfo.BUILDING_NAME, "name");
		ai.setField(AddressInfo.BUILDING_ROOM, "room");
		ai.setField(AddressInfo.BUILDING_ZONE, "zone");
		ai.setField(AddressInfo.CITY, "city");
		ai.setField(AddressInfo.COUNTY, "county");
		ai.setField(AddressInfo.COUNTRY_CODE, "country code");
		ai.setField(AddressInfo.CROSSING1, "crossing one");
		ai.setField(AddressInfo.CROSSING2, "crossing two");
		ai.setField(AddressInfo.DISTRICT,	"district");
		ai.setField(AddressInfo.EXTENSION, "extension");
		ai.setField(AddressInfo.PHONE_NUMBER, "phone_number");
		ai.setField(AddressInfo.POSTAL_CODE, "postal code");
		ai.setField(AddressInfo.STATE, "state");
		ai.setField(AddressInfo.STREET, "street");
		ai.setField(AddressInfo.URL, "url");
		
		
		Landmark a = new Landmark(name, desc, coordinates, ai);
		Landmark b = FSLandmarkStore.landmarkFromString(FSLandmarkStore.landmarkToString(a));
		
		result.assertTrue(this, b.getName(), a.getName(), "In converting landmarks to strings and then back again, getName() does not return the same result.");
		result.assertTrue(this, b.getDescription(), a.getDescription(), "In converting landmarks to strings and then back again, getDescription() does not return the same result.");
		
		QualifiedCoordinates qca = a.getQualifiedCoordinates();
		QualifiedCoordinates qcb = b.getQualifiedCoordinates();
		
		result.assertTrue(this, Double.toString(qca.getLatitude()), Double.toString(qcb.getLatitude()), "In converting landmarks to strings and then back again, the QualifiedCoordinates are being corrupted.");
		result.assertTrue(this, Double.toString(qca.getLongitude()), Double.toString(qcb.getLongitude()), "In converting landmarks to strings and then back again, the QualifiedCoordinates are being corrupted.");
		result.assertTrue(this, Float.toString(qca.getAltitude()), Float.toString(qcb.getAltitude()), "In converting landmarks to strings and then back again, the QualifiedCoordinates are being corrupted.");
		result.assertTrue(this, Float.toString(qca.getHorizontalAccuracy()), Float.toString(qcb.getHorizontalAccuracy()), "In converting landmarks to strings and then back again, the QualifiedCoordinates are being corrupted.");
		result.assertTrue(this, Float.toString(qca.getVerticalAccuracy()), Float.toString(qcb.getVerticalAccuracy()), "In converting landmarks to strings and then back again, the QualifiedCoordinates are being corrupted.");
		
		AddressInfo aia = a.getAddressInfo();
		AddressInfo aib = b.getAddressInfo();
		
		int[] fields = new int[] { AddressInfo.BUILDING_FLOOR,
				AddressInfo.BUILDING_NAME, AddressInfo.BUILDING_ROOM,
				AddressInfo.BUILDING_ZONE, AddressInfo.CITY,
				AddressInfo.COUNTY, AddressInfo.COUNTRY_CODE,
				AddressInfo.CROSSING1, AddressInfo.CROSSING2,
				AddressInfo.DISTRICT, AddressInfo.EXTENSION,
				AddressInfo.PHONE_NUMBER, AddressInfo.POSTAL_CODE,
				AddressInfo.STATE, AddressInfo.STREET, AddressInfo.URL };
		
		for (int i=0;i<fields.length;i++) {
			result.assertTrue(this, aia.getField(fields[i]), aib.getField(fields[i]), "In converting landmarks to strings and then back again, the AddressInfo is being corrupted [ field = " + i + " ]");	
		}
		
		
	}
	
}
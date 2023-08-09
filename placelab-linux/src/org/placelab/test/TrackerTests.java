package org.placelab.test;


import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.IntersectionTracker;
import org.placelab.client.tracker.PositionTracker;
import org.placelab.client.tracker.Tracker;
import org.placelab.client.tracker.TwoDPositionEstimate;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BluetoothReading;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.WiFiReading;
import org.placelab.midp.GSMReading;

/**
 * 
 *
 */
public class TrackerTests extends Harness implements Testable {

	public final static double EPSILON=0.00001;
	
	public void setupAllTests(String[] IGNORED) {
		addTest(this);
	}

	public String getName() {
		return "TrackerTests";
	}

	public void runTests(TestResult result) {
		testCentroidTracker(result);
		testGPSTruthTracker(result);
		testPointXlate(result);
		testIntersectionTracker(result);
	}
	
    private BeaconMeasurement makeMeasurement(long timestamp, String id) {
		BeaconMeasurement m = new BeaconMeasurement(timestamp);
		m.addReading(new WiFiReading(id, "", -10, false, true));
		return m;
	}
	private TwoDCoordinate getEstimate(Tracker t) {
		return ((TwoDPositionEstimate)t.getEstimate()).getTwoDPosition();
	}
	public void testCentroidTracker(TestResult result) {
		TestMapper mapper = new TestMapper();
		mapper.addWiFiBeacon("aa:aa:aa:aa:aa:aa", 1.0000, 1.0000);
		mapper.addWiFiBeacon("bb:bb:bb:bb:bb:bb", 1.0010, 1.0020);
		mapper.addWiFiBeacon("cc:cc:cc:cc:cc:cc", 10.0, 20.0);  // far off lat,lon
		CentroidTracker centroid=new CentroidTracker(mapper);
		
		BeaconMeasurement m1 = makeMeasurement(0L, "aa:aa:aa:aa:aa:aa");
		m1.addReading(new WiFiReading("bb:bb:bb:bb:bb:bb", "", -10, false, true));
		BeaconMeasurement m2 = makeMeasurement(2L, "aa:aa:aa:aa:aa:aa");
		BeaconMeasurement m3 = makeMeasurement(3L, "bb:bb:bb:bb:bb:bb");
		BeaconMeasurement m4 = makeMeasurement(4L, "cc:cc:cc:cc:cc:cc");
		
		result.assertTrue(this,true, getEstimate(centroid).isNull(),
				"no info yet, just gives garbage");

		
		centroid.updateEstimate(m1);

		result.assertTrueDouble(this,1.0005,getEstimate(centroid).getLatitude(),EPSILON,
				"should be the average of the values of the first measurement");
		result.assertTrueDouble(this,1.0010,getEstimate(centroid).getLongitude(),EPSILON,
				"should be the average of the values of the first measurement");
			
		centroid.updateEstimate(m2);
		
		result.assertTrueDouble(this,1.0,getEstimate(centroid).getLatitude(),EPSILON,
				"should be the value of the third point");
		result.assertTrueDouble(this,1.0,getEstimate(centroid).getLongitude(),EPSILON,
				"should be the value of the third point");
			

		centroid.updateEstimate(m3);
		result.assertTrueDouble(this,1.0010,getEstimate(centroid).getLatitude(),EPSILON,
				"should be the position of bb:bb:bb:bb:bb:bb");
		result.assertTrueDouble(this,1.0020,getEstimate(centroid).getLongitude(),EPSILON,
				"because that was the only thing seen");
		

		centroid.updateEstimate(m4);
		result.assertTrueDouble(this,1.0010,getEstimate(centroid).getLatitude(),EPSILON,
				"position estimate should not have changed");
		result.assertTrueDouble(this,1.0020,getEstimate(centroid).getLongitude(),EPSILON,
				"position estimate should not have changed");		
	}
	
	public void testGPSTruthTracker(TestResult result) {
		double LONG=5.0;
		double LAT=100.0;
		
		GPSMeasurement meas=new GPSMeasurement(0L,new TwoDCoordinate(LAT,LONG));
		PositionTracker truth=new PositionTracker();
		result.assertTrueDouble(this,0.0,getEstimate(truth).getLatitude(),"init check1");
		result.assertTrueDouble(this,0.0,getEstimate(truth).getLongitude(),"init check2");
		
		result.assertTrue(this,true,truth.acceptableMeasurement(meas),
			"make sure our types are right");
		
		truth.updateEstimate(meas);
			
		result.assertTrueDouble(this,LAT,getEstimate(truth).getLatitude(),"lat is ok");
		result.assertTrueDouble(this,LONG,getEstimate(truth).getLongitude(),"long is ok");
	}
	
    public void testIntersectionTracker(TestResult result) {
        final double LAT=33.88638;
        final double LON=-123.872491;
        final String WiFiId1="aa:aa:aa:aa:aa:a1";
        final String WiFiId2="aa:aa:aa:aa:aa:a2";
        final String BluetoothId="bb:bb:bb:bb:bb:b1";
        final String GSMId="1234";
        final boolean debugprint=false;

        // coordinates
        TwoDCoordinate WiFiPos1, WiFiPos2, BluetoothPos, GSMPos, GPSPos;
        GSMPos=new TwoDCoordinate(LAT,LON);
        GSMPos.moveBy(0,50);
        WiFiPos1=new TwoDCoordinate(LAT,LON);
        WiFiPos2=new TwoDCoordinate(LAT,LON);
        WiFiPos2.moveBy(50,50);
        BluetoothPos=new TwoDCoordinate(LAT,LON);
        BluetoothPos.moveBy(5000,5000);
        GPSPos=new TwoDCoordinate(LAT,LON);
        GPSPos.moveBy(500,520);
        
        if(debugprint) {
            System.out.println("WiFi1 at " + WiFiPos1);
            System.out.println("WiFi2 at " + WiFiPos2);
            System.out.println("GSM at " + GSMPos);
            System.out.println("BT at " + BluetoothPos);
            System.out.println("GPS at " + GPSPos);
        }
        
        // mapper
        TestMapper m = new TestMapper();
        m.addWiFiBeacon(WiFiId1,WiFiPos1);
        m.addWiFiBeacon(WiFiId2,WiFiPos2);
        m.addBluetoothBeacon(BluetoothId,BluetoothPos);
        m.addGSMBeacon(GSMId,GSMPos);
        
        // create measurements
        BeaconMeasurement gsmMeas, wifiMeas, wifi2Meas, bluetoothMeas;
        GPSMeasurement gpsMeas;
        long now = System.currentTimeMillis();
        gsmMeas = new BeaconMeasurement(now);
        gsmMeas.addReading(new GSMReading(GSMId,"","","","",""));
        wifiMeas = new BeaconMeasurement(now);
        wifiMeas.addReading(new WiFiReading(WiFiId1, "",-57,false,true));
        wifi2Meas = new BeaconMeasurement(now);
        wifi2Meas.addReading(new WiFiReading(WiFiId1,"",-23,false,true));
        wifi2Meas.addReading(new WiFiReading(WiFiId2,"",-25,false,true));
        bluetoothMeas = new BeaconMeasurement(now);
        bluetoothMeas.addReading(new BluetoothReading("",BluetoothId,"","",0));
        gpsMeas = new GPSMeasurement(now,GPSPos);
        // run tests
        IntersectionTracker it = new IntersectionTracker(m);
        TwoDPositionEstimate est;
        TwoDCoordinate coord;
                
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrue(this,true,coord.isNull(),
		"null starting estimate for intersection tracker");
        if(debugprint) System.out.println("IT start: " + est);

        it.updateEstimate(gsmMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        double stdev = est.getStdDev();
        coord = est.getTwoDPosition();
		result.assertTrueDouble(this,GSMPos.getLatitude(),coord.getLatitude(),EPSILON,
		        "intersection tracker at gsm (lat)");
		result.assertTrueDouble(this,GSMPos.getLongitude(),coord.getLongitude(),EPSILON,
		        "intersection tracker at gsm (lon)");
        if(debugprint) System.out.println("IT add gsm: " + est);
		
        it.advanceTimeMillis(10000);
        est = (TwoDPositionEstimate) it.getEstimate();
		result.assertTrueDouble(this,GSMPos.getLatitude(),coord.getLatitude(),EPSILON,
        "intersection tracker still at gsm (lat)");
		result.assertTrueDouble(this,GSMPos.getLongitude(),coord.getLongitude(),EPSILON,
        "intersection tracker still at gsm (lon)");
		result.assertTrue(this,true, est.getStdDev()>stdev,
        "intersection tracker accuracy worsens over time");
        if(debugprint) System.out.println("IT add 10s: " + est);
        
        it.updateEstimate(wifiMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrueDouble(this,WiFiPos1.getLatitude(),coord.getLatitude(),EPSILON,
        "intersection tracker at WiFi1 despite GSM");
		result.assertTrueDouble(this,WiFiPos1.getLongitude(),coord.getLongitude(),EPSILON,
        "intersection tracker at WiFi1 despite GSM");
		result.assertTrue(this,true, est.getStdDev()<stdev,
        "intersection tracker accuracy got better with WiFi");
		if(debugprint) System.out.println("IT add wifi: " + est);
        
        it.updateEstimate(wifi2Meas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrue(this,true,coord.within(WiFiPos1, WiFiPos2),  
        "intersection tracker somewhere between wifi1 and wifi2");
        if(debugprint) System.out.println("IT add wifi2: " + est);
        
        it.updateEstimate(bluetoothMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrue(this,true,coord.within(BluetoothPos, WiFiPos1),
        "intersection tracker in middle of conflicting loci (lat)");
		result.assertTrue(this,true,est.getStdDev()>5000,
		"intersection tracker has enlarged error because of clashing readings");
		
		// JWS - these are dodgy since they were obtained from the tracker's results itself
		// and not an independent calcuation.  They will flag up any error though.
		result.assertTrueDouble(this,33.90946,coord.getLatitude(),EPSILON,
        "intersection tracker in middle of conflicting loci (lat)");
		result.assertTrueDouble(this,-123.83888,coord.getLongitude(),EPSILON,
        "intersection tracker in middle of conflicting loci (lon)");
		result.assertTrueDouble(this,7872.13182,est.getStdDev(),EPSILON,
		"intersection tracker has enlarged error because of clashing readings");
        if(debugprint) System.out.println("IT add non-fitting bt: " + est);
		
        it.advanceTimeMillis(100000L);
        it.updateEstimate(wifiMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrueDouble(this,WiFiPos1.getLatitude(),coord.getLatitude(),EPSILON,
        "intersection tracker at WiFi1 despite old measurements (lat)");
		result.assertTrueDouble(this,WiFiPos1.getLongitude(),coord.getLongitude(),EPSILON,
        "intersection tracker at WiFi1 despite old measurements (lon)");
        result.assertTrue(this,1,it.lociSize(), "intersection tracker should have got rid of old measurements");
        if(debugprint) System.out.println("IT advance to force cull and add wifi again: " + est);
        
        
        it.advanceTimeMillis(10000000L);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
        result.assertTrue(this,true,coord.isNull(), "intersection tracker should be at null pos if long time elapses");
        result.assertTrue(this,0,it.lociSize(), "intersection tracker should have got rid of all measurements");
        if(debugprint) System.out.println("IT add aeon: " + est);
        if(debugprint) System.out.println("IT add aeon: locisize " + it.lociSize());
		
        
		it.reset();
		it.updateEstimate(gpsMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrueDouble(this,GPSPos.getLatitude(),coord.getLatitude(),EPSILON,
		"at GPS pos in intersection tracker (lat)");
		result.assertTrueDouble(this,GPSPos.getLongitude(),coord.getLongitude(),EPSILON,
		"at GPS pos in intersection tracker (lon)");
        if(debugprint) System.out.println("IT add gps: " + est);		
		
		it.updateEstimate(bluetoothMeas);
        est = (TwoDPositionEstimate) it.getEstimate();
        coord = est.getTwoDPosition();
		result.assertTrue(this,true,coord.within(GPSPos,BluetoothPos), //###JAMES
		"within Bluetooth and GPS positions in intersection tracker (lat)");
        if(debugprint) System.out.println("IT add bt: " + est);	
        
    }

	public void testPointXlate(TestResult result) {
		double lat=5.5;
		double lon=4.0;
		TwoDCoordinate p = new TwoDCoordinate(lat, lon);
		
		double xDist = p.xDistanceFrom(new TwoDCoordinate(0,0));
		double yDist = p.yDistanceFrom(new TwoDCoordinate(0.5,0));
		
		p.moveBy(-xDist, -yDist);
		result.assertTrueDouble(this,0.0,p.getLongitude(),"shifted to origin lon");
		result.assertTrueDouble(this,0.5,p.getLatitude(),"not quite in lat");
		
		p.moveBy(xDist, yDist);
		result.assertTrueDouble(this,lat,p.getLatitude(),"start lat");
		result.assertTrueDouble(this,lon,p.getLongitude(),"start lon");
	}
	
	
}

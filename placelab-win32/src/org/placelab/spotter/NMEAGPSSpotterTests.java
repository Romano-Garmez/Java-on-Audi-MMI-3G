package org.placelab.spotter;

import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class NMEAGPSSpotterTests implements Testable, SpotterListener {

    FakeNMEAGPSSpotter gps;
    TestResult result;
    int seen = 0;
    Throwable oops = null;
    boolean done=false;
    
    public String getName() {
        return "NMEAGPSSpotterTests";
    }

    public void runTests(TestResult result) throws Throwable {
        this.result = result;
        setup();
        synchronized(this) {
	        try {
	            if (!done) this.wait();
	        } catch(InterruptedException ie) { }
        }
        result.assertTrue(this, 2, seen, "updates seen");
        if(oops != null) {
            result.errorCaught(this, oops);
        }
    }
    
    private void setup() throws Exception {
        gps = new FakeNMEAGPSSpotter("NMEAGPSSpotterTest");
        gps.addListener(this);
        gps.open();
        gps.startScanning();
    }

    public void gotMeasurement(Spotter sender, Measurement measurement) {
        seen++;
        try {
            GPSMeasurement m = (GPSMeasurement)measurement;
            TwoDCoordinate ftdc = (TwoDCoordinate)m.getPosition();
            double errorLat = Math.abs(47.66192167 - ftdc.getLatitude());
            double errorLon = Math.abs(-122.31618667 - ftdc.getLongitude());
            //System.out.println("lat " + ftdc.getLatitudeAsString() + " lon " + ftdc.getLongitudeAsString());
            result.assertTrue(this, true, (errorLat < .0001), "latitude");
            result.assertTrue(this, true, (errorLon < .0001), "longitude");
            result.assertTrue(this, true, m.isValid(), "validity");
            
            //Test Byte Compression
            GPSMeasurement byteTest = (GPSMeasurement) Measurement.fromCompressedBytes(m.toCompressedBytes());
            for(int i=0;i<GPSMeasurement.gpsTypes.length;i++) {
            	String a = m.getField(GPSMeasurement.gpsTypes[i]);
            	String b = byteTest.getField(GPSMeasurement.gpsTypes[i]);
				if(!a.equals(b)) {
					double d1 = Double.parseDouble(a);
					double d2 = Double.parseDouble(b);
					result.assertTrue(this, true, ((d1-d2) < .0001), GPSMeasurement.gpsTypes[i]);
				}
            }
        } catch (Throwable t) {
            oops = t;
            gps.stopScanning();
            gps.close();
            synchronized(this) {
            	done = true;
                this.notify();
            }
        }
        if(seen >= 2) {
            gps.stopScanning();
            gps.close();
            synchronized(this) {
            	done = true;
                this.notifyAll();
            }
        }
    }

	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}
}

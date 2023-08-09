package org.placelab.stumbler;

import java.util.Hashtable;

import org.placelab.core.Measurement;
import org.placelab.spotter.FakeNMEAGPSSpotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SyncSpotter;
import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class StumblerFunnelTests implements Testable, StumblerFunnelUpdateListener {
    
    private StumblerFunnel funnel;
    private SpotterExtension gps;
    private SpotterExtension dumb;
    private TestResult result;
    private Throwable oops = null;
    private int seen = 0;
    
    private class DumbMeasurement extends Measurement {
        private int value;
        DumbMeasurement(long timestamp, int value) {
            super(timestamp);
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    
    private class DumbStumblerSpotter extends SyncSpotter {

        int timesCalled = 0;
        
        public Measurement do_getMeasurement() {
            return new DumbMeasurement(System.currentTimeMillis(), timesCalled);
        }
        
        protected Measurement getMeasurementImpl() {
            timesCalled++;
            return do_getMeasurement();
        }
        public void open() { ; }
        public void close() { ; }
        public long nextScanInterval() { return Integer.MAX_VALUE; }
    }

    public String getName() {
        return "StumblerFunnelTests";
    }
    
    public void runTests(TestResult result) throws Throwable {
        this.result = result;
        setup();
        synchronized(this) {
	        try {
	            this.wait();
	        } catch(InterruptedException ie) { }
        }
        result.assertTrue(this, 4, seen, "updates seen");
        if(oops != null) {
            result.errorCaught(this, oops);
        }
    }
    
    private void setup() {
        gps = new SpotterExtension(new FakeNMEAGPSSpotter("stumbler"), true, 0);
        dumb = new SpotterExtension(new DumbStumblerSpotter(), true, 0);
        try {
			gps.open();
			dumb.open();
		} catch (SpotterException e) {
			e.printStackTrace();
		}
        funnel = new StumblerFunnel(2000);
        funnel.addTriggerSpotter(gps);
        funnel.addDependentSpotter(dumb);
        funnel.addUpdateListener(this);
        funnel.start();
    }
    
    public void stumblerUpdated(Hashtable measurements) {
        try {
            seen++;
            if(measurements.size() == 0) {
                // should never happen
                result.fail(this, "empty measurement hashtable");
            }
            DumbMeasurement dm = (DumbMeasurement)measurements.get(dumb.getSpotter());
            result.assertTrue(this, dm.getValue(), seen, "updates seen");
            if(seen <= 2) {
                result.assertTrue(this, measurements.containsKey(gps.getSpotter()), true, "fake gps existance");
            } else {
                result.assertTrue(this, measurements.containsKey(gps.getSpotter()), false, "fake gps (non)existance");
            }
            if(seen == 2) {
                // move to letting the timeout push the updates rather than letting the fake gps do it
            	gps.stopScanning();
                gps.close();
            } else if(seen == 4) {
                // ok, all done here
                funnel.shutdown();
                synchronized(this) {
                    this.notify();
                }
            }
        } catch (Throwable t) {
            oops = t;
            funnel.shutdown();
            synchronized(this) {
                this.notify();
            }
        }
    }

}

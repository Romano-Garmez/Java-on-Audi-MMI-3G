package org.placelab.spotter;

import java.io.File;

import org.placelab.core.Measurement;
import org.placelab.core.NetStumblerFileParser;
import org.placelab.core.PlacelabProperties;
import org.placelab.util.ns1.NS1Translator;

/**
 * LogSpotters are Spotters that replay stored Measurements from log files.
 */
public abstract class LogSpotter extends SyncSpotter {
	
	// for use with setRate
	public static final int NO_DELAY = -1;
	
	/**
	 * Creates a new LogSpotter from the given log file, the type of log
	 * is recognized and the correct LogSpotter for the file is returned.
	 * Currently, this method supports LogSpotters for the following formats:
	 * <ul>
	 * <li> NetStumbler text format (0.3.x and 0.4.x)
	 * <li> NetStumbler binary format (0.3.x and 0.4.x)
	 * <li> PlacelabStumbler log format (v 0, 1, 2)
	 * </ul>
	 */
    public static LogSpotter newSpotter(String logName) 
	{
		String dataLoc;
		File logFile = new File(logName);
		if (logFile.exists()) {
		    dataLoc = logName;
		} else {
		    dataLoc = PlacelabProperties.get("placelab.logdir") + 
			    File.separator + logName;
		}
		if (NetStumblerFileParser.isValidFile(dataLoc)) {
			return new NetStumblerLogSpotter(dataLoc);
		} else if(NS1Translator.isValidFile(dataLoc)) {
			return NS1Translator.newSpotter(dataLoc);
		} else if(PlacelabStumblerLogSpotter.isValidFile(dataLoc)) {
			return new PlacelabStumblerLogSpotter(dataLoc);
		} else {
			return new OldPlacelabStumblerLogSpotter(dataLoc);
		}
	}
	
    private Measurement nextMeasurement=null;
    private Measurement lastMeasurement=null;
    private boolean logIsFinished=false;
    private int rate=100;
    
    protected Measurement getMeasurementImpl()  throws SpotterException {
    	if (nextMeasurement==null) {
    		lastMeasurement = getMeasurementFromLog();
    	} else {
    		lastMeasurement = nextMeasurement;
    		nextMeasurement = null;
    	}
    	logIsFinished = (lastMeasurement==null);
    	//System.out.println("lis: " + logIsFinished);
    	return lastMeasurement;
    }
    protected long nextScanInterval() {
		if (lastMeasurement == null)
			return Integer.MIN_VALUE;
		try {
			nextMeasurement = getMeasurementFromLog();
		} catch (SpotterException e) {
			e.printStackTrace();
			return 0;
		}
		if (nextMeasurement == null)
			return 0;
		
		if (rate == NO_DELAY) 
			return 0;
		
		return (nextMeasurement.getTimestamp() - lastMeasurement.getTimestamp())
				* 100 / rate;
	}
    
    /**
     * @return <code>true</code> when a log file has no more Measurements remaining
     * <code>false</code> if there still remain Measurements yet to be consumed.
     * Calling {@link Spotter#getMeasurement()} consumes a single Measurement.
     */
    public boolean logIsFinished() {
        return logIsFinished;
    }
    
    /**
     * For doing continous scanning, set the rate at which the Measurements should
     * be sent off to registered {@link SpotterListener} objects as a percentage of
     * real-time.
     */
    public void setRate(int percentage) {
    	this.rate = percentage;
    }
    public int getRate() { return rate; }

    /**
     * Returns a single Measurement from the log, or null if there are no Measurements
     * remaining.  This method is only of interest to implementers of LogSpotters, users
     * should instead call {@link Spotter#getMeasurement()}
     * @throws SpotterException if an error is encountered in the log
     */
    public abstract Measurement getMeasurementFromLog() throws SpotterException;

}

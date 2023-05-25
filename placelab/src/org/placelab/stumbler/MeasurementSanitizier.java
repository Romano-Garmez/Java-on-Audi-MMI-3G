package org.placelab.stumbler;

import java.text.DecimalFormat;

import org.placelab.core.StumblerMeasurement;
import org.placelab.core.TwoDCoordinate;

/*
 * 
 * 
 *
 * TODO 1. recording maximum consecutive time segment
 *      2. netstumbler sometimes logs measurements non-chronologically
 *         needs fix, current version assumes measurements are taken 
 *         chornologically
 */
public class MeasurementSanitizier {
	private StumblerMeasurement prev;

	private StumblerMeasurement curr;

	boolean verbose;

	long currentTimeMinute;
	public static final long DEFULAT_MAX_TIMEGAP = 5000;
	public static final long DEFULAT_MIN_TIMEGAP = 50;
	private long maxTimeGap = DEFULAT_MAX_TIMEGAP;
	private long minTimeGap = DEFULAT_MIN_TIMEGAP;
	private TwoDCoordinate upperleft;
	private TwoDCoordinate lowerright;
	boolean checkRange = false;
	
	public double setPositionRange(TwoDCoordinate ul, TwoDCoordinate lr)
	{
		checkRange = true;
		upperleft = ul;
		lowerright = lr;
		return ul.distanceFrom(lr);
	}
	
	public MeasurementSanitizier(long minTimeGap, long maxTimeGap, double speedLimit) {
		if (minTimeGap > maxTimeGap)
			throw new IllegalArgumentException("min timeGap = " + minTimeGap
					+ " > max timeGap " + maxTimeGap);
		reset(minTimeGap, maxTimeGap, speedLimit);

	}

	public void reset() {
		reset(minTimeGap, maxTimeGap, speedLimit);
		checkRange = false;
	}
	
	public void reset(long minTimeGap, long maxTimeGap, double speedLimit) {
		currentTimeMinute = System.currentTimeMillis() / (60 * 1000);
		prev = curr = null;
		this.maxTimeGap = maxTimeGap;
		this.minTimeGap = minTimeGap;
		this.speedLimit = speedLimit;
		numOfErrors = 0;
		numOfMeasurements = 0;
	}

	public static final int ERROR_OK = 0;
	public static final int ERROR_MIN_TIMEGAP = 1;
	public static final int ERROR_MAX_TIMEGAP = 2;
	public static final int ERROR_CLOCK_SKEW = 3;
	public static final int ERROR_DUPLICATE = 4;
	public static final int ERROR_MAX_SPEED = 5;
	public static final int ERROR_POS_RANGE = 6;
	private int numOfErrors = 0;
	private int numOfMeasurements = 0;
	private double speedLimit = 65.0; //mph
	private int error = ERROR_OK;

	public void report() {
		System.out.println("\nMeasurement Santiy Check:" +
				" time gap min = " 	+ minTimeGap + ", max = " + maxTimeGap + "(ms)" +
				", speed limit=" + speedLimit + "mph");
		System.out.println(numOfMeasurements + " checked, " + numOfErrors + " errors");
	}
	
	private String errorString = "";

	public int getError() {
		return error;
	}

	public String getErrorString() {
		return errorString;
	}

	boolean setError(int error, final String msg) {
		this.error = error;
		errorString = msg;
		System.err.println("Measurement Sanitizer: " + msg);
		numOfErrors++;
		return false;
	}
	
	public boolean check(StumblerMeasurement m) {
		error = ERROR_OK;
		errorString = "";
		
		++numOfMeasurements;
		
		TwoDCoordinate c = (TwoDCoordinate)m.getPosition();
		if (!c.within(upperleft, lowerright)) 
			return setError(ERROR_POS_RANGE,
					"Position exceeds range [ " + upperleft.toString()  + " - " + lowerright.toString() + "] :" + c.toString());

		if (m.getTimestamp() >= currentTimeMinute * 60 * 1000) {
			return setError(ERROR_CLOCK_SKEW,
					"clock skewed: measurement taken in the future");
		}
		
		if (prev == null) {
			prev = m;
			curr = null;
			return true;
		}

		curr = m;

		long prev_ts = prev.getTimestamp();
		long curr_ts = curr.getTimestamp();
		long timeGap = curr_ts - prev_ts;


		if (prev == curr) {
			setError(ERROR_DUPLICATE,
					"measurement is identical (object reference)");
		} else if (timeGap < minTimeGap) {
			setError(ERROR_MIN_TIMEGAP, "measurement timeGap is " + timeGap + " ms");
			//Date d = new Date(prev_ts);
			//System.err.println("prev data=" + d.toString());
			//d.setTime(curr_ts);
			//System.err.println("curr data=" + d.toString());
			//+ " ms" + prev_ts + " " + curr_ts);
		} else if (timeGap > maxTimeGap) {
			setError(ERROR_MAX_TIMEGAP, "measurement timeGap is " + timeGap + "ms");
					//+ " ms" + prev_ts + " " + curr_ts);
		} else {
			TwoDCoordinate cp = (TwoDCoordinate) curr.getPosition();
			TwoDCoordinate pp = (TwoDCoordinate) prev.getPosition();
			double hour = timeGap / (60*60*1000.0D);
			double mile = (cp.distanceFrom(pp) / 1000)/1.65D;
			double mph = mile/hour;
			if (mph > speedLimit) {
				DecimalFormat f = new DecimalFormat();
				f.setMaximumFractionDigits(2);
				setError(ERROR_MAX_SPEED, "speeding! " + f.format(mph) +" mph");
			}
		}

		prev = curr;
		curr = null;
		return error == ERROR_OK;
	}
}
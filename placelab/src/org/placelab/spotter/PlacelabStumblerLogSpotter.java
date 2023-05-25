
/*
 * Created on Jun 1, 2004
 */
package org.placelab.spotter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Coordinate;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.StumblerMeasurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.core.WiFiReading;
import org.placelab.stumbler.LogWriter;
import org.placelab.util.Cmdline;
import org.placelab.util.Logger;
import org.placelab.util.NumUtil;
import org.placelab.util.StringUtil;

public class PlacelabStumblerLogSpotter extends LogSpotter {
	private String filename=null;
	private InputStream is=null;
	private BufferedReader reader = null;
	private GPSMeasurement prevGPSMeas, nextGPSMeas;
	private boolean beaconsAvailable;
	
	private HashMap savedLine;
	private LinkedList cache;
	private boolean outputStumblerMeasurements = true; // output StumblerMeasurements or raw GPS and BeaconMeasurements
	private int gpsThreshold = GPS_DEFAULT_THRESHOLD;
	private int gpsInterpolationMode=GPS_THRESHOLD;

	public static int GPS_INTERPOLATE=1;
	public static int GPS_THRESHOLD=2;
	public static int GPS_NONE=3;
	
	public static int GPS_DEFAULT_THRESHOLD = 5000;
		
	public PlacelabStumblerLogSpotter(String file) {
		filename = file;
	}
	
	public PlacelabStumblerLogSpotter(InputStream in) {
		is = in;
	}
	
	public void open() throws SpotterException {
		try {
			if (filename != null) {
				reader = new BufferedReader(new FileReader(filename));
			} else {
				reader = new BufferedReader(new InputStreamReader(is));
			}
		} catch (FileNotFoundException e) {
			throw new SpotterException(e);
		}

		if (reader == null) {
			throw new SpotterException("No reader available");
		}
		
		prevGPSMeas = nextGPSMeas = null;
		savedLine = null;
		cache = new LinkedList();
		
		/* start with beaconsAvailable = true to avoid generating a single empty reading for the first GPS line */
		beaconsAvailable = true;
	}
	public void close() throws SpotterException {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				throw new SpotterException(e);
			}
			reader = null;
		}
	}
	public void setGPSMethod(int method, int thresholdMillis) {
		gpsInterpolationMode = method;
		gpsThreshold = thresholdMillis; 
	}
	public int getGPSMethod() { return gpsInterpolationMode; }
	public int getGPSThreshold() { return gpsThreshold; }
	

    /**
     * @return Returns true if we are outputting StumblerMeasurements instead of raw
     * GPS and Beacon Measurements.
     */
    public boolean isOutputStumblerMeasurements() {
        return outputStumblerMeasurements;
    }
    /**
     * @param outputStumblerMeasurements Set whether to output StumblerMeasurements
     * instead of raw GPS and Beacon Measurements.
     */
    public void setOutputStumblerMeasurements(boolean outputStumblerMeasurements) {
        this.outputStumblerMeasurements = outputStumblerMeasurements;
    }

    private BeaconMeasurement newBeaconMeasurement(long timestamp, LinkedList list) {
		BeaconReading wrs[] = new BeaconReading[list.size()];
		int i=0;
		for (Iterator it=list.iterator(); it.hasNext(); ) {
			wrs[i++] =(BeaconReading) it.next();
		}
		return new BeaconMeasurement(timestamp, wrs);
	}

	public Measurement getMeasurementFromLog() throws SpotterException {

		if (reader == null && cache.isEmpty()) {
		    return null;
		}
		
		if (outputStumblerMeasurements) {
			if (gpsInterpolationMode == GPS_INTERPOLATE) {
				return getInterpolatedMeasurement();
			}
			else if (gpsInterpolationMode == GPS_THRESHOLD) {
				return getThresholdedMeasurement(true);
			} else {
				return getThresholdedMeasurement(false);
			}
		} else {
		    return getNextMeasurement();
		}
	}
	

	/* this returns either a GPSMeasurement or a BeaconMeasurement object */
	private Measurement getNextMeasurement() throws SpotterException {
		if (reader==null && savedLine==null) return null;
		
		LinkedList list=new LinkedList();
		long currentTimestamp = 0;
		String currentType = null;
		
		String line = null;
		while (true) {
			HashMap allFields;
			if (savedLine == null) {
				try { line = reader.readLine(); }
				catch (Throwable ex) {
					throw new SpotterException(ex);
				}

				if (line == null) {
					try { reader.close(); }
					catch (IOException ex) { }
					reader = null;
					if (list.isEmpty()) return null;
					else return newBeaconMeasurement(currentTimestamp, list);
				}
			
				allFields = StringUtil.storageStringToHashMap(line);
				if (allFields==null) continue;
			} else {
				allFields = savedLine;
				savedLine = null;
			}

		    String type = (String)allFields.get(Types.TYPE);
			String timeStr = (String)allFields.get(Types.TIME);
			if (type==null || timeStr==null) continue;
			long time=0;
			try {
				time = Long.parseLong(timeStr);
			} catch (NumberFormatException e) {
				continue;
			}
			
			if ((currentType != null) && (/*!type.equals(currentType) ||*/ (time != currentTimestamp))) {
		    	/* we are done with this group of readings */
		    	savedLine = allFields;
//		    	System.out.println("Doing " + type + " vs " + currentType);
		    	return newBeaconMeasurement(currentTimestamp, list);
		    }

		    if (type.equals(Types.GPS)) {
		        String status = (String)allFields.get(Types.STATUS);
				if (status != null && status.equalsIgnoreCase("a")) {
				    // in v2 logs lat and lon are stored as regular signed
					// double values
				    // not NMEA lat and lons.
					String lat = (String) allFields.get(Types.LATITUDE);
					String lon = (String) allFields.get(Types.LONGITUDE);
//			    	System.out.println("GPS triggered ");
					
					return new GPSMeasurement(time, Types.newCoordinate(lat, lon), allFields);
				} else {
				    return new GPSMeasurement(time, Types.newCoordinate(), allFields);
				}
		    }
		    BeaconReading reading = null;
		    try {
		    	reading = Types.newReading(allFields);
		    } catch (IllegalArgumentException ex) {
		    	// ignore bad line
		    	continue;
		    }
		    if (reading == null) continue;

		    if (currentType==null) {
		    	currentType = type;
		    	currentTimestamp = time;
		    }
		    list.add(reading);
		}		
	}
	
	private StumblerMeasurement getThresholdedMeasurement(boolean useThresholding) throws SpotterException {
		Measurement m = getNextMeasurement();
		if (m==null) {
			if (!beaconsAvailable && prevGPSMeas != null) { 
				return new StumblerMeasurement(prevGPSMeas.getTimestamp(), 
						prevGPSMeas.getPosition());
			} else {
				return null;
			}
		}
		
		/* the measurement is either a GPSMeasurement or a BeaconMeasurement */
		if (m instanceof BeaconMeasurement) {
			BeaconMeasurement meas = (BeaconMeasurement) m;
			Coordinate pos;
			if (prevGPSMeas != null) {
				if (useThresholding) {
					pos = (meas.getTimestamp() - prevGPSMeas.getTimestamp() > gpsThreshold ?
							Types.newCoordinate() : prevGPSMeas.getPosition());
				} else {
					pos = prevGPSMeas.getPosition();
					prevGPSMeas = null;
				}
			} else {
				pos = Types.newCoordinate();
			}
			beaconsAvailable = true;
			return new StumblerMeasurement(meas.getTimestamp(), pos, meas.getReadings());
		} else {
			GPSMeasurement gpsMeas = (GPSMeasurement) m;
			if (beaconsAvailable==true) {
				// there were beacons available between this GPS reading and the previous one
				beaconsAvailable = false;
				prevGPSMeas = gpsMeas;
				return getThresholdedMeasurement(useThresholding);
			} else {
				// there were no beacons available between this GPS reading 
				// and the previous one
				prevGPSMeas = gpsMeas;
				return new StumblerMeasurement(prevGPSMeas.getTimestamp(), 
						prevGPSMeas.getPosition());
			}
		}
	}

	private Coordinate interpolateGPS(long thisTimestamp) {
		if (prevGPSMeas==null) return Types.newCoordinate();
		if (nextGPSMeas==null) return prevGPSMeas.getPosition().createClone();
		
		double frac = ((double)(thisTimestamp - prevGPSMeas.getTimestamp()))/
			((double)(nextGPSMeas.getTimestamp()-prevGPSMeas.getTimestamp()));
		double lat = ((TwoDCoordinate)prevGPSMeas.getPosition()).getLatitude() + 
			frac * (((TwoDCoordinate) nextGPSMeas.getPosition()).getLatitude() - 
					((TwoDCoordinate) prevGPSMeas.getPosition()).getLatitude());
		double lon = ((TwoDCoordinate)prevGPSMeas.getPosition()).getLongitude() + 
			frac * (((TwoDCoordinate) nextGPSMeas.getPosition()).getLongitude() - 
					((TwoDCoordinate) prevGPSMeas.getPosition()).getLongitude());
		return new TwoDCoordinate(lat, lon);
	}
	private Measurement getInterpolatedMeasurement() {
		if (cache.isEmpty()) {
			gatherReadingsForInterpolation();
			if (cache.isEmpty()) return null;

			GPSMeasurement saved = prevGPSMeas;
			if (prevGPSMeas==null && nextGPSMeas==null) {
				Measurement first = (Measurement)cache.getFirst();
				if (first instanceof GPSMeasurement) {
					prevGPSMeas = (GPSMeasurement) first;
					cache.removeFirst();
				}
			} else {
				prevGPSMeas = nextGPSMeas;
			}
			
			Measurement last = (Measurement)cache.getLast();
			if (last instanceof GPSMeasurement) {
				nextGPSMeas = (GPSMeasurement) last;
				cache.removeLast();
			} else {
				/* extrapolate from the previous pair of GPS readings */
				GPSMeasurement tmp = prevGPSMeas;
				prevGPSMeas = saved;
				nextGPSMeas = new GPSMeasurement(last.getTimestamp(), 
						interpolateGPS(last.getTimestamp()));
				prevGPSMeas = tmp;
			}
		
			if (cache.isEmpty()) {
				// there were no BeaconMeasurements in between these two GPS measurements
				return new StumblerMeasurement(prevGPSMeas.getTimestamp(), 
						prevGPSMeas.getPosition());
			}
		}
		
		/* grab the next BeaconMeasurement from the cache */
		BeaconMeasurement meas = (BeaconMeasurement)cache.removeFirst();
		Coordinate pos = interpolateGPS(meas.getTimestamp());
		return new StumblerMeasurement(meas.getTimestamp(), pos, meas.getReadings());	
	}
	
	private void gatherReadingsForInterpolation() {
		/* cache everything until the next GPS line */
		
		while (true) {
			Measurement m = null;
			try {
				m = getNextMeasurement();
			} catch (SpotterException ex) {
				ex.printStackTrace();
				continue;
			}
				
			if (m==null) return;
			
			if (m instanceof GPSMeasurement && 
					(prevGPSMeas != null || nextGPSMeas != null || !cache.isEmpty())) {
				cache.add(m);
				return;
			}
			cache.add(m);
		}
	}
	
	public static boolean isValidFile(String file) {
		try {
			InputStream is = new FileInputStream(file);
			BufferedReader br = 
				new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			br.close();
			is.close();
		    int version = LogWriter.getLogVersion(line);
		    if(version >= 2) {
		        // log versions 2 and greater have a preamble on the
		        // first line, all others have data on the first line
		    	return true;
		    } else {
		    	return false;
		    }
		} catch (IOException ex) {
			return false; 
		}
	}


	
	// This main will convert a placelab log format into netwstumbler's old
	// format
	public static void main(String args[]) {
		Cmdline.parse(args);
		
		String log = Cmdline.getArg("log");
		String out = Cmdline.getArg("outputfile");
		try {
			PlacelabStumblerLogSpotter ps = 
				new PlacelabStumblerLogSpotter(Cmdline.getArg("log"));
			String outputfile = Cmdline.getArg("outputfile");
			PrintWriter pw = (outputfile != null ? 
					new PrintWriter(new BufferedWriter(new FileWriter(outputfile,false))) :
						new PrintWriter(System.out));
			try {
				ps.open();
			} catch (SpotterException e1) {
				e1.printStackTrace();
				return;
			}


		boolean headerPrinted = false;
  			
				Calendar c = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
			while (true) {
				StumblerMeasurement sm=null;
				try {
					sm = (StumblerMeasurement)ps.getMeasurement();
				} catch (SpotterException e2) {
					e2.printStackTrace();
					continue;
				}
				if(sm == null) {
				    break;
				}
				Logger.println(Integer.toString(sm.numberOfReadings()), Logger.HIGH);
				for (int i = 0; i < sm.numberOfReadings(); i++) {
						WiFiReading wr = (WiFiReading)sm.getReading(i);
						Date d = new Date(sm.getTimestamp());
						c.setTime(d);
						if (!headerPrinted) {
							headerPrinted = true;
							
							pw.println("# $Creator: Network Stumbler Version 0.3.30");
							pw.println("# $Format: wi-scan with extensions");
							pw.println("# Latitude\tLongitude\t( SSID )\tType\t( BSSID )\tTime (GMT)\t[ SNR Sig Noise ]\t# ( Name )\tFlags\tChannelbits\tBcnIntvl");
							pw.println("# $DateGMT: " + c.get(Calendar.YEAR) + "-" + blah(c.get(Calendar.MONTH) + 1) + "-" + blah(c.get(Calendar.DAY_OF_MONTH)));
						}
						String sss;
						if (wr.getRssi() == 0) {
							sss = "[ 0 0 0]";
						} else {
							sss = "[ 10 " + (-wr.getRssi()) + " " + ((-wr.getRssi())-10) + " ]";
						}
						pw.println(
						        //((TwoDCoordinate)sm.getPosition()).getLatitudeAsString()
								// + "\t" +
						        //((TwoDCoordinate)sm.getPosition()).getLongitudeAsString()
								// + "\t" +
							jingoLat(((TwoDCoordinate)sm.getPosition()).getLatitude()) + "\t" +
							jingoLon(((TwoDCoordinate)sm.getPosition()).getLongitude()) + "\t" +
							"( " + wr.getSsid() + " )" + "\t" + (wr.getIsInfrastructure() ? "BBS" : "adhoc") + 
							"\t( " + wr.getBssid() + " )" + "\t" +
							blah(c.get(Calendar.HOUR_OF_DAY)) + ":" + blah(c.get(Calendar.MINUTE)) + ":" + blah(c.get(Calendar.SECOND)) + " (GMT)" + "\t" +
							sss + "\t" +
							"# ( )" + "\t" +
							"0051" + "\t" +
							"0040" + "\t" +
							"0");
// N 55.8614150 W 4.2486717 ( bcwgroup ) BBS ( 00:41:05:cc:8d:9f ) 08:40:40
// (GMT) [ 0 0 0 ] # ( ) 0051 0040 0
				}
				pw.flush();
			} 
		} catch (IOException e) {
			System.err.println("error: " + e.getMessage());
		}
	}

	
	public static String jingoLat(double d) {
	    if(Double.isNaN(d)) {
	        return "N " + "0.0";
	    }
		if (d < 0) {
			return "S " + NumUtil.doubleToString(-d, 7);
		} else {
			return "N " + NumUtil.doubleToString(d, 7);
		}
	}
	public static String jingoLon(double d) {
	    if(Double.isNaN(d)) {
	        return "E " + "0.0";
	    }
		if (d < 0) {
			return "W " + NumUtil.doubleToString(-d, 7);
		} else {
			return "E " + NumUtil.doubleToString(d, 7);
		}
	}
	
	
	public static String blah(int i) {
		if (i >=10) {
		  return "" + i;
		} else {
			return "0" + i;
		}
	}

	public static void testmain(String args[]) throws SpotterException {
		PlacelabStumblerLogSpotter s = new PlacelabStumblerLogSpotter("/home/yatin/tmp/seattle.log");
		s.setGPSMethod(GPS_THRESHOLD, 2000);
		s.open();
		for (int i=0; i < 3; i++) {
			StumblerMeasurement m = (StumblerMeasurement)s.getMeasurement();
			System.out.println("Measurement "+(i+1)+" has "+m.numberOfReadings()+" readings");
		}
		s.close();
	}
}

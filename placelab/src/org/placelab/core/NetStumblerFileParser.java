package org.placelab.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Calendar;

import org.placelab.spotter.LogSpotter;
import org.placelab.util.Logger;
import org.placelab.util.StringUtil;

/**
 * NetStumblerFileParser will read an exported tab-separated text-log from NetStumbler
 * and read it line by line. Each line corresponds to a NetStumblerMeasurement.
 * We take each measurement and add it to a List.
 * 
 * There is a trick we do to get the absolute timestamp. We get the date from the 
 * 3rd line of the logfile and then parse out the year, month and day
 * 
 * Below is an example of a Netstumbler output:
 * 
 * <pre>
 * # $Creator: Network Stumbler Version 0.3.30
 * # $Format: wi-scan with extensions
 * # Latitude 	Longitude		( SSID )	Type	( BSSID )	Time (GMT)	[ SNR Sig Noise ]	...
 * $ $Date GMT: 2004-04-06
 * 
 * N 47.6619200	E122.3162617	( default )	BBS		( 00:80:c8:19:5a:1d )	01:25:07 (GMT)	[ 35 87 52 ]
 * N 47.6619200	E122.3162617	( linksys-g )	BBS	( 00:0c:41:14:a1:92 )	01:25:07 (GMT)	[ 23 73 50 ]
 * N 47.6619200	E122.3162617	( 1100 )	BBS		( 00:0d:28:88:c7:56 )	01:25:07 (GMT)	[ 14 72 53 ]
 * 
 * </pre>
 */

public class NetStumblerFileParser extends LogSpotter{
	public static boolean IgnoreDuplicateReading = false;
	private InputStream is;
	private LineNumberReader br;
	private final int NUM_HEADER_LINES = 4;
	private int year;
	private int month;
	private int day;
	private long lastTimestamp;
	Calendar cal;
	
	private NetStumblerReading latestReading=null;
	
	public static final int NETSTUMBLER_RSSI_ADJUSTMENT = Types.NETSTUMBLER_RSSI_ADJUSTMENT;

	public NetStumblerFileParser(String tracefile) throws IOException {
		//this.skipDuplicateReading = skipDuplicateReading;
		is = new FileInputStream(tracefile);
		br = new LineNumberReader( new InputStreamReader(is) );

		/* check if there is a header */
		br.mark(4);
		int ch = br.read();
		boolean shouldParseHeader = (((char)ch)=='#');
		br.reset();
		if (shouldParseHeader) parseHeader();
		else { year=1900; month=0; day=1; }

		cal = Calendar.getInstance();
		cal.set(year, month-1, day, 0, 0, 0);
		lastTimestamp = normalize(cal.getTime().getTime());
	}
	
	
	public void open() {} // here for compatibility with the LogSpotter interface
	
	public void close() {
		try {
		if (br != null) { br.close(); br = null; }
		if (is != null) { is.close(); is = null; }
		} catch (IOException ex) {
			System.out.println("Error closing NetStumblerFileParser. Exiting.");
		}
	}

	private long normalize(long timestamp) {
		return (timestamp/1000L) * 1000L;
	}
	private void parseHeader() throws IOException {
		String line=null;
		for (int i = 0; i < NUM_HEADER_LINES; i++) {
			// first three lines are part of the header
			line = br.readLine();
			if (line == null) {
				throw new IOException
					("Not a valid NetStumbler text log file");
			}
		}
		// parse the fourth line which has the Date
		parseNetStumblerDate(line);
	}
	private void parseNetStumblerDate(String line)
	{
		// incoming date string is of the form # $DateGMT: yyyy-mm-dd
		line = line.substring(2, line.length());
		int beginIndex = line.indexOf(' ', 0);
		line = line.substring( beginIndex+1, line.length() );
		int firstdashIndex = line.indexOf('-');
		year = Integer.parseInt( line.substring(0, firstdashIndex) );
		int seconddashIndex = line.indexOf('-', firstdashIndex+1);
		month = Integer.parseInt( line.substring(firstdashIndex+1, 
							 seconddashIndex) );
		day = Integer.parseInt( line.substring(seconddashIndex+1, 
						       line.length()) );
	}

	public NetStumblerReading readLine() throws IOException {
		String[] sarr;
		String line;
		double lat, lon;
		int latmultiplier = 0, lonmultiplier = 0;
		int rssi;

		if (latestReading != null) {
			NetStumblerReading r = latestReading;
			latestReading = null;
			return r;
		}
		
		if (br==null) {
			return null;
		}
		line = br.readLine();
		
		if (line==null) {
			br.close();
			is.close();
			br = null;
			is = null;
			return null;
		}

		if (line.trim().equals("")) {
			throw new IOException("line#" + br.getLineNumber() + ": Invalid empty line in NetStumbler text file");
		}
		
		while (line.trim().startsWith("#")) {
			line = br.readLine();
		}

		//break it up according to the tab separation
		sarr = StringUtil.split(line);
		
		String timestamp = null; 
		try {
			timestamp = sarr[5];
			timestamp = timestamp.substring(0, 8);
		} catch (Exception e) {
			System.out.println(line);
			e.printStackTrace();
		}
		

		
		//parse the timestamp string into integers
		int hour = Integer.parseInt(timestamp.substring(0, 2));
		int minute=Integer.parseInt(timestamp.substring(3, 5));
		int second=Integer.parseInt(timestamp.substring(6, 8));
		// January is 0! 
		cal.set(year, month-1, day, hour, minute, second);
		long ts = normalize(cal.getTime().getTime());

		if (ts < lastTimestamp) {
			/*
			 * Netstumbler rolls hh:mm:ss overnight without specifying the date 
			 * (and that is HIS bug) 
			 */
			final long numOfMillisPerDay =1000*60*60*24; 
			ts = ts + numOfMillisPerDay;
			cal.setTimeInMillis(ts);
			month = cal.get(Calendar.MONTH);
			++month; // January is 0
			day = cal.get(Calendar.DAY_OF_MONTH);
		}
		lastTimestamp = ts;
		
		// get the lat and lon
		String latitude = sarr[0];
		String longitude = sarr[1];
		TwoDCoordinate twodc = (TwoDCoordinate)Types.newCoordinate(latitude, longitude);
		String ssid = sarr[2];
		if ((ssid.length() > 4) && (ssid.charAt(0) == '(')) {
			ssid = ssid.substring(2, ssid.length() - 2);
		}
		
		String bssid = sarr[4];
		if ((bssid.length() > 4) && (bssid.charAt(0) == '(')) {
			bssid = bssid.substring(2, bssid.length() - 2);
		}
		
		String sq = sarr[6]; //sq = signal quality
		sq = sq.substring(2, sq.length()-2); // trim the [ ]
		String [] sqColumn = StringUtil.split(sq, ' ');
		
		if (sqColumn.length != 3)
			throw new IOException("line#" + br.getLineNumber() + ": error parsing signal quality values\n" + line);
		
		int snr = Integer.parseInt(sqColumn[0]);
		int signal = Integer.parseInt(sqColumn[1]);
		int noise = Integer.parseInt(sqColumn[2]);
		
		// we check this here because we only input the synthesized Rssi into WiFiReading constructor
		if (noise <  0 ||
			noise >= NETSTUMBLER_RSSI_ADJUSTMENT ) 
			throw new IOException("line#" + br.getLineNumber() + ": invalid snr value\n" + line);
		
		if (noise <  0 ||
			noise >= NETSTUMBLER_RSSI_ADJUSTMENT ) 
			throw new IOException("line#" + br.getLineNumber() + ": invalid noise value\n" + line);

		if (signal <  0 || 
			signal >= NETSTUMBLER_RSSI_ADJUSTMENT)
			throw new IOException("line#" + br.getLineNumber() + ": invalid signal value\n" + line);

		rssi = signal - NETSTUMBLER_RSSI_ADJUSTMENT;

		try {
		    if(twodc.getLatitude() == 0.0 && twodc.getLongitude() == 0.0) {
		        twodc = TwoDCoordinate.NULL;
		    }
			NetStumblerReading n = new NetStumblerReading(ts, twodc, bssid, ssid, rssi, false, true);
			return n;
		} catch (IllegalArgumentException e) {
			throw new IOException("line#" + br.getLineNumber() + ": " + e.getMessage() + "\n" + line);
		}
	}
	
	public StumblerMeasurement readMeasurement() throws IOException {
		StumblerMeasurement meas = null;
		NetStumblerReading reading;
		while (true) {
			try {
				reading = readLine();
			} catch (IOException e) { //we should havet netstumbler format exception ... 
				Logger.println(e.getMessage(), Logger.HIGH);
				continue;
			} 
			
			if (reading == null) break;
			
			if (meas != null && reading.getTimestamp() != meas.getTimestamp()) {
				// we have reached a new measurement.  let's stop parsing, and remember
				// this reading
				latestReading = reading;
				break;
			}
			
			if (meas == null) {
					meas = new StumblerMeasurement(reading.getTimestamp(), 
							reading.getPosition(), null);
			}
			
			meas.addReading(reading);
		}
		return meas;
	}

	public static boolean isValidFile(String file) {
		try {
			InputStream is = new FileInputStream(file);
			BufferedReader br = 
				new BufferedReader(new InputStreamReader(is));
			String line = br.readLine();
			br.close();
			is.close();
			return isValidFirstLine(line);
		} catch (IOException ex) {
			return false; 
		}

	}
	public static boolean isValidFirstLine(String line) {
		if (line.startsWith("# $Creator: Network Stumbler"))
			return true;

		/* check if this is a valid NetStumbler log line */
		String[] sarr = StringUtil.split(line);
		if (sarr.length != 11 ||
		    !(sarr[0].startsWith("N ") || sarr[0].startsWith("S ")) ||
		    !(sarr[1].startsWith("W ") || sarr[1].startsWith("E ")) ||
		    !(sarr[2].startsWith("( ") && sarr[2].  endsWith(" )")) ||
		    !(sarr[4].startsWith("( ") && sarr[4].  endsWith(" )")) ||
		    !(sarr[6].startsWith("[ ") && sarr[6].  endsWith(" ]")))
			return false;

		/* pretend that this is a valid netstumbler file */
		return true;
	}

	/* (non-Javadoc)
	 * @see org.placelab.spotter.LogSpotter#getMeasurementFromLog()
	 */
	public Measurement getMeasurementFromLog() {
		Measurement m=null;
		try {
			m = readMeasurement();
		} catch (IOException ex) {
			System.out.println("Error in netStumblerFileParser.java, 'getMeasurementFromLog'.  Exiting");
		}
		return m;
	}

}

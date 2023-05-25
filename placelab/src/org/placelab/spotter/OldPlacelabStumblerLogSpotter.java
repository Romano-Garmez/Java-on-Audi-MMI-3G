/*
 * Created on Jun 1, 2004
 */
package org.placelab.spotter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.Vector;

import org.placelab.collections.HashMap;
import org.placelab.core.BeaconReading;
import org.placelab.core.Coordinate;
import org.placelab.core.Measurement;
import org.placelab.core.StumblerMeasurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.Types;
import org.placelab.core.WiFiReading;
import org.placelab.util.Cmdline;
import org.placelab.util.Logger;
import org.placelab.util.NumUtil;
import org.placelab.util.StringUtil;

public class OldPlacelabStumblerLogSpotter extends LogSpotter {
	private BufferedReader reader;
	private Coordinate lastPos;
	private long lastGPS;
	private String filename;
	
	private BeaconReading nextReading;
	private long nextTs = -1;
	private Vector v;
	private int gpsThreshold = GPS_THRESHOLD;

	public static int GPS_THRESHOLD = 5000;
	
	public static boolean done = false;
	public int lineNum = 0;
	
	public OldPlacelabStumblerLogSpotter(String file) {
		filename = file;
	}
	public void open() throws SpotterException {
		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			throw new SpotterException(e);
		}
		//lastLat = lastLon = 0.0;
		lastPos = TwoDCoordinate.NULL;
		lastGPS = 0;
		nextReading = null;
		nextTs = -1;
		v = new Vector();
	}
	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
			}
			reader = null;
		}
	}
	public void setGPSThreshold(int millis) { gpsThreshold = millis; }
	public int getGPSThreshold() { return gpsThreshold; }
	

	public Measurement getMeasurementFromLog() {
	    long curTs = nextTs;

		if (reader == null) {
			done = true;
		    return null;
		}

		if (nextReading != null) {
			v.add(nextReading);
		}

		int version = 0;
		String line = null;
		while (true) {
			try {
				line = reader.readLine();
				if (line == null) {
					reader.close();
					reader = null;
					done = true;
					WiFiReading wrs[] = new WiFiReading[v.size()];
					for (int i=0; i<v.size(); i++) {
						wrs[i] =(WiFiReading) v.get(i);
					}
					StumblerMeasurement sm = new StumblerMeasurement(curTs,
							lastPos.createClone(),
							wrs);
					return sm;
				}
				lineNum++;

				//System.out.println("line :" + line);
				
				StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() < 3)
					continue;

				String type = st.nextToken();
				String stamp = st.nextToken();

				if (type.equalsIgnoreCase("wifi")) {
					String parse =
						line.substring(line.indexOf('\t', "WIFI\tX".length()) + 1);
					
					// backwards compatibility for pre-rewhack style logs
					if(parse.startsWith("TIME")) {
					    parse = parse.substring(parse.indexOf(' ', "TIME=X".length()) + 1);
					}
					
					int index = 0;
					
					if(!parse.startsWith("BSSID = ")) {
					    Logger.println("parse doesn't start with bssid", Logger.HIGH);
					    continue;
					}
					parse = parse.substring("BSSID = ".length());
					index = parse.indexOf(' ');
					String bssid = parse.substring(0, index);
					parse = parse.substring(index + 1);
					
					if(!parse.startsWith("SSID = '")) {
					    Logger.println("parse doesn't start with ssid", Logger.HIGH);
					    continue;
					}
					parse = parse.substring("SSID = '".length());
					//XXX: ugly hack for SSID like SSID = 'Stefan's Network', i am assuming 
					//     the format of the following field of SSID is "' RSSID = " 
					index = parse.indexOf("' RSSI = ");
					//index = parse.indexOf('\'');
					String ssid = parse.substring(0, index);
					parse = parse.substring(index + 1);
					
					if(!parse.startsWith(" RSSI = ")) {
					    Logger.println("parse doesn't start with rssi", Logger.HIGH);
					    continue;
					}
					parse = parse.substring(" RSSI = ".length());
					index = parse.indexOf(' ');
					String rssi = parse.substring(0, index);
					parse = parse.substring(index + 1);
					
					
					String[] junk = StringUtil.split(parse, ' ');
					if(junk.length < 4) {
					    Logger.println("bogus end junk length " + parse, Logger.HIGH);
					    continue;
					}
					String wep = junk[1];
					String inf = junk[3];
					
					long timestamp = Long.parseLong(stamp);

					if (timestamp > lastGPS + gpsThreshold)
						lastPos = Types.newCoordinate();
					//lastLat = lastLon = 0.0;

					StumblerMeasurement sm = null;
					
					WiFiReading newM = 
					    new WiFiReading(bssid, ssid, Integer.parseInt(rssi), 
					            wep.equalsIgnoreCase("true"),
					            inf.equalsIgnoreCase("true"));
					nextTs = timestamp;
					if (curTs != nextTs) {
						WiFiReading wrs[] = new WiFiReading[v.size()];
						for (int i=0; i<v.size(); i++) {
							wrs[i] =(WiFiReading) v.get(i);
						}
						nextReading = null;
						sm = new StumblerMeasurement(curTs, lastPos.createClone(),wrs);//								new FloatTwoDCoordinate(CoordinateFrame.GPS,lastLat,lastLon),
//								wrs);
						v.clear();
					}
					v.add(newM);
					if(sm != null) return sm;
					
				} else if (type.equalsIgnoreCase("gps")) {
					String parse = line.substring(line.indexOf('\t', "GPS\tX".length()) + 1);

					String[] junk = StringUtil.split(parse, ' ');

					HashMap fields = new HashMap();
					for (int n = 0; n + 2 < junk.length; n += 3) {
						fields.put(junk[n], junk[n + 2]);
					}

					// workarounds for various different log file versions below
					String status = (String) fields.get("STATUS");
					if(status == null) {
					    status = (String)fields.get(NMEASentence.STATUS);
					}
					
					lastGPS = Long.parseLong(stamp);
					Logger.println("LastGPS: " + stamp, Logger.HIGH);
					if (status != null && status.equalsIgnoreCase("a")) {
						String lat = (String) fields.get(NMEASentence.LATITUDE);
						if(lat == null) {
						    lat = (String)fields.get("LATITUDE");
						}
						String lon = (String) fields.get(NMEASentence.LONGITUDE);
						if(lon == null) {
						    lon = (String)fields.get("LONGITUDE");
						}
						String latHem = (String) fields.get(NMEASentence.LATITUDEHEMISPHERE);
						if(latHem == null) {
						    latHem = (String)fields.get("LATITUDEHEMISPHERE");
						}
						String lonHem = (String) fields.get(NMEASentence.LONGITUDEHEMISPHERE);
						if(lonHem == null) {
						    lonHem = (String)fields.get("LONGITUDEHEMISPHERE");
						}
						    
						/*
						lastLat =
							fromNMEA(
								Double.parseDouble(lat),
								(String) fields.get("LATITUDEHEMISPHERE"));
						lastLon =
							fromNMEA(
								Double.parseDouble(lon),
								(String) fields.get("LONGITUDEHEMISPHERE"));
						*/
						
						lastPos=Types.newCoordinateFromNMEA(lat,latHem,lon,lonHem);
						
						Logger.println("GPS lat: " + lastPos.getLatitudeAsString() + " lon: " + lastPos.getLongitudeAsString(), Logger.HIGH);
					} else {
					    Logger.println("GPS without fix", Logger.HIGH);
					    lastPos = Types.newCoordinate(); // null by default
						//lastLat = lastLon = 0.0;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Error parsing line:" + line);
			}
		}
	}
	
	// This main will convert a placelab log format into netwstumbler's old format
	public static void main(String args[]) {
		Cmdline.parse(args);
		
		String log = Cmdline.getArg("log");
		String out = Cmdline.getArg("outputfile");
		try {
			OldPlacelabStumblerLogSpotter ps = 
				new OldPlacelabStumblerLogSpotter(Cmdline.getArg("log"));
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(Cmdline.getArg("outputfile"),false)));
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
				    if(done) break;
				    else continue;
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
						        //((TwoDCoordinate)sm.getPosition()).getLatitudeAsString() + "\t" +
						        //((TwoDCoordinate)sm.getPosition()).getLongitudeAsString() + "\t" +
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
// N 55.8614150  W 4.2486717 ( bcwgroup )  BBS ( 00:41:05:cc:8d:9f ) 08:40:40 (GMT)  [ 0 0 0 ] # (  )  0051  0040  0
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


}

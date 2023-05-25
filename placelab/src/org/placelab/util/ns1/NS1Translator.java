package org.placelab.util.ns1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.DataFormatException;

import org.placelab.spotter.NetStumblerLogSpotter;
import org.placelab.util.NumUtil;

/// Translates NS1 files into netstumbler text format
public class NS1Translator {
	
	public Ns1 ns1;
	protected DHeap readings = null;
	int position;
	
	public NS1Translator(InputStream in) throws IOException, DataFormatException {
		ns1 = Ns1Reader.readNs1FromStream(in);
		this.flatten();
	}
	
	public NS1Translator(String file) throws IOException, DataFormatException {
		this(new FileInputStream(file));
	}
	
	// returns an ns1 text formatted string or null if there aren't
	// anymore
	public String nextLine() {
		if(!readings.isEmpty()) {
			String ans = ((DateStringPackage)readings.deleteMin()).string;
			return ans;
		} else {
			return null;
		}
	}
	
	private class DateStringPackage {
		public Date date;
		public String string;
		public DateStringPackage(Date date, String string) {
			this.date = date;
			this.string = string;
		}
		public int compareTo(DateStringPackage other) {
			return this.date.compareTo(other.date);
		}
	}
	
	// This method takes and flattens the ns1 into a single list
	// of all the readings in order
	private void flatten() {
		readings = new DHeap(new Comparator() {
			public int compare(Object one, Object two) {
				DateStringPackage uno = (DateStringPackage)one;
				DateStringPackage dos = (DateStringPackage)two;
				return uno.compareTo(dos);
			}
		}, 4);
		Enumeration i = ns1.getAPs().elements();
		while(i.hasMoreElements()) {
			AP a = (AP)i.nextElement();
			Vector encounters = a.getEncounters();
			if(encounters.size() == 0) {
				// then this is an old school ns1
				String stringRep = readingToString(a);
				readings.insert(
						new DateStringPackage(a.getFirstSeen(), stringRep));
			} else {
				Enumeration ei = encounters.elements();
				while(ei.hasMoreElements()) {
					Encounter e = (Encounter)ei.nextElement();
					String stringRep = readingToString(a, e);
					readings.insert(
							new DateStringPackage(e.getTime(), stringRep));
				}
			}
		}
	}

	// this is for old school (ns1 version <= 2) aps
	// this will probably never be used, but i include it for completeness
	private String readingToString(AP a) {
		return readingToString(a.getBestLat(), a.getBestLon(),
				a.getSsid(), "BBS", a.getMac(), a.getFirstSeen(),
				a.getMaxSnr(), a.getMaxSignal(), a.getMaxNoise(),
				a.getName(), a.getFlags(), a.getChannels(), a.getBeaconInterval());
	}
	
	private String readingToString(AP a, Encounter e) {
	    int signal = e.getSignal() + 149;
	    int noise = e.getNoise() + 149;
	    int snr = signal - noise;
		return readingToString(e.getLat(), e.getLon(), a.getSsid(),
				"BBS", a.getMac(), e.getTime(), snr, 
				signal, noise, a.getName(),
				a.getFlags(), a.getChannels(), a.getBeaconInterval());
	}
	
	private String readingToString(double lat, double lon,
			String ssid, String type, String bssid, Date time,
			int maxSnr, int maxSignal, int maxNoise, String name, 
			int flags, long channels, int beaconInterval) { 
		return ((lat >= 0.0) ? "N " : "S ") + 
		NumUtil.doubleToString(Math.abs(lat), 7) + "\t" +
		((lon >= 0.0) ? "E " : "W ") + 
		NumUtil.doubleToString(Math.abs(lon), 7) + "\t" +
		"( " + ssid + " )\t" + type + "\t" + 
		"( " + bssid + " )\t" + 
		nsGmtTime(time) + "\t" +
		"[ " + maxSnr + " " + maxSignal + " " + maxNoise + " ]\t" +
		"# ( " + name  + " )\t" +
		Integer.toString(flags, 4) + "\t" +
		Long.toString(channels, 4) + "\t" +
		Integer.toString(beaconInterval, 4);
	}
	
	public static boolean isValidFile(String file) {
		try {
			return Ns1Reader.isNs1(file);
		} catch (Exception e) {
			return false;
		}
	}
	
	public static NetStumblerLogSpotter newSpotter(String file) 
	{
		try {
			// evil hack
			File temp = File.createTempFile("log", "txt");
			temp.deleteOnExit();
			PrintWriter out = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream(temp)));
			writeTo(file, out);
			out.close();
			return new NetStumblerLogSpotter(temp.getAbsolutePath());
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (DataFormatException e) {
			return null;
		}
	}
	
	public static void writeTo(String ns1file, PrintWriter out) 
		throws IOException, DataFormatException 
	{
		NS1Translator t = new NS1Translator(ns1file);
		String line;
	    // write out the header
	    out.println("# $Creator: Network Stumbler Version " + t.ns1.getVersion());
	    out.println("# $Format: wi-scan with extensions");
	    out.println("# Latitude	Longitude\t( SSID )\tType\t( BSSID )\tTime" +
	    		" (GMT)\t[ SNR Sig Noise ]\t# ( Name )\tFlags\tChannelbits" +
	    		"\tBcnIntvl");
	    out.println("# $DateGMT: " + 
	    		nsGmtDate(new Date(new File(ns1file).lastModified()))); 
		while((line = t.nextLine()) != null) {
			out.println(line);
		}
		out.flush();
	}
	
	public static void main(String args[]) {
		try {
			String from = args[0];
			writeTo(from, new PrintWriter(System.out));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static String nsGmtTime(Date date) {
		return dateToString(date, "HH:mm:ss' (GMT)'");
	}
	
	public static String nsGmtDate(Date date) {
		return dateToString(date, "yyyy-MM-dd");
	}
	
	// you'd think this would be a straightforward thing the 
	// Date class could do, but no ...
	public static String dateToString(Date date, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
}

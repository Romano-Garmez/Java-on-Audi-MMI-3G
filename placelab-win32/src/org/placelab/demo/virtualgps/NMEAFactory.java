
package org.placelab.demo.virtualgps;

/**
 * Builds NMEA Sentences.  We only need to support one sentence (GGA) to 
 * fool MS MapPoint.  More sentences may come...
 */

public class NMEAFactory {
	
	public static String prefix = "GP";
	
	/**
	 * @param hhmmss seconds UTC of position: HHMMSS.ss 
	 * @param lat latitude
	 * @param latDir N or S
	 * @param lon longitude
	 * @param lonDir E or W
	 * @param fixQuality 0 (invalid), 1 (gps fix), 2 (dgps fix)
	 * @param numSats number of satellites
	 * @param hdop horizontal dilution of precision
	 * @param altitude meters above mean sea level
	 * @param altitudeUnit should be M
	 * @param heightOfGeoid height of geoid above WGS84 ellipsoid
	 * @param heightOfGeoidUnit should be M
	 */
	public static String GGA (String hhmmss, String lat, String latDir, String lon, String lonDir, int fixQuality, int numSats, String hdop, String altitude, String altitudeUnit, String heightOfGeoid, String heightOfGeoidUnit) {
		String sent = "$"+prefix+"GGA,"+hhmmss+","+lat+","+latDir+","+lon+","+lonDir+","+fixQuality+","+new String(numSats < 10 ? "0"+numSats : Integer.toString(numSats))+","+hdop+","+altitude+","+altitudeUnit+","+heightOfGeoid+","+heightOfGeoidUnit+","+/*NO DGPS*/","+/*NO DGPS*/"";
		sent += "*" + getChecksum(sent) + "\r\n";
		return sent;
	}
	
	
	/*
	public static String GSA () {
		String sent = "$"+prefix+"GSA,A,2,,,,,,,,,,,,,1.0,1.0,1.0";
		sent += "*" + getChecksum(sent) + "\r";
		return sent;
	}
	*/
	
	// public static String GLL (String lat, String latDir, String lon, String lonDir, String hhmmss, String active) {
	//	String sent = "$"+prefix+"GLL,"+lat+","+latDir+","+lon+","+lonDir+","+hhmmss+","+active+","/*don't know what this field is*/;
	//	sent += "*" + getChecksum(sent) + "\r";
	//	return sent;
	// }
	
	/**
	 * Return something reasonable but in no way accurate.
	 * @return sensible GSV string
	 */
	// public static String GSV () {
	//	String sent = "$"+prefix+"GSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00";
	//	sent += "*" + getChecksum(sent) + "\r";
	//	return sent;
	// }
	

	/*
	public static String RMC (String hhmmss, String validity, String lat, String latDir, String lon, String lonDir, String knots, String trueCourse, String date, String variation, String dir) {
		String sent = "$"+prefix+"RMC,"+hhmmss+","+validity+","+lat+","+latDir+","+lon+","+lonDir+","+knots+","+trueCourse+","+date+","+variation+","+dir;
		sent += "*" + getChecksum(sent) + "\r";
		return sent;
	}
	*/
	
	private static String getChecksum (String str) {
		int chksum = 0;
		for (int i=1; i<str.length(); i++) {
			chksum = (chksum ^ (int) str.charAt(i));
		}
		return Integer.toHexString(chksum);
	}
	
	/**
	 * TESTS
	 */
	public static void main (String[] args) {
		System.out.println(GGA("170834", "4124.8963", "N", "08151.6838", "W", 1, 5, "1.5", "280.2", "M", "-34.0", "M"));
		System.out.println(GGA("183426.855", "", "", "", "", 0, 0, "", "", "M", "", ""));
		
	}
}

package org.placelab.spotter;


/***
 * Take a line of gpsmon output data and parse it into a rmc or gga sentence.
 * @deprecated
 **/
public class NMEASentenceFactory {
	/***
	 * Parse a line of text into a rmc or gga nmea sentence.
	 * @param line a line of text from gpsmon
	 * @return a RMCSentence, a GGASentence or null if neither
	 **/
	public static NMEASentence createNMEASentence(String line) {
		if ( line.startsWith("TYPE = GPRMC") ) {
			return new RMCSentence(line);
		}
		else if ( line.startsWith("TYPE = GPGGA") ) {
			return new GGASentence(line);
		}
		else {
			return null;
		}
	}
}


/*
 * Created on Jul 21, 2004
 */
package org.placelab.spotter;

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Types;
import org.placelab.util.StringUtil;

/**
 * Used by the NMEAGPSSpotter to collate matching GPGGA and GPRMC sentences into
 * GPSMeasurements
 */
public abstract class NMEASentenceGatherer {
	private HashMap unmatchedSentences=new HashMap();
	
	public void lineAvailable(String line) {
	    //Logger.println("Got sentence: " + line, Logger.HIGH);
		NMEASentence sentence = NMEASentence.expandSentence(line);
		if (sentence == null) return;

		NMEASentence match = (NMEASentence)unmatchedSentences.get(sentence.getField(NMEASentence.TIMEOFFIX));
		if (match != null) {
			foundMatch(match, sentence);
		}
		else {
			unmatchedSentences.put(sentence.getField(NMEASentence.TIMEOFFIX),sentence);
		}
	}
	
	private void foundMatch(NMEASentence sentence1, NMEASentence sentence2) {
		NMEASentence rmc = null;
		NMEASentence gga = null;
		if (sentence1 != null && StringUtil.equalsIgnoreCase(sentence1.getType(),"GPRMC") ) {
			rmc = sentence1;
		}
		else if (sentence1 != null && StringUtil.equalsIgnoreCase(sentence1.getType(),"GPGGA") ) {
			gga = sentence1;
		}
		if (sentence2 != null && StringUtil.equalsIgnoreCase(sentence2.getType(),"GPRMC") ) {
			rmc = sentence2;
		}
		else if (sentence2 != null && StringUtil.equalsIgnoreCase(sentence2.getType(),"GPGGA") ) { 
			gga = sentence2;
		}
		// final assert
		if (rmc == null || gga == null) {
			// got two rmcs and not a gga or vice versa
			return;
		}
		
		GPSMeasurement m = createMeasurement(rmc, gga);
		measurementAvailable(m);
	}
	
	
	private GPSMeasurement createMeasurement(NMEASentence rmc, NMEASentence gga) {
		String lat = rmc.getField(NMEASentence.LATITUDE);
		String lon = rmc.getField(NMEASentence.LONGITUDE);
		String latHem = rmc.getField(NMEASentence.LATITUDEHEMISPHERE);
		String lonHem = rmc.getField(NMEASentence.LONGITUDEHEMISPHERE);
		Coordinate twodc = Types.newCoordinateFromNMEA(lat,latHem,lon,lonHem);
	    
		HashMap combinedEntry = new HashMap();
		combinedEntry.put(Types.TIMEOFFIX, rmc.getField(NMEASentence.TIMEOFFIX));
		combinedEntry.put(Types.STATUS, rmc.getField(NMEASentence.STATUS));
		combinedEntry.put(Types.LATITUDE, twodc.getLatitudeAsString());
		//combinedEntry.put(Types.LATITUDEHEMISPHERE, rmc.getField(NMEASentence.LATITUDEHEMISPHERE));
		combinedEntry.put(Types.LONGITUDE, twodc.getLongitudeAsString());
		//combinedEntry.put(Types.LONGITUDEHEMISPHERE, rmc.getField(NMEASentence.LONGITUDEHEMISPHERE));
		combinedEntry.put(Types.SPEEDOVERGROUND, rmc.getField(NMEASentence.SPEEDOVERGROUND));
		combinedEntry.put(Types.COURSEOVERGROUND, rmc.getField(NMEASentence.COURSEOVERGROUND));
		combinedEntry.put(Types.DATEOFFIX, rmc.getField(NMEASentence.DATEOFFIX));
		combinedEntry.put(Types.MAGNETICVARIATION, rmc.getField(NMEASentence.MAGNETICVARIATION));
		combinedEntry.put(Types.MAGNETICVARIATIONDIRECTION, rmc.getField(NMEASentence.MAGNETICVARIATIONDIRECTION));
		combinedEntry.put(Types.MODE, rmc.getField(NMEASentence.MODE));
		combinedEntry.put(Types.GPSQUALITY, gga.getField(NMEASentence.GPSQUALITY));
		combinedEntry.put(Types.NUMOFSATELLITES, gga.getField(NMEASentence.NUMOFSATELLITES));
		combinedEntry.put(Types.HORIZONTALDILUTIONOFPRECISION, gga.getField(NMEASentence.HORIZONTALDILUTIONOFPRECISION));
		combinedEntry.put(Types.ANTENNAHEIGHT, gga.getField(NMEASentence.ANTENNAHEIGHT));
		combinedEntry.put(Types.GEOIDALHEIGHT, gga.getField(NMEASentence.GEOIDALHEIGHT));
		combinedEntry.put(Types.DIFFERENTIALGPSDATAAGE, gga.getField(NMEASentence.DIFFERENTIALGPSDATAAGE));
		combinedEntry.put(Types.DIFFERENTIALREFERENCESTATIONID, gga.getField(NMEASentence.DIFFERENTIALREFERENCESTATIONID));
		

		return new GPSMeasurement(System.currentTimeMillis(), twodc, combinedEntry);
	}
	
	public abstract void measurementAvailable(GPSMeasurement m);
}

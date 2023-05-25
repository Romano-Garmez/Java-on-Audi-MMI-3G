package org.placelab.spotter;

import org.placelab.util.StringUtil;

public abstract class NMEASentence {

	// these are strings gpsmon uses for its output
	public static final String ANTENNAHEIGHT =			"AntennaHeightBelow/AboveMeanSeaLevel";
	public static final String COURSEOVERGROUND =			"Truecourse";
	public static final String DATEOFFIX =				"DateStamp";
	public static final String DIFFERENTIALGPSDATAAGE =		"DifferentialGPSdataage";
	public static final String DIFFERENTIALREFERENCESTATIONID =	"DifferentialReferenceStationID";
	public static final String GEOIDALHEIGHT =			"GeoidalHeight";
	public static final String GPSQUALITY =				"Quality";
	public static final String HORIZONTALDILUTIONOFPRECISION =	"HorizontalDilutionOfPrecision";
	public static final String LATITUDE =				"Latitude";
	public static final String LATITUDEHEMISPHERE =			"LatitudeHemisphere";
	public static final String LONGITUDE =				"Longitude";
	public static final String LONGITUDEHEMISPHERE =		"LongitudeHemisphere";
	public static final String MAGNETICVARIATION =			"Variation";
	public static final String MAGNETICVARIATIONDIRECTION =		"VariationDirection";
	public static final String MODE =				"ModeIndicator";
	public static final String NUMOFSATELLITES =			"NumberOfSatellites";
	public static final String SPEEDOVERGROUND =			"Groundspeed(knots)";
	public static final String STATUS =				"Validity";
	public static final String TIMEOFFIX =				"Timeoffix";

	/**
	 * Turns the standard nmea sentences from a gps into an NMEASentence.
	 * @param compactSentence the nmea version of the sentence
	 * @return the NMEASentence for the compactSentence, or null if the sentence
	 * is not understood
	 */
	public static NMEASentence expandSentence(String compactSentence) {
	    // an nmea sentence must begin with $ and can't have more than
	    // 82 characters.
	    if(!compactSentence.startsWith("$") ||
	            compactSentence.length() > 82) return null;
	    int numTokens;
	    boolean GPGGA;
	    if(compactSentence.startsWith("$GPGGA")) {
	        numTokens = 15;
	        GPGGA = true;
	    } else if(compactSentence.startsWith("$GPRMC")) {
	        numTokens = 13;
	        GPGGA = false;
	    } else {
	        // don't bother with anything but RMC or GGA
	        return null;
	    }
	    String[] tokens = StringUtil.split(compactSentence, ',', numTokens);
	    if(!GPGGA && tokens.length == 12) {
	        // the mode field (what would be the 13th field) is actually optional
	        // the belkin gps thing for instance doesn't offer it.
	        numTokens = 12;
	    }
	    if(tokens.length != numTokens) return null;
	    //pull out the last token from the checksum
	    int starIndex = tokens[numTokens - 1].indexOf('*');
	    if(starIndex != -1) {
	        // then there is a checksum
	        String checksumS = tokens[numTokens - 1].substring(starIndex + 1, 
	                tokens[numTokens - 1].length());
	        try {
	            int checksumN = Integer.parseInt(checksumS, 16);
	            int checksumC = 0;
	            for(int i = 1; i < compactSentence.length() - (checksumS.length() + 1); i++) {
	                checksumC ^= (int)compactSentence.charAt(i);
	            }
	            if(checksumN != checksumC) {
	                    /*Logger.println("failed checksum " + checksumN + " != " +
	                        checksumC, Logger.MEDIUM);
	                    if(Logger.getLogLevel() < Logger.HIGH) {
	                        Logger.println(compactSentence, Logger.MEDIUM);
	                    }*/
	                return null;
	            }
	        } catch(NumberFormatException nfe) {
	            return null;
	        }
	        tokens[numTokens - 1] = tokens[numTokens - 1].substring(0, starIndex);
	    }
	    if(!GPGGA && tokens.length == 12) {
	        // now solve the problem of the missing mode field for rmc from belkin gps
	        // turns out that these guys are always going to be in autonomous mode for our purposes
	        // so make it so
	        String[] temp = new String[13];
	        System.arraycopy(tokens, 0, temp, 0, 12);
	        temp[12] = "A";
	        tokens = temp;
	    }
	    NMEASentence ret = null;
	    if(GPGGA) {
	        ret = new GGASentence(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], tokens[8],
	                tokens[9], tokens[11], tokens[13], tokens[14]);
	        /*sb.append("TYPE = GPGGA ");
	        sb.append(TIMEOFFIX + " = " + tokens[1] + " ");
	        sb.append(LATITUDE + " = " + tokens[2] + " ");
	        sb.append(LATITUDEHEMISPHERE + " = " + tokens[3] + " ");
	        sb.append(LONGITUDE + " = " + tokens[4] + " ");
	        sb.append(LONGITUDEHEMISPHERE + " = " + tokens[5] + " ");
	        sb.append(GPSQUALITY + " = " + tokens[6] + " ");
	        sb.append(NUMOFSATELLITES + " = " + tokens[7] + " ");
	        sb.append(HORIZONTALDILUTIONOFPRECISION + " = " + tokens[8] + " ");
	        sb.append(ANTENNAHEIGHT + " = " + tokens[9] + " ");
	        sb.append(GEOIDALHEIGHT + " = " + tokens[10] + " ");
	        sb.append(DIFFERENTALGPSDATAAGE + " = " + tokens[11] + " ");
	        sb.append(DIFFERENTALREFERENCESTATIONID + " = " + tokens[12] + " ");*/
	    } else {
	        ret = new RMCSentence(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], tokens[8],
	                tokens[9], tokens[10], tokens[11], tokens[12]);
	        /*sb.append("TYPE = GPRMC ");
	        sb.append(TIMEOFFIX + " = " + tokens[1] + " ");
	        sb.append(STATUS + " = " + tokens[2] + " ");
	        sb.append(LATITUDE + " = " + tokens[3] + " ");
	        sb.append(LATITUDEHEMISPHERE + " = " + tokens[4] + " ");
	        sb.append(LONGITUDE + " = " + tokens[5] + " ");
	        sb.append(LONGITUDEHEMISPHERE + " = " + tokens[6] + " ");
	        sb.append(SPEEDOVERGROUND + " = " + tokens[7] + " ");
	        sb.append(COURSEOVERGROUND + " = " + tokens[8] + " ");
	        sb.append(DATEOFFIX + " = " + tokens[9] + " ");
	        sb.append(MAGNETICVARIATION + " = " + tokens[10] + " ");
	        sb.append(MAGNETICVARIATIONDIRECTION + " = " + tokens[11] + " ");
	        sb.append(MODE + " = " + tokens[12] + " ");*/
	    }
	    return ret;
	}
	
	protected NMEASentence() { }
	/***
	 * @return either GPRMC or GPGGA for the type of the sentence
	 **/
	public abstract String getType();
	/***
	 * Look for a field in the sentence
	 * @return the value of the field or null if not found
	 **/
	public abstract String getField(String field);

	protected String parseField(String line, String field) {
		String canonical_line = line.toLowerCase();
		String canonical_field = field.toLowerCase();
		//System.out.println("looking for " + canonical_field + " in " + canonical_line);
		int idx = 0;
		if ( (idx = canonical_line.indexOf(canonical_field)) == -1 ) {
			//System.out.println("got index of " + idx);
			return null;
		}
		String field_and_rest_of_line = canonical_line.substring(idx + canonical_field.length() + 3);
		int next_space_idx = 0;
		//System.out.println("field and rest of line = " + field_and_rest_of_line);
		if ( (next_space_idx = field_and_rest_of_line.indexOf(' ')) == -1 ) {
			return field_and_rest_of_line;
		}
		//System.out.println("next_space_idx = " + next_space_idx);
		String ret = field_and_rest_of_line.substring(0,next_space_idx);
		//System.out.println("returning " + ret);
		return ret;
	}
	/***
	 * debugging method
	 * @return the debugging info
	 **/
	public String dump() {
		return getType() + "time of fix: " + getField(TIMEOFFIX);
	}
}

/***
 * A class that represents a RMC sentence
 *
 * @see NMEASentence
 **/
class RMCSentence extends NMEASentence {
	// fields in this class
	String timeOfFix;
	String status;
	String latitude;
	String latitudeHemisphere;
	String longitude;
	String longitudeHemisphere;
	String speedOverGround;
	String courseOverGround;
	String dateOfFix;
	String magneticVariation;
	String magneticVariationDirection;
	String mode;
	
    public RMCSentence(String timeOfFix, 
            String status,
            String latitude,
            String latitudeHemisphere, 
            String longitude,
            String longitudeHemisphere,
            String speedOverGround,
            String courseOverGround,
            String dateOfFix,
            String magneticVariation, 
            String magneticVariationDirection,
            String mode) {
    	this.timeOfFix = timeOfFix;
    	this.status = status;
    	this.latitude = latitude;
    	this.latitudeHemisphere = latitudeHemisphere;
    	this.longitude = longitude;
    	this.longitudeHemisphere = longitudeHemisphere;
    	this.speedOverGround = speedOverGround;
    	this.courseOverGround = courseOverGround;
    	this.dateOfFix = dateOfFix;
    	this.magneticVariation = magneticVariation;
    	this.magneticVariationDirection = magneticVariationDirection;
    	this.mode = mode;
    }
	
	/***
	 * Constructor. Parses the text.
	 * @param line of data
	 **/
	RMCSentence(String line) {
		super();
		timeOfFix = parseField(line,NMEASentence.TIMEOFFIX);
		status = parseField(line,NMEASentence.STATUS);
		latitude = parseField(line,NMEASentence.LATITUDE);
		latitudeHemisphere = parseField(line,NMEASentence.LATITUDEHEMISPHERE);
		longitude = parseField(line,NMEASentence.LONGITUDE);
		longitudeHemisphere = parseField(line,NMEASentence.LONGITUDEHEMISPHERE);
		speedOverGround = parseField(line,NMEASentence.SPEEDOVERGROUND);
		courseOverGround = parseField(line,NMEASentence.COURSEOVERGROUND);
		dateOfFix = parseField(line,NMEASentence.DATEOFFIX);
		magneticVariation = parseField(line,NMEASentence.MAGNETICVARIATION);
		magneticVariationDirection = parseField(line,NMEASentence.MAGNETICVARIATIONDIRECTION);
		mode = parseField(line,NMEASentence.MODE);
	}

    /***
	 * Get the value of a field. Implementation of abstract method in base class.
	 * @param field name
	 * @return value of field or null if not found
	 **/
	public String getField(String field) {
	    // these field string names are static, so i can use == here, so long as people
	    // use the correct constants.
	    if(field == NMEASentence.TIMEOFFIX) return this.timeOfFix;
	    else if(field == NMEASentence.STATUS) return this.status;
	    else if(field == NMEASentence.LATITUDE) return this.latitude;
	    else if(field == NMEASentence.LATITUDEHEMISPHERE) return this.latitudeHemisphere;
	    else if(field == NMEASentence.LONGITUDE) return this.longitude;
	    else if(field == NMEASentence.LONGITUDEHEMISPHERE) return this.longitudeHemisphere;
	    else if(field == NMEASentence.SPEEDOVERGROUND) return this.speedOverGround;
	    else if(field == NMEASentence.COURSEOVERGROUND) return this.courseOverGround;
	    else if(field == NMEASentence.DATEOFFIX) return this.dateOfFix;
	    else if(field == NMEASentence.MAGNETICVARIATION) return this.magneticVariation;
	    else if(field == NMEASentence.MAGNETICVARIATIONDIRECTION) return this.magneticVariationDirection;
	    else if(field == NMEASentence.MODE) return this.mode;
	    else return null;	    
	}
	
	/***
	 * Tell me the type. Implementation of abstract method in base class.
	 * @return the type
	 **/
	public String getType() {
		return "GPRMC";
	}
}

/***
 * A class that represents a GGA sentence
 *
 * @see NMEASentence
 **/
class GGASentence extends NMEASentence {
	// fields in this class
	String timeOfFix;
	String latitude;
	String latitudeHemisphere;
	String longitude;
	String longitudeHemisphere;
	String gpsQuality;
	String numOfSatellites;
	String horizontalDilutionOfPrecision;
	String antennaHeight;
	String geoidalHeight;
	String differentalGPSDataAge;
	String differentalReferenceStationID;
	
    public GGASentence(String timeOfFix,
            String latitude,
            String latitudeHemisphere,
            String longitude,
            String longitudeHemisphere,
            String gpsQuality,
            String numOfSatellites,
            String horizontalDilutionOfPrecision,
            String antennaHeight,
            String geoidalHeight,
            String differentalGPSDataAge,
            String differentalReferenceStationID) {
    	this.timeOfFix = timeOfFix;
    	this.latitude = latitude;
    	this.latitudeHemisphere = latitudeHemisphere;
    	this.longitude = longitude;
    	this.longitudeHemisphere = longitudeHemisphere;
    	this.gpsQuality = gpsQuality;
    	this.numOfSatellites = numOfSatellites;
    	this.horizontalDilutionOfPrecision = horizontalDilutionOfPrecision;
    	this.antennaHeight = antennaHeight;
    	this.geoidalHeight = geoidalHeight;
    	this.differentalGPSDataAge = differentalGPSDataAge;
    	this.differentalReferenceStationID = differentalReferenceStationID;
    }
	
	/***
	 * Constructor. Parses the text.
	 * @param line of data
	 **/
	GGASentence(String line) {
		super();
		timeOfFix = parseField(line,NMEASentence.TIMEOFFIX);
		latitude = parseField(line,NMEASentence.LATITUDE);
		latitudeHemisphere = parseField(line,NMEASentence.LATITUDEHEMISPHERE);
		longitude = parseField(line,NMEASentence.LONGITUDE);
		longitudeHemisphere = parseField(line,NMEASentence.LONGITUDEHEMISPHERE);
		gpsQuality = parseField(line,NMEASentence.GPSQUALITY);
		numOfSatellites = parseField(line,NMEASentence.NUMOFSATELLITES);
		horizontalDilutionOfPrecision = parseField(line,NMEASentence.HORIZONTALDILUTIONOFPRECISION);
		antennaHeight = parseField(line,NMEASentence.ANTENNAHEIGHT);
		geoidalHeight = parseField(line,NMEASentence.GEOIDALHEIGHT);
		differentalGPSDataAge = parseField(line,NMEASentence.DIFFERENTIALGPSDATAAGE);
		differentalReferenceStationID = parseField(line,NMEASentence.DIFFERENTIALREFERENCESTATIONID);
	}

    /***
	 * Get the value of a field. Implementation of abstract method in base class.
	 * @param field name
	 * @return value of field or null if not found
	 **/
	public String getField(String field) {
	    // these field string names are static, so i can use == here, so long as people
	    // use the correct constants.
	    if(field == NMEASentence.TIMEOFFIX) return this.timeOfFix;
	    else if(field == NMEASentence.LATITUDE) return this.latitude;
	    else if(field == NMEASentence.LATITUDEHEMISPHERE) return this.latitudeHemisphere;
	    else if(field == NMEASentence.LONGITUDE) return this.longitude;
	    else if(field == NMEASentence.LONGITUDEHEMISPHERE) return this.longitudeHemisphere;
	    else if(field == NMEASentence.GPSQUALITY) return this.gpsQuality;
	    else if(field == NMEASentence.NUMOFSATELLITES) return this.numOfSatellites;
	    else if(field == NMEASentence.HORIZONTALDILUTIONOFPRECISION) return this.horizontalDilutionOfPrecision;
	    else if(field == NMEASentence.ANTENNAHEIGHT) return this.antennaHeight;
	    else if(field == NMEASentence.GEOIDALHEIGHT) return this.geoidalHeight;
	    else if(field == NMEASentence.DIFFERENTIALGPSDATAAGE) return this.differentalGPSDataAge;
	    else if(field == NMEASentence.DIFFERENTIALREFERENCESTATIONID) return this.differentalReferenceStationID;
	    else return null;	    
	}
	/***
	 * Tell me the type. Implementation of abstract method in base class.
	 * @return the type
	 **/
	public String getType() {
		return "GPGGA";
	}
}


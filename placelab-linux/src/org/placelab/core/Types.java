package org.placelab.core;

import org.placelab.client.tracker.Estimate;
import org.placelab.collections.HashMap;
import org.placelab.midp.GSMReading;

/**
 * Maintains list of classes and factory methods for generic types.
 * 
 * 
 *  
 */
//It is imperative that somewhere in each jar the appropriate 
//TwoDCoordinate or FixedTwoDCoordinate file is packaged together
//This should only be a problem on the phones


public class Types {
	public static final String TYPE="TYPE";
	public static final String WIFI="WIFI";	
	public static final String BLUETOOTH="BT";
	public static final String GPS="GPS";
	public static final String GSM = "GSM";
	public static final String UNKNOWN="unknown";
	public static final String TIME="TIME";
	public static final String ID="ID";
	public static final String HUMANREADABLENAME="NAME";
	public static final String LATITUDE="LAT";
	public static final String LONGITUDE="LON";
	public static final String ELEVATION="ELV";
	public static final String NEIGHBORHOOD="NBRS";	
	
	// WIFI
	public static final String BSSID="BSSID";
	public static final String SSID="SSID";
	public static final String RSSI="RSSI";
	public static final String WEP="WEP";
	public static final String INFR="INFR";
	
	// BLUETOOTH
	public static final String BLUETOOTH_ADDRESS = "BLUETOOTHADDRESS";
	public static final String MAJOR_DEVICE_CLASS = "MAJORDEVICECLASS";
	public static final String MINOR_DEVICE_CLASS = "MINORDEVICECLASS";
	public static final String SERVICE_CLASSES_LIST = "SERVICECLASSESLIST";
	public static final String SERVICE_CLASSES = "SERVICECLASSES";
	
	//GSM
	public static final String CELLID = "CELLID";
	public static final String AREAID = "AREAID";
	public static final String MCC = "MCC";
	public static final String MNC = "MNC";
	public static final String SIGNAL = "SIGNAL";
	public static final String PERCENTAGE = "PERCENTAGE";
	
	// some gps stuff
	public static final String ANTENNAHEIGHT = "ANTHEIGHT";
	public static final String COURSEOVERGROUND = "COG";
	public static final String DATEOFFIX = "DATE";
	public static final String DIFFERENTIALGPSDATAAGE = "DGPSAGE";
	public static final String DIFFERENTIALREFERENCESTATIONID = "DGPSID";
	public static final String GEOIDALHEIGHT = "GEOHEIGHT";
	public static final String GPSQUALITY = "QUALITY";
	public static final String HORIZONTALDILUTIONOFPRECISION = "HDOP";
	public static final String LATITUDEHEMISPHERE = "LATHEMI";
	public static final String LONGITUDEHEMISPHERE = "LONHEMI";
	public static final String MAGNETICVARIATION = "VAR";
	public static final String MAGNETICVARIATIONDIRECTION = "VARDIR";
	public static final String MODE = "MODE";
	public static final String NUMOFSATELLITES = "NUMSAT";
	public static final String STATUS = "STATUS";
	public static final String TIMEOFFIX = "TIMEOFFIX";
	public static final String SPEEDOVERGROUND = "SOG";
	public static final String NETWORK_NAME = "NETWORKNAME";
	
	public static final int NETSTUMBLER_RSSI_ADJUSTMENT = 149;
	
	//Coordinate type stuff
	public static final String TWODCOORDINATE = "org.placelab.core.TwoDCoordinate";
	public static final String FIXEDTWODCOORDINATE = "org.placelab.core.FixedTwoDCoordinate";
	public static final String THREEDCOORDINATE = "org.placelab.core.ThreeDCoordinate";
	public static final String COORDPROPERTY = "coordinate.type";

    public static Types t;
    protected Class coordClass = null;
    protected Class estimateClass = null;
    
    public static void init() {
        String coordClassName = "";
        String estimateClassName = "";
        String config = System.getProperty("microedition.configuration");
        String coordType = System.getProperty(COORDPROPERTY);
        if(config != null && config.equals("CLDC-1.0")) {
            // use fixed
            coordClassName=FIXEDTWODCOORDINATE;
            estimateClassName="org.placelab.client.tracker.FixedTwoDPositionEstimate";
        } else if(coordType != null && coordType.equals(THREEDCOORDINATE)) {
            coordClassName=THREEDCOORDINATE;
            estimateClassName="org.placelab.client.tracker.ThreeDPositionEstimate";
        } else {
            coordClassName=TWODCOORDINATE;
            estimateClassName="org.placelab.client.tracker.TwoDPositionEstimate";
        }
        Types.t = new Types();
        try {
            Types.t = new Types();
            t.coordClass = Class.forName(coordClassName);
        } catch(Exception e) {
            throw new TypesException("Cannot find coordinate class " + coordClassName + " : " + e);
        }
        try {
            t.estimateClass = Class.forName(estimateClassName);
        } catch(Exception e) {
            throw new TypesException("Cannot find estimate class " + estimateClassName + " : " + e);
        }
    }
    
    /**
     * Creates a new Coordinate for the Null location.
     * @see #newCoordinate(String, String)
     */
    public static Coordinate newCoordinate() {
        if(t == null) init();
        //if(t == null || t.coordClass == null) throw new TypesException("COORD_TYPE not specified");
        try {
            return (Coordinate) t.coordClass.newInstance();
        } catch (Exception e) {
            throw new TypesException("Cannot create coord: " + e);
        }
    }
    
    /**
     * Creates a new Coordinate for the given latitude and longitude
     * The actual coordinate class used will depend on the 
     * platform the code is running on.
     */
    public static Coordinate newCoordinate(String lat, String lon) {
        Coordinate twodc=newCoordinate();
        twodc.constructFromStrings(lat,lon);
        return twodc;
    }
    /**
     * Creates a Coordinate from info stored in a HashMap.
     * @see Coordinate#constructFromMap(HashMap)
     */
    public static Coordinate newCoordinate(HashMap map) {
        Coordinate threedc=newCoordinate();
        threedc.constructFromMap(map);
        return threedc;
    }
    /**
     * Creates a Coordinate from lat and lon stored as Strings
     * in NMEA format.
     * @see Coordinate#constructFromNMEA(String, String, String, String)
     */
    public static Coordinate newCoordinateFromNMEA(String latNMEA, String latHem, String lonNMEA, String lonHem) {
        Coordinate twodc=newCoordinate();
        twodc.constructFromNMEA(latNMEA,latHem,lonNMEA,lonHem);
        return twodc;        
    }
    
    /**
     * Create a new BeaconReading from a HashMap read out of the
     * pipe delimited format for BeaconReadings in log files.
     * @see BeaconReading#toLogString()
     */
    public static BeaconReading newReading(HashMap map) {
    	String type = (String) map.get(Types.TYPE);
    	if (type.equals(Types.WIFI)) {
    		return new WiFiReading(map);
    	} else if (type.equals(Types.BLUETOOTH)) {
    		return new BluetoothReading(map);
    	} else if (type.equals(Types.GSM)) {
    	    return new GSMReading(map);
    	} else return null;
    }

    /**
     * Utility method for creating the proper Estimate subclass for the platform
     * that the code is currently running on.
     * @param timestamp the time for the Estimate
     * @param position Coordinate representing estimated position 
     * @param stdDevString String representing estimated error in metres
     */
    public static Estimate newEstimate(long timestamp, Coordinate position, String stdDevString) {
        if(t==null) init();
        try {
            Estimate e = (Estimate) t.estimateClass.newInstance();
            e.construct(timestamp, position, stdDevString);
            return e;
        } catch (Exception e) {
            throw new TypesException("Cannot create estimate: " + e);
        }
    }
}

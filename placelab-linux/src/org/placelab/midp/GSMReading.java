package org.placelab.midp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.placelab.collections.HashMap;
import org.placelab.core.BeaconReading;
import org.placelab.core.Types;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;
import org.placelab.util.StringUtil;

/**
 * Encapsulates a GSM reading from the native servers. This includes
 * cell id, area id, signal strength, MNC, MCC, and network name.
 * 
 */
public class GSMReading implements BeaconReading {
	String cellId;
	String areaId;
	String signalStrength;
	String MCC;
	String MNC;
	String networkName;

	/**
	 * Constructs a GSM reading
	 * @param _cellId cell id
	 * @param _areaId area id
	 * @param _signalStrength signal strength (passed in as a positive number from the server)
	 * @param _MCC MCC value for the cell
	 * @param _MNC MNC value for the cell
	 * @param _networkName network name of the cell owner
	 */
	public GSMReading(String _cellId, String _areaId, String _signalStrength,
					  String _MCC, String _MNC, String _networkName) {
		cellId = _cellId;
		areaId = _areaId;
		signalStrength = _signalStrength;
		MCC = _MCC;
		MNC = _MNC;
		networkName = _networkName;
	}
	
	/**
	 * Throws a number format exception if it doesn't like the ID you passed in
	 * @param _id
	 * @param _networkName
	 * @param _signalStrength
	 * @throws NumberFormatException
	 */
	public GSMReading(String _id,String _networkName,String _signalStrength) throws NumberFormatException {
		String[] sArr = StringUtil.split(_id,':');
		if (sArr.length != 4) {
			throw new NumberFormatException("Bad ID");
		}
		cellId = sArr[2];
		areaId = sArr[3];
		MCC = sArr[0];
		MNC = sArr[1];
		signalStrength = _signalStrength;
		networkName = _networkName;
	}

	/**
	 * Construct a GSM reading from a HashMap
	 * @param map HashMap of GSM reading values
	 */
	public GSMReading(HashMap map) {
	    networkName = (String)map.get(Types.HUMANREADABLENAME);
	    cellId = (String)map.get(Types.CELLID);
	    areaId = (String)map.get(Types.AREAID);
	    MCC = (String)map.get(Types.MCC);
	    MNC = (String)map.get(Types.MNC);
	    signalStrength = (String)map.get(Types.SIGNAL);
	}

	/**
	 * Construct a GSM reading from a DataInputStream
	 * @param dis DataInputStream to read GSM reading values from
	 */
	public GSMReading(DataInputStream dis) throws IOException {
		networkName = dis.readUTF();
		cellId = Integer.toString(dis.readInt());
		areaId = Integer.toString(dis.readInt());
		MCC = Integer.toString(dis.readInt());
		MNC = Integer.toString(dis.readInt());
		signalStrength = Integer.toString(dis.readInt());
	}

	
	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#getType()
	 */
	public String getType() {
		return Types.GSM;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#getId()
	 */
	public String getId() {
		return MCC+":"+MNC+":"+cellId+":"+areaId;//cellId + ":" + areaId;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#isValid()
	 */
	public boolean isValid() {
		for(int i = 0; i < MCC.length(); i++) {
		    if(MCC.charAt(i) != '0') return true;
		}
		for(int i = 0; i < MNC.length(); i++) {
		    if(MNC.charAt(i) != '0') return true;
		}
		for(int i = 0; i < cellId.length(); i++) {
		    if(cellId.charAt(i) != '0') return true;
		}
		for(int i = 0; i < areaId.length(); i++) {
		    if(areaId.charAt(i) != '0') return true;
		}
		return false;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#getNormalizedSignalStrength()
	 */
	public int getNormalizedSignalStrength() {
		//This is an approximate algorithm
		//All the #'s should come in positive form, changing them up top
		
		int nativeSignal = Integer.parseInt(signalStrength);
		if(nativeSignal > 0)
			nativeSignal *= -1;
		
//		double p= nativeSignal*1.6164 + 182.3836;
//		int value = (new Long(Math.round(p))).intValue(); 

		long a,b;
		try {
		    a = FixedPointLong.stringToFlong("1.6164");
		    b = FixedPointLong.stringToFlong("182.3836");
		} catch(FixedPointLongException fple) {
		    throw new RuntimeException("Cannot get FPLs for statics!");
		}
		
		long signal = nativeSignal*a + b;
		int value = FixedPointLong.intValue(signal);
		if(value > 100) value = 100;
		
		return value;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#toShortString()
	 */
	public String toShortString() {
		return "Cell ID: "+cellId+", Area ID: "+areaId;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return Types.HUMANREADABLENAME + "=" + networkName + "|" +
			   Types.CELLID + "=" + cellId + "|" +
			   Types.AREAID + "=" + areaId + "|" +
			   Types.MCC + "=" + MCC + "|" +
			   Types.MNC + "=" + MNC + "|" +
			   Types.SIGNAL + "=" + signalStrength + "|" +
			   Types.PERCENTAGE + "=" + getNormalizedSignalStrength();
	}


	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#toLogString()
	 */
	public String toLogString() {
		return toString();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#toCompressedBytes(java.io.DataOutputStream)
	 */
	public void toCompressedBytes(DataOutputStream dos) throws IOException {
		dos.writeUTF(networkName);
		dos.writeInt(Integer.parseInt(cellId));
		dos.writeInt(Integer.parseInt(areaId));
		dos.writeInt(Integer.parseInt(MCC));
		dos.writeInt(Integer.parseInt(MNC));
		dos.writeInt(Integer.parseInt(signalStrength));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.placelab.core.BeaconReading#getHumanReadableName()
	 */
	public String getHumanReadableName() {
        return "" + networkName + ":" + cellId + ":" + areaId;
    }

	/*
	 * Get the unique ID for this GSM reading. Constructed by
	 * MCC:MNC:cellId:areaId
	 */
    public String getUniqueId() {
    	return "" + MCC+":"+MNC+":" + cellId + ":" + areaId;
    }
    
    public boolean isNull() {
    	try {
    		return (Long.parseLong(areaId) == 0) || (Long.parseLong(cellId) == 0);
    	} catch (Exception ex) { return true; }
    }

}

/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.placelab.collections.HashMap;
import org.placelab.util.StringUtil;

/**
 * A BeaconReading produced by a WiFiSpotter scanning for WiFi beacons
 */
public class WiFiReading implements BeaconReading {
	/** The unique id of the AP. This is the same thing as the AP's mac address **/
	private String bssid;
	/** The human readable network address (eg: 'Starbucks' or 'Cometa hotspot') **/
	private String ssid;
	/** The observed signal strength **/
	private int rssi = 0;
	/** Denotes wether encryption in enabled for the AP or not **/
	private boolean wepEnabled = false;
	/** Denotes wether the AP in in infrastructure or peer-to-peer mode **/
	private boolean isInfrastructure = true;
	private final int IEEE80211_SSID_MAX_LEN = 32; /* section 7.3.2.1, IEEE802.11 1999 spec */
	
	public WiFiReading(String bssid, String ssid, int rssi, boolean wepEnabled, boolean isInfrastructure) {
		if (bssid == null) 
			throw new IllegalArgumentException("bssid cannot be null");
		
		bssid = bssid.toLowerCase();
		
		//XXX: We should write a 802 MAC address class to avoid this
		if (bssid.length() != 17 || 
			bssid.startsWith("00:00:00") ||
			bssid.startsWith("ff:ff:ff") ||
			bssid.endsWith("00:00:00") ||
			bssid.endsWith("ff:ff:ff")) {
			throw new IllegalArgumentException("bssid: " + bssid + " has invalid format");
		}
		
		if (ssid == null)
			throw new IllegalArgumentException("ssid cannot be null (but it can be \"\" ");
		
		if (ssid.getBytes().length > IEEE80211_SSID_MAX_LEN)
			throw new IllegalArgumentException("ssid " + ssid + 
					" exceeds maximum length " + IEEE80211_SSID_MAX_LEN 
					+ "octet");
	
		// -149 is from NetStumblerFileParser.java
		final int minRssi = -Types.NETSTUMBLER_RSSI_ADJUSTMENT;
		final int maxRssi = 0;
		
		if (rssi < minRssi)
			throw new 
			IllegalArgumentException("rssi: " + rssi + " must be > " + minRssi);
		
		if (rssi > maxRssi) 
			throw new IllegalArgumentException("rssi: " + rssi + " must < " + maxRssi);		
		
		this.bssid = bssid;
		this.ssid = ssid;
		this.rssi = rssi;
		this.wepEnabled = wepEnabled;
		this.isInfrastructure = isInfrastructure;
	}
	public WiFiReading(HashMap map) {
		this((String)map.get(Types.ID), (String)map.get(Types.HUMANREADABLENAME), 
				Integer.parseInt((String)map.get(Types.RSSI)),
				StringUtil.stringToBoolean((String)map.get(Types.WEP)), 
				StringUtil.stringToBoolean((String)map.get(Types.INFR)));
	}
	
	public String toString() {
		return "BSSID = " + bssid + " SSID = '"  + ssid + "' RSSI = " +
		 	rssi + " WEP " + wepEnabled + " InfMode " + isInfrastructure;
	}

	public String toLogString() {
	    return Types.ID+"=" + StringUtil.percentEscape(bssid) + "|" + Types.HUMANREADABLENAME + "=" +
			StringUtil.percentEscape(ssid) + "|" + Types.RSSI + "=" + rssi +
	    "|" + Types.WEP + "=" + wepEnabled + "|" + Types.INFR + "=" + isInfrastructure;
	    
	}
	public String toShortString() {
		return "BSSID: "+bssid+", SSID: "+ssid;
	}

	public String getType() {
		return Types.WIFI;
	}
	public String getId() {
		return bssid;
	}
	
	public int getRssi() {
		return rssi;
	}
	
	public boolean isValid() {
	    // bad MACs are filtered in the constructor
	    return true;
	}
	
	public String getSsid() {
		return ssid;
	}
	
	public String getBssid() {
		return bssid;
	}
	
	public int getNormalizedSignalStrength() {
		// return an int 0-100 depending on the signal strength, or -1 if unsupported		
		// linear heuristic based on histogram of logs at IRS
		// -45 => 100, -60 => 67, -75 => 33, -90 => 0	    
		//int retval = (100*(90 + rssi)) / 45;
	    
	    // JWS changed this since supporting particlefilters is easier this way
	    // will affect the colouring on XMapDemo - more yellows and less reds/greens
	    int retval = rssi+100;
	    if(retval < 0) retval = 0;
		if(retval > 100) retval = 100;
		return retval;
	}
	
	
	public boolean isInfrastructureMode() {
		return isInfrastructure;
	}
	
	public boolean isWEP() {
		return wepEnabled;
	}
	

	public boolean getIsInfrastructure() {
	    return isInfrastructure;
	}
	
	public boolean getWepEnabled() {
	    return wepEnabled;
	}
	
	public String getHumanReadableName() {
	    return ssid;
	}

	public void toCompressedBytes(DataOutputStream dos) throws IOException {
		dos.writeUTF(bssid);
		dos.writeUTF(ssid);
		dos.writeInt(rssi);
		dos.writeBoolean(wepEnabled);
		dos.writeBoolean(isInfrastructure);
	}
	public WiFiReading(DataInputStream dis) throws IOException {
		bssid = dis.readUTF();
		ssid = dis.readUTF();
		rssi = dis.readInt();
		wepEnabled = dis.readBoolean();
		isInfrastructure = dis.readBoolean();
	}
}

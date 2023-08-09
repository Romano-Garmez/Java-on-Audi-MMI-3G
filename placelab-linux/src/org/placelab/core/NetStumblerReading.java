/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;


/**
 * 
 *
 */
public class NetStumblerReading extends WiFiReading {
	private Coordinate coord;
	private long timestamp;
	
	public NetStumblerReading(long timestamp, Coordinate coord, String bssid, 
			String ssid, int rssi, boolean wepEnabled, boolean isInfrastructure) {
		super(bssid, ssid, rssi, wepEnabled, isInfrastructure);
		this.timestamp = timestamp;
		this.coord = coord;
	}
	
	public Coordinate getPosition() { return coord; }
	public long getTimestamp() { return timestamp; }
}

/*
 * Created on Sep 17, 2004
 *
 */
package org.placelab.spotter;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;


public class HardcodedBluetoothGPSSpotter  extends BluetoothGPSSpotter {
	protected String address;
	
	public HardcodedBluetoothGPSSpotter(String address) {
		this.address = address;
	}
	
	public void open() {
		super.open();
		findGPS();
	}
	
	protected void findGPS() {
//		 fabricate the whole thing
		String fabUrl = 
			"btspp://" // this means bt serial port service 
			+ address
			+ ":1"  // bt gps uses 1 as the RFCOMM channel
			+ ";authenticate=false"  // NOAUTHENTICATE_NOENCRYPT
			+ ";encrypt=false";
		try {
			state = "Attempting to connect to " + address;
			conn = (StreamConnection)Connector.open(fabUrl);
			is = conn.openDataInputStream();
		} catch (Exception e) {
			state = "Failed connection attempt to " + address + " (" + e + ")";
			serviceUrl = null;
			return;
			// this could be a problem
		}
		// if i get here, then the connection succeeded
		serviceUrl = fabUrl;
		state = "Connected " + address;
	}

}

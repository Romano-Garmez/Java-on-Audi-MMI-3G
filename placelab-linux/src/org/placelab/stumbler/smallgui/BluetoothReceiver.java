package org.placelab.stumbler.smallgui;

import org.placelab.core.BeaconReading;
import java.util.Hashtable;
import org.placelab.core.BluetoothReading;
import org.placelab.spotter.BluetoothSpotter;

/**
 * This is just a place holder until we get a functioning JSR-82 implementation
 * on the PocketPC.
 */

public class BluetoothReceiver extends BeaconReceiver {

	public BluetoothReceiver() {
		super(BluetoothReading.class);
	}
	
	public Hashtable extractFields (BeaconReading reading) {
		return null;
	}
	
	public Class[] getSupportedSpotters () {
		return new Class[]{
				BluetoothSpotter.class
		};
	}
	
	public String getType () {
		return "Bluetooth";
	}
}

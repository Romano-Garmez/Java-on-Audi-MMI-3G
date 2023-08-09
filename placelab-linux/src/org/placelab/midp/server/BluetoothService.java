package org.placelab.midp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Interface that should be implemented by services that want to register
 * with the Bluetooth server
 */
public interface BluetoothService {

	public final static byte LOG_UPLOAD_SERVICE = 1;
	public final static byte MAP_LOADER_SERVICE = 2;
	public final static byte ACTIVITY_UPLOAD_SERVICE = 3;
	public final static byte GPS_TIME_SERVICE = 4;
	public final static byte ESM_QUESTION_SERVICE = 5;
	public final static byte CONSOLE_SERVICE = 6;
	public final static byte PLACE_UPLOAD_SERVICE = 7;
	
	public String getName();

	public byte getServiceType();

	public void newClient(DataInputStream in, DataOutputStream out);
}
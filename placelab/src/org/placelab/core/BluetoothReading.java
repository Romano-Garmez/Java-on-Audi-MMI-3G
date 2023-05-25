/*
 * Created on 22-Jun-2004
 *
 */
package org.placelab.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.placelab.collections.HashMap;
import org.placelab.spotter.BluetoothUtil;
    
/**
 * This class represents the sighting of a remote bluetooth device
 * 
 * 
 */
public class BluetoothReading implements BeaconReading {
	private String humanReadableName;
	private String bluetoothAddress;
	protected String majorDeviceClass;
	protected String minorDeviceClass;
	protected int serviceClasses;
	protected String[] serviceClassesList;

	public BluetoothReading(String humanReadableName, String bluetoothAddress,
			String majorDeviceClass, String minorDeviceClass, int serviceClasses) {
		this.humanReadableName = humanReadableName;
		this.bluetoothAddress = bluetoothAddress;
		if (humanReadableName == null)
			humanReadableName = "";
		if (bluetoothAddress == null)
			bluetoothAddress = "";

		this.majorDeviceClass = majorDeviceClass;
		this.minorDeviceClass = minorDeviceClass;
		this.serviceClasses = serviceClasses;
		this.serviceClassesList = BluetoothUtil.getServiceClasses(serviceClasses);
	}

	public BluetoothReading(HashMap map) {
		this((String)map.get(Types.HUMANREADABLENAME),
				(String)map.get(Types.BLUETOOTH_ADDRESS),
				(String)map.get(Types.MAJOR_DEVICE_CLASS),
				(String)map.get(Types.MINOR_DEVICE_CLASS),
				Integer.parseInt((String)map.get(Types.SERVICE_CLASSES)));
	}
	public String getType() {
		return Types.BLUETOOTH;
	}
	
	public String getMajorDeviceClass() {
	    return majorDeviceClass;
	}
	
	public String getMinorDeviceClass() {
	    return minorDeviceClass;
	}
	
	public String[] getServiceClassesList() {
	    return serviceClassesList;
	}
	
	public int getServiceClasses() {
	    return serviceClasses;
	}
	
	/**
	 * Returns Bluetooth MAC address
	 */
	public String getId() {
		return bluetoothAddress;
	}
	
	public boolean isValid() {
	    return !(getId().equals("") || getId().equals("0000000000"));
	}

	/**
	 * Returns Bluetooth Friendly Name
	 */
	public String getHumanReadableName() {
		return humanReadableName;
	}
	
	/**
	 * No signal strength available in javax.bluetooth - returns -1
	 */
	public int getNormalizedSignalStrength() {
		return -1;
	}

	public String toString() {
		return "BluetoothMeasurement: " + getHumanReadableName();
	}
	
	public String toLogString() {
		String list = "";
		for (int i=0;i<serviceClassesList.length;i++) {
			list += serviceClassesList[i] + ((i+1<serviceClassesList.length) ? "," : "");
		}
		return 
			Types.BLUETOOTH_ADDRESS + "=" + bluetoothAddress + "|" + 
			Types.HUMANREADABLENAME + "=" + humanReadableName + "|" + 
			Types.MAJOR_DEVICE_CLASS + "=" + majorDeviceClass + "|" + 
			Types.MINOR_DEVICE_CLASS + "=" + minorDeviceClass + "|" + 
			Types.SERVICE_CLASSES + "=" + serviceClasses + "|" +
			Types.SERVICE_CLASSES_LIST + "=" + list;
	}

	public String toShortString() {
		return "Address: "+bluetoothAddress+", Name: "+humanReadableName;
	}

	public void toCompressedBytes(DataOutputStream dos) throws IOException {
		dos.writeUTF(humanReadableName);
		dos.writeUTF(bluetoothAddress);
		dos.writeUTF(majorDeviceClass);
		dos.writeUTF(minorDeviceClass);
		dos.writeInt(serviceClasses);
	}
	public BluetoothReading(DataInputStream dis) throws IOException {
		humanReadableName = dis.readUTF();
		bluetoothAddress = dis.readUTF();
		if (humanReadableName == null)
			humanReadableName = "";
		if (bluetoothAddress == null)
			bluetoothAddress = "";

		majorDeviceClass = dis.readUTF();
		minorDeviceClass = dis.readUTF();
		serviceClasses = dis.readInt();
		serviceClassesList = BluetoothUtil.getServiceClasses(serviceClasses);
	}
}

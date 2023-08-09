/*
 * Created on Jul 21, 2004
 *
 */
package org.placelab.spotter;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.placelab.core.BluetoothReading;

/**
 * An abstract class to scan for bluetooth using the JSR-82 bluetooth apis
 */
public abstract class BluetoothScan implements DiscoveryListener {
	public abstract void gotReading(BluetoothReading br);
	public abstract void scanDone();
	
	private boolean filterPhones = false;
	
	public void setFilterPhones(boolean value) {
		filterPhones = value;
	}
	
	public boolean getFilterPhones() {
		return filterPhones;
	}
	
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		String fn, ba;
		
		ba = btDevice.getBluetoothAddress();
		try {
			fn = btDevice.getFriendlyName(false);
		} catch (IOException e) {
			fn = "";
		}
		if(fn==null) fn=""; 

		if(filterPhones && (cod.getMajorDeviceClass() == BluetoothUtil.PHONE_MAJOR_CLASS)) {
			return; //don't pass this up
		}
		
		String majorDeviceClass = BluetoothUtil.getMajorDeviceClass(cod.getMajorDeviceClass());
		String minorDeviceClass = BluetoothUtil.getMinorDeviceClass(cod.getMajorDeviceClass(),cod.getMinorDeviceClass());
		int serviceClasses = cod.getServiceClasses();
		BluetoothReading br = new BluetoothReading(fn, ba,majorDeviceClass,minorDeviceClass,serviceClasses);
		this.gotReading(br);
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// unused		
	}
	public void serviceSearchCompleted(int transID, int respCode) {
		// unused
	}

	public void inquiryCompleted(int discType) {
		this.scanDone();
	}
	
	public boolean start() {
		try {
			LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
		} catch (BluetoothStateException e) {
			return false;
		}
		return true;
	}
	public boolean cancel() {
		try {
			LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(this);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}

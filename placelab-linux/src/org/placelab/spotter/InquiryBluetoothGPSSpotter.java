/*
 * Created on 01-Jul-2004
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
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.placelab.collections.LinkedList;
import org.placelab.midp.EventLogger;


/**
 * An NMEAGPSSpotter for Bluetooth equipped devices to talk to
 * Bluetooth gps units.
 * 
 */
public class InquiryBluetoothGPSSpotter extends BluetoothGPSSpotter
{
	private BluetoothGPSScan scanInProgress = null;
	private static long SMALL_AMOUNT_OF_TIME=100; /* milliseconds */
	private long LARGE_AMOUNT_OF_TIME; /* milliseconds */
	
	public InquiryBluetoothGPSSpotter() {
		this(5*60*1000);
	}
	
	public InquiryBluetoothGPSSpotter(long sleepBetweenSearches) {
		this.LARGE_AMOUNT_OF_TIME = sleepBetweenSearches;
	}
	
	public void open() {
	    super.open();
	    scanInProgress = null;
	}
	
	private class BluetoothGPSScan implements DiscoveryListener {
		protected LinkedList serviceRecords = new LinkedList();
		private String url;
		// list of RemoteDevices
		private LinkedList deviceList = new LinkedList();
		
		
		public String getUrl() { return url; }

		boolean urlReady = false;
		boolean inquiring=false;
		
		public synchronized void getNewUrl() {
		    urlReady = false;
			if(deviceList.size() == 0) {
				url = null;
				start();
			} else {
				startServiceSearch();
			}
		}
		
		public synchronized void start() {
			try {
			    inquiring=true;
				state = "Looking for Devices";
				LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
			} catch (BluetoothStateException e) {
				state = "Inquiry Failed";
				finish();
			}
		}
		
		private synchronized void startServiceSearch() {
			try {
				//0x1101 is serial port
				state = "Looking for GPS Service";
				RemoteDevice d = (RemoteDevice) deviceList.remove(0);
				connectedDeviceName = d.getFriendlyName(false);
				LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(null,new UUID[] {new UUID(0x1101)},d,this);
				state = "Looking for GPS Service on "+connectedDeviceName;
				//System.err.println(state);
			} catch(BluetoothStateException bse) {
				//don't do anything
				bse.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		public synchronized void deviceDiscovered(RemoteDevice device, DeviceClass dClass) {
			String bluetoothAddress,friendlyName;
			bluetoothAddress = device.getBluetoothAddress();
			try {
				friendlyName = device.getFriendlyName(false);
				if(friendlyName == null)
					friendlyName = "";
			} catch(IOException ioe) {
				friendlyName = "";
			}

			//System.err.println("found "+ friendlyName);
			if(friendlyName.toLowerCase().indexOf("gps") >= 0) {
				deviceList.add(device);
			}
		}

		/* Done discovering devices, start a service search */
		public synchronized void inquiryCompleted(int discType) {
		    inquiring=false;
			if (deviceList.size() > 0) {
				startServiceSearch();
			} else {
				state = "No Devices Found";
				finish();
			}
		}

		
		/* This should only be called when we have our Bluetooth GPS Device */
		public synchronized void servicesDiscovered(int transID, ServiceRecord[] servRecord) {		
			//System.err.println("service discovered");
			for(int i=0;i<servRecord.length;i++) {
				serviceRecords.add(servRecord[i]);
			}
		}
		
		public synchronized void cancel() {
		    if(inquiring == true) {
		        try {
		            LocalDevice.getLocalDevice().getDiscoveryAgent().cancelInquiry(this);
		        } catch(Exception e) {
		            
		        }
  		    }
		    url=null;
		    urlReady = true;
		}

		
		public synchronized void serviceSearchCompleted(int transID, int respCode) {
			//System.err.println("done");
			
			if(serviceRecords.size() > 0) {
				state = "Found GPS Service";
				ServiceRecord sr = (ServiceRecord) serviceRecords.remove(0); 
				url = sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				finish();
			} else if(deviceList.size() > 0) {
				startServiceSearch();
			} else {
				finish();
			}
		}

		private void finish() {
		    urlReady = true;
		}
	}


	protected synchronized void findGPS() {
	    try {
	    System.err.println(scanInProgress);
		scanInProgress.getNewUrl();
		while (!scanInProgress.urlReady) {
			try {
				Thread.sleep(SMALL_AMOUNT_OF_TIME);
			} catch (InterruptedException e1) {
			}
			if(scanThread == null || scanThread.isDone()) {
			    scanInProgress.cancel();
			    cleanup();
			    return;
			}
		}

		serviceUrl = scanInProgress.getUrl();
		if (serviceUrl == null) {//this happens when there are no gps devices
			try {
				state = "Sleeping for "+(LARGE_AMOUNT_OF_TIME/1000)+" seconds";
				System.err.println("sleeping...");
				long currentMillis = 0;
				while(currentMillis < LARGE_AMOUNT_OF_TIME) {
					if(scanThread  == null || scanThread.isDone()) {
					    cleanup();
						return;
					}
					Thread.sleep(SMALL_AMOUNT_OF_TIME);
					currentMillis += SMALL_AMOUNT_OF_TIME;
				}
				state = "Trying Again";
			} catch(InterruptedException ie) {
				//ie.printStackTrace();
			}
			return;
		}

		// open a connection to the server
		try {
			conn = (StreamConnection) Connector.open(serviceUrl);
			is = conn.openDataInputStream();
			state = "Connected "+connectedDeviceName;
		} catch (IOException e) {
			//e.printStackTrace();
			cleanup();
			return;
		}
	    } catch(Exception e) {
	        EventLogger.logError("findGPS: " + e);
	        cleanup();
	        return;
	    }
	}
	
	protected void scanningThreadRun() {
		scanInProgress = new BluetoothGPSScan();
		super.scanningThreadRun();
	}

//	public static void main(String[] args) {
//		
//		final InquiryBluetoothGPSSpotter bgps = new InquiryBluetoothGPSSpotter();
//		bgps.open();			
//		bgps.addListener(new SpotterListener() {
//			int i =0;
//	 		public void gotMeasurement(Spotter sender, Measurement m) {
//				System.err.println(""+i+": "+m.toLogString());
//				i++;
//	 		}
//
//			public void spotterExceptionThrown(Spotter sender, SpotterException ex) {
//			}
//
//		});	
//
//		bgps.startScanning();
//
//	}
	
}

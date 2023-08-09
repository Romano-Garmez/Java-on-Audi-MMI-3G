package org.placelab.spotter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Measurement;
import org.placelab.midp.GSMReading;
import org.placelab.util.StringUtil;

/**
 * This spotter talkes via bluetooth to read GSM readings from a 60 Series Bluetooth phone. The phone
 * must be running the 'BTGSM' Midlet which creates the GSM sharing service. Other than the remoteness, this
 * spotters behaves as expected. The phone is capable of buffering up to 60 seconds of 
 * GSM readings. This spotter can be created to either pull over all of the buffered readings,
 * or just the latest reading. In the case that all readings are pulled over, repeated calls to
 * getMeasurement will empty the buffer.   
 */
public class RemoteGSMSpotter extends SyncSpotter implements PeriodicScannable {
	protected String bluetoothAddress, connectionURL;
	protected long scanInterval = 10000;
	Vector bufferedMeasurements;
	boolean bufferMeasurements;
	boolean buffered = false;
	

	/*
	 * The constructor requires the address of the cell phone. This address will be printed on the cell phone's screen
	 * when BTGSM is first run. The bluetoothAddress is the mac address, a colon and the port number.
	 * Example: "000e6d43ec17:4".
	 */
	public RemoteGSMSpotter(String bluetoothAddress, boolean bufferMeasurements) {
		super();
		bufferedMeasurements = new Vector();
		this.bufferMeasurements = bufferMeasurements;
		this.bluetoothAddress = bluetoothAddress;
		connectionURL = "btspp://" + bluetoothAddress + ";authenticate=false;encrypt=false";
	}
	
	protected long nextScanInterval() {
		return scanInterval;
	}

	protected synchronized Measurement getMeasurementImpl() throws SpotterException {
		
		if (buffered) {
			return getFromBuf();
		}
		
		StreamConnection con = null;
		DataInputStream is = null;
		DataOutputStream os = null;
		String id = null;
		String name = null;
		String ss = null;
		try {
			try {
				con = (StreamConnection)Connector.open(connectionURL);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("CERR " + bluetoothAddress);
				return null;
			}
			is = con.openDataInputStream();
			long time = System.currentTimeMillis();
			// Read the length
			int len = is.readInt();
			final StringBuffer logBuf = new StringBuffer();
			for (int i=0; i<len; i++) {
				// Read the string
				String str = is.readUTF();
				// break it up
				String sarr[] = StringUtil.split(str,'|');
				if (sarr.length == 4) {
					GSMReading gr = new GSMReading(sarr[2],sarr[1],sarr[3]);
					long brTime = time - Long.parseLong(sarr[0]);
					BeaconReading brArr[] = new BeaconReading[1];
					brArr[0] = gr;
					Measurement m = new BeaconMeasurement(brTime,brArr);
					if (bufferMeasurements) {
						bufferedMeasurements.addElement(m);
					} else {
						// kick out a reading that is older than us
						if (bufferedMeasurements.size() == 0) {
							bufferedMeasurements.addElement(m);
						} else {
							Measurement mOld = (Measurement)bufferedMeasurements.get(0);
							if (mOld.getTimestamp() < m.getTimestamp()) {
								bufferedMeasurements.clear();
								bufferedMeasurements.addElement(m);
							}
						}
					}
					buffered = true;
				} else {
					System.out.println("Ack, got line with " + sarr.length);
				}
			}
			return getFromBuf();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new SpotterException(ioe);
		} finally {
			if(con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Measurement getFromBuf() {
		if (buffered) {
			if (bufferedMeasurements.size() > 0) {
				return (Measurement)bufferedMeasurements.remove(0);
			} else {
				buffered = false;
				return null;
			}
			
		} else {
			return null;
		}
	}

	public void setPeriodicScanInterval(long intervalMillis) {
		scanInterval = intervalMillis;
	}

	public long getPeriodicScanInterval() {
		return scanInterval;
	}

	public void open() throws SpotterException {
		// test to see if there is a bluetooth interface
		try {
			LocalDevice localDevice = LocalDevice.getLocalDevice();
			if(localDevice.getBluetoothAddress().equals("000000000000")) {
				throw new SpotterException("Can't see local bluetooth device.");
			}
		} catch (Throwable t) {
			throw new SpotterException("Can't see local bluetooth device "
					+ t.getMessage());
		}
	}
	
	public void close() throws SpotterException {
		
	}

}

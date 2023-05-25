package org.placelab.spotter;

import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.placelab.util.StringUtil;


public abstract class BluetoothGPSSpotter extends NMEAGPSSpotter {
    protected ScanThread scanThread=null;
	protected StreamConnection conn = null;
	protected DataInputStream is = null;
    protected String serviceUrl = null;
	protected String state;
	protected String connectedDeviceName;
	protected boolean shutdown;
	protected String lingeringData = null;
	
	protected class ScanThread extends Thread {
		private boolean done=false;
		public synchronized void cancel() {
			done = true;
		}
		protected synchronized boolean isDone() { return done; }

		public void run() {
			BluetoothGPSSpotter.this.scanningThreadRun();
		}
	}
	
	 public void open() {
    	conn = null;
    	is = null;
    	lingeringData = null;
    	shutdown = false;
    	state = "Disconnected";
    }
	public void close() {
	    cleanup();
	}
	
    
	protected void startScanningImpl() {
		scanThread = new ScanThread();
		scanThread.start();
	}
	protected void stopScanningImpl() {
		if (scanThread != null) {
			/* notify the scanning thread that we are stopping */
			scanThread.cancel();
			/* wait for the scanning thread to stop */
			waitForThread(scanThread);
			scanThread = null;
		}
		try {
		    // sabotage any read in progress
		    if(is != null) is.close();
		} catch(Exception e) {
		}

	}
	protected void scanningThreadRun() {
	    while(scanThread != null && !scanThread.isDone()) {
	        if(serviceUrl == null || is == null || conn == null) {
	            findGPS();
	        } else {
				try {
					readGPSData();
				} catch(IOException e) {
					//An IOException means the connection is gone
					cleanup();
				}
	        }
	    }
	}
	protected void cleanup() {
	    try { conn.close(); } catch (Exception ex) { }
		conn = null;
		try { is.close(); } catch (Exception ex) { }
		is = null;
		serviceUrl = null;
		lingeringData = null;
		state = "Disconnected";
	}
	/**
	 * Locates a bluetooth gps device and hooks up serviceUrl, conn, and is.
	 * If no bluetooth gps device can be found, leave those 3 alone.
	 * Feel free to block in here if you like.
	 */
	protected abstract void findGPS();
	protected void readGPSData() throws IOException {
		String data = null;
		byte[] b = new byte[1024];
		if(is == null)
			throw new IOException("readGPSData: null InputStream");
		int numread = is.read(b); //will throw IOException upwards
		data = new String(b,0,numread);
		
		if(data==null || data.length() <= 0)
			return;

		if (lingeringData != null) {
			data = lingeringData + data;
		}
				
		String[] array = StringUtil.split(data,'\n');
		/* the last element of this array will be non-newline-terminated lingeringData */
		if (array[array.length-1].length() > 0) {			
			lingeringData = array[array.length-1];
			if (lingeringData.endsWith("\r")) {
				lingeringData = lingeringData.substring(0, lingeringData.length() - 1);
				if (lingeringData.length() <= 0) lingeringData = null;
			}
		} else {
			lingeringData = null;
		}

		for(int i=0; i < array.length-1; i++) {

			String line = array[i];
			if (line.endsWith("\r")) {
				line = line.substring(0, line.length() - 1);
			}
			lineAvailable(line);
		}
	}
	
	public void sendASentence(String sentence) {
	    // not implemented
	}    
	
	public String getState() {
		return state;
	}
}

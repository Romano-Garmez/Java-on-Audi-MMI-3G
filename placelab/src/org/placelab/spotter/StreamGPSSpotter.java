package org.placelab.spotter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.placelab.util.Logger;

/**
 * An NMEAGPSSpotter that provides default implementations for almost
 * everything.  All you need to provide is input and output streams
 * to talk to the gps unit.
 */

public abstract class StreamGPSSpotter extends NMEAGPSSpotter {
	private InputStream in = null;
	private PrintWriter out=null;
	private ScanThread scanThread=null;
	
	public void open(InputStream in, OutputStream out) throws SpotterException {

		if ( (in == null) || (out == null)) {
			throw new SpotterException("The InputStream or OutputStream supplied was null.");
		}

		this.in = in;
		this.out = new PrintWriter(out);
		
	}
	public void close() throws SpotterException {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				throw new SpotterException(e);
			}
			in = null;
		}
		if (out != null) {
			out.close();
			out = null;
		}
	}
	
	protected void startScanningImpl() {
		scanThread = new ScanThread();
		scanThread.start();
	}
	protected void stopScanningImpl() {
		if (scanThread != null) {
			/* notify the scanning thread that we are stopping */
			scanThread.cancel();
			waitForThread(scanThread);
			scanThread = null;
		}
	}
	
	private class ScanThread extends Thread {
		private static final int POLL_INTERVAL_MILLIS=100;
		
		
		private volatile boolean done = false;
		public synchronized void cancel() {
			if (done) return;
			done = true;
		}

		public void run () {
			while (!done) {
				/* figure out if any data is available for reading */
				try {
					if (in.available() > 0) {
						dataAvailable();
					}
				} catch (IOException e) {
					notifyError();
					return;
				}
				
				try {
					Thread.sleep(POLL_INTERVAL_MILLIS);
				} catch (InterruptedException e) {
					
				}
			}
		}
		
		private synchronized void dataAvailable() throws IOException {
			/*
			 * It's important not to use any sort of reader on top of the InputStream
			 * so to maintain compatibility w/ the J9 serial port streams.  I suspect
			 * there is something quirky about the # of bytes return by available()
			 * that is messing things up.
			 */
			StringBuffer buffer = new StringBuffer();
			while(in.available() > 0) {
				int b = in.read();
				
				if ( (b==-1) || (b=='\n') || (b=='\r') )
					break;
				
				buffer.append((char) b);
			}
			
			if (buffer.length() > 0) 
				lineAvailable(buffer.toString());
			
		}
		
		private void notifyError() {
			done = true;
			measurementAvailable(null);
		}
	}
	
	public void sendASentence(String sentence) {
        if (out == null) return;
        Logger.println("Sending sentence: " + sentence, Logger.HIGH);
        // synchronize so I'm not reading and writing at the same time
        synchronized(this) {
            out.print(sentence + "\r\n");
        }
    }
	
}
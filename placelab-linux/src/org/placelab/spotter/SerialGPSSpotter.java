package org.placelab.spotter;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.TooManyListenersException;

import org.placelab.core.PlacelabProperties;
import org.placelab.util.Logger;

/**
 * The SerialGPSSpotter uses rxtx to communicate with a serial interface
 * gps unit.
 * <p>
 * The System property placelab.gps_device is used to specify the serial port
 * that the gps device is connected to.  placelab.gps_speed may be used to
 * set a non-standard baud rate for communicating with the gps device, but be
 * aware that the NMEA standard specifies 4800 baud and that most gps devices
 * won't function properly at other speeds.
**/
public class SerialGPSSpotter extends NMEAGPSSpotter {
    private String gpsPort = "/dev/tty.usbserial0";
    private SerialPort serialPort;
    private InputStreamReader in;
    private PrintWriter out;
    
    public static NMEAGPSSpotter newSpotter () {
    		String osName = System.getProperty("os.name").toLowerCase();
    		
    		if (osName.equals("windows ce")) 
    			return new SerialJ9GPSSpotter();
    		else 
    			return new SerialGPSSpotter();
    }
    
    public void open() throws SpotterException {
	    try {
	        if(serialPort != null) {
	            in.close();
	            out.close();
	            serialPort.close();
	        }
	        gpsPort = PlacelabProperties.get("placelab.gps_device");
	        if(gpsPort == null) {
	            throw new SpotterException("placelab.gps_device undefined");
	        }
	        int speed = getSerialSpeed();
	        System.out.println("SerialGPSSpotter: Using serial device: " + gpsPort + 
	                " @ " + speed + " baud");
	        serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(gpsPort).open("SerialGPSSpotter", 2000);
	        
	        in = new InputStreamReader(serialPort.getInputStream());
	        out = new PrintWriter(serialPort.getOutputStream());
	        serialPort.setSerialPortParams(speed, 
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);
	        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT &  
	                SerialPort.FLOWCONTROL_RTSCTS_IN);
	        serialPort.setDTR(true);
	        serialPort.setDTR(false);
	        if (in != null && out != null) return;
	    } catch (IOException ioe) {
	    	throw new SpotterException("Aack, broken pipe");
	    } catch (NoSuchPortException nspe) {
	    	throw new SpotterException("Aack, no such port (check placelab.gps_device system property)");
	    } catch (PortInUseException piue) {
	    	throw new SpotterException("Aack, port in use (check rxtx install)");
	    } catch (UnsupportedCommOperationException ucoe) {
	    	throw new SpotterException("Can't set up gps serial parameters");
	    }
    }
    
	private static int getSerialSpeed() {
	    int speed = 4800;
	    try {
	        speed = Integer.parseInt(PlacelabProperties.get("placelab.gps_speed"));
	    } catch (NumberFormatException nfe) { }
	    return speed;
	}
	
	
	public void close() throws SpotterException{
		try {
		    if(in != null) {
		        in.close();
		        in = null;
		    }
            if(out != null) {
                out.close();
                out = null;
            }
		} catch (IOException e) {
			throw new SpotterException(e);
        }
		if(serialPort != null) {
		    serialPort.close();
		    serialPort = null;
		}
	}
	
	private class SerialPortScanner implements SerialPortEventListener {
		private char[] cbuf=null;
		private int filled=0;
		private boolean done = false;
		
		public synchronized boolean start() {
			cbuf = null;
			filled = 0;
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e) {
				return false;
			}
			serialPort.notifyOnDataAvailable(true);
			return true;
		}
		public synchronized void cancel() {
			if (done) return;
			done = true;
			serialPort.notifyOnDataAvailable(false);
			serialPort.removeEventListener();
		}

		public void serialEvent(SerialPortEvent event) {
			if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
				dataAvailable();
			}
		}
		private synchronized void dataAvailable() {
			if (done) return;
			
			/* figure out how much data is available for reading */
			int numAvail=0;
			try {
				numAvail = serialPort.getInputStream().available();
			} catch (IOException e) {
				notifyError();
				return;
			}

			/* see if we have enough room to read in the available data */
			if (cbuf == null || cbuf.length - filled < numAvail) {
				/* we need to grow the size of the buffer */
				int size = (cbuf==null ? 256 : cbuf.length);
				while (size - filled < numAvail) size *=2;
				char[] newBuf = new char [size];
				if (filled > 0) System.arraycopy(cbuf, 0, newBuf, 0, filled);
				cbuf = newBuf;
			}
			/* read the data into our own buffer */
			try {
				int n = in.read(cbuf, filled, numAvail);
				filled += n;
			} catch (IOException e) {
				e.printStackTrace();
				notifyError();
				return;
			}

			/* now read a line at a time from the buffer */
			int index, start;
			index = 0;
			start = 0;
			while (index < filled) {
				if (cbuf[index] == '\n' || cbuf[index] == '\r') {
					StringBuffer sb = new StringBuffer();
					sb.append(cbuf, start, index - start);
					//System.out.println("got line: "+sb.toString());
					lineAvailable(sb.toString());
					/* the lineAvailable() method may have triggered a callback that
					 * resulted in this scan being terminated.  In that case we should
					 * return promptly.
					 */
					if (done) return;
					if (cbuf[index]=='\r' && index+1 < filled && cbuf[index+1]=='\n') index++;
					start = index+1;
				}
				index++;
			}
			if (start < filled) {
				System.arraycopy(cbuf, start, cbuf, 0, filled-start);
				filled = filled-start;
			} else {
				filled = 0;
			}
		}
		private void notifyError() {
			done = true;
			serialPort.notifyOnDataAvailable(false);
			serialPort.removeEventListener();

			measurementAvailable(null);
		}
	}

	private SerialPortScanner scanner=null;
	protected void startScanningImpl() {
		if (serialPort==null) return;
		scanner = new SerialPortScanner();
		scanner.start();
	}
	protected void stopScanningImpl() {
		if (scanner != null) scanner.cancel();
	}
	
    public void sendASentence(String sentence) {
        if(out == null) return;
        Logger.println("Sending sentence: " + sentence, Logger.HIGH);
        // synchronize so I'm not reading and writing at the same time
        synchronized(this) {
            out.print(sentence + "\r\n");
        }
    }
}

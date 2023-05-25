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

import org.placelab.collections.HashMap;
import org.placelab.core.Coordinate;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;
	



/*
 * 
 * 
 * Simple Text Output Format:

The simple text (ASCII) output contains time, position, and velocity data in
the fixed width fields (not delimited) defined in the following table:

    FIELD DESCRIPTION:      WIDTH:  NOTES:
    ----------------------- ------- ------------------------
    Sentence start          1       Always '@'
    ----------------------- ------- ------------------------
   /Year                    2       Last two digits of UTC year
  | ----------------------- ------- ------------------------
  | Month                   2       UTC month, "01".."12"
T | ----------------------- ------- ------------------------
i | Day                     2       UTC day of month, "01".."31"
m | ----------------------- ------- ------------------------
e | Hour                    2       UTC hour, "00".."23"
  | ----------------------- ------- ------------------------
  | Minute                  2       UTC minute, "00".."59"
  | ----------------------- ------- ------------------------
   \Second                  2       UTC second, "00".."59"
    ----------------------- ------- ------------------------
   /Latitude hemisphere     1       'N' or 'S'
  | ----------------------- ------- ------------------------
  | Latitude position       7       WGS84 ddmmmmm, with an implied
  |                                 decimal after the 4th digit
  | ----------------------- ------- ------------------------
  | Longitude hemishpere    1       'E' or 'W'
  | ----------------------- ------- ------------------------
  | Longitude position      8       WGS84 dddmmmmm with an implied
P |                                 decimal after the 5th digit
o | ----------------------- ------- ------------------------
s | Position status         1       'd' if current 2D differential GPS position
i |                                 'D' if current 3D differential GPS position
t |                                 'g' if current 2D GPS position
i |                                 'G' if current 3D GPS position
o |                                 'S' if simulated position
n |                                 '_' if invalid position
  | ----------------------- ------- ------------------------
  | Horizontal posn error   3       EPH in meters
  | ----------------------- ------- ------------------------
  | Altitude sign           1       '+' or '-'
  | ----------------------- ------- ------------------------
  | Altitude                5       Height above or below mean
   \                                sea level in meters
    ----------------------- ------- ------------------------
   /East/West velocity      1       'E' or 'W'
  |     direction
  | ----------------------- ------- ------------------------
  | East/West velocity      4       Meters per second in tenths,
  |     magnitude                   ("1234" = 123.4 m/s)
V | ----------------------- ------- ------------------------
e | North/South velocity    1       'N' or 'S'
l |     direction
o | ----------------------- ------- ------------------------
c | North/South velocity    4       Meters per second in tenths,
i |     magnitude                   ("1234" = 123.4 m/s)
t | ----------------------- ------- ------------------------
y | Vertical velocity       1       'U' (up) or 'D' (down)
  |     direction
  | ----------------------- ------- ------------------------
  | Vertical velocity       4       Meters per second in hundredths,
   \    magnitude                   ("1234" = 12.34 m/s)
    ----------------------- ------- ------------------------
    Sentence end            2       Carriage return, '0x0D', and
                                    line feed, '0x0A'
    ----------------------- ------- ------------------------

If a numeric value does not fill its entire field width, the field is padded
with leading '0's (eg. an altitude of 50 meters above MSL will be output as
"+00050").

Any or all of the data in the text st (except for the st start
and st end fields) may be replaced with underscores to indicate
invalid data.
*/

/**
 * A GPS Spotter that uses the proprietary Garmin text output
 * rather than NMEA.
 */
public class GarminSimpleTextSpotter extends AbstractSpotter {
    private String gpsPort = "/dev/tty.usbserial0";
    private SerialPort serialPort;
    private InputStreamReader in;
    private PrintWriter out;
    boolean isScanning = false;
    private SerialPortScanner scanner = null;
    
   	public void startScanning() {
		startScanning(null);
	}
   	
	public void startScanning(EventSystem evs) {
		if (isScanning)
			throw new UnsupportedOperationException("a continuous scan operation is already in progress");
		isScanning = true;
		if (serialPort == null)
			return;
		scanner = new SerialPortScanner();
		scanner.start();
	}
	
	public void stopScanning() {
		if (!isScanning) 
			return;
		if (scanner != null) 
			scanner.cancel();
		isScanning = false;
	}

    public Measurement getMeasurement() {
    	System.err.println("calling getMeasurement()");
    	return null;
    }
    
    protected void notifyGotMeasurement(EventSystem evs, Measurement m) {
		if (evs==null) {
			notifyGotMeasurement(m);
		} else {
			evs.notifyTransientEvent(new EventListener() {
				public void callback(Object eventType, Object data) {
					notifyGotMeasurement((Measurement)data);
				}
			}, m);
		}
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
	            throw new SpotterException("placelab.gps_device not defined");
	        }
	        int speed = getSerialSpeed();
	        System.out.println("GarminSimpleTextSpotter: Using serial device: " + gpsPort + 
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
	    throw new SpotterException("Spotter could not be created");
    }
    
	private static int getSerialSpeed() {
	    int speed = 9600;
	    try {
	        speed = Integer.parseInt(PlacelabProperties.get("placelab.gps_speed"));
	    } catch (NumberFormatException nfe) { }
	    return speed;
	}
	
	
	public void close() {
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
            System.err.println("SerialGPSSpotter: Couldn't close serial pipe");
            e.printStackTrace();
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
		String line = new String("");

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
				while (size - filled < numAvail) 
					size *=2;
				char[] newBuf = new char [size];
				if (filled > 0) System.arraycopy(cbuf, 0, newBuf, 0, filled);
				cbuf = newBuf;
			}

			/* read the data into our own buffer */
			try {
				char[] buf = new char[numAvail];
				//int n = in.read(cbuf, filled, numAvail);
				int n = in.read(buf, 0, numAvail);
				String bufStr = new String(buf, 0, numAvail);
				line += bufStr;
				
				for(int i = 0; i < line.length(); i++) {
					if (i > 0 && 
					    line.charAt(i-1) == '\r' &&
						line.charAt(i) == '\n' ) {
						lineAvailable(line.substring(0, i+1));
						String newline = line.substring(i+1,line.length());
						line = newline;
						i = 0;
					}
				}
	            //filled += n;
			} catch (IOException e) {
				e.printStackTrace();
				notifyError();
				return;
			}

			return;
			/* now read a line at a time from the buffer */
		}
		private void notifyError() {
			done = true;
			serialPort.notifyOnDataAvailable(false);
			serialPort.removeEventListener();
		}
	}


	public boolean lineAvailable(String st) {
		System.out.println("Sentence (" +st.length() + ") " + st);
		if (st.charAt(0) != '@')
			return false;
		if (st.length() != 57)
			return false;
	    if (st.indexOf("_") != -1) // '_' is invalid
			return false;	
		String year = st.substring(1,3);
		String mon = st.substring(3,5);
		String day = st.substring(5,7);
		String hour =  st.substring(7,9);
		String minute =  st.substring(9,11);
		String second =  st.substring(11,13);
		String latHem = st.substring(13,14);
		String lat = st.substring(14, 16) + "." + st.substring(16,21);
		String lonHem = st.substring(21,22);
		String lon = st.substring(22, 25)+ "."+st.substring(25,30);
		String positionStatus = st.substring(30,31);
		String error = st.substring(31,34);
		String altitude = st.substring(34,34+1+5);

		HashMap t = new HashMap();
		t.put(NMEASentence.TIMEOFFIX, hour+minute+second);
		t.put(NMEASentence.STATUS, "?");
		t.put(NMEASentence.LATITUDE, lat);
		t.put(NMEASentence.LATITUDEHEMISPHERE, latHem);
		t.put(NMEASentence.LONGITUDE, lon);
		t.put(NMEASentence.LONGITUDEHEMISPHERE, lonHem);
		t.put(NMEASentence.SPEEDOVERGROUND, "");
		t.put(NMEASentence.COURSEOVERGROUND, "");
		t.put(NMEASentence.DATEOFFIX, day + mon + year);
		t.put(NMEASentence.MAGNETICVARIATION, "");
		t.put(NMEASentence.MAGNETICVARIATIONDIRECTION, "");
		t.put(NMEASentence.MODE, "A");
		t.put(NMEASentence.GPSQUALITY, "");
		t.put(NMEASentence.NUMOFSATELLITES, "");
		t.put(NMEASentence.HORIZONTALDILUTIONOFPRECISION, "");
		t.put(NMEASentence.ANTENNAHEIGHT, "");
		t.put(NMEASentence.GEOIDALHEIGHT, "");
		t.put(NMEASentence.DIFFERENTIALGPSDATAAGE, "");
		t.put(NMEASentence.DIFFERENTIALREFERENCESTATIONID, "");
		double latitude = Double.parseDouble(lat);
		double longitude = Double.parseDouble(lon);
		if (latHem.equals("W")) 
			latitude = - latitude;
		if (lonHem.equals("S")) 
			longitude = - longitude;
		
		Coordinate twodc = new TwoDCoordinate(latitude, longitude); 
		GPSMeasurement m = new GPSMeasurement(System.currentTimeMillis(), twodc, t);
		
		notifyGotMeasurement(null, m);
		return true;
	}
}

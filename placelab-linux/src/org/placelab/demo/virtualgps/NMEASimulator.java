
package org.placelab.demo.virtualgps;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

/**
 * Outputs NMEA format data from PlaceLab.  Right now we only output a GGA
 * sentence as that's all that's necessary to fool Mappoint.  
 */

public class NMEASimulator implements SpotterListener, EstimateListener {
	
	private InputStreamMultiplexer multiplexer;
	private PrintStream out;
	private BestGuessCompoundTracker tracker;
	
	public NMEASimulator () {
		tracker = new BestGuessCompoundTracker();
		tracker.addEstimateListener(this);
		multiplexer = new InputStreamMultiplexer();
		out = new PrintStream(multiplexer);
	}
	
	public void addTracker (Tracker t) {
		tracker.addTracker(t);
	}
	
	public void addSpotter (Spotter s) {
		s.addListener(this);
	}
	
	
	public void gotMeasurement (Spotter s, Measurement m) {
		
		if (tracker.acceptableMeasurement(m))
			tracker.updateEstimate(m);
	}
	
	public String toNMEA (double n) {
		n = (n < 0) ? -n : n;
		double degrees = Math.floor(n);
		double minutes = (n % degrees) * 60;
		double total = (degrees * 100) + minutes;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(5);
		nf.setMaximumIntegerDigits(5);
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		nf.setGroupingUsed(false);
		return nf.format(total);
	}
	
	public void estimateUpdated (Tracker t, Estimate e, Measurement m) {
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		String hhmmss = twoDigitFormat(cal.get(Calendar.HOUR)) + twoDigitFormat(cal.get(Calendar.MINUTE)) + twoDigitFormat(cal.get(Calendar.SECOND)) + "." + cal.get(Calendar.MILLISECOND);
		
		double lat = ((TwoDCoordinate) e.getCoord()).getLatitude();
		double lon = ((TwoDCoordinate) e.getCoord()).getLongitude();
		
		String latDir = (lat < 0) ? "S" : "N";
		String lonDir = (lon < 0) ? "W" : "E";
		
		String latNMEA = toNMEA(lat);
		String lonNMEA = toNMEA(lon);
		
		System.err.println("Latitude: " + latNMEA + " " + latDir + "  Longitude: " + lonNMEA + " " + lonDir);
		
		if (lat == 0L) {
			out.print(NMEAFactory.GGA(hhmmss, "", "","","", 0, 0, "", "", "", "", ""));
			return;
		}
		
		out.print(NMEAFactory.GGA(
				hhmmss,
				latNMEA,
				latDir,
				lonNMEA,
				lonDir,
				1, // GPS FIX
				5, // we're lying...
				"",
				"0",
				"M",
				"0",
				"M"
		));
		
	}
	
	private String twoDigitFormat (int n) {
		return (n < 10) ? "0" + Integer.toString(n) : Integer.toString(n);
	}
	
	public InputStream getInputStream () {
		return multiplexer.getInputStream();
	}
	
	/**
	 * Masquerades itself as an OutputStream and allows us to write
	 * to one output and have many inputs reading at the same time.
	 */
	class InputStreamMultiplexer extends OutputStream {
		
		/**
		 * From looking at the buffer mechanism, you might think
		 * it would be inefficient but it seems to run pretty quick.
		 */
		class PushInputStream extends InputStream {
			
			private static final int MAX_BUFFER_SIZE = 1024;
			private volatile int[] buffer;
			private volatile boolean pushing;
			private volatile boolean reading;
			
			PushInputStream () {
				buffer = new int[0];
				pushing = false;
				reading = false;
			}
			
			public void push (int b) {
				if (buffer.length >= MAX_BUFFER_SIZE) 
					return;
			
				while (reading);
				
				pushing = true;
				
				int[] bigger = new int[buffer.length+1];
				for (int index=0;index < buffer.length; index++) 
					bigger[index] = buffer[index];
				
				bigger[buffer.length] = b;
				
				buffer = bigger;
				
				pushing = false;
			}
				
			public int read () {
				while (buffer.length == 0) 
					Thread.yield();
				
				while (pushing);
					
				reading = true;
				
				int b = buffer[0];
				
				
				int[] smaller = new int[buffer.length-1];
				for (int index = 1; index<buffer.length; index++) 
					smaller[index-1] = buffer[index];
				buffer = smaller;
				
				reading = false;
				
				return b;
			}
			
			public int available () {
				return buffer.length;
			}
		}
		
		Vector inputs;
		
		public InputStreamMultiplexer () {
			inputs = new Vector();
		}
		
		public void write(int b) {
			for (int i=0;i<inputs.size();i++) {
				((PushInputStream) inputs.elementAt(i)).push(b);
			}
		}
		
		public InputStream getInputStream () {
			PushInputStream i = new PushInputStream();
			inputs.addElement(i);
			return (InputStream) i;
		}
	}

	public void spotterExceptionThrown(Spotter s,SpotterException ex) {
		ex.printStackTrace();
	}
}

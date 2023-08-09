
package org.placelab.stumbler.smallgui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.placelab.spotter.NMEAGPSSpotter;
import org.placelab.spotter.SerialGPSSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.stumbler.LogWriter;
import org.placelab.stumbler.SpotterExtension;
import org.placelab.stumbler.StumblerFunnel;

public class Stumbler {
	
	final static Class[] RECEIVERS = {
			BluetoothReceiver.class,
			WiFiReceiver.class,
			GPSReceiver.class
	};
	
	Vector spotters = new Vector();
	Vector receivers = new Vector();
	StumblerFunnel funnel = new StumblerFunnel();
	LogWriter logWriter = null;
	boolean isStumbling = false;
	String logFile = null;
	boolean logIsListener = false;
	
	private static Stumbler myself;
	
	public static Stumbler getStumbler () {
		return myself;
	}
	
	public Stumbler () {
		myself = this;		
	}
	
	private void loadReceiver (Spotter s) {
		s = ((SpotterExtension) s).getSpotter();
		for (int i = 0; i<RECEIVERS.length; i++ ) {
			Receiver r;
			
			try {
				r = (Receiver) RECEIVERS[i].newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
			
			Class[] rClasses = r.getSupportedSpotters();
			
			for (int n=0;n<rClasses.length;n++) {
				if (rClasses[n].getName().equals(s.getClass().getName())) {
										
					if (!receivers.contains(r)) {
						receivers.addElement(r);						
						s.addListener(r);
						break;
					}
				}
			}
		}
	}
	
	public void addSpotter (Spotter s) {
		if (!spotters.contains(s)) {
			
			SpotterExtension sext;
			
			if (s instanceof NMEAGPSSpotter) {
				sext = new SpotterExtension(s, false, SpotterExtension.GPS_STALE_TIME);				
			} else { 
				sext = new SpotterExtension(s, false, 0);
			}
			
			if (s instanceof NMEAGPSSpotter) {
				funnel.addTriggerSpotter(sext);
			} else { 
				funnel.addDependentSpotter(sext);
				sext.addListener(funnel);
			}
			
			spotters.addElement(sext);
			loadReceiver(sext);
		}
	}
	
	public Spotter getSpotter (Class c) {
		for (Enumeration e = getSpotters(); e.hasMoreElements(); ) {
			SpotterExtension sext = (SpotterExtension) ((Spotter) e.nextElement());
			Spotter s = ((SpotterExtension) sext).getSpotter();
			Class sc = s.getClass();
			if (sc.equals(c)) {
				return sext;
			}
		}
		
		return null;
	}
	
	public Enumeration getSpotters () {
		return spotters.elements();
	}
	
	public Enumeration getReceivers () {
		return receivers.elements();
	}
	
	public Receiver getReceiver (Class spotterClass) {
		for (Enumeration e = receivers.elements(); e.hasMoreElements(); ) {
			Receiver r = (Receiver) e.nextElement();
			
			Class[] supSpotters = r.getSupportedSpotters();
			
			for (int i = 0; i<supSpotters.length;i++) {
				if (supSpotters[i].equals(spotterClass))
					return r;
			}
		}
		
		return null;
	}
	
	public void startStumbling () {
		
		if (logWriter == null) {
			try {
				newLog();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			if (!logIsListener) {
				funnel.addUpdateListener(logWriter);
				logIsListener = true;
			}
		}
		
		if (funnel.isSuspended())
			funnel.resumeListen();
		else
			funnel.start();
		
		isStumbling = true;
	}
	
	public void stopStumbling () {
		
		// don't actually shutdown, just pause the funnel
		
		funnel.suspendListen();
		
		isStumbling = false;
	}

	public boolean isStumbling () {
		return isStumbling;
	}
	
	public void newLog () throws IOException {
		logFile = LogWriter.makeTempFile();
		logWriter = new LogWriter(logFile);
	}
	
	public void openLog (String path) throws IOException {
		logWriter = new LogWriter(path, true);
		logFile = path;
	}
	
	public void saveLog (String path) throws IOException {
		if (logWriter != null) {
			logWriter.setOutputFile(path);
			logFile = path;
		}
	}
	
	public void saveLogAs (String path) throws IOException {
		if (logWriter != null) {
			logWriter.saveFile(path);
		}
	}
	
	public String getLogFile() {
		return logFile;
	}

	public void shutdown () {
		funnel.shutdown();
		logWriter.shutdown();
	}
	
	public static void main (String[] args) {
	    
		Stumbler stumbler = new Stumbler();
		
		WiFiSpotter wifi = new WiFiSpotter();
		NMEAGPSSpotter gps = SerialGPSSpotter.newSpotter();
		
		stumbler.addSpotter(wifi);
		stumbler.addSpotter(gps);
		Spotter spot = stumbler.getSpotter(gps.getClass());
		
		try {
			Spotter w = stumbler.getSpotter(WiFiSpotter.class);
			
			w.open();
			w.startScanning();
			
		} catch (SpotterException e) {
			e.printStackTrace();
			return;
		}
		
		new View();
		
		while (!View.shell.isDisposed()) {
			if (!View.display.readAndDispatch()) {	
				View.display.sleep();
			}
		}
		
		View.display.dispose();
		
		stumbler.stopStumbling();
		stumbler.shutdown();
		System.exit(0);
		
	}
}

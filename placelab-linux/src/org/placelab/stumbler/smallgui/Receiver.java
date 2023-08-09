package org.placelab.stumbler.smallgui;

import java.util.Enumeration;
import java.util.Vector;

import org.placelab.core.Measurement;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;

public abstract class Receiver implements SpotterListener {
	
	private Vector listeners = new Vector(0);
	
	public Receiver (Class measurementClass, Class readingClass) {
		this(new Class[][]{
				{
					measurementClass,
					readingClass
				}
		});
	}
	
	public Receiver (Class[][] classTypes) {
	}
	
	public void addListener (ReceiverListener listener) {
		if (!listeners.contains(listener))
			listeners.addElement(listener);
	}
	
	
	public void gotMeasurement (Spotter s, Measurement m) {
		if (m == null)
			return;
		
		gotMeasurementImpl(s, m);
		
		for (Enumeration el = listeners.elements(); el.hasMoreElements(); ){ 
			((ReceiverListener) el.nextElement()).receiverUpdated(this);
		}
	}
	
	public abstract void gotMeasurementImpl (Spotter s, Measurement m);
	
	public void spotterExceptionThrown (Spotter s, SpotterException e) {
		// who cares
	}
	
	public abstract Class[] getSupportedSpotters ();
	
	public abstract String getType (); 
}

package org.placelab.jsr0179;


import javax.microedition.location.Location;
import javax.microedition.location.AddressInfo;
import javax.microedition.location.QualifiedCoordinates;
import org.placelab.client.tracker.Estimate;
import org.placelab.core.TwoDCoordinate;
import org.placelab.client.tracker.TwoDPositionEstimate;

public class LocationImpl extends Location {
	
	private Estimate estimate;
	private String extraInfo = null;
	
	public LocationImpl(Estimate estimate) {
		this(estimate, null);
	}
	
	public LocationImpl(Estimate estimate, String extraInfo) {
		this.estimate = estimate;
		
		if (extraInfo == null)
			this.extraInfo = "";
		else
			this.extraInfo = extraInfo;
	}
	
	public AddressInfo getAddressInfo () {
		// should look up address info in landmark store
		return null;
	}
	
	public int getLocationMethod () {
		return Location.MTY_TERMINALBASED | Location.MTE_SHORTRANGE;
	}
	
	public long getTimestamp () {
		// if it's in invalid location, presumably the timestamp doesn't matter.
		if (estimate == null)
			return System.currentTimeMillis();
		
		return estimate.getTimestamp();
	}
	
	public String getExtraInfo () {
		if (estimate == null)
			return extraInfo;
		
		return estimate.toString();
	}
	
	public QualifiedCoordinates getQualifiedCoordinates () {
		
		if (estimate == null)
			return null;
		
		// TODO insert some idea of accuracy into these coordinates...
		TwoDCoordinate c = ((TwoDPositionEstimate)estimate).getTwoDPosition();
		return new QualifiedCoordinates(c.getLatitude(), c.getLongitude(), 0F, (float) ((TwoDPositionEstimate) estimate).getStdDev(), Float.NaN);
	}
	
	public float getCourse () {
		// TODO get heading info
		return 0F;
	}
	
	public float getSpeed () {
		// TODO get speed info
		return 0F;
	}
	
	public boolean isValid () {
		if (estimate == null) 
			return false;
		
		return !Double.toString(((TwoDCoordinate) estimate.getCoord()).getLongitude()).equals("NaN");
	}
}

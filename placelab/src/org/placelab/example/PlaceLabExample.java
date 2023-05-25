package org.placelab.example;

import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.Coordinate;
import org.placelab.core.Measurement;
import org.placelab.spotter.LogSpotter;

/**
 * This sample shows how to use the PlacelabWithProxy object to manage
 * spotters and trackers for you.
 */
public class PlaceLabExample implements EstimateListener {
	Estimate lastEstimate=null;

	public static void main(String[] args) {
		try {
			PlacelabWithProxy placelab;			
			if (args.length == 0) {
				// Create a default placelab (with a live spotter)
				placelab = new PlacelabWithProxy();
			} else {
				// Create a placelab (with an explicit log spotter)
				placelab = new PlacelabWithProxy(LogSpotter.newSpotter(args[0]),
				                        null, // use default tracker
										null, // use default mapper
				                        2000  // poll spotter every 2s
				                        );
			}
			placelab.addEstimateListener(new PlaceLabExample());
			placelab.createProxy();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
			System.out.println("Estimated position: " + e.getCoord());
			
			if (lastEstimate != null) {
				Coordinate c =  e.getCoord();
				Coordinate last = lastEstimate.getCoord();
				System.out.println("Estimate moved " + c.distanceFromAsString(last) + " meters.");
			}
			lastEstimate = e;
	}
}

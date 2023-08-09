
package org.placelab.demo.virtualgps;

import org.placelab.client.tracker.CompoundEstimate;
import org.placelab.client.tracker.CompoundTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.collections.Iterator;

/**
 * A slightly smarter CompoundTracker that returns whichever estimate
 * has the lowest standard deviation.
 */

public class BestGuessCompoundTracker extends CompoundTracker {

	public Estimate getEstimate () {
		CompoundEstimate cEstimate = (CompoundEstimate) super.getEstimate();
		
		float lowestStdDev = -1.0F;
		Estimate bestEstimate = null;
		
		
		for (Iterator i = cEstimate.getEstimates().iterator(); i.hasNext();) {
			Estimate e = (Estimate) i.next();
			
			float stdDev = Float.parseFloat(e.getStdDevAsString());
			
			if (lowestStdDev == -1.0F)
				lowestStdDev = stdDev;
			
			if (Math.min(lowestStdDev, stdDev) == stdDev) {
				lowestStdDev = stdDev;
				bestEstimate = e;
			}
			
		}
		
		// never happens
		if (bestEstimate == null)
			return null;
		
		return bestEstimate;
		
	}
	
}

/*
 * Created on 13-Aug-2004
 *
 */
package org.placelab.client.tracker;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Coordinate;
import org.placelab.core.FixedTwoDCoordinate;
import org.placelab.core.Measurement;
import org.placelab.core.PositionMeasurement;
import org.placelab.core.Types;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.Mapper;
import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;

/**
 * The IntersectionTracker performs basic "fusion" by taking both
 * PositionMeasurements and BeaconMeasurements and creating Estimates
 * that take both types into account.
 * <p>
 * The aims of this tracker are: 
 * <ol>
 * <li> to work on both fixed-point and floating-point platforms,
 * <li> to accept any PositionMeasurement or BeaconMeasurement,
 * <li> to simply but effectively fuse together whatever information is available
 * </ol>
 * <p>
 * It maintains a list of "loci" of recently seen BeaconReadings and PositionMeasurements
 * It computes Estimates by taking the intersection of the bounding boxes of the loci
 * These bounding boxes expand as the locus gets older (thus making the position less accurate)   
 * 
 * 
 *
 */
public final class IntersectionTracker extends BeaconTracker {
    
    private Estimate curEst;

    long maxLife = 600000; // 10 minutes
    
    private class Locus {
        protected long initialRadiusFlong; 
        protected Coordinate centre;
        protected long timestamp;
        
        public Locus(long timestamp,Coordinate centre,int initialRadius) {
            this.centre = centre;
            this.timestamp = timestamp;
            try {
//                System.out.println("new Locus with radius " + initialRadius);
                this.initialRadiusFlong = FixedPointLong.intToFlong(initialRadius);
            } catch(FixedPointLongException fple) {
                this.initialRadiusFlong=0;
                System.err.println("Invalid radius creating locus");
            }
        }
    }
    
    public IntersectionTracker(Mapper m) {
        super(m);
        loci = new HashMap();
    }

    private HashMap loci;
    private Object lociSyncObj = new Object();
    public Object lociSyncObject() {
        return lociSyncObj;
    }

    
    /** accepts BeaconMeasurements and PositionMeasurements
     * N.B. Overrides BeaconTracker.acceptableMeasurement
     */
    public boolean acceptableMeasurement(Measurement m) {
        if(m == null) return false;
        if(m instanceof BeaconMeasurement) return true;
        if(m instanceof PositionMeasurement) return true;
        return false;
    }
    
    /**
     * @see org.placelab.client.tracker.Tracker#updateEstimateImpl(org.placelab.core.Measurement)
     */
    protected synchronized void updateEstimateImpl(Measurement m) {
    	if(m == null) return;
        synchronized(lociSyncObject()) {
        	if(m instanceof PositionMeasurement) {
        		PositionMeasurement pm = (PositionMeasurement)m;
        		if(pm.getPosition().isNull()) return;
            	if(!loci.containsKey(pm.getType()) || pm.getTimestamp() > ((Locus)loci.get(pm.getType())).timestamp) {
            		loci.put(pm.getType(),new Locus(pm.getTimestamp(),pm.getPosition(),getInitRadius(pm)));            
            	}
            } else if(m instanceof BeaconMeasurement) {
            	Iterator i = ((BeaconMeasurement)m).iterator();
            	while(i.hasNext()) {
            		BeaconReading br = (BeaconReading) i.next();
            		Beacon b = findBeacon(br.getId(),(BeaconMeasurement)m,(curEst==null || curEst.getCoord().isNull())?null:curEst.getCoord(), 0);

            		if(b==null || b.getPosition().isNull()) {
            			continue;
            		}
            		if(!loci.containsKey(b) || m.getTimestamp() > ((Locus)loci.get(b)).timestamp) {
            			loci.put(b,new Locus(m.getTimestamp(),b.getPosition(),getInitRadius(b)));
            		}
            	}
            }
        }
    }
        
    private int getInitRadius(PositionMeasurement pm) {
        // should not be doing this here!
        return 50;
    }
    
    private int getInitRadius(Beacon b) {
        return b.getMaximumRange();
    }
    
    /**
     * @see org.placelab.client.tracker.Tracker#getEstimate()
     */
    public Estimate getEstimate() {
        curEst= getEstimateImpl();
        return curEst==null ? Types.newEstimate(System.currentTimeMillis(),Types.newCoordinate(),"0") : curEst;
    }
    
    public void setMaxLife(long maxLife) {
    	this.maxLife = maxLife;
    }
    
    public synchronized Estimate getEstimateImpl() {
    	// this should be in a subclass, with this class being abstract
        // begin sheer hackery
        
        // assumptions 
        final long SPEED_FLONG = FixedPointLong.intToFlongSafe(5); // 5 meters per second
        final long MAXRADIUS_FLONG = FixedPointLong.intToFlongSafe(10000); // 10km
        long north=0,south=0,east=0,west=0;
        long now = System.currentTimeMillis();
        long minRadiusFlong = 0;
        
        while (true) {
			boolean first = true;
			minRadiusFlong = 0;
			long oldestTime = 0;
			Object oldestKey = null;

			synchronized (lociSyncObject()) {
				Iterator i = loci.keySet().iterator();

				while (i.hasNext()) {
					Object key = i.next();
					Locus l = (Locus) loci.get(key);
					long curRadiusFlong = l.initialRadiusFlong
							+ ((now - l.timestamp) / 1000L) * SPEED_FLONG;
					//System.out.println("initial:
					// "+FixedPointLong.flongToString(l.initialRadiusFlong));
					//System.out.println("cur radius:
					// "+FixedPointLong.flongToString(curRadiusFlong));

					// cull old readings
					if (curRadiusFlong > MAXRADIUS_FLONG
							|| now - l.timestamp > maxLife || l.centre.isNull()) {
						i.remove(); // remove last key
						//System.out.println("REMOVED locus");
						continue;
					}
					// make sure its a flong (simplest way to be
					// coordinate-type-and-platform-agnostic)
					FixedTwoDCoordinate centre;
					if (l.centre instanceof FixedTwoDCoordinate) {
						centre = (FixedTwoDCoordinate) l.centre;
					} else {
						centre = new FixedTwoDCoordinate(l.centre
								.getLatitudeAsString(), l.centre
								.getLongitudeAsString());
						//System.out.println("Making new FixedTwoDCoordinate
						// out of " + l.centre + " : " + centre);
					}
					FixedTwoDCoordinate northeast, southwest;
					try {
						northeast = centre.translateFixed(curRadiusFlong,
								curRadiusFlong);
						southwest = centre.translateFixed(-curRadiusFlong,
								-curRadiusFlong);
					} catch (FixedPointLongException fple) {
						throw new ArithmeticException(
								"IntersectionTracker.getEstimateImpl: FixedPointLongException: "
										+ fple);
					}
					//System.out.println("current diagonal " +
					// northeast.distanceFromAsString(southwest));
					long curNorth = northeast.getLatitudeFlong();
					long curEast = northeast.getLongitudeFlong();
					long curSouth = southwest.getLatitudeFlong();
					long curWest = southwest.getLongitudeFlong();
					if (first) {
						north = curNorth;
						east = curEast;
						south = curSouth;
						west = curWest;
						minRadiusFlong = curRadiusFlong;
						oldestTime = l.timestamp;
						oldestKey = key;
						//System.out.println("First box " +
						// FixedPointLong.flongToString(curNorth) + "," +
						// FixedPointLong.flongToString(curEast) + "," +
						// FixedPointLong.flongToString(curSouth) + "," +
						// FixedPointLong.flongToString(curWest));
						first = false;

					} else {
						//System.out.println("Merging box " +
						// FixedPointLong.flongToString(curNorth) + "," +
						// FixedPointLong.flongToString(curEast) + "," +
						// FixedPointLong.flongToString(curSouth) + "," +
						// FixedPointLong.flongToString(curWest));
						north = Math.min(north, curNorth);
						east = Math.min(east, curEast);
						south = Math.max(south, curSouth);
						west = Math.max(west, curWest);
						minRadiusFlong = Math.min(minRadiusFlong,
								curRadiusFlong);
						if (oldestTime > l.timestamp) {
							oldestTime = l.timestamp;
							oldestKey = key;
						}
						//System.out.println("Result box " +
						// FixedPointLong.flongToString(north) +
						// ","+FixedPointLong.flongToString(east)+","+FixedPointLong.flongToString(south)+","+FixedPointLong.flongToString(west));
					}
				} //inner while
			}//synchronized
			
			if (first) {
				return null;
			}
			if (north >= south && east >= west) {
				// the test needs to handle dateline issues
				// got intersection
				break;
			}

			// NO INTERSECTION

			// First, remove oldest locus if it is older than some
			// threshold.
			// Threshold should be high enough that we do not experience
			// thrashing due to not seeing a visible beacon
			long MAXLIFE_CLASH = 60000; // 1 minute
			//System.out.println("Oldest is " + (now - oldestTime));
			if (now - oldestTime > MAXLIFE_CLASH) {
				//System.out.println("Removed locus due to age: " +
				// oldestKey);
				synchronized (lociSyncObject()) {
					Object x = loci.remove(oldestKey);
					continue;
				}
			}

			// If no locus is that old, then it's likely that we've got bad
			// stumbling data.
			// try to compensate for this error and return
			try {
				FixedTwoDCoordinate northeast = new FixedTwoDCoordinate(north,
						east);
				FixedTwoDCoordinate southwest = new FixedTwoDCoordinate(south,
						west);
				minRadiusFlong += northeast.distanceFromFlong(southwest);
			} catch (FixedPointLongException fple) {
				throw new ArithmeticException(
						"IntersectionTracker.getEstimateImpl: FixedPointLongException: "
								+ fple);
			}
			break;
		} //while
        
        if(System.getProperty("microedition.configuration") == null) {
	        try {
	            Coordinate estCentre = Types.newCoordinate(FixedPointLong.flongToString((north+south)/2),FixedPointLong.flongToString((east+west)/2));
	            Estimate resultEstimate = Types.newEstimate(now, estCentre, FixedPointLong.flongToString(minRadiusFlong));
		        //System.out.println("Final estimate: " + resultEstimate);
	            return resultEstimate;
	        } catch(FixedPointLongException fple) {
	            return null;
	        }
        } else {
	        // this way won't work on the desktop
	        return new FixedTwoDPositionEstimate(now,new FixedTwoDCoordinate((north+south)/2,(east+west)/2),minRadiusFlong);
        }
    }

    /**
     * @see org.placelab.client.tracker.Tracker#updateWithoutMeasurement(long)
     */
    public synchronized void updateWithoutMeasurement(long timeSinceMeasurementMillis) {
        // not modelling velocity, so no-op here
    }

    /**
     * @see org.placelab.client.tracker.Tracker#resetImpl()
     */
    protected void resetImpl() {
    	synchronized(lociSyncObject()) {
    		loci = new HashMap();
    	}
    }

    //Avoid concurrent modification exceptions when passing the
    //original iterator out
    public Iterator getLociBeacons() {
    	synchronized(lociSyncObject()) {
    		if(loci == null) {
    			loci = new HashMap();
    		}
    		return loci.keySet().iterator();
    	}
    }
    
    /** Artificially age (that is, pay less attention to) currently cached readings */
    public synchronized void advanceTimeMillis(long timeMillis) {
    	synchronized(lociSyncObject()) {
    		Iterator i = loci.keySet().iterator();
    		while(i.hasNext()) {
    			Object key = i.next();
    			Locus l = (Locus) loci.get(key);
    			l.timestamp-=timeMillis;
    		}
    	}
    }
    
    public synchronized int lociSize() {
    	synchronized(lociSyncObject()) {
    		if(loci == null) return -1;
    		return loci.size();
    	}
    }
}

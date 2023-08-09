package org.placelab.demo.mapview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.FilteredEstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.BluetoothBeacon;
import org.placelab.mapper.GSMBeacon;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.WiFiBeacon;
import org.placelab.util.Cmdline;
import org.placelab.util.swt.Glyph;
import org.placelab.util.swt.GlyphBeacon;

public class APOverlay extends MapViewOverlay implements EstimateListener {

    Mapper mapper;
    protected HashMap beaconHash, currentBeaconMap;
    protected Glyph top;
    protected Color beaconColor = null;
    protected boolean drawNames = false;
    
    public APOverlay(MapView view, Mapper mapper) {
        super(view);
        this.mapper = mapper;
    }
    
    public APOverlay(MapView view, PlacelabWithProxy daemon) {
        super(view);
        daemon.getTracker().addEstimateListener(new FilteredEstimateListener(this, FILTER_TIME, FilteredEstimateListener.FILTER_BY_TIME));
        this.mapper = daemon.getMapper();
        // APOverlay is invisible by default because its so darn slow
        this.setVisible(false);
    }
    
    public void setDefaultBeaconColor(Color aColor) {
    	beaconColor = aColor;
    	Iterator i = glyphs.iterator();
    	while(i.hasNext()) {
    		GlyphBeacon gb = (GlyphBeacon)i.next();
    		gb.setDefaultColor(aColor);
    	}
    }
    
    public void setDrawNames(boolean flag) {
    	this.drawNames = flag;
    	this.loadBeaconList();
    }

    public void setDaemon(PlacelabWithProxy daemon) {
    	daemon.getTracker().addEstimateListener(new FilteredEstimateListener(this, FILTER_TIME, FilteredEstimateListener.FILTER_BY_TIME));
    	mapper = daemon.getMapper();
    }

    public void mapChanged(MapBacking newMap) {
        if(this.isVisible()) {
            this.loadBeaconList();
        }
    }
    
    public void setVisible(boolean flag) {
        super.setVisible(flag);
		// if we've never loaded APs for the current map, do so now
		// once the aps are loaded, only the above line applies, meaning
		// that toggling is fast.  In this way, though, map switching can
		// also be fast if the user has no interest in viewing aps, since
		// they are only reloaded at switch time if aps are shown.
		if(flag && this.beaconHash == null) loadBeaconList();
    }

    /* (non-Javadoc)
     * @see org.placelab.demo.mapview.MapViewOverlay#getTopGlyph()
     */
    public Glyph getTopGlyph() {
        return top;
    }

    /* (non-Javadoc)
     * @see org.placelab.demo.mapview.MapViewOverlay#getName()
     */
    public String getName() {
        return "beacons";
    }
    
    public void loadBeaconList() {
		if ("true".equalsIgnoreCase(PlacelabProperties.get("placelab.disableapdrawing"))
				|| (Cmdline.getArg("disableapdrawing") != null) || (view.getMapData() == null)) {
			beaconHash = null;
			return;
		}
		// dispose all the old aps first
		if(beaconHash != null) {
			Iterator it= beaconHash.values().iterator();
			while(it.hasNext()) {
				GlyphBeacon a = (GlyphBeacon)it.next();
				a.dispose();
			}
			beaconHash.clear();
		}
		try {
		    holder.freeze();
			Iterator it = null;
			it = mapper.query(
			        new TwoDCoordinate(view.getMapData().getOriginLat(),
			                view.getMapData().getOriginLon()),
			        new TwoDCoordinate(view.getMapData().getMaxLat(),
			                view.getMapData().getMaxLon()));
			if(it == null) return;
			int i=0;
			int j=0;
			beaconHash = new HashMap();
			currentBeaconMap = new HashMap(); // remember from one estimate to the next
			GlyphBeacon first = null;
			while (it.hasNext()) {
				Beacon b = (Beacon)it.next();
				TwoDCoordinate coord = (TwoDCoordinate) b.getPosition();
				int x = view.longitudeToPixels(coord.getLongitude());
				int y = view.latitudeToPixels(coord.getLatitude());
				GlyphBeacon c = new GlyphBeacon(holder, SWT.NONE,
							x, y);
				if(beaconColor != null) {
					c.setDefaultColor(beaconColor);
				}
				if(drawNames) {
					if(b instanceof WiFiBeacon) {
						c.setName(((WiFiBeacon)b).getSsid());
					} else if(b instanceof BluetoothBeacon) {
						c.setName(((BluetoothBeacon)b).getHumanReadableName());
					} else if(b instanceof GSMBeacon) {
						c.setName(((GSMBeacon)b).getNetworkName());
					}
				}
				if (first == null) {
					first = c;
				}
				beaconHash.put(b.getId(),c);
				this.glyphs.add(c);
				i++;
				j++;
			}
			if (first != null) {
			    top = first;
				view.overlayHasNewTopGlyph(this);		
			}
			GlyphBeacon.setShowSignalStrength(!"false".equalsIgnoreCase(PlacelabProperties.get("placelab.showrssi")));
			//GlyphBeacon.setShowBeacons(!"false".equalsIgnoreCase(PlacelabProperties.get("placelab.showaps")));
//			System.out.println(i + " APs will be shown on the map.");
		
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
		    holder.thaw();
		}
	}

	public void lightBeacons(BeaconMeasurement seen) {
		if (beaconHash == null || !isVisible()) { //not lighting APs
			return;
		}
		
		// if we saw it last time, and not this time, unlight it
		HashMap oldBeaconMap = currentBeaconMap;
		currentBeaconMap = new HashMap();
		
		// light all the new ones
		for (int i=0; i<seen.numberOfReadings(); i++) {
			GlyphBeacon c = null;
			Beacon beacon = mapper.findBeacon(seen.getReading(i).getId());
			if (beacon != null && beacon.getId() != null) {
				c = (GlyphBeacon)beaconHash.get(beacon.getId());
			}
			if (c != null) {
				c.setSeen(true, seen.getReading(i).getNormalizedSignalStrength());
				currentBeaconMap.put(beacon.getId(), "");
			}
		}
		
		// Unlight all the old ones that aren't in the new list
		for (Iterator it=oldBeaconMap.keySet().iterator(); it.hasNext(); ) {
			String id = (String) it.next();
			if (! currentBeaconMap.containsKey(id)) {
				GlyphBeacon c = (GlyphBeacon)beaconHash.get(id);
				c.setSeen(false, -1);
			}
		}
	}

    public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
        if(m instanceof BeaconMeasurement) {
            lightBeacons((BeaconMeasurement)m);
        }
    }
    

}

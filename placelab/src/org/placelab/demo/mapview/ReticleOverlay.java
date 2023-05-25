package org.placelab.demo.mapview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CompoundEstimate;
import org.placelab.client.tracker.CompoundTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.FilteredEstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.client.tracker.TwoDPositionEstimate;
import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.util.swt.Glyph;
import org.placelab.util.swt.GlyphReticle;

/**
 * Implements the reticle that tracks movement on the mapviews by listening to
 * updates from placelab.
 */
public class ReticleOverlay extends MapViewOverlay implements EstimateListener {

    protected GlyphReticle[] reticles;
    public static int reticleColors[] = {SWT.COLOR_RED,SWT.COLOR_BLUE,SWT.COLOR_GREEN,SWT.COLOR_DARK_MAGENTA,SWT.COLOR_CYAN};
    public boolean autoscroll = true;
	static final double RETICLE_SCALE_FACTOR = 10.0;
    
    public ReticleOverlay(MapView view, PlacelabWithProxy daemon) {
        super(view);
        int size = 1;
        Tracker tracker = daemon.getTracker();
        if(tracker instanceof CompoundTracker) {
            size = ((CompoundTracker)tracker).getTrackers().size();
        }
        tracker.addEstimateListener(new FilteredEstimateListener(this, FILTER_TIME, FilteredEstimateListener.FILTER_BY_TIME));
        reticles = new GlyphReticle[size];
        for(int i = 0; i < size; i++) {
            reticles[i] = new GlyphReticle(holder, SWT.NONE);
            glyphs.add(reticles[i]);
            if("false".equalsIgnoreCase(PlacelabProperties.get("placelab.showconfidence"))) {
                reticles[i].setDrawConfidence(false);
            }
            reticles[i].setVisible(this.isVisible());
            reticles[i].enableMouseEvents(false);
            reticles[i].setWidth(2);
            int idx = i;
			if (i > reticleColors.length) {
				idx = reticleColors.length-1;
			}
			reticles[i].setForeground(view.getDisplay().getSystemColor(reticleColors[idx]));
        }
    }
    
    public void setDaemon(PlacelabWithProxy daemon) {
        Tracker tracker = daemon.getTracker();
        tracker.addEstimateListener(new FilteredEstimateListener(this, FILTER_TIME, FilteredEstimateListener.FILTER_BY_TIME));
    }
    

    public void setAutoScroll(boolean scroll) {
        if(scroll) {
            Point p = reticles[0].getLocation();
            view.doscroll(p.x, p.y);
        }
        autoscroll = scroll;
    }
    
    public void mapChanged(MapBacking map) {
        for(int i = 0; i < reticles.length; i++) {
            reticles[i].setZoom(view.getZoom());
        }
    }
    
    public void mapZoomed(double zoom) {
        if(autoscroll) {
            Point p = reticles[0].getLocation();
            view.doscroll(p.x, p.y);
        }
    }

    public String getName() {
        return null;
    }

    double totErr = 0.0;
    int cnt=0;
    int warmup =50;
    public void estimateUpdated(Tracker t, Estimate est, Measurement m) {
        if(est instanceof CompoundEstimate) {
            CompoundEstimate ce = (CompoundEstimate)est;
        	if (ce.getEstimates().size() == 2) {
        		warmup--;
        		if (warmup <=0) {
            		totErr += ((Estimate)ce.getEstimates().get(0)).getCoord().distanceFromInMeters(
            				((Estimate)ce.getEstimates().get(1)).getCoord());
        			cnt++;
            		System.out.println(cnt + " " + (totErr/cnt));
        		}
        	}
            for(int i = 0; i < ce.getEstimates().size(); i++) {
                Estimate e = (Estimate)ce.getEstimates().get(i);
                plotReticle(t, e, i);
            }
        } else {
            plotReticle(t, est, 0);
        }
    }
    
    public void plotReticle(Tracker t, Estimate est, int reticleIdx) {
		//System.out.println("plotting reticle");
		if(view.getMapData() == null) return;
		if (est == null) {System.out.println("boo!"); System.exit(0); }
		if(est.getCoord().isNull()) {
		    this.reticles[reticleIdx].setVisible(false);
		    return;
		} else {
		    this.reticles[reticleIdx].setVisible(true);
		}
		
		double meanDevLat;
		TwoDCoordinate pos = (TwoDCoordinate)est.getCoord();

		meanDevLat = pos.metersToLatitudeUnits(((TwoDPositionEstimate)est).getStdDev());
		if (meanDevLat==0.0) {
			// lets assume 30 meters
			meanDevLat = pos.metersToLatitudeUnits(30);
		}
		double meanDevPix = meanDevLat*view.getPixelsPerLat();

		
		int pixEstX = view.longitudeToPixels(pos.getLongitude());
		int pixEstY = view.latitudeToPixels(pos.getLatitude());
		int pixDeviation = (int)meanDevPix;

		updateReticle(reticleIdx, pixEstX, pixEstY, pixDeviation);

	}
	
	public void updateReticle(int idx, int x, int y, int radius) {
		if(view.getMapData() == null) return;

		//System.out.println("Reticle x: " + x + "y: " + y);
		
		reticles[idx].setVisible(true);
		// reticle will do its own translation for zoom
		reticles[idx].set(x, y, radius);
		// for some reason, the reticle just won't stay on top
		// unless i do this.
		holder.getChild().moveAbove(reticles[idx], null);

		if (autoscroll && !view.isInTigerMode()) {
			Point trans = reticles[idx].getLocation();
			// only do a scroll if the reticle is onscreen.  this avoids the problem
			// of multiple reticles are available, but the last one is offscreen while
			// others are on and so it scrolls it somewhere unexpected.
			if(trans.x < view.mapImageGlyph.getBounds().width &&
			        trans.y < view.mapImageGlyph.getBounds().height) {
			    view.doscroll(trans.x, trans.y);
			}
		}
		
	}
    
	public void setDrawConfidence(boolean flag) {
	    for(int i = 0; i < reticles.length; i++) {
	        reticles[i].setDrawConfidence(flag);
	    }
	    holder.redraw();
	}
	
	public boolean getDrawConfidence() {
	    return reticles[0].getDrawConfidence();
	}
	
	public Rectangle getSuggestedArea() {
        Rectangle newArea;
		Rectangle gbounds;
		int r;
		
		// allow for reticles
		newArea = reticles[0].getBounds();
		for(r=0; r<reticles.length; r++) { 
			GlyphReticle gr = reticles[r];
			gbounds = gr.getBounds();
			gbounds.x -= (int)(((double)gbounds.width) * RETICLE_SCALE_FACTOR / 2.0);
			gbounds.width = (int)(((double)gbounds.width) * RETICLE_SCALE_FACTOR);
			gbounds.y -= (int)(((double)gbounds.height) * RETICLE_SCALE_FACTOR / 2.0);
			gbounds.height = (int)(((double)gbounds.height) * RETICLE_SCALE_FACTOR);
			newArea.union(gbounds);
			//System.out.println("gr " + r + " is at " + gr.getBounds());			
		}
		return newArea;
	}
	
	public Glyph getTopGlyph() {
	    return reticles[reticles.length - 1];
	}
	
}

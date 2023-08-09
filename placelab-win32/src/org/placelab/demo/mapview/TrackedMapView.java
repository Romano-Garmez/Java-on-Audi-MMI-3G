package org.placelab.demo.mapview;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.util.swt.GlyphBeacon;


public class TrackedMapView extends MapView implements EstimateListener {

	protected ReticleOverlay reticles;
	protected APOverlay beacons;
//	protected ParticleFilterOverlay particles;
	
	private boolean      autoScroll;
	private boolean		 autoZoom;
//	protected boolean		showParticles;
	protected SpotterPoker poker=null;
	protected PlacelabWithProxy daemon;
	
	protected Tracker tracker = null;

	public TrackedMapView(Composite parent, int style, PlacelabWithProxy daemon) {
		this(parent, style,
		     getUseScrollBars(parent.getDisplay()), daemon);
	}

	
	public TrackedMapView(Composite parent, int style, boolean useScrollBars, PlacelabWithProxy daemon) {
		super(parent, style, useScrollBars);

		if (daemon != null) {
			setDaemon(daemon);
		}

		autoScroll = false;
		autoZoom = false;
//		showParticles = false;
		this.setMenu(this.getOverlaysMenu(this));
	}
	
	public void setDaemon(PlacelabWithProxy daemon) {
		this.daemon = daemon;
		poker = new SpotterPoker(getDisplay(), daemon);
		tracker = daemon.getTracker();
		// note the order in which the overlays are added is important for two reasons:
		// 1) reticles should draw above beacons, so reticles are added after beacons
		// 2) the estimateListener for the TrackedMapView should be added after the overlays
		// are created because when estimateUpdate is called on the TrackedMapView, the overlays
		// will already be updated and that way dozoom and so forth will work.
		if(beacons == null) {
		    beacons = new APOverlay(this, daemon);
		} else {
			beacons.setDaemon(daemon);
		}
		if(reticles == null) {
		    reticles = new ReticleOverlay(this, daemon);
		} else {
			reticles.setDaemon(daemon);
		}
//		if(particles == null) {
//		    particles = new ParticleFilterOverlay(this, daemon);
//		} else {
//			particles.setDaemon(daemon);
//		}

		daemon.addEstimateListener(this);
	}

	public void setPulseInfo(BeaconMeasurement meas) {
	}

	public SpotterPoker getPoker() {
		return poker;
	}

	public void setAutoScroll(boolean scroll) {		
		reticles.setAutoScroll(scroll);
		if(autoScroll) autoZoom = false;
	}
	public boolean getAutoScroll() {
		return autoScroll;
	}
	public void setAutoZoom(boolean zoom) {
		if(zoom) {
			dozoom();
		}
		autoZoom = zoom;
		if(autoZoom) autoScroll = false;
	}
	public boolean getAutoZoom() {
		return autoZoom;
	}
	
//	public void setShowParticles(boolean showParticles) {
//	    particles.setVisible(showParticles);
//	}
//	public boolean getShowParticles() {
//		if (particles == null) {
//			return false;
//		}
//		return particles.isVisible();
//	}
	
	
	int modCnt=0;
	
	public void estimateUpdated(Tracker t, Estimate est, Measurement m) {
	    //System.out.println("estimate updated");
		if (m instanceof BeaconMeasurement) setPulseInfo((BeaconMeasurement)m);
		if(autoZoom) dozoom(); 
	}
		

	
	
	
	public boolean getShowBeacons() {
		if (beacons == null) {
			return false;
		}
		return beacons.isVisible();
	}
	public void setShowBeacons(boolean flag) {
		if (beacons == null) {
			return;
		}
		beacons.setVisible(flag);
	}

	
	public void handleKeyPress(KeyEvent e) {
		super.handleKeyPress(e);
		if (e.character == 'a') {
			setShowBeacons(!getShowBeacons());
		} else if (e.character == 's') {
			GlyphBeacon.setShowSignalStrength(!GlyphBeacon.getShowSignalStrength());
			mapImageGlyph.redraw(null);
		} else if (e.character == 'r') {
			reticles.setDrawConfidence(reticles.getDrawConfidence());
//		} else if (e.character == 'p') {
//		    particles.setVisible(!particles.isVisible());
		} else if ((e.character == '+') || (e.character == '=')) {
			if (poker != null) {
				poker.setFps(poker.getFps()+1);
			}
		} else if (e.character == '-') {
			if (poker != null) {
				poker.setFps(poker.getFps()-1);
			}
		}
	}

	//public GlyphReticle getReticle() { return reticle; }

}

package org.placelab.stumbler.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.demo.mapview.MapView;
import org.placelab.demo.mapview.TextInfoGlyph;
import org.placelab.mapper.Mapper;
import org.placelab.util.swt.GlyphBeacon;
import org.placelab.util.swt.GlyphGC;

/**
 * A BeaconPlace is a special subclass of a PlaceBacking so that the user can toggle
 * whether they want to see current APs, old aps, 
 */
public class BeaconPlace extends GlyphBeacon {
    
    // current old
	private static Color yellow = new Color(null,255,255,0);
	// historical new
	private static Color red    = new Color(null,255,0,0);
	// current new
	private static Color green  = new Color(null,0,255,0);
	private static Color black = new Color(null, 0, 0, 0);
	// historical old
	private static Color gray   = new Color(null,171,251,249);
	
    protected static int radius = 3;
    
    public BeaconHistory beacon;
    public boolean active;
    public Mapper mapper;
    public MapView mapView;
    
    public static boolean showBeacons2 = true;
    
    public TextInfoGlyph textInfo;
    
	public BeaconPlace(MapView parent, BeaconHistory beacon, Mapper mapper, boolean active) {
	    super(parent.getHolder(), SWT.NONE, parent.latitudeToPixels(beacon.highestRssi.gpsLL.getLatitude()),
	            parent.longitudeToPixels(beacon.highestRssi.gpsLL.getLongitude()));
	    this.beacon = beacon;
	    this.active = active;
	    this.mapper = mapper;
	    this.mapView = parent;
	    this.addMouseListener(new MouseAdapter() {
	        public void mouseDown(MouseEvent e) {
	            moused(e);
	        }
	    });
	}
	
	public static boolean getShowBeacons() {
	    return showBeacons2;
	}
	public static void setShowBeacons(boolean show) {
	    showBeacons2 = show;
	}
	
	/**
	 * Tells the BeaconPlace that the BeaconHistory that it owns has had
	 * its highest rssi updated, and therefore it should update where it draws
	 *
	 */
	public void updateLocation() {
	    int x = mapView.latitudeToPixels(beacon.highestRssi.gpsLL.getLatitude());
	    int y = mapView.longitudeToPixels(beacon.highestRssi.gpsLL.getLongitude());
	    if(x != this.getOriginalLocation().x || y != this.getOriginalLocation().y) {
	        this.setLocation(new Point(x, y));
	    }
	}
	
	public boolean getActive() {
	    return active;
	}
	
	public void setActive(boolean flag) {
	    if(active != flag) {
	        active = flag;
	    	this.redraw(this.getBounds());
	    }
	}
	
	public void paint(PaintEvent e, GlyphGC gc) {
	    if(!getShowBeacons() || !beacon.highestRssi.hasGPS()) return;
	    super.paint(e, gc);
	    gc.setLineWidth(1);
	    gc.setForeground(black);
	    gc.setLineWidth(1);
        gc.setForeground(black);
        gc.drawOval(radius, radius, radius*2, radius*2);
        // draw fill
        gc.setLineWidth(radius - 1);
        gc.setForeground(getColor());
        gc.drawOval(radius, radius, radius * 2 - 1, radius * 2 - 1);
	}
	
	protected Color getColor() {
	    if(beacon.highestRssi.isNew(mapper)) {
	        if(active) {
	            return green;
	        } else {
	            return red;
	        }
	    } else {
	        if(active) {
	            return yellow;
	        } else {
	            return gray;
	        }
	    }
	}
	
	protected String getName() {
	    return beacon.highestRssi.reading.getHumanReadableName();
	}
    
    protected void moused(MouseEvent e) {
        if(textInfo == null) {
            Rectangle r = this.getBounds();
            textInfo = new TextInfoGlyph(this.getHolder(), SWT.NONE);
            textInfo.setLocation(r.x + r.width /2 , r.y + r.height + 2);
            textInfo.setText(getName() + "\n" + beacon.highestRssi.getId());
            textInfo.setVisible(true);
        } else {
            textInfo.setVisible(!textInfo.isVisible());
        }
    }
    
}

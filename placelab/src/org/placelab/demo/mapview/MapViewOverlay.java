package org.placelab.demo.mapview;

import org.eclipse.swt.graphics.Rectangle;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.util.swt.Glyph;
import org.placelab.util.swt.GlyphHolder;

/**
 * A MapViewOverlay exists to group a set of glyphs that are displayed over
 * a MapView.  Specifics of the drawing and updating of those glyphs are left
 * to implementers.
 */
public abstract class MapViewOverlay {

    public MapView view;
    public GlyphHolder holder;
    protected boolean isVisible;
    /** when you create a new glyph, add it here */
    protected LinkedList glyphs;
    
    
    public static final long FILTER_TIME=200; /* milliseconds */
    
    /**
     * Creates a new overlay for the given mapview, and registers
     * the overlay with the mapview.
     */
    public MapViewOverlay(MapView view) {
        this.view = view;
        this.holder = view.getHolder();
        isVisible = true;
        glyphs = new LinkedList();  
        view.addOverlay(this);
    }
    
    /**
     * Sets whether or not the overlay ought to be shown on the mapview.
     */
    public void setVisible(boolean visible) {
        if(isVisible != visible) {
	        holder.freeze();
	        isVisible = visible;
	        Iterator i = glyphs.iterator();
	        while(i.hasNext()) {
	            Glyph g = (Glyph)i.next();
	            g.setVisible(isVisible);
	        }
	        holder.thaw();
        }
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * When the mapview has its maps switched, this callback is given to 
     * allow the overlay to update its data for the new map.
     */
    public abstract void mapChanged(MapBacking newMap);
    
    /**
     * When the mapview is zoomed, this callback is given to allow you to update
     * your overlay.  Generally you shouldn't need to do anything here because your
     * glyphs will automatically resize and point translate for the zoom.
     */
    public void mapZoomed(double newZoom) {
        
    }
    
    /**
     * If your overlay has an area that it would like to keep onscreen if possible, then
     * you can return that here.  If not, just return null.  The mapview may use this information
     * to do autoscrolling.
     */
    public Rectangle getSuggestedArea() {
        return null;
    }
    
    /**
     * MapViewOverlays are layered in the order that they are added to the mapview.
     * For this order to be maintained a MapViewOverlay may be requested to move its
     * glyphs above the argument.  When you create a new set of glyphs in your MapViewOverlay
     * you are requested to notify the view with the top of them by sending
     * overlayHasNewTopGlyph(this)
     */
    public void moveAbove(Glyph below) {
        Iterator i = glyphs.iterator();
        Glyph last = below;
        while(i.hasNext()) {
            Glyph g = (Glyph)i.next();
            g.moveAbove(last);
            last = g;
        }
    }
    
    /**
     * Returns the top glyph in this overlay
     */
    public abstract Glyph getTopGlyph();
        
    
    
    /**
     * Returns the name that is displayed in menus and so forth referring to this overlay.
     * In this way, applications using mapviews can build menus that allow toggling of
     * overlays in a generic fashion.
     */
    public abstract String getName();
    
    public void dispose() {
        view.removeOverlay(this);
        Iterator i = glyphs.iterator();
        while(i.hasNext()) {
            Glyph g = (Glyph)i.next();
            g.dispose();
        }
        glyphs = null;
    }
    
    
    
}

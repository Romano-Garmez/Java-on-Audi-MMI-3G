package org.placelab.util.swt;

import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


public class GlyphComposite extends Glyph {
	private Vector children;
	private Rectangle bounds;
	private GlyphGC gc;

	public Glyph[] getChildren() {
		int size = children.size();
		Glyph[] list = new Glyph[size];
		for (int i=0; i<size; i++) {
			list[i] = (Glyph) children.get(i);
		}
		return list;
	}

	public GlyphComposite(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphComposite(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	GlyphComposite(GlyphHolder holder, int style, boolean isToplevel) {
		super(holder, style, isToplevel);
		init();
	}

	void init() {
		this.bounds   = new Rectangle(0, 0, 0, 0);
		this.gc       = null;
		this.children = new Vector();
	}

	void notifyAddChild(Glyph child) {
		children.add(child);
		redraw(null);
	}
	void notifyRemoveChild(Glyph child) {
		if (isDisposed()) return;
		children.remove(child);
		Rectangle r = child.getBounds();
		this.redraw(r);
	}

	protected Rectangle getBoundsImpl() {
		return new Rectangle(bounds.x, bounds.y, bounds.width, 
				     bounds.height);
	}

	public void setBounds(int x, int y, int width, int height) {
		Rectangle invalidate = getBounds();
		bounds.x      = x;
		bounds.y      = y;
		bounds.width  = width;
		bounds.height = height;
		if (gc != null) gc.resetClipping();
		redraw(invalidate);
	}
	public void setBounds(Rectangle bounds) {
		Rectangle invalidate = getBounds();
		this.bounds = bounds;
		if (gc != null) gc.resetClipping();
		redraw(invalidate);
	}

	public void setLocation(int x, int y) {
		Rectangle invalidate = getBounds();
		bounds.x = x;
		bounds.y = y;
		if (gc != null) gc.resetClipping();
		
		redraw(invalidate);
	}
	public void setLocation(Point aLoc) {
		Rectangle invalidate = getBounds();
		bounds.x = aLoc.x;
		bounds.y = aLoc.y;
		if (gc != null) gc.resetClipping();
		
		redraw(invalidate);
	}

	public void setSize(int width, int height) {
		Rectangle invalidate = getBounds();
		bounds.width  = width;
		bounds.height = height;
		if (gc != null) gc.resetClipping();
		
		redraw(invalidate);
	}
	public void setSize(Point size) {
		Rectangle invalidate = getBounds();
		bounds.width  = size.x;
		bounds.height = size.y;
		if (gc != null) gc.resetClipping();
		
		redraw(invalidate);
	}
	
	public void setZoom(double z) {
		//long time = System.currentTimeMillis();
		super.setZoom(z);
		for(Enumeration i = children.elements(); i.hasMoreElements(); ) {
			Glyph child = (Glyph)i.nextElement();
			child.setZoom(z);
		}
		//time = System.currentTimeMillis() - time;
		//System.out.println("Zoom time: " + time + "ms");
	}


	protected void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);
		Rectangle paintArea = new Rectangle(e.x,e.y,e.width,e.height);
		if (! getBounds().intersects(paintArea)) return;

		// convert the paintArea coordinates to be local to 
		// this glyph 
		Point p = new Point(paintArea.x, paintArea.y);
		p = toGlyph(p);
		paintArea.x = p.x;
		paintArea.y = p.y;

		int saveX = e.x, saveY = e.y;
		e.x = paintArea.x;
		e.y = paintArea.y;

		for (Enumeration it = children.elements(); it.hasMoreElements(); ) {
			Glyph child = (Glyph)it.nextElement();
			if (! child.getBounds().intersects(paintArea) ||
			    ! child.isVisible())
				continue;
			child.paint(e, getGC());
		}

		e.y = saveY;
		e.x = saveX;
	}

	public Glyph pickGlyphAt(int x, int y, boolean checkMouseEnabled) {
		if (! isVisible()) return null;
		if (checkMouseEnabled && !areMouseEventsEnabled()) 
			return null;

		Point p = new Point(x, y);
		p = toGlyph(p);
		
		// jws-reworked to avoid using collections framework 
		Glyph glyph = null;
		Enumeration it = children.elements();
		while(it.hasMoreElements()) { 
			Glyph child = (Glyph)it.nextElement();
			Glyph poss = child.pickGlyphAt(p.x, p.y,checkMouseEnabled);
			if(poss != null) glyph=poss;
		}
		return glyph;
	}

	/*public boolean handleMouseEvent(int eventType, MouseEvent e) {
		if (! areMouseEventsEnabled()) return false;
		super.handleMouseEvent(eventType, e);

		Point p = new Point(e.x, e.y);
		p = toGlyph(p);

		int saveX = e.x, saveY = e.y;
		e.x = p.x;
		e.y = p.y;

		boolean retval = false;
		for (ListEnumeration it = children.listEnumeration(children.size()); 
		     it.hasPrevious(); ) {
			Glyph child = (Glyph)it.previous();
			if (child.isVisible() && child.pointInside(e.x, e.y)) {
				if (child.handleMouseEvent(eventType, e)) {
					retval = true;
					break;
				}
			}
		}

		e.y = saveY;
		e.x = saveX;
		return retval;
	}*/

	public Point toHolder(Point p) {
		Point to = new Point(p.x, p.y);
		to.x += bounds.x;
		to.y += bounds.y;

		if (getParent() == null) {
			return to;
		} else {
			return getParent().toHolder(to);
		}
	}
	public Point toGlyph(Point p) {
		Point to = new Point(p.x, p.y);
		if (getParent() != null) {
			to = getParent().toGlyph(to);
		}
		to.x -= bounds.x;
		to.y -= bounds.y;
		return to;
	}

	public void dispose() {
		super.dispose();
		for (Enumeration it = children.elements(); it.hasMoreElements(); ) {
			Glyph child = (Glyph)it.nextElement();
			child.dispose();
		}
		children.clear();
		if (gc != null) {
			gc.dispose();
			gc = null;
		}
	}
	public GlyphGC getGC() {
		if (gc == null) {
			gc = new GlyphGC(this);
		}
		return gc;
	}
	protected boolean pointInsideImpl(int x, int y) {
		Enumeration it = children.elements();
		while(it.hasMoreElements()) { 
			Glyph child = (Glyph)it.nextElement();
			if (child.pointInside(x, y)) return true;
		}
		return false;
	}

	public void moveAbove(Glyph glyph, Glyph aboveWhat) {
		if (aboveWhat != null &&
		    glyph.getParent() != aboveWhat.getParent())
			throw new IllegalArgumentException("glyph is not a sibling");
		children.remove(glyph);
		if (aboveWhat != null) 
			children.add(children.indexOf(aboveWhat)+1, glyph);
		else
			children.add(glyph);
		glyph.redraw(null);
	}
	public void moveBelow(Glyph glyph, Glyph belowWhat) {
		if (belowWhat != null &&
		    glyph.getParent() != belowWhat.getParent())
			throw new IllegalArgumentException("glyph is not a sibling");
		children.remove(glyph);
		if (belowWhat != null) 
			children.add(children.indexOf(belowWhat), glyph);
		else
			children.insertElementAt(glyph,0);
		glyph.redraw(null);
	}
};

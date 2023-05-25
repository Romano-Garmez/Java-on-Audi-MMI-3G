package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Implements a cross-hair with an optional circle showing confidence of estimate.
 * 
 */
public class GlyphReticle extends Glyph {
	//private Point location;
	private int radius;
	private int originalRadius;
	private int   width;
	private Color color;
	private boolean drawConfidence;

	private final int HLEN=6;
	private final int HGAP=4;

	public GlyphReticle(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphReticle(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	private void init() {
		originalRadius = radius = 0;

		width  = 1;
		color  = null;
		drawConfidence = true;
		/* getHolder().getDisplay().getSystemColor
		   (SWT.COLOR_BLACK); */
	}
	public void set(int x, int y, int radius) {
		Rectangle invalidate = getBounds();
		setLocation(new Point(x, y));
		setRadius(radius);
		redraw(invalidate);
	}
	
	public void setRadius(int r) {
		originalRadius = r;
		radius = (int)(r * this.zoom);
	}
	
	public void setDrawConfidence(boolean b) {
		drawConfidence = b;
	}
	public boolean getDrawConfidence() {
		return drawConfidence;
	}
	public void set(Point location, int radius) {
		set(location.x, location.y, radius);
	}
	public void setForeground(Color c) {
		color = c;
		redraw(null);
	}
	public void setWidth(int w) {
		Rectangle invalidate = getBounds();
		width = w;
		redraw(invalidate);
	}

	public Point getCenter() {
		return getLocation();
	}
	public int getRadius() {
		return radius;
	}
	public void dispose() {
	}
			
	protected void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);

		// draw cross hairs
		int   w = gc.getLineWidth();
		Color c=null;

		gc.setLineWidth(width);
		if (color != null) {
			c = gc.getForeground();
			gc.setForeground(color);
		}
		gc.drawLine(getLocation().x + HGAP, getLocation().y, getLocation().x + HGAP + HLEN, 
			    getLocation().y);
		gc.drawLine(getLocation().x + HGAP, getLocation().y, getLocation().x + HGAP + HLEN, 
			    getLocation().y);
		gc.drawLine(getLocation().x - HGAP, getLocation().y, getLocation().x-HGAP-HLEN, 
			    getLocation().y);
		gc.drawLine(getLocation().x, getLocation().y + HGAP, getLocation().x, 
			    getLocation().y + HGAP + HLEN);
		gc.drawLine(getLocation().x, getLocation().y - HGAP, getLocation().x,
			    getLocation().y - HGAP - HLEN);

		if (drawConfidence) {
			gc.drawOval(getLocation().x - radius, getLocation().y - radius,
				    radius*2, radius*2);
		}
		gc.setLineWidth(w);
		if (color != null) gc.setForeground(c);
	}

	protected Rectangle getBoundsImpl() {
		Rectangle r = new Rectangle(0,0,0,0);
		r.x = getLocation().x - (radius + (width+1)/2);
		r.y = getLocation().y - (radius + (width+1)/2);

		Point p = new Point(getLocation().x + (radius + (width+1)/2),
				    getLocation().y + (radius + (width+1)/2));

		r.width = p.x - r.x + 1;
		r.height= p.y - r.y + 1;

		Rectangle hair = new Rectangle(0,0,0,0);
		hair.x = getLocation().x - HGAP - HLEN - (width+1)/2;
		hair.y = getLocation().y - HGAP - HLEN - (width+1)/2;

		p.x = getLocation().x + HGAP + HLEN + (width+1)/2;
		p.y = getLocation().y + HGAP + HLEN + (width+1)/2;

		hair.width = p.x - hair.x + 1;
		hair.height= p.y - hair.y + 1;

		if (drawConfidence) {
			return r.union(hair);
		} else {
			return hair;
		}
	}

	private double hypot(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}
	protected boolean pointInsideImpl(int x, int y) {
		if (drawConfidence && pointInsideOval(x, y)) return true;
		return pointInsideCrossHairs(x, y);
	}

	private boolean pointInsideOval(int x, int y) {
		double xDelta, yDelta, distToCenter, scaledDistance, 
			distToOutline;

		/* allow for an additional pixel on each side */
		int w = width+getCloseEnough()*2;

		xDelta = x - getLocation().x;
		yDelta = y - getLocation().y;

		/*
		 * Compute the distance between the location of the oval and the
		 * point in question, using a coordinate system where the oval
		 * has been transformed to a circle with unit radius.
		 */
		distToCenter = hypot(xDelta, yDelta);
		scaledDistance = hypot(xDelta / (radius + w/2.0),
				       yDelta / (radius + w/2.0));

		/*
		 * If the scaled distance is greater than 1 then it means no
		 * hit.
		 */
		if (scaledDistance > 1.0) {
			return false;
		}

		/*
		 * Scaled distance less than 1 means the point is inside the
		 * outer edge of the oval.  Do the same computation as above
		 * (scale back to original coordinate system), but also check
		 * to see if the point is within the width of the outline.
		 */
		if (scaledDistance > 1E-10) {
			distToOutline = (distToCenter/scaledDistance) * 
				(1.0 - scaledDistance) - w;
		} else {
			/*
			 * Avoid dividing by a very small number 
			 * (it could cause an arithmetic overflow).  
			 * This problem occurs if the point is
			 * very close to the location of the oval.
			 */
 
			distToOutline = (2*radius+1 - w)/2;
		}

		if (distToOutline < 0.0) {
			return true;
		}
		return false;
	}
	
	public void setZoom(double z) {
		super.setZoom(z);
		radius = (int)(originalRadius * z);
	}

	private boolean pointInsideCrossHairs(int x, int y) {
		Rectangle r = new Rectangle(0,0,0,0);

		/* allow for additional pixels on each side */
		int w = width+getCloseEnough()*2;

		r.x = getLocation().x - HGAP - HLEN - w/2;
		r.y = getLocation().y - w/2;
		r.width = HLEN + w;
		r.height= w;
		if (r.contains(x, y)) return true;

		r.x = getLocation().x + HGAP;
		r.y = getLocation().y - w/2;
		r.width = HLEN + w;
		r.height= w;
		if (r.contains(x, y)) return true;

		r.x = getLocation().x - w/2;
		r.y = getLocation().y - HGAP - HLEN - w/2;
		r.width = w;
		r.height= HLEN + w;
		if (r.contains(x, y)) return true;

		r.x = getLocation().x - w/2;
		r.y = getLocation().y + HGAP;
		r.width = w;
		r.height= HLEN + w;
		if (r.contains(x, y)) return true;

		return false;
	}
}

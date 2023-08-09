/*
 * Created on Jul 20, 2004
 *
 */
package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * 
 */
public class GlyphLine extends Glyph {
	private Point startPoint, endPoint;
	private Color color;
	private int width;

	public GlyphLine(GlyphHolder holder, int style, Point start, Point end) {
		super(holder, style);
		init();
		set(start, end);
	}
	
	public GlyphLine(GlyphHolder holder, int style, Point start, Point end, Color color, int thickness) {
	    super(holder, style);
	    init();
	    width = thickness;
	    this.color = color;
	    set(start, end);
	}
	
	private void init() {
		startPoint = endPoint = null;
		color = null;
		width = 2;
	}
	
	public void set(int x1, int y1, int x2, int y2) {
		set(new Point (x1, y1), new Point(x2, y2));
	}
	
	public void set(Point start, Point end) {
		Rectangle invalidate = getBounds();
		startPoint = start;
		endPoint = end;
		redraw(invalidate);
	}
	
	public void setColor(Color c) {
		color = c;
		this.redraw(null);
	}
	
	public void setThickness(int t) {
		width = t;
		this.redraw(null);
	}

	protected Rectangle getBoundsImpl() {
		if (startPoint == null || endPoint == null) {
			return null;
		}
		
		return new Rectangle((int)(Math.min(startPoint.x, endPoint.x) * getZoom()), 
				(int)(Math.min(startPoint.y, endPoint.y) * getZoom()),
				(int)(Math.abs(startPoint.x - endPoint.x) * getZoom()), 
				(int)(Math.abs(startPoint.y - endPoint.y) * getZoom()));
	}

	protected boolean pointInsideImpl(int x, int y) {
		return getBounds().contains(x, y);
	}
	
	public void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);
		
		int savedWidth = gc.getLineWidth();
		Color savedColor = gc.getForeground();

		gc.setLineWidth(width);
		if (color != null) gc.setForeground(color);
		
		gc.drawLine((int)(startPoint.x * getZoom()), (int)(startPoint.y * getZoom()), 
				(int)(endPoint.x * getZoom()), (int)(endPoint.y * getZoom()));
		
		gc.setLineWidth(savedWidth);
		if (color != null) gc.setForeground(savedColor);
	}
}

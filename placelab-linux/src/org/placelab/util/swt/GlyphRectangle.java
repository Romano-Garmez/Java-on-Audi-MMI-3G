package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GlyphRectangle extends Glyph {
	//private Point location;
	private int width, height, thickness;
	private Color fg, bg;

	public GlyphRectangle(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphRectangle(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	private void init() {
		width = height = 0;
		fg = bg = null;
		thickness = 1;
	}
	public void set(int x, int y, int width, int height) {
		Rectangle invalidate = getBounds();
		setLocation(new Point(x, y));
		this.width = width;
		this.height= height;
		redraw(invalidate);
	}
	public void set(Rectangle r) {
		set(r.x, r.y, r.width, r.height);
	}
	public void setForeground(Color c) {
		fg = c;
		redraw(null);
	}
	public void setBackground(Color c) {
		bg = c;
		redraw(null);
	}
	public void setThickness(int t) {
		Rectangle invalidate = getBounds();
		thickness = t;
		redraw(invalidate);
	}

	public Rectangle get() {
		return new Rectangle(getOriginalLocation().x, 
				     getOriginalLocation().y, 
				     width, height);
	}
			
	protected void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);

		int   w = gc.getLineWidth();
		Color save_fg=null, save_bg;

		gc.setLineWidth(thickness);
		if (fg != null) {
			save_fg = gc.getForeground();
			gc.setForeground(fg);
		}
		if (bg != null) {
			save_bg = gc.getBackground();
			gc.setBackground(bg);
		}
		if (bg!=null) {
			gc.fillRectangle(getLocation().x, getLocation().y,
					 (int)(width * zoom + 0.5),
					 (int)(height * zoom + 0.5));
		}
		gc.drawRectangle(getLocation().x, getLocation().y,
				 (int)(width * zoom + 0.5),
				 (int)(height * zoom + 0.5));
		
		gc.setLineWidth(w);
		if (fg != null) gc.setForeground(fg);
		if (bg != null) gc.setBackground(bg);
	}

	protected Rectangle getBoundsImpl() {
		int x1 = getLocation().x - thickness/2,
			y1 = getLocation().y - thickness/2,
			x2 = getLocation().x + ((int)(width *zoom + 0.5)) + 
			thickness/2,
			y2 = getLocation().y + ((int)(height*zoom + 0.5)) + 
			thickness/2;
		return new Rectangle(x1, y1, x2-x1+1, y2-y1+1);
	}

	protected boolean pointInsideImpl(int x, int y) {
		if (bg != null)
			return getBounds().contains(x, y);

		int x1 = getLocation().x - thickness/2 + thickness,
			y1 = getLocation().y - thickness/2 + thickness,
			x2 = getLocation().x + ((int)(width *zoom + 0.5)) + 
			thickness/2 - thickness,
			y2 = getLocation().y + ((int)(height*zoom + 0.5)) + 
			thickness/2 - thickness;
		Rectangle r=new Rectangle(x1, y1, x2-x1+1, y2-y1+1);
		return (getBounds().contains(x, y) && !r.contains(x, y));
	}
}

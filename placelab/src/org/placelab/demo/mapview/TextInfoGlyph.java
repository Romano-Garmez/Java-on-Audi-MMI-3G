package org.placelab.demo.mapview;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.util.swt.GlyphComposite;
import org.placelab.util.swt.GlyphGC;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphLabel;


public class TextInfoGlyph extends GlyphLabel {
	private Color background;

	private final int XGAP = 5;
	private final int YGAP = 5;
	private final int SIZEOF_CROSS = 5;

	public TextInfoGlyph(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public TextInfoGlyph(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	private void init() {
	}

	public void setWidth(int maxWidth) {
		super.setWidth(maxWidth - 2 * XGAP);
	}
	public void setLocation(int x, int y) {
		super.setLocation(x + XGAP, y + YGAP + SIZEOF_CROSS);
	}
	public void setBackground(Color bg) {
		background = bg;
		redraw(null);
	}
	public Color getBackground() { return background; }

	public void paint(PaintEvent e, GlyphGC gc) {
		Rectangle paintArea = new Rectangle(e.x,e.y,e.width,e.height);
		Rectangle bounds = getBounds();
		if (! bounds.intersects(paintArea)) return;

		Color bg=null, fg=null;
		if (background != null) {
			bg = gc.getBackground();
			gc.setBackground(background);
		}
		if (getForeground() != null) {
			fg = gc.getForeground();
			gc.setForeground(getForeground());
		}

		/*paintArea = bounds.intersection(paintArea);
		  gc.fillRectangle(paintArea.x, paintArea.y, 
		  paintArea.width-1, paintArea.height-1);*/
		gc.fillRectangle(bounds.x, bounds.y, 
				 bounds.width-1, bounds.height-1);
		gc.drawRectangle(bounds.x, bounds.y, 
				 bounds.width-1, bounds.height-1);
		gc.drawLine(bounds.x+bounds.width-1-XGAP, 
			    bounds.y+YGAP,
			    bounds.x+bounds.width-1-XGAP-SIZEOF_CROSS, 
			    bounds.y+YGAP+SIZEOF_CROSS);

		gc.drawLine(bounds.x+bounds.width-1-XGAP-SIZEOF_CROSS, 
			    bounds.y+YGAP,
			    bounds.x+bounds.width-1-XGAP, 
			    bounds.y+YGAP+SIZEOF_CROSS);

		if (background != null) gc.setBackground(bg);
		if (getForeground() != null) gc.setForeground(fg);

		super.paint(e, gc);
	}

	public Rectangle getBounds() {
		Rectangle bounds = super.getBounds();
		bounds.x -= XGAP;
		bounds.y -= YGAP + SIZEOF_CROSS;
		bounds.width  += 2 * XGAP;
		bounds.height += 2 * YGAP + SIZEOF_CROSS;
		return bounds;
	}
}


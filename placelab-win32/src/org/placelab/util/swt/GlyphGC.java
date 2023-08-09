package org.placelab.util.swt;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


public class GlyphGC {
	private GlyphComposite glyph;
	private GC directGC;
	
	public GlyphGC(GlyphComposite glyph) {
		this.glyph = glyph;
		resetClipping();
	}
	public void dispose() {
		glyph = null;
		directGC = null;
	}
	// for the affine transform stuff I have it draw into a temporary
	// buffer so I want the gc to just draw direct to there.
	public GlyphGC(GC drawDirect) {
	    glyph = null;
	    directGC = drawDirect;
	    resetClipping();
	}
	
	private GC getGC() { 
	    if(glyph != null)
	        return glyph.getHolder().getOffscreenGC(); 
	    else 
	        return directGC;
	}

	public int getLineWidth() { return getGC().getLineWidth(); }
	public Color getForeground() { return getGC().getForeground(); }
	public Color getBackground() { return getGC().getBackground(); }
	public Font getFont() { return getGC().getFont(); }
	public void setLineWidth(int w) { getGC().setLineWidth(w); }
	public void setForeground(Color fg) { getGC().setForeground(fg); }
	public void setBackground(Color bg) { getGC().setBackground(bg); }
	public void setFont(Font f) { getGC().setFont(f); }

	public void drawLine(int x1, int y1, int x2, int y2) {
		Point p1 = translate(x1, y1);
		Point p2 = translate(x2, y2);
		getGC().drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	public void drawOval(int x, int y, int w, int h) {
		Point p = translate(x, y);
		getGC().drawOval(p.x, p.y, w, h);
	}
	public void drawRectangle(int x, int y, int w, int h) {
		Point p = translate(x, y);
		getGC().drawRectangle(p.x, p.y, w, h);
	}
	public void fillRectangle(int x, int y, int w, int h) {
		Point p = translate(x, y);
		getGC().fillRectangle(p.x, p.y, w, h);
	}
	public void drawImage(Image image, int x, int y) {
		Point p = translate(x, y);
		getGC().drawImage(image, p.x, p.y);
	}
	public void drawImage(Image image, int srcX, int srcY, 
			      int srcWidth, int srcHeight, 
			      int destX, int destY, 
			      int destWidth, int destHeight) {
		Point srcP = translate(srcX, srcY);
		Point destP = translate(destX, destY);
		getGC().drawImage(image, srcP.x, srcP.y, srcWidth, srcHeight,
				  destP.x, destP.y, destWidth, destHeight);
	}
	public void drawText(String text, int x, int y) {
		Point p = translate(x, y);
	    getGC().drawText(text, p.x, p.y, true);
	}
	public void drawTextWithBackground(String text, int x, int y) {
	    Point p = translate(x, y);
	    getGC().drawText(text, p.x, p.y, false);
	}
	public Point textExtent(String text) {
		return getGC().textExtent(text);
	}

	public void resetClipping() {
		if(glyph != null) {
		    Rectangle bounds = glyph.getBounds();
			Point p = glyph.toHolder(new Point(0, 0));
			getGC().setClipping(p.x, p.y, bounds.width, bounds.height);
		}
	}

    public void drawPolygon(int[] is) {
        if(is.length % 2 != 0 || is.length == 0) return;
        getGC().drawPolygon(translate(is));
    }		
    
    public void fillPolygon(int[] is) {
        if(is.length % 2 != 0 || is.length == 0) return;
        getGC().fillPolygon(translate(is));
    }
    
    private int[] translate(int[] is) {
        int[] translated = new int[is.length];
        for(int i = 0; i < is.length; i++) {
            int x = is[i];
            i++;
            int y = is[i];
    		Point p = translate(x, y);
            translated[i - 1] = p.x;
            translated[i] = p.y;
        }
        return translated;
    }
    
    private Point translate(int x, int y) {
		Point p;
	    if(glyph != null) p = glyph.toHolder(new Point(x, y));
		else p = new Point(x, y);
	    return p;
    }
};

package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;


public class GlyphPoly extends Glyph {

    protected Rectangle bounding;
    protected boolean extremaValid;
    protected int[] pointArray;
    
    protected int thickness;
    
    protected Color fg, bg;
    
    public GlyphPoly(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphPoly(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	private void init() {
	    pointArray = new int[0];
		fg = bg = null;
		thickness = 1;
		bounding = new Rectangle(0, 0, 0, 0);
		extremaValid = true;
	}
	
	private int translate(int point) {
	    return (int) ((double)point * zoom);
	}
	
	private int[] translate(int[] points) {
	    if(zoom == 1.0) return points;
	    else {
	        int[] ret = new int[points.length];
	        for(int i = 0; i < points.length; i++) {
	            ret[i] = translate(points[i]);
	        }
	        return ret;
	    }
	}
    
    protected Rectangle getBoundsImpl() {
        if(!extremaValid) {
            int minX = 0;
            int minY = 0;
            int maxX = 0;
            int maxY = 0;
            for(int i = 0; i < pointArray.length; i++) {
                if(i == 0) {
                    minX = translate(pointArray[i]);
                    maxX = translate(pointArray[i]);
                } else if(i == 1) {
                    minY = translate(pointArray[i]);
                    maxY = translate(pointArray[i]);
                } else {
                    if((i % 2) == 0) {
                        // x coordinates
                        minX = Math.min(minX, pointArray[i]);
                        maxX = Math.max(maxX, pointArray[i]);
                    } else {
                        // y coordinates
                        minY = Math.min(minY, pointArray[i]);
                        maxY = Math.max(maxY, pointArray[i]);
                    }
                }
            }
            bounding = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }
        return bounding;
    }

    public void set(int[] pointArray) {
        this.pointArray = pointArray;
        extremaValid = false;
        redraw(null);
    }
    
    public void setForeground(Color c) {
        fg = c;
        redraw(null);
    }
    
    public void setBackground(Color c) {
        bg = c;
        redraw(null);
    }
    
    protected boolean pointInsideImpl(int x, int y) {
        // TODO: do this right
        return getBounds().contains(x, y);
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
		int[] translated = null;
		if (bg!=null) {
		    translated = translate(pointArray);
			gc.fillPolygon(translated);
		}
		if(fg != null) {
		    gc.drawPolygon(translated == null ? translate(pointArray) : translated);
		}
		
		gc.setLineWidth(w);
		if (fg != null) gc.setForeground(fg);
		if (bg != null) gc.setBackground(bg);
    }

}

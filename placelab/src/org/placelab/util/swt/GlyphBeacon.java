package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class GlyphBeacon extends Glyph {
	private int radius=4;
	private int signalStrength = -1; // not known
	private boolean seen = false;
	private String name = null;
	private boolean alwaysColor = false;
	private static Font font = null;
	
	// turn this off hides AP drawing
	private static boolean showBeacons = true;
	// show different signalStrength levels or one
	private static boolean showSignalStrength = true;
	
	private static Color yellow = new Color(null,255,255,0);
	private static Color white   = new Color(null,255,255,255);

	public GlyphBeacon(GlyphComposite parent, int style, int x, int y) {
		super(parent, style);
		init(x,y);
	}
	public GlyphBeacon(GlyphHolder holder, int style, int x, int y) {
		super(holder, style);
		init(x,y);
	}
	private void init(int x, int y) {
		setLocation(new Point(x,y));
	}
	public void setName(String humanReadableName) {
		this.name = humanReadableName;
		this.redraw(null);
	}
	
	public static void setShowBeacons(boolean b) {
		showBeacons = b;
	}
	
	public static boolean getShowBeacons() {
		return showBeacons;
	}
	
	public void setSeen(boolean s, int signalStrength) {
		seen = s;
		this.signalStrength = signalStrength;
		redraw(getBounds());
	}

	public static boolean getShowSignalStrength() {
		return showSignalStrength;
	}
	public static void setShowSignalStrength(boolean b) {
		showSignalStrength = b;
	}
	public Point getCenter() {
		return this.getLocation();
	}
	
	public void setDefaultColor(Color aColor) {
		defaultBeaconColor = aColor;
		alwaysColor = true;
		redraw(null);
	}

	private Color defaultBeaconColor = yellow;
	
	protected void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);
		
		if (!showBeacons) {
			return;
		}

		// always draw black circle
		gc.setLineWidth(1);
		gc.setForeground(new Color(null,0,0,0));
		gc.drawOval(getLocation().x - radius, getLocation().y - radius,
				radius*2, radius*2);

		if (!seen && !alwaysColor) {
			// JWS - 20040517 - do not fill in circle if not seen
			// (map gets too cluttered)
	//		return;
			 gc.setForeground(white);
		} else if (!showSignalStrength || signalStrength == -1) {
			// draw default color	
		   	gc.setForeground(defaultBeaconColor);
		} else {			
		   	gc.setForeground(ssToColor(signalStrength));
		}
		
   		gc.setLineWidth(radius);
   		int i = 2;
		gc.drawOval(getLocation().x - i, getLocation().y - i,
				    i*2, i*2);
		
		if(name != null) {
			Point extent = textExtent(name);
			gc.drawText(this.name, getLocation().x - (extent.x / 2),
					getLocation().y + radius + 1);
		}
	}
	
	public Point textExtent(String string) {
		Font f=null;
		GlyphGC gc = getParent().getGC();
		if (font != null) {
			f = gc.getFont();
			gc.setFont(font);
		}
		Point extent = gc.textExtent(string);
		if (font != null) gc.setFont(f);
		return extent;
	}

	private Color ssToColor(int ss) {
		// transform signal strength 0.0-1.0 to red-yellow-green
		if(ss == -1) return defaultBeaconColor;
		
		return HSVtoRGB(0.0033*(double)ss,1.0,1.0);
	}
	
	private Color HSVtoRGB(double h, double s, double v) {
		double r,g,b;
		double hue = h*6.0;
		int ihue = (int) Math.floor(hue);
		double v1 = v*(1-s);
		double v2 = v*(1-s*(hue-(double)ihue));
		double v3 = v*(1-s*(1-(hue-(double)ihue)));

		if(h < 0.0 || h > 1.0 || s < 0.0 || s > 1.0 || v < 0.0 || v > 1.0) {
			System.out.println("HSVtoRGB: invalid h,s,v values " + h + "," + s + "," + v);
			return null;
		}
		if(s==0) return new Color(null,(int)(v*255),(int)(v*255),(int)(v*255));
		switch((int) Math.floor(h*6.0)) {
			case 0:
				r=v;
				g=v3;
				b=v1;
				break;
			case 1:
				r=v2;
				g=v;
				b=v1;
				break;
			case 2:
				r=v1;
				b=v;
				g=v3;
				break;		 
			case 3:
				r=v1;
				g=v2;
				b=v;
				break;
			case 4:
				r=v3;
				g=v1;
				b=v;
				break;
			case 5:
				r=v;
				g=v1;
				b=v2;
				break;
			default:
				r=0;
				g=0;
				b=0;
				break;
		}
		return new Color(null,(int)(r*255),(int)(g*255),(int)(b*255));
	}

	protected Rectangle getBoundsImpl() {
		Rectangle r = new Rectangle(0,0,0,0);
		if (!showBeacons) {
			return r;
		}
		r.x = getLocation().x - (radius + 1);
		r.y = getLocation().y - (radius + 1);
		Point p = new Point(getLocation().x + (radius + 1),
				    getLocation().y + (radius + 1));
		r.width = p.x - r.x + 1;
		r.height= p.y - r.y + 1;
		if(name != null) {
			Point extent = textExtent(name);
			if(r.x > (getLocation().x - (extent.x / 2))) {
				r.x = getLocation().x - (extent.x /2);
			}
			if(extent.x > r.width) r.width = extent.x;
			r.width += 1 + extent.y;
		}
		return r;
	}

	private double hypot(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}
	protected boolean pointInsideImpl(int x, int y) {
		if (!showBeacons) {
			return false;
		}
		return (pointInsideOval(x, y));
	}

	private boolean pointInsideOval(int x, int y) {
		double xDelta, yDelta, distToCenter, scaledDistance, 
			distToOutline;

		/* allow for an additional pixel on each side */
		int w = 1+getCloseEnough()*2;

		xDelta = x - getLocation().x;
		yDelta = y - getLocation().y;

		/*
		 * Compute the distance between the center of the oval and the
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
			 * very close to the center of the oval.
			 */
 
			if (radius < radius) {
				distToOutline = (2*radius+1 - w)/2;
			} else {
				distToOutline = (2*radius+1 - w)/2;
			}
		}

		if (distToOutline < 0.0) {
			return true;
		}
		return false;
	}

}

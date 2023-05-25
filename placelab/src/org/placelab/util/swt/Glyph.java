package org.placelab.util.swt;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.placelab.util.Logger;


public abstract class Glyph {
	public static final int ANCHOR_NW=0;
	public static final int ANCHOR_N =1;
	public static final int ANCHOR_NE=2;
	public static final int ANCHOR_E =3;
	public static final int ANCHOR_SE=4;
	public static final int ANCHOR_S =5;
	public static final int ANCHOR_SW=6;
	public static final int ANCHOR_W =7;
	public static final int ANCHOR_C =8;


	public static final int NONE=0;
	public static final int DISPOSED= 1<<0;
	public static final int VISIBLE = 1<<1;
	public static final int MOUSE_EVENTS = 1<<2;
	public static final int IGNORE_TRANSPARENCY = 1<<3;



	private GlyphComposite parent;
	private GlyphHolder holder;
	private int state;
	private GlyphEventTable eventTable;
	private Cursor cursor;
	private int closeEnough;
	
	protected AffineTransform transform;
	protected boolean needsTransformRedraw = false;
	protected Image transformedImage = null;
	
	protected double zoom;
	
	private Point originalLocation;
	private Point location;

	void init(GlyphComposite parent, int style) {
		if (parent==null) SWT.error(SWT.ERROR_NULL_ARGUMENT);

		this.state  = Glyph.VISIBLE | Glyph.MOUSE_EVENTS;
		this.parent = parent;
		this.holder = parent.getHolder();
		this.cursor = null;
		this.zoom = parent.zoom;
		this.parent.notifyAddChild(this);
		this.holder.notifyAddDescendant(this);
	}

	private Glyph() {
		this.closeEnough = 0;
		this.location = new Point(0,0);
		this.originalLocation = new Point(0,0);
		this.zoom = 1;
	}
	
	public Glyph(GlyphComposite parent, int style) {
		this();
		init(parent, style);
	}

	public Glyph(GlyphHolder holder, int style) {
		this();
		if (holder==null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		init(holder.getChild(), style);
	}


	/* this method is only meant for the toplevel GlyphComposite that
	 * is the child of the GlyphHolder */
	Glyph(GlyphHolder holder, int style, boolean isToplevel) {
		this();
		if (holder==null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (! isToplevel) {
			init(holder.getChild(), style);
			return;
		}

		if (! (this instanceof GlyphComposite) )
			throw new IllegalArgumentException("only GlyphComposites can have a GlyphHolder as their parent");
		
		this.state  = Glyph.VISIBLE | Glyph.MOUSE_EVENTS;
		this.parent = null;
		this.holder = holder;
		this.holder.notifyAddDescendant(this);
	}

	public void    setFlag(int flag) { state |=  flag; }
	public void  resetFlag(int flag) { state &= ~flag; }
	public boolean getFlag(int flag) { return ((state & flag) != 0); }

	public boolean isDisposed() { return getFlag(Glyph.DISPOSED); }

	public GlyphHolder getHolder() {
		return holder;
	}
	public GlyphComposite getParent() {
		return parent;
	}
	public void setCloseEnough(int c) { closeEnough = c; }
	public int  getCloseEnough() { return closeEnough; }

	public void setTransform(AffineTransform aTransform) {
	    this.transform = aTransform;
	    this.setNeedsTransformRedraw(true);
	    this.redraw(null);
	}
	
	public AffineTransform getTransform() {
	    return this.transform;
	}
	
	public void setZoom(double z) { 
		zoom = z;
		this.setLocation(originalLocation);
	}
	public double getZoom() { return zoom; }
	public void setLocation(Point loc) {
		Rectangle invalidate = this.getBounds();
		originalLocation = loc;
		location = new Point((int)(this.originalLocation.x * zoom),
					(int)(this.originalLocation.y * zoom));
		this.setNeedsTransformRedraw(true);
		this.redraw(invalidate);
	}
	/** Gets the location that the Glyph is set to for a zoom
	 *  of 1.0
	 * @return the unzoomed location
	 */
	public Point getOriginalLocation() {
		return this.originalLocation;
	}
	/**
	 * Gets the location that the Glyph is currently at, translated
	 * to account for zoom
	 * @return the zoom translated location
	 */
	public Point getLocation() {
		return this.location;
	}
	
	public void dispose() {
		if (parent != null) parent.notifyRemoveChild(this);
		setFlag(Glyph.DISPOSED);
		notifyListeners(SWT.Dispose, null);
		eventTable = null;
		holder.notifyRemoveDescendant(this);
 	}

	public void addListener(int eventType, Listener handler) {
		if (handler == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) eventTable = new GlyphEventTable ();
		eventTable.hook(eventType, handler);
	}
	public void removeListener(int eventType, Listener handler) {
		if (handler == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null) return;
		eventTable.unhook(eventType, handler);
	}
	boolean hooks(int eventType) {
		if (eventTable == null) return false;
		return eventTable.hooks(eventType);
	}
	public void notifyListeners(int eventType, Event event) {
		if (eventTable == null) return;

		if (event == null) event = new Event();
		event.type = eventType;
		event.display = holder.getDisplay();
		event.widget = holder;
		if (event.time == 0) {
			//event.time = event.display.getLastEventTime();
		}
		eventTable.sendEvent(event);
	}

	public void addPaintListener(PaintListener listener) {
		if (listener == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Paint,typedListener);
	}
	public void addMouseListener(MouseListener listener) {
		if (listener == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener (listener);
		addListener(SWT.MouseDown,typedListener);
		addListener(SWT.MouseUp,typedListener);
		addListener(SWT.MouseDoubleClick,typedListener);
	}


	/**
	 * If you want to do your own drawing for the AffineTransform (for instance,
	 * your glyph is a vector graphic thats easily drawn transformed, override this
	 * and handle for the transform yourself).
	 */
	public void paint(PaintEvent e, GlyphGC gc) {
		notifyListeners(SWT.Paint, GlyphEventTable.getEvent(e));
		if(transform != null) {
		    
		    if(this.getNeedsTransformRedraw() || transformedImage == null) {
			    
		        if(transformedImage != null) transformedImage.dispose();
		        
			    if(getBoundsImpl().width <= 0 || getBoundsImpl().height <= 0) {
			        // can't draw something thinner than a pixel :P
			        return;
			    }
			    
			    // want the glyph to paint at 0,0, not at wherever it thinks it should
			    // paint, also zoom doesn't stack with AffineTransform, you can use one
			    // or the other
			    Point oldLocation = this.originalLocation;
			    double oldZoom = this.zoom;
			    
			    this.setLocation(new Point(0, 0));
			    this.setZoom(1.0);
			    
			    Image buffer = new Image(holder.getDisplay(), getBoundsImpl());
			    GC innerGC = new GC(buffer);
			    GlyphGC tempGC = new GlyphGC(innerGC);
			    
			    // make the glyph paint into it
			    paintImpl(e, tempGC);
			    
			    // now mask the image
			    ImageData temp = buffer.getImageData();
			    
			    if(this.getMaskColor() != null) {
				    int maskRed = this.getMaskColor().getRed();
				    int maskBlue = this.getMaskColor().getBlue();
				    int maskGreen = this.getMaskColor().getGreen();

					int[] lineData = new int[temp.width];
					for (int y = 0; y < temp.height; y++) {
						temp.getPixels(0,y,temp.width,lineData,0);
						// Analyze each pixel value in the line
						for (int x=0; x<lineData.length; x++){
							// Extract the red, green and blue component
							int pixelValue = lineData[x];
							int r = temp.palette.getRGB(pixelValue).red;
							int g = temp.palette.getRGB(pixelValue).green;
							int b = temp.palette.getRGB(pixelValue).blue;
							// if the pixel rgb value is a linear combination of the 
							// mask rgb value then choose an appropriate alpha.  
							// if it isn't, then make the pixel opaque
							double rScale = 1.0 - (double)r / (double)maskRed;
							double gScale = 1.0 - (double)g / (double)maskGreen;
							double bScale = 1.0 - (double)b / (double)maskBlue;
							/*System.out.println("x:" + x + " " +
											   "y:" + y + " " +
											   "rgb:" + r + "," + g + "," + b + " " +
											   "rgbScale:" + rScale + "," + gScale + "," + bScale);*/
							int opacity = 255;
							if(Math.abs(rScale - gScale) < 0.01
								&& Math.abs(rScale - bScale) < 0.01) {
								if(rScale < 1.0) {
									// map the scale from 0 to 1 into 0 to 255
									opacity = (int)(255.0 * rScale);
								}
							}
							// note that it is important to set an alpha value
							// for every pixel, not just those that are less than 255
							temp.setAlpha(x, y, opacity);
						}
					}
			    }
			    
			    
			    this.setLocation(oldLocation);
			    this.setZoom(oldZoom);
			    
			    // now transform it using the transform
			    ImageData transformedImageData;
			    try {
			        transformedImageData = transform.transformImage(temp, 
			                AffineTransform.NEAREST_NEIGHBOR);
			    } catch (AffineTransform.NotInvertibleException nie) {
			        // if its not invertible, i can't make a sensible onscreen representation of it
			        // so forget about it
			        Logger.println("couldn't draw glyph " + this + " because it has a noninvertible transform attached",
			                Logger.HIGH);
			        tempGC.dispose();
			        innerGC.dispose();
			        buffer.dispose();
			        return;
			    }
			           
			    transformedImage = new Image(holder.getDisplay(), transformedImageData);
			    gc.drawImage(transformedImage, getBounds().x, getBounds().y);
			    
			    tempGC.dispose();
			    buffer.dispose();
			    innerGC.dispose();
			    this.setNeedsTransformRedraw(false);
		    } else {
		        gc.drawImage(transformedImage, getBounds().x, getBounds().y);
		    }
		    
		} else {
		    paintImpl(e, gc);
		}
	}
	
	/**
	 * Implement this if you don't want to worry about AffineTransforms.  Your
	 * graphics will be rasterized and have an AffineTransform applied to them
	 */
	protected void paintImpl(PaintEvent e, GlyphGC gc) { }
	
	/**
	 * Because swt cannot draw into an image and leave the pixels in the image
	 * not drawn on in a transparent state and make the other pixels opaque, Glyphs
	 * must set a mask color which stands for transparent.  You may return
	 * null if you don't need any areas masked out.
	 */
	protected Color getMaskColor() {
	    return new Color(holder.getDisplay(), 255, 255, 255);
	}
	
	/**
	 * Return true here if your glyph needs to have its transform image recalculated
	 * That is, if its changed anywhere that will matter, return true.
	 * If it hasn't, return false and you get the cached image so we don't waste a bunch
	 * of time transforming images, which is not fast I might add.
	 */
	protected boolean getNeedsTransformRedraw() {
	    return needsTransformRedraw;
	}
	
	protected void setNeedsTransformRedraw(boolean flag) {
	    needsTransformRedraw = flag;
	}
	
	public void handleMouseEvent(int eventType, MouseEvent e) {
		notifyListeners(eventType, GlyphEventTable.getEvent(e));
	}
	public Glyph pickGlyphAt(int x, int y, boolean checkMouseEnabled) {
		if (! isVisible()) return null;
		if (checkMouseEnabled && !areMouseEventsEnabled()) 
			return null;

		return (pointInside(x, y) ? this : null);
	}

	public void redraw(Rectangle invalidate) {
	    this.setNeedsTransformRedraw(true);
	    if (!isVisible()) return;

		Rectangle r = getBounds();
		if (invalidate != null && r.intersects(invalidate)) {
			r = r.union(invalidate);
		} else if (invalidate != null) {
			if (parent != null) {
				Point p = parent.toHolder
					(new Point(invalidate.x,invalidate.y));
				invalidate.x = p.x;
				invalidate.y = p.y;
			}
			getHolder().redraw(invalidate.x, invalidate.y, 
					   invalidate.width,invalidate.height, 
					   true);
		}

		if (parent != null) {
			Point p = parent.toHolder(new Point(r.x, r.y));
			r.x = p.x;
			r.y = p.y;
		}
		getHolder().redraw(r.x, r.y, r.width, r.height, true);
	}

	public void setVisible(boolean visible) {
		if (getFlag(Glyph.VISIBLE) == visible) return;
		if (visible) setFlag(Glyph.VISIBLE);
		else resetFlag(Glyph.VISIBLE);

		Rectangle r = getBounds();
		getHolder().redraw(r.x, r.y, r.width, r.height, true);
	}
	public boolean isVisible() { return getFlag(Glyph.VISIBLE); }

	public void enableMouseEvents(boolean enable) {
		if (enable) setFlag(Glyph.MOUSE_EVENTS);
		else resetFlag(Glyph.MOUSE_EVENTS);
	}
	public boolean areMouseEventsEnabled() { 
		return getFlag(Glyph.MOUSE_EVENTS); 
	}

	public Rectangle getBounds() {
	    if(transform != null) {
	        return transform.getBoundingRect(getBoundsImpl());
	    } else {
	        return getBoundsImpl();
	    }
	}
	
	public boolean pointInside(int x, int y) {
	    if(transform != null) {
	        Point transformed;
	        try {
	            transformed = transform.inverseTransform(new Point(x, y));
	        } catch (AffineTransform.NotInvertibleException nie) {
	            // if its not invertible, its not drawable, so the mouse isn't inside
	            // it
	            return false;
	        }
	        return pointInsideImpl(transformed.x, transformed.y);
	    } else {
	        return pointInsideImpl(x, y);
	    }
	}
	
	/**
	 * @return your bounds without having the AffineTransform applied to them
	 */
	protected abstract Rectangle getBoundsImpl();
	/**
	 * Return if the point is inside your glyph without the AffineTransform applied
	 */
	protected abstract boolean pointInsideImpl(int x, int y);
	public boolean pointInside(Point p) { return pointInside(p.x, p.y); }

	public void moveAbove(Glyph glyph) {
		if (parent == null) 
			throw new IllegalArgumentException("cannot rearrange root of the glyph hierarchy");
		
		if (glyph != null && parent != glyph.getParent())
			throw new IllegalArgumentException("glyph is not a sibling");
		parent.moveAbove(this, glyph);		
	}
	public void moveBelow(Glyph glyph) {
		if (parent == null) 
			throw new IllegalArgumentException("cannot rearrange root of the glyph hierarchy");
		
		if (glyph != null && parent != glyph.getParent())
			throw new IllegalArgumentException("glyph is not a sibling");
		parent.moveBelow(this, glyph);		
	}


	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
		if (holder.activeCursorGlyph() != this) return;

		/* we need to reset the cursor */
		activateCursor();
	}

	void activateCursor() {
		if (cursor != null) {
			holder.activateCursor(cursor, this);
			return;
		}

		Glyph p = parent;
		while (p != null) {
			if (p.cursor != null) {
				holder.activateCursor(p.cursor, p);
				return;
			}
			p = p.parent;
		}
		holder.activateCursor();
	}

	void enter(MouseEvent e) {
	}
	void leave(MouseEvent e) {
	}
}

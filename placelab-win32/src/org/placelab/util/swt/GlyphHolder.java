package org.placelab.util.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.placelab.collections.HashSet;
import org.placelab.collections.Iterator;


public class GlyphHolder extends Canvas {
	private GlyphComposite child;
	private Glyph  currentGlyph;
	private Cursor myCursor, showingCursor;
	private Glyph  cursorGlyph;
	private int    frozenCount;
	private Rectangle missedRedraw;
	private Image  offscreenDrawable;
	private GC     offscreenGC;
	
	protected HashSet phantoms;

	public GlyphHolder(Composite parent, int style) {
		super(parent, style|SWT.NO_BACKGROUND);

		Rectangle bounds = this.getBounds();
		bounds.x = bounds.y = 0;
		if (bounds.width  <= 0) bounds.width  = 1;
		if (bounds.height <= 0) bounds.height = 1;
		offscreenDrawable = new Image(getDisplay(), bounds);
		offscreenGC = null;

		child = new GlyphComposite(this, SWT.NONE, true);
		currentGlyph = null;
		cursorGlyph  = null;
		myCursor     = null;
		showingCursor= null;
		missedRedraw = null;
		frozenCount  = 0;

		phantoms = new HashSet();
		
		addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					paintControlImpl(e);
				}
			});
		addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					controlResizedImpl(e);
				}
			});
		addMouseListener(new MouseListener() {
				public void mouseDown(MouseEvent e) {
					handleMouseEvent(SWT.MouseDown, e);
				}
				public void mouseUp(MouseEvent e) {
					handleMouseEvent(SWT.MouseUp,e);
				}
				public void mouseDoubleClick(MouseEvent e) {
					handleMouseEvent(SWT.MouseDoubleClick,
							 e);
				}
			});
		addMouseMoveListener(new MouseMoveListener() {
				public void mouseMove(MouseEvent e) {
					handleMouseMove(e);
				}
			});
		addMouseTrackListener(new MouseTrackAdapter() {
				public void mouseEnter(MouseEvent e) {
					handleMouseEnter(e);
				}
				public void mouseLeave(MouseEvent e) {
					handleMouseLeave(e);
				}
			});
		addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					GlyphHolder h = (GlyphHolder)e.widget;
					h.getChild().dispose();
					if (h.offscreenDrawable != null) {
						h.offscreenDrawable.dispose();
						h.offscreenDrawable = null;
					}
					if (h.offscreenGC != null) {
						h.offscreenGC.dispose();
						h.offscreenGC = null;
					}
				}
			});
	}
	
	public boolean isPhantomed() {
	    return phantoms.size() > 0;
	}
	
	public void addPhantom(PhantomView phantom) {
	    phantoms.add(phantom);
	}
	
	public void removePhantom(PhantomView phantom) {
	    phantoms.remove(phantom);
	}
	
	public void redrawPhantoms(int x, int y, int width, int height) {
	    Iterator i = phantoms.iterator();
	    while(i.hasNext()) {
	        PhantomView p = (PhantomView)i.next();
	        p.redraw(x, y, width, height, false);
	    }
	}
	
	public void controlResizedImpl(ControlEvent e) {
	    Rectangle r = getBounds();
		int w = r.width, h = r.height;
		r.x = r.y = 0;
		if (r.width  <= 0) r.width  = 1;
		if (r.height <= 0) r.height = 1;

		if (offscreenDrawable != null)
			offscreenDrawable.dispose();
		if (offscreenGC != null) {
			offscreenGC.dispose();
			offscreenGC = null;
		}
		offscreenDrawable = new Image
			(getDisplay(), r);
		child.setBounds(0,0,w,h);
	}
	
	public void paintControlImpl(PaintEvent e) {
	    if (child.isVisible()) {
	        drawOffscreen(e);

			e.gc.drawImage
				(offscreenDrawable,
				 e.x, e.y, e.width,
				 e.height, e.x, e.y, 
				 e.width, e.height);
		}
	}
	
	protected void drawOffscreen(PaintEvent e) {
		GC gc = getOffscreenGC();
		Color bg = gc.getBackground();
		gc.setBackground
			(e.gc.getBackground());
		gc.fillRectangle
			(e.x, e.y, 
			 e.width, e.height);
		gc.setBackground(bg);
		bg.dispose();
		child.paint(e, null);
	}

	public GlyphComposite getChild() {
		return child;
	}
	public GC getOffscreenGC() { 
		if (offscreenGC == null) { 
			offscreenGC = new GC(offscreenDrawable);
		}
		return offscreenGC;
	}
	
	/** for testing only */
	public Image getOffscreenDrawable() {
	    return offscreenDrawable;
	}

	void notifyAddDescendant(Glyph glyph) {
	}
	void notifyRemoveDescendant(Glyph glyph) {
		if (child == glyph) child = null;
		if (currentGlyph == glyph) currentGlyph = null;
		if (cursorGlyph  == glyph) {
			/* XXX: reset the cursor to our own cursor */
			activateCursor();
			cursorGlyph  = null;
		}
	}
	public void freeze() {
		frozenCount++;
	}
	public void thaw() {
		if (frozenCount <= 0) return;
		frozenCount--;
		if (missedRedraw != null) {
			redraw(missedRedraw.x, missedRedraw.y, 
			       missedRedraw.width, missedRedraw.height, true);
			missedRedraw = null;
		}
	}
	public boolean isFrozen() { return (frozenCount > 0); }

	public void redraw() {
		Rectangle r = getBounds();
		redraw(r.x, r.y, r.width, r.height, true);
	}
	boolean fullDraw = true;
	Rectangle lastBounds = null;
	public void redraw(int x, int y, int w, int h, boolean all) {
		if (isFrozen()) {
			Rectangle r = new Rectangle(x, y, w, h);
			if (missedRedraw==null)
				missedRedraw = r;
			else
				missedRedraw = missedRedraw.union(r);
		} else {
			if (!isDisposed()) {
				if(!this.isPhantomed() || this.isVisible()) {
				    fullDraw = true;
				    lastBounds = null;
				    super.redraw(x, y, w, h, all);
				}
				else {
				    // if phantomed, then i need to draw anyway so the phantoms
				    // can see it.
				    // concoct a paint event
				    System.out.println("drawing offscreen for phantoms");
				    Event e = new Event();
				    Rectangle bounds;
				    if(fullDraw) {
				        bounds = getBounds();
				        fullDraw = false;
				    } else {
				        bounds = new Rectangle(x, y, w, h);
				    }
				    Rectangle redraw;
				    if(lastBounds == null) {
				        redraw = bounds;
				        lastBounds = bounds;
				    } else {
				        redraw = lastBounds.union(bounds);
				    }
				    e.setBounds(redraw);
				    e.count = 1;
				    e.display = getDisplay();
				    e.doit = true;
				    e.gc = getOffscreenGC();
				    e.item = this;
				    e.widget = this;
				    PaintEvent pe = new PaintEvent(e);
				    this.drawOffscreen(pe);
				    redrawPhantoms(redraw.x, redraw.y, redraw.width, redraw.height);
				    lastBounds = bounds;
				}
			}
		}
	}

	public void setCursor(Cursor cursor) {
		myCursor = cursor;
		if (cursorGlyph==null) activateCursor();
	}
	void activateCursor() {
		activateCursor(myCursor, null);
	}
	void activateCursor(Cursor cursor, Glyph glyph) {
		this.cursorGlyph = glyph;
		if (showingCursor != cursor) {
			super.setCursor(cursor);
			showingCursor = cursor;
		}
	}
	Glyph activeCursorGlyph() { return cursorGlyph; }
	public void handleMouseMove(MouseEvent e) {
		if(isFrozen()) return;
		Glyph glyph = child.pickGlyphAt(e.x, e.y, true);
		if (glyph != currentGlyph) {
			if (currentGlyph != null) currentGlyph.leave(e);
			currentGlyph = glyph;
			if (currentGlyph != null) {
				currentGlyph.enter(e);
				currentGlyph.activateCursor();
			}
		}
	}
	public void handleMouseEnter(MouseEvent e) {
		if(isFrozen()) return;
		Glyph glyph = child.pickGlyphAt(e.x, e.y, true);
		if (glyph != currentGlyph) {
			if (currentGlyph != null) currentGlyph.leave(e);
			currentGlyph = glyph;
			if (currentGlyph != null) {
				currentGlyph.enter(e);
				currentGlyph.activateCursor();
			}
		}
	}
	public void handleMouseLeave(MouseEvent e) {
	}
	public void handleMouseEvent(int eventType, MouseEvent e) {
		if(isFrozen()) return;
		Glyph glyph = child.pickGlyphAt(e.x, e.y, true);
		if (glyph != null) {
			Point p = new Point(e.x, e.y);
			if (glyph.getParent() != null) 
				p = glyph.getParent().toGlyph(p);

			int saveX = e.x, saveY = e.y;
			e.x = p.x;
			e.y = p.y;
			
			glyph.handleMouseEvent(eventType, e);

			e.y = saveY;
			e.x = saveX;
		}
	}
}

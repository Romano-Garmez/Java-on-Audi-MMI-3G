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
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * A phantom view is a phantom represenation of a GlyphHolder.  It appears and behaves
 * to be just like the original GlyphHolder it was created with.  This fakes reparenting
 * in swt for the Glyph hierarchy.
 */
public class PhantomView extends Canvas {
    
    GlyphHolder original;
    
    public PhantomView(GlyphHolder holder, Composite parent, int style) {
        super(parent, style);
        this.original = holder;
        original.addPhantom(this);
        original.addPaintListener(new PaintListener() {
           public void paintControl(PaintEvent e) {
               redraw(e.x, e.y, e.width, e.height, true);
           }
        });
		original.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
			    System.out.println("control resized");
				setSize(original.getSize());
			}
		});
        addPaintListener(new PaintListener() {
           public void paintControl(PaintEvent e) {
               e.gc.drawImage(original.getOffscreenDrawable(),
                       e.x, e.y, e.width, e.height, e.x, e.y,
                       e.width, e.height);
           }
        });
        addMouseListener(new MouseListener() {
			public void mouseDown(MouseEvent e) {
				original.handleMouseEvent(SWT.MouseDown, e);
				original.notifyListeners(SWT.MouseDown,
				        mouseEventToEvent(e, SWT.MouseDown));
			}
			public void mouseUp(MouseEvent e) {
				original.handleMouseEvent(SWT.MouseUp,e);
				original.notifyListeners(SWT.MouseUp,
				        mouseEventToEvent(e, SWT.MouseUp));
			}
			public void mouseDoubleClick(MouseEvent e) {
				original.handleMouseEvent(SWT.MouseDoubleClick,
						 e);
				original.notifyListeners(SWT.MouseDoubleClick,
				        mouseEventToEvent(e, SWT.MouseDoubleClick));
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				original.handleMouseMove(e);
				original.notifyListeners(SWT.MouseMove,
				        mouseEventToEvent(e, SWT.MouseMove));
			}
		});
		addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				original.handleMouseEnter(e);
				original.notifyListeners(SWT.MouseEnter,
				        mouseEventToEvent(e, SWT.MouseEnter));
			}
			public void mouseLeave(MouseEvent e) {
				original.handleMouseLeave(e);
				original.notifyListeners(SWT.MouseExit,
				        mouseEventToEvent(e, SWT.MouseExit));
			}
		});
		addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent arg0) {
                removeMe();
            }
		    
		});
        // phantoms are fixed to the size of the originals
        this.setSize(original.getSize());
    }
    
    protected void removeMe() {
        original.removePhantom(this);
    }
    
    protected Event mouseEventToEvent(MouseEvent me, int type) {
        Event e = new Event();
        e.button = me.button;
        e.data = me.data;
        e.display = me.display;
        e.item = me.widget;
        e.widget = me.widget;
        e.stateMask = me.stateMask;
        e.time = me.time;
        e.type = type;
        e.x = me.x;
        e.y = me.y;
        return e;
    }

}

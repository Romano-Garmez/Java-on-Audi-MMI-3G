package org.placelab.demo.mapview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 *
 */
public class BoundingBox extends Composite {

	public BoundingBox(Composite parent) {
		super(parent,SWT.NO_BACKGROUND);
//		addPaintListener(new SimplePaintListener());
	}
	
	public void paintSelf(GC gc) {
		Rectangle bounds=getBounds();
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

		gc.drawOval(0,0,bounds.width-1,bounds.height-1);
		gc.drawRectangle(0,0,bounds.width-1,bounds.height-1);
	}
	
	class SimplePaintListener implements PaintListener {
		public void paintControl(PaintEvent e) {
			if (getVisible()) {
				
				GC gc=e.gc;
				
				Rectangle bounds=getBounds();
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

//				gc.drawOval(0,0,bounds.width-1,bounds.height-1);
				gc.drawRectangle(0,0,bounds.width-1,bounds.height-1);
			}
			
		}
	}	
	
}

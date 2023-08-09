package org.placelab.demo.mapview;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.placelab.util.StringUtil;

/**
 * 
 */
public class TextInfoBox  extends Composite implements MouseListener {

	private static Hashtable boxes = new Hashtable();
	public static int TIMEOUT = 10000;
	Label txtLabel /*,icoLabel */;
	
	int gap = 5;
	int xgap = 5;

	public TextInfoBox(Composite parent, String text, Image img) {
		super(parent,SWT.SHADOW_NONE);
		addPaintListener(new SimplePaintListener(this));
		// add the text control
		setVisible(true);
		//this.setAlignment(SWT.LEFT);

		Color c= new Color(getDisplay(),255,255,180);
		this.setBackground(c);

/*
		icoLabel = new Label(this,SWT.WRAP);
		icoLabel.setImage(img);
		icoLabel.setVisible(true);
		icoLabel.setBackground(c);
		Point p = icoLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT);
		icoLabel.setBounds(gap,gap+xgap,p.x,p.y);
		
*/
		
		text = fixText(text);
		txtLabel = new Label(this,SWT.WRAP);
		txtLabel.setText(text);
		txtLabel.setVisible(true);
		txtLabel.setBackground(c);
		Point p2 = txtLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT);
		txtLabel.setBounds(gap,/*p.y+*/1*gap+xgap,p2.x,p2.y);
		
		
		Font f = new Font(getDisplay(),getFont().getFontData()[0].getName(),9,
			                    getFont().getFontData()[0].getStyle());
		setFont(f);
		boxes.put(this,new Long(new Date().getTime() + TIMEOUT));
		addMouseListener(this);
		txtLabel.addMouseListener(this);
		this.moveAbove(null);

	}
	
	public Point computeSize(int x, int y) {
		Point p = txtLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT);
	//	Point p2 = icoLabel.computeSize(SWT.DEFAULT,SWT.DEFAULT);
		return new Point(p.x + /* p2.x + */ 2*gap, p.y + /* p2.y + */ 2*gap + xgap);
	}
	
	public Point computeSize(int x, int y, boolean b) {
		return computeSize(x,y);
	}
	
	class SimplePaintListener implements PaintListener {
		TextInfoBox t;
		
		public SimplePaintListener(TextInfoBox _t) {
			t = _t;
		}
		
		public void paintControl(PaintEvent e) {
			Color c= new Color(t.getDisplay(),50,50,50);
			e.gc.setForeground(c);
			int w = t.getBounds().width;
			int h = t.getBounds().height;
			e.gc.drawRectangle(0,0,w-1,h-1);

			int a = 5;
			int bx = 5;
			int by = 4;
			e.gc.drawLine(w-a-bx,a+by,w-bx,by);
			e.gc.drawLine(w-a-bx,by,w-bx,a+by);
		}
	}	

	public void dumpIt() {
		boxes.remove(this);
		this.dispose();
	}

	public static String fixText(String s) {
		String sarr[];
		sarr = StringUtil.split(s,'|');
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<sarr.length; i++) {
			sb.append(sarr[i] + "\n");
		}
		return sb.toString();
	}	
	
	public static void expireBoxes() {
		long time = new Date().getTime();
		for (Enumeration en = boxes.keys(); en.hasMoreElements(); ) {
			TextInfoBox t = (TextInfoBox)en.nextElement();
			long ttime = ((Long)boxes.get(t)).longValue();
			if (ttime < time) {
				t.dumpIt();
				expireBoxes(); // I know we recurse. This is because the enemerator isn't too clever, so we need a new one
				return; 
			}
		}	
	}
	

	public void mouseDoubleClick(MouseEvent e) {			
		dumpIt();
	}
	public void mouseDown(MouseEvent e) {
		dumpIt();
	}
	public void mouseUp(MouseEvent e) {
	}



}

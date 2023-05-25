package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.util.StringUtil;

public class GlyphLabel extends Glyph {
	private String text, wrappedLines;
	private Font font;
	private int maxWidth;
	private Color color, bg;
	private Point wrappedExtent;

	private void init() {
		text = "";
		wrappedLines = "";
		font = null;
		maxWidth = 0;
		color = null;
		wrappedExtent = new Point(0, 0);
	}

	public GlyphLabel(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphLabel(GlyphHolder holder, int style) {
		super(holder, style);
		init();
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
	public void setFont(Font font) {
		Rectangle invalidate = getBounds();
		this.font = font;
		rewrap();
		redraw(invalidate);
	}
	public void setWidth(int maxWidth) {
		Rectangle invalidate = getBounds();
		this.maxWidth = maxWidth;
		rewrap();
		
		redraw(invalidate);
	}
	public void setForeground(Color c) {
		color = c;
		
		redraw(null);
	}
	public Color getForeground() { return color; }
	public void setBackground(Color c) {
	    bg = c;
	    
	    redraw(null);
	}

	public void setLocation(int x, int y) {
		setLocation(new Point(x, y));
	}

	public void setText(String text) {
		Rectangle invalidate = getBounds();
		this.text = text;
		rewrap();
		
		redraw(invalidate);
	}
	public void setText(String text, boolean wrap) {
		Rectangle invalidate = getBounds();
		this.text = text;
		if (!wrap) maxWidth = 0;
		else {
			Point extent = textExtent(text);
			if (extent.x <= extent.y * 4 / 3) maxWidth = 0;
			else {
				int area = extent.x * extent.y;
				maxWidth = (int)(Math.sqrt(area * 4 / 3)*1.2);
			}
		}
		rewrap();
		
		redraw(invalidate);		
	}

	public void paintImpl(PaintEvent e, GlyphGC gc) {
	    
		super.paintImpl(e, gc);
		if (wrappedLines == null || wrappedLines.equals("")) return;

		Rectangle paintArea = new Rectangle(e.x,e.y,e.width,e.height);
		if (! getBounds().intersects(paintArea)) return;

		Font f = null;
		Color c = null, oldBack = null;
		if (font != null) {
			f = gc.getFont();
			gc.setFont(font);
		}
		if (color != null) {
			c = gc.getForeground();
			gc.setForeground(color);
		} if(bg != null) {
		    oldBack = gc.getBackground();
		    gc.setBackground(bg);
			gc.drawTextWithBackground(wrappedLines, getLocation().x, getLocation().y);
		} else {
		    gc.drawText(wrappedLines, getLocation().x, getLocation().y);
		}
		if (color != null) gc.setForeground(c);
		if (bg != null) gc.setBackground(oldBack);
		if (font  != null) gc.setFont(f);
	}

	private String trimTrailingWhitespace(String string) {
		for (int i=string.length()-1; i >=0; i--) {
			if (string.charAt(i) != ' ') {
				if (i==string.length()) return string;
				return string.substring(0, i+1);
			}
		}
		return "";
	}
	private String trimLeadingWhitespace(String string) {
		for (int i=0; i < string.length(); i++) {
			if (string.charAt(i) != ' ') {
				if (i==0) return string;
				return string.substring(i);
			}
		}
		return "";
	}

	private String dropWord(String string) {
		int i=string.lastIndexOf(' ');
		if (i < 0) return string.substring(0, string.length()-1);
		
		while (i >= 0 && string.charAt(i) == ' ') i--;
		if (i < 0) return "";
		return string.substring(0, i+1);
	}
	private void rewrap() {
		wrappedLines = text;

		if (maxWidth <= 0) {
			wrappedExtent = textExtent(wrappedLines);
			return;
		}
		Point extent = textExtent(wrappedLines);
		if (extent.x <= maxWidth) {
			wrappedExtent = textExtent(wrappedLines);
			return;
		}

		wrappedLines = "";
		String lines[] = StringUtil.split(text, '\n');
		for (int i=0; i < lines.length; i++) {
			String line = trimTrailingWhitespace(lines[i]);
			if (line.length() <= 0) {
				if (i < lines.length-1) wrappedLines += "\n";
			}
			while (line.length() > 0) {
				String save = line;
				do {
					extent = textExtent(line);
					if (extent.x <= maxWidth) break;
					line = dropWord(line);
					if (line.length() <= 1) break;
				} while (true);
				if (!wrappedLines.equals("")) 
					wrappedLines += "\n";
				wrappedLines += line;
				
				line = trimLeadingWhitespace
					(save.substring(line.length()));
			}
		}
		wrappedExtent = textExtent(wrappedLines);
	}
	public Rectangle getBoundsImpl() {
		return new Rectangle(getLocation().x, getLocation().y, 
				     wrappedExtent.x, wrappedExtent.y);
	}
	public boolean pointInsideImpl(int x, int y) {
		return (getBounds().contains(x, y));
	}
}

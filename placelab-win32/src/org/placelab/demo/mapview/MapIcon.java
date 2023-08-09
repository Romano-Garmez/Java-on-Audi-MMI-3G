package org.placelab.demo.mapview;

/**
 * 
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.util.swt.Glyph;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphImage;


public class MapIcon extends GlyphImage {
	private MapView mapUI;
	private GlyphHolder holder;
	private PlaceBacking data;
	private IconTextInfo textInfo;
	private Color iconTextBg;


	private class IconTextInfo extends TextInfoGlyph {
		private class Timeout implements Runnable {
			private IconTextInfo info;
			public Timeout(IconTextInfo i) { info = i; }
			public void run() { info.timeout(); }
		}


		private Timeout timeout;
		private final int TEXTINFO_TIMEOUT=10000;

		public IconTextInfo(GlyphHolder holder, Color bg, String text,
				    int x, int y) {
			super(holder, SWT.NONE);

			setBackground(bg);
			setText(text, true);
			setLocation(x, y);

			addMouseListener(new MouseAdapter() {
					public void mouseDown
						(MouseEvent e) {
						handleMouseDown(e);
					}
				});
			timeout = null;
		}

		public void show() {
			setVisible(true);
			moveAbove(null);
			if (timeout != null)
				holder.getDisplay().timerExec(-1, timeout);
			timeout = new Timeout(this);
			holder.getDisplay().timerExec(TEXTINFO_TIMEOUT,
					timeout);
		}
		public void hide() {
			setVisible(false);
			if (timeout != null) {
				holder.getDisplay().timerExec(-1, timeout);
				timeout = null;
			}
		}

		void handleMouseDown(MouseEvent e) {
			hide();
		}

		void timeout() {
			timeout = null;
			hide();
		}
	}

	
	public MapIcon(MapView ui, PlaceBacking data, Color textBg,
		       double zoom) {
		super(ui.getHolder(), SWT.NONE);
		this.mapUI = ui;
		this.holder = ui.getHolder();
		this.data = data;
		this.setImage(new Image(holder.getDisplay(), data.getImageResource()),
				ui.longitudeToPixels(data.lon), 
				ui.latitudeToPixels(data.lat), Glyph.ANCHOR_NW);

		this.iconTextBg = textBg;

		this.setZoom(zoom);
		//glyph.setCloseEnough(1);
		this.setCursor(mapUI.getIconCursor());
		this.setFlag(Glyph.IGNORE_TRANSPARENCY);
		textInfo = null;
		addMouseListener(new MouseAdapter() {
			public void mouseDown
				(MouseEvent e) {
				moused(e);
			}
		});
	}
	
	public void setZoom(double d) {
		super.setZoom(d);
		//System.out.println("icon for " + data.name + " asked to zoom");
	}
	
	protected Image getZoomedImage() {
		// MapIcons don't scale their images, since they
		// are just points and don't cover any area
		return originalImage;
	}

	public void dispose() {
		super.dispose();
		if (textInfo != null) textInfo.dispose();
		textInfo = null;
	}
	public Glyph getGlyph() { return this; }
	
	// helper method to switch the _ to spaces
	private String replaceAll(String str, String original, 
				  String replacement) {
		StringBuffer ret = new StringBuffer(str);
		int index = 0;
		while (index < ret.length()) {
			index = ret.toString().indexOf(original, index);
			if (index < 0) break;
			ret.replace(index, index+original.length(), 
				    replacement);
		}
		return ret.toString();
	}
	private String unescape(String text) {
		text = replaceAll(text, "|", "\n");
		text = replaceAll(text, "\\n", "\n");
		return text;
	}
	public void moused(MouseEvent e) {
		if (textInfo == null) {
			Rectangle r = this.getBounds();
			textInfo = new IconTextInfo(holder, iconTextBg,
						    unescape(data.text),
						    r.x + r.width/2,
						    r.y + r.height + 2);
			textInfo.show();
		} else {
			if (textInfo.isVisible()) textInfo.hide();
			else textInfo.show();
		}
	}
}


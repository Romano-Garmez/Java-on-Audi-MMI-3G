/*
 * @(#)src/demo/applets/ImageMap/ButtonFilter.java, ui, dsdev 1.11
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v5.0
 * (C) Copyright IBM Corp. 1998, 2005. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */





/*
 * @(#)ButtonFilter.java	1.11 02/06/13
 */

import java.applet.Applet;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Hashtable;
import java.net.URL;
import java.awt.image.*;
import java.net.MalformedURLException;

/**
 * An extensible ImageMap applet class.
 * The active areas on the image are controlled by ImageArea classes
 * that can be dynamically loaded over the net.
 *
 * @author 	Jim Graham
 * @version 	1.11, 06/13/02
 */
class ButtonFilter extends RGBImageFilter {
    boolean pressed;
    int defpercent;
    int border;
    int width;
    int height;

    ColorModel models[] = new ColorModel[7];
    ColorModel origbuttonmodel;

    public ButtonFilter(boolean press, int p, int b, int w, int h) {
	pressed = press;
	defpercent = p;
	border = b;
	width = w;
	height = h;
    }

    public void setHints(int hints) {
	super.setHints(hints & (~ImageConsumer.COMPLETESCANLINES));
    }

    public void setColorModel(ColorModel model) {
	if (model instanceof IndexColorModel && true) {
	    IndexColorModel icm = (IndexColorModel) model;
	    models[0] = filterIndexColorModel(icm, false, false, 0);
	    models[1] = filterIndexColorModel(icm, true, !pressed, defpercent);
	    models[2] = null;
	    if (pressed) {
		models[3] = filterIndexColorModel(icm, true, false,
						  defpercent/2);
	    } else {
		models[3] = models[0];
	    }
	    models[4] = null;
	    models[5] = filterIndexColorModel(icm, true, pressed, defpercent);
	    models[6] = models[0];
	    origbuttonmodel = model;
	    consumer.setColorModel(models[3]);
	} else {
	    super.setColorModel(model);
	}
    }

    public IndexColorModel filterIndexColorModel(IndexColorModel icm,
						 boolean opaque,
						 boolean brighter,
						 int percent) {
	byte r[] = new byte[256];
	byte g[] = new byte[256];
	byte b[] = new byte[256];
	byte a[] = new byte[256];
	int mapsize = icm.getMapSize();
	icm.getReds(r);
	icm.getGreens(g);
	icm.getBlues(b);
	if (opaque) {
	    icm.getAlphas(a);
	    for (int i = 0; i < mapsize; i++) {
		int rgb = filterRGB(icm.getRGB(i), brighter, percent);
		a[i] = (byte) (rgb >> 24);
		r[i] = (byte) (rgb >> 16);
		g[i] = (byte) (rgb >> 8);
		b[i] = (byte) (rgb >> 0);
	    }
	}
	return new IndexColorModel(icm.getPixelSize(), mapsize, r, g, b, a);
    }

    /**
     * Define the ranges of varying highlight for the button.
     * ranges is an array of 8 values which split up a scanline into
     * 7 different regions of highlighting effect:
     *
     * ranges[0-1] = area outside of left edge of button
     * ranges[1-2] = area inside UpperLeft highlight region left of center
     * ranges[2-3] = area requiring custom highlighting left of center
     * ranges[3-4] = area inside center of button
     * ranges[4-5] = area requiring custom highlighting right of center
     * ranges[5-6] = area inside LowerRight highlight region right of center
     * ranges[6-7] = area outside of right edge of button
     * ranges[8-9] = y coordinates for which these ranges apply
     *
     * Note that ranges[0-1] and ranges[6-7] are empty where the edges of
     * the button touch the left and right edges of the image (everywhere
     * on a square button) and ranges[2-3] and ranges[4-5] are only nonempty
     * in those regions where the UpperLeft highlighting has leaked over
     * the "top" of the button onto parts of its right edge or where the
     * LowerRight highlighting has leaked under the "bottom" of the button
     * onto parts of its left edge (can't happen on square buttons, happens
     * occasionally on round buttons).
     */
    public void buttonRanges(int y, int ranges[]) {
	ranges[0] = ranges[1] = 0;
	if (y < border) {
	    ranges[2] = ranges[3] = ranges[4] = ranges[5] = width - y;
	    ranges[8] = ranges[9] = y;
	} else if (y > height - border) {
	    ranges[2] = ranges[3] = ranges[4] = ranges[5] = height - y;
	    ranges[8] = ranges[9] = y;
	} else {
	    ranges[2] = ranges[3] = border;
	    ranges[4] = ranges[5] = width - border;
	    ranges[8] = border;
	    ranges[9] = height - border;
	}
	ranges[6] = ranges[7] = width;
    }

    private int savedranges[];

    protected int[] getRanges(int y) {
	int ranges[] = savedranges;
	if (ranges == null) {
	    ranges = savedranges = new int[10];
	    ranges[8] = ranges[9] = -1;
	}
	if (y < ranges[8] || y > ranges[9]) {
	    buttonRanges(y, ranges);
	}
	return ranges;
    }

    public void setPixels(int x, int y, int w, int h,
			  ColorModel model, byte pixels[], int off,
			  int scansize) {
	if (model == origbuttonmodel) {
	    int ranges[] = getRanges(y);
	    int x2 = x + w;
	    int y2 = y + h;
	    for (int cy = y; cy < y2; cy++) {
		if (cy < ranges[8] || cy > ranges[9]) {
		    buttonRanges(cy, ranges);
		}
		for (int i = 0; i < 7; i++) {
		    if (x2 > ranges[i] && x < ranges[i+1]) {
			int cx1 = Math.max(x, ranges[i]);
			int cx2 = Math.min(x2, ranges[i+1]);
			if (models[i] == null) {
			    super.setPixels(cx1, cy, cx2 - cx1, 1,
					    model, pixels,
					    off + (cx1 - x), scansize);
			} else {
			    if (cx1 < cx2) {
				consumer.setPixels(cx1, cy, cx2 - cx1, 1,
						   models[i], pixels,
						   off + (cx1 - x), scansize);
			    }
			}
		    }
		}
		off += scansize;
	    }
	} else {
	    super.setPixels(x, y, w, h, model, pixels, off, scansize);
	}
    }

    public int filterRGB(int x, int y, int rgb) {
	boolean brighter;
	int percent;
	if ((x < border && y < height - x) || (y < border && x < width - y)) {
	    brighter = !pressed;
	    percent = defpercent;
	} else if (x >= width - border || y >= height - border) {
	    brighter = pressed;
	    percent = defpercent;
	} else if (pressed) {
	    brighter = false;
	    percent = defpercent / 2;
	} else {
	    return rgb & 0x00ffffff;
	}
	return filterRGB(rgb, brighter, percent);
    }

    public int filterRGB(int rgb, boolean brighter, int percent) {
	int r = (rgb >> 16) & 0xff;
	int g = (rgb >> 8) & 0xff;
	int b = (rgb >> 0) & 0xff;
	if (brighter) {
	    r = (255 - ((255 - r) * (100 - percent) / 100));
	    g = (255 - ((255 - g) * (100 - percent) / 100));
	    b = (255 - ((255 - b) * (100 - percent) / 100));
	} else {
	    r = (r * (100 - percent) / 100);
	    g = (g * (100 - percent) / 100);
	    b = (b * (100 - percent) / 100);
	}
	return (rgb & 0xff000000) | (r << 16) | (g << 8) | (b << 0);
    }

  public String getAppletInfo() {
    return "Title: ButtonFilter \nAuthor: Jim Graham \nAn extensible ImageMap applet class.  The active areas on the image are controlled by ImageArea classes that can be dynamically loaded over the net.";
  }
}

/*
 * @(#)src/demo/applets/ImageMap/RoundButtonFilter.java, ui, dsdev 1.11
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
 * @(#)RoundButtonFilter.java	1.11 02/06/13
 */

/**
 * An extensible ImageMap applet class.
 * The active areas on the image are controlled by ImageArea classes
 * that can be dynamically loaded over the net.
 *
 * @author 	Jim Graham
 * @version 	1.11, 06/13/02
 */
class RoundButtonFilter extends ButtonFilter {
    int Xcenter;
    int Ycenter;
    int Yradsq;
    int innerW;
    int innerH;
    int Yrad2sq;

    public RoundButtonFilter(boolean press, int p, int b, int w, int h) {
	super(press, p, b, w, h);
	Xcenter = w/2;
	Ycenter = h/2;
	Yradsq = h * h / 4;
	innerW = w - border * 2;
	innerH = h - border * 2;
	Yrad2sq = innerH * innerH / 4;
    }

    public boolean inside(int x, int y) {
	int yrel = Math.abs(Ycenter - y);
	int xrel = (int) (Math.sqrt(Yradsq - yrel * yrel) * width / height);
	return (x >= Xcenter - xrel && x < Xcenter + xrel);
    }

    public void buttonRanges(int y, int ranges[]) {
	int yrel = Math.abs(Ycenter - y);
	int xrel = (int) (Math.sqrt(Yradsq - yrel * yrel) * width / height);
	ranges[0] = 0;
	ranges[1] = Xcenter - xrel;
	ranges[6] = Xcenter + xrel;
	ranges[7] = width;
	ranges[8] = ranges[9] = y;
	if (y < border) {
	    ranges[2] = ranges[3] = ranges[4] = Xcenter;
	    ranges[5] = ranges[6];
	} else if (y + border >= height) {
	    ranges[2] = ranges[1];
	    ranges[3] = ranges[4] = ranges[5] = Xcenter;
	} else {
	    int xrel2 = (int) (Math.sqrt(Yrad2sq - yrel * yrel)
			       * innerW / innerH);
	    ranges[3] = Xcenter - xrel2;
	    ranges[4] = Xcenter + xrel2;
	    if (y < Ycenter) {
		ranges[2] = ranges[3];
		ranges[5] = ranges[6];
	    } else {
		ranges[2] = ranges[1];
		ranges[5] = ranges[4];
	    }
	}
    }

    public int filterRGB(int x, int y, int rgb) {
	boolean brighter;
	int percent;
	int i;
	int xrel, yrel;
	int ranges[] = getRanges(y);
	for (i = 0; i < 7; i++) {
	    if (x >= ranges[i] && x < ranges[i+1]) {
		break;
	    }
	}
	switch (i) {
	default:
	case 0:
	case 6:
	    return rgb & 0x00ffffff;
	case 1:
	    brighter = !pressed;
	    percent = defpercent;
	    break;
	case 5:
	    brighter = pressed;
	    percent = defpercent;
	    break;
	case 2:
	    yrel = y - Ycenter;
	    xrel = Xcenter - x;
	    percent = (int) (yrel * defpercent * 2 /
			     Math.sqrt(yrel * yrel + xrel * xrel))
		- defpercent;
	    if (!pressed) {
		percent = -percent;
	    }
	    if (percent == 0) {
		return rgb;
	    } else if (percent < 0) {
		percent = -percent;
		brighter = false;
	    } else {
		brighter = true;
	    }
	    break;
	case 4:
	    yrel = Ycenter - y;
	    xrel = x - Xcenter;
	    percent = (int) (yrel * defpercent * 2 /
			     Math.sqrt(yrel * yrel + xrel * xrel))
		- defpercent;
	    if (pressed) {
		percent = -percent;
	    }
	    if (percent == 0) {
		return rgb;
	    } else if (percent < 0) {
		percent = -percent;
		brighter = false;
	    } else {
		brighter = true;
	    }
	    break;
	case 3:
	    if (!pressed) {
		return rgb & 0x00ffffff;
	    }
	    brighter = false;
	    percent = defpercent;
	    break;
	}
	return filterRGB(rgb, brighter, percent);
    }
}

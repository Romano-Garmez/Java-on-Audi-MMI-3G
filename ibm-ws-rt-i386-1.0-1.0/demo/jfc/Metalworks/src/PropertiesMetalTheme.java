/*
 * @(#)src/demo/jfc/Metalworks/src/PropertiesMetalTheme.java, swing, dsdev 1.11
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
 * @(#)PropertiesMetalTheme.java	1.8 02/06/13
 */


import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This class allows you to load a theme from a file.
 * It uses the standard Java Properties file format.
 * To create a theme you provide a text file which contains
 * tags corresponding to colors of the theme along with a value
 * for that color.  For example:
 *
 * name=My Ugly Theme
 * primary1=255,0,0
 * primary2=0,255,0
 * primary3=0,0,255
 *
 * This class only loads colors from the properties file,
 * but it could easily be extended to load fonts -  or even icons.
 *
 * @version 1.8 06/13/02
 * @author Steve Wilson
 */
public class PropertiesMetalTheme extends DefaultMetalTheme {

    private String name = "Custom Theme";

    private ColorUIResource primary1;
    private ColorUIResource primary2;
    private ColorUIResource primary3;

    private ColorUIResource secondary1;
    private ColorUIResource secondary2;
    private ColorUIResource secondary3;

    private ColorUIResource black;
    private ColorUIResource white;


    /**
      * pass an inputstream pointing to a properties file.
      * Colors will be initialized to be the same as the DefaultMetalTheme,
      * and then any colors provided in the properties file will override that.
      */
    public PropertiesMetalTheme( InputStream stream ) {
        initColors();
        loadProperties(stream);
    }

    /**
      * Initialize all colors to be the same as the DefaultMetalTheme.
      */
    private void initColors() {
        primary1 = super.getPrimary1();
        primary2 = super.getPrimary2();
        primary3 = super.getPrimary3();

        secondary1 = super.getSecondary1();
        secondary2 = super.getSecondary2();
        secondary3 = super.getSecondary3();

	black = super.getBlack();
	white = super.getWhite();
    }

    /**
      * Load the theme name and colors from the properties file
      * Items not defined in the properties file are ignored
      */
    private void loadProperties(InputStream stream) {
	Properties prop = new Properties();
	try {
	    prop.load(stream);
	} catch (IOException e) {
	    System.out.println(e);
	}

	Object tempName = prop.get("name");
	if (tempName != null) {
	    name = tempName.toString();
	}

	Object colorString = null;

	colorString = prop.get("primary1");
	if (colorString != null){
	    primary1 = parseColor(colorString.toString());
	}

	colorString = prop.get("primary2");
	if (colorString != null) {
	    primary2 = parseColor(colorString.toString());
	}

	colorString = prop.get("primary3");
	if (colorString != null) {
	    primary3 = parseColor(colorString.toString());
	}

	colorString = prop.get("secondary1");
	if (colorString != null) {
	    secondary1 = parseColor(colorString.toString());
	}

	colorString = prop.get("secondary2");
	if (colorString != null) {
	    secondary2 = parseColor(colorString.toString());
	}

	colorString = prop.get("secondary3");
	if (colorString != null) {
	    secondary3 = parseColor(colorString.toString());
	}

	colorString = prop.get("black");
	if (colorString != null) {
	    black = parseColor(colorString.toString());
	}

	colorString = prop.get("white");
	if (colorString != null) {
	    white = parseColor(colorString.toString());
	}

    }

    public String getName() { return name; }

    protected ColorUIResource getPrimary1() { return primary1; }
    protected ColorUIResource getPrimary2() { return primary2; }
    protected ColorUIResource getPrimary3() { return primary3; }

    protected ColorUIResource getSecondary1() { return secondary1; }
    protected ColorUIResource getSecondary2() { return secondary2; }
    protected ColorUIResource getSecondary3() { return secondary3; }

    protected ColorUIResource getBlack() { return black; }
    protected ColorUIResource getWhite() { return white; }

    /**
      * parse a comma delimited list of 3 strings into a Color
      */
    private ColorUIResource parseColor(String s) {
        int red = 0;
	int green = 0;
	int blue = 0;
	try {
	    StringTokenizer st = new StringTokenizer(s, ",");

	    red = Integer.parseInt(st.nextToken());
	    green = Integer.parseInt(st.nextToken());
	    blue = Integer.parseInt(st.nextToken());

	} catch (Exception e) {
	    System.out.println(e);
	    System.out.println("Couldn't parse color :" + s);
	}

	return new ColorUIResource(red, green, blue);
    }
}

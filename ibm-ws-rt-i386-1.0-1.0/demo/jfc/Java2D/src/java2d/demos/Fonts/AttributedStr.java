/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Fonts/AttributedStr.java, dsdev, dsdev 1.6
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
 * @(#)AttributedStr.java	1.9 02/06/13
 */

package java2d.demos.Fonts;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java2d.Surface;


/**
 * Demonstrates how to build an AttributedString and then render the
 * string broken over lines. 
 */
public class AttributedStr extends Surface {

    static Color black = new Color(20, 20, 20); 
    static Color blue = new Color(94, 105, 176); 
    static Color yellow = new Color(255, 255, 140);
    static Color red = new Color(149, 43, 42);
    static Color white = new Color(240, 240, 255); 
    static String text = "  A quick brown  fox  jumped  over the lazy duke  ";
    static AttributedString as = new AttributedString(text);
    static AttributedCharacterIterator aci; 
    static {
        Shape shape = new Ellipse2D.Double(0,25,12,12);
	ShapeGraphicAttribute sga = new ShapeGraphicAttribute(shape, GraphicAttribute.TOP_ALIGNMENT, false);
	as.addAttribute(TextAttribute.CHAR_REPLACEMENT, sga, 0, 1);


        Font font = new Font("sanserif", Font.BOLD | Font.ITALIC, 20);
        int index = text.indexOf("quick");
        as.addAttribute(TextAttribute.FONT, font, index, index+5);

        index = text.indexOf("brown");
        font = new Font("serif", Font.BOLD, 20);
        as.addAttribute(TextAttribute.FONT, font, index, index+5);
        as.addAttribute(TextAttribute.FOREGROUND, red, index, index+5);

        index = text.indexOf("fox");
        AffineTransform fontAT = new AffineTransform();
        fontAT.rotate(Math.toRadians(10));
        Font fx = new Font("serif", Font.BOLD, 30).deriveFont(fontAT);
        as.addAttribute(TextAttribute.FONT, fx, index, index+1);
        as.addAttribute(TextAttribute.FONT, fx, index+1, index+2);
        as.addAttribute(TextAttribute.FONT, fx, index+2, index+3);

        fontAT.setToRotation(Math.toRadians(-4));
        fx = font.deriveFont(fontAT);
        index = text.indexOf("jumped");
        as.addAttribute(TextAttribute.FONT, fx, index, index+6);

        font = new Font("serif", Font.BOLD | Font.ITALIC, 30);
        index = text.indexOf("over");
        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, index, index+4);
        as.addAttribute(TextAttribute.FOREGROUND, white, index, index+4);
        as.addAttribute(TextAttribute.FONT, font, index, text.length());

        font = new Font("dialog", Font.PLAIN, 20);
        int i = text.indexOf("duke");
        as.addAttribute(TextAttribute.FONT, font, index, i-1);

        BufferedImage bi = new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, 0xffffffff); 
        TexturePaint tp = new TexturePaint(bi,new Rectangle(0,0,4,4));
        as.addAttribute(TextAttribute.BACKGROUND, tp, i, i+4);
        font = new Font("serif", Font.BOLD, 40);
        as.addAttribute(TextAttribute.FONT, font, i, i+4);
    }


    public AttributedStr() {
        setBackground(Color.white);

        Font font = getFont("A.ttf");
        if (font != null) {
            font = font.deriveFont(Font.PLAIN, 70);
        } else {
            font = new Font("serif", Font.PLAIN, 50);
        }
        int index = text.indexOf("A")+1;
        as.addAttribute(TextAttribute.FONT, font, 0, index);
        as.addAttribute(TextAttribute.FOREGROUND, white, 0, index);

        font = new Font("dialog", Font.PLAIN, 40);
        int size = getFontMetrics(font).getHeight();
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D big = bi.createGraphics();
        big.drawImage(getImage("snooze.gif"), 0, 0, size, size, null);
	ImageGraphicAttribute iga = new ImageGraphicAttribute(bi, GraphicAttribute.TOP_ALIGNMENT);
	as.addAttribute(TextAttribute.CHAR_REPLACEMENT, iga, text.length()-1, text.length());

        aci = as.getIterator();
    }


    public void render(int w, int h, Graphics2D g2) {

        float x = 5, y = 0;
        FontRenderContext frc = g2.getFontRenderContext();
        LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);

        g2.setPaint(new GradientPaint(0,h,blue,w,0,black));
        g2.fillRect(0, 0, w, h);

        g2.setColor(white);
        String s = "AttributedString LineBreakMeasurer";
        Font font = new Font("serif", Font.PLAIN, 12);
        TextLayout tl = new TextLayout(s, font, frc);
        
        tl.draw(g2, 5, y += (float) tl.getBounds().getHeight());

        g2.setColor(yellow);

        while (y < h-tl.getAscent()) {
            lbm.setPosition(0);
            while (lbm.getPosition() < text.length()) {
                tl = lbm.nextLayout(w-x);
                if (!tl.isLeftToRight()) {
                    x = w - tl.getAdvance();
                }
                tl.draw(g2, x, y += tl.getAscent());
                y += tl.getDescent() + tl.getLeading();
            }
        }
    }


    public static void main(String s[]) {
        createDemoFrame(new AttributedStr());
    }
}

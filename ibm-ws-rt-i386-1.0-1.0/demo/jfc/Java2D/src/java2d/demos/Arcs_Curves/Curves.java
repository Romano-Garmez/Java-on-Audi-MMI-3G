/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Arcs_Curves/Curves.java, dsdev, dsdev 1.6
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
 * @(#)Curves.java	1.22 02/06/13
 */

package java2d.demos.Arcs_Curves;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.FlatteningPathIterator;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java2d.Surface;


/**
 * CubicCurve2D & QuadCurve2D curves includes FlattenPathIterator example.
 */
public class Curves extends Surface {

    private static Color colors[] = { Color.blue, Color.green, Color.red };

    public Curves() {
        setBackground(Color.white);
    }


    public void render(int w, int h, Graphics2D g2) {

        int y = 0;
        g2.setColor(Color.black);
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout("QuadCurve2D", g2.getFont(), frc);
        float xx = (float) (w*.5-tl.getBounds().getWidth()/2);
        tl.draw(g2, xx, tl.getAscent());

        tl = new TextLayout("CubicCurve2D", g2.getFont(), frc);
        xx = (float) (w*.5-tl.getBounds().getWidth()/2);
        tl.draw(g2, xx, h*.5f);
        g2.setStroke(new BasicStroke(5.0f));

        float yy = 20;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                Shape shape = null;

                if (i == 0) {
                     shape = new QuadCurve2D.Float(w*.1f,yy,w*.5f,50,w*.9f,yy);
                } else {
                     shape = new CubicCurve2D.Float(w*.1f,yy,w*.4f,yy-15,
                                            w*.6f,yy+15,w*.9f,yy);
                }
                g2.setColor(colors[j]);
                if (j != 2)
                    g2.draw(shape);

                if (j == 1 ) {
                    g2.setColor(Color.lightGray);
                    PathIterator f = shape.getPathIterator(null);
                    while ( !f.isDone() ) {
                        float[] pts = new float[6];
                        switch ( f.currentSegment(pts) ) {
                            case PathIterator.SEG_MOVETO:
                            case PathIterator.SEG_LINETO:
                                g2.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
                                break;
                            case PathIterator.SEG_CUBICTO :
                            case PathIterator.SEG_QUADTO :
                                g2.fill(new Rectangle2D.Float(pts[0], pts[1], 5, 5));
                                if (pts[2] != 0) {
                                    g2.fill(new Rectangle2D.Float(pts[2], pts[3], 5, 5));
                                }
                                if (pts[4] != 0) {
                                    g2.fill(new Rectangle2D.Float(pts[4], pts[5], 5, 5));
                                }
                        }
                        f.next();
                    }
                } else if (j == 2) {
                    PathIterator p = shape.getPathIterator(null);
                    FlatteningPathIterator f = new FlatteningPathIterator(p,0.1);
                    while ( !f.isDone() ) {
                        float[] pts = new float[6];
                        switch ( f.currentSegment(pts) ) {
                            case PathIterator.SEG_MOVETO:
                            case PathIterator.SEG_LINETO:
                                g2.fill(new Ellipse2D.Float(pts[0], pts[1],3,3));
                        }
                        f.next();
                    }
                }
                yy += h/6;
            }
            yy = h/2+15;
        }
    }


    public static void main(String argv[]) {
        createDemoFrame(new Curves());
    }
}

/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Clipping/ClipAnim.java, dsdev, dsdev 1.9
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
 * @(#)ClipAnim.java	1.21 02/06/13
 */

package java2d.demos.Clipping;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java2d.AnimatingControlsSurface;
import java2d.CustomControls;


/**
 * Animated clipping of an image & composited shapes.
 */
public class ClipAnim extends AnimatingControlsSurface {

    private static Image dimg, cimg;
    private static Color redBlend = new Color(255, 0, 0, 120);
    private static Color greenBlend = new Color(0, 255, 0, 120);
    private static BasicStroke bs = new BasicStroke(20.0f);
    static TexturePaint texture;
    static {
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bi.createGraphics();
        big.setBackground(Color.yellow);
        big.clearRect(0,0,5,5);
        big.setColor(Color.red);
        big.fillRect(0,0,3,3);
        texture = new TexturePaint(bi,new Rectangle(0,0,5,5));
    }
    private AnimVal animval[] = new AnimVal[3]; 
    protected boolean doObjects = true;
    private Font originalFont = new Font("serif", Font.PLAIN, 12);
    private Font font;
    private GradientPaint gradient;
    private int strX, strY;
    private int dukeX, dukeY;


    public ClipAnim() {
        cimg = getImage("clouds.jpg");
        dimg = getImage("duke.gif");
        setBackground(Color.white);
        animval[0] = new AnimVal(true);
        animval[1] = new AnimVal(false);
        animval[2] = new AnimVal(false);
        setControls(new Component[] { new DemoControls(this) });
    }


    public void reset(int w, int h) {
        for (int i = 0; i < animval.length; i++) {
            animval[i].reset(w, h);
        }
        gradient = new GradientPaint(0,h/2,Color.red,w*.4f,h*.9f,Color.yellow);
        dukeX = (int) (w*.25-dimg.getWidth(this)/2);
        dukeY = (int) (h*.25-dimg.getHeight(this)/2);
        FontMetrics fm = getFontMetrics(originalFont);
        double sw = fm.stringWidth("CLIPPING");
        double sh = fm.getAscent()+fm.getDescent();
        double sx = (w/2-30)/sw;
        double sy = (h/2-30)/sh;
        AffineTransform Tx = AffineTransform.getScaleInstance(sx, sy);
        font = originalFont.deriveFont(Tx);
        fm = getFontMetrics(font);
        strX = (int) (w*.75 - fm.stringWidth("CLIPPING")/2);
        strY = (int) (h*.72 + fm.getAscent()/2);
    }


    public void step(int w, int h) {
        for (int i = 0; i < animval.length; i++) {
            if (animval[i].isSelected) {
                animval[i].step(w, h);
            }
        }
    }


    public void render(int w, int h, Graphics2D g2) {

        GeneralPath p1 = new GeneralPath();
        GeneralPath p2 = new GeneralPath();

        for (int i = 0; i < animval.length; i++) {
            if (animval[i].isSelected) {
                double x = animval[i].x; double y = animval[i].y;
                double ew = animval[i].ew; double eh = animval[i].eh;
                p1.append(new Ellipse2D.Double(x, y, ew, eh), false);
                p2.append(new Rectangle2D.Double(x+5, y+5, ew-10, eh-10),false);
            }
        }
        if (animval[0].isSelected || animval[1].isSelected || 
            animval[2].isSelected) 
        {
            g2.setClip(p1);
            g2.clip(p2);
        }

        if (doObjects) {
            int w2 = w/2;
            int h2 = h/2;
            g2.drawImage(cimg, 0, 0, w2, h2, null);
            g2.drawImage(dimg, dukeX, dukeY, null);

            g2.setPaint(texture);
            g2.fillRect(w2, 0, w2, h2);

            g2.setPaint(gradient);
            g2.fillRect(0, h2, w2, h2);

            g2.setColor(Color.lightGray);
            g2.fillRect(w2, h2, w2, h2);
            g2.setColor(Color.red);
            g2.drawOval(w2, h2, w2-1, h2-1);
            g2.setFont(font);
            g2.drawString("CLIPPING", strX, strY);
        } else {
            g2.setColor(Color.lightGray);
            g2.fillRect(0, 0, w, h);
        }
    }


    public static void main(String argv[]) {
        createDemoFrame(new ClipAnim());
    }


    public class AnimVal {
        double ix = 5.0;
        double iy = 3.0;
        double iw = 5.0;
        double ih = 3.0;
        double x, y;
        double ew, eh;   // ellipse width & height
        boolean isSelected;

        public AnimVal(boolean isSelected) {
            this.isSelected = isSelected;
        }


        public void step(int w, int h) {
            x += ix;
            y += iy;
            ew += iw;
            eh += ih;
            if (ew > w/2) {
                ew = w/2;
                iw = Math.random() * -w/16 - 1;
            }
            if (ew < w/8) {
                ew = w/8;
                iw = Math.random() * w/16 + 1;
            }
            if (eh > h/2) {
                eh = h/2;
                ih = Math.random() * -h/16 - 1;
            }
            if (eh < h/8) {
                eh = h/8;
                ih = Math.random() * h/16 + 1;
            }
            if ((x+ew) > w) {
                x = (w - ew)-1;
                ix = Math.random() * -w/32 - 1;
            }
            if (x < 0) {
                x = 2;
                ix = Math.random() * w/32 + 1;
            }
            if ((y+eh) > h) {
                y = (h - eh)-2;
                iy = Math.random() * -h/32 - 1;
            }
            if (y < 0) {
                y = 2;
                iy = Math.random() * h/32 + 1;
            }
        }


        public void reset(int w, int h) {
            x = Math.random()*w;
            y = Math.random()*h;
            ew = (Math.random()*w)/2;
            eh = (Math.random()*h)/2;
        }
    }


    static class DemoControls extends CustomControls implements ActionListener {

        ClipAnim demo;
        JToolBar toolbar;

        public DemoControls(ClipAnim demo) {
            super(demo.name);
            this.demo = demo;
            add(toolbar = new JToolBar());
            toolbar.setFloatable(false);
            addTool("Objects", true);
            addTool("Clip1", true);
            addTool("Clip2", false);
            addTool("Clip3", false);
        }


        public void addTool(String str, boolean state) {
            JToggleButton b = (JToggleButton) toolbar.add(new JToggleButton(str));
            b.setFocusPainted(false);
            b.setSelected(state);
            b.addActionListener(this);
            int width = b.getPreferredSize().width;
            Dimension prefSize = new Dimension(width, 21);
            b.setPreferredSize(prefSize);
            b.setMaximumSize(prefSize);
            b.setMinimumSize(prefSize);
        }


        public void actionPerformed(ActionEvent e) {
            JToggleButton b = (JToggleButton) e.getSource();
            if (b.getText().equals("Objects")) {
                demo.doObjects = b.isSelected();
            } else if (b.getText().equals("Clip1")) {
                demo.animval[0].isSelected = b.isSelected();
            } else if (b.getText().equals("Clip2")) {
                demo.animval[1].isSelected = b.isSelected();
            } else if (b.getText().equals("Clip3")) {
                demo.animval[2].isSelected = b.isSelected();
            }
            if (demo.animating.thread == null) {
                demo.repaint();
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(200,40);
        }


        public void run() {
            try { 
                thread.sleep(5000);
            } catch (InterruptedException e) { return; }
            java2d.Java2Demo.doClickOnDispatchThread((AbstractButton) toolbar.getComponentAtIndex(2));
            try { 
                thread.sleep(5000);
            } catch (InterruptedException e) { return; }
            if (getSize().width > 400) {
                java2d.Java2Demo.doClickOnDispatchThread((AbstractButton) toolbar.getComponentAtIndex(3));
            }
            thread = null;
        }
    } // End DemoControls
} // End ClipAnim

/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Arcs_Curves/BezierAnim.java, dsdev, dsdev 1.9
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
 * @(#)BezierAnim.java	1.18 02/06/13
 */

package java2d.demos.Arcs_Curves;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import java2d.AnimatingControlsSurface;
import java2d.CustomControls;


/**
 * Animated Bezier Curve with controls for different draw & fill paints.
 */
public class BezierAnim extends AnimatingControlsSurface {

    private static final int NUMPTS = 6;
    protected BasicStroke solid = new BasicStroke(10.0f, 
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    protected BasicStroke dashed = new BasicStroke(10.0f, 
       BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[] {5}, 0);
    private float animpts[] = new float[NUMPTS * 2];
    private float deltas[] = new float[NUMPTS * 2];
    protected Paint fillPaint, drawPaint;
    protected boolean doFill = true;
    protected boolean doDraw = true;
    protected GradientPaint gradient;
    protected BasicStroke stroke;


    public BezierAnim() {
        setBackground(Color.white);
        gradient = new GradientPaint(0,0,Color.red,200,200,Color.yellow);
        fillPaint = gradient;
        drawPaint = Color.blue;
        stroke = solid;
        setControls(new Component[] { new DemoControls(this) });
    }


    public void animate(float[] pts, float[] deltas, int index, int limit) {
        float newpt = pts[index] + deltas[index];
        if (newpt <= 0) {
            newpt = -newpt;
            deltas[index] = (float) (Math.random() * 4.0 + 2.0);
        } else if (newpt >= (float) limit) {
            newpt = 2.0f * limit - newpt;
            deltas[index] = - (float) (Math.random() * 4.0 + 2.0);
        }
        pts[index] = newpt;
    }


    public void reset(int w, int h) {
        for (int i = 0; i < animpts.length; i += 2) {
            animpts[i + 0] = (float) (Math.random() * w);
            animpts[i + 1] = (float) (Math.random() * h);
            deltas[i + 0] = (float) (Math.random() * 6.0 + 4.0);
            deltas[i + 1] = (float) (Math.random() * 6.0 + 4.0);
            if (animpts[i + 0] > w / 2.0f) {
                deltas[i + 0] = -deltas[i + 0];
            }
            if (animpts[i + 1] > h / 2.0f) {
                deltas[i + 1] = -deltas[i + 1];
            }
        }
        gradient = new GradientPaint(0,0,Color.red,w*.7f,h*.7f,Color.yellow);
    }


    public void step(int w, int h) {
        for (int i = 0; i < animpts.length; i += 2) {
            animate(animpts, deltas, i + 0, w);
            animate(animpts, deltas, i + 1, h);
        }
    }


    public void render(int w, int h, Graphics2D g2) {
        float[] ctrlpts = animpts;
        int len = ctrlpts.length;
        float prevx = ctrlpts[len - 2];
        float prevy = ctrlpts[len - 1];
        float curx = ctrlpts[0];
        float cury = ctrlpts[1];
        float midx = (curx + prevx) / 2.0f;
        float midy = (cury + prevy) / 2.0f;
        GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gp.moveTo(midx, midy);
        for (int i = 2; i <= ctrlpts.length; i += 2) {
            float x1 = (midx + curx) / 2.0f;
            float y1 = (midy + cury) / 2.0f;
            prevx = curx;
            prevy = cury;
            if (i < ctrlpts.length) {
                curx = ctrlpts[i + 0];
                cury = ctrlpts[i + 1];
            } else {
                curx = ctrlpts[0];
                cury = ctrlpts[1];
            }
            midx = (curx + prevx) / 2.0f;
            midy = (cury + prevy) / 2.0f;
            float x2 = (prevx + midx) / 2.0f;
            float y2 = (prevy + midy) / 2.0f;
            gp.curveTo(x1, y1, x2, y2, midx, midy);
        }
        gp.closePath();
        if (doDraw) {
            g2.setPaint(drawPaint);
            g2.setStroke(stroke);
            g2.draw(gp);
        }
        if (doFill) {
            if (fillPaint instanceof GradientPaint) {
                fillPaint = gradient;
            }
            g2.setPaint(fillPaint);
            g2.fill(gp);
        }
    }


    public static void main(String argv[]) {
        createDemoFrame(new BezierAnim());
    }


    static class DemoControls extends CustomControls implements ActionListener {
        static TexturePaint tp1, tp2;
        static {
            BufferedImage bi = new BufferedImage(2,1,BufferedImage.TYPE_INT_RGB);
            bi.setRGB(0, 0, 0xff00ff00); bi.setRGB(1, 0, 0xffff0000);
            tp1 = new TexturePaint(bi,new Rectangle(0,0,2,1));
            bi = new BufferedImage(2,1,BufferedImage.TYPE_INT_RGB);
            bi.setRGB(0, 0, 0xff0000ff); bi.setRGB(1, 0, 0xffff0000);
            tp2 = new TexturePaint(bi,new Rectangle(0,0,2,1));
        }

        BezierAnim demo;
        static Paint drawPaints[] = 
                {new Color(0,0,0,0), Color.blue, new Color(0, 0, 255, 126), 
                  Color.blue, tp2 };
        static String drawName[] =
                {"No Draw", "Blue", "Blue w/ Alpha", "Blue Dash", "Texture" }; 
        static Paint fillPaints[] = 
                {new Color(0,0,0,0), Color.green, new Color(0, 255, 0, 126), 
                  tp1, new GradientPaint(0,0,Color.red,30,30,Color.yellow) };
        String fillName[] =
                {"No Fill", "Green", "Green w/ Alpha", "Texture", "Gradient"}; 
        
        JMenu fillMenu, drawMenu;
        JMenuItem fillMI[] = new JMenuItem[fillPaints.length];
        JMenuItem drawMI[] = new JMenuItem[drawPaints.length];
        PaintedIcon fillIcons[] = new PaintedIcon[fillPaints.length];
        PaintedIcon drawIcons[] = new PaintedIcon[drawPaints.length];
        Font font = new Font("serif", Font.PLAIN, 10);


        public DemoControls(BezierAnim demo) {
            super(demo.name);
            this.demo = demo;
            setBackground(Color.gray);

            JMenuBar drawMenuBar = new JMenuBar();
            add(drawMenuBar);

            JMenuBar fillMenuBar = new JMenuBar();
            add(fillMenuBar);

            drawMenu = (JMenu) drawMenuBar.add(new JMenu("Draw Choice"));
            drawMenu.setFont(font);
            for (int i = 0; i < drawPaints.length; i++) {
                drawIcons[i]= new PaintedIcon(drawPaints[i]);
                drawMI[i] = drawMenu.add(new JMenuItem(drawName[i]));
                drawMI[i].setFont(font);
                drawMI[i].setIcon(drawIcons[i]);
                drawMI[i].addActionListener(this);
            } 
            drawMenu.setIcon(drawIcons[1]);

            fillMenu = (JMenu) fillMenuBar.add(new JMenu("Fill Choice"));
            fillMenu.setFont(font);
            for (int i = 0; i < fillPaints.length; i++) {
                fillIcons[i]= new PaintedIcon(fillPaints[i]);
                fillMI[i] = fillMenu.add(new JMenuItem(fillName[i]));
                fillMI[i].setFont(font);
                fillMI[i].setIcon(fillIcons[i]);
                fillMI[i].addActionListener(this);
            } 
            fillMenu.setIcon(fillIcons[fillPaints.length-1]);
        }


        public void actionPerformed(ActionEvent e) {
            Object obj = e.getSource();
            for (int i = 0; i < fillPaints.length; i++) {
                if (obj.equals(fillMI[i])) {
                    demo.doFill = true;
                    demo.fillPaint = fillPaints[i];
                    fillMenu.setIcon(fillIcons[i]);
                    break;
                } 
            }
            for (int i = 0; i < drawPaints.length; i++) {
                if (obj.equals(drawMI[i])) {
                    demo.doDraw = true;
                    demo.drawPaint = drawPaints[i];
                    if (((JMenuItem) obj).getText().endsWith("Dash")) {
                        demo.stroke = demo.dashed;
                    } else {
                        demo.stroke = demo.solid;
                    }
                    drawMenu.setIcon(drawIcons[i]);
                    break;
                } 
            }
            if (obj.equals(fillMI[0])) {
                demo.doFill = false;
            } else if (obj.equals(drawMI[0])) {
                demo.doDraw = false;
            }
            if (demo.animating.thread == null) {
                demo.repaint();
            }
        }


        public Dimension getPreferredSize() {
            return new Dimension(200,36);
        }


        public void run() {
            Thread me = Thread.currentThread();
            while (thread == me) {
                for (int i = 1; i < drawMI.length; i++) {
                    java2d.Java2Demo.doClickOnDispatchThread(drawMI[i]);
                    for (int j = 0; j < fillMI.length; j++) {
                        java2d.Java2Demo.doClickOnDispatchThread(fillMI[j]);
                        try {
                            thread.sleep(3000 + (long) (Math.random() * 3000));
                        } catch (InterruptedException e) { break; }
                    }
                }
            }
            thread = null;
        }


        static class PaintedIcon implements Icon {
            Paint paint;
            public PaintedIcon(Paint p) {
                this.paint = p;
            }
    
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(paint);
                g2.fillRect(x,y,getIconWidth(), getIconHeight());
                g2.setColor(Color.gray);
                g2.draw3DRect(x, y, getIconWidth()-1, getIconHeight()-1, true);
            }
            public int getIconWidth() { return 12; }
            public int getIconHeight() { return 12; }
        } // End PaintedIcon class
    } // End DemoControls class
} // End BezierAnim class

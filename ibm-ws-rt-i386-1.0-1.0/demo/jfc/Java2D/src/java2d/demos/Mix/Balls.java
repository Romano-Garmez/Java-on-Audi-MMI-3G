/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Mix/Balls.java, dsdev, dsdev 1.11
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
 * Change activity:
 *
 * Reason Date   Origin   Description
 * ------ ----   ------   ----------------------------------------------------
 * 98991  090106 niced    Java2D ClassCastException
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */





/*
 * @(#)Balls.java	1.22 02/06/13
 */

package java2d.demos.Mix;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java2d.AnimatingControlsSurface;
import java2d.CustomControls;



/**
 * Animated color bouncing balls with custom controls.
 */
public class Balls extends AnimatingControlsSurface {

    private static Color colors[] = 
            { Color.red, Color.orange, Color.yellow, Color.green.darker(),
              Color.blue, new Color(75, 00, 82), new Color(238,130,238) };
    private long now, deltaT, lasttime;
    private boolean active;
    protected Ball balls[] = new Ball[colors.length];
    protected boolean clearToggle;
    protected JComboBox combo;


    public Balls() {
        setBackground(Color.white);
        for (int i = 0; i < colors.length; i++) {
            balls[i] = new Ball(colors[i], 30);
        }
        balls[0].isSelected = true;
        balls[3].isSelected = true;
        balls[4].isSelected = true;
        balls[6].isSelected = true;
        setControls(new Component[] { new DemoControls(this) });
    }


    public void reset(int w, int h) {
        if (w > 400 && h > 100) {
            combo.setSelectedIndex(5);
        }
    }


    public void step(int w, int h) {
        if (lasttime == 0) {
            lasttime = System.currentTimeMillis();
        }
        now = System.currentTimeMillis();
        deltaT = now - lasttime;
        active = false;
        for (int i = 0; i < balls.length; i++) {
            if (balls[i] == null) {
                return;
            }
            balls[i].step(deltaT, w, h);
            if (balls[i].Vy > .02 || -balls[i].Vy > .02 ||
                    balls[i].y + balls[i].bsize < h) {
                active = true;
            }
        }
        if (!active) {
            for (int i = 0; i < balls.length; i++) {
                balls[i].Vx = (float)Math.random() / 4.0f - 0.125f;
                balls[i].Vy = -(float)Math.random() / 4.0f - 0.2f;
            }
            clearToggle = true;
        }
    }


    public void render(int w, int h, Graphics2D g2) {
        for (int i = 0; i < balls.length; i++) {
            Ball b = balls[i];
            if (b == null || b.imgs[b.index] == null || !b.isSelected) {
                continue;
            }
            g2.drawImage(b.imgs[b.index], (int) b.x, (int) b.y, this);
        }
        lasttime = now;
    }


    public static void main(String argv[]) {
        createDemoFrame(new Balls());
    }


    static class Ball {
    
        public int bsize;
        /*ibm@42692.1 - begin*/
        public double x, y;
        public double Vx = 0.1;
        public double Vy = 0.05;
        /*ibm@42692.1 - end*/
        public int nImgs = 5;
        public BufferedImage imgs[];
        public int index = (int) (Math.random() * (nImgs-1));
    
        /*ibm@42692.1 - begin*/
        private final double inelasticity = .96;
        private final double Ax = 0.0;
        private final double Ay = 0.0002;
        private final double Ar = 0.9;
        /*ibm@42692.1 - end*/
        private final int UP = 0;
        private final int DOWN = 1;
        private int indexDirection = UP;
        private boolean collision_x, collision_y;
        private double jitter;                       /*ibm@42692*/
        private Color color;
        private boolean isSelected;
    
    
        public Ball(Color color, int bsize) {
            this.color = color;
            makeImages(bsize);
        }
    
    
        public void makeImages(int bsize) {
            this.bsize = bsize*2;
            int R = bsize;
            byte[] data = new byte[R * 2 * R * 2];
            int maxr = 0;
            for (int Y = 2 * R; --Y >= 0;) {
                int x0 = (int) (Math.sqrt(R * R - (Y - R) * (Y - R)) + 0.5);
                int p = Y * (R * 2) + R - x0;
                for (int X = -x0; X < x0; X++) {
                    int x = X + 15;
                    int y = Y - R + 15;
                    int r = (int) (Math.sqrt(x * x + y * y) + 0.5);
                    if (r > maxr) {
                        maxr = r;
                    }
                    data[p++] = r <= 0 ? 1 : (byte) r;
                }
            }
    
            imgs = new BufferedImage[nImgs];
    
            int bg = 255;
            byte red[] = new byte[256];
            red[0] = (byte) bg;
            byte green[] = new byte[256];
            green[0] = (byte) bg;
            byte blue[] = new byte[256];
            blue[0] = (byte) bg;
    
            for (int r = 0; r < imgs.length; r++) {
                float b = 0.5f + (float) ((r+1f)/imgs.length/2f);
                for (int i = maxr; i >= 1; --i) {
                    float d = (float) i / maxr;
                    red[i] = (byte) blend(blend(color.getRed(), 255, d), bg, b);
                    green[i] = (byte) blend(blend(color.getGreen(), 255, d), bg, b);
                    blue[i] = (byte) blend(blend(color.getBlue(), 255, d), bg, b);
                }
                IndexColorModel icm = new IndexColorModel(8, maxr + 1,
                            red, green, blue, 0);
                DataBufferByte dbb = new DataBufferByte(data, data.length);
                int bandOffsets[] = {0};
                WritableRaster wr = Raster.createInterleavedRaster(dbb,
                    R*2,R*2,R*2,1, bandOffsets,null);
                imgs[r] = new BufferedImage(icm, wr,icm.isAlphaPremultiplied(),null);
            }
        }
    
    
        private final int blend(int fg, int bg, float fgfactor) {
            return (int) (bg + (fg - bg) * fgfactor);
        }
    
    
        public void step(long deltaT, int w, int h) {
            collision_x = false;
            collision_y = false;
    
            jitter = (float) Math.random() * .01f - .005f;
    
            x += Vx * deltaT + (Ax / 2.0) * deltaT * deltaT;
            y += Vy * deltaT + (Ay / 2.0) * deltaT * deltaT;
            if (x <= 0.0f) {
                x = 0.0f;
                Vx = -Vx * inelasticity + jitter;
                collision_x = true;
            }
            if (x + bsize >= w) {
                x = w - bsize;
                Vx = -Vx * inelasticity + jitter;
                collision_x = true;
            }
            if (y <= 0) {
                y = 0;
                Vy = -Vy * inelasticity + jitter;
                collision_y = true;
            }
            if (y + bsize >= h) {
                y = h - bsize;
                Vx *= inelasticity;
                Vy = -Vy * inelasticity + jitter;
                collision_y = true;
            }
            Vy = Vy + Ay * deltaT;
            Vx = Vx + Ax * deltaT;
    
            if (indexDirection == UP) {
                index++; 
            }
            if (indexDirection == DOWN) {
                --index; 
            }
            if (index+1 == nImgs) {
                indexDirection = DOWN;
            }
            if (index == 0) {
                indexDirection = UP;
            }
        }
    }  // End class Ball



    class DemoControls extends CustomControls implements ActionListener {

        Balls demo;
        JToolBar toolbar;

        public DemoControls(Balls demo) {
            super(demo.name);
            this.demo = demo;
            add(toolbar = new JToolBar());
            toolbar.setFloatable(false);
            addTool("Clear", true);
            addTool("R", demo.balls[0].isSelected);
            addTool("O", demo.balls[1].isSelected);
            addTool("Y", demo.balls[2].isSelected);
            addTool("G", demo.balls[3].isSelected);
            addTool("B", demo.balls[4].isSelected);
            addTool("I", demo.balls[5].isSelected);
            addTool("V", demo.balls[6].isSelected);
            add(combo = new JComboBox());
            combo.addItem("10");
            combo.addItem("20");
            combo.addItem("30");
            combo.addItem("40");
            combo.addItem("50");
            combo.addItem("60");
            combo.addItem("70");
            combo.addItem("80");
            combo.setSelectedIndex(2);
            combo.addActionListener(this);
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
            if (e.getSource() instanceof JComboBox) {
                int size = Integer.parseInt((String) combo.getSelectedItem());
                for (int i = 0; i < demo.balls.length; i++) {
                    demo.balls[i].makeImages(size);
                }
                return;
            }
            JToggleButton b = (JToggleButton) e.getSource();
            if (b.getText().equals("Clear")) {
                demo.clearSurface = b.isSelected();
            } 
            else {
                int index = toolbar.getComponentIndex(b)-1;
                demo.balls[index].isSelected = b.isSelected();
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(200,40);
        }


        public void run() {
            try { thread.sleep(999); } catch (Exception e) { return; }
            Thread me = Thread.currentThread();
            java2d.Java2Demo.doClickOnDispatchThread((AbstractButton) toolbar.getComponentAtIndex(2));
            while (thread == me) {
                try {
                    thread.sleep(222);
                } catch (InterruptedException e) { return; }
                if (demo.clearToggle) {
                    if (demo.clearSurface) {
                        combo.setSelectedIndex((int) (Math.random()*5));
                    }
                    java2d.Java2Demo.doClickOnDispatchThread((AbstractButton) toolbar.getComponentAtIndex(0));
                    demo.clearToggle = false;
                }
            }
            thread = null;
        }
    } // End DemoControls
} // End Balls

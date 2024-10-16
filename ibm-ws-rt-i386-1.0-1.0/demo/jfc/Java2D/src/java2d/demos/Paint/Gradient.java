/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Paint/Gradient.java, dsdev, dsdev 1.9
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
 * @(#)Gradient.java	1.11 02/06/13
 */

package java2d.demos.Paint;


import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import java2d.ControlsSurface;
import java2d.CustomControls;



public class Gradient extends ControlsSurface {

    protected Color innerC, outerC;
    private DemoControls controls;


    public Gradient() {
        setBackground(Color.white);
        innerC = Color.green;
        outerC = Color.blue;
        setControls(new Component[] { new DemoControls(this) });
    }


    public void render(int w, int h, Graphics2D g2) {

        int w2 = w/2;
        int h2 = h/2;
        g2.setPaint(new GradientPaint(0,0,outerC,w*.35f,h*.35f,innerC));
        g2.fillRect(0, 0, w2, h2);
        g2.setPaint(new GradientPaint(w,0,outerC,w*.65f,h*.35f,innerC));
        g2.fillRect(w2, 0, w2, h2);
        g2.setPaint(new GradientPaint(0,h,outerC,w*.35f,h*.65f,innerC));
        g2.fillRect(0, h2, w2, h2);
        g2.setPaint(new GradientPaint(w,h,outerC,w*.65f,h*.65f,innerC));
        g2.fillRect(w2, h2, w2, h2);

        g2.setColor(Color.black);
        TextLayout tl = new TextLayout(
                "GradientPaint", g2.getFont(), g2.getFontRenderContext());
        tl.draw(g2, (int) (w/2-tl.getBounds().getWidth()/2),
                (int) (h/2+tl.getBounds().getHeight()/2));
    }


    public static void main(String s[]) {
        createDemoFrame(new Gradient());
    }


    static class DemoControls extends CustomControls implements ActionListener {

        Gradient demo;
        Color colors[] = 
                { Color.red, Color.orange, Color.yellow, Color.green,
                  Color.blue, Color.lightGray, Color.cyan, Color.magenta };
        String colorName[] =
                { "Red", "Orange", "Yellow", "Green", 
                  "Blue", "lightGray", "Cyan", "Magenta" };
        
        JMenuItem innerMI[] = new JMenuItem[colors.length];
        JMenuItem outerMI[] = new JMenuItem[colors.length];
        ColoredSquare squares[] = new ColoredSquare[colors.length];
        JMenu imenu, omenu;

        public DemoControls(Gradient demo) {
            super(demo.name);
            this.demo = demo;
            setBackground(Color.gray);
            JMenuBar inMenuBar = new JMenuBar();
            add(inMenuBar);
            JMenuBar outMenuBar = new JMenuBar();
            add(outMenuBar);
            Font font = new Font("serif", Font.PLAIN, 10);

            imenu = (JMenu) inMenuBar.add(new JMenu("Inner Color"));
            imenu.setFont(font);
            imenu.setIcon(new ColoredSquare(demo.innerC));
            omenu = (JMenu) outMenuBar.add(new JMenu("Outer Color"));
            omenu.setFont(font);
            omenu.setIcon(new ColoredSquare(demo.outerC));
            for (int i = 0; i < colors.length; i++) {
                squares[i] = new ColoredSquare(colors[i]);
                innerMI[i] = imenu.add(new JMenuItem(colorName[i]));
                innerMI[i].setFont(font);
                innerMI[i].setIcon(squares[i]);
                innerMI[i].addActionListener(this);
                outerMI[i] = omenu.add(new JMenuItem(colorName[i]));
                outerMI[i].setFont(font);
                outerMI[i].setIcon(squares[i]);
                outerMI[i].addActionListener(this);
            } 
        }


        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < colors.length; i++) {
                if (e.getSource().equals(innerMI[i])) {
                    demo.innerC = colors[i];
                    imenu.setIcon(squares[i]);
                    break;
                } else if (e.getSource().equals(outerMI[i])) {
                    demo.outerC = colors[i];
                    omenu.setIcon(squares[i]);
                    break;
                }
            }
            demo.repaint();
        }


        public Dimension getPreferredSize() {
            return new Dimension(200,37);
        }


        public void run() {
            // goto double buffering
            if (demo.getImageType() <= 1) {
                demo.setImageType(2);
            }
            Thread me = Thread.currentThread();
            while (thread == me) {
                for (int i = 0; i < innerMI.length; i++) {
                    if (i != 4) {
                        try {
                            thread.sleep(4444);
                        } catch (InterruptedException e) { return; }
                        java2d.Java2Demo.doClickOnDispatchThread(innerMI[i]);
                    }
                }
            }
            thread = null;
        }


        class ColoredSquare implements Icon {
            Color color;
            public ColoredSquare(Color c) {
                this.color = c;
            }
    
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Color oldColor = g.getColor();
                g.setColor(color);
                g.fill3DRect(x,y,getIconWidth(), getIconHeight(), true);
                g.setColor(oldColor);
            }
            public int getIconWidth() { return 12; }
            public int getIconHeight() { return 12; }
        } // End ColoredSquare class
    } // End DemoControls
} // End Gradient class

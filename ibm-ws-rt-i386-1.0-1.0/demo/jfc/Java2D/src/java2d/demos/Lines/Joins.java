/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Lines/Joins.java, dsdev, dsdev 1.9
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
 * @(#)Joins.java	1.23 02/06/13
 */

package java2d.demos.Lines;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java2d.ControlsSurface;
import java2d.CustomControls;


/**
 * BasicStroke join types and width sizes illustrated.  Control for
 * rendering a shape returned from BasicStroke.createStrokedShape(Shape).
 */
public class Joins extends ControlsSurface implements ChangeListener {

    protected int joinType = BasicStroke.JOIN_MITER;
    protected float bswidth = 20.0f;
    protected JSlider slider;
    protected JLabel label;


    public Joins() {
        setBackground(Color.white);
        slider = new JSlider(JSlider.VERTICAL, 0, 100, (int)(bswidth*2));
        slider.setPreferredSize(new Dimension(15, 100));
        slider.addChangeListener(this);
        setControls(new Component[] { new DemoControls(this), slider });
        setConstraints(new String[] { BorderLayout.NORTH, BorderLayout.WEST});
    }


    public void stateChanged(ChangeEvent e) {
        // when using these sliders use double buffering, which means
        // ignoring when DemoSurface.imageType = 'On Screen'
        if (getImageType() <= 1) {
            setImageType(2);
        }
        bswidth = (float) slider.getValue() / 2.0f;
        label.setText(" Width = " + String.valueOf(bswidth));
        label.repaint();
        repaint();
    }    


    public void render(int w, int h, Graphics2D g2) {
        BasicStroke bs = new BasicStroke(bswidth, 
                                    BasicStroke.CAP_BUTT, joinType);
        GeneralPath p = new GeneralPath();
        p.moveTo(- w / 4.0f, - h / 12.0f);
        p.lineTo(+ w / 4.0f, - h / 12.0f);
        p.lineTo(- w / 6.0f, + h / 4.0f);
        p.lineTo(+     0.0f, - h / 4.0f);
        p.lineTo(+ w / 6.0f, + h / 4.0f);
        p.closePath();
        p.closePath();
        g2.translate(w/2, h/2);
        g2.setColor(Color.black);
        g2.draw(bs.createStrokedShape(p));
    }


    public static void main(String s[]) {
        createDemoFrame(new Joins());
    }


    class DemoControls extends CustomControls implements ActionListener {

        Joins demo;
        int joinType[] = { BasicStroke.JOIN_MITER, 
                       BasicStroke.JOIN_ROUND, BasicStroke.JOIN_BEVEL };
        String joinName[] = { "Mitered Join", "Rounded Join", "Beveled Join" };
        JMenu menu;
        JMenuItem menuitem[] = new JMenuItem[joinType.length];
        JoinIcon icons[] = new JoinIcon[joinType.length];
        JToolBar toolbar;


        public DemoControls(Joins demo) {
            super(demo.name);
            setBorder(new CompoundBorder(getBorder(), new EmptyBorder(2, 2, 2, 2)));
            this.demo = demo;
            setLayout(new BorderLayout());
            label = new JLabel(" Width = " + String.valueOf(demo.bswidth));
            Font font = new Font("serif", Font.BOLD, 14);
            label.setFont(font);
            add("West", label);
            JMenuBar menubar = new JMenuBar();
            add("East", menubar);
            menu = (JMenu) menubar.add(new JMenu(joinName[0]));
            menu.setFont(font = new Font("serif", Font.PLAIN, 10));
            for (int i = 0; i < joinType.length; i++) {
                icons[i]= new JoinIcon(joinType[i]);
                menuitem[i] = menu.add(new JMenuItem(joinName[i]));
                menuitem[i].setFont(font);
                menuitem[i].setIcon(icons[i]);
                menuitem[i].addActionListener(this);
            } 
            menu.setIcon(icons[0]);
        }


        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < joinType.length; i++) {
                if (e.getSource().equals(menuitem[i])) {
                    demo.joinType = joinType[i];
                    menu.setIcon(icons[i]);
                    menu.setText(joinName[i]);
                    break;
                } 
            }
            demo.repaint();
        }


        public Dimension getPreferredSize() {
            return new Dimension(200,37);
        }


        public void run() {
            try { thread.sleep(999); } catch (Exception e) { return; }
            Thread me = Thread.currentThread();
            while (thread == me) {
                for (int i = 0; i < menuitem.length; i++) {
                    java2d.Java2Demo.doClickOnDispatchThread(menuitem[i]);
                    for (int k = 10; k < 60; k+=2) {
                        demo.slider.setValue(k);
                        try {
                            thread.sleep(100);
                        } catch (InterruptedException e) { return; }
                    }
                    try {
                        thread.sleep(999);
                    } catch (InterruptedException e) { return; }
                }
            }
            thread = null;
        }


        class JoinIcon implements Icon {
            int joinType;
            public JoinIcon(int joinType) {
                this.joinType = joinType;
            }
    
            public void paintIcon(Component c, Graphics g, int x, int y) {
                ((Graphics2D) g).setRenderingHint(
                     RenderingHints.KEY_ANTIALIASING, 
                     RenderingHints.VALUE_ANTIALIAS_ON);
                BasicStroke bs = new BasicStroke(8.0f, 
                                    BasicStroke.CAP_BUTT, joinType);
                ((Graphics2D) g).setStroke(bs);
                GeneralPath p = new GeneralPath();
                p.moveTo(0, 3);
                p.lineTo(getIconWidth()-2, getIconHeight()/2);
                p.lineTo(0,getIconHeight());
                ((Graphics2D) g).draw(p);
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 20; }
        } // End JoinIcon class
    } // End DemoControls class
} // End Joins class

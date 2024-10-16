/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Fonts/AllFonts.java, dsdev, dsdev 1.6
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
 * @(#)AllFonts.java	1.30 02/06/13
 */

package java2d.demos.Fonts;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java2d.AnimatingControlsSurface;
import java2d.CustomControls;



/**
 * Scrolling text of fonts returned from GraphicsEnvironment.getAllFonts().
 */
public class AllFonts extends AnimatingControlsSurface {

    private static Vector fonts = new Vector();
    static {
        GraphicsEnvironment ge = 
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font allfonts[] = ge.getAllFonts();
        for (int i = 0; i < allfonts.length; i++) {
            if (allfonts[i].canDisplayUpTo(allfonts[i].getName()) != 0) {
                fonts.addElement(allfonts[i]);
            }
        }
    }
    private int nStrs;
    private int strH;
    private int fi;
    protected int fsize = 14;
    protected Vector v = new Vector();


    public AllFonts() {
        setBackground(Color.white);
        setSleepAmount(500);
        setControls(new Component[] { new DemoControls(this) });
    }


    public void handleThread(int state) { }


    public void reset(int w, int h) {
        v.clear();
        Font f = ((Font) fonts.get(0)).deriveFont(Font.PLAIN,fsize);
        FontMetrics fm = getFontMetrics(f);
        strH = (int) (fm.getAscent()+fm.getDescent());
        nStrs = h/strH + 1;
        fi = 0;
    }


    public void step(int w, int h) {
        if (fi < fonts.size()) {
            v.addElement(((Font) fonts.get(fi)).deriveFont(Font.PLAIN,fsize));
        }
        if (v.size() == nStrs && v.size() != 0 || fi > fonts.size()) {
            v.removeElementAt(0);
        }
        fi = (v.size() == 0) ? 0 : ++fi;
    }


    public void render(int w, int h, Graphics2D g2) {

        g2.setColor(Color.black);

        int yy = (fi >= fonts.size()) ? 0 : h - v.size() * strH - strH/2;

        for (int i = 0; i < v.size(); i++) {
            Font f = (Font) v.get(i);
            int sw = getFontMetrics(f).stringWidth(f.getName());
            g2.setFont(f);
            g2.drawString(f.getName(), (int) (w/2-sw/2),yy += strH);
        }
    }


    public static void main(String argv[]) {
        createDemoFrame(new AllFonts());
    }


    static class DemoControls extends CustomControls implements ActionListener, ChangeListener {

        AllFonts demo;
        JSlider slider;
        int fsize[] = { 8, 14, 18, 24 };
        JMenuItem menuitem[] = new JMenuItem[fsize.length];
        Font font[] = new Font[fsize.length];


        public DemoControls(AllFonts demo) {
            this.demo = demo;
            setBackground(Color.gray);

            int sleepAmount = (int) demo.getSleepAmount();
            slider = new JSlider(JSlider.HORIZONTAL, 0, 999, sleepAmount);
            slider.setBorder(new EtchedBorder());
            slider.setPreferredSize(new Dimension(90,22));
            slider.addChangeListener(this);
            add(slider);
            JMenuBar menubar = new JMenuBar();
            add(menubar);
            JMenu menu = (JMenu) menubar.add(new JMenu("Font Size"));
            for (int i = 0; i < fsize.length; i++) {
                font[i] = new Font("serif", Font.PLAIN, fsize[i]);
                menuitem[i] = menu.add(new JMenuItem(String.valueOf(fsize[i])));
                menuitem[i].setFont(font[i]);
                menuitem[i].addActionListener(this);
            }
        }


        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < fsize.length; i++) {
                if (e.getSource().equals(menuitem[i])) {
                    demo.fsize = fsize[i];
                    Dimension d = demo.getSize();
                    demo.reset(d.width, d.height);
                    break;
                }
            }
        }


        public void stateChanged(ChangeEvent e) {
            demo.setSleepAmount(slider.getValue());
        }
    }
}

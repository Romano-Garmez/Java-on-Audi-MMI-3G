/*
 * @(#)src/demo/jfc/Java2D/src/java2d/TextureChooser.java, dsdev, dsdev 1.8
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
 * @(#)TextureChooser.java	1.29 03/07/11
 */

package java2d;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/**
 * Four types of Paint displayed: Geometry, Text & Image Textures and
 * a Gradient Paint.  Paints can be selected with the Mouse.
 */
public class TextureChooser extends JPanel {

    static public Object texture = getGeomTexture();
    public int num;

    public TextureChooser(int num) {
        this.num = num;
        setLayout(new GridLayout(0,2,5,5));
        setBorder(new TitledBorder(new EtchedBorder(), "Texture Chooser"));

        add(new Surface(getGeomTexture(), this, 0));
        add(new Surface(getImageTexture(), this, 1));
        add(new Surface(getTextTexture(), this, 2));
        add(new Surface(getGradientPaint(), this, 3));
    }


    static public TexturePaint getGeomTexture() {
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics2D tG2 = bi.createGraphics();
        tG2.setBackground(Color.white);
        tG2.clearRect(0,0,5,5);
        tG2.setColor(new Color(211,211,211,200));
        tG2.fill(new Ellipse2D.Float(0,0,5,5));
        Rectangle r = new Rectangle(0,0,5,5);
        return new TexturePaint(bi,r);
    }

    public TexturePaint getImageTexture() {
        Image img = DemoImages.getImage("java-logo.gif", this);
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);
        BufferedImage bi = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
        Graphics2D tG2 = bi.createGraphics();
        tG2.drawImage(img, 0, 0, this);
        Rectangle r = new Rectangle(0,0,iw,ih);
        return new TexturePaint(bi,r);
    }


    public TexturePaint getTextTexture() {
        Font f = new Font("Times New Roman", Font.BOLD, 10);
        TextLayout tl = new TextLayout("Java2D", f, new FontRenderContext(null, false, false));
        int sw = (int) tl.getBounds().getWidth();
        int sh = (int) (tl.getAscent()+tl.getDescent());
        BufferedImage bi = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_RGB);
        Graphics2D tG2 = bi.createGraphics();
        tG2.setBackground(Color.white);
        tG2.clearRect(0,0,sw,sh);
        tG2.setColor(Color.lightGray);
        tl.draw(tG2, 0, (float) tl.getAscent());
        Rectangle r = new Rectangle(0,0,sw,sh);
        return new TexturePaint(bi,r);
    }


    public GradientPaint getGradientPaint() {
        return new GradientPaint(0,0,Color.white,80,0,Color.green);
    }

    public class Surface extends JPanel implements MouseListener {

        public boolean clickedFrame;
        private int num;
        private TextureChooser tc;
        private boolean enterExitFrame = false;
        private Object t;

        public Surface(Object t, TextureChooser tc, int num) {
            setBackground(Color.white);
            this.t = t;
            this.tc = tc;
            this.clickedFrame = (num == tc.num);
            this.num = num;
            if (num == tc.num)
                tc.texture = t;
            addMouseListener(this);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getSize().width;
            int h = getSize().height;
            if (t instanceof TexturePaint)
                g2.setPaint((TexturePaint) t);
            else {
                g2.setPaint((GradientPaint) t);
            }
            g2.fill(new Rectangle(0,0,w,h));
            if (clickedFrame || enterExitFrame) {
                g2.setColor(Color.gray);
                BasicStroke bs = new BasicStroke(3, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER);
                g2.setStroke(bs);
                g2.drawRect(0,0,w-1,h-1);
                tc.num = num;
            }
        }

        public void mouseClicked(MouseEvent e) {
            tc.texture = t;
            clickedFrame = true;

            Component cmps[] = tc.getComponents();
            for (int i = 0; i < cmps.length; i++) {
                if (cmps[i] instanceof Surface) {
                    Surface surf = (Surface) cmps[i];
                    if (!surf.equals(this) && surf.clickedFrame) {
                        surf.clickedFrame = false;
                        surf.repaint();
                    }
                }
            }
            
            // ABP
            if (Java2Demo.controls.textureCB.isSelected()) {
                Java2Demo.doClickOnDispatchThread(Java2Demo.controls.textureCB);
                Java2Demo.doClickOnDispatchThread(Java2Demo.controls.textureCB);
	    }            
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            enterExitFrame = true;
            repaint();
        }

        public void mouseExited(MouseEvent e) {
            enterExitFrame = false;
            repaint();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            return new Dimension(30,30);
        }

    }

    public static void main(String s[]) {
        Frame f = new Frame("Java2D Demo - TextureChooser");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        f.add("Center", new TextureChooser(0));
        f.pack();
        f.setSize(new Dimension(400,400));
        f.setVisible(true);
    }
}

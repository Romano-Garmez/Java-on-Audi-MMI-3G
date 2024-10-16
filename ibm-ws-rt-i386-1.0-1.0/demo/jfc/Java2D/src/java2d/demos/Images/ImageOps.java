/*
 * @(#)src/demo/jfc/Java2D/src/java2d/demos/Images/ImageOps.java, dsdev, dsdev 1.8
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
 * @(#)ImageOps.java	1.28 02/06/13
 */

package java2d.demos.Images;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.event.*;
import java2d.ControlsSurface;
import java2d.CustomControls;



/**
 * Images drawn using operators such as ConvolveOp LowPass & Sharpen,
 * LookupOp and RescaleOp.
 */
public class ImageOps extends ControlsSurface implements ChangeListener {

    protected JSlider slider1, slider2;
    private static String imgName[] = { "bld.jpg", "boat.png" };
    private static BufferedImage img[] = new BufferedImage[imgName.length];
    private static String opsName[] = { 
              "Threshold", "RescaleOp" ,"Invert", "Yellow Invert", "3x3 Blur", 
              "3x3 Sharpen", "3x3 Edge", "5x5 Edge"};
    private static BufferedImageOp biop[] = new BufferedImageOp[opsName.length];
    private static int rescaleFactor = 128;
    private static float rescaleOffset = 0;
    private static int low = 100, high = 200;
    private int opsIndex, imgIndex;
 
    static {
        thresholdOp(low, high);
        int i = 1;
        biop[i++] = new RescaleOp(1.0f, 0, null);
        byte invert[] = new byte[256];
        byte ordered[] = new byte[256];
        for (int j = 0; j < 256 ; j++) {
            invert[j] = (byte) (256-j);
            ordered[j] = (byte) j;
        }
        biop[i++] = new LookupOp(new ByteLookupTable(0,invert), null);
        byte[][] yellowInvert = new byte[][] { invert, invert, ordered };
        biop[i++] = new LookupOp(new ByteLookupTable(0,yellowInvert), null);
        int dim[][] = {{3,3}, {3,3}, {3,3}, {5,5}};
        float data[][] = { {0.1f, 0.1f, 0.1f,              // 3x3 blur
                            0.1f, 0.2f, 0.1f,
                            0.1f, 0.1f, 0.1f},
                           {-1.0f, -1.0f, -1.0f,           // 3x3 sharpen
                            -1.0f, 9.0f, -1.0f,
                            -1.0f, -1.0f, -1.0f},
                           { 0.f, -1.f,  0.f,                  // 3x3 edge
                            -1.f,  5.f, -1.f,
                             0.f, -1.f,  0.f},
                           {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  // 5x5 edge
                            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                            -1.0f, -1.0f, 24.0f, -1.0f, -1.0f,
                            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
                            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}};
        for (int j = 0; j < data.length; j++, i++) {
            biop[i] = new ConvolveOp(new Kernel(dim[j][0],dim[j][1],data[j]));
        }
    }


    public ImageOps() {
        setBackground(Color.white);
        for (int i = 0; i < imgName.length; i++) {
            Image image = getImage(imgName[i]);
            int iw = image.getWidth(this);
            int ih = image.getHeight(this);
            img[i] = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
            img[i].createGraphics().drawImage(image,0,0,null);
        }
        slider1 = new JSlider(JSlider.VERTICAL, 0, 255, low);
        slider1.setPreferredSize(new Dimension(15, 100));
        slider1.addChangeListener(this);
        slider2 = new JSlider(JSlider.VERTICAL, 0, 255, high);
        slider2.setPreferredSize(new Dimension(15, 100));
        slider2.addChangeListener(this);
        setControls(new Component[]{new DemoControls(this),slider1,slider2});
        setConstraints(new String[] { 
            BorderLayout.NORTH, BorderLayout.WEST, BorderLayout.EAST });
    }


    public static void thresholdOp(int low, int high) {
        byte threshold[] = new byte[256];
        for (int j = 0; j < 256 ; j++) {
            if (j > high) {
                threshold[j] = (byte) 255;
            } else if (j < low) {
                threshold[j] = (byte) 0;
            } else {
                threshold[j] = (byte) j;
            }
        }
        biop[0] = new LookupOp(new ByteLookupTable(0,threshold), null);
    }


    public void render(int w, int h, Graphics2D g2) {
        int iw = img[imgIndex].getWidth(null);
        int ih = img[imgIndex].getHeight(null);
        BufferedImage bi = new BufferedImage(iw,ih,BufferedImage.TYPE_INT_RGB);
        biop[opsIndex].filter(img[imgIndex], bi);
        g2.drawImage(bi,0,0,w,h,null);
    }


    public void stateChanged(ChangeEvent e) {
        // when using these sliders use double buffering, which means
        // ignoring when DemoSurface.imageType = 'On Screen'
        if (getImageType() <= 1) {
            setImageType(2);
        }
        if (e.getSource().equals(slider1)) {
            if (opsIndex == 0) {
                thresholdOp(slider1.getValue(), high);
            } else {
                rescaleFactor = slider1.getValue();
                biop[1] = new RescaleOp((float)rescaleFactor/128.0f, rescaleOffset, null);
            }
        } else {
            if (opsIndex == 0) {
                thresholdOp(low, slider2.getValue());
            } else {
                rescaleOffset = (float) slider2.getValue();
                biop[1] = new RescaleOp((float)rescaleFactor/128.0f, rescaleOffset, null);
            }

        }
        repaint();
    }


    public static void main(String s[]) {
        createDemoFrame(new ImageOps());
    }


    static class DemoControls extends CustomControls implements ActionListener {

        ImageOps demo;
        JComboBox imgCombo, opsCombo;
        Font font = new Font("serif", Font.PLAIN, 10);

        public DemoControls(ImageOps demo) {
            super(demo.name);
            this.demo = demo;
            add(imgCombo = new JComboBox());
            imgCombo.setFont(font);
            for (int i = 0; i < ImageOps.imgName.length; i++) {
                imgCombo.addItem(ImageOps.imgName[i]);
            }
            imgCombo.addActionListener(this);
            add(opsCombo = new JComboBox());
            opsCombo.setFont(font);
            for (int i = 0; i < ImageOps.opsName.length; i++) {
                opsCombo.addItem(ImageOps.opsName[i]);
            }
            opsCombo.addActionListener(this);
        }


        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(opsCombo)) {
                demo.opsIndex = opsCombo.getSelectedIndex();
                if (demo.opsIndex == 0) {
                    demo.slider1.setValue(demo.low);
                    demo.slider2.setValue(demo.high);
                    demo.slider1.setEnabled(true);
                    demo.slider2.setEnabled(true);
                } else if (demo.opsIndex == 1) {
                    demo.slider1.setValue(demo.rescaleFactor);
                    demo.slider2.setValue((int) demo.rescaleOffset);
                    demo.slider1.setEnabled(true);
                    demo.slider2.setEnabled(true);
                } else {
                    demo.slider1.setEnabled(false);
                    demo.slider2.setEnabled(false);
                }
            } else if (e.getSource().equals(imgCombo)) {
                demo.imgIndex = imgCombo.getSelectedIndex();
            }
            demo.repaint(10);
        }


        public Dimension getPreferredSize() {
            return new Dimension(200,39);
        }


        public void run() {
            try { thread.sleep(1111); } catch (Exception e) { return; }
            Thread me = Thread.currentThread();
            while (thread == me) {
                for (int i = 0; i < ImageOps.imgName.length; i++) {
                    imgCombo.setSelectedIndex(i);
                    for (int j = 0; j < ImageOps.opsName.length; j++) {
                        opsCombo.setSelectedIndex(j);
                        if (j <= 1) {
                            for (int k = 50; k <= 200; k+=10) {
                                demo.slider1.setValue(k);
                                try {
                                    thread.sleep(200);
                                } catch (InterruptedException e) { return; }
                            }
                        }
                        try {
                            thread.sleep(4444);
                        } catch (InterruptedException e) { return; }
                    }
                }
            }
            thread = null;
        }
    } // End DemoControls
} // End ImageOps

/*
 * @(#)src/demo/jfc/Java2D/src/java2d/Java2DemoApplet.java, dsdev, dsdev 1.7
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






package java2d;

import java.awt.*;
import javax.swing.*;


/**
 * A demo that shows Java2D features. 
 *
 * Parameters that can be used in the Java2Demo.html file inside
 * the applet tag to customize demo runs :
              <param name="runs" value="10">
              <param name="delay" value="10">
              <param name="ccthread" value=" ">
              <param name="screen" value="5">
              <param name="antialias" value="true">
              <param name="rendering" value="true">
              <param name="texture" value="true">
              <param name="composite" value="true">
              <param name="verbose" value=" ">
              <param name="buffers" value="3,10">
              <param name="verbose" value=" ">
              <param name="zoom" value=" ">
 *
 * @version @(#)Java2DemoApplet.java	1.16 02/06/13
 * @author Brian Lichtenwalter  (Framework, Intro, demos)
 * @author Jim Graham           (demos)
 */
public class Java2DemoApplet extends JApplet {

    public static JApplet applet;


    public void init() {

        applet = this;

        JPanel panel = new JPanel();
        getContentPane().add(panel,BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel progressPanel = new JPanel() {
            public Insets getInsets() {
                return new Insets(40,30,20,30);
            }
        };
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

        panel.add(Box.createGlue());
        panel.add(progressPanel);
        panel.add(Box.createGlue());

        progressPanel.add(Box.createGlue());

        Dimension d = new Dimension(400, 20);
        Java2Demo.progressLabel = new JLabel("Loading, please wait...");
        Java2Demo.progressLabel.setMaximumSize(d);
        progressPanel.add(Java2Demo.progressLabel);
        progressPanel.add(Box.createRigidArea(new Dimension(1,20)));

        Java2Demo.progressBar = new JProgressBar();
        Java2Demo.progressBar.setStringPainted(true);
        Java2Demo.progressLabel.setLabelFor(Java2Demo.progressBar);
        Java2Demo.progressBar.setAlignmentX(CENTER_ALIGNMENT);
        Java2Demo.progressBar.setMaximumSize(d);
        Java2Demo.progressBar.setMinimum(0);
        Java2Demo.progressBar.setValue(0);
        progressPanel.add(Java2Demo.progressBar);
        progressPanel.add(Box.createGlue());
        progressPanel.add(Box.createGlue());

        Rectangle ab = getContentPane().getBounds();
        panel.setPreferredSize(new Dimension(ab.width,ab.height));
        getContentPane().add(panel,BorderLayout.CENTER);
        validate();
        setVisible(true);

        Java2Demo.demo = new Java2Demo();
        getContentPane().remove(panel);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(Java2Demo.demo, BorderLayout.CENTER);

        String param = null;

        if ((param = getParameter("delay")) != null) {
            RunWindow.delay = Integer.parseInt(param);
        } 
        if (getParameter("ccthread") != null) {
            Java2Demo.demo.ccthreadCB.setSelected(true);
        }
        if ((param = getParameter("screen")) != null) {
            Java2Demo.demo.controls.screenCombo.setSelectedIndex(Integer.parseInt(param));
        } 
        if ((param = getParameter("antialias")) != null) {
            Java2Demo.demo.controls.aliasCB.setSelected(param.endsWith("true"));
        } 
        if ((param = getParameter("rendering")) != null) {
            Java2Demo.demo.controls.renderCB.setSelected(param.endsWith("true"));
        } 
        if ((param = getParameter("texture")) != null) {
            Java2Demo.demo.controls.textureCB.setSelected(param.endsWith("true"));
        } 
        if ((param = getParameter("composite")) != null) {
            Java2Demo.demo.controls.compositeCB.setSelected(param.endsWith("true"));
        } 
        if (getParameter("verbose") != null) {
            Java2Demo.demo.verboseCB.setSelected(true);
        } 
        if ((param = getParameter("columns")) != null) {
            DemoGroup.columns = Integer.parseInt(param);
        } 
        if ((param = getParameter("buffers")) != null) {
            // usage -buffers=3,10
            RunWindow.buffersFlag = true;
            int i = param.indexOf(',');
            String s1 = param.substring(0, i);
            RunWindow.bufBeg = Integer.parseInt(s1);
            s1 = param.substring(i+1, param.length());
            RunWindow.bufEnd = Integer.parseInt(s1);
        } 
        if (getParameter("zoom") != null) {
            RunWindow.zoomCB.setSelected(true);
        }
        if ((param = getParameter("runs")) != null) {
            RunWindow.numRuns = Integer.parseInt(param);
            Java2Demo.demo.createRunWindow();
            Java2Demo.doClickOnDispatchThread(RunWindow.runB);
        } 
        validate();
        repaint();
        Java2Demo.demo.requestDefaultFocus();
    }

    public void start() {
        Java2Demo.demo.start();
    }

    public void stop() {
        Java2Demo.demo.stop();
    }
}

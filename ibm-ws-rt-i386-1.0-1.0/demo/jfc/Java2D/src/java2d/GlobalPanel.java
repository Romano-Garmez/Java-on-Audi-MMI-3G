/*
 * @(#)src/demo/jfc/Java2D/src/java2d/GlobalPanel.java, dsdev, dsdev 1.6
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
 * @(#)GlobalPanel.java	1.15 02/06/13
 */


package java2d;

import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * Panel that holds the Demo groups, Controls and Monitors for each tab.
 * It's a special "always visible" panel for the Controls, MemoryMonitor &
 * PerformanceMonitor.
 */
public class GlobalPanel extends JPanel implements ChangeListener {


    private JPanel p;
    private int index;


    public GlobalPanel() {
        setLayout(new BorderLayout());
        p = new JPanel(new GridBagLayout());
        EmptyBorder eb = new EmptyBorder(5,0,5,5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        p.setBorder(new CompoundBorder(eb,bb));
        Java2Demo.addToGridBag(p,Java2Demo.controls,0,0,1,1,0,0);
        Java2Demo.addToGridBag(p,Java2Demo.memorymonitor,0,1,1,1,0,0);
        Java2Demo.addToGridBag(p,Java2Demo.performancemonitor,0,2,1,1,0,0);
        add(Java2Demo.intro);
    }


    public void stateChanged(ChangeEvent e) {
         
        Java2Demo.group[index].shutDown(Java2Demo.group[index].getPanel());
        if (Java2Demo.tabbedPane.getSelectedIndex() == 0) {
            Java2Demo.memorymonitor.surf.stop();
            Java2Demo.performancemonitor.surf.stop();
            removeAll();
            add(Java2Demo.intro);
            Java2Demo.intro.start();
        } else {
            if (getComponentCount() == 1) {
                Java2Demo.intro.stop();
                remove(Java2Demo.intro);
                add(p, BorderLayout.EAST);
                if (Java2Demo.memoryCB.getState()) { 
                    Java2Demo.memorymonitor.surf.start();
                }
                if (Java2Demo.perfCB.getState()) { 
                    Java2Demo.performancemonitor.surf.start();
                }
            } else {
                remove(Java2Demo.group[index]);
            }
            index = Java2Demo.tabbedPane.getSelectedIndex()-1;
            add(Java2Demo.group[index]);
            Java2Demo.group[index].setup(false);
        }
        validate();
    }
}

/*
 * @(#)src/demo/jfc/Java2D/src/java2d/GlobalControls.java, dsdev, dsdev 1.6
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
 * @(#)GlobalControls.java	1.27 02/06/13
 */


package java2d;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;


/**
 * Global Controls panel for changing graphic attributes of
 * the demo surface.
 */
public class GlobalControls extends JPanel implements ItemListener, ChangeListener {

    static String[] screenNames = { 
            "Auto Screen", "On Screen", "Off Screen",
            "INT_xRGB", "INT_ARGB", "INT_ARGB_PRE", "INT_BGR",
            "3BYTE_BGR", "4BYTE_ABGR", "4BYTE_ABGR_PRE", "USHORT_565_RGB",
            "USHORT_x555_RGB", "BYTE_GRAY", "USHORT_GRAY",
	    "BYTE_BINARY", "BYTE_INDEXED", "BYTE_BINARY 2 bit", "BYTE_BINARY 4 bit",
	    "INT_RGBx", "USHORT_555x_RGB"};
    static JComboBox screenCombo;
    public TextureChooser texturechooser;
    public JCheckBox aliasCB, renderCB, toolBarCB;
    public JCheckBox compositeCB, textureCB;
    public JSlider slider;
    public Object obj;

    private Font font = new Font("serif", Font.PLAIN, 12);


    public GlobalControls() {
        setLayout(new GridBagLayout());
        setBorder(new TitledBorder(new EtchedBorder(), "Global Controls"));

        aliasCB = createCheckBox("Anti-Aliasing", true, 0);
        renderCB = createCheckBox("Rendering Quality", false, 1);
        textureCB = createCheckBox("Texture", false, 2);
        compositeCB = createCheckBox("AlphaComposite", false, 3);

        screenCombo = new JComboBox();
        screenCombo.setPreferredSize(new Dimension(120, 18));
        screenCombo.setLightWeightPopupEnabled(true);
        screenCombo.setFont(font);
        for (int i = 0; i < screenNames.length; i++) {
            screenCombo.addItem(screenNames[i]);
        } 
        screenCombo.addItemListener(this);
        Java2Demo.addToGridBag(this, screenCombo, 0, 4, 1, 1, 0.0, 0.0);

        toolBarCB = createCheckBox("Tools", false, 5);

        slider = new JSlider(JSlider.HORIZONTAL, 0, 200, 30);
        slider.addChangeListener(this);
        TitledBorder tb = new TitledBorder(new EtchedBorder());
        tb.setTitleFont(font);
        tb.setTitle("Anim delay = 30 ms");
        slider.setBorder(tb);
        slider.setMinimumSize(new Dimension(80,46));
        Java2Demo.addToGridBag(this, slider, 0, 6, 1, 1, 1.0, 1.0);

        texturechooser = new TextureChooser(0);
        Java2Demo.addToGridBag(this, texturechooser, 0, 7, 1, 1, 1.0, 1.0);
    }


    private JCheckBox createCheckBox(String s, boolean b, int y) {
        JCheckBox cb = new JCheckBox(s, b);
        cb.setFont(font);
        cb.setHorizontalAlignment(JCheckBox.LEFT);
        cb.addItemListener(this);
        Java2Demo.addToGridBag(this, cb, 0, y, 1, 1, 1.0, 1.0);
        return cb;
    }


    public void stateChanged(ChangeEvent e) {
        int value = slider.getValue();
        TitledBorder tb = (TitledBorder) slider.getBorder();
        tb.setTitle("Anim delay = " + String.valueOf(value) + " ms");
        int index = Java2Demo.tabbedPane.getSelectedIndex()-1;
        DemoGroup dg = Java2Demo.group[index];
        JPanel p = dg.getPanel();
        for (int i = 0; i < p.getComponentCount(); i++) {
            DemoPanel dp = (DemoPanel) p.getComponent(i);
            if (dp.tools != null && dp.tools.slider != null) {
                dp.tools.slider.setValue(value);
            }
        } 
        slider.repaint();
    }


    public void itemStateChanged(ItemEvent e) {
        if (Java2Demo.tabbedPane.getSelectedIndex() != 0) {
            obj = e.getSource();
            int index = Java2Demo.tabbedPane.getSelectedIndex()-1;
            Java2Demo.group[index].setup(true);
            obj = null;
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(135,260);
    }
}

/*
 * @(#)src/demo/applets/ArcTest/ArcTest.java, ui, dsdev 1.14
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
 * @(#)ArcTest.java	1.9 02/06/13
 */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

/**
 * An interactive test of the Graphics.drawArc and Graphics.fillArc
 * routines. Can be run either as a standalone application by
 * typing "java ArcTest" or as an applet in the AppletViewer.
 */
public class ArcTest extends Applet {
    ArcControls controls;   // The controls for marking and filling arcs
    ArcCanvas canvas;       // The drawing area to display arcs

    public void init() {
	setLayout(new BorderLayout());
	canvas = new ArcCanvas();
	add("Center", canvas);
	add("South", controls = new ArcControls(canvas));
    }

    public void destroy() {
        remove(controls);
        remove(canvas);
    }

    public void start() {
	controls.setEnabled(true);
    }

    public void stop() {
	controls.setEnabled(false);
    }

    public void processEvent(AWTEvent e) {
        if (e.getID() == Event.WINDOW_DESTROY) {
            System.exit(0);
        }
    }

    public static void main(String args[]) {
	Frame f = new Frame("ArcTest");
	ArcTest	arcTest = new ArcTest();

	arcTest.init();
	arcTest.start();

	f.add("Center", arcTest);
	f.setSize(300, 300);
	f.show();
    }

    public String getAppletInfo() {
        return "An interactive test of the Graphics.drawArc and \nGraphics.fillArc routines. Can be run \neither as a standalone application by typing 'java ArcTest' \nor as an applet in the AppletViewer.";
    }
}

class ArcCanvas extends Canvas {
    int		startAngle = 0;
    int		extent = 45;
    boolean	filled = false;
    Font	font = new java.awt.Font("SansSerif", Font.PLAIN, 12);

    public void paint(Graphics g) {
	Rectangle r = getBounds();
	int hlines = r.height / 10;
	int vlines = r.width / 10;

	g.setColor(Color.pink);
	for (int i = 1; i <= hlines; i++) {
	    g.drawLine(0, i * 10, r.width, i * 10);
	}
	for (int i = 1; i <= vlines; i++) {
	    g.drawLine(i * 10, 0, i * 10, r.height);
	}

	g.setColor(Color.red);
	if (filled) {
	    g.fillArc(0, 0, r.width - 1, r.height - 1, startAngle, extent);
	} else {
	    g.drawArc(0, 0, r.width - 1, r.height - 1, startAngle, extent);
	}

	g.setColor(Color.black);
	g.setFont(font);
	g.drawLine(0, r.height / 2, r.width, r.height / 2);
	g.drawLine(r.width / 2, 0, r.width / 2, r.height);
	g.drawLine(0, 0, r.width, r.height);
	g.drawLine(r.width, 0, 0, r.height);
	int sx = 10;
	int sy = r.height - 28;
	g.drawString("Start = " + startAngle, sx, sy);
	g.drawString("Extent = " + extent, sx, sy + 14);
    }

    public void redraw(boolean filled, int start, int extent) {
	this.filled = filled;
	this.startAngle = start;
	this.extent = extent;
	repaint();
    }
}

class ArcControls extends Panel
                  implements ActionListener {
    TextField startTF;
    TextField extentTF;
    ArcCanvas canvas;

    public ArcControls(ArcCanvas canvas) {
	Button b = null;

	this.canvas = canvas;
	add(startTF = new IntegerTextField("0", 4));
	add(extentTF = new IntegerTextField("45", 4));
	b = new Button("Fill");
	b.addActionListener(this);
	add(b);
	b = new Button("Draw");
	b.addActionListener(this);
	add(b);
    }

    public void actionPerformed(ActionEvent ev) {
	String label = ev.getActionCommand();

        int start, extent; 
        try {
            start = Integer.parseInt(startTF.getText().trim());
        } catch (NumberFormatException nfe) {
            start = 0;
        }
        try {
            extent = Integer.parseInt(extentTF.getText().trim());
        } catch (NumberFormatException nfe) {
            extent = 0;
        }

	canvas.redraw(label.equals("Fill"), start, extent);
    }
}

class IntegerTextField extends TextField {

    String oldText = null;

    public IntegerTextField(String text, int columns) {
        super(text, columns);
        enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.TEXT_EVENT_MASK);
        oldText = getText();
    }

    // Consume non-digit KeyTyped events
    // Note that processTextEvent kind of eliminates the need for this
    // function, but this is neater, since ideally, it would prevent
    // the text from appearing at all.  Sigh.  See bugid 4100317/4114565.
    //
    protected void processEvent(AWTEvent evt) {
        int id = evt.getID();
        if (id != KeyEvent.KEY_TYPED) {
            super.processEvent(evt);
            return;
        }

        KeyEvent kevt = (KeyEvent) evt;
        char c = kevt.getKeyChar();

        // Digits, backspace, and delete are okay
        // Note that the minus sign is allowed, but not the decimal
        if (Character.isDigit(c) || (c == '\b') || (c == '\u007f') || 
            (c == '\u002d')) {
            super.processEvent(evt);
            return;
        }

        Toolkit.getDefaultToolkit().beep();
        kevt.consume();
    }

    // Should consume TextEvents for non-integer Strings
    // Store away the text in the tf for every TextEvent
    // so we can revert to it on a TextEvent (paste, or 
    // legal key in the wrong location) with bad text
    //
    protected void processTextEvent(TextEvent te) {
        // The empty string is okay, too
        String newText = getText();
        if (newText.equals("") || textIsInteger(newText)) {
            oldText = newText;
            super.processTextEvent(te);
            return;
        }

        Toolkit.getDefaultToolkit().beep();
        setText(oldText);
    }

    // Returns true for Integers (zero and negative 
    // values are allowed).
    // Note that the empty string is not allowed. 
    // 
    private boolean textIsInteger(String textToCheck) {
        int value = -1;

        try {
            value = Integer.parseInt(textToCheck, 10);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
}



/*
 * @(#)src/demo/jfc/Stylepad/src/HelloWorld.java, swing, dsdev 1.11
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
 * @(#)HelloWorld.java	1.6 02/06/13
 */



import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import java.net.URL;
import java.util.Hashtable;
import java.awt.Color;
import javax.swing.*;
import javax.swing.text.*;

/**
 * hack to load attributed content.
 */
public class HelloWorld {

  HelloWorld(DefaultStyledDocument doc, StyleContext styles) {
    this.doc = doc;
    this.styles = styles;
    runAttr = new Hashtable();
  }

  void loadDocument() {
    createStyles();
    for (int i = 0; i < data.length; i++) {
      Paragraph p = data[i];
      addParagraph(p);
    }
  }

  void addParagraph(Paragraph p) {
    try {
      Style s = null;
      for (int i = 0; i < p.data.length; i++) {
	Run run = p.data[i];
	s = (Style) runAttr.get(run.attr);
	doc.insertString(doc.getLength(), run.content, s);
      }

      // set logical style
      Style ls = styles.getStyle(p.logical);
      doc.setLogicalStyle(doc.getLength() - 1, ls);
      doc.insertString(doc.getLength(), "\n", null);
    } catch (BadLocationException e) {
      System.err.println("Internal error: " + e);
    }
  }

  void createStyles() {
    // no attributes defined
    Style s = styles.addStyle(null, null);
    runAttr.put("none", s);
    s = styles.addStyle(null, null);
    StyleConstants.setItalic(s, true);
    StyleConstants.setForeground(s, new Color(153,153,102));
    runAttr.put("cquote", s); // catepillar quote

    s = styles.addStyle(null, null);
    StyleConstants.setItalic(s, true);
    StyleConstants.setForeground(s, new Color(51,102,153));
    runAttr.put("aquote", s); // alice quote
    
    try {
        ResourceBundle resources
            = ResourceBundle.getBundle("resources.Stylepad", 
                                       Locale.getDefault());
	    s = styles.addStyle(null, null);
	    Icon alice = new ImageIcon(resources.getString("aliceGif"));    
	    StyleConstants.setIcon(s, alice);
	    runAttr.put("alice", s); // alice

	    s = styles.addStyle(null, null);
	    Icon caterpillar = new ImageIcon(resources.getString("caterpillarGif"));    
	    StyleConstants.setIcon(s, caterpillar);
	    runAttr.put("caterpillar", s); // caterpillar

	    s = styles.addStyle(null, null);
	    Icon hatter = new ImageIcon(resources.getString("hatterGif"));    
	    StyleConstants.setIcon(s, hatter);
	    runAttr.put("hatter", s); // hatter

	    
    } catch (MissingResourceException mre) {
      // can't display image
    }

    Style def = styles.getStyle(StyleContext.DEFAULT_STYLE);

    Style heading = styles.addStyle("heading", def);
    //StyleConstants.setFontFamily(heading, "SansSerif");
    StyleConstants.setBold(heading, true);
    StyleConstants.setAlignment(heading, StyleConstants.ALIGN_CENTER);
    StyleConstants.setSpaceAbove(heading, 10);
    StyleConstants.setSpaceBelow(heading, 10);
    StyleConstants.setFontSize(heading, 18);

    // Title 
    Style sty = styles.addStyle("title", heading);
    StyleConstants.setFontSize(sty, 32);

    // edition
    sty = styles.addStyle("edition", heading);
    StyleConstants.setFontSize(sty, 16);

    // author
    sty = styles.addStyle("author", heading);
    StyleConstants.setItalic(sty, true);
    StyleConstants.setSpaceBelow(sty, 25);

    // subtitle
    sty = styles.addStyle("subtitle", heading);
    StyleConstants.setSpaceBelow(sty, 35);

    // normal 
    sty = styles.addStyle("normal", def);
    StyleConstants.setLeftIndent(sty, 10);
    StyleConstants.setRightIndent(sty, 10);
    //StyleConstants.setFontFamily(sty, "SansSerif");
    StyleConstants.setFontSize(sty, 14);
    StyleConstants.setSpaceAbove(sty, 4);
    StyleConstants.setSpaceBelow(sty, 4);
  }

  DefaultStyledDocument doc; 
  StyleContext styles;
  Hashtable runAttr;
  
  static class Paragraph {
    Paragraph(String logical, Run[] data) {
      this.logical = logical;
      this.data = data;
    }
    String logical;
    Run[] data;
  }

  static class Run {
    Run(String attr, String content) {
      this.attr = attr;
      this.content = content;
    }
    String attr;
    String content;
  }

  Paragraph[] data = new Paragraph[] {
    new Paragraph("title", new Run[] {
          new Run("none", "Hello from Cupertino")
	}),
    new Paragraph("title", new Run[] {
      new Run("none", "\u53F0\u5317\u554F\u5019\u60A8\u0021")
	}),
    new Paragraph("title", new Run[] {
      new Run("none", "\u0391\u03B8\u03B7\u03BD\u03B1\u03B9\u0020"   // Greek
           + "\u03B1\u03C3\u03C0\u03B1\u03B6\u03BF\u03BD"
           + "\u03C4\u03B1\u03B9\u0020\u03C5\u03BC\u03B1"
           + "\u03C2\u0021")
	}),
    new Paragraph("title", new Run[] {
      new Run("none", "\u6771\u4eac\u304b\u3089\u4eca\u65e5\u306f")
	}),
    new Paragraph("title", new Run[] {
      new Run("none", "\u05e9\u05dc\u05d5\u05dd \u05de\u05d9\u05e8\u05d5"
              + "\u05e9\u05dc\u05d9\u05dd")
	}),
    new Paragraph("title", new Run[] {
      new Run("none", "\u0633\u0644\u0627\u0645")
	}),
  };


}

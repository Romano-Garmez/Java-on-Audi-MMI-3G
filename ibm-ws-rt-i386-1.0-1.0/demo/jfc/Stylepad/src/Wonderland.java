/*
 * @(#)src/demo/jfc/Stylepad/src/Wonderland.java, swing, dsdev 1.13
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
 * @(#)Wonderland.java	1.10 02/06/13
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
 * hack to load attributed content
 */
public class Wonderland {

  Wonderland(DefaultStyledDocument doc, StyleContext styles) {
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
            ResourceBundle resources = ResourceBundle.getBundle("resources.Stylepad", 
								Locale.getDefault());
	    s = styles.addStyle(null, null);
	    Icon alice = 
                new ImageIcon(getClass().
                              getResource(resources.getString("aliceGif")));
	    StyleConstants.setIcon(s, alice);
	    runAttr.put("alice", s); // alice

	    s = styles.addStyle(null, null);
	    Icon caterpillar = 
                new ImageIcon(getClass().
                              getResource(resources.getString("caterpillarGif")));
	    StyleConstants.setIcon(s, caterpillar);
	    runAttr.put("caterpillar", s); // caterpillar

	    s = styles.addStyle(null, null);
	    Icon hatter = 
                new ImageIcon(getClass().
                              getResource(resources.getString("hatterGif")));
	    StyleConstants.setIcon(s, hatter);
	    runAttr.put("hatter", s); // hatter


    } catch (MissingResourceException mre) {
      // can't display image
    }

    Style def = styles.getStyle(StyleContext.DEFAULT_STYLE);

    Style heading = styles.addStyle("heading", def);
    StyleConstants.setFontFamily(heading, "SansSerif");
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
    StyleConstants.setFontFamily(sty, "SansSerif");
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
      new Run("none", "ALICE'S ADVENTURES IN WONDERLAND")
	}),
    new Paragraph("author", new Run[] {
      new Run("none", "Lewis Carroll")
	}),
    new Paragraph("heading", new Run[] {
      new Run("alice", " ")
	}),
    new Paragraph("edition", new Run[] {
      new Run("none", "THE MILLENNIUM FULCRUM EDITION 3.0")
	}),
    new Paragraph("heading", new Run[] {
      new Run("none", "CHAPTER V")
	}),
    new Paragraph("subtitle", new Run[] {
      new Run("none", "Advice from a Caterpillar")
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", " "),
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", "The Caterpillar and Alice looked at each other for some time in silence:  at last the Caterpillar took the hookah out of its mouth, and addressed her in a languid, sleepy voice.")
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "Who are YOU?  "),
      new Run("none", "said the Caterpillar.")
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", "This was not an encouraging opening for a conversation.  Alice replied, rather shyly, "),
      new Run("aquote", "I--I hardly know, sir, just at present--at least I know who I WAS when I got up this morning, but I think I must have been changed several times since then. "),
	}),
    new Paragraph("heading", new Run[] {
      new Run("caterpillar", " ")
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "What do you mean by that? "),
      new Run("none", " said the Caterpillar sternly.  "),
      new Run("cquote", "Explain yourself!"),
	}),
    new Paragraph("normal", new Run[] {
      new Run("aquote", "I can't explain MYSELF, I'm afraid, sir"),
      new Run("none", " said Alice, "),
      new Run("aquote", "because I'm not myself, you see."),
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "I don't see,"),
      new Run("none", " said the Caterpillar."),
	}),
    new Paragraph("normal", new Run[] {
      new Run("aquote", "I'm afraid I can't put it more clearly,  "),
      new Run("none", "Alice replied very politely, "),
      new Run("aquote", "for I can't understand it myself to begin with; and being so many different sizes in a day is very confusing."),
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "It isn't,  "),
      new Run("none", "said the Caterpillar.")
	}),
    new Paragraph("normal", new Run[] {
      new Run("aquote", "Well, perhaps you haven't found it so yet,"),
      new Run("none", " said Alice; "),
      new Run("aquote", "but when you have to turn into a chrysalis--you will some day, you know--and then after that into a butterfly, I should think you'll feel it a little queer, won't you?")
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "Not a bit, "),
      new Run("none", "said the Caterpillar.")
	}),
    new Paragraph("normal", new Run[] {
      new Run("aquote", "Well, perhaps your feelings may be different,"),
      new Run("none", " said Alice; "),
      new Run("aquote", "all I know is, it would feel very queer to ME."),
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "You!"),
      new Run("none", " said the Caterpillar contemptuously.  "),
      new Run("cquote", "Who are YOU?"),
	}),
    new Paragraph("normal", new Run[] {
      new Run("normal", "Which brought them back again to the beginning of the conversation.  Alice felt a little irritated at the Caterpillar's making such VERY short remarks, and she drew herself up and said, very gravely, "),
      new Run("aquote", "I think, you ought to tell me who YOU are, first."),
	}),
    new Paragraph("normal", new Run[] {
      new Run("cquote", "Why?  "),
      new Run("none", "said the Caterpillar."),
	}),
    new Paragraph("heading", new Run[] {
      new Run("hatter", " ")
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", " "),
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", " "),
	}),
    new Paragraph("normal", new Run[] {
      new Run("none", " "),
	})
  };


}

/*
 * @(#)src/demo/jfc/SampleTree/src/DynamicTreeNode.java, swing, dsdev 1.11
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
 * @(#)DynamicTreeNode.java	1.9 02/06/13
 */

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Random;

/**
  * DynamicTreeNode illustrates one of the possible ways in which dynamic
  * loading can be used in tree.  The basic premise behind this is that
  * getChildCount() will be messaged from JTreeModel before any children
  * are asked for.  So, the first time getChildCount() is issued the
  * children are loaded.<p>
  * It should be noted that isLeaf will also be messaged from the model.
  * The default behavior of TreeNode is to message getChildCount to
  * determine this. As such, isLeaf is subclassed to always return false.<p>
  * There are others ways this could be accomplished as well.  Instead of
  * subclassing TreeNode you could subclass JTreeModel and do the same
  * thing in getChildCount().  Or, if you aren't using TreeNode you could
  * write your own TreeModel implementation.
  * Another solution would be to listen for TreeNodeExpansion events and
  * the first time a node has been expanded post the appropriate insertion
  * events.  I would not recommend this approach though, the other two
  * are much simpler and cleaner (and are faster from the perspective of
  * how tree deals with it).
  *
  * NOTE: getAllowsChildren() can be messaged before getChildCount().
  *       For this example the nodes always allow children, so it isn't
  *       a problem, but if you do support true leaf nodes you may want
  *       to check for loading in getAllowsChildren too.
  *
  * @version 1.9 06/13/02
  * @author Scott Violet
  */

public class DynamicTreeNode extends DefaultMutableTreeNode
{
    // Class stuff.
    /** Number of names. */
    static protected float                    nameCount;

    /** Names to use for children. */
    static protected String[]                 names;

    /** Potential fonts used to draw with. */
    static protected Font[]                   fonts;

    /** Used to generate the names. */
    static protected Random                   nameGen;

    /** Number of children to create for each node. */
    static protected final int                DefaultChildrenCount = 7;

    static {
	String[]            fontNames;

	try {
	    fontNames = Toolkit.getDefaultToolkit().getFontList();
	} catch (Exception e) {
	    fontNames = null;
	}
	if(fontNames == null || fontNames.length == 0) {
	    names = new String[] {"Mark Andrews", "Tom Ball", "Alan Chung",
				      "Rob Davis", "Jeff Dinkins",
				      "Amy Fowler", "James Gosling",
				      "David Karlton", "Dave Kloba", 
				      "Dave Moore", "Hans Muller",
				      "Rick Levenson", "Tim Prinzing",
				      "Chester Rose", "Ray Ryan",
				      "Georges Saab", "Scott Violet",
				      "Kathy Walrath", "Arnaud Weber" };
	}
	else {
	    /* Create the Fonts, creating fonts is slow, much better to
	       do it once. */
	    int              fontSize = 12;

	    names = fontNames;
	    fonts = new Font[names.length];
	    for(int counter = 0, maxCounter = names.length;
		counter < maxCounter; counter++) {
		try {
		    fonts[counter] = new Font(fontNames[counter], 0, fontSize);
		}
		catch (Exception e) {
		    fonts[counter] = null;
		}
		fontSize = ((fontSize + 2 - 12) % 12) + 12;
	    }
	}
	nameCount = (float)names.length;
	nameGen = new Random(System.currentTimeMillis());
    }


    /** Have the children of this node been loaded yet? */
    protected boolean           hasLoaded;

    /**
      * Constructs a new DynamicTreeNode instance with o as the user
      * object.
      */
    public DynamicTreeNode(Object o) {
	super(o);
    }

    public boolean isLeaf() {
	return false;
    }

    /**
      * If hasLoaded is false, meaning the children have not yet been
      * loaded, loadChildren is messaged and super is messaged for
      * the return value.
      */
    public int getChildCount() {
	if(!hasLoaded) {
	    loadChildren();
	}
	return super.getChildCount();
    }

    /**
      * Messaged the first time getChildCount is messaged.  Creates
      * children with random names from names.
      */
    protected void loadChildren() {
	DynamicTreeNode             newNode;
	Font                        font;
	int                         randomIndex;
	SampleData                  data;

	for(int counter = 0; counter < DynamicTreeNode.DefaultChildrenCount;
	    counter++) {
	    randomIndex = (int)(nameGen.nextFloat() * nameCount);
	    if(fonts != null)
		font = fonts[randomIndex];
	    else
		font = null;
	    if(counter % 2 == 0)
		data = new SampleData(font, Color.red, names[randomIndex]);
	    else
		data = new SampleData(font, Color.blue, names[randomIndex]);
	    newNode = new DynamicTreeNode(data);
	    /* Don't use add() here, add calls insert(newNode, getChildCount())
	       so if you want to use add, just be sure to set hasLoaded = true
	       first. */
	    insert(newNode, counter);
	}
	/* This node has now been loaded, mark it so. */
	hasLoaded = true;
    }
}

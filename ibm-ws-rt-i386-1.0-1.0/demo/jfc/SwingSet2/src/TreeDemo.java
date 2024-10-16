/*
 * @(#)src/demo/jfc/SwingSet2/src/TreeDemo.java, swing, dsdev 1.13
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
 * @(#)TreeDemo.java	1.6 02/06/13
 */


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.accessibility.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

/**
 * JTree Demo
 *
 * @version 1.10 07/26/04
 * @author Jeff Dinkins
 */
public class TreeDemo extends DemoModule {

    JTree tree;

    /**
     * main method allows us to run as a standalone demo.
     */
    public static void main(String[] args) {
	TreeDemo demo = new TreeDemo(null);
	demo.mainImpl();
    }

    /**
     * TreeDemo Constructor
     */
    public TreeDemo(SwingSet2 swingset) {
	// Set the title for this demo, and an icon used to represent this
	// demo inside the SwingSet2 app.
	super(swingset, "TreeDemo", "toolbar/JTree.gif");

	getDemoPanel().add(createTree(), BorderLayout.CENTER);
    }
 
    public JScrollPane createTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(getString("TreeDemo.music"));
        DefaultMutableTreeNode catagory = null ;
	DefaultMutableTreeNode artist = null;
	DefaultMutableTreeNode record = null;

	// open tree data 
	URL url = getClass().getResource("/resources/tree.txt");

	try {
	    // convert url to buffered string
	    InputStream is = url.openStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader reader = new BufferedReader(isr);

	    // read one line at a time, put into tree
	    String line = reader.readLine();
	    while(line != null) {
		// System.out.println("reading in: ->" + line + "<-");
		char linetype = line.charAt(0);
		switch(linetype) {
		   case 'C':
		     catagory = new DefaultMutableTreeNode(line.substring(2));
		     top.add(catagory);
		     break;
		   case 'A':
		     if(catagory != null) {
		         catagory.add(artist = new DefaultMutableTreeNode(line.substring(2)));
		     }
		     break;
		   case 'R':
		     if(artist != null) {
		         artist.add(record = new DefaultMutableTreeNode(line.substring(2)));
		     }
		     break;
		   case 'S':
		     if(record != null) {
		         record.add(new DefaultMutableTreeNode(line.substring(2)));
		     }
		     break;
		   default:
		     break;
		}
		line = reader.readLine();
	    }
	} catch (IOException e) {
	}

	tree = new JTree(top) {
	    public Insets getInsets() {
		return new Insets(5,5,5,5);
	    }
	};
        
        tree.setEditable(true);
            
	return new JScrollPane(tree);
    }
    
    void updateDragEnabled(boolean dragEnabled) {
        tree.setDragEnabled(dragEnabled);
    }

}

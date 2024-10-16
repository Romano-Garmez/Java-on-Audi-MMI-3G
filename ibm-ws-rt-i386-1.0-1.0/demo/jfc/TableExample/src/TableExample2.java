/*
 * @(#)src/demo/jfc/TableExample/src/TableExample2.java, swing, dsdev 1.11
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
 * @(#)TableExample2.java	1.13 02/06/13
 */

/**
 * A minimal example, using the JTable to view data from a database.
 *
 * @version 1.20 09/25/97
 * @author Philip Milne
 */

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;

public class TableExample2 {

    public TableExample2(String URL, String driver, String user,
                         String passwd, String query) {
        JFrame frame = new JFrame("Table");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}});
        JDBCAdapter dt = new JDBCAdapter(URL, driver, user, passwd);
        dt.executeQuery(query);

        // Create the table
        JTable tableView = new JTable(dt);

        JScrollPane scrollpane = new JScrollPane(tableView);
        scrollpane.setPreferredSize(new Dimension(700, 300));

        frame.getContentPane().add(scrollpane);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Needs database parameters eg. ...");
            System.err.println("java TableExample2 \"jdbc:sybase://dbtest:1455/pubs2\" \"connect.sybase.SybaseDriver\" guest trustworthy \"select * from titles\"");
            return;
        }
        new TableExample2(args[0], args[1], args[2], args[3], args[4]);
    }
}

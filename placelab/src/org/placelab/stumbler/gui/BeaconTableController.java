package org.placelab.stumbler.gui;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.placelab.collections.HashMap;
import org.placelab.collections.HashSet;
import org.placelab.collections.Iterator;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.Mapper;
import org.placelab.util.ns1.DHeap;
import org.placelab.util.ns1.NS1Translator;

/**
 * A BeaconTableController is displays a table where the history of beacons seen is shown.  
 * In a departure from the old style, there is now a single table where active readings
 * are shown in a different colour at the top.
 * The PlacelabStumblerGUI will offer the
 * BeaconTableController measurements for the StumblerSpotter it is displaying info
 * for every update, and the BeaconTableController will collate that data and display
 * it in the tables that it manages.
 */
public class BeaconTableController {

    public Label label;
    public TableTree table;
    public TableColumn cols[];
    public Composite parent;
    public String name;
    public Font labelFont, tableFont;
    public Mapper mapper;
    public Composite body;
    public Image activeIcon;
    public Image inactiveIcon; 
    public TableTreeViewer viewer;
    
    public HashMap active, history; 
    public HashSet expanded;
    
    public Label seenLabel;
    
    public long activeThresholdTime = 2000;
    
    public int numNew = 0;
    
    public BeaconTableController(String name, Font labelFont, Font tableFont, Composite parent,
            Mapper mapper, long activeThresholdTime) {
        active = new HashMap();
        history = new HashMap();
        expanded = new HashSet();
        this.name = name;
        this.labelFont = labelFont;
        this.tableFont = tableFont;
        this.parent = parent;
        this.mapper = mapper;
        this.activeThresholdTime = activeThresholdTime;
        build();
    }
    
    protected void build() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = layout.marginHeight = 0;
        GridData gridData;

        
        body = new Composite(parent, 0);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = layout.marginHeight = 0;
        body.setLayout(layout);
      
        label = new Label(body, SWT.NONE);
        label.setText(name);
        label.setFont(labelFont);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
        label.setLayoutData(gridData);
        
		seenLabel = new Label(body, SWT.NONE);
		seenLabel.setText("Active Beacons:  None    Total Seen:  None    Total New:  None    ");
		seenLabel.setFont(tableFont);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		seenLabel.setLayoutData(gridData);
		seenLabel.setVisible(true);
        
        table = new TableTree(body, SWT.BORDER);
        table.setFont(tableFont);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        table.setLayoutData(gridData);
        table.setFont(tableFont);
        table.addTreeListener(new TreeListener() {
		    public void treeExpanded(TreeEvent e) {
		        Widget source = e.item;
		        if(source instanceof TableTreeItem) {
		            expandHistoryPackage((TableTreeItem)source);
		            expanded.add(source.getData("HistoryPackage"));
		        } else {
		            System.out.println("doh");
		        }
		    }
		    public void treeCollapsed(TreeEvent e) {
		        Widget source = e.item;
		        expanded.remove(source.getData("HistoryPackage"));
		    }
		});
        buildColumns();
    }
    
    protected void buildColumns() {
        buildColumns(new String[] {"Beacon ID", "Signal Strength", "New?", "Lat", "Lon"});
    }
    
    protected void buildColumns(String[] names) {
        cols = new TableColumn[names.length + 2];
        Table historyUnderlying = table.getTable();
        cols[0] = new TableColumn(historyUnderlying, 0);
        cols[0].setWidth(30);
        for(int i = 0; i < names.length; i++) {
            cols[i + 1] = new TableColumn(historyUnderlying, 0);
            cols[i + 1].setText(names[i]);
            cols[i + 1].setWidth(50);
        }
        cols[names.length + 1] = new TableColumn(historyUnderlying, 0);
        cols[names.length + 1].setText("Time Seen");
        cols[names.length + 1].setWidth((historyUnderlying.getBounds().width - 10) / (names.length + 1));
        historyUnderlying.setHeaderVisible(true);
    }
    
    public void expandHistoryPackage(TableTreeItem item) {
        BeaconHistory p = (BeaconHistory)item.getData("HistoryPackage");
        HashSet alreadyThere = new HashSet();
        TableTreeItem[] subItems = item.getItems();
        for(int i = 0; i < subItems.length; i++) {
            alreadyThere.add(subItems[i].getData("HistoryHelper"));
        }
        for(int i = 0; i < p.readings.size(); i++) {
            BeaconHistory.BeaconHistoryHelper h = 
                (BeaconHistory.BeaconHistoryHelper)p.readings.get(i);
            if(!alreadyThere.contains(h)) {
                TableTreeItem newSubItem = new TableTreeItem(item, 0);
                newSubItem.setData("HistoryHelper", h);
                setHistoryTableItemText(newSubItem, h);
            }
        }
    }
    
    /* TODO: When we do the attribute-value coding for measurements, just do this all dynamically rather
     * than hard-coding it like this.
     */
    protected void setHistoryTableItemText(TableTreeItem item, BeaconHistory.BeaconHistoryHelper h) {
        if(active.containsKey(h.getId())) {
            item.setImage(1, getActiveIcon());
        } else {
            item.setImage(1, getInactiveIcon());
        }
        item.setText(1, h.getId());
        item.setText(2, h.reading.getNormalizedSignalStrength() + "");
        item.setText(3, h.isNew(mapper) + "");
        item.setText(4, h.gpsLL.getLatitudeAsString());
        item.setText(5, h.gpsLL.getLongitudeAsString());
        item.setText(6, humanReadableTime(h.timestamp));
    }
    
    public void addMeasurement(BeaconMeasurement meas, GPSMeasurement gps) {
        TwoDCoordinate gpsLL;
        if(gps == null) {
            gpsLL = new TwoDCoordinate();//(CoordinateFrame.GPS, 0.0, 0.0);
        } else {
            gpsLL = (TwoDCoordinate) gps.getPosition();
        }
        active.clear();
        if(meas != null) {
	        BeaconReading[] readings = meas.getReadings();
	        if(readings != null) {
		        for(int i = 0; i < readings.length; i++) {
		            if(!(minimumBeaconClass().isAssignableFrom(readings[i].getClass()))) continue;
		            BeaconHistory.BeaconHistoryHelper helper = new BeaconHistory.BeaconHistoryHelper(readings[i], gpsLL, meas.getTimestamp());
		            //active.put(helper.reading.getId(), helper);
		            addReadingToHistory(helper);
		        }
	        }
        }
        updateHistoryTable();
        updateSeenLabel();
    }
    
    protected void addReadingToHistory(BeaconHistory.BeaconHistoryHelper reading) {
        if(history.containsKey(reading.getId())) {
            BeaconHistory bh = (BeaconHistory)history.get(reading.getId());
            bh.addReading(reading);
        } else {
            BeaconHistory bh = new BeaconHistory();
            bh.addReading(reading);
            if(bh.highestRssi.isNew(mapper)) numNew++;
            history.put(reading.getId(), bh);
        }
    }
    
	protected static String humanReadableTime(long timestamp) {
	    Date d = new Date(timestamp);
	    return NS1Translator.dateToString(d, "HH:mm:ss");
	}
	
	protected void updateHistoryTable() {
		table.setRedraw(false);
	    table.removeAll();
	    Iterator e = history.values().iterator();
	    DHeap dh = new DHeap(new Comparator() {
	        public int compare(Object o1, Object o2) {
	            BeaconHistory bh1 = (BeaconHistory)o1;
	            BeaconHistory bh2 = (BeaconHistory)o2;
	            long time1 = bh1.getMostRecent();
	            long time2 = bh2.getMostRecent();
	            if(time1 < time2) return 1;
	            if(time1 == time2) return 0;
	            else return -1;
	        }
	    }, 4);
	    while(e.hasNext()) {
	        dh.insert(e.next());
	    }
	    long start = System.currentTimeMillis();
	    //if(!dh.isEmpty()) start = ((BeaconHistory)dh.findMin()).mostRecent;
	    //System.out.println("start time: " + start);
	    while(!dh.isEmpty()) {
	        BeaconHistory p = (BeaconHistory)dh.deleteMin();
	        if(Math.abs(p.mostRecent - start) < this.activeThresholdTime) {
	        	active.put(p.highestRssi.getId(), p);
	        }
	        //if(dh.isEmpty()) System.out.println("end time: " + p.mostRecent);
	        //System.out.println(p.getMostRecent());
            TableTreeItem item = new TableTreeItem(table, 0, table.getItemCount());
            item.setData("HistoryPackage", p);
            for(int i = 0; i < p.readings.size(); i++) {
                BeaconHistory.BeaconHistoryHelper h = (BeaconHistory.BeaconHistoryHelper)p.readings.get(i);
                TableTreeItem newSubItem = new TableTreeItem(item, 0);
                newSubItem.setData("HistoryHelper", h);
                setHistoryTableItemText(newSubItem, h);
            }
            setHistoryTableItemText(item, p.highestRssi);
            if(expanded.contains(p)) {
                item.setExpanded(true);
            }
	    }
	    table.setRedraw(true);
	    table.redraw();
	    //System.out.println("-----------");
	}
	
	protected Image getActiveIcon() {
	    if(activeIcon == null) {
	        activeIcon = new Image(Display.getCurrent(), 10, 10);
	        GC gc = new GC(activeIcon);
	        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
	        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
	        gc.fillOval(0, 0, 7, 7);
	        gc.dispose();
	    }
	    return activeIcon;
	}
	
	protected Image getInactiveIcon() {
	    if(inactiveIcon == null) {
	        inactiveIcon = new Image(Display.getCurrent(), 10, 10);
	        GC gc = new GC(inactiveIcon);
	        gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
	        gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
	        gc.fillOval(0, 0, 7, 7);
	        gc.dispose();
	    }
	    return inactiveIcon;
	}
    
	protected void updateSeenLabel() {
		// update the number of APs seen
		String numAPs = "Active Beacons:  " + active.size() + "    Total Seen:  " + 
			history.size() + "    Total New: " + numNew;
		seenLabel.setText(numAPs);
	}
	
	protected Class minimumBeaconClass() { return BeaconReading.class; }
    
    
}

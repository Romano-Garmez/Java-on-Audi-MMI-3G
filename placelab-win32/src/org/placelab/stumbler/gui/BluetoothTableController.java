package org.placelab.stumbler.gui;

import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.placelab.core.BluetoothReading;
import org.placelab.mapper.Mapper;
import org.placelab.util.StringUtil;

public class BluetoothTableController extends BeaconTableController {

    public BluetoothTableController(Font labelFont, Font tableFont, Composite parent, Mapper mapper,
    		long activeThreshold) {
        super("Bluetooth", labelFont, tableFont, parent, mapper, activeThreshold);
    }
    
    protected void buildColumns() {
        buildColumns(new String[] {"Name", "ID", "New?", "Lat", "Lon", "Major Device Class",
                "Minor Device Class", "Service Classes"});
        cols[0].setWidth(20);
        cols[1].setWidth(120);
        cols[2].setWidth(100);
        for(int i = 3; i < 6; i++) {
        	cols[i].setWidth(45);
        }
        for(int i = 6; i < 10; i++) {
        	cols[i].setWidth(110);
        }
    }
    
    
    protected void setHistoryTableItemText(TableTreeItem item, BeaconHistory.BeaconHistoryHelper h) {
        super.setHistoryTableItemText(item, h);
    	BeaconHistory.BeaconHistoryHelper w = (BeaconHistory.BeaconHistoryHelper)h;
        BluetoothReading br = (BluetoothReading)w.reading;
        item.setText(1, br.getHumanReadableName());
        item.setText(2, br.getId());
        item.setText(3, w.isNew(mapper) + "");
        item.setText(4, w.gpsLL.getLatitudeAsString());
        item.setText(5, w.gpsLL.getLongitudeAsString());
        item.setText(6, br.getMajorDeviceClass() + "");
        item.setText(7, br.getMinorDeviceClass() + "");
        item.setText(8, StringUtil.join(br.getServiceClassesList(), ','));
        item.setText(9, humanReadableTime(w.timestamp));
    }
    
    protected Class minimumBeaconClass() { return BluetoothReading.class; }
    
}

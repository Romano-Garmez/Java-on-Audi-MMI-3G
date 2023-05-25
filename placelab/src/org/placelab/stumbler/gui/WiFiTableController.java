package org.placelab.stumbler.gui;

import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.placelab.core.WiFiReading;
import org.placelab.mapper.Mapper;


public class WiFiTableController extends BeaconTableController {

    public WiFiTableController(Font labelFont, Font tableFont, Composite parent, Mapper mapper,
    		long activeThreshold) {
        super("Wifi", labelFont, tableFont, parent, mapper, activeThreshold);
    }
    
    protected void buildColumns() {
        buildColumns(new String[] {"SSID", "MAC Address", "RSSI", "New?", "Lat", "Lon", "WEP?", "fixed?"});
        cols[0].setWidth(20);
        cols[1].setWidth(120);
        cols[2].setWidth(120);
        for(int i = 3; i < 8; i++) {
            cols[i].setWidth(45);
        }
        cols[9].setWidth(100);
    }
    
	
	/* TODO: When we do the attribute-value coding for measurements, just do this all dynamically rather
     * than hard-coding it like this.
     */
    protected void setHistoryTableItemText(TableTreeItem item, BeaconHistory.BeaconHistoryHelper h) {
        super.setHistoryTableItemText(item, h);
        BeaconHistory.BeaconHistoryHelper w = (BeaconHistory.BeaconHistoryHelper)h;
        WiFiReading wr = (WiFiReading)w.reading;
        item.setText(1, wr.getSsid());
        item.setText(2, wr.getId());
        item.setText(3, wr.getRssi() + "");
        item.setText(4, w.isNew(mapper) + "");
        item.setText(5, w.gpsLL.getLatitudeAsString());
        item.setText(6, w.gpsLL.getLongitudeAsString());
        item.setText(7, wr.getWepEnabled() + "");
        item.setText(8, wr.getIsInfrastructure() + "");
        item.setText(9, humanReadableTime(w.timestamp));
    }

    protected Class minimumBeaconClass() { return WiFiReading.class; }

}

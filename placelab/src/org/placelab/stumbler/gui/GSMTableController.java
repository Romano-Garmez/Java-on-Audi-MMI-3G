
package org.placelab.stumbler.gui;

import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.placelab.mapper.Mapper;
import org.placelab.midp.GSMReading;

public class GSMTableController extends BeaconTableController {

    public GSMTableController(Font labelFont, Font tableFont, Composite parent, Mapper mapper,
    		long activeThreshold) {
        super("GSM", labelFont, tableFont, parent, mapper, activeThreshold);
    }
    
    protected void buildColumns() {
        buildColumns(new String[] {"Network Name", "ID", "Signal", "New?", "Lat", "Lon"});
        cols[0].setWidth(20);
        cols[1].setWidth(160);
        cols[2].setWidth(120);
        cols[3].setWidth(60);
        for(int i = 4; i < 6; i++) {
            cols[i].setWidth(45);
        }
        cols[7].setWidth(100);
    }
    
    protected void setHistoryTableItemText(TableTreeItem item, 
    		BeaconHistory.BeaconHistoryHelper h) {
    	super.setHistoryTableItemText(item, h);
    	BeaconHistory.BeaconHistoryHelper g = (BeaconHistory.BeaconHistoryHelper)h;
    	GSMReading gr = (GSMReading)g.reading;
    	item.setText(1, gr.getHumanReadableName());
    	item.setText(2, gr.getId());
    	item.setText(3, "" + gr.getNormalizedSignalStrength());
    	item.setText(4, g.isNew(mapper) + "");
    	item.setText(5, g.gpsLL.getLatitudeAsString());
    	item.setText(6, g.gpsLL.getLongitudeAsString());
    	item.setText(7, humanReadableTime(g.timestamp));
    }
	
    protected Class minimumBeaconClass() { return GSMReading.class; }
}

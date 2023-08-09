/*
 */
package org.placelab.midp.stumbler;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStore;

import org.placelab.collections.LinkedList;
import org.placelab.midp.EventLogger;
import org.placelab.midp.UIComponent;
import org.placelab.util.StringUtil;
import org.placelab.util.TimeUtil;

/**
 * Part of the phone stumbler package displaying all stumble log files
 * to be viewed by the user
 */
public class RMSList extends List implements CommandListener, UIComponent {
	LinkedList recordArray;
	Command showEntries = new Command("Show Entries", Command.ITEM, 1);
	Command deleteLog = new Command("Delete Log", Command.ITEM, 1);
	Command deleteAll = new Command("Delete All", Command.ITEM, 1);
	Command backCommand = new Command("Back", Command.BACK, 1);
	Display display;
	UIComponent backUI;
	RMSRecordCanvas rrc;
	
	
	public RMSList(Display display, UIComponent back) {
		super("Logs", List.IMPLICIT);
		addCommand(showEntries);
		addCommand(deleteLog);
		addCommand(deleteAll);
		addCommand(backCommand);
		setCommandListener(this);

		this.display = display;
		this.backUI=back;
	}

	public void showUI(UIComponent from) {
		this.deleteAll();
		recordArray = new LinkedList();

		String[] records = RecordStore.listRecordStores();
		for (int i = 0; records != null && i < records.length; i++) {
			if (!records[i].startsWith("phoneStumbler-"))
				continue;
			
			recordArray.add(records[i]);
			String[] recordDisplay = StringUtil.split(records[i], '-');
			if (recordDisplay.length == 3) {
				try {
					long time = Long.parseLong(recordDisplay[2]);
					append(
							TimeUtil.getDateAndTime(time), null);
				} catch (NumberFormatException nfe) { //for older log formats
					append(records[i], null);
				}
			} else {
				append(records[i], null);
			}
		}
		if(display != null) display.setCurrent(this);
	}

	public String getRecordName(int index) {
		if (recordArray == null)
			return null;
		try {
		    return (String)recordArray.get(index);
		} catch(IndexOutOfBoundsException ioobe) {
		    EventLogger.logError("RMSList.getRecordName: index out of bounds");
		    return null;
		}
	}

	public void commandAction(Command c, Displayable d) {
		if (c == showEntries || c == List.SELECT_COMMAND) {
			int index = this.getSelectedIndex();
			if (index == -1) {
				Alert alert = new Alert("Exception", "No Record Selected",
						null, AlertType.ALARM);
				alert.setTimeout(Alert.FOREVER);
				display.setCurrent(alert, this);
			} else {
				String recordName = this.getRecordName(index);
				rrc = new RMSRecordCanvas(recordName,display,this);
				rrc.showUI(this);
			}
		} else if (c == deleteLog) {
			int index = this.getSelectedIndex();
			if (index == -1) {
				Alert alert = new Alert("Exception", "No Record Selected",
						null, AlertType.ALARM);
				alert.setTimeout(Alert.FOREVER);
				display.setCurrent(alert, this);
			} else {
				String recordName = this.getRecordName(index);
				try {
					RecordStore.deleteRecordStore(recordName);
					this.showUI(null);
				} catch (Exception e) {
					e.printStackTrace();
					Alert alert = new Alert("Exception", "Unable to Delete",
							null, AlertType.ALARM);
					alert.setTimeout(Alert.FOREVER);
					display.setCurrent(alert, this);
				}				
			}
		} else if (c == deleteAll) {
		    try {
				String[] records = RecordStore.listRecordStores();
				for (int i = 0; records != null && i < records.length; i++) {
					if (!records[i].startsWith("phoneStumbler-"))
						continue;
					
						RecordStore.deleteRecordStore(records[i]);
				}
	
				this.showUI(null);
			} catch (Exception e) {
				e.printStackTrace();
				Alert alert = new Alert("Exception", "Unable to Delete",
						null, AlertType.ALARM);
				alert.setTimeout(Alert.FOREVER);
				display.setCurrent(alert, this);
			}

		} else if(c == backCommand) {
		    if(backUI != null) backUI.showUI(this);
		}
	} 
}


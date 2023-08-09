/*
 * Created on Jun 23, 2004
 */
package org.placelab.midp;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;


/**
 * 
 */
public class PlacelabUtilitiesMidlet extends MIDlet implements CommandListener, UIComponent {
	
	private Form f;
	
	static final String CS_EVENT = "Event Log Utilities";
	static final String CS_STORAGE = "Show Storage";
	List displayList;
	Command exitC, selectC;
	
	public PlacelabUtilitiesMidlet() {
		displayList = new List("Place Lab Utilities", List.IMPLICIT);

		displayList.append(CS_EVENT, null);
		displayList.append(CS_STORAGE, null);
		
		selectC = new Command("Select", Command.OK,1);
		exitC = new Command("Exit", Command.EXIT,1);
		displayList.addCommand(selectC);
		displayList.addCommand(exitC);
		displayList.setCommandListener(this);
	}
	
	public void startApp() {
		showUI(null);
	}

	public void showUI(UIComponent from) {
		Display.getDisplay(this).setCurrent(displayList);
	}

	public void pauseApp() {
	    destroyApp(false);
	}
	
	public void destroyApp(boolean condition) {
	}
	
	
	public void commandAction(Command c, Displayable s) {
		if (c==selectC || c==List.SELECT_COMMAND) {
			String str = displayList.getString(displayList.getSelectedIndex());
			if(str == CS_EVENT) {
				EventLogger.getEventLoggerUI(Display.getDisplay(this),this).showUI(this);
			} else if(str == CS_STORAGE) {
				try {
					// seed the storage...
					Storage.getBooleanPref(Storage.PREFS_EMULATOR,false);

					Storage.getUI(Display.getDisplay(this),this).showUI(this);
				} catch(Exception e) {
					EventLogger.logError("PlacelabUtilities: show storage: " + e);
				}
			}
		} else if (c==exitC) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

}
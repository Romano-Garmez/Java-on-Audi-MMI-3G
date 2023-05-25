/*
 * Created on 23-Jul-2004
 *
 */
package org.placelab.midp.stumbler;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.FixedTwoDCoordinate;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.midp.EventLogger;
import org.placelab.midp.GSMReading;
import org.placelab.midp.RMSMapper;
import org.placelab.midp.UIComponent;
import org.placelab.util.FixedPointLong;
import org.placelab.util.TimeUtil;


/**
 * This is the phone stumbler midlet which is the entry point for stumbling, analyzing
 * stumble logs, and downloading/uploading map data.
 *
 */
public class StumblerMidlet extends MIDlet implements CommandListener,
	StumblerListener, UIComponent {

	private PhoneStumblerManager manager;
	private RMSMapper map;
	
    private RMSStumbleWriter rmsWriter;
    private Display display;
    private Form mainForm;

	public StringBuffer stb;

	private RMSMapLoader mapLoader = null;
	private RMSList rmsList = null; 
	private StumblerForm stumblerForm = null;
	private StumbleUploadClient client = null;
	private Form clientForm = null;
	private boolean initialized = false;

	Command startStumblingCommand = new Command("Start Stumbling",Command.ITEM,1);
	Command listLogsCommand = new Command("List Stumble Logs",Command.ITEM,1);
	Command uploadLogsCommand =new Command("Upload Stumble Logs",Command.ITEM,1); 
	Command mapperCommand = new Command("Show/Get Map Data",Command.ITEM,1); 
	//Command eventCommand = new Command("Show Event Logs",Command.ITEM,1);
	Command exitCommand = new Command("Exit",Command.EXIT,1);
	

	

	
	public StumblerMidlet() throws IOException {
        stb = new StringBuffer();
        map = new RMSMapper();
    }
        
		
		
    public void start() {
        manager.start();
    }
    
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		if(manager != null) manager.close();
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);		

		mainForm = new Form("Phone Stumbler");

		mainForm.addCommand(startStumblingCommand);
		mainForm.addCommand(listLogsCommand);
		mainForm.addCommand(uploadLogsCommand);
		mainForm.addCommand(mapperCommand);
		//mainForm.addCommand(eventCommand);
		mainForm.addCommand(exitCommand);
		mainForm.setCommandListener(this);
		
		showUI(this);
	}
	
	public void showUI(UIComponent from) {
		mainForm.deleteAll();
		mainForm.append("Status:\n");
        mainForm.append(" " + map.numBeacons() + " beacons mapped\n");
        String[] records = RecordStore.listRecordStores();
        int numLogs=0;
		for (int i = 0; records != null && i < records.length; i++) {
			if (records[i].startsWith("phoneStumbler-")) numLogs++;
		}
		mainForm.append(" " + numLogs + " stumble logs.\n");
		mainForm.append("Instructions:\n");
		mainForm.append(" 1. Start native Placelab server\n");
		mainForm.append(" 2. To stumble, have a Bluetooth GPS device nearby " +
						"and select 'Options->" + startStumblingCommand.getLabel() + "'\n");
		mainForm.append(" 3. To upload stumble logs, run BluetoothServer on a " +
						"computer and use 'Options->" + uploadLogsCommand.getLabel() + "'\n");
		mainForm.append(" 4. To get beacon maps, use 'Options->" + mapperCommand.getLabel() + "'\n");
		
		display.setCurrent(mainForm);
     
	}

	protected void pauseApp() {
	}
	
	protected void startStumbling() {
try {
		if(!initialized) {
	        rmsWriter = new RMSStumbleWriter();
	        stumblerForm = new StumblerForm();
	        initialized = true;
			manager = new PhoneStumblerManager();
			manager.addAllListener(this);
			manager.addListener(rmsWriter);
		} 
		
		rmsWriter.open();		
		manager.open();
		manager.start();
		stumblerForm.start();
} catch(Exception e) {
	EventLogger.logError(e);
}
	}
	
	UIComponent eventUI;
	public void commandAction(Command c, Displayable d) {
		if(d==mainForm) {
		    if(c == startStumblingCommand) {
		        startStumbling();			
		        display.setCurrent(stumblerForm);
		    } else if(c == listLogsCommand) {
		        if(rmsList == null) rmsList = new RMSList(display,this);		
		        rmsList.showUI(this);
		        return;
		    } else if(c == uploadLogsCommand) {
		        if(client == null) client = new StumbleUploadClient(display,this);
		        client.showUI(this);
		    } else if(c == mapperCommand) {
		        if(mapLoader==null) mapLoader = new RMSMapLoader(display,this,map);
		        mapLoader.showUI(this);
//		    } else if(c == eventCommand) {
//		    	EventLogger.logError("Stumbler: opening Event Logger UI");
//				eventUI = EventLogger.getEventLoggerUI(display,this);
//				if(eventUI != null) eventUI.showUI(this);
		    } else if(c == exitCommand) {
		        try {
		            destroyApp(true);
		        }catch(Throwable t) {
		        }
		        notifyDestroyed();
		    }
		    
		} else if (d == clientForm) {
		    if(c.getLabel().equals("Back")) {
		        showUI(this);
		    } 
		    
		} else if (d == stumblerForm) {
		    if(c.getLabel().equals("Stop")) {
				manager.stop();
				manager.close();
				rmsWriter.shutdown();
				stumblerForm.stop();
		    } else if(c.getLabel().equals("Start")) {
		        startStumbling();
		        display.setCurrent(stumblerForm);
		    } else if(c.getLabel().equals("Back")) {
		        showUI(this);
		    }
		}
	}

	public void gotMeasurement(Measurement[] meas) {
	    if(stumblerForm == null) return;
    	for(int i=0;i<meas.length;i++) {
    		Measurement m = meas[i];
    		if(m == null) continue;
    		   		
    		if(m instanceof BeaconMeasurement && ((BeaconMeasurement)m).numberOfReadings() > 0 && 
    				((BeaconMeasurement)m).getReading(0) instanceof GSMReading) {
    			stumblerForm.setGSMEntry(m.getTimestamp(), (GSMReading) ((BeaconMeasurement)m).getReading(0));
    		} else if (m instanceof GPSMeasurement) {
    			stumblerForm.setGPSEntry(m.getTimestamp(), (GPSMeasurement) m);
    		}
        }
		stumblerForm.updateGPSStatus();
	}


	public static void alert(String str) {
		//if (instance != null) {
		//	instance.waitForAlert(str);
		//}
	    EventLogger.logError("StumblerMidlet: " + str);
	}
	
	private class StumblerForm extends Form {
		StringBuffer sb;
		boolean running;
		Command stopCommand;
		Command startCommand;
		Command backCommand;
		
		StringItem latestAction;
		StringItem currentGSM; //= "No Measurements";
		StringItem currentGPS; // = "No Measurements";
		StringItem gpsStatus;
		

		int gsmNum;
		int gpsNum;
		
		public StumblerForm() {
			super("");

			startCommand = new Command("Start",Command.ITEM,1);
			stopCommand = new Command("Stop",Command.ITEM,1);
			backCommand = new Command("Back",Command.BACK,2); 
			addCommand(backCommand);
			addCommand(startCommand);
			setCommandListener(StumblerMidlet.this);
			latestAction = new StringItem("","");
			currentGSM = new StringItem("GSM Readings","No reading yet\n");
			currentGPS = new StringItem("GPS Readings","No reading yet\n");
			gpsStatus = new StringItem("","");
			this.append(latestAction);
			this.append(new Spacer(1000,10));
			this.append(currentGSM);
			this.append(new Spacer(1000,10));
			this.append(currentGPS);
			this.append(new Spacer(1000,1));
			this.append(gpsStatus);			
		}
		
		String prevId = "";
		public void setGSMEntry(long ts, GSMReading gsmr) {
			//if(prevId == null || prevId.equals(gsmr.getId())) {
			//    gsmNum--;
			//}
			gsmNum++;

			currentGSM.setText("At " + TimeUtil.getTime(ts) + " GMT (total " +gsmNum + ")\n" + gsmr.toShortString());
		}

		final static long GPS_VELOCITY_TIMEOUT = 2000;
		GPSMeasurement lastgpsm;
		String movementString;
		public void setGPSEntry(long ts, GPSMeasurement gpsm) {
			gpsNum++;
			if(lastgpsm == null || lastgpsm.getPosition().isNull()) {
			    movementString="";
			    lastgpsm = gpsm;
			} else {
			    if(!gpsm.getPosition().isNull()) {
				    long timediff = gpsm.getTimestamp() - lastgpsm.getTimestamp();
				    if(timediff > GPS_VELOCITY_TIMEOUT) {
				        try {
				            long distanceFlong = ((FixedTwoDCoordinate)gpsm.getPosition()).distanceFromFlong((FixedTwoDCoordinate)lastgpsm.getPosition());
				            movementString=" (Speed " + FixedPointLong.flongToString(1000L * distanceFlong / timediff,1) + " m/s)";
				        } catch(Throwable fple) {
				            movementString=" ERROR: Cannot determine speed: " + fple;
				        }
				        lastgpsm=gpsm;
				    }
			    }
			}
			currentGPS.setText("At " + TimeUtil.getTime(ts) + " GMT (total " + gpsNum + ")\n" + gpsm.toShortString()+ movementString); 
		}
		
		public void updateGPSStatus() {
		    gpsStatus.setText("Status: " + manager.getGPSStatus());
		}
/*
		public void updateDisplay() {
			String text = latestAction+"\n"+
						"Recent GSM Measurement ("+gsmNum+")\n"+currentGSM+"\n\n"+
						"Recent GPS Measurement ("+gpsNum+")\n"+
						"Status: "+manager.getGPSStatus()+
						"\n"+currentGPS;
			si.setText(text);
		}
*/		
		public void start() {
			if(!running) {
				gsmNum = 0;
				gpsNum = 0;
				String ts = TimeUtil.getTime(System.currentTimeMillis());				
				latestAction.setText("STARTED at time: "+ts+"\n");
				running = true;
			}
			removeCommand(backCommand);
			removeCommand(startCommand);
			addCommand(stopCommand);
			updateGPSStatus();
		}

		public void stop() {
			if(running) {
				String ts = TimeUtil.getTime(System.currentTimeMillis());				
				latestAction.setText("STOPPED at time: "+ts+"\n");				
				running = false;
			}
			removeCommand(stopCommand);
			addCommand(startCommand);
			addCommand(backCommand);
			updateGPSStatus();
		}


	}
}

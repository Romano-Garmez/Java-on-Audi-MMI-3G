/*
 * Created on Sep 16, 2004
 *
 */
package org.placelab.midp.debug;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.midp.BluetoothClient;
import org.placelab.midp.GSMSpotter;
import org.placelab.midp.UIComponent;
import org.placelab.midp.server.BluetoothService;
import org.placelab.spotter.BluetoothGPSSpotter;
import org.placelab.spotter.HardcodedBluetoothGPSSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;


public class GPSTimeMidlet extends MIDlet implements CommandListener,SpotterListener,UIComponent {
	protected Display display;
	protected Form mainForm;
	protected StringItem gsmStatus;
	protected StringItem gpsStatus;
	protected StringItem total;
	protected StringItem errors;
	
	protected Command startCommand;
	protected Command stopCommand;
	protected Command uploadCommand;
	
	protected StringBuffer buffer;
	protected int gsmCount, gpsCount, count;
	
	protected RecordStore store;

	protected Timer timer;
	
	protected GSMSpotter gsmSpotter;
	protected BluetoothGPSSpotter gpsSpotter;
	
	protected GPSMeasurement lastGPS;
	
	public final static byte GPS_LOCK = 2;
	public final static byte GPS_ON = 1;
	public final static byte GPS_OFF = 0;
	
	public GPSTimeMidlet() {
		gsmSpotter = new GSMSpotter(15000);
		gpsSpotter = new HardcodedBluetoothGPSSpotter("000A3A007942");
		gpsSpotter.addListener(this);

		buffer = new StringBuffer();		
	}
	
	private class CheckTask extends TimerTask {
		public void run() {
			boolean gsm = false;
			byte gps = GPS_OFF;

			try {
				try {
//					log("opening gsm");
					gsmSpotter.open();
					BeaconMeasurement m = (BeaconMeasurement)gsmSpotter.getMeasurement();
					gsm = m.numberOfReadings() > 0 && m.getReading(0).isValid();	
					gsmSpotter.close();
//					log("closing gsm");
				} catch (SpotterException e) {
					logError(e);
				}
				
				if (lastGPS != null && System.currentTimeMillis() - lastGPS.getTimestamp() < 10000) {
					if (lastGPS.isValid())
						gps = GPS_LOCK;
					else
						gps = GPS_ON;
				}
			} catch (Throwable e) {
				logError(e);
			}

			log(gsm, gps);
		}
	}
	
	private class UploadClient extends BluetoothClient {

		public UploadClient(Display display, UIComponent from) {
			super(display, from);
		}

		public String getName() {
			return "Upload Data";
		}

		public byte getServiceType() {
			return BluetoothService.GPS_TIME_SERVICE;
		}

		public void handleConnection(DataInputStream in, DataOutputStream out) {
			setStatus("Connected.");
			
			String[] stores = RecordStore.listRecordStores();
			
			for (int i = 0; i < stores.length; i++) {
				if (stores[i].startsWith("gpstime-")) {
					updateStatus("Uploading " + stores[i]);
					try {
						RecordStore store = RecordStore.openRecordStore(stores[i], false);
						RecordEnumeration re = store.enumerateRecords(null,	null, false);
						updateStatus("Enumerating " + re.numRecords());
						out.writeInt(re.numRecords());
						out.writeLong(Long.parseLong(stores[i].substring(8)));
						while (re.hasNextElement()) {
							out.write(re.nextRecord());
						}
						store.closeRecordStore();
						updateStatus("Deleteing " + stores[i]);
						RecordStore.deleteRecordStore(stores[i]);
					} catch (Throwable e) {
						updateStatus("error: " + e);
					}
				}
				updateStatus("Done.");
			}
			
			try {
				out.writeInt(0);
				out.flush();
			} catch (IOException e) {}
		}
	}
	
	protected void resetCount() {
		gsmCount = gpsCount = 0;
		// this is because count is automatically incremented in updateCount
		count = -1;
		updateCount(false, GPS_OFF);
	}
	
	protected void updateCount(boolean gsm, byte gps) {
		if (gsm) gsmCount++;
		if (gps == GPS_LOCK) gpsCount++;
		count++;
		
		StringBuffer status = new StringBuffer(gpsCount + " ");
		switch (gps) {
			case GPS_LOCK: status.append("(lock)"); break;
			case GPS_ON: status.append("(on)"); break;
			case GPS_OFF: status.append("(off)"); break;
		}
		
		gpsStatus.setText(status.toString());
		gsmStatus.setText(gsmCount + (gsm ? " (on)" : " (off)"));
		total.setText(count + (timer != null ? " (on)" : " (off)"));
	}
	
	protected void log(String s) {
		buffer.append(s);
		buffer.append("\n");
		errors.setText(buffer.toString());
	}
	
	protected void logError(Throwable e) {
		log("Exception: " + e);
	}
	
	protected void log(boolean gsm, byte gps) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(out);
			
			//data.writeLong(System.currentTimeMillis());
			data.writeBoolean(gsm);
			data.writeByte(gps);
			
			byte[] bytes = out.toByteArray();
			
			store.addRecord(bytes, 0, bytes.length);
			
			updateCount(gsm, gps);
		} catch (Exception e) {
			logError(e);
		}
	}
	
	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);

		gsmStatus = new StringItem("GSM Status:", "0 (off)");
		gpsStatus = new StringItem("GPS Status:", "0 (off)");
		total = new StringItem("Total:", "0 (off)");
		errors = new StringItem("Log:", "");
		
		startCommand = new Command("Start Scan", Command.ITEM, 0);
		stopCommand = new Command("Stop Scan", Command.ITEM, 0);
		uploadCommand = new Command("Upload", Command.ITEM, 1);
		
		mainForm = new Form("GPS/GSM Time");
		mainForm.append(gpsStatus);
		mainForm.append(gsmStatus);
		mainForm.append(total);
		mainForm.append(new Spacer(1000,10));
		mainForm.append(errors);
		
		mainForm.addCommand(startCommand);
		mainForm.addCommand(uploadCommand);
		mainForm.setCommandListener(this);

		display.setCurrent(mainForm);
	}

	protected void pauseApp() {}
	protected void destroyApp(boolean b) throws MIDletStateChangeException {}

	public void commandAction(Command c, Displayable d) {
		if (c == startCommand) {
			try {
				resetCount();

				store = RecordStore.openRecordStore("gpstime-" + System.currentTimeMillis(), true);

				timer = new Timer();
				timer.scheduleAtFixedRate(new CheckTask(), 0, 60000);
				
				gpsSpotter.open();
				log("open gps (" + gpsSpotter.getState() + ")");
				gpsSpotter.startScanning();
				
				mainForm.addCommand(stopCommand);
				mainForm.removeCommand(startCommand);
				mainForm.removeCommand(uploadCommand);
			} catch (Throwable t) {
				logError(t);
			}
			
			log("Started.");
		} else if (c == stopCommand) {
			try {
				store.closeRecordStore();
				store = null;
			} catch (RecordStoreException e) {}
			
			timer.cancel();
			timer = null;
			
			gpsSpotter.stopScanning();
			gpsSpotter.close();
			
			mainForm.removeCommand(stopCommand);
			mainForm.addCommand(startCommand);
			mainForm.addCommand(uploadCommand);
			
			log("Stopped.");
		} else if (c == uploadCommand) {
			UploadClient client = new UploadClient(display, this);
			client.showUI(this);
		}
	}
	
	public void gotMeasurement(Spotter sender, Measurement m) {
		lastGPS = (GPSMeasurement)m;
	}


	public void spotterExceptionThrown(Spotter sender, SpotterException ex) {
		logError(ex);
	}

	public void showUI(UIComponent from) {
		display.setCurrent(mainForm);
	}
}

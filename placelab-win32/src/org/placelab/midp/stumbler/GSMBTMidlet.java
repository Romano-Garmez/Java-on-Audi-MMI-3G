
package org.placelab.midp.stumbler;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.midp.GSMReading;
import org.placelab.midp.GSMSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SpotterListener;


public class GSMBTMidlet extends MIDlet implements CommandListener, SpotterListener {
		private Display display;
		Form displayForm;
		
		StringItem coverageSi;
		StringItem curTowerSi;
		StringItem bluetoothSi;
		StringItem elapsedSi;

		String currentID = null;
		String currentName = null;
		String currentSS = null;
		
		long startTime;
		
		public static long PERIOD = 1000;
		
		long connCount=0;
		long updates=0;
		long seen=0;
		
		RecordStore rms;
		
		public static String STORE_NAME = "gsmbt";
		
		private String lastElapsed = "";
		private String lastRunTime = "";
		
		
		public static int BUF_SIZE = 60;
		BeaconMeasurement buffer[];
		int bufIdx = 0;
		boolean wrapped = false;
		
		public GSMBTMidlet() {
			buffer = new BeaconMeasurement[BUF_SIZE];
			display = Display.getDisplay(this);
		}
		
		// Start the MIDlet by creating two command buttons
		public void startApp() {
			displayForm = new Form("GSMBT");
			coverageSi = new StringItem("Initializing...","");
			curTowerSi = new StringItem("Current Tower","");
			bluetoothSi = new StringItem("Bluetooth Requests","");
			elapsedSi = new StringItem("Elapsed Time","");
			
			coverageSi.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
			curTowerSi.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
			bluetoothSi.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
			elapsedSi.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
			
			displayForm.append(coverageSi);
			displayForm.addCommand(new Command("Exit", Command.EXIT,1));
			displayForm.setCommandListener(this);
			display.setCurrent(displayForm);
			
			startTime = System.currentTimeMillis();
			long lastDelta = -1;
			try {
				rms = RecordStore.openRecordStore(STORE_NAME, true,
						RecordStore.AUTHMODE_ANY, true);
				try {
					RecordEnumeration re = rms.enumerateRecords(null,null,true);
					while (re.hasNextElement()) {
						int id = re.nextRecordId();
						byte ba[] = rms.getRecord(id);
						if (ba != null) {
								lastDelta = Long.parseLong(new String(ba));
							}
						rms.deleteRecord(id);
					}
				} catch (Exception ex) {;}
			} catch (Exception ex) {
				rms = null;
			}
			if (lastDelta > 0) {
				lastRunTime = " (Previously " + msToStr(lastDelta) + ")";
			}
			
			GSMSpotter spotter = new GSMSpotter(PERIOD);
			try {
			    spotter.open();
			} catch(SpotterException e) {
				coverageSi.setLabel("Error");
				coverageSi.setText("Cannot open link with native spotter");
			}
			spotter.addListener(this);
	        spotter.startScanning();
	        startServer();
		}

		public void pauseApp() {
		}

		public void destroyApp(boolean unconditional) {
		}

		// Respond to commands.
		public void commandAction(Command c, Displayable s) {
			if (c.getCommandType() == Command.EXIT) {
				System.exit(1);
			}
		}
		
		public synchronized void updateStrings() {
			int x;
			if (wrapped) {
				x = buffer.length;
			} else {
				x = bufIdx;
			}
			
			String curTow = "(none)";
			String covPct = "";
			if (currentID != null) {
				curTow = currentID + " (" + currentSS + "%)";
			}
			if (updates > 0) {
				long l = (seen * 100) / updates;
				covPct = "  " + l + "%";
			}

			long now = System.currentTimeMillis();
			long delta = now-startTime;
			String elapsed = msToStr(delta) + lastRunTime;
			
			if (!elapsed.equals(lastElapsed)) {
			  lastElapsed = elapsed;
			  // lets write this out
			  if (rms != null) {
				try {
					RecordEnumeration re = rms.enumerateRecords(null,null,true);
					while (re.hasNextElement()) {
						int id = re.nextRecordId();
						rms.deleteRecord(id);
					}
				} catch (Exception ex) {;}
			  	try {
			  		String ds = "" + delta;
			  		rms.addRecord(ds.getBytes(),0,ds.length());
			  	} catch (Exception ex) {;}
			  }
			}

			coverageSi.setText(seen + "/" + updates + covPct);
			curTowerSi.setText(curTow);
			bluetoothSi.setText("" + connCount); 
			elapsedSi.setText("" + elapsed);
		}
		

		public void gotMeasurement(Spotter sender, Measurement m) {
			try {
				BeaconMeasurement bm = (BeaconMeasurement) m;
				currentID = null;
				if (bm.numberOfReadings() > 0) {
					GSMReading gr = (GSMReading)bm.getReading(0);
					try {
						if (!gr.isNull()) {
							currentName = gr.getHumanReadableName();
							currentID = gr.getUniqueId();
							currentSS = "" + gr.getNormalizedSignalStrength();
							synchronized (buffer) {
								buffer[bufIdx++] = bm;
								if (bufIdx >= buffer.length) {
									bufIdx = 0;
									wrapped = true;
								}
							}
							seen++;							
						}
					} catch (Exception ex) {
						;
					}
				}
				updates++;
				
				if (updates == 9) {
					coverageSi.setLabel("Coverage");
					displayForm.append(curTowerSi);
					displayForm.append(bluetoothSi);
					displayForm.append(elapsedSi);
				}
				if ((updates>8) && (updates%3) ==  0) updateStrings();
			} catch (Exception ex) {
				coverageSi.setLabel("Error");
				coverageSi.setText("Error happened during getMeasurement: "
						+ ex.getMessage());
			}
		}

		public static String msToStr(long ms) {
			ms /= 1000;
			if (ms < 60) {
				return ms + " sec";
			}
			ms /= 60;
			if (ms < 60) {
				return ms + " min";
			}
			ms /= 60;
			return ms + " hr";
			
		}

		



		public void spotterExceptionThrown(Spotter sender, SpotterException ex) {
			coverageSi.setLabel("Error");
			coverageSi.setText("Error happened: " + ex.getMessage());
		}
		
		public void startServer() {		
			LocalDevice local = null;
			StreamConnectionNotifier server = null;
			StreamConnection conn = null;
			
			String connectionURL = "btspp://localhost:393a84ee7cd111d89527000bdb544cb1;"
				+ "authenticate=false;encrypt=false;name=MyService";
			try {
				local = LocalDevice.getLocalDevice();
			} catch (Exception e) {
				coverageSi.setLabel("Error");
				coverageSi.setText("Cant get local: " + e.getMessage());
			}
			
			try {
				server = (StreamConnectionNotifier) Connector.open(connectionURL);
			} catch (IOException e1) {
				coverageSi.setLabel("Error");
				coverageSi.setText("Badness " + e1.getClass().getName());
			}
			
			coverageSi.setText("Connect To: "
					+ local.getRecord(server).getConnectionURL(
							ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
							false));

			while (true) {
				try {
					conn = server.acceptAndOpen();
					DataOutputStream os = conn.openDataOutputStream();
					synchronized (buffer) {
						int end = bufIdx;
						if (wrapped) {
							end = buffer.length;
						}
						// write the length
						os.writeInt(end);
						// now write the string
						long time = System.currentTimeMillis();
						for (int i=0; i<end; i++) {
							BeaconMeasurement bm = buffer[i];
							String timeDelta = "" + (time - bm.getTimestamp());
							GSMReading gr = (GSMReading)bm.getReading(0);
							String currentName = gr.getHumanReadableName();
							String currentID = gr.getUniqueId();
							String currentSS = "" + gr.getNormalizedSignalStrength();
							String biggie = timeDelta + "|" +
											currentName + "|" +
											currentID + "|" +
											currentSS;
							os.writeUTF(biggie);
						}
						bufIdx = 0;
						wrapped = false;
					}
					os.flush();
					conn.close();
					connCount++;
				} catch (IOException e2) {
					bufIdx = 0;
					wrapped = false;
					coverageSi.setLabel("Error");
					coverageSi.setText("Badness " + e2.getClass().getName());
				}
			}
		}
		
		
	}
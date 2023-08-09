package org.placelab.midp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.StringItem;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.placelab.collections.HashMap;
import org.placelab.midp.server.BluetoothService;
import org.placelab.util.StringUtil;

/** Event logging helper class.  In particular, use EventLogger.logError(String s) to log errors in your midlets for debugging */
public class EventLogger { 
	public static int JAVA_ERROR = 0;
	public static String STORE_NAME = "placelab_eventlog";
	public static EventLogger actInst = null;
	
	private RecordStore rms;
	private HashMap eventTypes;
	
	protected EventLogger() throws RecordStoreException {
		rms = RecordStore.openRecordStore(STORE_NAME, true,
				RecordStore.AUTHMODE_ANY, true);
		eventTypes = new HashMap();
		Vector v = new Vector();
		
		eventTypes.put(new Integer(0),"Java Error");
	}
	
	public static void setEventString(int eventNumber, String eventString) {
		try {
			EventLogger el = getInst();
			el.eventTypes.put(new Integer(eventNumber),eventString);
		} catch(Exception e) {
		}
	}
	
	private static EventLogger getInst() throws RecordStoreException {
		if (actInst == null) {
			actInst = new EventLogger();
		}
		return actInst;
	}
	
	public static void add(int eventType,String message) {
		try {
			long time = System.currentTimeMillis();
			String s = time + "|" + eventType + "|" + message;
			// this outputs a line in the emulator
			System.err.println("Event Logged: " + s);
			byte[] ba = s.getBytes();
			getInst().rms.addRecord(ba, 0, ba.length);
		} catch (Exception ex) {
			// No idea what to do. Can't print it or log it. Gotta swallow it...
		}
	}
	
	public static void logError(Exception ex) {
		add(JAVA_ERROR, ex.getClass().getName() + " : "
				+ ex.getMessage());
	}
	
	public static void logError(String s) {
	    add(JAVA_ERROR, s);
	}
	
	public static void deleteAll() throws RecordStoreException {
		EventLogger inst = getInst();
		if(inst != null) inst.deleteAll_impl();
	}
	
	public static void deleteType(int type) throws RecordStoreException {
		EventLogger inst = getInst();
		if(inst != null) inst.deleteType_impl(type);
	}
	
	private void deleteAll_impl() throws RecordStoreException {
		rms.closeRecordStore();
		RecordStore.deleteRecordStore(STORE_NAME);
		rms = RecordStore.openRecordStore(STORE_NAME, true,
				RecordStore.AUTHMODE_ANY, true);

	}
	
	private class TypeRecordFilter implements RecordFilter {
		int type;
		public TypeRecordFilter(int type) {
			this.type=type;
		}
		public boolean matches(byte[] bytes) {
			String s = new String(bytes);
			String[] sarr = StringUtil.split(s, '|');
			if(sarr == null || sarr.length < 2) return false;
			return Integer.parseInt(sarr[1]) == type;
		}
	}
	
	private void deleteType_impl(int type) {
		try {
			RecordEnumeration enum = rms.enumerateRecords(new TypeRecordFilter(type), null, false);
			
			while (enum.hasNextElement()) {
				rms.deleteRecord(enum.nextRecordId());
			}
		} catch (RecordStoreException e) {
		}
	}

	
	public static EventLoggerUI getEventLoggerUI(Display display, UIComponent back) {
		try {
			EventLoggerUI elui = getInst().new EventLoggerUI(display,back);
			return elui;
		} catch(RecordStoreException rse) {
		}
		return null;
	}
	
	public class EventLoggerUI implements CommandListener,UIComponent {
		Display display;
		List displayList;
		Command backC,selectC;
		
		static final String CS_SHOW = "Show Event Log";
		static final String CS_UPLOAD = "Upload Event Log";
		static final String CS_CLEAR = "Clear Event Log";
		
		public EventLoggerUI(Display display, UIComponent back) {
			this.display=display;
			
			displayList = new List("Event Log Utilities", List.IMPLICIT);

			displayList.append(CS_SHOW, null);
			displayList.append(CS_UPLOAD, null);
			displayList.append(CS_CLEAR, null);
			selectC = new Command("Select", Command.OK,1);
			backC = new Command("Back", Command.BACK,1);
			displayList.addCommand(selectC);
			displayList.addCommand(backC);
			displayList.setCommandListener(this);
			this.back=back;
		}
		
//		
		// Respond to commands. 
		public void commandAction(Command c, Displayable s) {
			if (c==selectC || c==List.SELECT_COMMAND) {
				try {
					String str = displayList.getString(displayList.getSelectedIndex());
					if(str == CS_SHOW) {
						EventLogListUI loglist = new EventLogListUI(display,this);
						loglist.showUI(this);
					} else if(str == CS_UPLOAD) {
						EventLogUploadUI logupload = new EventLogUploadUI(display, this);
						logupload.showUI(this);
					} else if(str == CS_CLEAR) {
						EventLogger.deleteAll();
						showUI(this);
					}
				} catch(NullPointerException npe) {
				} catch(IndexOutOfBoundsException iobe) {
				} catch(RecordStoreException rse) {
				}
			} else if (c==backC) {
				if(back != null) back.showUI(this);
			}
		}
		
		UIComponent back;
		
		public void showUI(UIComponent from) {
			String rmsnum = "unknown";
			try {
				rmsnum = ""+ rms.getNumRecords();
			} catch(RecordStoreException rse) {
			}
			displayList.setTitle("Event Log (" + rmsnum + " events in log)");
			display.setCurrent(displayList);
		}
	}
	
	public class EventLogListUI implements UIComponent, CommandListener {
		Form browseForm = null;
		Display display = null;
		UIComponent back = null;
		RecordEnumeration re = null;
		int curRec = 0;
		int showAtMost = 5;
		Command next10Command = null;
		Command backCommand = null;
		//Command uploadCommand = null;
		Command clearCommand = null;
		
		public EventLogListUI(Display display, UIComponent back) {
			this.display=display;
			this.back=back;
		}
		
		public void setContent() throws RecordStoreException {
			if ((re == null) || (!re.hasNextElement())) {
				re = null;
				return;
			}
			int min = curRec;
			int max = min + showAtMost;
			if (max > rms.getNumRecords()) {
				max = rms.getNumRecords(); //recordCount;
			}
			browseForm.deleteAll();
			for (int i = min; i < max; i++) {
				if ((re != null) && re.hasNextElement()) {
					try {
						curRec++;
						String s = new String(re.nextRecord());
						String[] sarr = StringUtil.split(s, '|');
						int idx = Integer.parseInt(sarr[1]);
						String stat = null;
						try {
							long l = (new Date().getTime() - Long.parseLong(sarr[0]))/1000;
							long lo = l;
							long sec = l%60;
							l /= 60;
							long mn = l%60;
							l /= 60;
							stat = "-" + l + ":" + mn + ":" + sec;
						} catch (Exception ex) {;}//stat = ex.getClass().getName() + ex.getMessage();}	
						if ((idx < 0) || (idx >= eventTypes.size() || eventTypes.get(new Integer(idx)) == null)) {
						
							stat = stat + "  Unknown";
						} else {
							stat = stat + "  " + eventTypes.get(new Integer(idx));//[idx];
						}
						String msg = sarr[2];
						browseForm.append(new StringItem(stat, msg + "\n"));
						browseForm.append(new Spacer(0, 4));
					} catch (Exception ex) {
						;
					}
				} else {
				    browseForm.append(new StringItem("","End of records"));
				}
			}
			if(max == 0)  {
			    browseForm.append(new StringItem("","No records"));
			}
	
			setTitle(min, max);
			if ((re == null) || (!re.hasNextElement())) {
				re = null;
				browseForm.removeCommand(next10Command);
			}
		}
		public void setTitle(int min, int max) {
			String numrecs = "unknown";
			try {
				numrecs = ""+ rms.getNumRecords();
			} catch(RecordStoreException rse) {
			}
			browseForm.setTitle("Event Log (" + (min + 1) + "-" + max + " of "
					+ numrecs + ")");
		}
		public void commandAction(Command c, Displayable s) {
			if (c.getLabel().startsWith("Next")) {
				try {
					setContent();
				} catch (RecordStoreException e) {
					e.printStackTrace();
				}
			}
			if (c.getLabel().startsWith("Back")) {
				back.showUI(this);//display.setCurrent(back);
			}

		}
		
		public void showUI(UIComponent from) {
			if (browseForm == null) {
				browseForm = new Form("Event Log");
				next10Command = new Command("Next " + showAtMost, Command.SCREEN, 1);
				backCommand = new Command("Back", Command.BACK, 1);
				//uploadCommand = new Command("Upload", Command.ITEM, 1);
				clearCommand = new Command("Clear", Command.ITEM,1);
				//browseForm.addCommand(uploadCommand);
			} else {
				browseForm.deleteAll();
				browseForm.removeCommand(next10Command);
				browseForm.removeCommand(backCommand);
			}
			curRec = 0;
			try {
			if (rms.getNumRecords() > showAtMost) {
				browseForm.addCommand(next10Command);
			}
			} catch(RecordStoreException rse) {
				
			}
			if (back != null) {
				browseForm.addCommand(backCommand);
			}
			browseForm.setCommandListener(this);
			display.setCurrent(browseForm);
			try {
				re = rms.enumerateRecords(null, null, false);
				if(!re.hasNextElement()) {
				    browseForm.append(new StringItem("","No records"));
				    browseForm.setTitle("Event Log");
				} else {
				    setContent();
				}
			} catch(RecordStoreException rse) {
				browseForm.append(new StringItem("Error", ""+rse));
			}
		}
	}
	
	private class EventLogUploadUI extends BluetoothClient {

		public EventLogUploadUI(Display display, UIComponent from) {
			super(display, from);
		}

		public String getName() {
			return "Event Log Uploader";
		}

		public byte getServiceType() {
			return BluetoothService.ACTIVITY_UPLOAD_SERVICE;
		}

		public void handleConnection(DataInputStream in, DataOutputStream out) {
			setStatus("Connected to Event Log Downloader.");
			try {
				RecordEnumeration enum = rms.enumerateRecords(null, null, false);
				updateStatus("Uploading " + enum.numRecords() + " records...");
//				out.writeUTF(UberStore.getUserName());
//				out.writeUTF(UberStore.getUserNumber());
				out.writeInt(enum.numRecords());
				while (enum.hasNextElement()) {
					out.writeUTF(new String(enum.nextRecord()));
				}
				
				out.flush();
				out.close();
				
//				updateStatus("Deleting records...");
//				deleteAll();
			} catch (Exception e) {
				updateStatus("Error: " + e);
			}
			
			updateStatus("Finished Uploading.");
		}
	}

}
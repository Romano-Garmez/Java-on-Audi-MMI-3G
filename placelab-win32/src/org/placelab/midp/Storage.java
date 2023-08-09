package org.placelab.midp;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;


/** General storage infrastructure for placelab on midp phones, using the phones' RMS storage **/
public class Storage {
	public static final String STORE_NAME = "placelab-storage";

	public static final String TYPE_PREFS = "prefs";
	public static final String PREFS_EMULATOR="emulator";
	
	public static final String TRUE = "t";
	public static final String FALSE ="f";
		
	public RecordStore rms;
	
	
	protected Storage() throws RecordStoreException {
		rms = RecordStore.openRecordStore(STORE_NAME, true,
				RecordStore.AUTHMODE_ANY, true);
	}
	
	public static int getRecordCount() {
		try {
			return getInst().rms.getNumRecords();
		} catch (RecordStoreException e) {
			return -1;
		}
	}

	static Storage inst;
	protected static Storage getInst() throws RecordStoreException {
		if (inst == null) {
			inst = new Storage();
		}
		return inst;
	}

	
	/** Add an entry into the uberstore.
	 * Example: add(Preferences.TYPE_PREFS,Preferences.PREFS_EMULATOR,"t"); */
	public static void add(String type, String key, String value) throws RecordStoreException {
		String s = type + "|" + key + "|" + value;
		byte[] ba = s.getBytes();
		getInst().rms.addRecord(ba,0,ba.length);
	}
	
	/** returns the record corresponding to this type and key.
	 * Use getValue() to get the value.
	 */
	public static String get(String type, String key) {
		RecordEnumeration re = getByType(type);
		while (re.hasNextElement()) {
			try {
				String s = new String(re.nextRecord());
				if (s == null) {
					return null;
				}
				String thekey = getKey(s);
				if(thekey != null && thekey.equals(key)) {
					return s;
				}
			} catch (Exception ex) {
				EventLogger.logError(ex);
			}
		}
		return null;
	}

	/** delete a record.  You must pass the record string in, not the value */
	public static boolean delete(String record) throws RecordStoreException {
		Storage us = getInst();
		RecordEnumeration re = us.rms.enumerateRecords(null,null,false);
		while (re.hasNextElement()) {
			try {
				int idx = re.nextRecordId();
				String s = new String(us.rms.getRecord(idx));
				if (s.equals(record)) {
					us.rms.deleteRecord(idx);
					return true;
				}
			} catch (Exception ex) {
				;
			}
		}
		return false;
	}
	
	private static RecordEnumeration getRecordsForReplace() throws RecordStoreException {
		Storage us = getInst();
		return us.rms.enumerateRecords(null,null,false);
	}
	
	private static boolean replaceInnerLoop(int index, String oldRecord, String type, String key, String val) throws RecordStoreException {
		Storage us = getInst();
		String s = new String(us.rms.getRecord(index));
		if (s.equals(oldRecord)) {
			String newStr = type + "|" + key + "|" + val;
			us.rms.setRecord(index,newStr.getBytes(),0,newStr.length());
			return true;
		}
		return false;
	}
	
	public static boolean replace(String oldRecord, String type, String key, String val) throws RecordStoreException {
		Storage us = getInst();
		RecordEnumeration re = getRecordsForReplace();
		while (re.hasNextElement()) {
			try {
				int idx = re.nextRecordId();
				if (replaceInnerLoop(idx,oldRecord,type,key,val)) {
					return true;
				}
			} catch (Exception ex) {
				throw new RecordStoreException("WRAPPED EXCEPTION:"+ex.getClass().getName()+":"+ex.getMessage());
			}
		}
		return false;
	}
	
	/** get all records for a type */
	public static RecordEnumeration getByType(final String type) {
		try {
			return getInst().rms.enumerateRecords(new RecordFilter() {
				public boolean matches(byte[] ba) {
					String s = new String(ba);
					return s.startsWith(type + "|");
				}}
					,null,false);
		} catch (Exception ex) {
			// this really shouldn't happen, honest
			return null;
		}
	}
	
	public static void deleteAll() throws RecordStoreException {
		Storage us = getInst();
		us.rms.closeRecordStore();
		RecordStore.deleteRecordStore(STORE_NAME);
		us.rms = RecordStore.openRecordStore(STORE_NAME, true,
				RecordStore.AUTHMODE_ANY, true);
	}
	

	
	/** Get the type for a record */
	public static String getType(String record) {
		// I tried to make this mildly efficient
	    if(record == null) return null;
		int idx = record.indexOf('|');
		if (idx > 0) {
			return record.substring(0,idx);
		} else {
			return "error";
		}
	}
	
	/** Get the key for a record */
	public static String getKey(String record) {
		// I tried to make this mildly efficient
	    if(record == null) return null;
		int idx = record.indexOf('|');
		if (idx > 0) {
			int idx2 = record.indexOf('|',idx+1);
			if (idx2 > 0) {
			return record.substring(idx+1,idx2);
			} else {
				return "error";
			}
		} else {
			return "error";
		}
	}
	
	/** Get the value for a record */
	public static String getValue(String record) {
		// I tried to make this mildly efficient
	    if(record == null) return null;
		int idx = record.lastIndexOf('|');
		if (idx > 0) {
			return record.substring(idx+1,record.length());
		} else {
			return "error";
		}
	}
		
	/** toggles a boolean of TYPE_PREFS */
	public static void toggleBooleanPref(String name, boolean defaultBeforeToggle) throws RecordStoreException {
		boolean oldVal=getBooleanPref(name,defaultBeforeToggle);
		String newVal;
		if (oldVal) {
			newVal=FALSE;
		} else {
			newVal=TRUE;
		}
		String rec=get(TYPE_PREFS,name);
		replace(rec,TYPE_PREFS,name,newVal);
	}
	
	/** gets a boolean preference from TYPE_PREFS */
	public static boolean getBooleanPref(String name, boolean defaultValue) {
		String me = get(TYPE_PREFS,name);
		if (me == null) {
			me = defaultValue ? "t" : "f";
			try {
				add(TYPE_PREFS,name,me);
			} catch (Exception ex) {;}
		} 
		return isTrue(getValue(me));
	}
	
	private static boolean isTrue(String trueOrFalse) {
		if (trueOrFalse==null) {
			return false;
		}
		if (trueOrFalse.startsWith("t")==true) {
			return true;
		}
		if (trueOrFalse.startsWith("T")==true) {
			return true;
		}
		return false;
	}

	public static UIComponent getUI(Display display, UIComponent back) throws RecordStoreException {
		return getInst().new StorageUI(display, back);
	}
	

	public class StorageUI implements CommandListener, UIComponent {
		// In case they decide to display and edit
		List browseForm;
		Display display = null;
		UIComponent back = null;
		Command next10Command = null;
		Command backCommand = null;
		Command editCommand = null;
		Command removeCommand = null;
		int curRec = 0;
		int showAtMost = 5;
		RecordEnumeration re = null;
		
		protected StorageUI(Display display, UIComponent back) {
			this.display=display;
			this.back=back;
			browseForm = new List("Storage",List.IMPLICIT);
			browseForm.setFitPolicy(List.TEXT_WRAP_ON);
			backCommand = new Command("Back", Command.BACK, 1);
			editCommand = new Command("Edit", Command.SCREEN, 1);
			removeCommand = new Command("Remove", Command.SCREEN, 1);
			next10Command = new Command("Next " + showAtMost, Command.SCREEN, 1);
		}
		
		public void showUI(UIComponent from) {
			browseForm.deleteAll();
			browseForm.removeCommand(next10Command);
			browseForm.removeCommand(backCommand);

			curRec = 0;
			try {
				if (rms.getNumRecords() > showAtMost) {
					browseForm.addCommand(next10Command);
				}
				if (back != null) {
					browseForm.addCommand(backCommand);
				}
				browseForm.setCommandListener(this);
				display.setCurrent(browseForm);
				re = rms.enumerateRecords(null,null,false);
				setContent();
			} catch(Exception e) {
				EventLogger.logError("StorageUI.showUI : " + e);
			}
		}
		
		public void setContent() throws  RecordStoreException {
			if ((re == null) ||(! re.hasNextElement())) {
				return;
			}
			
			int min = curRec;
			int max = min + showAtMost;
			if (max > rms.getNumRecords()) {
				max = rms.getNumRecords();
			}
			browseForm.deleteAll();
			
			for (int i=min; i<max; i++) {
				if ((re != null) && re.hasNextElement()) {
					try {
						curRec++;
						String s = new String(re.nextRecord());
						browseForm.append(s,null);
					} catch (Exception ex) {;}
				}
			}
			setTitle(min,max);
			if (browseForm.size() > 0) {
				browseForm.addCommand(editCommand);
				browseForm.addCommand(removeCommand);
			}
			if ((re == null) || (!re.hasNextElement())) {
				browseForm.removeCommand(next10Command);
			}
		}
		
		public void setTitle(int min, int max) throws RecordStoreException {
			browseForm.setTitle("Storage (" + (min+1) +"-"+ max +" of " + rms.getNumRecords() +")");
		}
	
		public void commandAction(Command c, Displayable s) {
			if (c == next10Command) {
				try {
					setContent();
				} catch (RecordStoreException e) {
					e.printStackTrace();
				}
			} else if (c == backCommand) {
				back.showUI(this);
			} else if (c == removeCommand) {
				removeFromList(browseForm.getString(browseForm.getSelectedIndex()));
			} else if (c == editCommand || c == List.SELECT_COMMAND) {
				edit(browseForm.getString(browseForm.getSelectedIndex()));
			}
		}
		
		public void removeFromList(String displayText) {
			try {
				delete(displayText);
				showUI(null);
			} catch (RecordStoreException e) {
			}
		}
	
		public StorageEditorUI seui;
		public void edit(String displayText) {
			if(seui == null) seui = new StorageEditorUI(display,this);
			seui.setRecord(displayText);
			seui.showUI(this);			
		}
	}
	

	public class StorageEditorUI implements CommandListener, UIComponent {
		String original = null;
		Form f = null;
		TextField typeTF,keyTF,valTF;
		Display display;
		UIComponent back;
		Command saveC,discardC;
		
		public StorageEditorUI(Display display, UIComponent back) {
			this.display = display;
			this.back = back;
			f = new Form("Storage Editor");
			saveC = new Command("Save", Command.OK,1);
			discardC=new Command("Discard", Command.CANCEL, 1);
			f.addCommand(saveC);
			f.addCommand(discardC);
			f.setCommandListener(this);
		}
		
		public void setRecord(String record) {
			original = record;
		}
		
		public void showUI(UIComponent from) {
			f.deleteAll();
			typeTF = new TextField("Type", getType(original), 32, TextField.ANY);
			keyTF = new TextField("Key", getKey(original), 32, TextField.ANY);
			valTF = new TextField("Value", getValue(original), 256, TextField.ANY);
			f.append(typeTF);
			f.append(keyTF);
			f.append(valTF);
			display.setCurrent(f);
		}
		
		public void commandAction(Command c, Displayable s) {
			if (c == discardC) {
				if(back != null) back.showUI(this);
			} else if (c == saveC) {
				try {
					replace(original,typeTF.getString(),keyTF.getString(),valTF.getString());
				} catch (RecordStoreException e) {
					EventLogger.logError(e);
				}
				if(back != null) back.showUI(this);
			}
		}
	}

}

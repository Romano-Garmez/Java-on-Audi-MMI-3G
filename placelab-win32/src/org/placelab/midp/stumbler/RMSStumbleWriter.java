package org.placelab.midp.stumbler;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import org.placelab.core.Measurement;
import org.placelab.core.ShutdownListener;

/**
 * Writes measurement data to the RMS
 *
 */
public class RMSStumbleWriter implements StumblerListener,
		ShutdownListener {

	
	private boolean shutdown = false;
	private RecordStore rms;
	private String rmsName;
	
	public RMSStumbleWriter() {
		rms = null;
	}
	
	public void open() {
		boolean create = true;
		if(rmsName != null) {
			create = false;
		} else {
	    	String[] records = RecordStore.listRecordStores();
	    	if(records == null)
	    		rmsName = "phoneStumbler-0-"+System.currentTimeMillis();
	    	else 
	    		rmsName = "phoneStumbler-"+records.length+"-"+System.currentTimeMillis();
		}
    	try {
    		rms = RecordStore.openRecordStore(rmsName,create);  		
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
		
	public RecordEnumeration getRecords() {
		RecordEnumeration re = null;
		try {
			re = rms.enumerateRecords(null,null,false);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public void shutdown() {
		try {
			if(rms != null)
				rms.closeRecordStore();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void gotMeasurement(Measurement[] m) {
		if (shutdown)
			return;
		synchronized (this) {
			for (int i=0;i<m.length;i++) {
				if(m[i] == null) continue;
				
//				String entry = m[i].toLogString();
//				//write to the RMS here
//				byte entryBytes[] = entry.getBytes();
				byte entryBytes[] = m[i].toCompressedBytes();
				try {
					rms.addRecord(entryBytes, 0, entryBytes.length);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

}
package org.placelab.midp.stumbler;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.microedition.lcdui.Display;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import org.placelab.midp.BluetoothClient;
import org.placelab.midp.UIComponent;
import org.placelab.midp.server.BluetoothService;

/**
 * Upload client for sending stumble logs to the desktop proxy server
 * through bluetooth. These logs are then uploaded to the main placelab
 * database by the proxy server.
 * 
 */
public class StumbleUploadClient extends BluetoothClient {

	public StumbleUploadClient(Display d, UIComponent c) {
	    super(d,c);
	}
	
	public String getName() {
		return "Log Uploader";
	}

	public byte getServiceType() {
		return BluetoothService.LOG_UPLOAD_SERVICE;
	}

	/**
	 * Handle the connection between the phone client and the server
	 */
	public void handleConnection(DataInputStream in, DataOutputStream out) {
		setStatus("Connected to StumbleUploadProxy");
		String[] records = RecordStore.listRecordStores();
		try {
			out.write("#PlacelabStumbler Log Version 2\r\n".getBytes());
			for (int i = 0; i < records.length; i++) {
				if (!records[i].startsWith("phoneStumbler-"))
					continue;

				updateStatus("Opening log: " + records[i]);
				RecordStore rms = RecordStore.openRecordStore(records[i], false);
				if (rms == null)
					continue;

				//read data and send it out
				RecordEnumeration re = rms.enumerateRecords(null, null, false);
				
				updateStatus("Enumerating " + re.numRecords() + " records");

				//decompressing bytes happens on the server end
				while (re.hasNextElement()) {
					byte[] b = re.nextRecord();
					out.write(b);
					byte[] newline = { '\r','\n' };
					out.write(newline);
					out.flush();
				}

				//delete the record store
				rms.closeRecordStore();
				
			}
			out.write("DONE\r\n".getBytes());
			out.flush();
			for(int i=0;i<records.length;i++) {
				if (!records[i].startsWith("phoneStumbler-"))
					continue;
				updateStatus("Deleting " + records[i]);
				RecordStore.deleteRecordStore(records[i]);
			}
			updateStatus("Done.");
		} catch (Exception e) {
			updateStatus("Exception: " + e.getMessage());
		}
	}

}

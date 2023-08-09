/*
 * Created on Sep 24, 2004
 *
 */
package org.placelab.midp;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.placelab.midp.server.BluetoothService;
import org.placelab.util.StringUtil;


public class BluetoothConsole {
	
	private static BluetoothConsole instance = null;
	
	private DataOutputStream out;
	
	private BluetoothConsole() throws RecordStoreException, IOException {
		RecordStore store = RecordStore.openRecordStore(BluetoothClient.RECORDSTORE_NAME, false);
		RecordEnumeration re = store.enumerateRecords(null, null, false);
		while (re.hasNextElement()) {
			try {
				String[] server = StringUtil.split(new String(re.nextRecord()));
				StreamConnection conn = (StreamConnection)Connector.open(server[1]);
				out = conn.openDataOutputStream();
				out.writeByte(BluetoothService.CONSOLE_SERVICE);
				out.flush();
				break;
			} catch (IOException e) {
				out = null;
				continue;
			}
		}
		store.closeRecordStore();
		if (out == null) throw new IOException("No Placelab Server Found");
	}
	
	private static BluetoothConsole getInstance() {
		try {
			if (instance == null) instance = new BluetoothConsole();
		} catch (Exception e) {
			instance = null;
		}
		
		return instance;
	}
	
	public static void println(String line) {
		BluetoothConsole console = getInstance();

		if (console == null) return;
		
		console.send(line);
	}
	
	private void send(String line) {
		try {
			out.writeUTF(line);
			out.flush();
		} catch (IOException e) {}
	}
}

/*
 * Created on Oct 4, 2004
 *
 */
package org.placelab.midp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.placelab.core.PlacelabProperties;


public class PlaceService implements BluetoothService {

	public String getName() {
		return "Place Downloader";
	}

	public byte getServiceType() {
		return BluetoothService.PLACE_UPLOAD_SERVICE;
	}

	public void newClient(DataInputStream in, DataOutputStream out) {
		File f = null;
		
		for (int i = 0;; i++) {
			f = new File(PlacelabProperties.get("placelab.datadir") + File.separator + 
					"places-" + i + ".txt");
			
			if (!f.exists())
				break;
		}

		
		try {
			System.out.println("Saving to " + f.getName());
			PrintStream ps = new PrintStream(new FileOutputStream(f));

			String name = in.readUTF();
			String number = in.readUTF();
			ps.println(name + "|" + number);
			
			int count = in.readInt();
			while (count-- > 0) {
				ps.println(in.readUTF());
			}
			
			ps.close();
		} catch (IOException e) {
			System.err.println("Failed: " + e);
			e.printStackTrace();
		}
		
	}

}

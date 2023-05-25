/*
 * Created on Sep 17, 2004
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


public class GPSTimeService implements BluetoothService {

	public String getName() {
		return "GPS Time Downloader";
	}

	public byte getServiceType() {
		return BluetoothService.GPS_TIME_SERVICE;
	}

	public void newClient(DataInputStream in, DataOutputStream out) {
		System.out.println("Receiving gpstime log...");
		
		File f = null;
		
		for (int i = 0;; i++) {
			f = new File(PlacelabProperties.get("placelab.datadir") + File.separator + 
					"gpstime-log-" + i + ".txt");
			if (!f.exists())
				break;
		}
		try {
			PrintStream ps = new PrintStream(new FileOutputStream(f));

			for (;;) {
				int records = in.readInt();
				long time = in.readLong();
				if (records == 0)
					break;
				ps.println("start " + time);
				for (int i = 0; i < records; i++) {
					boolean gsm = in.readBoolean();
					byte gps = in.readByte();
					
					// translate readings
					gps -= 1;
					
					ps.print(gsm ? "1" : "0");
					if (gps < 0)
						ps.println("-");
					else
						ps.println(gps);
				}

			}
			
			ps.close();
		} catch (IOException e) {
		}
	}

}

package org.placelab.midp.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.placelab.mapper.Beacon;
import org.placelab.mapper.MapUtils;
import org.placelab.mapper.TempMapper;

/**
 * Proxy service that communicates with the map loader on the phone
 * via bluetooth. The proxy downloads the latest map data from
 * http://www.placelab.org/data/do-retrieve.php and sends the data
 * to the phone.
 */
public class MapLoaderProxy implements BluetoothService {

	public String getName() {
		return "Map Loader Proxy";
	}

	public byte getServiceType() {
		return BluetoothService.MAP_LOADER_SERVICE;
	}

	public void newClient(DataInputStream in, DataOutputStream out) {
		long start = 0, end = 0;
		int numSent = 0;
		InputStream placelabIn = MapUtils.getHttpStream("http://www.placelab.org/data/do-retrieve.php?style=new&type=gsm,bt&extra=false");
		if(placelabIn == null) {
		    System.err.println("Cannot open URL");
		    return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(placelabIn));
		TempMapper map = new TempMapper();
		while (true) {
			try {
				String str = br.readLine();
				if (str == null) {
					out.writeBytes("DONE\n");
					out.flush();
					System.out.println("Sent " + numSent + " beacons.");
					System.out.println("Waiting for disconnect...");
					in.read();
					end = System.currentTimeMillis();
					System.out.println("elapsed = " + ((end - start) / 1000));
					break;
				}
				
				Beacon b = null;
				
				try {
				    b = map.createBeacon(str);
				} catch(Exception e) {
				    System.err.println("Exception with beacon: " + e);
				}
    			if (b == null) {
    				System.err.println("Error on the following line: " + str);
    				continue;
    			}
    			
				out.writeBytes(str + "\n");
				
				if (start == 0)
					start = System.currentTimeMillis();
					
				numSent++;
				if (numSent % 10 == 0) {
					out.flush();
					System.out.println("Sending " + numSent);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				break;
			}
		}
		
		try {
			br.close();
			placelabIn.close();
		} catch (IOException e) {}
	}

}

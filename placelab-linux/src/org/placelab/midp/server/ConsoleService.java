/*
 * Created on Sep 24, 2004
 *
 */
package org.placelab.midp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class ConsoleService implements BluetoothService {

	public String getName() {
		return "Bluetooth Console";
	}

	public byte getServiceType() {
		return BluetoothService.CONSOLE_SERVICE;
	}

	public void newClient(DataInputStream in, DataOutputStream out) {
		while (true) {
			try {
				String line = in.readUTF();
				System.out.println("console: " + line);
			} catch (IOException e) {
				System.out.println("console died");
				//e.printStackTrace();
				break;
			}
		}
	}

}

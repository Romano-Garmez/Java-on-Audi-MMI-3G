/*
 * Created on 27-Jul-2004
 *
 */
package org.placelab.midp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 * Bluetooth server that handles incoming connection requests from 
 * a phone client and starts the appropriate service to handle the connection.
 * 
 * Note: If the server is not receiving the connection request, one possibility
 * is the bluetooth request is being intercepted by another process on the machine,
 * possibly mRouter if that is set to connect to a phone via bluetooth. Turn this
 * feature off before communicating with this server.
 */

public class BluetoothServer {
	public static final UUID uuid =	new UUID("27012f0c68af4fbf8dbe6bbaf7ab651c", false);
	
	private StreamConnectionNotifier notifier = null;
	
	private Hashtable services;
	
	private class ServiceThread implements Runnable {
		private BluetoothService service;
		private StreamConnection stream;
		private DataInputStream in;
		private DataOutputStream out;
		
		public ServiceThread(BluetoothService service, StreamConnection stream) throws IOException {
			this.service = service;
			this.stream = stream;
			this.in = stream.openDataInputStream();
			this.out = stream.openDataOutputStream();
		}
		
		public void run() {
			System.out.println("Servicing " + service.getName() + " request...");
			service.newClient(in, out);
			System.out.println("Done servicing " + service.getName());
			try {
				in.close();
				out.close();
				stream.close();
			} catch (IOException e) {
				System.err.println("Error closing the stream.");
			}
		}
	}
	
	public BluetoothServer() throws IOException {
		services = new Hashtable();
		notifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:"
						+ uuid
						+ ";name=PlacelabServer;authorize=false;authenticate=false;encrypt=false");

		System.out.println("initialized bluetooth service.");
	}
	
	public void addService(BluetoothService service) {
		Byte type = new Byte(service.getServiceType());
		
		if (services.containsKey(type)) {
			System.err.println("Trying to add two services of the same type");
		} else {
			System.out.println("Registering " + service.getName());
			services.put(type, service);
		}	
	}
	
	public void listen() {
		while (true) {
			try {
				System.out.println("listening...");
				StreamConnection conn = notifier.acceptAndOpen();
				System.out.println("connecting...");
				
				DataInputStream in = conn.openDataInputStream();
				byte command = in.readByte();
				BluetoothService service = (BluetoothService)services.get(new Byte(command));
				if (service == null) {
					System.err.println("Unrecognized command (" + command + ")");
					conn.close();
				} else {
					System.out.println("Spawning new thread...");
					new Thread(new ServiceThread(service, conn)).start();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {
		try {
			BluetoothServer bs = new BluetoothServer();
			bs.addService(new StumbleUploadProxy());
			bs.addService(new MapLoaderProxy());
			bs.addService(new EventService());
			bs.addService(new GPSTimeService());
			bs.addService(new ESMQuestionService());
			bs.addService(new ConsoleService());
			bs.addService(new PlaceService());
			bs.listen();
		} catch (IOException e) {
			System.err.println("Puked: " + e);
			e.printStackTrace();
		}
	}
}

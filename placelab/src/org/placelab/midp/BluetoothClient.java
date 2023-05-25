package org.placelab.midp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.placelab.spotter.BluetoothUtil;
import org.placelab.util.StringUtil;


/**
 * 
 */
public abstract class BluetoothClient implements DiscoveryListener, CommandListener, UIComponent {
	
	public static final UUID uuid =	new UUID("27012f0c68af4fbf8dbe6bbaf7ab651c", false);
	
	public final static String RECORDSTORE_NAME = "placelab-servers";
	
	public abstract String getName();
	public abstract byte getServiceType();
	public abstract void handleConnection(DataInputStream in, DataOutputStream out);
	
	private Hashtable servers;
	private StringBuffer statusBuffer;
	
	private Form form;
	private ChoiceGroup group;
	private StringItem statusItem;
	private Command connectCommand;
	private Command searchCommand;
	private Command backCommand;

	private Display display;
	
	public BluetoothClient(Display display, UIComponent from)  {
		servers = new Hashtable();
		statusBuffer = new StringBuffer("Ready.\n");
		
		group = new ChoiceGroup("Hosts:", Choice.POPUP);
		statusItem = new StringItem("", statusBuffer.toString());
		
		connectCommand = new Command("Connect", Command.ITEM, 1);
		searchCommand = new Command("More Devices", Command.ITEM, 1);
		
		backCommand = new Command("Back", Command.BACK, 1);
		
		form = new Form(getName());
		form.append(group);
		form.append(statusItem);
		form.addCommand(connectCommand);
		form.addCommand(searchCommand);
		form.addCommand(backCommand);
		form.setCommandListener(this);
		this.display = display;
		populateServers();
	    back=from;
	}
	
	UIComponent back;
	
	public void showUI(UIComponent from) {
		display.setCurrent(form);
	}

	private void recordServer(String name, String url, boolean replace) {
		try {
			String full = name + "\t" + url;
			//updateStatus("record " + full);
			byte[] bytes = full.getBytes();
			int stored = -1;
			RecordStore store = RecordStore.openRecordStore(RECORDSTORE_NAME, true);
			for (int i = 1; i <= store.getNumRecords(); i++) {
				String s = new String(store.getRecord(i));
				String[] entry = StringUtil.split(s);
			
				if (entry[0].equals(name)) {
					if (replace) store.setRecord(i, bytes, 0, bytes.length);
					stored = i;
					break;
				}
			}
			
			if (stored == -1) {
				store.addRecord(bytes, 0, bytes.length);
			}
			
			//updateStatus("recorded");
			store.closeRecordStore();
		} catch (RecordStoreException e) {
			//updateStatus("failed record = " + e);
		}
	}
	
	private void addServer(String name, String url) {
		if (!servers.containsKey(name))
			group.append(name, null);
		//updateStatus("adding host " + name);

		servers.put(name, url);
	}
	
	private void populateServers() {
		group.deleteAll();
		servers.clear();

		try {
			RecordStore store = RecordStore.openRecordStore(RECORDSTORE_NAME, true);
			//updateStatus("populating with " + store.getNumRecords());
			for (int i = 1; i <= store.getNumRecords(); i++) {
				String s = new String(store.getRecord(i));
				//updateStatus("s = " + s);
				String[] entry = StringUtil.split(s);
				
				addServer(entry[0], entry[1]);
			}
			store.closeRecordStore();
		} catch (RecordStoreException e) {}
	}
	
	protected void updateStatus(String newStatus, boolean newLine) {
		statusBuffer.append(newStatus);
		if (newLine) statusBuffer.append('\n');
		statusItem.setText(statusBuffer.toString());
	}
	
	protected void updateStatus(String newStatus) {
		updateStatus(newStatus, true);
	}
	
	protected void setStatus(String newStatus) {
		statusBuffer.setLength(0);
		updateStatus(newStatus);
	}
	
	private void tryConnect(String url) {
		try {
			setStatus("Connecting (" + url + ")...");
			StreamConnection conn = (StreamConnection)Connector.open(url);
			DataInputStream in = conn.openDataInputStream();
			DataOutputStream out = conn.openDataOutputStream();
			updateStatus("Sending service type...");
			out.write(getServiceType());
			out.flush();
			handleConnection(in, out);
			out.flush();
			out.close();
			in.close();
			conn.close();
		} catch (Throwable e) {
			updateStatus("Transfer failed: " + e);
		}
	}
	
	public void commandAction(Command c, Displayable d) {
		if (c == connectCommand) {
			int index = group.getSelectedIndex();
			if(index >= 0) {
				tryConnect((String)servers.get(group.getString(index)));
			} else {
				setStatus("Invalid Selection");
			}
		} else if (c == searchCommand) {
			startInquiry();
		} else if (c == backCommand) {
		    back.showUI(this);
		}
	}
	
	private void startInquiry() {
		try {
			LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(
					DiscoveryAgent.GIAC, this);
			setStatus("Starting Inquiry");
		} catch (BluetoothStateException e) {
			updateStatus("Failed to start inquiry: " + e);
		}
	}
	
	public void deviceDiscovered(RemoteDevice device, DeviceClass cod) {
		try {
			updateStatus("Discovered " + device.getFriendlyName(false));
			if (cod.getMajorDeviceClass() == BluetoothUtil.COMPUTER_MAJOR_CLASS) {
				String url = "btspp://" + device.getBluetoothAddress() + ":1";
				recordServer(device.getFriendlyName(false), url, false);
			}
		} catch (IOException e) {}
	}

	public void inquiryCompleted(int discType) {
		updateStatus("Discovery Complete");
		populateServers();
	}

	public void servicesDiscovered(int transID, ServiceRecord[] records) {}

	public void serviceSearchCompleted(int transID, int respCode) {}
}

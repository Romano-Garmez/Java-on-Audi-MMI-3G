package org.placelab.midp.stumbler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.midlet.MIDletStateChangeException;

import org.placelab.collections.Iterator;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.BluetoothBeacon;
import org.placelab.mapper.GSMBeacon;
import org.placelab.midp.BluetoothClient;
import org.placelab.midp.RMSMapper;
import org.placelab.midp.UIComponent;
import org.placelab.midp.server.BluetoothService;
import org.placelab.util.StringUtil;

/**
 * Communicates with a PlacelabServer to grab data then loads it into the Mapper
 * Entry point for loading over bluetooth, local resource file, and local test file
 */
public class RMSMapLoader implements CommandListener, UIComponent {
	private RMSMapper map;
	private Display display;
	private UIComponent returnDisplay;
	private Form form;
	private StringItem info;
	private StringBuffer screenBuffer = new StringBuffer();

	private Command mapperInfo = new Command("Show Map Info", Command.ITEM, 1);
	private Command resourceUpdate = new Command("Update (Local)", Command.ITEM, 1);
	private Command proxyUpdate = new Command("Update (Bluetooth)", Command.ITEM, 1);
	private Command testUpdate = new Command("Update (Test)", Command.ITEM, 1);
	private Command deleteAll = new Command("Delete All Maps", Command.ITEM, 1);
	private Command backCommand = new Command("Back",Command.BACK,1);
	
	public static final String RESOURCE_FILENAME = "Beacons.txt";
	public static final String TEST_FILENAME = "TestBeacons.txt";

	private class MapLoaderClient extends BluetoothClient {

		public String extra = "";
		
		public MapLoaderClient(Display d,UIComponent c) {
		    super(d,c);
		}
		
		public String getName() {
			return "Map Loader";
		}

		public byte getServiceType() {
			return BluetoothService.MAP_LOADER_SERVICE;
		}
		
		
//		public String readLine(Reader reader) throws IOException {
//			char[] buffer = new char[64];
//			
//			int index = -1;
//			String contents = extra;
//			while ((index = contents.indexOf('\n')) == -1) {
//				int read = reader.read(buffer);
//				if (read == -1)
//					break;
//				
//				contents += new String(buffer, 0, read);
//			}
//			
//			if (index != -1) {
//				extra = contents.substring(index + 1);
//				contents = contents.substring(0, index);
//			}
//			
//			return contents;
//		}
		
		public void handleConnection(DataInputStream in, DataOutputStream out) {
			setStatus("Connected to MapLoaderProxy");
//			updateStatus("Currently something is happening where this loader never thinks the stream terminates."
//					+ "  So kill the BluetoothServer when it appears no more beacons are loading.");
			Reader reader = new InputStreamReader(in);
			int numAdded = 0;
			map.open();
			map.startBulkPuts();
			updateStatus("Loading ", false);
			while (true) {
				try {
					String line = StringUtil.readLine(reader);

					if (line == null || line.equals("DONE")) {
					    updateStatus("");
						if(line == null) updateStatus("Null line");
						updateStatus("Sending DISCONNECT");
						out.write(42);
						out.flush();
						break;
					}

					// removed sanity check - moved to proxy
//					Beacon b = null;
//					
//					try {
//					    b = map.createBeacon(line);
//					} catch(Exception e) {
//					    updateStatus("Exception creating beacon: " + e);
//					}
//
//        			if (b == null) {
//        				updateStatus("Error on the following line: " + line);
//        				continue;
//        			}
					
        			//map.putBeacon(b.getId(), b);
        			map.putBeaconRaw(line.trim());

        			numAdded++;
       				//updateStatus(".", false);
					if (numAdded % 10 == 0) updateStatus(numAdded + " ", false);
				} catch (Exception e) {
					updateStatus("Error loading: " + e);
					break;
				}
			}
			map.endBulkPuts();
			map.close();
			updateStatus("Imported " + numAdded + " beacons into RMS.");
		}	
	}

	public RMSMapLoader(Display display, UIComponent returnDisplay, RMSMapper map) {
		this.map = map; //map = new RMSMapper();
		this.display = display;
		this.returnDisplay = returnDisplay;
	}

	
	protected void clear() {
		screenBuffer.setLength(0);
		append("", false);
	}
	
	protected void append(String s) {
		append(s, true);
	}
	
	protected void append(String s, boolean newline) {
		screenBuffer.append(s);
		if (newline) screenBuffer.append("\n");
		info.setText(screenBuffer.toString());
	}

	public long start() {
		return System.currentTimeMillis();
	}
	
	public void end(long start) {
		long end = System.currentTimeMillis();
		
		append("Took " + (end - start) + " ms");
	}
	
	public void showInfo() {
		clear();
		append("Collecting Mapper Information...");
		
		int gsm = 0, bt = 0, other = 0, nullpos=0;
		
		map.open();
		long start = start();
		Iterator iter = map.iterator();
		while (iter.hasNext()) {
			Beacon b = (Beacon)iter.next();
			if(b == null) nullpos++;
			if (b instanceof GSMBeacon)
				gsm++;
			else if (b instanceof BluetoothBeacon)
				bt++;
			else
				other++;
			if(b.getPosition() == null || b.getPosition().isNull()) nullpos++;
		}
		end(start);
		
		append("Number of Beacons:");
		append("GSM: " + gsm);
		append("Bluetooth: " + bt);
		append("Other: " + other);
		append("Total: " + (gsm + bt + other));
		append("Nulls: " + nullpos);
		map.close();
	}
	
	public void updateFromResource(String file, boolean printAll) {
		InputStream is = getClass().getResourceAsStream("/" + file);
		if (is == null) {
			waitForAlert("Cannot find resource file " + file);
			return;
		}

		try {
			loadData(is,printAll);
			is.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void updateFromProxy() {
		append("Running MapLoaderClient");
		MapLoaderClient client = new MapLoaderClient(display,this);
		client.showUI(this);
	}
	
	protected void loadData(InputStream is, boolean printAll) {
		append("Loading...", false);
		if(is == null) {
		    append("Loading failed - null InputStream", false);
		    return;
		}
		int numAdded = 0;
		Reader reader = new InputStreamReader(is);
		if(reader == null) {
		    append("Loading failed - null Reader", false);
		    return;
		}
		if(map == null) {
		    append("Loading failed - null map", false);
		    return;
		}
		map.open();
		map.startBulkPuts();
		long start = System.currentTimeMillis();
		while (true) {
			try {
				
				String line = StringUtil.readLine(reader);
				if (line == null) break;
				line = line.trim();
				if(line.length()==0) continue;
				if(line.startsWith("#")) continue;
				
    			Beacon b = map.createBeacon(line);
    			if (b == null) {
    				append("\nError adding beacon " + numAdded + " when parsing \"" + line + "\"");
    				continue;
    			}
    			if(printAll) {
    			    append("\n" + b.toString());
    			}

    			map.putBeaconRaw(line);
   				numAdded++;
   				//append(".", false);
				if (!printAll && numAdded % 10 == 0) append(numAdded + " ",false);

			} catch (IOException e) {
				append("Exception: "+e.getMessage());
			}
		}
		long end = System.currentTimeMillis();
		map.endBulkPuts();
		map.close();

		waitForAlert("Imported " + numAdded + " beacons into RMS");
		append("");
		append("Imported " + numAdded + " beacons into RMS");
		append("Took " + (end - start) + " ms");
	}

	public void showUI(UIComponent from) {
		info = new StringItem("", "");
		
		form = new Form("Map Loader");
		form.append(info);
		commandsShown=false;
		addCommands();
		form.setCommandListener(this);
		display.setCurrent(form);
		append("Map Loader ready.\n" +
				"Use menu to load maps or view map data.\n" +
				"");
	}
	
	protected boolean commandsShown = false;
	protected void removeCommands() {
	    if(commandsShown) {
	        form.removeCommand(mapperInfo);
			form.removeCommand(resourceUpdate);
			form.removeCommand(proxyUpdate);
			form.removeCommand(testUpdate);
			form.removeCommand(deleteAll);
			form.removeCommand(backCommand);
	        commandsShown = false;
	    }
	}
	protected void addCommands() {
	    if(!commandsShown) {
			form.addCommand(mapperInfo);
			form.addCommand(proxyUpdate);
			form.addCommand(resourceUpdate);
			form.addCommand(testUpdate);
			form.addCommand(deleteAll);
			form.addCommand(backCommand);
			commandsShown = true;
	    }
	}


	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {}
	protected void pauseApp() {}

	public void waitForAlert(String str) {
		Alert a = new Alert("Map Loader", str, null, AlertType.INFO);
		display.setCurrent(a,form);
		while (a.isShown());
	}

	public void commandAction(Command c, Displayable d) {
	    if(commandsShown) {
			clear();
			if (c == mapperInfo) {
				removeCommands();
				showInfo();
				addCommands();
			} else if (c == resourceUpdate) {
				removeCommands();
				updateFromResource(RESOURCE_FILENAME,false);
				addCommands();
			} else if (c == proxyUpdate) {
				updateFromProxy();
			} else if (c == testUpdate) {
				removeCommands();
				updateFromResource(TEST_FILENAME,true);	
				addCommands();
			} else if (c == deleteAll) {
			    removeCommands();
				if (map.deleteAll()) {
					append("Deleted All Mappings");
				} else {
					append("Deletion Failed");
				}
				addCommands();
			} else if(c == backCommand) {
			    returnDisplay.showUI(this);
				return;
			} 
	    }
	}

}
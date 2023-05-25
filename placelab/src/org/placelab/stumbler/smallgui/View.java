package org.placelab.stumbler.smallgui;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.placelab.core.GPSMeasurement;
import org.placelab.spotter.BluetoothSpotter;
import org.placelab.spotter.NMEAGPSSpotter;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.stumbler.SpotterExtension;


public class View extends Composite {
	
	public static Display display;
	public static Shell shell;
	
	private static int PLATFORM;
	private static int PLATFORM_PPC = 1;
	private static int PLATFORM_OTHER = 2;

	
	static {
		display = new Display();
		
		// get platform
		if (System.getProperty("os.name").equalsIgnoreCase("Windows CE")) {
			PLATFORM = PLATFORM_PPC;
			//System.out.println("PocketPC Platform");
		} else { 
			//System.out.println("Non-PocketPC Platform = " + System.getProperty("os.name"));
			PLATFORM = PLATFORM_OTHER;
		}

		if (PLATFORM == PLATFORM_PPC) {
			shell = new Shell(display, SWT.NO_TRIM | SWT.RESIZE);
			shell.setLocation(0,26);
			
			// take up the whole screen.
			Rectangle screen = display.getClientArea();
			shell.setSize(screen.width, screen.height);
		} else {
			shell = new Shell(display, SWT.SHELL_TRIM);
		}
		{
			RowLayout rl = new RowLayout(SWT.VERTICAL);
			rl.marginTop = 0;
			rl.marginLeft = 0;
			rl.marginRight = 0;
			rl.marginBottom = 0;
			shell.setLayout(rl);
		}		
		shell.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		shell.setText("PlaceLab Stumbler");
		Display.setAppName("Stumbler");
		
	}
	
	private Composite beaconTable;
	private Composite status;
	private Menu menuBar;
	public View () {
		super(shell, SWT.NONE);
		
		
		GridLayout l = new GridLayout();
		l.numColumns = 1;
		l.marginHeight = 0;
		l.marginWidth = 0;
		setLayout(l);
		
	
		
	
		beaconTable = new BeaconTable(this);
		beaconTable.setVisible(true);
		
		
		
		status = new Status(this);
		status.setVisible(true);
		
		loadMenus();
		
		shell.open();
	}
	
	private void loadMenus () {
		

		menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		// FILE
		{
			MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
			item.setText("File");
			
			Menu sub = new Menu(shell, SWT.DROP_DOWN);
			item.setMenu(sub);
			
			MenuItem newLog = new MenuItem(sub, SWT.PUSH);
			newLog.setText("New");
			newLog.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					try {
					Stumbler.getStumbler().newLog();
					} catch (IOException ioe) {
						reportError(ioe.toString());
					}
				}
			});
			
			MenuItem open = new MenuItem(sub, SWT.PUSH);
			open.setText("Open...");
			open.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(View.shell, SWT.OPEN);
					fd.setFilterExtensions(new String[]{".log"});
					String file = fd.open();
					if (file != null) {
						try {
							Stumbler.getStumbler().openLog(file);
						} catch (IOException ioe) {
							reportError(ioe.toString());
						}
					}
				}
			});
			
			MenuItem save = new MenuItem(sub, SWT.PUSH);
			save.setText("Save");
			save.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(View.shell, SWT.SAVE);
					fd.setFilterExtensions(new String[]{".log"});
					String file = fd.open();
					if (file != null) {
						try {
							Stumbler.getStumbler().saveLog(file);
						} catch (IOException ioe) {
							reportError(ioe.toString());
						}
					}
				}
			});
			
			MenuItem saveAs = new MenuItem(sub, SWT.PUSH);
			saveAs.setText("Save As...");
			saveAs.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					FileDialog fd = new FileDialog(View.shell, SWT.SAVE);
					fd.setFilterExtensions(new String[]{".log"});
					String file = fd.open();
					if (file != null) {
						try {
							Stumbler.getStumbler().saveLogAs(file);
						} catch (IOException ioe) {
							reportError(ioe.toString());
						}
					}
				}
			});
			
			MenuItem upload = new MenuItem(sub, SWT.PUSH);
			upload.setText("Upload...");
			upload.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					MessageBox mb = new MessageBox(View.shell, SWT.ICON_INFORMATION | SWT.OK );
					mb.setMessage("Not yet implemented.");
					mb.open();
				}
			});
			
			new MenuItem(sub, SWT.SEPARATOR);
			
			MenuItem enable = new MenuItem(sub, SWT.CHECK);
			enable.setText("Stumble");
			enable.setSelection(Stumbler.getStumbler().isStumbling() ? true : false);
			enable.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					if (((MenuItem) e.widget).getSelection()) {						
						Stumbler.getStumbler().startStumbling();
					} else {
						Stumbler.getStumbler().stopStumbling();
					}
				}
			});
			
			new MenuItem(sub, SWT.SEPARATOR);
			
			MenuItem quit = new MenuItem(sub, SWT.CHECK);
			quit.setText("Quit");
			quit.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					Stumbler.getStumbler().stopStumbling();
					View.display.dispose();
					System.exit(1);
				}
			});
			
			
		}
		
		// SPOTTER
		{
			MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
			item.setText("Devices");
			
			Menu sub = new Menu(shell, SWT.DROP_DOWN);
			item.setMenu(sub);
			
			for (Enumeration e = Stumbler.getStumbler().getReceivers(); e.hasMoreElements(); ) {
				Receiver r = (Receiver) e.nextElement();
				//System.out.println("r: " + r.getType());
				
				// don't show GPS on this menu.
				if (r instanceof GPSReceiver) 
					continue;
				
				MenuItem mi = new MenuItem(sub, SWT.CHECK);
				mi.setText(r.getType());
				mi.setSelection(false);
				
				Class[] supSpotter = r.getSupportedSpotters();
				
				for (int i = 0;i<supSpotter.length;i++) {
					SpotterExtension s = (SpotterExtension) Stumbler.getStumbler().getSpotter(supSpotter[i]);
					if (s == null)
						continue;
					
					if (s.isScanning()) {
						mi.setSelection(true);
						break;
					}
				}
				
				mi.addListener(SWT.Selection, new Listener(){
					public void handleEvent (Event e) {
						MenuItem mi = (MenuItem) e.widget;
						for (Enumeration re = Stumbler.getStumbler().getReceivers(); re.hasMoreElements(); ) {
							Receiver r = (Receiver) re.nextElement();
							
							if (!r.getType().equals(mi.getText()))
								continue;
							
							Class[] supSpotter = r.getSupportedSpotters();
							
							for (int i = 0;i<supSpotter.length;i++) {
								SpotterExtension s = (SpotterExtension) Stumbler.getStumbler().getSpotter(supSpotter[i]);
								if (s == null)
									continue;
								
								if (mi.getSelection()) {
									s.startScanning();
								} else {
									s.stopScanning();
								}
							}
						}
						
					}
				});
				
				
			}
			
		}
		
		
		/**
		 * This code will eventually provide a quick on/off toggle button
		 * that will live in the MenuBar on the PocketPC.  SWT on the PPC
		 * currently has a bug that prevents MenuItem's from triggering
		 * ArmEvent's when clicked. 
		 
		// Mac's will not tolerate images in the menu bar, and
		// it just doesn't look right unless you're on a PocketPC
		if (StringUtil.equalsIgnoreCase(System.getProperty("os.name"), "windows ce")) {
			MenuItem start = new MenuItem(menuBar, SWT.PUSH);
			start.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start"+(Stumbler.getStumbler().isStumbling() ? "-hot" : "")+".png")));
			start.addArmListener(new ArmListener(){
				
				public void widgetArmed (ArmEvent e) {
					MenuItem mi = (MenuItem) e.widget;
					System.out.println("blah!!!!!!");
					if (Stumbler.getStumbler().isStumbling()) {
						Stumbler.getStumbler().stopStumbling();
						mi.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start.png")));
					} else {
						Stumbler.getStumbler().startStumbling();
						mi.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start-hot.png")));
					}
					
				}
			});	
		}
		*/
	}
	
	public static void reportError (String msg) {
		MessageBox mb = new MessageBox(View.shell, SWT.ICON_ERROR | SWT.OK);
		mb.setMessage("Error: " + msg);
		mb.open();
	}
	
}


class Status extends Composite implements Runnable {
	
	Label gps;
	Label bt;
	Label wifi;
	Label logSize;
	Button toggleStumble;
	
	Composite line1;
	Composite line2;
	
	public Status (Composite parent) {
		super(parent, SWT.NONE);
		
		setSize(225, 20);
		
		{
			RowLayout rl = new RowLayout(SWT.VERTICAL);
			rl.marginTop = 0;
			rl.marginLeft = 3;
			rl.marginRight = 3;
			rl.justify = true;
			setLayout(rl);
		}
		
		line1 = new Composite(this, SWT.NONE);
		{
			
			RowLayout rl = new RowLayout(SWT.HORIZONTAL);
			rl.marginTop = 0;
			rl.marginLeft = 3;
			rl.marginRight = 3;
			rl.justify = true;
			line1.setLayout(rl);
			
			
			gps = new Label(line1, SWT.NONE);
			//new Label(line1, SWT.SEPARATOR);
			wifi = new Label(line1, SWT.NONE);
			//new Label(line1, SWT.SEPARATOR);
			bt = new Label(line1, SWT.NONE);
		}
		
		line2 = new Composite(this, SWT.NONE);
		{
			
			RowLayout rl = new RowLayout(SWT.HORIZONTAL);
			rl.marginTop = 0;
			rl.marginLeft = 3;
			rl.marginRight = 3;
			rl.justify = true;
			line2.setLayout(rl);
			
			//toggleStumble = new Button(line2, SWT.FLAT | SWT.NO_TRIM) ;
					
			logSize = new Label(line2, SWT.NONE);
			
			/*
			toggleStumble.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start"+(Stumbler.getStumbler().isStumbling() ? "-hot" : "")+".png")));
			toggleStumble.addSelectionListener(new SelectionListener(){
				
				public void widgetDefaultSelected(SelectionEvent e) {
					
				}
				
				public void widgetSelected (SelectionEvent e) {
					
					System.out.println("blah!!!!!!");
					if (Stumbler.getStumbler().isStumbling()) {
						Stumbler.getStumbler().stopStumbling();
						Status.this.toggleStumble.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start.png")));
					} else {
						Stumbler.getStumbler().startStumbling();
						Status.this.toggleStumble.setImage(new Image(View.display, this.getClass().getClassLoader().getResourceAsStream("resources/start-hot.png")));
					}
					
				}
			});
			*/	
		}
			
		
		
		gps.setText("No GPS");
		bt.setText("0 BT");
		wifi.setText("0 AP");
		
		logSize.setText("Log Empty");
		
		View.display.timerExec(1000, this);
	}
	
	public void receiverUpdated (Receiver r) {
		
	}
	
	public void run () {
		
		if (View.shell.isDisposed())
			return;
		
		WiFiReceiver wr = (WiFiReceiver) Stumbler.getStumbler().getReceiver(WiFiSpotter.class);
		if (wr != null) {
			wifi.setText(wr.totalNumBeacons() + " AP");
			wifi.setSize(wifi.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		BluetoothReceiver br = (BluetoothReceiver) Stumbler.getStumbler().getReceiver(BluetoothSpotter.class);
		if (br != null) {
			bt.setText(br.totalNumBeacons() + " BT");
			bt.setSize(bt.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		GPSReceiver gr = (GPSReceiver) Stumbler.getStumbler().getReceiver(NMEAGPSSpotter.class);
		if (gr != null) {
			String text = "No GPS";
			
			if (gr.hasLock() == GPSMeasurement.NO_INFO_RE_A_LOCK)
				text = "No Lock";
			else if (gr.hasLock() == GPSMeasurement.HAVE_A_LOCK)
				text = "GPS Lock";
			
			
			gps.setText(text);
			gps.setSize(gps.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		line1.setSize(line1.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		String logFile = Stumbler.getStumbler().getLogFile();
		if (logFile != null) {
			File f = new File(logFile);
			logSize.setText("Logged " + (f.length()/1024) + "k");
			logSize.setSize(logSize.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			line2.setSize(line2.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		
		this.setSize(this.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		View.display.timerExec(1000, this);
	}
}

class BeaconTable extends Composite implements ReceiverListener {
	final static int THRESHOLD_STALE = 5000;
	final static int THRESHOLD_REMOVE = 15000;
	
	
	private Table tbl;
	
	Hashtable container = new Hashtable();
	
	public BeaconTable (Composite parent) {
		super(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		
		setLayout(layout);
		
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.widthHint = 225;
		gd.heightHint = 200;
		
		tbl = new Table(this, SWT.BORDER);
		tbl.setLinesVisible(true);
		tbl.setHeaderVisible(true);
		
		
		tbl.setLayoutData(gd);
		
		
//		 define columns
		String[] cols = { "Address" , "Name", "Type", "RSSI", "Mode", "Lat", "Lon"};
		int[] colWidths = { 120, 80, 40, 50, 70, 70, 70 };
		for (int i = 0;i<cols.length; i++ ) {
			TableColumn col = new TableColumn(tbl, SWT.NULL);
			col.setText(cols[i]);	
			col.setWidth(colWidths[i]);
		}
		
		for (Enumeration e = Stumbler.getStumbler().getReceivers(); e.hasMoreElements(); ) {
			Receiver r = (Receiver) e.nextElement();
			if (r instanceof WiFiReceiver) 
				r.addListener(this);
		}
		
		View.display.timerExec(1000, new Runnable(){
			public void run () {
				
				if (View.shell.isDisposed())
					return;
				
				BeaconTable.this.tbl.removeAll();
				
				for (Enumeration e = container.keys(); e.hasMoreElements(); ) {
					String key = (String) e.nextElement();
					Hashtable h = (Hashtable) container.get(key);

					long time = ( (Long) h.get("time")).longValue();
					

					if ((System.currentTimeMillis() - time) >= THRESHOLD_REMOVE) {
						container.remove(key);
						continue;
					} 
					
					TableItem ti = new TableItem(BeaconTable.this.tbl, SWT.NONE);
					ti.setText(new String[]{
							(String) h.get("address"),
							(String) h.get("name"),
							(String) h.get("type"),
							(String) h.get("rssi"),
							(String) h.get("mode"),
							(String) h.get("lat"),
							(String) h.get("lon")
					});
					
					if ((System.currentTimeMillis() - time) >= THRESHOLD_STALE) {
						ti.setForeground(View.display.getSystemColor(SWT.COLOR_GRAY));
					}
					
				}
				
				View.display.timerExec(1000, this);
			}
		});
	}
	
	public void receiverUpdated (Receiver r) {
		Hashtable[] data = ((BeaconReceiver) r).getBeaconData();
		
		if (data == null) {
			return;
		}
		
		for (int i=0;i<data.length;i++) {
			data[i].put("time", new Long(System.currentTimeMillis()));
			container.put(data[i].get("address"), data[i]);
			
		}
		
		
	}
}




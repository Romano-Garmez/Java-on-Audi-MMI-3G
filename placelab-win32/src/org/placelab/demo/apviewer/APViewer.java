package org.placelab.demo.apviewer;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Tracker;
import org.placelab.client.tracker.TwoDPositionEstimate;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.Map;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.core.WiFiReading;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.WiFiSpotter;




/**
 * A sample graphical application that displays the currently viewable access points and an estimate of position
 */
public class APViewer extends Composite  implements Runnable {
	private Shell shell;
	private Display display;
	private PlacelabWithProxy daemon;
	private Label text;
	private Table data;
	private TableColumn cols[];
	private HashMap readings;
	private Tracker tracker;
	private Mapper mapper;
	public static final int TIMER_INTERVAL_MILLIS=1000;

	private class SpotterReading {
		public BeaconReading reading=null;
		public boolean saw=false;
		public TableItem tableItem=null;
	}

	
	private Tracker createTracker() {
		mapper = CompoundMapper.createDefaultMapper(true, true);
		tracker = new CentroidTracker(mapper);
		return tracker;
	}

	/**
	 * If this program is passed a file name it will be used as a log to be passed to a log spotter. If not a live WiFi spotter will be created.
	 */
	public static void main (String [] args) {
		try {
			int flag=SWT.DIALOG_TRIM;
			
			if (System.getProperty("os.name").equalsIgnoreCase("Windows CE")) {
				flag=SWT.NO_TRIM;
			}
			Display display = new Display ();
			APViewer hello=new APViewer(display, new Shell(display, flag), (((args.length > 0) && (args[0].length() > 1)) ? args[0] : null));
			hello.runLoop();
		} catch (Exception e) {
			System.err.println("Couldn't start the placelab daemon:\n"+	
				e.getClass().getName()+":\n"+e.getMessage());
			e.printStackTrace();
		}
		System.exit(0); /* XXX: kludge coz there is another thread
				 * that happens to be lingering around */
	}

	public APViewer(Display theDisplay, Shell shell, String logfile) throws IOException {
		super(shell,SWT.SHADOW_NONE);
		this.shell = shell;
		shell.setText("PlaceLab");
		shell.setLayout(new FillLayout());
		display=theDisplay;
		this.setBackground(display.getSystemColor(SWT.COLOR_WHITE));		
		readings = new HashMap();
		createTracker();
		if (logfile == null) {
			daemon=new PlacelabWithProxy(new WiFiSpotter(), tracker, mapper,-1); 
		} else {
			daemon=new PlacelabWithProxy(LogSpotter.newSpotter(logfile),tracker,mapper,-1); 
		}
		daemon.createProxy();
		setShellSize(shell,theDisplay);
		
		int fontdec = 0;
		if (shell.getSize().x < 350) {
			fontdec = 2;
		}
		
		text = new Label(this,0);
		Font f = new Font(getDisplay(),getFont().getFontData()[0].getName(),10-fontdec,
			                    getFont().getFontData()[0].getStyle());
		Font f2 = new Font(getDisplay(),getFont().getFontData()[0].getName(),9-fontdec,
			                    getFont().getFontData()[0].getStyle());
		text.setFont(f);
		text.setText("Estimated Latitude: ???\nEstimated Longitude: ???\n\nNearby Access Points:");
		int gap = 8;
		int th = text.computeSize(SWT.DEFAULT,SWT.DEFAULT).y;
		text.setBounds(gap,gap,this.getClientArea().width-2*gap,th);
		text.setBackground(display.getSystemColor(SWT.COLOR_WHITE));		
		text.setVisible(true);
		
		data = new Table(this,0);
		cols = new TableColumn[4];
		cols[0] = new TableColumn(data,0);
		cols[0].setText("MAC Address");
		cols[1] = new TableColumn(data,0);
		cols[1].setText("SSID");
		cols[2] = new TableColumn(data,0);
		cols[2].setText("RSSI");
		cols[3] = new TableColumn(data,0);
		cols[3].setText("Known?");

		int w = this.getClientArea().width-2*gap;
		cols[0].setWidth(3*w/8);
		cols[1].setWidth(2*w/8-4);
		cols[2].setWidth(3*w/16);
		cols[3].setWidth(3*w/16+4);
		
		data.setBounds(gap,th+gap+2,w,this.getClientArea().height-3*gap-th);
		data.setVisible(true);
		data.setHeaderVisible(true);
		data.setFont(f2);
		
		shell.open();
		display.timerExec(TIMER_INTERVAL_MILLIS,this);
		
	}
	
	public void runLoop() {
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
			  display.sleep ();
			}
		}
		getDaemon().shutdown();
		display.dispose ();
	}

	public void setShellSize(Shell s, Display d) {
		Rectangle screen= shell.getDisplay().getClientArea();
		
		int w=screen.width;
		int h=screen.height;
		if (w>450) {
			w=450;
		} 
		if (h>550) {
			h=550;
		}
		s.setSize(w,h);
	}

	public void updated(BeaconMeasurement meas) {
		TwoDPositionEstimate estimate = (TwoDPositionEstimate) tracker.getEstimate();
		TwoDCoordinate pos = (TwoDCoordinate)estimate.getCoord();

		if (text.isDisposed()) return;
		text.setText("Estimated Latitude: " +  pos.getLatitudeAsString() + "\nEstimated Longitude: " + 
		                        pos.getLongitudeAsString() + "\n\nNearby Access Points:");

		/*
		// clear the table
		data.setVisible(false);
		TableItem arr[] = data.getItems();
		for (int i=0; i<arr.length; i++) {
			arr[i].dispose();
		}
		DaemonPulseInfo pi = poker.info;
		if (pi == null) {
			return;
		}
		for (int i=0; i<pi.readArr.length; i++) {
			TableItem it = new TableItem(data,0);
			String sarr[] = new String[4];
			sarr[0] = pi.readArr[i].uniqueID;
			sarr[1] = pi.readArr[i].humanReadableName;
			sarr[2] = "" + pi.readArr[i].RSSI;
			sarr[3] = pi.readArr[i].beacon != Beacon.UNKNOWN_BEACON ? "Yes" : "No";
			it.setText(sarr);
		}
		data.setVisible(true);
		*/
		update_table(meas);
	}		

	private void update_table(BeaconMeasurement meas)
	{
		if (meas == null) return;
		
		for (Iterator it = readings.values().iterator(); it.hasNext(); ) {
			SpotterReading r = (SpotterReading) it.next();
			r.saw = false;
		}

		for (int i=0; i < meas.numberOfReadings(); i++) {
			//if(!(meas.getReading(i) instanceof WiFiReading)) continue;
			BeaconReading br = meas.getReading(i);
			if (readings.containsKey(br.getId())) {
				((SpotterReading)readings.get
				 (br.getId())).saw = true;
				//System.out.println("Seen " + pi.readArr[i].uniqueID);
			} else {
				//System.out.println("NOT Seen (" + readings.size() + ") " + pi.readArr[i].uniqueID);
				SpotterReading r = new SpotterReading();
				r.reading = br;
				r.saw = true;
			
				r.tableItem = new TableItem(data,0);
				String sarr[] = new String[4];
				sarr[0] = r.reading.getId();
				sarr[1] = r.reading.getHumanReadableName();
				if(r.reading instanceof WiFiReading){
				    sarr[2] = "" + ((WiFiReading)r.reading).getRssi();
				} else {
				    sarr[2] = "" + r.reading.getNormalizedSignalStrength();
				}

				Beacon beacon = mapper.findBeacon(r.reading.getId());
				sarr[3] = beacon != null ? "Yes" : "No";
				r.tableItem.setText(sarr);

				readings.put(r.reading.getId(), r);
			}
		}
		//System.out.println("--------------------------------------");

		for (Iterator it=readings.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			SpotterReading r = (SpotterReading) entry.getValue();
			if (r.saw) continue;

			r.tableItem.dispose();
			r.tableItem = null;
			it.remove();
		}
	}

	public PlacelabWithProxy getDaemon() {
		return daemon;
	}

	public void run() {
		Measurement meas = daemon.pulse();
		if (meas != null) {
			updated((BeaconMeasurement)meas);
		}
		scheduleTimeout();
	}
	public void scheduleTimeout() {
		display.timerExec(TIMER_INTERVAL_MILLIS,this);
	}


}

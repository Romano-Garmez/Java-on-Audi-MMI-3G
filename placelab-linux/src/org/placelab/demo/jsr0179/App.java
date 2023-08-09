
package org.placelab.demo.jsr0179;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationProvider;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Shell;


public class App {

	public static int PLATFORM;
	public static int PLATFORM_PPC = 1;
	public static int PLATFORM_OTHER = 2;
	
	static {
		
		if (System.getProperty("os.name").equalsIgnoreCase("Windows CE")) {
			PLATFORM = PLATFORM_PPC;
			//System.out.println("PocketPC Platform");
		} else { 
			//System.out.println("Non-PocketPC Platform = " + System.getProperty("os.name"));
			PLATFORM = PLATFORM_OTHER;
		}
	}
	
	public static LandmarkStore store;
	public volatile static Landmark currentLandmark = null;
	public static Monitor view;
	public static App self;

	
	public App () {
		
		self = this;
		
		store = LandmarkStore.getInstance("fs");
		
		try { 
			LocationProvider.getInstance(null);
		} catch (LocationException e) {
			e.printStackTrace();
		}
		
		new NearbyScanner().start();
	}
	
	public static void main (String[] args) {
		
		Shell shell = Monitor.shell;
		
		shell.setLayout(new FillLayout());
		
		view = new Monitor(shell);

		App app = new App();
		
		
		
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.heightHint = 300;
		gd.widthHint = 400;
		view.setLayoutData(gd);
		 
		if (App.PLATFORM != App.PLATFORM_PPC) {
			shell.setSize(shell.computeSize(300, 500));
		}
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!Monitor.display.readAndDispatch()) {	
				Monitor.display.sleep();
			}
		}
		
		Monitor.display.dispose();
		
			
	}
	
	class NearbyScanner extends Thread {
		private volatile boolean cancel = false;
		private Coordinates lastCoordinates;
		
		public void run () {
			for (;!cancel && !Monitor.shell.isDisposed();) {
				try {
					
					Location lastLocation = LocationProvider.getLastKnownLocation();
					
					if ( (lastLocation == null) ) {
						view.setClosest("Waiting for signal...");
						view.setMark(false);
					} else if (!lastLocation.isValid()) {
						view.setClosest("No signal");
						view.setMark(false);
					} else if (lastLocation.isValid()) {
						view.setMark(true);
						
						lastCoordinates = lastLocation.getQualifiedCoordinates();
						
						Vector entries = new Vector();
						double lowestDistance = -1.0D;
						Landmark closestLandmark = null;
						
						for (Enumeration e = store.getLandmarks(); (e != null) && e.hasMoreElements();) {
							
							Landmark l = (Landmark) e.nextElement();
							Coordinates c = l.getQualifiedCoordinates();
							
							double distance = c.distance(lastCoordinates);
							
							
							if ( (lowestDistance < 0) || (distance < lowestDistance)) {
								lowestDistance = distance;
								closestLandmark = l;
							}
							
							if (distance <= view.getResolution()) {
									
								NumberFormat nf = NumberFormat.getInstance();
								nf.setMaximumFractionDigits(1);
								
								entries.addElement(new String[]{ l.getName(), nf.format(distance) + "m"});
								
							}
							
						}
						
						String items[][] = new String[entries.size()][];
						
						entries.copyInto(items);
						
						entries = null;
						
						view.setNearby(items);
						
						// set closest
						if ( (closestLandmark != null) && (closestLandmark.getQualifiedCoordinates().distance(lastCoordinates) < 100)) {
							view.setClosest(closestLandmark.getName());
							currentLandmark = closestLandmark;
						} else {
							view.setClosest("No landmarks present");
							currentLandmark = null;
						}
						
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					cancel();
				}
			}
		}
		
		public void cancel () {
			cancel = true;
		}
	}
	
}

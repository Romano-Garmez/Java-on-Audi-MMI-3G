package org.placelab.demo.mapview;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Tracker;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.util.Cmdline;

/**
 * A map application.
 *
 */
public class MapDemo {
	protected PlacelabWithProxy daemon;
	
	protected Display display;
	protected Shell shell;
	protected TrackedMapView mapView;
	protected Menu bar;
	
	protected Vector placeItems;
	
	protected WadData data;
	
	
	protected Tracker createTracker(Mapper m) {
		return new CentroidTracker(m);
	}
	
	protected MapDemo() { }
	
	public MapDemo(String logfile, String mapArchive, String mapName, Mapper m) throws IOException {
		if (m==null) m = CompoundMapper.createDefaultMapper(true, true);
		buildShell();
		
		if (logfile==null) {
			daemon = new PlacelabWithProxy(createTracker(m), m, -1); 
		} else {
			Spotter sp = LogSpotter.newSpotter(logfile);
			daemon = new PlacelabWithProxy(sp, createTracker(m), m, -1); 
		}
		mapView = new TrackedMapView(shell, SWT.NONE, daemon);
		mapView.setLayoutData(new GridData(GridData.FILL_BOTH));
		mapView.setShowBeacons(false);

		loadWad(mapArchive, mapName);
		
		if(Cmdline.getArg("zoom") != null) {
			try {
				double zoom = Double.parseDouble(Cmdline.getArg("zoom"));
				if(zoom > 0) {
					mapView.setZoom(zoom);
				}
			} catch (NumberFormatException nfe) {
				mapView.setZoom(1.0);
			}
		}
		resize();
		shell.open();
	}
	
	protected void loadWad() {
		loadWad(null, null);
	}
	
	protected void loadWad(String path, String mapName) {
		WadData temp;
		if(path == null) {
			path = askUserForFile();
			// if path is still null, user doesn't want to
			// load a wad.  so be it.
			if(path == null) {
				buildMenu();	
				return; 
			} 
		}
		try {
			temp = new WadData(path);
		} catch (Exception e) {
			errorDialog("Couldn't load mapwad", e);
			return;
		}
		MapBacking mapBack = null;
		if (mapName != null) {
			mapBack = temp.getMap(mapName);
			if (mapBack == null) {
				// print this stuff on the console since its only going to result
				// from incorrect cmdline args
				System.out.println("Map: " + mapName + " not found. Available maps are:\n");
				for (Enumeration en = temp.getMaps().keys(); en.hasMoreElements();) {
					System.out.println("  - " + en.nextElement().toString());
				}
				//System.exit(1);
				System.out.println("Choosing an arbitrary map instead");
				mapName = null;
			}
		if(mapName == null)
			mapBack = temp.getDefaultMap();
		}
		Hashtable defaultPlaces = temp.getDefaultPlaceSets();
		if(mapBack == null) {
			// render the first map if there is no default
			try {
				mapBack = (MapBacking)temp.getMaps().elements().nextElement();
			} catch (java.util.NoSuchElementException e) {
				errorDialog("That mapwad doesn't have any maps!");
				return;
			}
		}
		if(mapBack == null) {
			errorDialog("That mapwad doesn't have any maps!");
			return;
		}
		if(defaultPlaces == null) defaultPlaces = new Hashtable();
		data = temp;
		
		mapView.setMapData(mapBack);
		mapView.setPlaceSets(defaultPlaces);
		shell.setText(mapBack.getName());
		buildMenu();
	}
	
	protected String askUserForFile() {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		String path = fd.open();
		return path;
	}
	
	protected void errorDialog(String msg) {
		errorDialog(msg, null);
	}
	protected void errorDialog(String msg, Throwable e) {
	    if(e != null) e.printStackTrace();
		MessageDialog d = new MessageDialog(shell,
				e == null ? "Error" : msg, null, 
				e == null ? msg : e.toString(), MessageDialog.ERROR,
				new String[]{"OK"}, 0);
		d.open();
	}
	
	protected void buildShell() {
		int flag = SWT.SHELL_TRIM;
		if(System.getProperty("os.name").equalsIgnoreCase
				("Windows CE")) flag = SWT.NO_TRIM;
		Display.setAppName("Placelab XMapDemo");
		display = new Display();
		shell = new Shell(display, flag);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		shell.setLayout(layout);
	}
	protected void buildMenu() {
		// building menus by hand is a crime
		// but alas, this is how its done in swt
		int commandKey = SWT.CONTROL;
		if(BrowserControl.isMacPlatform()) commandKey = SWT.COMMAND;
		bar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(bar);
		
		// build file menu
		MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
		fileItem.setText("File");
		Menu fileMenu = new Menu(bar);
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("Open...");
		openItem.setAccelerator(commandKey | 'O');
		openItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				loadWad();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				loadWad();
			}
		});
		if(!BrowserControl.isMacPlatform()) {
			MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
			exitItem.setText("Exit");
			exitItem.setAccelerator(SWT.ALT | SWT.F4);
			exitItem.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					shutdown();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					shutdown();
				}
			});
		}
		fileItem.setMenu(fileMenu);
		
		// cut out early and don't have View, Hashtable or Place menus if there isn't anything
		// loaded yet
		if(data == null || mapView == null || mapView.getMapData() == null) return;
		
		// build view menu
		MenuItem viewItem = new MenuItem(bar, SWT.CASCADE);
		viewItem.setText("View");
		viewItem.setAccelerator(commandKey | 'V');
		Menu viewMenu = new Menu(bar);
		//
		MenuItem beaconShowItem = new MenuItem(viewMenu, SWT.CHECK);
		beaconShowItem.setText("Show Beacons");
		beaconShowItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleBeacons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				toggleBeacons();
			}
		});
		beaconShowItem.setSelection(mapView.getShowBeacons());
		//
//		MenuItem particlesShowItem = new MenuItem(viewMenu, SWT.CHECK);
//		particlesShowItem.setText("Show Particles");
//		particlesShowItem.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				toggleParticles();
//			}
//			public void widgetDefaultSelected(SelectionEvent e) {
//				toggleParticles();
//			}
//		});
//		particlesShowItem.setSelection(mapView.getShowParticles());
		//		
		MenuItem autoScrollItem = new MenuItem(viewMenu, SWT.CHECK);
		autoScrollItem.setText("Auto Scroll Map");
		autoScrollItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleAutoScroll();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				toggleAutoScroll();
			}
		});
		autoScrollItem.setSelection(mapView.getAutoScroll());
		//
		MenuItem autoZoomItem = new MenuItem(viewMenu, SWT.CHECK);
		autoZoomItem.setText("Auto Zoom Map");
		autoZoomItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleAutoZoom();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				toggleAutoZoom();
			}
		});
		autoZoomItem.setSelection(mapView.getAutoZoom());
		//
		MenuItem zoomInItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomInItem.setText("Zoom In");
		zoomInItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				mapView.zoomIn();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				mapView.zoomIn();
			}
		});
		//
		MenuItem zoomOutItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomOutItem.setText("Zoom Out");
		zoomOutItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				mapView.zoomOut();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				mapView.zoomOut();
			}
		});
		viewItem.setMenu(viewMenu);
		
		// build map menu
		MenuItem mapItem = new MenuItem(bar, SWT.CASCADE);
		mapItem.setText("Maps");
		Menu mapMenu = new Menu(bar);
		Enumeration e = data.getMaps().keys();
		String selected = mapView.getMapData().getName();
		MenuItem select = null;
		while(e.hasMoreElements()) {
			String name = (String) e.nextElement();
			MenuItem item = new MenuItem(mapMenu, SWT.RADIO);
			if(select == null && name.equals(selected)) {
				select = item;
				item.setSelection(true);
			}
			item.setText(name);
			item.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					mapAction(e);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					mapAction(e);
				}
			});
		}
		if(select != null) mapMenu.setDefaultItem(select);
		MenuItem placeItem = new MenuItem(bar, SWT.CASCADE);
		placeItem.setText("Places");
		Menu placeMenu = new Menu(bar);
		placeItems = new Vector();
		Hashtable defaultPlaceSets = data.getDefaultPlaceSets();
		e = data.getPlaceSetNames();
		while(e.hasMoreElements()) {
			String name = (String)e.nextElement();
			MenuItem item = new MenuItem(placeMenu, SWT.CHECK);
			if(defaultPlaceSets.containsKey(name)) item.setSelection(true);
			item.setText(name);
			item.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					placeAction(e);
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					placeAction(e);
				}
			});
			placeItems.add(item);
		}
		mapItem.setMenu(mapMenu);
		placeItem.setMenu(placeMenu);
	}
	protected void mapAction(SelectionEvent e) {
		MenuItem item = (MenuItem)e.getSource();
		String mapName = item.getText();
		MapBacking map = data.getMap(mapName);
		if(mapName != null) {
			shell.setText(mapName);
			mapView.setMapData(map);
		}
	}
	protected void placeAction(SelectionEvent e) {
		// XXX: This is lame
		Hashtable selectedPlaces = new Hashtable();
		Enumeration i = placeItems.elements();
		while(i.hasMoreElements()) {
			MenuItem mi = (MenuItem)i.nextElement();
			if(mi.getSelection()) {
				Hashtable placeSet = data.getPlaceSet(mi.getText());
				selectedPlaces.put(mi.getText(), placeSet);
			}
		}
		mapView.setPlaceSets(selectedPlaces);
	}
	
	protected void toggleBeacons() {
		mapView.setShowBeacons(!mapView.getShowBeacons());
	}
	protected void toggleAutoScroll() {
		mapView.setAutoScroll(!mapView.getAutoScroll());
		buildMenu();
	}
	protected void toggleAutoZoom() {
		mapView.setAutoZoom(!mapView.getAutoZoom());
		buildMenu();
	}
//	protected void toggleParticles() {
//		mapView.setShowParticles(!mapView.getShowParticles());
//	}
	
	protected void shutdown() {
		// XXX: Should do cleanup here
		System.exit(0);
	}
	
	protected void resize() {
		Rectangle r = display.getClientArea();
		r.height = (int)(0.95 * r.height);

		/*System.out.println("bounds: ("+r.x+","+r.y+"):"+r.width+"x"+
		  r.height);*/

		Point mapSize = mapView.getMapSize();
		Rectangle trim = mapView.computeTrim(0, 0, mapSize.x, mapSize.y);
		trim = shell.computeTrim(0, 0, trim.width, trim.height);

/*
		System.out.println("image: "+mapSize.x+
				   "x"+mapSize.y);
		System.out.println("trim: ("+trim.x+","+trim.y+"):"+trim.width+
				   "x"+trim.height);
*/
		if (trim.width  < r.width ) r.width  = trim.width;
		if (trim.height < r.height) r.height = trim.height;
		shell.setBounds(r);
	}

	public void run() {
		mapView.getPoker().scheduleTimeout();
		mapView.getPoker().setFps(10);
		doEventLoop();
		getDaemon().shutdown();
	}
	
	protected void doEventLoop() {
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public PlacelabWithProxy getDaemon() {
		return daemon;
	}

	/**
	 * Passing in a file will cause a log spotter to be created. If no file name is passed, a wifi spotter will be created
	 */
	public static void main (String [] args) {
		Cmdline.parse(args);
		String inputLog = Cmdline.getArg("log");
		//System.out.println(inputLog);
		String mapArchive = Cmdline.getArg("maps");
		String mapName = Cmdline.getArg("mapname");
		if (Cmdline.getArg("demo") != null) {
		 	inputLog = "seattle.log";
		 	mapArchive = "seattlemaps.zip";
		 	mapName = "University District";			
		}
		if (Cmdline.getArg("demolive") != null) {
		 	inputLog = null;
		 	mapArchive = "seattlemaps.zip";
		 	mapName = "University District";			
		}

		//if(mapArchive == null) {
		//	System.err.println("Usage: java XMapDemo --maps <map archive> --mapname <map name> [--log <logfile>]");
		//	System.exit(1);
		//}

		try {
			MapDemo hello = new MapDemo(inputLog, mapArchive, mapName, null);
			hello.run();
		} catch (Exception e) {
			System.err.println("Error:\n"+	
				e.getClass().getName()+":\n"+e.getMessage());
			e.printStackTrace();
		}
	}
}

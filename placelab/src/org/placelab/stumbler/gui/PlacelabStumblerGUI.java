package org.placelab.stumbler.gui;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.CompoundEstimate;
import org.placelab.client.tracker.CompoundTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.PositionTracker;
import org.placelab.client.tracker.Tracker;
import org.placelab.collections.ArrayList;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.BeaconMeasurement;
import org.placelab.core.GPSMeasurement;
import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.demo.mapview.BrowserControl;
import org.placelab.demo.mapview.MapBacking;
import org.placelab.demo.mapview.MapView;
import org.placelab.demo.mapview.TrackedMapView;
import org.placelab.demo.mapview.WadData;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.BluetoothGPSSpotter;
import org.placelab.spotter.BluetoothSpotter;
import org.placelab.spotter.ListenerBluetoothGPSSpotter;
import org.placelab.spotter.NMEAGPSSpotter;
import org.placelab.spotter.RemoteGSMSpotter;
import org.placelab.spotter.SerialGPSSpotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.stumbler.AudioNotifier;
import org.placelab.stumbler.LogWriter;
import org.placelab.stumbler.SpotterExtension;
import org.placelab.stumbler.StumblerFunnel;
import org.placelab.stumbler.StumblerFunnelUpdateListener;
import org.placelab.util.Cmdline;
import org.placelab.util.Logger;
import org.placelab.util.NumUtil;
import org.placelab.util.swt.PhantomView;
import org.placelab.util.swt.QuestionBox;

/** 
 * The gui for the PlacelabStumbler application.  This gui is only intended for
 * laptops or other devices with a standard sized display and a full swt and jface
 * implementation.  The pocket pc will have to use the text based version or a scaled
 * down ui.
 * 
 */
public class PlacelabStumblerGUI implements StumblerFunnelUpdateListener,  
	EstimateListener
{
    
    protected PlacelabWithProxy daemon;
     
	protected Display display;
	protected Shell shell;
	
	protected PhantomView phantom;
	protected Composite phantomHolder;
	protected TrackedMapView mapView1, mapView2;
	protected WadData wad1, wad2;
	protected Vector placeItems;
	
	protected Menu bar;
	protected TabFolder mainTabFolder;
	protected TabItem item1, item2;
	protected Composite mainComposite, item1Comp, item2Comp;
	protected Scale tmvZoomScale;
	protected Group tmvGroup, mapHolderGroup;
	protected Label tmvLabel, tmvBigLabel;
	protected Label plEstimateText, gpsEstimateText;
	protected SashForm sash;
	protected WiFiTableController wifiTableController;
	protected BluetoothTableController bluetoothTableController;
	protected GSMTableController gsmTableController;
	protected Hashtable beaconIcons;
	protected Hashtable lastActiveBeaconIcons;
	protected int tableCount;
	
	protected StumblerFunnel funnel;
	protected NMEAGPSSpotter gps;
	protected WiFiSpotter wifi;
	protected BluetoothSpotter bluetooth;
	protected RemoteGSMSpotter gsm;
	protected SpotterExtension gpsExtension, wifiExtension, bluetoothExtension, gsmExtension;
	protected LogWriter logWriter;
	
	protected LinkedList eventQueue;
	protected Hashtable latestMeasurements;
	
	protected GPSMeasurement latestGPSMeasurement;

    protected Button tmvBtnPosition, tmvBtnTrack, tmvBtnBigMap, tmvBtnScroll;
    protected int gap;
    protected boolean boolStumbleOnOff, boolAutoScroll;
    
    protected String lastSavePath = null;

    protected Mapper mapper;
    
    protected static String sp = "\t";
    
    protected PositionTracker positionTracker;
    protected CentroidTracker centroidTracker;
	
	protected Tracker createTracker(Mapper m) {
	    CompoundTracker compound = new CompoundTracker();
	    centroidTracker = new CentroidTracker(m);
	    positionTracker = new PositionTracker();
	    compound.addTracker(centroidTracker);
	    compound.addTracker(positionTracker);
	    return compound;
	}
	
	public PlacelabStumblerGUI(String mapArchive, String mapName, Mapper m) throws IOException {
	    if(m == null) m = CompoundMapper.createDefaultMapper(true, true);
	    mapper = m;
	    lastActiveBeaconIcons = new Hashtable();
	    beaconIcons = new Hashtable();	
	    beginStumbling();
	    if(wifi != null) daemon = new PlacelabWithProxy(wifiExtension, createTracker(m), m, -1);
	    else if(bluetooth != null) daemon = new PlacelabWithProxy(bluetoothExtension, createTracker(m), m, -1);
	    else if(gps != null) daemon = new PlacelabWithProxy(gpsExtension, new PositionTracker(), m, -1);
	    else {
	        System.err.println("I was unable to get a WiFiSpotter nor a BluetoothSpotter nor an NMEAGPSSpotter.");
	        System.err.println("If you have any of that hardware, you need to set up your classpath and native libraries" +
	        		" appropriately");
	        System.err.println("If you don't have any of that hardware, this program can't work for you.");
	        System.exit(1);
	    }
	    daemon.addEstimateListener(this);
	    
	    boolStumbleOnOff = true;
	    
	    buildShell();
	    shell.open();
	}
	
	public static void main(String args[]) {
	    Cmdline.parse(args);
	    try {
	        PlacelabStumblerGUI gui = new PlacelabStumblerGUI(null, null, null);
	        gui.run();
	    } catch(Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	}
	
	protected void buildShell() {
		/** TODO:  The GUI in Windows is rather bland;  it looks better with the WinXP look and feel but 
		 * this seems to be something that you set with a manifest for all of Eclipse.  I need to find out
		 * how to make the manifest application-specific. --tabert
		 */
		
		int flag = SWT.SHELL_TRIM;
		
		Display.setAppName("PlacelabStumbler");
		display = Display.getCurrent();
		if(display == null) display = new Display();
		shell = new Shell(display, flag);
		shell.setText("Place Lab Stumbler");
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = layout.marginHeight = 0;
		shell.setLayout(layout);
		
		
		// create the main tab folder
		mainTabFolder = new TabFolder(shell,SWT.NONE);
		mainTabFolder.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		        buildMenu();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) { }
		});
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		mainTabFolder.setLayoutData(gridData);
		
		// create the tabs and their composite containers
		item1 = new TabItem(mainTabFolder, SWT.NONE);
		item1.setText("Stumbler");
		item2 = new TabItem(mainTabFolder,SWT.NONE);
		item2.setText("Map");
		item1Comp = new Composite(mainTabFolder, SWT.NONE);
		item2Comp = new Composite(mainTabFolder, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		item1Comp.setLayout(gridLayout);
		
		
		//set the size of the shell
		setShellSize(shell,display);
		
		int fontdec = 0;
		if (shell.getSize().x < 350) {
			fontdec = 2;
		}
		
		
		/*
		 * STUMBLER TAB GUI SETUP
		 */
		
		Font f = new Font(getDisplay(),shell.getFont().getFontData()[0].getName(),10-fontdec,
                shell.getFont().getFontData()[0].getStyle());
		Font f2 = new Font(getDisplay(),shell.getFont().getFontData()[0].getName(),9-fontdec,
                shell.getFont().getFontData()[0].getStyle());
		Font f3 = new Font(getDisplay(),shell.getFont().getFontData()[0].getName(),10-fontdec,
                SWT.BOLD);
		
		plEstimateText = new Label(item1Comp,SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		plEstimateText.setLayoutData(gridData);
		
		gpsEstimateText = new Label(item1Comp,SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 1;
		gpsEstimateText.setLayoutData(gridData); 
		
		//set the text for the label at the top showing lat/lon
		plEstimateText.setFont(f);
		plEstimateText.setText("Place Lab Estimated Latitude: Unknown\tEstimated Longitude: Unknown\tNearby Access Points: Unknown");
		gpsEstimateText.setFont(f);
		gpsEstimateText.setText("GPS Estimated Latitude: Unknown\tEstimated Longitude: Unknown");
		
		
		int th = plEstimateText.getBounds().height;
		
		Label seperatorLabel = new Label(item1Comp, SWT.SEPARATOR | SWT.HORIZONTAL); 
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 2;
		seperatorLabel.setLayoutData(gridData);
		
		
        // apparently vertical means horizontal in the world of SashForms
        sash = new SashForm(item1Comp, SWT.VERTICAL);
        sash.setLayout(layout);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = item1Comp.getClientArea().height;
        gridData.widthHint = item1Comp.getClientArea().width;
        sash.setLayoutData(gridData);
		
        tableCount = 0;
		if(wifi != null) {
			wifiTableController = new WiFiTableController(f3, f, sash, mapper,
					SpotterExtension.GPS_STALE_TIME);
			tableCount++;
		}
		if(bluetooth != null) {
			bluetoothTableController = new BluetoothTableController(f3, f, sash, mapper,
					20000);
			tableCount++;
		}
		if(gsm != null) {
			gsmTableController = new GSMTableController(f3, f, sash, mapper,
					20000);
			tableCount++;
		}

		int[] weights = new int[tableCount];
		for(int i = 0; i < tableCount; i++) {
			weights[i] = 100 / tableCount;
		}
			
		sash.setWeights(weights);
		sash.setVisible(true);

		
//		 placeholder label for the tmv
		String loadMessage = "Choose Load MapWad from the File menu to load a map";
		tmvBigLabel = new Label(item2Comp,SWT.NONE);
		tmvBigLabel.setBounds(gap,gap,shell.getClientArea().width-gap*5,shell.getClientArea().height-gap*8);
		tmvBigLabel.setBackground(new Color(display,242,255,179));
		tmvBigLabel.setText(loadMessage);
		tmvBigLabel.setVisible(true);
		
		// set the Composite containers to their respective tabs
		item1.setControl(item1Comp);
		item2.setControl(item2Comp);
		
		//set the tab folder size
		mainTabFolder.setBounds(shell.getClientArea());
		
		buildMenu();
		
		// resize display to fit
		Rectangle r1 = display.getClientArea();
		Rectangle r2 = shell.getBounds();
		if(r2.height > r1.height) r2.height = r1.height;
		if(r2.width > r2.width) r2.width = r1.width;
		shell.setBounds(r2);		
	}
	
	protected String askUserForFile() {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		String path = fd.open();
		return path;
	}
	
	protected String askUserForNewFile() {
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		String path = fd.open();
		return path;
	}
	
	protected void errorDialog(String msg) {
		errorDialog(msg, null);
	}
	protected void errorDialog(String msg, Throwable e) {
		MessageDialog d = new MessageDialog(shell,
				e == null ? "Error" : msg, null, 
				e == null ? msg : e.toString(), MessageDialog.ERROR,
				new String[]{"OK"}, 0);
		d.open();
	}
	
	
	
	public Display getDisplay() {
	    return display;
	}
	
	public void checkZoomLevel() {
		int selection = tmvZoomScale.getSelection();
		if (selection < 20){
			mapView1.setZoom(0.5);
		}else if(selection < 40){
			mapView1.setZoom(0.75);
		}else if(selection < 80){
			mapView1.setZoom(1.0);
		}else if(selection < 100){
			mapView1.setZoom(1.5);
		}else if(selection < 120){
			mapView1.setZoom(2.0);
		}
		fixZoomSlider();
	}
	
	protected void fixZoomSlider() {
	    double zoom = mapView1.getZoom();
	    if(zoom <= 0.5) {
	        tmvZoomScale.setSelection(20);
	    } else if(zoom <= 0.75) {
	        tmvZoomScale.setSelection(40);
	    } else if(zoom <= 1.0) {
	        tmvZoomScale.setSelection(60);
	    } else if(zoom <= 1.5) {
	        tmvZoomScale.setSelection(80);
	    } else {
	        tmvZoomScale.setSelection(100);
	    }
	}
	
	
	protected void doMapZoomIn() {
	    // check which map to zoom
	    if(this.mainTabFolder.getSelectionIndex() == 0) {
	        mapView1.zoomIn();
	        fixZoomSlider();
	    } else {
	        mapView2.zoomIn();
	    }
	}
	
	protected void doMapZoomOut() {
	    // check which map to zoom
	    if(this.mainTabFolder.getSelectionIndex() == 0) {
	        mapView1.zoomOut();
	        fixZoomSlider();
	    } else {
	        mapView2.zoomOut();
	    }
	}
	
	protected void displayPlacelabReticle() {
	    
	}
	
	protected void buildMapViews() {
	    GridData gridData;
		/* create a group to hold all the tracked map view stuff
		 */
	    
	    Label seperatorLabel = new Label(wifiTableController.body, SWT.SEPARATOR | SWT.HORIZONTAL);    
        gridData = new GridData(GridData.FILL_HORIZONTAL);
		seperatorLabel.setLayoutData(gridData);
	    
		tmvGroup = new Group(sash, SWT.SHADOW_ETCHED_IN);
		tmvGroup.setText("Mini Map");
		//tmvGroup.setBounds(gap,wifiHistoryTable.getBounds().y + wifiHistoryTable.getBounds().height + gap,500,300);
		//tmvGroup.setBounds(gap,historyLabel.getBounds().y + historyLabel.getBounds().height + gap,500,300);
		//tmvGroup.setBounds(gap,wifiActiveTable.getBounds().y + wifiActiveTable.getBounds().height + gap,500,300);
		
		tmvGroup.setVisible(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		//gridData.grabExcessHorizontalSpace = true;
		//gridData.grabExcessVerticalSpace = true;
		gridData.heightHint = 250;
		//gridData.widthHint = 500;
		tmvGroup.setLayoutData(gridData);
		
		GridLayout tmvLayout = new GridLayout();
		tmvLayout.numColumns = 2;
		tmvGroup.setLayout(tmvLayout);
		
		
		/*mapHolderGroup = new Group(tmvGroup, SWT.BORDER);
		//mapHolderGroup.setLocation(gap, gap*3);
		//mapHolderGroup.setBounds(gap, gap * 3, 300, 270);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.widthHint = 300;
		gridData.heightHint = 250;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		mapHolderGroup.setLayoutData(gridData);
		

		GridLayout mapLayout = new GridLayout();
		mapLayout.numColumns = 1;
		mapLayout.marginHeight = 0;
		mapLayout.marginWidth = 0;
		mapHolderGroup.setLayout(mapLayout);*/
		
		
		/*tmvLabel = new Label(mapHolderGroup,SWT.NONE);
		tmvLabel.setLocation(0,0);
		//tmvLabel.setBounds(gap,gap*3,300,270);		
		//tmvLabel.setLocation(gap,gap);
		tmvLabel.setBackground(new Color(display,242,255,179));
		tmvLabel.setText(loadMessage);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 250;
		gridData.widthHint = 300;
		tmvLabel.setLayoutData(gridData);
		tmvLabel.setVisible(true);*/
		
		// stumbler tab's map view
		mapView1 = new TrackedMapView(tmvGroup, SWT.NONE, daemon);
		mapView1.setVisible(true);
		mapView1.setShowBeacons(false);
		mapView1.setAutoScroll(true);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 1;
		mapView1.setLayoutData(gridData);
		
		
		Composite mapControls = new Composite(tmvGroup, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 1;
		//gridData.heightHint = 270;
		mapControls.setLayoutData(gridData);
		GridLayout mapControlLayout = new GridLayout();
		mapControlLayout.numColumns = 1;
		mapControls.setLayout(mapControlLayout);
		
		// zoom label and scale
		Label tmvZoomLabel = new Label(mapControls,SWT.NONE);
		//tmvZoomLabel.setBounds(300+gap*3,gap*3,150,20);
		//tmvZoomLabel.setLocation(300+gap*3, gap*3);
		tmvZoomLabel.setText("Zoom Level");
		tmvZoomLabel.setVisible(true);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		tmvZoomLabel.setLayoutData(gridData);
		
		
		tmvZoomScale = new Scale(mapControls,SWT.HORIZONTAL);
		tmvZoomScale.setMinimum(0);
		tmvZoomScale.setMaximum(120);
		tmvZoomScale.setPageIncrement(20);
		tmvZoomScale.setSelection(60);
		tmvZoomScale.setVisible(true);
		tmvZoomScale.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		        checkZoomLevel();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) { }
		});
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		gridData.widthHint = 150;
		gridData.heightHint = 20;
		tmvZoomScale.setLayoutData(gridData);
		
		
		// check boxes
		Composite positionComp = new Composite(mapControls, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		positionComp.setLayoutData(gridData);
		tmvBtnPosition = new Button(positionComp,SWT.CHECK);
		tmvBtnPosition.setBounds(0,0,20,20);
		tmvBtnPosition.setVisible(true);
		Label tmvLblPosition = new Label(positionComp,SWT.NONE);
		tmvLblPosition.setText("Show My Position");
		tmvLblPosition.setBounds(25, 0, 140, 15);
		tmvLblPosition.setVisible(true);
		
		Composite trackComp = new Composite(mapControls, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		trackComp.setLayoutData(gridData);
		tmvBtnTrack = new Button(trackComp,SWT.CHECK);
		tmvBtnTrack.setBounds(0,0,20,20);
		tmvBtnTrack.setVisible(true);
		tmvBtnTrack.setEnabled(false);
		Label tmvLblTrack = new Label(trackComp,SWT.NONE);
		tmvLblTrack.setText("Track My Movement");
		tmvLblTrack.setBounds(25,0,140,15);
		tmvLblTrack.setVisible(true);
		
		Composite scrollComp = new Composite(mapControls, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 1;
		gridData.verticalSpan = 1;
		scrollComp.setLayoutData(gridData);
		scrollComp.setVisible(true);
		tmvBtnScroll = new Button(scrollComp, SWT.CHECK);
		tmvBtnScroll.setBounds(0,0,20,20);
		tmvBtnScroll.setVisible(true);
		Label tmvLblScroll = new Label(scrollComp, SWT.NONE);
		tmvLblScroll.setText("Auto-Scroll Map");
		tmvLblScroll.setBounds(25,0,140,15);
		tmvLblScroll.setVisible(true);
		boolAutoScroll = true;
		tmvBtnScroll.setSelection(true);
		
		tmvBtnScroll.addSelectionListener(new SelectionListener() {
		    public void widgetSelected(SelectionEvent e) {
		    		toggleAutoScroll();
		    }
		    public void widgetDefaultSelected(SelectionEvent e) { }
		});
		
		/*
		 * MAP TAB GUI SETUP
		 */

		
		// map tab's big map view
		/*mapView2 = new TrackedMapView(item2Comp, SWT.NONE, daemon);
		mapView2.setLayoutData(new GridData(GridData.FILL_BOTH));
		mapView2.setBounds(gap,gap,shell.getClientArea().width-gap*5,shell.getClientArea().height-gap*8);
		mapView2.setVisible(true);
		mapView2.setShowBeacons(false);
		mapView2.setAutoScroll(true);*/
		if(tmvBigLabel != null && !tmvBigLabel.isDisposed()) {
		    tmvBigLabel.dispose();
		    tmvBigLabel = null;
		}
		//phantomHolder = new Composite(item2Comp, SWT.NONE);
		//phantomHolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		phantom = new PhantomView(mapView1.getHolder(), item2Comp, SWT.NONE);
		phantom.setLayoutData(new GridData(GridData.FILL_BOTH));
		item2Comp.layout();
		
		int[] weights = new int[tableCount + 1];
		int i;
		for(i = 0; i < tableCount; i++) {
			weights[i] = 60 / tableCount;
		}
		weights[tableCount] = 40;
		sash.setWeights(weights);
		
//		Object[] children = sash.getChildren();
//		System.out.println("length " + children.length);
//		for (int z = 0; z < children.length; z++){
//			System.out.println("child " + z + ": " + children[z].getClass().toString());
//		}
////		System.out.println("look at my children: " + children);
	
		item1Comp.layout();
		
	}
	
	
	public void widgetDefaultSelected(SelectionEvent ee){
		System.out.println("widgetDefaultSelected fired");
	}
	
   private static long getDefaultTimeout() {
        long timeout = 2000;
        try {
            timeout = Long.parseLong(
                    PlacelabProperties.get("placelab.StumblerFunnel.timeout"));
        } catch (NumberFormatException nfe) {
            
        }
        return timeout;
    }
    
	protected void beginStumbling() throws IOException {
	    eventQueue = new LinkedList();
	    
	    wifi = new WiFiSpotter();
	    boolean stumbleBT = PlacelabProperties.get("placelab.stumble_bluetooth").
							equalsIgnoreCase("true");
	    String btgsm = PlacelabProperties.get("placelab.btgsmaddress");
	    boolean stumbleGSM = (btgsm != null) && (btgsm.length() > 0);
	    if (stumbleBT) {
		    bluetooth = new BluetoothSpotter();
		} 
	    if(stumbleGSM) {
	    	System.out.println("Making RemoteGSM...");
	    	gsm = new RemoteGSMSpotter(btgsm,false);
	    }
	    try {
			wifi.open();
		} catch (SpotterException e) {
			wifi = null;
			e.printStackTrace();
		}
		//wifi = null;
		if(bluetooth != null) {
			try {
				bluetooth.open();
			} catch (SpotterException e) {
				bluetooth = null;
				e.printStackTrace();
			}
		}
		if(gsm != null) {
			try {
				gsm.open();
			} catch (SpotterException e) {
				gsm = null;
				e.printStackTrace();
			}
		}
		
	    logWriter = new LogWriter();
	    
	    funnel = new StumblerFunnel(getDefaultTimeout());
	    
	    setupGPS();
	    
	    // the 500 argument to a SpotterExtension when the second argument is true
	    // means that we know wifi returns with a quickness, but nonetheless we don't
	    // want it to be polled more frequently than once every 500ms (
	    // (this is because there is a 300ms minimum for wifi scans on windows
	    //  and in practice this is a bit longer.
	    // -- lowest common denominator and all that)
	    if(wifi != null) {
	        long scanMinimum = 500;
	        long timeout = getDefaultTimeout();
	        if(scanMinimum < timeout) scanMinimum = timeout;
	    	wifiExtension = new SpotterExtension(wifi, true, -1);
	    	try {
	    		wifiExtension.open();
	    	} catch (SpotterException se) {
	    		se.printStackTrace();
	    	}
	    	funnel.addDependentSpotter(wifiExtension);
	    }
	    if(gsm != null) {
	    	gsmExtension = new SpotterExtension(gsm, false, 20000);
	    	try {
	    		gsmExtension.open();
	    	} catch (SpotterException se) {
	    		se.printStackTrace();
	    	}
	    	funnel.addIndependentSpotter(gsmExtension);	    	
	    }
	    if(bluetooth != null) {
	    	// we say that bluetooth spotters aren't stale until 11 seconds
	    	bluetoothExtension = new SpotterExtension(bluetooth, false, 11000);
	    	funnel.addIndependentSpotter(bluetoothExtension);
	    }
	    funnel.addUpdateListener(logWriter);
	    funnel.addShutdownListener(logWriter);
	    funnel.addUpdateListener(this);
	    if(Cmdline.getArg("audio") != null) {
	        funnel.addUpdateListener(new AudioNotifier());
	    }
	    funnel.start();
	}
	
	protected void resetGPS() {
	    PlacelabProperties.set("placelab.gps_device", "");
	    setupGPS();
	}
	
	protected void setupGPS() {
	    if(gps != null) {
	        try {
	            gpsExtension.close();
	        } catch (SpotterException e) {
	            errorDialog("GPS Error", e);
	        }
	        if(funnel != null) {
	            funnel.suspendListen();
	            funnel.removeSpotter(gpsExtension);
	            funnel.resumeListen();
	        }
	        gps = null;
	        gpsExtension = null;
	    }
	    
	    if(PlacelabProperties.get("placelab.gps_device").length() == 0) {
		    doGPSPref();
		}
		
		if(bluetooth != null && // if i couldn't open the bluetooth spotter, i can't open bt gps
				PlacelabProperties.get("placelab.gps_device").equalsIgnoreCase("bluetooth")) {
	        // specifying false here means that the InquiryBluetoothGPSSpotter doesn't
	    	// do inquiries for bluetooth gps devices.  Instead, BluetoothSpotter
			// feeds it readings through the SpotterListener interface.  In this way,
	    	// the two don't fight for who gets to do inquiries.
			// the 30000 means that if it can't establish connections with any newly
			// found gps devices, it will wait 30 seconds before trying the same ones
			// again, so long as those same ones are still being seen.
	    	gps = new ListenerBluetoothGPSSpotter(bluetooth);
	    } else if(PlacelabProperties.get("placelab.gps_device").length() >= 0) {
	        gps = new SerialGPSSpotter();
	    }
	    if(gps != null) {
			try {
				gps.open();
			} catch (SpotterException e) {
				gps = null;
				e.printStackTrace();
			}
	    }
	    
	    if(gps != null) {
	    	gpsExtension = new SpotterExtension(gps, false, SpotterExtension.GPS_STALE_TIME);
	    	funnel.suspendListen();
	    	funnel.addTriggerSpotter(gpsExtension);
	    	funnel.resumeListen();
	    }
	    
	    // XXX: hack to get timeout lines
	    ArrayList criticalSpotters = new ArrayList();
	    if(gps != null) criticalSpotters.add(gps);
	    logWriter.criticalSpotters = criticalSpotters;
	}
	
	protected boolean doGPSPref() {
	    // get the serial ports for the system
	    ArrayList ports = new ArrayList();
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while(e.hasMoreElements()) {
            CommPortIdentifier cpi = (CommPortIdentifier)e.nextElement();
            if(cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                ports.add(cpi.getName());
            }
        }
        String[] portsS = new String[ports.size()];
        ports.toArray(portsS);
        String q1 = "Scan for nearby bluetooth gps devices";
        String q2 = "Or, use a serial gps device (choose port)";
        String q3 = "Choose the serial port your gps is connected to";
	    // run a dialog to get the user's preference about gps
	    ArrayList questions = new ArrayList(); 
	    if(bluetooth != null) {
	        // this computer can support using InquiryBluetoothGPSSpotter
	        QuestionBox.QuestionPackage qp = new QuestionBox.QuestionPackage(
	                q1, true);
	        questions.add(qp);
	        qp = new QuestionBox.QuestionPackage(
	                q2,
	                portsS, false);
	        questions.add(qp);
	    } else {
	        QuestionBox.QuestionPackage qp = new QuestionBox.QuestionPackage(
	                q3,
	                portsS, false);
	        questions.add(qp);
	    }
	    QuestionBox qb = new QuestionBox(shell, questions, "Setup GPS Preferences");
	    qb.setButtons(new String[] { "Don't use GPS",
	            "Use but don't save",
	            "Use and save"});
	    Hashtable answers = (Hashtable)qb.open();
	    if(answers == null || qb.getButtonChosen() == 0) {
	        return false;
	    } else {
	        Boolean b = (Boolean)answers.get(q1);
	        if(b != null && b.booleanValue()) {
	            PlacelabProperties.set("placelab.gps_device", "bluetooth");
	        } else {
	            String port = (String)answers.get(q2);
	            if(port == null) port = (String)answers.get(q3);
	            PlacelabProperties.set("placelab.gps_device", port);
	        }
	        if(qb.getButtonChosen() == 2) PlacelabProperties.synchronize();
	        return true;
	    }
	}
	
	private String basename(String path) {
		return path.substring(path.lastIndexOf(File.separatorChar) + 1,
					   		path.length());
	}
	
	protected void doSaveAs() {
	    String path = askUserForNewFile();
	    if(path == null) return;
	    try {
	        logWriter.setOutputFile(path);
	    } catch (IOException e) {
	        this.errorDialog("Couldn't save trace log", e);
	    }
	    shell.setText("Place Lab Stumbler - " + basename(path));
	    lastSavePath = path;
	}
	
	protected void doSubmitLog() {
	    if(lastSavePath == null) {
	        doSaveAs();
	        if(lastSavePath == null) return;
	    }
	    ArrayList questions = new ArrayList();
	    HTTPTransport transport = new HTTPTransport(lastSavePath, null);
	    questions.add(new QuestionBox.QuestionPackage("Username", transport.username, 40, false));
	    questions.add(new QuestionBox.QuestionPackage("Password", transport.passwd, 40, true));
	    questions.add(new QuestionBox.QuestionPackage("Trace Description", "Submission from Place lab Stumbler", 400, false));
	    QuestionBox dialog = new QuestionBox(shell, questions, "Enter your placelab.org account info");
	    Hashtable results = (Hashtable)dialog.open();
	    if(results == null || dialog.getButtonChosen() == 0) return;
	    transport.username = (String)results.get("Username");
	    transport.passwd = (String)results.get("Password");
	    transport.description = (String)results.get("Trace Description");
	    try {
	        new ProgressMonitorDialog(shell).run(true, true, transport);
	    } catch(InvocationTargetException e) {
	        errorDialog("Your log was not accepted", e);
	    } catch(InterruptedException e) {
	        // user canceled it.
	    }
	}
	
	public void run() {
		doEventLoop();
		shutdown = true;
		funnel.shutdown();
		daemon.shutdown();
		System.exit(0);
	}
	
	protected volatile boolean shutdown = false;

	protected static long lastStumblerUpdate = 0;
	
    public void stumblerUpdated(Hashtable measurements) {

        long now = System.currentTimeMillis();
        Logger.println("Time since last stumbler update " + (now - lastStumblerUpdate), Logger.MEDIUM);
        lastStumblerUpdate = now;
        
        //System.out.println("Got measurements: " + measurements);
        synchronized(eventQueue) {
            eventQueue.addLast(measurements);
        }
        if(display == null) return;
        synchronized(display) {
    		display.wake();
        }

    }
    
    protected static long lastUpdate = 0;
    
    protected void doEventLoop() {
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                boolean updateTable = false;
                synchronized(eventQueue) {
                    if(eventQueue.size() > 0) {
                        daemon.pulse();
                        latestMeasurements = (Hashtable)eventQueue.removeFirst();
                        // and anything else we want to do here
                        //System.out.println("latest measurements: " + latestMeasurements.values());
                        if (boolStumbleOnOff){
                        	updateTable = true;
                        }
                    }
                }
                if(updateTable) {
                    long now = System.currentTimeMillis();
                    Logger.println("Time since last table update: " + (now - lastUpdate), Logger.HIGH);
                    lastUpdate = now;
                    updateUI();
                }
                display.sleep();
            }
        }
    }
    
	public void setShellSize(Shell s, Display d) {
		Rectangle screen= shell.getDisplay().getClientArea();
		
		int w=screen.width;
		int h=screen.height;
		if (w>850) {
			w=850;
		} 
		if (h>550) {
			h=750;
		}
		s.setSize(w,h);
	}

	/* (non-Javadoc)
	 * @see org.placelab.client.tracker.EstimateListener#estimateUpdated(org.placelab.client.tracker.Tracker, org.placelab.client.tracker.Estimate)
	 */
	public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
	    if(e instanceof CompoundEstimate) {
	        e = centroidTracker.getEstimate();
	    }
		TwoDCoordinate coord = (TwoDCoordinate) e.getCoord();
		String lat,lon;
		lat = NumUtil.doubleToString(coord.getLatitude(),4);
		lon = NumUtil.doubleToString(coord.getLongitude(),4);
		
		//System.out.println("Place Lab thinks: " + lat + " " + lon);
		plEstimateText.setText("Place Lab Estimated Latitude: " + lat + "    Estimated Longitude: " + lon);
		
	}
	
	protected void updateUI() {
        if(gps != null && latestMeasurements.containsKey(gps)) {
        	//System.out.println("got gps");
            latestGPSMeasurement = (GPSMeasurement)latestMeasurements.get(gps);
        }
	    if(wifi != null)
	    	wifiTableController.addMeasurement((BeaconMeasurement)latestMeasurements.get(wifi),
	            (isGPSStale()) ? null : latestGPSMeasurement);
	    if(bluetooth != null) {
	    	BeaconMeasurement btMeas = (BeaconMeasurement)latestMeasurements.get(bluetooth);
	    	//if(btMeas != null) System.out.println("Got bt: " + btMeas.toLogString());
	    	bluetoothTableController.addMeasurement((BeaconMeasurement)latestMeasurements.get(bluetooth),
	    			(isGPSStale()) ? null : latestGPSMeasurement);
	    }
//	    if(bluetooth != null && (bluetooth instanceof BluetoothGSMSpotter)) {
//	    	BeaconMeasurement gsmMeas = (BeaconMeasurement)latestMeasurements.get(bluetooth);
//	    	gsmTableController.addMeasurement(gsmMeas, 
//	    			(isGPSStale()) ? null : latestGPSMeasurement);
//	    }
	    if(gsm != null) {
	    	BeaconMeasurement gsmMeas = (BeaconMeasurement)latestMeasurements.get(gsm);
	    	gsmTableController.addMeasurement(gsmMeas, 
	    			(isGPSStale()) ? null : latestGPSMeasurement);
	    }
	    updateGPSText();
	    //updateBeaconIcons();
	    updatePositionTracker();
	    item1Comp.layout();
	}
	
	protected boolean isGPSStale() {
	    return latestGPSMeasurement == null ||
	    	(System.currentTimeMillis() - latestGPSMeasurement.getTimestamp()) > SpotterExtension.GPS_STALE_TIME;
	}
	
	protected void updatePositionTracker() {
	    GPSMeasurement gpsMeas;
	    if(isGPSStale()) {
	        gpsMeas = new GPSMeasurement(0L, TwoDCoordinate.NULL);
	    } else {
	        gpsMeas = latestGPSMeasurement;
	    }
	    positionTracker.updateEstimate(gpsMeas);
	}
	
	protected void updateBeaconIcons() {
	    // this is kind of a kludge, since the BeaconHistory objects are currently being collated
	    // by the BeaconTableControllers, but thats probably ok.  I just need to have a look at them here
	    // XXX: this should be &&, i just have it changed temporarily while i work
	    // on phantoms - fats
	    if(mapView1 == null || mapView2 == null) {
	        // then don't waste time
	        return;
	    }
	    Hashtable temp = new Hashtable();
	    if(mapView1 != null) {
	        mapView1.getHolder().freeze();
	    }
	    if(mapView2 != null) {
	        mapView2.getHolder().freeze();
	    }
	    Iterator i = wifiTableController.active.keySet().iterator();
	    while(i.hasNext()) {
	        String id = (String)i.next();
	        // if id is in beaconIcons, then poke the icon to reflect the latest data
	        if(beaconIcons.containsKey(id)) {
	            BeaconIcons bi = (BeaconIcons)beaconIcons.get(id);
	            bi.updateAll(true);
	            // remove it from lastActiveBeaconIcons, if it exists, and add it to temp
	            // this indirection prevents me from doing any more computation than I absolutely have to
	            // with regards to glyph drawing
	            temp.put(id, bi);
	            lastActiveBeaconIcons.remove(id);
	        } else {
	            // this is a new ap that doesn't have an icon yet.  build it up, one for each
	            // mapview.
	            BeaconHistory bh = (BeaconHistory)wifiTableController.history.get(id);
	            BeaconIcons bi = new BeaconIcons();
	            // isn't bp1 a great name for a variable?  It rolls off the tongue
	            // so nice ...
	            if(mapView1 != null) {
	                BeaconPlace bp1 = new BeaconPlace(mapView1, bh, mapper, true);
	            	bi.addBeaconPlace(bp1);
	            }
	            if(mapView2 != null) {
	                BeaconPlace bp2 = new BeaconPlace(mapView2, bh, mapper, true);
	                bi.addBeaconPlace(bp2);
	            }
	            beaconIcons.put(id, bi);
	            temp.put(id, bi);
	        }
	        if(mapView1 != null)
	            mapView1.getHolder().thaw();
	        if(mapView2 != null)
	            mapView2.getHolder().thaw();
	    }
	    // now for everthing that was in lastActiveBeaconIcons, go through and set it to
	    // not active because it isn't active anymore
	    Enumeration e = lastActiveBeaconIcons.elements();
	    while(e.hasMoreElements()) {
	        BeaconIcons bi = (BeaconIcons)e.nextElement();
	        bi.updateAll(false);
	    }
	    // now the icons in temp are the ones that are active for this round
	    // so lastActiveBeaconIcons to them
	    lastActiveBeaconIcons = temp;
	}
	
	/**
	 * When the map is changed for a mapview, all of the BeaconPlace glyphs
	 * to be updated to move to their proper locations
	 */
	protected void updateAllBeaconIcons() {
	    /*Enumeration e = beaconIcons.keys();
	    while(e.hasMoreElements()) {
	        Object id = e.nextElement();
	        BeaconIcons bi = (BeaconIcons)beaconIcons.get(id);
	        bi.updateAll(wifiTableController.active.containsKey(id));
	    }*/
	}
	
	/**
	 * When a mapview is first loaded, all beacons to date must be plotted on it.
	 * They cannot be made in advance, because swt does not support making a widget
	 * without a parent.
	 */
	protected void createBeaconIcons(MapView onView) {
	    onView.getHolder().freeze();
	    Iterator i = wifiTableController.history.values().iterator();
	    while(i.hasNext()) {
	        BeaconHistory bh = (BeaconHistory)i.next();
	        // check to see if there is already a BeaconIcons package
	        // for this element
	        // note that a BeaconHistory object hashes to the same place as
	        // its bssid string does, so this works
	        BeaconIcons bi = (BeaconIcons)beaconIcons.get(bh);
	        if(bi == null) {
	            bi = new BeaconIcons();
	            beaconIcons.put(bh, bi);
	        }
	        boolean isActive = wifiTableController.active.containsKey(bh);
	        BeaconPlace bp = new BeaconPlace(onView, bh, mapper, isActive);
	        bi.addBeaconPlace(bp);
	        if(isActive) {
	            lastActiveBeaconIcons.put(bh, bi);
	        }
	    }
	    onView.getHolder().thaw();
	}
	
	
	protected void updateGPSText() {
	    String text = null;
	    if(latestGPSMeasurement == null) {
	        text = "GPS not connected";
	    } else if(isGPSStale()) {
	        text = "No recent GPS Measurements (last one at " + 
	        	BeaconTableController.humanReadableTime(latestGPSMeasurement.getTimestamp()) + ")";
	    } else {
	        String latS = "unknown", lonS = "unknown";
	        if(!latestGPSMeasurement.getPosition().isNull()) {
	            latS = NumUtil.doubleToString(
	                    ((TwoDCoordinate)latestGPSMeasurement.getPosition()).getLatitude(), 4);
	            lonS = NumUtil.doubleToString(
	                    ((TwoDCoordinate)latestGPSMeasurement.getPosition()).getLongitude(), 4);
	        }
	        text = "GPS Estimated Latitude: " + latS + "    Estimated Longitude: " + lonS 
	        	+ "    " + (latestGPSMeasurement.isValid() ? "(fix obtained)" : "(fix not obtained)");
	    }
	    if(gps != null && gps instanceof BluetoothGPSSpotter) {
	    	text += " (bluetooth gps state: " + ((BluetoothGPSSpotter)gps).getState()
	    	+ ")";
        }
	    gpsEstimateText.setText(text);
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
		
		MenuItem saveItem = new MenuItem(fileMenu, SWT.PUSH);
		saveItem.setText("Set log file ...");
		saveItem.setAccelerator(commandKey | 'S');
		saveItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doSaveAs();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				doSaveAs();
			}
		});
		
		// blank space
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem submitItem = new MenuItem(fileMenu, SWT.PUSH);
		submitItem.setText("Submit log to placelab.org ...");
		submitItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				doSubmitLog();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				doSubmitLog();
			}
		});
		
		// blank space
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("Open Mapwad...");
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
		
		MenuItem editItem = new MenuItem(bar, SWT.CASCADE);
		editItem.setText("Edit");
		Menu editMenu = new Menu(bar);
		
		MenuItem gpsPrefItem = new MenuItem(editMenu, SWT.PUSH);
		gpsPrefItem.setText("Reset GPS Preferences ...");
		gpsPrefItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				resetGPS();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				resetGPS();
			}
		});
		editItem.setMenu(editMenu);
		
		buildMapMenus();
		shell.setBounds(shell.getBounds());
	}
	
	protected void buildMapMenus() {
		int commandKey = SWT.CONTROL;
		if(BrowserControl.isMacPlatform()) commandKey = SWT.COMMAND;
		
		TrackedMapView mapView = null;
		WadData wad;
	    // find out what mapView is in the front
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		
		// cut out early and don't have View, map or Place menus if there isn't anything
		// loaded yet
		if(wad == null || mapView == null || mapView.getMapData() == null) return;
		
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
		beaconShowItem.setSelection(mapView1.getShowBeacons());
		//
		MenuItem particlesShowItem = new MenuItem(viewMenu, SWT.CHECK);
		particlesShowItem.setText("Show Particles");
		particlesShowItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleParticles();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				toggleParticles();
			}
		});
//		particlesShowItem.setSelection(mapView1.getShowParticles());
		MenuItem autoScrollItem = new MenuItem(viewMenu, SWT.CHECK);
		autoScrollItem.setText("Auto Scroll Map");
		autoScrollItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleAutoScroll();
				tmvBtnScroll.setSelection(!tmvBtnScroll.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				toggleAutoScroll();
				tmvBtnScroll.setSelection(!tmvBtnScroll.getSelection());
			}
		});
		autoScrollItem.setSelection(mapView1.getAutoScroll());
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
		autoZoomItem.setSelection(mapView1.getAutoZoom());
		//
		MenuItem zoomInItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomInItem.setText("Zoom In");
		zoomInItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				mapView1.zoomIn();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				mapView1.zoomIn();
			}
		});
		//
		MenuItem zoomOutItem = new MenuItem(viewMenu, SWT.PUSH);
		zoomOutItem.setText("Zoom Out");
		zoomOutItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				mapView1.zoomOut();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				mapView1.zoomOut();
			}
		});
		viewItem.setMenu(viewMenu);
	    
		// build map menu
		MenuItem mapItem = new MenuItem(bar, SWT.CASCADE);
		mapItem.setText("Maps");
		Menu mapMenu = new Menu(bar);
		Enumeration e = wad.getMaps().keys();
		String selected = mapView1.getMapData().getName();
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
		Hashtable defaultPlaceSets = wad.getDefaultPlaceSets();
		e = wad.getPlaceSetNames();
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
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		MenuItem item = (MenuItem)e.getSource();
		String mapName = item.getText();
		MapBacking map = wad.getMap(mapName);
		if(mapName != null) {
			//shell.setText(mapName);
			mapView.setMapData(map);
			// despite that this runs for all the mapviews, if a mapview hasn't had its
			// view changed, there shouldn't be anything happening for it down the line
			// so its ok.
			updateAllBeaconIcons();
		}
		tmvGroup.layout();
	}
	protected void placeAction(SelectionEvent e) {
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		Hashtable selectedPlaces = new Hashtable();
		Enumeration i = placeItems.elements();
		while(i.hasMoreElements()) {
			MenuItem mi = (MenuItem)i.nextElement();
			if(mi.getSelection()) {
				Hashtable placeSet = wad.getPlaceSet(mi.getText());
				selectedPlaces.put(mi.getText(), placeSet);
			}
		}
		mapView1.setPlaceSets(selectedPlaces);
	}
	
	protected void toggleBeacons() {
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		mapView.setShowBeacons(!mapView1.getShowBeacons());
	}
	protected void toggleAutoScroll() {
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		mapView1.setAutoScroll(!mapView.getAutoScroll());
		buildMenu();
	}
	protected void toggleAutoZoom() {
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
		mapView1.setAutoZoom(!mapView1.getAutoZoom());
		buildMenu();
	}
	protected void toggleParticles() {
	    TrackedMapView mapView;
	    WadData wad;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
//		mapView1.setShowParticles(!mapView1.getShowParticles());
	}
	
	protected void loadWad() {
	    TrackedMapView mapView;
	    WadData wad;
	    String path = askUserForFile();
	    if(path == null) return;
	    if(mainTabFolder.getSelectionIndex() == 0) {
	        mapView = mapView1;
	        wad = wad1;
	    } else {
	        mapView = mapView2;
	        wad = wad2;
	    }
	    if(mapView == null) {
	        this.buildMapViews();
		    if(mainTabFolder.getSelectionIndex() == 0) {
		        mapView = mapView1;
		        wad = wad1;
		    } else {
		        mapView = mapView2;
		        wad = wad2;
		    }
	    }
		loadWad(path, null, mapView);
	}
	
	protected void loadWad(String path, String mapName, TrackedMapView view) {
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
				Logger.println("Map: " + mapName + " not found. Available maps are:\n", Logger.LOW);
				for (Enumeration en = temp.getMaps().keys(); en.hasMoreElements();) {
					Logger.println("  - " + en.nextElement().toString(), Logger.LOW);
				}
				//System.exit(1);
				Logger.println("Choosing an arbitrary map instead", Logger.LOW);
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
		if(view == mapView1) {
		    wad1 = temp;
			mapView1.setVisible(true);
		}
		else wad2 = temp;
		
		view.setMapData(mapBack);
		view.setPlaceSets(defaultPlaces);
		//this.createBeaconIcons(view);
		view.setVisible(true);
		if(mapView2 != null && mapView1 != null) {
			if(view == mapView1 && mapView2.getMapData() == null) {
				//errorDialog("entered view==mapView1 && mapView2.getMapData() == null");
			    mapView2.setMapData(mapBack);
			    mapView2.setPlaceSets(defaultPlaces);
			    mapView2.setVisible(true);
			    tmvBigLabel.setVisible(false);
			    wad2 = temp;
			    //this.createBeaconIcons(mapView2);
			} else if(view == mapView2 && mapView1.getMapData() == null) {
			    mapView1.setMapData(mapBack);
			    mapView1.setPlaceSets(defaultPlaces);
				mapView1.setVisible(true);
			    wad1 = temp;
			    //this.createBeaconIcons(mapView1);
			}
		}
		buildMenu();
	}
	
	protected void shutdown() {
	    
	    display.close();
	}
	
	protected class BeaconIcons {
	    public Hashtable icons;
	    public BeaconIcons() {
	        icons = new Hashtable();
	    }
	    public void addBeaconPlace(BeaconPlace place) {
	        icons.put(place.mapView, place);
	    }
	    public void updateAll(boolean active) {
	        for(Enumeration e = icons.elements(); e.hasMoreElements();) {
	            BeaconPlace bp = (BeaconPlace)e.nextElement();
	            bp.updateLocation();
	            bp.setActive(active);
	        }
	    }
	    public BeaconPlace getBeaconPlace(MapView forMapView) {
	        return (BeaconPlace)icons.get(forMapView);
	    }
	}
    
}
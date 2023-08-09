package org.placelab.stumbler;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.collections.ArrayList;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.SerialGPSSpotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.util.Cmdline;
import org.placelab.util.ns1.DHeap;

public class PlacelabStumbler implements StumblerFunnelUpdateListener {

    LogWriter log;
    SpotterExtension wifi;
    SpotterExtension gps;
    StumblerFunnel funnel;
    DHeap logQueue;
    
    PlacelabWithProxy daemon;
    CentroidTracker tracker;
//    GPSHinter hinter;
    Mapper mapper;
    
    public PlacelabStumbler() throws IOException {
        logQueue = new DHeap(new MeasurementComparator(), 4);
        wifi = new SpotterExtension(new WiFiSpotter(), true, 0);
        gps  = new SpotterExtension(new SerialGPSSpotter(), false, SpotterExtension.GPS_STALE_TIME);
        try {
        	wifi.open();
        } catch (SpotterException ex) {
        	ex.printStackTrace();
        	System.out.println("*** Trouble opening the wifi spotter");
        }
        try {
            gps.open();
        } catch (SpotterException ex) {
        	ex.printStackTrace();
        	System.out.println("*** Trouble opening the gps spotter");
        }
        mapper = CompoundMapper.createDefaultMapper(true, true);;
        tracker = new CentroidTracker(mapper);
        daemon = new PlacelabWithProxy(wifi, tracker, mapper, -1);
        String logFile = nextAvailableName(Cmdline.getArg("log"));
        System.out.println("logging to " + logFile);
        funnel = new StumblerFunnel();
        log = new LogWriter(logFile);
        log.criticalSpotters = new ArrayList();
        log.criticalSpotters.add(gps.getSpotter());
//        hinter = new GPSHinter(funnel, daemon, gps, tracker);
        funnel.addDependentSpotter(wifi);
        funnel.addTriggerSpotter(gps);
        funnel.addUpdateListener(this);
        funnel.addUpdateListener(log);
        if (Cmdline.getArg("a") != null) {
	        AudioNotifier an = new AudioNotifier();
	        funnel.addUpdateListener(an);
        }
    }
    
    public static String nextAvailableName(String name) {
    	// Find the next file name so we don't stomp over the old file
    	int i = 2;
    	File f = new File(name);
    	String rv = name;
    	while (f.isFile() || f.isDirectory()) {
    		rv = name + "." + i;
    		f = new File(rv);
    		i++;
    	}
    	return rv;
    }
    
    public void start() {
        funnel.start();
    }
    
    public void stumblerUpdated(Hashtable updates) {
        daemon.pulse();
    }
    
    public static void main(String[] args) {
        Cmdline.parse(args);
        try {
            PlacelabStumbler ps = new PlacelabStumbler();
            ps.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}

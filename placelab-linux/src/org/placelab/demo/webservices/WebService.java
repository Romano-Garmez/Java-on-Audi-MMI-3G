package org.placelab.demo.webservices;

import java.io.IOException;

import org.placelab.client.PlacelabWithProxy;
import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.Measurement;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.mapper.WigleMapper;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.util.Cmdline;

public class WebService implements EstimateListener {
    private PlacelabWithProxy placelab;
    private Estimate estimate=null;

    public WebService(String logFile, String mapperName) {
        try {
        	Mapper m = null;
        	if (mapperName != null && mapperName.equalsIgnoreCase("wigle")) {
        		m = new WigleMapper();
        	}
            if (logFile == null) {
            	if (m == null) {
            		m = CompoundMapper.createDefaultMapper(true,true);
            	}
                placelab = new PlacelabWithProxy(new WiFiSpotter(), new CentroidTracker(m), m, 2000);
//                placelab = new PlacelabWithProxy(null, m, 2000);
            } else {
                placelab = new PlacelabWithProxy(LogSpotter.newSpotter(logFile), 
                                        null, // use the default tracker
										m, // use the default mapper
                                        2000  // poll spotter every two seconds
                                        );
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        placelab.addEstimateListener(this);
    }

    public void estimateUpdated(Tracker t, Estimate e, Measurement m) {
        estimate = e;
    }

    public PlacelabWithProxy getDaemon() {
        return placelab;
    }
    public TwoDCoordinate getEstimatedPosition() {
        if (estimate == null) {
            return TwoDCoordinate.NULL;
        } else {
            return (TwoDCoordinate)estimate.getCoord();
        }
    }

    public void start() {
        placelab.createProxy();
    }

    public static void main(String[] args) {
        WebService ws;
        Cmdline.parse(args);
        ws = new WebService(Cmdline.getArg("log"), Cmdline.getArg("mapper"));
        new MapquestServlet(ws);
        new TerraServerServlet(ws);
        new GoogleServlet(ws);
        new IndexServlet(ws);
        ws.start();
    }
}

package org.placelab.client;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.placelab.client.tracker.CentroidTracker;
import org.placelab.client.tracker.Estimate;
import org.placelab.client.tracker.EstimateListener;
import org.placelab.client.tracker.Tracker;
import org.placelab.core.Measurement;
import org.placelab.core.ShutdownListener;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.proxy.CoreServlet;
import org.placelab.proxy.ProxyServletEngine;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;
import org.placelab.util.Cmdline;

/**
 * A useful class that manages a spotter and a set of trackers for the program.
 * Many simple application can use this as their core class. This object can be
 * constructed by explicitly passing in spotters and tracker, or by letting it
 * create default spotters and trackers. This object can also be configured to
 * either ping the spotter and trackers automatically at a predetermined
 * interval, or rely on manual pinging via the pulse method. This object also
 * has a side effect that it creates an
 * {@link org.placelab.proxy.ProxyServletEngine}that is active for the duration
 * of the execution. It is important to call the {@link #createProxy}method to ensure
 * that the proxy object is active.
 * 
 *  
 */
public class PlacelabWithProxy implements ShutdownListener, Runnable {
	private Spotter spotter;

	private Vector shutdowns;

	private boolean shuttingDown = false;

	private Tracker tracker;
	private Mapper  mapper;

	private CoreServlet coreServlet=null;

	public static int spotterDelay;

	/**
	 * This constructor creates a PlacelabWithProxy object that with a
	 * {@link org.placelab.spotter.WiFiSpotter}and a
	 * {@link org.placelab.tracker.CentroidTracker}. This object
	 * will pulse the spotter once every 2000 ms by default.
	 */
	public PlacelabWithProxy() throws IOException {
		this(newDefaultSpotter(), null, null, 2000);
	}
	private static Spotter newDefaultSpotter() {
		return new WiFiSpotter();
	}

	/**
	 * This constructor is similar to the no-arg constructor, except that it
	 * uses the tracker passed in and the pulse frequency that is passed in.
	 * Setting the spotterDelay to -1 will cause the PlacelabWithProxy object to not
	 * pulse automatically
	 *  
	 */
	public PlacelabWithProxy(Tracker tracker, Mapper mapper, int spotterDelay) throws IOException {
		this(newDefaultSpotter(), tracker, mapper,
				spotterDelay);
	}

	/**
	 * This constructor allows all arguments (spotters, trackers and pulse
	 * frequency) to be passed in.
	 *  
	 */
	public PlacelabWithProxy(Spotter _spotter, Tracker tracker, Mapper mapper, int spotterDelay)
			throws IOException {
		PlacelabWithProxy.spotterDelay = spotterDelay;
		shutdowns = new Vector();
		spotter = _spotter;
		try {
			spotter.open();
		} catch (SpotterException ex) {
			ex.printStackTrace();
			System.exit(1); //###ALM Spotter exception rewhack
		}
		addShutdownListener(new ShutdownListener() {
			public void shutdown() {
				try {
					spotter.close();
				} catch (SpotterException ex) {
					ex.printStackTrace();
				}
			}
		});
		if (tracker == null) {
			this.mapper = (mapper!=null ? mapper : CompoundMapper.createDefaultMapper(true, true));
			this.tracker = new CentroidTracker(this.mapper);
		} else {
			this.tracker = tracker;
			this.mapper  = mapper;
		}
		if (spotterDelay > 0) {
			new Thread(this).start();
		}
	}

	/**
	 * Start up the http proxy.
	 *  
	 */
	public void createProxy() {
		// Start up the proxy and default servlets
		coreServlet = CoreServlet.createAndRegister();
		ProxyServletEngine.setShutdownListener(this);
		ProxyServletEngine.startProxy(false);
	}

	/**
	 * Add a listener that will be called back when the tracker's position
	 * estimates change
	 */
	public void addEstimateListener(EstimateListener listener) {
		if (tracker != null) {
			tracker.addEstimateListener(listener);
		}
	}

	/**
	 * Return the tracker the PlaceLab object is maintaining
	 */
	public Tracker getTracker() {
		return tracker;
	}
	
	public Mapper getMapper() {
		return mapper;
	}
	
	public Spotter getSpotter() {
		return spotter;
	}
	/**
	 * This entry point causes a default PlacelabWithProxy object to be created at which
	 * point the program goes idle and does not exit. The created
	 * {@link org.placelab.proxy.ProxyServletEngine}makes this a good entry
	 * point for applications with no local UI that want to make use of the web
	 * proxy and servlet features.
	 *  
	 */
	public static void main(String args[]) {
		try {
			String inputLog = null;
			Cmdline.parse(args);
			if (args.length >= 1 && !args[0].substring(0, 2).equals("--")) {
				inputLog = args[0];
			} else
				inputLog = Cmdline.getArg("inputlog");
			PlacelabWithProxy daemon;
			if (inputLog == null) {
				System.out.println("Hey!!!!");
				daemon = new PlacelabWithProxy();
			} else {
				System.out.println("Hey2!!!!");
				daemon = new PlacelabWithProxy(LogSpotter.newSpotter(inputLog), null, null, 1000);
			}
			daemon.createProxy();
		} catch (Exception x) {
			System.err.println("Exception happened : " + x.toString());
		}
	}

	/**
	 * Shuts the proxy and pulsing off.
	 */
	public void shutdown() {
		shuttingDown = true;
		for (Enumeration it = shutdowns.elements(); it.hasMoreElements();) {
			ShutdownListener sl = (ShutdownListener) it.nextElement();
			sl.shutdown();
		}
	}

	/**
	 * Allows an application to register to find out when the place lab
	 * infrastructure is being shut down.
	 */
	public void addShutdownListener(ShutdownListener listener) {
		shutdowns.add(listener);
	}

	public Measurement pulse() {
		try {
			Measurement m = spotter.getMeasurement();
			if (m == null) {
				return null;
			}
			if (tracker.acceptableMeasurement(m)) {
			    tracker.updateEstimate(m);
			}
			Estimate e = tracker.getEstimate();
			if ((coreServlet != null) && (e != null)) {
				coreServlet.trackerPoke(e.getCoord());
			}
			return m;
		} catch (SpotterException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * This method implements the core execution loop for the pulsing.
	 */
	public void run() {

		while (!shuttingDown) {
			// wait a bit
			try {
				Thread.sleep(spotterDelay);
			} catch (Exception ex) {
				;
			}
			pulse();
		}

	}

}
package org.placelab.demo.mapview;

import org.eclipse.swt.widgets.Display;
import org.placelab.client.PlacelabWithProxy;
import org.placelab.core.PlacelabProperties;



/**
 * 
 *
 */
public class SpotterPoker implements Runnable {
	private Display display;
	private PlacelabWithProxy daemon;

	private int fps; // frames per second
	public boolean stopped;
	public int timerIntervalMillis;

	public SpotterPoker(Display disp, PlacelabWithProxy d) {
		display = disp;
		daemon  = d;
		try {
			fps = Integer.parseInt(PlacelabProperties.get("placelab.demofps"));
		} catch (Throwable ex) {
			fps = 3;
		}
		setFps(fps);	
	}
	
	public void setFps(int f) {
		fps = f;
		if(fps > 100) fps = 100;
		if (fps <= 0) {
			fps = 1;
			stopped = true;
		} else {
			stopped = false;
		}
		timerIntervalMillis = 1000/fps;
	}
	public int getFps() {
		return fps;
	}
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(!stopped) {
			daemon.pulse();
		}
		scheduleTimeout();
	}
	public void scheduleTimeout() {
		display.timerExec(timerIntervalMillis,this);
	}
}

package org.placelab.spotter;



/**
 * For testing purposes.
 */
public class FakeNMEAGPSSpotter extends NMEAGPSSpotter {
	private ScanThread t;
	public FakeNMEAGPSSpotter(String name) { ; }
	public void open() { ; }
	public void close() { ; }
	public void sendASentence(String sentence) {}
	protected void startScanningImpl() {
		t = new ScanThread();
		//t.setName(name);
		t.start();
	}
	protected void stopScanningImpl() {
		t.cancel();
		waitForThread(t);
	}
	
	private class ScanThread extends Thread {
		private boolean done=false;
		public synchronized void cancel() { done = true; }
		public synchronized boolean isDone() { return done; }
		public void run() {
			while (!isDone()) {
				lineAvailable("$GPGGA,021526,4739.7153,N,12218.9712,W,1,04,2.4,50.5,M,-18.4,M,,*4C");
				lineAvailable("$GPRMC,021526,A,4739.7153,N,12218.9712,W,0.0,108.8,160604,18.2,E,A*38");
				if (!isDone()) {
					try { Thread.sleep(500); } catch (InterruptedException e) {}
				}
			}
		}
	}
}

package org.placelab.demo.mapview;


/**
 * 
 *
 */
public class ReticleMotion implements Runnable {
	private int pixEstX=0, pixEstY=0, pixDeviation=0;
	private ReticleMotion previous;
	private ReticleOverlay ui=null;
	private int timeout=0, count;
	private final int STEPS=2;

	public ReticleMotion(ReticleOverlay ui, ReticleMotion prev, 
			     int x, int y, int dev, int timeout) {
	        if (ui.view.isDisposed()) return;
		if (prev != null) prev.stop();
		this.ui  = ui;
		previous = prev;
		pixEstX  = x;
		pixEstY  = y;
		pixDeviation = dev;
		this.timeout = timeout;
		count = 0;

		/*if (prev != null) 
					   prev.pixEstX+","+prev.pixEstY+"):"+
					   prev.pixDeviation+" to "+"("+
					   pixEstX+","+pixEstY+"):"+
					   pixDeviation);*/
		if (prev==null ||
		    (pixEstX==prev.pixEstX && pixEstY==prev.pixEstY &&
		     pixDeviation==prev.pixDeviation)) {
			ui.updateReticle(0,pixEstX, pixEstY, pixDeviation);
		} else {
		    ui.view.getDisplay().timerExec(timeout/(STEPS+1), this);
		}
	}

	public void stop() {
		if (!ui.view.isDisposed())
			ui.view.getDisplay().timerExec(-1, this);
	}

	private int step(int prev, int current, int step) {
		if (step > STEPS) step = STEPS;
		return (prev + (current - prev) * step / STEPS);
	}
	public void run() {
		/* timeout */
		
		count++;
		ui.updateReticle(0,step(previous.pixEstX, pixEstX, count), 
				 step(previous.pixEstY, pixEstY, count),
				 step(previous.pixDeviation, pixDeviation,
				      count));
		if (count < STEPS && !ui.view.isDisposed())
			ui.view.getDisplay().timerExec(timeout/(STEPS+1), this);
	}
}


/*
 * NOTE ON IMPORTS:
 * I've changed the way I do some things to use what I have
 * available.  I've also added a jface.jar to the lib directory.
 * The jface.jar is minimal in that it contains only what is necessary
 * to make this program work -- its not the whole jface.
 * The upshot of this is that it will work in the device developer
 * with no tweaking.
 * 
 * Also, with the hacked up jface jar it tends to throw an SWTException
 * when I put stuff on the clipboard.  It seems safe to ignore that,
 * I dunno what its problem is.  - james
 */

package org.placelab.demo.mapview;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphImage;
import org.placelab.util.swt.SwtScrolledComposite;


public class WadSpecifier extends SwtScrolledComposite {
	
	//private int x1, x2, x3, y1, y2, y3;
	private int[] x;
	private int[] y;
	private double[] lat;
	private double[] lon;
	private int clickNo;
	
	private double pixelsPerLat, pixelsPerLon;
	private double originLat, originLon;
	private double upperRightLat, upperRightLon;
	
	private Image mapImage;
	private GlyphImage mapImageGlyph;
	private GlyphHolder holder;
	
	public WadSpecifier(Composite parent, int style, String imagePath,
			int clicksReq) {
		super(parent, style |= SWT.H_SCROLL | SWT.V_SCROLL);
		clickNo = 0;
		x = new int[clicksReq];
		y = new int[clicksReq];
		lat = new double[clicksReq];
		lon = new double[clicksReq];
		holder = new GlyphHolder(this, SWT.NONE);

		mapImage = new Image(parent.getDisplay(), imagePath);
		mapImageGlyph = new GlyphImage(holder, SWT.NONE);
		
		mapImageGlyph.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {
				mouseClicked(e);
			}});
		
		mapImageGlyph.setImage(mapImage, 0, 0);
		Rectangle r = mapImage.getBounds();
		holder.setSize(r.width, r.height);
		setContent(holder);
	}
	
	private void mouseClicked(MouseEvent e) {
		x[clickNo] = e.x;
		y[clickNo] = e.y;
		lat[clickNo] = queryDouble("Enter point " + clickNo + " lat");
		if(lat[clickNo] == 0) return;
		lon[clickNo] = queryDouble("Enter point " + clickNo + " lon");
		if(lon[clickNo] == 0) return;
		System.out.println("Click no " + clickNo);
		System.out.println("x: " + x[clickNo] + " y: " + y[clickNo]);
		System.out.println("lat: " + lat[clickNo] + " lon: " + lon[clickNo]);
		clickNo++;
		if(clickNo == lat.length) {
			calculateValues();
			clickNo = 0;
		}
	}
	
	private double queryDouble(String msg) {
		InputDialog d = new InputDialog(this.getShell(),
				"Enter Value", msg, "0.0", null);
		d.setBlockOnOpen(true);
		d.open();
		double ret;
		try {
			ret = Double.parseDouble(d.getValue());
		} catch (NumberFormatException nfe) {
			System.err.println("That wasn't a double");
			return 0.0;
		} catch (NullPointerException npe) {
			return 0.0;
		}
		return ret;
	}
	
	
	private void ppl() {
		// for x points chosen, there are
		// x choose 2 deltas
		int deltas = 0;
		double pplatSum = 0, pplonSum = 0;
		for(int i = 0; i < lat.length; i++) {
			for(int j = i + 1; j < lat.length; j++) {
				deltas++;
				int deltaX = x[i] - x[j];
				int deltaY = y[i] - y[j];
				double deltaLat = lat[i] - lat[j];
				double deltaLon = lon[i] - lon[j];
				double pplat = ((double)deltaY) / deltaLat;
				double pplon = ((double)deltaX) / deltaLon;
				pplatSum += pplat;
				pplonSum += pplon;
			}
		}
		pixelsPerLat = pplatSum / ((double)deltas);
		pixelsPerLon = pplonSum / ((double)deltas);
	}
	private void ol() {
		double oLatSum = 0, oLonSum = 0;
		for(int i = 0; i < lat.length; i++) {
			oLatSum += lat[i] - 1 / (pixelsPerLat / 
					(mapImage.getBounds().height - y[i]));
			oLonSum += lon[i] - 1 / (pixelsPerLon / x[i]);
		}
		originLat = oLatSum / (double)lat.length;
		originLon = oLonSum / (double)lat.length;
	}
	
	private void calculateValues() {
		ppl();
		System.out.println("pplat: " + pixelsPerLat);
		System.out.println("pplon: " + pixelsPerLon);
		
		ol();
		System.out.println("oLat: " + originLat);
		System.out.println("oLon: " + originLon);
		String[] buttons = new String[2];
		buttons[0] = "Close";
		buttons[1] = "Copy to clipboard";
		String message = "origin_lat=" + originLat + "\n" +
			"origin_lon=" + originLon + "\n" +
			"upper_right_lat=" + upperRightLat + "\n" +
			"upper_right_lon=" + upperRightLon + "\n" +
			"#pixels_per_lat=" + Math.abs(pixelsPerLat) + "\n" +
			"#pixels_per_lon=" + Math.abs(pixelsPerLon);
		
		MessageDialog m = new MessageDialog(this.getShell(),
				"Result", null, message,
				MessageDialog.INFORMATION,
				buttons, 1);
		m.setBlockOnOpen(true);
		if(m.open() == 1) {
			/* This is ugly, I know.  I can explain it, though.
			 * It started out as being done "right" (as shown below)
			 * but to do that i need the swt Clipboard, which isn't part
			 * of the stripped down swt in the device developer.  Its not
			 * feasible to provide a standalone Clipboard class extracted
			 * from swt sources, since its very platform dependent.
			 * 
			 * So then, I think, the awt Clipboard is still around
			 * on pocket pc, I'll just use that.  It turns out that swt
			 * has hacked things up to the point that it buggers the way
			 * the awt clipboard works.  On windows this simply results
			 * in an error message printed to the console, but it works fine.
			 * On Mac OS X, the application just hangs when it trys to copy
			 * the text.  So I've gone here and I use the command line tool
			 * pbcopy to load up the clipboard on mac os x, and I leave
			 * the other way for windows.
			 * 
			 * If, at some point in the future, the device developer swt
			 * starts including the dnd package, then it should be
			 * switched to the commented out block below. 
			 */
			if(BrowserControl.isMacPlatform()) {
				try {
					Process p = Runtime.getRuntime().exec("pbcopy");
					BufferedWriter out = new BufferedWriter(
							new OutputStreamWriter(p.getOutputStream()));
					out.write(message);
					out.close();
				} catch (IOException ioe) {
					System.err.println("couldn't do copy");
				}
			} else {
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection vex = new StringSelection(message);
				clip.setContents(vex, vex);
			}
			
			// the _right_ way to do it
			/*Clipboard clip = new Clipboard(this.getDisplay());
			clip.setContents(new Object[]{message}, 
					new Transfer[]{TextTransfer.getInstance()});
			clip.dispose();*/
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("java WadSpecifier <image file> [points]");
			System.exit(1);
		}
		int points = 2;
		if(args.length == 2) {
			points = Integer.parseInt(args[1]);
		}
		if(points < 2) points = 2;
		Display display = new Display();
		Display.setAppName("WadSpecifier");
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setText(args[0]);
		WadSpecifier thingy = new WadSpecifier(shell, SWT.NONE, args[0],
												points);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		shell.setLayout(layout);
		thingy.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.open();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

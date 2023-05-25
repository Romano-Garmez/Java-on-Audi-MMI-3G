package org.placelab.midp.stumbler;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.StringItem;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.Measurement;
import org.placelab.midp.UIComponent;

/**
 * Unpacks each entry in a stumble log file and shows it on screen.
 */

public class RMSRecordCanvas extends Canvas implements CommandListener, UIComponent {
	String recordName = null;
	String initStatus = "";
	StringItem recordLog;
	String[] records;
	StringBuffer sb;
	int skipLines = 0;
	int currentRecord = 0;
	Command backCommand = new Command("Back",Command.BACK,1);

	Display display;
	UIComponent back;
	
	boolean ready;
	boolean die;
	
	/**
	 * Initialize the canvas with these files. The canvas will read out of
	 * the record store and show the values on screen one by one
	 * 
	 * @param recordName the record store name to open
	 * @param display display that has control to show this canvas
	 * @param backUI the back button leads to this UI
	 */
	public RMSRecordCanvas(String recordName,Display display, UIComponent backUI) {
		super();
		sb = new StringBuffer();

		addCommand(backCommand);
		setCommandListener(this);

		recordLog = new StringItem("Log Entries","");
		this.recordName = recordName;
		die=false;
		ready=false;
		new Thread(){ public void run() { init_rmslist(); }}.start();
		
		this.display = display;
		this.back = backUI;
		repaint();
	}

	public void showUI(UIComponent from) {
	    if(display != null) {
	        display.setCurrent(this);
	    }
	}

	/**
	 * Initialize the list of records by reading in the entire record store
	 * into memory
	 */
	public void init_rmslist() {
		try {
		    initStatus="Loading list";
			RecordStore rms = RecordStore.openRecordStore(recordName, false);
			RecordEnumeration enum = rms.enumerateRecords(null, null,false);
			records = new String[enum.numRecords()];

			for(int i=0;i<records.length;i++) {
			    if(i % 100 == 0) {
			        initStatus = initStatus + '.';
			        repaint();
			    }
				if(die==true) {
				    initStatus="Told to die";
				    return;
				}
				byte[] b = enum.nextRecord();
				String s;
				Measurement m = Measurement.fromCompressedBytes(b);
				if (m != null) {
					s = m.toLogString();
				} else {
					s = new String(b);
				}
				records[i] = s;
			}
			currentRecord = 0;

			rms.closeRecordStore();
		} catch (Exception e) {
		    initStatus="Oops - " + e + "\n"; 
			//e.printStackTrace();
		}
		ready=true;
		repaint();
	}
	
	
	protected void keyReleased(int keyCode) {
		if(!ready) return;
		if(keyCode == -4) { //right pointer stick key
			skipLines = 0;
			if(currentRecord == records.length-1) {
				currentRecord = 0;
			} else {
				currentRecord++;
			}
		} else if(keyCode == -3) { //left pointer stick key
			skipLines = 0;
			if(currentRecord == 0) {
				currentRecord = records.length-1;
			} else {
				currentRecord--;
			}
		} else if(keyCode == -1) {
			if(skipLines != 0)
				skipLines--;
		} else if(keyCode == -2) {
			skipLines++;
		}
		
		repaint();

	}
	
	protected void keyRepeated(int keyCode) {
		if(!ready) return;
		
		if(keyCode == -4) { // right pointer stick key
			skipLines = 0;
			if(currentRecord == records.length-1) {
				currentRecord = 0;
			} else {
				currentRecord++;
			}
		} else if(keyCode == -3) { //left pointer stick key
			skipLines = 0;
			if(currentRecord == 0) {
				currentRecord = records.length-1;
			} else {
				currentRecord--;
			}
		} else if(keyCode == -1) {
			if(skipLines > 0)
				skipLines--;
		} else if(keyCode == -2) {
			skipLines++;
		} 
		
		repaint();

	}

	/**
	 * Write the string on the graphics object
	 * @param g graphics object
	 * @param y y offset to write the string
	 * @param s string to write
	 * @return y offset after writing the string
	 */
	public int write (Graphics g, int y, String s) {
			g.drawString (s, 0, y, Graphics.LEFT|Graphics.TOP);
			return y + g.getFont ().getHeight (); 				
	}

	/**
	 * Take a string and a current font and break it into lines
	 * that will fit in the specified width.
	 * 
	 * @param s string to chunk up
	 * @param f font to use
	 * @param width width to use for breaking the string into lines
	 * @return list of string objects
	 */
	public LinkedList breakStringToLines(String s,Font f,int width) {
		LinkedList lines = new LinkedList();
		
		char[] sarr = s.toCharArray();
		String copyStr = "";
		for(int i=0;i<sarr.length;i++) {
			if(f.stringWidth(copyStr) < width) {
				if(sarr[i] == '|') {
					lines.add(copyStr);
					copyStr = "";
				} else {
					copyStr = copyStr + sarr[i];
					if(i == sarr.length-1)
						lines.add(copyStr);
				}
			} else {
				lines.add(copyStr);
				copyStr = "";
			}
		}
		
		return lines;
	}
	
	public void paint(Graphics g) {			
		g.setGrayScale (255);
	    g.fillRect (0, 0, getWidth (), getHeight ());
	    g.setGrayScale (0);

	    int numberOfLines = getHeight()/g.getFont().getHeight();
	    
		LinkedList lineList = new LinkedList();
		int y =0;
		String status;
		if(!ready) {
		    status = initStatus;
		}
		else if(records.length == 0) {
		    status = "No records.";
		} else {
		    status = "Record "+(currentRecord+1)+"/"+records.length;
		    lineList = breakStringToLines(records[currentRecord],g.getFont(),this.getWidth());
		}
		lineList.addFirst(status);

	    if((lineList.size() - skipLines) <= numberOfLines) {
	    	skipLines = lineList.size() - numberOfLines;
	    }
		

	    int skipLinesTemp = skipLines;
	    for(Iterator iter = lineList.iterator();iter.hasNext();) {
			String line = (String)iter.next();
			if(skipLinesTemp > 0) {
				skipLinesTemp--;
			} else if(numberOfLines > 0){
				numberOfLines--;
				y = write (g, y, line);
			}
		}		    
	}

	public void commandAction(Command c, Displayable d) {
		if(c == backCommand && d == this) {
		    die=true;
		    back.showUI(this);
		}
		
	}
}

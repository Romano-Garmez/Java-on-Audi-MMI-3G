package org.placelab.stumbler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.placelab.collections.ArrayList;
import org.placelab.core.Measurement;
import org.placelab.core.ShutdownListener;
import org.placelab.util.ZipUtil;
import org.placelab.util.ns1.DHeap;

/**
 * The LogWriter class is geared toward the type of usage we expect
 * for people using the PlacelabStumbler gui application.  That is, they'll
 * open it up, start stumbling, and then save once their finished.
 * 
 * So this saves the log text to a temporary file, and then copies it to
 * the destination specified by the user once they request a save.
 */
public class LogWriter implements StumblerFunnelUpdateListener, ShutdownListener {

    String currentFile;
    PrintWriter log;
    DHeap logQueue;
    
    public ArrayList criticalSpotters;
    
    boolean shutdown = false;
    
    public static String PREAMBLE = "#PlacelabStumbler Log Version ";
    public static int LOG_VERSION = 2; 
    
    public LogWriter() throws IOException {
        this(makeTempFile(), false);
    }
    
    public static String makeTempFile() throws IOException {
        File tempFile = File.createTempFile("plstumbler", "txt");
        tempFile.deleteOnExit();
        return tempFile.getAbsolutePath();
    }
    
    public LogWriter(String path) throws IOException {
        this(path, false);
    }
    
    public LogWriter(String path, boolean append) throws IOException {
		this.currentFile = path;
		
		if (append) {
			// check to see if file is of the correct version
			FileInputStream fin = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String firstLine = br.readLine();
			if (firstLine == null) 
				throw new IOException("Unable to read log file.");
			int ver = getLogVersion(firstLine);
			if (ver != LOG_VERSION) 
				throw new IOException("Log file version " + ver + " is too old and cannot be opened.");
			br.close();
			fin.close();
		}
		
		logQueue = new DHeap(new MeasurementComparator(), 4);
		System.out.println("logging to " + path);
		log = new PrintWriter(new BufferedOutputStream(new FileOutputStream(
				path, true)));

		if (!append)
			log.println(PREAMBLE + LOG_VERSION);
			
		log.flush();
	}
    
    public static int getLogVersion(String firstLine) {
        if(firstLine.startsWith(PREAMBLE)) {
            try {
                int version = Integer.parseInt(firstLine.substring(PREAMBLE.length()));
                return version;
            } catch (NumberFormatException nfe) {
                return 0;
            }
        } else {
            // the original logs had no preamble or version number
            return 1;
        }
    }
    
    public void stumblerUpdated(Hashtable updates) {
        if(shutdown) return;
        synchronized(this) {
            // XXX: hack to print timeout lines
            if(criticalSpotters != null) {
                boolean gotOne = false;
                for(int i = 0; i < criticalSpotters.size(); i++) {
                    Object o = (Object)criticalSpotters.get(i);
                    if(updates.containsKey(o)) {
                        gotOne = true;
                        break;
                    }
                }
                if(!gotOne) {
                    log.println("TYPE=TIMEOUT");
                }
            }
            
	        for(Enumeration e1 = updates.elements(); e1.hasMoreElements(); ) {
	            logQueue.insert(e1.nextElement());
	        }
	        while(!logQueue.isEmpty()) {
	            String entry = ((Measurement)logQueue.deleteMin()).toLogString();
	            log.println(entry);
	            //System.out.println(entry);
	        }
	        log.flush();
        }
    }
    
    public boolean saveFile(String to) throws IOException {
        if(shutdown) return false;
        FileInputStream in = new FileInputStream(currentFile);
        FileOutputStream out = new FileOutputStream(to);
        ZipUtil.pipeStreams(out, in);
        in.close();
        out.close();
        return true;
    }
    
    /* Transfers the contents of everything logged this session into the new
     * output file at path and continues to log to the file at path.
     */
    public boolean setOutputFile(String path) throws IOException {
        boolean ret = false;
        synchronized(this) {
            if(saveFile(path)) {
                currentFile = path;
                ret = true;
            }
            log = new PrintWriter(new BufferedOutputStream(
                    new FileOutputStream(path, true)));
        }
        return ret;
    }
    
    public void shutdown() {
        // clean up mess
        shutdown = true;
    }
    
}

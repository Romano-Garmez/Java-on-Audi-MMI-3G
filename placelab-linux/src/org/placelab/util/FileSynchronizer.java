package org.placelab.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

import org.placelab.core.PlacelabProperties;


public class FileSynchronizer {
	private String urlStr;
	private String destDir;
	private boolean verbose=false;
	
	
	public static String MANIFEST = "manifest.txt";
	
	public FileSynchronizer(String urlStr, String destDir) {
		this.urlStr = urlStr;
		this.destDir = destDir;
	}

	/*
	 * return true on success
	 */
	public boolean synch() {
		return synch(false);
	}	
	public boolean synch(boolean verbose) {
		try {
			this.verbose = verbose;
			// pull down the manifest file (pointed to by the URL)
			if (verbose) System.out.println(" -- Synchronizing: " + destDir + " to " + urlStr);
			URLConnection urc = new URL(urlStr + "/" + MANIFEST).openConnection();
			InputStream is = urc.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String str = r.readLine();
				if (str == null) {
					break;
				}
				dealWith(str);
			}
			is.close();
		} catch (Exception ex) {
			System.out.println("Synchronization of " + destDir + " failed.");
			ex.printStackTrace();
			return false;
		}
		return true;
		
	}	
	
	private void dealWith(String line) throws Exception {
		if (line.startsWith("#") || (line.length() == 0)) {
			return;
		}
		String sarr[];
		sarr = StringUtil.split(line,'\t');
		String path = destDir + File.separator + sarr[0];

		boolean copyFile = true;

		// if there's a hash check it
		if (sarr.length == 2) {
			try {
				String serverHash = sarr[1];
				String fileHash = loadFileHash(destDir + File.separator + sarr[0]);
				if (fileHash != null) {
//					System.out.println(fileHash + " vs " + serverHash);
					if (serverHash.equalsIgnoreCase(fileHash)) {
						copyFile = false;
					} else {
						if (verbose) {
							System.out.println(" -- " + sarr[0] + " has changed");
						}
					}
				}
			} catch (Exception ex) {
				// no big, we'll just try to copy it
				ex.printStackTrace();
				System.out.println(ex.getMessage());
				System.exit(2);
			}
			
		}
		if (copyFile) {
			// copy over file
			int idx = path.lastIndexOf('/');
			if (idx > 0) {
				String dir = path.substring(0,idx);
				File f = new File(dir);
				f.mkdirs();
			}
			FileOutputStream os = new FileOutputStream(path);
			byte buf[] = new byte[1024];
			URLConnection urc = new URL(urlStr + "/" + sarr[0]).openConnection();
			InputStream is = urc.getInputStream();
			boolean done=false;
			while (!done) {
				int read = is.read(buf,0,1024);
				if (read == -1) {
					done = true;
				} else {
					os.write(buf,0,read);
				}
			}
			os.close();
			is.close();
			if (verbose) {
				System.out.println(" -- Copied: " + sarr[0]);
			}
		}
		
	}
	
	public static String loadFileHash(String path) throws Exception {
		File f = new File(path);
		
		if (!(f.isFile() &&  f.exists())) {
			return null;
		}
	
			
	MessageDigest m = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(path);
		boolean done=false;
		byte buf[] = new byte[1024];
		while (!done) {
			int read = is.read(buf,0,1024);
			if (read == -1) {
				done = true;
			} else {
		        m.update(buf,0,read);
			}
		}
		is.close();
        byte dig[] = m.digest();
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<dig.length; i++) {
			sb.append(toHexChar((dig[i]>>>4)&0x0F));
			sb.append(toHexChar(dig[i]&0x0F));
        	}
        	
		return sb.toString();
	}

	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9))
			return (char) ('0' + i);
		else
			return (char) ('a' + (i - 10));
	}
	
	

	public static void main(String[] args) {

		try {
			FileSynchronizer fs;
			
			fs = new FileSynchronizer(
			  	"http://seattleweb.intel-research.net/projects/placelab/data/mapdemo",
			  	PlacelabProperties.get("placelab.dir"));
			fs.synch(true);
			fs = new FileSynchronizer(
				"http://seattle.intel-research.net/projects/placelab/data/regression",
				PlacelabProperties.get("placelab.dir"));
			fs.synch(true);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

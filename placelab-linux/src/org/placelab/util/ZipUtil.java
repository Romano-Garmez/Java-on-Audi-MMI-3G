package org.placelab.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utilities for zip files
 * 
 */
public class ZipUtil {
	public static void dirToZip(File dir, File zipTo) 
		throws FileNotFoundException, IOException {
		ZipOutputStream out = new ZipOutputStream(
			new FileOutputStream(zipTo));
		Vector files = new Vector();
		listRec(dir, files);
		String basename = dir.getCanonicalPath();
		for(int i = 0; i < files.size(); i++) {
			File file = (File)files.get(i);
			String fullName = file.getCanonicalPath();
			String relName = fullName.substring(basename.length() + 1,
				fullName.length());
			if(File.separator.equals("\\")) {
				relName = StringUtil.switchAllChars(relName, '\\', '/');
			} 
			ZipEntry entry = new ZipEntry(relName);
			out.putNextEntry(entry);
			if(!file.isDirectory()) {
				FileInputStream ins = new FileInputStream(file);
				pipeStreams(out, ins);
				ins.close();
			}
			out.closeEntry();
		}
		out.close();		
	}
	private static void listRec(File dir, Vector soFar) {
		File[] files = dir.listFiles();
		if(files == null) return;
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			if(file.isDirectory()) {
				soFar.add(file);
				listRec(file, soFar);
			} else {
				soFar.add(file);
			}
		}
	}
	
	public static void pipeStreams(OutputStream to, InputStream from) 
		throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(from);
		BufferedOutputStream out = new BufferedOutputStream(to);
		byte[] buffer = new byte[8192];
		int read;
		while((read = in.read(buffer, 0, 8192)) != -1) {
			out.write(buffer, 0, read);
		}
		out.flush();
	}
	
	public static void pipeStreams(ZipOutputStream out, InputStream from) 
		throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(from);
		byte[] buffer = new byte[8192];
		int read;
		while((read = in.read(buffer, 0, 8192)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	
	public static void extractFile(File zipPath, String entry, File extractTo)
		throws IOException
	{
		ZipFile zip = new ZipFile(zipPath);
		ZipEntry e = zip.getEntry(entry);
		if(e == null) {
			throw new IOException("ZipUtil.extractFile: entry not found");
		}
		FileOutputStream outs = new FileOutputStream(extractTo);
		pipeStreams(outs, zip.getInputStream(e));
		outs.flush();
		outs.close();
	}
}		

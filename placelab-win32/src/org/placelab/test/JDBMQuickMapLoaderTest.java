package org.placelab.test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.JDBMQuickMapLoader;
import org.placelab.mapper.MapLoader;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;
import org.placelab.proxy.ProxyServletEngine;
import org.placelab.proxy.Servlet;
import org.placelab.util.FileSynchronizer;
import org.placelab.util.ZipUtil;

public class JDBMQuickMapLoaderTest implements Testable {

	// a mini file vending servlet.  this is just for testing
	// purposes, don't use it for anything real because it sucks
	private static class FileVend implements Servlet {
		private Hashtable files;
		public static String SERVLET_PREFIX = "http://placelab.mapload.test/file";
		// key => value = basename => path
		public FileVend(Hashtable files) {
			this.files = files;
		}
		public String getName() {
			return "FileVendServlet";
		}
		public void register() {
			ProxyServletEngine.addServlet(SERVLET_PREFIX, this);
		}
		// the way you get files out of this thing is like
		// "http://placelab.test/file/x" where x is the file
		// basename.
		public HTTPResponse serviceRequest(HTTPRequest req) {
			StringBuffer sb = new StringBuffer();
			String s = req.url.toString().substring(SERVLET_PREFIX.length());
			if(!s.startsWith("/")) {
				return whoops();
			}
			// strip out the slash
			String f = s.substring(1);
			String path = (String)files.get(f);
			if(path == null) return whoops();
			try {
				return fileResponse(path);
			} catch (IOException e) {
				return whoops();
			}
		}
		private HTTPResponse fileResponse(String path) throws IOException {
			// this is precisely the reason you don't want to use
			// this thing for anything real.  but this works for the
			// few k i want to push over
			File file = new File(path);
			ByteArrayOutputStream to = new ByteArrayOutputStream();
			FileInputStream from = new FileInputStream(file);
			ZipUtil.pipeStreams(to, from);
			from.close();
			return new HTTPResponse(HTTPResponse.RESPONSE_OK,
					"application/unknown",
					to.size(),
					to.toByteArray());
		}
		private HTTPResponse whoops() {
			return new HTTPResponse(
					HTTPResponse.RESPONSE_NOT_FOUND,
					"text/plain",
					"whoops".length(),
					"whoops".getBytes());
		}
		public Hashtable injectHeaders(HTTPRequest req) {
			return null;
		}
	}
	
	public String getName() {
		return "JDBMQuickMapLoaderTest";
	}
	public void runTests(TestResult result) throws Throwable {
		setup();
		String oldPort = System.getProperty("http.proxyPort");
		String oldHost = System.getProperty("http.proxyHost");
		System.setProperty("http.proxyPort", "2080");
		System.setProperty("http.proxyHost", "localhost");
		JDBMQuickMapLoader qml = 
			new JDBMQuickMapLoader(FileVend.SERVLET_PREFIX + "/mapper-zip.php", 
					FileVend.SERVLET_PREFIX + "/mapper-md5.php",
					dPath);
		//qml.url = FileVend.SERVLET_PREFIX + "/mapper-zip.php";
		//qml.md5url = FileVend.SERVLET_PREFIX + "/mapper-md5.php";
		checkCreate(result, qml);
		// now make a change to the map so that it won't checksum
		// with the server version for the next test
		((JDBMMapper)qml.getMapper()).deleteAll();
		StringBuffer sb = new StringBuffer();
		sb.append("47.6636333\t-122.3083683\tlambda\t004005b45c85\n"); // a piece of a real log
		sb.append("47.6636333\t-122.3083683\tlinksys\t000c41424432\n");
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		qml.loadMap(is);
		checkDifferent(result, qml);
		checkSame(result, qml);
		if(oldPort != null) System.setProperty("http.proxyPort", oldPort);
		if(oldHost != null) System.setProperty("http.proxyHost", oldHost);
	}
	
	// this test checks that no download occurs when it isn't
	// necessary
	private void checkSame(TestResult result, JDBMQuickMapLoader qml) 
		throws Exception
	{
		result.assertTrue(this, true, qml.doIt(),
			"qml checkSame: up to date check check");
		result.assertTrue(this, 3, ((JDBMMapper)qml.getMapper()).size(), 
			"qml checkSame: Number of items in fresh download check");
	}
	
	
	// this test checks that the mapper.db is created correctly
	// when it previously didn't exist
	private void checkCreate(TestResult result, JDBMQuickMapLoader qml) 
		throws Exception
	{
		result.assertTrue(this, false, qml.doIt(),
				"qml checkCreate: up to date check check");
		result.assertTrue(this, 3, ((JDBMMapper)qml.getMapper()).size(), 
				"qml checkCreate: Number of items in fresh download check");
	}
	
	// this test checks that the mapper.db is overwritten correctly when
	// it differs from the server version
	private void checkDifferent(TestResult result, JDBMQuickMapLoader qml) 
		throws Exception
	{
		result.assertTrue(this, false, qml.doIt(),
			"qml checkDifferent: up to date check check");
		result.assertTrue(this, 3, ((JDBMMapper)qml.getMapper()).size(), 
			"qml checkDifferent: Number of items in fresh download check");
	}
	
	// locations of the temp versions of these
	String tmapperdbPath, tmapperlgPath, tmapperMD5Path;
	
	// locations of the downloaded versions of these
	String dPath, dmapperdbPath, dmapperlgPath, dmapperMD5Path;
	
	private void setup() throws Exception {
		// want to make a zip file containing
		// a fake mapper.db/lg/md5
		File tempDir = File.createTempFile("test", "zip");
		tempDir.delete();
		tempDir.mkdir();
		tempDir.deleteOnExit();
		// assume this part works, since its tested elsewhere
		MapLoader ml = new MapLoader(new JDBMMapper(tempDir.getAbsolutePath() +
				File.separator + "mapper"));
		StringBuffer sb = new StringBuffer();
		sb.append("47.6636333\t-122.3083683\tlambda\t004005b45c85\n"); // a piece of a real log
		sb.append("47.6636333\t-122.3083683\tlinksys\t000c41424432\n");
		sb.append("47.6637783\t-122.30837\t4714\t00095b5322ec\n");
		InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		ml.loadMap(is);
		is.close();
		tmapperdbPath = ((JDBMMapper)ml.getMapper()).getDbName() + ".db";
		tmapperlgPath = ((JDBMMapper)ml.getMapper()).getDbName() + ".lg";
		tmapperMD5Path = ((JDBMMapper)ml.getMapper()).getDbName() + ".md5";
		// make the md5 sum
		String md5sum = FileSynchronizer.loadFileHash(tmapperdbPath);
		PrintWriter p = new PrintWriter(new BufferedOutputStream(
				new FileOutputStream(tmapperMD5Path)));
		p.println(md5sum);
		p.close();
		File tempZip = File.createTempFile("mappertmp", "zip");
		ZipUtil.dirToZip(tempDir, tempZip);
		Hashtable files = new Hashtable();
		files.put("mapper-zip.php", tempZip.getAbsolutePath());
		files.put("mapper-md5.php", tmapperMD5Path);
		// kick off the mini web server
		FileVend vend = new FileVend(files);
		vend.register();
		ProxyServletEngine.startProxy(true);
		// make a temp place for the downloaded stuff to go
		File dFile = File.createTempFile("test_download", "");
		dFile.delete();
		dFile.mkdir();
		dFile.deleteOnExit();
		dPath = dFile.getAbsolutePath();
	}
}

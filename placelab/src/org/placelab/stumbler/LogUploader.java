/*
 * Created on Aug 30, 2004
 *  
 */
package org.placelab.stumbler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import org.placelab.core.PlacelabProperties;

// TODO: propogate this code to HTTPTransport
public class LogUploader {

	public final static String UPLOAD_URL = "http://www.placelab.org/data/do-submit.php";

	protected String username;
	protected String password;
	protected String device;
	protected String description;
	protected URLConnection lastConn;

	public LogUploader(String device, String description) {
		this(PlacelabProperties.get("placelab.uploadLogs_username"),
				PlacelabProperties.get("placelab.uploadLogs_password"), device,
				description);
	}

	public LogUploader(String username, String password, String device,
			String description) {
		this.username = username;
		this.password = password;
		this.device = device;
		this.description = description;
	}

	protected DataOutputStream establishConnection() throws IOException {
		URL url = new URL(UPLOAD_URL);
		URLConnection connection = url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		lastConn = connection;

		StringBuffer dfsb = new SimpleDateFormat("M/dd/yyyy").format(
				new java.util.Date(), new StringBuffer(), new FieldPosition(0));
		String dateStr = dfsb.toString();

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		String args = "username=" + URLEncoder.encode(username) + "&"
				+ "passwd=" + URLEncoder.encode(password) + "&"
				+ "readDisclaimer=agree&" + "cvt_to_ns=true&" + "trace_device="
				+ URLEncoder.encode(device) + "&" + "trace_descr="
				+ URLEncoder.encode(description) + "&" + "mailBack=on&"
				+ "simple_output=true&" + "trace_date="
				+ URLEncoder.encode(dateStr) + "&" + "trace_data=";

		out.writeBytes(args);

		return out;
	}

	protected void finishConnection(DataOutputStream out) throws IOException {
		out.flush();
		out.close();
		
		InputStream in = lastConn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String s = reader.readLine();
		reader.close();
		
		lastConn = null;
		
		if (!"SUCCESS".equals(s))
			throw new IOException("POST failed");
	}

	public void upload(InputStream in) throws IOException {
		DataOutputStream out = establishConnection();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		while (true) {
			String line = reader.readLine();
			
			//System.out.println("line = " + line);
			if (line == null || line.equals("DONE"))
				break;
			
			String contents = URLEncoder.encode(line + "\n");
			out.writeBytes(contents);
		}
		
		finishConnection(out);
	}
	
	protected void upload() throws IOException {
		DataOutputStream out = establishConnection();

		out.writeBytes("Test1");
		out.writeBytes("Test2");
		
		finishConnection(out);
	}
	
	public static void main(String args[]) {
		LogUploader uploader = new LogUploader("laptop", "Test Submit");
		try {
			uploader.upload();
		} catch (IOException e) {
			System.err.println("Barfed: " + e);
		}
		
	}

  
    public void suggestLoginDetails(String user, String pass) {
        if(username == null || username== "") {
            username=user;
            password=pass;
        }
    }
}
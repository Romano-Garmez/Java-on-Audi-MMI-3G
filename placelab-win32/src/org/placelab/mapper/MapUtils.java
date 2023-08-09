/*
 * Created on 21-Sep-2004
 *
 */
package org.placelab.mapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Utilities for Mapping
 */
public class MapUtils {

	/**
	 * Utility to get an input stream from a file
	 */
	public static InputStream getFileStream(String dataLoc) {
		try {
			return new FileInputStream(dataLoc);
		} catch (Exception ex) {
			System.out.println("Could not open file '" + dataLoc + "'");
			ex.printStackTrace();
			System.exit(1);
		}
		return null;

	}

	/**
	 * Utility to get an input stream from a URL string
	 */
	public static InputStream getHttpStream(String dataLoc) {
		try {
			// For some reason, URLConnection is ass slow
			// so we'll just use a socket to get the data
			URL url = new URL(dataLoc);
			int port = url.getPort();
			if (port == -1)
				port = 80;
			String host = url.getHost();
			String file = url.getFile();

			String proxyPortStr = System.getProperty("http.proxyPort");
			String proxyHost = System.getProperty("http.proxyHost");
			if (proxyHost != null) {
				host = proxyHost;
				try {
					port = (proxyPortStr != null) ? Integer
							.parseInt(proxyPortStr) : 80;
				} catch (NumberFormatException ex) {
					port = 80;
				}

				file = dataLoc;
			}

			Socket client = null;
			try {
			    client = new Socket(host, port);
			} catch(UnknownHostException uhe) {
			    System.err.println("Unknown host: " + dataLoc + " (could be connectivity problem)");
			    return null;
			}
			PrintWriter output = new PrintWriter(new OutputStreamWriter(client
					.getOutputStream()));
			output.print("GET " + file + " HTTP/1.0\r\nHost: " + url.getHost()
					+ (port == 80 ? "" : "" + port) + "\r\n\r\n");
			output.flush();

			InputStream response = client.getInputStream();
			StringBuffer headers = new StringBuffer();

			int ch, length = 0;
			boolean eoh = false;
			while (true) {
				try {
					ch = response.read();
				} catch (IOException ex) {
					break;
				}
				if (ch < 0)
					break;
				headers.append((char) ch);
				length++;

				if ((length >= 2 && headers.charAt(length - 1) == '\n' && headers
						.charAt(length - 2) == '\n')
						|| (length >= 3 && headers.charAt(length - 1) == '\n'
								&& headers.charAt(length - 2) == '\r' && headers
								.charAt(length - 3) == '\n')) {
					eoh = true;
					break;
				}
			}
			BufferedReader br = new BufferedReader(new StringReader(headers
					.toString()));
			String httpResponse = br.readLine().trim();
			br.close();
			if (!httpResponse.regionMatches(true, 0, "HTTP", 0, 4))
				return null;

			/*
			 * skip over the HTTP version number and the following whitespace
			 */
			int index = 4;
			while (!Character.isWhitespace(httpResponse.charAt(index)))
				index++;
			while (Character.isWhitespace(httpResponse.charAt(index)))
				index++;

			int statusIndex = index;
			while (index < httpResponse.length()
					&& !Character.isWhitespace(httpResponse.charAt(index)))
				index++;
			int status = Integer.parseInt(httpResponse.substring(statusIndex,
					index));
			if (status != 200)
				return null;

			return response;

			//URLConnection uc = new URL(dataLoc).openConnection();
			//			System.out.println("A");
			//			System.out.println("Caching is: " + uc.getDefaultUseCaches());
			//uc.setAllowUserInteraction(false);
			//			uc.getOutputStream().close();
			//			System.out.println("B");

			//uc.connect();

			//InputStream is = uc.getInputStream();
			//			System.out.println("I see " + is.available());
			//Thread.currentThread().sleep(4000);
			//			System.out.println("I see " + is.available());
			//return uc.getInputStream();
		} catch (Exception ex) {
			System.out.println("Could not URL '" + dataLoc + "'");
			ex.printStackTrace();
			//System.exit(1);
		}
		return null;
	}

}

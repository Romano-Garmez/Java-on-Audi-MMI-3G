package org.placelab.spotter;

import java.io.IOException;

import org.placelab.core.Measurement;
import org.placelab.core.NetStumblerFileParser;

/**
 * NetStumblerLogSpotter will read an exported tab-separated text-log from NetStumbler
 * and read it line by line. Each line corresponds to a NetStumblerMeasurement.
 * We take each measurement and add it to a List.
 * 
 * There is a trick we do to get the absolute timestamp. We get the date from the 
 * 3rd line of the logfile and then parse out the year, mothan dn day
 * 
 * Below is an example of a Netstumbler output:
 * 
 * <pre>
 * # $Creator: Network Stumbler Version 0.3.30
 * # $Format: wi-scan with extensions
 * # Latitude 	Longitude		( SSID )	Type	( BSSID )	Time (GMT)	[ SNR Sig Noise ]	...
 * $ $Date GMT: 2004-04-06
 * 
 * N 47.6619200	E122.3162617	( default )	BBS		( 00:80:c8:19:5a:1d )	01:25:07 (GMT)	[ 35 87 52 ]
 * N 47.6619200	E122.3162617	( linksys-g )	BBS	( 00:0c:41:14:a1:92 )	01:25:07 (GMT)	[ 23 73 50 ]
 * N 47.6619200	E122.3162617	( 1100 )	BBS		( 00:0d:28:88:c7:56 )	01:25:07 (GMT)	[ 14 72 53 ]
 * 
 * </pre>
 */

public class NetStumblerLogSpotter extends LogSpotter {
	private String traceFile;
	private NetStumblerFileParser parser;
	private boolean doneParsing = false;
	
	
	public NetStumblerLogSpotter(String traceFile) {
		this.traceFile = traceFile;
	}
	public void open() throws SpotterException {
		doneParsing = false;
		try {
			parser = new NetStumblerFileParser(traceFile);
		}
		catch (IOException ex) {
			throw new SpotterException("Error opening trace file");
		}
	}
	
	public Measurement getMeasurementFromLog() {
		if (doneParsing) {
		    return null;
		}		
		try {
			Measurement m = parser.readMeasurement();
			if (m == null) {
				doneParsing = true;
			} else {
			    return m;
			}
			return null;
		}		
		catch (IOException ex) {
			System.out.println("Error parsing trace file");
			ex.printStackTrace();
			System.exit(1);
		}		
		return null;
	}
	


	public void close() {
		//try {

			if (parser != null) parser.close();
			parser = null;
		//} catch (IOException e) {
			// ignore the exception on close()

		//}
		
		//}

	}
}
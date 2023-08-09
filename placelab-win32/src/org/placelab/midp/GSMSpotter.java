/*
 * Created on Jun 29, 2004
 */
package org.placelab.midp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.Measurement;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.SyncSpotter;
import org.placelab.util.StringUtil;

/**
 * This spotter polls a localhost server running on the phone. This is a
 * synchronous spotter that makes a blocking read request to the server, returning
 * when the read completes.
 */
public class GSMSpotter extends SyncSpotter {
	//the server passes back parameterNumber values
	//cellId,areaId,signalStrength,MCC,MNC,networkName
	private int parameterNumber = 6;

	private long scanIntervalMillis;

	SocketConnection sc = null;
	DataInputStream is = null;
	DataOutputStream dos = null;

	/**
	 * Construct a GSMSpotter with default scan time of 1 second
	 *
	 */
	public GSMSpotter() {
		this(1000);
	}

	/**
	 * Construct a GSMSpotter
	 * @param intervalMillis time between scans
	 */
	public GSMSpotter(long intervalMillis) {
		super();
		scanIntervalMillis = intervalMillis;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.placelab.spotter.Spotter#open()
	 */
	public void open() throws SpotterException {
		try {
			//native server runs on port 4040
			sc = (SocketConnection) Connector.open("socket://127.0.0.1:4040");
			if(sc == null) throw new SpotterException("Cannot open socket to native GSM spotter");
			sc.setSocketOption(SocketConnection.DELAY, 0);
			is = sc.openDataInputStream();
			dos = sc.openDataOutputStream();
		} catch (IOException ioe) {
			throw new SpotterException("Cannot open stream to native GSM spotter");
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.placelab.spotter.Spotter#close()
	 */
	public void close() throws SpotterException {
		try {
			if (is != null)
				is.close();
			if (sc != null)
				sc.close();
			if (dos != null)
				dos.close();
		} catch (IOException ioe) {
			throw new SpotterException("Cannot close stream to native GSM spotter");
		}
	}

	/**
	 * Polls the native server for a GSM reading
	 * @return Measurement containing a GSM reading
	 * @throws IOException
	 * @throws SpotterException
	 */
	private Measurement queryServer() throws IOException,SpotterException {
		
		StringBuffer message = new StringBuffer("");

		//open connection
		if(dos == null) throw new SpotterException("GSMSpotter.query: output stream  not open");
		if(is == null) throw new SpotterException("GSMSpotter.query: input stream not open");
		
		byte[] b = { 1 };
		dos.write(b);
		dos.flush();

		//get data
		int ch;
		while ((ch = is.read()) != -1) {
			if (ch == '\n')
				break;
			message = message.append((char) ch);
		}
		//parse data
		String data[] = StringUtil.split(message.toString(), ',');
		if (data.length == parameterNumber) {

			String cellId = data[0];
			String areaId = data[1];
			String signalStrength = data[2];
			String MCC = data[3];
			String MNC = data[4];
			String networkName = data[5];

			//construct a GSM Reading
			GSMReading[] grs = new GSMReading[1];
			GSMReading gr = new GSMReading(cellId, areaId, signalStrength, MCC,
					MNC, networkName);
			grs[0] = gr;

			//construct a beaconmeasurement
			return new BeaconMeasurement(System.currentTimeMillis(), grs);
		}
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.placelab.spotter.SyncSpotter#getMeasurementImpl()
	 */
	protected Measurement getMeasurementImpl() throws SpotterException {
		try {
			return queryServer();
		} catch (IOException e) {
			throw new SpotterException(e.getMessage());
		}
	}

	/**
	 * Returns the scan interval for polling the native server
	 * @return nextScanInterval
	 */
	protected long nextScanInterval() {
		return scanIntervalMillis;
	}
}


/*
 * Created on Jun 16, 2004
 *
 */
package org.placelab.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * Measurements are produced by Spotters and
 * represent the result of a single scan of the
 * environment.
 */
public class Measurement implements Observable {
	private long timestamp;
	
	/**
	 * Creates a Measurement with the given timestamp
	 * @param timestampInMillis the time at which the 
	 * Measurement was taken
	 */
	public Measurement(long timestampInMillis) {
			timestamp = timestampInMillis;
	}
	public void setTimestamp(long timestamp) 
	{
		this.timestamp = timestamp;
	}
	/**
	 * Gets the time at which the Measurement was taken. 
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * Converts the Measurement to a form which is convenient
	 * for writing to a log file.  The convention is to have the 
	 * following form:
	 * <pre>
	 * TYPE=type|TIME=timestamp|OTHERFIELD=value ...
	 * </pre>
	 * All necessary fields should be stored in that pipe separated
	 * format, and all values should be percentEscaped with
	 * {@link org.placelab.util.StringUtil#percentEscape(String)}
	 * <p>
	 * Such a format is easily converted into a HashMap form, and
	 * a Measurement should (but is not required to) provide a constructor
	 * to build itself from a HashMap created from its log string form.
	 */
	public String toLogString() {
	    return Types.TYPE + "=" + Types.UNKNOWN + "|" +
	    	Types.TIME + "=" + getTimestamp();
	}
	
	/**
	 * A short string based representation for debugging purposes
	 */
	public String toShortString() {
		return "UNKNOWN: " + getTimestamp();
	}
	
	/**
	 * For storage on impoverished devices
	 * @return a compressed form of the Measurement suitable for storing
	 * on small devices such as phones
	 */
	public byte[] toCompressedBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);

		try {
			dos.write("UNKNOWN".getBytes());
			dos.writeLong(getTimestamp());
			dos.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	/**
	 * Reconstruct the Measurement from its compressed form
	 * @see #toCompressedBytes()
	 */
	public static Measurement fromCompressedBytes(byte[] array) {
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		DataInputStream dis = new DataInputStream(bais);
		Measurement m = null;
		try {
			String type = dis.readUTF();
			long time = dis.readLong();
			if (type.equals(Types.GPS)) {
				m = new GPSMeasurement(time, dis);
			} else if (type.equals(Types.WIFI) || type.equals(Types.BLUETOOTH) || type.equals(Types.GSM)) {
				m = new BeaconMeasurement(type, time, dis);
			}
			dis.close();
			bais.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
    	return m;
	}
}

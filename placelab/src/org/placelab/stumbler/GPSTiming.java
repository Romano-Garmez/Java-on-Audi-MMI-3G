/*
 * Created on Jul 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.placelab.stumbler;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.placelab.spotter.NMEASentence;

/**
 * 
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GPSTiming {
    
    BufferedReader inputStream;
    OutputStream outputStream;
    SerialPort serialPort;
    Thread readThread;
    
    public GPSTiming(String port) 
	throws IOException, UnsupportedCommOperationException, TooManyListenersException, PortInUseException, NoSuchPortException 
	{
	    System.out.println("using " + port);
	    serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(port).open("GPSEcho", 2000);
	    inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
	    outputStream = serialPort.getOutputStream();
	    serialPort.setSerialPortParams(4800, 
	            SerialPort.DATABITS_8,
	            SerialPort.STOPBITS_1,
	            SerialPort.PARITY_NONE);
	    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT &  
	            SerialPort.FLOWCONTROL_RTSCTS_IN);
	    serialPort.setDTR(true);
	    serialPort.setDTR(false);
	    //serialPort.addEventListener(this);
	    long latest = System.currentTimeMillis();
	    boolean gga = false, rmc = false;
	    while(true) {
	        String line = inputStream.readLine();
	        if(line == null) {
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException ie) { }
	            continue;
	        }
	        //System.out.println(line);
	        NMEASentence sentence = NMEASentence.expandSentence(line);
	        if(sentence == null) continue;
	        if(sentence.getType().equalsIgnoreCase("gpgga")) {
	            gga = true;
	        } else if(sentence.getType().equalsIgnoreCase("gprmc")) {
	            rmc = true;
	        }
	        if(gga && rmc) {
	            gga = rmc = false;
	            long temp = System.currentTimeMillis();
	            System.out.println("RMC & GGA : " + (temp - latest));
	            latest = temp;
	        }
	    }
	}
	
	
	public static void main(String[] args) {
	    try {
	        GPSTiming e = new GPSTiming(args[0]);
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
	    // look pretty
	    while(true) {
	        try {
	            Thread.sleep(500);
	        } catch (InterruptedException ie) { }
	    }
	}
}

package org.placelab.util;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

public class GPSEcho implements SerialPortEventListener {
    
    InputStream inputStream;
    OutputStream outputStream;
    SerialPort serialPort;
    Thread readThread;
    
    public GPSEcho(String port) 
    	throws IOException, UnsupportedCommOperationException, TooManyListenersException, PortInUseException, NoSuchPortException 
    {
        System.out.println("using " + port);
        serialPort = (SerialPort)CommPortIdentifier.getPortIdentifier(port).open("GPSEcho", 2000);
        inputStream = serialPort.getInputStream();
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

        while(true) {
            int ch;
            
            ch = inputStream.read();
            if(ch == -1) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) { }
            } else {
                System.out.print((char)ch);
            }
        }
    }
    
    public void serialEvent(SerialPortEvent event) {
        try {
            if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
	            int ch;
	            while((ch = inputStream.read()) != -1) {
	                System.out.print((char)ch);
	            }
	        }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        try {
            GPSEcho e = new GPSEcho(args[0]);
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

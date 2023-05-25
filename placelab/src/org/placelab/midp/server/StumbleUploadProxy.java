package org.placelab.midp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.placelab.core.Measurement;
import org.placelab.core.PlacelabProperties;
import org.placelab.stumbler.LogUploader;


/**
 * Receives stumble logs from the phone to be uploaded to Place Lab.
 * The bytes that are received are in compressed format which the
 * proxy decompresses before uploading. All stumble logs are saved
 * on the PC before being uploaded.
 * 
 * Note: Change default login to be your placelab id
 */
public class StumbleUploadProxy implements BluetoothService {

	protected LogUploader uploader =
		new LogUploader("phone", "Uploaded by the StumbleUploadProxy.");
	
	public String getName() {
		return "Stumble Upload Proxy";
	}
	
	public byte getServiceType() {
		return BluetoothService.LOG_UPLOAD_SERVICE;
	}
	
	public void newClient(DataInputStream in, DataOutputStream out) {
		System.out.println("Receiving stumbler log...");
		
		File f = null;
		
		for (int i = 0;; i++) {
			f = new File(PlacelabProperties.get("placelab.logdir") + File.separator + 
					"uploaded-log-" + i + ".txt");
			if (!f.exists())
				break;
		}
		
		try {
			System.out.println("Saving to " + f.getName());
			PrintStream ps = new PrintStream(new FileOutputStream(f));
			
//			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
//				String line = reader.readLine();
				
				//Log strings are less than 256
				//Read a byte at a time, until we see \r\n
				
				byte[] byteLine = new byte[256];
				boolean checkNext = false;
				for(int i=0;i<byteLine.length;i++) {
					byte b = in.readByte();
					if (((char) b) == '\r') {
						checkNext = true;
					} else if (checkNext) {
						if (((char) b) == '\n') {
							byte[] tempBytes = byteLine;
							//array needs to be exact length
							byteLine = new byte[i - 1];
							System.arraycopy(tempBytes, 0, byteLine, 0, i - 1);
							break;
						}
						checkNext = false;
					}
					byteLine[i] = b;
				}
				
				String line = new String(byteLine,0,byteLine.length);
				if(line.startsWith("#")) { //this would be the #Placelab v2 string
					ps.println(line);
					continue;
				} else if(line.equals("DONE")) { //we're done here
					break;
				} else { //it must be a measurement
					Measurement m = Measurement.fromCompressedBytes(byteLine);
					if (m != null) {
						line = m.toLogString();
					}
				}
				if (line == null)
					break;
				ps.println(line);
			}
			ps.close();
						
			System.out.println("Saved.");
		} catch (IOException e) {
			System.err.println("Failed to download log: " + e);
			e.printStackTrace();
		}
		
		try {	
			System.out.println("Uploading to placelab.org");
		    uploader.suggestLoginDetails("Phone","phone");
			uploader.upload(new FileInputStream(f));
			System.out.println("Data forwarded to placelab.org");
		} catch (IOException e) {
			System.err.println("Failed to upload log: " + e);
			e.printStackTrace();
		}
	}
}

package org.placelab.demo.mapview;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.placelab.collections.Iterator;
import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.WiFiBeacon;

/**
 * 
 */
public class MapExporter {

	public MapExporter() {
		
	}
	
	public InputStream exportMapPointTSV() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(os);
		JDBMMapper mapper = new JDBMMapper();
		Iterator it = mapper.iterator();
		pw.println("Latitude\tLongitude\tbssid\tssid\n");
		while (it.hasNext()) {
			WiFiBeacon b = (WiFiBeacon)it.next();
			pw.println(b.getPosition().getLatitudeAsString() + "\t" + b.getPosition().getLongitudeAsString()+ "\t" + b.getId() + "\t" + b.getSsid());
		}
		pw.close();
		
		return new ByteArrayInputStream(os.toByteArray());
	}
	

	public static void main(String[] args) {
		
		try {
			MapExporter ml = new MapExporter();
			InputStream is = ml.exportMapPointTSV();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String s = br.readLine();
				if (s == null) {
					break;
				}
				System.out.println(s);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	

}

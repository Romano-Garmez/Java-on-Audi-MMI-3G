/*
 * Created on Sep 18, 2004
 *
 */
package org.placelab.midp.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.Map;
import org.placelab.collections.Set;
import org.placelab.util.StringUtil;


public class ESMQuestionService implements BluetoothService {
	private Map questions;
	
	public ESMQuestionService() {
		questions = new HashMap();
	}
	
	public ESMQuestionService(String f) {
		this();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			for (;;) {
				String line = reader.readLine();
				
				if (line == null)
					break;
				
				String[] fields = StringUtil.split(line, '|', 2);
				questions.put(fields[0], fields[1]);
			}
		} catch(IOException e) {
		    System.err.println("ESM: cannot load questions: " + e);
		}
	}
	
	public String getName() {
		return "ESM Question Uploader (" + questions.size() + ")";
	}

	public byte getServiceType() {
		return BluetoothService.ESM_QUESTION_SERVICE;
	}

	public void newClient(DataInputStream in, DataOutputStream out) {
		Set keys = questions.keySet();
		
		try {
			int count = keys.size();
			System.out.println("Uploading " + count + " questions");
			out.writeInt(count);
			Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				String key = (String)iter.next();
				
				out.writeUTF(key);
				out.writeUTF((String)questions.get(key));
			}
			out.flush();
			if (in.readInt() != count) {
				System.err.println("Client didn't read the right abount");
			}
		} catch (IOException e) {
			System.err.println("bad: " + e);
		}
	}

}

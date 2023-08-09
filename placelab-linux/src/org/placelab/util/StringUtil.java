/*
 * Created on 07-Jul-2004
 *
 */
package org.placelab.util;

import java.io.IOException;
import java.io.Reader;

import org.placelab.collections.ArrayList;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;
import org.placelab.collections.Map;

/**
 * Utilities for Strings ;)
 */
public class StringUtil {

	public static boolean equalsIgnoreCase(String a, String b) {
		return (a.toLowerCase()).equals(b.toLowerCase());
	}
	
	public static String[] split(String input) {
		return StringUtil.split(input, '\t', 0);
	}

	public static String[] split(String input, char separator) {
		return StringUtil.split(input, separator, 0);
	}

	public static String[] split(String input, char separator, int limit) {
		return StringUtil.split(input, String.valueOf(separator), limit);
	}
	
	public static String[] split(String input, String separator) {
		return StringUtil.split(input, separator, 0);
	}
	
	public static String[] split(String input, String separator, int limit) {
	    try {
			if(input == null || separator == null) return null;
			if (limit == 1) {
				String[] slist = new String[1];
				slist[0] = input;
				return slist;
			}
			
			int index = 0;
			int length = input.length();
			int slength = separator.length();
			ArrayList list = new ArrayList();
			while (index <= length) {
				int new_index = input.indexOf(separator, index);
				if (new_index < 0) new_index = length;
				list.add(input.substring(index, new_index));
				index = new_index+slength;
		
				if (limit > 0 && list.size() == limit - 1 && 
				    index <= length) {
					/* add the remaining stuff to the list */
					list.add(input.substring(index, length));
					break;
				}
			}
			String[] slist = new String[list.size()];
			list.toArray(slist);
//			index = 0;
//			for (Iterator it=list.iterator(); it.hasNext(); ) {
//				slist[index++] = (String) it.next();
//			}
			return slist;
	    } catch(RuntimeException e) {
	        throw new RuntimeException("StringUtil.split: ERR: " + e.getClass().getName() + ":" + e.getMessage());
	    }
	}
	
	/** Looks for a string within a "packed string" which is set of strings delimited by a separator */
	public static boolean match(String packedString, String separator, String searchString) {
		if(packedString == null || searchString == null || separator == null) return false;

		int packedLength = packedString.length();
		int sepLength = separator.length();
		int searchLength = searchString.length();
		if(packedLength == 0 || sepLength==0 || searchLength==0) return false;
		
		int index = packedString.indexOf(searchString);
		if(index < 0) return false;
		
		// we've found it - but make sure its delimited by the separator or the ends of the string
		if(index > 0 && !packedString.regionMatches(false,index-sepLength,separator,0,sepLength)) return false;
		if(index + searchLength < packedLength && !packedString.regionMatches(false,index+searchLength,separator,0,sepLength)) return false;
		return true;
		
//		while (index >=0) {
//			if(packedString.regionMatches(false,index,searchString,0,searchLength)) return true;
//			index = packedString.indexOf(separator, index) + sepLength;
//		}
//		return false;
}

	public static String join(String[] strings, char separator) {
		return join(strings, String.valueOf(separator));
	}
	
	public static String join(String[] strings, String separator) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < strings.length; i++) {
			if (i != 0) sb.append(separator);
			sb.append(strings[i]);
		}
		return sb.toString();
	}
	
	public static String replace(String s, String from, String to) {
		String[] parts = StringUtil.split(s, from);
		return StringUtil.join(parts, to);
	}

	public static String canonicalizeBSSID(String bssid) {
		bssid = bssid.toLowerCase();
		if (bssid.length()==12 && bssid.indexOf(':') < 0) {
			/* this is the old 12-character format; change it
			 * to the new XX:XX:XX:XX:XX:XX format */
			bssid = bssid.substring( 0, 2) + ':' + 
				bssid.substring( 2, 4) + ':' +
				bssid.substring( 4, 6) + ':' +
				bssid.substring( 6, 8) + ':' +
				bssid.substring( 8,10) + ':' +
				bssid.substring(10,12);
		}
		return bssid;
	}

	public static String switchAllChars(String str, char from, char to) {
		StringBuffer ret = new StringBuffer();
		for(int i = 0; i < str.length(); i++) {
			char at = str.charAt(i);
			if(at == from) ret.append(to);
			else ret.append(at);
		}
		return ret.toString();
	}

	public static String pad(String str, int len) {
		StringBuffer sb = new StringBuffer(str);
		for (int i=str.length(); i < len; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

    public static String percentEscape(String s) {
    	if (s == null) {
    		return null;
    	}
    	StringBuffer sb = new StringBuffer();
    	int len = s.length();
    	for (int i=0; i < len; i++) {
    		char ch = s.charAt(i);
    		switch (ch) {
    		case '=' : sb.append("%3D"); break;
    		case '%' : sb.append("%25"); break;
    		case '|' : sb.append("%7C"); break;
    		case '\n': sb.append("%0A"); break;
    		case '\r': sb.append("%0D"); break;
    		default:   sb.append(ch);    break;
    		}
    	}
    	return sb.toString();
    }

    public static String percentUnescape(String s) {
        if(s == null) return null;
    	if (s.indexOf('%') < 0) return s;
    	StringBuffer sb = new StringBuffer();
    	int len = s.length();
    	for (int i=0; i < len; i++) {
    		char ch = s.charAt(i);
    		if (ch=='%' && ((i+3) <= len)) {
    			String esc = s.substring(i, i+3);
    			if (esc.equals("%3D")) sb.append('=');
    			else if (esc.equals("%25")) sb.append('%');
    			else if (esc.equals("%7C")) sb.append('|');
    			else if (esc.equals("%0A")) sb.append('\n');
    			else if (esc.equals("%0D")) sb.append('\r');
    		} else {
    			sb.append(ch);
    		}
    	}
    	return sb.toString();		
    }

	
	/**
	 * To avoid java serialization, beacons know how to send themselves to and
	 * reconstitute from string form.
	 */
	public static HashMap storageStringToHashMap(String s) {
		HashMap map = new HashMap();
		if(s == null) return null;
		String[] sarr = StringUtil.split(s, '|');
		if(sarr == null) return null;
		for (int i=0; i < sarr.length; i++) {
		    if(sarr[i] == null || sarr[i].length() == 0) continue; // allow null fields
			String[] keyvalue = StringUtil.split(sarr[i], '=', 2);
			if (keyvalue==null || keyvalue.length != 2) continue;
			map.put(percentUnescape(keyvalue[0]), percentUnescape(keyvalue[1]));
		}
		return map;
	}

	/**
	 * To avoid java serialization, beacons know how to send themselves to and
	 * reconstitute from string form.
	 */
	public static String hashMapToStorageString(HashMap map) {
		StringBuffer sb = new StringBuffer();
		if(map == null) return "";
		for (Iterator it=map.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry) it.next();
			sb.append(percentEscape((String)entry.getKey()) + "=" + percentEscape((String)entry.getValue()));
			if (it.hasNext()) sb.append("|");
		}
		return sb.toString();
	}
	
	public static boolean stringToBoolean(String str) {
		return ((str != null) && (str.equals("1") || str.toLowerCase().equals("true")));
	}
	
	static StringBuffer readBuffer = new StringBuffer();
	public static synchronized String readLine(Reader reader) throws IOException {
		//StringBuffer buffer = new StringBuffer();
		readBuffer.setLength(0);
		int x = reader.read();
		if (x == -1)
			return null;
		
		while (true) {
			char c = (char)x;
			
			if (c == '\n')
				break;
			
			readBuffer.append(c);
			
			x = reader.read();
			if (x == -1)
				break;
		}
		
		return readBuffer.toString();
	}
}

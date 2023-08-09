package org.placelab.util;


import java.lang.NumberFormatException;

/**
 * For parsing command line arguments.
 * In your main method, call Cmdline.parse(args)
 * and then anywhere in your app, you can get a value
 * for the argument by doing Cmdline.get().
 * <p>
 * Args are expected to be preceded with -- and 
 * are put into stray args if not.
 */
public class Cmdline {
	public static void parse(String[] argv) {
		int i=0;
		int stray=0;

		while (i < argv.length) {
			String key;
			String value;
			if ((argv[i].length() > 1) && (argv[i].substring(0, 2).equals("--"))) {
				key = "args." + argv[i].substring(2);
				if (i < argv.length-1 &&
				    ! ((argv[i+1].length() > 1) && (argv[i+1].substring(0, 2).equals("--")))) {
					value = argv[i+1];
					i += 2;
				} else {
					value = "1";
					i++;
				}
//				System.out.println("ARG: "+key+": "+value);
				System.setProperty(key, value);
			} else {
				key = "args.stray_" + stray;
//				System.out.println("ARG: "+key+": "+argv[i]);
				System.setProperty(key, argv[i]);
				i++;
				stray++;
			}
		}
//		System.out.println("ARG: "+"args.stray_cnt"+": "+Integer.toString(stray));
		System.setProperty("args.stray_cnt", Integer.toString(stray));
	}

	public static String getArg(String arg) {
		return System.getProperty("args." + arg);
	}
    public static String[] getStrayArgs() {
		String cntStr = getArg("stray_cnt");
		if (cntStr == null)
			cntStr = "0";
		int cnt = 0;
		try {
			cnt = Integer.parseInt(cntStr);
		} catch (NumberFormatException ex) {
		}

		String args[] = new String[cnt];
		for (int i = 0; i < cnt; i++) {
			args[i] = getArg("stray_" + i);
		}
		return args;
	}
    
	public static String getString(String argName, String defaultVal) {
		String val = getArg(argName);
		return val==null ? defaultVal : val;
	}
	public static int getInt(String argName, int defaultVal) {
		String val = getArg(argName);
		return val==null ? defaultVal : Integer.parseInt(val);
	}
	public static double getDouble(String argName, double defaultVal) {
		String val = getArg(argName);
		return val==null ? defaultVal : Double.parseDouble(val);
	}
	public static boolean getBoolean(String argName, boolean defaultVal) {
		String val = getArg(argName);
		return val==null ? defaultVal : Boolean.valueOf(val).booleanValue();
	}
}

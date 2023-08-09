package org.placelab.util;

public class TimeUtil {

	public static String getDateAndTime(long l) {
		long time = l / (1000L * 60L * 60L * 24L);
		return time + "_" + getTime(l);
	}
	
	public static String getTime(long l) {
		long time = l / 1000;
		long seconds = time % 60;
		time /= 60;
		long minutes = time % 60;
		time /= 60;
		long hours = time % 24;
		return (hours < 10 ? "0": "") + hours + ":" + 
				(minutes < 10 ? "0" : "") + minutes + ":" +
				(seconds < 10 ? "0" : "") + seconds;
	}

}

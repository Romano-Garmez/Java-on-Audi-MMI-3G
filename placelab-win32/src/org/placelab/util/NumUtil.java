package org.placelab.util;

/**
 * 
 * Utilities for Numbers ;)
 */

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

public class NumUtil {
	public static Random rand = new Random(17); // Call seedRand if you want a different seed
	
	public static void seedRand(long seed) {
		rand = new Random(seed);
	}

	public static String padDouble(double d, int digitsAfterDecimal, int len) {
		long mult=1;
		for (int i=0; i<digitsAfterDecimal; i++) {
			mult *= 10;
		}
		long l = (long)((d + (0.5/mult))*mult);
		double d2 = l/(double)mult;
		if (digitsAfterDecimal == 0) {
			return StringUtil.pad("" + ((int)d2),len);
		} else {
			return StringUtil.pad("" + d2,len);
		}
	}

	public static String doubleToString(double value, int precision) {
		NumberFormat formatter = NumberFormat.getInstance(Locale.US);
		formatter.setMaximumFractionDigits(precision);
		formatter.setMinimumFractionDigits(precision);
		formatter.setGroupingUsed(false);
		return formatter.format(value);
	}	
}

package org.placelab.demo.mapview;

public class MapUtil {
	private static double piValueTable[] = null;
	private static int piHeadingTable[] = null;
	
	private static void fillValueTable() {
		piValueTable = new double[16];
		piHeadingTable = new int[16];
		int curr=0;
		
		for (int i=1; i<17;++i) {
			double mult=(double)i;
			double val=mult*Math.PI/8.0;
			int index=i-1;
			piValueTable[index]=val;
			if (i % 2 == 0) {
				++curr;
			}
			piHeadingTable[index]=(curr % 8) + 1;
		}
	}
	//1 = E 2=NE 3=N 4=NW 5=W 6=SW 7=S 8=SE
	public static int piToEightHeadings(double d) {
		
		if (piValueTable==null) {
			fillValueTable();
		}
		if (d<0.0) {
			throw new IllegalArgumentException("can't handle a negative "+
				"heading in conversion to image number");
		}
		if (d>=2.0*Math.PI) {
			throw new IllegalArgumentException("can't handle a heading "+
				"equal to or greater than 2*PI");
		}
		int result=0;
		while (piValueTable[result]<d) {
			if (result==16) {
				throw new IllegalArgumentException("Internal error: can't find "+
					d+" in the table (past 2PI?)");
			}
			++result;
		}
		return piHeadingTable[result];
	}

}

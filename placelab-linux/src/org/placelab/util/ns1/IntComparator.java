package org.placelab.util.ns1;

/* This class implements a function to compare two objects to provide
   an ordering.  In this case, the two objects to be compared must be
   two Integers, and this function just uses their natural ordering,
   but you could write a new function that applies some different
   type of ordering if desired. */

import java.util.Comparator;

public class IntComparator implements Comparator {
    public int compare(Object o1, Object o2)
    {
		int priority1 = ((Integer)o1).intValue();
		int priority2 = ((Integer)o2).intValue();
	
		return compare(priority1, priority2);
    }

    public int compare(int val1, int val2) {
		if (val1 < val2) 
		    return -1;
		else if (val1 > val2)
		    return +1;
		else
		    return 0;
    }


}

/* Log entries:
   $Log: IntComparator.java,v $
   Revision 1.1  2004/06/04 00:50:31  jhoward
   The recent move to no java 1.2 collections broke my NS1Translator since I was (stupidly) usinga tree to sort.  I went back and changed it to use a priority queue I made back in data structures class (that doesn't use any collections stuff) and its actually way better this way anyhow.

   Revision 1.3  2003/10/24 01:06:09  jamesh
   *** empty log message ***

   Revision 1.2  2003/10/19 03:14:48  jamesh
   Removed the rcsid thing because it was dumband added in the support files for eclipse.

   Revision 1.1  2003/10/19 02:31:39  jamesh

   Initial Checkin

*/

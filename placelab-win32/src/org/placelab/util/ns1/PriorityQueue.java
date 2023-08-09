package org.placelab.util.ns1;

/**
 * Base interface for priority queue implementations.  
 *
 */

public interface PriorityQueue {
	/**
	 * Returns true if priority queue has no elements
	 *
	 * @return 	true if the priority queue has no elements
	 */
	public boolean isEmpty();


	/**
	 * Returns a reference to the minimum element in the priority queue
	 *
	 * @return 	reference to the minimum element in the priority queue.
	 */
	public Object findMin();


	/**
	 * Inserts a new object to the priority queue
	 *
	 * @param x	Object to be inserted into the priority queue.
	 */
	public void insert(Object x);


	/**
	 * Removes the minimum element from the priority queue.
	 *
	 * @return 	reference to the minimum element.
	 */
	public Object deleteMin();


	/**
	 * Erases all elements from the priority queue.
	 */
	public void makeEmpty();

}

/* Log entries:
   $Log: PriorityQueue.java,v $
   Revision 1.1  2004/06/04 00:50:31  jhoward
   The recent move to no java 1.2 collections broke my NS1Translator since I was (stupidly) usinga tree to sort.  I went back and changed it to use a priority queue I made back in data structures class (that doesn't use any collections stuff) and its actually way better this way anyhow.

   Revision 1.2  2003/10/19 03:14:48  jamesh
   Removed the rcsid thing because it was dumband added in the support files for eclipse.

   Revision 1.1  2003/10/19 02:31:39  jamesh

   Initial Checkin

*/

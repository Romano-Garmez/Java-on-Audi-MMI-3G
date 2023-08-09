package org.placelab.util.ns1;

/**
 * A DHeap is like a binary heap except nodes have d children instead of
 * 2.
 * 
 * 
 * Last modified by: jamesh 
 * On 2003/10/28 01:53:46
 */

import java.util.*;

public class DHeap implements PriorityQueue {
	
	private int d; // number of children that a node may have
	
	protected Object[] data; // stores the heap
	
	/// Comparator used for ordering the elements in the priority queue.
	protected Comparator comparator;
	
	/// Default capacity of the heap.
	private static final int DEFAULT_CAPACITY = 100;
	
	/// Grow factor of the heap capacity
	private static final int GROW_FACTOR = 2;
	
	/// Shrink on factor.  The heap will shrink on size 1/SHRINK_ON_FACTOR to 
	/// use a capacity of 1/SHRINK_FACTOR
	private static final int SHRINK_ON_FACTOR = 4;
	
	/// Shrink factor.  The heap capacity will shrink to 1/SHRINK_FACTOR when 
	/// size 1/SHRINK_ON_FACTOR is reached.
	private static final int SHRINK_FACTOR = 2;
	
	/// remember the initial capacity provided to the constructor because it
	/// never shrinks below that.
	private int initialCapacity;
	
	private class RemoveComparator implements Comparator {
	    Object remove;
	    Comparator original;
	    
	    public RemoveComparator(Object toRemove, Comparator original) {
	        remove = toRemove;
	        this.original = original;
	    }
	    
	    public int compare(Object o1, Object o2) {
	        if(o1.equals(remove)) return -1;
	        else if(o2.equals(remove)) return 1;
	        else return original.compare(o1, o2);
	    }
	}
	
	
	/** Default Constructor.
	 *  Initializes the heap with the default capacity.
	 *  @param c an instance of the comparator to use for ordering the heap
	 *  @param dSize the number of children a node can have (>=2)
	 */
	public DHeap(Comparator c, int dSize) {
		this(c, dSize, DEFAULT_CAPACITY);
	}
	
	/** Other Constructor.
	 *  Lets you initialize the heap to whatever capacity you want.
	 *  @param c an instance of the comparator to use for ordering the heap
	 *  @param dSize the number of children a node can have (>=2)
	 *  @param capacity the initial size of the heap (>=1)
	 */
	public DHeap(Comparator c, int dSize, int capacity) {
		// a less than 2-heap would be dumb
		if(dSize < 2) {
			throw new IllegalArgumentException(
					  "Can't have less than a 2 heap");
		}
		// also, not having space for even a root node would be dumb
		if(capacity < 1) {
			throw new IllegalArgumentException(
                      "Can't have a capacity less than 1");
		}
		
		this.d = dSize;
		this.initialCapacity = capacity;
		// capacity + 1 since I use a spot at the beginning to store the size
		this.data = new Object[capacity + 1];
		this.setSize(0);
		this.comparator = c;
	}
	
	/** Finds the size of the heap
	 *  @return the size of the heap
	 */
	public int size() {
		return ((Integer)this.data[0]).intValue();
	}
	
	// convenience method to set the size of the heap
	// for my private use so I don't have to muddle things later
	// doesn't do any error checking for that reason.
	private void setSize(int size) {
		this.data[0] = new Integer(size);
		/* check if the heap should shrink (not in deleteMin as the 
		   corresponding check for growing is in insert because shrink is 
		   not a necessary operation for deleteMin to conclude, whereas
		   grow is necessary for insert.  To say it another way, grow
		   is triggered when I can't insert but shrink is triggered when
		   a certain size/capacity ratio is reached */
		if((size <= (data.length - 1)/SHRINK_ON_FACTOR) && 
				((data.length - 1) > this.initialCapacity)) {
			this.shrink();
		}
	}
	
	
	/**
	 * Removes the requested element from the queue, if it exists.
	 * @return <code>true</code> if element found and removed
	 * <code>false</code> otherwise.
	 */
	public boolean remove(Object o) {
	    // start by finding the element
	    int i;
	    for(i = 1; i < this.size() + 2; i++) {
	        if(this.data[i].equals(o)) break;
	    }
	    // unused spots are nulled out
	    if(data[i] == null) return false;
	    // set the comparator to the remove comparator temporarily
	    Comparator remove = new RemoveComparator(o, comparator);
	    Comparator original = comparator;
	    comparator = remove;
	    percolateUp(i);
	    deleteMin();
	    comparator = original;
	    return true;
	}
	


	/** @return if the heap is empty or not */
	public boolean isEmpty() {
		return (this.size() == 0);
	}

	/** Find the smallest item in the heap.
	 *  @return the smallest item
	 *  @throws EmptyHeapException if heap is empty
	 */
	public Object findMin() {
		if(this.isEmpty()) {
			throw new EmptyHeapException();
		}
		return this.data[1];
	}

	/* (non-Javadoc)
	 * @see PriorityQueue#insert(java.lang.Object)
	 */
	public void insert(Object x) {
		// make sure there is room to add
		if((this.size() + 1) == this.data.length) {
			// then it is full and needs to grow
			this.grow();
		}
		data[this.size() + 1] = x;
		
		percolateUp(this.size() + 1);
		
		this.setSize(this.size() + 1);
	}

	/** Returns and removes the smallest object in the heap.
	 *  @return the smallest object
	 *  @throws EmptyHeapException if the heap is empty
	 */
	public Object deleteMin() {
		if(this.isEmpty()) {
			throw new EmptyHeapException();
		}
		Object min = this.data[1];
		// now go find the last object
		Object last = this.data[this.size()];
		this.data[1] = last;
		this.data[this.size()] = null;
		// i need to set the size prior to percolateDown or else
		// percolateDown will break
		this.setSize(this.size() - 1);
		this.percolateDown(1);
		return min;
	}

	/** Makes the heap empty by replacing the old data array with a new
	 *  empty one of size initialSize.
	 */
	public void makeEmpty() {
		Object[] tmp = new Object[this.initialCapacity + 1];
		this.data = tmp;
		this.setSize(0);
	}
	
	/** Gets the index of the parent node given the index of a child
	 *  node.
	 *  @param child the index of the node to get the parent of
	 *  @return index of the parent node or -1 if no parent node
	 */
	private int getParent(int child) {
		if(child == 1) {
			// then the child is the root and has no parent
			return -1;
		}
		return (int)Math.ceil(((double)child - 1.0) / (double)this.d);
	}
	
	/** Gets the index of a child node given the index of the parent
	 *  and a number between 0 and d-1 for the child (if > d-1 is given, it returns -1).
	 *  @param parent the index of the node to get the child of
	 *  @param childNumber the particular child index to get
	 *  @return the child corresponding to the parent and childNumber or -1 if child doesn't exist
	 */
	private int getChild(int parent, int childNumber) {
		if(childNumber >= d) return -1;
		int childLoc = ((this.d) * (parent - 1)) + (2 + childNumber);
		if(childLoc > this.size()) return -1; // the child at that place doesn't exist
		return childLoc;
	}
	
	/// grows the heap by GROW_FACTOR
	/// used when the heap is full and needs to expand
	private void grow() {
		Object[] temp = new Object[((this.data.length - 1) * GROW_FACTOR) + 1];
		System.arraycopy(this.data, 0, temp, 0, data.length);
		this.data = temp;
	}
	
	/// shrinks the heap by SHRINK_FACTOR
	/// used when the heap size reaches 1/SHRINK_ON_FACTOR
	private void shrink() {
		Object[] temp = new Object[(int)((double)(this.data.length -1) * (1.0/(double)SHRINK_FACTOR)) + 1];
		System.arraycopy(this.data, 0, temp, 0, this.size() + 1);
		this.data = temp;
	}
	
	/**
	 * Method to percolate up from a given node in the heap.
	 * @param startNode	the index of the node at which the percolate begins.
	 */
	protected void percolateUp(int startNode) {
		Object parent;
		Object tmp = data[startNode];
		int parentIndex;
		if((parentIndex = this.getParent(startNode)) != -1) {
			parent = data[parentIndex];
			if(comparator.compare(tmp, parent) < 0) {
				// then the one I am percolating up is smaller than
				// its parent so swap them
				this.swap(startNode, parentIndex);
				// continue percolating up (value at data[parentIndex]
				// is what was at data[startNode])
				this.percolateUp(parentIndex);
			} // else its fine where it is
		} // else its already at the top
	}
	
	/**
	 * Method to percolate down from a given node in the heap.
	 * The runtime for percolateDown ought to be O(d log(base d) n)
	 * since d time to find smallest node for log(base d) n levels 
	 * @param startNode	the index of the node at which the percolate begins.
	 */
	protected void percolateDown(int startNode) {
		int smallChild;
		if((smallChild = this.smallestChild(startNode)) != -1) {
			// find out if data at smallChild is smaller than the
			// data at startNode
			if(comparator.compare(this.data[smallChild], this.data[startNode])
			   < 0) {
				// then a smaller child exists
				this.swap(smallChild, startNode);
				// the value at smallChild is now the value that 
				// was at startNode so I am still moving the same value
				// on down the heap
				this.percolateDown(smallChild);
		    }
			// else there isn't a smaller child, the node is fine
			// where it is
		}
		// else there aren't any children, the node is fine where it is
	}

	// helper methods to clarify percolateDown ...

	/// find the smallest child of a parent node.  
    /// returns -1 if there are no
	/// child nodes
	/// runtime ought to be O(d)
	private int smallestChild(int parent)
	{
		Object smallest;
		int childCount = 0;
		int smallestIndex = this.getChild(parent, childCount);
		if(smallestIndex != -1)
			smallest = this.data[smallestIndex];
		else
			return -1; // there isn't a smallestChild
		childCount++;
		for(int i = this.getChild(parent, childCount);
			i != -1; i = this.getChild(parent, ++childCount)) {
			Object next = this.data[i];
			if(comparator.compare(smallest, next) > 0) {
				smallest = next;
				smallestIndex = i;
			}
	    }
		return smallestIndex;
	}
	
	/// performs a simple swap between node1 and node2
	/// runtime ought to be O(1)
	private void swap(int node1, int node2) {
		Object tmp = data[node1];
		data[node1] = data[node2];
		data[node2] = tmp;
	}
	
	/** Returns a level-order traversal of the heap
		@return the string representation
	*/
	public String toString() {
		int nextLevel = 1;
		int levelCount = 1;
		java.io.StringWriter writer = new java.io.StringWriter();
		writer.write(this.d + "-Heap of size " + this.size() + "\n");
		for(int i = 1; i <= this.size(); i++) {
			writer.write(this.data[i].toString() + " ");
			if((i % nextLevel) == 0) {
				// then it begins a new level
				writer.write("\n");
				nextLevel += Math.pow(this.d, levelCount);
				levelCount++;
			}
			
		}
		return writer.toString();
	}


	/// for testing purposes
	public static void main(String args[]) {
		Comparator c = new IntComparator();
		Random rand = new Random();
		
		if(args.length != 3) {
			System.err.println("Requires arguments <start d> <end d> <initial inserts>");
			System.exit(1);
		}
		
		int startD = Integer.parseInt(args[0]);
		int endD = Integer.parseInt(args[1]);
		int initialInserts = Integer.parseInt(args[2]);
		
		DHeap heap;
		for(int d = startD; d <= endD; d++) {
			System.out.println("Making new +" + d + "-heap");
			heap = new DHeap(c, d);
			// now insert a few values
			for(int i = 1; i <= initialInserts; i++) {
				heap.insert(new Integer(i));
				System.out.println("Inserted " + i);
				System.out.println("Heap is now:");
				System.out.println(heap);
			}
			// now remove the min
			System.out.println("Deleted the min and its " + heap.deleteMin());
			// now decrement until exception (hopefully)
			System.out.println("Now findMin reports " + heap.findMin());
			try {
				while(true) {
					System.out.println("Deleted the min and its " + heap.deleteMin());
				}
			} catch (EmptyHeapException ehe) {
				System.out.println("Encountered empty heap exception");
			}
			System.out.println("Now packing the heap with " + initialInserts + " random numbers");
			for(int i = 0; i < initialInserts; i++) {
				heap.insert(new Integer(rand.nextInt(initialInserts)));
			}
			System.out.println("Heap is:");
			System.out.println(heap);
			System.out.println("Now deleting min until exception");
			try {
				while(true) {
					System.out.println("Deleted the min and its " + heap.deleteMin());
				}
			} catch (EmptyHeapException ehe) {
				System.out.println("Encountered empty heap exception");
			}
			System.out.println("Heap empty: ");
			System.out.println(heap);
			System.out.println("Now testing heap grow by adding 1000 random elements");
			for(int i = 0; i < 10000; i++) {
				heap.insert(new Integer(rand.nextInt()));
			}
			System.out.println("Now testing heap shrink by deleteMin until exception");
			try {
				while(true) {
					heap.deleteMin();
				}
			} catch (EmptyHeapException ehe) {
				System.out.println("Encountered empty heap exception");
			}
			System.out.println("Filling heap with 10000 elements");
			for(int i = 0; i < 10000; i++) {
				heap.insert(new Integer(rand.nextInt()));
			}
			System.out.println("Emptying heap");
			heap.makeEmpty();
			System.out.println("Heap Emptied.  Heap is: ");
			System.out.println(heap);
			System.out.println("Now testing heap remove");
			System.out.println("Loading heap with values 1-10");
			for(int i =1; i < 11; i++) heap.insert(new Integer(i));
			System.out.println("Heap is:");
			System.out.println(heap);
			System.out.println("Now removing 4");
			heap.remove(new Integer(4));
			System.out.println("Now heap is:");
			System.out.println(heap);
			try {
				while(true) {
					System.out.println("Deleted the min and its " + heap.deleteMin());
				}
			} catch (EmptyHeapException ehe) {
				//System.out.println("Encountered empty heap exception");
			}
		}
	}

}

/*  Log entries:
	Revision 1.7  2003/10/28 01:53:46  jamesh
	Yay!  It all works, even A*
	
	Revision 1.6  2003/10/21 04:31:36  jamesh
	changed makeEmpty to just allocate a new array and point data to it
	instead of nulling out the individual locations.
	
	Revision 1.5  2003/10/21 01:41:00  jamesh
	Fixed a problem where smallestChild was really broken and caused
	deleteMin to take forever
	
	Revision 1.4  2003/10/21 01:10:42  jamesh
	Fixed bugs of various and widespread natures that became apparent
	after use of the full test battery.  Full test battery is also, obviously,
	included in this revision.
	
	Revision 1.3  2003/10/20 23:17:48  jamesh
	Fixed the bug in percolateUp, I had that if no parent, then try to percolate
	but it needed to be only try to percolate if parent exists.  Also modifications
	to toString() so that it really will do level-order traversal instead of, um,
	not doing it.
	
	Revision 1.2  2003/10/20 21:13:59  jamesh
	DHeap methods all filled in, but they don't work so they need to be debugged
	I expect its just simple off by one errors
	
	The test battery for DHeap is partially implemented, but must be finished
	
	Revision 1.1  2003/10/20 07:17:33  jamesh
	put the stuff i worked on on sunday in so I could workon them at school tomorrow
	
*/

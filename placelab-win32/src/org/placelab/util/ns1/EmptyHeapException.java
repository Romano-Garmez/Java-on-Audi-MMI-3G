package org.placelab.util.ns1;

/**
* This exception signifies an invalid access on an empty heap.
* We subclass UndeflowException because this is a type of
* underflow.  However, it is nice, since exceptions are
* dispatched on type, to make a more specific exception so
* we have more information on what happened if/when we try
* to handle the error.
*
* 
*
*/


public class EmptyHeapException extends UnderflowException {
	public EmptyHeapException() 
	{}

	public EmptyHeapException(String message) {
	    //super(message);
	}
}

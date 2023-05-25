package org.placelab.util.ns1;

/*  UnderflowException should be thrown when the data structure is empty yet
	someone is still trying to get data from it.

	There is a method that can be checked in this projects stack to see if its 
	empty, so this is a RuntimeException

	
	CSE 326 Fall Quarter 2003
	Homework 1, October 2
*/

import java.lang.RuntimeException;

public class UnderflowException extends RuntimeException {
	public UnderflowException() {
	    //super("Its already empty!");
	}
}


/*
 * @(#)src/demo/jvmti/gctest/README.txt, dsdev, dsdev, 20060202 1.2
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v1.4.2
 * (C) Copyright IBM Corp. 2004. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 * ===========================================================================
 */
gctest: @(#)README.txt	1.6 04/06/23

This agent library can be used to track garbage collection events.
You can use this agent library as follows:

    java -agentlib:gctest ...

The Events JVMTI_EVENT_GARBAGE_COLLECTION_START,
JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, and JVMTI_EVENT_OBJECT_FREE 
all have limitations as to what can be called directly inside the 
agent callback functions (e.g. no JNI calls are allowed, and limited 
interface calls can be made). However, by using raw monitors and a separate 
watcher thread, this agent demonstrates how these limitations can be 
easily avoided, allowing the watcher thread to do just about anything
after the JVMTI_EVENT_GARBAGE_COLLECTION_FINISH event.

If the Virtual Machine complains that it can't find the library, 
you may need to add the directory containing the library into the 
LD_LIBRARY_PATH environment variable (Unix), or the PATH environment 
variable (Windows).



package org.placelab.demo.virtualgps;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to encapsulate the IO streams for serial ports on different
 * platforms in a generic way.
 */

public abstract class IOPort {
	
	public abstract InputStream getInputStream ();
	
	public abstract OutputStream getOutputStream ();
	
}

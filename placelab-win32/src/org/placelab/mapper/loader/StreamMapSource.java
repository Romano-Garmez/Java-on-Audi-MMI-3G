/*
 * Created on Aug 25, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;

/**
 * 
 */
public abstract class StreamMapSource implements MapSource {
	protected String name;
	protected boolean enabled;
	
	protected StreamMapSource(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}
	
	public String getName() {
		return name;
	}

	public boolean isDefault() {
		return enabled;
	}

	public abstract Iterator query(Coordinate one, Coordinate two);
	
	protected Iterator queryImpl(InputStream stream) {
		return new StreamIterator(stream);
	}

	protected class StreamIterator implements Iterator {
		protected BufferedReader reader;
		protected String nextLine;
		
		public StreamIterator(InputStream stream) {
			reader = new BufferedReader(new InputStreamReader(stream));
			nextLine = getNext();
		}
		
		protected String getNext() {
			try {
				return reader.readLine();
			} catch (IOException e) {
				return null;
			}
		}
		
		public boolean hasNext() {
			return nextLine != null;
		}
	
		public Object next() {
			String line = nextLine;
			nextLine = getNext();
			return line;
		}
	
		public void remove() {
			throw new UnsupportedOperationException("Does not support remove");
		}
	}
}

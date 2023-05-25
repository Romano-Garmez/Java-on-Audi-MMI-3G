/*
 * Created on Aug 20, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.IOException;

import org.placelab.collections.Iterator;
import org.placelab.core.Coordinate;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.mapper.WigleDownloader;

public class WigleMapSource implements MapSource {
	protected WigleDownloader downloader;
	
	public WigleMapSource() {
		downloader = new WigleDownloader();
		try {
			downloader.authenticate();
		} catch (IOException e) {}
	}
	
	public WigleMapSource(String login, String password) {
	    this();
	    setLogin(login, password);
	}
	
	public void setLogin(String login, String password) {
	    PlacelabProperties.set("placelab.wigle_username", login);
	    PlacelabProperties.set("placelab.wigle_password", password);
	}
	
	public String getName() {
		return "Wigle.net";
	}
	
	public boolean isDefault() {
		return downloader != null && downloader.isAuthenticated();
	}
	
	public Iterator query(Coordinate one, Coordinate two) throws MapSourceException {
		try {
            if(!downloader.authenticate()) {
                throw new MapSourceException("wigle authentication failed");
            }
        } catch (IOException e) {
            throw new MapSourceException(e.getMessage());
        } 
		
		return downloader == null ? null :
				downloader.query((TwoDCoordinate)one, (TwoDCoordinate)two);
	}
}

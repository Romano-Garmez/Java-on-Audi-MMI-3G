/**
 * This class is the data backing for a place in the XMapDemo.  Places are 
 * independent of maps, and belong to a set of related places or types of
 * places.  Places have:
 * + A sense of importance
 * + A lat/lon
 * + An icon (an image)
 * 
 * PlaceBacking replaces MapData.icon since MapData is being scrapped
 */
package org.placelab.demo.mapview;

import org.eclipse.swt.graphics.ImageData;

public class PlaceBacking extends ItemBacking {
	public String type;
	public String name;
	public String url;
	public String text;
	public double lat;
	public double lon;
	
	/**
	 * Use this constructor for PlaceBackings loaded from map wads
	 */
	public PlaceBacking(String type,
						String name,
						String url,
						String text,
						double lat,
						double lon,
						String imageName,
						String wadRelativeImagePath,
						WadData loadedFromWad) {
		super(imageName, wadRelativeImagePath, loadedFromWad);
		this.type = type;
		this.name = name;
		this.url = url;
		this.text = text;
		this.lat = lat;
		this.lon = lon;
	}
	
	/**
	 * Use this constructor when generating a PlaceBacking
	 */
	public PlaceBacking(String type,
						String name,
						String url,
						String text,
						double lat,
						double lon,
						String iconName,
						ImageData icon)
	{
		super(icon, iconName);
		this.type = type;
		this.name = name;
		this.url = url;
		this.text = text;
		this.lat = lat;
		this.lon = lon;
	}
	
}

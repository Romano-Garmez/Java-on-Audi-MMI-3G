package org.placelab.demo.mapview;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.placelab.core.TwoDCoordinate;

/**
 * MapBacking is the data backing for a map to be displayed.  Maps have:
 * + An origin in gps (upper left)
 * + An image that is the map
 * + Pixels per latitude, and Pixels per longitude
 * + A name
 */
public class BitmapMapBacking extends ItemBacking implements MapBacking {
	private double originLat;
	private double originLon;
	private double pixelsPerLat;
	private double pixelsPerLon;
	private String name;
	
	/**
	 * Use this constructor if you are generating a MapBacking.
	 * @param mapImage the image data for the map
	 * @param mapImageName the filename for the map image (like amap.png)
	 * @param originLat the latitude of the upper left of the image
	 * @param originLon the longitude of the upper left of the image
	 * @param pixelsPerLat 
	 * @param pixelsPerLon
	 * @param mapName the user defined name for the map
	 */
	public BitmapMapBacking(ImageData mapImage,
					  String mapImageName, double originLat, double originLon,
					  double pixelsPerLat, double pixelsPerLon, String mapName) {
		super(mapImage, mapImageName);
		this.originLat = originLat;
		this.originLon = originLon;
		this.pixelsPerLat = pixelsPerLat;
		this.pixelsPerLon = pixelsPerLon;
		this.name = mapName;
	}
	
	/**
	 * Use this constructor when loading a BitmapMapBacking from a map wad
	 * @param imageName the filename used for the image
	 * @param wadRelativeImagePath the path needed to get the image out of the
	 * 		wad
	 * @param loadedFromWad the wad this MapBacking was loaded from
	 * @param mapName the user defined name for the map
	 */
	public BitmapMapBacking(String imageName,
					  String wadRelativeImagePath,
					  WadData loadedFromWad,
					  double originLat,
					  double originLon,
					  double pixelsPerLat,
					  double pixelsPerLon,
					  String mapName) {
		super(imageName, wadRelativeImagePath, loadedFromWad);
		this.name = mapName;
		this.originLat = originLat;
		this.originLon = originLon;
		this.pixelsPerLat = pixelsPerLat;
		this.pixelsPerLon = pixelsPerLon;
	}
	
	/**
	 * Use this constructor when loading a BitmapMapBacking from a map wad and you don't
	 * have a pixelsPerLat and pixelsPerLon, but you do have the lat, lon for the
	 * upper right corner of the map.  This has the disadvantage of circumventing
	 * the lazy loading scheme the WadData uses for the images, since we have to
	 * know the image dimensions to use this, which requires loading the image.
	 * @param imageName the filename used for the image
	 * @param wadRelativeImagePath the path needed to get the image out of the
	 * 		wad
	 * @param loadedFromWad the wad this MapBacking was loaded from
	 * @param mapName the user defined name for the map
	 */
	public BitmapMapBacking(String imageName,
					  String wadRelativeImagePath,
					  WadData loadedFromWad,
					  String mapName,
					  double originLat,
					  double originLon,
					  double upperRightLat,
					  double upperRightLon)
	{
		super(imageName, wadRelativeImagePath, loadedFromWad);
		int width = this.getImageResource().width;
		int height = this.getImageResource().height;
		this.pixelsPerLat = (double)height / (upperRightLat - originLat);
		this.pixelsPerLon = (double)width / (upperRightLon - originLon);
		this.originLat = originLat;
		this.originLon = originLon;
		this.name = mapName;
	}

	public double getOriginLat()    { return originLat; }
	public double getOriginLon()    { return originLon; }
	public double getPixelsPerLat() { return pixelsPerLat; }
	public double getPixelsPerLon() { return pixelsPerLon; }
	public int latitudeToPixels(double lat) {
		double tmp = getImageResource().height -
			((lat - originLat) * pixelsPerLat);
		return (int)tmp;
	}
	public int longitudeToPixels(double lon) {
		double tmp = (lon - originLon) * pixelsPerLon;
		return (int)tmp;
	}	
	public String getName() { return name; }
	
	public double getLatHeight() {
		return getImageResource().height / getPixelsPerLat();
	}

	public double getLonWidth() {
		return getImageResource().width / getPixelsPerLon();
	}
	
	public double getMaxLat() {
		return getOriginLat() + getLatHeight();
	}
	
	public double getMaxLon() {
		return getOriginLon() + getLonWidth();
	}
	
	public boolean containsCoordinate(TwoDCoordinate coord) {
		return coord.getLatitude() > getOriginLat() && coord.getLatitude() < getMaxLat() &&
				coord.getLongitude() > getOriginLon() && coord.getLongitude() < getMaxLon();
	}
	
	public Point getPoint(TwoDCoordinate coord) {
		if (!containsCoordinate(coord))
			return null;
		
		return new Point(longitudeToPixels(coord.getLongitude()), 
				latitudeToPixels(coord.getLatitude()));
	}
}

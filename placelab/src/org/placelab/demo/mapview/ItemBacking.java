/*
 * ItemBacking provides support for items that are loaded out of map wads.
 * Since the graphics for an item may be large and since the XMapDemo
 * might not need all of the graphics, it is worth deferring their loading
 * until they are needed, and it is also worth unloading them from memory
 * when they won't be needed anymore.
 * 
 * To that end, this class keeps a reference to the WadData that created it
 * and it can ask the WadData to get its resources for it when it needs them
 * After that the image data is stored here in image.
 */

package org.placelab.demo.mapview;

import org.eclipse.swt.graphics.ImageData;

public abstract class ItemBacking {
	// the image data is lazily loaded from the wad, and then
	// cached here once it is loaded if requested
	protected ImageData image;
	
	// the filename for the image
	protected String imageName;
	
	// I retain a reference to my loading wad (if I was loaded
	// from a wad) so that if my image isn't here, I can 
	// have the wad fetch it for me.
	protected String wadRelativeImagePath;
	protected WadData loadWad;
	
	
	/**
	 * This constructor is used if you are creating an ItemBacking without
	 * loading it from a wad (that is, you're generating the item or 
	 * something)
	 */
	public ItemBacking(ImageData image, String imageName) {
		this.image = image;
		this.imageName = imageName;
		assignDefaultImagePath();
		loadWad = null;
	}
	
	/**
	 * Use this constructor when the ItemBacking is being loaded from
	 * the wad.
	 */ 
	public ItemBacking(String imageName, String wadRelativeImagePath,
					   WadData loadedFromWad)
	{
		this.imageName = imageName;
		this.wadRelativeImagePath = wadRelativeImagePath;
		this.loadWad = loadedFromWad;
		this.image = null;
	}
	
	
	protected void assignDefaultImagePath() {
		wadRelativeImagePath = "images/" + imageName;
	}
	
	public String getWadRelativeImagePath() {
		return wadRelativeImagePath;
	}
	
	/**
	 * Loads the image data from the wad, or simply returns it if 
	 * it is cached
	 * @return the image data or null if it cannot be found
	 */
	public ImageData getImageResource() {
		ImageData ret = null;
		if(image != null) {
			ret = image;
		} else {
			try {
				ret = loadWad.getImageData(wadRelativeImagePath);
			} catch (Exception e) {
				ret = null;
			}
		}
		this.image = ret;
		return ret;
	}

}

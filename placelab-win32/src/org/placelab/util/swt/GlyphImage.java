package org.placelab.util.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.collections.HashMap;
import org.placelab.collections.Iterator;


public class GlyphImage extends Glyph {
	protected Image originalImage;
	protected Image image;
	protected ImageData data;
	//private Point  location;
	private int    anchor;
	
	protected HashMap zoomCache;
	
	protected boolean doCaching;
	
	private void init() {
		originalImage = null;
		image         = null;
		data          = null;
		anchor        = Glyph.ANCHOR_NW;
		//zoom          = 1.0;
		doCaching = true;
		zoomCache = new HashMap();
	}

	public GlyphImage(GlyphComposite parent, int style) {
		super(parent, style);
		init();
	}
	public GlyphImage(GlyphHolder holder, int style) {
		super(holder, style);
		init();
	}
	public void setImage(Image image, Point location) {
		setImage(image, location.x, location.y, Glyph.ANCHOR_NW);
	}
	public void setImage(Image image, int x, int y) {
		setImage(image, x, y, Glyph.ANCHOR_NW);
	}
	public void setImage(Image image, Point location, int anchor) {
		setImage(image, location.x, location.y, anchor);
	}

	public void setImage(Image image, int x, int y, int anchor) {
		Rectangle invalidate = getBounds();
		if (this.image != this.originalImage && this.image != null) {
			this.image.dispose();
		}
		this.setLocation(new Point(x, y));
		this.originalImage = image;
		this.image         = getZoomedImage();
		this.data          = image.getImageData();
		this.anchor        = anchor;
		
		redraw(invalidate);
	}
	public boolean getCachesZoomHistory() {
		return doCaching;
	}
	/**
	 * When a zoom is requested, the GlyphImage first
	 * checks to see if it has a cached version of the
	 * image at that size (or one within one one hundredth
	 * of a factor) and it returns that.  If not, it will 
	 * compute the scaled version and cache that for future
	 * requests, supposing it caches the zoom history.
	 * 
	 * It turns out that the image scaling takes more than
	 * 90% of the time in a zoom operation (the rest goes to
	 * redraws and point translation I presume) so this can
	 * speed things up significantly.
	 * 
	 * The downside of caching the zoom history is that it
	 * requires that the various sizes of the image sit around
	 * in memory.  You'll want to only alternate between a few
	 * different zoom factors so as not to have hundreds of images
	 * sitting around taking up space.
	 * 
	 * Setting the zoom history flag to false will ensure that no
	 * more scaled images are cached, but it doesn't clear the existing
	 * zoom cache
	 * @param flag whether or not to cache scaled versions of the image
	 */
	public void setCachesZoomHistory(boolean flag) {
		doCaching = flag;
	}
	public void clearZoomHistory() {
		int j = 0;
		Iterator it = this.zoomCache.values().iterator();
		while(it.hasNext()) {
			Image i = (Image)it.next();
			if(!i.isDisposed()) {
				i.dispose();
				j++;
			}
		}
		zoomCache.clear();
		/*System.out.println("clearZoomHistory: " + j + 
				" images disposed");*/
	}
	public Image getImage() {
		return image;
	}
	public void setZoom(double z) {
		super.setZoom(z);
		Rectangle invalidate = this.getBounds();
		if(z > zoom) {
			zoom = z;
			image = getZoomedImage();
			this.redraw(image.getBounds());
		} else {
			zoom = z;
			image = getZoomedImage();
			this.redraw(invalidate);
		}
	}
	public double getZoom() { return zoom; }
	
	public void dispose() {
		super.dispose();
		if (image != originalImage && image != null) {
			image.dispose();
			/*System.out.println("GlyphImage dispose: " + 
					"current image disposed");*/
		}
		clearZoomHistory();
		image = null;
		originalImage = null;
		data = null;
		// don't dispose the original image here.  Let the 
		// application do it
		//System.out.println("GlyphImage disposed");
	}
	protected Image getZoomedImage() {
		String z = "" + (int)(zoom * 100.0);
		if (zoom == 1.0 || zoom <= 0.0 ||
		    originalImage == null || 
			(int)(zoom * 100) == 100) return originalImage;
		
		//System.out.println("Zoom code: " + z);
		if(zoomCache.containsKey(z)) {
			return (Image)zoomCache.get(z);
		}
		//long time = System.currentTimeMillis();
		Rectangle bounds = originalImage.getBounds();
		Rectangle zoomed = new Rectangle
			(0, 0, (int) (((double)bounds.width) * zoom + 0.5),
			 (int) (((double)bounds.height) * zoom + 0.5));

		// jws - restored this method of creating images: 
		// it seems to avoid out-of-handle errors better
		Image image= new Image(getHolder().getDisplay(), 
				       originalImage.getImageData().scaledTo
				       (zoomed.width, zoomed.height));

		// this way to do the scaling gives the same results as the
		// above way, but it seems to be twice as fast for me.
		// go figure.
		//Image image = new Image(getHolder().getDisplay(), zoomed);
		//GC gc = new GC(image);
		//gc.drawImage(originalImage, 0, 0, bounds.width, bounds.height,
		//	     0, 0, zoomed.width, zoomed.height);
		//gc.dispose();

		zoomCache.put(z, image);
		//time = System.currentTimeMillis() - time;
		//System.out.println("Image scale: " + time + "ms");
		return image;
	}
	private Point nw_corner() {
		if (image == null || image.isDisposed()) return new Point(0,0);

		Rectangle r = image.getBounds();
		Point p = new Point(getLocation().x, getLocation().y);

		switch (anchor) {
		case Glyph.ANCHOR_N:
			p.x -= r.width/2;
			break;
		case Glyph.ANCHOR_NE:
			p.x -= r.width - 1;
			break;
		case Glyph.ANCHOR_E:
			p.x -= r.width - 1;
			p.y -= r.height/2;
			break;
		case Glyph.ANCHOR_SE:
			p.x -= r.width - 1;
			p.y -= r.height- 1;
			break;
		case Glyph.ANCHOR_S:
			p.x -= r.width/2;
			p.y -= r.height - 1;
			break;
		case Glyph.ANCHOR_SW:
			p.y -= r.height - 1;
			break;
		case Glyph.ANCHOR_W:
			p.y -= r.height/2;
			break;
		case Glyph.ANCHOR_C:
			p.x -= r.width /2;
			p.y -= r.height/2;
			break;
		default:
			break;
		}
		return p;
	}
			
	protected void paintImpl(PaintEvent e, GlyphGC gc) {
		super.paintImpl(e, gc);
		if (image==null) return;
		Point p = nw_corner();
		Rectangle r = getBounds();
		Rectangle s = new Rectangle(e.x, e.y, e.width, e.height);
		r = r.intersection(s);

		gc.drawImage(image, r.x-p.x, r.y-p.y, r.width, r.height,
			     r.x, r.y, r.width, r.height);
	}

	protected Rectangle getBoundsImpl() {
		if (image==null || image.isDisposed()) return new Rectangle(0,0,0,0);
		Point p = nw_corner();
		Rectangle r = image.getBounds();
		r.x = p.x;
		r.y = p.y;
		return r;
	}
	protected boolean pointInsideImpl(int x, int y) {
		if (image==null) return false;
		Rectangle r = getBounds();
		if (!r.contains(x, y)) return false;
		if (getFlag(Glyph.IGNORE_TRANSPARENCY)) return true;

		x -= r.x;
		y -= r.y;

		int closeEnough = getCloseEnough();
		Rectangle test = new Rectangle(x-closeEnough, y-closeEnough,
					       1 + 2*closeEnough,
					       1 + 2*closeEnough);
		test = test.intersection(image.getBounds());
		for (x=test.x; x < test.x + test.width; x++) {
			for (y=test.y; y < test.y + test.height; y++) {
				if (image.getImageData().getPixel(x, y) != 
				    data.transparentPixel) return true;
			}
		}
		return false;
	}
}

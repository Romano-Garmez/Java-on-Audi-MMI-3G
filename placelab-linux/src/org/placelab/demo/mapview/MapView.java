package org.placelab.demo.mapview;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.core.TwoDCoordinate;
import org.placelab.util.swt.Glyph;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphImage;
import org.placelab.util.swt.GlyphRectangle;
import org.placelab.util.swt.SwtScrolledComposite;

public class MapView extends SwtScrolledComposite {
    
	protected GlyphHolder holder;
	// this is only used when in bitmap mode.  it is null in tiger mode
	protected GlyphImage mapImageGlyph = null;
	protected Image mapImage;
	
	// this is an instance of BitmapMapBacking when in bitmap mode, and
	// TigerMapBacking when in tiger mode
	protected MapBacking mapData;
	
	// this is the history for the map zooming when in tiger mode
	// since it is all vector data, any size we choose is just arbitrary anyway
	// so the concept of a zoom level doesn't apply in tiger mode
	// instead, users may "zoom" by choosing a rectangle that they wish to see in
	// detail and the view will then do what is effectively a map change to highlight
	// that area.  So with the mapHistory we store the map backings for all of those
	// areas and that way when the user zooms out the history of regions highlighted
	// is reversed.
	protected LinkedList mapHistory = null;
	
	// the tiger drawing code is implemented as an overlay for the sake of convenience
	// it is null when the mapview is not in tiger mode
//ALM	protected TigerOverlay tigerOverlay = null;
	
//ALM	protected boolean tigerMode = false;
	
	// a Hashtable of PlaceBackings => MapIcons
	protected Hashtable places;
	protected Vector placeIcons;
	
	public LinkedList overlays;
	
	protected Cursor iconCursor;
	protected Color iconTextBg;
	
	protected MouseEvent dragBegin = null;
	protected GlyphRectangle dragOutline = null;
	
	// used to work around too many resize messages
	protected Rectangle oldBounds = null;
	
	//protected int zoomIndex = 3;
	
	// the zoom values only apply to bitmap mode
	protected double[] zoomValues = new double[]{0.25, 0.5, 0.75, 1.0, 1.5, 2.0};
	
	protected double zoom;
	
	//private static final double ZOOM_STEP = 1.25;
	protected static final int SHIFT = 20;
	protected static final double SMALL_DEVICE_SIZE = 5.0; // inches
	
	public static boolean drawBorder = true;
	
	public boolean isVisible() { return true; }
	
	protected static boolean getUseScrollBars(Display display) {
		Point dpi = display.getDPI();
		Rectangle bounds = display.getBounds();
		double width  = ((double)bounds.width )/dpi.x;
		double height = ((double)bounds.height)/dpi.y;

		/*System.out.println("Screen size is "+width+"\" x "+height+
		  "\"");*/

		/* don't use scrollbars if our device is too small */
		if (width  < SMALL_DEVICE_SIZE) return false;
		if (height < SMALL_DEVICE_SIZE) return false;
		return true;
	}
	private static int checkStyle(int style, boolean useScrollBars) {
		if (useScrollBars) style |= SWT.H_SCROLL | SWT.V_SCROLL;
		if (drawBorder) {
			style |= SWT.BORDER;		
		}
		return style;
	}
	
	Cursor getIconCursor() { return iconCursor; }
	
	public MapView(Composite parent, int style) {
		this(parent, style, getUseScrollBars(parent.getDisplay()));
	}
	
	public MapView(Composite parent, int style, boolean useScrollBars) {
		super(parent, checkStyle(style, useScrollBars));
		setAlwaysShowScrollBars(false);
		setLayout(new FillLayout());
		holder = new GlyphHolder(this, SWT.NONE);
		holder.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPress(e);
			}
		});
		setContent(holder);
		//mapImageGlyph = new GlyphImage(holder, SWT.NONE);
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				((MapView)e.widget).onDispose();
			}
		});
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
			    // there is a tendency to get controlResized events way too many times
			    // only actually do something about it if there really is a change
			    if(oldBounds != null && (oldBounds.x != getBounds().x ||
			            oldBounds.y != getBounds().y || oldBounds.width != getBounds().width
			            || oldBounds.height != getBounds().height)) {
//ALM				    if(tigerMode) {
//				        System.out.println("resized");
//				        holder.setSize(getBounds().width, getBounds().height);
//				        setMapData(mapData);
//				    } else {
						Rectangle r = getBounds();
						if (getVerticalBar() != null)
							getVerticalBar().
								setPageIncrement
								(r.height-10);
						if (getHorizontalBar() != null)
							getHorizontalBar().
								setPageIncrement
								(r.width-10);
//				    }
			    }
			    oldBounds = new Rectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPress(e);
			}
		});
		holder.addMouseListener(new MouseAdapter() {
		    public void mouseDown(MouseEvent e) {
		        dragBegin = e;
		    }
		    public void mouseDoubleClick(MouseEvent e) {
		        dragBegin = null;
		    }
		    public void mouseUp(MouseEvent e) {
		        if(dragBegin != null && e.x != dragBegin.x && e.y != dragBegin.y) {
		            // the user has dragged out a rectangle
		            // zoom to it
		            Rectangle area = new Rectangle(Math.min(dragBegin.x, e.x), Math.min(dragBegin.y, dragBegin.x),
		                    Math.abs(dragBegin.x - e.x), Math.abs(dragBegin.y - e.y));
		            dragOutline.dispose();
		            dragOutline = null;
		            zoomForRectangle(area);
		        }
	            dragBegin = null;
		    }
		});
		holder.addMouseMoveListener(new MouseMoveListener() {
		    public void mouseMove(MouseEvent e) {
		        //System.out.println("mouse moved");
		        if(dragBegin != null) {
		            if(dragOutline == null) {
		                dragOutline = new GlyphRectangle(holder, 0);
		                dragOutline.setThickness(1);
		                dragOutline.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		                // too bad swt sucks and doesn't support alpha or we could do something really cool
		                // looking here
		                dragOutline.setBackground(null);
		            }
		            Rectangle area = new Rectangle(Math.min(dragBegin.x, e.x), Math.min(dragBegin.y, dragBegin.x),
		                    Math.abs(dragBegin.x - e.x), Math.abs(dragBegin.y - e.y));
		            dragOutline.set(area);
		        }
		    }
		});
		
		//allGlyphs = new HashSet();
		//currentGlyphs = new HashSet();
		placeIcons = new Vector();
		places = new Hashtable();
		iconCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
	
		iconTextBg = new Color(getDisplay(), 255, 255, 180);
		overlays = new LinkedList();
		mapHistory = new LinkedList();
	}
	
	
	public void zoomForRectangle(Rectangle area) {
//ALM	    if(tigerMode) {
//	        double lat1 = this.pixelsToLatitude(area.y);
//	        double lat2 = this.pixelsToLatitude(area.y + area.height);
//	        double lon1 = this.pixelsToLongitude(area.x);
//	        double lon2 = this.pixelsToLongitude(area.x + area.width);
//	        TwoDCoordinate one = new TwoDCoordinate(lat1, lon1);
//	        TwoDCoordinate two = new TwoDCoordinate(lat2, lon2);
//	        try {
//                TigerLineData subData = ((TigerMapBacking)mapData).getTigerLineData().getSubset(
//                        new LocationFilter(one, two));
//                MapBacking subBacking = new TigerMapBacking(mapData.getName(), subData);
//                mapHistory.addFirst(mapData);
//                this.setMapData(subBacking);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (InvalidRecordException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//	    }
	}
	
	/**
	 * Adds an overlay to the mapview.
	 * MapViewOverlays call this themselves when they are created,
	 * so you should never have to call it yourself
	 */
	public void addOverlay(MapViewOverlay overlay) {
	    overlays.add(overlay);
	}
	
	public void removeOverlay(MapViewOverlay overlay) {
	    overlays.remove(overlay);
	    this.redraw();
	}
	
	public void overlayHasNewTopGlyph(MapViewOverlay overlay) {
	    int index = overlays.indexOf(overlay);
	    if(index == -1 || index + 1 >= overlays.size()) return;
	    Iterator i = overlays.listIterator(index + 1);
	    MapViewOverlay last = overlay;
	    while(i.hasNext()) {
	        MapViewOverlay o = (MapViewOverlay)i.next();
	        Glyph lastTop = last.getTopGlyph();
	        last = o;
	        if(lastTop == null) continue;
	        o.moveAbove(lastTop);
	    }
	}
	
	/** 
	 * Gets an menu containing all of the overlays loaded for the mapview.
	 * The menu items toggle the visibility of the overlays.
	 */
	public Menu getOverlaysMenu(Widget parent) {
	    Menu menu = null;
	    if(parent instanceof Control) {
	        menu = new Menu((Control)parent);
	    } else if(parent instanceof Decorations) {
	        menu = new Menu((Decorations)parent, SWT.CASCADE);
	    } else if(parent instanceof Menu) {
	        menu = new Menu((Menu)parent);
	    } else if(parent instanceof MenuItem) {
	        menu = new Menu((MenuItem)parent);
	    }
	    MenuItem item = null;
	    Iterator i = overlays.iterator();
	    while(i.hasNext()) {
	        final MapViewOverlay overlay = (MapViewOverlay)i.next();
	        item = new MenuItem(menu, SWT.CHECK);
	        item.setText("Show " + overlay.getName());
	        item.addSelectionListener(new SelectionListener() {
	            public void widgetSelected(SelectionEvent e) {
	                overlay.setVisible(!overlay.isVisible());
	            }
	            public void widgetDefaultSelected(SelectionEvent e) {
	                widgetSelected(e);
	            }
	        });
	        item.setSelection(overlay.isVisible());
	    }
	    return menu;
	}
	
	public void zoomIn() {
//ALM	    if(tigerMode) {
//	        zoomForRectangle(new Rectangle(getBounds().x + getBounds().width / 2,
//	                getBounds().y + getBounds().height / 2,
//	                getBounds().width / 2,
//	                getBounds().height / 2));
//	    } else {
			int i;
			for(i=0;i<zoomValues.length;i++) {
				if(zoomValues[i] > zoom) {				
					setZoom(zoomValues[i]);
					return;
				}
			}
			//if(zoomIndex < zoomValues.length - 1)
			//	setZoom(zoomValues[++zoomIndex]);
//	    }
	}
	public void zoomOut() {
//ALM		if(tigerMode) {
//		    if(mapHistory.size() > 0) {
//		        this.setMapData((TigerMapBacking)mapHistory.removeFirst());
//		    }
//		} else {
			int i;
			for(i=zoomValues.length-1;i>=0;i--) {
				if(zoomValues[i] < zoom) {
					setZoom(zoomValues[i]);
					return;
				}
			}
//		}
		//if(zoomIndex > 0)
		//	setZoom(zoomValues[--zoomIndex]);
	}
	public double getZoom() {
	    return zoom;
	}
	public void setZoom(double zoom) {
	    if(this.mapData == null) return;
//ALM		if(!tigerMode) {
		    this.zoom = zoom;
			holder.freeze();
			holder.getChild().setZoom(zoom);
			this.resizeHolder();
			Iterator i = overlays.iterator();
			while(i.hasNext()) {
			    MapViewOverlay overlay = (MapViewOverlay)i.next();
			    overlay.mapZoomed(zoom);
			}
			holder.thaw();
			System.gc();
//		} else {
//		    if(zoom > 1.0) zoomIn();
//		    else if(zoom < 1.0) zoomOut();
//		}
	}
	
	public void handleKeyPress(KeyEvent e) {
		if (e.keyCode==SWT.ARROW_RIGHT) {
			shift(SHIFT, 0);
		} else if (e.keyCode==SWT.ARROW_LEFT) {
			shift(-SHIFT, 0);
		} else if (e.keyCode==SWT.ARROW_UP) {
			shift(0, -SHIFT);
		} else if (e.keyCode==SWT.ARROW_DOWN) {
			shift(0, SHIFT);
		} else if(e.character == 'z') {
			zoomIn();
		} else if(e.character == 'x') {
			zoomOut();
		}
	}
	
	protected void shift(int x, int y) {
//ALM	    if(tigerMode) {
//	        // shifting in tigermode is a little trickier since the idea is to convert
//	        // number of pixels given to lat and lon, then get a new TigerLineData from
//	        // the one this one zoomed out of containing the amount shifted
//	        if(mapHistory.size() == 0) {
//	            // then there is no parent map and so the entire map displayable is onscreen
//	            return;
//	        }
//	        double shiftLat = pixelsToLatitude(getBounds().height - y);
//	        double shiftLon = pixelsToLongitude(x);
//	        // these will hold the new coordinates
//	        double oLat, oLon, maxLat, maxLon;
//	        if(shiftLat < getOriginLat()) {
//	            oLat = shiftLat;
//	            maxLat = mapData.getMaxLat() - shiftLat;
//	        } else {
//	            oLat = getOriginLat() + shiftLat;
//	            maxLat = mapData.getMaxLat() + shiftLat;
//	        }
//	        if(shiftLon < getOriginLon()) {
//	            oLon = shiftLon;
//	            maxLon = mapData.getMaxLon() - shiftLon;
//	        } else {
//	            oLon = getOriginLon() + shiftLon;
//	            maxLon = mapData.getMaxLon() + shiftLon;
//	        }
//	    } else {
		    Point origin = getOrigin();
			Rectangle bounds = holder.getBounds();
			Rectangle clientArea = getClientArea();
	
			Point newOrigin = new Point(origin.x, origin.y);
			newOrigin.x += x;
			newOrigin.y += y;
	
			if (newOrigin.x    >= bounds.width -clientArea.width) 
				newOrigin.x = bounds.width -clientArea.width;
			if (newOrigin.y    >= bounds.height-clientArea.height) 
				newOrigin.y = bounds.height-clientArea.height;
	
			if (newOrigin.x < 0) newOrigin.x = 0;
			if (newOrigin.y < 0) newOrigin.y = 0;
	
			if (newOrigin.x == origin.x && newOrigin.y == origin.y) return;

			setOrigin(newOrigin);
//	    }
	}
	
	public void setOrigin(Point origin) {
		if ((getStyle() & SWT.H_SCROLL) != 0) 
			super.setOrigin(origin);
		else holder.setLocation(-origin.x, -origin.y);
	}
	
	public void onDispose() {
		/*Enumeration i = allGlyphs.elements();
		while(i.hasMoreElements()) {
			Glyph g = (Glyph)i.nextElement();
			g.dispose();
		}
		allGlyphs.clear();

		if (mapImageGlyph != null) mapImageGlyph.dispose();
		if (mapImage   != null) mapImage  .dispose();
		if (iconCursor != null) iconCursor.dispose();
		if (iconTextBg != null) iconTextBg.dispose();*/
		//holder.getChild().dispose();
		mapImageGlyph = null;
		mapImage = null;
		iconCursor = null;
		iconTextBg = null;
		mapData = null;
	}
	protected void clearPlaceIcons() {
		for(int i = 0; i < placeIcons.size(); i++) {
			((MapIcon)placeIcons.get(i)).dispose();
		}
		placeIcons.clear();
	}
	
	public GlyphHolder getHolder() { return holder; }
	
	public Point getMapSize() {
	    return holder.getSize();
	}
	
	public void setMapData(MapBacking d) {
	    if (mapImage != null) mapImage.dispose();
	    if (mapImageGlyph != null) mapImageGlyph.dispose();
		zoom=1.0; //zoomIndex = 3;
        if(mapHistory.size() > 0) {
            if(mapData != null && !mapData.getName().equals(d.getName())) {
                // if this is a totally new map, and not just
                // a variation on an existing tigermap, clear out
                // the tiger zoom history
                mapHistory.clear();
            }
        }
		mapData = d;
	    if(d instanceof BitmapMapBacking) {
//ALM	        if(tigerOverlay != null) tigerOverlay.dispose();
//	        tigerMode = false;
//			tigerOverlay = null;
	        mapImageGlyph = new GlyphImage(holder, SWT.NONE);
			mapImage = new Image(getDisplay(), ((BitmapMapBacking)mapData).getImageResource());
			mapImageGlyph.setImage(mapImage, 0, 0);
//ALM	    } else {
//	        tigerMode = true;
//	        if(tigerOverlay != null) tigerOverlay.dispose();
//	        try {
//                tigerOverlay = new TigerOverlay(this, ((TigerMapBacking)d).getTigerLineData());
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (InvalidRecordException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            mapImage = null;
//            mapImageGlyph = null;
	    }
		this.resizeHolder();
		setPlaceSets(places);
		// notify overlays
		Iterator i = overlays.iterator();
		while(i.hasNext()) {
		    MapViewOverlay overlay = (MapViewOverlay)i.next();
		    overlay.mapChanged(d);
		}
		//holder.redraw();
		this.getParent().redraw();
		System.gc();
	}
	
	public boolean isInTigerMode() {
	    return false; //ALM tigerMode;
	}
	
	private void resizeHolder() {
//ALM	    if(tigerMode) {
//	        // don't want to get a squished looking map, so allow scrollbars
//	        // when the view is disproportionately thin or fat
//	        int width = getBounds().width;
//	        int height = getBounds().height;
//	        if(width > (height * 2)) height = (int)((double)width * 0.75);
//	        else if(height > (width * 2)) width = (int)((double)height * 0.75);
//	        holder.setSize(width, height);
//	    } else {
			Rectangle r = mapImage.getBounds();
			holder.setSize((int)(r.width * zoom + 0.5), //zoomValues[zoomIndex] + 0.5), 
				       (int)(r.height * zoom + 0.5)); //zoomValues[zoomIndex] + 0.5));
	    }
//ALM	}
	
	/**
	 * The placeSets are a Hashtable where place set names
	 * key to Hashtables where place names key to PlaceBackings
	 */
	public void setPlaceSets(Hashtable placeSets) {
		// to draw the map, the set that a place belongs
		// to is irrelevant.  Therefore the structure is flattened
		//HashSet temp = new HashSet();
		if(mapData == null) return;
		clearPlaceIcons();
		places = placeSets;
		Enumeration eSets = placeSets.keys();
		while(eSets.hasMoreElements()) {
			String placeSetName = (String)eSets.nextElement();
			Hashtable placeSet = (Hashtable)placeSets.get(placeSetName);
			Enumeration ePlaces = placeSet.elements();
			while(ePlaces.hasMoreElements()) {
				// we don't want anything to do with stuff
				// that we couldn't possibly display on this map
				// it will just slow us down.
				PlaceBacking place = (PlaceBacking)ePlaces.nextElement();
				if(placeOnMap(place)) {
					MapIcon placeIcon = new MapIcon(this, place, iconTextBg, 
							zoom); //zoomValues[zoomIndex]);
					placeIcons.add(placeIcon);
				}
			}
		}
	}
	
	protected boolean placeOnMap(PlaceBacking place) {
		return mapData.containsCoordinate(new TwoDCoordinate(place.lat, place.lon));
	}
	
    
	public MapBacking getMapData() {
		return mapData;
	}

	/**
	 * Gets the origin latitude of the currently onscreen map.
	 * Note the origin is defined to be the lower left corner
	 */
	public double getOriginLat() {
	    return mapData.getOriginLat();
	}
	
	/**
	 * Gets the origin longitude of the currently onscreen map.
	 * Note the origin is defined to be the lower left corner
	 */
	public double getOriginLon() {
	    return mapData.getOriginLon();
	}
	
	public double getPixelsPerLat() {
	    int height = holder.getSize().y;
	    double pixelsPerLat = (double)height / (mapData.getMaxLat() - mapData.getOriginLat());
	    return pixelsPerLat;
	}
	
	public double getPixelsPerLon() {
	    int width = holder.getSize().x;
	    double pixelsPerLon = (double)width / (mapData.getMaxLon() - mapData.getOriginLon());
	    return pixelsPerLon;
	}
	
	public double pixelsToLatitude(int pixelsY) {
	    return getOriginLat() + ((holder.getBounds().height - (double)pixelsY) / getPixelsPerLat());
	}
	
	public double pixelsToLongitude(int pixelsX) {
	    return getOriginLon() + ((double)pixelsX / getPixelsPerLon());
	}
	
	public int latitudeToPixels(double lat) {
	    if(Double.isNaN(lat)) {
	        throw new IllegalArgumentException("You passed NaN to me.  SWT allows pixels to be drawn at NaN, but it silently breaks all kinds of stuff, so we don't allow it");
	    }
		if(mapData == null) return -1;
		if(mapData instanceof BitmapMapBacking) {
		    return ((BitmapMapBacking)mapData).latitudeToPixels(lat);
		} else {
		    double tmp = holder.getBounds().height - ((lat - mapData.getOriginLat()) * getPixelsPerLat());
		    return (int)tmp;
		}   
	}
	
	public int longitudeToPixels(double lon) {
	    if(Double.isNaN(lon)) {
	        throw new IllegalArgumentException("You passed NaN to me.  SWT allows pixels to be drawn at NaN, but it silently breaks all kinds of stuff, so we don't allow it");
	    }
		if(mapData == null) return -1;
		if(mapData instanceof BitmapMapBacking) {
		    return ((BitmapMapBacking)mapData).longitudeToPixels(lon);
		} else {
		    double tmp = (lon - mapData.getOriginLon()) * getPixelsPerLon();
		    return (int)tmp;
		}  
	}
	
	public Point getPoint(TwoDCoordinate coord) {
		if (!containsCoordinate(coord))
			return null;
		
		return new Point(longitudeToPixels(coord.getLongitude()), 
				latitudeToPixels(coord.getLatitude()));
	}
	
	public boolean containsCoordinate(TwoDCoordinate coord) {
	    return mapData.containsCoordinate(coord);
	}
	
	
    public void doscroll(int x, int y) {
    	// jws - this has a bug in it somewhere - doesnt work at zoom != 1.0
    	if (isDisposed()) return;
    	Rectangle clientArea = getClientArea();
    	Rectangle bounds = holder.getBounds();
    	
    	/* adjust the coordinate system so that the bounds
    	 * start at (0,0) */
    	Rectangle center = new Rectangle(clientArea.x, clientArea.y,
    					 clientArea.width, 
    					 clientArea.height);
    	center.x = -bounds.x;
    	center.y = -bounds.y;
    	bounds.x = bounds.y = 0;
    	
    	/* give a little margin around the sides */
    	center.x += SHIFT;
    	center.y += SHIFT;
    	center.width  -= 2 * SHIFT;
    	center.height -= 2 * SHIFT;
    	
    	if (center.contains(x, y)) return;
    
    	Point origin = new Point(0, 0);
    	if (x < center.x) {
    		origin.x = x - SHIFT;
    		if (origin.x < 0) origin.x = 0;
    	} else if (x >= center.x + center.width) {
    		origin.x = x + SHIFT - clientArea.width;
    		if (origin.x    >= bounds.width- clientArea.width) 
    			origin.x = bounds.width- clientArea.width;
    	} else origin.x = clientArea.x;
    
    
    	if (y < center.y) {
    		origin.y = y - SHIFT;
    		if (origin.y < 0) origin.y = 0;
    	} else if (y >= center.y + center.height) {
    		origin.y = y + SHIFT - clientArea.height;
    		if (origin.x    >= bounds.width- clientArea.height) 
    			origin.x = bounds.width- clientArea.height;
    	} else origin.y = clientArea.y;
    
    	if (origin.x != clientArea.x || origin.y != clientArea.y)
    		setOrigin(origin);
    }
    
	static final int MARGIN=SHIFT;
	static final double MAXZOOM=5.0;
	static final double BEACON_SCALE_FACTOR = 10.0;
	static final double HYSTERESIS = 1.2;
	static final boolean usePresetZooms = true;
	
	public void dozoom() {
		// calculate correct centering and zoom
		// aim 1: keep all reticles on screen (yes)
		// aim 2: keep seen beacons on screen (yes)
		// aim 3: allow for motion (not yet)

		if (isDisposed()) return;
		
		Rectangle newArea = null;
		Rectangle gbounds;
		int r;
		
		Iterator i = overlays.iterator();
		while(i.hasNext()) {
		    MapViewOverlay o = (MapViewOverlay)i.next();
		    Rectangle area = o.getSuggestedArea();
		    if(newArea == null) newArea = area;
		    if(area == null) continue;
		    else newArea.union(area);
		}
		
		// if nobody has any suggestions then the current view is as good as anywhere
		if(newArea == null) return;
		
		// expand newArea a bit (should temper this with screen size constraints)
		newArea.width += 2*MARGIN;
		newArea.height += 2*MARGIN;
		newArea.x -= MARGIN;
		newArea.y -= MARGIN;


		// area taken on screen (starts 0,0)
		Rectangle clientArea = getClientArea();		 
		// area in window (inc scrolled out bits) 
		Rectangle imagebounds = holder.getBounds();
		//System.out.println("clientArea " + clientArea + " bounds " + bounds);
		
		double newzoom = ((double) clientArea.width) / ((double) newArea.width) / zoom;
		//if(newzoom < 1.0 / MAXZOOM) newzoom = 1.0 / MAXZOOM;
		//if(newzoom > MAXZOOM) newzoom = MAXZOOM;
		double oldzoom = zoom;
		
		
		//fSystem.out.println("newArea is " + newArea + ", current zoom is " + zoom + ", current area is " + clientArea.width + " new zoom is " + newzoom);		
		if(usePresetZooms) {
			if(newzoom > zoom * HYSTERESIS) {
				zoomIn();
				newzoom = zoom;
			}  else if(newzoom < zoom / HYSTERESIS) {
				zoomOut(); 
				newzoom = zoom;
			} else {
				newzoom = zoom;
			}
		} else {
			setZoom(newzoom);
		}

		// new area covered by window
		imagebounds = holder.getBounds();
		
		newArea.x = (int)(((double)newArea.x)*newzoom / oldzoom);
		newArea.y = (int)(((double)newArea.y)*newzoom / oldzoom);
		newArea.width = (int)(((double)newArea.width)*newzoom / oldzoom);
		newArea.height = (int)(((double)newArea.height)*newzoom / oldzoom);

		// determine new origin
		Point origin = new Point(newArea.x + (newArea.width-clientArea.width)/ 2, newArea.y + (newArea.height-clientArea.height)/2);
		if(origin.x < 0) origin.x = 0;
		if(origin.y < 0) origin.y = 0;
		if(origin.x + clientArea.width > imagebounds.width) origin.x =  imagebounds.width - clientArea.width;
		if(origin.y + clientArea.height > imagebounds.height) origin.y = imagebounds.width - clientArea.height;
		//if (origin.x != imagebounds.x || origin.y != imagebounds.y)  
		setOrigin(origin);
	}
    
	public void setSize(int width, int height) {
//ALM	    if(tigerMode) {
//	        holder.setSize(width, height);
//		    this.setMapData(mapData);
//	    }
	    super.setSize(width, height);
	}
	
	public void setSize(Point size) {
	    this.setSize(size.x, size.y);
	}
	
}

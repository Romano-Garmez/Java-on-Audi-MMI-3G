package org.placelab.demo.mapview;

import org.placelab.core.TwoDCoordinate;

public interface MapBacking {
    
    public double getOriginLat();
    public double getOriginLon();
    
    public String getName();
    
    public double getLatHeight();
    public double getLonWidth();
    public double getMaxLat();
    public double getMaxLon();
    
    public boolean containsCoordinate(TwoDCoordinate coord);
    
}

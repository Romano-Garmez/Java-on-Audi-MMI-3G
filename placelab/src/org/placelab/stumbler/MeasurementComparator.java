package org.placelab.stumbler;

import java.util.Comparator;

import org.placelab.core.Measurement;


public class MeasurementComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        long t1 = ((Measurement)o1).getTimestamp();
        long t2 = ((Measurement)o2).getTimestamp();
        if(t1 < t2) return -1;
        else if(t1 > t2) return 1;
        else return 0;
    }
    
}
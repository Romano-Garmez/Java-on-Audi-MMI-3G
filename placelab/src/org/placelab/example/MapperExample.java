package org.placelab.example;

import org.placelab.core.BeaconMeasurement;
import org.placelab.core.BeaconReading;
import org.placelab.mapper.Beacon;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.spotter.LogSpotter;
import org.placelab.spotter.Spotter;
import org.placelab.spotter.SpotterException;
import org.placelab.spotter.WiFiSpotter;

/**
 * This sample is very similar to CoordinateSample with the addition
 * of a lookup in the persistent AP cache
 */
public class MapperExample {

	public static void main(String[] args) {
	    Spotter s;
	    if(args.length >= 1) {
	        s = LogSpotter.newSpotter(args[0]);
	    } else {
	        s = new WiFiSpotter();
	    }
		try {
			s.open();
			BeaconMeasurement m = (BeaconMeasurement) s.getMeasurement();
			Mapper mapper;
			// This Mapper can tell us where APs are
			// The default Mapper (set in PlacelabProperties) will be selected
			// here.  The first argument says to exit on error, and the second
			// says to cache Beacons in memory as they are accessed.
			mapper = CompoundMapper.createDefaultMapper(true, true);
			
			int knownAPs = 0;
			for (int i = 0; i < m.numberOfReadings(); i++) {
				BeaconReading r = (BeaconReading) m.getReading(i);
				// Lets lookup this AP in the map
				Beacon b = (Beacon) mapper.findBeacon(r.getId());
				if (b == null) {
					System.out.println(r.getId() + " is an unknown AP");
				} else {
					System.out.println(r.getId() + " (" + r.getHumanReadableName()
							+ ") is thought to be at " + b.getPosition());
					knownAPs++;
				}
			}
			System.out.println("\nOf the " + m.numberOfReadings() + " APs "
					+ knownAPs + " were known.");
		} catch (SpotterException ex) {
			ex.printStackTrace();
		}
	}
}

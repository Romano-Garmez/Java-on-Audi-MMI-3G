package org.placelab.stumbler;

import org.placelab.spotter.NMEAGPSSpotterTests;
import org.placelab.spotter.NMEASentenceTests;
import org.placelab.test.Harness;

public class StumblerTests extends Harness {

    public void setupAllTests(String[] argv) {
        addTest(new NMEASentenceTests());
        addTest(new NMEAGPSSpotterTests());
        addTest(new StumblerFunnelTests());
    }

}

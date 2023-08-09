package org.placelab.test;


public class EventSystemTests extends Harness {

    public void setupAllTests(String[] argv) {
        addTest(new SimpleEventSystemTest());
        addTest(new SWTEventSystemTest());
    }

}

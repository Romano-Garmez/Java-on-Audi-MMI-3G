package org.placelab.test;


public class GlyphTests extends Harness {

    public void setupAllTests(String[] argv) {
        addTest(new AffineTransformTest());
        //addTest(new JavaAffineTransformTest());
    }

}

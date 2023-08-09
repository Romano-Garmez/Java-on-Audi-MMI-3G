package org.placelab.test;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.placelab.util.swt.AffineTransform;

public class AffineTransformTest implements Testable {

    // note: i made identical versions of these tests that used
    // the awt AffineTransform and the awt geometry stuff and passed
    // the tests with that so my expected results are verified
    // - fats
    
    protected static Rectangle unit = new Rectangle(0, 0, 1, 1);
    
    public String getName() {
        return "AffineTransformTest";
    }
    
    public void runTests(TestResult result) throws Throwable {
        scale(result);
        translate(result);
        rotate(result);
        flip(result);
        inverse(result);
        //rotateAndScale(result);
    }
    
    public void rotateAndScale(TestResult result) {
        AffineTransform t = AffineTransform.getRotateInstance(Math.PI / 4.0, 20, 20);
        verifyBounds(result, t, new Rectangle(0, 0, 20, 20), new Rectangle(20, 20, 40, 40), "rotate and scale");
    }
    
    public void rotate(TestResult result) {
        AffineTransform t = AffineTransform.getRotateInstance(Math.PI / 2.0);
        verifyBounds(result, t, new Rectangle(0, 0, 2, 1), new Rectangle(-1, 0, 1, 2), "rotate");
    }
    
    public void translate(TestResult result) {
        AffineTransform t = AffineTransform.getTranslateInstance(-5, -5);
        verifyBounds(result, t, unit, new Rectangle(-5, -5, 1, 1), "translate");
    }
    
    public void inverse(TestResult result) throws Throwable {
        AffineTransform t = AffineTransform.getRotateInstance(Math.PI / 4.0, -100.0, -50.0);
        Point original = new Point(5, 7);
        Point transform = t.transform(original);
        AffineTransform inverse = t.createInverse();
        Point inversed = inverse.transform(transform);
        result.assertTrue(this, 5, inversed.x, "inverse x check");
        result.assertTrue(this, 7, inversed.y, "inverse y check");
    }
    
    public void scale(TestResult result) {
        AffineTransform t = AffineTransform.getScaleInstance(3, 4);
        verifyBounds(result, t, unit, new Rectangle(0, 0, 3, 4), "scale");
    }
    
    public void flip(TestResult result) {
        // flip along y axis and then push right to make positive coords
        AffineTransform t = AffineTransform.getScaleInstance(-1, 1);
        // translate by -1 rather than 1 because the x coordinates are now
        // flipped.
        t.concatenate(AffineTransform.getTranslateInstance(-1, 0));
        verifyBounds(result, t, unit, unit, "flip");
    }
    
    public void verifyBounds(TestResult result, AffineTransform transform, Rectangle original, Rectangle expected, String msg) {
        Rectangle actual = transform.getBoundingRect(original);
        result.assertTrue(this, expected.x, actual.x, "bounding rectangle x check for " + msg);
        result.assertTrue(this, expected.y, actual.y, "bounding rectangle y check for " + msg);
        result.assertTrue(this, expected.width, actual.width, "bounding rectangle width check for " + msg);
        result.assertTrue(this, expected.height, actual.height, "bounding rectangle height check for " + msg);
    }

}

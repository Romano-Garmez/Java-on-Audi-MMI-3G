package org.placelab.demo.mapview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.placelab.core.TwoDCoordinate;
import org.placelab.test.TestResult;
import org.placelab.test.Testable;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphImage;

public class MapViewTests implements Testable {

	private String testWad = "goodwad6.mapwad";
	private WadData wad;
	
	private Display display;
	private Shell shell;
	private MapView mapView;
	
	public String getName() {
		return "MapViewTests";
	}
	
	public void runTests(TestResult result) throws Throwable {
		String testDataDir = DemoTestsUtil.getTestDataSubDir(this,
				result, "WadDataTests");
		if(testDataDir == null) return;
		testWad = testDataDir + testWad;
		wad = new WadData(testWad);
		openView();
		checkBasics(result);
		originTests(result);
		zoomTests(result);
		nanTest(result);
		closeView();
	}
	
	private void nanTest(TestResult result) {
	    try {
	        mapView.latitudeToPixels(TwoDCoordinate.NULL.getLatitude());
	        mapView.longitudeToPixels(TwoDCoordinate.NULL.getLongitude());
	    } catch (IllegalArgumentException iae) {
	        // mapView must barf on the above or else things go really haywire
	        result.assertTrue(this, true, true, "mapview wins");
	        return;
	    }
	    result.fail(this, "mapview didn't throw IllegalArgumentException when NULL coordinate" +
	    		"latitude/longitudeToPixels method");
	}
	
	private void openView() {
		display = new Display();
		shell = new Shell(display);
		shell.setSize(200, 200);
		shell.setLayout(new GridLayout());
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		mapView = new MapView(shell, SWT.NONE, true);
		mapView.setLayoutData(gridData);
		mapView.setMapData(wad.getDefaultMap());
		mapView.setPlaceSets(wad.allPlaceSets());
		shell.open();
	}
	private void closeView() {
		shell.dispose();
	}
	
	private void zoomTests(TestResult result) {
		// this zooms to 2x and back down to 1/2x
		// and checks to make sure that a) the image is the correct
		// new dimensions and b) the point translation is correct
		mapView.setZoom(2.0);
		Rectangle b = getMapImage().getBounds();
		result.assertTrue(this, 1460, b.width, "2x zoom width check");
		result.assertTrue(this, 1996, b.height, "2x zoom height check");
		checkColor(result, 50, 50, 0, 0, 255, "bluesquare 2x zoom");
		checkColor(result, 1360, 50, 255, 0, 0, "redquare 2x zoom");
		checkColor(result, 50, 1896, 0, 255, 0, "greensquare 2x zoom");
		checkColor(result, 1360, 1896, 0, 0, 0, "blacksquare 2x zoom");
		
		mapView.setZoom(0.5);
		b = getMapImage().getBounds();
		result.assertTrue(this, 365, b.width, "2x zoom width check");
		result.assertTrue(this, 499, b.height, "2x zoom height check");
		checkColor(result, 13, 13, 0, 0, 255, "bluesquare 2x zoom");
		checkColor(result, 340, 13, 255, 0, 0, "redquare 2x zoom");
		checkColor(result, 13, 474, 0, 255, 0, "greensquare 2x zoom");
		checkColor(result, 340, 474, 0, 0, 0, "blacksquare 2x zoom");
		mapView.setZoom(1.0);
	}
	
	private GlyphHolder getHolder() {
		return (GlyphHolder)(mapView.getChildren())[0];
	}
	private GlyphImage getMapImage() {
		return (GlyphImage)getHolder().getChild().getChildren()[0];
	}
	private void checkColor(TestResult result,
			int x, int y, int red, int green, int blue,
			String checkName) {
		GlyphHolder holder = getHolder();
		GlyphImage at = (GlyphImage)holder.getChild().pickGlyphAt(x, y, false);
		int px = at.getImage().getImageData().getPixel(0,0);
		RGB aPx = at.getImage().getImageData().palette.getRGB(px);
		result.assertTrue(this, red, aPx.red,
				checkName + " green component check");
		result.assertTrue(this, green, aPx.green,
				checkName + " green component check");
		result.assertTrue(this, blue, aPx.blue,
				checkName + " green component check");
		
	}
	
	private void originTests(TestResult result) {
		// the origin of the map data is the lower left
		// the origin in swt is the upper left.  its easy
		// to get this confused.  i've hand calculated
		// the positioning of the squares on the map so
		// if the MapView is translating locations to points
		// correctly, it will draw the squares in the correct
		// places.  If not, we've got issues.
		checkColor(result, 25, 25, 0, 0, 255, "bluesquare");
		checkColor(result, 680, 25, 255, 0, 0, "redquare");
		checkColor(result, 25, 948, 0, 255, 0, "greensquare");
		checkColor(result, 680, 948, 0, 0, 0, "blacksquare");
	}
	
	private void checkBasics(TestResult result) {
		// the strategy here is just to load up the map
		// view with all the info from the wad, just to see
		// how it does with that
		GlyphHolder holder = (GlyphHolder)(mapView.getChildren())[0];
		// despite that there are 6 places + 1 map = 7 places
		// only 4 places (radplaces) are on screen.  Don't draw stuff
		// that doesn't appear on this map.
		result.assertTrue(this, 5, holder.getChild().getChildren().length,
				"checkBasics number of children test");
	}
	
	
}

/**
 * For these tests to succeed, you'll need to have my test map wads
 * in testdata/WadDataTests/.  You can set where I look for the testdata
 * directory by setting the test_data_dir key in your placelab.ini
 */
package org.placelab.demo.mapview;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.swt.graphics.ImageData;
import org.placelab.mapper.JDBMMapper;
import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class WadDataTests implements Testable {

	// bad wads
	private String missingMapImage = "badwad1.mapwad";
	private String missingPlaceImage = "badwad2.mapwad";
	private String malformedMapIndex = "badwad3.mapwad";
	private String wrongSectionMapIndex = "badwad4.mapwad";
	private String malformedPlaceIndex = "badwad5.mapwad";
	private String wrongSectionPlaceIndex = "badwad6.mapwad";
	private String missingMapMeta = "badwad7.mapwad";
	private String missingPlaceSet = "badwad8.mapwad";
	private String malformedMapMeta = "badwad9.mapwad";
	private String malformedPlaceSet = "badwad10.mapwad";
	private String malformedDefaults = "badwad11.mapwad";
	
	// good wads
	private String noAPCache = "goodwad1.mapwad";
	private String noMapIndex = "goodwad2.mapwad";
	private String noPlaceIndex = "goodwad3.mapwad";
	private String apsMapsAndPlaces = "goodwad4.mapwad";
	private String withDefaults = "goodwad5.mapwad";
	private String fromDirectory = "wadbase";
	private String twoOrigins = "goodwad7.mapwad";
	
	private String testDataDir;

	public String getName() {
		return "WadDataTests";
	}

	public void runTests(TestResult result) throws Throwable {
		testDataDir = DemoTestsUtil.getTestDataSubDir(this, result, "WadDataTests");
		if(testDataDir == null) return;
		missingMapImage = testDataDir + missingMapImage;
		missingPlaceImage = testDataDir + missingPlaceImage;
		malformedMapIndex = testDataDir + malformedMapIndex;
		wrongSectionMapIndex = testDataDir + wrongSectionMapIndex;
		malformedPlaceIndex = testDataDir + malformedPlaceIndex;
		wrongSectionPlaceIndex = testDataDir + wrongSectionPlaceIndex;
		missingMapMeta = testDataDir + missingMapMeta;
		missingPlaceSet = testDataDir + missingPlaceSet;
		malformedMapMeta = testDataDir + malformedMapMeta;
		malformedPlaceSet = testDataDir + malformedPlaceSet;
		malformedDefaults = testDataDir + malformedDefaults;
		
		noAPCache = testDataDir + noAPCache;
		noMapIndex = testDataDir + noMapIndex;
		noPlaceIndex = testDataDir + noPlaceIndex;
		apsMapsAndPlaces = testDataDir + apsMapsAndPlaces;
		withDefaults = testDataDir + withDefaults;
		fromDirectory = testDataDir + fromDirectory;
		twoOrigins = testDataDir + twoOrigins;
		
		failTests(result);
		succeedTests(result);
	}
	
	// tests things that should fail
	private void failTests(TestResult result) {
		missingMapImage(result);
		missingPlaceImage(result);
		malformedMapIndex(result);
		wrongSectionMapIndex(result);
		malformedPlaceIndex(result);
		wrongSectionPlaceIndex(result);
		missingMapMeta(result);
		missingPlaceSet(result);
		malformedMapMeta(result);
		malformedPlaceSet(result);
		malformedDefaults(result);
	}
	private void missingMapImage(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(missingMapImage);
		} catch (WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.MISSING_RESOURCE_ERROR,
					wdfe.failCode(), "missing map image error code check");
			result.assertTrue(this, "images/greatmap.gif", wdfe.badEntryLocation(),
				"missing map image entry location check");
			return;
		} catch (Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in missingMapImage: " + e.toString());
			return;
		}
		result.fail(this, "a missing map image didn't throw an exception");
	}
	private void missingPlaceImage(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(missingPlaceImage);
		} catch (WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.MISSING_RESOURCE_ERROR,
					wdfe.failCode(), "missing place image error code check");
			result.assertTrue(this, "images/myhouse.gif", wdfe.badEntryLocation(),
				"missing place image entry location check");
			return;
		} catch (Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in missingPlaceImage: " + e.toString());
			return;
		}
		result.fail(this, "a missing place image didn't throw an exception");
	}
	private void malformedMapIndex(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(malformedMapIndex);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "malformed map index check");
			result.assertTrue(this, "maps/maps.index", wdfe.badEntryLocation(),
				"malformed map index entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in malformedMapIndex: " + e.toString());
			return;
		}
		result.fail(this, "a malformed map index didn't throw an exception");
	}
	private void wrongSectionMapIndex(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(wrongSectionMapIndex);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "wrong section map index check");
			result.assertTrue(this, "maps/maps.index", wdfe.badEntryLocation(),
				"wrong section map index entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in wrongSectionMapIndex: " + e.toString());
			return;
		}
		result.fail(this, "a wrong section map index didn't throw an exception");
	}
	private void malformedPlaceIndex(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(malformedPlaceIndex);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "malformed place index check");
			result.assertTrue(this, "places/places.index", wdfe.badEntryLocation(),
				"malformed place index entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in malformedPlaceIndex: " + e.toString());
			return;
		}
		result.fail(this, "a malformed place index didn't throw an exception");
	}
	private void wrongSectionPlaceIndex(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(wrongSectionPlaceIndex);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "wrong section place index error code check");
			result.assertTrue(this, "places/places.index", wdfe.badEntryLocation(),
					"wrong section place index entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in wrongSectionPlaceIndex: " + e.toString());
			return;
		}
		result.fail(this, "a wrong section place index didn't throw an exception");
	}
	private void missingMapMeta(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(missingMapMeta);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.MISSING_RESOURCE_ERROR,
					wdfe.failCode(), "missing map meta error code check");
			result.assertTrue(this, "maps/greatmap.meta", wdfe.badEntryLocation(),
					"missing map meta entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in missingMapMeta: " + e.toString());
			return;
		}
		result.fail(this, "a missing map meta didn't throw an exception");
	}
	private void missingPlaceSet(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(missingPlaceSet);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.MISSING_RESOURCE_ERROR,
					wdfe.failCode(), "missing place set error code check");
			result.assertTrue(this, "places/lame.txt", wdfe.badEntryLocation(),
					"missing place set entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in missingPlaceSetMeta: " + e.toString());
			return;
		}
		result.fail(this, "a missing place set meta didn't throw an exception");
	}
	private void malformedMapMeta(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(malformedMapMeta);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "malformed map meta error code check");
			result.assertTrue(this, "maps/greatmap.meta", wdfe.badEntryLocation(),
					"malformed map meta entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in malformedMapMeta: " + e.toString());
			return;
		}
		result.fail(this, "a malformed map meta didn't throw an exception");
	}
	private void malformedPlaceSet(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(malformedPlaceSet);
		} catch(WadDataFormatException wdfe) {
			result.assertTrue(this, WadDataFormatException.BAD_RESOURCE_ERROR,
					wdfe.failCode(), "malformed place set error code check");
			result.assertTrue(this, "places/rad.txt", wdfe.badEntryLocation(),
					"malformed place set entry location check");
			return;
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in malformedPlaceSetMeta: " + e.toString());
			return;
		}
		result.fail(this, "a malformed place set meta didn't throw an exception");
	}
	// this is now a succeed test.  the correct behaviour is now for WadData to give
	// back an empty JDBMMapper when there is no apcache rather than throwing an exception
	// in this way, with the new JDBMMapper additions to do hierarchical lookup of Beacons
	// we can always load the wad cache and if it has the Beacons, great, if not then don't worry
	// about it.
	private void loadApsWhenNoneExist(TestResult result) {
		WadData wad = null;
		File tempFile = null;
		JDBMMapper mapper = null;
		try {
			wad = new WadData(noAPCache);
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in loadApsWhenNoneExist 1: " + e.toString());
			return;
		}
		try {
			tempFile = File.createTempFile("mapper", "db");
			tempFile.deleteOnExit();
			mapper = wad.loadAPCacheIntoMapper(tempFile.getAbsolutePath());
		} catch(Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in loadApsWhenNoneExist 2: " + e.toString());
			return;
		}
		result.assertTrue(this, true, mapper == null, "load missing ap cache size check");
	}
	private void malformedDefaults(TestResult result) {
		WadData wad = null;
		try {
			wad = new WadData(malformedDefaults);
		} catch (WadDataFormatException wdfe) {
			result.assertTrue(this, 
				WadDataFormatException.BAD_RESOURCE_ERROR,
				wdfe.failCode(), "malformed defaults error code check");
			result.assertTrue(this, "defaults.txt", wdfe.badEntryLocation(),
				"malformed defaults entry location check");
		} catch (Exception e) {
			result.errorCaught(this, e);
			result.print("^^ in malformedDefaults: " + e.toString());
		}
	}
	
	// tests things that should succeed
	private void succeedTests(TestResult result) {
		// none of these guys should throw exceptions
		try {
			loadAPCacheAsPlaces(result);
			checkPlaces(result);
			checkMap(result);
			checkDefaults(result);
			checkDir(result);
			check2Origins(result);
			
			// This is no longer an error, the behaviour is now to give 
			// simply an empty JDBMMapper back in this case
			loadApsWhenNoneExist(result);
		} catch (Exception e) {
			result.errorCaught(this, e);
		}
	}
	private void loadAPCacheAsPlaces(TestResult result) 
		throws IOException, WadDataFormatException {
		WadData wad = new WadData(apsMapsAndPlaces);
		_loadAPCacheAsPlaces(result, wad);
	}
	private void _loadAPCacheAsPlaces(TestResult result, WadData wad)
		throws IOException, WadDataFormatException {
		Hashtable apPlaces = wad.loadAPCacheAsPlaces(
				new ImageData(testDataDir + "apicon.gif"));
		// the keys are the mac addresses
		PlaceBacking ht = (PlaceBacking)apPlaces.get("02:04:23:ab:57:80");
		result.assertTrue(this, true, ht != null, 
				"load ap cache as places place 1 existance check");
		result.assertTrueDouble(this, 47.658121282353, ht.lat,
				"load ap cache as places place 1 lat check");
		result.assertTrueDouble(this, -122.30429284705, ht.lon,
			"load ap cache as places place 1 lon check");
		result.assertTrue(this, "HealthTrends", ht.name,
			"load ap cache as places place 1 name check");
		PlaceBacking uw = (PlaceBacking)apPlaces.get("00:0d:29:1a:f5:c2");
		result.assertTrue(this, true, uw != null, 
			"load ap cache as places place 2 existance check");
		result.assertTrueDouble(this, 47.659275515384, uw.lat,
				"load ap cache as places place 2 lat check");
		result.assertTrueDouble(this, -122.3082921923, uw.lon,
			"load ap cache as places place 2 lon check");
		result.assertTrue(this, "University of Washington", uw.name,
			"load ap cache as places place 2 name check");
	}
	private void checkPlaces(TestResult result)
		throws IOException, WadDataFormatException
	{
		WadData wad = new WadData(apsMapsAndPlaces);
		_checkPlaces(result, wad);
	}
	private void _checkPlaces(TestResult result, WadData wad) {
		// without loss of generality, i'll just check the facts for my
		// house, since there's nothing special about it as a place
		// over any other as far as the wad is concerned
		PlaceBacking myHouse = wad.getPlace("radplaces", "James's House");
		result.assertTrue(this, true, myHouse != null,
				"check places house existance check");
		result.assertTrueDouble(this, 47.19829, myHouse.lat, 
				"check places house lat check");
		result.assertTrueDouble(this, -122.8132, myHouse.lon,
				"check places house lon check");
		result.assertTrue(this, "at least its still standing",
				myHouse.text, "check places house text check");
		result.assertTrue(this, 20 * 20,
				myHouse.getImageResource().width * 
				myHouse.getImageResource().height,
				"check places house image size check");
	}
	private void checkMap(TestResult result)
		throws IOException, WadDataFormatException
	{
		WadData wad = new WadData(noPlaceIndex);
		_checkMap(result, wad);
	}
	private void _checkMap(TestResult result, WadData wad) {
		BitmapMapBacking map = (BitmapMapBacking)wad.getMap("greatmap");
		result.assertTrue(this, true, map != null,
				"check map existance check");
		result.assertTrueDouble(this, 47.6551847973423, map.getOriginLat(),
				"check map lat check");
		result.assertTrueDouble(this, -122.318433676012, map.getOriginLon(),
				"check map lon check");
		result.assertTrueDouble(this, 100998.13932293755577592136948456,
				map.getPixelsPerLat(), "check map pixels per lat check");
		result.assertTrueDouble(this, 68250.274852131006688294884574341,
				map.getPixelsPerLon(), "check map pixels per lon check");
		result.assertTrue(this, 730 * 998,
				map.getImageResource().width *
				map.getImageResource().height,
				"check map image size check");
	}
	private void checkDefaults(TestResult result) 
		throws IOException, WadDataFormatException
	{
		WadData wad = new WadData(withDefaults);
		_checkDefaults(result, wad);
	}
	private void _checkDefaults(TestResult result, WadData wad) {
		BitmapMapBacking map = (BitmapMapBacking) wad.getDefaultMap();
		result.assertTrue(this, true, map != null,
			"checkDefaults default map existance check");
		// checkHashtablechecks whether or not the map backing was
		// loaded correctly, so there is no need to do that here
		Hashtable placeSets = wad.getDefaultPlaceSets();
		result.assertTrue(this, true, placeSets != null,
			"checkDefaults place set existance check");
		result.assertTrue(this, true, placeSets.containsKey("radplaces"),
			"checkDefaults place set place existance check");
	}
	private void checkDir(TestResult result)
		throws IOException, WadDataFormatException 
	{
		WadData wad = new WadData(fromDirectory);
		_loadAPCacheAsPlaces(result, wad);
		_checkPlaces(result, wad);
		_checkMap(result, wad);
		_checkDefaults(result, wad);
	}
	private void check2Origins(TestResult result) 
		throws IOException, WadDataFormatException 
	{
		WadData wad = new WadData(twoOrigins);
		_loadAPCacheAsPlaces(result, wad);
		_checkPlaces(result, wad);
		_checkMap(result, wad);
		_checkDefaults(result, wad);
	}
}

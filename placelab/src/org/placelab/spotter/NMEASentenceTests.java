package org.placelab.spotter;

import org.placelab.test.TestResult;
import org.placelab.test.Testable;

public class NMEASentenceTests implements Testable {

    private static String rmc = "$GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70";
    private static String rmcVersion3 = "$GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W,A*1D";
    private static String gga = "$GPGGA,021526,4739.7153,N,12218.9712,W,1,04,2.4,50.5,M,-18.4,M,,*4C";
    private static String badChecksum = "$GPGGA,170834,4124.8963,N,08151.6838,W,1,05,1.5,280.2,M,-34.0,M,,,*76";
    
    public String getName() {
        return "NMEASentenceTests";
    }

    public void runTests(TestResult result) throws Throwable {
        checkRMC(result);
        checkRMCVersion3(result);
        checkGGA(result);
        checkBadChecksum(result);
    }
    
    private void checkRMC(TestResult result) {
        NMEASentence rmcS = NMEASentence.expandSentence(rmc);
        if(rmcS == null) {
            result.fail(this, "NMEASentence.expandSentence() failed to parse what should be a valid sentence for rmc v1");
            return;
        }
        // test all the fields
        result.assertTrue(this, "220516", rmcS.getField(NMEASentence.TIMEOFFIX), "basic rmc timeoffix");
        result.assertTrue(this, "A", rmcS.getField(NMEASentence.STATUS), "basic rmc status");
        result.assertTrue(this, "5133.82", rmcS.getField(NMEASentence.LATITUDE), "basic rmc latitude");
        result.assertTrue(this, "N", rmcS.getField(NMEASentence.LATITUDEHEMISPHERE), "basic rmc latitude hemisphere");
        result.assertTrue(this, "00042.24", rmcS.getField(NMEASentence.LONGITUDE), "basic rmc longitude");
        result.assertTrue(this, "W", rmcS.getField(NMEASentence.LONGITUDEHEMISPHERE), "basic rmc longitude hemisphere");
        result.assertTrue(this, "173.8", rmcS.getField(NMEASentence.SPEEDOVERGROUND), "basic rmc speed in knots");
        result.assertTrue(this, "231.8", rmcS.getField(NMEASentence.COURSEOVERGROUND), "basic rmc true course");
        result.assertTrue(this, "130694", rmcS.getField(NMEASentence.DATEOFFIX), "basic rmc date stamp");
        result.assertTrue(this, "004.2", rmcS.getField(NMEASentence.MAGNETICVARIATION), "basic rmc variation");
        result.assertTrue(this, "W", rmcS.getField(NMEASentence.MAGNETICVARIATIONDIRECTION), "basic rmc variation direction");
    }
    
    private void checkRMCVersion3(TestResult result) {
        NMEASentence rmcS = NMEASentence.expandSentence(rmcVersion3);
        if(rmcS == null) {
            result.fail(this, "NMEASentence.expandSentence() failed to parse what should be a valid sentence for rmc v3");
            return;
        }
        // test all the fields
        result.assertTrue(this, "220516", rmcS.getField(NMEASentence.TIMEOFFIX), "extended rmc timeoffix");
        result.assertTrue(this, "A", rmcS.getField(NMEASentence.STATUS), "extended rmc status");
        result.assertTrue(this, "5133.82", rmcS.getField(NMEASentence.LATITUDE), "extended rmc latitude");
        result.assertTrue(this, "N", rmcS.getField(NMEASentence.LATITUDEHEMISPHERE), "extended rmc latitude hemisphere");
        result.assertTrue(this, "00042.24", rmcS.getField(NMEASentence.LONGITUDE), "extended rmc longitude");
        result.assertTrue(this, "W", rmcS.getField(NMEASentence.LONGITUDEHEMISPHERE), "extended rmc longitude hemisphere");
        result.assertTrue(this, "173.8", rmcS.getField(NMEASentence.SPEEDOVERGROUND), "extended rmc speed in knots");
        result.assertTrue(this, "231.8", rmcS.getField(NMEASentence.COURSEOVERGROUND), "extended rmc true course");
        result.assertTrue(this, "130694", rmcS.getField(NMEASentence.DATEOFFIX), "extended rmc date stamp");
        result.assertTrue(this, "004.2", rmcS.getField(NMEASentence.MAGNETICVARIATION), "extended rmc variation");
        result.assertTrue(this, "W", rmcS.getField(NMEASentence.MAGNETICVARIATIONDIRECTION), "extended rmc variation direction");
        result.assertTrue(this, "A", rmcS.getField(NMEASentence.MODE), "extended rmc mode");
    }
    
    private void checkGGA(TestResult result) {
        NMEASentence ggaS = NMEASentence.expandSentence(gga);
        if(ggaS == null) {
            result.fail(this, "NMEASentence.expandSentence() failed to parse what should be a valid sentence for gga");
            return;
        }
        result.assertTrue(this, "021526", ggaS.getField(NMEASentence.TIMEOFFIX), "gga timeoffix");
        result.assertTrue(this, "4739.7153", ggaS.getField(NMEASentence.LATITUDE), "gga latitude");
        result.assertTrue(this, "N", ggaS.getField(NMEASentence.LATITUDEHEMISPHERE), "gga latitude hemisphere");
        result.assertTrue(this, "12218.9712", ggaS.getField(NMEASentence.LONGITUDE), "gga longitude");
        result.assertTrue(this, "W", ggaS.getField(NMEASentence.LONGITUDEHEMISPHERE), "gga longitude hemisphere");
        result.assertTrue(this, "1", ggaS.getField(NMEASentence.GPSQUALITY), "gga fix quality");
        result.assertTrue(this, "04", ggaS.getField(NMEASentence.NUMOFSATELLITES), "gga number of satellites");
        result.assertTrue(this, "2.4", ggaS.getField(NMEASentence.HORIZONTALDILUTIONOFPRECISION), "gga hdop");
        result.assertTrue(this, "50.5", ggaS.getField(NMEASentence.ANTENNAHEIGHT), "gga altitude");
        result.assertTrue(this, "-18.4", ggaS.getField(NMEASentence.GEOIDALHEIGHT), "gga height of geoid above wgs84 ellipsoid");
    }
    
    private void checkBadChecksum(TestResult result) {
        NMEASentence badS = NMEASentence.expandSentence(badChecksum);
        result.isNull(this, badS, "checksum");
    }
}

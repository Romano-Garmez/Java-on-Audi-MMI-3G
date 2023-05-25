/*
 * Created on 25-Aug-2004
 *
 */
package org.placelab.test;

import org.placelab.util.FixedPointLong;
import org.placelab.util.FixedPointLongException;
import org.placelab.util.NumUtil;

/**
 * 
 *
 */
public class FixedPointLongTest implements Testable {

    public String getName() {
        return "FixedPointLongTest";
    }

    public void runTests(TestResult result) throws Throwable {
        final String teststrings[] = new String[]{"-23.4E-1","2.99999999","1214215.3284325893285093280932","-328.000002321","0","3.74166666666667E-4"};
        final double testpairs1[] = new double[]{52.23,12412.090909,0,32.000324,-3952.234};
        final double testpairs2[] = new double[]{-2392.2,23425.543895234324,-0.23453258283,95.47532E5,0.32};
        final boolean testpairs3[] = new boolean[]{true,false,true,false,true};
        int i;
        
        for(i=0;i<teststrings.length;i++) {
            result.assertTrue(this,false, dotest(teststrings[i]),
                    "FixedPointLong translation of "+teststrings[i]);
        }
        for(i=0;i<testpairs1.length;i++) {
            result.assertTrue(this,false,dotest_add(testpairs1[i],testpairs2[i],testpairs3[i]),
                    "FixedPointLong " + (testpairs3[i] ? "add " : "sub ") + testpairs1[i] + " " + testpairs2[i]);
        }
        for(i=0;i<testpairs1.length;i++) {
            result.assertTrue(this,false,dotest_mult(testpairs1[i],testpairs2[i],testpairs3[i]),
                    "FixedPointLong " + (testpairs3[i] ? "mult " : "div ") + testpairs1[i] + " " + testpairs2[i]);
        }
        
        for(i=0;i<100;i++) {
            long flong = FixedPointLong.intToFlong(i);
            long flongsquare = FixedPointLong.square(flong);
            long flongsqrt = FixedPointLong.sqrt(flong);
            //System.out.println("" + i + ": " + FixedPointLong.flongToString(flongsquare) + ", " + FixedPointLong.flongToString(flongsqrt));
    		result.assertTrueDouble(this,(double)i,
    		        Double.parseDouble(FixedPointLong.flongToString(FixedPointLong.sqrt(flongsquare))),
    		        TrackerTests.EPSILON, "square/sqrt of " + i);
        }
    }

    static boolean print_on_failure = true;

    /*
	public static String toBinaryString(long f) {
	    StringBuffer sb = new StringBuffer(Long.toBinaryString(f));
	    while(sb.length() < FixedPointLong.LENGTH) sb.insert(0,"0");
	    sb.insert(sb.length() - FixedPointLong.OFFSET, ".");
	    return sb.toString();
	}
	*/
	
	public static void test_and_stop_if_broken(String start) {
  	    if(dotest(start)) {
  	        print_on_failure=true; 
  	        System.out.println();
  	        dotest(start);
  	        System.exit(0);
  	    }
	    //System.out.println("TEST: \"" + d + "\" becomes \"" + s + "\", representation " + toBinaryString(l));
	    //Thread.yield();
	}

	private static void testAdd_and_stop_if_broken(double d, double e, boolean b) {
	    if(dotest_add(d,e,b)) {
	        print_on_failure=true;
	        System.out.println();
	        dotest_add(d,e,b);
	        System.exit(0);
	    }
	}

	private static boolean dotest_add(double d, double e, boolean b) {
	    try {
	        String origstring1="", origstring2="", endstring="";
	        long flong1=0, flong2=0, flongsum=0;
	        boolean err=false;
	        origstring1=NumUtil.doubleToString(d,10);
	        flong1 = FixedPointLong.stringToFlong(origstring1);
	        origstring2=NumUtil.doubleToString(e,10);
	        flong2 = FixedPointLong.stringToFlong(origstring2);
	        flongsum = b ? flong1 + flong2 : flong1 - flong2;
	        endstring = FixedPointLong.flongToString(flongsum);
	        err= Math.abs((b ? d+e : d-e) - Double.parseDouble(endstring)) > 1E-6;
		    if(err && print_on_failure) {
		        System.out.println("ERR " + (b?"add ":"sub ") + d + " " + e + " != " + endstring);
		    }
		    return err;
	    } catch(FixedPointLongException fple) {
	        throw new ArithmeticException("dotest_add: " + fple);
	    }
	}

	private static boolean dotest_mult(double d, double e, boolean b) {
	    try {
	        String origstring1="", origstring2="", endstring="";
	        long flong1=0, flong2=0, flongresult=0;
	        boolean err=false;
	        origstring1=NumUtil.doubleToString(d,10);
	        flong1 = FixedPointLong.stringToFlong(origstring1);
	        origstring2=NumUtil.doubleToString(e,10);
	     	flong2 = FixedPointLong.stringToFlong(origstring2);
	       	flongresult = b ? FixedPointLong.mult(flong1,flong2) : FixedPointLong.div(flong1, flong2);        
	       	endstring = FixedPointLong.flongToString(flongresult);
	       	err= Math.abs((b ? d*e : d/e) - Double.parseDouble(endstring)) > 1E-6;
		    if(err && print_on_failure) {
		        System.out.println("ERR " + (b?"mult ":"div ") + d + " " + e + " is " + (b?d*e:d/e) + " not " + endstring);
		    }
		    return err;
	    } catch(FixedPointLongException fple) {
	        throw new ArithmeticException("dotest_add: " + fple);
	    }
	}

	public static boolean dotest(String origstring) {
	    try {
		    String endstring="";
		    long flong = 0;
		    double err;
	        //origstring=NumUtil.doubleToString(start,10);
		    double start=Double.parseDouble(origstring);
	        flong = FixedPointLong.stringToFlong(origstring);
	        endstring = FixedPointLong.flongToString(flong);
		    err= Math.abs(start - Double.parseDouble(endstring));
		    if(err > TestResult.EPSILON && print_on_failure) {
		        System.out.println("ERR with " + start + ": flongtostring " + endstring + " flongtodouble " + (new Double(flong).doubleValue() / Math.pow(2,32)) + " : " + err);
		    }
		    return err > TestResult.EPSILON;
	    } catch(FixedPointLongException fple) {
	        throw new ArithmeticException("dotest_add: " + fple);
	    }
	}
	
	public static void main(String[] args) {
		test_and_stop_if_broken("3.74166666666667E-4");
		
	    // random tests
	    int numrandom = 10000;
	    System.out.println("Starting " + numrandom + " random tests");
	    for(int i=0;i<numrandom;i++) {
	        double d = Math.random() + Math.floor(Math.random() * 10000000.0);
	        if(i % 2 == 0) d = -d;
	        //System.out.println(NumUtil.doubleToString(d,8));
	        String s = "" + d;
	        if(Math.random() > 0.6) {
	        	s += "E";
	        	s += (int) Math.floor(Math.random() * 10)-7;
	        	System.out.println(s);
	        }
	        test_and_stop_if_broken(s);
	        System.out.println(i);
	    }
	    System.out.println("Random tests passed");

	    // random tests
	    int numrandom2 = 1000;
	    System.out.println("Starting " + numrandom2 + " random add/subtract tests");
	    for(int i=0;i<numrandom2;i++) {
	        double d = Math.random() + Math.floor(Math.random() * 100000000.0);
	        double e = Math.random() + Math.floor(Math.random() * 100000000.0);
	        if(Math.random() < 0.5) d = -d;
	        if(Math.random() < 0.5) e = -e;	        
	        //System.out.println(NumUtil.doubleToString(d,8));
	        testAdd_and_stop_if_broken(d,e,Math.random() < 0.5);
	    }
	    System.out.println("Random add/sub tests passed");
	    
	    //System.exit(0);
	    // exhaustive tests
	    int intprecision = 10;
	    int fracprecision = 10000;
	    int percent=0;
	    System.out.println("Starting exhaustive tests with intprecision " + intprecision + " and fracprecision " + fracprecision);
	    for(double d = -intprecision*fracprecision; d < intprecision*fracprecision; d ++) {
	            test_and_stop_if_broken(""+d/fracprecision);
	            if(d % (intprecision*fracprecision/100) == 0) {
	                System.out.print("\r" + percent/2.0 + "% " + NumUtil.doubleToString(d/fracprecision,6));
	                percent++;
	            }
	    }
	    System.out.println("\nDone!");
	}

}

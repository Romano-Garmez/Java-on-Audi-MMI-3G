package org.placelab.core;

/**
 * A small application that prints the version of the Placelab Toolkit
 */
public class Version {

    public static String majorVersion = "Port Orchard";
    public static String dateString = "$Date $";
    public static double numericVersion = 2.0;
    
    public static void main(String[] args) {
        System.out.println(majorVersion + " (" + numericVersion + ") " + 
                dateString.substring(5, dateString.length() - 1));
    }
}

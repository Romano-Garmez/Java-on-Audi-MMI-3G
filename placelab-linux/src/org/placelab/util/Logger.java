package org.placelab.util;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Dead simple logging for debugging text.
 * <p>
 * You can submit debugging text with 2 levels:
 * Logger.LOW and Logger.HIGH
 * and you can set the system property
 * placelab.Logger.level to
 * OFF, LOW, and HIGH, default is LOW.
 * <p>
 * You can redirect the logger to a file by setting
 * the placelab.Logger.out to a file of your choice
 * or STDOUT or STDERR.  Default is STDOUT.
 * 
 * 
 */
public class Logger {

    public static int LOW = 1;
    public static int MEDIUM = 2;
    public static int HIGH = 3;
    
    private static Logger singleton;
    
    protected PrintStream out;
    protected int level;
    
    protected static Logger getLogger() {
        if(singleton == null) {
            singleton = new Logger();
        }
        return singleton;
    }
    
    protected Logger() {
        String outfile = System.getProperty("placelab.Logger.out");
        if(outfile == null) outfile = "";
        if(outfile.equalsIgnoreCase("")) {
            out = System.out;
        } else if(outfile.equalsIgnoreCase("stdout")) {
            out = System.out;
        } else if(outfile.equalsIgnoreCase("stderr")) {
            out = System.err;
        } else {
            try {
                out = new PrintStream(new FileOutputStream(outfile));
            } catch(Exception e) {
                out = System.out;
            }
        }
        String levelS = System.getProperty("placelab.Logger.level");
        if(levelS == null) levelS = "";
        if(levelS.equalsIgnoreCase("off")) {
            level = 0;
        } else if(levelS.equalsIgnoreCase("low")) {
            level = LOW;
        } else if(levelS.equalsIgnoreCase("medium")) {
            level = MEDIUM;
        } else if(levelS.equalsIgnoreCase("high")) {
            level = HIGH;
        } else {
            level = 1;
        }
    }
    
    public static void print(String msg, int level) {
        Logger logger = getLogger();
        if(logger.level >= level)
            logger.out.print(msg);
    }
    
    public static void println(String msg, int level) {
        Logger logger = getLogger();
        if(logger.level >= level)
            logger.out.println(msg);
    }
    
    public static void setLogLevel(int level) {
        Logger logger = getLogger();
        logger.level = level;
    }
    
    public static int getLogLevel() {
        Logger logger = getLogger();
        return logger.level;
    }
    
}

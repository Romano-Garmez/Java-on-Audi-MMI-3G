/*
 * @(#)src/demo/applets/ImageMap/SoundArea.java, ui, dsdev 1.11
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v5.0
 * (C) Copyright IBM Corp. 1998, 2005. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */





/*
 * @(#)SoundArea.java	1.12 02/06/13
 */

import java.awt.Graphics;
import java.applet.AudioClip;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * An audio feedback ImageArea class.
 * This class extends the basic ImageArea Class to play a sound when
 * the user enters the area.
 *
 * @author 	Jim Graham
 * @author 	Chuck McManis
 * @version 	1.12, 06/13/02
 */
class SoundArea extends ImageMapArea {
    /** The URL of the sound to be played. */
    URL sound;
    AudioClip soundData = null;
    boolean hasPlayed; 
    boolean isReady = false;
    long	lastExit = 0;
    final static int HYSTERESIS = 1500;

    /**
     * The argument is the URL of the sound to be played.
     */
    public void handleArg(String arg) {
	try {
	    sound = new URL(parent.getDocumentBase(), arg);
	} catch (MalformedURLException e) {
	    sound = null;
	}
	hasPlayed = false;
    }

    /**
     * The applet thread calls the getMedia() method when the applet
     * is started.
     */
    public void getMedia() {
	if (sound != null && soundData == null) {
	    soundData = parent.getAudioClip(sound);
	}
	if (soundData == null) {
	    System.out.println("SoundArea: Unable to load data "+sound);
	}
	isReady = true;
    }

    /**
     * The enter method is called when the mouse enters the area.
     * The sound is played if the mouse has been outside of the
     * area for more then the delay indicated by HYSTERESIS.
     */
    public void enter() {
	// is the sound sample loaded?
	if (! isReady) {
	    parent.showStatus("Loading media file...");
	    return;
	}

	/*
 	 * So we entered the selection region, play the sound if
	 * we need to. Track the mouse entering and exiting the
	 * the selection box. If it doesn't stay out for more than
	 * "HYSTERESIS" millis, then don't re-play the sound.
	 */
	long now = System.currentTimeMillis();
	if (Math.abs(now - lastExit) < HYSTERESIS) {
	    // if within the window pretend that it was played.
	    hasPlayed = true;
    	    return;
	}

	// Else play the sound.
	if (! hasPlayed && (soundData != null)) {
	    hasPlayed = true;
	    soundData.play();
	}
    }

    /**
     * The exit method is called when the mouse leaves the area.
     */
    public void exit() {
	if (hasPlayed) {
	    hasPlayed = false;
	    lastExit = System.currentTimeMillis(); // note the time of exit
	}
    }
}

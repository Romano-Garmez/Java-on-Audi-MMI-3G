/*
 * @(#)src/demo/applets/ImageMap/DelayedSoundArea.java, ui, dsdev 1.11
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
 * @(#)DelayedSoundArea.java	1.12 02/06/13
 */

import java.awt.Graphics;
import java.applet.AudioClip;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

/**
 * This ImageArea Class will play a sound each time the user enters the 
 * area. It is different from SoundArea in that it accepts a delay (in 
 * tenths of a second) before it plays the sound. If the mouse leaves
 * the area before the time delay, the sound is not played.
 *
 * This allows you to have one piece of audio when the button is "hit"
 * via SoundArea and another if the user stays on the button.
 *
 * @author 	Chuck McManis
 * @version 	1.12, 06/13/02
 */
class DelayedSoundArea extends ImageMapArea {
    /** The URL of the sound to be played. */
    URL 	sound;
    AudioClip	soundData;
    boolean 	hasPlayed; 
    int	    	delay;
    int		countDown;

    /**
     * The argument is the URL of the sound to be played.
     * This method also sets this type of area to be non-terminal.
     */
    public void handleArg(String arg) {
	Thread soundLoader;
	StringTokenizer st = new StringTokenizer(arg, ", ");
	
	delay = Integer.parseInt(st.nextToken());
	try {
	    sound = new URL(parent.getDocumentBase(), st.nextToken());
	} catch (MalformedURLException e) {
	    sound = null;
	}
    }

    public void getMedia() {
	if (sound != null) {
	    soundData = parent.getAudioClip(sound);
	}
	if (soundData == null) {
	    System.out.println("DelayedSoundArea: Unable to load data "+sound);
	}
    }

    /**
     * The highlight method plays the sound in addition to the usual
     * graphical highlight feedback.
     */
    public void enter() {
	hasPlayed = false;
	countDown = delay;
	parent.startAnimation();
    }

    /**
     * This method is called every animation cycle if there are any
     * active animating areas.
     * @return true if this area requires further animation notifications
     */
    public boolean animate() {
	if (entered && ! hasPlayed) {
	    if (countDown > 0) {
		countDown--;
		return true;
	    }
	    hasPlayed = true;
	    if (soundData != null) {
	        soundData.play();
	    }
	}
	return false;
    }
}


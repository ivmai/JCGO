/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)AppletAudioClip.java     1.31 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.applet;

import java.lang.reflect.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.applet.AudioClip;

import java.security.*;

// TEMP
import java.io.*;


/**
 * Applet audio clip;
 *
 * @version     1.31 03/01/23
 * @author Arthur van Hoff, Kara Kytle
 */

public class
AppletAudioClip implements AudioClip {


        // Constructor that we use for instantiating AudioClip objects.
        // Represents the constructor for either sun.audio.SunAudioClip (default) or
        // com.sun.media.sound.JavaSoundAudioClip (if the Java Sound extension is installed).
        private static Constructor acConstructor = null;

    // url that this AudioClip is based on
    private URL url = null;

    // the audio clip implementation
    private AudioClip audioClip = null;

    boolean DEBUG = false /*true*/;

    /**
     * Constructs an AppletAudioClip from an URL.
     */
    public AppletAudioClip(URL url) {

        // store the url
        this.url = url;

        try {
            // create a stream from the url, and use it
            // in the clip.
            InputStream in = url.openStream();
            createAppletAudioClip(in);

        } catch (IOException e) {
                /* just quell it */
            /* if (DEBUG) */ {
                System.err.println("IOException creating AppletAudioClip" + e);
            }
        }
    }

    /**
     * Constructs an AppletAudioClip from a URLConnection.
     */
    public AppletAudioClip(URLConnection uc) {

        try {
            // create a stream from the url, and use it
            // in the clip.
            createAppletAudioClip(uc.getInputStream());

        } catch (IOException e) {
                /* just quell it */
            /* if (DEBUG) */ {
                System.err.println("IOException creating AppletAudioClip" + e);
            }
        }
    }


    /**
     * For constructing directly from Jar entries, or any other
     * raw Audio data. Note that the data provided must include the format
     * header.
     */
    public AppletAudioClip(byte [] data) {

        try {

            // construct a stream from the byte array
            InputStream in = new ByteArrayInputStream(data);

            createAppletAudioClip(in);

        } catch (IOException e) {
                /* just quell it */
            /* if (DEBUG) */ {
                System.err.println("IOException creating AppletAudioClip " + e);
            }
        }
    }


    /*
     * Does the real work of creating an AppletAudioClip from an InputStream.
     * This function is used by both constructors.
     */
    void createAppletAudioClip(InputStream in) throws IOException {

        // If we haven't initialized yet, we need to find the AudioClip constructor using reflection.
        // We'll use com.sun.media.sound.JavaSoundAudioClip to implement AudioClip if the Java Sound
        // extension is installed.  Otherwise, we use sun.audio.SunAudioClip.


        if (acConstructor == null) {

                if (DEBUG) System.out.println("Initializing AudioClip constructor.");

                try {

                        acConstructor = (Constructor) AccessController.doPrivileged( new PrivilegedExceptionAction() {

                                public Object run() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

                                        try {

                                                // attempt to load the Java Sound extension class JavaSoundAudioClip

                                                Class acClass = Class.forName("com.sun.media.sound.JavaSoundAudioClip",
                                                                        true,
                                                                        ClassLoader.getSystemClassLoader()); // may throw ClassNotFoundException

                                                if (DEBUG) System.out.println("Loaded JavaSoundAudioClip");

                                                Class[] parms = new Class[] {
                                                    Class.forName("java.io.InputStream") };
                                                return acClass.getConstructor(parms);   // may throw NoSuchMethodException or SecurityException

                                        } catch (ClassNotFoundException e) {
                                        }

                                        Class acClass = Class.forName("sun.audio.SunAudioClip", true, null);  // may throw ClassNotFoundException

                                        if (DEBUG) System.out.println("Loaded SunAudioClip");

                                        Class[] parms = new Class[] {
                                            Class.forName("java.io.InputStream") }; // may throw ClassNotFoundException
                                        return acClass.getConstructor(parms);   // may throw NoSuchMethodException or SecurityException
                                }
                        } );

                } catch (PrivilegedActionException e) {

                        if (DEBUG) System.out.println("Got a PrivilegedActionException: " + e.getException());

                        // e.getException() may be a NoSuchMethodException, SecurityException, or ClassNotFoundException.
                        // however, we throw an IOException to avoid changing the interfaces....

                        throw new IOException("Failed to get AudioClip constructor: " + e.getException());
                }

        } // if not initialized


        // Now instantiate the AudioClip object using the constructor we discovered above.

        try {

                Object[] args = new Object[] {in};
                audioClip = (AudioClip)acConstructor.newInstance(args); // may throw InstantiationException,
                                                                                                                                // IllegalAccessException,
                                                                                                                                // IllegalArgumentException,
                                                                                                                                // InvocationTargetException


        } catch (Exception e3) {

                // no matter what happened, we throw an IOException to avoid changing the interfaces....
                throw new IOException("Failed to construct the AudioClip: " + e3);
        }

    }


    public synchronized void play() {

                if (audioClip != null)
                        audioClip.play();
    }


    public synchronized void loop() {

                if (audioClip != null)
                        audioClip.loop();
    }

    public synchronized void stop() {

                if (audioClip != null)
                        audioClip.stop();
    }
}

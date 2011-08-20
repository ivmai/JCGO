/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SimpleOutputDeviceProvider.java  1.9 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.MixerProvider;


/**
 * Simple output device provider.
 *
 * @version 1.9 03/01/23
 * @author Kara Kytle
 */
public class SimpleOutputDeviceProvider extends MixerProvider {


    // STATIC VARIABLES


    /**
     * Set of info objects for all simple output devices on the system.
     */
    private static OutputDeviceInfo[] infos;

    /**
     * Set of all simple output devices on the system.
     */
    private static SimpleOutputDevice[] devices;



    // STATIC

    /**
     * Create objects representing all simple output devices on the system.
     */
    static {

        if (Printer.trace) Printer.trace("SimpleOutputDeviceProvider: static");

        // initialize
        Platform.initialize();

        // get the number of output devices
        int numDevices = nGetNumDevices();

        // initialize the arrays
        infos = new OutputDeviceInfo[numDevices];
        devices = new SimpleOutputDevice[numDevices];

        // fill in the info objects now.
        // we'll fill in the device objects as they're requested.

        String name;
        String vendor;
        String description;
        String version;

        for (int i = 0; i < infos.length; i++) {

            name = nGetName(i);
            vendor = nGetVendor(i);
            description = nGetDescription(i);
            version = nGetVersion(i);

            infos[i] = new OutputDeviceInfo(name, vendor, description, version, i, SimpleOutputDeviceProvider.class);
        }

        if (Printer.trace) Printer.trace("SimpleOutputDeviceProvider: static: found numDevices: " + numDevices);
    }


    // CONSTRUCTOR


    /**
     * Required public no-arg constructor.
     */
    public SimpleOutputDeviceProvider() {

        if (Printer.trace) Printer.trace("SimpleOutputDeviceProvider: constructor");
    }


    public Mixer.Info[] getMixerInfo() {
        // $$fb 2002-04-10: fix for 4667064: Java Sound provides bogus SourceDataLine and TargetDataLine
        return new Mixer.Info[0];
        /*
          Mixer.Info[] localArray = new Mixer.Info[infos.length];
          System.arraycopy(infos, 0, localArray, 0, infos.length);
          return localArray;
        */
    }


    public Mixer getMixer(Mixer.Info info) {
        // $$fb 2002-04-10: do not return non-working mixers
        /*
          for (int i = 0; i < infos.length; i++) {

          if (info == infos[i]) {
          return getDevice(infos[i]);
          }
          }
        */
        throw new IllegalArgumentException("Mixer " + info.toString() + " not supported by this provider.");
    }


    private Mixer getDevice(OutputDeviceInfo info) {
        int index = info.getIndex();

        if (devices[index] == null) {
            devices[index] = new SimpleOutputDevice(info);
        }

        return devices[index];
    }



    // INNER CLASSES


    /**
     * Info class for SimpleOutputDevices.  Adds an index value for
     * making native references to a particular device and a the
     * provider's Class to keep the provider class from being
     * unloaded.  Otherwise, at least on JDK1.1.7 and 1.1.8,
     * the provider class can be unloaded.  Then, then the provider
     * is next invoked, the static block is executed again and a new
     * instance of the device object is created.  Even though the
     * previous instance may still exist and be open / in use / etc.,
     * the new instance will not reflect that state....
     */
    static class OutputDeviceInfo extends Mixer.Info {

        private int index;
        private Class providerClass;

        private OutputDeviceInfo(String name, String vendor, String description, String version, int index, Class providerClass) {

            super(name, vendor, description, version);
            this.index = index;
            this.providerClass = providerClass;
        }


        int getIndex() {
            return index;
        }

    } // class OutputDeviceInfo


    // NATIVE METHODS

    private static int nGetNumDevices() { return 0; }
    private static String nGetName(int index) { return "SoundOut"; }
    private static String nGetVendor(int index) { return "Unknown Vendor"; }
    private static String nGetDescription(int index) { return "No details available"; }
    private static String nGetVersion(int index) { return "Unknown Version"; }
}

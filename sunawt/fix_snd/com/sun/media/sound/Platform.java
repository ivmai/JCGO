/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Platform.java    1.26 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

/**
 * Audio configuration class for exposing attributes specific to the platform or system.
 *
 * @version 1.26 03/01/23
 * @author Kara Kytle
 * @author Florian Bomers
 */
class Platform {


    // STATIC FINAL CHARACTERISTICS

    // native library we need to load
    private static final String libNameMain     = "jsound";

    private static final String libNameALSA     = "jsoundalsa";
    private static final String libNameDSound   = "jsoundds";
    private static final String libNameSolMIDI  = "jsoundsolmidi";

    // extra libs handling: bit flags for each different library
    public static final int LIB_MAIN     = 1;
    public static final int LIB_ALSA     = 2;
    public static final int LIB_DSOUND   = 4;
    public static final int LIB_SOLMIDI  = 8;

    // bit field of the constants above. Willbe set in loadLibraries
    private static int loadedLibs = 0;

    // features: the main native library jsound reports which feature is
    // contained in which lib
    public static final int FEATURE_MIDIIO       = 1;
    public static final int FEATURE_PORTS        = 2;
    public static final int FEATURE_DIRECT_AUDIO = 3;

    // SYSTEM CHARACTERISTICS
    // vary according to hardware architecture

    // signed8 (use signed 8-bit values) is true for everything we support except for
    // the solaris sbpro card.
    // we'll leave it here as a variable; in the future we may need this in java.
    // wait, is that true?  i'm not sure.  i think solaris takes unsigned data?
    // $$kk: 03.11.99: i think solaris takes unsigned 8-bit or signed 16-bit data....
    private static boolean signed8;

    // intel is little-endian.  sparc is big-endian.
    private static boolean bigEndian;

    // this is the value of the "java.home" system property.  i am looking it up here
    // for use when trying to load the soundbank, just so
    // that all the privileged code is localized in this file....
    private static String javahome;

    // this is the value of the "java.class.path" system property
    private static String classpath;

    // SECURITY

    private static JSSecurity jsSecurity = null;
    private static boolean securityPrivilege = false;

    static {

        /* Method m[] = new Method[1];
        Class cl[] = new Class[1];
        Object args[][] = new Object[1][0]; */

        if(Printer.trace)Printer.trace(">> Platform.java: static");

        try {
            jsSecurity = JSSecurityManager.getJSSecurity();
            securityPrivilege = true;
        } catch (SecurityException e) {
            if(Printer.err)Printer.err("Platform.java: Security Exception: " + e);
        }

        /* if(Printer.debug)Printer.debug("jsSecurity: " + jsSecurity);
        if(Printer.debug)Printer.debug("securityPrivilege: " + securityPrivilege);

        if ( securityPrivilege && (jsSecurity != null ) ) {

            if( jsSecurity instanceof DisabledSecurity ) {
                // do nothing...

            } else if( jsSecurity.getName().startsWith("JDK12") ) {
                loadLibraries();
                readProperties();

            } else {
                // run privileged code with non-1.2-style security
                try {
                    jsSecurity.requestPermission(m, cl, args, JSSecurity.READ_PROPERTY);
                    m[0].invoke(cl[0], args[0]);

                    jsSecurity.requestPermission(m, cl, args, JSSecurity.LINK);
                    m[0].invoke(cl[0], args[0]);

                } catch (Exception e) {
                    if(Printer.err)Printer.err("Unable to get read property privilege: " + e);
                }

                loadLibraries();
                readProperties();
            }
        } else */ {

            // run privileged code without security
            loadLibraries();
            readProperties();
        }
    }


    /**
     * Private constructor.
     */
    private Platform() {
    }


    // METHODS FOR INTERNAL IMPLEMENTATION USE


    /**
     * Dummy method for forcing initialization.
     */
    static void initialize() {

        if(Printer.trace)Printer.trace("Platform: initialize()");
    }


    /**
     * Determine whether the system is big-endian.
     */
    static boolean isBigEndian() {

        return bigEndian;
    }


    /**
     * Determine whether the system takes signed 8-bit data.
     */
    static boolean isSigned8() {

        return signed8;
    }


    /**
     * Obtain javahome.
     * $$kk: 04.16.99: this is *bad*!!
     */
    static String getJavahome() {

        return javahome;
    }

    /**
     * Obtain classpath.
     * $$jb: 04.21.99: this is *bad* too!!
     */
    static String getClasspath() {

        return classpath;
    }


    // PRIVATE METHODS

    /**
     * Load the native library or libraries.
     */
    private static void loadLibraries() {
        if(Printer.trace)Printer.trace(">>Platform.loadLibraries");

        try {
            // load the main library
            loadLibrary(libNameMain);
            // just for the heck of it...
            loadedLibs |= LIB_MAIN;
        } catch (SecurityException e) {
            if(Printer.err)Printer.err("Security exception loading main native library.  JavaSound requires access to these resources.");
            throw(e);
        }

        // now try to load extra libs. They are defined at compile time in the Makefile
        // with the define EXTRA_SOUND_JNI_LIBS
        String extraLibs = nGetExtraLibraries();
        // the string is the libraries, separated by white space
        StringTokenizer st = new StringTokenizer(extraLibs);
        while (st.hasMoreTokens()) {
            String lib = st.nextToken();
            try {
                loadLibrary(lib);
                if (lib.equals(libNameALSA)) {
                    loadedLibs |= LIB_ALSA;
                    if (Printer.debug) Printer.debug("Loaded ALSA lib successfully.");
                }
                else if (lib.equals(libNameDSound)) {
                    loadedLibs |= LIB_DSOUND;
                    if (Printer.debug) Printer.debug("Loaded DirectSound lib successfully.");
                }
                else if (lib.equals(libNameSolMIDI)) {
                    loadedLibs |= LIB_SOLMIDI;
                    if (Printer.debug) Printer.debug("Loaded Solaris MIDI lib successfully.");
                } else {
                    if (Printer.err) Printer.err("Loaded unknown lib '"+lib+"' successfully.");
                }
            } catch (Throwable t) {
                if (Printer.err) Printer.err("Couldn't load library "+lib+": "+t.toString());
            }
        }
    }

    private static void loadLibrary(String libName) {
        try {
            if (securityPrivilege) {
                if (jsSecurity != null) {
                    if(Printer.debug) Printer.debug("using security manager to load library");
                    jsSecurity.loadLibrary(libName);
                } else {
                    if(Printer.debug) Printer.debug("not using security manager to load library");
                    System.loadLibrary(libName);
                }
                if (Printer.debug) Printer.debug("loaded library " + libName);
            } else {
                if (Printer.debug) Printer.debug("no security privilege, did not load library " + libName);
            }
        } catch (UnsatisfiedLinkError e2) {
            if (Printer.err)Printer.err("UnsatisfiedLinkError loading native library " + libName);
            throw(e2);
        }
    }

    static boolean isMidiIOEnabled() {
        return isFeatureLibLoaded(FEATURE_MIDIIO);
    }

    static boolean isPortsEnabled() {
        return isFeatureLibLoaded(FEATURE_PORTS);
    }

    static boolean isDirectAudioEnabled() {
        return isFeatureLibLoaded(FEATURE_DIRECT_AUDIO);
    }

    private static boolean isFeatureLibLoaded(int feature) {
        if (Printer.debug) Printer.debug("Platform: Checking for feature "+feature+"...");
        int requiredLib = nGetLibraryForFeature(feature);
        boolean isLoaded = (requiredLib != 0) && ((loadedLibs & requiredLib) == requiredLib);
        if (Printer.debug) Printer.debug("          ...needs library "+requiredLib+". Result is loaded="+isLoaded);
        return isLoaded;
    }

    // the following native methods are implemented in Platform.c
    private native static boolean nIsBigEndian();
    private native static boolean nIsSigned8();
    private native static String nGetExtraLibraries();
    private native static int nGetLibraryForFeature(int feature);

    /**
     * Read the required system properties.
     */
    private static void readProperties() {
        // $$fb 2002-03-06: implement check for endianness in native. Facilitates porting !
        bigEndian = nIsBigEndian();
        signed8 = nIsSigned8(); // Solaris on Sparc: signed, all others unsigned
        try {
            // check out the hardware architecture and determine its endianism

            // get the system properties here.  use security manager if present.
            /* if ( securityPrivilege && (jsSecurity != null ) ) {

                if( jsSecurity.getName().startsWith("JDK12") ) {

                    if(Printer.debug)Printer.debug("using 12 security to get properties");
                    try{
                        Constructor cons = JDK12PropertyAction.cons;
                        javahome = (String) JDK12.doPrivM.invoke(
                                                                 JDK12.ac,
                                                                 new Object[] {
                                                                     cons.newInstance( new Object[] { "java.home" } )
                                                                 });
                        classpath = (String) JDK12.doPrivM.invoke(
                                                                  JDK12.ac,
                                                                  new Object[] {
                                                                      cons.newInstance( new Object[] { "java.class.path" } )
                                                                  });
                    } catch( Exception e ) {
                        if(Printer.debug)Printer.debug("not using security to get properties");
                        javahome = System.getProperty("java.home");
                        classpath = System.getProperty("java.class.path");
                    }
                } else {
                    if(Printer.debug)Printer.debug("using security to get properties");
                    javahome = jsSecurity.readProperty("java.home");
                    classpath = jsSecurity.readProperty("java.class.path");
                }
            } else */ {
                if(Printer.debug)Printer.debug("not using security to get properties");
                javahome = System.getProperty("java.home");
                classpath = System.getProperty("java.class.path");
            }

        } catch (SecurityException e) {
            if(Printer.err)Printer.err("Security exception getting system properties.  JavaSound requires access to these resources.");
            throw(e);
        }
    }
}

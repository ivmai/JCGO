/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JSSecurityManager.java   1.22 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;



class JSSecurityManager {

    private static JSSecurity security = null;
    private static SecurityManager securityManager;

    static {
        // $$fb 2001-11-01 part of fix for Bug 4521048:
        // Applets: Java Sound dies with an exception at init time
        // Handle the case where the securitymanager changes over time.
        // Initialization of JSSecurity is done in a method.
        // Not sure whether this should be in static initializer at all.
        initSecurity();
    }


    private static void initSecurity() {
        if (Printer.trace) Printer.trace("JSSecurityManager: initSecurity()");
        securityManager = System.getSecurityManager();
        /* boolean jdk12=false;
        boolean jdk11=false;
        boolean msjvm=false;

        try {
            String javaVersion = System.getProperty("java.version");

            if (Printer.debug) Printer.debug("javaVersion "+javaVersion);

            // $$jb: 06.24.99: assume we're running on version 1.2 and up
            // unless we discover otherwise.  Local JDK builds are marked with
            // a username and timestamp, so checking for "1.2" in the string
            // isn't a good check to see if we're on 1.2 or up.


            if (!javaVersion.equals(""))  {
                // verify that the version string starts with a number
                if (javaVersion.startsWith("1.1")) {
                    if (Printer.debug) Printer.debug("JDK 1.1.x");
                    jdk11 = true;
                }
                else {
                    jdk12 = true;
                    if (Printer.debug) Printer.debug("JDK 1.2 and up");
                }
            }

            String javaVendor =
                System.getProperty("java.vendor", "Sun").toLowerCase();


            if (javaVendor.indexOf("icrosoft") > 0) { // microsoft JVM
                msjvm = true;
            }

        } catch (Throwable t) {
            if (Printer.debug) Printer.debug("Exception caught: " + t);
        }

        if (securityManager != null) { // check the security manager
            if (Printer.debug) Printer.debug("securityManager: " + securityManager);

            if ( securityManager.toString().indexOf("netscape") != -1 ) {
                // Netscape's security manager
                if (Printer.debug) Printer.debug("NetscapeSecurity");
                security = NetscapeSecurity.security;
            } else if ( ( securityManager.toString().indexOf("com.ms.security") != -1 ) || msjvm ){
                // Internet Explorer security manager
                if (Printer.debug) Printer.debug("IESecurity");
                security = IESecurity.security;
            } else if ( (securityManager.toString().indexOf("sun.applet.AppletSecurity") != -1) ||
                        (securityManager.toString().indexOf("sun.plugin.ActivatorSecurityManager") != -1) ) {
                // appletviewer
                if (jdk11) { // JDK 1.1
                    //securityManager = null;
                    //security = null;
                    if (Printer.debug) Printer.debug("DefaultSecurity");
                    security = com.sun.media.sound.DefaultSecurity.security;
                }
                if (jdk12) {
                    if(Printer.debug) Printer.debug("JDK12Security for applets");
                    security = JDK12Security.security;
                }

            } else if ( securityManager.toString().indexOf("java.lang.SecurityManager") != -1) {
                // JDk 1.2 security manager
                if (jdk12) { // verify that the version is 1.2 and up
                    //securityManager = null;
                    if (Printer.debug) Printer.debug("JDK12Security");
                    security = JDK12Security.security;
                }
            }

            else { // TODO
                if (Printer.debug) Printer.debug("unknown security manager");
                if(jdk12) {
                    security = JDK12Security.security;
                } else {
                    security = com.sun.media.sound.DefaultSecurity.security;
                }
            }
        } */
        if (Printer.trace) Printer.trace("JSSecurityManager: initSecurity()");
    }

    // sun.applet.AppletSecurity

    static JSSecurity getJSSecurity() throws SecurityException {
        // $$fb 2001-11-01 part of fix for Bug 4521048: Applets: Java Sound dies with an exception at init time
        // if security manager changed, initialize again security
        if (securityManager!=System.getSecurityManager()) {
            initSecurity();
        }

        return security;
    }

    static void checkRecord() throws SecurityException {

        // $$fb 2001-11-01 part of fix for Bug 4521048: Applets: Java Sound dies with an exception at init time
        // do not use cached instance of JSSecurity
        /* JSSecurity security=getJSSecurity();

        // $$jb:  09.02.99:  This method is only used in
        // non-1.2-security environments.  By default,
        // in non-1.2 environments, we do not allow audio
        // capture in applets unless the permission is
        // explicitly granted by the JMFRegistry.  We may
        // need to revisit this issue....

        // If its not an applet, no need to check permissions
        if (security==null) {
            return;
        }
        //
        try {
            if(Printer.debug)Printer.debug("JSSecurity.checkRecord(): looking up JMF registry class, using reflection");
            Class registry = Class.forName("com.sun.media.util.Registry");
            Method m = registry.getMethod( "get", new Class[] { String.class } );
            Object[] arguments = new Object[] { "secure.allowCaptureFromApplets" } ;
            Object captureFromApplets = m.invoke(registry,arguments);

            if (captureFromApplets == null ||
                !(captureFromApplets instanceof Boolean) ||
                ((Boolean)captureFromApplets).booleanValue() == false) {

                if(Printer.debug)Printer.debug("    record permission denied by Registry");
                throw new SecurityException("record permission denied");
            }

        } catch( ClassNotFoundException e1) {
            if(Printer.debug)Printer.debug("caught ClassNotFoundException");
            throw new SecurityException("record permission denied");
        } catch( InvocationTargetException e2) {
            if(Printer.debug)Printer.debug("caught InvocationTargetException");
            throw new SecurityException("record permission denied");
        } catch( IllegalAccessException e3) {
            if(Printer.debug)Printer.debug("caught IllegalAccessException");
            throw new SecurityException("record permission denied");
        } catch( NoSuchMethodException e4) {
            if(Printer.debug)Printer.debug("caught NoSuchMethodException");
            throw new SecurityException("record permission denied");
        } */

    }

    static boolean isLinkPermissionEnabled() {
        // $$fb 2001-11-01 part of fix for Bug 4521048: Applets: Java Sound dies with an exception at init time
        // do not use cached instance of JSSecurity
        JSSecurity security=getJSSecurity();
        if (security == null)
            return true;
        else {
            return security.isLinkPermissionEnabled();
        }
    }

    static void loadLibrary(String name) throws UnsatisfiedLinkError {
        try {
            JSSecurity s = getJSSecurity();
            if (s != null) {
                s.loadLibrary(name);
            } else {
                System.loadLibrary(name);
            }
        } catch (Throwable t) {
            throw new UnsatisfiedLinkError("JSSecurityManager: " + t);
        }
    }
}

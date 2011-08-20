/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MixerThread.java 1.24 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * Thread to manage the inner loop of the audio engine.
 *
 * @version 1.24 03/01/23
 * @author David Rivas, Chris Schardt, Kara Kytle
 */
class MixerThread extends Thread {

    // STATIC VARIABLES

    /**
     * Vector of all created instances of this class.
     */
    private static Vector threadObjects = new Vector();

    private static ThreadGroup topmostThreadGroup = null;

    private static final String threadName = "Headspace mixer frame proc thread";

    // Variables needed for Security

    private static JSSecurity jsSecurity = null;
    private static boolean securityPrivilege = false;
    /* private Method m[] = new Method[1];
    private Class cl[] = new Class[1];
    private Object args[][] = new Object[1][0]; */

    // INSTANCE VARIABLES

    /**
     * True after runNative() returns.
     * False if the thread is running in the native code.
     * When true, causes this thread to repeatedly call wait(), just after runNative().
     */
    private boolean paused;

    private long frameProc;


    // Get our security class in the static{} block

    static {

        /* Method m[] = new Method[1];
        Class cl[] = new Class[1];
        Object args[][] = new Object[1][0];

        securityPrivilege = true;

        if(Printer.debug)Printer.debug("MixerThread.static: securityPrivilege = " + securityPrivilege);

        try {
            jsSecurity = JSSecurityManager.getJSSecurity();
            if( (jsSecurity != null) && !(jsSecurity instanceof DisabledSecurity) ) {
                jsSecurity.requestPermission(m, cl, args, JSSecurity.THREAD_GROUP);
                m[0].invoke(cl[0],args[0]);
            }

            // $$jb: with no exception, we can assume we're running locally
            //       as an application, so securityPrivilege remains true
            //       even if jsSecurity is null.

        } catch (Exception e) {
            if(Printer.debug)Printer.debug("Exception caught: " + e);
            if(Printer.debug)Printer.debug("Setting securityPrivilege to false");
            securityPrivilege = false;
        }

        if (securityPrivilege) {
            if(Printer.debug)Printer.debug("MixerThread.java: getting the real topmost thread group");

            if( (jsSecurity!=null) && (jsSecurity instanceof DisabledSecurity) ) {

                                // do nothing

            } else if( (jsSecurity!=null) && (jsSecurity.getName().startsWith("JDK12") )) {
                try {

                    // invoke the privileged action using 1.2 security

                    Constructor cons = JDK12TopmostThreadGroupAction.cons;
                    topmostThreadGroup = (ThreadGroup) JDK12.doPrivM.invoke(
                                                                            JDK12.ac,
                                                                            new Object[] {
                                                                                cons.newInstance( new Object[0] )
                                                                            });

                    if(Printer.debug)Printer.debug("Got topmost thread group with 1.2 style security");

                } catch (Exception e) {
                    if(Printer.debug)Printer.debug("Exception getting topmost thread group with 1.2 style security");
                    // try without using 1.2 style
                    topmostThreadGroup = getTopmostThreadGroup();
                }

            } else */ {
                // not JDK 1.2 style, assume we already have permission
                topmostThreadGroup = getTopmostThreadGroup();
            }
        /* } else {
            if(Printer.debug)Printer.debug("MixerThread.java: no securityPrivilege, settling for current threadgroup");
            topmostThreadGroup = Thread.currentThread().getThreadGroup();
        } */
    }


    /**
     * Private constructor, invoked by our getNewThreadObject method
     */
    // $$jb:06.24.99: taking the frameProc argument out of this constructor
    // to simplify the 1.2 security privileged block in getNewThreadObject.
    //  private MixerThread() {
    protected MixerThread() {

        // we use the top parent thread group.
        super(topmostThreadGroup, "");

        if(Printer.trace)Printer.trace(">> MixerThread() CONSTRUCTOR");

        //this.frameProc = frameProc;

        /* if( (jsSecurity!=null) && (jsSecurity instanceof DisabledSecurity)) {

            // do nothing

        } else if( (jsSecurity!=null) && (jsSecurity.getName().startsWith("JDK12") )) {
            try {
                if(Printer.debug)Printer.debug("Configuring thread with 1.2 style security");
                                // run privileged code with 1.2-style security

                                // invoke the privileged action using 1.2 security

                Constructor cons = JDK12ConfigureThreadAction.cons;
                JDK12.doPrivM.invoke(
                                     JDK12.ac,
                                     new Object[] {
                                         cons.newInstance( new Object[] {
                                             this, threadName
                                         })
                                     });

                if(Printer.debug)Printer.debug("Configured thread with 1.2 style security");

            } catch (Exception e) {
                if(Printer.debug)Printer.debug("Exception configuring thread with 1.2 style security");
                                // try without using 1.2 style
                configureThread();
            }

        } else */ {
            // not JDK 1.2 style, assume we already have permission
            configureThread();
        }

        paused = false;
        if(Printer.trace)Printer.trace(">> MixerThread() CONSTRUCTOR completed");
    }
    /*
     * private setter - $$jb:06.24.99: was an argument to private constructor,
     * but I split it to simplify the 1.2 security privileged block in
     * getNewThreadObject
     */
    private void setFrameProc( long frameProc ) {
        this.frameProc = frameProc;
    }


    private static MixerThread getExistingThreadObject(long frameProc) {

        if(Printer.trace)Printer.trace(">> MixerThread: getExistingThreadObject(" + frameProc + ")");

        MixerThread currentThreadObject;

        synchronized(threadObjects) {

            for (int i = 0; i < threadObjects.size(); i++) {

                currentThreadObject = (MixerThread)threadObjects.elementAt(i);

                if (currentThreadObject.frameProc == frameProc) {

                    if(Printer.trace)Printer.trace("<< MixerThread: getExistingThreadObject() returning existing object: " + currentThreadObject);
                    return currentThreadObject;
                }
            }
        }

        return null;
    }


    private static MixerThread getNewThreadObject(long frameProc) {

        /* Method m[] = new Method[1];
        Class cl[] = new Class[1];
        Object args[][] = new Object[1][0]; */

        MixerThread newThreadObject = null;

        if(Printer.trace)Printer.trace(">> MixerThread: getNewThreadObject(" + frameProc + ")");

        // $$jb: 05.03.99:
        // get THREAD and THREAD_GROUP permissions here ... they
        // will be needed in the private constructor

        if(Printer.debug)Printer.debug("MixerThread.getNewThreadObject: asking for permissions");

        /* if( securityPrivilege && (jsSecurity != null) ) {

            if( jsSecurity.getName().startsWith("JDK12") ) {
                try {

                    // invoke the privileged action using 1.2 security

                    Constructor cons = JDK12NewMixerThreadAction.cons;
                    newThreadObject = (MixerThread) JDK12.doPrivM.invoke(
                                                                         JDK12.ac,
                                                                         new Object[] {
                                                                             cons.newInstance( new Object[0] )
                                                                         });

                    if(Printer.debug)Printer.debug("Got mixer thread object with 1.2 style security");

                } catch (Exception e) {
                    if(Printer.debug)Printer.debug("Exception getting mixer thread object with 1.2 style security");

                    // try without using 1.2 style
                    newThreadObject = new MixerThread();
                    newThreadObject.setFrameProc( frameProc );
                }

                newThreadObject.setFrameProc( frameProc );

            } else {

                try {
                    jsSecurity.requestPermission(m, cl, args, JSSecurity.THREAD);
                    m[0].invoke(cl[0], args[0]);

                    jsSecurity.requestPermission(m, cl, args, JSSecurity.THREAD_GROUP);
                    m[0].invoke(cl[0], args[0]);

                    if(Printer.debug)Printer.debug("MixerThread.getNewThreadObject: got THREAD, THREAD_GROUP permissions");

                } catch (Exception e) {
                    if(Printer.debug)Printer.debug("MixerThread.getNewThreadObject: could not get THREAD, THREAD_GROUP permissions");
                }
                newThreadObject = new MixerThread();
                newThreadObject.setFrameProc( frameProc );
            }

        } else */ {
            if(Printer.debug)Printer.debug("MixerThread.getNewThreadObject: no securityPrivilege or jsSecurity=null, not using security");
            newThreadObject = new MixerThread();
            newThreadObject.setFrameProc( frameProc );
        }

        threadObjects.addElement(newThreadObject);

        return newThreadObject;
    }


    private void configureThread() {

        if(Printer.trace)Printer.trace(">> MixerThread: configureThread()");

        setDaemon(true);
        setPriority(Thread.MAX_PRIORITY);
        setName(threadName);

        if(Printer.trace)Printer.trace("<< MixerThread: configureThread() completed");
    }


    private static ThreadGroup getTopmostThreadGroup() {

        if(Printer.trace)Printer.trace(">> MixerThread: getTopmostThreadGroup()");

        ThreadGroup g = currentThread().getThreadGroup();

        while ((g.getParent() != null) && (g.getParent().getParent() != null)) {
            g = g.getParent();
        }

        if(Printer.trace)Printer.trace("<< MixerThread: getTopmostThreadGroup() completed");

        return g;
    }


    public void run() {

        if(Printer.trace)Printer.trace(">> MixerThread: run()");

        while (true) {

            if(Printer.debug)Printer.debug("MixerThread: run(): calling runNative()");
            runNative(frameProc);
            if(Printer.debug)Printer.debug("MixerThread: run(): runNative() returned");

            synchronized(this) {

                paused = true;

                                // repeatedly call wait() until a call to unpause()
                while (paused) {

                    try {
                        if(Printer.debug)Printer.debug("MixerThread: run(): calling wait()");
                        wait();     // paused gets cleared here
                        if(Printer.debug)Printer.debug("MixerThread: run(): returned from wait()");
                    } catch(InterruptedException e) {
                        if(Printer.debug)Printer.debug("MixerThread: run(): wait() interrupted");
                    }
                }

                if(Printer.debug)Printer.debug("MixerThread: run(): exited while(paused)");
            }

            if(Printer.debug)Printer.debug("MixerThread: run(): exited synchronized block");
        }
    }


    // Causes the while loop above to continue by returning from its wait() call
    private synchronized void unpause() {

        if(Printer.trace)Printer.trace(">> MixerThread: unpause() called, notifying...");
        paused = false;
        notify();
        if(Printer.trace)Printer.trace("<< MixerThread: unpause() completed");
    }


    // Processes frames of audio data
    // Returns after HAE_ReleaseAudioCard() is called
    // native private void runNative();
    native private void runNative(long frameProc);
}

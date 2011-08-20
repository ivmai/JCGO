/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)EventDispatcher.java     1.27 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.EventObject;
import java.util.Vector;

import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.ControllerEventListener;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// needs at least J2SE 1.2.x!
import java.util.ArrayList;


/**
 * EventDispatcher.  Used by various classes in the Java Sound implementation
 * to send events.
 *
 * @version 1.27 03/01/23
 * @author David Rivas
 * @author Kara Kytle
 * @author Florian Bomers
 */
class EventDispatcher implements Runnable {

    /**
     * time of inactivity until the auto closing clips
     * are closed
     */
    private static final int AUTO_CLOSE_TIME = 5000;

    /**
     * the last time that an event was processed
     */
    private long lastProcessEventTime = System.currentTimeMillis();

    /**
     * List of events
     */
    private Vector eventQueue = new Vector();


    /**
     * True if thread should exit
     */
    private boolean done = false;


    /**
     * Thread object for this EventDispatcher instance
     */
    Thread thread = null;
    static boolean creatingThread = false;

    // Variables needed for Security

    private static JSSecurity jsSecurity = null;
    private static boolean securityPrivilege = false;
    private static ThreadGroup topmostThreadGroup = null;
    /* private Method m[] = new Method[1];
    private Class cl[] = new Class[1];
    private Object args[][] = new Object[1][0]; */


    /*
     * support for auto-closing Clips
     */
    private ArrayList autoClosingClips = new ArrayList();

    /**
     * Static block for getting security permissions
     */
    // $$fb 2001-11-01 part of fix for Bug 4521048: Applets: Java Sound dies with an exception at init time
    // do not use cached instance of JSSecurity
    private static void initSecurity() {
        securityPrivilege = true;

        /* if(Printer.debug)Printer.debug("EventDispatcher.initSecurity(): securityPrivilege = " + securityPrivilege);

        try {

            jsSecurity = JSSecurityManager.getJSSecurity();

        } catch (SecurityException e) {
            if(Printer.debug)Printer.debug("Exception caught: " + e);
            if(Printer.debug)Printer.debug("Setting securityPrivilege to false");
            securityPrivilege = false;
        } */

        //$$fb copied from MixerThread.java for bug 4521048. Using the main threadgroup also fixes 4304996.
        if (securityPrivilege) {
            if(Printer.debug)Printer.debug("EventDispatcher.java: getting the real topmost thread group");

            /* if( (jsSecurity!=null) && (jsSecurity instanceof DisabledSecurity) ) {

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
        } /* else {
            if(Printer.debug)Printer.debug("EventDispatcher.java: no securityPrivilege, settling for current threadgroup");
            topmostThreadGroup = Thread.currentThread().getThreadGroup();
        } */
    }


    /**
     * This start() method starts an event thread if one is not already active.
     */
    void start() {

        if( ((thread == null) && !creatingThread ) || (done) ) {
            // $$fb 2001-11-01 part of fix for Bug 4521048
            initSecurity();

            final Runnable localRunnable = this;
            done = false;

            /* if( securityPrivilege && (jsSecurity != null) ) {

                if( jsSecurity.getName().startsWith("JDK12") ) {
                    try{
                        creatingThread = true;
                        Constructor cons = JDK12NewEventDispatcherAction.cons;
                        //$$fb create thread in main thread group
                        Object args[] = new Object[] { topmostThreadGroup, localRunnable};
                        thread = (Thread) JDK12.doPrivM.invoke(
                                                               JDK12.ac,
                                                               new Object[] {
                            cons.newInstance( args )
                                });
                        if(Printer.debug) Printer.debug("Got EventDispatcher with 1.2 style security");
                        creatingThread = false;

                    } catch( InstantiationException ise) {
                        if( Printer.debug ) Printer.debug("InstantiationException getting event dispatcher with 1.2 style security: "+ ise);
                        // try without using 1.2 style
                        thread = new Thread(topmostThreadGroup, (Runnable)this);
                        thread.start();
                    } catch( IllegalAccessException iae) {
                        if( Printer.debug ) Printer.debug("IllegalAccessException getting event dispatcher with 1.2 style security: "+iae);
                        // try without using 1.2 style
                        thread = new Thread(topmostThreadGroup, (Runnable)this);
                        thread.start();
                    } catch( InvocationTargetException ite) {
                        if( Printer.debug ) Printer.debug("InvocationTargetException getting event dispatcher with 1.2 style security: "+ite);
                        // try without using 1.2 style
                        thread = new Thread(topmostThreadGroup, (Runnable)this);
                        thread.start();
                    }

                } else {
                    try {
                        jsSecurity.requestPermission(m, cl, args, JSSecurity.THREAD);
                        m[0].invoke(cl[0], args[0]);

                        if(Printer.debug)Printer.debug("EventDispatcher.start(): got THREAD permission");
                    } catch( InvocationTargetException ite) {
                        if(Printer.debug)Printer.debug("EventDispatcher.start(): could not got THREAD permission");
                    } catch( IllegalAccessException iae) {
                        if(Printer.debug)Printer.debug("EventDispatcher.start(): could not got THREAD permission");
                    }
                    thread = new Thread(topmostThreadGroup, this);
                    thread.start();
                }
            } else */ {
                if(Printer.debug)Printer.debug("EventDispatcher.start(): no securityPrivilege or jsSecurity==null, not using security");
                thread = new Thread(topmostThreadGroup, this);
                thread.start();
            }
            if (thread!=null) {
                try {
                    thread.setName("Java Sound event dispatcher");
                } catch (SecurityException se) {}
                if (Printer.debug) Printer.debug("Created thread in group "+thread.getThreadGroup());
            }
        }
    }


    /**
     * Invoked when there is at least one event in the queue.
     * Implement this as a callback to process one event.
     */
    protected void processEvent(EventInfo eventInfo) {

        // process an LineEvent

        if (eventInfo.getEvent() instanceof LineEvent) {

            LineEvent event = (LineEvent)eventInfo.getEvent();
            Vector currentListeners = eventInfo.getListeners();
            if (Printer.debug) Printer.debug("Sending "+event+" to "+currentListeners.size()+" listeners");
            for (int i = 0; i < currentListeners.size(); i++) {
                ((LineListener)currentListeners.elementAt(i)).update(event);
            }

            return;
        }

        // process a MetaMessage

        if (eventInfo.getEvent() instanceof MetaMessage) {

            MetaMessage event = (MetaMessage)eventInfo.getEvent();
            Vector currentListeners = eventInfo.getListeners();

            for (int i = 0; i < currentListeners.size(); i++) {
                ((MetaEventListener)currentListeners.elementAt(i)).meta(event);
            }

            return;
        }

        // process a Controller or Mode Event

        if (eventInfo.getEvent() instanceof ShortMessage) {

            ShortMessage event = (ShortMessage)eventInfo.getEvent();
            int status = event.getStatus();

            // Controller and Mode events have status byte 0xBc, where
            // c is the channel they are sent on.

            if( (status>>4)==11 ) {

                Vector currentListeners = eventInfo.getListeners();

                for (int i = 0; i < currentListeners.size(); i++) {
                    ((ControllerEventListener)currentListeners.elementAt(i)).controlChange(event);
                }
            }
            return;
        }


        /*
          // process a MidiDeviceEvent

          if (eventInfo.getEvent() instanceof MidiDeviceEvent) {

          MidiDeviceEvent event = (MidiDeviceEvent)eventInfo.getEvent();
          Vector currentListeners = eventInfo.getListeners();

          for (int i = 0; i < currentListeners.size(); i++) {
          ((MidiDeviceListener)currentListeners.elementAt(i)).update(event);
          }

          return;
          }
        */

        Printer.err("Unknown event type: " + eventInfo.getEvent());
    }


    /**
     * Wait until there is something in the event queue to process.  Then
     * dispatch the event to the listeners.The entire method does not
     * need to be synchronized since this includes taking the event out
     * from the queue and processing the event. We only need to provide
     * exclusive access over the code where an event is removed from the
     *queue.
     */
    protected void dispatchEvents() {

        EventInfo eventInfo = null;

        synchronized (this) {

            // Wait till there is an event in the event queue.
            try {

                if ((!done) && (eventQueue.size() == 0)) {
                    if (autoClosingClips.size() > 0) {
                        // make sure this exceeds AUTO_CLOSE_TIME !
                        wait(AUTO_CLOSE_TIME + 100);
                    } else {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                done = true;
                notifyAll();
                if (Printer.debug) e.printStackTrace();
            }
            if (!done && eventQueue.size() > 0) {
                // Remove the event from the queue and dispatch it to the listeners.
                eventInfo = (EventInfo)eventQueue.elementAt(0);
                eventQueue.removeElementAt(0);
            }

        } // end of synchronized
        if (!done) {
            if (eventInfo != null) {
                lastProcessEventTime = System.currentTimeMillis();
                processEvent(eventInfo);
            } else {
                long timeSinceLastEvent = System.currentTimeMillis() - lastProcessEventTime;
                if  (timeSinceLastEvent >= AUTO_CLOSE_TIME) {
                    closeAutoClosingClips();
                }
            }
        }
    }


    /**
     * Queue the given event in the event queue.
     */
    synchronized void postEvent(EventInfo eventInfo) {

        eventQueue.addElement(eventInfo);
        notifyAll();
    }


    /**
     * Stop the thread.
     * $$kk: 12.21.98: we never call this -- but we should!!
     */
    synchronized void kill() {

        done = true;
        notifyAll();
    }


    /**
     * A loop to dispatch events.
     */
    public void run() {

        while (!done) {
            try {
                dispatchEvents();
            } catch (Throwable t) {
                if (Printer.err) t.printStackTrace();
            }
        }
        if (Printer.debug) Printer.debug("Exiting Java Sound EventDispatcher thread.");
    }


    /**
     * Send audio events.
     */
    void sendAudioEvents(Object event, Vector listeners) {

        start();

        Vector currentListeners = (Vector)listeners.clone();

        EventInfo eventInfo = new EventInfo(event, currentListeners);
        postEvent(eventInfo);
    }

    // $$fb 2001-11-01 part of fix for Bug 4521048: Applets: Java Sound dies with an exception at init time
    // copied from MixerThread.java
    private static ThreadGroup getTopmostThreadGroup() {

        if(Printer.trace)Printer.trace(">> EventDispatcher: getTopmostThreadGroup()");

        ThreadGroup g = Thread.currentThread().getThreadGroup();

        while ((g.getParent() != null) && (g.getParent().getParent() != null)) {
            g = g.getParent();
        }

        if(Printer.trace)Printer.trace("<< EventDispatcher: getTopmostThreadGroup() completed");

        return g;
    }

    /*
     * go through the list of registered auto-closing
     * Clip instances and close them, if appropriate
     *
     * This method is called in regular intervals
     */
    private void closeAutoClosingClips() {
        synchronized(autoClosingClips) {
            if (Printer.debug)Printer.debug("> EventDispatcher.closeAutoClosingClips ("+autoClosingClips.size()+" clips)");
            for (int i = autoClosingClips.size()-1; i >= 0 ; i--) {
                AutoClosingClip clip = (AutoClosingClip) autoClosingClips.get(i);
                // sanity check
                if (!clip.isOpen() || !clip.isAutoClosing()) {
                    autoClosingClips.remove(i);
                }
                else if (!clip.isRunning() && !clip.isActive() && clip.isAutoClosing()) {
                    if (Printer.debug)Printer.debug("EventDispatcher: closing clip "+clip);
                    clip.close();
                } else {
                    if (Printer.debug)Printer.debug("Doing nothing with clip "+clip+":");
                    if (Printer.debug)Printer.debug("  open="+clip.isOpen()+", autoclosing="+clip.isAutoClosing());
                    if (Printer.debug)Printer.debug("  isRunning="+clip.isRunning()+", isActive="+clip.isActive());
                }
            }
        }
        if (Printer.debug)Printer.debug("< EventDispatcher.closeAutoClosingClips ("+autoClosingClips.size()+" clips)");
    }

    /**
     * called from auto-closing clips when one of their open() method is called
     */
    void autoClosingClipOpened(AutoClosingClip clip) {
        int index = 0;
        synchronized(autoClosingClips) {
            index = autoClosingClips.indexOf(clip);
            if (index == -1) {
                if (Printer.debug)Printer.debug("EventDispatcher: adding auto-closing clip "+clip);
                autoClosingClips.add(clip);
            }
        }
        if (index == -1) {
            synchronized (this) {
                // this is only for the case that the first clip is set to autoclosing,
                // and it is already open, and nothing is done with it.
                // EventDispatcher.process() method would block in wait() and
                // never close this first clip, keeping the device open.
                notifyAll();
            }
        }
    }

    /**
     * called from auto-closing clips when their closed() method is called
     */
    void autoClosingClipClosed(AutoClosingClip clip) {
        /*synchronized(autoClosingClips) {
          int index = autoClosingClips.indexOf(clip);
          if (index >= 0) {
          if (Printer.debug)Printer.debug("EventDispatcher: removing auto-closing clip "+clip);
          autoClosingClips.remove(index);
          }
          }*/
    }

    /**
     * Container for an event and a set of listeners to deliver it to.
     */
    class EventInfo {

        private Object event;
        private Vector listeners;

        EventInfo(Object event, Vector listeners) {

            this.event = event;
            this.listeners = listeners;
        }

        Object getEvent() {

            return event;
        }

        Vector getListeners() {

            return listeners;
        }
    } // class EventInfo

} // class EventDisapatcher

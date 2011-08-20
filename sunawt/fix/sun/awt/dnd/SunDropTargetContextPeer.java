/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SunDropTargetContextPeer.java    1.20 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.dnd;

import java.awt.AWTPermission;
import java.awt.Component;
import java.awt.Point;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.DnDConstants;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.InvalidDnDOperationException;

import java.awt.dnd.peer.DropTargetContextPeer;

import java.util.HashSet;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

import sun.awt.DebugHelper;
import sun.awt.SunToolkit;
import sun.awt.datatransfer.DataTransferer;
import sun.awt.datatransfer.ToolkitThreadBlockedHandler;

/**
 * <p>
 * The SunDropTargetContextPeer class is the generic class responsible for handling
 * the interaction between a windowing systems DnD system and Java.
 * </p>
 *
 * @version 1.7.12/20/00
 * @since JDK1.3.1
 *
 */

public abstract class SunDropTargetContextPeer implements DropTargetContextPeer, Transferable {

    private   DropTarget              currentDT;
    private   DropTargetContext       currentDTC;
    private   long[]                  currentT;
    private   int                     currentA;   // target actions
    private   int                     currentSA;  // source actions
    private   int                     currentDA;  // current drop action
    private   int                     previousDA;

    private   long                    nativeDragContext;

    private   Transferable            local;

    private boolean                   dragRejected = false;

    protected int                     dropStatus   = STATUS_NONE;
    protected boolean                 dropComplete = false;

    /*
     * global lock
     */

    protected static final Object _globalLock = new Object();

    /*
     * a primitive mechanism for advertising intra-JVM Transferables
     */

    protected static Transferable         currentJVMLocalSourceTransferable = null;

    public static void setCurrentJVMLocalSourceTransferable(Transferable t) throws InvalidDnDOperationException {
        synchronized(_globalLock) {
            if (t != null && currentJVMLocalSourceTransferable != null) {
                    throw new InvalidDnDOperationException();
            } else {
                currentJVMLocalSourceTransferable = t;
            }
        }
    }

    /**
     * obtain the transferable iff the operation is in the same VM
     */

    private static Transferable getJVMLocalSourceTransferable() {
        return currentJVMLocalSourceTransferable;
    }

    /*
     * constants used by dropAccept() or dropReject()
     */

    protected final static int STATUS_NONE   =  0; // none pending
    protected final static int STATUS_WAIT   =  1; // drop pending
    protected final static int STATUS_ACCEPT =  2;
    protected final static int STATUS_REJECT = -1;

    /**
     * create the peer
     */

    public SunDropTargetContextPeer() {
        super();
    }

    /**
     * @return the DropTarget associated with this peer
     */

    public DropTarget getDropTarget() { return currentDT; }

    /**
     * @param actions set the current actions
     */

    public synchronized void setTargetActions(int actions) {
        currentA = actions &
            (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
    }

    /**
     * @return the current target actions
     */

    public int getTargetActions() {
        return currentA;
    }

    /**
     * get the Transferable associated with the drop
     */

    public Transferable getTransferable() {
        return this;
    }

    /**
     * @return current DataFlavors available
     */
    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!

    public DataFlavor[] getTransferDataFlavors() {
        final Transferable    localTransferable = local;

        if (localTransferable != null) {
            return localTransferable.getTransferDataFlavors();
        } else {
            return DataTransferer.getInstance().getFlavorsForFormatsAsArray
                (currentT, DataTransferer.adaptFlavorMap
                    (currentDT.getFlavorMap()));
        }
    }

    /**
     * @return if the flavor is supported
     */

    public boolean isDataFlavorSupported(DataFlavor df) {
        Transferable localTransferable = local;

        if (localTransferable != null) {
            return localTransferable.isDataFlavorSupported(df);
        } else {
            return DataTransferer.getInstance().getFlavorsForFormats
                (currentT, DataTransferer.adaptFlavorMap
                    (currentDT.getFlavorMap())).
                containsKey(df);
        }
    }

    /**
     * @return the data
     */

    public Object getTransferData(DataFlavor df)
      throws UnsupportedFlavorException, IOException,
        InvalidDnDOperationException
    {
        Long lFormat = null;
        Transferable localTransferable = local;

        if (localTransferable != null) {
            return localTransferable.getTransferData(df);
        }

        if (dropStatus != STATUS_ACCEPT || dropComplete) {
            throw new InvalidDnDOperationException("No drop current");
        }

        Map flavorMap = DataTransferer.getInstance().getFlavorsForFormats
            (currentT, DataTransferer.adaptFlavorMap
                (currentDT.getFlavorMap()));

        lFormat = (Long)flavorMap.get(df);
        if (lFormat == null) {
            throw new UnsupportedFlavorException(df);
        }

        if (df.isRepresentationClassRemote() &&
            currentDA != DnDConstants.ACTION_LINK) {
            throw new InvalidDnDOperationException("only ACTION_LINK is permissable for transfer of java.rmi.Remote objects");
        }

        final long format = lFormat.longValue();
        Object ret = getNativeData(format);

        if (ret instanceof byte[]) {
            try {
                return DataTransferer.getInstance().
                    translateBytes((byte[])ret, df, format, this);
            } catch (IOException e) {
                throw new InvalidDnDOperationException(e.getMessage());
            }
        } else if (ret instanceof InputStream) {
            try {
                return DataTransferer.getInstance().
                    translateStream((InputStream)ret, df, format, this);
            } catch (IOException e) {
                throw new InvalidDnDOperationException(e.getMessage());
            }
        } else {
            throw new IOException("no native data was transfered");
        }
    }

    protected abstract Object getNativeData(long format);

    /**
     * @return if the transfer is a local one
     */
    public boolean isTransferableJVMLocal() {
        return local != null || getJVMLocalSourceTransferable() != null;
    }

    private int handleEnterMessage(final Component component,
                                   final int x, final int y,
                                   final int dropAction,
                                   final int actions, final long[] formats,
                                   final long nativeCtxt) {
        EventDispatcher dispatcher =
            new EventDispatcher(this, dropAction, actions, formats, nativeCtxt,
                                EventDispatcher.EXIT_BLOCKED_MODE);

        SunDropTargetEvent event =
            new SunDropTargetEvent(component, SunDropTargetEvent.MOUSE_ENTERED,
                                 x, y, dispatcher);

        ToolkitThreadBlockedHandler handler =
            DataTransferer.getInstance().getToolkitThreadBlockedHandler();
        handler.lock();
        try {
            // schedule callback
            SunToolkit.postEvent(SunToolkit.targetToAppContext(component),
                event);

            while (!dispatcher.isDone()) {
                handler.enter();
            }
        }
        finally {
            handler.unlock();
        }

        // return target's response
        return dispatcher.getReturnValue();
    }

    /**
     * actual processing on EventQueue Thread
     */

    private void processEnterMessage(SunDropTargetEvent event) {
        Component  c    = (Component)event.getSource();
        DropTarget dt   = c.getDropTarget();
        Point      hots = event.getPoint();

        local = getJVMLocalSourceTransferable();

        if (currentDTC != null) { // some wreckage from last time
            currentDTC.removeNotify();
            currentDTC = null;
        }

        if (c.isShowing() && dt != null && dt.isActive()) {
            currentDT  = dt;
            currentDTC = currentDT.getDropTargetContext();

            currentDTC.addNotify(this);

            currentA   = dt.getDefaultActions();

            try {
                ((DropTargetListener)dt).dragEnter(new DropTargetDragEvent(currentDTC,
                                                                           hots,
                                                                           currentDA,
                                                                           currentSA));
            } catch (Exception e) {
                e.printStackTrace();
                currentDA = DnDConstants.ACTION_NONE;
            }
        } else {
            currentDT  = null;
            currentDTC = null;
            currentDA   = DnDConstants.ACTION_NONE;
            currentSA   = DnDConstants.ACTION_NONE;
            currentA   = DnDConstants.ACTION_NONE;
        }

    }

    /**
     * upcall to handle exit messages
     */

    private void handleExitMessage(final Component component,
                                   final long nativeCtxt) {
        EventDispatcher dispatcher =
            new EventDispatcher(this, nativeCtxt,
                                EventDispatcher.EXIT_BLOCKED_MODE);

        SunDropTargetEvent event =
            new SunDropTargetEvent(component,
                                 SunDropTargetEvent.MOUSE_EXITED,
                                 0, 0, dispatcher);

        ToolkitThreadBlockedHandler handler =
            DataTransferer.getInstance().getToolkitThreadBlockedHandler();
        handler.lock();
        try {
            SunToolkit.postEvent(SunToolkit.targetToAppContext(component),
                event);

            while (!dispatcher.isDone()) {
                handler.enter();
            }
        }
        finally {
            handler.unlock();
        }
    }

    /**
     *
     */

    private void processExitMessage(SunDropTargetEvent event) {
        Component         c   = (Component)event.getSource();
        DropTarget        dt  = c.getDropTarget();
        DropTargetContext dtc = null;

        if (dt == null) {
            currentDT = null;
            currentT  = null;

            if (currentDTC != null) {
                currentDTC.removeNotify();
            }

            currentDTC = null;

            return;
        }

        if (dt != currentDT) {

            if (currentDTC != null) {
                currentDTC.removeNotify();
            }

            currentDT  = dt;
            currentDTC = dt.getDropTargetContext();

            currentDTC.addNotify(this);
        }

        dtc = currentDTC;

        if (dt.isActive()) try {
            ((DropTargetListener)dt).dragExit(new DropTargetEvent(dtc));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            currentA  = DnDConstants.ACTION_NONE;
            currentSA = DnDConstants.ACTION_NONE;
            currentDA = DnDConstants.ACTION_NONE;
            currentDT = null;
            currentT  = null;

            currentDTC.removeNotify();
            currentDTC = null;

            local = null;

            dragRejected = false;
        }
    }

    private int handleMotionMessage(final Component component,
                                    final int x, final int y,
                                    final int dropAction,
                                    final int actions, final long[] formats,
                                    final long nativeCtxt) {
        EventDispatcher dispatcher =
            new EventDispatcher(this, dropAction, actions, formats, nativeCtxt,
                                EventDispatcher.EXIT_BLOCKED_MODE);

        SunDropTargetEvent event =
            new SunDropTargetEvent(component,
                                 SunDropTargetEvent.MOUSE_DRAGGED,
                                 x, y, dispatcher);

        ToolkitThreadBlockedHandler handler =
            DataTransferer.getInstance().getToolkitThreadBlockedHandler();
        handler.lock();
        try {
            SunToolkit.postEvent(SunToolkit.targetToAppContext(component),
                event);

            while (!dispatcher.isDone()) {
                handler.enter();
            }
        }
        finally {
            handler.unlock();
        }

        // return target's response
        return dispatcher.getReturnValue();
    }

    /**
     *
     */

    private void processMotionMessage(SunDropTargetEvent event,
                                      boolean operationChanged) {
        Component         c    = (Component)event.getSource();
        Point             hots = event.getPoint();
        int               id   = event.getID();
        DropTarget        dt   = c.getDropTarget();
        DropTargetContext dtc  = null;

        if (c.isShowing() && (dt != null) && dt.isActive()) {
            if (currentDT != dt) {
                if (currentDTC != null) {
                    currentDTC.removeNotify();
                }

                currentDT  = dt;
                currentDTC = null;
            }

            dtc = currentDT.getDropTargetContext();
            if (dtc != currentDTC) {
                if (currentDTC != null) {
                    currentDTC.removeNotify();
                }

                currentDTC = dtc;
                currentDTC.addNotify(this);
            }

            currentA = currentDT.getDefaultActions();

            try {
                DropTargetDragEvent dtde = new DropTargetDragEvent(dtc,
                                                                   hots,
                                                                   currentDA,
                                                                   currentSA);
                DropTargetListener dtl = (DropTargetListener)dt;
                if (operationChanged) {
                    dtl.dropActionChanged(dtde);
                } else {
                    dtl.dragOver(dtde);
                }

                if (dragRejected) {
                    currentDA = DnDConstants.ACTION_NONE;
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentDA = DnDConstants.ACTION_NONE;
            }
        } else {
            currentDA = DnDConstants.ACTION_NONE;
        }
    }

    /**
     * upcall to handle the Drop message
     */

    private void handleDropMessage(final Component component,
                                   final int x, final int y,
                                   final int dropAction, final int actions,
                                   final long[] formats,
                                   final long nativeCtxt) {
        EventDispatcher dispatcher =
            new EventDispatcher(this, dropAction, actions, formats, nativeCtxt,
                                !EventDispatcher.EXIT_BLOCKED_MODE);

        SunDropTargetEvent event =
            new SunDropTargetEvent(component,
                                 SunDropTargetEvent.MOUSE_DROPPED,
                                 x, y, dispatcher);
        SunToolkit.postEvent(SunToolkit.targetToAppContext(component), event);

    }

    /**
     *
     */

    private void processDropMessage(SunDropTargetEvent event) {
        Component  c    = (Component)event.getSource();
        Point      hots = event.getPoint();
        DropTarget dt   = c.getDropTarget();

        dropStatus   = STATUS_WAIT; // drop pending ACK
        dropComplete = false;

        if (c.isShowing() && dt != null && dt.isActive()) {
            DropTargetContext dtc = dt.getDropTargetContext();

            currentDT = dt;

            if (currentDTC != null) {
                currentDTC.removeNotify();
            }

            currentDTC = dtc;
            currentDTC.addNotify(this);
            currentA = dt.getDefaultActions();

            synchronized(_globalLock) {
                if ((local = getJVMLocalSourceTransferable()) != null)
                    setCurrentJVMLocalSourceTransferable(null);
            }

            try {
                ((DropTargetListener)dt).drop(new DropTargetDropEvent(dtc,
                                                                      hots,
                                                                      currentDA,
                                                                      currentSA,
                                                                      local != null));
            } finally {
                if (dropStatus == STATUS_WAIT) {
                    rejectDrop();
                } else if (dropComplete == false) {
                    dropComplete(false);
                }
            }
        } else {
            rejectDrop();
        }
    }

    /**
     * acceptDrag
     */

    public synchronized void acceptDrag(int dragOperation) {
        if (currentDT == null) {
            throw new InvalidDnDOperationException("No Drag pending");
        }
        currentDA = mapOperation(dragOperation);
        if (currentDA != DnDConstants.ACTION_NONE) {
            dragRejected = false;
        }
    }

    /**
     * rejectDrag
     */

    public synchronized void rejectDrag() {
        if (currentDT == null) {
            throw new InvalidDnDOperationException("No Drag pending");
        }
        currentDA = DnDConstants.ACTION_NONE;
        dragRejected = true;
    }

    /**
     * acceptDrop
     */

    public synchronized void acceptDrop(int dropOperation) {
        if (dropOperation == DnDConstants.ACTION_NONE)
            throw new IllegalArgumentException("invalid acceptDrop() action");

        if (dropStatus != STATUS_WAIT) {
            throw new InvalidDnDOperationException("invalid acceptDrop()");
        }

        currentDA = currentA = mapOperation(dropOperation & currentSA);

        dropStatus   = STATUS_ACCEPT;
        dropComplete = false;
    }

    /**
     * reject Drop
     */

    public synchronized void rejectDrop() {
        if (dropStatus != STATUS_WAIT) {
            throw new InvalidDnDOperationException("invalid rejectDrop()");
        }
        dropStatus = STATUS_REJECT;
        /*
         * Fix for 4285634.
         * The target rejected the drop means that it doesn't perform any
         * drop action. This change is to make Solaris behavior consistent
         * with Win32.
         */
        currentDA = DnDConstants.ACTION_NONE;
        dropComplete(false);
    }

    /**
     * mapOperation
     */

    private int mapOperation(int operation) {
        int[] operations = {
                DnDConstants.ACTION_MOVE,
                DnDConstants.ACTION_COPY,
                DnDConstants.ACTION_LINK,
        };
        int   ret = DnDConstants.ACTION_NONE;

        for (int i = 0; i < operations.length; i++) {
            if ((operation & operations[i]) == operations[i]) {
                    ret = operations[i];
                    break;
            }
        }

        return ret;
    }

    /**
     * signal drop complete
     */

    public synchronized void dropComplete(boolean success) {
        if (dropStatus == STATUS_NONE) {
            throw new InvalidDnDOperationException("No Drop pending");
        }

        if (currentDTC != null) currentDTC.removeNotify();

        currentDT  = null;
        currentDTC = null;
        currentT   = null;
        currentA   = DnDConstants.ACTION_NONE;

        synchronized(_globalLock) {
            currentJVMLocalSourceTransferable = null;
        }

        dropStatus   = STATUS_NONE;
        dropComplete = true;

        doDropDone(success, currentDA, local != null);

        currentDA = DnDConstants.ACTION_NONE;

        nativeDragContext = 0;
    }

    protected abstract void doDropDone(boolean success,
                                       int dropAction, boolean isLocal);

    protected synchronized long getNativeDragContext() {
        return nativeDragContext;
    }

    static class EventDispatcher {

        private final SunDropTargetContextPeer peer;

        // context fields
        private final int dropAction;
        private final int actions;
        private final long[] formats;
        private final long nativeCtxt;
        private final boolean exitBlockedMode;

        // dispatcher state fields
        private int returnValue = 0;
        // set of events to be dispatched by this dispatcher
        private final HashSet eventSet = new HashSet(3);

        static final boolean EXIT_BLOCKED_MODE = true;
        static ToolkitThreadBlockedHandler handler;

        private static synchronized void initHandler() {
            if (handler == null)
                handler =
                 DataTransferer.getInstance().getToolkitThreadBlockedHandler();
        }

        EventDispatcher(SunDropTargetContextPeer peer,
                        int dropAction,
                        int actions,
                        long[] formats,
                        long nativeCtxt,
                        boolean exitBlockedMode) {

            this.peer         = peer;
            this.nativeCtxt   = nativeCtxt;
            this.dropAction   = dropAction;
            this.actions      = actions;
            this.formats      = formats;
            this.exitBlockedMode = exitBlockedMode;
        }

        EventDispatcher(SunDropTargetContextPeer peer, long nativeCtxt,
                        boolean exitBlockedMode) {

            this.peer         = peer;
            this.nativeCtxt   = nativeCtxt;

            this.dropAction   = DnDConstants.ACTION_NONE;
            this.actions      = DnDConstants.ACTION_NONE;
            this.formats      = null;

            this.exitBlockedMode = exitBlockedMode;
        }

        void dispatchEvent(SunDropTargetEvent e) {
            int id = e.getID();

            switch (id) {
            case SunDropTargetEvent.MOUSE_ENTERED:
                dispatchEnterEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_DRAGGED:
                dispatchMotionEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_EXITED:
                dispatchExitEvent(e);
                break;
            case SunDropTargetEvent.MOUSE_DROPPED:
                dispatchDropEvent(e);
                break;
            default:
                throw new InvalidDnDOperationException();
            }
        }

        private void dispatchEnterEvent(SunDropTargetEvent e) {
            synchronized (peer) {

                // store the drop action here to track operation changes
                peer.previousDA = dropAction;

                // setup peer context
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;

                peer.processEnterMessage(e);

                setReturnValue(peer.currentDA);
            }
        }

        private void dispatchMotionEvent(SunDropTargetEvent e) {
            synchronized (peer) {

                boolean operationChanged = peer.previousDA != dropAction;
                peer.previousDA = dropAction;

                // setup peer context
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;

                peer.processMotionMessage(e, operationChanged);

                setReturnValue(peer.currentDA);
            }
        }

        private void dispatchExitEvent(SunDropTargetEvent e) {
            synchronized (peer) {

                // setup peer context
                peer.nativeDragContext = nativeCtxt;

                peer.processExitMessage(e);
            }
        }

        private void dispatchDropEvent(SunDropTargetEvent e) {
            synchronized (peer) {

                // setup peer context
                peer.nativeDragContext = nativeCtxt;
                peer.currentT          = formats;
                peer.currentSA         = actions;
                peer.currentDA         = dropAction;

                peer.processDropMessage(e);
            }
        }

        void setReturnValue(int ret) {
            returnValue = ret;
        }

        int getReturnValue() {
            return returnValue;
        }

        boolean isDone() {
            return eventSet.isEmpty();
        }

        void registerEvent(SunDropTargetEvent e) {
            if (exitBlockedMode) {
                if (handler == null)
                    initHandler();
                handler.lock();
                try {
                    eventSet.add(e);
                }
                finally {
                    handler.unlock();
                }
            }
        }

        void unregisterEvent(SunDropTargetEvent e) {
            if (exitBlockedMode) {
                if (handler == null)
                    initHandler();
                handler.lock();
                try {
                    eventSet.remove(e);
                    if (eventSet.isEmpty()) {
                        handler.exit();
                    }
                }
                finally {
                    handler.unlock();
                }
            }
        }
    }
}

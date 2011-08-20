/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MDragSourceContextPeer.java      1.38 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;

import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;

import java.awt.event.InputEvent;

import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;

import java.util.Map;

import sun.awt.dnd.SunDragSourceContextPeer;

/**
 * <p>
 * TBC
 * </p>
 *
 * @version 1.38
 * @since JDK1.2
 *
 */

final class MDragSourceContextPeer extends SunDragSourceContextPeer {

    private static final MDragSourceContextPeer theInstance =
        new MDragSourceContextPeer(null);

    /**
     * construct a new MDragSourceContextPeer
     */

    private MDragSourceContextPeer(DragGestureEvent dge) {
        super(dge);
    }

    static MDragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        theInstance.setTrigger(dge);
        return theInstance;
    }

    protected void startDrag(Transferable transferable,
                             long[] formats, Map formatMap) {
        try {
            long nativeCtxtLocal = startDrag(getTrigger().getComponent(),
                                             transferable,
                                             getTrigger().getTriggerEvent(),
                                             getCursor(),
                                             getCursor() == null ? 0 : getCursor().getType(),
                                             getDragSourceContext().getSourceActions(),
                                             formats,
                                             formatMap);
            setNativeContext(nativeCtxtLocal);
        } catch (Exception e) {
            throw new InvalidDnDOperationException("failed to create native peer: " + e);
        }

        if (getNativeContext() == 0) {
            throw new InvalidDnDOperationException("failed to create native peer");
        }

        MDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(transferable);
    }

    /**
     * downcall into native code
     */

    private native long startDrag(Component component,
                                  Transferable transferable,
                                  InputEvent nativeTrigger,
                                  Cursor c, int ctype, int actions,
                                  long[] formats, Map formatMap);

    /**
     * set cursor
     */

    public void setCursor(Cursor c) throws InvalidDnDOperationException {
        AWTLockAccess.awtLock();
        try {
            super.setCursor(c);
        }
        finally {
            AWTLockAccess.awtUnlock();
        }
    }

    protected boolean needsBogusExitBeforeDrop() {
        return false;
    }
}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MDropTargetContextPeer.java      1.43 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.InvalidDnDOperationException;

import java.io.InputStream;

import java.util.Map;

import java.io.IOException;
import sun.awt.dnd.SunDropTargetContextPeer;

/**
 * <p>
 * The MDropTargetContextPeer class is the class responsible for handling
 * the interaction between the Motif DnD system and Java.
 * </p>
 *
 * @version 1.43
 * @since JDK1.2
 *
 */

final class MDropTargetContextPeer extends SunDropTargetContextPeer {

    private long              nativeDropTransfer;

    long                      nativeDataAvailable = 0;
    Object                    nativeData          = null;

    /**
     * create the peer
     */

    static MDropTargetContextPeer createMDropTargetContextPeer() {
        return new MDropTargetContextPeer();
    }

    /**
     * create the peer
     */

    private MDropTargetContextPeer() {
        super();
    }

    protected Object getNativeData(long format) {
        AWTLockAccess.awtLock();
        try {
            if (nativeDropTransfer == 0) {
                nativeDropTransfer = startTransfer(getNativeDragContext(),
                                                   format);
            } else {
                addTransfer (nativeDropTransfer, format);
            }

            for (nativeDataAvailable = 0;
                 format != nativeDataAvailable;) {
                try {
                    AWTLockAccess.awtWait();
                } catch (ThreadDeath death) {
                    throw death;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        finally {
            AWTLockAccess.awtUnlock();
        }

        return nativeData;
    }

    /**
     * signal drop complete
     */

    protected void doDropDone(boolean success, int dropAction,
                              boolean isLocal) {
        dropDone(getNativeDragContext(), nativeDropTransfer, isLocal,
                 success, dropAction);
    }

    /**
     * notify transfer complete
     */

    private void newData(long format, String type, byte[] data) {
        nativeDataAvailable = format;
        nativeData          = data;

        AWTLockAccess.awtNotifyAll();
    }

    /**
     * notify transfer failed
     */

    private void transferFailed(long format) {
        nativeDataAvailable = format;
        nativeData          = null;

        AWTLockAccess.awtNotifyAll();
    }

    /**
     * schedule a native DnD transfer
     */

    private native long startTransfer(long nativeDragContext, long format);

    /**
     * schedule a native DnD data transfer
     */

    private native void addTransfer(long nativeDropTransfer, long format);

    /**
     * signal that drop is completed
     */

    private native void dropDone(long nativeDragContext, long nativeDropTransfer,
                                 boolean localTx, boolean success, int dropAction);
}

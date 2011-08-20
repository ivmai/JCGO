/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WObjectPeer.java 1.14 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.awt.Component;

abstract class WObjectPeer {

    static {
        initIDs();
    }

    long      pData;    // The Windows handle for the native widget.
    Object    target;   // The associated AWT object.

    private boolean disposed = false;

    public static WObjectPeer getPeerForTarget(Object t) {
        WObjectPeer peer = (WObjectPeer) WToolkit.targetToPeer(t);
        return peer;
    }

    /*
     * Subclasses should override disposeImpl() instead of dispose(). Client
     * code should always invoke dispose(), never disposeImpl().
     */
    abstract protected void disposeImpl();
    public final void dispose() {
        boolean call_disposeImpl = false;

        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    disposed = call_disposeImpl = true;
                }
            }
        }

        if (call_disposeImpl) {
            disposeImpl();
        }
    }
    protected final boolean isDisposed() {
        return disposed;
    }

    protected void finalize() throws Throwable {
        // Calling dispose() here is essentially a NOP since the current
        // implementation prohibts gc before an explicit call to dispose().
        dispose();
        super.finalize();
    }

    /**
     * Initialize JNI field and method IDs
     */
    private static native void initIDs();
}

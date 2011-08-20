/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)EmbeddedFrame.java       1.22 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.peer.*;

/**
 * A generic container used for embedding Java components, usually applets.
 * An EmbeddedFrame has two related uses:
 *
 * . Within a Java-based application, an EmbeddedFrame serves as a sort of
 *   firewall, preventing the contained components or applets from using
 *   getParent() to find parent components, such as menubars.
 *
 * . Within a C-based application, an EmbeddedFrame contains a window handle
 *   which was created by the application, which serves as the top-level
 *   Java window.  EmbeddedFrames created for this purpose are passed-in a
 *   handle of an existing window created by the application.  The window
 *   handle should be of the appropriate native type for a specific
 *   platform, as stored in the pData field of the ComponentPeer.
 *
 * @version     1.15, 04/07/00
 * @author      Thomas Ball
 */
public abstract class EmbeddedFrame extends Frame {

    private boolean isCursorAllowed = true;

    // JDK 1.1 compatibility
    private static final long serialVersionUID = 2967042741780317130L;

    protected EmbeddedFrame() {
        this((long)0);
    }

    /**
     * @deprecated This constructor will be removed in 1.5
     */
    protected EmbeddedFrame(int handle) {
        this((long)handle);
    }

    protected EmbeddedFrame(long handle) {
    }

    /**
     * Block introspection of a parent window by this child.
     */
    public Container getParent() {
        return null;
    }

    /**
     * Block modifying any frame attributes, since they aren't applicable
     * for EmbeddedFrames.
     */
    public void setTitle(String title) {}
    public void setIconImage(Image image) {}
    public void setMenuBar(MenuBar mb) {}
    public void setResizable(boolean resizable) {}
    public void remove(MenuComponent m) {}

    public boolean isResizable() {
        return false;
    }

    public void addNotify() {
        synchronized (getTreeLock()) {
            if (getPeer() == null) {
                setPeer(new NullEmbeddedFramePeer());
            }
            super.addNotify();
        }
    }

    // These three functions consitute RFE 4100710. Do not remove.
    public void setCursorAllowed(boolean isCursorAllowed) {
        this.isCursorAllowed = isCursorAllowed;
        getPeer().updateCursorImmediately();
    }
    public boolean isCursorAllowed() {
        return isCursorAllowed;
    }
    public Cursor getCursor() {
        return (isCursorAllowed)
            ? super.getCursor()
            : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }

    // Need a native call to circumvent Component.peer not being public.
    protected native void setPeer(ComponentPeer p);

    private static class NullEmbeddedFramePeer
        extends NullComponentPeer implements FramePeer {
        public void setTitle(String title) {}
        public void setIconImage(Image im) {}
        public void setMenuBar(MenuBar mb) {}
        public void setResizable(boolean resizeable) {}
        public void setState(int state) {}
        public int getState() { return Frame.NORMAL; }
        public void setMaximizedBounds(Rectangle b) {}
        public void toFront() {}
        public void toBack() {}
        public Component getGlobalHeavyweightFocusOwner() { return null; }
        public void synthesizeWindowActivation(boolean flag) {}
    }
} // class EmbeddedFrame

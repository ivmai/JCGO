/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WPanelPeer.java  1.20 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.awt.*;
import java.awt.peer.*;

import sun.awt.SunGraphicsCallback;

class WPanelPeer extends WCanvasPeer implements PanelPeer {

    // ComponentPeer overrides

    public void paint(Graphics g) {
        super.paint(g);
        SunGraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().
            runComponents(((Container)target).getComponents(), g,
                          SunGraphicsCallback.LIGHTWEIGHTS |
                          SunGraphicsCallback.HEAVYWEIGHTS);
    }
    public void print(Graphics g) {
        super.print(g);
        SunGraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().
            runComponents(((Container)target).getComponents(), g,
                          SunGraphicsCallback.LIGHTWEIGHTS |
                          SunGraphicsCallback.HEAVYWEIGHTS);
    }

    // ContainerPeer (via PanelPeer) implementation

    public Insets getInsets() {
        return insets_;
    }

    // Toolkit & peer internals

    Insets insets_;

    static {
        initIDs();
    }

    /**
     * Initialize JNI field IDs
     */
    private static native void initIDs();

    WPanelPeer(Component target) {
        super(target);
    }

    void initialize() {
        super.initialize();
        insets_ = new Insets(0,0,0,0);

        Color c = ((Component)target).getBackground();
        if (c == null) {
            c = WColor.getDefaultColor(WColor.WINDOW_BKGND);
            ((Component)target).setBackground(c);
            setBackground(c);
        }
        c = ((Component)target).getForeground();
        if (c == null) {
            c = WColor.getDefaultColor(WColor.WINDOW_TEXT);
            ((Component)target).setForeground(c);
            setForeground(c);
        }
    }

    /**
     * DEPRECATED:  Replaced by getInsets().
     */
    public Insets insets() {
        return getInsets();
    }

    /*
     * Called from WCanvasPeer.displayChanged().
     * Since graphicsConfiguration for java.awt.Panels are never set, and there
     * are often many panels in a GUI, we can save some time by overriding this
     * method to do nothing.
     */
    void resetTargetGC() {}

    /**
     * Recursive method that handles the propagation of the displayChanged
     * event into the entire hierarchy of peers.  This ensures that
     * any heavyweights embedded in a lightweight hierarchy (e.g.,
     * a Canvas in the contentPane of a Swing JComponent) will
     * receive the message.
     */
    private void recursiveDisplayChanged(Component c) {
        ComponentPeer peer = c.getPeer();
        if (c instanceof Container && !(peer instanceof WPanelPeer)) {
            Component children[] = ((Container)c).getComponents();
            for (int i = 0; i < children.length; ++i) {
                recursiveDisplayChanged(children[i]);
            }
        }
        if (peer != null && peer instanceof WComponentPeer) {
            WComponentPeer wPeer = (WComponentPeer)peer;
            wPeer.displayChanged();
        }
    }

    /*
     * From the DisplayChangedListener interface.
     * Often up-called from a WWindowPeer instance.
     * Calls displayChanged() on all heavyweight childrens' peers.
     * Recurses into Container children to ensure all heavyweights
     * get the message.
     */
    public void displayChanged() {
        super.displayChanged();
        Component children[] = ((Container)target).getComponents();
        for (int i = 0; i < children.length; i++) {
            recursiveDisplayChanged(children[i]);
        }
    }
}

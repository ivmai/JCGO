/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MEmbeddedFramePeer.java  1.15 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.awt.Rectangle;

import sun.awt.EmbeddedFrame;

public class MEmbeddedFramePeer extends MFramePeer {

    public MEmbeddedFramePeer(EmbeddedFrame target) {
        super(target);
    }

    void create(MComponentPeer parent) {
        NEFcreate(parent, ((MEmbeddedFrame)target).handle);
    }

    public void setVisible(boolean on) {
        super.setVisible(on);
        if (on)
            synthesizeFocusIn();
    }

    Rectangle constrainBounds(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }

    native void NEFcreate(MComponentPeer parent, long handle);
    native void pShow();

    native void synthesizeFocusIn();
}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WEmbeddedFramePeer.java  1.12 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import sun.awt.EmbeddedFrame;

public class WEmbeddedFramePeer extends WFramePeer {

    public WEmbeddedFramePeer(EmbeddedFrame target) {
        super(target);
    }

    Rectangle constrainBounds(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }

    // force AWT into modal state
    native void pushModality();

    // release AWT from modal state
    native  void popModality();

    native void create(WComponentPeer parent);

    // suppress printing of an embedded frame.
    public void print(Graphics g) {}
}

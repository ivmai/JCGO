/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MDialogPeer.java 1.50 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.motif;

import java.util.Vector;
import java.awt.*;
import java.awt.peer.*;
import java.awt.event.*;
import sun.awt.motif.X11InputMethod;
import sun.awt.motif.MInputMethodControl;
import sun.awt.im.*;

class MDialogPeer extends MWindowPeer implements DialogPeer, MInputMethodControl {

    static Vector allDialogs = new Vector();

    MDialogPeer(Dialog target) {

        /* create MWindowPeer object */
        super();

        winAttr.nativeDecor = !target.isUndecorated();
        winAttr.initialFocus = true;
        winAttr.isResizable =  target.isResizable();
        winAttr.initialState = MWindowAttributes.NORMAL;
        winAttr.title = target.getTitle();
        winAttr.icon = null;
        if (winAttr.nativeDecor) {
            winAttr.decorations = winAttr.AWT_DECOR_ALL;
        } else {
            winAttr.decorations = winAttr.AWT_DECOR_NONE;
        }
        /* create and init native component */
        init(target);
        allDialogs.addElement(this);
    }

    public void setTitle(String title) {
        pSetTitle(title);
    }

    protected void disposeImpl() {
        allDialogs.removeElement(this);
        super.disposeImpl();
    }

    // NOTE: This method is called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleMoved(int x, int y) {
        postEvent(new ComponentEvent(target, ComponentEvent.COMPONENT_MOVED));
    }

    public void show() {
        pShowModal( ((Dialog)target).isModal() );
    }


    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleIconify() {
// Note: These routines are necessary for Coaleseing of native implementations
//       As Dialogs do not currently send Iconify/DeIconify messages but
//       Windows/Frames do.  If this should be made consistent...to do so
//       uncomment the postEvent.
//       postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_ICONIFIED));
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleDeiconify() {
// Note: These routines are necessary for Coaleseing of native implementations
//       As Dialogs do not currently send Iconify/DeIconify messages but
//       Windows/Frames do. If this should be made consistent...to do so
//       uncomment the postEvent.
//       postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_DEICONIFIED));
    }

    boolean isTargetUndecorated() {
        return ((Dialog)target).isUndecorated();
    }

}

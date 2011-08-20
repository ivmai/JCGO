/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MWindowPeer.java 1.49 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.motif;

import java.util.Vector;
import java.awt.*;
import java.awt.peer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import sun.awt.image.ImageRepresentation;
import sun.awt.motif.X11InputMethod;
import sun.awt.motif.MInputMethodControl;
import sun.awt.im.*;
import sun.awt.DisplayChangedListener;
import sun.awt.SunToolkit;

class MWindowPeer extends MPanelPeer implements WindowPeer,
 DisplayChangedListener {

    Insets insets = new Insets( 0, 0, 0, 0 );
    MWindowAttributes winAttr;
    static Vector allWindows = new Vector();
    int         iconWidth  = -1;
    int         iconHeight = -1;

    native void pCreate(MComponentPeer parent, String targetClassName);
    native void pShow();
    native void pShowModal(boolean isModal);
    native void pHide();
    native void pReshape(int x, int y, int width, int height);
    native void pDispose();
    native void pSetTitle(String title);
    public native void setState(int state);
    public native int getState();

    public native void setResizable(boolean resizable);
    native void addTextComponentNative(MComponentPeer tc);
    native void removeTextComponentNative();
    native void pSetIMMOption(String option);
    native void pSetMenuBar(MMenuBarPeer mbpeer);
    native void setSaveUnder(boolean state);


    private static native void initIDs();

    static {
        initIDs();
    }

    // this function is privileged! do not change it to public!
    private static int getInset(final String name, final int def) {
        Integer tmp = (Integer) java.security.AccessController.doPrivileged(
            new sun.security.action.GetIntegerAction(name, def));
        return tmp.intValue();
    }

    MWindowPeer() {
        insets = new Insets(0,0,0,0);
        winAttr = new MWindowAttributes();
    }

    MWindowPeer(Window target) {

        this();
        init(target);

        allWindows.addElement(this);
    }

    void create(MComponentPeer parent) {
        pCreate(parent, target.getClass().getName());
    }

    void init( Window target ) {
        if ( winAttr.nativeDecor == true ) {
                insets.top = getInset("awt.frame.topInset", -1);
                insets.left = getInset("awt.frame.leftInset", -1);
                insets.bottom = getInset("awt.frame.bottomInset", -1);
                insets.right = getInset("awt.frame.rightInset", -1);
        }

        Rectangle rectangle = target.getBounds();
        sysX = rectangle.x;
        sysY = rectangle.y;
        sysW = rectangle.width;
        sysH = rectangle.height;

        super.init(target);
        InputMethodManager imm = InputMethodManager.getInstance();
        String menuString = imm.getTriggerMenuString();
        if (menuString != null)
        {
          pSetIMMOption(menuString);
        }
        pSetTitle(winAttr.title);

        /*
         * For Windows and undecorated Frames and Dialogs this just
         * disables/enables resizing functions in the system menu.
         */
        setResizable(winAttr.isResizable);

        setSaveUnder(true);

        Font f = target.getFont();
        if (f == null) {
            f = defaultFont;
            target.setFont(f);
            setFont(f);
        }
        Color c = target.getBackground();
        if (c == null) {
            target.setBackground(SystemColor.window);
            setBackground(SystemColor.window);
        }
        c = target.getForeground();
        if (c == null) {
            target.setForeground(SystemColor.windowText);
            setForeground(SystemColor.windowText);
        }
    }

    protected void disposeImpl() {
        allWindows.removeElement(this);
        super.disposeImpl();
    }

    public native void toBack();

    public void toFront() {
        if (target.isVisible()) {
            pShow();
        }
    }

    public Insets getInsets() {
        return insets;
    }

    public void handleQuit() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_CLOSING));
    }

    // XXX: nasty WM, foul play.  spank WM author.
    public void handleDestroy() {
        final Window target = (Window)this.target;
        SunToolkit.executeOnEventHandlerThread(target,
            new Runnable() {
                public void run() {
                    // This seems like the only reasonable thing we
                    // could do in this situation as the native window
                    // is already dead.
                    target.dispose();
                }
            });
    }


    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleIconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_ICONIFIED));
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleDeiconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_DEICONIFIED));
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleStateChange(int oldState, int newState) {
        postEvent(new WindowEvent((Window)target,
                                  WindowEvent.WINDOW_STATE_CHANGED,
                                  oldState, newState));
    }

    /**
     * Called to inform the Window that its size has changed and it
     * should layout its children.
     */
    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleResize(int width, int height) {

      sysW = width;
      sysH = height;

      // REMIND: Is this secure? Can client code subclass input method?
      if (!tcList.isEmpty() &&
           !imList.isEmpty()){
        int i;
        for (i = 0; i < imList.size(); i++){
          ((X11InputMethod)imList.elementAt(i)).configureStatus();
        }
      }
        postEvent(new ComponentEvent(target, ComponentEvent.COMPONENT_RESIZED));
    }


    /**
     * DEPRECATED:  Replaced by getInsets().
     */
    public Insets insets() {
        return getInsets();
    }

    public void handleMoved(int x, int y) {
        sysX = x;
        sysY = y;

        postEvent(new ComponentEvent(target, ComponentEvent.COMPONENT_MOVED));
    }

    private native AWTEvent wrapInSequenced(AWTEvent event);

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleWindowFocusIn() {
      WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_GAINED_FOCUS);
      /* wrap in Sequenced, then post*/
      postEvent(wrapInSequenced((AWTEvent) we));
    }

    // NOTE: This method may be called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    public void handleWindowFocusOut(Window oppositeWindow) {
      WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_LOST_FOCUS,
                                       oppositeWindow);
      /* wrap in Sequenced, then post*/
      postEvent(wrapInSequenced((AWTEvent) we));
    }


// relocation of Imm stuff
    private Vector imList = new Vector();
    private Vector tcList = new Vector();

    // NOTE: This method is called by privileged threads.
    //       DO NOT INVOKE CLIENT CODE ON THIS THREAD!
    void notifyIMMOptionChange(){

      // REMIND: IS THIS SECURE? CAN USER CODE SUBCLASS INPUTMETHODMGR?
      InputMethodManager.getInstance().notifyChangeRequest(target);
    }

    public void addInputMethod(X11InputMethod im) {
        if (!imList.contains(im))
            imList.addElement(im);
    }

    public void removeInputMethod(X11InputMethod im) {
        if (imList.contains(im))
            imList.removeElement(im);
    }

    public void addTextComponent(MComponentPeer tc) {
        if (tcList.contains(tc))
            return;
        if (tcList.isEmpty()){
            addTextComponentNative(tc);
            if (!imList.isEmpty()) {
              for (int i = 0; i < imList.size(); i++) {
                ((X11InputMethod)imList.elementAt(i)).reconfigureXIC((MInputMethodControl)this);
              }
            }
            MToolkit.executeOnEventHandlerThread(target, new Runnable() {
                public void run() {
                    synchronized(target.getTreeLock()) {
                        target.doLayout();
                    }
                }
            });
        }
        tcList.addElement(tc);

    }

    public void removeTextComponent(MComponentPeer tc) {
        if (!tcList.contains(tc))
            return;
        tcList.removeElement(tc);
        if (tcList.isEmpty()){
            removeTextComponentNative();
            if (!imList.isEmpty()) {
              for (int i = 0; i < imList.size(); i++) {
                ((X11InputMethod)imList.elementAt(i)).reconfigureXIC((MInputMethodControl)this);
              }
            }
            target.doLayout();
        }
    }

    public MComponentPeer getTextComponent() {
        if (!tcList.isEmpty()) {
            return (MComponentPeer)tcList.firstElement();
        } else {
            return null;
        }
    }

    boolean hasDecorations(int decor) {
        if (!winAttr.nativeDecor) {
            return false;
        }
        else {
            int myDecor = winAttr.decorations;
            boolean hasBits = ((myDecor & decor) == decor);
            if ((myDecor & MWindowAttributes.AWT_DECOR_ALL) != 0)
                return !hasBits;
            else
                return hasBits;
        }
    }

    /* Returns the native paint should be posted after setting new size
     */
    public boolean checkNativePaintOnSetBounds(int width, int height) {
        // Fix for 4418155. Window does not repaint
        // automticaly if shrinking. Should not wait for Expose
        return (width > oldWidth) || (height > oldHeight);
    }

/* --- DisplayChangedListener Stuff --- */

    native void resetTargetGC(Component target);

    /* Xinerama
     * called to update our GC when dragged onto another screen
     */
    public void draggedToNewScreen(int screenNum) {
        final int finalScreenNum = screenNum;

        SunToolkit.executeOnEventHandlerThread((Component)target, new Runnable()
        {
            public void run() {
                displayChanged(finalScreenNum);
            }
        });
    }

    /* Xinerama
     * called to update our GC when dragged onto another screen
     */
    public void displayChanged(int screenNum) {
        // update our GC
        resetLocalGC(screenNum);         /* upcall to MCanvasPeer */
        resetTargetGC(target);           /* call Window.resetGC() via native */

        //propagate to children
        super.displayChanged(screenNum); /* upcall to MPanelPeer */
    }

    private volatile int sysX;
    private volatile int sysY;
    private volatile int sysW;
    private volatile int sysH;

    private final boolean hasWarningWindow() {
        return ((Window)target).getWarningString() != null;
    }

    boolean isTargetUndecorated() {
        return true;
    }

    public void setBounds(int x, int y, int width, int height) {
        Rectangle rectangle = constrainBounds(x, y, width, height);
        super.setBounds(rectangle.x, rectangle.y, rectangle.width,
         rectangle.height);
    }

    Rectangle constrainBounds(int i, int j, int k, int l) {
        if (!hasWarningWindow())
            return new Rectangle(i, j, k, l);

        int i1 = i;
        int j1 = j;
        int k1 = k;
        int l1 = l;
        GraphicsConfiguration graphicsconfiguration =
         ((Window)target).getGraphicsConfiguration();
        Rectangle rectangle = graphicsconfiguration.getBounds();
        Insets insets1 =
         ((Window)target).getToolkit().getScreenInsets(graphicsconfiguration);
        int i2 = rectangle.width - insets1.left - insets1.right;
        int j2 = rectangle.height - insets1.top - insets1.bottom;

        if (!target.isVisible() || isTargetUndecorated()) {
            int k2 = rectangle.x + insets1.left;
            int i3 = rectangle.y + insets1.top;
            if (k1 > i2)
                k1 = i2;
            if (l1 > j2)
                l1 = j2;
            if (i1 < k2) {
                i1 = k2;
            } else if (i1 + k1 > k2 + i2) {
                i1 = (k2 + i2) - k1;
            }
            if (j1 < i3) {
                j1 = i3;
            } else if (j1 + l1 > i3 + j2) {
                j1 = (i3 + j2) - l1;
            }
        } else {
            int l2 = Math.max(i2, sysW);
            int j3 = Math.max(j2, sysH);
            if (k1 > l2)
                k1 = l2;
            if (l1 > j3)
                l1 = j3;
        }

        return new Rectangle(i1, j1, k1, l1);
    }
}

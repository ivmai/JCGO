/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WWindowPeer.java 1.42 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.util.Vector;
import java.awt.*;
import java.awt.peer.*;
import java.awt.event.*;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsConfig;
import java.lang.ref.WeakReference;
import sun.awt.DebugHelper;
import sun.awt.DisplayChangedListener;
import sun.awt.SunToolkit;

public class WWindowPeer extends WPanelPeer implements WindowPeer {

    /**
     * Initialize JNI field IDs
     */
    private static native void initIDs();
    static {
        initIDs();
    }

    protected boolean focusableWindow; // value queried from native code

    private volatile int sysX;
    private volatile int sysY;
    private volatile int sysW;
    private volatile int sysH;

    // WComponentPeer overrides

    protected void disposeImpl() {
        // Remove ourself from the Map of DisplayChangeListeners
        GraphicsConfiguration gc = getGraphicsConfiguration();
        ((Win32GraphicsDevice)gc.getDevice()).removeDisplayChangedListener(this);
        allWindows.removeElement(this);
        super.disposeImpl();
    }

    // WindowPeer implementation

    public void toFront() {
        focusableWindow = ((Window)target).isFocusableWindow();
        _toFront();
    }
    native void _toFront();
    public native void toBack();

    private final boolean hasWarningWindow() {
        return ((Window)target).getWarningString() != null;
    }

    boolean isTargetUndecorated() {
        return true;
    }

    public void setBounds(int x, int y, int width, int height) {
        Rectangle rect = constrainBounds(x, y, width, height);
        sysX = rect.x;
        sysY = rect.y;
        sysW = rect.width;
        sysH = rect.height;
        super.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    Rectangle constrainBounds(int x, int y, int width, int height) {
       if (!hasWarningWindow())
           return new Rectangle(x, y, width, height);
       GraphicsConfiguration gconf =
           ((Window)target).getGraphicsConfiguration();
       Rectangle rect = gconf.getBounds();
       Insets insets = ((Window)target).getToolkit().getScreenInsets(gconf);
       int innerWidth = rect.width - insets.left - insets.right;
       int innerHeight = rect.height - insets.top - insets.bottom;
       if (!((Window)target).isVisible() || isTargetUndecorated()) {
           int x2 = rect.x + insets.left;
           int y2 = rect.y + insets.top;
           if (width > innerWidth)
               width = innerWidth;
           if (height > innerHeight)
               height = innerHeight;
           if (x < x2) {
               x = x2;
           } else {
              if (x + width > x2 + innerWidth)
                  x = x2 + innerWidth - width;
           }
           if (y < y2) {
               y = y2;
           } else {
               if (y + height > y2 + innerHeight)
                   y = y2 + innerHeight - height;
           }
       } else {
           innerWidth = Math.max(innerWidth, sysW);
           innerHeight = Math.max(innerHeight, sysH);
           if (width > innerWidth)
               width = innerWidth;
           if (height > innerHeight)
               height = innerHeight;
       }
       return new Rectangle(x, y, width, height);
    }

    // FramePeer & DialogPeer partial shared implementation

    public void setTitle(String title) {
        // allow a null title to pass as an empty string.
        if (title == null) {
            title = new String("");
        }
        _setTitle(title);
    }
    native void _setTitle(String title);

    public void setResizable(boolean resizable) {
        _setResizable(resizable);
    }
    public native void _setResizable(boolean resizable);

    // Toolkit & peer internals

    static final Vector allWindows = new Vector();  //!CQ for anchoring windows, frames, dialogs

    WWindowPeer(Window target) {
        super(target);
    }

    void initialize() {
        super.initialize();

        updateInsets(insets_);
        allWindows.addElement(this);

        Font f = ((Window)target).getFont();
        if (f == null) {
            f = defaultFont;
            ((Window)target).setFont(f);
            setFont(f);
        }
        // Express our interest in display changes
        GraphicsConfiguration gc = getGraphicsConfiguration();
        ((Win32GraphicsDevice)gc.getDevice()).addDisplayChangedListener(this);
    }

    native void createAwtWindow(WComponentPeer parent);
    void create(WComponentPeer parent) {
        createAwtWindow(parent);
    }

    public void show() {
        focusableWindow = ((Window)target).isFocusableWindow();
        super.show();
    }

    // Synchronize the insets members (here & in helper) with actual window
    // state.
    native void updateInsets(Insets i);

    private native Component getContainerElement(Container c, int i);

    static native int getSysMinWidth();
    static native int getSysMinHeight();

    synchronized native void reshapeFrame(int x, int y, int width, int height);

/*
 * ----DISPLAY CHANGE SUPPORT----
 */

    /*
     * Called from native code when we have been dragged onto another screen.
     */
    void draggedToNewScreen() {
        SunToolkit.executeOnEventHandlerThread((Component)target,new Runnable()
        {
            public void run() {
                displayChanged();
            }
        });
    }


    /*
     * Called from WCanvasPeer.displayChanged().
     * Override to do nothing - Window and WWindowPeer GC must never be set to
     * null!
     */
    void clearLocalGC() {}

    /*
     * Called from WCanvasPeer.displayChanged().
     * Reset the graphicsConfiguration member of our target Component.
     * Component.resetGC() is a package-private method, so we have to call it
     * through JNI.
     */
    native void resetTargetGC();

    /*
     * From the DisplayChangedListener interface
     *
     * This method handles a display change - either when the display settings
     * are changed, or when the window has been dragged onto a different
     * display.
     */
    public void displayChanged() {
        int scrn = getScreenImOn();

        // get current GD
        Win32GraphicsDevice oldDev = (Win32GraphicsDevice)winGraphicsConfig
                                     .getDevice();

        // get new GD
        Win32GraphicsDevice newDev = (Win32GraphicsDevice)GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices()[scrn];

        // Set winGraphicsConfig to the default GC for the monitor this Window
        // is now mostly on.
        winGraphicsConfig = (Win32GraphicsConfig)newDev
                            .getDefaultConfiguration();

        // if on a different display, take off old GD and put on new GD
        if (oldDev != newDev) {
            oldDev.removeDisplayChangedListener(this);
            newDev.addDisplayChangedListener(this);
        }
        super.displayChanged();
    }

    private native int getScreenImOn();

/*
 * ----END DISPLAY CHANGE SUPPORT----
 */
}

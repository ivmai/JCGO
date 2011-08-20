/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WToolkit.java    1.143 03/02/18
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.awt.*;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.*;
import java.awt.peer.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Clipboard;
import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.print.PageFormat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.image.ByteArrayImageSource;
import sun.awt.image.FileImageSource;
import sun.awt.image.URLImageSource;
import sun.awt.image.ImageRepresentation;
import sun.awt.print.PrintControl;
import sun.awt.AppContext;
import sun.awt.AWTAutoShutdown;
import sun.awt.EmbeddedFrame;
import sun.awt.GlobalCursorManager;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.awt.DisplayChangedListener;
import sun.awt.DebugHelper;
import sun.awt.datatransfer.DataTransferer;

import sun.print.PrintJob2D;

import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
 
import sun.awt.windows.WDragSourceContextPeer;

import sun.misc.PerformanceLogger;
import sun.security.action.GetPropertyAction;


public class WToolkit extends SunToolkit implements Runnable {
    private static final DebugHelper dbg = DebugHelper.create(WToolkit.class);

    static GraphicsConfiguration config;

    // System clipboard.
    WClipboard clipboard;

    // cache of font peers
    private Hashtable cacheFontPeer;

    // Windows properties
    private WDesktopProperties  wprops;

    // Dynamic Layout Resize client code setting
    protected boolean dynamicLayoutSetting = false;

    /**
     * Initialize JNI field and method IDs
     */
    private static native void initIDs();

    static {
        java.security.AccessController.doPrivileged(
                    new sun.security.action.LoadLibraryAction("awt"));
        Win32GraphicsEnvironment.initDisplayWrapper();
        initIDs();

        // Print out which version of Windows is running
        if (dbg.on) {
            printWindowsVersion();
        }

        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction()
        {
            public Object run() {
                String browserProp = System.getProperty("browser");
                if (browserProp != null && browserProp.equals("sun.plugin")) {
                    disableCustomPalette();
                }
                return null;
            }
        });
    }

    private static native void printWindowsVersion();
    private static native void disableCustomPalette();


    /*
     * Reset the static GraphicsConfiguration to the default.  Called on
     * startup and when display settings have changed.
     */
    public static void resetGC() {
        if (GraphicsEnvironment.isHeadless()) {
            config = null;
        } else {
          config = (GraphicsEnvironment
                  .getLocalGraphicsEnvironment()
          .getDefaultScreenDevice()
          .getDefaultConfiguration());
        }
    }

    /*
     * NOTE: The following embedded*() methods are non-public API intended 
     * for internal use only.  The methods are unsupported and could go 
     * away in future releases.
     *
     * New hook functions for using the AWT as an embedded service. These
     * functions replace the global C function AwtInit() which was previously
     * exported by awt.dll.
     *
     * When used as an embedded service, the AWT does NOT have its own
     * message pump. It instead relies on the parent application to provide
     * this functionality. embeddedInit() assumes that the thread on which it
     * is called is the message pumping thread. Violating this assumption
     * will lead to undefined behavior.
     *
     * embeddedInit must be called before the WToolkit() constructor.
     * embeddedDispose should be called before the applicaton terminates the
     * Java VM. It is currently unsafe to reinitialize the toolkit again
     * after it has been disposed. Instead, awt.dll must be reloaded and the
     * class loader which loaded WToolkit must be finalized before it is
     * safe to reuse AWT. Dynamic reusability may be added to the toolkit in
     * the future.
     */

    /**
     * Initializes the Toolkit for use in an embedded environment.
     *
     * @return true if the the initialization succeeded; false if it failed.
     *         The function will fail if the Toolkit was already initialized.
     * @since 1.3
     */
    public static native boolean embeddedInit();

    /**
     * Disposes the Toolkit in an embedded environment. This method should
     * not be called on exit unless the Toolkit was constructed with
     * embeddedInit.
     *
     * @return true if the disposal succeeded; false if it failed. The
     *         function will fail if the calling thread is not the same
     *         thread which called embeddedInit(), or if the Toolkit was
     *         already disposed.
     * @since 1.3
     */
    public static native boolean embeddedDispose();

    /**
     * To be called after processing the event queue by users of the above
     * embeddedInit() function.  The reason for this additional call is that
     * there are some operations performed during idle time in the AwtToolkit
     * event loop which should also be performed during idle time in any
     * other native event loop.  Failure to do so could result in
     * deadlocks.
     *
     * This method was added at the last minute of the jdk1.4 release
     * to work around a specific customer problem.  As with the above
     * embedded*() class, this method is non-public and should not be
     * used by external applications.
     *
     * See bug #4526587 for more information.
     */
    public native void embeddedEventLoopIdleProcessing();

    public static final String DATA_TRANSFERER_CLASS_NAME = "sun.awt.windows.WDataTransferer";

    public WToolkit() {
        // Startup toolkit threads
        /* if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("WToolkit construction");
        } */
        synchronized (this) {
            // Fix for bug #4046430 -- Race condition
            // where notifyAll can be called before
            // the "AWT-Windows" thread's parent thread is 
            // waiting, resulting in a deadlock on startup.
            Thread toolkitThread = new Thread(this, "AWT-Windows");
            toolkitThread.setDaemon(true);
            toolkitThread.setPriority(Thread.NORM_PRIORITY+1);

            /*
             * Fix for 4701990.
             * AWTAutoShutdown state must be changed before the toolkit thread
             * starts to avoid race condition.
             */
            AWTAutoShutdown.notifyToolkitThreadBusy();

            toolkitThread.start();
                                                  
            try {
                wait();
            }
            catch (InterruptedException x) {
            }
        }
        SunToolkit.setDataTransfererClassName(DATA_TRANSFERER_CLASS_NAME);
    }

    public void run() {
        boolean startPump = init();

        if (startPump) {
            ThreadGroup mainTG = (ThreadGroup)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        ThreadGroup currentTG =
                            Thread.currentThread().getThreadGroup();
                        ThreadGroup parentTG = currentTG.getParent();
                        while (parentTG != null) {
                            currentTG = parentTG;
                            parentTG = currentTG.getParent();
                        }
                        return currentTG;
                    }
            });

            Runtime.getRuntime().addShutdownHook(
                new Thread(mainTG, new Runnable() {
                    public void run() {
                        shutdown();
                    }
                })
            );
        }

        synchronized(this) {
            notifyAll();
        }

        if (startPump) {
            eventLoop(); // will Dispose Toolkit when shutdown hook executes
        }
    }

    /* 
     * eventLoop() begins the native message pump which retrieves and processes
     * native events.
     *
     * When shutdown() is called by the ShutdownHook added in run(), a
     * WM_QUIT message is posted to the Toolkit thread indicating that
     * eventLoop() should Dispose the toolkit and exit.
     */
    private native boolean init();
    private native void eventLoop();
    private native void shutdown();
    protected native void finalize(); // only called if runFinalizersOnExit

    /*
     * Instead of blocking the "AWT-Windows" thread uselessly on a semaphore,
     * use these functions. startSecondaryEventLoop() corresponds to wait()
     * and quitSecondaryEventLoop() corresponds to notify.
     *
     * These functions simulate blocking while allowing the AWT to continue
     * processing native events, eliminating a potential deadlock situation
     * with SendMessage.
     *
     * WARNING: startSecondaryEventLoop must only be called from the "AWT-
     * Windows" thread.
     */
    public static native void startSecondaryEventLoop();
    public static native void quitSecondaryEventLoop();

    /*
     * Create peer objects.
     */

    public ButtonPeer createButton(Button target) {
        ButtonPeer peer = new WButtonPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TextFieldPeer createTextField(TextField target) {
        TextFieldPeer peer = new WTextFieldPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public LabelPeer createLabel(Label target) {
        LabelPeer peer = new WLabelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ListPeer createList(List target) {
        ListPeer peer = new WListPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CheckboxPeer createCheckbox(Checkbox target) {
        CheckboxPeer peer = new WCheckboxPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollbarPeer createScrollbar(Scrollbar target) {
        ScrollbarPeer peer = new WScrollbarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollPanePeer createScrollPane(ScrollPane target) {
        ScrollPanePeer peer = new WScrollPanePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TextAreaPeer createTextArea(TextArea target) {
        TextAreaPeer peer = new WTextAreaPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ChoicePeer createChoice(Choice target) {
        ChoicePeer peer = new WChoicePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FramePeer  createFrame(Frame target) {
        FramePeer peer = new WFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CanvasPeer createCanvas(Canvas target) {
        CanvasPeer peer = new WCanvasPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public PanelPeer createPanel(Panel target) {
        PanelPeer peer = new WPanelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public WindowPeer createWindow(Window target) {
        WindowPeer peer = new WWindowPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public DialogPeer createDialog(Dialog target) {
        DialogPeer peer = new WDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FileDialogPeer createFileDialog(FileDialog target) {
        FileDialogPeer peer = new WFileDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuBarPeer createMenuBar(MenuBar target) {
        MenuBarPeer peer = new WMenuBarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuPeer createMenu(Menu target) {
        MenuPeer peer = new WMenuPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        PopupMenuPeer peer = new WPopupMenuPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuItemPeer createMenuItem(MenuItem target) {
        MenuItemPeer peer = new WMenuItemPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        CheckboxMenuItemPeer peer = new WCheckboxMenuItemPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
        // (target is unused for now)
        // Robot's don't need to go in the peer map since
        // they're not Component's
        return new WRobotPeer(screen);
    }

    public WEmbeddedFramePeer createEmbeddedFrame(WEmbeddedFrame target) {
        WEmbeddedFramePeer peer = new WEmbeddedFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    WPrintDialogPeer createWPrintDialog(WPrintDialog target) {
        WPrintDialogPeer peer = new WPrintDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    protected native void setDynamicLayoutNative(boolean b);

    public void setDynamicLayout(boolean b) {
        if (b == dynamicLayoutSetting) {
            return;
        }

        dynamicLayoutSetting = b;
        setDynamicLayoutNative(b);
    }

    protected boolean isDynamicLayoutSet() {
        return dynamicLayoutSetting;
    }

    /*
     * Called from lazilyLoadDynamicLayoutSupportedProperty because
     * Windows doesn't always send WM_SETTINGCHANGE when it should.
     */
    protected native boolean isDynamicLayoutSupportedNative();

    protected boolean isDynamicLayoutSupported() {
        Boolean dynamicSupported = (Boolean) Toolkit.getDefaultToolkit().
          getDesktopProperty("awt.dynamicLayoutSupported");

        // Do not cache this value - need to read this from the system
        // every time
        clearDesktopProperty("awt.dynamicLayoutSupported");

        if ((dynamicSupported == null) ||
            dynamicSupported.equals(Boolean.FALSE)) {
            return false;
        }
        return true;
    }

    public boolean isDynamicLayoutActive() {
        return (isDynamicLayoutSet() && isDynamicLayoutSupported());
    }

    /**
     * Returns <code>true</code> if this frame state is supported.
     */
    public boolean isFrameStateSupported(int state) {
        switch (state) {
          case Frame.NORMAL:
          case Frame.ICONIFIED:
          case Frame.MAXIMIZED_BOTH:
              return true;
          default:
              return false;
        }
    }

    static boolean prepareScrImage(Image img, int w, int h, ImageObserver o) {
        if (w == 0 || h == 0) {
            return true;
        }
        
        // Must be an OffScreenImage
        if (!(img instanceof WImage)) {
           return true;
        }

        WImage ximg = (WImage) img;
        if (ximg.hasError()) {
            if (o != null) {
                o.imageUpdate(img, ImageObserver.ERROR|ImageObserver.ABORT,
                              -1, -1, -1, -1);
            }
            return false;
        }
        ImageRepresentation ir = ximg.getImageRep();
        return ir.prepare(o);
    }

    static int checkScrImage(Image img, int w, int h, ImageObserver o) {
        if (!(img instanceof WImage)) {
            return ImageObserver.ALLBITS;
        }
        WImage ximg = (WImage) img;
        int repbits;
        if (w == 0 || h == 0) {
            repbits = ImageObserver.ALLBITS;
        }
        else {
            repbits = ximg.getImageRep().check(o);
        }
        return ximg.check(o) | repbits;
    }

    public int checkImage(Image img, int w, int h, ImageObserver o) {
        return checkScrImage(img, w, h, o);
    }

    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return prepareScrImage(img, w, h, o);
    }

    public Image createImage(ImageProducer producer) {
        return new WImage(producer);
    }

    static native ColorModel makeColorModel();
    static ColorModel screenmodel;

    static ColorModel getStaticColorModel() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException();
        }
        if (config == null) {
            resetGC();
        }
        return config.getColorModel();
    }

    public ColorModel getColorModel() {
        return getStaticColorModel();
    }

    public Insets getScreenInsets(GraphicsConfiguration gc)
    {
        return getScreenInsets(((Win32GraphicsDevice) gc.getDevice()).getScreen());
    }

    public native int getScreenResolution();
    protected native int getScreenWidth();
    protected native int getScreenHeight();
    protected native Insets getScreenInsets(int screen);


    public FontMetrics getFontMetrics(Font font) {
        // REMIND: platform font flag should be removed post-merlin.
        if (sun.awt.font.NativeFontWrapper.usePlatformFontMetrics()) {
            return WFontMetrics.getFontMetrics(font);
        }
        return super.getFontMetrics(font);
    }
    
    public FontPeer getFontPeer(String name, int style) {
        FontPeer retval = null;
        String lcName = name.toLowerCase();
        if (null != cacheFontPeer) {
            retval = (FontPeer)cacheFontPeer.get(lcName + style);
            if (null != retval) {
                return retval;
            }
        }
        retval = new WFontPeer(name, style);
        if (retval != null) {
            if (null == cacheFontPeer) {
                cacheFontPeer = new Hashtable(5, (float)0.9);
            }
            if (null != cacheFontPeer) {
                cacheFontPeer.put(lcName + style, retval);
            }
        }
        return retval;
    }

    public native void sync();

    public PrintJob getPrintJob(Frame frame, String doctitle,
                                Properties props) {
        return getPrintJob(frame, doctitle, null, null);
    }

    public PrintJob getPrintJob(Frame frame, String doctitle,
                                JobAttributes jobAttributes,
                                PageAttributes pageAttributes) {

        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException();
        }

        PrintJob2D printJob = new PrintJob2D(frame, doctitle,
                                             jobAttributes, pageAttributes);

        if (printJob.printDialog() == false) {
            printJob = null;
        }

        return printJob;
    }

    public native void beep();

    public boolean getLockingKeyState(int key) {
        if (! (key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK ||
               key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        return getLockingKeyStateNative(key);
    }

    public native boolean getLockingKeyStateNative(int key);

    public void setLockingKeyState(int key, boolean on) {
        if (! (key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK ||
               key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
        }
        setLockingKeyStateNative(key, on);
    }

    public native void setLockingKeyStateNative(int key, boolean on);

    public Clipboard getSystemClipboard() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkSystemClipboardAccess();
        }
        synchronized (this) {
            if (clipboard == null) {
                clipboard = new WClipboard();
            }
        }
        return clipboard;
    }

    protected native void loadSystemColors(int[] systemColors);

    public static final Object targetToPeer(Object target) {
        return SunToolkit.targetToPeer(target);
    }

    public static final void targetDisposedPeer(Object target, Object peer) {
        SunToolkit.targetDisposedPeer(target, peer);
    }

    /**
     * Returns a new input method adapter descriptor for native input methods.
     */
    public InputMethodDescriptor getInputMethodAdapterDescriptor() {
        return new WInputMethodDescriptor();
    }

    /**
     * Returns a style map for the input method highlight.
     */
    public Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        return WInputMethod.mapInputMethodHighlight(highlight);
    }

    /**
     * Returns whether enableInputMethods should be set to true for peered
     * TextComponent instances on this platform.
     */
    public boolean enableInputMethodsForTextComponent() {
        return true;
    }

    /**
     * Returns the default keyboard locale of the underlying operating system
     */
    public Locale getDefaultKeyboardLocale() {
        Locale locale = WInputMethod.getNativeLocale();

        if (locale == null) {
            return super.getDefaultKeyboardLocale();
        } else {
            return locale;
        }
    }

    /**
     * Returns a new custom cursor.
     */
    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name)
        throws IndexOutOfBoundsException {
        return new WCustomCursor(cursor, hotSpot, name);
    }

    /**
     * Returns the supported cursor size (Win32 only has one).
     */
    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) {
        return new Dimension(WCustomCursor.getCursorWidth(), 
                             WCustomCursor.getCursorHeight());
    }

    public native int getMaximumCursorColors();

    static void paletteChanged() {
        ((Win32GraphicsEnvironment)GraphicsEnvironment
        .getLocalGraphicsEnvironment())
        .paletteChanged();
    }

    /*
     * Called from Toolkit native code when a WM_DISPLAYCHANGE occurs.
     * Have Win32GraphicsEnvironment execute the display change code on the
     * Event thread.
     */
    static public void displayChanged() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                ((Win32GraphicsEnvironment)GraphicsEnvironment
                .getLocalGraphicsEnvironment())
                .displayChanged();
            }
        });
    }

    /**
     * create the peer for a DragSourceContext
     */
 
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        return WDragSourceContextPeer.createDragSourceContextPeer(dge);
    }

    public DragGestureRecognizer createDragGestureRecognizer(Class abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        if (MouseDragGestureRecognizer.class.equals(abstractRecognizerClass))
            return new WMouseDragGestureRecognizer(ds, c, srcActions, dgl);
        else
            return null;
    }

    /**
     *
     */

    private static final String prefix  = "DnD.Cursor.";
    private static final String postfix = ".32x32";
    private static final String awtPrefix  = "awt.";

    protected Object lazilyLoadDesktopProperty(String name) {
        if (name.startsWith(prefix)) {
            String cursorName = name.substring(prefix.length(),
              name.length()) + postfix;

            try {
                return Cursor.getSystemCustomCursor(cursorName);
            } catch (AWTException awte) {
                throw new RuntimeException("cannot load system cursor: " +
                  cursorName);
            }
        } else if (WDesktopProperties.isWindowsProperty(name) ||
                   name.startsWith(awtPrefix)) {
            synchronized(this) {
                if (wprops == null) {
                    wprops = new WDesktopProperties(this);
                } else {
                    // Only need to do this if wprops already existed,
                    // because in that case the value could be stale
                    if (name.equals("awt.dynamicLayoutSupported")) {
                        return lazilyLoadDynamicLayoutSupportedProperty(name);
                    }
                }

                // XXX do the same for "win.text.fontSmoothingOn" ? ?

                Object prop = wprops.getProperty(name);
                return prop;
            }
        }

        return super.lazilyLoadDesktopProperty(name);
    }

    /*
     * Called from lazilyLoadDesktopProperty because Windows doesn't
     * always send WM_SETTINGCHANGE when it should.
     */
    protected Boolean lazilyLoadDynamicLayoutSupportedProperty(String name) {
        boolean nativeDynamic = isDynamicLayoutSupportedNative();
        Boolean prop = (Boolean) wprops.getProperty(name);

        if (dbg.on) {
            dbg.print("In WTK.lazilyLoadDynamicLayoutSupportedProperty()" +
              "   nativeDynamic == " + nativeDynamic +
              "   wprops.dynamic == ");
            if (prop == null)
                dbg.println("null");
            else
                dbg.println(prop);
        }

        if ((prop == null) || (nativeDynamic != prop.booleanValue())) {
            // We missed the WM_SETTINGCHANGE, so we pretend
            // we just got one - fire the propertyChange, etc.
            windowsSettingChange();
            return new Boolean(nativeDynamic);
        }

        return prop;
    }

    /*
     * Called from native toolkit code when WM_SETTINGCHANGE message received
     * Also called from lazilyLoadDynamicLayoutSupportedProperty because
     * Windows doesn't always send WM_SETTINGCHANGE when it should.
     */
    private void windowsSettingChange() {
        //wprops created lazily, so may be null
        if (wprops != null) {
            wprops.firePropertyChanges();
        }
    }

    /*
     * Removes the desktop property from java.awt.Toolkit's Hashmap.
     * Used to force the value to be reloaded from WDesktopProperties the
     * next time its value is requested via Toolkit:getDesktopProperty().
     */
    synchronized void clearDesktopProperty(String name) {
        desktopProperties.remove(name);
    }

    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        if ( WDesktopProperties.isWindowsProperty(name) ) {
        if (wprops == null) {
            wprops = new WDesktopProperties(this);
        }
            wprops.addPropertyChangeListener(name, pcl);
        } else {
            super.addPropertyChangeListener(name, pcl);
        }
    }

    public synchronized void removePropertyChangeListener(String name, PropertyChangeListener pcl) {
        if ( WDesktopProperties.isWindowsProperty(name) ) {
        //wprops created lazily, so may be null
        if (wprops != null) {
            wprops.removePropertyChangeListener(name, pcl);
        }
        } else {
            super.removePropertyChangeListener(name, pcl);
        }
    }

    protected void initializeDesktopProperties() {
        desktopProperties.put("DnD.Autoscroll.initialDelay",     new Integer(50));
        desktopProperties.put("DnD.Autoscroll.interval",         new Integer(50));
        //desktopProperties.put("DnD.Autoscroll.cursorHysteresis", new Integer(5));
        // DnD uses one value for x and y drag diff, but Windows provides separate ones.
        // For now, just use the x value - rnk

        wprops = new WDesktopProperties(this);

        desktopProperties.put("DnD.Autoscroll.cursorHysteresis", wprops.getProperty("win.drag.x"));


        // This property access is duplicated from sun.awt.shell.ShellFolder but must
        // be here for initialization ordering purposes
        try {
            String prop = (String)AccessController.doPrivileged(
                                new GetPropertyAction("swing.disableFileChooserSpeedFix"));
            if (prop != null && !prop.equalsIgnoreCase("false")) {
                desktopProperties.put("Shell.shellFolderManager",
                                      Class.forName("sun.awt.shell.Win32ShellFolderManager"));
            } else {
                desktopProperties.put("Shell.shellFolderManager",
                                      Class.forName("sun.awt.shell.Win32ShellFolderManager2"));
            }
        } catch (ClassNotFoundException ex) {
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // The following is used by the Java Plug-in to coordinate dialog modality
    // between containing applications (browsers, ActiveX containers etc) and
    // the AWT.
    //
    ///////////////////////////////////////////////////////////////////////////

    private ModalityListenerList        modalityListeners = new ModalityListenerList();

    public static WToolkit getWToolkit() {
        WToolkit toolkit = (WToolkit)Toolkit.getDefaultToolkit();
        return toolkit;
    }

    /**
     * Pushes AWT into another level of modality. Used by Java Plug-in when
     * the containing application puts up a modal dialog of it's own
     */
    public native void pushModality();

    /**
     * Called by Java plug-in when containing app dismisses one of it's
     * own dialogs
     */
    public native void popModality();

    /**
     * Adds a listener to be notified of changes in global modality
     */
    public void addModalityListener(ModalityListener listener) {
        modalityListeners.add(listener);
    }
    
    /**
     * Removes modality listener
     */
    public void removeModalityListener(ModalityListener listener) {
        modalityListeners.remove(listener);
    }
    
    /**
     * Call to send modality events to interested parties
     */
    final void notifyModalityChange(int id) {
        ModalityEvent ev = new ModalityEvent(this, modalityListeners, id);
        ev.dispatch();
    }
        
    class ModalityListenerList implements ModalityListener {
        Vector  listeners = new Vector();
        
        void add(ModalityListener listener) {
            listeners.addElement(listener);
        }
        
        void remove(ModalityListener listener) {
            listeners.removeElement(listener);
        }
        
        //
        // ModalityListener implementation
        //
        public void modalityPushed(ModalityEvent ev) {
            Enumeration enum = listeners.elements();
            while (enum.hasMoreElements()) {
                ((ModalityListener)enum.nextElement()).modalityPushed(ev);
            }
        }
        
        public void modalityPopped(ModalityEvent ev) {
            Enumeration enum = listeners.elements();
            while (enum.hasMoreElements()) {
                ((ModalityListener)enum.nextElement()).modalityPopped(ev);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // End Plug-in code
    ///////////////////////////////////////////////////////////////////////////
}


/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SunToolkit.java  1.76 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.peer.*;
import java.awt.event.WindowEvent;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.net.URL;
import java.net.JarURLConnection;
import java.security.AccessController;
import java.util.*;
import sun.misc.SoftCache;
import sun.awt.font.FontDesignMetrics;
import sun.awt.im.InputContext;
import sun.awt.im.SimpleInputMethodWindow;
import sun.awt.image.*;
import sun.awt.font.NativeFontWrapper;
import sun.security.action.GetPropertyAction;
import java.lang.reflect.Constructor;

public abstract class SunToolkit extends Toolkit
    implements WindowClosingSupport, WindowClosingListener,
    ComponentFactory, InputMethodSupport {

    /* Force the debug helper classes to initialize */
    {
        DebugHelper.init();
    }

    private static boolean hotjavaUrlCache = false; // REMIND: UGH!!!!!

    /* The key to put()/get() the PostEventQueue into/from the AppContext.
     */
    private static final String POST_EVENT_QUEUE_KEY = "PostEventQueue";

    public SunToolkit() {
        /* If awt.threadgroup is set to class name the instance of
         * this class is created (should be subclass of ThreadGroup)
         * and EventDispatchThread is created inside of it
         *
         * If loaded class overrides uncaughtException instance
         * handles all uncaught exception on EventDispatchThread
         */
        ThreadGroup threadGroup = null;
        String tgName = System.getProperty("awt.threadgroup", "");

        if (tgName.length() != 0) {
            try {
                Constructor ctor = Class.forName(tgName).
                    getConstructor(new Class[] {String.class});
                threadGroup = (ThreadGroup)ctor.newInstance(new Object[] {"AWT-ThreadGroup"});
            } catch (Exception e) {
                System.err.println("Failed loading " + tgName + ": " + e);
            }
        }

        Runnable initEQ = new Runnable() {
            public void run () {
                EventQueue eventQueue;

                String eqName = Toolkit.getProperty("AWT.EventQueueClass",
                                                    "java.awt.EventQueue");

                try {
                    eventQueue = (EventQueue)Class.forName(eqName).newInstance();
                } catch (Exception e) {
                    System.err.println("Failed loading " + eqName + ": " + e);
                    eventQueue = new EventQueue();
                }
                AppContext appContext = AppContext.getAppContext();
                appContext.put(AppContext.EVENT_QUEUE_KEY, eventQueue);

                PostEventQueue postEventQueue = new PostEventQueue(eventQueue);
                appContext.put(POST_EVENT_QUEUE_KEY, postEventQueue);
            }
        };

        if (threadGroup != null) {
            Thread eqInitThread = new Thread(threadGroup, initEQ, "EventQueue-Init");
            eqInitThread.start();
            try {
                eqInitThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            initEQ.run();
        }
    }

    public abstract WindowPeer createWindow(Window target)
        throws HeadlessException;

    public abstract FramePeer createFrame(Frame target)
        throws HeadlessException;

    public abstract DialogPeer createDialog(Dialog target)
        throws HeadlessException;

    public abstract ButtonPeer createButton(Button target)
        throws HeadlessException;

    public abstract TextFieldPeer createTextField(TextField target)
        throws HeadlessException;

    public abstract ChoicePeer createChoice(Choice target)
        throws HeadlessException;

    public abstract LabelPeer createLabel(Label target)
        throws HeadlessException;

    public abstract ListPeer createList(java.awt.List target)
        throws HeadlessException;

    public abstract CheckboxPeer createCheckbox(Checkbox target)
        throws HeadlessException;

    public abstract ScrollbarPeer createScrollbar(Scrollbar target)
        throws HeadlessException;

    public abstract ScrollPanePeer createScrollPane(ScrollPane target)
        throws HeadlessException;

    public abstract TextAreaPeer createTextArea(TextArea target)
        throws HeadlessException;

    public abstract FileDialogPeer createFileDialog(FileDialog target)
        throws HeadlessException;

    public abstract MenuBarPeer createMenuBar(MenuBar target)
        throws HeadlessException;

    public abstract MenuPeer createMenu(Menu target)
        throws HeadlessException;

    public abstract PopupMenuPeer createPopupMenu(PopupMenu target)
        throws HeadlessException;

    public abstract MenuItemPeer createMenuItem(MenuItem target)
        throws HeadlessException;

    public abstract CheckboxMenuItemPeer createCheckboxMenuItem(
        CheckboxMenuItem target)
        throws HeadlessException;

    public abstract DragSourceContextPeer createDragSourceContextPeer(
        DragGestureEvent dge)
        throws InvalidDnDOperationException;

    public abstract FontPeer getFontPeer(String name, int style);

    public abstract RobotPeer createRobot(Robot target, GraphicsDevice screen)
        throws AWTException;

    /*
     * Create a new AppContext, along with its EventQueue, for a
     * new ThreadGroup.  Browser code, for example, would use this
     * method to create an AppContext & EventQueue for an Applet.
     */
    public static AppContext createNewAppContext() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        EventQueue eventQueue;
        String eqName = Toolkit.getProperty("AWT.EventQueueClass",
                                            "java.awt.EventQueue");
        try {
            eventQueue = (EventQueue)Class.forName(eqName).newInstance();
        } catch (Exception e) {
            System.err.println("Failed loading " + eqName + ": " + e);
            eventQueue = new EventQueue();
        }
        AppContext appContext = new AppContext(threadGroup);
        appContext.put(AppContext.EVENT_QUEUE_KEY, eventQueue);

        PostEventQueue postEventQueue = new PostEventQueue(eventQueue);
        appContext.put(POST_EVENT_QUEUE_KEY, postEventQueue);

        return appContext;
    }

    private static native Object getPrivateKey(Object o);

    static native void wakeupEventQueue(EventQueue q, boolean isShutdown);

    // mapping of components to peers, Hashtable<Component,Peer>
    protected static final Hashtable peerMap =
        AWTAutoShutdown.getInstance().getPeerMap();

    /*
     * Fetch the peer associated with the given target (as specified
     * in the peer creation method).  This can be used to determine
     * things like what the parent peer is.  If the target is null
     * or the target can't be found (either because the a peer was
     * never created for it or the peer was disposed), a null will
     * be returned.
     */
    protected static Object targetToPeer(Object target) {
        if (target != null && !GraphicsEnvironment.isHeadless()) {
            return peerMap.get(getPrivateKey(target));
        }
        return null;
    }

    protected static void targetCreatedPeer(Object target, Object peer) {
        if (target != null && peer != null &&
            !GraphicsEnvironment.isHeadless()) {
            peerMap.put(getPrivateKey(target), peer);
        }
    }

    protected static void targetDisposedPeer(Object target, Object peer) {
        if (target != null && peer != null &&
            !GraphicsEnvironment.isHeadless()) {
            Object key = getPrivateKey(target);
            if (peerMap.get(key) == peer) {
                peerMap.remove(key);
            }
        }
    }

    // mapping of Components to AppContexts, WeakHashMap<Component,AppContext>
    private static final Map appContextMap =
        Collections.synchronizedMap(new WeakHashMap());

    /*
     * Fetch the AppContext associated with the given target.
     * This can be used to determine things like which EventQueue
     * to use for posting events to a Component.  If the target is
     * null or the target can't be found, a null with be returned.
     */
    public static AppContext targetToAppContext(Object target) {
        if (target != null && !GraphicsEnvironment.isHeadless()) {
            return (AppContext)appContextMap.get(getPrivateKey(target));
        }
        return null;
    }

     /**
      * Sets the synchronous status of focus requests on lightweight
      * components in the specified window to the specified value.
      * If the boolean parameter is <code>true</code> then the focus
      * requests on lightweight components will be performed
      * synchronously, if it is <code>false</code>, then asynchronously.
      * By default, all windows have their lightweight request status
      * set to asynchronous.
      * <p>
      * The application can only set the status of lightweight focus
      * requests to synchronous for any of its windows if it doesn't
      * perform focus transfers between different heavyweight containers.
      * In this case the observable focus behaviour is the same as with
      * asynchronous status.
      * <p>
      * If the application performs focus transfer between different
      * heavyweight containers and sets the lightweight focus request
      * status to synchronous for any of its windows, then further focus
      * behaviour is unspecified.
      * <p>
      * @param    w window for which the lightweight focus request status
      *             should be set
      * @param    status the value of lightweight focus request status
      */
    public static native void setLWRequestStatus(Window changed, boolean status);


     /**
      * Moves the specified component to the specified order index in the
      * specified container.
      * <p>
      * If the component already exists in this container or a child of
      * this container, it is removed from that container before being
      * added to this container.
      * <p>
      * The important difference of this method from
      * <p>
      * <blockquote>
      * <code>java.awt.Container.add(Component comp, int index)</code>
      * </blockquote>
      * <p>
      * is that this method doesn't call <code>removeNotify</code> on
      * comp while removing it from its previous container. This way,
      * if the component is lightweight and has focus, it will be moved
      * to the new position and keep the focus when the method returns.
      * <p>
      * This method should not be used to move the component from one
      * heavyweight container to another, and it should not be used for
      * heavyweight components. If such an attempt is made, the result
      * is unspecified.
      * <p>
      * It is recommended to call this method on EventDispatchThread.
      * <p>
      * @param     cont the container to which the component should be added
      * @param     comp the component that should be added
      * @param     order the position in the container's list at which to
      *              insert the component, where <code>-1</code> means
      *              append to the end.
      * @exception IllegalArgumentException if <code>index</code> is invalid
      * @exception IllegalArgumentException if adding the container's parent
      *                  to itself
      * @exception IllegalArgumentException if adding a window to a container
      */
    public static native void setZOrder(Container cont, Component comp, int order);

    /*
     * Insert a mapping from target to AppContext, for later retrieval
     * via targetToAppContext() above.
     */
    public static void insertTargetMapping(Object target, AppContext appContext) {
        if (!GraphicsEnvironment.isHeadless()) {
            appContextMap.put(getPrivateKey(target), appContext);
        }
    }

    /*
     * Post an AWTEvent to the Java EventQueue, using the PostEventQueue
     * to avoid possibly calling client code (EventQueueSubclass.postEvent())
     * on the toolkit (AWT-Windows/AWT-Motif) thread.  This function should
     * not be called under another lock since it locks the EventQueue.
     * See bugids 4632918, 4526597.
     */
    public static void postEvent(AppContext appContext, AWTEvent event) {
        if (event == null) {
            throw new NullPointerException();
        }
        PostEventQueue postEventQueue =
            (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if(postEventQueue != null) {
            postEventQueue.postEvent(event);
        }
    }

    /*
     * Flush any pending events which haven't been posted to the AWT
     * EventQueue yet.
     */
    public static void flushPendingEvents()  {
        AppContext appContext = AppContext.getAppContext();
        PostEventQueue postEventQueue =
            (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if(postEventQueue != null) {
            postEventQueue.flush();
        }
    }

    public static boolean isPostEventQueueEmpty()  {
        AppContext appContext = AppContext.getAppContext();
        PostEventQueue postEventQueue =
            (PostEventQueue)appContext.get(POST_EVENT_QUEUE_KEY);
        if (postEventQueue != null) {
            return postEventQueue.noEvents();
        } else {
            return true;
        }
    }

    /*
     * Execute a chunk of code on the Java event handler thread for the
     * given target.  Does not wait for the execution to occur before
     * returning to the caller.
     */
    public static void executeOnEventHandlerThread(Object target,
                                                   Runnable runnable) {
        executeOnEventHandlerThread(new PeerEvent(target, runnable, PeerEvent.PRIORITY_EVENT));
    }

    /*
     * Execute a chunk of code on the Java event handler thread for the
     * given target.  Does not wait for the execution to occur before
     * returning to the caller.
     */
    public static void executeOnEventHandlerThread(PeerEvent peerEvent) {
        postEvent(targetToAppContext(peerEvent.getSource()), peerEvent);
    }

    public Dimension getScreenSize() {
        return new Dimension(getScreenWidth(), getScreenHeight());
    }
    protected abstract int getScreenWidth();
    protected abstract int getScreenHeight();

    public static final FontMetrics[] lastMetrics = new FontMetrics[5];

    public FontMetrics getFontMetrics(Font font) {
        for (int i = 0; i < lastMetrics.length; i++) {
            FontMetrics lm = lastMetrics[i];
            if (lm == null) {
                break;
            }
            if (lm.getFont() == font) {
                return lm;
            }
        }
        FontMetrics lm = new FontDesignMetrics(font);
        System.arraycopy(lastMetrics, 0, lastMetrics, 1, lastMetrics.length-1);
        lastMetrics[0] = lm;
        return lm;
    }

    public String[] getFontList() {
        String[] hardwiredFontList = {
            "Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"

            // -- Obsolete font names from 1.0.2.  It was decided that
            // -- getFontList should not return these old names:
            //    "Helvetica", "TimesRoman", "Courier", "ZapfDingbats"
        };
        return hardwiredFontList;
    }

    public PanelPeer createPanel(Panel target) {
        return (PanelPeer)createComponent(target);
    }

    public CanvasPeer createCanvas(Canvas target) {
        return (CanvasPeer)createComponent(target);
    }

    static SoftCache imgCache = new SoftCache();

    static synchronized java.awt.Image getImageFromHash(Toolkit tk, URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                java.security.Permission perm =
                    url.openConnection().getPermission();
                if (perm != null) {
                    try {
                        sm.checkPermission(perm);
                    } catch (SecurityException se) {
                        // fallback to checkRead/checkConnect for pre 1.2
                        // security managers
                        if ((perm instanceof java.io.FilePermission) &&
                            perm.getActions().indexOf("read") != -1) {
                            sm.checkRead(perm.getName());
                        } else if ((perm instanceof
                            java.net.SocketPermission) &&
                            perm.getActions().indexOf("connect") != -1) {
                            sm.checkConnect(url.getHost(), url.getPort());
                        } else {
                            throw se;
                        }
                    }
                }
            } catch (java.io.IOException ioe) {
                    sm.checkConnect(url.getHost(), url.getPort());
            }
        }
        java.awt.Image img = (java.awt.Image)imgCache.get(url);
        if (img == null) {
            img = tk.createImage(new URLImageSource(url));
            imgCache.put(url, img);
        }
        return img;
    }

    static synchronized java.awt.Image getImageFromHash(Toolkit tk,
                                                        String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
        java.awt.Image img = (java.awt.Image)imgCache.get(filename);
        if (img == null) {
            img = tk.createImage(new FileImageSource(filename));
            imgCache.put(filename, img);
        }
        return img;
    }

    public java.awt.Image getImage(String filename) {
        return getImageFromHash(this, filename);
    }

    public java.awt.Image getImage(URL url) {
        return getImageFromHash(this, url);
    }

    public java.awt.Image createImage(String filename) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkRead(filename);
        }
        return createImage(new FileImageSource(filename));
    }

    public java.awt.Image createImage(URL url) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                java.security.Permission perm =
                    url.openConnection().getPermission();
                if (perm != null) {
                    try {
                        sm.checkPermission(perm);
                    } catch (SecurityException se) {
                        // fallback to checkRead/checkConnect for pre 1.2
                        // security managers
                        if ((perm instanceof java.io.FilePermission) &&
                            perm.getActions().indexOf("read") != -1) {
                            sm.checkRead(perm.getName());
                        } else if ((perm instanceof
                            java.net.SocketPermission) &&
                            perm.getActions().indexOf("connect") != -1) {
                            sm.checkConnect(url.getHost(), url.getPort());
                        } else {
                            throw se;
                        }
                    }
                }
            } catch (java.io.IOException ioe) {
                    sm.checkConnect(url.getHost(), url.getPort());
            }
        }
        return createImage(new URLImageSource(url));
    }

    public java.awt.Image createImage(byte[] data, int offset, int length) {
        return createImage(new ByteArrayImageSource(data, offset, length));
    }

    protected EventQueue getSystemEventQueueImpl() {
        return getSystemEventQueueImplPP();
    }

    // Package private implementation
    static EventQueue getSystemEventQueueImplPP() {
        AppContext appContext = AppContext.getAppContext();
        EventQueue theEventQueue =
            (EventQueue)appContext.get(AppContext.EVENT_QUEUE_KEY);
        return theEventQueue;
    }

    /**
     * Give native peers the ability to query the native container
     * given a native component (eg the direct parent may be lightweight).
     */
    public static Container getNativeContainer(Component c) {
        return Toolkit.getNativeContainer(c);
    }

    /**
     * Returns a new input method window, with behavior as specified in
     * {@link java.awt.im.spi.InputMethodContext#createInputMethodWindow}.
     * If the inputContext is not null, the window should return it from its
     * getInputContext() method. The window needs to implement
     * sun.awt.im.InputMethodWindow.
     * <p>
     * SunToolkit subclasses can override this method to return better input
     * method windows.
     */
    public Window createInputMethodWindow(String title, InputContext context) {
        return new sun.awt.im.SimpleInputMethodWindow(title, context);
    }

    /**
     * Returns whether enableInputMethods should be set to true for peered
     * TextComponent instances on this platform. False by default.
     */
    public boolean enableInputMethodsForTextComponent() {
        return false;
    }

    private static Locale startupLocale = null;

    /**
     * Returns the locale in which the runtime was started.
     */
    public static Locale getStartupLocale() {
        if (startupLocale == null) {
            String language, region, country, variant;
            language = (String) AccessController.doPrivileged(
                            new GetPropertyAction("user.language", "en"));
            // for compatibility, check for old user.region property
            region = (String) AccessController.doPrivileged(
                            new GetPropertyAction("user.region"));
            if (region != null) {
                // region can be of form country, country_variant, or _variant
                int i = region.indexOf('_');
                if (i >= 0) {
                    country = region.substring(0, i);
                    variant = region.substring(i + 1);
                } else {
                    country = region;
                    variant = "";
                }
            } else {
                country = (String) AccessController.doPrivileged(
                                new GetPropertyAction("user.country", ""));
                variant = (String) AccessController.doPrivileged(
                                new GetPropertyAction("user.variant", ""));
            }
            startupLocale = new Locale(language, country, variant);
        }
        return startupLocale;
    }

    /**
     * Returns the default keyboard locale of the underlying operating system
     */
    public Locale getDefaultKeyboardLocale() {
        return getStartupLocale();
    }

    private static String dataTransfererClassName = null;

    protected static void setDataTransfererClassName(String className) {
        dataTransfererClassName = className;
    }

    public static String getDataTransfererClassName() {
        if (dataTransfererClassName == null) {
            Toolkit.getDefaultToolkit(); // transferer set during toolkit init
        }
        return dataTransfererClassName;
    }

    // Support for window closing event notifications
    private transient WindowClosingListener windowClosingListener = null;
    /**
     * @see sun.awt.WindowClosingSupport#getWindowClosingListener
     */
    public WindowClosingListener getWindowClosingListener() {
        return windowClosingListener;
    }
    /**
     * @see sun.awt.WindowClosingSupport#setWindowClosingListener
     */
    public void setWindowClosingListener(WindowClosingListener wcl) {
        windowClosingListener = wcl;
    }

    /**
     * @see sun.awt.WindowClosingListener#windowClosingNotify
     */
    public RuntimeException windowClosingNotify(WindowEvent event) {
        if (windowClosingListener != null) {
            return windowClosingListener.windowClosingNotify(event);
        } else {
            return null;
        }
    }
    /**
     * @see sun.awt.WindowClosingListener#windowClosingDelivered
     */
    public RuntimeException windowClosingDelivered(WindowEvent event) {
        if (windowClosingListener != null) {
            return windowClosingListener.windowClosingDelivered(event);
        } else {
            return null;
        }
    }

} // class SunToolkit



/*
 * PostEventQueue is a Thread that runs in the same AppContext as the
 * Java EventQueue.  It is a queue of AWTEvents to be posted to the
 * Java EventQueue.  The toolkit Thread (AWT-Windows/AWT-Motif) posts
 * events to this queue, which then calls EventQueue.postEvent().
 *
 * We do this because EventQueue.postEvent() may be overridden by client
 * code, and we mustn't ever call client code from the toolkit thread.
 */
class PostEventQueue {
    private EventQueueItem queueHead = null;
    private EventQueueItem queueTail = null;
    private final EventQueue eventQueue;

    PostEventQueue(EventQueue eq) {
        eventQueue = eq;
    }

    public boolean noEvents() {
        return queueHead == null;
    }

    /*
     * Continually post pending AWTEvents to the Java EventQueue.
     */
    public void flush() {
        if (queueHead != null) {
            EventQueueItem tempQueue;
            /*
             * We have to execute the loop inside the synchronized block
             * to ensure that the flush is completed before a new event
             * can be posted to this queue.
             */
            synchronized (this) {
                tempQueue = queueHead;
                queueHead = queueTail = null;
                /*
                 * If this PostEventQueue is flushed in parallel on two
                 * different threads tempQueue will be null for one of them.
                 */
                while (tempQueue != null) {
                    eventQueue.postEvent(tempQueue.event);
                    tempQueue = tempQueue.next;
                }
            }
        }
    }

    /*
     * Enqueue an AWTEvent to be posted to the Java EventQueue.
     */
    void postEvent(AWTEvent event) {
        EventQueueItem item = new EventQueueItem(event);

        synchronized (this) {
            if (queueHead == null) {
                queueHead = queueTail = item;
            } else {
                queueTail.next = item;
                queueTail = item;
            }
        }
        SunToolkit.wakeupEventQueue(eventQueue, event.getSource() == AWTAutoShutdown.getInstance());
    }
} // class PostEventQueue

class EventQueueItem {
    AWTEvent event;
    EventQueueItem next;

    EventQueueItem(AWTEvent evt) {
        event = evt;
    }
} // class EventQueueItem

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WComponentPeer.java      1.137 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.awt.*;
import java.awt.peer.*;
import java.awt.image.MemoryImageSource;
import java.awt.image.WritableRaster;
import java.awt.image.VolatileImage;
import sun.awt.RepaintArea;
import sun.awt.AppContext;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.OffScreenImage;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.ImageObserver;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.event.PaintEvent;
import sun.awt.Win32GraphicsConfig;
import sun.awt.Win32GraphicsDevice;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.awt.DisplayChangedListener;

import java.awt.dnd.DropTarget;
import java.awt.dnd.peer.DropTargetPeer;

import sun.awt.DebugHelper;

public abstract class WComponentPeer extends WObjectPeer
    implements ComponentPeer, DropTargetPeer, DisplayChangedListener
{

    private static final boolean ddoffscreen = ddoffscreen();

    static {
        wheelInit();
    }

    private static boolean ddoffscreen() {
        String ddoffscreenProp =
            (String)java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddoffscreen"));
        if (ddoffscreenProp != null) {
            if (ddoffscreenProp.equals("true") || ddoffscreenProp.equals("t"))
                return true;
            if (ddoffscreenProp.equals("false") || ddoffscreenProp.equals("f"))
                System.out.println("Disabling offscreen DirectDraw acceleration");
        }
        return false;
    }

    // Only actually does stuff if running on 95
    native static void wheelInit();

    // ComponentPeer implementation
    Win32SurfaceData surfaceData;

    private RepaintArea paintArea;

    protected Win32GraphicsConfig winGraphicsConfig;

    boolean isLayouting = false;
    boolean paintPending = false;
    int     oldWidth = -1;
    int     oldHeight = -1;
    private int numBackBuffers = 0;
    private Win32BackBuffer backBuffer = null;

    public native boolean isObscured();
    public boolean canDetermineObscurity() { return true; }

    // DropTarget support

    int nDropTargets;
    long nativeDropTargetContext; // native pointer

    public synchronized native void pShow();
    public synchronized native void hide();
    public synchronized native void enable();
    public synchronized native void disable();

    /* New 1.1 API */
    public native Point getLocationOnScreen();

    /* New 1.1 API */
    public void setVisible(boolean b) {
        if (b) {
            show();
        } else {
            hide();
        }
    }

    public void show() {
        Dimension s = ((Component)target).getSize();
        oldHeight = s.height;
        oldWidth = s.width;
        pShow();
    }

    /* New 1.1 API */
    public void setEnabled(boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }

    public int serialNum = 0;

    /* New 1.1 API */
    public void setBounds(int x, int y, int width, int height) {
        // Should set paintPending before reahape to prevent
        // thread race between paint events
        // Native components do redraw after resize
        paintPending = (width != oldWidth) || (height != oldHeight);

        reshape(x, y, width, height);
        if ((width != oldWidth) || (height != oldHeight)) {
            // Only recreate surfaceData if this setBounds is called
            // for a resize; a simple move should not trigger a recreation
            try {
                replaceSurfaceData();
            } catch (InvalidPipeException e) {
                // REMIND : what do we do if our surface creation failed?
            }
            oldWidth = width;
            oldHeight = height;
        }

        serialNum++;
    }

    /*
     * Called from native code (on Toolkit thread) in order to
     * dynamically layout the Container during resizing
     */
    void dynamicallyLayoutContainer() {
        // If we got the WM_SIZING, this must be a Container, right?
        // In fact, it must be the top-level Container.
        final Container cont = (Container)target;

        WToolkit.executeOnEventHandlerThread(cont, new Runnable() {
            public void run() {
                // Discarding old paint events doesn't seem to be necessary.
                cont.invalidate();
                cont.validate();
                // Forcing a paint here doesn't seem to be necessary.
                // paintDamagedAreaImmediately();
            }
        });
    }

    /*
     * Paints any portion of the component that needs updating
     * before the call returns (similar to the Win32 API UpdateWindow)
     */
    void paintDamagedAreaImmediately() {
        // force Windows to send any pending WM_PAINT events so
        // the damage area is updated on the Java side
        updateWindow();
        // make sure paint events are transferred to main event queue
        // for coalescing
        WToolkit.getWToolkit().flushPendingEvents();
        // paint the damaged area
        paintArea.paint(target, shouldClearRectBeforePaint());
    }

    native synchronized void updateWindow();

    public void paint(Graphics g) {
        ((Component)target).paint(g);
    }

    public void repaint(long tm, int x, int y, int width, int height) {
    }

    private static final double BANDING_DIVISOR = 4.0;
    private native int[] createPrintedPixels(int srcX, int srcY,
                                             int srcW, int srcH);
    public void print(Graphics g) {

        Component comp = (Component)target;

        // To conserve memory usage, we will band the image.

        int totalW = comp.getWidth();
        int totalH = comp.getHeight();

        int hInc = (int)(totalH / BANDING_DIVISOR);

        for (int startY = 0; startY < totalH; startY += hInc) {
            int endY = startY + hInc - 1;
            if (endY >= totalH) {
                endY = totalH - 1;
            }
            int h = endY - startY + 1;

            int[] pix = createPrintedPixels(0, startY, totalW, h);
            if (pix != null) {
                BufferedImage bim = new BufferedImage(totalW, h,
                                              BufferedImage.TYPE_INT_RGB);
                bim.setRGB(0, 0, totalW, h, pix, 0, totalW);
                g.drawImage(bim, 0, startY, null);
                bim.flush();
            }
        }

        comp.print(g);
    }

    public void coalescePaintEvent(PaintEvent e) {
        Rectangle r = e.getUpdateRect();
        paintArea.add(r, e.getID());
    }

    public synchronized native void reshape(int x, int y, int width, int height);

    native void nativeHandleEvent(AWTEvent e);

    public void handleEvent(AWTEvent e) {
        int id = e.getID();

        switch(id) {
        case PaintEvent.PAINT:
            // Got native painting
            paintPending = false;
            // Fallthrough to next statement
        case PaintEvent.UPDATE:
            // Skip all painting while layouting and all UPDATEs
            // while waiting for native paint
            if (!isLayouting && ! paintPending) {
                paintArea.paint(target,shouldClearRectBeforePaint());
            }
            return;
        default:
            break;
        }

        // Call the native code
        nativeHandleEvent(e);
    }

    public Dimension getMinimumSize() {
        return ((Component)target).getSize();
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Rectangle getBounds() {
        return ((Component)target).getBounds();
    }

    public boolean isFocusable() {
        return false;
    }

    /*
     * Return the GraphicsConfiguration associated with this peer, either
     * the locally stored winGraphicsConfig, or that of the target Component.
     */
    public GraphicsConfiguration getGraphicsConfiguration() {
        if (winGraphicsConfig != null) {
            return winGraphicsConfig;
        }
        else {
            // we don't need a treelock here, since
            // Component.getGraphicsConfiguration() gets it itself.
            return ((Component)target).getGraphicsConfiguration();
        }
    }

    public Win32SurfaceData getSurfaceData() {
        return surfaceData;
    }

    /**
     * Creates new surfaceData object and invalidates the previous
     * surfaceData object.
     * Replacing the surface data should never lock on any resources which are
     * required by other threads which may have them and may require
     * the tree-lock.
     */
    public void replaceSurfaceData() {
        synchronized(((Component)target).getTreeLock()) {
            synchronized(this) {
                if (pData == 0) {
                    return;
                }
                Win32SurfaceData oldData = surfaceData;
                surfaceData = Win32SurfaceData.createData(this, numBackBuffers);
                if (oldData != null) {
                    oldData.invalidate();
                }
                createBackBuffer();
            }
        }
    }

    public void replaceSurfaceDataLater() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    replaceSurfaceData();
                } catch (InvalidPipeException e) {
                    // REMIND : what do we do if our surface creation failed?
                }
            }
        });
    }

    /**
     * From the DisplayChangedListener interface.
     *
     * Called after a change in the display mode.  This event
     * triggers replacing the surfaceData object (since that object
     * reflects the current display depth information, which has
     * just changed).
     */
    public void displayChanged() {
        try {
            replaceSurfaceData();
        } catch (InvalidPipeException e) {
            // REMIND : what do we do if our surface creation failed?
        }
    }

    /**
     * Part of the DisplayChangedListener interface: components
     * do not need to react to this event
     */
    public void paletteChanged() {
    }

    //This will return null for Components not yet added to a Container
    public ColorModel getColorModel() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel();
        }
        else {
            return null;
        }
    }

    //This will return null for Components not yet added to a Container
    public ColorModel getDeviceColorModel() {
        Win32GraphicsConfig gc =
            (Win32GraphicsConfig)getGraphicsConfiguration();
        if (gc != null) {
            return gc.getDeviceColorModel();
        }
        else {
            return null;
        }
    }

    //Returns null for Components not yet added to a Container
    public ColorModel getColorModel(int transparency) {
//      return WToolkit.config.getColorModel(transparency);
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel(transparency);
        }
        else {
            return null;
        }
    }
    public java.awt.Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }

    // fallback default font object
    final static Font defaultFont = new Font("Dialog", Font.PLAIN, 12);

    public synchronized Graphics getGraphics() {
        if (!isDisposed()) {
            Component target = (Component) this.target;

            /* Fix for bug 4746122. Color and Font shouldn't be null */
            Color bgColor = target.getBackground();
            if (bgColor == null) {
                bgColor = SystemColor.window;
            }
            Color fgColor = target.getForeground();
            if (fgColor == null) {
                fgColor = SystemColor.windowText;
            }
            Font font = target.getFont();
            if (font == null) {
                font = defaultFont;
            }
            return new SunGraphics2D(surfaceData, fgColor, bgColor, font);
        }

        return null;
    }
    public FontMetrics getFontMetrics(Font font) {
        return WFontMetrics.getFontMetrics(font);
    }

    private synchronized native void _dispose();
    protected void disposeImpl() {
        Win32SurfaceData oldData = surfaceData;
        surfaceData = null;
        oldData.invalidate();
        // remove from updater before calling targetDisposedPeer
        WToolkit.targetDisposedPeer(target, this);
        _dispose();
    }

    public synchronized void setForeground(Color c) {_setForeground(c.getRGB());}
    public synchronized void setBackground(Color c) {_setBackground(c.getRGB());}

    public native void _setForeground(int rgb);
    public native void _setBackground(int rgb);

    public synchronized native void setFont(Font f);
    public final void updateCursorImmediately() {
        WGlobalCursorManager.getCursorManager().updateCursorImmediately();
    }

    native static boolean processSynchronousLightweightTransfer(Component heavyweight, Component descendant,
                                                                boolean temporary, boolean focusedWindowChangeAllowed,
                                                                long time);
    public boolean requestFocus
        (Component lightweightChild, boolean temporary,
         boolean focusedWindowChangeAllowed, long time) {
        if (processSynchronousLightweightTransfer((Component)target, lightweightChild, temporary,
                                                                      focusedWindowChangeAllowed, time)) {
            return true;
        } else {
            return _requestFocus(lightweightChild, temporary, focusedWindowChangeAllowed, time);
        }
    }
    public native boolean _requestFocus
        (Component lightweightChild, boolean temporary,
         boolean focusedWindowChangeAllowed, long time);

    public Image createImage(ImageProducer producer) {
        return new WImage(producer);
    }
    public Image createImage(int width, int height) {
        if (ddoffscreen) {
            return (Image)createVolatileImage(width, height);
        }
        else {
            ColorModel model = getColorModel(Transparency.OPAQUE);
            WritableRaster wr =
                model.createCompatibleWritableRaster(width, height);
            return new Win32OffScreenImage((Component)target, model, wr,
                                           model.isAlphaPremultiplied());
        }
    }
    public VolatileImage createVolatileImage(int width, int height) {
        return new WVolatileImage((Component)target, width, height);
    }
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return WToolkit.prepareScrImage(img, w, h, o);
    }
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        return WToolkit.checkScrImage(img, w, h, o);
    }

    // Object overrides

    public String toString() {
        return getClass().getName() + "[" + target + "]";
    }

    // Toolkit & peer internals

    private int updateX1, updateY1, updateX2, updateY2;

    WComponentPeer(Component target) {
        this.target = target;
        this.paintArea = new RepaintArea();
        Container parent = WToolkit.getNativeContainer(target);
        WComponentPeer parentPeer = (WComponentPeer) WToolkit.targetToPeer(parent);
        create(parentPeer);
        this.surfaceData = Win32SurfaceData.createData(this, numBackBuffers);
        initialize();
        start();  // Initialize enable/disable state, turn on callbacks
    }
    abstract void create(WComponentPeer parent);

    synchronized native void start();

    void initialize() {
        initZOrderPosition();

        if (((Component)target).isVisible()) {
            show();  // the wnd starts hidden
        }
        Color fg = ((Component)target).getForeground();
        if (fg != null) {
            setForeground(fg);
        }
        // Set background color in C++, to avoid inheriting a parent's color.
        Font  f = ((Component)target).getFont();
        if (f != null) {
            setFont(f);
        }
        if (! ((Component)target).isEnabled()) {
            disable();
        }
        Rectangle r = ((Component)target).getBounds();
        setBounds(r.x, r.y, r.width, r.height);
    }

    // Callbacks for window-system events to the frame

    // Invoke a update() method call on the target
    void handleRepaint(int x, int y, int w, int h) {
        // Repaints are posted from updateClient now...
    }

    // Invoke a paint() method call on the target, after clearing the
    // damaged area.
    void handleExpose(int x, int y, int w, int h) {
        // Bug ID 4081126 & 4129709 - can't do the clearRect() here,
        // since it interferes with the java thread working in the
        // same window on multi-processor NT machines.

        if (!((Component)target).getIgnoreRepaint()) {
            postEvent(new PaintEvent((Component)target, PaintEvent.PAINT,
                                 new Rectangle(x, y, w, h)));
        }
    }

    /* Invoke a paint() method call on the target, without clearing the
     * damaged area.  This is normally called by a native control after
     * it has painted itself.
     *
     * NOTE: This is called on the privileged toolkit thread. Do not
     *       call directly into user code using this thread!
     */
    void handlePaint(int x, int y, int w, int h) {
        if (!((Component)target).getIgnoreRepaint()) {
            postEvent(new PaintEvent((Component)target, PaintEvent.PAINT,
                                  new Rectangle(x, y, w, h)));
        }
    }

    /*
     * Post an event. Queue it for execution by the callback thread.
     */
    void postEvent(AWTEvent event) {
        WToolkit.postEvent(WToolkit.targetToAppContext(target), event);
    }

    // Routines to support deferred window positioning.
    public void beginLayout() {
        // Skip all painting till endLayout
        isLayouting = true;
    }

    public void endLayout() {
        if(!paintArea.isEmpty() && !paintPending &&
            !((Component)target).getIgnoreRepaint()) {
            // if not waiting for native painting repaint damaged area
            postEvent(new PaintEvent((Component)target, PaintEvent.PAINT,
                          new Rectangle()));
        }
        isLayouting = false;
    }

    public native void beginValidate();
    public native void endValidate();

    public void initZOrderPosition() {
        Container p = ((Component)target).getParent();
        WComponentPeer peerAbove = null;

        if (p != null) {
            Component children[] = p.getComponents();
            for (int i = 0; i < children.length; i++) {
                if (children[i] == target) {
                    break;
                } else {
                    Object cpeer = WToolkit.targetToPeer(children[i]);
                    if (cpeer != null &&
                        !(cpeer instanceof java.awt.peer.LightweightPeer)) {
                        peerAbove = (WComponentPeer)cpeer;
                    }
                }
            }

        }
        setZOrderPosition(peerAbove);
    }

    native void setZOrderPosition(WComponentPeer compAbove);


    /**
     * DEPRECATED
     */
    public Dimension minimumSize() {
        return getMinimumSize();
    }

    /**
     * DEPRECATED
     */
    public Dimension preferredSize() {
        return getPreferredSize();
    }

    /**
     * register a DropTarget with this native peer
     */

    public synchronized void addDropTarget(DropTarget dt) {
        if (nDropTargets == 0) {
            nativeDropTargetContext = addNativeDropTarget();
        }
        nDropTargets++;
    }

    /**
     * unregister a DropTarget with this native peer
     */

    public synchronized void removeDropTarget(DropTarget dt) {
        nDropTargets--;
        if (nDropTargets == 0) {
            removeNativeDropTarget();
            nativeDropTargetContext = 0;
        }
    }

    /**
     * add the native peer's AwtDropTarget COM object
     * @return reference to AwtDropTarget object
     */

    native long addNativeDropTarget();

    /**
     * remove the native peer's AwtDropTarget COM object
     */

    native void removeNativeDropTarget();
    native boolean nativeHandlesWheelScrolling();

    public boolean handlesWheelScrolling() {
        // should this be cached?
        return nativeHandlesWheelScrolling();
    }

    // Returns true if we are inside begin/endLayout and
    // are waiting for native painting
    public boolean isPaintPending() {
        return paintPending && isLayouting;
    }

    // Multi-buffering

    private boolean isFullScreenExclusive() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Win32GraphicsDevice gd = (Win32GraphicsDevice)gc.getDevice();
        Component target = (Component)this.target;
        while (target != null && !(target instanceof Window)) {
            target = target.getParent();
        }
        return (target == gd.getFullScreenWindow()) && gd.isDDEnabledOnDevice();
    }

    public synchronized void createBuffers(int numBuffers, BufferCapabilities caps)
        throws AWTException {
        if (!isFullScreenExclusive()) {
            throw new AWTException(
                "The operation requested is only supported on a full-screen" +
                " exclusive window");
        }
        // Re-create the primary surface
        this.numBackBuffers = (numBuffers - 1);
        try {
            replaceSurfaceData();
        } catch (InvalidPipeException e) {
            throw new AWTException(e.getMessage());
        }
    }

    public synchronized void destroyBuffers() {
        disposeBackBuffer();
        numBackBuffers = 0;
    }

    private synchronized void disposeBackBuffer() {
        if (backBuffer == null) {
            return;
        }
        backBuffer = null;
    }

    private synchronized void createBackBuffer() {
        if (numBackBuffers > 0) {
            // Create the back buffer object
            backBuffer = new Win32BackBuffer((Component)target, surfaceData);
        } else {
            backBuffer = null;
        }
    }

    public synchronized void flip(BufferCapabilities.FlipContents flipAction) {
        if (backBuffer == null) {
            throw new IllegalStateException(
                "Buffers have not been created");
        }
        Component target = (Component)this.target;
        int width = target.getWidth();
        int height = target.getHeight();
        if (flipAction == BufferCapabilities.FlipContents.COPIED) {
            Graphics g = target.getGraphics();
            g.drawImage(backBuffer, 0, 0, width, height,
                target);
            g.dispose();
        } else {
            try {
                surfaceData.flip(backBuffer.getHWSurfaceData());
            } catch (sun.java2d.InvalidPipeException e) {
                return; // Flip failed
            }
            if (flipAction ==
                BufferCapabilities.FlipContents.BACKGROUND) {
                Graphics g = backBuffer.getGraphics();
                g.setColor(target.getBackground());
                g.fillRect(0, 0, width, height);
                g.dispose();
            }
        }
    }

    public Image getBackBuffer() {
        if (backBuffer == null) {
            throw new IllegalStateException("Buffers have not been created");
        }
        return backBuffer;
    }

    /* override and return false on components that DO NOT require
       a clearRect() before painting (i.e. native components) */
    public boolean shouldClearRectBeforePaint() {
        return true;
    }
}

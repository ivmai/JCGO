/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WVolatileImage.java      1.14 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.awt.*;
import java.awt.image.BufferedImage;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.image.SunVolatileImage;
import sun.awt.Win32GraphicsEnvironment;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import sun.awt.Win32GraphicsConfig;
import sun.awt.DisplayChangedListener;

/**
 * Windows platform implementation of the VolatileImage class.
 * This implementation has to handle the case of surfaceLoss due
 * to displayChange or other events.  The class attempts to create
 * and use a hardware-based surfaceData object (Win32OffScreenSurfaceData).
 * If this object cannot be created or re-created as necessary, the
 * class falls back to a software-based SurfaceData object
 * (BufImgSurfaceData) that will be used until the hardware-based
 * surfaceData can be restored.
 */
public class WVolatileImage extends SunVolatileImage
    implements DisplayChangedListener
{

    static {
        // Default value of accelerationEnabled set in SunVolatileImage
        // Override here based on system properties
        String noddraw = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.noddraw"));
        String ddoffscreenProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddoffscreen"));
        boolean ddoffscreenDisable = (ddoffscreenProp != null &&
                                      (ddoffscreenProp.equals("false") ||
                                       ddoffscreenProp.equals("f")));
        if ((noddraw != null) || ddoffscreenDisable ||
            (ddoffscreenProp == null && getOsMajorVer() >= 6)) {
            accelerationEnabled = false;
        }
    }

    private static int getOsMajorVer() {
        try {
            String ver = System.getProperty("os.version");
            int pos;
            if (ver != null && (pos = ver.indexOf('.')) > 0)
                return Integer.parseInt(ver.substring(0, pos));
        }
        catch (SecurityException e) {
        }
        catch (NumberFormatException e) {
        }
        return -1;
    }

    /**
     * Called by constuctors to initialize common functionality
     */
    private void initialize()
    {
        ((Win32GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment()).addDisplayChangedListener(this);
    }

    /**
     * Constructor for win32-based VolatileImage using Component
     */
    public WVolatileImage(Component c, int width, int height)
    {
        super(c, width, height);
        initialize();
    }

    /**
     * Constructor for win32-based VolatileImage using Component
     */
    public WVolatileImage(Component c, int width, int height, Object context)
    {
        super(c, width, height, context);
        initialize();
    }

    /**
     * Constructor for win32-based VolatileImage using GraphicsConfiguration
     */
    public WVolatileImage(GraphicsConfiguration graphicsConfig, int width, int height)
    {
        super(graphicsConfig, width, height);
        initialize();
    }

    protected Win32OffScreenSurfaceData createHWData() {
        ColorModel cm =  getDeviceColorModel();
        return (Win32OffScreenSurfaceData)
            Win32OffScreenSurfaceData.createData(getWidth(), getHeight(),
                                                 cm,
                                                 graphicsConfig,
                                                 this,
                                                 Transparency.OPAQUE);
    }

    /**
     * Create a vram-based surfaceData object
     */
    public void initAcceleratedBackground() {
        try {
            surfaceDataHw = createHWData();
            if (surfaceDataHw != null) {
                surfaceData = surfaceDataHw;
                initContents();
                return;
            }

        } catch (sun.java2d.InvalidPipeException e) {
            // Problems during creation.  Don't propagate the exception, just
            // set the hardware surface data to null; the software surface
            // data will be used in the meantime
            surfaceDataHw = null;
        }
        surfaceData = getSurfaceDataSw();
    }

    /**
     * Called from Win32OffScreenSurfaceData to notify us that our
     * accelerated surface has been lost.
     */
    public SurfaceData restoreContents() {
        if (accelerationEnabled) {
            surfaceLossHw = true;
            surfaceLoss = true;
        }
        return super.restoreContents();
    }

    protected ColorModel getDeviceColorModel() {
        if (comp != null) {
            Win32GraphicsConfig gc =
                (Win32GraphicsConfig)comp.getGraphicsConfiguration();
            return gc.getDeviceColorModel();
        } else {
            return ((Win32GraphicsConfig)graphicsConfig).getDeviceColorModel();
        }
    }

    /**
     * Called from superclass to force restoration of this surface
     * during the validation process.  The method calls into the
     * hardware surfaceData object to force the restore.
     */
    protected void restoreSurfaceDataHw() {
        ((Win32OffScreenSurfaceData)surfaceDataHw).restoreSurface();
    }

    /**
     * Called from Win32GraphicsEnv when there has been a display mode change.
     * Note that we simply invalidate hardware surfaces here; we do not
     * attempt to recreate or re-render them.  This is to avoid doing
     * rendering operations on the AWT-Windows thread, which tends to
     * get into deadlock states with the rendering thread.  Instead,
     * we just nullify the old surface data object and wait for a future
     * method in the rendering process to recreate the surface.
     */
    public void displayChanged() {
        if (!accelerationEnabled) {
            return;
        }
        surfaceLoss = true;
        surfaceLossHw = true;
        if (surfaceDataHw != null) {
            // First, nullify the software surface.  This guards against
            // using a surfaceData that was created in a different
            // display mode.
            surfaceDataSw = null;
            surfaceData = getSurfaceDataSw();
            // Now, invalidate the old hardware-based surfaceData
            SurfaceData oldData = surfaceDataHw;
            surfaceDataHw = null;
            oldData.invalidate();
        }
        // If we were created by a Component, get the new GC
        // associated with that object.  Then we won't return
        // IMAGE_INCOMPATIBLE from our next validate() call.
        // 4636548: We need to reset graphicsConfig every time
        // through here, not just when surfaceDataHw is not null.
        if (comp != null) {
            graphicsConfig = comp.getGraphicsConfiguration();
        }
    }

    /**
     * When device palette changes, need to force a new copy
     * of the image into our hardware cache to update the
     * color indices of the pixels (indexed mode only).
     */
    public void paletteChanged() {
        surfaceLoss = true;
    }


    private class DDImageCaps extends DefaultImageCapabilities {
        private DDImageCaps() {
        }
        public boolean isTrueVolatile() {
            return isAccelerated();
        }
    }

    /**
     * Returns an ImageCapabilities object which can be
     * inquired as to the specific capabilities of this
     * VolatileImage.  This would allow programmers to find
     * out more runtime information on the specific VolatileImage
     * object that they have created.  For example, the user
     * might create a VolatileImage but the system may have
     * no video memory left for creating an image of that
     * size, so although the object is a VolatileImage, it is
     * not as accelerated as other VolatileImage objects on
     * this platform might be.  The user might want that
     * information to find other solutions to their problem.
     * @since 1.4
     */
    public ImageCapabilities getCapabilities() {
        if (accelerationEnabled) {
            if (!(imageCaps instanceof DDImageCaps)) {
                imageCaps = new DDImageCaps();
            }
        }
        return super.getCapabilities();
    }

    /**
     * Releases any associated hardware memory for this image by
     * calling flush on surfaceDataHw.  This method forces a surfaceLoss
     * situation so any future operations on the image will need to
     * revalidate the image first.
     */
    public void flush() {
        surfaceLoss = true;
        Win32OffScreenSurfaceData oldSD =
            (Win32OffScreenSurfaceData)surfaceDataHw;
        surfaceDataHw = null;
        if (oldSD != null) {
            oldSD.flush();
        }
    }
}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Win32OffScreenSurfaceData.java   1.42 03/01/30
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import sun.awt.SunHints;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.RenderLoops;
import sun.awt.Win32ColorModel24;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsConfig;

/**
 * Win32OffScreenSurfaceData
 *
 * This class implements a hardware-accelerated video memory surface.  It uses
 * a custom renderer (Win32DDRenderer) to render via DirectDraw into the
 * surface and uses a custom Blit loop (Win32BlitLoops) to copy between
 * two hardware-accelerated surfaces (including the screen).
 */
public class Win32OffScreenSurfaceData extends SurfaceData {

    int     width;
    int     height;
    int     transparency;

    private GraphicsConfiguration graphicsConfig;
    private Image image;
    private static boolean forceDDVram;
    private RenderLoops solidloops;
    private static boolean d3dEnabled = true;
    private boolean localD3dEnabled = true;
    protected boolean d3dClippingEnabled = false;
    private static boolean ddScaleEnabled = false;
    private boolean ddSurfacePunted = false;

    private static native void initDDraw(boolean sharing);
    private static native boolean enableD3D(boolean forceD3D);
    private static native void initIDs();
    private static int textureBpp = 32;
    static boolean directXInitialized = false;

    public static void initD3D() {
        if (!directXInitialized) {
            boolean forceD3D = false;
            directXInitialized = true;
            String d3dProp = (String) java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("sun.java2d.d3d"));
            String d3dTexBppProp = (String) java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("sun.java2d.d3dtexbpp"));
            if (d3dProp != null) {
                if (d3dProp.equals("true") || d3dProp.equals("t")) {
                    forceD3D = true;
                    d3dEnabled = true;
                } else if (d3dProp.equals("false") || d3dProp.equals("f")) {
                    d3dEnabled = false;
                }
            }
            if (d3dTexBppProp != null) {
                try {
                    int parsed = Integer.parseInt(d3dTexBppProp);
                    if (parsed == 32 || parsed == 16) {
                        textureBpp = parsed;
                        System.out.println("Texture bpp is set to " + textureBpp);
                    }
                } catch (NumberFormatException e) {}
            }
            if (d3dEnabled) {
                d3dEnabled = enableD3D(forceD3D);
                if (d3dEnabled) {
                    D3DBlitLoops.register();
                }
            }
        }
    }

    static {
        initIDs();
        String noddraw = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.noddraw"));
        String ddoffscreenProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddoffscreen"));
        String ddForceVramProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddforcevram"));
        String ddBlitProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddblit"));
        String ddScaleProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.ddscale"));
        String offscreenSharingProp = (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.java2d.offscreenSharing"));
        boolean ddoffscreenDisable = (ddoffscreenProp != null &&
                                      (ddoffscreenProp.equals("false") ||
                                       ddoffscreenProp.equals("f")));
        forceDDVram = (ddForceVramProp != null &&
                       (ddForceVramProp.equals("true") ||
                        ddForceVramProp.equals("t")));
        boolean ddBlitDisable = (ddBlitProp != null &&
                                 (ddBlitProp.equals("false") ||
                                  ddBlitProp.equals("f")));
        boolean ddScaleDisable = (ddScaleProp == null ||
                                  (ddScaleProp.equals("false") ||
                                   ddScaleProp.equals("f")));
        boolean offscreenSharing = ((offscreenSharingProp != null) &&
                                    !(offscreenSharingProp.equals("false") ||
                                      offscreenSharingProp.equals("f")));
        if (offscreenSharing) {
            System.out.println("Warning: offscreenSharing has been enabled. " +
                               "The use of this capability will change in future " +
                               "releases and applications that depend on it " +
                               "may not work correctly");
        }

        initD3D();
        // REMIND: This isn't really thought-out; if the user doesn't have or
        // doesn't want ddraw then we should not even have this surface type
        // in the loop
        if (noddraw == null && !ddoffscreenDisable) {
            String magPresent = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("javax.accessibility.screen_magnifier_present"));
            if (magPresent == null || !magPresent.equals("true")) {
                if ("false".equals(magPresent) || getOsMajorVer() < 6)
                  initDDraw(offscreenSharing);
                if (!ddBlitDisable) {
                    // Register out hardware-accelerated Blit loops
                    Win32BlitLoops.register();
                } else {
                    System.out.println("DirectDraw Blits disabled");
                }
                if (!ddScaleDisable) {
                    Win32ScaleLoops.register();
                    ddScaleEnabled = true;
                    System.out.println("DirectDraw Scaling enabled");
                }
                if (forceDDVram) {
                    System.out.println("DirectDraw surfaces constrained to use vram");
                }
            }
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

    public static SurfaceType getSurfaceType(ColorModel cm, int transparency) {
        // REMIND: If ddraw not available, set sType to non-ddraw surface type

        if (transparency == Transparency.TRANSLUCENT) {
            if (cm.getPixelSize() == 16) {
                return Win32SurfaceData.Ushort4444ArgbD3D;
            } else {
                return Win32SurfaceData.IntArgbD3D;
            }
        }
        boolean transparent = (transparency == Transparency.BITMASK);
        switch (cm.getPixelSize()) {
        case 32:
        case 24:
            if (cm instanceof DirectColorModel) {
                if (((DirectColorModel)cm).getRedMask() == 0xff0000) {
                    return transparent ? Win32SurfaceData.IntRgbDD_BM :
                                         Win32SurfaceData.IntRgbDD;
                } else {
                    return transparent ? Win32SurfaceData.IntRgbxDD_BM :
                                         Win32SurfaceData.IntRgbxDD;
                }
            } else {
                return transparent ? Win32SurfaceData.ThreeByteBgrDD_BM :
                                     Win32SurfaceData.ThreeByteBgrDD;
            }
        case 15:
            return transparent ? Win32SurfaceData.Ushort555RgbDD_BM :
                                 Win32SurfaceData.Ushort555RgbDD;
        case 16:
            if ((cm instanceof DirectColorModel) &&
                (((DirectColorModel)cm).getBlueMask() == 0x3e))
            {
                return transparent ? Win32SurfaceData.Ushort555RgbxDD_BM :
                                     Win32SurfaceData.Ushort555RgbxDD;
            } else {
                return transparent ? Win32SurfaceData.Ushort565RgbDD_BM :
                                     Win32SurfaceData.Ushort565RgbDD;
            }
        case 8:
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                return transparent ? Win32SurfaceData.ByteGrayDD_BM :
                                     Win32SurfaceData.ByteGrayDD;
            } else if (cm instanceof IndexColorModel &&
                       isOpaqueGray((IndexColorModel)cm)) {
                return transparent ? Win32SurfaceData.Index8GrayDD_BM :
                                     Win32SurfaceData.Index8GrayDD;
            } else {
                return transparent ? Win32SurfaceData.ByteIndexedDD_BM :
                                     Win32SurfaceData.ByteIndexedOpaqueDD;
            }
        default:
            throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                      "depth: " +
                                                      cm.getPixelSize());
        }
    }

    public static Win32OffScreenSurfaceData createData(int width, int height,
        ColorModel cm, GraphicsConfiguration graphicsConfig,
        Image image, int transparency) {

        if (!((Win32GraphicsDevice)graphicsConfig.getDevice()).
            isOffscreenAccelerationEnabled()) {
            // If acceleration is disabled on this device, don't create
            // a surface of this type
            return null;
        }

        // need to use device color model for textures
        if (transparency == Transparency.TRANSLUCENT) {
            cm = ((Win32PeerlessImage)image).getDeviceColorModel();
        }

        Win32OffScreenSurfaceData ret = new Win32OffScreenSurfaceData(width,
            height, getSurfaceType(cm, transparency),
            cm, graphicsConfig, image, transparency);
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)graphicsConfig.getDevice();

        ret.initSurface(cm.getPixelSize(), width, height, gd.getScreen(),
            (image instanceof WVolatileImage), transparency, forceDDVram);
        // d3dClippingEnabled is set during the call to initSurface
        if (ret.d3dClippingEnabled) {
            ret.d3dPipe = d3dClipPipe;
            ret.d3dTxPipe = d3dTxClipPipe;
        } else {
            ret.d3dPipe = d3dNoClipPipe;
            ret.d3dTxPipe = d3dTxNoClipPipe;
        }
        return ret;
    }

    protected static Win32D3DRenderer d3dNoClipPipe;
    protected static Win32D3DRenderer d3dClipPipe;
    protected static Win32DDRenderer ddPipe;
    protected static PixelToShapeConverter d3dTxNoClipPipe;
    protected static PixelToShapeConverter d3dTxClipPipe;
    protected static PixelToShapeConverter ddTxPipe;
    // The next 2 instance variables are set if d3dEnabled during
    // construction, depending on whether d3d can handle clipping
    // or not
    protected Win32D3DRenderer d3dPipe = null;
    protected PixelToShapeConverter d3dTxPipe = null;

    static {
        d3dNoClipPipe = new Win32D3DRenderer(false);
        d3dClipPipe = new Win32D3DRenderer(true);
        ddPipe = new Win32DDRenderer();
        if (GraphicsPrimitive.tracingEnabled()) {
            d3dNoClipPipe = d3dNoClipPipe.traceWrapD3D();
            d3dClipPipe = d3dClipPipe.traceWrapD3D();
            ddPipe = ddPipe.traceWrapDD();
        }
        d3dTxNoClipPipe = new PixelToShapeConverter(d3dNoClipPipe);
        d3dTxClipPipe = new PixelToShapeConverter(d3dClipPipe);
        ddTxPipe = new PixelToShapeConverter(ddPipe);
    }

    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON &&
            sg2d.paintState == sg2d.PAINT_SOLIDCOLOR &&
            sg2d.compositeState == sg2d.COMP_ISCOPY &&
            sg2d.clipState != sg2d.CLIP_SHAPE)
        {
            PixelToShapeConverter txPipe;
            Win32DDRenderer nontxPipe;
            if (d3dEnabled && localD3dEnabled) {
                txPipe    = d3dTxPipe;
                nontxPipe = d3dPipe;
            } else {
                txPipe    = ddTxPipe;
                nontxPipe = ddPipe;
            }
            sg2d.imagepipe = imagepipe;
            if (sg2d.transformState > sg2d.TRANSFORM_TRANSLATEONLY) {
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = txPipe;
            } else if (sg2d.strokeState != sg2d.STROKE_THIN){
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = nontxPipe;
            } else {
                sg2d.drawpipe = nontxPipe;
                sg2d.fillpipe = nontxPipe;
            }
            sg2d.shapepipe = nontxPipe;
            if (sg2d.textAntialiasHint == SunHints.INTVAL_TEXT_ANTIALIAS_ON) {
                sg2d.textpipe = aaTextRenderer;
            } else {
                sg2d.textpipe = solidTextRenderer;
            }
            // This is needed for AA text.
            // Note that even a SolidTextRenderer can dispatch AA text
            // if a GlyphVector overrides the AA setting.
            sg2d.loops = solidloops;
        } else {
            super.validatePipe(sg2d);
        }
    }

    /**
     * Disables D3D on this surfaceData object.  This can happen
     * when we encounter an error in rendering a D3D primitive
     * (for example, if we were unable to create a D3D device).
     * Upon next validation, this renderer will then choose a
     * non-D3D pipe.
     */
    public void disableD3D() {
        localD3dEnabled = false;
    }

    public static boolean isDDScaleEnabled() {
        return ddScaleEnabled;
    }

    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }

    public void lock() {
        // REMIND: Do we need this call here?  Who calls the Java method?
    }

    public void unlock() {
        // REMIND: Do we need this call here?  Who calls the Java method?
    }

    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState == sg2d.PAINT_SOLIDCOLOR &&
            sg2d.compositeState == sg2d.COMP_ISCOPY)
        {
            return solidloops;
        }
        return super.getRenderLoops(sg2d);
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }

    /**
     * Initializes the native Ops pointer.
     */
    private native void initOps(int depth, int transparency);

    /**
     * This native method creates the offscreen surface in video memory and
     * (if necessary) initializes DirectDraw
     */
    public native void initSurface(int depth, int width, int height,
                                   int screen,
                                   boolean isVolatile,
                                   int transparency,
                                   boolean disablePunts);

    public native void restoreSurface();

    /**
     * Protected constructor.  Use createData() to create an object.
     */
    protected Win32OffScreenSurfaceData(int width, int height,
                                     SurfaceType sType, ColorModel cm,
                                     GraphicsConfiguration graphicsConfig,
                                     Image image, int transparency)
    {
        super(sType, cm);
        this.width = width;
        this.height = height;
        this.graphicsConfig = graphicsConfig;
        this.image = image;
        initOps(cm.getPixelSize(), transparency);
        this.transparency = transparency;
        this.solidloops =
            ((Win32GraphicsConfig)graphicsConfig).getSolidLoops(sType);
    }

    /**
     * Need this since the surface data is created with
     * the color model of the target GC, which is always
     * opaque. But in SunGraphics2D.blitSD we choose loops
     * based on the transparency on the source SD, so
     * we could choose wrong loop (blit instead of blitbg,
     * for example, which will cause problems in transparent
     * case).
     */
    public int getTransparency() {
        return transparency;
    }

    public static int getTextureBpp() {
        return textureBpp;
    }

    public SurfaceData getReplacement() {
        // When someone asks for a new surface data, we punt to our
        // container image which will attempt to restore the contents
        // of this surface or, failing that, will return null

        if (image instanceof Win32OffScreenImage) {
            // Only this type of image has auto-restore
            // REMIND: don't like this hack - we should just generically
            // punt to our parent image and let it decide without having
            // to figure out what our parent is.
            return ((Win32OffScreenImage)image).restoreContents();
        } else  if (image instanceof WVolatileImage) {
            return ((WVolatileImage)image).restoreContents();
        } else {
            throw new InternalError();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(width, height);
    }

    private native void nativeInvalidate();

    public void invalidate() {
        if (isValid()) {
            nativeInvalidate();
            super.invalidate();
        }
    }

    public native void setTransparentPixel(int pixel);

    public native void flush();

    /**
     * Returns true if the native representation of this image has been
     * moved into ddraw system memory.  This happens when many reads
     * or read-modify-write operations are requested of that surface.
     * If we have moved that surface into system memory, we should note that
     * here so that someone wanting to copy something to this surface will
     * take that into account during that copy.
     */
    public boolean surfacePunted() {
        return ddSurfacePunted;
    }
}

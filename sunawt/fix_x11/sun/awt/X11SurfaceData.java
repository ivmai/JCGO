/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)X11SurfaceData.java      1.41 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import sun.java2d.SurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.XORComposite;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.Region;
import sun.awt.X11PMBlitLoops;
import sun.awt.X11PMBlitBgLoops;
import sun.awt.motif.MComponentPeer;
import sun.awt.motif.X11OffScreenImage;
import sun.awt.motif.X11VolatileImage;
import sun.awt.font.X11TextRenderer;
import sun.awt.image.PixelConverter;

public abstract class X11SurfaceData extends SurfaceData {
    MComponentPeer peer;
    private X11GraphicsConfig graphicsConfig;
    private RenderLoops solidloops;

    protected int depth;

    private static native void initIDs(Class xorComp);
    protected native void initSurface(int depth, int width, int height,
                                      long drawable, int bitmask);

    public static final String
        DESC_INT_BGR_X11        = "Integer BGR Pixmap";
    public static final String
        DESC_INT_RGB_X11        = "Integer RGB Pixmap";
    public static final String
        DESC_BYTE_IND_OPQ_X11   = "Byte Indexed Opaque Pixmap";

    public static final String
        DESC_INT_BGR_X11_BM     = "Integer BGR Pixmap with 1-bit transp";
    public static final String
        DESC_INT_RGB_X11_BM     = "Integer RGB Pixmap with 1-bit transp";
    public static final String
        DESC_BYTE_IND_X11_BM    = "Byte Indexed Pixmap with 1-bit transp";

    public static final String
        DESC_BYTE_GRAY_X11      = "Byte Gray Opaque Pixmap";
    public static final String
        DESC_INDEX8_GRAY_X11    = "Index8 Gray Opaque Pixmap";

    public static final String
        DESC_BYTE_GRAY_X11_BM   = "Byte Gray Opaque Pixmap with 1-bit transp";
    public static final String
        DESC_INDEX8_GRAY_X11_BM = "Index8 Gray Opaque Pixmap with 1-bit transp";

    public static final String
        DESC_3BYTE_RGB_X11      = "3 Byte RGB Pixmap";
    public static final String
        DESC_3BYTE_BGR_X11      = "3 Byte BGR Pixmap";

    public static final String
        DESC_3BYTE_RGB_X11_BM   = "3 Byte RGB Pixmap with 1-bit transp";
    public static final String
        DESC_3BYTE_BGR_X11_BM   = "3 Byte BGR Pixmap with 1-bit transp";

    public static final String
        DESC_USHORT_555_RGB_X11 = "Ushort 555 RGB Pixmap";
    public static final String
        DESC_USHORT_565_RGB_X11 = "Ushort 565 RGB Pixmap";

    public static final String
        DESC_USHORT_555_RGB_X11_BM
                                = "Ushort 555 RGB Pixmap with 1-bit transp";
    public static final String
        DESC_USHORT_565_RGB_X11_BM
                                = "Ushort 565 RGB Pixmap with 1-bit transp";

    public static final String DESC_USHORT_INDEXED_X11
                                = "Ushort Indexed Pixmap";
    public static final String DESC_USHORT_INDEXED_X11_BM
                                = "Ushort Indexed Pixmap with 1-bit transp";

    public static final SurfaceType IntBgrX11 =
        SurfaceType.IntBgr.deriveSubType(DESC_INT_BGR_X11);
    public static final SurfaceType IntRgbX11 =
        SurfaceType.IntRgb.deriveSubType(DESC_INT_RGB_X11);

    public static final SurfaceType ThreeByteRgbX11 =
        SurfaceType.ThreeByteRgb.deriveSubType(DESC_3BYTE_RGB_X11);
    public static final SurfaceType ThreeByteBgrX11 =
        SurfaceType.ThreeByteBgr.deriveSubType(DESC_3BYTE_BGR_X11);

    public static final SurfaceType UShort555RgbX11 =
        SurfaceType.Ushort555Rgb.deriveSubType(DESC_USHORT_555_RGB_X11);
    public static final SurfaceType UShort565RgbX11 =
        SurfaceType.Ushort565Rgb.deriveSubType(DESC_USHORT_565_RGB_X11);

    public static final SurfaceType UShortIndexedX11 =
        SurfaceType.UshortIndexed.deriveSubType(DESC_USHORT_INDEXED_X11);

    public static final SurfaceType ByteIndexedOpaqueX11 =
        SurfaceType.ByteIndexedOpaque.deriveSubType(DESC_BYTE_IND_OPQ_X11);

    public static final SurfaceType ByteGrayX11 =
        SurfaceType.ByteGray.deriveSubType(DESC_BYTE_GRAY_X11);
    public static final SurfaceType Index8GrayX11 =
        SurfaceType.Index8Gray.deriveSubType(DESC_INDEX8_GRAY_X11);

    // Bitmap surface types
    public static final SurfaceType IntBgrX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INT_BGR_X11_BM,
                                         PixelConverter.Xbgr.instance);
    public static final SurfaceType IntRgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INT_RGB_X11_BM,
                                         PixelConverter.Xrgb.instance);

    public static final SurfaceType ThreeByteRgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_3BYTE_RGB_X11_BM,
                                         PixelConverter.Xbgr.instance);
    public static final SurfaceType ThreeByteBgrX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_3BYTE_BGR_X11_BM,
                                         PixelConverter.Xrgb.instance);

    public static final SurfaceType UShort555RgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_555_RGB_X11_BM,
                                         PixelConverter.Ushort555Rgb.instance);
    public static final SurfaceType UShort565RgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_565_RGB_X11_BM,
                                         PixelConverter.Ushort565Rgb.instance);

    public static final SurfaceType UShortIndexedX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_INDEXED_X11_BM);
    public static final SurfaceType ByteIndexedX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_BYTE_IND_X11_BM);

    public static final SurfaceType ByteGrayX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_BYTE_GRAY_X11_BM);
    public static final SurfaceType Index8GrayX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INDEX8_GRAY_X11_BM);


    private static Boolean accelerationEnabled = null;

    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }

    protected static X11Renderer x11pipe;
    protected static PixelToShapeConverter x11txpipe;
    protected static TextPipe x11textpipe;

    static {
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs(XORComposite.class);

            String xtextpipe = (String) java.security.AccessController.doPrivileged
                (new sun.security.action.GetPropertyAction("sun.java2d.xtextpipe"));
            if (xtextpipe == null || "true".startsWith(xtextpipe)) {
                if ("true".equals(xtextpipe)) {
                    // Only verbose if they use the full string "true"
                    System.out.println("using X11 text renderer");
                }
                x11textpipe = new X11TextRenderer();
            } else {
                if ("false".equals(xtextpipe)) {
                    // Only verbose if they use the full string "false"
                    System.out.println("using DGA text renderer");
                }
                x11textpipe = solidTextRenderer;
            }

            if (isAccelerationEnabled()) {
                X11PMBlitLoops.register();
                X11PMBlitBgLoops.register();
            }

            x11pipe = new X11Renderer();
            if (GraphicsPrimitive.tracingEnabled()) {
                x11pipe = x11pipe.traceWrap();
                if (x11textpipe instanceof X11TextRenderer) {
                    x11textpipe = ((X11TextRenderer) x11textpipe).traceWrap();
                }
            }
            x11txpipe = new PixelToShapeConverter(x11pipe);
        }
    }

    /**
     * Returns true if we can use DGA on any of the screens
     */
    public static native boolean isDgaAvailable();

    public static boolean isAccelerationEnabled() {
        if (accelerationEnabled == null) {

            if (GraphicsEnvironment.isHeadless()) {
                accelerationEnabled = Boolean.FALSE;
            } else {
                String prop =
                    (String) java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction("sun.java2d.pmoffscreen"));
                if (prop != null) {
                    // true iff prop==true, false otherwise
                    accelerationEnabled = new Boolean(prop);
                } else {
                    // use pixmaps if there is no dga, no matter local or remote
                    accelerationEnabled = new Boolean(!isDgaAvailable());
                }
            }
        }
        return accelerationEnabled.booleanValue();
    }

    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON &&
            sg2d.paintState == sg2d.PAINT_SOLIDCOLOR &&
            (sg2d.compositeState == sg2d.COMP_ISCOPY ||
             sg2d.compositeState == sg2d.COMP_XOR) &&
            sg2d.clipState != sg2d.CLIP_SHAPE)
        {
            sg2d.imagepipe = imagepipe;
            if (sg2d.transformState > sg2d.TRANSFORM_TRANSLATEONLY) {
                sg2d.drawpipe = x11txpipe;
                sg2d.fillpipe = x11txpipe;
            } else if (sg2d.strokeState != sg2d.STROKE_THIN){
                sg2d.drawpipe = x11txpipe;
                sg2d.fillpipe = x11pipe;
            } else {
                sg2d.drawpipe = x11pipe;
                sg2d.fillpipe = x11pipe;
            }
            sg2d.shapepipe = x11pipe;
            // REMIND: There is no alternate text pipe for now...
            if (sg2d.textAntialiasHint != SunHints.INTVAL_TEXT_ANTIALIAS_ON) {
                if (sg2d.compositeState == sg2d.COMP_ISCOPY) {
                    sg2d.textpipe = x11textpipe;
                } else {
                    sg2d.textpipe = solidTextRenderer;
                }
            } else {
                sg2d.textpipe = aaTextRenderer;
            }
            // This is needed for AA text.
            // Note that even an X11TextRenderer can dispatch AA text
            // if a GlyphVector overrides the AA setting.
            // We use getRenderLoops() rather than setting solidloops
            // directly so that we get the appropriate loops in XOR mode.
            sg2d.loops = getRenderLoops(sg2d);
        } else {
            super.validatePipe(sg2d);
        }
    }

    public void lock() {
        // REMIND: Need to DGA lock here...
    }

    public void unlock() {
        // REMIND: Need to DGA lock here...
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
     * Method for instantiating a Window SurfaceData
     */
    public static X11WindowSurfaceData createData(MComponentPeer peer) {
       X11GraphicsConfig gc = getGC(peer);
       return new X11WindowSurfaceData(peer, gc, gc.getSurfaceType());
    }

    /**
     * Method for instantiating a Pixmap SurfaceData (offscreen)
     */
    public static X11PixmapSurfaceData createData(X11GraphicsConfig gc,
                                                  int width, int height,
                                                  ColorModel cm, Image image) {
        return createData(gc, width, height, cm, image, 0, 0);
    }

    public static X11PixmapSurfaceData createData(X11GraphicsConfig gc,
                                                  int width, int height,
                                                  ColorModel cm, Image image,
                                                  long drawable, int bitmask) {
        return new X11PixmapSurfaceData(gc, width, height, image,
                                        getSurfaceType(gc, bitmask != 0),
                                        cm, drawable, bitmask);
    }

    /**
     * Initializes the native Ops pointer.
     */
    private native void initOps(MComponentPeer peer,
                                X11GraphicsConfig gc, int depth);

    protected X11SurfaceData(MComponentPeer peer,
                             X11GraphicsConfig gc,
                             SurfaceType sType,
                             ColorModel cm) {
        super(sType, cm);
        this.peer = peer;
        this.graphicsConfig = gc;
        this.solidloops = graphicsConfig.getSolidLoops(sType);
        this.depth = cm.getPixelSize();
        initOps(peer, graphicsConfig, depth);
    }

    public static X11GraphicsConfig getGC(MComponentPeer peer) {
        if (peer != null) {
            return (X11GraphicsConfig) peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            return (X11GraphicsConfig)gd.getDefaultConfiguration();
        }
    }

    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        CompositeType comptype = sg2d.imageComp;
        if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE &&
            sg2d.clipState != sg2d.CLIP_SHAPE &&
            (CompositeType.SrcOverNoEa.equals(comptype) ||
             CompositeType.SrcNoEa.equals(comptype)))
        {
            x += sg2d.transX;
            y += sg2d.transY;
            int dstx1 = x + dx;
            int dsty1 = y + dy;
            int dstx2 = dstx1 + w;
            int dsty2 = dsty1 + h;
            Region clip = sg2d.getCompClip();
            if (dstx1 < clip.getLoX()) dstx1 = clip.getLoX();
            if (dsty1 < clip.getLoY()) dsty1 = clip.getLoY();
            if (dstx2 > clip.getHiX()) dstx2 = clip.getHiX();
            if (dsty2 > clip.getHiY()) dsty2 = clip.getHiY();
            if (dstx1 < dstx2 && dsty1 < dsty2) {
                x11pipe.devCopyArea(this, dstx1 - dx, dsty1 - dy,
                                    dstx1, dsty1,
                                    dstx2 - dstx1, dsty2 - dsty1);
            }
            return true;
        }
        return false;
    }

    protected static SurfaceType getSurfaceType(X11GraphicsConfig gc, boolean transparent) {
        SurfaceType sType;
        ColorModel cm = gc.getColorModel();
        switch (cm.getPixelSize()) {
        case 24:
            if (gc.getBitsPerPixel() == 24) {
                if (cm instanceof DirectColorModel) {
                    // 4517321: We will always use ThreeByteBgr for 24 bpp
                    // surfaces, regardless of the pixel masks reported by
                    // X11.  Despite ambiguity in the X11 spec in how 24 bpp
                    // surfaces are treated, it appears that the best
                    // SurfaceType for these configurations (including
                    // some Matrox Millenium and ATI Radeon boards) is
                    // ThreeByteBgr.
                    sType = transparent ? X11SurfaceData.ThreeByteBgrX11_BM : X11SurfaceData.ThreeByteBgrX11;
                } else {
                    throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                              "depth/cm combo: " +
                                                              cm.getPixelSize()  +
                                                              ", " + cm);
                }
                break;
            }
            // Fall through for 32 bit case
        case 32:
            if (cm instanceof DirectColorModel) {
                if (((DirectColorModel)cm).getRedMask() == 0xff0000) {
                    sType = transparent ? X11SurfaceData.IntRgbX11_BM : X11SurfaceData.IntRgbX11;
                } else {
                    sType = transparent ? X11SurfaceData.IntBgrX11_BM : X11SurfaceData.IntBgrX11;
                }
            } else {
                throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                          "depth/cm combo: " +
                                                          cm.getPixelSize()  +
                                                          ", " + cm);
            }
            break;
        case 15:
            sType = transparent ? X11SurfaceData.UShort555RgbX11_BM : X11SurfaceData.UShort555RgbX11;
            break;
        case 16:
            if ((cm instanceof DirectColorModel) &&
                (((DirectColorModel)cm).getGreenMask() == 0x3e0))
            {
                // fix for 4352984: Riva128 on Linux
                sType = transparent ? X11SurfaceData.UShort555RgbX11_BM : X11SurfaceData.UShort555RgbX11;
            } else {
                sType = transparent ? X11SurfaceData.UShort565RgbX11_BM : X11SurfaceData.UShort565RgbX11;
            }
            break;

        case 12:
            if (cm instanceof IndexColorModel) {
                sType = transparent ? UShortIndexedX11_BM : UShortIndexedX11;
            } else {
                throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                          "depth: " +
                                                          cm.getPixelSize() +
                                                          " cm=" + cm);
            }
            break;

        case 8:
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                sType = transparent ? X11SurfaceData.ByteGrayX11_BM : X11SurfaceData.ByteGrayX11;
            } else if (cm instanceof IndexColorModel &&
                       isOpaqueGray((IndexColorModel)cm)) {
                sType = transparent ? X11SurfaceData.Index8GrayX11_BM : X11SurfaceData.Index8GrayX11;
            } else {
                sType = transparent ? X11SurfaceData.ByteIndexedX11_BM : X11SurfaceData.ByteIndexedOpaqueX11;
            }
            break;
        default:
            throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                      "depth: " +
                                                      cm.getPixelSize());
        }
        return sType;
    }

    public native void setInvalid();

    public void invalidate() {
        if (isValid()) {
            setInvalid();
            super.invalidate();
        }
    }

    public static class X11WindowSurfaceData extends X11SurfaceData {

        public X11WindowSurfaceData(MComponentPeer peer,
                                    X11GraphicsConfig gc,
                                    SurfaceType sType) {
            super(peer, gc, sType, peer.getColorModel());
        }

        public SurfaceData getReplacement() {
            return peer.getSurfaceData();
        }

        public Rectangle getBounds() {
            Rectangle r = peer.getBounds();
            r.x = r.y = 0;
            return r;
        }
    }

    public static class X11PixmapSurfaceData extends X11SurfaceData {

        Image                   offscreenImage;
        int                     width;
        int                     height;
        int                     transparency;

        public X11PixmapSurfaceData(X11GraphicsConfig gc,
                                    int width, int height,
                                    Image image,
                                    SurfaceType sType, ColorModel cm,
                                    long drawable, int bitmask) {
            super(null, gc, sType, cm);
            this.width = width;
            this.height = height;
            offscreenImage = image;
            transparency = bitmask != 0 ?
                Transparency.BITMASK : Transparency.OPAQUE;
            initSurface(depth, width, height, drawable, bitmask);
        }

        public SurfaceData getReplacement() {
            // When someone asks for a new surface data, we punt to our
            // container image which will attempt to restore the contents
            // of this surface or, failing that, will return null
            // REMIND: X11 surface datas should never lose their contents,
            // so this method should never be called.
            if (offscreenImage instanceof X11OffScreenImage) {
                // Only this type of image has auto-restore
                // REMIND: don't like this hack - we should just generically
                // punt to our parent image and let it decide without having
                // to figure out what our parent is.
                return ((X11OffScreenImage)offscreenImage).restoreContents();
            } else {
                // default case: offscreenImage must be a VolatileImage
                return ((X11VolatileImage)offscreenImage).restoreContents();
            }
        }

        /**
         * Need this since the surface data is created with
         * the color model of the target GC, which is always
         * opaque. But in SunGraphics2D.blitSD we choose loops
         * based on the transparency on the source SD, so
         * it could choose wrong loop (blit instead of blitbg,
         * for example).
         */
        public int getTransparency() {
            return transparency;
        }

        public Rectangle getBounds() {
            return new Rectangle(width, height);
        }
    }


}

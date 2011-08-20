/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SurfaceData.java 1.35 03/03/19
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.java2d;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.loops.RenderCache;
import sun.java2d.loops.RenderLoops;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.MaskFill;
import sun.java2d.loops.DrawLine;
import sun.java2d.loops.FillRect;
import sun.java2d.loops.DrawRect;
import sun.java2d.loops.DrawPolygons;
import sun.java2d.loops.FillSpans;
import sun.java2d.loops.DrawGlyphList;
import sun.java2d.loops.DrawGlyphListAA;
import sun.java2d.pipe.LoopPipe;
import sun.java2d.pipe.CompositePipe;
import sun.java2d.pipe.GeneralCompositePipe;
import sun.java2d.pipe.SpanClipRenderer;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.java2d.pipe.DuctusShapeRenderer;
import sun.java2d.pipe.AlphaPaintPipe;
import sun.java2d.pipe.AlphaColorPipe;
import sun.java2d.pipe.PixelToShapeConverter;
import sun.java2d.pipe.TextPipe;
import sun.java2d.pipe.TextRenderer;
import sun.java2d.pipe.AATextRenderer;
import sun.java2d.pipe.SolidTextRenderer;
import sun.java2d.pipe.OutlineTextRenderer;
import sun.java2d.pipe.DrawImagePipe;
import sun.java2d.pipe.DrawImage;
import sun.awt.SunHints;
import sun.awt.image.AcceleratedOffScreenImage;
import sun.awt.image.SunVolatileImage;


/**
 * This class provides various pieces of information relevant to a
 * particular drawing surface.  The information obtained from this
 * object describes the pixels of a particular instance of a drawing
 * surface and can only be shared among the various graphics objects
 * that target the same BufferedImage or the same screen Component.
 */
public abstract class SurfaceData implements Transparency, DisposerTarget {
    private long pData;
    private boolean valid;
    private SurfaceType surfaceType;
    private ColorModel colorModel;
    /**
     * The following variables all deal with our model for transparently
     * accelerating copies from surface data objects to hardware-based
     * surfaces.  See Win32OffScreenImage for more details.
     */
    private boolean dirty;
    private boolean needsBackup = true;
    private int numCopies;
    private final Object disposerReferent = new Object();

    private static native void initIDs();

    static {
        initIDs();
    }

    public void setDisposerRecord(DisposerRecord rec) {
    }

    protected SurfaceData(SurfaceType surfaceType, ColorModel cm) {
        this.colorModel = cm;
        this.surfaceType = surfaceType;
        valid = true;
    }

    protected SurfaceData() {
        valid = true;
    }

    public static SurfaceData getSurfaceDataFromImage(Image img,
                                                      SurfaceData dstData,
                                                      CompositeType comp,
                                                      Color bgColor,
                                                      boolean scale)
    {
        if (img instanceof AcceleratedOffScreenImage) {
            return ((AcceleratedOffScreenImage)img).
                getSourceSurfaceData(dstData, comp, bgColor, scale);
        } else if (img instanceof BufferedImage) {
            return BufImgSurfaceData.createData((BufferedImage)img);
        } else if (img instanceof SunVolatileImage) {
            return ((SunVolatileImage)img).getSurfaceData();
        } else {
            return null;
        }
    }


    /**
     * Retrieves the value of the dirty flag.  This flag is set to true
     * whenever the surface is rendered to and false whenever we copy from
     * the surface.
     */
    private static native void setDirtyNative(SurfaceData sd, boolean dirty);

    /**
     * Sets the value of the needsBackup variable, which indicates whether
     * the surface is newer than other copies of it we may have.  This would
     * be true if we rendered to this surface since the last time we backed
     * it up.
     */
    public void setNeedsBackup(boolean needsBackup) {
        this.needsBackup = needsBackup;
        if (needsBackup) {
            numCopies = 0;
        }
    }

    public boolean needsBackup() {
        return needsBackup;
    }

    /**
     * Retrieves value of numCopies variable, which is used to track the number
     * of times we have copied from this SurfaceData object since we last
     * rendered to it.  This is used in some image classes (e.g.,
     * Win32OffScreenImage) to determine when to provide under-the-hood
     * acceleration by using a vram-based version of the image instead.
     */
    public final int getNumCopies() {
        return numCopies;
    }

    /**
     * Increments the variable numCopies.  See comment for getNumCopies()
     * for more information about this variable.  Returns the new value of
     * the variable.  If the surface was marked "dirty" (meaning that it
     * has been rendered to since the last time we copied from the image),
     * then we clear the flag (both in Java and in native code).
     */
    public int increaseNumCopies() {
        if (dirty) {
            setDirtyNative(this, false);
            dirty = false;
        }
        numCopies++;
        return numCopies;
    }

    /**
     * Returns a boolean indicating whether or not this SurfaceData is valid.
     */
    public final boolean isValid() {
        return valid;
    }

    public Object getDisposerReferent() {
        return disposerReferent;
    }

    /**
     * Sets this SurfaceData object to the invalid state.  All Graphics
     * objects must get a new SurfaceData object via the refresh method
     * and revalidate their pipelines before continuing.
     */
    public void invalidate() {
        valid = false;
    }

    public static boolean isNull(SurfaceData sd) {
        return sd == null || sd == NullSurfaceData.theInstance;
    }

    /**
     * Return a new SurfaceData object that represents the current state
     * of the destination that this SurfaceData object describes.
     * This method is typically called when the SurfaceData is invalidated.
     */
    public abstract SurfaceData getReplacement();

    protected static final LoopPipe colorPrimitives = new LoopPipe();

    public static final TextPipe outlineTextRenderer = new OutlineTextRenderer();
    public static final TextPipe solidTextRenderer = new SolidTextRenderer();
    public static final TextPipe aaTextRenderer = new AATextRenderer();

    protected static final CompositePipe colorPipe;
    protected static final PixelToShapeConverter colorViaShape;
    protected static final TextPipe colorText;
    protected static final CompositePipe clipColorPipe;
    protected static final TextPipe clipColorText;
    protected static final DuctusShapeRenderer AAColorShape;
    protected static final PixelToShapeConverter AAColorViaShape;
    protected static final DuctusShapeRenderer AAClipColorShape;
    protected static final PixelToShapeConverter AAClipColorViaShape;

    protected static final CompositePipe paintPipe;
    protected static final SpanShapeRenderer paintShape;
    protected static final PixelToShapeConverter paintViaShape;
    protected static final TextPipe paintText;
    protected static final CompositePipe clipPaintPipe;
    protected static final TextPipe clipPaintText;
    protected static final DuctusShapeRenderer AAPaintShape;
    protected static final PixelToShapeConverter AAPaintViaShape;
    protected static final DuctusShapeRenderer AAClipPaintShape;
    protected static final PixelToShapeConverter AAClipPaintViaShape;

    protected static final CompositePipe compPipe;
    protected static final SpanShapeRenderer compShape;
    protected static final PixelToShapeConverter compViaShape;
    protected static final TextPipe compText;
    protected static final CompositePipe clipCompPipe;
    protected static final TextPipe clipCompText;
    protected static final DuctusShapeRenderer AACompShape;
    protected static final PixelToShapeConverter AACompViaShape;
    protected static final DuctusShapeRenderer AAClipCompShape;
    protected static final PixelToShapeConverter AAClipCompViaShape;

    static {
        colorPipe = new AlphaColorPipe();
        // colorShape = colorPrimitives;
        colorViaShape = new PixelToShapeConverter(colorPrimitives);
        colorText = new TextRenderer(colorPipe);
        clipColorPipe = new SpanClipRenderer(colorPipe);
        clipColorText = new TextRenderer(clipColorPipe);
        AAColorShape = new DuctusShapeRenderer(colorPipe);
        AAColorViaShape = new PixelToShapeConverter(AAColorShape);
        AAClipColorShape = new DuctusShapeRenderer(clipColorPipe);
        AAClipColorViaShape = new PixelToShapeConverter(AAClipColorShape);

        paintPipe = new AlphaPaintPipe();
        paintShape = new SpanShapeRenderer.Composite(paintPipe);
        paintViaShape = new PixelToShapeConverter(paintShape);
        paintText = new TextRenderer(paintPipe);
        clipPaintPipe = new SpanClipRenderer(paintPipe);
        clipPaintText = new TextRenderer(clipPaintPipe);
        AAPaintShape = new DuctusShapeRenderer(paintPipe);
        AAPaintViaShape = new PixelToShapeConverter(AAPaintShape);
        AAClipPaintShape = new DuctusShapeRenderer(clipPaintPipe);
        AAClipPaintViaShape = new PixelToShapeConverter(AAClipPaintShape);

        compPipe = new GeneralCompositePipe();
        compShape = new SpanShapeRenderer.Composite(compPipe);
        compViaShape = new PixelToShapeConverter(compShape);
        compText = new TextRenderer(compPipe);
        clipCompPipe = new SpanClipRenderer(compPipe);
        clipCompText = new TextRenderer(clipCompPipe);
        AACompShape = new DuctusShapeRenderer(compPipe);
        AACompViaShape = new PixelToShapeConverter(AACompShape);
        AAClipCompShape = new DuctusShapeRenderer(clipCompPipe);
        AAClipCompViaShape = new PixelToShapeConverter(AAClipCompShape);
    }

    protected static final DrawImagePipe imagepipe = new DrawImage();

    public void validatePipe(SunGraphics2D sg2d) {
        sg2d.imagepipe = imagepipe;
        if (sg2d.compositeState == sg2d.COMP_XOR) {
            if (sg2d.paintState == sg2d.PAINT_TILE) {
                sg2d.drawpipe = paintViaShape;
                sg2d.fillpipe = paintViaShape;
                sg2d.shapepipe = paintShape;
                // REMIND: Ideally PAINT_TILE mode would use glyph
                // rendering as opposed to outline rendering but the
                // glyph paint rendering pipeline uses MaskBlit which
                // is not defined for XOR.  This means that text drawn
                // in XOR mode with a Color object is different than
                // text drawn in XOR mode with a Paint object.
                sg2d.textpipe = outlineTextRenderer;
            } else {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = colorViaShape;
                    sg2d.fillpipe = colorViaShape;
                    // REMIND: We should not be changing text strategies
                    // between outline and glyph rendering based upon the
                    // presence of a complex clip as that could cause a
                    // mismatch when drawing the same text both clipped
                    // and unclipped on two separate rendering passes.
                    // Unfortunately, all of the clipped glyph rendering
                    // pipelines rely on the use of the MaskBlit operation
                    // which is not defined for XOR.
                    sg2d.textpipe = outlineTextRenderer;
                } else {
                    if (sg2d.transformState > sg2d.TRANSFORM_TRANSLATEONLY) {
                        sg2d.drawpipe = colorViaShape;
                        sg2d.fillpipe = colorViaShape;
                    } else {
                        if (sg2d.strokeState != sg2d.STROKE_THIN) {
                            sg2d.drawpipe = colorViaShape;
                        } else {
                            sg2d.drawpipe = colorPrimitives;
                        }
                        sg2d.fillpipe = colorPrimitives;
                    }
                    sg2d.textpipe = solidTextRenderer;
                }
                sg2d.shapepipe = colorPrimitives;
                sg2d.loops = getRenderLoops(sg2d);
                // assertion: sg2d.surfaceData == this
            }
        } else if (sg2d.compositeState == sg2d.COMP_CUSTOM) {
            if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipCompViaShape;
                    sg2d.fillpipe = AAClipCompViaShape;
                    sg2d.shapepipe = AAClipCompShape;
                    sg2d.textpipe = clipCompText;
                } else {
                    sg2d.drawpipe = AACompViaShape;
                    sg2d.fillpipe = AACompViaShape;
                    sg2d.shapepipe = AACompShape;
                    sg2d.textpipe = compText;
                }
            } else {
                sg2d.drawpipe = compViaShape;
                sg2d.fillpipe = compViaShape;
                sg2d.shapepipe = compShape;
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipCompText;
                } else {
                    sg2d.textpipe = compText;
                }
            }
        } else if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON) {
            boolean usingfill = false;
            if (sg2d.paintState != sg2d.PAINT_TILE) {
                if (sg2d.alphafill == null) {
                    sg2d.alphafill =
                        MaskFill.getFromCache(SurfaceType.AnyColor,
                                              sg2d.fillComp,
                                              getSurfaceType());
                    // assertion: sg2d.surfaceData == this
                }
                if (sg2d.alphafill != null) {
                    if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                        sg2d.drawpipe = AAClipColorViaShape;
                        sg2d.fillpipe = AAClipColorViaShape;
                        sg2d.shapepipe = AAClipColorShape;
                        sg2d.textpipe = clipColorText;
                    } else {
                        sg2d.drawpipe = AAColorViaShape;
                        sg2d.fillpipe = AAColorViaShape;
                        sg2d.shapepipe = AAColorShape;
                        sg2d.textpipe = colorText;
                    }
                    usingfill = true;
                }
            }
            if (!usingfill) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.drawpipe = AAClipPaintViaShape;
                    sg2d.fillpipe = AAClipPaintViaShape;
                    sg2d.shapepipe = AAClipPaintShape;
                    sg2d.textpipe = clipPaintText;
                } else {
                    sg2d.drawpipe = AAPaintViaShape;
                    sg2d.fillpipe = AAPaintViaShape;
                    sg2d.shapepipe = AAPaintShape;
                    sg2d.textpipe = paintText;
                }
            }
        } else if (sg2d.paintState != sg2d.PAINT_SOLIDCOLOR ||
                   sg2d.compositeState != sg2d.COMP_ISCOPY ||
                   sg2d.clipState == sg2d.CLIP_SHAPE)
        {
            sg2d.drawpipe = paintViaShape;
            sg2d.fillpipe = paintViaShape;
            sg2d.shapepipe = paintShape;
            boolean usingfill = false;
            if (sg2d.paintState != sg2d.PAINT_TILE &&
                sg2d.compositeState != sg2d.COMP_CUSTOM)
            {
                if (sg2d.alphafill == null) {
                    sg2d.alphafill =
                        MaskFill.getFromCache(SurfaceType.AnyColor,
                                              sg2d.fillComp,
                                              getSurfaceType());
                    // assertion: sg2d.surfaceData == this
                }
                if (sg2d.alphafill != null) {
                    if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                        sg2d.textpipe = clipColorText;
                    } else {
                        sg2d.textpipe = colorText;
                    }
                    usingfill = true;
                }
            }
            if (!usingfill) {
                if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                    sg2d.textpipe = clipPaintText;
                } else {
                    sg2d.textpipe = paintText;
                }
            }
        } else {
            if (sg2d.transformState > sg2d.TRANSFORM_TRANSLATEONLY) {
                sg2d.drawpipe = colorViaShape;
                sg2d.fillpipe = colorViaShape;
            } else {
                if (sg2d.strokeState != sg2d.STROKE_THIN) {
                    sg2d.drawpipe = colorViaShape;
                } else {
                    sg2d.drawpipe = colorPrimitives;
                }
                sg2d.fillpipe = colorPrimitives;
            }
            if (sg2d.textAntialiasHint == SunHints.INTVAL_TEXT_ANTIALIAS_ON) {
                sg2d.textpipe = aaTextRenderer;
            } else {
                sg2d.textpipe = solidTextRenderer;
            }
            sg2d.shapepipe = colorPrimitives;
            sg2d.loops = getRenderLoops(sg2d);
            // assertion: sg2d.surfaceData == this
        }
    }

    private static RenderCache loopcache = new RenderCache(30);

    /**
     * Return a RenderLoops object containing all of the basic
     * GraphicsPrimitive objects for rendering to the destination
     * surface with the current attributes of the given SunGraphics2D.
     */
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        SurfaceType src;
        switch (sg2d.paintState) {
        case SunGraphics2D.PAINT_SOLIDCOLOR:
            src = SurfaceType.OpaqueColor;
            break;
        case SunGraphics2D.PAINT_SINGLECOLOR:
            src = SurfaceType.AnyColor;
            break;
        default:
        case SunGraphics2D.PAINT_TILE:
            // REMIND: Distinguish Gradient and Texture fills...
            src = SurfaceType.AnyPaint;
            break;
        }
        CompositeType comp = sg2d.fillComp;
        SurfaceType dst = sg2d.getSurfaceData().getSurfaceType();

        Object o = loopcache.get(src, comp, dst);
        if (o != null) {
            return (RenderLoops) o;
        }

        RenderLoops loops = makeRenderLoops(src, comp, dst);
        loopcache.put(src, comp, dst, loops);
        return loops;
    }

    /**
     * Construct and return a RenderLoops object containing all of
     * the basic GraphicsPrimitive objects for rendering to the
     * destination surface with the given source, destination, and
     * composite types.
     */
    public static RenderLoops makeRenderLoops(SurfaceType src,
                                              CompositeType comp,
                                              SurfaceType dst)
    {
        RenderLoops loops = new RenderLoops();
        loops.drawLineLoop = DrawLine.locate(src, comp, dst);
        loops.fillRectLoop = FillRect.locate(src, comp, dst);
        loops.drawRectLoop = DrawRect.locate(src, comp, dst);
        loops.drawPolygonsLoop = DrawPolygons.locate(src, comp, dst);
        loops.fillSpansLoop = FillSpans.locate(src, comp, dst);
        loops.drawGlyphListLoop = DrawGlyphList.locate(src, comp, dst);
        loops.drawGlyphListAALoop = DrawGlyphListAA.locate(src, comp, dst);
        /*
        System.out.println("drawLine: "+loops.drawLineLoop);
        System.out.println("fillRect: "+loops.fillRectLoop);
        System.out.println("drawRect: "+loops.drawRectLoop);
        System.out.println("drawPolygons: "+loops.drawPolygonsLoop);
        System.out.println("fillSpans: "+loops.fillSpansLoop);
        System.out.println("drawGlyphList: "+loops.drawGlyphListLoop);
        System.out.println("drawGlyphListAA: "+loops.drawGlyphListAALoop);
        */
        return loops;
    }

    public abstract void lock() throws InvalidPipeException;

    public abstract void unlock();

    /**
     * Return the GraphicsConfiguration object that describes this
     * destination surface.
     */
    public abstract GraphicsConfiguration getDeviceConfiguration();

    /**
     * Return the SurfaceType object that describes the destination
     * surface.
     */
    public final SurfaceType getSurfaceType() {
        return surfaceType;
    }

    /**
     * Return the ColorModel for the destination surface.
     */
    public final ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * Returns the type of this <code>Transparency</code>.
     * @return the field type of this <code>Transparency</code>, which is
     *          either OPAQUE, BITMASK or TRANSLUCENT.
     */
    public int getTransparency() {
        return getColorModel().getTransparency();
    }

    /**
     * Return a readable Raster which contains the pixels for the
     * specified rectangular region of the destination surface.
     * The coordinate origin of the returned Raster is the same as
     * the device space origin of the destination surface.
     * In some cases the returned Raster might also be writeable.
     * In most cases, the returned Raster might contain more pixels
     * than requested.
     *
     * @see useTightBBoxes
     */
    public abstract Raster getRaster(int x, int y, int w, int h);

    /**
     * Does the pixel accessibility of the destination surface
     * suggest that rendering algorithms might want to take
     * extra time to calculate a more accurate bounding box for
     * the operation being performed?
     * The typical case when this will be true is when a copy of
     * the pixels has to be made when doing a getRaster.  The
     * fewer pixels copied, the faster the operation will go.
     *
     * @see getRaster
     */
    public boolean useTightBBoxes() {
        // Note: The native equivalent would trigger on VISIBLE_TO_NATIVE
        // REMIND: This is not used - should be obsoleted maybe
        return true;
    }

    /**
     * Returns the pixel data for the specified Argb value packed
     * into an integer for easy storage and conveyance.
     */
    public int pixelFor(int rgb) {
        return surfaceType.pixelFor(rgb, colorModel);
    }

    /**
     * Returns the pixel data for the specified color packed into an
     * integer for easy storage and conveyance.
     *
     * This method will use the getRGB() method of the Color object
     * and defer to the pixelFor(int rgb) method if not overridden.
     *
     * For now this is a convenience function, but for cases where
     * the highest quality color conversion is requested, this method
     * should be overridden in those cases so that a more direct
     * conversion of the color to the destination color space
     * can be done using the additional information in the Color
     * object.
     */
    public int pixelFor(Color c) {
        return pixelFor(c.getRGB());
    }

    /**
     * Returns the Argb representation for the specified integer value
     * which is packed in the format of the associated ColorModel.
     */
    public int rgbFor(int pixel) {
        return surfaceType.rgbFor(pixel, colorModel);
    }

    /**
     * Returns the bounds of the destination surface.
     */
    public abstract Rectangle getBounds();

    static java.security.Permission compPermission;

    /**
     * Performs Security Permissions checks to see if a Custom
     * Composite object should be allowed access to the pixels
     * of this surface.
     */
    protected void checkCustomComposite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (compPermission == null) {
                compPermission =
                    new java.awt.AWTPermission("readDisplayPixels");
            }
            sm.checkPermission(compPermission);
        }
    }

    /**
     * Fetches private field IndexColorModel.allgrayopaque
     * which is true when all palette entries in the color
     * model are gray and opaque.
     */
    protected static native boolean isOpaqueGray(IndexColorModel icm);

    /**
     * Performs a copyarea within this surface.  Returns
     * false if there is no algorithm to perform the copyarea
     * given the current settings of the SunGraphics2D.
     */
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        return false;
    }

}

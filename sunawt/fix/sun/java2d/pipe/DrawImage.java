/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)DrawImage.java   1.19 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.java2d.pipe;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import sun.awt.SunHints;
import sun.java2d.InvalidPipeException;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.Blit;
import sun.java2d.loops.BlitBg;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.ScaledBlit;
import sun.java2d.loops.SurfaceType;
import sun.awt.image.AcceleratedOffScreenImage;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.OffScreenImage;


public class DrawImage implements DrawImagePipe
{

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor) {
        if (sg.transformState < sg.TRANSFORM_TRANSLATESCALE) {
            return copyImage(sg, img, x, y, 0, 0,
                             img.getWidth(null), img.getHeight(null),
                             bgColor);
        }
        transformImage(sg, img, x, y, null, null, bgColor);
        return true;
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor) {
        if (sg.transformState < sg.TRANSFORM_TRANSLATESCALE) {
            SurfaceData sData =
                SurfaceData.getSurfaceDataFromImage(img, sg.surfaceData,
                                                    sg.imageComp, bgColor, false);
            if (sData != null) {
                renderSurfaceData(sg, sData, bgColor,
                                  dx + sg.transX, dy + sg.transY,
                                  sx, sy, w, h);
                return true;
            } else {
                return false;
            }
        }
        scaleImage(sg, img, dx, dy, (dx + w), (dy + h),
                   sx, sy, (sx + w), (sy + h), bgColor);
        return true;
    }

    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y,
                              int width, int height,
                              Color bgColor) {
        sg.getCompClip();
        // Only accelerate scale if:
        //          - w/h positive values
        //          - sg transform translate/identity only
        //          - no bgColor in operation
        if ((width > 0)                                    &&
            (height > 0)                                   &&
            (sg.transformState < sg.TRANSFORM_GENERIC))
        {
            SurfaceData sData =
                SurfaceData.getSurfaceDataFromImage(img, sg.surfaceData,
                                                    sg.imageComp, bgColor, true);
            if (!isBgOperation(sData, bgColor)) {
                SurfaceType src = sData.getSurfaceType();
                SurfaceType dst = sg.surfaceData.getSurfaceType();
                if (scaleSurfaceData(sg, sData, sg.surfaceData, src, dst,
                                     x, y, 0, 0,
                                     width, height,
                                     img.getWidth(null), img.getHeight(null),
                                     null))
                {
                    return true;
                }
            }
        }
        AffineTransform atfm = getTransform(img, x, y, width, height);
        transformImage(sg, img, 0, 0, null, atfm, bgColor);
        return true;
    }

    protected AffineTransform getTransform(Image img, int x, int y,
                                           int width, int height) {
        AffineTransform atfm = null;
        if (width != img.getWidth(null) || height != img.getHeight(null) ||
            x != 0 || y != 0) {
            atfm = new AffineTransform();
            atfm.translate(x, y);
            atfm.scale((double)width/img.getWidth(null),
                       (double)height/img.getHeight(null));
        }
        return atfm;
    }

    protected void transformImage(SunGraphics2D sg, Image img, int x, int y,
                                  BufferedImageOp op,
                                  AffineTransform xf, Color bgColor) {
        Region clip = sg.getCompClip();
        int interpolationType = sg.interpolationHint;
        // fast case most common call
        if ((op == null)                            &&
            (sg.transformState != sg.TRANSFORM_GENERIC))
        {
            if ((sg.transformState < sg.TRANSFORM_TRANSLATESCALE) &&
                ((xf == null) || (xf.isIdentity())))
            {
                copyImage(sg, img, x, y, 0, 0,
                          img.getWidth(null), img.getHeight(null), bgColor);
                return;
            } else if ((xf == null ||
                        ((xf.getType() &
                         (AffineTransform.TYPE_FLIP |
                          AffineTransform.TYPE_MASK_ROTATION |
                          AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0)))
            {
                // Must be a simple scale - we can handle this
                SurfaceData sData =
                    SurfaceData.getSurfaceDataFromImage(img, sg.surfaceData,
                                                        sg.imageComp, bgColor, true);
                if (!isBgOperation(sData, bgColor)) {
                    SurfaceType src = sData.getSurfaceType();
                    SurfaceType dst = sg.surfaceData.getSurfaceType();
                    int dstX = x;
                    int dstY = y;
                    int dstW = img.getWidth(null);
                    int dstH = img.getHeight(null);
                    double coords[] = null;
                    if (xf != null) {
                        coords = new double[] {
                            dstX, dstY, dstX+dstW, dstY+dstH,
                        };
                        xf.transform(coords, 0, coords, 0, 2);
                        // Round to integer coordinates first,
                        // then subtract to get w/h
                        dstX = (int)(coords[0] + 0.5);
                        dstY = (int)(coords[1] + 0.5);
                        dstW = (int)(coords[2] + 0.5);
                        dstH = (int)(coords[3] + 0.5);
                        dstW -= dstX;
                        dstH -= dstY;
                    }
                    if (scaleSurfaceData(sg, sData, sg.surfaceData, src, dst,
                                         dstX, dstY, 0, 0,
                                         dstW, dstH,
                                         img.getWidth(null),
                                         img.getHeight(null), coords))
                    {
                        return;
                    }
                }
            }
        }

        // Must not have been acceleratable as a simple scale - do this
        // as a general transform
        BufferedImage bImg = getBufferedImage(img);
        AffineTransform opXform = null;
        if (op != null) {
            if (op instanceof AffineTransformOp) {
                AffineTransformOp atop = (AffineTransformOp) op;
                opXform = atop.getTransform();
                interpolationType = atop.getInterpolationType();
            }
            else {
                bImg = op.filter(bImg, null);
            }
        }

        int bImgWidth = bImg.getWidth();
        int bImgHeight = bImg.getHeight();

        // Begin Transform
        AffineTransform tx = new AffineTransform(sg.transform);
        tx.translate(x, y);
        if (xf != null && !xf.isIdentity()) {
            tx.concatenate(xf);
        }
        if (opXform != null && !opXform.isIdentity()) {
            tx.concatenate(opXform);
        }

        double mat[] = new double[6];
        tx.getMatrix(mat);

        // Only a translation
        if (mat[0] == 1. && mat[1] == 0. && mat[2] == 0. && mat[3] == 1.) {
            x = (int) Math.round(mat[4]);
            y = (int) Math.round(mat[5]);
            Region bounds = Region.getInstanceXYWH(x, y, bImgWidth, bImgHeight);
            clip = clip.getBoundsIntersection(bounds);
            if (clip.isEmpty()) {
                return;
            }
            if (!clip.encompasses(bounds)) {
                bImgWidth = clip.getWidth();
                bImgHeight = clip.getHeight();
                bImg = bImg.getSubimage(clip.getLoX() - x, clip.getLoY() - y,
                                        bImgWidth, bImgHeight);
            }
        } else {
            int hint = interpolationType;
            if (interpolationType == -1) {
                hint = (sg.renderHint == SunHints.INTVAL_RENDER_QUALITY
                        ? AffineTransformOp.TYPE_BILINEAR
                        : AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            }

            // MediaLib/AffineTransformOp handle indexed images poorly in
            // the bilinear case, so we will convert the source image into
            // an INT_ARGB_PRE image (MediaLib produces better results with
            // premultiplied alpha)
            // REMIND: this and other similar special case hacks should go
            // away in the future...
            if ((hint != AffineTransformOp.TYPE_NEAREST_NEIGHBOR) &&
                (bImg.getColorModel() instanceof IndexColorModel))
            {
                BufferedImage tmpBI =
                    new BufferedImage(bImg.getWidth(), bImg.getHeight(),
                                      BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D g2d = tmpBI.createGraphics();

                try {
                    g2d.setComposite(AlphaComposite.Src);
                    g2d.drawImage(bImg, 0, 0, null);
                } finally {
                    g2d.dispose();
                }

                bImg = tmpBI;
            }

            ColorModel dstCM = getTransformColorModel(sg, bImg, tx);
            AffineTransformOp affOp = new AffineTransformOp(tx, hint);
            // Transform the bounding box of the image in order to
            // extract the translation and to figure out the destination
            // width and height
            Rectangle bounds = affOp.getBounds2D(bImg).getBounds();
            clip = clip.getBoundsIntersection(bounds);
            if (clip.isEmpty()) {
                return;
            }

            x = clip.getLoX();
            y = clip.getLoY();
            bImgWidth  = clip.getWidth();
            bImgHeight = clip.getHeight();

            if (x != 0 || y != 0) {
                tx.preConcatenate(new AffineTransform(1.,0.,0.,1., -x, -y));
                affOp = new AffineTransformOp(tx, hint);
            }

            // Create a new dst image.
            BufferedImage dst;
            if (dstCM == bImg.getColorModel()) {
                // It is better to use Raster.createCompatibleWritableRaster
                // because the equivalent call in ColorModel cannot
                // figure out if the image is, for example, RGB vs BGR.
                // This is the fix for bug 4245031.
                dst = new BufferedImage(dstCM,
                  bImg.getRaster().createCompatibleWritableRaster(bImgWidth,
                                                                  bImgHeight),
                                        dstCM.isAlphaPremultiplied(), null);
            }
            else {
                dst = new BufferedImage(dstCM,
                             dstCM.createCompatibleWritableRaster(bImgWidth,
                                                                  bImgHeight),
                             dstCM.isAlphaPremultiplied(), null);
            }

            // Now transform the image by the new transform.
            bImg = affOp.filter(bImg, dst);
            if (bImg == null) {
                return;
            }
        }

        // End Transform
        SurfaceData sData;

        if (bImg instanceof OffScreenImage) {
            // If bImg is an OffScreenImage, then there may be a hardware-
            // accelerated sd to use.  The image that contains sData will
            // return the best surface data object to use in this situation.
            // Pass in the destination of the copy (surfaceData) to help
            // determine the best source sd.
            sData = ((OffScreenImage)bImg).getSourceSurfaceData(sg.surfaceData,
                                                                sg.imageComp,
                                                                bgColor, false);
        } else {
            sData = BufImgSurfaceData.createData(bImg);
        }
        /* REMIND: Should avoid a getSubImage above by preserving srcx, srcy */
        renderSurfaceData(sg, sData, bgColor,
                          clip.getLoX(), clip.getLoY(),
                          0, 0,
                          clip.getWidth(), clip.getHeight());
    }

    public static void renderSurfaceData(SunGraphics2D sg,
                                         SurfaceData sData,
                                         Color bgColor,
                                         int dx, int dy,
                                         int sx, int sy,
                                         int w, int h)
    {
        int attempts = 0;
        // Loop up to twice through; this gives us a chance to
        // revalidate the surfaceData objects in case of an exception
        // and try it once more
        while (true) {
            try {
                SurfaceType src = sData.getSurfaceType();
                SurfaceType dst = sg.surfaceData.getSurfaceType();
                blitSurfaceData(sg, sData, sg.surfaceData, src, dst,
                                sx, sy, dx, dy, w, h, bgColor);
                return;
            } catch (NullPointerException e) {
                if (!SurfaceData.isNull(sg.surfaceData) &&
                    !SurfaceData.isNull(sData)) {
                    // Something else caused the exception, throw it...
                    throw e;
                }
                return;
                // NOP if we have been disposed
            } catch (InvalidPipeException e) {
                // Always catch the exception; try this a couple of times
                // and fail silently if the system is not yet ready to
                // revalidate the source or dest surfaceData objects.
                ++attempts;
                sData = sData.getReplacement();
                sg.getCompClip();       // ensures sg.surfaceData is valid
                if (SurfaceData.isNull(sg.surfaceData) ||
                    SurfaceData.isNull(sData) || (attempts > 1)) {
                    return;
                }
            }
        }
    }

    /**
     * Draws a subrectangle of an image scaled to a destination rectangle in
     * nonblocking mode with a solid background color and a callback object.
     */
    protected boolean scaleBufferedImage(SunGraphics2D sg, BufferedImage bImg,
                                         Color bgColor,
                                         int dx1, int dy1, int dx2, int dy2,
                                         int sx1, int sy1, int sx2, int sy2,
                                         int xOrig, int yOrig,
                                         int xNew, int yNew,
                                         int width, int height) {
        AffineTransform atfm = null;
        if ((dx2-dx1 != sx2-sx1) || (dy2-dy1 != sy2-sy1)) {
            // Append the new transform
            atfm = new AffineTransform();
            atfm.translate( dx1, dy1);
            double m00 = (double)(dx2-dx1)/(sx2-sx1);
            double m11 = (double)(dy2-dy1)/(sy2-sy1);
            atfm.scale(m00, m11);
            atfm.translate(xOrig-sx1, yOrig-sy1);
            xNew = yNew = 0;
        }
        if (xOrig >= 0 && yOrig >=0) {
            int bw = bImg.getWidth();
            int bh = bImg.getHeight();

            // Make sure we are not out of bounds
            if (xOrig + width > bw) {
                width = bw - xOrig;
            }
            if (yOrig + height > bh) {
                height = bh - yOrig;
            }

            // The actual semantics of this method is to draw the image
            // up to, but not including the width and height.  We need
            // to include the width and height in the source image so
            // that the interpolation will have enough data to fill in
            // the last pixel(s).  We subtract the last point in the
            // bounding box rectangle in renderingPipeImage.
            bImg = bImg.getSubimage(xOrig+bImg.getMinX(),
                                    yOrig+bImg.getMinY(), width, height);
        }

        transformImage(sg, bImg, xNew, yNew, null, atfm, bgColor);
        return true;
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor) {
        int width, height;
        int srcW, srcH, dstW, dstH;
        int srcX, srcY, dstX, dstY;
        boolean srcWidthFlip = false;
        boolean srcHeightFlip = false;
        boolean dstWidthFlip = false;
        boolean dstHeightFlip = false;

        if (sx2 > sx1) {
            srcW = sx2 - sx1;
            srcX = sx1;
        }
        else {
            srcWidthFlip = true;
            srcW = sx1 - sx2;
            srcX = sx2;
        }
        if (sy2 > sy1) {
            srcH = sy2-sy1;
            srcY = sy1;
        }
        else {
            srcHeightFlip = true;
            srcH = sy1-sy2;
            srcY = sy2;
        }
        if (dx2 > dx1) {
            dstW = dx2 - dx1;
            dstX = dx1;
        }
        else {
            dstW = dx1 - dx2;
            dstWidthFlip = true;
            dstX = dx2;
        }
        if (dy2 > dy1) {
            dstH = dy2 - dy1;
            dstY = dy1;
        }
        else {
            dstH = dy1 - dy2;
            dstHeightFlip = true;
            dstY = dy2;
        }
        if (srcW <= 0 || srcH <=0 ) {
            return true;
        }
        // Only accelerate scale if it doesn not involve a flip,
        // transform, or shape clip
        if ((srcWidthFlip == dstWidthFlip)                  &&
            (srcHeightFlip == dstHeightFlip)                &&
            (sg.transformState < sg.TRANSFORM_GENERIC))
        {
            SurfaceData sData =
                SurfaceData.getSurfaceDataFromImage(img, sg.surfaceData,
                                                    sg.imageComp, bgColor, true);
            if (!isBgOperation(sData, bgColor)) {
                // only accelerate scale if there is no bg color involved
                SurfaceType src = sData.getSurfaceType();
                SurfaceType dst = sg.surfaceData.getSurfaceType();
                if (scaleSurfaceData(sg, sData, sg.surfaceData, src, dst,
                                     dstX, dstY, srcX, srcY,
                                     dstW, dstH,
                                     srcW, srcH, null))
                {
                    return true;
                }
            }
        }
        BufferedImage bImg = getBufferedImage(img);
        return scaleBufferedImage(sg, bImg, bgColor, dx1, dy1, dx2, dy2,
                                  sx1, sy1, sx2, sy2, srcX, srcY,
                                  dstX, dstY, srcW, srcH);
    }

    /**
     ** Utilities
     ** The following methods are used by the public methods above
     ** for performing various operations
     **/

    protected static boolean isBgOperation(SurfaceData srcData, Color bgColor) {
        // If we cannot get the sData, then cannot assume anything about
        // the image
        return ((srcData == null) ||
                ((bgColor != null) &&
                 (srcData.getTransparency() != Transparency.OPAQUE)));
    }

    protected BufferedImage getBufferedImage(Image img) {
        if (img instanceof AcceleratedOffScreenImage) {
            return ((AcceleratedOffScreenImage)img).getSnapshot();
        } else if (img instanceof BufferedImage) {
            return (BufferedImage)img;
        }
        // Must be VolatileImage; get BufferedImage representation
        return ((VolatileImage)img).getSnapshot();
    }

    /*
     * Return the color model to be used with this BufferedImage and
     * transform.
     */
    private ColorModel getTransformColorModel(SunGraphics2D sg,
                                              BufferedImage bImg,
                                              AffineTransform tx) {
        ColorModel cm = bImg.getColorModel();
        ColorModel dstCM = cm;

        if (tx.isIdentity()) {
            return dstCM;
        }
        int type = tx.getType();
        boolean needTrans =
            ((type&(tx.TYPE_MASK_ROTATION|tx.TYPE_GENERAL_TRANSFORM)) != 0);
        if (! needTrans && type != tx.TYPE_TRANSLATION && type != tx.TYPE_IDENTITY)
        {
            double[] mtx = new double[4];
            tx.getMatrix(mtx);
            // Check out the matrix.  A non-integral scale will force ARGB
            // since the edge conditions cannot be guaranteed.
            needTrans = (mtx[0] != (int)mtx[0] || mtx[3] != (int)mtx[3]);
        }

        if (sg.renderHint != SunHints.INTVAL_RENDER_QUALITY) {
            if (cm instanceof IndexColorModel) {
                Raster raster = bImg.getRaster();
                IndexColorModel icm = (IndexColorModel) cm;
                // Just need to make sure that we have a transparent pixel
                if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                    // Fix 4221407
                    if (raster instanceof sun.awt.image.BytePackedRaster) {
                        dstCM = ColorModel.getRGBdefault();
                    }
                    else {
                        double[] matrix = new double[6];
                        tx.getMatrix(matrix);
                        if (matrix[1] == 0. && matrix[2] ==0.
                            && matrix[4] == 0. && matrix[5] == 0.) {
                            // Only scaling so do not need to create
                        }
                        else {
                            int mapSize = icm.getMapSize();
                            if (mapSize < 256) {
                                int[] cmap = new int[mapSize+1];
                                icm.getRGBs(cmap);
                                cmap[mapSize] = 0x0000;
                                dstCM = new
                                    IndexColorModel(icm.getPixelSize(),
                                                    mapSize+1,
                                                    cmap, 0, true, mapSize,
                                                    DataBuffer.TYPE_BYTE);
                            }
                            else {
                                dstCM = ColorModel.getRGBdefault();
                            }
                        }  /* if (matrix[0] < 1.f ...) */
                    }   /* raster instanceof sun.awt.image.BytePackedRaster */
                } /* if (cm.getTransparency() == cm.OPAQUE) */
            } /* if (cm instanceof IndexColorModel) */
            else if (needTrans && cm.getTransparency() == cm.OPAQUE) {
                // Need a bitmask transparency
                // REMIND: for now, use full transparency since no loops
                // for bitmask
                dstCM = ColorModel.getRGBdefault();
            }
        } /* if (sg.renderHint == RENDER_QUALITY) */
        else {

            if (cm instanceof IndexColorModel ||
                (needTrans && cm.getTransparency() == cm.OPAQUE))
            {
                // Need a bitmask transparency
                // REMIND: for now, use full transparency since no loops
                // for bitmask
                dstCM = ColorModel.getRGBdefault();
            }
        }

        return dstCM;
    }

    protected static void blitSurfaceData(SunGraphics2D sg,
                                          SurfaceData srcData,
                                          SurfaceData dstData,
                                          SurfaceType srcType,
                                          SurfaceType dstType,
                                          int sx, int sy, int dx, int dy,
                                          int w, int h,
                                          Color bgColor) {
        if (w <= 0 || h <= 0) {
            /*
             * Fix for bugid 4783274 - BlitBg throws an exception for
             * a particular set of anomalous parameters.
             * REMIND: The native loops do proper clipping and would
             * detect this situation themselves, but the Java loops
             * all seem to trust their parameters a little too well
             * to the point where they will try to process a negative
             * area of pixels and throw exceptions.  The real fix is
             * to modify the Java loops to do proper clipping so that
             * they can deal with negative dimensions as well as
             * improperly large dimensions, but that fix is too risky
             * to integrate for Mantis at this point.  In the meantime
             * eliminating the negative or zero dimensions here is
             * "correct" and saves them from some nasty exceptional
             * conditions, one of which is the test case of 4783274.
             */
            return;
        }
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE ||
             bgColor != null))
        {
            comp = CompositeType.SrcNoEa;
        }
        if (!isBgOperation(srcData, bgColor)) {
            Blit blit = Blit.getFromCache(srcType, comp, dstType);
            blit.Blit(srcData, dstData, sg.composite, sg.getCompClip(),
                      sx, sy, dx, dy, w, h);
        } else {
            BlitBg blit = BlitBg.getFromCache(srcType, comp, dstType);
            blit.BlitBg(srcData, dstData, sg.composite, sg.getCompClip(),
                        bgColor, sx, sy, dx, dy, w, h);
        }
    }

    protected boolean areaWithinRect(Rectangle rect, int x, int y,
                                     int width, int height) {
        return ((rect.x <= x) &&
                (rect.y <= y) &&
                ((rect.x + rect.width) >= (x + width)) &&
                ((rect.y + rect.height) >= (y + height)));
    }

    protected boolean scaleSurfaceData(SunGraphics2D sg,
                                       SurfaceData srcData,
                                       SurfaceData dstData,
                                       SurfaceType srcType,
                                       SurfaceType dstType,
                                       int dx, int dy, int sx, int sy,
                                       int dw, int dh, int sw, int sh,
                                       double coords[])
    {
        // We currently punt on non-default interpolationHint or
        // renderHint; we cannot control the rendering quality of accelerated
        // scaling operations.
        if ((sg.interpolationHint != -1) ||
            (sg.renderHint == SunHints.INTVAL_RENDER_QUALITY))
        {
            return false;
        }
        CompositeType comp = sg.imageComp;
        if (CompositeType.SrcOverNoEa.equals(comp) &&
            (srcData.getTransparency() == Transparency.OPAQUE))
        {
            comp = CompositeType.SrcNoEa;
        }
        if (sg.transformState == sg.TRANSFORM_TRANSLATEONLY) {
            dx += sg.transX;
            dy += sg.transY;
        } else if (sg.transformState == sg.TRANSFORM_TRANSLATESCALE) {
            if (coords == null) {
                coords = new double[] { dx, dy, dx+dw, dy+dh };
            }
            sg.transform.transform(coords, 0, coords, 0, 2);
            // Round to integer coordinates first, then subtract for w/h
            dx = (int)(coords[0] + 0.5);
            dy = (int)(coords[1] + 0.5);
            dw = (int)(coords[2] + 0.5);
            dh = (int)(coords[3] + 0.5);
            dw -= dx;
            dh -= dy;
        }
        ScaledBlit blit = ScaledBlit.getFromCache(srcType, comp, dstType);
        if (blit != null) {
            Region clipRegion = sg.getCompClip();
            if (sg.clipState == sg.CLIP_SHAPE) {
                int box[] = {dx, dy, dx+dw, dy+dh};
                SpanIterator si = clipRegion.getSpanIterator(box);
                while (si.nextSpan(box)) {
                    blit.Scale(srcData, dstData, sg.composite,
                               sx, sy, dx, dy, sw, sh, dw, dh,
                               box[0], box[1], box[2], box[3]);
                }
            } else {
                blit.Scale(srcData, dstData, sg.composite,
                           sx, sy, dx, dy, sw, sh, dw, dh,
                           clipRegion.getLoX(), clipRegion.getLoY(),
                           clipRegion.getHiX(), clipRegion.getHiY());
            }
            return true;
        }
        return false;
    }

    protected boolean imageReady(sun.awt.image.Image sunimg,
                                 ImageObserver observer) {
        if (sunimg.hasError()) {
            if (observer != null) {
                observer.imageUpdate(sunimg,
                                     ImageObserver.ERROR|ImageObserver.ABORT,
                                     -1, -1, -1, -1);
            }
            return false;
        }
        return true;
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof sun.awt.image.Image)) {
            return copyImage(sg, img, x, y, bgColor);
        } else {
            sun.awt.image.Image sunimg = (sun.awt.image.Image)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, bgColor, observer);
        }
    }

    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor,
                             ImageObserver observer) {
        if (!(img instanceof sun.awt.image.Image)) {
            return copyImage(sg, img, dx, dy, sx, sy, w, h, bgColor);
        } else {
            sun.awt.image.Image sunimg = (sun.awt.image.Image)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, dx, dy, (dx + w), (dy + h),
                                     sx, sy, (sx + w), (sy + h), bgColor, observer);
        }
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                                int x, int y,
                                int width, int height,
                                Color bgColor,
                                ImageObserver observer) {
        if (!(img instanceof sun.awt.image.Image)) {
            return scaleImage(sg, img, x, y, width, height, bgColor);
        } else {
            sun.awt.image.Image sunimg = (sun.awt.image.Image)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, x, y, width, height, bgColor,
                                     observer);
        }
    }

    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor,
                              ImageObserver observer) {
        if (!(img instanceof sun.awt.image.Image)) {
            return scaleImage(sg, img, dx1, dy1, dx2, dy2,
                              sx1, sy1, sx2, sy2, bgColor);
        } else {
            sun.awt.image.Image sunimg = (sun.awt.image.Image)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, dx1, dy1, dx2, dy2,
                                     sx1, sy1, sx2, sy2, bgColor, observer);
        }
    }

    public boolean transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform atfm,
                                  ImageObserver observer) {
        if (!(img instanceof sun.awt.image.Image)) {
            transformImage(sg, img, 0, 0, null, atfm, null);
            return true;
        } else {
            sun.awt.image.Image sunimg = (sun.awt.image.Image)img;
            if (!imageReady(sunimg, observer)) {
                return false;
            }
            ImageRepresentation ir = sunimg.getImageRep();
            return ir.drawToBufImage(sg, sunimg, atfm, observer);
        }
    }

    public void transformImage(SunGraphics2D sg, BufferedImage img,
                                  BufferedImageOp op, int x, int y) {
        transformImage(sg, img, x, y, op, null, null);
    }


}

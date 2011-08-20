/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)PSPathGraphics.java      1.20 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Transparency;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import sun.awt.image.ByteInterleavedRaster;

/**
 * This class converts paths into PostScript
 * by breaking all graphics into fills and
 * clips of paths.
 */

class PSPathGraphics extends PathGraphics {

    /**
     * For a drawing application the initial user space
     * resolution is 72dpi.
     */
    private static final int DEFAULT_USER_RES = 72;

    PSPathGraphics(Graphics2D graphics, PrinterJob printerJob,
                   Printable painter, PageFormat pageFormat, int pageIndex,
                   boolean canRedraw) {
        super(graphics, printerJob, painter, pageFormat, pageIndex, canRedraw);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     *                       this graphics context.
     * @since      JDK1.0
     */
    public Graphics create() {

        return new PSPathGraphics((Graphics2D) getDelegate().create(),
                                  getPrinterJob(),
                                  getPrintable(),
                                  getPageFormat(),
                                  getPageIndex(),
                                  canDoRedraws());
    }


    /**
     * Override the inherited implementation of fill
     * so that we can generate PostScript in user space
     * rather than device space.
     */
    public void fill(Shape s, Color color) {
        deviceFill(s.getPathIterator(new AffineTransform()), color);
    }

    /**
     * Draws the text given by the specified string, using this
     * graphics context's current font and color. The baseline of the
     * first character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     * @param       str      the string to be drawn.
     * @param       x        the <i>x</i> coordinate.
     * @param       y        the <i>y</i> coordinate.
     * @see         java.awt.Graphics#drawBytes
     * @see         java.awt.Graphics#drawChars
     * @since       JDK1.0
     */
    public void drawString(String str, int x, int y) {
        drawString(str, (float) x, (float) y);
    }

    /**
     * Renders the text specified by the specified <code>String</code>,
     * using the current <code>Font</code> and <code>Paint</code> attributes
     * in the <code>Graphics2D</code> context.
     * The baseline of the first character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in the User Space.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, <code>Paint</code>, <code>Font</code> and
     * <code>Composite</code> attributes. For characters in script systems
     * such as Hebrew and Arabic, the glyphs can be rendered from right to
     * left, in which case the coordinate supplied is the location of the
     * leftmost character on the baseline.
     * @param s the <code>String</code> to be rendered
     * @param x,&nbsp;y the coordinates where the <code>String</code>
     * should be rendered
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
     public void drawString(String str, float x, float y) {

        boolean drawnWithPS = false;
        boolean directToPS = getFont().getTransform().isIdentity();

        if (!PSPrinterJob.shapeTextProp && directToPS) {

            PSPrinterJob psPrinterJob = (PSPrinterJob) getPrinterJob();
            if (psPrinterJob.setFont(getFont())) {

                /* Set the text color.
                 * We should not be in this shape printing path
                 * if the application is drawing with non-solid
                 * colors. We should be in the raster path. Because
                 * we are here in the shape path, the cast of the
                 * paint to a Color should be fine.
                 */
                try {
                    psPrinterJob.setColor((Color)getPaint());
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(
                                                "Expected a Color instance");
                }

                psPrinterJob.setTransform(getTransform());
                psPrinterJob.setClip(getClip());

                drawnWithPS = psPrinterJob.textOut(this, str, x, y);
            }
        }

        /* The text could not be converted directly to PS text
         * calls so decompose the text into a shape.
         */
        if (drawnWithPS == false) {
            super.drawString(str, x, y);
        }
    }

     /**
     * Draws as much of the specified image as is currently available.
     * The image is drawn with its top-left corner at
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate
     * space. Transparent pixels in the image do not affect whatever
     * pixels are already there.
     * <p>
     * This method returns immediately in all cases, even if the
     * complete image has not yet been loaded, and it has not been dithered
     * and converted for the current output device.
     * <p>
     * If the image has not yet been completely loaded, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * @param    img the specified image to be drawn.
     * @param    x   the <i>x</i> coordinate.
     * @param    y   the <i>y</i> coordinate.
     * @param    observer    object to be notified as more of
     *                          the image is converted.
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since    JDK1.0
     */
    public boolean drawImage(Image img, int x, int y,
                             ImageObserver observer) {

        return drawImage(img, x, y, null, observer);
    }

    /**
     * Draws as much of the specified image as has already been scaled
     * to fit inside the specified rectangle.
     * <p>
     * The image is drawn inside the specified rectangle of this
     * graphics context's coordinate space, and is scaled if
     * necessary. Transparent pixels do not affect whatever pixels
     * are already there.
     * <p>
     * This method returns immediately in all cases, even if the
     * entire image has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the image observer by calling its <code>imageUpdate</code> method.
     * <p>
     * A scaled version of an image will not necessarily be
     * available immediately just because an unscaled version of the
     * image has been constructed for this output device.  Each size of
     * the image may be cached separately and generated from the original
     * data in a separate image production sequence.
     * @param    img    the specified image to be drawn.
     * @param    x      the <i>x</i> coordinate.
     * @param    y      the <i>y</i> coordinate.
     * @param    width  the width of the rectangle.
     * @param    height the height of the rectangle.
     * @param    observer    object to be notified as more of
     *                          the image is converted.
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since    JDK1.0
     */
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             ImageObserver observer) {

        return drawImage(img, x, y, width, height, null, observer);

    }

    /*
     * Draws as much of the specified image as is currently available.
     * The image is drawn with its top-left corner at
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate
     * space.  Transparent pixels are drawn in the specified
     * background color.
     * <p>
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p>
     * This method returns immediately in all cases, even if the
     * complete image has not yet been loaded, and it has not been dithered
     * and converted for the current output device.
     * <p>
     * If the image has not yet been completely loaded, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * @param    img    the specified image to be drawn.
     * @param    x      the <i>x</i> coordinate.
     * @param    y      the <i>y</i> coordinate.
     * @param    bgcolor the background color to paint under the
     *                   non-opaque portions of the image.
     *                   In this WPathGraphics implementation,
     *                   this parameter can be null in which
     *                   case that background is made a transparent
     *                   white.
     * @param    observer    object to be notified as more of
     *                          the image is converted.
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since    JDK1.0
     */
    public boolean drawImage(Image img, int x, int y,
                             Color bgcolor,
                             ImageObserver observer) {
        boolean result;

        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);

        if (srcWidth <= 0 || srcHeight <= 0) {
            result = false;
        } else {
            result = drawImage(img, x, y, srcWidth, srcHeight, bgcolor, observer);
        }

        return result;
    }

    /**
     * Draws as much of the specified image as has already been scaled
     * to fit inside the specified rectangle.
     * <p>
     * The image is drawn inside the specified rectangle of this
     * graphics context's coordinate space, and is scaled if
     * necessary. Transparent pixels are drawn in the specified
     * background color.
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p>
     * This method returns immediately in all cases, even if the
     * entire image has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * <p>
     * A scaled version of an image will not necessarily be
     * available immediately just because an unscaled version of the
     * image has been constructed for this output device.  Each size of
     * the image may be cached separately and generated from the original
     * data in a separate image production sequence.
     * @param    img       the specified image to be drawn.
     * @param    x         the <i>x</i> coordinate.
     * @param    y         the <i>y</i> coordinate.
     * @param    width     the width of the rectangle.
     * @param    height    the height of the rectangle.
     * @param    bgcolor   the background color to paint under the
     *                         non-opaque portions of the image.
     * @param    observer    object to be notified as more of
     *                          the image is converted.
     * @see      java.awt.Image
     * @see      java.awt.image.ImageObserver
     * @see      java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since    JDK1.0
     */
    public boolean drawImage(Image img, int x, int y,
                             int width, int height,
                             Color bgcolor,
                             ImageObserver observer) {

        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);

        if (srcWidth <= 0 || srcHeight <= 0) {
            result = false;
        } else {
            result = drawImage(img,
                         x, y, x + width, y + height,
                         0, 0, srcWidth, srcHeight,
                         observer);
        }

        return result;
    }

    /**
     * Draws as much of the specified area of the specified image as is
     * currently available, scaling it on the fly to fit inside the
     * specified area of the destination drawable surface. Transparent pixels
     * do not affect whatever pixels are already there.
     * <p>
     * This method returns immediately in all cases, even if the
     * image area to be drawn has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * <p>
     * This method always uses the unscaled version of the image
     * to render the scaled rectangle and performs the required
     * scaling on the fly. It does not use a cached, scaled version
     * of the image for this operation. Scaling of the image from source
     * to destination is performed such that the first coordinate
     * of the source rectangle is mapped to the first coordinate of
     * the destination rectangle, and the second source coordinate is
     * mapped to the second destination coordinate. The subimage is
     * scaled and flipped as needed to preserve those mappings.
     * @param       img the specified image to be drawn
     * @param       dx1 the <i>x</i> coordinate of the first corner of the
     *                    destination rectangle.
     * @param       dy1 the <i>y</i> coordinate of the first corner of the
     *                    destination rectangle.
     * @param       dx2 the <i>x</i> coordinate of the second corner of the
     *                    destination rectangle.
     * @param       dy2 the <i>y</i> coordinate of the second corner of the
     *                    destination rectangle.
     * @param       sx1 the <i>x</i> coordinate of the first corner of the
     *                    source rectangle.
     * @param       sy1 the <i>y</i> coordinate of the first corner of the
     *                    source rectangle.
     * @param       sx2 the <i>x</i> coordinate of the second corner of the
     *                    source rectangle.
     * @param       sy2 the <i>y</i> coordinate of the second corner of the
     *                    source rectangle.
     * @param       observer object to be notified as more of the image is
     *                    scaled and converted.
     * @see         java.awt.Image
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since       JDK1.1
     */
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {

        return drawImage(img,
                         dx1, dy1, dx2, dy2,
                         sx1, sy1, sx2, sy2,
                         null, observer);
    }

    /**
     * Draws as much of the specified area of the specified image as is
     * currently available, scaling it on the fly to fit inside the
     * specified area of the destination drawable surface.
     * <p>
     * Transparent pixels are drawn in the specified background color.
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p>
     * This method returns immediately in all cases, even if the
     * image area to be drawn has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that draws the image notifies
     * the specified image observer.
     * <p>
     * This method always uses the unscaled version of the image
     * to render the scaled rectangle and performs the required
     * scaling on the fly. It does not use a cached, scaled version
     * of the image for this operation. Scaling of the image from source
     * to destination is performed such that the first coordinate
     * of the source rectangle is mapped to the first coordinate of
     * the destination rectangle, and the second source coordinate is
     * mapped to the second destination coordinate. The subimage is
     * scaled and flipped as needed to preserve those mappings.
     * @param       img the specified image to be drawn
     * @param       dx1 the <i>x</i> coordinate of the first corner of the
     *                    destination rectangle.
     * @param       dy1 the <i>y</i> coordinate of the first corner of the
     *                    destination rectangle.
     * @param       dx2 the <i>x</i> coordinate of the second corner of the
     *                    destination rectangle.
     * @param       dy2 the <i>y</i> coordinate of the second corner of the
     *                    destination rectangle.
     * @param       sx1 the <i>x</i> coordinate of the first corner of the
     *                    source rectangle.
     * @param       sy1 the <i>y</i> coordinate of the first corner of the
     *                    source rectangle.
     * @param       sx2 the <i>x</i> coordinate of the second corner of the
     *                    source rectangle.
     * @param       sy2 the <i>y</i> coordinate of the second corner of the
     *                    source rectangle.
     * @param       bgcolor the background color to paint under the
     *                    non-opaque portions of the image.
     * @param       observer object to be notified as more of the image is
     *                    scaled and converted.
     * @see         java.awt.Image
     * @see         java.awt.image.ImageObserver
     * @see         java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since       JDK1.1
     */
    public boolean drawImage(Image img,
                             int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor,
                             ImageObserver observer) {


        int srcWidth = sx2 - sx1;
        int srcHeight = sy2 - sy1;

        if (srcWidth <= 0 || srcHeight <= 0) {
            return false;
        }

        /* Create a transform which describes the changes
         * from the source coordinates to the destination
         * coordinates. The scaling is determined by the
         * ratio of the two rectangles, while the translation
         * comes from the difference of their origins.
         */
        float scalex = (float) (dx2 - dx1) / srcWidth;
        float scaley = (float) (dy2 - dy1) / srcHeight;
        AffineTransform xForm
            = new AffineTransform(scalex,
                                  0,
                                  0,
                                  scaley,
                                  dx1 - (sx1 * scalex),
                                  dy1 - (sy1 * scaley));

        return drawImageToPS(img, xForm, null, bgcolor,
                              sx1, sy1, srcWidth, srcHeight, false);


    }

    /**
     * Draws an image, applying a transform from image space into user space
     * before drawing.
     * The transformation from user space into device space is done with
     * the current transform in the Graphics2D.
     * The given transformation is applied to the image before the
     * transform attribute in the Graphics2D state is applied.
     * The rendering attributes applied include the clip, transform,
     * and composite attributes. Note that the result is
     * undefined, if the given transform is noninvertible.
     * @param img The image to be drawn.
     * @param xform The transformation from image space into user space.
     * @param obs The image observer to be notified as more of the image
     * is converted.
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public boolean drawImage(Image img,
                             AffineTransform xform,
                             ImageObserver obs) {
        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);

        if (srcWidth <= 0 || srcHeight <= 0) {
            result = false;
        } else {
            result = drawImageToPS(img, xform,
                                    null, null,
                                    0, 0,
                                    srcWidth, srcHeight, false);
        }

        return result;
    }

    /**
     * Draws a BufferedImage that is filtered with a BufferedImageOp.
     * The rendering attributes applied include the clip, transform
     * and composite attributes.  This is equivalent to:
     * <pre>
     * img1 = op.filter(img, null);
     * drawImage(img1, new AffineTransform(1f,0f,0f,1f,x,y), null);
     * </pre>
     * @param op The filter to be applied to the image before drawing.
     * @param img The BufferedImage to be drawn.
     * @param x,y The location in user space where the image should be drawn.
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void drawImage(BufferedImage img,
                          BufferedImageOp op,
                          int x,
                          int y) {

        boolean result;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);

        if (op != null) {
            img = op.filter(img, null);
        }
        if (srcWidth <= 0 || srcHeight <= 0) {
            result = false;
        } else {
            AffineTransform xform = new AffineTransform(1f,0f,0f,1f,x,y);
            result = drawImageToPS(img, xform,
                                    null, null,
                                    0, 0,
                                    srcWidth, srcHeight, false);
        }

    }

    /**
     * Draws an image, applying a transform from image space into user space
     * before drawing.
     * The transformation from user space into device space is done with
     * the current transform in the Graphics2D.
     * The given transformation is applied to the image before the
     * transform attribute in the Graphics2D state is applied.
     * The rendering attributes applied include the clip, transform,
     * and composite attributes. Note that the result is
     * undefined, if the given transform is noninvertible.
     * @param img The image to be drawn.
     * @param xform The transformation from image space into user space.
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void drawRenderedImage(RenderedImage img,
                                  AffineTransform xform) {

        BufferedImage bufferedImage = null;
        int srcWidth = img.getWidth();
        int srcHeight = img.getHeight();

        if (img instanceof BufferedImage) {
            bufferedImage = (BufferedImage) img;
        } else {
            bufferedImage = new BufferedImage(srcWidth, srcHeight,
                                              BufferedImage.TYPE_INT_ARGB);
            Graphics2D imageGraphics = bufferedImage.createGraphics();
            imageGraphics.drawRenderedImage(img, xform);
        }

        drawImageToPS(bufferedImage, xform,
                       null, null,
                       0, 0, srcWidth, srcHeight, false);

    }

    /* An optimisation for the special case of ICM images which have
     * bitmask transparency.
     */
    private boolean drawBitmaskImage(Image img,
                                     AffineTransform xform,
                                     BufferedImageOp op, Color bgcolor,
                                     int srcX, int srcY,
                                     int srcWidth, int srcHeight) {

        ColorModel colorModel;
        IndexColorModel icm;
        BufferedImage bufferedImage;
        int [] pixels;

        /* first do a set of checks to see if this is something we
         * can handle in this method. If not return false.
         */

        if (img instanceof BufferedImage) {
            bufferedImage = (BufferedImage) img;
            colorModel = bufferedImage.getColorModel();

        } else if (img instanceof sun.awt.image.Image) {
             sun.awt.image.Image sunImage = (sun.awt.image.Image) img;
            bufferedImage = sunImage.getBufferedImage();
            if (bufferedImage == null) {
                return true;
            }
            colorModel = sunImage.getColorModel();
        } else if (img instanceof java.awt.image.VolatileImage) {
            /* in 1.4 VolatileImage is always opaque so should not reach here
             * for that case. The code here to deal with that case is
             * basically a safeguard in 1.4, but may be invoked in 1.5
             */
            bufferedImage = ((java.awt.image.VolatileImage)img).getSnapshot();
            colorModel = bufferedImage.getColorModel();
        } else {
            return false;
        }

        if (!(colorModel instanceof IndexColorModel)) {
            return false;
        } else {
            icm = (IndexColorModel)colorModel;
        }

        if (colorModel.getTransparency() != ColorModel.BITMASK) {
            return false;
        }

        if (op != null) {
            return false;
        }

        // to be compatible with 1.1 printing which treated b/g colors
        // with alpha 128 as opaque
        if (bgcolor != null && bgcolor.getAlpha() < 128) {
            return false;
        }

        if ((xform.getType()
             & ~( AffineTransform.TYPE_UNIFORM_SCALE
                  | AffineTransform.TYPE_TRANSLATION
                  | AffineTransform.TYPE_QUADRANT_ROTATION
                  )) != 0) {
            return false;
        }

        if ((getTransform().getType()
             & ~( AffineTransform.TYPE_UNIFORM_SCALE
                  | AffineTransform.TYPE_TRANSLATION
                  | AffineTransform.TYPE_QUADRANT_ROTATION
                  )) != 0) {
            return false;
        }



        BufferedImage subImage = null;
        Raster raster = bufferedImage.getRaster();
        int transpixel = icm.getTransparentPixel();
        byte[] alphas = new byte[icm.getMapSize()];
        icm.getAlphas(alphas);
        if (transpixel >= 0) {
            alphas[transpixel] = 0;
        }

        /* don't just use srcWidth & srcHeight from application - they
         * may exceed the extent of the image - may need to clip.
         * The image xform will ensure that points are still mapped properly.
         */
        int rw = raster.getWidth();
        int rh = raster.getHeight();
        if (srcX > rw || srcY > rh) {
            return false;
        }
        int right, bottom, wid, hgt;
        if (srcX+srcWidth > rw) {
            right = rw;
            wid = right - srcX;
        } else {
            right = srcX+srcWidth;
            wid = srcWidth;
        }
        if (srcY+srcHeight > rh) {
            bottom = rh;
            hgt = bottom - srcY;
        } else {
            bottom = srcY+srcHeight;
            hgt = srcHeight;
        }
        pixels = new int[wid];
        for (int j=srcY; j<bottom; j++) {
            int startx = -1;
            raster.getPixels(srcX, j, wid, 1, pixels);
            for (int i=srcX; i<right; i++) {
                if (alphas[pixels[i-srcX]] == 0) {
                    if (startx >=0) {
                        subImage = bufferedImage.getSubimage(startx, j,
                                                             i-startx, 1);
                        xform.translate(startx, j);
                        drawImageToPS(subImage, xform, op, bgcolor,
                                      0, 0, i-startx, 1, true);
                        xform.translate(-startx, -j);
                        startx = -1;
                    }
                } else if (startx < 0) {
                    startx = i;
                }
            }
            if (startx >= 0) {
                subImage = bufferedImage.getSubimage(startx, j,
                                                     right - startx, 1);
                xform.translate(startx, j);
                drawImageToPS(subImage, xform, op, bgcolor,
                              0, 0, right - startx, 1, true);
                xform.translate(-startx, -j);
            }
        }
        return true;
    }

    /**
     * The various <code>drawImage()</code> methods for
     * <code>WPathGraphics</code> are all decomposed
     * into an invocation of <code>drawImageToPS</code>.
     * The portion of the passed in image defined by
     * <code>srcX, srcY, srcWidth, and srcHeight</code>
     * is transformed by the supplied AffineTransform and
     * drawn using PS to the printer context.
     *
     * @param   img     The image to be drawn.
     * @param   xform   Used to tranform the image before drawing.
     *                  This can be null.
     * @param   op      Specifies an image operator to be used when
     *                  drawing the image. This parameter can be null.
     * @param   bgcolor This color is drawn where the image has transparent
     *                  pixels. If this parameter is null then the
     *                  pixels already in the destination should show
     *                  through.
     * @param   srcX    With srcY this defines the upper-left corner
     *                  of the portion of the image to be drawn.
     *
     * @param   srcY    With srcX this defines the upper-left corner
     *                  of the portion of the image to be drawn.
     * @param   srcWidth    The width of the portion of the image to
     *                      be drawn.
     * @param   srcHeight   The height of the portion of the image to
     *                      be drawn.
     * @param   handlingTransparency if being recursively called to
     *                    print opaque region of transparent image
     */
    private boolean drawImageToPS(Image img, AffineTransform xform,
                                BufferedImageOp op, Color bgcolor,
                                int srcX, int srcY,
                                int srcWidth, int srcHeight,
                                boolean handlingTransparency) {

        if (img instanceof sun.awt.image.Image) {
            sun.awt.image.Image sunImage = (sun.awt.image.Image) img;
            if (sunImage.getBufferedImage() == null) {
                return false;
            }
        }

         PSPrinterJob psPrinterJob = (PSPrinterJob) getPrinterJob();

        /* The full transform to be applied to the image is the
         * caller's transform concatenated on to the transform
         * from user space to device space. If the caller didn't
         * supply a transform then we just act as if they passed
         * in the identify transform.
         */
        AffineTransform fullTransform = getTransform();
        if (xform == null) {
            xform = new AffineTransform();
        }
        fullTransform.concatenate(xform);

        /* Split the full transform into a pair of
         * transforms. The first transform holds effects
         * such as rotation and shearing. The second transform
         * is setup to hold only the scaling effects.
         * These transforms are created such that a point,
         * p, in user space, when transformed by 'fullTransform'
         * lands in the same place as when it is transformed
         * by 'rotTransform' and then 'scaleTransform'.
         *
         * The entire image transformation is not in Java in order
         * to minimize the amount of memory needed in the VM. By
         * dividing the transform in two, we rotate and shear
         * the source image in its own space and only go to
         * the, usually, larger, device space when we ask
         * PostScript to perform the final scaling.
         */
        double[] fullMatrix = new double[6];
        fullTransform.getMatrix(fullMatrix);

        /* Calculate the amount of scaling in the x
         * and y directions. This scaling is computed by
         * transforming a unit vector along each axis
         * and computing the resulting magnitude.
         * The computed values 'scaleX' and 'scaleY'
         * represent the amount of scaling PS will be asked
         * to perform.
         */
        Point2D.Float unitVectorX = new Point2D.Float(1, 0);
        Point2D.Float unitVectorY = new Point2D.Float(0, 1);
        fullTransform.deltaTransform(unitVectorX, unitVectorX);
        fullTransform.deltaTransform(unitVectorY, unitVectorY);

        Point2D.Float origin = new Point2D.Float(0, 0);
        double scaleX = unitVectorX.distance(origin);
        double scaleY = unitVectorY.distance(origin);

        /* We do not need to draw anything if either scaling
         * factor is zero.
         */
        if (scaleX != 0 && scaleY != 0) {

            /* Here's the transformation we will do with Java2D,
            */
            fullMatrix[0] /= scaleX; //m00
            fullMatrix[1] /= scaleY; //m10
            fullMatrix[2] /= scaleX; //m01
            fullMatrix[3] /= scaleY; //m11
            fullMatrix[4] /= scaleX; //m02
            fullMatrix[5] /= scaleY; //m12
            for (int i = 0; i < 6; i++) {
                double val = Math.floor(fullMatrix[i] + 0.5);
                if (Math.abs(fullMatrix[i] - val) < 0.0001) {
                    fullMatrix[i] = val;
                }
            }
            AffineTransform rotTransform = new AffineTransform(fullMatrix);

            /* The scale transform is not used directly: we instead
             * directly multiply by scaleX and scaleY.
             *
             * Conceptually here is what the scaleTransform is:
             *
             * AffineTransform scaleTransform = new AffineTransform(
             *                      scaleX,                     //m00
             *                      0,                          //m10
             *                      0,                          //m01
             *                      scaleY,                     //m11
             *                      0,                          //m02
             *                      0);                         //m12
             */

            /* Convert the image source's rectangle into the rotated
             * and sheared space. Once there, we calculate a rectangle
             * that encloses the resulting shape. It is this rectangle
             * which defines the size of the BufferedImage we need to
             * create to hold the transformed image.
             */
            Rectangle2D.Float srcRect = new Rectangle2D.Float(srcX, srcY,
                                                              srcWidth,
                                                              srcHeight);

            Shape rotShape = rotTransform.createTransformedShape(srcRect);
            Rectangle2D rotBounds = rotShape.getBounds2D();

            /* add a fudge factor as some fp precision problems have
             * been observed which caused pixels to be rounded down and
             * out of the image.
             */
            rotBounds.setRect(rotBounds.getX(), rotBounds.getY(),
                              rotBounds.getWidth()+0.001,
                              rotBounds.getHeight()+0.001);

            int boundsWidth = (int) rotBounds.getWidth();
            int boundsHeight = (int) rotBounds.getHeight();

            if (boundsWidth > 0 && boundsHeight > 0) {


                /* If the image has transparent or semi-transparent
                 * pixels then we'll have the application re-render
                 * the portion of the page covered by the image.
                 * This will be done in a later call to print using the
                 * saved graphics state.
                 * However several special cases can be handled otherwise:
                 * - bitmask transparency with a solid background colour
                 * - images which have transparency color models but no
                 * transparent pixels
                 * - images with bitmask transparency and an IndexColorModel
                 * (the common transparent GIF case) can be handled by
                 * rendering just the opaque pixels.
                 */
                boolean drawOpaque = true;
                if (!handlingTransparency && hasTransparentPixels(img)) {
                    drawOpaque = false;
                    if (isBitmaskTransparency(img)) {
                        if (bgcolor == null) {
                            if (drawBitmaskImage(img, xform, op, bgcolor,
                                                srcX, srcY,
                                                 srcWidth, srcHeight)) {
                                // image drawn, just return.
                                return true;
                            }
                        } else if (bgcolor.getTransparency()
                                   == Transparency.OPAQUE) {
                            drawOpaque = true;
                        }
                    }
                    if (!canDoRedraws()) {
                        drawOpaque = true;
                    }
                } else {
                    // if there's no transparent pixels there's no need
                    // for a background colour. This can avoid edge artifacts
                    // in rotation cases.
                    bgcolor = null;
                }
                // if src region extends beyond the image, the "opaque" path
                // may blit b/g colour (including white) where it shoudn't.
                if ((srcX+srcWidth > img.getWidth(null) ||
                     srcY+srcHeight > img.getHeight(null))
                    && canDoRedraws()) {
                    drawOpaque = false;
                }
                if ( drawOpaque == false) {
                    psPrinterJob.saveState(getTransform(), getClip(),
                                           rotBounds, scaleX, scaleY,
                                           srcRect, xform);
                    return true;

                /* The image can be rendered directly by PS so we
                 * copy it into a BufferedImage (this takes care of
                 * ColorSpace and BufferedImageOp issues) and then
                 * send that to PS.
                 */
                } else {

                    /* Create a buffered image big enough to hold the portion
                     * of the source image being printed.
                     */
                    BufferedImage deepImage = new BufferedImage(
                                                    (int) rotBounds.getWidth(),
                                                    (int) rotBounds.getHeight(),
                                                    BufferedImage.TYPE_3BYTE_BGR);

                    /* Setup a Graphics2D on to the BufferedImage so that the
                     * source image when copied, lands within the image buffer.
                     */
                    Graphics2D imageGraphics = deepImage.createGraphics();
                    imageGraphics.clipRect(0, 0,
                                           deepImage.getWidth(),
                                           deepImage.getHeight());

                    imageGraphics.translate(-rotBounds.getX(),
                                            -rotBounds.getY());
                    imageGraphics.transform(rotTransform);

                    /* Fill the BufferedImage either with the caller supplied
                     * color, 'bgColor' or, if null, with white.
                     */
                    if (bgcolor == null) {
                        bgcolor = Color.white;
                    }

                    /* REMIND: no need to use scaling here. */
                    imageGraphics.drawImage(img,
                                            srcX, srcY,
                                            srcX + srcWidth, srcY + srcHeight,
                                            srcX, srcY,
                                            srcX + srcWidth, srcY + srcHeight,
                                            bgcolor, null);

                    /* In PSPrinterJob images are printed in device space
                     * and therefore we need to set a device space clip.
                     * FIX: this is an overly tight coupling of these
                     * two classes.
                     * The temporary clip set needs to be an intersection
                     * with the previous user clip.
                     * REMIND: two xfms may lose accuracy in clip path.
                     */
                    Shape holdClip = getClip();
                    Shape oldClip =
                        getTransform().createTransformedShape(holdClip);
                    AffineTransform sat = AffineTransform.getScaleInstance(
                                                             scaleX, scaleY);
                    Shape imgClip = sat.createTransformedShape(rotShape);
                    Area imgArea = new Area(imgClip);
                    Area oldArea = new Area(oldClip);
                    imgArea.intersect(oldArea);
                    psPrinterJob.setClip(imgArea);

                    /* Scale the bounding rectangle by the scale transform.
                     * Because the scaling transform has only x and y
                     * scaling components it is equivalent to multiply
                     * the x components of the bounding rectangle by
                     * the x scaling factor and to multiply the y components
                     * by the y scaling factor.
                     */
                    Rectangle2D.Float scaledBounds
                            = new Rectangle2D.Float(
                                    (float) (rotBounds.getX() * scaleX),
                                    (float) (rotBounds.getY() * scaleY),
                                    (float) (rotBounds.getWidth() * scaleX),
                                    (float) (rotBounds.getHeight() * scaleY));


                    /* Pull the raster data from the buffered image
                     * and pass it along to PS.
                     */
                    ByteInterleavedRaster tile =
                                   (ByteInterleavedRaster)deepImage.getRaster();

                    psPrinterJob.drawImageBGR(tile.getDataStorage(),
                                scaledBounds.x, scaledBounds.y,
                                (float)Math.rint(scaledBounds.width+0.5),
                                (float)Math.rint(scaledBounds.height+0.5),
                                0f, 0f,
                                deepImage.getWidth(), deepImage.getHeight(),
                                deepImage.getWidth(), deepImage.getHeight());

                    /* Reset the device clip to match user clip */
                    psPrinterJob.setClip(
                               getTransform().createTransformedShape(holdClip));


                    imageGraphics.dispose();
                }

            }
        }

        return true;
    }

    /** Redraw a rectanglular area using a proxy graphics
      * To do this we need to know the rectangular area to redraw and
      * the transform & clip in effect at the time of the original drawImage
      *
      */

    public void redrawRegion(Rectangle2D region, double scaleX, double scaleY,
                             Rectangle2D srcRect, AffineTransform xform)

            throws PrinterException {

        PSPrinterJob psPrinterJob = (PSPrinterJob)getPrinterJob();
        Printable painter = getPrintable();
        PageFormat pageFormat = getPageFormat();
        int pageIndex = getPageIndex();

        /* Create a buffered image big enough to hold the portion
         * of the source image being printed.
         */
        BufferedImage deepImage = new BufferedImage(
                                        (int) region.getWidth(),
                                        (int) region.getHeight(),
                                        BufferedImage.TYPE_3BYTE_BGR);

        /* Get a graphics for the application to render into.
         * We initialize the buffer to white in order to
         * match the paper and then we shift the BufferedImage
         * so that it covers the area on the page where the
         * caller's Image will be drawn.
         */
        Graphics2D g = deepImage.createGraphics();
        ProxyGraphics2D proxy = new ProxyGraphics2D(g, psPrinterJob);
        proxy.setColor(Color.white);
        proxy.fillRect(0, 0, deepImage.getWidth(), deepImage.getHeight());
        proxy.clipRect(0, 0, deepImage.getWidth(), deepImage.getHeight());

        proxy.translate(-region.getX(), -region.getY());

        /* Calculate the resolution of the source image.
         */
        float sourceResX = (float)(psPrinterJob.getXRes() / scaleX);
        float sourceResY = (float)(psPrinterJob.getYRes() / scaleY);

        /* The application expects to see user space at 72 dpi.
         * so change user space from image source resolution to
         *  72 dpi.
         */
        proxy.scale(sourceResX / DEFAULT_USER_RES,
                    sourceResY / DEFAULT_USER_RES);
       proxy.translate(
            -psPrinterJob.getPhysicalPrintableX(pageFormat.getPaper())
               / psPrinterJob.getXRes() * DEFAULT_USER_RES,
            -psPrinterJob.getPhysicalPrintableY(pageFormat.getPaper())
               / psPrinterJob.getYRes() * DEFAULT_USER_RES);
        proxy.transform(new AffineTransform(getPageFormat().getMatrix()));

        proxy.setPaint(Color.black);

        painter.print(proxy, pageFormat, pageIndex);

        g.dispose();

        /* In PSPrinterJob images are printed in device space
         * and therefore we need to set a device space clip.
         * FIX: this is an overly tight coupling of these
         * two classes.
         * This clip is ineffectual. However its probably harmless since
         * the offscreen rendering will ensure that the rectangular area
         * we will send to postscript has all pixels set appropriately.
         */
        Shape holdClip = getClip();
        psPrinterJob.setClip(getTransform().createTransformedShape(holdClip));


        /* Scale the bounding rectangle by the scale transform.
         * Because the scaling transform has only x and y
         * scaling components it is equivalent to multiply
         * the x components of the bounding rectangle by
         * the x scaling factor and to multiply the y components
         * by the y scaling factor.
         */
        Rectangle2D.Float scaledBounds
                = new Rectangle2D.Float(
                        (float) (region.getX() * scaleX),
                        (float) (region.getY() * scaleY),
                        (float) (region.getWidth() * scaleX),
                        (float) (region.getHeight() * scaleY));


        /* Pull the raster data from the buffered image
         * and pass it along to PS.
         */
        ByteInterleavedRaster tile = (ByteInterleavedRaster)deepImage.getRaster();

        psPrinterJob.drawImageBGR(tile.getDataStorage(),
                            scaledBounds.x, scaledBounds.y,
                            scaledBounds.width,
                            scaledBounds.height,
                            0f, 0f,
                            deepImage.getWidth(), deepImage.getHeight(),
                            deepImage.getWidth(), deepImage.getHeight());


    }

    /**
     * Return true of the Image <code>img</code> has non-opaque
     * bits in it and therefore can not be directly rendered by
     * PS. Return false if the image is opaque. If this function
     * can not tell for sure whether the image has transparent
     * pixels then it assumes that is does..
     */
    private boolean hasTransparentPixels(Image img) {
        boolean hasTransparency = true;
        BufferedImage bufferedImage = null;
        ColorModel colorModel;

        if (img instanceof BufferedImage) {
            bufferedImage = (BufferedImage) img;
            colorModel = bufferedImage.getColorModel();

       } else if (img instanceof sun.awt.image.Image) {
             sun.awt.image.Image sunImage = (sun.awt.image.Image) img;
            bufferedImage = sunImage.getBufferedImage();
            if (bufferedImage == null) {
                return false;
            }
            colorModel = sunImage.getColorModel();
       } else if (img instanceof java.awt.image.VolatileImage) {
           // VolatileImage is always opaque in 1.4
           return false;
        } else {
            colorModel = null;
        }

        hasTransparency = colorModel == null
            ? true
            : colorModel.getTransparency() != ColorModel.OPAQUE;
        /*
         * For INT ARGB check the image to see if any pixels are
         * really transparent. If there are no transparent pixels then the
         * transparency of the color model can be ignored.
         * We assume that IndexColorModel images (eg gifs) have already been
         * checked for transparency and will be OPAQUE unless they actually
         * have transparent pixels present.
         */
        if (hasTransparency && bufferedImage != null) {
            if (bufferedImage.getType()==BufferedImage.TYPE_INT_ARGB) {
                DataBuffer db =  bufferedImage.getRaster().getDataBuffer();
                SampleModel sm = bufferedImage.getRaster().getSampleModel();
                if (db instanceof DataBufferInt &&
                    sm instanceof SinglePixelPackedSampleModel) {
                    SinglePixelPackedSampleModel psm =
                        (SinglePixelPackedSampleModel)sm;
                    int[] int_data = ((DataBufferInt)db).getData();
                    int x = bufferedImage.getMinX();
                    int y = bufferedImage.getMinY();
                    int w = bufferedImage.getWidth();
                    int h = bufferedImage.getHeight();
                    int stride = psm.getScanlineStride();
                    boolean hastranspixel = false;
                    for (int j = y; j < y+h; j++) {
                        int yoff = y * stride;
                        for (int i = x; i < x+w; i++) {
                            if ((int_data[yoff+i] & 0xff000000)!=0xff000000 ) {
                                hastranspixel = true;
                                break;
                            }
                        }
                        if (hastranspixel) {
                            break;
                        }
                    }
                    if (hastranspixel == false) {
                        hasTransparency = false;
                    }
                }
            }
        }
        return hasTransparency;
    }

    private boolean isBitmaskTransparency(Image img) {
        ColorModel colorModel = null;

        if (img instanceof BufferedImage) {
            BufferedImage bufferedImage = (BufferedImage) img;
            colorModel = bufferedImage.getColorModel();

       } else if (img instanceof sun.awt.image.Image) {
            sun.awt.image.Image sunImage = (sun.awt.image.Image) img;
            colorModel = sunImage.getColorModel();
       } else if (img instanceof java.awt.image.VolatileImage) {
           // VolatileImage is always opaque in 1.4
           return false;
       }
        return (colorModel != null &&
                colorModel.getTransparency() == ColorModel.BITMASK);
    }

    /*
     * Fill the path defined by <code>pathIter</code>
     * with the specified color.
     * The path is provided in current user space.
     */
    protected void deviceFill(PathIterator pathIter, Color color) {

                                PSPrinterJob psPrinterJob = (PSPrinterJob) getPrinterJob();

        psPrinterJob.setTransform(getTransform());
        psPrinterJob.setClip(getClip());
                                psPrinterJob.setColor(color);
                                convertToPSPath(pathIter);
                                psPrinterJob.fillPath();
    }

    /*
     * Draw the bounding rectangle using path by calling draw()
     * function and passing a rectangle shape.
     */
    protected void deviceFrameRect(int x, int y, int width, int height,
                                   Color color) {

        draw(new Rectangle2D.Float(x, y, width, height));
    }

    /*
     * Draw a line using path by calling draw() function and passing
     * a line shape.
     */
    protected void deviceDrawLine(int xBegin, int yBegin,
                                  int xEnd, int yEnd, Color color) {

        draw(new Line2D.Float(xBegin, yBegin, xEnd, yEnd));
    }

    /*
     * Fill the rectangle with the specified color by calling fill().
     */
    protected void deviceFillRect(int x, int y, int width, int height,
                                  Color color) {
        fill(new Rectangle2D.Float(x, y, width, height));
    }


    /*
     * This method should not be invoked by PSPathGraphics.
     * FIX: Rework PathGraphics so that this method is
     * not an abstract method there.
     */
    protected void deviceClip(PathIterator pathIter) {
    }

    /**
     * Given a Java2D <code>PathIterator</code> instance,
     * this method translates that into a PostScript path..
     */
    private void convertToPSPath(PathIterator pathIter) {

        float[] segment = new float[6];
        int segmentType;

        PSPrinterJob psPrinterJob = (PSPrinterJob) getPrinterJob();

        /* Map the PathIterator's fill rule into the PostScript
         * fill rule.
         */
        int fillRule;
        if (pathIter.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
            fillRule = PSPrinterJob.FILL_EVEN_ODD;
        } else {
            fillRule = PSPrinterJob.FILL_WINDING;
        }
        psPrinterJob.setFillMode(fillRule);

        psPrinterJob.beginPath();

        while (pathIter.isDone() == false) {
            segmentType = pathIter.currentSegment(segment);

            switch (segmentType) {
             case PathIterator.SEG_MOVETO:
                psPrinterJob.moveTo(segment[0], segment[1]);
                break;

             case PathIterator.SEG_LINETO:
                psPrinterJob.lineTo(segment[0], segment[1]);
                break;

            /* Convert the quad path to a bezier.
             */
             case PathIterator.SEG_QUADTO:
                float lastX = psPrinterJob.getPenX();
                float lastY = psPrinterJob.getPenY();
                float c1x = lastX + (segment[0] - lastX) * 2 / 3;
                float c1y = lastY + (segment[1] - lastY) * 2 / 3;
                float c2x = segment[2] - (segment[2] - segment[0]) * 2/ 3;
                float c2y = segment[3] - (segment[3] - segment[1]) * 2/ 3;
                psPrinterJob.bezierTo(c1x, c1y,
                                      c2x, c2y,
                                      segment[2], segment[3]);
                break;

             case PathIterator.SEG_CUBICTO:
                psPrinterJob.bezierTo(segment[0], segment[1],
                                      segment[2], segment[3],
                                      segment[4], segment[5]);
                break;

             case PathIterator.SEG_CLOSE:
                psPrinterJob.closeSubpath();
                break;
            }


            pathIter.next();
        }

    }

}

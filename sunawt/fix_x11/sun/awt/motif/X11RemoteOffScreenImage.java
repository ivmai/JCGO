/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)X11RemoteOffScreenImage.java     1.15 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.motif;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import sun.awt.X11SurfaceData;
import sun.awt.X11SurfaceData.X11PixmapSurfaceData;
import sun.awt.image.AcceleratedOffScreenImage;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.image.DataBufferNative;
import sun.awt.image.WritableRasterNative;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.loops.CompositeType;
import sun.java2d.pipe.DrawImage;

/**
 * This class extends the functionality of X11OffScreenImage by
 * forcing the use of a pixmap-based surfaceData object.  In
 * X11OffScreenImage, the default surfaceData object is the default
 * one used by BufferedImage, setup by a call to the superclass
 * constructor.  Acceleration is achieved by caching a version of
 * the data in a pixmap and copying from that pixmap when appropriate.
 * <P>
 * This is sufficient for accelerating sprite-type images, which are
 * rendered to infrequently but copied from often.  Back buffers
 * do not benefit from this acceleration since the cached copy
 * would never be used.  This is deemed sufficient for local X
 * usage; users performing complex Java2D operations (e.g.,
 * anti-aliasing, compositing, text, or anything that requires
 * read-modify-write operations) would see a degradation in performance
 * were we to force all operations to go through the pixmap-based
 * image.
 * <P>
 * In the RemoteX case, however, performance is so abysmal in the
 * case of double-buffering (with the back buffer living in the
 * Java heap), that the advantage of speeding up the basic
 * applications (i.e., those using 1.1 API or simple Swing operations,
 * and thus avoiding complex Java2D read-modify-write operations)
 * is judged to outweigh the possible performance loss in some
 * applications from having the back buffer located in a pixmap.
 * <P>
 * The decision to instantiate this class (versus X11OffScreenImage)
 * is based on whether we are running remotely and whether the
 * user has enabled/disabled a property related to this issue:
 * -Dsun.java2d.remote
 */
public class X11RemoteOffScreenImage extends X11OffScreenImage {

    int bufImageTypeSw;     // Cache for later BI creation
                            // in getSnapshot()
    SurfaceData bisd;       // intermediate BufImgSD for use in
                            // copyBackupToAccelerated

    private static native void initIDs();

    static {
        initIDs();
    }

    public X11RemoteOffScreenImage(Component c, ColorModel cm, WritableRaster raster,
                                   boolean isRasterPremultiplied)
    {
        super(c, cm, raster, isRasterPremultiplied, false);
        bufImageTypeSw = getType();
        if (!accelerationEnabled) {
            return;
        }
        GraphicsConfiguration gc =
            X11SurfaceData.getGC(c == null ? null : (MComponentPeer)c.getPeer());
        initAcceleratedBackground(gc, getWidth(), getHeight());
        if (surfaceDataHw != null) {
            setCurrentSurfaceData(surfaceDataHw);
            // this is the trick: we treat surfaceDataHw as the default
            // software sd in X11OffScreenImage, this way we can use
            // all of the functionality of X11OSI.
            // REMIND: rename surfaceDataHw and surfaceDataSw to
            // something more appropriate, i.e. sdDefault and sdCached
            surfaceDataSw = surfaceDataHw;
            initContents();
            createNativeRaster();
        }
    }

    private native void
        setSurfaceDataNative(SurfaceData sData);
    private native void
        setRasterNative(WritableRaster raster);

    private void setCurrentSurfaceData(SurfaceData sd) {
        if (sd != surfaceData) {
            surfaceData = sd;           // OffScreenImage copy
            setSurfaceDataNative(sd);   // BufferedImage copy
        }
    }

    public void initContents() {
        Graphics2D g2 = createGraphics();
        g2.clearRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    /**
     * Need to override this method as we don't need to check for
     * the number of copies done from this image
     */
    public SurfaceData getSourceSurfaceData(SurfaceData destSD,
                                            CompositeType comp,
                                            Color bgColor) {
        if (accelerationEnabled             &&
            (destSD != surfaceDataHw)       &&
            destSurfaceAccelerated(destSD))
        {
            // First, we validate the pixmap sd if necessary and then return
            // the appropriate surfaceData object.

            validate(destSD.getDeviceConfiguration());
            if (surfaceDataHw != null) {
                return surfaceDataHw;
            }
        }
        return surfaceDataSw;
    }

    private void createNativeRaster() {
        ColorModel cm = getColorModel();
        SampleModel smHw = null;
        int dataType = 0;
        int scanStride = getWidth();

        switch (cm.getPixelSize()) {
        case 8:
        case 12:
            // 8-bits uses PixelInterleavedSampleModel
            if (cm.getPixelSize() == 8) {
                dataType = DataBuffer.TYPE_BYTE;
            } else {
                dataType = DataBuffer.TYPE_USHORT;
            }

            int[] bandOffsets = new int[1];
            bandOffsets[0] = 0;
            smHw = new PixelInterleavedSampleModel(dataType, getWidth(),
                                                   getHeight(),
                                                   1, scanStride,
                                                   bandOffsets);
            break;

            // all others use SinglePixelPackedSampleModel
        case 15:
        case 16:
            dataType = DataBuffer.TYPE_USHORT;
            int[] bitMasks = new int[3];
            DirectColorModel dcm = (DirectColorModel)cm;
            bitMasks[0] = dcm.getRedMask();
            bitMasks[1] = dcm.getGreenMask();
            bitMasks[2] = dcm.getBlueMask();
            smHw = new SinglePixelPackedSampleModel(dataType, getWidth(),
                                                    getHeight(), scanStride,
                                                    bitMasks);
            break;

        case 24:
        case 32:
            dataType = DataBuffer.TYPE_INT;
            bitMasks = new int[3];
            dcm = (DirectColorModel)cm;
            bitMasks[0] = dcm.getRedMask();
            bitMasks[1] = dcm.getGreenMask();
            bitMasks[2] = dcm.getBlueMask();
            smHw = new SinglePixelPackedSampleModel(dataType, getWidth(),
                                                    getHeight(), scanStride,
                                                    bitMasks);
            break;

        default:
            throw new InternalError("Unsupported depth " + cm.getPixelSize());
        }
        DataBuffer dbn = new DataBufferNative(surfaceDataHw, dataType,
                                              getWidth(), getHeight());
        // set the native raster as a default raster of BI
        setRasterNative(WritableRasterNative.createNativeRaster(smHw, dbn));
    }

    /**
     * Returns a BufferedImage representation of this image.
     * In this default method, it just returns this object.
     * Platform-dependent implementations may choose to return
     * something different.  For example, X11RemoteOffScreenImage
     * might want to return a BufferedImage that points to data current
     * with its pixmap version of the image.
     */
    public BufferedImage getSnapshot() {
        BufferedImage bImg;

        if (bufImageTypeSw > 0) {
            bImg = new BufferedImage(getWidth(), getHeight(),
                    bufImageTypeSw);
        } else {
            ColorModel colormodel = getColorModel();
            WritableRaster writableraster =
             colormodel.createCompatibleWritableRaster(getWidth(),
             getHeight());
            bImg = new BufferedImage(colormodel, writableraster,
                    colormodel.isAlphaPremultiplied(), null);
        }

        Graphics2D g = bImg.createGraphics();
        g.drawImage(this, 0, 0, null);
        g.dispose();
        return bImg;
    }

    protected void copyBackupToAccelerated() {
        if (surfaceDataSw != null && surfaceDataHw != null &&
            surfaceDataSw != surfaceDataHw) {
            java.awt.Font defaultFont =
                new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);

            // we can't render directly from hw sd to hw sd
            // as we might end up in XCopyArea with pixmaps from different
            // screens. So we use a temp BI SD as a bypass:
            // OSI.surfaceDataSw -> bisd -> AccOSI.surfaceDataHw
            if (bisd == null) {
                BufferedImage bi;

                if (bufImageTypeSw > 0) {
                    bi = new BufferedImage(getWidth(), getHeight(),
                          bufImageTypeSw);
                } else {
                    ColorModel colormodel = getColorModel();
                    WritableRaster writableraster =
                     colormodel.createCompatibleWritableRaster(getWidth(),
                     getHeight());
                    bi = new BufferedImage(colormodel, writableraster,
                          colormodel.isAlphaPremultiplied(), null);
                }

                bisd = BufImgSurfaceData.createData(bi);
            }

            SunGraphics2D swG2d =
                new SunGraphics2D(bisd, java.awt.Color.black,
                                  java.awt.Color.white, defaultFont);

            DrawImage.renderSurfaceData(swG2d, surfaceDataSw,
                                        (java.awt.Color)null,
                                        0, 0, 0, 0, getWidth(), getHeight());
            swG2d.dispose();
            SunGraphics2D hwG2D =
                new SunGraphics2D(surfaceDataHw, java.awt.Color.black,
                                  java.awt.Color.white, defaultFont);
            DrawImage.renderSurfaceData(hwG2D, bisd,
                                        (java.awt.Color)null,
                                        0, 0, 0, 0, getWidth(), getHeight());
            hwG2D.dispose();
        }
    }
}

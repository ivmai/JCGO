/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)BufImgSurfaceData.java   1.29 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.image;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.GraphicsConfiguration;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.BufferedImage;

import sun.java2d.SurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.RenderLoops;

public class BufImgSurfaceData extends SurfaceData {
    BufferedImage bufImg;
    private BufferedImageGraphicsConfig graphicsConfig;
    RenderLoops solidloops;

    private static native void initIDs();

    private static final int DCM_RGBX_RED_MASK   = 0xff000000;
    private static final int DCM_RGBX_GREEN_MASK = 0x00ff0000;
    private static final int DCM_RGBX_BLUE_MASK  = 0x0000ff00;
    private static final int DCM_555X_RED_MASK = 0xF800;
    private static final int DCM_555X_GREEN_MASK = 0x07C0;
    private static final int DCM_555X_BLUE_MASK = 0x003E;
    private static final int DCM_4444_RED_MASK   = 0x0f00;
    private static final int DCM_4444_GREEN_MASK = 0x00f0;
    private static final int DCM_4444_BLUE_MASK  = 0x000f;
    private static final int DCM_4444_ALPHA_MASK = 0xf000;
    private static final int DCM_ARGBBM_ALPHA_MASK = 0x01000000;
    private static final int DCM_ARGBBM_RED_MASK   = 0x00ff0000;
    private static final int DCM_ARGBBM_GREEN_MASK = 0x0000ff00;
    private static final int DCM_ARGBBM_BLUE_MASK  = 0x000000ff;

    static {
        initIDs();
    }

    private static native SurfaceData
        getSurfaceData(BufferedImage bufImg);
    private static native void
        setSurfaceData(BufferedImage bufImg, SurfaceData sData);

    public static SurfaceData createData(BufferedImage bufImg) {
        if (bufImg == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        SurfaceData sData = getSurfaceData(bufImg);
        if (sData != null) {
            return sData;
        }
        ColorModel cm = bufImg.getColorModel();
        int type = bufImg.getType();
        // REMIND: Check the image type and pick an appropriate subclass
        switch (type) {
        case BufferedImage.TYPE_INT_BGR:
            sData = createDataIC(bufImg, SurfaceType.IntBgr);
            break;
        case BufferedImage.TYPE_INT_RGB:
            sData = createDataIC(bufImg, SurfaceType.IntRgb);
            break;
        case BufferedImage.TYPE_INT_ARGB:
            sData = createDataIC(bufImg, SurfaceType.IntArgb);
            break;
        case BufferedImage.TYPE_INT_ARGB_PRE:
            sData = createDataIC(bufImg, SurfaceType.IntArgbPre);
            break;
        case BufferedImage.TYPE_3BYTE_BGR:
            sData = createDataBC(bufImg, SurfaceType.ThreeByteBgr, 2);
            break;
        case BufferedImage.TYPE_4BYTE_ABGR:
            sData = createDataBC(bufImg, SurfaceType.FourByteAbgr, 3);
            break;
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            sData = createDataBC(bufImg, SurfaceType.FourByteAbgrPre, 3);
            break;
        case BufferedImage.TYPE_USHORT_565_RGB:
            sData = createDataSC(bufImg, SurfaceType.Ushort565Rgb, null);
            break;
        case BufferedImage.TYPE_USHORT_555_RGB:
            sData = createDataSC(bufImg, SurfaceType.Ushort555Rgb, null);
            break;
        case BufferedImage.TYPE_BYTE_INDEXED:
            {
                SurfaceType sType;
                switch (cm.getTransparency()) {
                case OPAQUE:
                    if (isOpaqueGray((IndexColorModel)cm)) {
                        sType = SurfaceType.Index8Gray;
                    } else {
                        sType = SurfaceType.ByteIndexedOpaque;
                    }
                    break;
                case BITMASK:
                    sType = SurfaceType.ByteIndexedBm;
                    break;
                case TRANSLUCENT:
                    sType = SurfaceType.ByteIndexed;
                    break;
                default:
                    throw new InternalError("Unrecognized transparency");
                }
                sData = createDataBC(bufImg, sType, 0);
            }
            break;
        case BufferedImage.TYPE_BYTE_GRAY:
            sData = createDataBC(bufImg, SurfaceType.ByteGray, 0);
            break;
        case BufferedImage.TYPE_USHORT_GRAY:
            sData = createDataSC(bufImg, SurfaceType.UshortGray, null);
            break;
        case BufferedImage.TYPE_BYTE_BINARY:
            {
                SurfaceType sType;
                SampleModel sm = bufImg.getRaster().getSampleModel();
                switch (sm.getSampleSize(0)) {
                case 1:
                    sType = SurfaceType.ByteBinary1Bit;
                    break;
                case 2:
                    sType = SurfaceType.ByteBinary2Bit;
                    break;
                case 4:
                    sType = SurfaceType.ByteBinary4Bit;
                    break;
                default:
                    throw new InternalError("Unrecognized pixel size");
                }
                sData = createDataBP(bufImg, sType);
            }
            break;
        case BufferedImage.TYPE_CUSTOM:
        default:
            {
                Raster raster = bufImg.getRaster();
                int numBands = raster.getNumBands();
                if (raster instanceof IntegerComponentRaster &&
                    raster.getNumDataElements() == 1 &&
                    ((IntegerComponentRaster)raster).getPixelStride() == 1)
                {
                    SurfaceType sType = SurfaceType.AnyInt;
                    if (cm instanceof DirectColorModel) {
                        DirectColorModel dcm = (DirectColorModel) cm;
                        int aMask = dcm.getAlphaMask();
                        int rMask = dcm.getRedMask();
                        int gMask = dcm.getGreenMask();
                        int bMask = dcm.getBlueMask();
                        if (numBands == 3 &&
                            aMask == 0 &&
                            rMask == DCM_RGBX_RED_MASK &&
                            gMask == DCM_RGBX_GREEN_MASK &&
                            bMask == DCM_RGBX_BLUE_MASK)
                        {
                            sType = SurfaceType.IntRgbx;
                        } else if (numBands == 4 &&
                                   aMask == DCM_ARGBBM_ALPHA_MASK &&
                                   rMask == DCM_ARGBBM_RED_MASK &&
                                   gMask == DCM_ARGBBM_GREEN_MASK &&
                                   bMask == DCM_ARGBBM_BLUE_MASK)
                        {
                            sType = SurfaceType.IntArgbBm;
                        } else {
                            sType = SurfaceType.AnyDcm;
                        }
                    }
                    sData = createDataIC(bufImg, sType);
                    break;
                } else if (raster instanceof ShortComponentRaster &&
                           raster.getNumDataElements() == 1 &&
                           ((ShortComponentRaster)raster).getPixelStride() == 1)
                {
                    SurfaceType sType = SurfaceType.AnyShort;
                    IndexColorModel icm = null;
                    if (cm instanceof DirectColorModel) {
                        DirectColorModel dcm = (DirectColorModel) cm;
                        int aMask = dcm.getAlphaMask();
                        int rMask = dcm.getRedMask();
                        int gMask = dcm.getGreenMask();
                        int bMask = dcm.getBlueMask();
                        if (numBands == 3 &&
                            aMask == 0 &&
                            rMask == DCM_555X_RED_MASK &&
                            gMask == DCM_555X_GREEN_MASK &&
                            bMask == DCM_555X_BLUE_MASK)
                        {
                            sType = SurfaceType.Ushort555Rgbx;
                        } else
                        if (numBands == 4 &&
                            aMask == DCM_4444_ALPHA_MASK &&
                            rMask == DCM_4444_RED_MASK &&
                            gMask == DCM_4444_GREEN_MASK &&
                            bMask == DCM_4444_BLUE_MASK)
                        {
                            sType = SurfaceType.Ushort4444Argb;
                        }
                    } else if (cm instanceof IndexColorModel) {
                        icm = (IndexColorModel)cm;
                        if (icm.getPixelSize() == 12) {
                            if (isOpaqueGray(icm)) {
                                sType = SurfaceType.Index12Gray;
                            } else {
                                sType = SurfaceType.UshortIndexed;
                            }
                        } else {
                            icm = null;
                        }
                    }
                    sData = createDataSC(bufImg, sType, icm);
                    break;
                }
                sData = new BufImgSurfaceData(bufImg, SurfaceType.Custom);
            }
            break;
        }
        ((BufImgSurfaceData) sData).initSolidLoops();
        setSurfaceData(bufImg, sData);
        return sData;
    }

    public static SurfaceData createData(Raster ras, ColorModel cm) {
        throw new InternalError("SurfaceData not implemented for Raster/CM");
    }

    public static SurfaceData createDataIC(BufferedImage bImg,
                                           SurfaceType sType) {
        BufImgSurfaceData bisd = new BufImgSurfaceData(bImg, sType);
        IntegerComponentRaster icRaster =
            (IntegerComponentRaster)bImg.getRaster();
        bisd.initRaster(icRaster.getDataStorage(),
                        icRaster.getDataOffset(0) * 4,
                        icRaster.getWidth(),
                        icRaster.getHeight(),
                        icRaster.getPixelStride() * 4,
                        icRaster.getScanlineStride() * 4,
                        null);
        return bisd;
    }

    public static SurfaceData createDataSC(BufferedImage bImg,
                                           SurfaceType sType,
                                           IndexColorModel icm) {
        BufImgSurfaceData bisd = new BufImgSurfaceData(bImg, sType);
        ShortComponentRaster scRaster =
            (ShortComponentRaster)bImg.getRaster();
        bisd.initRaster(scRaster.getDataStorage(),
                        scRaster.getDataOffset(0) * 2,
                        scRaster.getWidth(),
                        scRaster.getHeight(),
                        scRaster.getPixelStride() * 2,
                        scRaster.getScanlineStride() * 2,
                        icm);
        return bisd;
    }

    public static SurfaceData createDataBC(BufferedImage bImg,
                                           SurfaceType sType,
                                           int primaryBank) {
        BufImgSurfaceData bisd = new BufImgSurfaceData(bImg, sType);
        ByteComponentRaster bcRaster =
            (ByteComponentRaster)bImg.getRaster();
        ColorModel cm = bImg.getColorModel();
        IndexColorModel icm = ((cm instanceof IndexColorModel)
                               ? (IndexColorModel) cm
                               : null);
        bisd.initRaster(bcRaster.getDataStorage(),
                        bcRaster.getDataOffset(primaryBank),
                        bcRaster.getWidth(),
                        bcRaster.getHeight(),
                        bcRaster.getPixelStride(),
                        bcRaster.getScanlineStride(),
                        icm);
        return bisd;
    }

    public static SurfaceData createDataBP(BufferedImage bImg,
                                           SurfaceType sType) {
        BufImgSurfaceData bisd = new BufImgSurfaceData(bImg, sType);
        BytePackedRaster bpRaster =
            (BytePackedRaster)bImg.getRaster();
        ColorModel cm = bImg.getColorModel();
        IndexColorModel icm = ((cm instanceof IndexColorModel)
                               ? (IndexColorModel) cm
                               : null);
        /* REMIND: bit offset needs to be implemented correctly */
        bisd.initRaster(bpRaster.getDataStorage(),
                        bpRaster.getDataBitOffset() / 8,
                        bpRaster.getWidth(),
                        bpRaster.getHeight(),
                        0,
                        bpRaster.getScanlineStride(),
                        icm);
        return bisd;
    }

    public void lock() {
        // BufferedImages cannot change configuration once constructed
    }

    public void unlock() {
        // BufferedImages cannot change configuration once constructed
    }

    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState == sg2d.PAINT_SOLIDCOLOR &&
            sg2d.compositeState == sg2d.COMP_ISCOPY)
        {
            return solidloops;
        }
        return super.getRenderLoops(sg2d);
    }

    public java.awt.image.Raster getRaster(int x, int y, int w, int h) {
        return bufImg.getRaster();
    }

    /**
     * Initializes the native Ops pointer.
     */
    protected native void initRaster(Object theArray,
                                     int offset,
                                     int width,
                                     int height,
                                     int pixStr,
                                     int scanStr,
                                     IndexColorModel icm);

    public BufImgSurfaceData(BufferedImage bufImg, SurfaceType sType) {
        super(sType, bufImg.getColorModel());
        this.bufImg = bufImg;
    }

    public void initSolidLoops() {
        this.solidloops = getSolidLoops(getSurfaceType());
    }

    private static final int CACHE_SIZE = 5;
    private static RenderLoops loopcache[] = new RenderLoops[CACHE_SIZE];
    private static SurfaceType typecache[] = new SurfaceType[CACHE_SIZE];
    public static synchronized RenderLoops getSolidLoops(SurfaceType type) {
        for (int i = CACHE_SIZE - 1; i >= 0; i--) {
            SurfaceType t = typecache[i];
            if (t == type) {
                return loopcache[i];
            } else if (t == null) {
                break;
            }
        }
        RenderLoops l = makeRenderLoops(SurfaceType.OpaqueColor,
                                        CompositeType.SrcNoEa,
                                        type);
        System.arraycopy(loopcache, 1, loopcache, 0, CACHE_SIZE-1);
        System.arraycopy(typecache, 1, typecache, 0, CACHE_SIZE-1);
        loopcache[CACHE_SIZE - 1] = l;
        typecache[CACHE_SIZE - 1] = type;
        return l;
    }

    public SurfaceData getReplacement() {
        if (bufImg instanceof OffScreenImage) {
            return ((OffScreenImage)bufImg).restoreContents();
        } else {
            // BufImgSurfaceData objects should never be lost, so simply
            // return the same surfaceData object
            return this;
        }
    }

    public synchronized GraphicsConfiguration getDeviceConfiguration() {
        if (graphicsConfig == null) {
            graphicsConfig = BufferedImageGraphicsConfig.getConfig(bufImg);
        }
        return graphicsConfig;
    }

    public java.awt.Rectangle getBounds() {
        return new Rectangle(bufImg.getWidth(), bufImg.getHeight());
    }

    protected void checkCustomComposite() {
        // BufferedImages always allow Custom Composite objects since
        // their pixels are immediately retrievable anyway.
    }

    public static native void freeNativeICMData(IndexColorModel icm);
}

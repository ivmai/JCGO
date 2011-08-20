/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)X11PMBlitLoops.java      1.10 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.GraphicsPrimitiveProxy;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.Blit;
import sun.java2d.loops.MaskBlit;
import sun.java2d.pipe.Region;
import sun.java2d.SurfaceData;
import java.awt.Composite;

/**
 * X11PMBlitLoops
 *
 * This class accelerates Blits between two surfaces of types *PM.  Since
 * the onscreen surface is of that type and some of the offscreen surfaces
 * may be of that type (if they were created via X11OffScreenImage), then
 * this type of Blit will accelerated double-buffer copies between those
 * two surfaces.
*/
public class X11PMBlitLoops extends Blit {

    public static void register()
    {
        GraphicsPrimitive[] primitives = {
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11,
                               X11SurfaceData.IntBgrX11, false),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11,
                               X11SurfaceData.IntRgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteBgrX11,
                               X11SurfaceData.ThreeByteBgrX11, false),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteRgbX11,
                               X11SurfaceData.ThreeByteRgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.ByteIndexedOpaqueX11,
                               X11SurfaceData.ByteIndexedOpaqueX11, false),
            new X11PMBlitLoops(X11SurfaceData.ByteGrayX11,
                               X11SurfaceData.ByteGrayX11, false),
            new X11PMBlitLoops(X11SurfaceData.Index8GrayX11,
                               X11SurfaceData.Index8GrayX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShort555RgbX11,
                               X11SurfaceData.UShort555RgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShort565RgbX11,
                               X11SurfaceData.UShort565RgbX11, false),
            new X11PMBlitLoops(X11SurfaceData.UShortIndexedX11,
                               X11SurfaceData.UShortIndexedX11, false),

            // 1-bit transparent to opaque loops
            new X11PMBlitLoops(X11SurfaceData.IntBgrX11_BM,
                               X11SurfaceData.IntBgrX11, true),
            new X11PMBlitLoops(X11SurfaceData.IntRgbX11_BM,
                               X11SurfaceData.IntRgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteBgrX11_BM,
                               X11SurfaceData.ThreeByteBgrX11, true),
            new X11PMBlitLoops(X11SurfaceData.ThreeByteRgbX11_BM,
                               X11SurfaceData.ThreeByteRgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.ByteIndexedX11_BM,
                               X11SurfaceData.ByteIndexedOpaqueX11, true),
            new X11PMBlitLoops(X11SurfaceData.ByteGrayX11_BM,
                               X11SurfaceData.ByteGrayX11, true),
            new X11PMBlitLoops(X11SurfaceData.Index8GrayX11_BM,
                               X11SurfaceData.Index8GrayX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShort555RgbX11_BM,
                               X11SurfaceData.UShort555RgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShort565RgbX11_BM,
                               X11SurfaceData.UShort565RgbX11, true),
            new X11PMBlitLoops(X11SurfaceData.UShortIndexedX11_BM,
                               X11SurfaceData.UShortIndexedX11, true),

            // delegate loops
            new DelegateBlitLoop(X11SurfaceData.IntBgrX11_BM,
                                 X11SurfaceData.IntBgrX11),
            new DelegateBlitLoop(X11SurfaceData.IntRgbX11_BM,
                                 X11SurfaceData.IntRgbX11),
            new DelegateBlitLoop(X11SurfaceData.ThreeByteBgrX11_BM,
                                 X11SurfaceData.ThreeByteBgrX11),
            new DelegateBlitLoop(X11SurfaceData.ThreeByteRgbX11_BM,
                                 X11SurfaceData.ThreeByteRgbX11),
            new DelegateBlitLoop(X11SurfaceData.ByteIndexedX11_BM,
                                 X11SurfaceData.ByteIndexedOpaqueX11),
            new DelegateBlitLoop(X11SurfaceData.ByteGrayX11_BM,
                                 X11SurfaceData.ByteGrayX11),
            new DelegateBlitLoop(X11SurfaceData.Index8GrayX11_BM,
                                 X11SurfaceData.Index8GrayX11),
            new DelegateBlitLoop(X11SurfaceData.UShort555RgbX11_BM,
                                 X11SurfaceData.UShort555RgbX11),
            new DelegateBlitLoop(X11SurfaceData.UShort565RgbX11_BM,
                                 X11SurfaceData.UShort565RgbX11),
            new DelegateBlitLoop(X11SurfaceData.UShortIndexedX11_BM,
                                 X11SurfaceData.UShortIndexedX11)

        };
        GraphicsPrimitiveMgr.register(primitives);
    }

    public X11PMBlitLoops(SurfaceType srcType, SurfaceType dstType,
                          boolean over) {
        super(srcType,
              over ? CompositeType.SrcOverNoEa : CompositeType.SrcNoEa,
              dstType);
    }

    /**
     * Blit
     * This native method is where all of the work happens in the
     * accelerated Blit.
     */
    public native void Blit(SurfaceData src, SurfaceData dst,
                            Composite comp, Region clip,
                            int sx, int sy, int dx, int dy, int w, int h);

    /**
     * This loop is used to render from Sw surface data
     * to the Hw one in AOSI.copyBackupToAccelerated.
     */
    static class DelegateBlitLoop extends Blit {
        SurfaceType dstType;

        /**
         * @param realDstType SurfaceType for which the loop should be
         * registered
         * @param delegateDstType SurfaceType which will be used
         * for finding delegate loop
         */
        public DelegateBlitLoop(SurfaceType realDstType, SurfaceType delegateDstType) {
            super(SurfaceType.Any, CompositeType.SrcNoEa, realDstType);
            this.dstType = delegateDstType;
        }

        public void Blit(SurfaceData src, SurfaceData dst,
                         Composite comp, Region clip,
                         int sx, int sy, int dx, int dy, int w, int h)
        {
            Blit blit = Blit.getFromCache(src.getSurfaceType(),
                                          CompositeType.SrcNoEa,
                                          dstType);
            blit.Blit(src, dst, comp, clip, sx, sy, dx, dy, w, h);
        }
    }
}

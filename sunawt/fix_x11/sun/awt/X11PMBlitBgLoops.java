/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)X11PMBlitBgLoops.java    1.7 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt;

import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.loops.GraphicsPrimitiveMgr;
import sun.java2d.loops.CompositeType;
import sun.java2d.loops.SurfaceType;
import sun.java2d.loops.BlitBg;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.Region;
import java.awt.Color;
import java.awt.Composite;

/**
 * X11PMBlitBgLoops
 *
 * This class accelerates Blits between two surfaces of types *PM.  Since
 * the onscreen surface is of that type and some of the offscreen surfaces
 * may be of that type (if they were created via X11OffScreenImage), then
 * this type of BlitBg will accelerated double-buffer copies between those
 * two surfaces.
*/
public class X11PMBlitBgLoops extends BlitBg {

    public static void register()
    {
        GraphicsPrimitive[] primitives = {
            new X11PMBlitBgLoops(X11SurfaceData.IntBgrX11_BM,
                                 X11SurfaceData.IntBgrX11),
            new X11PMBlitBgLoops(X11SurfaceData.IntRgbX11_BM,
                                 X11SurfaceData.IntRgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.ThreeByteBgrX11_BM,
                                 X11SurfaceData.ThreeByteBgrX11),
            new X11PMBlitBgLoops(X11SurfaceData.ThreeByteRgbX11_BM,
                                 X11SurfaceData.ThreeByteRgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.ByteIndexedX11_BM,
                                 X11SurfaceData.ByteIndexedOpaqueX11),
            new X11PMBlitBgLoops(X11SurfaceData.ByteGrayX11_BM,
                                 X11SurfaceData.ByteGrayX11),
            new X11PMBlitBgLoops(X11SurfaceData.Index8GrayX11_BM,
                                 X11SurfaceData.Index8GrayX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShort555RgbX11_BM,
                                 X11SurfaceData.UShort555RgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShort565RgbX11_BM,
                                 X11SurfaceData.UShort565RgbX11),
            new X11PMBlitBgLoops(X11SurfaceData.UShortIndexedX11_BM,
                                 X11SurfaceData.UShortIndexedX11)
        };
        GraphicsPrimitiveMgr.register(primitives);
    }

    public X11PMBlitBgLoops(SurfaceType srcType, SurfaceType dstType)
    {
        super(srcType, CompositeType.SrcNoEa, dstType);
    }

    public void BlitBg(SurfaceData src, SurfaceData dst,
                       Composite comp, Region clip, Color bgColor,
                       int sx, int sy,
                       int dx, int dy,
                       int w, int h)
    {
        int rgb = bgColor.getRGB();
        nativeBlitBg(src, dst, comp, clip, dst.pixelFor(rgb),
                     sx, sy, dx, dy, w, h);
    }

    /**
     * This native method is where all of the work happens in the
     * accelerated Blit.
     */
    public native void nativeBlitBg(SurfaceData src, SurfaceData dst,
                                    Composite comp, Region clip, int pixel,
                                    int sx, int sy,
                                    int dx, int dy,
                                    int w, int h);

}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: contains x11-specific fixes.
 */

/*
 * @(#)WritableRasterNative.java        1.3 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



package sun.awt.image;

import java.awt.image.WritableRaster;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;
import java.awt.Point;

/**
 * WritableRasterNative
 * This class exists to wrap a native DataBuffer object.  The
 * standard WritableRaster object assumes that a DataBuffer
 * of a given type (e.g., DataBuffer.TYPE_INT) implies a certain
 * subclass (e.g., DataBufferInt).  But this is not always the
 * case.  DataBufferNative, for example, may allow access to
 * integer-based data, but it is not DataBufferInt (which is a
 * final class and cannot be subclassed).
 * So this class exists simply to allow the WritableRaster
 * functionality for this new kind of DataBuffer object.
 */
public class WritableRasterNative extends WritableRaster {

    public static WritableRasterNative createNativeRaster(SampleModel sm, DataBuffer db) {
        return new WritableRasterNative(sm, db);
    }

    protected WritableRasterNative(SampleModel sm, DataBuffer db) {
        super(sm, db, new Point(0, 0));
    }
}

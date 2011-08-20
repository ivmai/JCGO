/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: contains x11-specific fixes.
 */

/*
 * @(#)DataBufferNative.java    1.6 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package sun.awt.image;

import java.awt.image.DataBuffer;
import sun.java2d.SurfaceData;
import java.awt.Rectangle;

/**
 * This class extends <CODE>DataBuffer</CODE> and allows access to
 * native data via the DataBuffer methods.  Note that, unlike other
 * DataBuffer classes, the data is not stored in this class but
 * has been created and stored elsewhere and this class is used
 * merely to access that data.  Note also that this class subclasses
 * from DataBuffer and not from any of the standard subclasses
 * (e.g., DataBufferInt); those subclasses allow the user to
 * get a pointer to the data and manipulate it directly.  That
 * operation may not be possible or wise with native data.
 * One important use of this DataBuffer class is in accessing the
 * data stored in an offscreen vram surface, such as that created
 * by the createVolatileImage() method.
 */

public class DataBufferNative extends DataBuffer
{
    protected SurfaceData surfaceData;
    protected int width;

    /**
     * Constructor.  The constructor of this object requires a
     * SurfaceData object; that surfaceData object will be used
     * to access the actual pixel data in native code.
     */
    public DataBufferNative(SurfaceData sData, int type, int width, int height) {
        super(type, width*height);
        this.width = width;
        this.surfaceData = sData;
    }

    protected native int getElem(int x, int y, SurfaceData sData);

    /**
     * getElem returns the pixel value for a given index into the
     * dataBuffer array.  The bank value is currently ignored (the
     * type of data accessed through this class is not stored in
     * separate banks).  The x and y coordinates of a pixel are calculated
     * from the index value and the native getElem() method is
     * called with the internal surfaceData object.
     */
    public int getElem(int bank, int i) {
        return getElem(i % width, i / width, surfaceData);
    }

    protected native void setElem(int x, int y, int val, SurfaceData sData);

    /**
     * setElem sets the pixel value of a given index into the
     * dataBuffer array.  The bank value is currently ignored (the
     * type of data accessed through this class is not stored in
     * separate banks).  The x and y coordinates of a pixel are calculated
     * from the index value and the native setElem() method is
     * called with the internal surfaceData object.
     */
    public void setElem(int bank, int i, int val) {
        setElem(i % width, i / width, val, surfaceData);
    }

}

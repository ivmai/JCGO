/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WFramePeer.java  1.43 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.windows;

import java.util.Vector;
import java.awt.*;
import java.awt.peer.*;
import java.awt.image.ImageObserver;
import sun.awt.image.ImageRepresentation;
import sun.awt.image.IntegerComponentRaster;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.BufferedImage;
import sun.awt.im.*;
import sun.awt.Win32GraphicsDevice;
import java.awt.image.ColorModel;


class WFramePeer extends WWindowPeer implements FramePeer {

    // FramePeer implementation
    public native void setState(int state);
    public native int getState();

    // Convenience methods to save us from trouble of extracting
    // Rectangle fields in native code.
    private native void setMaximizedBounds(int x, int y, int w, int h);
    private native void clearMaximizedBounds();

    public void setMaximizedBounds(Rectangle b) {
        if (b == null) {
            clearMaximizedBounds();
        } else {
            setMaximizedBounds(b.x, b.y, b.width, b.height);
        }
    }

    boolean isTargetUndecorated() {
        return ((Frame)target).isUndecorated();
    }

    public void reshape(int x, int y, int width, int height) {
        Rectangle rect = constrainBounds(x, y, width, height);
        if (((Frame)target).isUndecorated()) {
            super.reshape(rect.x, rect.y, rect.width, rect.height);
        } else {
            reshapeFrame(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public Dimension getMinimumSize() {
        Dimension d = new Dimension();
        if (!((Frame)target).isUndecorated()) {
            d.setSize(getSysMinWidth(), getSysMinHeight());
        }
        if (((Frame)target).getMenuBar() != null) {
            d.height += getSysMenuHeight();
        }
        return d;
    }

    public void setIconImage(Image im) {
        if (im == null) {
            setIconImageFromIntRasterData(null, null, 0, 0, 0);
        }
        else {
            int w = getSysIconWidth();
            int h = getSysIconHeight();
            BufferedImage bimage =
                new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics g = bimage.getGraphics();
            try {
                if (im instanceof WImage) {
                    ImageRepresentation ir = ((WImage)im).getImageRep();
                    ir.reconstruct(ImageObserver.ALLBITS);
                }
                g.drawImage(im, 0, 0, w, h, null);
            } finally {
                g.dispose();
            }
            Raster  raster = bimage.getRaster();
            DataBuffer buffer = raster.getDataBuffer();
            // REMIND: native code should use ScanStride _AND_ width

        ColorModel alphaCheck = bimage.getColorModel();
        //Create icon mask for transparent icons
        //Color icon, so only creating AND mask, not XOR mask
        byte iconmask[] = new byte[((w * h) + 7 / 8)];
        byte tempAND;
        int bufIdx = 0;
        int maskIdx = 0;
        boolean isTransparent = false;

        for (bufIdx = 0, maskIdx = 0; bufIdx < buffer.getSize() &&
             maskIdx < iconmask.length; maskIdx++) {

            tempAND = 0;
            for (int bitIdx = 0; bitIdx < 8 && bufIdx < buffer.getSize();
                 bitIdx++) {
                //This seems wrong - shouldn't it be masked if alpha
                //ISN'T 0?
                if (alphaCheck.getAlpha(buffer.getElem(bufIdx++)) == 0) {
                    isTransparent = true;
                    tempAND |= (byte)0x1;
                }
                else {
                    tempAND &= (byte)0xFE;
                }
                if (bitIdx < 7) {
                    tempAND = (byte)(tempAND << 1);
                }
            }
            iconmask[maskIdx] = tempAND;
        }
        if (!isTransparent) {
            iconmask = null;
        }

            int ficW = raster.getWidth();
            if (raster instanceof IntegerComponentRaster) {
                ficW = ((IntegerComponentRaster)raster).getScanlineStride();
            }
            setIconImageFromIntRasterData(
                ((DataBufferInt)buffer).getData(), iconmask,
                ficW, raster.getWidth(), raster.getHeight());
        }
    }

    // Note: Because this method calls resize(), which may be overridden
    // by client code, this method must not be executed on the toolkit
    // thread.
    public void setMenuBar(MenuBar mb) {
        WMenuBarPeer mbPeer = (WMenuBarPeer) WToolkit.targetToPeer(mb);
        setMenuBar0(mbPeer);
        updateInsets(insets_);
    }

    // Note: Because this method calls resize(), which may be overridden
    // by client code, this method must not be executed on the toolkit
    // thread.
    private native void setMenuBar0(WMenuBarPeer mbPeer);

    // Toolkit & peer internals

    WFramePeer(Frame target) {
        super(target);

        InputMethodManager imm = InputMethodManager.getInstance();
        String menuString = imm.getTriggerMenuString();
        if (menuString != null)
        {
          pSetIMMOption(menuString);
        }
    }

    native void createAwtFrame(WComponentPeer parent);
    void create(WComponentPeer parent) {
        createAwtFrame(parent);
    }

    void initialize() {
        super.initialize();

        Frame target = (Frame)this.target;

        if (target.getTitle() != null) {
            setTitle(target.getTitle());
        }
        setResizable(target.isResizable());
        setState(target.getExtendedState());

        Image icon = target.getIconImage();
        if (icon != null) {
            setIconImage(icon);
        }
    }

    private native void setIconImageFromIntRasterData(int[] rData,
                                                      byte[] maskData,
                                                      int nScanStride,
                                                      int nW, int nH);
    private native static int getSysIconWidth();
    private native static int getSysIconHeight();
    private native static int getSysMenuHeight();

    native void pSetIMMOption(String option);
    void notifyIMMOptionChange(){
      InputMethodManager.getInstance().notifyChangeRequest((Component)target);
    }
}

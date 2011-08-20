/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WEmbeddedFrame.java      1.22 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import sun.awt.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.peer.ComponentPeer;
import java.util.*;
import java.awt.color.*;
import java.awt.image.*;
import sun.awt.image.ByteInterleavedRaster;


public class WEmbeddedFrame extends EmbeddedFrame {

    static {
        initIDs();
    }

    private long handle;

    private int bandWidth = 0;
    private int bandHeight = 0;
    private BufferedImage bandImage = null;
    private static final int MAX_BAND_SIZE = (1024*768);

    public WEmbeddedFrame() {
        this((long)0);
    }

    /**
     * @deprecated This constructor will be removed in 1.5
     */
    public WEmbeddedFrame(int handle) {
        this((long)handle);
    }

    public WEmbeddedFrame(long handle) {
        this.handle = handle;
        WToolkit toolkit = (WToolkit)Toolkit.getDefaultToolkit();
        setPeer(toolkit.createEmbeddedFrame(this));
        show();
    }

    /*
     * Print the embedded frame and its children using the specified HDC.
     */

    void print(int hdc) {
        int frameHeight = getHeight();
        if (bandImage == null) {
            bandWidth = getWidth();
            if (bandWidth % 4 != 0) {
                bandWidth += (4 - (bandWidth % 4));
            }
            if (bandWidth <= 0) {
                return;
            }

            bandHeight = Math.min(MAX_BAND_SIZE/bandWidth, frameHeight);
            bandImage = new BufferedImage(bandWidth, bandHeight,
                                          BufferedImage.TYPE_3BYTE_BGR);
        }

        Graphics clearGraphics = bandImage.getGraphics();
        clearGraphics.setColor(Color.white);
        Graphics g2d = bandImage.getGraphics();

        ByteInterleavedRaster ras = (ByteInterleavedRaster)bandImage.getRaster();
        byte[] data = ras.getDataStorage();

        for (int bandTop = 0; bandTop < frameHeight; bandTop += bandHeight) {
            clearGraphics.fillRect(0, 0, bandWidth, bandHeight);

            printComponents(g2d);

            int currBandHeight = bandHeight;
            if ((bandTop+bandHeight) > frameHeight) {
                // last band
                currBandHeight = frameHeight - bandTop;
            }
            printBand((long)hdc, data, 0, bandTop, bandWidth, currBandHeight);
            g2d.translate(0, -bandHeight);
        }
    }

    public void synthesizeWindowActivation(boolean doActivate) {
        synthesizeWmActivate(handle, doActivate);
    }

    private static native void synthesizeWmActivate(long handle,
                                                    boolean doActivate);

    protected native void printBand(long hdc, byte[] data, int x, int y,
                                    int width, int height);

    /**
     * Initialize JNI field IDs
     */
    private static native void initIDs();
}

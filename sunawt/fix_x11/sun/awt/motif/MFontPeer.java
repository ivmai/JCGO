/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MFontPeer.java   1.21 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package sun.awt.motif;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.io.CharToByteConverter;
import sun.awt.PlatformFont;
import sun.io.CharToByteISO8859_1;
import java.awt.GraphicsEnvironment;
import java.util.Properties;

public class MFontPeer extends PlatformFont {

    /*
     * XLFD name for XFontSet.
     */
    private String xfsname;
    /*
     * converter name for this XFontSet encoding.
     */
    private String converter;

    static {
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Initialize JNI field and method IDs for fields that may be
       accessed from C.
     */
    private static native void initIDs();

    public MFontPeer(String name, int style){
        super(name, style);

        if (props != null){
            xfsname = props.getProperty
                ("fontset." + familyName + "." + styleString);
        }
    }

    public CharToByteConverter
        getFontCharset(final String charsetName, String fontName){

        CharToByteConverter fc;
        if (charsetName.equals("default")){
            fc = (CharToByteConverter)charsetRegistry.get(fontName);
        } else {
            fc = (CharToByteConverter)charsetRegistry.get(charsetName);
        }
        if (fc instanceof CharToByteConverter){
            return fc;
        }

        Class fcc = (Class)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Class.forName(charsetName, true,
                        Thread.currentThread().getContextClassLoader());
                } catch(ClassNotFoundException e){
                    try {
                        return Class.forName("sun.io." + charsetName, true,
                            Thread.currentThread().getContextClassLoader());
                    } catch (ClassNotFoundException exx){
                        try {
                            return Class.forName("sun.awt.motif." + charsetName,
                                true, Thread.currentThread().getContextClassLoader());
                        } catch(ClassNotFoundException ex){
                            return null;
                        }
                    }
                }
            }
        });

        if (fcc == null) {
            return getDefaultFontCharset(fontName);
        }

        try {
            fc = (CharToByteConverter)fcc.newInstance();
        } catch(Exception e){
            return getDefaultFontCharset(fontName);
        }

        if (charsetName.equals("default")){
            charsetRegistry.put(fontName, fc);
        } else {
            charsetRegistry.put(charsetName, fc);
        }
        return fc;
    }


    private CharToByteConverter getDefaultFontCharset(String fontName){
        return new CharToByteISO8859_1();
    }


    /**
     * Get default font for Motif widgets to use, preventing them from
     * wasting time accessing inappropriate X resources.  This is called
     * only from native code.
     *
     * This is part of a Motif specific performance enhancement.  By
     * default, when Motif widgets are created and initialized, Motif will
     * set up default fonts for the widgets, which we ALWAYS override.
     * This set up includes finding the default font in the widget's X
     * resources and fairly expensive requests of the X server to identify
     * the specific font or fontset.  We avoid all of this overhead by
     * providing a well known font to use at the creation of widgets, where
     * possible.
     *
     * The X11 fonts are specified by XLFD strings which have %d as a
     * marker to indicate where the fontsize should be substituted.  [The
     * libc function sprintf() is used to replace it.]  The value 140
     * specifies a font size of 14 points.
     */
    private static String getDefaultMotifFontSet() {
        int i;
        String font = fprops.getProperty("fontset.default");
        if (font != null) {
            while ((i = font.indexOf("%d")) >= 0)
                font = font.substring(0, i) + "140" + font.substring(i+2);
        }
        return font;
    }
}

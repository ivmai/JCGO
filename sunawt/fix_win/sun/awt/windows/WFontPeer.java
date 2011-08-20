/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WFontPeer.java   1.16 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.io.CharToByteConverter;
import sun.awt.PlatformFont;

public class WFontPeer extends PlatformFont {

    public WFontPeer(String name, int style){
        super(name, style);
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
                } catch(ClassNotFoundException e) {
                    try {
                        return Class.forName("sun.io." + charsetName, true,
                            Thread.currentThread().getContextClassLoader());
                    } catch (ClassNotFoundException exx) {
                        try {
                            return Class.forName("sun.awt.windows." + charsetName,
                                true, Thread.currentThread().getContextClassLoader());
                        } catch(ClassNotFoundException ex) {
                            return null;
                        }
                    }
                }
            }
        });

        if (fcc == null) {
            fc = getDefaultFontCharset(fontName);
        }

        if (fc == null) {
            try {
                fc = (CharToByteConverter)fcc.newInstance();
            } catch(Exception e) {
                return getDefaultFontCharset(fontName);
            }
        }

        if (charsetName.equals("default")){
            charsetRegistry.put(fontName, fc);
        } else {
            charsetRegistry.put(charsetName, fc);
        }
        return fc;
    }


    private CharToByteConverter getDefaultFontCharset(String fontName){
        return new WDefaultFontCharset(fontName);
    }
}

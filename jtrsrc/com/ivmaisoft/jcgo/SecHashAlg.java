/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/SecHashAlg.java --
 * a part of JCGO translator.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 **
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License (GPL) for more details.
 **
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 **
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module. An independent module is a module which is not derived from
 * or based on this library. If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package com.ivmaisoft.jcgo;

/**
 * This class implements the Secure Hash Algorithm (SHA).
 */

final class SecHashAlg {

    private int count;

    private int aa;

    private int bb;

    private int cc;

    private int dd;

    private int ee;

    private final int[] w = new int[80];

    SecHashAlg() {
        init();
    }

    void init() {
        aa = 0x67452301;
        bb = 0xefcdab89;
        cc = 0x98badcfe;
        dd = 0x10325476;
        ee = 0xc3d2e1f0;
        for (int i = 0; i < 80; i++) {
            w[i] = 0;
        }
        count = 0;
    }

    private void computeBlock() {
        for (int t = 16; t < 80; t++) {
            int v = w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16];
            w[t] = (v << 1) | (v >>> 31);
        }
        int a = aa;
        int b = bb;
        int c = cc;
        int d = dd;
        int e = ee;
        for (int i = 0; i < 20; i++) {
            int v = ((a << 5) | (a >>> 27)) + ((b & c) | (~b & d)) + e + w[i]
                    + 0x5a827999;
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = v;
        }
        for (int i = 20; i < 40; i++) {
            int v = ((a << 5) | (a >>> 27)) + (b ^ c ^ d) + e + w[i]
                    + 0x6ed9eba1;
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = v;
        }
        for (int i = 40; i < 60; i++) {
            int v = ((a << 5) | (a >>> 27)) + ((b & c) | (b & d) | (c & d)) + e
                    + w[i] + 0x8f1bbcdc;
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = v;
        }
        for (int i = 60; i < 80; i++) {
            int v = ((a << 5) | (a >>> 27)) + (b ^ c ^ d) + e + w[i]
                    + 0xca62c1d6;
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = v;
        }
        aa += a;
        bb += b;
        cc += c;
        dd += d;
        ee += e;
    }

    void engineUpdate(int b) {
        int word = (count & 0x3f) >>> 2;
        int offset = (~count & 0x3) << 3;
        w[word] = (w[word] & ~(0xff << offset)) | ((b & 0xff) << offset);
        if ((count & 0x3f) == 0x3f) {
            computeBlock();
        }
        count++;
    }

    void updateInt(int v) {
        engineUpdate(v >> 24);
        engineUpdate(v >> 16);
        engineUpdate(v >> 8);
        engineUpdate(v);
    }

    void updateUTF(String str) {
        int len = getUTFlength(str);
        engineUpdate(len >> 8);
        engineUpdate(len);
        len = str.length();
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == 0 || ch > 0x7f) {
                if (ch > 0x7ff) {
                    engineUpdate((ch >> 12) | 0xe0);
                    engineUpdate(((ch >> 6) & 0x3f) | 0x80);
                } else {
                    engineUpdate((ch >> 6) | 0xc0);
                }
                ch = (char) ((ch & 0x3f) | 0x80);
            }
            engineUpdate(ch);
        }
    }

    private static int getUTFlength(String str) {
        int len = str.length();
        int utfLen = len;
        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);
            if (ch == 0 || ch > 0x7f) {
                utfLen++;
                if (ch > 0x7ff) {
                    utfLen++;
                }
            }
        }
        return utfLen;
    }

    void finishUpdate() {
        int bits = count << 3;
        engineUpdate(0x80);
        while ((count & 0x3f) != 0x38) {
            engineUpdate(0);
        }
        w[14] = 0;
        w[15] = bits;
        count += 8;
        computeBlock();
    }

    int engineDigestVal(byte[] hashvalue) {
        if (hashvalue != null) {
            hashvalue[0] = (byte) (aa >>> 24);
            hashvalue[1] = (byte) (aa >> 16);
            hashvalue[2] = (byte) (aa >> 8);
            hashvalue[3] = (byte) aa;
            hashvalue[4] = (byte) (bb >>> 24);
            hashvalue[5] = (byte) (bb >> 16);
            hashvalue[6] = (byte) (bb >> 8);
            hashvalue[7] = (byte) bb;
            hashvalue[8] = (byte) (cc >>> 24);
            hashvalue[9] = (byte) (cc >> 16);
            hashvalue[10] = (byte) (cc >> 8);
            hashvalue[11] = (byte) cc;
            hashvalue[12] = (byte) (dd >>> 24);
            hashvalue[13] = (byte) (dd >> 16);
            hashvalue[14] = (byte) (dd >> 8);
            hashvalue[15] = (byte) dd;
            hashvalue[16] = (byte) (ee >>> 24);
            hashvalue[17] = (byte) (ee >> 16);
            hashvalue[18] = (byte) (ee >> 8);
            hashvalue[19] = (byte) ee;
        }
        return aa;
    }
}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)ParseUtil.java   1.16 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.net.www;

import java.util.BitSet;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A class that contains useful routines common to sun.net.www
 * @author  Mike McCloskey
 */

public class ParseUtil {
    static BitSet encodedInPath;

    static {
        encodedInPath = new BitSet(256);

        // Set the bits corresponding to characters that are encoded in the
        // path component of a URI.

        // These characters are reserved in the path segment as described in
        // RFC2396 section 3.3.
        encodedInPath.set('=');
        encodedInPath.set(';');
        encodedInPath.set('?');
        encodedInPath.set('/');

        // These characters are defined as excluded in RFC2396 section 2.4.3
        // and must be escaped if they occur in the data part of a URI.
        encodedInPath.set('#');
        encodedInPath.set(' ');
        encodedInPath.set('<');
        encodedInPath.set('>');
        encodedInPath.set('%');
        encodedInPath.set('"');
        encodedInPath.set('{');
        encodedInPath.set('}');
        encodedInPath.set('|');
        encodedInPath.set('\\');
        encodedInPath.set('^');
        encodedInPath.set('[');
        encodedInPath.set(']');
        encodedInPath.set('`');

        // US ASCII control characters 00-1F and 7F.
        for (int i=0; i<32; i++)
            encodedInPath.set(i);
        encodedInPath.set(127);
    }

    /**
     * Constructs an encoded version of the specified path string suitable
     * for use in the construction of a URL.
     *
     * A path separator is replaced by a forward slash. The string is UTF8
     * encoded. The % escape sequence is used for characters that are above
     * 0x7F or those defined in RFC2396 as reserved or excluded in the path
     * component of a URL.
     */
    public static String encodePath(String path) {
        StringBuffer sb = new StringBuffer();
        int n = path.length();
        for (int i=0; i<n; i++) {
            char c = path.charAt(i);
            if (c == File.separatorChar)
                sb.append('/');
            else {
                if (c <= 0x007F) {
                    if (encodedInPath.get(c))
                        escape(sb, c);
                    else
                        sb.append(c);
                } else if (c > 0x07FF) {
                    escape(sb, (char)(0xE0 | ((c >> 12) & 0x0F)));
                    escape(sb, (char)(0x80 | ((c >>  6) & 0x3F)));
                    escape(sb, (char)(0x80 | ((c >>  0) & 0x3F)));
                } else {
                    escape(sb, (char)(0xC0 | ((c >>  6) & 0x1F)));
                    escape(sb, (char)(0x80 | ((c >>  0) & 0x3F)));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Appends the URL escape sequence for the specified char to the
     * specified StringBuffer.
     */
    private static void escape(StringBuffer s, char c) {
        s.append('%');
        s.append(Character.forDigit((c >> 4) & 0xF, 16));
        s.append(Character.forDigit(c & 0xF, 16));
    }

    /**
     * Un-escape and return the character at position i in string s.
     */
    private static char unescape(String s, int i) {
        return (char) Integer.parseInt(s.substring(i+1,i+3),16);
    }

    /**
     * Returns a new String constructed from the specified String by replacing
     * the URL escape sequences and UTF8 encoding with the characters they
     * represent.
     */
    public static String decode(String s) {
        StringBuffer sb = new StringBuffer();

        int i=0;
        while (i<s.length()) {
            char c = s.charAt(i);
            char c2, c3;

            if (c != '%') {
                i++;
            } else {
                try {
                    c = unescape(s, i);
                    i += 3;

                    if ((c & 0x80) != 0) {
                        switch (c >> 4) {
                            case 0xC: case 0xD:
                                c2 = unescape(s, i);
                                i += 3;
                                c = (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                                break;

                            case 0xE:
                                c2 = unescape(s, i);
                                i += 3;
                                c3 = unescape(s, i);
                                i += 3;
                                c = (char)(((c & 0x0f) << 12) |
                                           ((c2 & 0x3f) << 6) |
                                            (c3 & 0x3f));
                                break;

                            default:
                                throw new IllegalArgumentException();
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Returns a canonical version of the specified string.
     */
    public String canonizeString(String file) {
        int i = 0;
        int lim = file.length();

        // Remove embedded /../
        while ((i = file.indexOf("/../")) >= 0) {
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim) + file.substring(i + 3);
            } else {
                file = file.substring(i + 3);
            }
        }
        // Remove embedded /./
        while ((i = file.indexOf("/./")) >= 0) {
            file = file.substring(0, i) + file.substring(i + 2);
        }
        // Remove trailing ..
        while (file.endsWith("/..")) {
            i = file.indexOf("/..");
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim+1);
            } else {
                file = file.substring(0, i);
            }
        }
        // Remove trailing .
        if (file.endsWith("/."))
            file = file.substring(0, file.length() -1);

        return file;
    }

    public static URL fileToEncodedURL(File file)
        throws MalformedURLException
    {
        String path = file.getAbsolutePath();
        path = ParseUtil.encodePath(path);
        if (!path.startsWith("/")) {
            path = "/" + path;
        } else if (path.startsWith("//")) {
            path = "//" + path;
        }
        if (!path.endsWith("/") && file.isDirectory()) {
            path = path + "/";
        }
        return new URL("file", "", path);
    }

}

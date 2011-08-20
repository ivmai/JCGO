/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)NativeFontWrapper.java   1.61 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @author Charlton Innovations, Inc.
 */

package sun.awt.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import sun.java2d.SurfaceData;


/**
 * NativeFontWrapper: a collection of native methods which perform
 * mutual exlcusion to prevent problems with non-reentrant native
 * code:  REMIND: make native code reentrant
 */

public class NativeFontWrapper {

    private static boolean usePlatformFontMetrics = false;

    static {
        java.security.AccessController.doPrivileged(
                  new sun.security.action.LoadLibraryAction("awt"));
        java.security.AccessController.doPrivileged(
                  new sun.security.action.LoadLibraryAction("fontmanager"));

        /*
         * Check if the old buggy font metrics are requested.
         * Enable this on win32 platforms only.
         */
       String osName = (String) java.security.AccessController.doPrivileged(
             new sun.security.action.GetPropertyAction("os.name"));
       String prop = (String) java.security.AccessController.doPrivileged(
             new sun.security.action.GetPropertyAction(
                                          "java2d.font.usePlatformFont"));
       if (osName != null && osName.indexOf("Windows") != -1 &&
           ("true".equals(prop) || getPlatformFontVar())) {
           usePlatformFontMetrics = true;
           System.out.println("Enabling platform font metrics for win32. This is an unsupported option.");
       }
    }

    /*
     * Workaround for apps which are dependent on a font metrics bug
     * in JDK 1.1. This is an unsupported win32 private setting.
     */
    public static boolean usePlatformFontMetrics() {
        return usePlatformFontMetrics;
    }

    static /* native */ boolean getPlatformFontVar() {
        try {
            return System.getenv("JAVA2D_USEPLATFORMFONT") != null;
        }
        catch (ThreadDeath e) {
            throw e;
        }
        catch (Error e) {
        }
        catch (SecurityException e) {
        }
        return false;
    }


    public static final int FONTFORMAT_NONE = -1;
    public static final int FONTFORMAT_TRUETYPE = 0;
    public static final int FONTFORMAT_TYPE1    = 1;
    public static final int FONTFORMAT_T2K      = 2;
    public static final int FONTFORMAT_TTC      = 3;
    public static final int FONTFORMAT_COMPOSITE = 4;
    public static final int FONTFORMAT_NATIVE   = 5;

    public static final int ROTATE_UNKNOWN = -1;
    public static final int ROTATE_FALSE = 0;
    public static final int ROTATE_TRUE = 1;
    public static final int ROTATE_CHECK_STRING = 2;

    public synchronized static native String getFontPath(boolean noType1Fonts);
    public synchronized static native void setNativeFontPath(String fontPath);

    // methods which operate on the font collection
    public synchronized static native void registerFonts(
            Vector fontCollection,
            int size, Vector nameCollection, int fontFormat, boolean useJavaRasterizer);
    public synchronized static native void registerCompositeFont(
            String compositeFontName,
            String [] componentFontNames,
            int [] exclusionRanges, int [] exclusionCounts);

    /**
     * Returns whether a font is registered that has fontName as a family
     * name or a face name and supports the given style (more or less).
     */
    public synchronized static native boolean isFontRegistered(String fontName, int style);

    public synchronized static native int getNumFonts();
    public synchronized static native String getFamilyNameByIndex(
            int fontIndex, int localeID);
    public synchronized static native String getFullNameByIndex(
            int fontIndex);
    public synchronized static native String getFullNameByFileName(
            String fileName);
    public synchronized static native String createFont ( String fontFilePath, int fontFormat );
    public synchronized static native boolean getType1FontVar();




// device independent font operations -- float returns are in "points"
    // REMIND: may want more state in the native instance
    public synchronized static native void initializeFont(
            Font theFont, String name, int style);
    public synchronized static native String getFamilyName(Font theFont, short lcid);
    public synchronized static native String getFullName(Font theFont, short lcid);
    public synchronized static native String getPostscriptName(Font theFont);
    public synchronized static native int getNumGlyphs(Font theFont);
    public synchronized static native int getMissingGlyphCode(Font theFont);
    public static byte getBaselineFor(Font theFont, char c) {
        return 0;
    }
    public synchronized static native boolean canDisplay(Font theFont, char c);
    public synchronized static native boolean doesGlyphShaping(Font theFont);
    public synchronized static native boolean isStyleSupported(Font theFont,int theStyle);
    public synchronized static native boolean charsToGlyphs(Font theFont,
           char[] chars, int coffset, int[] glyphs, int goffset, int count);
// end of device independent font operations

// device dependent font operations -- float returns are in "pixels"
    /**
     * returns baselines[]
     */
    public static void getBaseLineOffsetsFor(
            Font theFont, char c,
            double[] matrix,
            float [] baselines)
    {
        float metrics[] = {0, 0, 0, 0};
        getFontMetrics(theFont, matrix, false, false, metrics);
        //REMIND: replace with real implementation
        baselines[0] = 0;
        baselines[1] = (metrics[0] + metrics[1]) / 2 - metrics[0];
        baselines[2] = metrics[1] - metrics[0];
    }

    /* Does the font handle rotation */
    public synchronized static native int fontCanRotate (
            Font theFont);

    /* Can the font rotate the given string */
    public synchronized static native boolean fontCanRotateText(
            Font theFont,
            String s,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable);

    /* Can the font rotate the given Glyph Vector */
    public synchronized static native boolean fontCanRotateGlyphVector(
            Font theFont,
            int [] gv,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable);

    /**
     * Returns metrics constaining four floats: the ascent, descent, leading, and max advance.
     */
    // REMIND - answers depend on baseline
    public synchronized static native void getFontMetrics(
            Font font,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable,
            float [] metrics);

    public synchronized static native float getItalicAngle(
            Font font,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable);

    // performs a char-to-glyph on the ch, then returns the glyph metrics
    public synchronized static native void getCharMetrics(
            Font theFont,
            int ch,
            double[] fontTx,
            double[] devTx,
            boolean doAntiAlias,
            boolean doFractEnable,
            float[] result);

  // this returns a shape representing the logical bounds of the (possibly transformed) glyph.
  public synchronized static native Shape getGlyphLogicalBounds(
    Font theFont,
    int glyphIndex,
    int[] glyphs,
    float[] positions,
    double[] transforms,
    int[] txIndices,
    double[] fontTX,
    boolean doAntiAlias,
    boolean doFractEnable);

  // this returns the bounds of the glyph vector outline
  public synchronized static native Rectangle2D getVisualBounds(
    Font theFont,
    int[] glyphs,
    float[] positions,
    double[] transforms,
    int[] txIndices,
    double[] fontTX,
    boolean doAntiAlias,
    boolean doFractEnable);

  // this returns the bounds of the outline of the given glyph
  public synchronized static native Rectangle2D getGlyphVisualBounds(
    Font theFont,
    int glyphIndex,
    int[] glyphs,
    float[] positions,
    double[] transforms,
    int[] txIndices,
    double[] fontTX,
    boolean doAntiAlias,
    boolean doFractEnable);

    // this returns the advX, advY, l, t, w, h of the glyph as rendered using matrix.
    public synchronized static native void getGlyphMetrics(
            Font theFont,
            int glyphCode,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable,
            float[] result);

    public synchronized static native float[] getGlyphInfo(
            Font font,
            int[] glyphs,
            float[] positions,
            double[] transforms,
            int[] txIndices,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable);

   public synchronized static native Rectangle getPixelBounds(
            Font font,
            float gvx,
            float gvy,
            int[] glyphs,
            float[] positions,
            double[] transforms,
            int[] txIndices,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable);

   public synchronized static native Rectangle getGlyphPixelBounds(
            Font font,
            int glyphIndex,
            float gvx,
            float gvy,
            int[] glyphs,
            float[] positions,
            double[] transforms,
            int[] txIndices,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable);

// end of device dependent font operations

// glyphvector operations
    /* public synchronized static native void populateGlyphVector(
            Font theFont,
            char [] unicodes,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable,
            GlyphVector target
    ); */
    public synchronized static native void populateGlyphVector(
            Font theFont,
            char [] unicodes,
            int start,
            int count,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable,
            GlyphVector target
    );
    public synchronized static native void populateAndLayoutGlyphVector(
            Font theFont,
            char [] unicodes,
            int start,
            int count,
            int flags,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable,
            GlyphVector target
    );
    public synchronized static native void layoutGlyphVector(
            Font theFont,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable,
            GlyphVector target
    );
    // REMIND: implement shaping client.
    // if it is not present, perform maximum compression
    // otherwise, ask the client to approve substitutions, and
    // then give it a bidirectional mapping from src to dst indices
    public synchronized static native void shapeGlyphVector(
            GlyphVector src,
            Font theFont,
            double[] matrix,
            boolean doAntiAlias,
            boolean doFractEnable,
            Object  shapingClient,
            GlyphVector target
    );
// end of glyphvector operations


// drawGlyphVector operations
    public synchronized static native Shape getGlyphVectorOutline(
            GlyphVector src,
            Font theFont,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable,
            float xpos,
            float ypos
    );
    public synchronized static native Shape getGlyphOutline(
            GlyphVector src,
            int glyphIndex,
            Font theFont,
            double[] fontTX,
            double[] devTX,
            boolean doAntiAlias,
            boolean doFractEnable,
            float xpos,
            float ypos
    );


    private static final short US_LCID = 0x0409;  // US English - default

    // Return a Microsoft LCID from the given Locale.
    // Used when getting localized font data.
    public static short getLCIDFromLocale(Locale locale) {

        // optimize for common case
        if (locale.equals(Locale.US)) {
            return US_LCID;
        }

        if (lcidMap == null) {
            createLCIDMap();
        }

        String key = locale.toString();
        while (!"".equals(key)) {
            Short lcidObject = (Short) lcidMap.get(key);
            if (lcidObject != null) {
                return lcidObject.shortValue();
            }
            int pos = key.lastIndexOf('_');
            if (pos < 1) {
                return US_LCID;
            }
            key = key.substring(0, pos);
        }

        return US_LCID;
    }

    private static Map lcidMap;

    private static synchronized void createLCIDMap() {
        if (lcidMap != null) {
            return;
        }

        Map map = new HashMap(200);

        // the following statements are derived from the langIDMap
        // in src/windows/native/java/lang/java_props_md.c using the following
        // awk script:
        //    $1~/\/\*/   { next}
        //    $3~/\?\?/   { next }
        //    $3!~/_/     { next }
        //    $1~/0x0409/ { next }
        //    $1~/0x0c0a/ { next }
        //    $1~/0x042c/ { next }
        //    $1~/0x0443/ { next }
        //    $1~/0x0812/ { next }
        //    $1~/0x04/   { print "        addLCIDMapEntry(map, " substr($3, 0, 3) "\", (short) " substr($1, 0, 6) ");" ; next }
        //    $3~/,/      { print "        addLCIDMapEntry(map, " $3  " (short) " substr($1, 0, 6) ");" ; next }
        //                { print "        addLCIDMapEntry(map, " $3 ", (short) " substr($1, 0, 6) ");" ; next }
        // The lines of this script:
        // - eliminate comments
        // - eliminate questionable locales
        // - eliminate language-only locales
        // - eliminate the default LCID value
        // - eliminate a few other unneeded LCID values
        // - print language-only locale entries for x04* LCID values
        //   (apparently Microsoft doesn't use language-only LCID values -
        //   see http://www.microsoft.com/OpenType/otspec/name.htm
        // - print complete entries for all other LCID values
        // Run
        //     awk -f awk-script langIDMap > statements
        addLCIDMapEntry(map, "ar", (short) 0x0401);
        addLCIDMapEntry(map, "bg", (short) 0x0402);
        addLCIDMapEntry(map, "ca", (short) 0x0403);
        addLCIDMapEntry(map, "zh", (short) 0x0404);
        addLCIDMapEntry(map, "cs", (short) 0x0405);
        addLCIDMapEntry(map, "da", (short) 0x0406);
        addLCIDMapEntry(map, "de", (short) 0x0407);
        addLCIDMapEntry(map, "el", (short) 0x0408);
        addLCIDMapEntry(map, "es", (short) 0x040a);
        addLCIDMapEntry(map, "fi", (short) 0x040b);
        addLCIDMapEntry(map, "fr", (short) 0x040c);
        addLCIDMapEntry(map, "iw", (short) 0x040d);
        addLCIDMapEntry(map, "hu", (short) 0x040e);
        addLCIDMapEntry(map, "is", (short) 0x040f);
        addLCIDMapEntry(map, "it", (short) 0x0410);
        addLCIDMapEntry(map, "ja", (short) 0x0411);
        addLCIDMapEntry(map, "ko", (short) 0x0412);
        addLCIDMapEntry(map, "nl", (short) 0x0413);
        addLCIDMapEntry(map, "no", (short) 0x0414);
        addLCIDMapEntry(map, "pl", (short) 0x0415);
        addLCIDMapEntry(map, "pt", (short) 0x0416);
        addLCIDMapEntry(map, "rm", (short) 0x0417);
        addLCIDMapEntry(map, "ro", (short) 0x0418);
        addLCIDMapEntry(map, "ru", (short) 0x0419);
        addLCIDMapEntry(map, "hr", (short) 0x041a);
        addLCIDMapEntry(map, "sk", (short) 0x041b);
        addLCIDMapEntry(map, "sq", (short) 0x041c);
        addLCIDMapEntry(map, "sv", (short) 0x041d);
        addLCIDMapEntry(map, "th", (short) 0x041e);
        addLCIDMapEntry(map, "tr", (short) 0x041f);
        addLCIDMapEntry(map, "ur", (short) 0x0420);
        addLCIDMapEntry(map, "in", (short) 0x0421);
        addLCIDMapEntry(map, "uk", (short) 0x0422);
        addLCIDMapEntry(map, "be", (short) 0x0423);
        addLCIDMapEntry(map, "sl", (short) 0x0424);
        addLCIDMapEntry(map, "et", (short) 0x0425);
        addLCIDMapEntry(map, "lv", (short) 0x0426);
        addLCIDMapEntry(map, "lt", (short) 0x0427);
        addLCIDMapEntry(map, "fa", (short) 0x0429);
        addLCIDMapEntry(map, "vi", (short) 0x042a);
        addLCIDMapEntry(map, "hy", (short) 0x042b);
        addLCIDMapEntry(map, "eu", (short) 0x042d);
        addLCIDMapEntry(map, "mk", (short) 0x042f);
        addLCIDMapEntry(map, "tn", (short) 0x0432);
        addLCIDMapEntry(map, "af", (short) 0x0436);
        addLCIDMapEntry(map, "ka", (short) 0x0437);
        addLCIDMapEntry(map, "fo", (short) 0x0438);
        addLCIDMapEntry(map, "hi", (short) 0x0439);
        addLCIDMapEntry(map, "mt", (short) 0x043a);
        addLCIDMapEntry(map, "gd", (short) 0x043c);
        addLCIDMapEntry(map, "ms", (short) 0x043e);
        addLCIDMapEntry(map, "kk", (short) 0x043f);
        addLCIDMapEntry(map, "ky", (short) 0x0440);
        addLCIDMapEntry(map, "sw", (short) 0x0441);
        addLCIDMapEntry(map, "pa", (short) 0x0446);
        addLCIDMapEntry(map, "gu", (short) 0x0447);
        addLCIDMapEntry(map, "ta", (short) 0x0449);
        addLCIDMapEntry(map, "te", (short) 0x044a);
        addLCIDMapEntry(map, "kn", (short) 0x044b);
        addLCIDMapEntry(map, "mr", (short) 0x044e);
        addLCIDMapEntry(map, "sa", (short) 0x044f);
        addLCIDMapEntry(map, "mn", (short) 0x0450);
        addLCIDMapEntry(map, "gl", (short) 0x0456);
        addLCIDMapEntry(map, "ar_IQ", (short) 0x0801);
        addLCIDMapEntry(map, "zh_CN", (short) 0x0804);
        addLCIDMapEntry(map, "de_CH", (short) 0x0807);
        addLCIDMapEntry(map, "en_GB", (short) 0x0809);
        addLCIDMapEntry(map, "es_MX", (short) 0x080a);
        addLCIDMapEntry(map, "fr_BE", (short) 0x080c);
        addLCIDMapEntry(map, "it_CH", (short) 0x0810);
        addLCIDMapEntry(map, "nl_BE", (short) 0x0813);
        addLCIDMapEntry(map, "no_NO_NY", (short) 0x0814);
        addLCIDMapEntry(map, "pt_PT", (short) 0x0816);
        addLCIDMapEntry(map, "ro_MD", (short) 0x0818);
        addLCIDMapEntry(map, "ru_MD", (short) 0x0819);
        addLCIDMapEntry(map, "sh_YU", (short) 0x081a);
        addLCIDMapEntry(map, "sv_FI", (short) 0x081d);
        addLCIDMapEntry(map, "az_AZ", (short) 0x082c);
        addLCIDMapEntry(map, "ga_IE", (short) 0x083c);
        addLCIDMapEntry(map, "ms_BN", (short) 0x083e);
        addLCIDMapEntry(map, "uz_UZ", (short) 0x0843);
        addLCIDMapEntry(map, "ar_EG", (short) 0x0c01);
        addLCIDMapEntry(map, "zh_HK", (short) 0x0c04);
        addLCIDMapEntry(map, "de_AT", (short) 0x0c07);
        addLCIDMapEntry(map, "en_AU", (short) 0x0c09);
        addLCIDMapEntry(map, "fr_CA", (short) 0x0c0c);
        addLCIDMapEntry(map, "sr_YU", (short) 0x0c1a);
        addLCIDMapEntry(map, "ar_LY", (short) 0x1001);
        addLCIDMapEntry(map, "zh_SG", (short) 0x1004);
        addLCIDMapEntry(map, "de_LU", (short) 0x1007);
        addLCIDMapEntry(map, "en_CA", (short) 0x1009);
        addLCIDMapEntry(map, "es_GT", (short) 0x100a);
        addLCIDMapEntry(map, "fr_CH", (short) 0x100c);
        addLCIDMapEntry(map, "ar_DZ", (short) 0x1401);
        addLCIDMapEntry(map, "zh_MO", (short) 0x1404);
        addLCIDMapEntry(map, "de_LI", (short) 0x1407);
        addLCIDMapEntry(map, "en_NZ", (short) 0x1409);
        addLCIDMapEntry(map, "es_CR", (short) 0x140a);
        addLCIDMapEntry(map, "fr_LU", (short) 0x140c);
        addLCIDMapEntry(map, "ar_MA", (short) 0x1801);
        addLCIDMapEntry(map, "en_IE", (short) 0x1809);
        addLCIDMapEntry(map, "es_PA", (short) 0x180a);
        addLCIDMapEntry(map, "fr_MC", (short) 0x180c);
        addLCIDMapEntry(map, "ar_TN", (short) 0x1c01);
        addLCIDMapEntry(map, "en_ZA", (short) 0x1c09);
        addLCIDMapEntry(map, "es_DO", (short) 0x1c0a);
        addLCIDMapEntry(map, "ar_OM", (short) 0x2001);
        addLCIDMapEntry(map, "en_JM", (short) 0x2009);
        addLCIDMapEntry(map, "es_VE", (short) 0x200a);
        addLCIDMapEntry(map, "ar_YE", (short) 0x2401);
        addLCIDMapEntry(map, "es_CO", (short) 0x240a);
        addLCIDMapEntry(map, "ar_SY", (short) 0x2801);
        addLCIDMapEntry(map, "en_BZ", (short) 0x2809);
        addLCIDMapEntry(map, "es_PE", (short) 0x280a);
        addLCIDMapEntry(map, "ar_JO", (short) 0x2c01);
        addLCIDMapEntry(map, "en_TT", (short) 0x2c09);
        addLCIDMapEntry(map, "es_AR", (short) 0x2c0a);
        addLCIDMapEntry(map, "ar_LB", (short) 0x3001);
        addLCIDMapEntry(map, "en_ZW", (short) 0x3009);
        addLCIDMapEntry(map, "es_EC", (short) 0x300a);
        addLCIDMapEntry(map, "ar_KW", (short) 0x3401);
        addLCIDMapEntry(map, "en_PH", (short) 0x3409);
        addLCIDMapEntry(map, "es_CL", (short) 0x340a);
        addLCIDMapEntry(map, "ar_AE", (short) 0x3801);
        addLCIDMapEntry(map, "es_UY", (short) 0x380a);
        addLCIDMapEntry(map, "ar_BH", (short) 0x3c01);
        addLCIDMapEntry(map, "es_PY", (short) 0x3c0a);
        addLCIDMapEntry(map, "ar_QA", (short) 0x4001);
        addLCIDMapEntry(map, "es_BO", (short) 0x400a);
        addLCIDMapEntry(map, "es_SV", (short) 0x440a);
        addLCIDMapEntry(map, "es_HN", (short) 0x480a);
        addLCIDMapEntry(map, "es_NI", (short) 0x4c0a);
        addLCIDMapEntry(map, "es_PR", (short) 0x500a);

        lcidMap = map;
    }

    private static void addLCIDMapEntry(Map map, String key, short value) {
        map.put(key, new Short(value));
    }

    public static native boolean fontSupportsEncoding(Font font,
                                                      String encoding);

// methods to remove -- REMIND - do it
    // REMIND questionable value
    // REMIND need matrix on this call
    public synchronized static native void getGlyphJustificationInfo(
                                                Font theFont,
                                                int glyphCode,
                                                GlyphJustificationInfo gji);
// end of methods to remove -- REMIND - do it
}

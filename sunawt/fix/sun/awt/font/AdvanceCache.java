/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * A cache of latin-1, kana, and cjk advances, to speed up TextLayout metrics operations.
 *
 * @(#)AdvanceCache.java        1.4 02/10/09
 */
package sun.awt.font;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;

public final class AdvanceCache {
    private Font font;
    private FontRenderContext frc;
    private LineMetrics lineMetrics;
    private float[] latinAdvances;
    private float[] kanaAdvances; // hira, kana almost always monospaced, but just in case
    private float cjkAdvance;
    private float cjkFullAdvance; // probably redundant with cjkAdvance, but might have a bad composite font
    private float cjkHalfAdvance;
    private float[] latin1GlyphInfo; // redundant with advances, but advances are quicker to index?
    private float[] kanaGlyphInfo;
    private float[] missingGlyphInfo;
    private float missingGlyphAdvance;

    private static final char KANA_MIN = '\u3040';
    private static final char KANA_LIM = '\u3100';
    private static final char CJK_SAMPLE1 = '\u4e9e';
    private static final char CJK_SAMPLE2 = '\u4e9a';
    private static final char CJK_SAMPLE3 = '\u4e9c';
    private static final char CJK_MIN = '\u3200';
    private static final char CJK_LIM = '\u9fb0';
    private static final char CJKFULL_SAMPLE = '\uff01';
    private static final char CJKFULL1_MIN = '\uff00';
    private static final char CJKFULL1_LIM = '\uff5f';
    private static final char CJKFULL2_MIN = '\uffe0';
    private static final char CJKFULL2_LIM = '\uffe7';
    private static final char CJKHALF_SAMPLE = '\uff61';
    private static final char CJKHALF_MIN = '\uff61';
    private static final char CJKHALF_LIM = '\uff9e';

    // not used
    private static final char KSYL_SAMPLE = '\uac00';
    private static final char KSYL_MIN = '\uac00';
    private static final char KSYL_MAX = '\ud7a0';

    private static final Object cacheLock = new Object();

    private static final int CACHE_SIZE = 30;
    private static SoftReference[] cache = new SoftReference[CACHE_SIZE];
    private static int cacheNum;

    private static boolean enabled = true;

    private static String latin1;
    private static String kana;
    static {
        char[] chars = new char[0x100];
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = (char)i;
        }
        // these aren't rendered by textlayout, but are by drawstring...
        chars[9] = chars[0xa] = chars[0xd] = ' ';

        latin1 = new String(chars);

        int kanaLen = KANA_LIM - KANA_MIN;
        chars = new char[kanaLen + 5];
        for (int i = 0; i < kanaLen; ++i) {
            chars[i] = (char)(KANA_MIN + i);
        }
        chars[kanaLen] = CJK_SAMPLE1;
        chars[kanaLen+1] = CJKFULL_SAMPLE;
        chars[kanaLen+2] = CJKHALF_SAMPLE;
        chars[kanaLen+3] = CJK_SAMPLE2;
        chars[kanaLen+4] = CJK_SAMPLE3;

        kana = new String(chars);

        try {
            String useCache = System.getProperty("sun.awt.font.advancecache");
            if (useCache != null && useCache.equals("off")) {
                enabled = false;
            }
        } catch(SecurityException e) {
        }
    }

    private boolean equals(Font font, FontRenderContext frc) {
        return this.font.equals(font) && this.frc.equals(frc);
    }

    /**
     * Return true if the advance cache supports queries on the indicated text.
     * This implies also that no special layout is required.
     */
    public static boolean supportsText(char[] chars, int start, int limit) {
        if (!enabled) return false;

        for (int i = start; i < limit; ++i) {
            char c = chars[i];
            if (!((c < 0x100) ||
                  (c >= KANA_MIN && c < KANA_LIM) ||
                  (c >= CJK_MIN && c < CJK_LIM) ||
                  (c >= CJKHALF_MIN && c < CJKHALF_LIM) ||
                  (c >= CJKFULL1_MIN && c < CJKFULL1_LIM) ||
                  (c >= CJKFULL2_MIN && c < CJKFULL2_LIM))) {

                return false;
            }
        }
        return true;
    }

    public static boolean supportsText(char[] chars) {
        return supportsText(chars, 0, chars.length);
    }

    static int statCount = 0;
    static int statTest = 0;
    static int statMiss = 0;
    static int statFlush = 0;
    static int statFlushDelta = 0;
    static int statLastFlushCount = 0;

    public static AdvanceCache get(Font font, FontRenderContext frc) {
        if (!enabled) return null;

        synchronized (cacheLock) {
            ++statCount;

            int i = 0;
            while (i < cacheNum) {
                AdvanceCache ac = (AdvanceCache)cache[i].get();
                if (ac == null) {
                    // gc flushed our cache, assume all empty
                    ++statFlush;
                    statFlushDelta = statCount - statLastFlushCount;
                    statLastFlushCount = statCount;
                    cache = new SoftReference[CACHE_SIZE]; // reset to initial size
                    cacheNum = 0;
                    break;
                } else {
                    ++statTest;
                    if (ac.equals(font, frc)) {
                        if (i > 0) {
                            SoftReference ref = cache[i];
                            while (i > 0) {
                                cache[i] = cache[--i];
                            }
                            cache[0] = ref;
                        }
                        return ac;
                    }
                    ++i;
                }
            }

            ++statMiss;

            if (i == cache.length) { // cache is full, so grow cache (maybe just use a large cache?)
                SoftReference[] ncache = new SoftReference[cache.length + CACHE_SIZE]; // linear growth, gc should control it
                System.arraycopy(cache, 0, ncache, 1, cache.length);
                cache = ncache;
            } else {
                while (--i >= 0) {
                    cache[i+1] = cache[i];
                }
            }

            AdvanceCache ac = new AdvanceCache(font, frc);
            cache[0] = new SoftReference(ac);
            ++cacheNum;

            return ac;
        }
    }

    private void initLatinAdvances() {
        StandardGlyphVector sgv = new StandardGlyphVector(font, latin1, frc);
        latin1GlyphInfo = sgv.getGlyphInfo();

        latinAdvances = new float[256];
        for (int i = 0, n = 0; i < latinAdvances.length; ++i, n += 8) {
            latinAdvances[i] = latin1GlyphInfo[n+2];
            latin1GlyphInfo[n+4] -= latin1GlyphInfo[n]; // normalize visual bounds to position
            latin1GlyphInfo[n+6] += latin1GlyphInfo[n+4]; //  width -> right
            latin1GlyphInfo[n+7] += latin1GlyphInfo[n+5]; // height -> bottom
        }
        // these aren't rendered by textlayout
        // earlier mapping to spaces ensures glyph info width/height is empty
        // we don't use glyphInfo advances so this is ok
        latinAdvances[9] = latinAdvances[0xa] = latinAdvances[0xd] = 0;
    }

    private void initKanaAdvances() {
        StandardGlyphVector sgv = new StandardGlyphVector(font, kana, frc);
        kanaGlyphInfo = sgv.getGlyphInfo();

        int missingGlyph = font.getMissingGlyphCode();

        kanaAdvances = new float[kana.length()];
        for (int i = 0, n = 0; i < kanaAdvances.length; ++i, n += 8) {
            kanaAdvances[i] = kanaGlyphInfo[n+2];
            kanaGlyphInfo[n+4] -= kanaGlyphInfo[n]; // normalize visual bounds to position
            kanaGlyphInfo[n+6] += kanaGlyphInfo[n+4]; // width -> right
            kanaGlyphInfo[n+7] += kanaGlyphInfo[n+5]; // height -> bottom
        }

        // init other cjk here
        int kanaLen = KANA_LIM - KANA_MIN;
        int cjkIndex = kanaLen;
        if (sgv.getGlyphCode(kanaLen) == missingGlyph) {
            if (sgv.getGlyphCode(kanaLen+3) != missingGlyph) {
                cjkIndex = kanaLen+3;
            } else if (sgv.getGlyphCode(kanaLen+4) != missingGlyph) {
                cjkIndex = kanaLen+4;
            }
        }
        cjkAdvance = kanaAdvances[cjkIndex];
        if (cjkIndex != kanaLen) {
            System.arraycopy(kanaGlyphInfo, cjkIndex * 8, kanaGlyphInfo, kanaLen * 8, 8);
        }
        cjkFullAdvance = kanaAdvances[kanaLen + 1];
        cjkHalfAdvance = kanaAdvances[kanaLen + 2];
    }

    private void initMissingGlyphInfo() {
        int[] glyphIDs = { font.getMissingGlyphCode() };
        StandardGlyphVector sgv = new StandardGlyphVector(font, glyphIDs, frc);
        missingGlyphInfo = sgv.getGlyphInfo();
        missingGlyphInfo[4] -= missingGlyphInfo[0]; // normalize visual bounds to position
        missingGlyphInfo[6] += missingGlyphInfo[4]; // width -> right
        missingGlyphInfo[7] += missingGlyphInfo[5]; // height -> bottom
        missingGlyphAdvance = missingGlyphInfo[2];
    }

    private AdvanceCache(Font font, FontRenderContext frc) {
        this.font = font;
        this.frc = frc;

        initLatinAdvances();

        // defer init of kana advances?
        initKanaAdvances();

        initMissingGlyphInfo();

        // sigh, just assume ASCII metrics
        // or perhaps remove metrics API altogether
        this.lineMetrics = font.getLineMetrics(latin1, frc);
    }

    public LineMetrics getLineMetrics() {
        return lineMetrics;
    }

    public float getAdvance(char c) {
        if (c < 0x100) {  // this check doesn't slow down ASCII significantly
            return latinAdvances[c];
        } else if (c >= KANA_MIN && c < KANA_LIM) {
            return kanaAdvances[c - KANA_MIN];
        } else if (c >= CJK_MIN && c < CJK_LIM) {
            return font.canDisplay(c) ? cjkAdvance : missingGlyphAdvance;
        } else if (c >= CJKHALF_MIN && c < CJKHALF_LIM) {
            return font.canDisplay(c) ? cjkHalfAdvance : missingGlyphAdvance;
        } else if (c >= CJKFULL1_MIN && c < CJKFULL1_LIM) {
            return font.canDisplay(c) ? cjkFullAdvance : missingGlyphAdvance;
        } else if (c >= CJKFULL2_MIN && c < CJKFULL2_LIM) {
            return font.canDisplay(c) ? cjkFullAdvance : missingGlyphAdvance;
        } else {
            // throw same error as if we'd just looked up in a table
            throw new IndexOutOfBoundsException("no advance for char " + Integer.toHexString(c));
        }
    }

    public float getAdvance(String str) {
        return getAdvance(str.toCharArray());
    }

    public float getAdvance(char[] chars) {
        return getAdvance(chars, 0, chars.length);
    }

    public float getAdvance(char[] chars, int start, int limit) {
        float adv = 0;
        for (int i = start; i < limit; ++i) {
            //adv += getAdvance(chars[i]); // this adds 5-10% to the time
            char c = chars[i];
            if (c < 0x100) { // this check doesn't slow down ASCII significantly
                adv += latinAdvances[c];
            } else if (c >= KANA_MIN && c < KANA_LIM) {
                adv += kanaAdvances[c - KANA_MIN];
            } else if (c >= CJK_MIN && c < CJK_LIM) {
                adv += font.canDisplay(c) ? cjkAdvance : missingGlyphAdvance;
            } else if (c >= CJKHALF_MIN && c < CJKHALF_LIM) {
                adv += font.canDisplay(c) ? cjkHalfAdvance : missingGlyphAdvance;
            } else if (c >= CJKFULL1_MIN && c < CJKFULL1_LIM) {
                adv += font.canDisplay(c) ? cjkFullAdvance : missingGlyphAdvance;
            } else if (c >= CJKFULL2_MIN && c < CJKFULL2_LIM) {
                adv += font.canDisplay(c) ? cjkFullAdvance : missingGlyphAdvance;
            } else {
                // throw same error as if we'd just looked up in a table
                throw new IndexOutOfBoundsException("no advance for char " + Integer.toHexString(c));
            }
        }
        return adv;
    }

    public Rectangle2D getLogicalBounds(String str) {
        return getLogicalBounds(str.toCharArray());
    }

    public Rectangle2D getLogicalBounds(char[] chars) {
        return getLogicalBounds(chars, 0, chars.length);
    }

    public Rectangle2D getLogicalBounds(char[] chars, int start, int limit) {
        float adv = getAdvance(chars, start, limit);
        return new Rectangle2D.Float(0,
                                     -lineMetrics.getAscent(),
                                     adv,
                                     lineMetrics.getHeight());
    }

    public Rectangle2D getVisualBounds(String str) {
        return getVisualBounds(str.toCharArray());
    }

    public Rectangle2D getVisualBounds(char[] chars) {
        return getVisualBounds(chars, 0, chars.length);
    }

    public Rectangle2D getVisualBounds(char[] chars, int start, int limit) {
        float l = Float.MAX_VALUE;
        float t = l;
        float r = 0;
        float b = r;

        float x = 0;
        float[] data = null;
        int n = 0;

        for (int i = start; i < limit; ++i) {
            char c = chars[i];
            if (c < 0x100) {
                n = c;
                data = latin1GlyphInfo;
            } else if (c >= KANA_MIN && c < KANA_LIM) {
                n = c - KANA_MIN;
                data = kanaGlyphInfo;
            } else if (c >= CJK_MIN && c < CJK_LIM) {
                if (font.canDisplay(c)) {
                    n = KANA_LIM - KANA_MIN;
                    data = kanaGlyphInfo;
                } else {
                    n = 0;
                    data = missingGlyphInfo;
                }
            } else if (c >= CJKHALF_MIN && c < CJKHALF_LIM) {
                if (font.canDisplay(c)) {
                    n = KANA_LIM - KANA_MIN + 1;
                    data = kanaGlyphInfo;
                } else {
                    n = 0;
                    data = missingGlyphInfo;
                }
            } else if (c >= CJKFULL1_MIN && c < CJKFULL1_LIM) {
                if (font.canDisplay(c)) {
                    n = KANA_LIM - KANA_MIN + 2;
                    data = kanaGlyphInfo;
                } else {
                    n = 0;
                    data = missingGlyphInfo;
                }
            } else if (c >= CJKFULL2_MIN && c < CJKFULL2_LIM) {
                if (font.canDisplay(c)) {
                    n = KANA_LIM - KANA_MIN + 2;
                    data = kanaGlyphInfo;
                } else {
                    n = 0;
                    data = missingGlyphInfo;
                }
            } else {
                // throw same error as if we'd just looked up in a table
                throw new IndexOutOfBoundsException("no advance for char " + Integer.toHexString(c));
            }

            n *= 8;
            float cl = x + data[n+4];
            float ct = data[n+5];
            float cr = x + data[n+6];
            float cb = data[n+7];

            if (cr > cl && cb > ct) { // empty paths aren't added
                if (cl < l) l = cl;
                if (ct < t) t = ct;
                if (cr > r) r = cr;
                if (cb > b) b = cb;
            }
            x += data[n+2];
        }

        return new Rectangle2D.Float(l, t, r-l, b-t);
    }

    public void getStats(int[] results) {
        synchronized(cacheLock) {
            results[0] = statCount;
            results[1] = statTest;
            results[2] = statMiss;
            results[3] = statFlush;
            results[4] = statFlushDelta;
        }
    }
}

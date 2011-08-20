/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SunGraphicsEnvironment.java      1.109 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.java2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import sun.awt.FontProperties;
import sun.awt.font.NativeFontWrapper;
import sun.awt.image.BufImgSurfaceData;
import sun.awt.AppContext;

/**
 * This is an implementation of a GraphicsEnvironment object for the
 * default local GraphicsEnvironment.
 *
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 * @version 1.109 01/23/03
 */

public abstract class SunGraphicsEnvironment extends GraphicsEnvironment
    implements FontSupport {

    protected static boolean debugMapping = false;

    private static Font defaultFont;

    private static String [] logicalFontNames = {
        "default",
        "serif",
        "sansserif",
        "monospaced",
        "dialog",
        "dialoginput",
    };

    private FontProperties fprops;
    private TreeMap terminalNames;
    private HashSet physicalNames;
    private boolean loadedAllFonts;
    protected boolean registeredAllPaths = false;
    protected boolean noType1Font = false;
    protected String fontPath;
    protected TreeMap registeredFonts;
    private Hashtable mapFamilyCache;
    protected boolean loadNativeFonts = false;
    private ArrayList badFonts;

    public SunGraphicsEnvironment() {
        loadedAllFonts = false;
        registeredFonts = new TreeMap();

        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
                if (System.getProperty("sun.java2d.debugfonts") != null) {
                    debugMapping = true;
                }

                fontPath = System.getProperty("sun.java2d.fontpath", "");
                if (fontPath != null && !fontPath.equals(""))
                    registeredAllPaths = true;

                String defaultPathName =
                    System.getProperty("java.home","") + File.separator +
                    "lib" + File.separator + "fonts";

                File  badFontFile =
                    new File(defaultPathName + File.separator+ "badfonts.txt");
                if (badFontFile.exists()) {
                    FileInputStream fis = null;
                    try {
                        badFonts = new ArrayList();
                        fis = new FileInputStream(badFontFile);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);
                        while (true) {
                            String name = br.readLine();
                            if (name == null) {
                                break;
                            } else {
                                if (debugMapping) {
                                    System.out.println("read bad font "+ name);
                                }
                                badFonts.add(name);
                            }
                        }
                    } catch (IOException e) {
                        try {
                            if (fis != null) {
                                fis.close();
                            }
                        } catch (IOException ioe) {
                        }
                    }
                }
                /* Install the JRE fonts so that the native platform
                 * can access them.
                 */
                registerFontsWithPlatform(defaultPathName);

                fprops = createFontProperties();

                /* use "appendedfontpath" in font.properties file to add
                   additional fontpath(s) */
                String appendedPathName = null;
                if (fprops != null){
                    appendedPathName = fprops.getProperty("appendedfontpath");
                }
                if (fontPath.length() == 0) {

                    String prop = System.getProperty("sun.java2d.noType1Font");
                    if (prop == null) {
                        noType1Font = NativeFontWrapper.getType1FontVar();
                    }
                    if ("true".equals(prop)) {
                        noType1Font = true;
                    }
                    loadNativeFonts = true;
                    fontPath = getBasePlatformFontPath(noType1Font);
                    fontPath = defaultPathName + File.pathSeparator + fontPath;
                    if (appendedPathName != null){
                        fontPath = fontPath + File.pathSeparator + appendedPathName;
                    } else if (!registeredAllPaths) {
                        fontPath = fontPath + File.pathSeparator +
                            getPlatformFontPath(noType1Font);
                    }
                    if (debugMapping) {
                        System.out.println("Platform Font Path=" + fontPath);
                    }
                }
                /* On windows the registerFontPaths method does nothing.
                 * Solaris specific notes (nowhere better to document) :-
                 * register just the paths, (it doesn't register the fonts).
                 * This call was being made already when the
                 * composite font building code needed to find a file
                 * for a composite font entry. Its just been moved up
                 * front to make things more obvious.
                 * We register all the paths on Solaris, because
                 * the fontPath we have here is the complete one from
                 * parsing /var/sadm/install/contents, not just
                 * what's on the X font path (may be this should be
                 * changed).
                 * But for now what it means is that if we didn't do
                 * this then if the font weren't listed anywhere on the
                 * less complete font path we'd trigger loadFonts which
                 * actually registers the fonts. This may actually be
                 * the right thing tho' since that would also set up
                 * the X font path without which we wouldn't be able to
                 * display some "native" fonts.
                 * So something to revisit is that probably fontPath
                 * here ought to be only the X font path + jre font dir.
                 * loadFonts should have a separate native call to
                 * get the rest of the platform font path.
                 */
                registerFontPaths(fontPath);
                /*
                 * Here we get the fonts from the library and register them
                 * so they are always available and preferred over other fonts.
                 * This needs to be registered before the composite fonts as
                 * otherwise some native font that corresponds may be found
                 * instead for Lucida Sans which we now (since 1.4 beta 2)
                 * implicitly add as the last component of all composite fonts.
                 * Note that the jre fonts dir is now pre-pended to the font
                 * path too.  Pass "true" to registerFonts method as these
                 * JRE fonts always go through the T2K rasteriser.
                 */
                registerFonts(defaultPathName, true);
                initCompositeFonts(fprops);
                /* Add the fonts already registered : typically the JRE fonts
                 * and the platform fonts which comprise the composite fonts,
                 * into the terminal names map. Failing to do so triggers
                 * loadfonts() when they are referenced. Note that platform
                 * "native" fonts which we may not choose to expose may be
                 * added but this is harmless since the map is used only to
                 * decide whether to call loadfonts(). It is independent of
                 * those listed by getAllFonts().
                 * This has no effect if the font name isn't a full
                 * path name - so it works on solaris but not on windows.
                 * So on windows "physicalNames" serves the same purpose
                 * This needs rewriting to have just one way of doing it.
                 */
                Object [] regFonts = registeredFonts.keySet().toArray();
                for (int i=0; i<regFonts.length; i++) {
                    String key = (String)regFonts[i];
                    String name = NativeFontWrapper.getFullNameByFileName(key);
                    if (name != null) {
                        name = name.toLowerCase();
                    }
                    if (name != null && !terminalNames.containsKey(name)) {
                        terminalNames.put(name, new Integer(0));
                    }
                }
                return null;
            }
        });
    }

    protected String getBasePlatformFontPath(boolean noType1Font) {
        return getPlatformFontPath(noType1Font);
    }

    protected String getPlatformFontPath(boolean noType1Font) {
        registeredAllPaths = true;
        return NativeFontWrapper.getFontPath(noType1Font);
    }

    protected GraphicsDevice[] screens;

    protected synchronized void loadFonts() {
        if (loadedAllFonts) {
            return;
        }
        if (debugMapping) {
            System.out.println("loadfonts called");
        }
        final String newPath;
        if (!registeredAllPaths) {
            newPath = getPlatformFontPath(noType1Font);
            registerFontPaths(newPath);
            /* Results of next line will not be used by runtime code, but
             * may be clearer when debugging to know the full path */
            fontPath = fontPath + File.separator + newPath;
        } else {
            newPath = fontPath;
        }
        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
                // this will find all fonts including those already
                // registered. But we have checks in place to prevent
                // double registration.
                boolean foundFonts = registerFonts(newPath, false);
                boolean foundNativeFonts = false;
                if (loadNativeFonts) {
                    foundNativeFonts = registerNativeFonts ();
                }
                // Needed to add the test for existing # of registered
                // fonts being zero, else this would have caused a JRE
                // exits if no NEW fonts were registered.
                if ((!foundFonts) && (!foundNativeFonts) &&
                    registeredFonts.size() == 0) {
                    //- REMIND adg: need to throw an exception
                    System.out.println("\nNo fonts were found in '"
                                            + newPath + "'.\n");
                    System.exit(2);
                }

                loadedAllFonts = true;
                return null;
            }
        });
    }

    /**
     * Returns an array of all of the screen devices.
     */
    public synchronized GraphicsDevice[] getScreenDevices() {
        GraphicsDevice[] ret = screens;
        if (ret == null) {
            int num = getNumScreens();
            ret = new GraphicsDevice[num];
            for (int i = 0; i < num; i++) {
                ret[i] = makeScreenDevice(i);
            }
            screens = ret;
        }
        return ret;
    }

    protected abstract int getNumScreens();
    protected abstract GraphicsDevice makeScreenDevice(int screennum);

    /**
     * Returns the default screen graphics device.
     */
    public GraphicsDevice getDefaultScreenDevice() {
        return getScreenDevices()[0];
    }

    /**
     * Returns a Graphics2D object for rendering into the
     * given BufferedImage.
     * @throws NullPointerException if BufferedImage argument is null
     */
    public Graphics2D createGraphics(BufferedImage img) {
        if (img == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        SurfaceData sd = BufImgSurfaceData.createData(img);
        if (defaultFont == null) {
            defaultFont = new Font("Dialog", Font.PLAIN, 12);
        }
        return new SunGraphics2D(sd, Color.white, Color.black, defaultFont);
    }

    private Font[] allFonts;

    /**
     * Returns all fonts available in this environment.
     */
    public Font[] getAllFonts() {
        if (allFonts != null) {
            return allFonts;
        }
        loadFonts();
        Font [] fonts = null;
        String [] fontNames = null;
        int count = NativeFontWrapper.getNumFonts();
        if (count > 0) {
            TreeMap fontMapNames = new TreeMap();
            for (int i=0; i < count; i++) {
                String name = NativeFontWrapper.getFullNameByIndex(i);
                if (! name.startsWith(PLSF_PREFIX)) {
                    fontMapNames.put(name, null);
                }
            }
            if (fontMapNames.size() > 0) {
                fontNames = new String[fontMapNames.size()];
                Object [] keyNames = fontMapNames.keySet().toArray();
                for (int i=0; i < keyNames.length; i++) {
                    fontNames[i] = (String)keyNames[i];
                }
            }
        }
        if (fontNames != null) {
            fonts = new Font[fontNames.length];
            for (int i=0; i < fontNames.length; i++) {
                fonts[i] = new Font(fontNames[i], Font.PLAIN, 1);
            }
        }
        allFonts = fonts;
        return allFonts;
    }

    public String[] getAvailableFontFamilyNames(Locale theLocale) {
        // Need to allow for a null locale, as specified in doc
        if (theLocale == null) {
            return getAvailableFontFamilyNames();
        }
        loadFonts();
        String [] retval = null;
        int count = NativeFontWrapper.getNumFonts();

        int localeID = NativeFontWrapper.getLCIDFromLocale(theLocale);

        if (count > 0) {
            TreeMap familyNames = new TreeMap();
            for (int i=0; i < count; i++) {
                String name = NativeFontWrapper.getFamilyNameByIndex(i, localeID);
                String cmpName = name.toLowerCase();
                if ( cmpName.endsWith( ".bold" ) ||
                     cmpName.endsWith( ".bolditalic" ) ||
                     cmpName.endsWith ( ".italic" ) ||
                     cmpName.startsWith ( PLSF_PREFIX )) {
                }
                else {
                  familyNames.put(cmpName, name);
                }
            }
            String str;

            // compatibility
            str = "Serif";          familyNames.put(str.toLowerCase(), str);
            str = "SansSerif";      familyNames.put(str.toLowerCase(), str);
            str = "Monospaced";     familyNames.put(str.toLowerCase(), str);
            str = "Dialog";         familyNames.put(str.toLowerCase(), str);
            str = "DialogInput";    familyNames.put(str.toLowerCase(), str);
            str = "Default";        familyNames.put(str.toLowerCase(), str);

            if (familyNames.size() > 0) {
                retval = new String[familyNames.size()];
                Object [] keyNames = familyNames.keySet().toArray();
                for (int i=0; i < keyNames.length; i++) {
                    retval[i] = (String)familyNames.get(keyNames[i]);
                }
            }
        }
        return retval;
    }

    public String[] getAvailableFontFamilyNames() {
        return getAvailableFontFamilyNames(Locale.getDefault());
    }

    // implements FontSupport.mapFontName
    public String mapFontName(String fontName, int style) {

        // try the cache first
        String mappedName = (String) mapFontCache.get(fontName + "." + styleStr(style));
        if (mappedName != null) {
            if (fprops.supportPLSF() || fallbackFont != null) {
                return getInternalFontName(mappedName);
            }
            return mappedName;
        }

        String lowerCaseName = fontName.toLowerCase(Locale.ENGLISH);

        // The check below is just so that the bitmap fonts being set by
        // AWT and Swing thru the desktop properties do not trigger the
        // the load fonts case. The two bitmap fonts are now mapped to
        // appropriate equivalents for serif and sansserif.
        // Also check for a few common misspellings of sansserif.
        if (lowerCaseName.equals("sanserif") || lowerCaseName.equals("san serif") ||
                lowerCaseName.equals("sans serif") || lowerCaseName.equals("ms sans serif")) {
            lowerCaseName = "sansserif";
        } else if (lowerCaseName.equals("ms serif" )) {
            lowerCaseName = "serif";
        }

        // Check whether we have a logical font family name
        if (FontProperties.isLogicalFontFamilyName(lowerCaseName)) {
            mappedName = getLogicalFontFaceName(lowerCaseName, style);
        }

        // Check whether an alias is defined for the name
        if (mappedName == null) {
            String aliasName = fprops.getAliasedFamilyName(lowerCaseName);
            if (aliasName != null) {
                mappedName = getLogicalFontFaceName(aliasName, style);
            }
        }

        // Look for a physical font name, and use fallback font if no physical font exists
        if (mappedName == null) {
            // If the font name is one of the JDK 1.0 logical font names, we may want to
            // use special fallback mappings if no physical font exists. Therefore,
            // we need to be precise in looking for physical fonts, i.e., go to the native
            // level which has more complete information about physical fonts.
            // REMIND: remove this compatibility workaround from the next feature release.
            if (fprops.getFallbackFamilyName(lowerCaseName, null) != null) {
                // Check whether a font is registered to support this font/style
                // combination. At this point, this would normally be a physical font.
                // We may do this check twice, once without, once with loadFonts, since
                // loadFonts can be quite expensive, and the font may already have
                // been registered as the component of a logical font.
                // Also, we don't want to synchronize this entire section, so
                // we need to have the check on loadedAllFonts first.
                if (loadedAllFonts) {
                    if (NativeFontWrapper.isFontRegistered(fontName, style)) {
                        mappedName = fontName;
                    }
                } else {
                    if (NativeFontWrapper.isFontRegistered(fontName, style)) {
                        mappedName = fontName;
                    } else {
                        if (debugMapping) {
                            System.out.println("calling loadFonts to find font " + fontName);
                        }
                        loadFonts();
                        if (NativeFontWrapper.isFontRegistered(fontName, style)) {
                             mappedName = fontName;
                        }
                    }
                }
                // Apply a fallback mapping if there is no physical font.
                if (mappedName == null) {
                    String fallbackName = fprops.getFallbackFamilyName(lowerCaseName, "dialog");
                    mappedName = getLogicalFontFaceName(fallbackName, style);
                }
            } else {
                // For all other font names, we don't need to do a precise lookup here.
                // We're checking whether the font name has already been registered as
                // as the component font of a logical font in order to avoid unnecessary
                // calls to loadFonts. But if we don't find the font here, we just use
                // the given font name, make sure all fonts have been loaded, and let
                // initializeFont (called from the Font constructor) deal with a precise
                // lookup and, if necessary, the fallback to Dialog.
                if (!terminalNames.containsKey(lowerCaseName)
                        && (physicalNames == null || (!physicalNames.contains(lowerCaseName)))) {
                    if (debugMapping) {
                        System.out.println("calling loadFonts to find font " + fontName);
                    }
                    loadFonts();
                }
                mappedName = fontName;
            }
        }
        //assertion: mappedName != null;

        // cache and return the result
        if (debugMapping) {
            System.out.println("mapped font " + fontName
                    + " (" + styleStr(style) + ") " + " to " + mappedName);
        }
        mapFontCache.put(fontName + "." + styleStr(style), mappedName);
        if (fprops.supportPLSF() || fallbackFont != null) {
            if (debugMapping) {
                String newName = getInternalFontName(mappedName);
                System.out.println("==============================================================");
                System.out.println("originalName  =" + mappedName + ", localeName=" + newName);
                System.out.println("currentThread=" + Thread.currentThread());
                return newName;
            }
            else {
                return getInternalFontName(mappedName);
            }
        }
        return mappedName;
    }

    private String getLogicalFontFaceName(String familyName, int style) {
        String fullName = familyName.toLowerCase(Locale.ENGLISH) + "." + styleStr(style);
        if (terminalNames.containsKey(fullName)) {
            return fullName;
        }
        return familyName;
    }

    private static Hashtable mapFontCache = new Hashtable(5, (float) 0.9);

    // can have platform-specific overrides - see X11GraphicsEnvironment
    protected String parseFamilyNameProperty(String name) {
        int separator = name.indexOf(",");
        if (separator == -1) {
            separator = name.length();
        }
        return name.substring(0, separator);
    }

    // can have platform-specific overrides - see X11GraphicsEnvironment
    protected String getFontPropertyFD(String name) {
        return parseFamilyNameProperty(name);
    }

    // can have platform-specific overrides - see X11GraphicsEnvironment
    protected String getFileNameFromPlatformName(String platName) {
        if (fprops == null) {
            return null;
        }
        platName = platName.replace(' ','_');
        return fprops.getProperty("filename" + "." + platName);
    }

    /**
     * Gets a <code>PrintJob2D</code> object suitable for the
     * the current platform.
     * @return    a <code>PrintJob2D</code> object.
     * @see       java.awt.PrintJob2D
     * @since     JDK1.2
     */
    public PrinterJob getPrinterJob() {
        new Exception().printStackTrace();
        return null;
    }

    /* MACPORTING NOTE.needs to do file type on the Macintosh */
    // adg: ttc files are now handled by the ttf code
    public class TTFilter implements FilenameFilter{
        public boolean accept(File dir,String name) {
            return(name.endsWith(".ttf") ||
                   name.endsWith(".TTF") ||
                   name.endsWith(".ttc") ||
                   name.endsWith(".TTC"));
        }
    }

    public class T2KFilter implements FilenameFilter{
        public boolean accept(File dir,String name) {
            return(name.endsWith(".t2k") ||
                   name.endsWith(".T2K"));
        }
    }

    public class T1Filter implements FilenameFilter{
        public boolean accept(File dir,String name) {
            return(name.endsWith(".ps") ||
                   name.endsWith(".PS") ||
                   name.endsWith(".pfb") ||
                   name.endsWith(".PFB") ||
                   name.endsWith(".pfa") ||
                   name.endsWith(".PFA"));
        }
    }

    /* The majority of the register functions in this class are
     * registering platform fonts in the JRE's font maps.
     * The next one is opposite in function as it registers the JRE
     * fonts as platform fonts. If subsequent to calling this
     * your implementation enumerates platform fonts in a way that
     * would return these fonts too you may get duplicates.
     * This function is primarily used to install the JRE fonts
     * so that the native platform can access them.
     * It is intended to be overridden by platform subclasses
     * Currently minimal use is made of this as generally
     * Java 2D doesn't need the platform to be able to
     * use its fonts and platforms which already have matching
     * fonts registered (possibly even from other different JRE
     * versions) generally can't be guaranteed to use the
     * one registered by this JRE version in response to
     * requests from this JRE.
     */
    protected void registerFontsWithPlatform(String pathName) {
        return;
    }

    protected void registerFontPaths(String pathName) {
        return;
    }

    private boolean registerFonts(String pathName, boolean useJavaRasterizer) {
        boolean retval = false;
        StringTokenizer parser = new StringTokenizer(pathName,
                                                     File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                String newPath = parser.nextToken();
                // paths now registered in constructor.
                // registerFontPath(newPath);
                retval |= addPathFonts(newPath, new TTFilter(),
                                       NativeFontWrapper.FONTFORMAT_TRUETYPE,
                                       useJavaRasterizer);
                retval |= addPathFonts(newPath, new T1Filter(),
                                       NativeFontWrapper.FONTFORMAT_TYPE1,
                                       useJavaRasterizer);
                retval |= addPathFonts(newPath, new T2KFilter(),
                                       NativeFontWrapper.FONTFORMAT_T2K,
                                       useJavaRasterizer);
            }
        } catch (NoSuchElementException e) {
            System.err.println(e);
        }
        return retval;
    }

    // ** REMIND : VERIFY WHAT THIS DOES ON WINDOWS
    // It appears this method is used only on windows
    // On solaris it it were called it would be passed a full path name
    // which is clearly not what its expecting.
    // If it gets just a file name on windows that would "work" but be
    // really bad code. If it gets a full path then I don't see how it
    // could possibly work.
    // can have platform specific override
    protected void registerFontFile(String fontFileName, Vector nativeNames) {

        // REMIND: case compare depends on platform
        if (registeredFonts.containsKey(fontFileName)) {
            return;
        }
        int fontFormat;
        if (new TTFilter().accept(null, fontFileName)) {
            fontFormat = NativeFontWrapper.FONTFORMAT_TRUETYPE;
        } else if (new T1Filter().accept(null, fontFileName)) {
            fontFormat = NativeFontWrapper.FONTFORMAT_TYPE1;
        } else if (new T2KFilter().accept(null, fontFileName)) {
            fontFormat = NativeFontWrapper.FONTFORMAT_T2K;
        } else {
            registerNative (fontFileName);
            return;
        }

        StringTokenizer parser = new StringTokenizer(fontPath,
                                                     File.pathSeparator);
        try {
            while (parser.hasMoreTokens()) {
                String newPath = parser.nextToken();

                File theFile = new File(newPath, fontFileName);
                String path = null;
                try {
                    path = theFile.getCanonicalPath();
                } catch (IOException e) {
                    path = theFile.getAbsolutePath();
                }

                if (theFile.canRead()) {
                    Vector fontNames = new Vector(1, 1);
                    Vector platNames = new Vector(1, 1);
                    platNames.addElement(nativeNames);
                    fontNames.addElement(path);
                    registeredFonts.put(fontFileName, path);
                    NativeFontWrapper.registerFonts(fontNames,
                                                    fontNames.size(),
                                                    platNames,
                                                    fontFormat, false);
                    /* We note the physical fonts registered for font
                     * properties so that we can add these to the
                     * set searched before calling loadfonts()
                     */
                    if (physicalNames == null) {
                        physicalNames = new HashSet();
                    }
                    String name =
                        NativeFontWrapper.getFullNameByFileName(path);
                    if (name != null) {
                        name = name.toLowerCase();
                    }
                    if (!physicalNames.contains(name)) {
                        physicalNames.add(name);
                    }
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            System.err.println(e);
        }
    }

    // can have platform specific override
    protected void registerFontPath(String path) {
    }
    protected void registerNative (String fontFileName) {
    }
    protected Vector getNativeNames (String fontFileName) {
        Vector v = new Vector();
        //v.add(fontFileName);
        return v;
    }

    protected boolean registerNativeFonts () {
        return false;
    }

    /*
     * helper function for registerFonts
     */
    private boolean addPathFonts(String path, FilenameFilter filter,
                                 int fontFormat, boolean useJavaRasterizer) {
        boolean retval = false;
        Vector fontNames = new Vector(20, 10);
        Vector nativeNames = new Vector(20,10);
        File f1 = new File(path);
        String[] ls = f1.list(filter);
        if (ls == null) {
            return retval;
        }
        for (int i=0; i < ls.length; i++ ) {
            File theFile = new File(f1, ls[i]);
            String fullName = null;
            try {
                fullName = theFile.getCanonicalPath();
            } catch (IOException e) {
                fullName = theFile.getAbsolutePath();
            }
            // REMIND: case compare depends on platform
            if (registeredFonts.containsKey(fullName)) {
                continue;
            }

            if (badFonts != null && badFonts.contains(fullName)) {
                if (debugMapping) {
                    System.out.println("skip bad font " + fullName);
                }
                continue; // skip this font file.
            }

            registeredFonts.put(fullName, fullName);

            if (debugMapping) {
                System.out.println("Registering font " + fullName);
                Vector v = getNativeNames(fullName);
                if (v.size() == 0) {
                    System.out.println("No native name");
                } else {
                    for (int nn=0; nn< v.size(); nn++) {
                        System.out.println("native name : " +
                                           (String)v.elementAt(nn));
                    }
                }
            }
            fontNames.addElement(fullName);
            nativeNames.addElement (getNativeNames(fullName));
            retval = true;
        }
        // REMIND - native code might not register everything which we
        // pass into it.
        NativeFontWrapper.registerFonts(fontNames, fontNames.size(),
                                        nativeNames,
                                        fontFormat, useJavaRasterizer );
        return retval;  // REMIND: get status of registration from native
    }

    /**
     * Resolve styles on the character at start into an instance of Font
     * that can best render the text between start and limit.
     * REMIND jk. Move it to graphics environment.
     */
    public static Font getBestFontFor(AttributedCharacterIterator text,
                                      int start, int limit) {

        /*
         * choose the first font that can display the first character
         * first iterate through the styles in the range of text we were
         * passed.  If none of them work, iterate through font families
         * using the attributes on the first character.  If this also
         * fails, use the first font.
         */
        char c = text.setIndex(start);
        Map ff = text.getAttributes();
        Font font = Font.getFont(ff);

        while (!font.canDisplay(c) && (text.getRunLimit() < limit)) {
                text.setIndex(text.getRunLimit());
                font = Font.getFont(text.getAttributes());
        }

        if (!font.canDisplay(c)) {
            text.setIndex(start);
            String[] families =
                GraphicsEnvironment.getLocalGraphicsEnvironment(
                                        ).getAvailableFontFamilyNames();
            for (int i = 0; i < families.length; ++i) {
                        Hashtable ht = new Hashtable();
                        ht.putAll(ff);
                ht.put(TextAttribute.FAMILY, families[i]);
                font = Font.getFont((Map)ht);
                if (font.canDisplay(c)) {
                    break;
                }
            }

            if (!font.canDisplay(c)) {
                font = Font.getFont(ff);
            }
        }

        return font;
    }

    /**
     * Creates this environment's FontProperties.
     */
    protected abstract FontProperties createFontProperties();

    private void initCompositeFonts(FontProperties fprops) {
        TreeMap terminalNames = initTerminalNames(fprops);
        Object [] terminalKeys = terminalNames.keySet().toArray();
        for (int i=0; i < terminalKeys.length; i++) {
            String compositeFontName = (String)terminalKeys[i];
            Integer maxEntryInt = (Integer)terminalNames.get(terminalKeys[i]);
            int numEntries = maxEntryInt.intValue();
            // Check to see if the Lucida Sans Regular is already in the
            // font.properties as an entry. If it is do not add it to the
            // list at the bottom as it would never be used.
            boolean containsLucida = false;
            for (int entries=0; entries < numEntries; entries++) {
                String entryName = parseFamilyNameProperty(
                                fprops.getProperty(
                                        compositeFontName + "." + entries));
                if (entryName.compareToIgnoreCase("Lucida Sans Regular")==0) {
                    containsLucida = true;
                    break;
                }
            }
            // Add an entry for Lucida Sans Regular
            if ( containsLucida == false ) {
                numEntries++; // one for the Lucida fallback
            }

            if (fallbackFont != null &&
                this.terminalNames.containsKey(fallbackFont.toLowerCase(Locale.ENGLISH))) {
                numEntries++;  //for the font specified by setFallbackFont();
            }

            String names[] = new String[numEntries];

            int exclusionMaxIndex[] = new int[numEntries];
            int exclusionRanges[] = new int[0];
            int totalEntries = numEntries;

            if ( containsLucida == false ) {
                // Add the Lucida Sans Regular font here for richer glyph
                // availablity in the logical fonts. This enables Dingbats
                // and Symbols glyphs.
                names[numEntries - 1] = "Lucida Sans Regular";
                totalEntries--;
            }
            if (fallbackFont != null &&
                this.terminalNames.containsKey(fallbackFont.toLowerCase(Locale.ENGLISH))) {
                if ( containsLucida == false ) {
                    names[numEntries - 2] = "Lucida Sans Regular";
                }
                names[numEntries - 1] = fallbackFont;
                totalEntries--;
            }

            for (int j=0; j < totalEntries; j++) {
                names[j] = parseFamilyNameProperty(
                                fprops.getProperty(
                                        compositeFontName + "." + j));
                if (debugMapping) {
                    System.out.println ( "The composite name = " + names[j] );
                }
                exclusionRanges =
                    appendExclusions(fprops, compositeFontName, j, exclusionRanges);
                exclusionMaxIndex[j] = exclusionRanges.length;
            }

            if (debugMapping) {
                System.out.println("initCompositeFonts compositeFontName="+
                                   compositeFontName);
            }

            if (initPLSFFallback){
                compositeFontName = prefixPLSF + compositeFontName;
            }

            if (debugMapping) {
                System.out.println("registerCompositeFont:" + compositeFontName);
                for (int j=0; j < numEntries; j++) {
                    System.out.println("    slot=" + names[j]);
                }
            }

            NativeFontWrapper.registerCompositeFont(
                compositeFontName, names,
                exclusionRanges, exclusionMaxIndex);
        }
        terminalNames.put("default",new Integer(0));
        if (!initPLSFFallback) {
            //don't update the table if its not the first time
            this.terminalNames = terminalNames;
        }
    }


    private int[] appendExclusions(FontProperties fprops, String name, int slot, int [] ranges) {
        // We check for exclusions first with family name and style, then
        // family name only.
        String familyName;
        String styleName;
        int period = name.indexOf('.');
        if (period > 0) {
            familyName = name.substring(0, period);
            styleName = name.substring(period + 1);
        } else {
            familyName = name;
            styleName = "plain";
        }

        String exclusions = fprops.getProperty(
                "exclusion." + familyName + "." + styleName + "." + slot);
        if (exclusions == null) {
            exclusions = fprops.getProperty(
                    "exclusion." + familyName + "." + slot);
        }

        // REMIND: invent exclusion ranges for dingbats and symbols
        // since no properties files specify them
        // (or fix all properties files --- better)
        if (exclusions != null) {
            /*
             * range format is xxxx-XXXX,yyyy-YYYY,.....
             */
            int numExclusions = (exclusions.length() + 1) / 10;
            if (numExclusions > 0) {
                int newRanges[] = new int[numExclusions * 2];
                for (int i = 0; i < numExclusions; i++) {
                    String lower = exclusions.substring(i*10    , i*10 + 4);
                    String upper = exclusions.substring(i*10 + 5, i*10 + 9);
                    newRanges[i*2  ] = Integer.parseInt(lower, 16);
                    newRanges[i*2+1] = Integer.parseInt(upper, 16);
                }
                int totalRanges = ranges.length + newRanges.length;
                int tempRanges[]  = new int[totalRanges];
                System.arraycopy(   ranges, 0,
                                    tempRanges, 0,
                                    ranges.length);
                System.arraycopy(   newRanges, 0,
                                    tempRanges, ranges.length,
                                    newRanges.length);
                ranges = tempRanges;
            }
        }
        return ranges;
    }

    private TreeMap initTerminalNames(FontProperties fprops) {
        TreeMap predefinedNames = new TreeMap();
        TreeMap registeredFileNames = new TreeMap();
        TreeMap terminalNames = new TreeMap();

        addPlatformCompatibilityFileNames(registeredFileNames);

        String str;

        // compatibility
        str = "Serif";
        predefinedNames.put(str.toLowerCase(Locale.ENGLISH), str);
        str = "SansSerif";
        predefinedNames.put(str.toLowerCase(Locale.ENGLISH), str);
        str = "Monospaced";
        predefinedNames.put(str.toLowerCase(Locale.ENGLISH), str);
        str = "Dialog";
        predefinedNames.put(str.toLowerCase(Locale.ENGLISH), str);
        str = "DialogInput";
        predefinedNames.put(str.toLowerCase(Locale.ENGLISH), str);

        if (fprops == null)
            throw new Error("no font properties file found.");

        Object [] propKeys = fprops.keySet().toArray();

        for (int i=0; i < propKeys.length; i++) {
            // discard keys which aren't predefined font family names
            String property = (String)propKeys[i];
            int separator = property.indexOf(".");
            if (separator == -1) {
                separator = property.length();
            }
            String propFamily = property.substring(0, separator);
            if (!predefinedNames.containsKey(propFamily)) {
                continue;   // discard, not predefined
            }

            // find out how many entries for this key
            separator = property.lastIndexOf(".");
            if (separator == -1) {
                continue;   // discard, invalid format
            }
            String familyStyle = property.substring(0, separator);
            if (terminalNames.containsKey(familyStyle)) {
                continue;   // discard, already analyzed
            }
            if (!fprops.containsKey(familyStyle + ".0")) {
                continue;   // discard, invalid file format
            }
            int maxEntry = 0;
            while (fprops.containsKey(familyStyle + "." + maxEntry)) {
                maxEntry++;
            }
            if (maxEntry == 0) {
                continue;   // discard, want direct mapping
            }
            terminalNames.put(familyStyle, new Integer(maxEntry));

            if (debugMapping) {
                System.out.println("FamilyStyle: " + familyStyle);
                System.out.println("NumSlots: " + maxEntry);
                System.out.println("Key: " + (String)propKeys[i]);
            }

            for (int j=0; j < maxEntry; j++) {
                String platName = getFontPropertyFD(
                                    fprops.getProperty(
                                                    familyStyle + "." + j));
                if (!initPLSFFallback) {
                    //needed only the first time? ? ?
                    addPlatformNameForFontProperties(platName);
                }
                String fontFileName = getFileNameFromPlatformName(platName);
                if (debugMapping) {
                    System.out.println("FS: [" + familyStyle + "." + j
                                       + "] PN: [" + platName + "] FN: ["
                                       + fontFileName + "]");
                }
                if (fontFileName == null) {
                    // invalid configuration file(s), but only warn if
                    // is  debugging. Usually saves xterminal users from
                    // being told they don't have sun dingbats fonts.
                    if (debugMapping) {
                        System.err.println(
                            "Font specified in font.properties not found [" +
                             platName + "]");
                    }
                    // on headless loadfonts was being triggered
                    // every time because the native only fonts were
                    // not returning null for fontFileName.
                    // So now do only if local & headful.
                    loadFonts();
                        // was "break" here - why?
                } else {
                    // A font file may occur more than once in font
                    // properties. It may appear with the same or different
                    // platform/native names - particularly on X11 where
                    // each encoding is a different platform name.
                    // We could just ask for the native names here and
                    // register those except that the font properties
                    // files have carefully crafted strings ready for
                    // a simple sprintf of the required pt size.
                    // We'd like to register those as our native names
                    // rather than the ones returned from X
                    // This is somewhat fiddly as we need to first gather
                    // all these used in the font properties file and
                    // associate them all with the same font.
                    // That needs to be delegated to the platform subclas
                    // as equating the native names needs to be done there.
                    HashSet s = (HashSet)registeredFileNames.get(fontFileName);
                    if (s == null) {
                        s = new HashSet();
                        s.add(platName);
                        registeredFileNames.put(fontFileName, s);
                    } else {
                        if (!s.contains(platName)) {
                            s.add(platName);
                        }
                    }

                }
            }
        }
        if (!initPLSFFallback) {
            //needed only the first time? ? ?
            registerFontPropertiesFonts(registeredFileNames);
        }
        return terminalNames;
    }

    /**
     * Adds entries to registeredFileNames for fonts that should be
     * preferred when looking for physical fonts. Each entry has a file name
     * as its key and a HashSet with the platform names of fonts in the file
     * as its value.
     * REMIND: remove this method and references to it from the next feature release.
     */
    protected void addPlatformCompatibilityFileNames(Map registeredFileNames) {
    }

    protected void addPlatformNameForFontProperties(String platName) {
        return;
    }

    // this method accepts a TreeMap where either
    //  - the keys are font file names which may be or may not be full
    // path names, and the value is a possibly empty set of native names
    //  - or the key and value are native names.
    protected void registerFontPropertiesFonts(TreeMap fPropFonts) {

        Object [] fonts = fPropFonts.keySet().toArray();

        for (int i=0; i<fonts.length; i++) {
            String fontFileName = (String)fonts[i];
            HashSet s = (HashSet)fPropFonts.get(fontFileName);
            String[] platNames = (String[])s.toArray(new String[0]);
            Vector nativeNames = getNativeNames(fontFileName);
            // merge the platNames & nativeNames.
            for (int j=0;j<platNames.length;j++) {
                if (!nativeNames.contains(platNames[j])) {
                    nativeNames.add(platNames[j]);
                }
            }
            registerFontFile(fontFileName, nativeNames);
        }
    }

    /*
     * return String representation of style
     */
    public static String styleStr(int num){
        switch(num){
          case Font.BOLD:
            return "bold";
          case Font.ITALIC:
            return "italic";
          case Font.ITALIC | Font.BOLD:
            return "bolditalic";
          default:
            return "plain";
        }
    }

    public static boolean isLogicalFont(Font f) {
        String name = f.getFamily();
        return isLogicalFont(name);
    }


    public static boolean isLogicalFont(String name) {
        name = name.toLowerCase(Locale.ENGLISH);
        for (int i=0; i<logicalFontNames.length; i++) {
            if (name.equals(logicalFontNames[i])) {
                return true;
            }
        }
        return false;
    }

    public static String createFont(File fontFile) {
        return
            NativeFontWrapper.createFont(fontFile.getAbsolutePath(),
                                         NativeFontWrapper.FONTFORMAT_TRUETYPE);
    }

    /**
     * Return the current font properties.
     */
    public FontProperties getFontProperties() {
       if (!FPAName.equals(AppContext.getAppContext().get(FPAKey)) ||
           fpropsPLSF == null) {
           return fprops;
       }
       return fpropsPLSF;
    }

    /**
     * Return the bounds of a GraphicsDevice, less its screen insets.
     * See also java.awt.GraphicsEnvironment.getUsableBounds();
     */
    public static Rectangle getUsableBounds(GraphicsDevice gd) {
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle usableBounds = gc.getBounds();

        usableBounds.x += insets.left;
        usableBounds.y += insets.top;
        usableBounds.width -= (insets.left + insets.right);
        usableBounds.height -= (insets.top + insets.bottom);

        return usableBounds;
    }

    /**
     * @param font representing a physical font.
     * @return true if the underlying font is a TrueType or OpenType font
     * that claims to support the Microsoft Windows encoding corresponding to
     * the default file.encoding property of this JRE instance.
     * This narrow value is useful for Swing to decide if the font is useful
     * for the the Windows Look and Feel, or, if a  composite font should be
     * used instead.
     * The information used to make the decision is obtained from
     * the ulCodePageRange fields in the font.
     * A caller can use isLogicalFont(Font) in this class before calling
     * this method and would not need to call this method if that
     * returns true.
     */
    public static boolean fontSupportsDefaultEncoding(Font font) {
        String encoding =
            (String) java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("file.encoding"));

        if (encoding == null || font == null) {
            return false;
        }

        encoding = encoding.toLowerCase(java.util.Locale.ENGLISH);

        return NativeFontWrapper.fontSupportsEncoding(font, encoding);
    }

    /**
     * Method invoked by applet to prefer "LocaleSpecificFonts" logic
     * font mapping for the current AppContext.
     */
    public static void preferLocaleSpecificFonts(){
        hasPLSF = true;
        AppContext.getAppContext().put(FPAKey, FPAName);
    }

    /**
     * Method invoked by applet to specify a fallback font for logic
     * fonts used with in current AppContext
     */
    public static void setFallbackFont(String name) {
        fallbackFont = name;
        AppContext.getAppContext().put(FPAKey, FPAName);
    }

    //key/value stored in AppContext
    private static final   String FPAKey   = "FontPropertiesAttr";
    private static final   String FPAName  = "PLSFFallback";
    private static final   String PLSF_PREFIX = "_plsf_";
    private String         prefixPLSF = null;
    private boolean        initPLSFFallback = false;
    private FontProperties fpropsPLSF = null;

    protected static boolean  hasPLSF = false;
    protected static String   fallbackFont = null;

    public String getInternalFontName(String orgName){
        if (!hasPLSF && fallbackFont == null) {
            return orgName;
        }
        String fpaValue;
        Object fpaName;
        //if nothing has been defined in AppContext, return the original name
        if ((fpaName = AppContext.getAppContext().get(FPAKey)) == null || ! fpaName.equals(FPAName)) {
            return orgName;
        }
        //Return the original name if it's not a logical font
        String name = orgName.toLowerCase();
        boolean isLogicalFont = false;
        for (int i = 0; i < logicalFontNames.length; i++) {
            if (name.startsWith(logicalFontNames[i])){
                isLogicalFont = true;
                break;
            }
        }
        if (!isLogicalFont) {
            return orgName;
        }

        if (prefixPLSF != null) {
            return prefixPLSF + orgName;
        }

        synchronized (this){
            if (prefixPLSF != null) {
                return prefixPLSF + orgName;
            }
            if (hasPLSF) {
                fpropsPLSF = fprops.applyPreferLocaleSpecificFonts(fprops);
            } else {
                fpropsPLSF = fprops;
            }
            initPLSFFallback = true;
            prefixPLSF = PLSF_PREFIX;
            initCompositeFonts(fpropsPLSF);
            return prefixPLSF + orgName;
        }
    }
}

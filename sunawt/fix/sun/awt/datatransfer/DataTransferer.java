/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)DataTransferer.java      1.28 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.datatransfer;

import java.awt.AWTError;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.FlavorTable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.rmi.MarshalledObject;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import sun.awt.AppContext;
import sun.awt.DebugHelper;
import sun.awt.SunToolkit;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;

import javax.imageio.spi.ImageWriterSpi;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import sun.awt.image.ImageRepresentation;

// TODO
//
// Remove import
import sun.io.CharToByteConverter;


/**
 * Provides a set of functions to be shared among the DataFlavor class and
 * platform-specific data transfer implementations.
 *
 * The concept of "flavors" and "natives" is extended to include "formats",
 * which are the numeric values Win32 and X11 use to express particular data
 * types. Like FlavorMap, which provides getNativesForFlavors(DataFlavor[]) and
 * getFlavorsForNatives(String[]) functions, DataTransferer provides a set
 * of getFormatsFor(Transferable|Flavor|Flavors) and
 * getFlavorsFor(Format|Formats) functions.
 *
 * Also provided are functions for translating a Transferable into a byte
 * array, given a source DataFlavor and a target format, and for translating
 * a byte array or InputStream into an Object, given a source format and
 * a target DataFlavor.
 *
 * @author David Mendenhall
 * @author Danila Sinopalnikov
 * @version 1.28, 01/23/03
 *
 * @since 1.3.1
 */
public abstract class DataTransferer {

    /**
     * Cached value of Class.forName("[C");
     */
    public static final Class charArrayClass = char[].class;

    /**
     * Cached value of Class.forName("[B");
     */
    public static final Class byteArrayClass = byte[].class;

    /**
     * The <code>DataFlavor</code> representing plain text with Unicode
     * encoding, where:
     * <pre>
     *     representationClass = java.lang.String
     *     mimeType            = "text/plain; charset=Unicode"
     * </pre>
     */
    public static final DataFlavor plainTextStringFlavor;

    /**
     * The <code>DataFlavor</code> representing a Java text encoding String
     * encoded in UTF-8, where
     * <pre>
     *     representationClass = [B
     *     mimeType            = "application/x-java-text-encoding"
     * </pre>
     */
    public static final DataFlavor javaTextEncodingFlavor;

    private static SortedSet standardEncodings;

    /**
     * Encodings that we know are or are not supported go in this Map. It's
     * much faster than actually encoding some text.
     */
    // TODO
    //
    // Eliminate this field in conjunction with isEncodingSupported(String)
    private static final Map knownEncodings;

    /**
     * Tracks whether a particular text/* MIME type supports the charset
     * parameter. The Map is initialized with all of the standard MIME types
     * listed in the DataFlavor.selectBestTextFlavor method comment. Additional
     * entries may be added during the life of the JRE for text/<other> types.
     */
    private static final Map textMIMESubtypeCharsetSupport;

    /**
     * Cache of the platform default encoding as specified in the
     * "file.encoding" system property.
     */
    private static String defaultEncoding;

    /**
     * A collection of all natives listed in flavormap.properties with
     * a primary MIME type of "text".
     */
    private static final Set textNatives =
        Collections.synchronizedSet(new HashSet());

    /**
     * The native encodings/charsets for the Set of textNatives.
     */
    private static final Map nativeCharsets =
        Collections.synchronizedMap(new HashMap());

    /**
     * The end-of-line markers for the Set of textNatives.
     */
    private static final Map nativeEOLNs =
        Collections.synchronizedMap(new HashMap());

    /**
     * The number of terminating NUL bytes for the Set of textNatives.
     */
    private static final Map nativeTerminators =
        Collections.synchronizedMap(new HashMap());

    /**
     * The key used to store pending data conversion requests for an AppContext.
     */
    private static final String DATA_CONVERTER_KEY = "DATA_CONVERTER_KEY";

    /**
     * The singleton DataTransferer instance. It is created during MToolkit
     * or WToolkit initialization.
     */
    private static DataTransferer transferer;

    private static final String DEPLOYMENT_CACHE_PROPERTIES[] = {
        "deployment.system.cachedir", "deployment.user.cachedir",
        "deployment.javaws.cachedir", "deployment.javapi.cachedir"
    };

    private static final ArrayList deploymentCacheDirectoryList =
        new ArrayList();

    static {
        DataFlavor tPlainTextStringFlavor = null;
        try {
            tPlainTextStringFlavor = new DataFlavor
                ("text/plain;charset=Unicode;class=java.lang.String");
        } catch (ClassNotFoundException cannotHappen) {
            throw new InternalError();
        }
        plainTextStringFlavor = tPlainTextStringFlavor;

        DataFlavor tJavaTextEncodingFlavor = null;
        try {
            tJavaTextEncodingFlavor = new DataFlavor
                ("application/x-java-text-encoding;class=\"[B\"");
        } catch (ClassNotFoundException cannotHappen) {
            throw new InternalError();
        }
        javaTextEncodingFlavor = tJavaTextEncodingFlavor;

        // TODO
        //
        // Eliminate this code in conjunction with isEncodingSupported(String)
        Map tempMap = new HashMap(11);
        tempMap.put("ASCII", Boolean.TRUE); // US-ASCII
        tempMap.put("ISO8859_1", Boolean.TRUE); // ISO-8859-1
        tempMap.put("UTF8", Boolean.TRUE); // UTF-8
        tempMap.put("UnicodeBigUnmarked", Boolean.TRUE); // UTF-16BE
        tempMap.put("UnicodeLittleUnmarked", Boolean.TRUE); // UTF-16LE
        tempMap.put("UTF16", Boolean.TRUE); // UTF-16
        knownEncodings = Collections.synchronizedMap(tempMap);

        tempMap = new HashMap(17);
        tempMap.put("sgml", Boolean.TRUE);
        tempMap.put("xml", Boolean.TRUE);
        tempMap.put("html", Boolean.TRUE);
        tempMap.put("enriched", Boolean.TRUE);
        tempMap.put("richtext", Boolean.TRUE);
        tempMap.put("uri-list", Boolean.TRUE);
        tempMap.put("directory", Boolean.TRUE);
        tempMap.put("css", Boolean.TRUE);
        tempMap.put("calendar", Boolean.TRUE);
        tempMap.put("plain", Boolean.TRUE);
        tempMap.put("rtf", Boolean.FALSE);
        tempMap.put("tab-separated-values", Boolean.FALSE);
        tempMap.put("t140", Boolean.FALSE);
        tempMap.put("rfc822-headers", Boolean.FALSE);
        tempMap.put("parityfec", Boolean.FALSE);
        textMIMESubtypeCharsetSupport = Collections.synchronizedMap(tempMap);
    }

    /**
     * The accessor method for the singleton DataTransferer instance. Note
     * that in a headless environment, there may be no DataTransferer instance;
     * instead, null will be returned.
     */
    public static DataTransferer getInstance() {
        if (transferer == null) {
            synchronized (DataTransferer.class) {
                if (transferer == null) {
                    final String name = SunToolkit.
                        getDataTransfererClassName();
                    if (name != null) {
                        PrivilegedAction action = new PrivilegedAction() {
                          public Object run() {
                              Class cls = null;
                              Method method = null;
                              Object ret = null;

                              try {
                                  cls = Class.forName(name);
                              } catch (ClassNotFoundException e) {
                                  ClassLoader cl = ClassLoader.
                                      getSystemClassLoader();
                                  if (cl != null) {
                                      try {
                                          cls = cl.loadClass(name);
                                      } catch (ClassNotFoundException ee) {
                                          ee.printStackTrace();
                                          throw new AWTError("DataTransferer not found: " + name);
                                      }
                                  }
                              }
                              if (cls != null) {
                                  try {
                                      method = cls.getMethod
                                          ("getInstanceImpl", null);
                                  } catch (NoSuchMethodException e) {
                                      e.printStackTrace();
                                      throw new AWTError("Cannot instantiate DataTransferer: " + name);
                                  }
                              }
                              if (method != null) {
                                  try {
                                      ret = method.invoke(null, null);
                                  } catch (InvocationTargetException e) {
                                      e.printStackTrace();
                                      throw new AWTError("Cannot instantiate DataTransferer: " + name);
                                  } catch (IllegalAccessException e) {
                                      e.printStackTrace();
                                      throw new AWTError("Cannot access DataTransferer: " + name);
                                  }
                              }
                              return ret;
                          }
                        };
                        transferer = (DataTransferer)
                            AccessController.doPrivileged(action);
                    }
                }
            }
        }
        return transferer;
    }

    /**
     * Converts an arbitrary text encoding to its canonical name.
     */
    public static String canonicalName(String encoding) {
        if (encoding == null) {
            return null;
        }

        // TODO
        //
        // Don't use java.nio API until alias list is equivalent to
        // CharacterEncoding.aliasName(String).
        //
        // String canonicalName = Charset.forName(encoding).toString();
        String canonicalName = sun.io.CharacterEncoding.aliasName(encoding);
        if (canonicalName == null) {
            canonicalName = encoding;
        }
        return canonicalName;
    }

    /**
     * If the specified flavor is a text flavor which supports the "charset"
     * parameter, then this method returns that parameter, or the default
     * charset if no such parameter was specified at construction. For non-
     * text DataFlavors, and for non-charset text flavors, this method returns
     * null.
     */
    public static String getTextCharset(DataFlavor flavor) {
        if (!isFlavorCharsetTextType(flavor)) {
            return null;
        }

        String encoding = flavor.getParameter("charset");

        return (encoding != null) ? encoding : getDefaultTextCharset();
    }

    /**
     * Returns the platform's default character encoding. This code is
     * based on code available in sun.io.Converters.getDefaultConverterClass.
     */
    public static String getDefaultTextCharset() {
        if (defaultEncoding != null) {
            return defaultEncoding;
        }

        PrivilegedAction a =
            new sun.security.action.GetPropertyAction("file.encoding");
        String enc = (String)AccessController.doPrivileged(a);
        if (enc != null) {
            defaultEncoding = DataTransferer.canonicalName(enc);
            if (!isEncodingSupported(defaultEncoding)) {
                // ISO8859_1 is a canonical encoding
                defaultEncoding = "ISO8859_1";
            }
            return defaultEncoding;
        } else {
            /* Property not yet set, so do not set default encoding and
               just use ISO8859_1 for now */
            return "ISO8859_1"; // ISO8859_1 is a canonical encoding
        }
    }

    /**
     * Tests only whether the flavor's MIME type supports the charset
     * parameter. Must only be called for flavors with a primary type of
     * "text".
     */
    public static boolean doesSubtypeSupportCharset(DataFlavor flavor) {
        String subType = flavor.getSubType();
        if (subType == null) {
            return false;
        }

        Object support = textMIMESubtypeCharsetSupport.get(subType);

        if (support != null) {
            return (support == Boolean.TRUE);
        }

        boolean ret_val = (flavor.getParameter("charset") != null);
        textMIMESubtypeCharsetSupport.put
            (subType, (ret_val) ? Boolean.TRUE : Boolean.FALSE);
        return ret_val;
    }
    public static boolean doesSubtypeSupportCharset(String subType,
                                                    String charset)
    {
        Object support = textMIMESubtypeCharsetSupport.get(subType);

        if (support != null) {
            return (support == Boolean.TRUE);
        }

        boolean ret_val = (charset != null);
        textMIMESubtypeCharsetSupport.put
            (subType, (ret_val) ? Boolean.TRUE : Boolean.FALSE);
        return ret_val;
    }

    /**
     * Returns whether this flavor is a text type which supports the
     * 'charset' parameter.
     */
    public static boolean isFlavorCharsetTextType(DataFlavor flavor) {
        // Although stringFlavor doesn't actually support the charset
        // parameter (because its primary MIME type is not "text"), it should
        // be treated as though it does. stringFlavor is semantically
        // equivalent to "text/plain" data.
        if (DataFlavor.stringFlavor.equals(flavor)) {
            return true;
        }

        if (!"text".equals(flavor.getPrimaryType()) ||
            !doesSubtypeSupportCharset(flavor))
        {
            return false;
        }

        Class rep_class = flavor.getRepresentationClass();

        if (flavor.isRepresentationClassReader() ||
            String.class.equals(rep_class) ||
            flavor.isRepresentationClassCharBuffer() ||
            DataTransferer.charArrayClass.equals(rep_class))
        {
            return true;
        }

        if (!(flavor.isRepresentationClassInputStream() ||
              flavor.isRepresentationClassByteBuffer() ||
              DataTransferer.byteArrayClass.equals(rep_class))) {
            return false;
        }

        String charset = flavor.getParameter("charset");

        return (charset != null)
            ? DataTransferer.isEncodingSupported(charset)
            : true; // null equals default encoding which is always supported
    }

    /**
     * Returns whether this flavor is a text type which does not support the
     * 'charset' parameter.
     */
    public static boolean isFlavorNoncharsetTextType(DataFlavor flavor) {
        if (!"text".equals(flavor.getPrimaryType()) ||
            doesSubtypeSupportCharset(flavor))
        {
            return false;
        }

        return (flavor.isRepresentationClassInputStream() ||
                flavor.isRepresentationClassByteBuffer() ||
                DataTransferer.byteArrayClass.
                    equals(flavor.getRepresentationClass()));
    }

    private static boolean isFileInWebstartedCache(File file) {
        if (deploymentCacheDirectoryList.isEmpty()) {
            for (int i = 0; i < DEPLOYMENT_CACHE_PROPERTIES.length; i++) {
                String s = DEPLOYMENT_CACHE_PROPERTIES[i];
                String s1 = System.getProperty(s);
                if (s1 == null)
                    continue;
                try {
                    File file3 = (new File(s1)).getCanonicalFile();
                    if (file3 != null)
                        deploymentCacheDirectoryList.add(file3);
                } catch (IOException e) {
                }
            }
        }

        for (Iterator it = deploymentCacheDirectoryList.iterator();
             it.hasNext(); ) {
            File file1 = (File)it.next();
            File file2 = file;
            while (file2 != null) {
                if (file2.equals(file1))
                    return true;
                file2 = file2.getParentFile();
            }
        }

        return false;
    }

    /**
     * Determines whether this JRE can both encode and decode text in the
     * specified encoding.
     */
    // TODO
    //
    // Eliminate this method and replace with calls to
    // Charset.isSupported(String)
    public static boolean isEncodingSupported(String encoding) {
        if (encoding == null) {
            return false;
        }
        String canonicalName = DataTransferer.canonicalName(encoding);

        Boolean bool = (Boolean)knownEncodings.get(canonicalName);

        if (bool == null) {
            // keep looking - try to encode something and see if io complains
            try {
                new String("abc".getBytes(canonicalName), canonicalName);
                bool = Boolean.TRUE;
            } catch (UnsupportedEncodingException encodingException) {
                bool = Boolean.FALSE;
            }

            knownEncodings.put(canonicalName, bool);
        }

        return (bool == Boolean.TRUE);
    }

    /**
     * Returns an Iterator which traverses a SortedSet of Strings which are
     * a total order of the standard character sets supported by the JRE. The
     * ordering follows the same principles as DataFlavor.selectBestTextFlavor.
     * So as to avoid loading all available character converters, optional,
     * non-standard, character sets are not included.
     */
    public static Iterator standardEncodings() {
        if (standardEncodings == null) {
            TreeSet tempSet = new TreeSet(defaultCharsetComparator);
            tempSet.add("US-ASCII");
            tempSet.add("ISO-8859-1");
            tempSet.add("UTF-8");
            tempSet.add("UTF-16BE");
            tempSet.add("UTF-16LE");
            tempSet.add("UTF-16");
            tempSet.add(getDefaultTextCharset());
            standardEncodings = Collections.unmodifiableSortedSet(tempSet);
        }
        return standardEncodings.iterator();
    }

    /**
     * Converts a FlavorMap to a FlavorTable.
     */
    public static FlavorTable adaptFlavorMap(final FlavorMap map) {
        if (map instanceof FlavorTable) {
            return (FlavorTable)map;
        }

        return new FlavorTable() {
                public Map getNativesForFlavors(DataFlavor[] flavors) {
                    return map.getNativesForFlavors(flavors);
                }
                public Map getFlavorsForNatives(String[] natives) {
                    return map.getFlavorsForNatives(natives);
                }
                public List getNativesForFlavor(DataFlavor flav) {
                    Map natives =
                        getNativesForFlavors(new DataFlavor[] { flav } );
                    String nat = (String)natives.get(flav);
                    if (nat != null) {
                        List list = new ArrayList(1);
                        list.add(nat);
                        return list;
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                }
                public List getFlavorsForNative(String nat) {
                    Map flavors =
                        getFlavorsForNatives(new String[] { nat } );
                    DataFlavor flavor = (DataFlavor)flavors.get(nat);
                    if (flavor != null) {
                        List list = new ArrayList(1);
                        list.add(flavor);
                        return list;
                    } else {
                        return Collections.EMPTY_LIST;
                    }
                }
            };
    }

    /**
     * Returns the default Unicode encoding for the platform. The encoding
     * need not be canonical. This method is only used by the archaic function
     * DataFlavor.getTextPlainUnicodeFlavor().
     */
    public abstract String getDefaultUnicodeEncoding();

    /**
     * This method is called for text flavor mappings established while parsing
     * the flavormap.properties file. It stores the "eoln" and "terminators"
     * parameters which are not officially part of the MIME type. They are
     * MIME parameters specific to the flavormap.properties file format.
     */
    public void registerTextFlavorProperties(String nat, String charset,
                                             String eoln, String terminators) {
        Long format = getFormatForNativeAsLong(nat);

        textNatives.add(format);
        nativeCharsets.put(format, (charset != null && charset.length() != 0)
            ? charset : getDefaultTextCharset());
        if (eoln != null && eoln.length() != 0 && !eoln.equals("\n")) {
            nativeEOLNs.put(format, eoln);
        }
        if (terminators != null && terminators.length() != 0) {
            Integer iTerminators = Integer.valueOf(terminators);
            if (iTerminators.intValue() > 0) {
                nativeTerminators.put(format, iTerminators);
            }
        }
    }

    /**
     * Determines whether the native corresponding to the specified long format
     * was listed in the flavormap.properties file.
     */
    private boolean isTextFormat(long format) {
        return textNatives.contains(new Long(format));
    }

    /**
     * Specifies whether text imported from the native system in the specified
     * format is locale-dependent. If so, when decoding such text,
     * 'nativeCharsets' should be ignored, and instead, the Transferable should
     * be queried for its javaTextEncodingFlavor data for the correct encoding.
     */
    public abstract boolean isLocaleDependentTextFormat(long format);

    /**
     * Determines whether the DataFlavor corresponding to the specified long
     * format is DataFlavor.javaFileListFlavor.
     */
    public abstract boolean isFileFormat(long format);

    /**
     * Determines whether the DataFlavor corresponding to the specified long
     * format is DataFlavor.imageFlavor.
     */
    public abstract boolean isImageFormat(long format);

    /**
     * Returns a Map whose keys are all of the possible formats into which the
     * Transferable's transfer data flavors can be translated. The value of
     * each key is the DataFlavor in which the Transferable's data should be
     * requested when converting to the format.
     */
    public Map getFormatsForTransferable(Transferable contents,
                                         FlavorTable map) {
        DataFlavor[] flavors = contents.getTransferDataFlavors();
        if (flavors == null) {
            return new HashMap(0);
        }
        return getFormatsForFlavors(flavors, map);
    }

    /**
     * Returns a Map whose keys are all of the possible formats into which data
     * in the specified DataFlavor can be translated. The value of each key
     * is the DataFlavor in which a Transferable's data should be requested
     * when converting to the format.
     */
    public Map getFormatsForFlavor(DataFlavor flavor, FlavorTable map) {
        return getFormatsForFlavors(new DataFlavor[] { flavor },
                                    map);
    }

    /**
     * Returns a Map whose keys are all of the possible formats into which data
     * in the specified DataFlavors can be translated. The value of each key
     * is the DataFlavor in which the Transferable's data should be requested
     * when converting to the format.
     *
     * @param flavors the data flavors
     * @param map the FlavorTable which contains mappings between
     *            DataFlavors and data formats
     * @throws NullPointerException if flavors or map is <code>null</code>
     */
    public Map getFormatsForFlavors(DataFlavor[] flavors, FlavorTable map) {
        Map formatMap = new HashMap(flavors.length);
        Map textPlainMap = new HashMap(flavors.length);

        // Iterate backwards so that preferred DataFlavors are used over
        // other DataFlavors. (See javadoc for
        // Transferable.getTransferDataFlavors.)
        for (int i = flavors.length - 1; i >= 0; i--) {
            DataFlavor flavor = flavors[i];
            if (flavor == null) continue;

            // Don't explicitly test for String, since it is just a special
            // case of Serializable
            if (flavor.isFlavorTextType() ||
                flavor.isFlavorJavaFileListType() ||
                DataFlavor.imageFlavor.equals(flavor) ||
                /* flavor.isRepresentationClassSerializable() || */
                flavor.isRepresentationClassInputStream()
                /* || flavor.isRepresentationClassRemote() */)
            {
                List natives = map.getNativesForFlavor(flavor);

                for (Iterator iter = natives.iterator(); iter.hasNext(); ) {
                    Long lFormat =
                        getFormatForNativeAsLong((String)iter.next());

                    formatMap.put(lFormat, flavor);

                    // SystemFlavorMap.getNativesForFlavor will return
                    // text/plain natives for all text/*. While this is good
                    // for a single text/* flavor, we would prefer that
                    // text/plain native data come from a text/plain flavor.
                    if (("text".equals(flavor.getPrimaryType()) &&
                         "plain".equals(flavor.getSubType())) ||
                        flavor.equals(DataFlavor.stringFlavor))
                    {
                        textPlainMap.put(lFormat, flavor);
                    }
                }
            }
        }

        formatMap.putAll(textPlainMap);

        return formatMap;
    }

    /**
     * Reduces the Map output for the root function to an array of the
     * Map's keys.
     */
    public long[] getFormatsForTransferableAsArray(Transferable contents,
                                                   FlavorTable map) {
        return keysToLongArray(getFormatsForTransferable(contents, map));
    }
    public long[] getFormatsForFlavorAsArray(DataFlavor flavor,
                                             FlavorTable map) {
        return keysToLongArray(getFormatsForFlavor(flavor, map));
    }
    public long[] getFormatsForFlavorsAsArray(DataFlavor[] flavors,
                                              FlavorTable map) {
        return keysToLongArray(getFormatsForFlavors(flavors, map));
    }

    /**
     * Returns a Map whose keys are all of the possible DataFlavors into which
     * data in the specified format can be translated. The value of each key
     * is the format in which the Clipboard or dropped data should be requested
     * when converting to the DataFlavor.
     */
    public Map getFlavorsForFormat(long format, FlavorTable map) {
        return getFlavorsForFormats(new long[] { format }, map);
    }

    /**
     * Returns a Map whose keys are all of the possible DataFlavors into which
     * data in the specified formats can be translated. The value of each key
     * is the format in which the Clipboard or dropped data should be requested
     * when converting to the DataFlavor.
     */
    public Map getFlavorsForFormats(long[] formats, FlavorTable map) {
        Map flavorMap = new HashMap(formats.length);
        Set mappingSet = new HashSet(formats.length);
        Set flavorSet = new HashSet(formats.length);

        // First step: build flavorSet, mappingSet and initial flavorMap
        // flavorSet  - the set of all the DataFlavors into which
        //              data in the specified formats can be translated;
        // mappingSet - the set of all the mappings from the specified formats
        //              into any DataFlavor;
        // flavorMap  - after this step, this map maps each of the DataFlavors
        //              from flavorSet to any of the specified formats.
        for (int i = 0; i < formats.length; i++) {
            long format = formats[i];
            String nat = getNativeForFormat(format);
            List flavors = map.getFlavorsForNative(nat);

            for (Iterator iter = flavors.iterator(); iter.hasNext(); ) {
                DataFlavor flavor = (DataFlavor)iter.next();

                // Don't explicitly test for String, since it is just a special
                // case of Serializable
                if (flavor.isFlavorTextType() ||
                    flavor.isFlavorJavaFileListType() ||
                    DataFlavor.imageFlavor.equals(flavor) ||
                    /* flavor.isRepresentationClassSerializable() || */
                    flavor.isRepresentationClassInputStream()
                    /* || flavor.isRepresentationClassRemote() */)
                {
                    Long lFormat = new Long(format);
                    Object mapping =
                        DataTransferer.createMapping(lFormat, flavor);
                    flavorMap.put(flavor, lFormat);
                    mappingSet.add(mapping);
                    flavorSet.add(flavor);
                }
            }
        }

        // Second step: for each DataFlavor try to figure out which of the
        // specified formats is the best to translate to this flavor.
        // Then map each flavor to the best format.
        // For the given flavor, FlavorTable indicates which native will
        // best reflect data in the specified flavor to the underlying native
        // platform. We assume that this native is the best to translate
        // to this flavor.
        // Note: FlavorTable allows one-way mappings, so we can occasionally
        // map a flavor to the format for which the corresponding
        // format-to-flavor mapping doesn't exist. For this reason we have built
        // a mappingSet of all format-to-flavor mappings for the specified formats
        // and check if the format-to-flavor mapping exists for the
        // (flavor,format) pair being added.
        for (Iterator flavorIter = flavorSet.iterator();
             flavorIter.hasNext(); ) {
            DataFlavor flavor = (DataFlavor)flavorIter.next();

            List natives = map.getNativesForFlavor(flavor);

            for (Iterator nativeIter = natives.iterator();
                 nativeIter.hasNext(); ) {
                Long lFormat =
                    getFormatForNativeAsLong((String)nativeIter.next());
                Object mapping = DataTransferer.createMapping(lFormat, flavor);

                if (mappingSet.contains(mapping)) {
                    flavorMap.put(flavor, lFormat);
                    break;
                }
            }
        }

        return flavorMap;
    }

    /**
     * Returns a Set of all DataFlavors for which
     * 1) a mapping from at least one of the specified formats exists in the
     * specified map and
     * 2) the data translation for this mapping can be performed by the data
     * transfer subsystem.
     *
     * @param formats the data formats
     * @param map the FlavorTable which contains mappings between
     *            DataFlavors and data formats
     * @throws NullPointerException if formats or map is <code>null</code>
     */
    public Set getFlavorsForFormatsAsSet(long[] formats, FlavorTable map) {
        Set flavorSet = new HashSet(formats.length);

        for (int i = 0; i < formats.length; i++) {
            String nat = getNativeForFormat(formats[i]);
            List flavors = map.getFlavorsForNative(nat);

            for (Iterator iter = flavors.iterator(); iter.hasNext(); ) {
                DataFlavor flavor = (DataFlavor)iter.next();

                // Don't explicitly test for String, since it is just a special
                // case of Serializable
                if (flavor.isFlavorTextType() ||
                    flavor.isFlavorJavaFileListType() ||
                    DataFlavor.imageFlavor.equals(flavor) ||
                    /* flavor.isRepresentationClassSerializable() || */
                    flavor.isRepresentationClassInputStream()
                    /* || flavor.isRepresentationClassRemote() */)
                {
                    flavorSet.add(flavor);
                }
            }
        }

        return flavorSet;
    }

    /**
     * Returns an array of all DataFlavors for which
     * 1) a mapping from the specified format exists in the specified map and
     * 2) the data translation for this mapping can be performed by the data
     * transfer subsystem.
     * The array will be sorted according to a
     * <code>DataFlavorComparator</code> created with the specified
     * map as an argument.
     *
     * @param format the data format
     * @param map the FlavorTable which contains mappings between
     *            DataFlavors and data formats
     * @throws NullPointerException if map is <code>null</code>
     */
    public DataFlavor[] getFlavorsForFormatAsArray(long format,
                                                   FlavorTable map) {
        return getFlavorsForFormatsAsArray(new long[] { format }, map);
    }

    /**
     * Returns an array of all DataFlavors for which
     * 1) a mapping from at least one of the specified formats exists in the
     * specified map and
     * 2) the data translation for this mapping can be performed by the data
     * transfer subsystem.
     * The array will be sorted according to a
     * <code>DataFlavorComparator</code> created with the specified
     * map as an argument.
     *
     * @param formats the data formats
     * @param map the FlavorTable which contains mappings between
     *            DataFlavors and data formats
     * @throws NullPointerException if formats or map is <code>null</code>
     */
    public DataFlavor[] getFlavorsForFormatsAsArray(long[] formats,
                                                    FlavorTable map) {
        // getFlavorsForFormatsAsSet() is less expensive than
        // getFlavorsForFormats().
        return setToSortedDataFlavorArray(getFlavorsForFormatsAsSet(formats, map));
    }

    /**
     * Returns an object that represents a mapping between the specified
     * key and value. <tt>null</tt> values and the <tt>null</tt> keys are
     * permitted. The internal representation of the mapping object is
     * irrelevant. The only requrement is that the two mapping objects are equal
     * if and only if their keys are equal and their values are equal.
     * More formally, the two mapping objects are equal if and only if
     * <tt>(value1 == null ? value2 == null : value1.equals(value2))
     * && (key1 == null ? key2 == null : key1.equals(key2))</tt>.
     */
    private static Object createMapping(Object key, Object value) {
        // NOTE: Should be updated to use AbstractMap.SimpleEntry as
        // soon as it is made public.
        return Arrays.asList(new Object[] { key, value });
    }

    /**
     * Looks-up or registers the String native with the native data transfer
     * system and returns a long format corresponding to that native.
     */
    protected abstract Long getFormatForNativeAsLong(String str);

    /**
     * Looks-up the String native corresponding to the specified long format in
     * the native data transfer system.
     */
    protected abstract String getNativeForFormat(long format);

    /**
     * Primary translation function for translating a Transferable into
     * a byte array, given a source DataFlavor and target format.
     */
    public byte[] translateTransferable(Transferable contents,
                                        DataFlavor flavor,
                                        long format) throws IOException
    {
        // Obtain the transfer data in the source DataFlavor.
        //
        // Note that we special case DataFlavor.plainTextFlavor because
        // StringSelection supports this flavor incorrectly -- instead of
        // returning an InputStream as the DataFlavor representation class
        // states, it returns a Reader. Instead of using this broken
        // functionality, we request the data in stringFlavor (the other
        // DataFlavor which StringSelection supports) and use the String
        // translator.
        Object obj;
        boolean stringSelectionHack;
        try {
            obj = contents.getTransferData(flavor);
            if (obj == null) {
                return null;
            }
            if (flavor.equals(DataFlavor.plainTextFlavor) &&
                !(obj instanceof InputStream))
            {
                obj = contents.getTransferData(DataFlavor.stringFlavor);
                if (obj == null) {
                    return null;
                }
                stringSelectionHack = true;
            } else {
                stringSelectionHack = false;
            }
        } catch (UnsupportedFlavorException e) {
            throw new IOException(e.getMessage());
        }

        // Source data is a String. Search-and-replace EOLN. Encode into the
        // target format. Append terminating NUL bytes.
        if (stringSelectionHack ||
            (String.class.equals(flavor.getRepresentationClass()) &&
             isFlavorCharsetTextType(flavor) && isTextFormat(format))) {

            String str = removeSuspectedData(flavor, contents, (String)obj);

            Long lFormat = new Long(format);
            String charset = (String)nativeCharsets.get(lFormat);
            if (charset == null) {
                // Only happens when we have a custom text type
                charset = getDefaultTextCharset();
            }
            String eoln = (String)nativeEOLNs.get(lFormat);
            Integer terminators = (Integer)nativeTerminators.get(lFormat);

            // Search and replace EOLN. Note that if EOLN is "\n", then we
            // never added an entry to nativeEOLNs anyway, so we'll skip this
            // code altogether.
            if (eoln != null) {
                int length = str.length();
                StringBuffer buffer =
                    new StringBuffer(length * 2); // 2 is a heuristic
                for (int i = 0; i < length; i++) {
                    char c = str.charAt(i);
                    if (c == '\n') {
                        buffer.append(eoln);
                    } else {
                        buffer.append(c);
                    }
                }
                str = buffer.toString();
            }

            // Encode text in target format.
            byte[] bytes = str.getBytes(charset);

            // Append terminating NUL bytes. Note that if terminators is 0,
            // the we never added an entry to nativeTerminators anyway, so
            // we'll skip code altogether.
            if (terminators != null) {
                int numTerminators = terminators.intValue();
                byte[] terminatedBytes =
                    new byte[bytes.length + numTerminators];
                System.arraycopy(bytes, 0, terminatedBytes, 0, bytes.length);
                for (int i = bytes.length; i < terminatedBytes.length; i++) {
                    terminatedBytes[i] = 0x0;
                }
                bytes = terminatedBytes;
            }

            return bytes;

        // Source data is a Reader. Convert to a String and recur. In the
        // future, we may want to rewrite this so that we encode on demand.
        } else if (flavor.isRepresentationClassReader()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as Reader");
            }

            Reader r = (Reader)obj;
            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = r.read()) != -1) {
                buf.append((char)c);
            }
            r.close();

            return translateTransferable(new StringSelection(
                  buf.toString()), DataFlavor.plainTextFlavor, format);

        // Source data is a CharBuffer. Convert to a String and recur.
        } else if (flavor.isRepresentationClassCharBuffer()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as CharBuffer");
            }

            CharBuffer buffer = (CharBuffer)obj;
            int size = buffer.remaining();
            char[] chars = new char[size];
            buffer.get(chars, 0, size);
            return translateTransferable(new StringSelection(
                  new String(chars)), DataFlavor.plainTextFlavor, format);

        // Source data is a char array. Convert to a String and recur.
        } else if (charArrayClass.equals(flavor.getRepresentationClass())) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as char array");
            }

            return translateTransferable(new StringSelection(
                  new String((char[])obj)),
                  DataFlavor.plainTextFlavor, format);

        // Source data is a ByteBuffer. For arbitrary flavors, simply return
        // the array. For text flavors, decode back to a String and recur to
        // reencode according to the requested format.
        } else if (flavor.isRepresentationClassByteBuffer()) {
            ByteBuffer buffer = (ByteBuffer)obj;
            int size = buffer.remaining();
            byte[] bytes = new byte[size];
            buffer.get(bytes, 0, size);

            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferable(new StringSelection(
                      new String(bytes, sourceEncoding)),
                      DataFlavor.plainTextFlavor, format);
            } else {
                return bytes;
            }

        // Source data is a byte array. For arbitrary flavors, simply return
        // the array. For text flavors, decode back to a String and recur to
        // reencode according to the requested format.
        } else if (byteArrayClass.equals(flavor.getRepresentationClass())) {
            byte[] bytes = (byte[])obj;

            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferable(new StringSelection(
                      new String(bytes, sourceEncoding)),
                      DataFlavor.plainTextFlavor, format);
            } else {
                return bytes;
            }
        // Source data is Image
        } else if (DataFlavor.imageFlavor.equals(flavor)) {
            if (!isImageFormat(format)) {
                throw new IOException("Data translation failed: " +
                                      "not an image format");
            }

            Image image = (Image)obj;
            byte[] bytes = imageToPlatformBytes(image, format);

            if (bytes == null) {
                throw new IOException("Data translation failed: " +
                    "cannot convert java image to native format");
            }
            return bytes;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Target data is a file list. Source data must be a
        // java.util.List which contains java.io.File or String instances.
        if (isFileFormat(format)) {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            final List list = (List)obj;
            final ArrayList fileList = new ArrayList();
            final ProtectionDomain userProtectionDomain =
                        getUserProtactionDomain(contents);
            int nFiles = 0;
            for (int i = 0; i < list.size(); i++) {
                Object o = list.get(i);
                if (o instanceof File || o instanceof String) {
                    nFiles++;
                }
            }

            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        for (int i = 0; i < list.size(); i++) {
                            File file = castToFile(list.get(i));
                            if (System.getSecurityManager() == null ||
                                (!DataTransferer.isFileInWebstartedCache(file) &&
                                !isForbiddenToRead(file, userProtectionDomain))) {
                                fileList.add(file.getCanonicalPath());
                            }
                        }
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new IOException(pae.getMessage());
            }

            for (int i = 0; i < fileList.size(); i++) {
                byte[] bytes = ((String) fileList.get(i)).getBytes();
                bos.write(bytes, 0, bytes.length);
                bos.write(0);
            }
            bos.write(0);

        // Source data is an InputStream. For arbitrary flavors, just grab the
        // bytes and dump them into a byte array. For text flavors, decode back
        // to a String and recur to reencode according to the requested format.
        } else if (flavor.isRepresentationClassInputStream()) {
            InputStream is = (InputStream)obj;
            boolean eof = false;
            int avail = is.available();
            byte[] tmp = new byte[avail > 8192 ? avail : 8192];
            do {
                int ret;
                if (!(eof = (ret = is.read(tmp, 0, tmp.length)) == -1)) {
                    bos.write(tmp, 0, ret);
                }
            } while (!eof);
            is.close();

            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                byte[] bytes = bos.toByteArray();
                bos.close();
                String sourceEncoding = DataTransferer.getTextCharset(flavor);
                return translateTransferable(new StringSelection(
                      new String(bytes, sourceEncoding)),
                      DataFlavor.plainTextFlavor, format);
            }

        // Source data is an RMI object
        } /* else if (flavor.isRepresentationClassRemote()) {
            MarshalledObject mo = new MarshalledObject(obj);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(mo);
            oos.close();

        // Source data is Serializable
        } else if (flavor.isRepresentationClassSerializable()) {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();

        } */ else {
            throw new IOException("data translation failed");
        }

        byte[] ret = bos.toByteArray();
        bos.close();
        return ret;
    }

    private File castToFile(Object obj) throws IOException {
        String name = null;
        if (obj instanceof File) {
            name = ((File)obj).getCanonicalPath();
        } else {
           if (obj instanceof String)
               name = (String)obj;
        }
        return new File(name);
    }

    private boolean isForbiddenToRead(File file, ProtectionDomain domain) {
        if (domain == null)
            return false;
        try {
            if (domain.implies(new FilePermission(file.getCanonicalPath(),
                                                "read, delete")))
                return false;
        } catch (IOException e) {
        }
        return true;
    }

    private static ProtectionDomain getUserProtactionDomain(Transferable contents) {
        return contents.getClass().getProtectionDomain();
    }

    private String removeSuspectedData(DataFlavor dataflavor,
                                Transferable transferable, final String str)
        throws IOException {
        if (System.getSecurityManager() == null ||
            !dataflavor.isMimeTypeEqual("text/uri-list"))
            return str;
        String s = "";
        final ProtectionDomain userProtectionDomain =
                        getUserProtactionDomain(transferable);
        try {
            s = (String)AccessController.doPrivileged(new PrivilegedExceptionAction() {

                public Object run() {
                    StringBuffer sb = new StringBuffer(str.length());
                    /* for (int i = 0; i < str.split("(\\s)+").length; i++) */
                    for (int i = 0; i < str.length(); ) {
                        /* String s1 = str.split("(\\s)+")[i]; */
                        int j = i;
                        for (; str.length() > i && str.charAt(i) > ' '; i++);

                        String s1 = str.substring(j, i);
                        for (; str.length() > i && str.charAt(i) <= ' '; i++);

                        File file = new File(s1);
                        if (file.exists() &&
                            (DataTransferer.isFileInWebstartedCache(file) ||
                            isForbiddenToRead(file, userProtectionDomain)))
                            continue;
                        if (0 != sb.length())
                            sb.append("\\r\\n");
                        sb.append(s1);
                    }
                    return sb.toString();
                }
            });
        } catch (PrivilegedActionException e) {
            throw new IOException(e.getMessage());
        }
        return s;
    }

    public Object translateBytes(byte[] bytes, DataFlavor flavor,
                                 long format, Transferable localeTransferable)
        throws IOException
    {
        return translateBytesOrStream(null, bytes, flavor, format,
                                      localeTransferable);
    }

    public Object translateStream(InputStream str, DataFlavor flavor,
                                  long format, Transferable localeTransferable)
        throws IOException
    {
        return translateBytesOrStream(str, null, flavor, format,
                                      localeTransferable);
    }

    /**
     * Primary translation function for translating either a byte array or
     * an InputStream into an Object, given a source format and a target
     * DataFlavor.
     *
     * One of str/bytes is non-null; the other is null.
     * The conversion from byte[] to InputStream is cheap, so do that
     * immediately if necessary. The opposite conversion is expensive,
     * so avoid it if possible.
     */
    protected Object translateBytesOrStream(InputStream str, byte[] bytes,
                                            DataFlavor flavor, long format,
                                            Transferable localeTransferable)
        throws IOException
    {

        if (str == null) {
            str = new ByteArrayInputStream(bytes);
        }

        // Source data is a file list. Use the dragQueryFile native function to
        // do most of the decoding. Then wrap File objects around the String
        // filenames and return a List.
        if (isFileFormat(format)) {
            if (!DataFlavor.javaFileListFlavor.equals(flavor)) {
                throw new IOException("data translation failed");
            }
            if (bytes == null) {
                bytes = inputStreamToByteArray(str);
            }
            String[] filenames = dragQueryFile(bytes);
            if (filenames == null) {
                str.close();
                return null;
            }

            // Convert the strings to File objects
            File[] files = new File[filenames.length];
            for (int i = 0; i < filenames.length; i++) {
                files[i] = new File(filenames[i]);
            }
            str.close();

            // Turn the list of Files into a List and return
            return Arrays.asList(files);

        // Target data is a String. Strip terminating NUL bytes. Decode bytes
        // into characters. Search-and-replace EOLN.
        } else if (String.class.equals(flavor.getRepresentationClass()) &&
                   isFlavorCharsetTextType(flavor) && isTextFormat(format)) {

            // A String holds all of its data in memory at one time, so
            // we can't avoid reading the entire InputStream at this point.
            if (bytes == null) {
                bytes = inputStreamToByteArray(str);
            }
            str.close();

            Long lFormat = new Long(format);
            String charset = null;
            if (isLocaleDependentTextFormat(format) &&
                localeTransferable != null &&
                localeTransferable.
                    isDataFlavorSupported(javaTextEncodingFlavor))
            {
                try {
                    charset = new String((byte[])localeTransferable.
                                       getTransferData(javaTextEncodingFlavor),
                                       "UTF-8");
                } catch (UnsupportedFlavorException cannotHappen) {
                }
            } else {
                charset = (String)nativeCharsets.get(lFormat);
            }

            if (charset == null) {
                // Only happens when we have a custom text type.
                charset = getDefaultTextCharset();
            }
            String eoln = (String)nativeEOLNs.get(lFormat);
            Integer terminators = (Integer)nativeTerminators.get(lFormat);

            int count;

            // Locate terminating NUL bytes. Note that if terminators is 0,
            // the we never added an entry to nativeTerminators anyway, so
            // we'll skip code altogether.
            if (terminators != null) {
                int numTerminators = terminators.intValue();
                search: for (count = 0;
                             count < (bytes.length - numTerminators + 1);
                             count += numTerminators)
                    {
                        for (int i = count; i < count + numTerminators; i++) {
                            if (bytes[i] != 0x0) {
                                continue search;
                            }
                        }

                        // found terminators
                        break search;
                    }
            } else {
                count = bytes.length;
            }

            // Decode text to chars. Don't include any terminators.
            String converted = new String(bytes, 0, count, charset);

            // Search and replace EOLN. Note that if EOLN is "\n", then we
            // never added an entry to nativeEOLNs anyway, so we'll skip this
            // code altogether.
            if (eoln != null) {

                /* Fix for 4463560: replace EOLNs symbol-by-symbol instead
                 * of using buf.replace()
                 */

                char[] buf = converted.toCharArray();
                char[] eoln_arr = eoln.toCharArray();
                converted = null;
                int j = 0;
                boolean match;

                for (int i = 0; i < buf.length; ) {
                    // Catch last few bytes
                    if (i + eoln_arr.length > buf.length) {
                        buf[j++] = buf[i++];
                        continue;
                    }

                    match = true;
                    for (int k = 0, l = i; k < eoln_arr.length; k++, l++) {
                        if (eoln_arr[k] != buf[l]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        buf[j++] = '\n';
                        i += eoln_arr.length;
                    } else {
                        buf[j++] = buf[i++];
                    }
                }
                converted = new String(buf, 0, j);
            }

            return converted;

        // Special hack to maintain backwards-compatibility with the brokenness
        // of StringSelection. Return a StringReader instead of an InputStream.
        // Recur to obtain String and encapsulate.
        } else if (DataFlavor.plainTextFlavor.equals(flavor)) {
            return new StringReader
                ((String)translateBytesOrStream(str, bytes,
                                                plainTextStringFlavor,
                                                format, localeTransferable));

        // Target data is an InputStream. For arbitrary flavors, just return
        // the raw bytes. For text flavors, decode to strip terminators and
        // search-and-replace EOLN, then reencode according to the requested
        // flavor.
        } else if (flavor.isRepresentationClassInputStream()) {
            return translateBytesOrStreamToInputStream(str, flavor, format,
                                                       localeTransferable);

        // Target data is a Reader. Obtain data in InputStream format, encoded
        // as "Unicode" (utf-16be). Then use an InputStreamReader to decode
        // back to chars on demand.
        } else if (flavor.isRepresentationClassReader()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as Reader");
            }

            InputStream is = (InputStream)
                translateBytesOrStreamToInputStream
                    (str, DataFlavor.plainTextFlavor, format,
                     localeTransferable);
            String unicode =
                DataTransferer.getTextCharset(DataFlavor.plainTextFlavor);
            Reader reader = new InputStreamReader(is, unicode);

            return constructFlavoredObject(reader, flavor, Reader.class);

        // Target data is a CharBuffer. Recur to obtain String and wrap.
        } else if (flavor.isRepresentationClassCharBuffer()) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as CharBuffer");
            }

            CharBuffer buffer = CharBuffer.wrap
                ((String)translateBytesOrStream(str, bytes,
                                                plainTextStringFlavor,
                                                format, localeTransferable));
            return constructFlavoredObject(buffer, flavor, CharBuffer.class);

        // Target data is a char array. Recur to obtain String and convert to
        // char array.
        } else if (charArrayClass.equals(flavor.getRepresentationClass())) {
            if (!(isFlavorCharsetTextType(flavor) && isTextFormat(format))) {
                throw new IOException
                    ("cannot transfer non-text data as char array");
            }

            return ((String)translateBytesOrStream
                    (str, bytes, plainTextStringFlavor, format,
                     localeTransferable)).toCharArray();

        // Target data is a ByteBuffer. For arbitrary flavors, just return
        // the raw bytes. For text flavors, convert to a String to strip
        // terminators and search-and-replace EOLN, then reencode according to
        // the requested flavor.
        } else if (flavor.isRepresentationClassByteBuffer()) {
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                bytes = ((String)translateBytesOrStream
                         (str, bytes, plainTextStringFlavor, format,
                          localeTransferable)).getBytes
                    (DataTransferer.getTextCharset(flavor));
            } else {
                if (bytes == null) {
                    bytes = inputStreamToByteArray(str);
                }
            }

            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return constructFlavoredObject(buffer, flavor, ByteBuffer.class);

        // Target data is a byte array. For arbitrary flavors, just return
        // the raw bytes. For text flavors, convert to a String to strip
        // terminators and search-and-replace EOLN, then reencode according to
        // the requested flavor.
        } else if (byteArrayClass.equals(flavor.getRepresentationClass())) {
            if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
                return ((String)translateBytesOrStream
                        (str, bytes, plainTextStringFlavor, format,
                         localeTransferable)).getBytes
                    (DataTransferer.getTextCharset(flavor));
            } else {
                return (bytes != null) ? bytes : inputStreamToByteArray(str);
            }

        // Target data is an RMI object
        } /* else if (flavor.isRepresentationClassRemote()) {
            try {
                byte[] ba = inputStreamToByteArray(str);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
                Object ret = ((MarshalledObject)(ois.readObject())).get();
                ois.close();
                str.close();
                return ret;
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        // Target data is Serializable
        } else if (flavor.isRepresentationClassSerializable()) {
            try {
                byte[] ba = inputStreamToByteArray(str);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ba));
                Object ret = ois.readObject();
                ois.close();
                str.close();
                return ret;
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        // Target data is Image
        } */ else if (DataFlavor.imageFlavor.equals(flavor)) {
            if (!isImageFormat(format)) {
                throw new IOException("data translation failed");
            }

            Image image = platformImageBytesOrStreamToImage(str, bytes, format);
            str.close();
            return image;
        }

        throw new IOException("data translation failed");
    }

    /**
     * For arbitrary flavors, just use the raw InputStream. For text flavors,
     * ReencodingInputStream will decode and reencode the InputStream on demand
     * so that we can strip terminators and search-and-replace EOLN.
     */
    private Object translateBytesOrStreamToInputStream
        (InputStream str, DataFlavor flavor, long format,
         Transferable localeTransferable) throws IOException
    {
        if (isFlavorCharsetTextType(flavor) && isTextFormat(format)) {
            str = new ReencodingInputStream
                (str, format, DataTransferer.getTextCharset(flavor),
                 localeTransferable);
        }

        return constructFlavoredObject(str, flavor, InputStream.class);
    }

    /**
     * We support representations which are exactly of the specified Class,
     * and also arbitrary Objects which have a constructor which takes an
     * instance of the Class as its sole parameter.
     */
    private Object constructFlavoredObject(Object arg, DataFlavor flavor,
                                           Class clazz)
        throws IOException
    {
        final Class dfrc = flavor.getRepresentationClass();

        if (clazz.equals(dfrc)) {
            return arg; // simple case
        } else {
            Constructor[] constructors = null;

            try {
                constructors = (Constructor[])
                    AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return dfrc.getConstructors();
                            }
                        });
            } catch (SecurityException se) {
                throw new IOException(se.getMessage());
            }

            Constructor constructor = null;

            for (int j = 0; j < constructors.length; j++) {
                if (!Modifier.isPublic(constructors[j].getModifiers())) {
                    continue;
                }

                Class[] ptypes = constructors[j].getParameterTypes();

                if (ptypes != null && ptypes.length == 1 &&
                    clazz.equals(ptypes[0])) {
                    constructor = constructors[j];
                    break;
                }
            }

            if (constructor == null) {
                throw new IOException("can't find <init>(L"+ clazz +
                                      ";)V for class: " + dfrc.getName());
            }

            try {
                return constructor.newInstance(new Object[] { arg } );
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    /**
     * Used for decoding and reencoding an InputStream on demand so that we
     * can strip NUL terminators and perform EOLN search-and-replace.
     */
    public class ReencodingInputStream extends InputStream {
        protected BufferedReader wrapped;
        protected CharToByteConverter converter; // TODO remove
        protected final char[] in = new char[1];
        protected byte[] out;

        protected CharsetEncoder encoder;
        protected CharBuffer inBuf;
        protected ByteBuffer outBuf;

        protected char[] eoln;
        protected int numTerminators;

        protected boolean eos;
        protected int index, limit;

        public ReencodingInputStream(InputStream bytestream, long format,
                                     String targetEncoding,
                                     Transferable localeTransferable)
            throws IOException
        {
            Long lFormat = new Long(format);

            String sourceEncoding = null;
            if (isLocaleDependentTextFormat(format) &&
                localeTransferable != null &&
                localeTransferable.
                    isDataFlavorSupported(javaTextEncodingFlavor))
            {
                try {
                    sourceEncoding = new String((byte[])localeTransferable.
                                       getTransferData(javaTextEncodingFlavor),
                                       "UTF-8");
                } catch (UnsupportedFlavorException cannotHappen) {
                }
            } else {
                sourceEncoding = (String)nativeCharsets.get(lFormat);
            }

            if (sourceEncoding == null) {
                // Only happens when we have a custom text type.
                sourceEncoding = getDefaultTextCharset();
            }
            wrapped = new BufferedReader
                (new InputStreamReader(bytestream, sourceEncoding));

            // TODO
            //
            // Remove converter. Use encoder only.
            try {
                converter = CharToByteConverter.getConverter(targetEncoding);
                out = new byte[converter.getMaxBytesPerChar()];
            } catch (UnsupportedEncodingException e) {
                try {
                    encoder = Charset.forName(targetEncoding).newEncoder();
                    out = new byte[(int)(encoder.maxBytesPerChar() + 0.5)];
                    inBuf = CharBuffer.wrap(in);
                    outBuf = ByteBuffer.wrap(out);
                } catch (IllegalCharsetNameException ee) {
                    throw e;
                } catch (UnsupportedCharsetException ee) {
                    throw e;
                } catch (UnsupportedOperationException ee) {
                    throw e;
                }
            }

            String sEoln = (String)nativeEOLNs.get(lFormat);
            if (sEoln != null) {
                eoln = sEoln.toCharArray();
            }

            // A hope and a prayer that this works generically. This will
            // definitely work on Win32.
            Integer terminators = (Integer)nativeTerminators.get(lFormat);
            if (terminators != null) {
                numTerminators = terminators.intValue();
            }
        }

        public int read() throws IOException {
            if (eos) {
                return -1;
            }

            if (index >= limit) {
                int c = wrapped.read();

                if (c == -1) { // -1 is EOS
                    eos = true;
                    return -1;
                }

                // "c == 0" is not quite correct, but good enough on Windows.
                if (numTerminators > 0 && c == 0) {
                    eos = true;
                    return -1;
                } else if (eoln != null && matchCharArray(eoln, c)) {
                    c = '\n' & 0xFFFF;
                }

                in[0] = (char)c;

                // TODO
                //
                // Remove converter. Use encoder only.
                if (converter != null) {
                    limit = converter.convert(in, 0, 1, out, 0, out.length);
                } else /* if (encoder != null) */ {
                    inBuf.rewind();
                    outBuf.rewind();
                    encoder.encode(inBuf, outBuf, false);
                    limit = outBuf.limit();
                }

                index = 0;

                return read();
            } else {
                return out[index++] & 0xFF;
            }
        }

        public int available() throws IOException {
            return ((eos) ? 0 : (limit - index));
        }

        public void close() throws IOException {
            wrapped.close();
        }

        /**
         * Checks to see if the next array.length characters in wrapped
         * match array. The first character is provided as c. Subsequent
         * characters are read from wrapped itself. When this method returns,
         * the wrapped index may be different from what it was when this
         * method was called.
         */
        private boolean matchCharArray(char[] array, int c)
            throws IOException
        {
            wrapped.mark(array.length);  // BufferedReader supports mark

            int count = 0;
            if ((char)c == array[0]) {
                for (count = 1; count < array.length; count++) {
                    c = wrapped.read();
                    if (c == -1 || ((char)c) != array[count]) {
                        break;
                    }
                }
            }

            if (count == array.length) {
                return true;
            } else {
                wrapped.reset();
                return false;
            }
        }
    }

    /**
     * Decodes a byte array into a set of String filenames.
     */
    private native String[] dragQueryFile(byte[] bytes);

    /**
     * Translates either a byte array or an input stream which contain
     * platform-specific image data in the given format into an Image.
     */
    protected abstract Image platformImageBytesOrStreamToImage(InputStream str,
                                                               byte[] bytes,
                                                               long format)
      throws IOException;

    /**
     * Translates either a byte array or an input stream which contain
     * an image data in the given standard format into an Image.
     *
     * @param mimeType image MIME type, such as: image/png, image/jpeg, image/gif
     */
    protected Image standardImageBytesOrStreamToImage(InputStream inputStream,
                                                      byte[] bytes,
                                                      String mimeType)
      throws IOException {
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        Iterator readerIterator = ImageIO.getImageReadersByMIMEType(mimeType);

        if (!readerIterator.hasNext()) {
            throw new IOException("No registered service provider can decode " +
                                  " an image from " + mimeType);
        }

        IOException ioe = null;

        while (readerIterator.hasNext()) {
            ImageReader imageReader = (ImageReader)readerIterator.next();
            try {
                ImageInputStream imageInputStream =
                    ImageIO.createImageInputStream(inputStream);

                try {
                    ImageReadParam param = imageReader.getDefaultReadParam();
                    imageReader.setInput(imageInputStream, true, true);
                    BufferedImage bufferedImage =
                        imageReader.read(imageReader.getMinIndex(), param);
                    if (bufferedImage != null) {
                        return bufferedImage;
                    }
                } finally {
                    imageInputStream.close();
                    imageReader.dispose();
                }
            } catch (IOException e) {
                ioe = e;
                continue;
            }
        }

        if (ioe == null) {
            ioe = new IOException("Registered service providers failed to decode"
                                  + " an image from " + mimeType);
        }

        throw ioe;
    }

    /**
     * Translates a Java Image into a byte array which contains platform-
     * specific image data in the given format.
     */
    protected abstract byte[] imageToPlatformBytes(Image image, long format)
      throws IOException;

    /**
     * Translates a Java Image into a byte array which contains
     * an image data in the given standard format.
     *
     * @param mimeType image MIME type, such as: image/png, image/jpeg
     */
    protected byte[] imageToStandardBytes(Image image, String mimeType)
      throws IOException {
        RenderedImage renderedImage = null;

        Iterator writerIterator = ImageIO.getImageWritersByMIMEType(mimeType);

        if (!writerIterator.hasNext()) {
            throw new IOException("No registered service provider can encode " +
                                  " an image to " + mimeType);
        }

        if (image instanceof RenderedImage) {
            renderedImage = (RenderedImage)image;
        } else {
            int width = 0;
            int height = 0;
            if (image instanceof sun.awt.image.Image) {
                ImageRepresentation ir = ((sun.awt.image.Image)image).getImageRep();
                ir.reconstruct(ImageObserver.ALLBITS);
                width = ir.getWidth();
                height = ir.getHeight();
            } else {
                width = image.getWidth(null);
                height = image.getHeight(null);
            }

            ColorModel model = ColorModel.getRGBdefault();
            WritableRaster raster =
                model.createCompatibleWritableRaster(width, height);

            BufferedImage bufferedImage =
                new BufferedImage(model, raster, model.isAlphaPremultiplied(),
                                  null);

            Graphics g = bufferedImage.getGraphics();
            try {
                g.drawImage(image, 0, 0, width, height, null);
            } finally {
                g.dispose();
            }

            renderedImage = bufferedImage;
        }

        ImageTypeSpecifier typeSpecifier =
            new ImageTypeSpecifier(renderedImage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOException ioe = null;

        while (writerIterator.hasNext()) {
            ImageWriter imageWriter = (ImageWriter)writerIterator.next();
            ImageWriterSpi writerSpi = imageWriter.getOriginatingProvider();

            if (!writerSpi.canEncodeImage(typeSpecifier)) {
                continue;
            }

            try {
                ImageOutputStream imageOutputStream =
                    ImageIO.createImageOutputStream(baos);
                try {
                    imageWriter.setOutput(imageOutputStream);
                    imageWriter.write(renderedImage);
                    imageOutputStream.flush();
                } finally {
                    imageOutputStream.close();
                }
            } catch (IOException e) {
                imageWriter.dispose();
                baos.reset();
                ioe = e;
                continue;
            }

            imageWriter.dispose();
            baos.close();
            return baos.toByteArray();
        }

        baos.close();

        if (ioe == null) {
            ioe = new IOException("Registered service providers failed to encode "
                                  + renderedImage + " to " + mimeType);
        }

        throw ioe;
    }

    /**
     * Concatenates the data represented by two objects. Objects can be either
     * byte arrays or instances of <code>InputStream</code>. If both arguments
     * are byte arrays byte array will be returned. Otherwise an
     * <code>InputStream</code> will be returned.
     * <p>
     * Currently is only called from native code to prepend palette data to
     * platform-specific image data during image transfer on Win32.
     *
     * @param obj1 the first object to be concatenated.
     * @param obj2 the second object to be concatenated.
     * @return a byte array or an <code>InputStream</code> which represents
     *         a logical concatenation of the two arguments.
     * @throws NullPointerException is either of the arguments is
     *         <code>null</code>
     * @throws ClassCastException is either of the arguments is
     *         neither byte array nor an instance of <code>InputStream</code>.
     */
    private Object concatData(Object obj1, Object obj2) {
        InputStream str1 = null;
        InputStream str2 = null;

        if (obj1 instanceof byte[]) {
            byte[] arr1 = (byte[])obj1;
            if (obj2 instanceof byte[]) {
                byte[] arr2 = (byte[])obj2;
                byte[] ret = new byte[arr1.length + arr2.length];
                System.arraycopy(arr1, 0, ret, 0, arr1.length);
                System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
                return ret;
            } else {
                str1 = new ByteArrayInputStream(arr1);
                str2 = (InputStream)obj2;
            }
        } else {
            str1 = (InputStream)obj1;
            if (obj2 instanceof byte[]) {
                str2 = new ByteArrayInputStream((byte[])obj2);
            } else {
                str2 = (InputStream)obj2;
            }
        }

        return new SequenceInputStream(str1, str2);
    }

    public byte[] convertData(final Object source,
                              final Transferable contents,
                              final long format,
                              final Map formatMap,
                              final boolean isToolkitThread)
        throws IOException
    {
        byte[] ret = null;

        /*
         * If the current thread is the Toolkit thread we should post a
         * Runnable to the event dispatch thread associated with source Object,
         * since translateTransferable() calls Transferable.getTransferData()
         * that may contain client code.
         */
        if (isToolkitThread) try {
            final Stack stack = new Stack();
            final Runnable dataConverter = new Runnable() {
                // Guard against multiple executions.
                private boolean done = false;
                public void run() {
                    if (done) {
                        return;
                    }
                    byte[] data = null;
                    try {
                        DataFlavor flavor = (DataFlavor)formatMap.get(new Long(format));
                        if (flavor != null) {
                            data = translateTransferable(contents, flavor, format);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        data = null;
                    }
                    try {
                        getToolkitThreadBlockedHandler().lock();
                        stack.push(data);
                        getToolkitThreadBlockedHandler().exit();
                    } finally {
                        getToolkitThreadBlockedHandler().unlock();
                        done = true;
                    }
                }
            };

            final AppContext appContext = SunToolkit.targetToAppContext(source);

            getToolkitThreadBlockedHandler().lock();

            if (appContext != null) {
                appContext.put(DATA_CONVERTER_KEY, dataConverter);
            }

            SunToolkit.executeOnEventHandlerThread(source, dataConverter);

            while (stack.empty()) {
                getToolkitThreadBlockedHandler().enter();
            }

            if (appContext != null) {
                appContext.remove(DATA_CONVERTER_KEY);
            }

            ret = (byte[])stack.pop();
        } finally {
            getToolkitThreadBlockedHandler().unlock();
        } else {
            DataFlavor flavor = (DataFlavor)
                formatMap.get(new Long(format));
            if (flavor != null) {
                ret = translateTransferable(contents, flavor, format);
            }
        }

        return ret;
    }

    public void processDataConversionRequests() {
        if (EventQueue.isDispatchThread()) {
            AppContext appContext = AppContext.getAppContext();
            getToolkitThreadBlockedHandler().lock();
            try {
                Runnable dataConverter =
                    (Runnable)appContext.get(DATA_CONVERTER_KEY);
                if (dataConverter != null) {
                    dataConverter.run();
                    appContext.remove(DATA_CONVERTER_KEY);
                }
            } finally {
                getToolkitThreadBlockedHandler().unlock();
            }
        }
    }

    public abstract ToolkitThreadBlockedHandler
        getToolkitThreadBlockedHandler();

    /**
     * Helper function to reduce a Map with Long keys to a long array.
     */
    public static long[] keysToLongArray(Map map) {
        Set keySet = map.keySet();
        long[] retval = new long[keySet.size()];
        int i = 0;
        for (Iterator iter = keySet.iterator(); iter.hasNext(); i++) {
            retval[i] = ((Long)iter.next()).longValue();
        }
        return retval;
    }

    /**
     * Helper function to reduce a Map with DataFlavor keys to a DataFlavor
     * array. The array will be sorted according to
     * <code>DataFlavorComparator</code>.
     */
    public static DataFlavor[] keysToDataFlavorArray(Map map) {
        return setToSortedDataFlavorArray(map.keySet(), map);
    }

    /**
     * Helper function to convert a Set of DataFlavors to a sorted array.
     * The array will be sorted according to <code>DataFlavorComparator</code>.
     */
    public static DataFlavor[] setToSortedDataFlavorArray(Set flavorsSet) {
        DataFlavor[] flavors = new DataFlavor[flavorsSet.size()];
        flavorsSet.toArray(flavors);
        Arrays.sort(flavors, defaultFlavorComparator);
        return flavors;
    }

    /**
     * Helper function to convert a Set of DataFlavors to a sorted array.
     * The array will be sorted according to a
     * <code>DataFlavorComparator</code> created with the specified
     * flavor-to-native map as an argument.
     */
    public static DataFlavor[] setToSortedDataFlavorArray
        (Set flavorsSet, Map flavorToNativeMap)
    {
        DataFlavor[] flavors = new DataFlavor[flavorsSet.size()];
        flavorsSet.toArray(flavors);
        Comparator comparator =
            new DataFlavorComparator(flavorToNativeMap,
                                     IndexedComparator.SELECT_WORST);
        Arrays.sort(flavors, comparator);
        return flavors;
    }

    /**
     * Helper function to convert an InputStream to a byte[] array.
     */
    protected static byte[] inputStreamToByteArray(InputStream str)
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = 0;
        byte[] buf = new byte[8192];

        while ((len = str.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }

        return baos.toByteArray();
    }

    private static final CharsetComparator defaultCharsetComparator =
        new CharsetComparator(IndexedComparator.SELECT_WORST);
    private static final DataFlavorComparator defaultFlavorComparator =
        new DataFlavorComparator(IndexedComparator.SELECT_WORST);

    /**
     * A Comparator which includes a helper function for comparing two Objects
     * which are likely to be keys in the specified Map.
     */
    public abstract static class IndexedComparator implements Comparator {

        /**
         * The best Object (e.g., DataFlavor) will be the last in sequence.
         */
        public static final boolean SELECT_BEST = true;

        /**
         * The best Object (e.g., DataFlavor) will be the first in sequence.
         */
        public static final boolean SELECT_WORST = false;

        protected final boolean order;

        public IndexedComparator() {
            this(SELECT_BEST);
        }

        public IndexedComparator(boolean order) {
            this.order = order;
        }

        /**
         * Helper method to compare two objects by their Integer indices in the
         * given map. If the map doesn't contain an entry for either of the
         * objects, the fallback index will be used for the object instead.
         *
         * @param indexMap the map which maps objects into Integer indexes.
         * @param obj1 the first object to be compared.
         * @param obj2 the second object to be compared.
         * @param fallbackIndex the Integer to be used as a fallback index.
         * @return a negative integer, zero, or a positive integer as the
         *             first object is mapped to a less, equal to, or greater
         *             index than the second.
         */
        protected static int compareIndices(Map indexMap,
                                            Object obj1, Object obj2,
                                            Integer fallbackIndex) {
            Integer index1 = (Integer)indexMap.get(obj1);
            Integer index2 = (Integer)indexMap.get(obj2);

            if (index1 == null) {
                index1 = fallbackIndex;
            }
            if (index2 == null) {
                index2 = fallbackIndex;
            }

            return index1.compareTo(index2);
        }

        /**
         * Helper method to compare two objects by their Long indices in the
         * given map. If the map doesn't contain an entry for either of the
         * objects, the fallback index will be used for the object instead.
         *
         * @param indexMap the map which maps objects into Long indexes.
         * @param obj1 the first object to be compared.
         * @param obj2 the second object to be compared.
         * @param fallbackIndex the Long to be used as a fallback index.
         * @return a negative integer, zero, or a positive integer as the
         *             first object is mapped to a less, equal to, or greater
         *             index than the second.
         */
        protected static int compareLongs(Map indexMap,
                                          Object obj1, Object obj2,
                                          Long fallbackIndex) {
            Long index1 = (Long)indexMap.get(obj1);
            Long index2 = (Long)indexMap.get(obj2);

            if (index1 == null) {
                index1 = fallbackIndex;
            }
            if (index2 == null) {
                index2 = fallbackIndex;
            }

            return index1.compareTo(index2);
        }
    }

    /**
     * An IndexedComparator which compares two String charsets. The comparison
     * follows the rules outlined in DataFlavor.selectBestTextFlavor. In order
     * to ensure that non-Unicode, non-ASCII, non-default charsets are sorted
     * in alphabetical order, charsets are not automatically converted to their
     * canonical forms.
     */
    public static class CharsetComparator extends IndexedComparator {
        private static final Map charsets;
        private static String defaultEncoding;

        private static final Integer DEFAULT_CHARSET_INDEX = new Integer(2);
        private static final Integer OTHER_CHARSET_INDEX = new Integer(1);
        private static final Integer WORST_CHARSET_INDEX = new Integer(0);
        private static final Integer UNSUPPORTED_CHARSET_INDEX =
            new Integer(Integer.MIN_VALUE);

        private static final String UNSUPPORTED_CHARSET = "UNSUPPORTED";

        static {
            HashMap charsetsMap = new HashMap(8, 1.0f);

            // we prefer Unicode charsets
            //
            // TODO
            //
            // Canonical names will change when we switch to java.nio
            charsetsMap.put("Unicode", new Integer(3));
            charsetsMap.put("UnicodeLittleUnmarked", new Integer(4));
            charsetsMap.put("UnicodeBigUnmarked", new Integer(5));
            charsetsMap.put("UTF8", new Integer(6));
            charsetsMap.put("UTF16", new Integer(7));

            // ASCII is the worst charset supported
            charsetsMap.put("ASCII", WORST_CHARSET_INDEX);

            String defEncoding = DataTransferer.canonicalName
                (DataTransferer.getDefaultTextCharset());

            if (charsetsMap.get(defaultEncoding) == null) {
                charsetsMap.put(defaultEncoding, DEFAULT_CHARSET_INDEX);
            }
            charsetsMap.put(UNSUPPORTED_CHARSET, UNSUPPORTED_CHARSET_INDEX);

            charsets = Collections.unmodifiableMap(charsetsMap);
        }

        public CharsetComparator() {
            this(SELECT_BEST);
        }

        public CharsetComparator(boolean order) {
            super(order);
        }

        /**
         * Compares two String objects. Returns a negative integer, zero,
         * or a positive integer as the first charset is worse than, equal to,
         * or better than the second.
         *
         * @param obj1 the first charset to be compared
         * @param obj2 the second charset to be compared
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is worse, equal to, or better than the
         *         second.
         * @throws ClassCastException if either of the arguments is not
         *         instance of String
         * @throws NullPointerException if either of the arguments is
         *         <code>null</code>.
         */
        public int compare(Object obj1, Object obj2) {
            String charset1 = null;
            String charset2 = null;
            if (order == SELECT_BEST) {
                charset1 = (String)obj1;
                charset2 = (String)obj2;
            } else {
                charset1 = (String)obj2;
                charset2 = (String)obj1;
            }

            return compareCharsets(charset1, charset2);
        }

        /**
         * Compares charsets. Returns a negative integer, zero, or a positive
         * integer as the first charset is worse than, equal to, or better than
         * the second.
         * <p>
         * Charsets are ordered according to the following rules:
         * <ul>
         * <li>All unsupported charsets are equal.
         * <li>Any unsupported charset is worse than any supported charset.
         * <li>Unicode charsets, such as "UTF-16", "UTF-8", "UTF-16BE" and
         *     "UTF-16LE", are considered best.
         * <li>After them, platform default charset is selected.
         * <li>"US-ASCII" is the worst of supported charsets.
         * <li>For all other supported charsets, the lexicographically less
         *     one is considered the better.
         * </ul>
         *
         * @param charset1 the first charset to be compared
         * @param charset2 the second charset to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *             first argument is worse, equal to, or better than the
         *             second.
         */
        protected int compareCharsets(String charset1, String charset2) {
            charset1 = getEncoding(charset1);
            charset2 = getEncoding(charset2);

            int comp = compareIndices(charsets, charset1, charset2,
                                      OTHER_CHARSET_INDEX);

            if (comp == 0) {
                return charset2.compareTo(charset1);
            }

            return comp;
        }

        /**
         * Returns encoding for the specified charset according to the
         * following rules:
         * <ul>
         * <li>If the charset is <code>null</code>, then <code>null</code> will
         *     be returned.
         * <li>Iff the charset specifies an encoding unsupported by this JRE,
         *     <code>UNSUPPORTED_CHARSET</code> will be returned.
         * <li>If the charset specifies an alias name, the corresponding
         *     canonical name will be returned iff the charset is a known
         *     Unicode, ASCII, or default charset.
         * </ul>
         *
         * @param charset the charset.
         * @return an encoding for this charset.
         */
        protected static String getEncoding(String charset) {
            if (charset == null) {
                return null;
            } else if (!DataTransferer.isEncodingSupported(charset)) {
                return UNSUPPORTED_CHARSET;
            } else {
                // Only convert to canonical form if the charset is one
                // of the charsets explicitly listed in the known charsets
                // map. This will happen only for Unicode, ASCII, or default
                // charsets.
                String canonicalName = DataTransferer.canonicalName(charset);
                return (charsets.containsKey(canonicalName))
                    ? canonicalName
                    : charset;
            }
        }
    }

    /**
     * An IndexedComparator which compares two DataFlavors. For text flavors,
     * the comparison follows the rules outlined in
     * DataFlavor.selectBestTextFlavor. For non-text flavors, unknown
     * application MIME types are preferred, followed by known
     * application/x-java-* MIME types. Unknown application types are preferred
     * because if the user provides his own data flavor, it will likely be the
     * most descriptive one. For flavors which are otherwise equal, the
     * flavors' native formats are compared, with greater long values
     * taking precedence.
     */
    public static class DataFlavorComparator extends IndexedComparator {
        protected final Map flavorToFormatMap;

        private final CharsetComparator charsetComparator;

        private static final Map exactTypes;
        private static final Map primaryTypes;
        private static final Map nonTextRepresentations;
        private static final Map textTypes;
        private static final Map decodedTextRepresentations;
        private static final Map encodedTextRepresentations;

        private static final Integer UNKNOWN_OBJECT_LOSES =
            new Integer(Integer.MIN_VALUE);
        private static final Integer UNKNOWN_OBJECT_WINS =
            new Integer(Integer.MAX_VALUE);

        private static final Long UNKNOWN_OBJECT_LOSES_L =
            new Long(Long.MIN_VALUE);
        private static final Long UNKNOWN_OBJECT_WINS_L =
            new Long(Long.MAX_VALUE);

        static {
            {
                HashMap exactTypesMap = new HashMap(4, 1.0f);

                // application/x-java-* MIME types
                exactTypesMap.put("application/x-java-file-list",
                                  new Integer(0));
                exactTypesMap.put("application/x-java-serialized-object",
                                  new Integer(1));
                exactTypesMap.put("application/x-java-jvm-local-objectref",
                                  new Integer(2));
                exactTypesMap.put("application/x-java-remote-object",
                                  new Integer(3));

                exactTypes = Collections.unmodifiableMap(exactTypesMap);
            }

            {
                HashMap primaryTypesMap = new HashMap(1, 1.0f);

                primaryTypesMap.put("application", new Integer(0));

                primaryTypes = Collections.unmodifiableMap(primaryTypesMap);
            }

            {
                HashMap nonTextRepresentationsMap = new HashMap(3, 1.0f);

                nonTextRepresentationsMap.put(java.io.InputStream.class,
                                              new Integer(0));
                nonTextRepresentationsMap.put(java.io.Serializable.class,
                                              new Integer(1));
                nonTextRepresentationsMap.put(java.rmi.Remote.class,
                                              new Integer(2));

                nonTextRepresentations =
                    Collections.unmodifiableMap(nonTextRepresentationsMap);
            }

            {
                HashMap textTypesMap = new HashMap(16, 1.0f);

                // plain text
                textTypesMap.put("text/plain", new Integer(0));

                // stringFlavor
                textTypesMap.put("application/x-java-serialized-object",
                                new Integer(1));

                // misc
                textTypesMap.put("text/calendar", new Integer(2));
                textTypesMap.put("text/css", new Integer(3));
                textTypesMap.put("text/directory", new Integer(4));
                textTypesMap.put("text/parityfec", new Integer(5));
                textTypesMap.put("text/rfc822-headers", new Integer(6));
                textTypesMap.put("text/t140", new Integer(7));
                textTypesMap.put("text/tab-separated-values", new Integer(8));
                textTypesMap.put("text/uri-list", new Integer(9));

                // enriched
                textTypesMap.put("text/richtext", new Integer(10));
                textTypesMap.put("text/enriched", new Integer(11));
                textTypesMap.put("text/rtf", new Integer(12));

                // markup
                textTypesMap.put("text/html", new Integer(13));
                textTypesMap.put("text/xml", new Integer(14));
                textTypesMap.put("text/sgml", new Integer(15));

                textTypes = Collections.unmodifiableMap(textTypesMap);
            }

            {
                HashMap decodedTextRepresentationsMap = new HashMap(4, 1.0f);

                decodedTextRepresentationsMap.put
                    (DataTransferer.charArrayClass, new Integer(0));
                decodedTextRepresentationsMap.put
                    (java.nio.CharBuffer.class, new Integer(1));
                decodedTextRepresentationsMap.put
                    (java.lang.String.class, new Integer(2));
                decodedTextRepresentationsMap.put
                    (java.io.Reader.class, new Integer(3));

                decodedTextRepresentations =
                    Collections.unmodifiableMap(decodedTextRepresentationsMap);
            }

            {
                HashMap encodedTextRepresentationsMap = new HashMap(3, 1.0f);

                encodedTextRepresentationsMap.put
                    (DataTransferer.byteArrayClass, new Integer(0));
                encodedTextRepresentationsMap.put
                    (java.nio.ByteBuffer.class, new Integer(1));
                encodedTextRepresentationsMap.put
                    (java.io.InputStream.class, new Integer(2));

                encodedTextRepresentations =
                    Collections.unmodifiableMap(encodedTextRepresentationsMap);
            }
        }

        public DataFlavorComparator() {
            this(SELECT_BEST);
        }

        public DataFlavorComparator(boolean order) {
            super(order);

            charsetComparator = new CharsetComparator(order);
            flavorToFormatMap = Collections.EMPTY_MAP;
        }

        public DataFlavorComparator(Map map) {
            this(map, SELECT_BEST);
        }

        public DataFlavorComparator(Map map, boolean order) {
            super(order);

            charsetComparator = new CharsetComparator(order);
            HashMap hashMap = new HashMap(map.size());
            hashMap.putAll(map);
            flavorToFormatMap = Collections.unmodifiableMap(hashMap);
        }

        public int compare(Object obj1, Object obj2) {
            DataFlavor flavor1 = null;
            DataFlavor flavor2 = null;
            if (order == SELECT_BEST) {
                flavor1 = (DataFlavor)obj1;
                flavor2 = (DataFlavor)obj2;
            } else {
                flavor1 = (DataFlavor)obj2;
                flavor2 = (DataFlavor)obj1;
            }

            if (flavor1.equals(flavor2)) {
                return 0;
            }

            int comp = 0;

            String primaryType1 = flavor1.getPrimaryType();
            String subType1 = flavor1.getSubType();
            String mimeType1 = primaryType1 + "/" + subType1;
            Class class1 = flavor1.getRepresentationClass();

            String primaryType2 = flavor2.getPrimaryType();
            String subType2 = flavor2.getSubType();
            String mimeType2 = primaryType2 + "/" + subType2;
            Class class2 = flavor2.getRepresentationClass();

            if (flavor1.isFlavorTextType() && flavor2.isFlavorTextType()) {
                // First, compare MIME types
                comp = compareIndices(textTypes, mimeType1, mimeType2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }

                // Only need to test one flavor because they both have the
                // same MIME type. Also don't need to worry about accidentally
                // passing stringFlavor because either
                //   1. Both flavors are stringFlavor, in which case the
                //      equality test at the top of the function succeeded.
                //   2. Only one flavor is stringFlavor, in which case the MIME
                //      type comparison returned a non-zero value.
                if (doesSubtypeSupportCharset(flavor1)) {
                    // Next, prefer the decoded text representations of Reader,
                    // String, CharBuffer, and [C, in that order.
                    comp = compareIndices(decodedTextRepresentations, class1,
                                          class2, UNKNOWN_OBJECT_LOSES);
                    if (comp != 0) {
                        return comp;
                    }

                    // Next, compare charsets
                    comp = charsetComparator.compareCharsets
                        (DataTransferer.getTextCharset(flavor1),
                         DataTransferer.getTextCharset(flavor2));
                    if (comp != 0) {
                        return comp;
                    }
                }

                // Finally, prefer the encoded text representations of
                // InputStream, ByteBuffer, and [B, in that order.
                comp = compareIndices(encodedTextRepresentations, class1,
                                      class2, UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
            } else {
                // First, prefer application types.
                comp = compareIndices(primaryTypes, primaryType1, primaryType2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }

                // Next, look for application/x-java-* types. Prefer unknown
                // MIME types because if the user provides his own data flavor,
                // it will likely be the most descriptive one.
                comp = compareIndices(exactTypes, mimeType1, mimeType2,
                                      UNKNOWN_OBJECT_WINS);
                if (comp != 0) {
                    return comp;
                }

                // Finally, prefer the representation classes of Remote,
                // Serializable, and InputStream, in that order.
                comp = compareIndices(nonTextRepresentations, class1, class2,
                                      UNKNOWN_OBJECT_LOSES);
                if (comp != 0) {
                    return comp;
                }
            }

            // As a last resort, take the DataFlavor with the greater integer
            // format.
            return compareLongs(flavorToFormatMap, flavor1, flavor2,
                                UNKNOWN_OBJECT_LOSES_L);
        }
    }
}

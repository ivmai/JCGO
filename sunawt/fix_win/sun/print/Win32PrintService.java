/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)Win32PrintService.java   1.30 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.print;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Vector;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.AttributeSetUtilities;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.Sides;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.SheetCollate;
import javax.print.event.PrintServiceAttributeListener;
import java.util.ArrayList;

import sun.print.SunPrinterJobService;

public class Win32PrintService implements PrintService, AttributeUpdater,
                                          SunPrinterJobService {

    public static MediaSize[] predefMedia;

    static {
        Class c = Win32MediaSize.class;
    }

    private static final DocFlavor[] supportedFlavors = {
        DocFlavor.BYTE_ARRAY.GIF,
        DocFlavor.INPUT_STREAM.GIF,
        DocFlavor.URL.GIF,
        DocFlavor.BYTE_ARRAY.JPEG,
        DocFlavor.INPUT_STREAM.JPEG,
        DocFlavor.URL.JPEG,
        DocFlavor.BYTE_ARRAY.PNG,
        DocFlavor.INPUT_STREAM.PNG,
        DocFlavor.URL.PNG,
        DocFlavor.SERVICE_FORMATTED.PAGEABLE,
        DocFlavor.SERVICE_FORMATTED.PRINTABLE,
        DocFlavor.BYTE_ARRAY.AUTOSENSE,
        DocFlavor.URL.AUTOSENSE,
        DocFlavor.INPUT_STREAM.AUTOSENSE
    };

    /* let's try to support a few of these */
    private static final Class[] serviceAttrCats = {
        PrinterName.class,
        PrinterIsAcceptingJobs.class,
        QueuedJobCount.class,
        ColorSupported.class,
    };

    /*  it turns out to be inconvenient to store the other categories
     *  separately because many attributes are in multiple categories.
     */
    private static Class[] otherAttrCats = {
        JobName.class,
        RequestingUserName.class,
        Copies.class,
        Destination.class,
        OrientationRequested.class,
        PageRanges.class,
        Media.class,
        MediaPrintableArea.class,
        Fidelity.class,
        SunAlternateMedia.class,
        Chromaticity.class
    };


    private static final MediaSizeName[] dmPaperToPrintService = {
      MediaSizeName.NA_LETTER, MediaSizeName.NA_LETTER,
      MediaSizeName.TABLOID, MediaSizeName.LEDGER,
      MediaSizeName.NA_LEGAL, MediaSizeName.INVOICE,
      MediaSizeName.EXECUTIVE, MediaSizeName.ISO_A3,
      MediaSizeName.ISO_A4, MediaSizeName.ISO_A4,
      MediaSizeName.ISO_A5, MediaSizeName.JIS_B4,
      MediaSizeName.JIS_B5, MediaSizeName.FOLIO,
      MediaSizeName.QUARTO, MediaSizeName.NA_10X14_ENVELOPE,
      MediaSizeName.B, MediaSizeName.NA_LETTER,
      MediaSizeName.NA_NUMBER_9_ENVELOPE, MediaSizeName.NA_NUMBER_10_ENVELOPE,
      MediaSizeName.NA_NUMBER_11_ENVELOPE, MediaSizeName.NA_NUMBER_12_ENVELOPE,
      MediaSizeName.NA_NUMBER_14_ENVELOPE, MediaSizeName.C,
      MediaSizeName.D, MediaSizeName.E,
      MediaSizeName.ISO_DESIGNATED_LONG, MediaSizeName.ISO_C5,
      MediaSizeName.ISO_C3, MediaSizeName.ISO_C4,
      MediaSizeName.ISO_C6, MediaSizeName.ITALY_ENVELOPE,
      MediaSizeName.ISO_B4, MediaSizeName.ISO_B5,
      MediaSizeName.ISO_B6, MediaSizeName.ITALY_ENVELOPE,
      MediaSizeName.MONARCH_ENVELOPE, MediaSizeName.PERSONAL_ENVELOPE,
      MediaSizeName.NA_10X15_ENVELOPE, MediaSizeName.NA_9X12_ENVELOPE,
      MediaSizeName.FOLIO, MediaSizeName.ISO_B4,
      MediaSizeName.JAPANESE_POSTCARD, MediaSizeName.NA_9X11_ENVELOPE,
    };

    private static final MediaTray[] dmPaperBinToPrintService = {
      MediaTray.TOP, MediaTray.BOTTOM, MediaTray.MIDDLE,
      MediaTray.MANUAL, MediaTray.ENVELOPE, Win32MediaTray.ENVELOPE_MANUAL,
      Win32MediaTray.AUTO, Win32MediaTray.TRACTOR,
      Win32MediaTray.SMALL_FORMAT, Win32MediaTray.LARGE_FORMAT,
      MediaTray.LARGE_CAPACITY, null, null,
      MediaTray.MAIN, Win32MediaTray.FORMSOURCE,
    };

    // from wingdi.h
    private static int DM_PAPERSIZE = 0x2;
    private static int DM_PRINTQUALITY = 0x400;
    private static int DM_YRESOLUTION = 0x2000;
    private static final int DMRES_MEDIUM = -3;
    private static final int DMRES_HIGH = -4;
    private static final int DMORIENT_LANDSCAPE = 2;
    private static final int DMDUP_VERTICAL = 2;
    private static final int DMDUP_HORIZONTAL = 3;
    private static final int DMCOLLATE_TRUE = 1;

    // media sizes with indices above dmPaperToPrintService' length
    private static final int DMPAPER_A2 = 66;
    private static final int DMPAPER_A6 = 70;
    private static final int DMPAPER_B6_JIS = 88;

    private String printer;
    private PrinterName name;
    private String port;

    transient private PrintServiceAttributeSet lastSet;
    transient private ServiceNotifier notifier = null;

    private DocFlavor[] supportedDocFlavors;
    private MediaSizeName[] mediaSizeNames;
    private MediaPrintableArea[] mediaPrintables;
    private MediaTray[] mediaTrays;
    private PrinterResolution[] printRes;
    private int nCopies;
    private int lenOptAttrCats;
    private int defQuality;
    private int defPaper;
    private int defYRes;
    private int defMedia;
    private int defCopies;
    private int defOrient;
    private int defCollate;
    private int defSides;

    private boolean isResSup;
    private boolean isCollateSup;
    private boolean isColorSup;
    private boolean isSidesSup;
    private boolean isPrQualitySup;
    private boolean gotTrays;
    private boolean gotCopies;
    private boolean mediaInitialized;

    private ArrayList idList;
    private MediaSize[] mediaSizes;

    Win32PrintService(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null printer name");
        }
        printer = name;

        // initialize flags
        mediaInitialized = false;
        gotTrays = false;
        gotCopies = false;

        // count optional attribute categories
        lenOptAttrCats=0;

        // get printer port needed for getting capabilities
        port = getPrinterPort(printer);

        /*
         * NOTE: bit settings for caps and indices for defaults must match
         * that in WPrinterJob.cpp
         */
        int caps = getCapabilities(printer, port);

        isColorSup = ((caps & 0x0001) != 0);

        if (isSidesSup = ((caps & 0x0002) != 0)) {
            lenOptAttrCats++;
        }

        // does the driver support collated copies?
        isCollateSup = ((caps & 0x0004) != 0);
        // but we always advertise we support collation for 2D printing.
        lenOptAttrCats++;

        int[] defaults = getDefaultSettings(printer);
        // indices must match those in WPrinterJob.cpp
        defPaper = defaults[0];
        defMedia = defaults[1];
        defYRes = defaults[2];
        defQuality = defaults[3];
        defCopies = defaults[4];
        defOrient = defaults[5];
        defSides = defaults[6];
        defCollate = defaults[7];

        isPrQualitySup=(((caps & 0x0008) != 0) &&
            // Added check: if supported, we should be able to get the default.
            (defQuality >= DMRES_HIGH) && (defQuality < 0));
        if (isPrQualitySup) {
            lenOptAttrCats++;
        }

        int len = supportedFlavors.length;

        // doc flavors supported
        // if PostScript is supported
        if ((caps & 0x0010) != 0) {
            supportedDocFlavors = new DocFlavor[len+3];
            System.arraycopy(supportedFlavors, 0, supportedDocFlavors, 0, len);
            supportedDocFlavors[len] = DocFlavor.BYTE_ARRAY.POSTSCRIPT;
            supportedDocFlavors[len+1] = DocFlavor.INPUT_STREAM.POSTSCRIPT;
            supportedDocFlavors[len+2] = DocFlavor.URL.POSTSCRIPT;
        } else {
            supportedDocFlavors = new DocFlavor[len];
            System.arraycopy(supportedFlavors, 0, supportedDocFlavors, 0, len);
        }

        // initialize Resolutions
        printRes = getPrintResolutions();

        if ((printRes!=null) && (isResSup=(printRes.length>0))) {
            lenOptAttrCats++;
        }

    }

    public String getName() {
        return printer;
    }

    private PrinterName getPrinterName() {
        if (name == null) {
            name = new PrinterName(printer, null);
        }
        return name;
    }

    private MediaSizeName findWin32Media(int dmIndex) {
        if (dmIndex >= 1 && dmIndex <= dmPaperToPrintService.length) {
           switch(dmIndex) {
            /* matching media sizes with indices beyond
               dmPaperToPrintService's length */
            case DMPAPER_A2:
                return MediaSizeName.ISO_A2;
            case DMPAPER_A6:
                return MediaSizeName.ISO_A6;
            case DMPAPER_B6_JIS:
                return MediaSizeName.JIS_B6;
            default:
                return dmPaperToPrintService[dmIndex - 1];
            }
        }

        return null;
    }

    private boolean addToUniqueList(ArrayList msnList, MediaSizeName mediaName) {
        MediaSizeName msn;
        for (int i=0; i< msnList.size(); i++) {
            msn = (MediaSizeName)msnList.get(i);
            if (msn == mediaName) {
                return false;
            }
        }
        msnList.add(mediaName);
        return true;
    }

    private synchronized void initMedia() {
        if (mediaInitialized == true) {
            return;
        }
        mediaInitialized = true;
        int[] media = getAllMediaIDs(printer, port);
        if (media == null) {
            return;
        }

        ArrayList msnList = new ArrayList();
        ArrayList printableList = new ArrayList();
        MediaSizeName mediaName;
        boolean added;
        float[] prnArea;

        // Get all mediaSizes supported by the printer.
        // We convert media to ArrayList idList and pass this to the
        // function for getting mediaSizes.
        // This is to ensure that mediaSizes and media IDs have 1-1 correspondence.
        // We remove from ID list any invalid mediaSize.  Though this is rare,
        // it happens in HP 4050 German driver.

        idList = new ArrayList();
        for (int i=0; i < media.length; i++) {
          idList.add(new Integer(media[i]));
        }

        mediaSizes = getMediaSizes(idList, media);

        for (int i = 0; i < idList.size(); i++) {

            // match Win ID with our predefined ID using table
            mediaName = findWin32Media(((Integer)idList.get(i)).intValue());

            // No match found, then we get the MediaSizeName out of the MediaSize
            // This requires 1-1 correspondence, lengths must be checked.
            if ((mediaName == null) && (idList.size() == mediaSizes.length)) {
                mediaName = mediaSizes[i].getMediaSizeName();
            }

            // Add mediaName to the msnList
            if (mediaName != null) {
                added = addToUniqueList(msnList, mediaName);

                // get MediaPrintableArea only for supported MediaSizeName ?
                if (added) {
                    prnArea=getMediaPrintableArea(printer,
                                                  ((Integer)idList.get(i)).intValue());
                    if (prnArea == null)
                        continue;
                    try {
                    MediaPrintableArea mpa = new MediaPrintableArea(prnArea[0],
                                                                    prnArea[1],
                                                                    prnArea[2],
                                                                    prnArea[3],
                                                      MediaPrintableArea.INCH);
                    printableList.add(mpa);
                    } catch (IllegalArgumentException iae) {
                    }
                }
            }
        }

        // init mediaSizeNames
        mediaSizeNames = new MediaSizeName[msnList.size()];
        msnList.toArray(mediaSizeNames);

        // init mediaPrintables
        mediaPrintables = new MediaPrintableArea[printableList.size()];
        printableList.toArray(mediaPrintables);
    }

    private synchronized MediaTray[] getMediaTrays() {
        if (gotTrays == true) {
            return mediaTrays;
        }
        gotTrays= true;
        int[] mediaTr = getAllMediaTrays(printer, port);
        String[] winMediaTrayNames = getAllMediaTrayNames(printer, port);

        if ((mediaTr == null) || (winMediaTrayNames == null)){
            return null;
        }

        int count = 0;
        for (int i = 0; i < mediaTr.length; i++)
            if (mediaTr[i] > 0)
                count++;
        MediaTray arr[] = new MediaTray[count];
        count = 0;
        for (int i = 0; i < mediaTr.length; i++) {
            int dmBin = mediaTr[i];
            // check for unsupported DMBINs and create new Win32MediaTray
            if (dmBin <= 0)
             continue;

            if (dmBin > dmPaperBinToPrintService.length ||
                dmPaperBinToPrintService[dmBin - 1] == null)
                arr[count++] = new Win32MediaTray(dmBin, winMediaTrayNames[i]);
            else
                arr[count++] = dmPaperBinToPrintService[dmBin - 1];
        }

        return arr;
    }

    private boolean isSameSize(float w1, float h1, float w2, float h2) {
        float diffX = w1 - w2;
        float diffY = h1 - h2;
        // Get diff of reverse dimensions
        // EPSON Stylus COLOR 860 reverses envelope's width & height
        float diffXrev = w1 - h2;
        float diffYrev = h1 - w2;

        if (((Math.abs(diffX)<=1) && (Math.abs(diffY)<=1)) ||
            ((Math.abs(diffXrev)<=1) && (Math.abs(diffYrev)<=1))){
          return true;
        } else {
          return false;
        }
    }

    private MediaSizeName findMatchingMediaSizeNameMM (float w, float h) {
        if (predefMedia != null) {
            for (int k=0; k<predefMedia.length;k++) {
                if (predefMedia[k] == null) {
                    continue;
                }

                if (isSameSize(predefMedia[k].getX(MediaSize.MM),
                               predefMedia[k].getY(MediaSize.MM),
                               w, h)) {
                  return predefMedia[k].getMediaSizeName();
                }
            }
        }
        return null;
    }


    private MediaSize[] getMediaSizes(ArrayList idList, int[] media) {
        int[] mediaSz = getAllMediaSizes(printer, port);
        String[] winMediaNames = getAllMediaNames(printer, port);
        MediaSizeName msn = null;
        MediaSize ms = null;
        float wid, ht;

        if ((mediaSz == null) || (winMediaNames == null)) {
            return null;
        }

        int nMedia = mediaSz.length/2;
        ArrayList msList = new ArrayList();

        for (int i = 0; i < nMedia; i++, ms=null) {
          wid = mediaSz[i*2]/10;
          ht = mediaSz[i*2+1]/10;

          // Make sure to validate wid & ht.
          // HP LJ 4050 (german) causes IAE in Sonderformat paper, wid & ht
          // returned is not constant.
          if ((wid <= 0) || (ht <= 0)) {
            //Remove corresponding ID from list
            if (nMedia == media.length) {
              Integer remObj = new Integer(media[i]);
              idList.remove(idList.indexOf(remObj));
            }
            continue;
          }
          // Find matching media using dimensions.  This call matches only with our
          // own predefined sizes.
          msn = findMatchingMediaSizeNameMM(wid, ht);
          if (msn != null) {
            ms = MediaSize.getMediaSizeForName(msn);
          }

          if (ms != null) {
            msList.add(ms);
          } else {
            Win32MediaSize wms = new Win32MediaSize(winMediaNames[i]);
            try {
              ms = new MediaSize(wid, ht, MediaSize.MM, wms);
              msList.add(ms);
            } catch(IllegalArgumentException e) {
              if (nMedia == media.length) {
                Integer remObj = new Integer(media[i]);
                idList.remove(idList.indexOf(remObj));
              }
            }
          }

        }

        MediaSize[] arr2 = new MediaSize[msList.size()];
        msList.toArray(arr2);

        return arr2;
    }


    private PrinterIsAcceptingJobs getPrinterIsAcceptingJobs() {
        if (getJobStatus(printer, 2) != 1) {
            return PrinterIsAcceptingJobs.NOT_ACCEPTING_JOBS;
        }
        else {
            return PrinterIsAcceptingJobs.ACCEPTING_JOBS;
        }
    }

    private QueuedJobCount getQueuedJobCount() {

        int count = getJobStatus(printer, 1);
        if (count != -1) {
            return new QueuedJobCount(count);
        }
        else {
            return new QueuedJobCount(0);
        }
    }

    private boolean isSupportedCopies(Copies copies) {
        synchronized (this) {
            if (gotCopies == false) {
                nCopies = getCopiesSupported(printer, port);
                gotCopies = true;
            }
        }
        int numCopies = copies.getValue();
        return (numCopies > 0 && numCopies <= nCopies);
    }

    private boolean isSupportedMedia(MediaSizeName msn) {
        if (mediaInitialized == false) {
            initMedia();
        }
        if (mediaSizeNames != null) {
            for (int i=0; i<mediaSizeNames.length; i++) {
                if (msn.equals(mediaSizeNames[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSupportedMediaPrintableArea(MediaPrintableArea mpa) {
        if (mediaInitialized == false) {
            initMedia();
        }
        if (mediaPrintables != null) {
            for (int i=0; i<mediaPrintables.length; i++) {
                if (mpa.equals(mediaPrintables[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSupportedMediaTray(MediaTray msn) {
        if (gotTrays == false) {
                mediaTrays = getMediaTrays();
        }
        if (mediaTrays != null) {
            for (int i=0; i<mediaTrays.length; i++) {
               if (msn.equals(mediaTrays[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private PrinterResolution[] getPrintResolutions() {

        int[] prnRes = getAllResolutions(printer, port);
        if (prnRes == null) {
            return null;
        }
        int nRes = prnRes.length/2;

        ArrayList arrList = new ArrayList();
        PrinterResolution pr;

        for (int i=0; i<nRes; i++) {
            try {
                pr = new PrinterResolution(prnRes[i*2],
                                        prnRes[i*2+1], PrinterResolution.DPI);
                arrList.add(pr);
            } catch (IllegalArgumentException e) {
            }
        }

        PrinterResolution[] arr =
            (PrinterResolution[]) arrList.toArray(new PrinterResolution[arrList.size()]);

        return arr;
    }

    private boolean isSupportedResolution(PrinterResolution res) {
        if (printRes != null) {
            for (int i=0; i<printRes.length; i++) {
                if (res.equals(printRes[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public DocPrintJob createPrintJob() {
      SecurityManager security = System.getSecurityManager();
      if (security != null) {
        security.checkPrintJobAccess();
      }
        return new Win32PrintJob(this);
    }

    private PrintServiceAttributeSet getDynamicAttributes() {
        PrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
        attrs.add(getPrinterIsAcceptingJobs());
        attrs.add(getQueuedJobCount());
        return attrs;
    }

    public PrintServiceAttributeSet getUpdatedAttributes() {
        PrintServiceAttributeSet currSet = getDynamicAttributes();
        if (lastSet == null) {
            lastSet = currSet;
            return AttributeSetUtilities.unmodifiableView(currSet);
        } else {
            PrintServiceAttributeSet updates =
                new HashPrintServiceAttributeSet();
            Attribute []attrs =  currSet.toArray();
            for (int i=0; i<attrs.length; i++) {
                Attribute attr = attrs[i];
                if (!lastSet.containsValue(attr)) {
                    updates.add(attr);
                }
            }
            lastSet = currSet;
            return AttributeSetUtilities.unmodifiableView(updates);
        }
    }

    public void wakeNotifier() {
        synchronized (this) {
            if (notifier != null) {
                notifier.wake();
            }
        }
    }

    public void addPrintServiceAttributeListener(PrintServiceAttributeListener
                                                 listener) {
        synchronized (this) {
            if (listener == null) {
                return;
            }
            if (notifier == null) {
                notifier = new ServiceNotifier(this);
            }
            notifier.addListener(listener);
        }
    }

    public void removePrintServiceAttributeListener(
                                      PrintServiceAttributeListener listener) {
        synchronized (this) {
            if (listener == null || notifier == null ) {
                return;
            }
            notifier.removeListener(listener);
            if (notifier.isEmpty()) {
                notifier.stopNotifier();
                notifier = null;
            }
        }
    }

    public PrintServiceAttribute getAttribute(Class category) {
        if (category == null) {
            throw new NullPointerException("category");
        }
        if (!(PrintServiceAttribute.class.isAssignableFrom(category))) {
            throw new IllegalArgumentException("Not a PrintServiceAttribute");
        }
        if (category == ColorSupported.class) {
            if (isColorSup) {
                return ColorSupported.SUPPORTED;
            } else {
                return ColorSupported.NOT_SUPPORTED;
            }
        } else if (category == PrinterName.class) {
            return getPrinterName();
        } else if (category == QueuedJobCount.class) {
            return getQueuedJobCount();
        } else if (category == PrinterIsAcceptingJobs.class) {
            return getPrinterIsAcceptingJobs();
        } else {
            return null;
        }
    }

    public PrintServiceAttributeSet getAttributes() {

        PrintServiceAttributeSet attrs = new  HashPrintServiceAttributeSet();
        attrs.add(getPrinterName());
        attrs.add(getPrinterIsAcceptingJobs());
        attrs.add(getQueuedJobCount());
        if (isColorSup) {
            attrs.add(ColorSupported.SUPPORTED);
        } else {
            attrs.add(ColorSupported.NOT_SUPPORTED);
        }

        return AttributeSetUtilities.unmodifiableView(attrs);
    }

    public DocFlavor[] getSupportedDocFlavors() {

        int len = supportedDocFlavors.length;
        DocFlavor[] flavors = new DocFlavor[len];
        System.arraycopy(supportedDocFlavors, 0, flavors, 0, len);
        return flavors;
    }

    public boolean isDocFlavorSupported(DocFlavor flavor) {

        for (int f=0; f<supportedDocFlavors.length; f++) {
            if (flavor.equals(supportedDocFlavors[f])) {
                return true;
            }
        }
        return false;
    }

    public Class[] getSupportedAttributeCategories() {

        int totalCats = otherAttrCats.length;
        Class [] cats = new Class[totalCats + lenOptAttrCats];
        System.arraycopy(otherAttrCats, 0, cats, 0, totalCats);

        int index=totalCats;

        if (isSidesSup) {
            cats[index++] = Sides.class;
        }
        // We support collation on 2D printer jobs, even if the driver can't.
        cats[index++] = SheetCollate.class;

        if (isPrQualitySup) {
            cats[index++] = PrintQuality.class;
        }
        if (isResSup) {
            cats[index++] = PrinterResolution.class;
        }

        return cats;
    }

    public boolean isAttributeCategorySupported(Class category) {

        if (category == null) {
            throw new NullPointerException("null category");
        }

        if (!(Attribute.class.isAssignableFrom(category))) {
            throw new IllegalArgumentException(category +
                                               " is not an Attribute");
        }

        for (int i=0;i<otherAttrCats.length;i++) {
            if (category == otherAttrCats[i]) {
                return true;
            }
        }

        if ((category==Sides.class) && isSidesSup) {
            return true;
        }
        if (category==SheetCollate.class) {
            return true;
        }
        if ((category==PrintQuality.class) && isPrQualitySup) {
            return true;
        }
        if ((category==PrinterResolution.class) && isResSup) {
            return true;
        }

        return false;
    }

    public Object getDefaultAttributeValue(Class category) {
        if (category == null) {
            throw new NullPointerException("null category");
        }
        if (!Attribute.class.isAssignableFrom(category)) {
            throw new IllegalArgumentException(category +
                                               " is not an Attribute");
        }

        if (!isAttributeCategorySupported(category)) {
            return null;
        }

        if (category == Copies.class) {
            if (defCopies > 0) {
                return new Copies(defCopies);
            } else {
                return new Copies(1);
            }
        } else if (category == Chromaticity.class) {
            if (!isColorSup) {
                return Chromaticity.MONOCHROME;
            } else {
                return Chromaticity.COLOR;
            }
        } else if (category == JobName.class) {
            return new JobName("Java Printing", null);
        } else if (category == OrientationRequested.class) {
            if (defOrient == DMORIENT_LANDSCAPE) {
                return OrientationRequested.LANDSCAPE;
            } else {
                return OrientationRequested.PORTRAIT;
            }
        } else if (category == PageRanges.class) {
            return new PageRanges(1, Integer.MAX_VALUE);
        } else if (category == Media.class) {
            MediaSizeName msn = findWin32Media(defPaper);
            if (msn != null) {
                 return msn;
             } else {
                 if (mediaInitialized == false) {
                     initMedia();
                 }

                 if ((mediaSizeNames != null) && (mediaSizeNames.length > 0)) {
                   // if 'mediaSizeNames' is not null, idList and mediaSizes cannot
                   // be null but to be safe, add a check
                   if ((idList != null) && (mediaSizes != null) &&
                       (idList.size() == mediaSizes.length)) {
                     Integer defIdObj = new Integer(defPaper);
                     int index = idList.indexOf(defIdObj);
                     if (index>=0 && index<mediaSizes.length) {
                       return mediaSizes[index].getMediaSizeName();
                     }
                   }

                     return mediaSizeNames[0];
                 }
             }
        } else if (category == MediaPrintableArea.class) {
            float[] prnArea = getMediaPrintableArea(printer, defPaper);
            if (prnArea == null)
                return null;
            MediaPrintableArea printableArea = null;
              try {
                printableArea = new MediaPrintableArea(prnArea[0],
                                       prnArea[1],
                                       prnArea[2],
                                       prnArea[3],
                                       MediaPrintableArea.INCH);
            } catch (IllegalArgumentException e) {
            }
            return printableArea;
        } else if (category == SunAlternateMedia.class) {
            return null;
        } else if (category == Destination.class) {
            return new Destination((new File("out.prn")).toURI());
        } else if (category == Sides.class) {
            switch(defSides) {
            case DMDUP_VERTICAL :
                return Sides.TWO_SIDED_LONG_EDGE;
            case DMDUP_HORIZONTAL :
                return Sides.TWO_SIDED_SHORT_EDGE;
            default :
                return Sides.ONE_SIDED;
            }
        } else if (category == PrinterResolution.class) {
            int yRes = defYRes;
            int xRes = defQuality;
            if ((xRes < 0) || (yRes < 0)) {
                int res = (yRes > xRes) ? yRes : xRes;
                if (res > 0) {
                 return new PrinterResolution(res, res, PrinterResolution.DPI);
                }
            }
            else {
               return new PrinterResolution(xRes, yRes, PrinterResolution.DPI);
            }
        } else if (category == ColorSupported.class) {
            if (isColorSup) {
                return ColorSupported.SUPPORTED;
            } else {
                return ColorSupported.NOT_SUPPORTED;
            }
        } else if (category == PrintQuality.class) {
            if ((defQuality < 0) && (defQuality >= DMRES_HIGH)) {
                switch (defQuality) {
                case DMRES_HIGH:
                    return PrintQuality.HIGH;
                case DMRES_MEDIUM:
                    return PrintQuality.NORMAL;
                default:
                    return PrintQuality.DRAFT;
                }
            }
        } else if (category == RequestingUserName.class) {
            String userName = "";
            try {
              userName = System.getProperty("user.name", "");
            } catch (SecurityException se) {
            }
            return new RequestingUserName(userName, null);
        } else if (category == SheetCollate.class) {
            if (defCollate == DMCOLLATE_TRUE) {
                return SheetCollate.COLLATED;
            } else {
                return SheetCollate.UNCOLLATED;
            }
        } else if (category == Fidelity.class) {
            return Fidelity.FIDELITY_FALSE;
        }
        return null;
    }

    private boolean isPostScriptFlavor(DocFlavor flavor) {
        if (flavor.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT) ||
            flavor.equals(DocFlavor.INPUT_STREAM.POSTSCRIPT) ||
            flavor.equals(DocFlavor.URL.POSTSCRIPT)) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isPSDocAttr(Class category) {
        if (category == OrientationRequested.class) {
                return true;
        }
        else {
            return false;
        }
    }

    private boolean isAutoSense(DocFlavor flavor) {
        if (flavor.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE) ||
            flavor.equals(DocFlavor.INPUT_STREAM.AUTOSENSE) ||
            flavor.equals(DocFlavor.URL.AUTOSENSE)) {
            return true;
        }
        else {
            return false;
        }
    }

    public Object getSupportedAttributeValues(Class category,
                                              DocFlavor flavor,
                                              AttributeSet attributes) {

        if (category == null) {
            throw new NullPointerException("null category");
        }
        if (!Attribute.class.isAssignableFrom(category)) {
            throw new IllegalArgumentException(category +
                                             " does not implement Attribute");
        }
        if (flavor != null) {
            if (!isDocFlavorSupported(flavor)) {
                throw new IllegalArgumentException(flavor +
                                                  " is an unsupported flavor");
                // if postscript & category is already specified within the
                //  PostScript data we return null
            } else if (isAutoSense(flavor) ||(isPostScriptFlavor(flavor) &&
                       (isPSDocAttr(category)))){
                return null;
            }
        }
        if (!isAttributeCategorySupported(category)) {
            return null;
        }

        if (category == JobName.class) {
            return new JobName("Java Printing", null);
        } else if (category == RequestingUserName.class) {
          String userName = "";
          try {
            userName = System.getProperty("user.name", "");
          } catch (SecurityException se) {
          }
            return new RequestingUserName(userName, null);
        } else if (category == ColorSupported.class) {
            if (isColorSup) {
                return ColorSupported.SUPPORTED;
            } else {
                return ColorSupported.NOT_SUPPORTED;
            }
        } else if (category == Chromaticity.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
                flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
                flavor.equals(DocFlavor.URL.GIF) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
                flavor.equals(DocFlavor.URL.JPEG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.PNG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
                flavor.equals(DocFlavor.URL.PNG)) {
                if (!isColorSup) {
                    Chromaticity []arr = new Chromaticity[1];
                    arr[0] = Chromaticity.MONOCHROME;
                    return (arr);
                } else {
                    Chromaticity []arr = new Chromaticity[2];
                    arr[0] = Chromaticity.MONOCHROME;
                    arr[1] = Chromaticity.COLOR;
                    return (arr);
                }
            } else {
                return null;
            }
        } else if (category == Destination.class) {
            return new Destination((new File("out.prn")).toURI());
        } else if (category == OrientationRequested.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE) ||
                flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
                flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.PNG) ||
                flavor.equals(DocFlavor.URL.GIF) ||
                flavor.equals(DocFlavor.URL.JPEG) ||
                flavor.equals(DocFlavor.URL.PNG)) {
                OrientationRequested []arr = new OrientationRequested[3];
                arr[0] = OrientationRequested.PORTRAIT;
                arr[1] = OrientationRequested.LANDSCAPE;
                arr[2] = OrientationRequested.REVERSE_LANDSCAPE;
                return arr;
            } else {
                return null;
            }
        } else if ((category == Copies.class) ||
                   (category == CopiesSupported.class)) {
            synchronized (this) {
                if (gotCopies == false) {
                    nCopies = getCopiesSupported(printer, port);
                    gotCopies = true;
                }
            }
            return new CopiesSupported(1, nCopies);
        } else if (category == Media.class) {
            if (mediaInitialized == false) {
                initMedia();
            }

            if (gotTrays == false) {
                mediaTrays = getMediaTrays();
            }

            Media []arr =
              new Media[(mediaSizeNames == null ? 0 : mediaSizeNames.length) +
                        (mediaTrays == null ? 0 : mediaTrays.length)];
            if (mediaSizeNames != null) {
                System.arraycopy(mediaSizeNames, 0, arr,
                                 0, mediaSizeNames.length);
            }
            if (mediaTrays != null) {
                System.arraycopy(mediaTrays, 0, arr,
                        arr.length - mediaTrays.length, mediaTrays.length);
            }
            return arr;
        } else if (category == MediaPrintableArea.class) {
            if (mediaInitialized == false) {
                initMedia();
            }
            if (mediaPrintables == null) {
                return null;
            }

            // if getting printable area for a specific media size
            Media mediaName;
            if ((attributes != null) &&
                ((mediaName =
                  (Media)attributes.get(Media.class)) != null)) {

                if (mediaName instanceof MediaSizeName) {
                    MediaPrintableArea []arr = new MediaPrintableArea[1];

                    if (mediaSizeNames.length == mediaPrintables.length) {

                        for (int j=0; j < mediaSizeNames.length; j++) {

                            if (mediaName.equals(mediaSizeNames[j])) {
                                arr[0] = mediaPrintables[j];
                                return arr;
                            }
                        }
                    }

                    MediaSize ms =
                      MediaSize.getMediaSizeForName((MediaSizeName)mediaName);

                    if (ms != null) {
                        arr[0] = new MediaPrintableArea(0, 0,
                                                        ms.getX(MediaSize.INCH),
                                                        ms.getY(MediaSize.INCH),
                                                        MediaPrintableArea.INCH);
                        return arr;
                    } else {
                        return null;
                    }
                }
                // else an instance of MediaTray, fall thru returning
                // all MediaPrintableAreas
            }

            MediaPrintableArea []arr =
                new MediaPrintableArea[mediaPrintables.length];
            System.arraycopy(mediaPrintables, 0, arr, 0, mediaPrintables.length);
            return arr;
        } else if (category == SunAlternateMedia.class) {
            return new SunAlternateMedia(
                              (Media)getDefaultAttributeValue(Media.class));
        } else if (category == PageRanges.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                PageRanges []arr = new PageRanges[1];
                arr[0] = new PageRanges(1, Integer.MAX_VALUE);
                return arr;
            } else {
                return null;
            }
        } else if (category == PrinterResolution.class) {
            if (printRes == null) {
                return null;
            }
            PrinterResolution []arr = new PrinterResolution[printRes.length];
            System.arraycopy(printRes, 0, arr, 0, printRes.length);
            return arr;
        } else if (category == Sides.class) {
            if (flavor == null ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
                Sides []arr = new Sides[3];
                arr[0] = Sides.ONE_SIDED;
                arr[1] = Sides.TWO_SIDED_LONG_EDGE;
                arr[2] = Sides.TWO_SIDED_SHORT_EDGE;
                return arr;
            } else {
                return null;
            }
        } else if (category == PrintQuality.class) {
            PrintQuality []arr = new PrintQuality[3];
            arr[0] = PrintQuality.DRAFT;
            arr[1] = PrintQuality.HIGH;
            arr[2] = PrintQuality.NORMAL;
            return arr;
        } else if (category == SheetCollate.class) {
            if (isCollateSup &&
                    (flavor == null ||
                    flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                    flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE))) {
                SheetCollate []arr = new SheetCollate[2];
                arr[0] = SheetCollate.COLLATED;
                arr[1] = SheetCollate.UNCOLLATED;
                return arr;
            } else {
                SheetCollate []arr = new SheetCollate[1];
                arr[0] = SheetCollate.UNCOLLATED;
                return arr;
            }
        } else if (category == Fidelity.class) {
            Fidelity []arr = new Fidelity[2];
            arr[0] = Fidelity.FIDELITY_FALSE;
            arr[1] = Fidelity.FIDELITY_TRUE;
            return arr;
        } else {
            return null;
        }
    }

    public boolean isAttributeValueSupported(Attribute attr,
                                             DocFlavor flavor,
                                             AttributeSet attributes) {

        if (attr == null) {
            throw new NullPointerException("null attribute");
        }
        Class category = attr.getCategory();
        if (flavor != null) {
            if (!isDocFlavorSupported(flavor)) {
                throw new IllegalArgumentException(flavor +
                                                   " is an unsupported flavor");
                // if postscript & category is already specified within the PostScript data
                // we return false
            } else if (isAutoSense(flavor) || (isPostScriptFlavor(flavor) &&
                       (isPSDocAttr(category)))) {
                return false;
            }
        }

        if (!isAttributeCategorySupported(category)) {
            return false;
        }
        else if (attr.getCategory() == Chromaticity.class) {
            if ((flavor == null) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
                flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
                flavor.equals(DocFlavor.URL.GIF) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
                flavor.equals(DocFlavor.URL.JPEG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.PNG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
                flavor.equals(DocFlavor.URL.PNG)) {
                if (isColorSup) {
                    return true;
                } else {
                    return attr == Chromaticity.MONOCHROME;
                }
            } else {
                return false;
            }
        } else if (attr.getCategory() == Copies.class) {
            return isSupportedCopies((Copies)attr);

        } else if (attr.getCategory() == Destination.class) {
            URI uri = ((Destination)attr).getURI();
            if ("file".equals(uri.getScheme()) &&
                !(uri.getSchemeSpecificPart().equals(""))) {
                return true;
            } else {
            return false;
            }

        } else if (attr.getCategory() == Media.class) {
            if (attr instanceof MediaSizeName) {
                return isSupportedMedia((MediaSizeName)attr);
            }
            if (attr instanceof MediaTray) {
                return isSupportedMediaTray((MediaTray)attr);
            }

        } else if (attr.getCategory() == MediaPrintableArea.class) {
            return isSupportedMediaPrintableArea((MediaPrintableArea)attr);

        } else if (attr.getCategory() == SunAlternateMedia.class) {
            Media media = ((SunAlternateMedia)attr).getMedia();
            return isAttributeValueSupported(media, flavor, attributes);

        } else if (attr.getCategory() == PageRanges.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        } else if (attr.getCategory() == SheetCollate.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        } else if (attr.getCategory() == Sides.class) {
            if (flavor != null &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE))) {
                return false;
            }
        } else if (attr.getCategory() == PrinterResolution.class) {
            if (attr instanceof PrinterResolution) {
                return isSupportedResolution((PrinterResolution)attr);
            }
        } else if (attr.getCategory() == OrientationRequested.class) {
            if (attr == OrientationRequested.REVERSE_PORTRAIT ||
                (flavor != null) &&
                !(flavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
                flavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE) ||
                flavor.equals(DocFlavor.INPUT_STREAM.GIF) ||
                flavor.equals(DocFlavor.INPUT_STREAM.JPEG) ||
                flavor.equals(DocFlavor.INPUT_STREAM.PNG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.GIF) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.JPEG) ||
                flavor.equals(DocFlavor.BYTE_ARRAY.PNG) ||
                flavor.equals(DocFlavor.URL.GIF) ||
                flavor.equals(DocFlavor.URL.JPEG) ||
                flavor.equals(DocFlavor.URL.PNG))) {
                return false;
            }

        } else if (attr.getCategory() == ColorSupported.class) {
            if  ((!isColorSup && (attr == ColorSupported.SUPPORTED)) ||
                (isColorSup && (attr == ColorSupported.NOT_SUPPORTED))) {
                return false;
            }
        }
        return true;
    }

    public AttributeSet getUnsupportedAttributes(DocFlavor flavor,
                                                 AttributeSet attributes) {

        if (flavor != null && !isDocFlavorSupported(flavor)) {
            throw new IllegalArgumentException("flavor " + flavor +
                                               "is not supported");
        }

        if (attributes == null) {
            return null;
        }

        Attribute attr;
        AttributeSet unsupp = new HashAttributeSet();
        Attribute []attrs = attributes.toArray();
        for (int i=0; i<attrs.length; i++) {
            try {
                attr = attrs[i];
                if (!isAttributeCategorySupported(attr.getCategory())) {
                    unsupp.add(attr);
                }
                else if (!isAttributeValueSupported(attr, flavor, attributes)) {
                    unsupp.add(attr);
                }
            } catch (ClassCastException e) {
            }
        }
        if (unsupp.isEmpty()) {
            return null;
        } else {
            return unsupp;
        }
    }

    public ServiceUIFactory getServiceUIFactory() {
        return null;
    }

    public String toString() {
        return "Win32 Printer : " + getName();
    }

    public boolean equals(Object obj) {
        return  (obj == this ||
                 (obj instanceof Win32PrintService &&
                  ((Win32PrintService)obj).getName().equals(getName())));
    }

   public int hashCode() {
        return this.getClass().hashCode()+getName().hashCode();
    }

    public boolean usesClass(Class c) {
        return (c == sun.awt.windows.WPrinterJob.class);
    }

    private native int[] getAllMediaIDs(String printerName, String port);
    private native int[] getAllMediaSizes(String printerName, String port);
    private native int[] getAllMediaTrays(String printerName, String port);
    private native float[] getMediaPrintableArea(String printerName,
                                                 int paperSize);
    private native String[] getAllMediaNames(String printerName, String port);
    private native String[] getAllMediaTrayNames(String printerName, String port);
    private native int getCopiesSupported(String printerName, String port);
    private native int[] getAllResolutions(String printerName, String port);
    private native int getCapabilities(String printerName, String port);

    private native int[] getDefaultSettings(String printerName);
    private native int getJobStatus(String printerName, int type);
    private native String getPrinterPort(String printerName);
}


class Win32MediaSize extends MediaSizeName {
    private static ArrayList winStringTable = new ArrayList();
    private static ArrayList winEnumTable = new ArrayList();

    private Win32MediaSize(int x) {
        super(x);

    }

    private synchronized static int nextValue(String name) {
      winStringTable.add(name);
      return (winStringTable.size()-1);
    }

    public Win32MediaSize(String name) {
        super(nextValue(name));
        winEnumTable.add(this);
    }

    private MediaSizeName[] getSuperEnumTable() {
      return (MediaSizeName[])super.getEnumValueTable();
    }

    static {
         /* initialize Win32PrintService.predefMedia */
        {
            Win32MediaSize winMedia = new Win32MediaSize(-1);

            // cannot call getSuperEnumTable directly because of static context
            MediaSizeName[] enumMedia = winMedia.getSuperEnumTable();
            if (enumMedia != null) {
                Win32PrintService.predefMedia = new MediaSize[enumMedia.length];

                for (int i=0; i<enumMedia.length; i++) {
                    Win32PrintService.predefMedia[i] =
                        MediaSize.getMediaSizeForName(enumMedia[i]);
                }
            }
        }
    }


    protected String[] getStringTable() {
      String[] nameTable = new String[winStringTable.size()];
      return (String[])winStringTable.toArray(nameTable);
    }

    protected EnumSyntax[] getEnumValueTable() {
      MediaSizeName[] enumTable = new MediaSizeName[winEnumTable.size()];
      return (MediaSizeName[])winEnumTable.toArray(enumTable);
    }

}

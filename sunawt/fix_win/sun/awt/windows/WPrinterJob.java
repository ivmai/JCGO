/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)WPrinterJob.java 1.44 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.windows;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.BasicStroke;

import java.awt.image.BufferedImage;

import java.awt.peer.ComponentPeer;

import java.awt.print.Pageable;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import javax.print.PrintService;

import java.io.IOException;

import java.util.Properties;

import sun.awt.EmbeddedFrame;
import sun.java2d.Disposer;
import sun.java2d.DisposerRecord;

import sun.awt.image.Image;
import sun.awt.image.ImageRepresentation;

import sun.print.PeekGraphics;
import sun.print.PeekMetrics;

import java.net.URL;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Attribute;
import javax.print.attribute.standard.Sides;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;

import sun.print.RasterPrinterJob;
import sun.print.SunAlternateMedia;

import sun.print.Win32MediaTray;

/**
 * A class which initiates and executes a Win32 printer job.
 *
 * @version 1.0 07 Nov 1997
 * @author Richard Blanchard
 */
public class WPrinterJob extends RasterPrinterJob {

 static final class HandleRecord extends DisposerRecord
 {

  private long mPrintDC;
  private long mPrintHDevMode;
  private long mPrintHDevNames;

  public void dispose()
  {
   WPrinterJob.deleteDC(mPrintDC, mPrintHDevMode, mPrintHDevNames);
   mPrintDC = mPrintHDevMode = mPrintHDevNames = 0L;
  }
 }

 /* Class Constants */


/* Instance Variables */

    /**
     * These are Windows' ExtCreatePen End Cap Styles
     * and must match the values in <WINGDI.h>
     */
    protected static final long PS_ENDCAP_ROUND  = 0x00000000;
    protected static final long PS_ENDCAP_SQUARE   = 0x00000100;
    protected static final long PS_ENDCAP_FLAT   =   0x00000200;

    /**
     * These are Windows' ExtCreatePen Line Join Styles
     * and must match the values in <WINGDI.h>
     */
    protected static final long PS_JOIN_ROUND   =    0x00000000;
    protected static final long PS_JOIN_BEVEL   =    0x00001000;
    protected static final long PS_JOIN_MITER   =    0x00002000;

    /**
     * This is the Window's Polygon fill rule which
     * Selects alternate mode (fills the area between odd-numbered
     * and even-numbered polygon sides on each scan line).
     * It must match the value in <WINGDI.h> It can be passed
     * to setPolyFillMode().
     */
    protected static final int POLYFILL_ALTERNATE = 1;

    /**
     * This is the Window's Polygon fill rule which
     * Selects winding mode which fills any region
     * with a nonzero winding value). It must match
     * the value in <WINGDI.h> It can be passed
     * to setPolyFillMode().
     */
    protected static final int POLYFILL_WINDING = 2;

    /**
     * The maximum value for a Window's color component
     * as passed to selectSolidBrush.
     */
    private static final int MAX_WCOLOR = 255;

    /**
     * Useful mainly for debugging, this system property
     * can be used to force the printing code to print
     * using a particular pipeline. The two currently
     * supported values are FORCE_RASTER and FORCE_PDL.
     */
    private static final String FORCE_PIPE_PROP = "sun.java2d.print.pipeline";

    /**
     * When the system property FORCE_PIPE_PROP has this value
     * then each page of a print job will be rendered through
     * the raster pipeline.
     */
    private static final String FORCE_RASTER = "raster";

    /**
     * When the system property FORCE_PIPE_PROP has this value
     * then each page of a print job will be rendered through
     * the PDL pipeline.
     */
    private static final String FORCE_PDL = "pdl";

    /**
     * When the system property SHAPE_TEXT_PROP has this value
     * then text is always rendered as a shape, and no attempt is made
     * to match the font through GDI
     */
    private static final String SHAPE_TEXT_PROP = "sun.java2d.print.shapetext";

    /**
     * values obtained from System properties in static initialiser block
     */
    private static boolean forcePDL;
    private static boolean forceRaster;
    static boolean shapeTextProp;

    private static final String boldNames[] = {
        "bold", "demibold", "demi-bold", "demi bold", "negreta", "demi"
    };

    private static final String italicNames[] = {
        "italic", "cursiva", "oblique", "inclined"
    };

    private static final String boldItalicNames[] = {
        "bolditalic", "bold-italic", "bold italic", "boldoblique",
        "bold-oblique", "bold oblique", "demibold italic", "negreta cursiva",
        "demi oblique"
    };

    /* Collation and copy flags.
     * The Windows PRINTDLG struct has a nCopies field which on return
     * indicates how many copies of a print job an application must render.
     * There is also a PD_COLLATE member of the flags field which if
     * set on return indicates the application generated copies should be
     * collated.
     * Windows printer drivers typically - but not always - support
     * generating multiple copies themselves, but uncollated is more
     * universal than collated copies.
     * When they do, they read the initial values from the PRINTDLG structure
     * and set them into the driver's DEVMODE structure and intialise
     * the printer DC based on that, so that when printed those settings
     * will be used.
     * For drivers supporting both these capabilities via DEVMODE, then on
     * return from the Print Dialog, nCopies is set to 1 and the PD_COLLATE is
     * cleared, so that the application will only render 1 copy and the
     * driver takes care of the rest.
     *
     * Applications which want to know what's going on have to be DEVMODE
     * savvy and peek at that.
     * DM_COPIES flag indicates support for multiple driver copies
     * and dmCopies is the number of copies the driver will print
     * DM_COLLATE flag indicates support for collated driver copies and
     * dmCollate == DMCOLLATE_TRUE indicates the option is in effect.
     *
     * Multiple copies from Java applications:
     * We provide API to get & set the number of copies as well as allowing the
     * user to choose it, so we need to be savvy about DEVMODE, so that
     * we can accurately report back the number of copies selected by
     * the user, as well as make use of the driver to render multiple copies.
     *
     * Collation and Java applications:
     * We presently provide no API for specifying collation, but its
     * present on the Windows Print Dialog, and when a user checks it
     * they expect it to be obeyed.
     * The best thing to do is to detect exactly the cases where the
     * driver doesn't support this and render multiple copies ourselves.
     * To support all this we need several flags which signal the
     * printer's capabilities and the user's requests.
     * Its questionable if we (yet) need to make a distinction between
     * the user requesting collation and the driver supporting it.
     * Since for now we only need to know whether we need to render the
     * copies. However it allows the logic to be clearer.
     * These fields are changed by native code which detects the driver's
     * capabilities and the user's choices.
     */
     private boolean driverDoesMultipleCopies = true;
     private boolean driverDoesCollation = true;
     private boolean userRequestedCollation = false;
     private boolean noDefaultPrinter = false;

    /**
     * The Windows device context we will print into.
     * This variable is set after the Print dialog
     * is okayed by the user. If the user cancels
     * the print dialog, then this variable is 0.
     * Much of the configuration information for a printer is
     * obtained through printer device specific handles.
     * We need to associate these with, and free with, the mPrintDC.
     */
    private final HandleRecord handleRecord = new HandleRecord();

    private int mPrintPaperSize;

    private int mPrintXRes;   // pixels per inch in x direction

    private int mPrintYRes;   // pixels per inch in y direction

    private int mPrintPhysX;  // x offset in pixels of printable area

    private int mPrintPhysY;  // y offset in pixels of printable area

    private int mPrintWidth;  // width in pixels of printable area

    private int mPrintHeight; // height in pixels of printable area

    private int mPageWidth;   // width in pixels of entire page

    private int mPageHeight;  // height in pixels of entire page

    private int mAttSides;
    private int mAttChromaticity;
    private int mAttXRes;
    private int mAttYRes;
    private int mAttQuality;
    private int mAttCollate;
    private int mAttCopies;
    private int mAttOrientation;
    private int mAttMediaSizeName;
    private int mAttMediaTray;

    private String mDestination = null;

    /**
     * The last color set into the print device context or
     * <code>null</code> if no color has been set.
     */
    private Color mLastColor;

    /**
     * The last text color set into the print device context or
     * <code>null</code> if no color has been set.
     */
    private Color mLastTextColor;

    /**
     * The last java font set as a GDI font in the printer
     * device context. Can be NULL if no GDI font has been
     * set.
     */
    private Font mLastFont;
    private int mLastRotation;
    private float mLastAwScale;

    private ComponentPeer dialogOwnerPeer;
    private final Object disposerReferent = new Object();

 /* Static Initializations */

    static {
        // AWT has to be initialized for the native code to function correctly.
        Toolkit.getDefaultToolkit();

        initIDs();

        forcePDL = false;
        forceRaster = false;
        shapeTextProp = false;

        /* The system property FORCE_PIPE_PROP
         * can be used to force the printing code to
         * use a particular pipeline. Either the raster
         * pipeline or the pdl pipeline can be forced.
         */
        String forceStr =
           (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction(FORCE_PIPE_PROP));

        if (forceStr != null) {
            if (forceStr.equalsIgnoreCase(FORCE_PDL)) {
                forcePDL = true;
            } else if (forceStr.equalsIgnoreCase(FORCE_RASTER)) {
                forceRaster = true;
            }
        }

        String shapeTextStr =
           (String)java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction(SHAPE_TEXT_PROP));

        if (shapeTextStr != null) {
            shapeTextProp = true;
        }
    }

 /* Constructors */

    public WPrinterJob()
    {
        Disposer.addRecord(disposerReferent, handleRecord);
        initAttributeMembers();
    }

/* Instance Methods */

    /**
     * Display a dialog to the user allowing the modification of a
     * PageFormat instance.
     * The <code>page</code> argument is used to initialize controls
     * in the page setup dialog.
     * If the user cancels the dialog, then the method returns the
     * original <code>page</code> object unmodified.
     * If the user okays the dialog then the method returns a new
     * PageFormat object with the indicated changes.
     * In either case the original <code>page</code> object will
     * not be modified.
     * @param     page    the default PageFormat presented to the user
     *                    for modification
     * @return    the original <code>page</code> object if the dialog
     *            is cancelled, or a new PageFormat object containing
     *            the format indicated by the user if the dialog is
     *            acknowledged
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     JDK1.2
     */
    public PageFormat pageDialog(PageFormat page) throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        PrintService printservice = getPrintService();
        PageFormat pageClone = (PageFormat) page.clone();
        Frame frame = getEmbeddedFrame();
        if (frame != null) {
            dialogOwnerPeer = frame.getPeer();
        }

        boolean doIt = pageSetup(pageClone, null);
        if (doIt && printservice != null)
        {
         updatePageAttributes(printservice, pageClone);
         return pageClone;
        }
        return page;
    }

    /**
     * Presents the user a dialog for changing properties of the
     * print job interactively.
     * @returns false if the user cancels the dialog and
     *          true otherwise.
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public boolean printDialog() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        if (noDefaultPrinter == true) {
            return false;
        }

        Pageable pageable = getPageable();
        Frame frame = getEmbeddedFrame();
        if (frame != null)
            dialogOwnerPeer = frame.getPeer();
        boolean flag = jobSetup(pageable, checkAllowedToPrintToFile());
        if (flag && isOpenBook(pageable)) {
            float f = (float)mPrintXRes / 72;
            float f1 = (float)mPrintYRes / 72;
            float f2 = (float)mPageWidth / f;
            float f3 = (float)mPageHeight / f1;
            PageFormat pageformat = pageable.getPageFormat(0);
            Paper paper = pageformat.getPaper();
            switch (mAttOrientation) {
              case 2:
                pageformat.setOrientation(0);
                paper.setSize(f3, f2);
                break;

              default:
                pageformat.setOrientation(1);
                paper.setSize(f2, f3);
                break;
            }
            pageformat.setPaper(paper);
            pageformat = validatePage(pageformat);
            setPrintable(pageable.getPrintable(0), pageformat);
            pageable = getPageable();
            pageformat = pageable.getPageFormat(0);
            paper = pageformat.getPaper();
           }
        return flag;
    }

    public final int[] getWin32MediaAttrib() {
        int ai[] = new int[2];
        if (attributes != null) {
            Media media = (Media)attributes.get(
                           javax.print.attribute.standard.Media.class);
            if (media instanceof MediaSizeName) {
                MediaSizeName mediasizename = (MediaSizeName)media;
                MediaSize mediasize =
                        MediaSize.getMediaSizeForName(mediasizename);
                if (mediasize != null) {
                    ai[0] = (int)(mediasize.getX(25400) * 72.0);
                    ai[1] = (int)(mediasize.getY(25400) * 72.0);
                }
            }
        }
        return ai;
    }

     /**
     * Associate this PrinterJob with a new PrintService.
     *
     * Throws <code>PrinterException</code> if the specified service
     * cannot support the <code>Pageable</code> and
     * </code>Printable</code> interfaces necessary to support 2D printing.
     * @param a print service which supports 2D printing.
     *
     * @throws PrinterException if the specified service does not support
     * 2D printing.
     */
    public void setPrintService(PrintService service)
        throws PrinterException {
        super.setPrintService(service);
        setNativePrintService(service.getName());
    }

    /* associates this job with the specified native service */
    private native void setNativePrintService(String name);

    public PrintService getPrintService() {
        String printerName = getNativePrintService();

        if (printerName != null) {
            PrintService defSvc =  super.lookupDefaultPrintService();
            if ((defSvc != null) && (defSvc.getName().equals(printerName))) {
                return defSvc;
            } else {
                PrintService[] svcs = super.lookupPrintServices();

                for (int i=0; i<svcs.length; i++) {
                    if (svcs[i].getName().equals(printerName)) {
                        return svcs[i];
                    }
                }
            }
        }

        return super.getPrintService();
    }

    private native String getNativePrintService();

    public final String getPrinterAttrib() {
        PrintService printservice = getPrintService();
        return printservice == null ? null : printservice.getName();
    }

    private int getStyle(String s) {
        String s1 = s.toLowerCase();
        for (int i = 0; i < boldItalicNames.length; i++)
            if (s1.indexOf(boldItalicNames[i]) != -1)
             return 3;

        for (int j = 0; j < italicNames.length; j++)
            if (s1.indexOf(italicNames[j]) != -1)
             return 2;

        for (int k = 0; k < boldNames.length; k++)
            if (s1.indexOf(boldNames[k]) != -1)
             return 1;

        return 0;
    }

    private void initAttributeMembers() {
            mAttSides = 0;
            mAttChromaticity = 0;
            mAttXRes = 0;
            mAttYRes = 0;
            mAttQuality = 0;
            mAttCollate = -1;
            mAttCopies = 0;
            mAttOrientation = 0;
            mAttMediaTray = 0;
            mAttMediaSizeName = 0;
            mDestination = null;

    }

    /**
     * copy the attributes to the native print job
     */
    protected void setAttributes(PrintRequestAttributeSet attributes)
        throws PrinterException {

            // initialize attribute values
            initAttributeMembers();

            super.setAttributes(attributes);

            mAttCopies = getCopiesInt();
            mDestination = destinationAttr;

            if (attributes == null) {
                return;
            }

            Attribute attr;
            Attribute[] attrs = attributes.toArray();
            for (int i=0; i<attrs.length; i++) {
                try {
                    attr = attrs[i];
                    if (attr.getCategory()==Sides.class) {
                        if (attr.equals(Sides.TWO_SIDED_LONG_EDGE)) {
                            mAttSides = 2; // DMDUP_VERTICAL
                        } else if (attr.equals(Sides.TWO_SIDED_SHORT_EDGE)) {
                            mAttSides = 3; // DMDUP_HORIZONTAL
                        } else { // Sides.ONE_SIDED
                            mAttSides = 1;
                        }
                    }
                    else if (attr.getCategory()==Chromaticity.class) {
                        if (attr.equals(Chromaticity.COLOR)) {
                            mAttChromaticity = 2; // DMCOLOR_COLOR
                        } else {
                            mAttChromaticity = 1; // DMCOLOR_MONOCHROME
                        }
                    }
                    else if (attr.getCategory()==PrinterResolution.class) {
                        PrinterResolution pr = (PrinterResolution)attr;
                        mAttXRes = pr.getCrossFeedResolution(PrinterResolution.DPI);
                        mAttYRes = pr.getFeedResolution(PrinterResolution.DPI);
                    }
                    else if (attr.getCategory()==PrintQuality.class) {
                        if (attr.equals(PrintQuality.HIGH)) {
                            mAttQuality = -4; // DMRES_HIGH
                        } else if (attr.equals(PrintQuality.NORMAL)) {
                            mAttQuality = -3; // DMRES_MEDIUM
                        } else {
                            mAttQuality = -2; // DMRES_LOW
                        }
                    }
                    else if (attr.getCategory()==SheetCollate.class) {
                        if (attr.equals(SheetCollate.COLLATED)) {
                            mAttCollate = 1; // DMCOLLATE_TRUE
                        } else {
                            mAttCollate = 0; // DMCOLLATE_FALSE
                        }
                    }  else if (attr.getCategory() == Media.class ||
                                attr.getCategory() ==SunAlternateMedia.class) {
                        /* SunAlternateMedia is used if its a tray, and
                         * any Media that is specified is not a tray.
                         */
                        if (attr.getCategory() == SunAlternateMedia.class) {
                            Media media = (Media)attributes.get(Media.class);
                            if (media == null ||
                                !(media instanceof MediaTray)) {
                                attr = ((SunAlternateMedia)attr).getMedia();
                            }
                        }
                        if (attr instanceof MediaSizeName) {
                            // Note: Nothing to do here.
                        }
                        if (attr instanceof MediaTray) {
                            if (attr.equals(MediaTray.BOTTOM)) {
                                mAttMediaTray = 2;        // DMBIN_LOWER
                            } else if (attr.equals(MediaTray.ENVELOPE)) {
                                mAttMediaTray = 5;        // DMBIN_ENVELOPE
                            } else if (attr.equals(MediaTray.LARGE_CAPACITY)) {
                                mAttMediaTray = 11;      // DMBIN_LARGECAPACITY
                            } else if (attr.equals(MediaTray.MAIN)) {
                                mAttMediaTray =1;           // DMBIN_UPPER
                            } else if (attr.equals(MediaTray.MANUAL)) {
                                mAttMediaTray = 4;          // DMBIN_MANUAL
                            } else if (attr.equals(MediaTray.MIDDLE)) {
                                mAttMediaTray = 3;          // DMBIN_MIDDLE
                            } else if (attr.equals(MediaTray.SIDE)) {
                                // no equivalent predefined value
                                mAttMediaTray = 7;          // DMBIN_AUTO
                            } else if (attr.equals(MediaTray.TOP)) {
                                mAttMediaTray =1;           // DMBIN_UPPER
                            } else {
                              if (attr instanceof Win32MediaTray) {
                                mAttMediaTray = ((Win32MediaTray)attr).winID;
                              } else {
                                mAttMediaTray =1;  // default
                              }
                            }
                        }
                    }

                } catch (ClassCastException e) {
                }
            }

        }

    /**
     * Alters the orientation and Paper to match defaults obtained
     * from a printer.
     */
    private native void getDefaultPage(PageFormat page);

    /**
     * The passed in PageFormat will be copied and altered to describe
     * the default page size and orientation of the PrinterJob's
     * current printer.
     * Note: PageFormat.getPaper() returns a clone and getDefaultPage()
     * gets that clone so it won't overwrite the original paper.
     */
    public PageFormat defaultPage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        getDefaultPage(newPage);
        return newPage;
    }

    /**
     * validate the paper size against the current printer.
     */
    protected native void validatePaper(Paper origPaper, Paper newPaper );

    /**
     * Examine the metrics captured by the
     * <code>PeekGraphics</code> instance and
     * if capable of directly converting this
     * print job to the printer's control language
     * or the native OS's graphics primitives, then
     * return a <code>PathGraphics</code> to perform
     * that conversion. If there is not an object
     * capable of the conversion then return
     * <code>null</code>. Returning <code>null</code>
     * causes the print job to be rasterized.
     */

    protected Graphics2D createPathGraphics(PeekGraphics peekGraphics,
                                            PrinterJob printerJob,
                                            Printable painter,
                                            PageFormat pageFormat,
                                            int pageIndex) {

        WPathGraphics pathGraphics;
        PeekMetrics metrics = peekGraphics.getMetrics();

        /* If the application has drawn anything that
         * out PathGraphics class can not handle then
         * return a null PathGraphics. If the property
         * to force the raster pipeline has been set then
         * we also want to avoid the path (pdl) pipeline
         * and return null.
         */
       if (forcePDL == false && (forceRaster == true
                                  || metrics.hasNonSolidColors()
                                  || metrics.hasCompositing()
                                  )) {
            pathGraphics = null;
        } else {
            BufferedImage bufferedImage = new BufferedImage(8, 8,
                                            BufferedImage.TYPE_INT_RGB);
            Graphics2D bufferedGraphics = bufferedImage.createGraphics();

            boolean canRedraw = peekGraphics.getAWTDrawingOnly() == false;
            pathGraphics =  new WPathGraphics(bufferedGraphics, printerJob,
                                              painter, pageFormat, pageIndex,
                                              canRedraw);
        }

        return pathGraphics;
    }


    protected double getXRes() {
        if (mAttXRes != 0) {
            return mAttXRes;
        } else {
            return mPrintXRes;
        }
    }

    protected double getYRes() {
        if (mAttYRes != 0) {
            return mAttYRes;
        } else {
            return mPrintYRes;
        }
    }

    protected double getPhysicalPrintableX(Paper p) {
        return mPrintPhysX;
    }

    protected double getPhysicalPrintableY(Paper p) {
        return mPrintPhysY;
    }

    protected double getPhysicalPrintableWidth(Paper p) {
        return mPrintWidth;
    }

    protected double getPhysicalPrintableHeight(Paper p) {
        return mPrintHeight;
    }

    protected double getPhysicalPageWidth(Paper p) {
        return mPageWidth;
    }

    protected double getPhysicalPageHeight(Paper p) {
        return mPageHeight;
    }

    /**
     * We don't (yet) provide API to support collation, and
     * when we do the logic here will require adjustment, but
     * this method is currently necessary to honour user-originated
     * collation requests - which can only originate from the print dialog.
     */
    protected boolean isCollated() {
        return userRequestedCollation;
    }

    /**
     * Returns how many times the entire book should
     * be printed by the PrintJob. If the printer
     * itself supports collation then this method
     * should return 1 indicating that the entire
     * book need only be printed once and the copies
     * will be collated and made in the printer.
     */
    protected int getCollatedCopies() {
        if  (!driverDoesCollation) {
            // if collation request is from attribute set
            if (super.isCollated()) {
                // we will do our own collation so we need to
                // tell the printer to not collate and copies=1
                mAttCollate = 0;
                mAttCopies = 1;
                return getCopiesInt();
                // else if collation request is from native print dialog
                // and collation attribue is not set
            } else if (isCollated() && !collateAttReq) {
                // we will do our own collation so we need to
                // tell the printer to not collate and copies=1
                mAttCollate = 0;
                mAttCopies = 1;
                return getCopies();
            }
        }

        return 1;
    }

    /**
     * Returns how many times each page in the book
     * should be consecutively printed by PrinterJob.
     * If the underlying Window's driver will
     * generate the copies, rather than having RasterPrinterJob
     * iterate over the number of copies, this method always returns
     * 1.
     */
    protected int getNoncollatedCopies() {
        if (driverDoesMultipleCopies || isCollated()) {
            return 1;
        } else {
            return getCopies();
        }
    }

    public final int getOrientAttrib() {
        OrientationRequested orientationrequested = attributes != null ?
            (OrientationRequested)attributes.get(
            javax.print.attribute.standard.OrientationRequested.class) : null;
        if (orientationrequested != null) {
            if (orientationrequested.equals(
                OrientationRequested.REVERSE_LANDSCAPE))
                return 2;
            if (orientationrequested.equals(OrientationRequested.LANDSCAPE))
                return 0;
        }
        return 1;
    }

    private long getDevMode() {
        return handleRecord.mPrintHDevMode;
    }

    private long getDevNames() {
        return handleRecord.mPrintHDevNames;
    }

    /**
     * Return the Window's device context that we are printing
     * into.
     */
    private long getPrintDC() {
        return handleRecord.mPrintDC;
    }

    protected void beginPath() {
        beginPath(getPrintDC());
    }

    protected void endPath() {
        endPath(getPrintDC());
    }

    protected void closeFigure() {
        closeFigure(getPrintDC());
    }

    protected void fillPath() {
        fillPath(getPrintDC());
    }

    protected void moveTo(float x, float y) {
        moveTo(getPrintDC(), x, y);
    }

    protected void lineTo(float x, float y) {
        lineTo(getPrintDC(), x, y);
    }

    protected void polyBezierTo(float control1x, float control1y,
                                float control2x, float control2y,
                                float endX, float endY) {

        polyBezierTo(getPrintDC(), control1x, control1y,
                               control2x, control2y,
                               endX, endY);
    }

    /**
     * Set the current polgon fill rule into the printer device context.
     * The <code>fillRule</code> should
     * be one of the following Windows constants:
     * <code>ALTERNATE</code> or <code>WINDING</code>.
     */
    protected void setPolyFillMode(int fillRule) {
        setPolyFillMode(getPrintDC(), fillRule);
    }

    private void setDevMode(long l) {
        handleRecord.mPrintHDevMode = l;
    }

    private void setDevNames(long l) {
        handleRecord.mPrintHDevNames = l;
    }

    private void setPrintDC(long l) {
        handleRecord.mPrintDC = l;
    }

    /*
     * Create a Window's solid brush for the color specified
     * by <code>(red, green, blue)</code>. Once the brush
     * is created, select it in the current printing device
     * context and free the old brush.
     */
    protected void selectSolidBrush(Color color) {

        /* We only need to select a brush if the color has changed.
        */
        if (color.equals(mLastColor) == false) {
            mLastColor = color;
            float[] rgb = color.getColorComponents(null);

            selectSolidBrush(getPrintDC(), (int) (rgb[0] * MAX_WCOLOR),
                                       (int) (rgb[1] * MAX_WCOLOR),
                                       (int) (rgb[2] * MAX_WCOLOR));
        }
    }

    /**
     * Return the x coordinate of the current pen
     * position in the print device context.
     */
    protected int getPenX() {

        return getPenX(getPrintDC());
    }


    /**
     * Return the y coordinate of the current pen
     * position in the print device context.
     */
    protected int getPenY() {

        return getPenY(getPrintDC());
    }

    /**
     * Set the current path in the printer device's
     * context to be clipping path.
     */
    protected void selectClipPath() {
        selectClipPath(getPrintDC());
    }


    protected void frameRect(float x, float y, float width, float height) {
        frameRect(getPrintDC(), x, y, width, height);
    }

    protected void fillRect(float x, float y, float width, float height,
                            Color color) {
        float[] rgb = color.getColorComponents(null);

        fillRect(getPrintDC(), x, y, width, height,
                 (int) (rgb[0] * MAX_WCOLOR),
                 (int) (rgb[1] * MAX_WCOLOR),
                 (int) (rgb[2] * MAX_WCOLOR));
    }


    protected void selectPen(float width, Color color) {

        float[] rgb = color.getColorComponents(null);

        selectPen(getPrintDC(), width,
                  (int) (rgb[0] * MAX_WCOLOR),
                  (int) (rgb[1] * MAX_WCOLOR),
                  (int) (rgb[2] * MAX_WCOLOR));
    }


    protected boolean selectStylePen(int cap, int join, float width,
                                     Color color) {

        long endCap;
        long lineJoin;

        float[] rgb = color.getColorComponents(null);

        switch(cap) {
        case BasicStroke.CAP_BUTT: endCap = PS_ENDCAP_FLAT; break;
        case BasicStroke.CAP_ROUND: endCap = PS_ENDCAP_ROUND; break;
        default:
        case BasicStroke.CAP_SQUARE: endCap = PS_ENDCAP_SQUARE; break;
        }

        switch(join) {
        case BasicStroke.JOIN_BEVEL:lineJoin = PS_JOIN_BEVEL; break;
        default:
        case BasicStroke.JOIN_MITER:lineJoin = PS_JOIN_MITER; break;
        case BasicStroke.JOIN_ROUND:lineJoin = PS_JOIN_ROUND; break;
        }

        return (selectStylePen(getPrintDC(), endCap, lineJoin, width,
                               (int) (rgb[0] * MAX_WCOLOR),
                               (int) (rgb[1] * MAX_WCOLOR),
                               (int) (rgb[2] * MAX_WCOLOR)));
    }


    /**
     * Set a GDI font capable of drawing the java Font
     * passed in.
     */
    protected boolean setFont(Font font, int rotation, float awScale) {

        boolean didSetFont = true;

        if (font.equals(mLastFont) == false || (rotation != mLastRotation)
            || (awScale != mLastAwScale)) {

            int fontStyle = font.getStyle() | getStyle(font.getFontName());
            didSetFont = setFont(getPrintDC(),
                                 font.getFamily(),
                                 font.getSize2D(),
                                 (fontStyle & Font.BOLD) != 0,
                                 (fontStyle & Font.ITALIC) != 0,
                                 rotation, awScale);

            if (didSetFont) {
                mLastFont = font;
                mLastRotation = rotation;
                mLastAwScale = awScale;
            }
        }

        return didSetFont;
    }

    /**
     * Set the GDI color for text drawing.
     */
    protected void setTextColor(Color color) {

        /* We only need to select a brush if the color has changed.
        */
        if (color.equals(mLastTextColor) == false) {
            mLastTextColor = color;
            float[] rgb = color.getColorComponents(null);

            setTextColor(getPrintDC(), (int) (rgb[0] * MAX_WCOLOR),
                                   (int) (rgb[1] * MAX_WCOLOR),
                                   (int) (rgb[2] * MAX_WCOLOR));
        }
    }

    /**
     * Draw the string <code>text</code> to the printer's
     * device context at the specified position.
     */
    protected void textOut(String text, float x, float y, Font font) {
        textOut(getPrintDC(), text, x, y, font);
    }


     /**
     * Draw the 24 bit BGR image buffer represented by
     * <code>image</code> to the GDI device context
     * <code>printDC</code>. The image is drawn at
     * <code>(destX, destY)</code> in device coordinates.
     * The image is scaled into a square of size
     * specified by <code>destWidth</code> and
     * <code>destHeight</code>. The portion of the
     * source image copied into that square is specified
     * by <code>srcX</code>, <code>srcY</code>,
     * <code>srcWidth</code>, and srcHeight.
     */
    protected void drawImage3ByteBGR(byte[] image,
                                     float destX, float destY,
                                     float destWidth, float destHeight,
                                     float srcX, float srcY,
                                     float srcWidth, float srcHeight) {


        drawImage3ByteBGR(getPrintDC(), image,
                        destX, destY,
                        destWidth, destHeight,
                        srcX, srcY,
                        srcWidth, srcHeight);

    }


    /**
     * Begin a new page.
     */
    protected void startPage(PageFormat format, Printable painter,
                             int index) {

        /* Invalidate any device state caches we are
         * maintaining. Win95/98 resets the device
         * context attributes to default values at
         * the start of each page.
         */
        invalidateCachedState();

        deviceStartPage(format, painter, index);
    }

    /**
     * End a page.
     */
    protected void endPage(PageFormat format, Printable painter,
                           int index) {

        deviceEndPage(format, painter, index);
    }

    /**
     * Forget any device state we may have cached.
     */
    private void invalidateCachedState() {
        mLastColor = null;
        mLastTextColor = null;
        mLastFont = null;
    }

    /**
     * Set the number of copies to be printed.
     */
    public void setCopies(int copies) {
        super.setCopies(copies);
        setNativeCopies(copies);
    }


 /* Native Methods */

    /**
     * Set copies in device.
     */
    public native void setNativeCopies(int copies);

    /**
     * Displays the page setup dialog placing the user's
     * settings into 'page'.
     */
    public native boolean pageSetup(PageFormat page, Printable painter);

    /**
     * Displays the print dialog and records the user's settings
     * into this object. Return false if the user cancels the
     * dialog.
     * If the dialog is to use a set of attributes, useAttributes is true.
     */
    private native boolean jobSetup(Pageable doc, boolean allowPrintToFile);

    private static native void initIDs();

    /* Make sure printer DC is intialised and that info about the printer
     * is reflected back up to Java code
     */
    protected native void initPrinter();

    /**
     * Call Window's StartDoc routine to begin a
     * print job. The DC from the print dialog is
     * used. If the print dialog was not displayed
     * then a DC for the default printer is created.
     */
    private native void _startDoc(String dest);
    protected void startDoc() {
        _startDoc(mDestination);
    }

    /**
     * Call Window's EndDoc routine to end a
     * print job.
     */
    protected native void endDoc();

    /**
     * Call Window's AbortDoc routine to abort a
     * print job.
     */
    protected native void abortDoc();

    /**
     * Call Window's deleteDC routine to end a
     * print job.
     */
    private static native void deleteDC(long l, long l1, long l2);


    /**
     * Begin a new page. This call's Window's
     * StartPage routine.
     */
    protected native void deviceStartPage(PageFormat format, Printable painter,
                                          int index);
    /**
     * End a page. This call's Window's EndPage
     * routine.
     */
    protected native void deviceEndPage(PageFormat format, Printable painter,
                                        int index);

    /**
     * Prints the contents of the array of ints, 'data'
     * to the current page. The band is placed at the
     * location (x, y) in device coordinates on the
     * page. The width and height of the band is
     * specified by the caller.
     */
    protected native void printBand(byte[] data, int x, int y,
                                    int width, int height);

    /**
     * Begin a Window's rendering path in the device
     * context <code>printDC</code>.
     */
    protected native void beginPath(long printDC);

    /**
     * End a Window's rendering path in the device
     * context <code>printDC</code>.
     */
    protected native void endPath(long printDC);

    /**
     * Close a subpath in a Window's rendering path in the device
     * context <code>printDC</code>.
     */
    protected native void closeFigure(long printDC);

    /**
     * Fill a defined Window's rendering path in the device
     * context <code>printDC</code>.
     */
    protected native void fillPath(long printDC);

    /**
     * Move the Window's pen position to <code>(x,y)</code>
     * in the device context <code>printDC</code>.
     */
    protected native void moveTo(long printDC, float x, float y);

    /**
     * Draw a line from the current pen position to
     * <code>(x,y)</code> in the device context <code>printDC</code>.
     */
    protected native void lineTo(long printDC, float x, float y);

    protected native void polyBezierTo(long printDC,
                                       float control1x, float control1y,
                                       float control2x, float control2y,
                                       float endX, float endY);

    /**
     * Set the current polgon fill rule into the device context
     * <code>printDC</code>. The <code>fillRule</code> should
     * be one of the following Windows constants:
     * <code>ALTERNATE</code> or <code>WINDING</code>.
     */
    protected native void setPolyFillMode(long printDC, int fillRule);

    /**
     * Create a Window's solid brush for the color specified
     * by <code>(red, green, blue)</code>. Once the brush
     * is created, select it in the device
     * context <code>printDC</code> and free the old brush.
     */
    protected native void selectSolidBrush(long printDC,
                                           int red, int green, int blue);

    /**
     * Return the x coordinate of the current pen
     * position in the device context
     * <code>printDC</code>.
     */
    protected native int getPenX(long printDC);

    /**
     * Return the y coordinate of the current pen
     * position in the device context
     * <code>printDC</code>.
     */
    protected native int getPenY(long printDC);

    /**
     * Select the device context's current path
     * to be the clipping path.
     */
    protected native void selectClipPath(long printDC);

                /**
                 * Draw a rectangle using specified brush.
                 */
        protected native void frameRect(long printDC, float x, float y,
                                float width, float height);

                /**
                 * Fill a rectangle specified by the coordinates using
                 * specified brush.
                 */
        protected native void fillRect(long printDC, float x, float y,
                                float width, float height, int red, int green, int blue);

                /**
                 * Create a solid brush using the RG & B colors and width.
                 * Select this brush and delete the old one.
                 */
                protected native void selectPen(long printDC, float width,
                        int red, int green, int blue);

                /**
                 * Create a solid brush using the RG & B colors and specified
                 * pen styles.  Select this created brush and delete the old one.
                 */
        protected native boolean selectStylePen(long printDC, long cap,
                        long join, float width, int red, int green, int blue);

    /**
     * Set a GDI font capable of drawing the java logical Font
     * passed in.
     */
    protected native boolean setLogicalFont(Font font,
                                            int rotation, float awScale);

    public Frame getEmbeddedFrame() {
        java.awt.Window window =
          KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (window != null && (window instanceof EmbeddedFrame))
            return (Frame)window;
        Frame aframe[] = Frame.getFrames();
        for (int i = 0; i < aframe.length; i++)
            if (aframe[i] instanceof EmbeddedFrame)
                return aframe[i];
        return null;
    }

    /**
     * Set a GDI font capable of drawing the java Font
     * passed in.
     */
    protected native boolean setFont(long printDC, String familyName,
                                                  float fontSize,
                                                  boolean bold,
                                                  boolean italic,
                                                  int rotation,
                                                  float awScale);


    /**
     * Set the GDI color for text drawing.
     */
    protected native void setTextColor(long printDC,
                                       int red, int green, int blue);


    /**
     * Draw the string <code>text</code> into the device
     * context <code>printDC</code> at the specified
     * position.
     */
    protected native void textOut(long printDC, String text, float x, float y,
                                  Font font);


     /**
     * Draw the 24 bit BGR image buffer represented by
     * <code>image</code> to the GDI device context
     * <code>printDC</code>. The image is drawn at
     * <code>(destX, destY)</code> in device coordinates.
     * The image is scaled into a square of size
     * specified by <code>destWidth</code> and
     * <code>destHeight</code>. The portion of the
     * source image copied into that square is specified
     * by <code>srcX</code>, <code>srcY</code>,
     * <code>srcWidth</code>, and srcHeight.
     */
    protected native void drawImage3ByteBGR(long printDC, byte[] image,
                       float destX, float destY,
                       float destWidth, float destHeight,
                       float srcX, float srcY,
                       float srcWidth, float srcHeight);
}

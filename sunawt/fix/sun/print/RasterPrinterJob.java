/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)RasterPrinterJob.java    1.50 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.print;

import java.io.FilePermission;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.Pageable;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import sun.awt.image.ByteInterleavedRaster;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.Fidelity;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobSheets;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;

import sun.print.PageableDoc;
import sun.print.ServiceDialog;
import sun.print.SunPrinterJobService;

/**
 * A class which rasterizes a printer job.
 *
 * @version 1.0 26 Jan 1998
 * @author Richard Blanchard
 */
public abstract class RasterPrinterJob extends PrinterJob {

 /* Class Constants */

     /* Printer destination type. */
    protected static final int PRINTER = 0;

     /* File destination type.  */
    protected static final int FILE = 1;

    /* Stream destination type.  */
    protected static final int STREAM = 2;

    /**
     * Maximum amount of memory in bytes to use for the
     * buffered image "band". 4Mb is a compromise between
     * limiting the number of bands on hi-res printers and
     * not using too much of the Java heap or causing paging
     * on systems with little RAM.
     */
    private static final int MAX_BAND_SIZE = (1024 * 1024 * 4);

    private static final float DPI = 72f;

/* Instance Variables */

    /**
     * Used to minimise GC & reallocation of band when printing
     */
    private int cachedBandWidth = 0;
    private int cachedBandHeight = 0;
    private BufferedImage cachedBand = null;

    /**
     * The number of book copies to be printed.
     */
    private int mNumCopies = 1;

    /**
     * Collation effects the order of the pages printed
     * when multiple copies are requested. For two copies
     * of a three page document the page order is:
     *  mCollate true: 1, 2, 3, 1, 2, 3
     *  mCollate false: 1, 1, 2, 2, 3, 3
     */
    private boolean mCollate = false;

    /**
     * The zero based indices of the first and last
     * pages to be printed. If 'mFirstPage' is
     * UNDEFINED_PAGE_NUM then the first page to
     * be printed is page 0. If 'mLastPage' is
     * UNDEFINED_PAGE_NUM then the last page to
     * be printed is the last one in the book.
     */
    private int mFirstPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
    private int mLastPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;

    /**
     * The document to be printed. It is initialized to an
     * empty (zero pages) book.
     */
    private Pageable mDocument = new Book();

    /**
     * The name of the job being printed.
     */
    private String mDocName = new String("Java Printing");


    /**
     * Printing cancellation flags
     */
    private boolean performingPrinting = false;
    private boolean userCancelled = false;

   /**
    * Print to file permission variables.
    */
    private FilePermission printToFilePermission;

    /**
     * List of areas & the graphics state for redrawing
     */
    private ArrayList redrawList = new ArrayList();


    /* variables representing values extracted from an attribute set.
     * These take precedence over values set on a printer job
     */
    private int copiesAttr;
    private String jobNameAttr;
    private String userNameAttr;
    private PageRanges pageRangesAttr;
    protected Sides sidesAttr;
    protected String destinationAttr;
    protected boolean noJobSheet = false;
    protected int mDestType = RasterPrinterJob.FILE;
    protected String mDestination = "";
    protected boolean collateAttReq = false;

   /**
     * attributes used by no-args page and print dialog and print method to
     * communicate state
     */
    protected PrintRequestAttributeSet attributes = null;

    /**
     * Class to keep state information for redrawing areas
     */
    private class GraphicsState {
        Rectangle2D region;  //
        Shape theClip;
        AffineTransform theTransform;
        double sx;
        double sy;
        Rectangle2D imageSrcRect; // source part of the image
        AffineTransform imageTransform; // used to set device clip
    }

    /**
     * Service for this job
     */
    private PrintService myService;

 /* Constructors */

    public RasterPrinterJob()
    {
    }

/* Abstract Methods */

    /**
     * Returns the resolution in dots per inch across the width
     * of the page.
     */
    abstract protected double getXRes();

    /**
     * Returns the resolution in dots per inch down the height
     * of the page.
     */
    abstract protected double getYRes();

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPrintableX(Paper p);

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPrintableY(Paper p);

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPrintableWidth(Paper p);

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPrintableHeight(Paper p);

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPageWidth(Paper p);

    /**
     * Must be obtained from the current printer.
     * Value is in device pixels.
     * Not adjusted for orientation of the paper.
     */
    abstract protected double getPhysicalPageHeight(Paper p);

    /**
     * Begin a new page. This call's Window's
     * StartPage routine.
     */
    abstract protected void startPage(PageFormat format, Printable painter,
                                      int index)
        throws PrinterException;

    /**
     * End a page.
     */
    abstract protected void endPage(PageFormat format, Printable painter,
                                    int index)
        throws PrinterException;

    /**
     * Prints the contents of the array of ints, 'data'
     * to the current page. The band is placed at the
     * location (x, y) in device coordinates on the
     * page. The width and height of the band is
     * specified by the caller.
     */
    abstract protected void printBand(byte[] data, int x, int y,
                                      int width, int height)
        throws PrinterException;

/* Instance Methods */

    /**
      * save graphics state of a PathGraphics for later redrawing
      * of part of page represented by the region in that state
      */

    public void saveState(AffineTransform at, Shape clip, Rectangle2D region,
                          double sx, double sy,
                          Rectangle2D srcRect, AffineTransform xform) {
        GraphicsState gstate = new GraphicsState();
        gstate.theTransform = at;
        gstate.theClip = clip;
        gstate.region = region;
        gstate.sx = sx;
        gstate.sy = sy;
        gstate.imageTransform = xform;
        gstate.imageSrcRect = srcRect;
        redrawList.add(gstate);
    }


    /*
     * A convenience method which returns the default service
     * for 2D <code>PrinterJob</code>s.
     * May return null if there is no suitable default (although there
     * may still be 2D services available).
     * @return default 2D print service, or null.
     * @since     1.4
     */
    protected static PrintService lookupDefaultPrintService() {
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();

        /* Pageable implies Printable so checking both isn't strictly needed */
        if (service != null &&
            service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PAGEABLE) &&
            service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
            return service;
        } else {
           PrintService []services =
             PrintServiceLookup.lookupPrintServices(
                                DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
           if (services.length > 0) {
               return services[0];
           }
        }
        return null;
    }

   /**
     * Returns the service (printer) for this printer job.
     * Implementations of this class which do not support print services
     * may return null;
     * @return the service for this printer job.
     *
     */
    public PrintService getPrintService() {
        if (myService == null) {
            PrintService svc = PrintServiceLookup.lookupDefaultPrintService();
            if (svc != null &&
                svc.isDocFlavorSupported(
                     DocFlavor.SERVICE_FORMATTED.PAGEABLE)) {
                try {
                    setPrintService(svc);
                    myService = svc;
                } catch (PrinterException e) {
                }
            }
            if (myService == null) {
                PrintService[] svcs = PrintServiceLookup.lookupPrintServices(
                    DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
                if (svcs.length > 0) {
                    try {
                        setPrintService(svcs[0]);
                        myService = svcs[0];
                    } catch (PrinterException e) {
                }
                }
            }
        }
        return myService;
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

        if (service == null) {
            throw new PrinterException("Service cannot be null");
        } else if (service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PAGEABLE) &&
            service.isDocFlavorSupported(
                                DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
           myService = service;
        } else {
            throw new PrinterException("Not a 2D print service: " + service);
        }
    }

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
    public PageFormat pageDialog(PageFormat page)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        GraphicsConfiguration gc =
          GraphicsEnvironment.getLocalGraphicsEnvironment().
          getDefaultScreenDevice().getDefaultConfiguration();

        PrintService service = getPrintService();
        if (service == null) {
          ServiceDialog.showNoPrintService(gc);
          return page;
        }

        updatePageAttributes(service, page);

        PageFormat newPage = pageDialog(attributes);

        if (newPage == null) {
            return page;
        } else {
            return newPage;
        }
    }

    protected void updatePageAttributes(PrintService service,
                                        PageFormat page) {
        if (service == null || page == null)
            return;
        float x = (float)Math.rint(
                         (page.getPaper().getWidth()*Size2DSyntax.INCH)/
                         (72.0))/(float)Size2DSyntax.INCH;
        float y = (float)Math.rint(
                         (page.getPaper().getHeight()*Size2DSyntax.INCH)/
                         (72.0))/(float)Size2DSyntax.INCH;

        OrientationRequested orient;
        switch (page.getOrientation()) {
        case PageFormat.LANDSCAPE :
            orient = OrientationRequested.LANDSCAPE;
            break;
        case PageFormat.REVERSE_LANDSCAPE:
            orient = OrientationRequested.REVERSE_LANDSCAPE;
            break;
        default:
            orient = OrientationRequested.PORTRAIT;
        }

        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        attributes.add(orient);

        Media mediaArr[] = (Media[]) service.getSupportedAttributeValues(
                                        Media.class, null, null);
        Media media = null;
        try {
            media = findMedia(mediaArr, x, y, Size2DSyntax.INCH);
        } catch (IllegalArgumentException e) {
        }
        if (media == null ||
            !service.isAttributeValueSupported(media, null, null)) {
            media = (Media)service.getDefaultAttributeValue(Media.class);
        }
        if (media != null) {
            attributes.add(media);
        }

        float x2 = (float)(page.getPaper().getImageableX() / 72.0);
        float w = (float)(page.getPaper().getImageableWidth() / 72.0);
        float y2 = (float)(page.getPaper().getImageableY() / 72.0);
        float h = (float)(page.getPaper().getImageableHeight() / 72.0);
        try {
            attributes.add(new MediaPrintableArea(x2, y2, w, h,
                                                Size2DSyntax.INCH));
        } catch (IllegalArgumentException e) {
            // ignore
        }
    }

    private MediaSizeName findMedia(Media[] mediaArr, float x, float y,
                                    int units) {
        if (x <= 0 || y <= 0 || units < 1)
            throw new IllegalArgumentException("args must be +ve values");
        if (mediaArr == null || mediaArr.length == 0)
            throw new IllegalArgumentException(
                        "args must have valid array of media");

        int count = 0;
        MediaSizeName names[] = new MediaSizeName[mediaArr.length];
        for (int i = 0; i < mediaArr.length; i++) {
            if (mediaArr[i] instanceof MediaSizeName) {
                names[count++] = (MediaSizeName)mediaArr[i];
            }
        }
        if (count == 0) {
            return null;
        }

        int bestIndex = 0;
        double d = x * x + y * y;
        Object obj = null;
        for (int i = 0; i < count; i++) {
            MediaSize mediasize = MediaSize.getMediaSizeForName(names[i]);
            if (mediasize == null) {
                continue;
            }
            float[] dim = mediasize.getSize(units);
            if (x == dim[0] && y == dim[1]) {
                bestIndex = i;
                break;
            }
            float x2 = x - dim[0];
            float y2 = y - dim[1];
            double d2 = x2 * x2 + y2 * y2;
            if (d2 < d) {
                d = d2;
                bestIndex = i;
            }
        }

        return names[bestIndex];
    }

    /**
     * return a PageFormat corresponding to the updated attributes,
     * or null if the user cancelled the dialog.
     */
    public PageFormat pageDialog(final PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        PageFormat newPage =
        (PageFormat) java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {

            public Object run() {
                GraphicsConfiguration gc =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();
                Rectangle bounds = gc.getBounds();
                int x = bounds.x+bounds.width/3;
                int y = bounds.y+bounds.height/3;

                PrintService service = getPrintService();
                if (service == null) {
                  ServiceDialog.showNoPrintService(gc);
                  return null;
                }

                boolean isNewFrame = false;
                Window window =
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        getActiveWindow();
                if (!(window instanceof Dialog) &&
                    !(window instanceof Frame)) {
                    window = new Frame();
                    isNewFrame = true;
                }
                ServiceDialog pageDialog;
                if (window instanceof Frame) {
                    pageDialog = new ServiceDialog(gc, x, y, service,
                                        DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                        attributes, (Frame)window);
                } else {
                    pageDialog = new ServiceDialog(gc, x, y, service,
                                        DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                        attributes, (Dialog)window);
                }
                pageDialog.show();
                if (isNewFrame) {
                    window.dispose();
                }

                if (pageDialog.getStatus() == ServiceDialog.APPROVE) {
                    PrintRequestAttributeSet newas =
                        pageDialog.getAttributes();
                    Class amCategory = SunAlternateMedia.class;

                    if (attributes.containsKey(amCategory) &&
                        newas.containsKey(amCategory)) {
                        attributes.remove(amCategory);
                    }
                    attributes.addAll(newas);

                    PageFormat page = defaultPage();

                    OrientationRequested orient =
                        (OrientationRequested)
                        attributes.get(OrientationRequested.class);
                    int pfOrient =  PageFormat.PORTRAIT;
                    if (orient != null) {
                        if (orient == OrientationRequested.REVERSE_LANDSCAPE) {
                            pfOrient = PageFormat.REVERSE_LANDSCAPE;
                        } else if (orient == OrientationRequested.LANDSCAPE) {
                            pfOrient = PageFormat.LANDSCAPE;
                        }
                    }
                    page.setOrientation(pfOrient);

                    Media media = (Media)attributes.get(Media.class);
                    if (media == null) {
                        media =
                          (Media)service.getDefaultAttributeValue(Media.class);
                    }
                    if (!(media instanceof MediaSizeName)) {
                        media = MediaSizeName.NA_LETTER;
                    }
                    MediaSize size =
                        MediaSize.getMediaSizeForName((MediaSizeName)media);
                    if (size == null) {
                        size = MediaSize.NA.LETTER;
                    }
                    int units = (int)(Size2DSyntax.INCH / 72.0);
                    Paper paper = new Paper();
                    float dim[] = size.getSize(units);
                    double w = Math.rint(dim[0]);
                    double h = Math.rint(dim[1]);
                    paper.setSize(w, h);

                    MediaPrintableArea area =
                        (MediaPrintableArea)
                        attributes.get(MediaPrintableArea.class);
                    double ix, iw, iy, ih;

                    if (area != null) {
                        ix = Math.rint(area.getX(Size2DSyntax.INCH) * DPI);
                        iy = Math.rint(area.getY(Size2DSyntax.INCH) * DPI);
                        iw = Math.rint(area.getWidth(Size2DSyntax.INCH) * DPI);
                        ih = Math.rint(area.getHeight(Size2DSyntax.INCH) * DPI);
                    }
                    else {
                        if (w >= 72.0 * 6.0) {
                            ix = 72.0;
                            iw = w - 2 * 72.0;
                        } else {
                            ix = w / 6.0;
                            iw = w * 0.75;
                        }
                        if (h >= 72.0 * 6.0) {
                            iy = 72.0;
                            ih = h - 2 * 72.0;
                        } else {
                            iy = h / 6.0;
                            ih = h * 0.75;
                        }
                    }
                    paper.setImageableArea(ix, iy, iw, ih);
                    page.setPaper(paper);

                    return page;
                } else {
                    return null;
                }
            }
        });
        return newPage;
    }

   /**
     * Presents the user a dialog for changing properties of the
     * print job interactively.
     * The services browsable here are determined by the type of
     * service currently installed.
     * If the application installed a StreamPrintService on this
     * PrinterJob, only the available StreamPrintService (factories) are
     * browsable.
     *
     * @param attributes to store changed properties.
     * @return false if the user cancels the dialog and true otherwise.
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public boolean printDialog(final PrintRequestAttributeSet attributes)
        throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        /* A security check has already been performed in the
         * java.awt.print.printerJob.getPrinterJob method.
         * So by the time we get here, it is OK for the current thread
         * to print either to a file (from a Dialog we control!) or
         * to a chosen printer.
         *
         * We raise privilege when we put up the dialog, to avoid
         * the "warning applet window" banner.
         */
        Boolean doPrint = (Boolean)java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
            public Object run() {

              GraphicsConfiguration gc =
                GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
              PrintService service = getPrintService();
              if (service == null) {
                ServiceDialog.showNoPrintService(gc);
                return Boolean.FALSE;
              }

                PrintService[] services;
                StreamPrintServiceFactory[] spsFactories = null;
                if (service instanceof StreamPrintService) {
                    spsFactories = lookupStreamPrintServices(null);
                    services = new StreamPrintService[spsFactories.length];
                    for (int i=0; i<spsFactories.length; i++) {
                        services[i] = spsFactories[i].getPrintService(null);
                    }
                } else {
                    services = PrinterJob.lookupPrintServices();
                    if ((services == null) || (services.length == 0)) {
                      /*
                       * No services but default PrintService exists?
                       * Create services using defaultService.
                       */
                      services = new PrintService[1];
                      services[0] = service;
                    }
                }

                Rectangle bounds = gc.getBounds();
                int x = bounds.x+bounds.width/3;
                int y = bounds.y+bounds.height/3;
                PrintService newService =
                   ServiceUI.printDialog(gc, x, y,
                                         services, service,
                                         DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                         attributes);

                if (newService == null) {
                    return Boolean.FALSE;
                }

                if (!service.equals(newService)) {
                    try {
                        setPrintService(newService);
                    } catch (PrinterException e) {
                    }
                }
                return Boolean.TRUE;
            }
        });

        return doPrint.booleanValue();
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

        PrintRequestAttributeSet attributes =
            new HashPrintRequestAttributeSet();
        attributes.add(new Copies(getCopies()));
        attributes.add(new JobName(getJobName(), null));

        try {
            setAttributes(attributes);
        } catch (PrinterException e) {
        }
        this.attributes = attributes;

        boolean doPrint = printDialog(attributes);
        if (doPrint) {
            JobName jobName = (JobName)attributes.get(JobName.class);
            if (jobName != null) {
                setJobName(jobName.getValue());
            }
            Copies copies = (Copies)attributes.get(Copies.class);
            if (copies != null) {
                setCopies(copies.getValue());
            }

            Destination dest = (Destination)attributes.get(Destination.class);

            if (dest != null) {
                try {
                    mDestType = RasterPrinterJob.FILE;
                    mDestination = (new File(dest.getURI())).getPath();
                } catch (Exception e) {
                    mDestination = "out.prn";
                }
            } else {
                mDestType = RasterPrinterJob.PRINTER;
                mDestination = getPrintService().getName();
            }
            try {
                setAttributes(attributes);
            } catch (PrinterException e) {
            }
            this.attributes = attributes;
        }

        return doPrint;
    }

    /**
     * The pages in the document to be printed by this PrinterJob
     * are drawn by the Printable object 'painter'. The PageFormat
     * for each page is the default page format.
     * @param Printable Called to render each page of the document.
     */
    public void setPrintable(Printable painter) {
        setPageable(new OpenBook(defaultPage(new PageFormat()), painter));
    }

    /**
     * The pages in the document to be printed by this PrinterJob
     * are drawn by the Printable object 'painter'. The PageFormat
     * of each page is 'format'.
     * @param Printable Called to render each page of the document.
     * @param PageFormat The size and orientation of each page to
     *                   be printed.
     */
    public void setPrintable(Printable painter, PageFormat format) {
        setPageable(new OpenBook(format, painter));
        updatePageAttributes(getPrintService(), format);
    }

    /**
     * The pages in the document to be printed are held by the
     * Pageable instance 'document'. 'document' will be queried
     * for the number of pages as well as the PageFormat and
     * Printable for each page.
     * @param Pageable The document to be printed. It may not be null.
     * @exception NullPointerException the Pageable passed in was null.
     * @see PageFormat
     * @see Printable
     */
    public void setPageable(Pageable document) throws NullPointerException {
        if (document != null) {
            mDocument = document;

        } else {
            throw new NullPointerException();
        }
    }

    protected final boolean isOpenBook(Pageable pageable) {
        return pageable instanceof OpenBook;
    }

    protected void initPrinter() {
        return;
    }

    protected boolean isSupportedValue(Attribute attrval,
                                     PrintRequestAttributeSet attrset) {
        return
            (attrval != null &&
             getPrintService().isAttributeValueSupported(attrval,
                                        DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                        attrset));
    }

    /* subclasses may need to pull extra information out of the attribute set
     * They can override this method & call super.setAttributes()
     */
    protected  void setAttributes(PrintRequestAttributeSet attributes)
        throws PrinterException {

        /*  reset all values to defaults */
        setCollated(false);
        sidesAttr = null;
        pageRangesAttr = null;
        copiesAttr = 0;
        jobNameAttr = null;
        userNameAttr = null;
        destinationAttr = null;
        collateAttReq = false;

        PrintService service = getPrintService();
        if (attributes == null || service == null) {
            return;
        }

        boolean fidelity = false;
        Fidelity attrFidelity = (Fidelity)attributes.get(Fidelity.class);
        if (attrFidelity != null && attrFidelity == Fidelity.FIDELITY_TRUE) {
            fidelity = true;
        }

        if (fidelity == true) {
           AttributeSet unsupported =
               service.getUnsupportedAttributes(
                                         DocFlavor.SERVICE_FORMATTED.PAGEABLE,
                                         attributes);
           if (unsupported != null) {
               throw new PrinterException("Fidelity cannot be satisfied");
           }
        }

        /*
         * Since we have verified supported values if fidelity is true,
         * we can either ignore unsupported values, or substitute a
         * reasonable alternative
         */

        SheetCollate collateAttr =
            (SheetCollate)attributes.get(SheetCollate.class);
        if (isSupportedValue(collateAttr,  attributes)) {
            setCollated(collateAttr == SheetCollate.COLLATED);
        }

        sidesAttr = (Sides)attributes.get(Sides.class);
        if (!isSupportedValue(sidesAttr,  attributes)) {
            sidesAttr = Sides.ONE_SIDED;
        }

        pageRangesAttr =  (PageRanges)attributes.get(PageRanges.class);
        if (!isSupportedValue(pageRangesAttr, attributes)) {
            pageRangesAttr = null;
        }

        Copies copies = (Copies)attributes.get(Copies.class);
        if (isSupportedValue(copies,  attributes) ||
            (!fidelity & copies != null)) {
            copiesAttr = copies.getValue();
        } else {
            copiesAttr = getCopies();
        }

        Destination destination =
            (Destination)attributes.get(Destination.class);
        if (isSupportedValue(destination,  attributes)) {
            destinationAttr = "out.prn";
            try {
                destinationAttr = (new File(destination.getURI())).getPath();
            } catch (Exception e) {
            }
        }

        JobSheets jobSheets = (JobSheets)attributes.get(JobSheets.class);
        if (jobSheets != null) {
            noJobSheet = jobSheets == JobSheets.NONE;
        }

        JobName jobName = (JobName)attributes.get(JobName.class);
        if (isSupportedValue(jobName,  attributes) ||
            (!fidelity & jobName != null)) {
            jobNameAttr = jobName.getValue();
        } else {
            jobNameAttr = getJobName();
        }

        RequestingUserName userName =
            (RequestingUserName)attributes.get(RequestingUserName.class);
        if (isSupportedValue(userName,  attributes) ||
            (!fidelity & userName != null)) {
            userNameAttr = userName.getValue();
        } else {
            try {
                userNameAttr = getUserName();
            } catch (SecurityException e) {
                userNameAttr = "";
            }
        }

        /* OpenBook is used internally only when app uses Printable.
         * This is the case when we use the values from the attribute set.
         */
        Media media = (Media)attributes.get(Media.class);
        OrientationRequested orientReq =
           (OrientationRequested)attributes.get(OrientationRequested.class);
        MediaPrintableArea mpa =
            (MediaPrintableArea)attributes.get(MediaPrintableArea.class);
        if ((orientReq != null || media != null || mpa != null) &&
            isOpenBook(getPageable())) {

            Pageable pageable = getPageable();
            Printable printable = pageable.getPrintable(0);
            PageFormat pf = (PageFormat)pageable.getPageFormat(0).clone();
            Paper paper = pf.getPaper();

            if (isSupportedValue(orientReq, attributes) ||
                (!fidelity & orientReq != null)) {
                int orient;
                if (orientReq.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
                    orient = PageFormat.REVERSE_LANDSCAPE;
                } else if (orientReq.equals(OrientationRequested.LANDSCAPE)) {
                    orient = PageFormat.LANDSCAPE;
                } else {
                    orient = PageFormat.PORTRAIT;
                }
                pf.setOrientation(orient);
            }

            if (isSupportedValue(mpa, attributes) ||
                (!fidelity & mpa != null)) {
                float [] printableArea =
                    mpa.getPrintableArea(MediaPrintableArea.INCH);
                for (int i=0; i < printableArea.length; i++) {
                    printableArea[i] = printableArea[i] * DPI;
                }
                paper.setImageableArea(printableArea[0], printableArea[1],
                                       printableArea[2], printableArea[3]);
            }

            if (isSupportedValue(media, attributes) ||
                (!fidelity & media != null)) {
                if (media instanceof MediaSizeName) {
                    MediaSizeName msn = (MediaSizeName)media;
                    MediaSize msz = MediaSize.getMediaSizeForName(msn);
                    // temporary fix for Exception caused by TABLOID
                    // -  not listed in MediaSize
                    if ((msz == null) && (msn == MediaSizeName.TABLOID)) {
                        msz = MediaSize.Other.LEDGER;
                    }
                    if (msz != null) {
                        float paperWid =  msz.getX(MediaSize.INCH) * DPI;
                        float paperHgt =  msz.getY(MediaSize.INCH) * DPI;
                        paper.setSize(paperWid, paperHgt);
                    }
                }
            }

            pf.setPaper(paper);
            pf = validatePage(pf);
            setPrintable(printable, pf);
        } else {
            this.attributes = attributes;
        }

    }

    /*
     * Services we don't recognize as built-in services can't be
     * implemented as subclasses of PrinterJob, therefore we create
     * a DocPrintJob from their service and pass a Doc representing
     * the application's printjob
     */
    private void spoolToService(PrintRequestAttributeSet attributes)
        throws PrinterException {

        DocPrintJob job = getPrintService().createPrintJob();
        Doc doc = new PageableDoc(getPageable());
        if (attributes == null) {
            attributes = new HashPrintRequestAttributeSet();
        }
        try {
            job.print(doc, attributes);
        } catch (PrintException e) {
            throw new PrinterException(e.toString());
        }
    }

    /**
     * Prints a set of pages.
     * @exception java.awt.print.PrinterException an error in the print system
     *                                          caused the job to be aborted
     * @see java.awt.print.Book
     * @see java.awt.print.Pageable
     * @see java.awt.print.Printable
     */
    public void print() throws PrinterException {
        print(attributes);
    }

    public void print(PrintRequestAttributeSet attributes)
        throws PrinterException {

        /*
         * In the future PrinterJob will probably always dispatch
         * the print job to the PrintService.
         * This is how third party 2D Print Services will be invoked
         * when applications use the PrinterJob API.
         * However the JRE's concrete PrinterJob implementations have
         * not yet been re-worked to be implemented as standalone
         * services, and are implemented only as subclasses of PrinterJob.
         * So here we dispatch only those services we do not recognize
         * as implemented through platform subclasses of PrinterJob
         * (and this class).
         */
        PrintService psvc = getPrintService();
        if (psvc == null) {
            throw new PrinterException("No print service found.");
        }
        if ((psvc instanceof SunPrinterJobService) &&
            ((SunPrinterJobService)psvc).usesClass(getClass())) {
            setAttributes(attributes);
        } else {
            spoolToService(attributes);
            return;
        }
        /* We need to make sure that the collation and copies
         * settings are initialised */
        initPrinter();

        int numCollatedCopies = getCollatedCopies();
        int numNonCollatedCopies = getNoncollatedCopies();

        /* Get the range of pages we are to print. If the
         * last page to print is unknown, then we print to
         * the end of the document. Note that firstPage
         * and lastPage are 0 based page indices.
         */
        int numPages = mDocument.getNumberOfPages();

        int firstPage = getFirstPage();
        int lastPage = getLastPage();
        if(lastPage == Pageable.UNKNOWN_NUMBER_OF_PAGES){
            int totalPages = mDocument.getNumberOfPages();
            if (totalPages != Pageable.UNKNOWN_NUMBER_OF_PAGES) {
                lastPage = mDocument.getNumberOfPages() - 1;
            }
        }

        try {
            synchronized (this) {
                performingPrinting = true;
                userCancelled = false;
            }

            startDoc();
            if (isCancelled()) {
                cancelDoc();
            }

            /* Three nested loops iterate over the document. The outer loop
             * counts the number of collated copies while the inner loop
             * counts the number of nonCollated copies. Normally, one of
             * these two loops will only execute once; that is we will
             * either print collated copies or noncollated copies. The
             * middle loop iterates over the pages.
             * If a PageRanges attribute is used, it constrains the pages
             * that are imaged. If a platform subclass (though a user dialog)
             * requests a page range via setPageRange(). it too can
             * constrain the page ranges that are imaged.
             * It is expected that only one of these will be used in a
             * job but both should be able to co-exist.
             */
            for(int collated = 0; collated < numCollatedCopies; collated++) {
                for(int i = firstPage, pageResult = Printable.PAGE_EXISTS;
                    (i <= lastPage ||
                     lastPage == Pageable.UNKNOWN_NUMBER_OF_PAGES)
                    && pageResult == Printable.PAGE_EXISTS;
                    i++)
                {
                    if (pageRangesAttr != null) {
                        int nexti = pageRangesAttr.next(i);
                        if (nexti == -1) {
                            break;
                        } else if (nexti != i+1) {
                            continue;
                        }
                    }
                    for(int nonCollated = 0;
                        nonCollated < numNonCollatedCopies
                        && pageResult == Printable.PAGE_EXISTS;
                        nonCollated++)
                    {
                        if (isCancelled()) {
                            cancelDoc();
                        }
                        pageResult = printPage(mDocument, i);

                    }
                }
            }

            if (isCancelled()) {
                cancelDoc();
            }

        } finally {
            synchronized (this) {
                if (performingPrinting) {
                    endDoc();
                }
                performingPrinting = false;
                notify();
            }
        }
    }

    /**
     * updates a Paper object to reflect the current printer's selected
     * paper size and imageable area for that paper size.
     * Default implementation copies settings from the original, applies
     * applies some validity checks, changes them only if they are
     * clearly unreasonable, then sets them into the new Paper.
     * Subclasses are expected to override this method to make more
     * informed decisons.
     */
    protected void validatePaper(Paper origPaper, Paper newPaper) {
        if (origPaper == null || newPaper == null) {
            return;
        } else {
            double wid = origPaper.getWidth();
            double hgt = origPaper.getHeight();
            double ix = origPaper.getImageableX();
            double iy = origPaper.getImageableY();
            double iw = origPaper.getImageableWidth();
            double ih = origPaper.getImageableHeight();

            /* Assume any +ve values are legal. Overall paper dimensions
             * take precedence. Make sure imageable area fits on the paper.
             */
            Paper defaultPaper = new Paper();
            wid = ((wid > 0.0) ? wid : defaultPaper.getWidth());
            hgt = ((hgt > 0.0) ? hgt : defaultPaper.getHeight());
            ix = ((ix > 0.0) ? ix : defaultPaper.getImageableX());
            iy = ((iy > 0.0) ? iy : defaultPaper.getImageableY());
            iw = ((iw > 0.0) ? iw : defaultPaper.getImageableWidth());
            ih = ((ih > 0.0) ? ih : defaultPaper.getImageableHeight());
            /* full width/height is not likely to be imageable, but since we
             * don't know the limits we have to allow it
             */
            if (iw > wid) {
                iw = wid;
            }
            if (ih > hgt) {
                ih = hgt;
            }
            if ((ix + iw) > wid) {
                ix = wid - iw;
            }
            if ((iy + ih) > hgt) {
                iy = hgt - ih;
            }
            newPaper.setSize(wid, hgt);
            newPaper.setImageableArea(ix, iy, iw, ih);
        }
    }

    /**
     * The passed in PageFormat will be copied and altered to describe
     * the default page size and orientation of the PrinterJob's
     * current printer.
     */
    public PageFormat defaultPage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        newPage.setOrientation(PageFormat.PORTRAIT);
        Paper newPaper = new Paper();

        /* Default to A4 paper outside North America. Platform
         * subclasses which can access the actual default paper size
         * for a printer should override this method.
         */
        String defaultCountry = Locale.getDefault().getCountry();
        if (defaultCountry != null &&
            (!defaultCountry.equals(Locale.US.getCountry()) &&
             !defaultCountry.equals(Locale.CANADA.getCountry()))) {

            double mmPerInch = 25.4;
            double ptsPerInch = 72.0;
            double a4Width = Math.rint((210.0*ptsPerInch)/mmPerInch);
            double a4Height = Math.rint((297.0*ptsPerInch)/mmPerInch);
            newPaper.setSize(a4Width, a4Height);
            newPaper.setImageableArea(ptsPerInch, ptsPerInch,
                                      a4Width - 2.0*ptsPerInch,
                                      a4Height - 2.0*ptsPerInch);
        }

        newPage.setPaper(newPaper);

        return newPage;
    }

    /**
     * The passed in PageFormat is cloned and altered to be usable on
     * the PrinterJob's current printer.
     */
    public PageFormat validatePage(PageFormat page) {
        PageFormat newPage = (PageFormat)page.clone();
        Paper newPaper = new Paper();
        validatePaper(newPage.getPaper(), newPaper);
        newPage.setPaper(newPaper);

        return newPage;
    }

    /**
     * Set the number of copies to be printed.
     */
    public void setCopies(int copies) {
        mNumCopies = copies;
    }

    /**
     * Get the number of copies to be printed.
     */
    public int getCopies() {
        return mNumCopies;
    }

   /* Used when executing a print job where an attribute set may
     * over ride API values.
     */
    protected int getCopiesInt() {
        return (copiesAttr > 0) ? copiesAttr : getCopies();
    }

    /**
     * Get the name of the printing user.
     * The caller must have security permission to read system properties.
     */
    public String getUserName() {
        return System.getProperty("user.name");
    }

   /* Used when executing a print job where an attribute set may
     * over ride API values.
     */
    protected String getUserNameInt() {
        if  (userNameAttr != null) {
            return userNameAttr;
        } else {
            try {
                return  getUserName();
            } catch (SecurityException e) {
                return "";
            }
        }
    }

    /**
     * Set the name of the document to be printed.
     * The document name can not be null.
     */
    public void setJobName(String jobName) {
        if (jobName != null) {
            mDocName = jobName;
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * Get the name of the document to be printed.
     */
    public String getJobName() {
        return mDocName;
    }

    /* Used when executing a print job where an attribute set may
     * over ride API values.
     */
    protected String getJobNameInt() {
        return (jobNameAttr != null) ? jobNameAttr : getJobName();
    }

    /**
     * Set the range of pages from a Book to be printed.
     * Both 'firstPage' and 'lastPage' are zero based
     * page indices. If either parameter is less than
     * zero then the page range is set to be from the
     * first page to the last.
     */
    protected void setPageRange(int firstPage, int lastPage) {
        if(firstPage >= 0 && lastPage >= 0) {
            mFirstPage = firstPage;
            mLastPage = lastPage;
            if(mLastPage < mFirstPage) mLastPage = mFirstPage;
        } else {
            mFirstPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
            mLastPage = Pageable.UNKNOWN_NUMBER_OF_PAGES;
        }
    }

    /**
     * Return the zero based index of the first page to
     * be printed in this job.
     */
    protected int getFirstPage() {
        return mFirstPage == Book.UNKNOWN_NUMBER_OF_PAGES ? 0 : mFirstPage;
    }

    /**
     * Return the zero based index of the last page to
     * be printed in this job.
     */
    protected int getLastPage() {
        return mLastPage;
    }

    /**
     * Set whether copies should be collated or not.
     * Two collated copies of a three page document
     * print in this order: 1, 2, 3, 1, 2, 3 while
     * uncollated copies print in this order:
     * 1, 1, 2, 2, 3, 3.
     * This is set when request is using an attribute set.
     */
    protected void setCollated(boolean collate) {
        mCollate = collate;
        collateAttReq = true;
    }

    /**
     * Return true if collated copies will be printed as determined
     * in an attribute set.
     */
    protected boolean isCollated() {
            return mCollate;
    }

    /**
     * Called by the print() method at the start of
     * a print job.
     */
    protected abstract void startDoc() throws PrinterException;

    /**
     * Called by the print() method at the end of
     * a print job.
     */
    protected abstract void endDoc() throws PrinterException;

    /* Called by cancelDoc */
    protected abstract void abortDoc();

    private void cancelDoc() throws PrinterAbortException {
        abortDoc();
        synchronized (this) {
            userCancelled = false;
            performingPrinting = false;
            notify();
        }
        throw new PrinterAbortException();
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
        return isCollated() ? getCopiesInt() : 1;
    }

    /**
     * Returns how many times each page in the book
     * should be consecutively printed by PrintJob.
     * If the printer makes copies itself then this
     * method should return 1.
     */
    protected int getNoncollatedCopies() {
        return isCollated() ? 1 : getCopiesInt();
    }

    /**
     * Print a page from the provided document.
     * @return int Printable.PAGE_EXISTS if the page existed and was drawn and
     *             Printable.NO_SUCH_PAGE if the page did not exist.
     * @see java.awt.print.Printable
     */
    protected int printPage(Pageable document, int pageIndex)
        throws PrinterException
    {
        PageFormat page;
        Printable painter;
        try {
            page = document.getPageFormat(pageIndex);
            painter = document.getPrintable(pageIndex);
        } catch (Exception e) {
            throw new PrinterException("No page or printable exists.");
        }

        /* Get the imageable area from Paper instead of PageFormat
         * because we do not want it adjusted by the page orientation.
         */
        Paper paper = page.getPaper();
        double xScale = getXRes() / 72.0;
        double yScale = getYRes() / 72.0;

        /* The deviceArea is the imageable area in the printer's
         * resolution.
         */
        Rectangle2D deviceArea =
            new Rectangle2D.Double(paper.getImageableX() * xScale,
                                   paper.getImageableY() * yScale,
                                   paper.getImageableWidth() * xScale,
                                   paper.getImageableHeight() * yScale);

        /* Build and hold on to a uniform transform so that
         * we can get back to device space at the beginning
         * of each band.
         */
        AffineTransform uniformTransform = new AffineTransform();

        /* The scale transform is used to switch from the
         * device space to the user's 72 dpi space.
         */
        AffineTransform scaleTransform = new AffineTransform();
        scaleTransform.scale(xScale, yScale);

        /* bandwidth is multiple of 4 as the data is used in a win32 DIB and
         * some drivers behave badly if scanlines aren't multiples of 4 bytes.
         */
        int bandWidth = (int) deviceArea.getWidth();
        if (bandWidth % 4 != 0) {
            bandWidth += (4 - (bandWidth % 4));
        }
        if (bandWidth <= 0) {
            throw new PrinterException("Paper's imageable width is too small.");
        }

        int deviceAreaHeight = (int)deviceArea.getHeight();
        if (deviceAreaHeight <= 0) {
            throw new PrinterException("Paper's imageable height is too small.");
        }

        /* Figure out the number of lines that will fit into
         * our maximum band size. The hard coded 3 reflects the
         * fact that we can only create 24 bit per pixel 3 byte BGR
         * BufferedImages. FIX.
         */
        int bandHeight = (int)(MAX_BAND_SIZE / bandWidth / 3);

        int deviceLeft = (int)Math.rint(paper.getImageableX() * xScale);
        int deviceTop  = (int)Math.rint(paper.getImageableY() * yScale);

        /* The device transform is used to move the band down
         * the page using translates. Normally this is all it
         * would do, but since, when printing, the Window's
         * DIB format wants the last line to be first (lowest) in
         * memory, the deviceTransform moves the origin to the
         * bottom of the band and flips the origin. This way the
         * app prints upside down into the band which is the DIB
         * format.
         */
        AffineTransform deviceTransform = new AffineTransform();
        deviceTransform.translate(-deviceLeft, deviceTop);
        deviceTransform.translate(0, bandHeight);
        deviceTransform.scale(1, -1);

        /* Create a BufferedImage to hold the band. We set the clip
         * of the band to be tight around the bits so that the
         * application can use it to figure what part of the
         * page needs to be drawn. The clip is never altered in
         * this method, but we do translate the band's coordinate
         * system so that the app will see the clip moving down the
         * page though it s always around the same set of pixels.
         */
        BufferedImage pBand = new BufferedImage(1, 1,
                                                BufferedImage.TYPE_3BYTE_BGR);

        /* Have the app draw into a PeekGraphics object so we can
         * learn something about the needs of the print job.
         */

        PeekGraphics peekGraphics = createPeekGraphics(pBand.createGraphics(),
                                                       this);

        Rectangle2D.Double pageFormatArea =
            new Rectangle2D.Double(page.getImageableX(),
                                   page.getImageableY(),
                                   page.getImageableWidth(),
                                   page.getImageableHeight());
        initPrinterGraphics(peekGraphics, pageFormatArea);
        //initPrinterGraphics(peekGraphics, deviceArea);

        int pageResult = painter.print(peekGraphics, page, pageIndex);

        if (pageResult == Printable.PAGE_EXISTS) {

            startPage(page, painter, pageIndex);

            Graphics2D pathGraphics = createPathGraphics(peekGraphics, this,
                                                         painter, page,
                                                         pageIndex);

            /* If we can convert the page directly to the
             * underlying graphics system then we do not
             * need to rasterize. We also may not need to
             * create the 'band' if all the pages can take
             * this path.
             */
            if (pathGraphics != null) {
                pathGraphics.transform(scaleTransform);
                // user (0,0) should be origin of page, not imageable area
                pathGraphics.translate(-getPhysicalPrintableX(paper) / xScale,
                                       -getPhysicalPrintableY(paper) / yScale);
                pathGraphics.transform(new AffineTransform(page.getMatrix()));
                initPrinterGraphics(pathGraphics, pageFormatArea);

                redrawList.clear();

                painter.print(pathGraphics, page, pageIndex);

                for (int i=0;i<redrawList.size();i++) {
                   GraphicsState gstate = (GraphicsState)redrawList.get(i);
                   pathGraphics.setTransform(gstate.theTransform);
                   pathGraphics.setClip(gstate.theClip);
                   ((PathGraphics)pathGraphics).redrawRegion(
                                                         gstate.region,
                                                         gstate.sx,
                                                         gstate.sy,
                                                         gstate.imageSrcRect,
                                                         gstate.imageTransform);
                }

            /* This is the banded-raster printing loop.
             * It should be moved into its own method.
             */
            } else {
                BufferedImage band = cachedBand;
                if (cachedBand == null ||
                    bandWidth != cachedBandWidth ||
                    bandHeight != cachedBandHeight) {
                    band = new BufferedImage(bandWidth, bandHeight,
                                             BufferedImage.TYPE_3BYTE_BGR);
                    cachedBand = band;
                    cachedBandWidth = bandWidth;
                    cachedBandHeight = bandHeight;
                }
                Graphics2D bandGraphics = band.createGraphics();

                Rectangle2D.Double clipArea =
                    new Rectangle2D.Double(0, 0, bandWidth, bandHeight);

                initPrinterGraphics(bandGraphics, clipArea);

                ProxyGraphics2D painterGraphics =
                    new ProxyGraphics2D(bandGraphics, this);

                Graphics2D clearGraphics = band.createGraphics();
                clearGraphics.setColor(Color.white);

                /* We need the actual bits of the BufferedImage to send to
                 * the native Window's code. 'data' points to the actual
                 * pixels. Right now these are in ARGB format with 8 bits
                 * per component. We need to use a monochrome BufferedImage
                 * for monochrome printers when this is supported by
                 * BufferedImage. FIX
                 */
                ByteInterleavedRaster tile = (ByteInterleavedRaster)band.getRaster();
                byte[] data = tile.getDataStorage();

                /* Loop over the page moving our band down the page,
                 * calling the app to render the band, and then send the band
                 * to the printer.
                 */
                int deviceBottom = deviceTop + deviceAreaHeight;

                /* device's printable x,y is really addressable origin
                 * we address relative to media origin so when we print a
                 * band we need to adjust for the different methods of
                 * addressing it.
                 */
                int deviceAddressableX = (int)getPhysicalPrintableX(paper);
                int deviceAddressableY = (int)getPhysicalPrintableY(paper);

                for (int bandTop = 0; bandTop <= deviceAreaHeight;
                     bandTop += bandHeight)
                {

                    /* Put the band back into device space and
                     * erase the contents of the band.
                     */
                    clearGraphics.fillRect(0, 0, bandWidth, bandHeight);

                    /* Put the band into the correct location on the
                     * page. Once the band is moved we translate the
                     * device transform so that the band will move down
                     * the page on the next iteration of the loop.
                     */
                    bandGraphics.setTransform(uniformTransform);
                    bandGraphics.transform(deviceTransform);
                    deviceTransform.translate(0, -bandHeight);

                    /* Switch the band from device space to user,
                     * 72 dpi, space.
                     */
                    bandGraphics.transform(scaleTransform);
                    bandGraphics.transform(new AffineTransform(page.getMatrix()));

                    Rectangle clip = bandGraphics.getClipBounds();

                    if ((clip == null) || peekGraphics.hitsDrawingArea(clip) &&
                        (bandWidth > 0 && bandHeight > 0)) {

                        /* if the client has specified an imageable X or Y
                         * which is off than the physically addressable
                         * area of the page, then we need to adjust for that
                         * here so that we pass only non -ve band coordinates
                         * We also need to translate by the adjusted amount
                         * so that printing appears in the correct place.
                         */
                        int bandX = deviceLeft - deviceAddressableX;
                        if (bandX < 0) {
                            bandGraphics.translate(bandX/xScale,0);
                            bandX = 0;
                        }
                        int bandY = deviceTop + bandTop - deviceAddressableY;
                        if (bandY < 0) {
                            bandGraphics.translate(0,bandY/yScale);
                            bandY = 0;
                        }
                        /* Have the app's painter image into the band
                         * and then send the band to the printer.
                         */
                        painterGraphics.setDelegate((Graphics2D) bandGraphics.create());
                        painter.print(painterGraphics, page, pageIndex);
                        painterGraphics.dispose();
                        printBand(data, bandX, bandY, bandWidth, bandHeight);
                    }
                }

                clearGraphics.dispose();
                bandGraphics.dispose();

            }
            endPage(page, painter, pageIndex);
        }

        return pageResult;
    }

    /**
     * If a print job is in progress, print() has been
     * called but has not returned, then this signals
     * that the job should be cancelled and the next
     * chance. If there is no print job in progress then
     * this call does nothing.
     */
    public void cancel() {
        synchronized (this) {
            if (performingPrinting) {
                userCancelled = true;
            }
            notify();
        }
    }

    /**
     * Returns true is a print job is ongoing but will
     * be cancelled and the next opportunity. false is
     * returned otherwise.
     */
    public boolean isCancelled() {

        boolean cancelled = false;

        synchronized (this) {
            cancelled = (performingPrinting && userCancelled);
            notify();
        }

        return cancelled;
    }

    /**
     * Return the Pageable describing the pages to be printed.
     */
    protected Pageable getPageable() {
        return mDocument;
    }

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
    protected Graphics2D createPathGraphics(PeekGraphics graphics,
                                            PrinterJob printerJob,
                                            Printable painter,
                                            PageFormat pageFormat,
                                            int pageIndex) {

        return null;
    }

    /**
     * Create and return an object that will
     * gather and hold metrics about the print
     * job. This method is passed a <code>Graphics2D</code>
     * object that can be used as a proxy for the
     * object gathering the print job matrics. The
     * method is also supplied with the instance
     * controlling the print job, <code>printerJob</code>.
     */
    protected PeekGraphics createPeekGraphics(Graphics2D graphics,
                                              PrinterJob printerJob) {

        return new PeekGraphics(graphics, printerJob);
    }

    /**
     * Configure the passed in Graphics2D so that
     * is contains the defined initial settings
     * for a print job. These settings are:
     *      color:  black.
     *      clip:   <as passed in>
     */
    void initPrinterGraphics(Graphics2D g, Rectangle2D clip) {

        g.setClip(clip);
        g.setPaint(Color.black);
    }


   /**
    * User dialogs should disable "File" buttons if this returns false.
    *
    */
    public boolean checkAllowedToPrintToFile() {
        try {
            throwPrintToFile();
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Break this out as it may be useful when we allow API to
     * specify printing to a file. In that case its probably right
     * to throw a SecurityException if the permission is not granted
     */
    private void throwPrintToFile() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (printToFilePermission == null) {
                printToFilePermission =
                    new FilePermission("<<ALL FILES>>", "read,write");
            }
            security.checkPermission(printToFilePermission);
        }
    }

}

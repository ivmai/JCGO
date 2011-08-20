/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)PrintJob2D.java  1.13 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Copyright 1998, 1999, 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 */

package sun.print;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.JobAttributes;
import java.awt.PrintJob;
import java.awt.PageAttributes;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Properties;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.ResolutionSyntax;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.SheetCollate;
import javax.print.attribute.standard.Sides;

import sun.awt.print.PrintDialog;
import sun.awt.print.AwtPrintControl;

/**
 * A class which initiates and executes a print job using
 * the underlying PrinterJob graphics conversions.
 *
 * @see Toolkit#getPrintJob
 *
 */
public class PrintJob2D extends PrintJob implements Printable, Runnable {

    private Frame frame;
    private String docTitle = "";
    private JobAttributes jobAttributes;
    private PageAttributes pageAttributes;
    private PrintRequestAttributeSet attributes;

    /*
     * Displays the native or cross-platform dialog and allows the
     * user to update job & page attributes
     */
    private AwtPrintControl printControl;

    /**
     * The PrinterJob being uses to implement the PrintJob.
     */
    private PrinterJob printerJob;

    /**
     * The size of the page being used for the PrintJob.
     */
    private PageFormat pageFormat;

    /**
     * The PrinterJob and the application run on different
     * threads and communicate through a pair of message
     * queues. This queue is the list of Graphics that
     * the PrinterJob has requested rendering for, but
     * for which the application has not yet called getGraphics().
     * In practice the length of this message queue is always
     * 0 or 1.
     */
    private MessageQ graphicsToBeDrawn = new MessageQ("tobedrawn");

    /**
     * Used to communicate between the application's thread
     * and the PrinterJob's thread this message queue holds
     * the list of Graphics into which the application has
     * finished drawing, but that have not yet been returned
     * to the PrinterJob thread. Again, in practice, the
     * length of this message queue is always 0 or 1.
     */
    private final MessageQ graphicsDrawn = new MessageQ("drawn");

    /**
     * The last Graphics returned to the application via
     * getGraphics. This is the Graphics into which the
     * application is currently drawing.
     */
    private Graphics2D currentGraphics;

    /**
     * The zero based index of the page currently being rendered
     * by the application.
     */
    private int pageIndex = -1;

    /**
     * The thread on which PrinterJob is running.
     * This is different than the applications thread.
     */
    private Thread printerJobThread;

    public PrintJob2D(Frame frame,  String doctitle,
                      final Properties props) {

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }

        if (frame == null) {
            throw new NullPointerException("Frame must not be null");
        }
        this.docTitle = (doctitle == null) ? "" : docTitle;
        printControl = new AwtPrintControl(frame, doctitle, props);
    }

    public PrintJob2D(Frame frame,  String doctitle,
                      JobAttributes jobAttributes,
                      PageAttributes pageAttributes) {

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPrintJobAccess();
        }

        if (frame == null &&
            (jobAttributes == null ||
             jobAttributes.getDialog() == JobAttributes.DialogType.NATIVE)) {
            throw new NullPointerException("Frame must not be null");
        }
        this.docTitle = (doctitle == null) ? "" : docTitle;
        this.jobAttributes = jobAttributes;
        this.pageAttributes = pageAttributes;
        printControl = new AwtPrintControl(frame, doctitle,
                                           jobAttributes, pageAttributes);
    }

    public boolean printDialog() {

        boolean proceedWithPrint = printControl.displayDialog();

        if (proceedWithPrint) {
            jobAttributes = printControl.getJobAttributes();
            pageAttributes = printControl.getPageAttributes();
            printerJob = PrinterJob.getPrinterJob();
            if (printerJob == null) {
                return false;
            }
            copyAttributes();
            if (pageFormat == null) {
                pageFormat = printerJob.defaultPage();
            }
            printerJob.setPrintable(this, pageFormat);
        }
        return proceedWithPrint;
    }

    private PrintService findNamedPrintService(String printerName) {

        PrintService service = printerJob.getPrintService();
        if (service != null && printerName.equals(service.getName())) {
            return service;
        } else {
            PrintService []services = PrinterJob.lookupPrintServices();
            for (int i=0; i<services.length; i++) {
                if (printerName.equals(services[i].getName())) {
                    return services[i];
                }
            }
        }
        return null;
    }

    /* From JobAttributes we will copy job name and duplex printing
     * and destination.
     * The majority of the rest of the attributes are reflected
     * attributes.
     *
     * From PageAttributes we copy color, media size, orientation,
     * origin type, resolution and print quality.
     * We use the media, orientation in creating the page format, and
     * the origin type to set its imageable area.
     *
     * REMIND: Interpretation of resolution, additional media sizes.
     */
    private void copyAttributes() {

        attributes = new HashPrintRequestAttributeSet();
        printerJob.setJobName(docTitle);
        attributes.add(new JobName(docTitle, null));

        JobAttributes.DestinationType dest = jobAttributes.getDestination();
        if (dest == JobAttributes.DestinationType.PRINTER) {
            String printerName = jobAttributes.getPrinter();
            if (printerName != null && printerName != "") {
                PrintService service = findNamedPrintService(printerName);
                if (service != null) {
                    try {
                        printerJob.setPrintService(service);
                    } catch (PrinterException e) {
                    }
                }
            }
        } else {
            String fileName = jobAttributes.getFileName();
            if (fileName == null) {
                fileName = "out.prn";
            }
            URI uri = (new File(fileName)).toURI();
            attributes.add(new Destination(uri));
        }
        JobAttributes.SidesType sType = jobAttributes.getSides();
        if (sType == JobAttributes.SidesType.TWO_SIDED_LONG_EDGE) {
            attributes.add(Sides.TWO_SIDED_LONG_EDGE);
        } else if (sType == JobAttributes.SidesType.TWO_SIDED_SHORT_EDGE) {
            attributes.add(Sides.TWO_SIDED_SHORT_EDGE);
        } else if (sType == JobAttributes.SidesType.ONE_SIDED) {
            attributes.add(Sides.ONE_SIDED);
        }

        JobAttributes.MultipleDocumentHandlingType hType =
          jobAttributes.getMultipleDocumentHandling();
        if (hType ==
            JobAttributes.MultipleDocumentHandlingType.SEPARATE_DOCUMENTS_COLLATED_COPIES) {
          attributes.add(SheetCollate.COLLATED);
        } else {
          attributes.add(SheetCollate.UNCOLLATED);
        }

        attributes.add(new Copies(jobAttributes.getCopies()));

        if (pageAttributes.getColor() == PageAttributes.ColorType.COLOR) {
            attributes.add(Chromaticity.COLOR);
        } else {
            attributes.add(Chromaticity.MONOCHROME);
        }

        pageFormat = printerJob.defaultPage();
        if (pageAttributes.getOrientationRequested() ==
            PageAttributes.OrientationRequestedType.LANDSCAPE) {
            pageFormat.setOrientation(PageFormat.LANDSCAPE);
        }

        int []mSize = AwtPrintControl.getSize(pageAttributes.getMedia());
        Paper paper = new Paper();
        paper.setSize(mSize[0], mSize[1]);
        if (pageAttributes.getOrigin()==PageAttributes.OriginType.PRINTABLE) {
            // AWT uses 1/4" borders by default
            paper.setImageableArea(18.0, 18.0,
                                   paper.getWidth()-36.0,
                                   paper.getHeight()-36.0);
        } else {
            paper.setImageableArea(0.0,0.0,paper.getWidth(),paper.getHeight());
        }
        pageFormat.setPaper(paper);

        PageAttributes.PrintQualityType qType =
            pageAttributes.getPrintQuality();
        if (qType == PageAttributes.PrintQualityType.DRAFT) {
            attributes.add(PrintQuality.DRAFT);
        } else if (qType == PageAttributes.PrintQualityType.NORMAL) {
            attributes.add(PrintQuality.NORMAL);
        } else if (qType == PageAttributes.PrintQualityType.HIGH) {
            attributes.add(PrintQuality.HIGH);
        }
    }

    /**
     * Gets a Graphics object that will draw to the next page.
     * The page is sent to the printer when the graphics
     * object is disposed.  This graphics object will also implement
     * the PrintGraphics interface.
     * @see PrintGraphics
     */
    public Graphics getGraphics() {

        Graphics printGraphics = null;

        synchronized (this) {
            ++pageIndex;

            if (pageIndex == 0) {

            /* We start a thread on which the PrinterJob will run.
             * The PrinterJob will ask for pages on that thread
             * and will use a message queue to fulfill the application's
             * requests for a Graphics on the application's
             * thread.
             */

                startPrinterJobThread();

            }
            notify();
        }

        /* If the application has already been handed back
         * a graphics then we need to put that graphics into
         * the drawn queue so that the PrinterJob thread can
         * return to the print system.
         */
        if (currentGraphics != null) {
            graphicsDrawn.append(currentGraphics);
            currentGraphics = null;
        }

        /* We'll block here until a new graphics becomes
         * available.
         */

        currentGraphics = graphicsToBeDrawn.pop();

        if (currentGraphics instanceof PeekGraphics) {
            ( (PeekGraphics) currentGraphics).setAWTDrawingOnly();
            graphicsDrawn.append(currentGraphics);
            currentGraphics = graphicsToBeDrawn.pop();
        }


        if (currentGraphics != null) {

            /* In the PrintJob API, the origin is at the upper-
             * left of the imageable area when using the new "printable"
             * origin attribute, otherwise its the physical origin (for
             * backwards compatibility. We emulate this by createing
             * a PageFormat which matches and then performing the
             * translate to the origin. This is a no-op if physical
             * origin is specified.
             */
            currentGraphics.translate(pageFormat.getImageableX(),
                                      pageFormat.getImageableY());

            /* Scale to accomodate AWT's notion of printer resolution */
            double awtScale = 72.0/getPageResolutionInternal();
            currentGraphics.scale(awtScale, awtScale);

            /* The caller wants a Graphics instance but we do
             * not want them to make 2D calls. We can't hand
             * back a Graphics2D. The returned Graphics also
             * needs to implement PrintGraphics, so we wrap
             * the Graphics2D instance. The PrintJob API has
             * the application dispose of the Graphics so
             * we create a copy of the one returned by PrinterJob.
             */
            printGraphics = new ProxyPrintGraphics(currentGraphics.create(),
                                                   this);

        }

        return printGraphics;
    }

    /**
     * Returns the dimensions of the page in pixels.
     * The resolution of the page is chosen so that it
     * is similar to the screen resolution.
     * Except (since 1.3) when the application specifies a resolution.
     * In that case it it scaled accordingly.
     */
    public Dimension getPageDimension() {
        double wid, hgt, scale;
        if (pageAttributes != null &&
            pageAttributes.getOrigin()==PageAttributes.OriginType.PRINTABLE) {
            wid = pageFormat.getImageableWidth();
            hgt = pageFormat.getImageableHeight();
        } else {
            wid = pageFormat.getWidth();
            hgt = pageFormat.getHeight();
        }
        scale = getPageResolutionInternal() / 72.0;
        return new Dimension((int)(wid * scale), (int)(hgt * scale));
    }

     private double getPageResolutionInternal() {
        if (pageAttributes != null) {
            int []res = pageAttributes.getPrinterResolution();
            if (res[2] == 3) {
                return res[0];
            } else /* if (res[2] == 4) */ {
                return (res[0] * 2.54);
            }
        } else {
            return 72.0;
        }
    }

    /**
     * Returns the resolution of the page in pixels per inch.
     * Note that this doesn't have to correspond to the physical
     * resolution of the printer.
     */
    public int getPageResolution() {
        return (int)getPageResolutionInternal();
    }

    /**
     * Returns true if the last page will be printed first.
     */
    public boolean lastPageFirst() {
        return false;
    }

    /**
     * Ends the print job and does any necessary cleanup.
     */
    public synchronized void end() {
        if (currentGraphics == null)
            return;

        graphicsDrawn.append(currentGraphics);

        /* Close the message queues so that nobody is stuck
         * waiting for one.
         */
        graphicsToBeDrawn.close();
        graphicsDrawn.closeWhenEmpty();

        if (printerJobThread != null && printerJobThread.isAlive()) {
            try {
                printerJobThread.join();
            }
            catch (InterruptedException e) {
            }
        }
        currentGraphics = null;
    }

    /**
     * Ends this print job once it is no longer referenced.
     * @see #end
     */
    public void finalize() {
        end();
    }

    /**
     * Prints the page at the specified index into the specified
     * {@link Graphics} context in the specified
     * format.  A <code>PrinterJob</code> calls the
     * <code>Printable</code> interface to request that a page be
     * rendered into the context specified by
     * <code>graphics</code>.  The format of the page to be drawn is
     * specified by <code>pageFormat</code>.  The zero based index
     * of the requested page is specified by <code>pageIndex</code>.
     * If the requested page does not exist then this method returns
     * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
     * The <code>Graphics</code> class or subclass implements the
     * {@link PrinterGraphics} interface to provide additional
     * information.  If the <code>Printable</code> object
     * aborts the print job then it throws a {@link PrinterException}.
     * @param graphics the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     * @return PAGE_EXISTS if the page is rendered successfully
     *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
     *         non-existent page.
     * @exception java.awt.print.PrinterException
     *         thrown when the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
                 throws PrinterException {

        int result;

        /* This method will be called by the PrinterJob on a thread other
         * that the application's thread. We hold on to the graphics
         * until we can rendevous with the application's thread and
         * hand over the graphics. The application then does all the
         * drawing. When the application is done drawing we rendevous
         * again with the PrinterJob thread and release the Graphics
         * so that it knows we are done.
         */

        /* Add the graphics to the message queue of graphics to
         * be rendered. This is really a one slot queue. The
         * application's thread will come along and remove the
         * graphics from the queue when the app asks for a graphics.
         */
        graphicsToBeDrawn.append( (Graphics2D) graphics);

        /* We now wait for the app's thread to finish drawing on
         * the Graphics. This thread will sleep until the application
         * release the graphics by placing it in the graphics drawn
         * message queue. If the application signals that it is
         * finished drawing the entire document then we'll get null
         * returned when we try and pop a finished graphic.
         */
        if (graphicsDrawn.pop() != null) {
            result = PAGE_EXISTS;
        } else {
            result = NO_SUCH_PAGE;
        }

        return result;
    }

    private void startPrinterJobThread() {

        printerJobThread = new Thread(this, "printerJobThread");
        printerJobThread.start();
    }


    public void run() {

        try {
            printerJob.print(attributes);
        } catch (PrinterException e) {
            //REMIND: need to store this away and not rethrow it.
        }

        /* Close the message queues so that nobody is stuck
         * waiting for one.
         */
        graphicsToBeDrawn.closeWhenEmpty();
        graphicsDrawn.close();
    }

    private class MessageQ {

        private String qid="noname";

        private ArrayList queue = new ArrayList();

        MessageQ(String id) {
          qid = id;
        }

        synchronized void closeWhenEmpty() {

            while (queue != null && queue.size() > 0) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    // do nothing.
                }
            }

            queue = null;
            notifyAll();
        }

        synchronized void close() {
            queue = null;
            notifyAll();
        }

        synchronized boolean append(Graphics2D g) {

            boolean queued = false;

            if (queue != null) {
                queue.add(g);
                queued = true;
                notify();
            }

            return queued;
        }

        synchronized Graphics2D pop() {
            Graphics2D g = null;

            while (g == null && queue != null) {

                if (queue.size() > 0) {
                    g = (Graphics2D) queue.remove(0);
                    notify();

                } else {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                        // do nothing.
                    }
                }
            }

            return g;
        }

    }

}

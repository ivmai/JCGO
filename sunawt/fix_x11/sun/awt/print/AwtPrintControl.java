/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)AwtPrintControl.java     1.27 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.print;

import java.awt.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.awt.JobAttributes.*;
import java.awt.PageAttributes.*;

/**
 * A class which presents a modal print dialog to the user.
 *
 * @version     1.27 01/23/03
 * @author      Amy Fowler
 * @author      David Mendenhall
 */
public class AwtPrintControl extends sun.awt.print.PrintControl {

    private static String[] sortedPrinterList = null;
    private static long lastPrinterListQueryMillis = 0;
    private static final long QUERY_INTERVAL = 900000L; // 15 minutes

    // The following Strings are maintained for backward-compatibility with
    // Properties based print control.
    private final static String DEST_PROP = "awt.print.destination";
    private final static String PRINTER = "printer";
    private final static String FILE = "file";

    private final static String PRINTER_PROP = "awt.print.printer";

    private final static String FILENAME_PROP = "awt.print.fileName";

    private final static String NUMCOPIES_PROP = "awt.print.numCopies";

    private final static String OPTIONS_PROP = "awt.print.options";

    private final static String ORIENT_PROP = "awt.print.orientation";
    private final static String PORTRAIT = "portrait";
    private final static String LANDSCAPE = "landscape";

    private final static String PAPERSIZE_PROP = "awt.print.paperSize";
    private final static String LETTER = "letter";
    private final static String LEGAL = "legal";
    private final static String EXECUTIVE = "executive";
    private final static String A4 = "a4";

    private Properties props;

    public AwtPrintControl(Frame dialogOwner, String doctitle,
                          JobAttributes jobAttributes,
                          PageAttributes pageAttributes) {
        super(dialogOwner, doctitle, jobAttributes, pageAttributes);
    }
    public AwtPrintControl(Frame dialogOwner, String doctitle,
                          Properties props) {
        super(dialogOwner, doctitle, null, null);
        this.props = props;
        translateInputProps();
    }

    public String getDefaultPrinterName() {
        String defaultPrinter = "lp";

        try {
            final String[] cmd = new String[3];
            String osName = System.getProperty("os.name");
            cmd[1] = "-c";
            if(osName.indexOf("Linux") != -1) {
                cmd[0] = "/bin/sh";
                cmd[2] =
                 "/usr/sbin/lpc status | grep : | sed -ne '1,1 s/://p'";
            } else if (osName.equals("FreeBSD") || osName.equals("NetBSD") ||
                       osName.equals("OpenBSD") ||
                       osName.equals("DragonFly") || osName.equals("BSD/OS")) {
                cmd[0] = "/bin/sh";
                cmd[2] =
                 "/usr/sbin/lpc status all | grep : | sed -ne '1,1 s/://p'";
            } else {
                cmd[0] = "/usr/bin/sh";
                cmd[2] =
                 "/usr/bin/lpstat -d|/usr/bin/expand|/usr/bin/cut -f4 -d' '";
            }
            Process lpstat;
            try {
                lpstat = (Process)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            return Runtime.getRuntime().exec(cmd);
                        }
                });
            } catch (PrivilegedActionException e) {
                throw (IOException)e.getException();
            }
            lpstat.waitFor();
            if (lpstat.exitValue() == 0) {
                InputStream stream = lpstat.getInputStream();
                byte[] streamBytes = new byte[stream.available()];
                stream.read(streamBytes);
                defaultPrinter = new String(streamBytes).trim();
            }
        } catch (IOException e) {
            // just return "lp"
        } catch (InterruptedException e) {
            // just return "lp"
        }
        return defaultPrinter;
    }

    public boolean getCapabilities(PrinterCapabilities capabilities) {
        capabilities.setCapabilities(PrinterCapabilities.COLOR |
                                     PrinterCapabilities.DUPLEX);
        return true;
    }

    public void getPrinterList(final PrinterListUpdatable updatable) {
        long currentTimeMillis = System.currentTimeMillis();
        if (sortedPrinterList != null &&
            currentTimeMillis - lastPrinterListQueryMillis < QUERY_INTERVAL) {
            updatable.updatePrinterList(sortedPrinterList);
            return;
        }

        lastPrinterListQueryMillis = currentTimeMillis;

        new Thread(new Runnable() {
            public void run() {
                String[] sortedNames = null;

                try {
                    final String[] cmd = new String[3];
                    String osName = System.getProperty("os.name");
                    cmd[1] = "-c";
                    if(osName.indexOf("Linux") != -1) {
                        cmd[0] = "/bin/sh";
                        cmd[2] =
                         "/usr/sbin/lpc status | grep : | sed -e 's/://'";
                    } else if (osName.equals("FreeBSD") ||
                               osName.equals("NetBSD") ||
                               osName.equals("OpenBSD") ||
                               osName.equals("DragonFly") ||
                               osName.equals("BSD/OS")) {
                        cmd[0] = "/bin/sh";
                        cmd[2] = "/usr/sbin/lpc status all | grep : |" +
                                  " sed -ne '1,1 s/://p'";
                    } else {
                        cmd[0] = "/usr/bin/sh";
                        cmd[2] =
                         "/usr/bin/lpstat -v|/usr/bin/expand|" +
                         "/usr/bin/cut -f3 -d' '|/usr/bin/cut -f1 -d':'|" +
                         "/usr/bin/sort -u";
                    }
                    Process lpstat;
                    try {
                        lpstat = (Process)AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                                public Object run() throws IOException {
                                    return Runtime.getRuntime().exec(cmd);
                                }
                        });
                    } catch (PrivilegedActionException e) {
                        throw (IOException)e.getException();
                    }
                    lpstat.waitFor();
                    if (lpstat.exitValue() == 0) {
                        InputStream stream = lpstat.getInputStream();
                        byte[] streamBytes;
                        String stdout = "";
                        while (stream.available() > 0) {
                            streamBytes = new byte[stream.available()];
                            stream.read(streamBytes);
                            stdout = stdout + new String(streamBytes);
                        }
                        StringTokenizer tokens =
                            new StringTokenizer(stdout);
                        int total = tokens.countTokens();
                        sortedNames = new String[total];
                        for (int i = 0; i < total; i++) {
                            sortedNames[i] = tokens.nextToken();
                        }
                    }
                }
                catch (IOException e) {
                    // just return null
                } catch (InterruptedException e) {
                    // just return null
                }

                sortedPrinterList = sortedNames;
                updatable.updatePrinterList(sortedPrinterList);
            }
        }).start();
    }

    private void translateInputProps() {
        if (props == null) {
            return;
        }

        String str;

        str = props.getProperty(DEST_PROP);
        if (str != null) {
            if (str.equals(PRINTER)) {
                setDestAttrib(DestinationType.PRINTER);
            } else if (str.equals(FILE)) {
                setDestAttrib(DestinationType.FILE);
            }
        }
        str = props.getProperty(PRINTER_PROP);
        if (str != null) {
            setPrinterAttrib(str);
        }
        str = props.getProperty(FILENAME_PROP);
        if (str != null) {
            setFileNameAttrib(str);
        }
        str = props.getProperty(NUMCOPIES_PROP);
        if (str != null) {
            setCopiesAttrib(Integer.parseInt(str));
        }
        str = props.getProperty(OPTIONS_PROP);
        if (str != null) {
            setOptions(str);
        }
        str = props.getProperty(ORIENT_PROP);
        if (str != null) {
            if (str.equals(PORTRAIT)) {
                setOrientAttrib(OrientationRequestedType.PORTRAIT);
            } else if (str.equals(LANDSCAPE)) {
                setOrientAttrib(OrientationRequestedType.LANDSCAPE);
            }
        }
        str = props.getProperty(PAPERSIZE_PROP);
        if (str != null) {
            if (str.equals(LETTER)) {
                setMediaAttrib(MediaType.LETTER.hashCode());
            } else if (str.equals(LEGAL)) {
                setMediaAttrib(MediaType.LEGAL.hashCode());
            } else if (str.equals(EXECUTIVE)) {
                setMediaAttrib(MediaType.EXECUTIVE.hashCode());
            } else if (str.equals(A4)) {
                setMediaAttrib(MediaType.A4.hashCode());
            }
        }
    }

    private void translateOutputProps() {
        if (props == null) {
            return;
        }

        String str;

        props.setProperty(DEST_PROP,
            (getDestAttrib() == DestinationType.PRINTER) ? PRINTER : FILE);
        str = getPrinterAttrib();
        if (str != null && !str.equals("")) {
            props.setProperty(PRINTER_PROP, getPrinterAttrib());
        }
        str = getFileNameAttrib();
        if (str != null && !str.equals("")) {
            props.setProperty(FILENAME_PROP, str);
        }
        int copies = getCopiesAttrib();
        if (copies > 0) {
            props.setProperty(NUMCOPIES_PROP, "" + copies);
        }
        str = getOptions();
        if (str != null && !str.equals("")) {
            props.setProperty(OPTIONS_PROP, str);
        }
        props.setProperty(ORIENT_PROP,
            (getOrientAttrib() == OrientationRequestedType.PORTRAIT)
                ? PORTRAIT : LANDSCAPE);
        MediaType media = sun.awt.print.PrintControl.SIZES[getMediaAttrib()];
        if (media == MediaType.LETTER) {
            str = LETTER;
        } else if (media == MediaType.LEGAL) {
            str = LEGAL;
        } else if (media == MediaType.EXECUTIVE) {
            str = EXECUTIVE;
        } else if (media == MediaType.A4) {
            str = A4;
        } else {
            str = media.toString();
        }
        props.setProperty(PAPERSIZE_PROP, str);
    }

    protected void createAttributes(PrintDialog dialog) {
        super.createAttributes(dialog);
        translateOutputProps();
    }
}

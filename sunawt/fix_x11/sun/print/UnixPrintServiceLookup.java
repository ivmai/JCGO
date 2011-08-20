/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)UnixPrintServiceLookup.java      1.13 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.print;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.io.File;
import java.io.FileReader;

/*
 * Remind: This class uses solaris commands. We also need a linux
 * version
 */
public class UnixPrintServiceLookup extends PrintServiceLookup
    implements BackgroundServiceLookup, Runnable {

    /* Remind: the current implementation is static, as its assumed
     * its preferable to minimise creation of PrintService instances.
     * Later we should add logic to add/remove services on the fly which
     * will take a hit of needing to regather the list of services.
     */
    private String defaultPrinter;
    private PrintService defaultPrintService;
    private String[] printers; /* excludes the default printer */
    private PrintService[] printServices; /* includes the default printer */
    private Vector lookupListeners = null;

    static final String osname =
        (String)java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("os.name"));

    static boolean isSysV() {
        return osname.indexOf("SunOS") != -1 ||
                osname.indexOf("Solaris") != -1;
    }

    static boolean isBSD() {
        return osname.indexOf("Linux") != -1 || osname.equals("FreeBSD") ||
                osname.equals("NetBSD") || osname.equals("OpenBSD") ||
                osname.equals("DragonFly") || osname.equals("BSD/OS");
    }

    static final int UNINITIALIZED = -1;
    static final int BSD_LPD = 0;
    static final int BSD_LPD_NG = 1;

    static int cmdIndex = UNINITIALIZED;

    String[] lpcFirstCom = {
        "/usr/sbin/lpc status | grep : | sed -ne '1,1 s/://p'",
        "/usr/sbin/lpc status | grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}'"
    };

    String[] lpcAllCom = {
        "/usr/sbin/lpc status | grep : | sed -e 's/://'",
        "/usr/sbin/lpc -a status | grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}'"
    };

    String[] lpcNameCom = {
        "| grep : | sed -ne 's/://p'",
        "| grep -E '^[ 0-9a-zA-Z_-]*@' | awk -F'@' '{print $1}'"
    };


    static int getBSDCommandIndex() {
        String command  = "/usr/sbin/lpc status | grep @";
        String[] names = execCmd(command);

        if ((names == null) || (names.length == 0)) {
            return BSD_LPD;
        } else {
            return BSD_LPD_NG;
        }
    }


    /* Want the PrintService which is default print service to have
     * equality of reference with the equivalent in list of print services
     * This isn't required by the API and there's a risk doing this will
     * lead people to assume its guaranteed.
     */
    public synchronized PrintService[] getPrintServices() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        if (printServices == null) {
            getDefaultPrintService();
            if (isSysV()) {
                printers = getAllPrinterNamesSysV();
                printServices = new PrintService[printers.length+1];
                printServices[0] = getDefaultPrintService();
                for (int p=0;p<printers.length; p++) {
                    printServices[p+1] = new UnixPrintService(printers[p]);
                }
            } else {
                printers = getAllPrinterNamesBSD();
                printServices = new PrintService[printers.length];
                for (int p=0;p<printers.length; p++) {
                    printServices[p] = new UnixPrintService(printers[p]);
                }
            }

        }
        return printServices;
    }

    private boolean matchesAttributes(PrintService service,
                                      PrintServiceAttributeSet attributes) {

        Attribute [] attrs =  attributes.toArray();
        Attribute serviceAttr;
        for (int i=0; i<attrs.length; i++) {
            serviceAttr = service.getAttribute(attrs[i].getCategory());
            if (serviceAttr == null || !serviceAttr.equals(attrs[i])) {
                return false;
            }
        }
        return true;
    }

      /* This checks for validity of the printer name before passing as
       * parameter to a shell command.
       */
      private boolean checkPrinterName(String s) {
        char c;

        for (int i=0; i < s.length(); i++) {
          c = s.charAt(i);
          if (Character.isLetterOrDigit(c) ||
              c == '-' || c == '_' || c == '.' || c == '/') {
            continue;
          } else {
            return false;
          }
        }
        return true;
      }

    /* On a network with many (hundreds) of network printers, it
     * can save several seconds if you know all you want is a particular
     * printer, to ask for that printer rather than retrieving all printers.
     */
    private PrintService getServiceByName(PrinterName nameAttr) {
        String name = nameAttr.getValue();
        PrintService printer = null;
        if (name == null || name.equals("") || !checkPrinterName(name)) {
            return null;
        }
        if (isSysV()) {
            printer = getNamedPrinterNameSysV(name);
        } else {
            printer = getNamedPrinterNameBSD(name);
        }
        return printer;
    }

    private PrintService[]
        getPrintServices(PrintServiceAttributeSet serviceSet) {

        if (serviceSet == null || serviceSet.isEmpty()) {
            return getPrintServices();
        }

        /* Typically expect that if a service attribute is specified that
         * its a printer name and there ought to be only one match.
         * Directly retrieve that service and confirm
         * that it meets the other requirements.
         * If printer name isn't mentioned then go a slow path checking
         * all printers if they meet the reqiremements.
         */
        PrintService[] services;
        PrinterName name = (PrinterName)serviceSet.get(PrinterName.class);
        if (name != null) {
            /* To avoid execing a unix command  see if the client is asking
             * for the default printer by name, since we already have that
             * initialised.
             */
            PrintService defService = getDefaultPrintService();
            PrinterName defName =
                (PrinterName)defService.getAttribute(PrinterName.class);

            if (defName != null && name.equals(defName)) {
                if (matchesAttributes(defService, serviceSet)) {
                    services = new PrintService[1];
                    services[0] = defService;
                    return services;
                } else {
                    return new PrintService[0];
                }
            } else {
                /* Its not the default service */
                PrintService service = getServiceByName(name);
                if (service != null &&
                    matchesAttributes(service, serviceSet)) {
                    services = new PrintService[1];
                    services[0] = service;
                    return services;
                } else {
                    return new PrintService[0];
                }
            }
        } else {
            /* specified service attributes don't include a name.*/
            Vector matchedServices = new Vector();
            services = getPrintServices();
            for (int i = 0; i< services.length; i++) {
                if (matchesAttributes(services[i], serviceSet)) {
                    matchedServices.add(services[i]);
                }
            }
            services = new PrintService[matchedServices.size()];
            for (int i = 0; i< services.length; i++) {
                services[i] = (PrintService)matchedServices.elementAt(i);
            }
            return services;
        }
    }

    /*
     * We know that all of these services are the same, so ask
     * only one and return none or all based on that result.
     * If service attributes are specified then there must be additional
     * filtering.
     */
    public PrintService[] getPrintServices(DocFlavor flavor,
                                           AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        PrintRequestAttributeSet requestSet = null;
        PrintServiceAttributeSet serviceSet = null;

        if (attributes != null && !attributes.isEmpty()) {

            requestSet = new HashPrintRequestAttributeSet();
            serviceSet = new HashPrintServiceAttributeSet();

            Attribute[] attrs = attributes.toArray();
            for (int i=0; i<attrs.length; i++) {
                if (attrs[i] instanceof PrintRequestAttribute) {
                    requestSet.add(attrs[i]);
                } else if (attrs[i] instanceof PrintServiceAttribute) {
                    serviceSet.add(attrs[i]);
                }
            }
        }

        PrintService service = getDefaultPrintService();
        if ((flavor == null || service.isDocFlavorSupported(flavor)) &&
            service.getUnsupportedAttributes(flavor, requestSet) == null) {
            return getPrintServices(serviceSet);
        } else {
            return new PrintService[0];
        }
    }

    /*
     * return empty array as don't support multi docs
     */
    public MultiDocPrintService[]
        getMultiDocPrintServices(DocFlavor[] flavors,
                                 AttributeSet attributes) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        return new MultiDocPrintService[0];
    }


    public synchronized PrintService getDefaultPrintService() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
          security.checkPrintJobAccess();
        }
        if (defaultPrintService == null) {
            if (isSysV()) {
                defaultPrinter = getDefaultPrinterNameSysV();
            } else {
                defaultPrinter = getDefaultPrinterNameBSD();
            }
           defaultPrintService = new UnixPrintService(defaultPrinter);
        }
        return defaultPrintService;
    }

    public synchronized void
        getServicesInbackground(BackgroundLookupListener listener) {
        if (printServices != null) {
            listener.notifyServices(printServices);
        } else {
            if (lookupListeners == null) {
                lookupListeners = new Vector();
                lookupListeners.add(listener);
                Thread lookupThread = new Thread(this);
                lookupThread.start();
            } else {
                lookupListeners.add(listener);
            }
        }
    }

    /* This method isn't used in most cases because we rely on code in
     * javax.print.PrintServiceLookup. This is needed just for the cases
     * where those interfaces are by-passed.
     */
    private PrintService[] copyOf(PrintService[] inArr) {
        if (inArr == null || inArr.length == 0) {
            return inArr;
        } else {
            PrintService []outArr = new PrintService[inArr.length];
            System.arraycopy(inArr, 0, outArr, 0, inArr.length);
            return outArr;
        }
    }

    public void run() {
        PrintService[] services = getPrintServices();
        synchronized (this) {
            BackgroundLookupListener listener;
            for (int i=0; i<lookupListeners.size(); i++) {
                listener =
                    (BackgroundLookupListener)lookupListeners.elementAt(i);
                listener.notifyServices(copyOf(services));
            }
            lookupListeners = null;
        }
    }

    private String getDefaultPrinterNameBSD() {
        if (cmdIndex == UNINITIALIZED) {
            cmdIndex = getBSDCommandIndex();
        }
        String[] names = execCmd(lpcFirstCom[cmdIndex]);
        if (names == null || names.length == 0) {
            return null;
        }

        if ((cmdIndex==BSD_LPD_NG) &&
            (names[0].startsWith("missingprinter"))) {
            return null;
        }
        return names[0];
    }

    private PrintService getNamedPrinterNameBSD(String name) {
      if (cmdIndex == UNINITIALIZED) {
        cmdIndex = getBSDCommandIndex();
      }
      String command = "/usr/sbin/lpc status " + name + lpcNameCom[cmdIndex];
      String[] result = execCmd(command);

      if (result == null || !(result[0].equals(name))) {
          return null;
      }
      return new UnixPrintService(name);
    }

     private String[] getAllPrinterNamesBSD() {
        if (cmdIndex == UNINITIALIZED) {
            cmdIndex = getBSDCommandIndex();
        }
        String[] names = execCmd(lpcAllCom[cmdIndex]);
        if (names == null || names.length == 0) {
          return null;
        }
        return names;
    }

    private String getDefaultPrinterNameSysV() {
        String defaultPrinter = "lp";
        String command = "/usr/bin/lpstat -d|/usr/bin/expand|/usr/bin/cut -f4 -d' '";

        String [] names = execCmd(command);
        if (names == null || names.length == 0) {
            return defaultPrinter;
        } else {
            return names[0];
        }
    }

    private PrintService getNamedPrinterNameSysV(String name) {

        String command = "/usr/bin/lpstat -v " + name;
        String []result = execCmd(command);

        if (result == null || result[0].indexOf("unknown printer") > 0) {
            return null;
        } else {
            return new UnixPrintService(name);
        }
    }

    private String[] getAllPrinterNamesSysV() {
        String defaultPrinter = "lp";
        String command = "/usr/bin/lpstat -v|/usr/bin/expand|/usr/bin/cut -f3 -d' ' |/usr/bin/cut -f1 -d':'";

        String [] names = execCmd(command);
        ArrayList printerNames = new ArrayList();
        for (int i=0; i < names.length; i++) {
            if (!names[i].equals("_default") &&
                !names[i].equals(defaultPrinter)) {
                printerNames.add(names[i]);
            }
        }
        return (String[])printerNames.toArray(new String[printerNames.size()]);
    }

    static String[] execCmd(String command) {
        ArrayList results = new ArrayList();

        try {
            final String[] cmd = new String[3];
            if (isSysV()) {
                cmd[0] = "/usr/bin/sh";
                cmd[1] = "-c";
                cmd[2] = "env LC_ALL=C " + command;
            } else {
                cmd[0] = "/bin/sh";
                cmd[1] = "-c";
                cmd[2] = command;
            }

            BufferedReader bufferedReader =
             (BufferedReader)AccessController.doPrivileged(
             new PrivilegedExceptionAction() {
                public Object run() throws IOException {

                  File f = File.createTempFile("prn","xc");

                  try {
                    cmd[2] = cmd[2]+">"+f.getAbsolutePath();

                    Process lpstat = Runtime.getRuntime().exec(cmd);
                    try {
                      lpstat.waitFor();
                    } catch (InterruptedException e) {
                    }

                    if (lpstat.exitValue() == 0) {
                      FileReader reader = new FileReader(f);
                      return new BufferedReader(reader);
                    }
                  } finally {
                    f.delete();
                  }
                  return null;
                }
             });

            if (bufferedReader != null) {
              String line;
              while((line = bufferedReader.readLine()) != null) {
                results.add(line);
                }
            }

        } catch (IOException e) {
        } catch (PrivilegedActionException e) {
        }
        return (String[])results.toArray(new String[results.size()]);
    }
}

/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)AppletSecurity.java      1.109 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.applet;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketPermission;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.security.*;
import sun.awt.AWTSecurityManager;
import sun.awt.AppContext;
import sun.security.provider.*;
import sun.security.util.SecurityConstants;


/**
 * This class defines an applet security policy
 *
 * @version     1.109, 01/23/03
 */
public
class AppletSecurity extends AWTSecurityManager {
    private AppContext mainAppContext;

    private static Field facc;
    private static Field fcontext;

    /* static {
        try {
            facc = URLClassLoader.class.getDeclaredField("acc");
            facc.setAccessible(true);
            fcontext = AccessControlContext.class.getDeclaredField("context");
            fcontext.setAccessible(true);
        } catch (NoSuchFieldException e) {
            UnsupportedOperationException uoe =
                        new UnsupportedOperationException();
            uoe.initCause(e);
            throw uoe;
        }
    } */

    /**
     * Construct and initialize.
     */
    public AppletSecurity() {
        reset();
        mainAppContext = AppContext.getAppContext();
    }

    // Cache to store known restricted packages
    private final HashSet restrictedPackages = new HashSet();

    /**
     * Reset from Properties
     */
    public void reset()
    {
        // Clear cache
        restrictedPackages.clear();

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run()
            {
                // Enumerate system properties
                Enumeration e = System.getProperties().propertyNames();

                while (e.hasMoreElements())
                {
                    String name = (String) e.nextElement();

                    if (name != null && name.startsWith("package.restrict.access."))
                    {
                        String value = System.getProperty(name);

                        if (value != null && value.equalsIgnoreCase("true"))
                        {
                            String pkg = name.substring(24);

                            // Cache restricted packages
                            restrictedPackages.add(pkg);
                        }
                    }
                }
                return null;
            }
        });
    }

    /**
     * get the current (first) instance of an AppletClassLoader on the stack.
     */
    private AppletClassLoader currentAppletClassLoader()
    {
        // try currentClassLoader first
        ClassLoader loader = currentClassLoader();
        if ((loader == null) || (loader instanceof AppletClassLoader))
            return (AppletClassLoader)loader;

        // if that fails, get all the classes on the stack and check them.
        Class[] context = getClassContext();
        for (int i = 0; i < context.length; i++) {
            loader = context[i].getClassLoader();
            if (loader instanceof AppletClassLoader)
                return (AppletClassLoader)loader;
        }

        /* for (int j = 0; j < aclass.length; j++) {
            final ClassLoader currentLoader = aclass[j].getClassLoader();
            if (!(currentLoader instanceof URLClassLoader))
                continue;
            classloader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    Object obj = null;
                    ProtectionDomain aprotectiondomain[] = null;
                    try {
                        AccessControlContext accesscontrolcontext =
                                (AccessControlContext)AppletSecurity.facc.get(
                                currentLoader);
                        if (accesscontrolcontext == null)
                            return null;
                        aprotectiondomain =
                                (ProtectionDomain[])AppletSecurity.fcontext.get(
                                accesscontrolcontext);
                        if (aprotectiondomain == null)
                            return null;
                    } catch (Exception exception) {
                        UnsupportedOperationException uoe =
                                new UnsupportedOperationException();
                        uoe.initCause(exception);
                        throw uoe;
                    }
                    for (int k = 0; k < aprotectiondomain.length; k++) {
                        ClassLoader classloader1 =
                                aprotectiondomain[k].getClassLoader();
                        if (classloader1 instanceof AppletClassLoader)
                            return classloader1;
                    }
                    return null;
                }

            });
            if (classloader != null)
                return (AppletClassLoader)classloader;
        } */

        // if that fails, try the context class loader
        loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof AppletClassLoader)
            return (AppletClassLoader)loader;

        // no AppletClassLoaders on the stack
        return (AppletClassLoader)null;
    }

    /**
     * Returns true if this threadgroup is in the applet's own thread
     * group. This will return false if there is no current class
     * loader.
     */
    protected boolean inThreadGroup(ThreadGroup g) {
        if (currentAppletClassLoader() == null)
            return false;
        else
            return getThreadGroup().parentOf(g);
    }

    /**
     * Returns true of the threadgroup of thread is in the applet's
     * own threadgroup.
     */
    protected boolean inThreadGroup(Thread thread) {
        return inThreadGroup(thread.getThreadGroup());
    }

    /**
     * Applets are not allowed to manipulate threads outside
     * applet thread groups.
     */
    public void checkAccess(Thread t) {
        if (!inThreadGroup(t)) {
            checkPermission(SecurityConstants.MODIFY_THREAD_PERMISSION);
        }
    }

    private boolean inThreadGroupCheck = false;

    /**
     * Applets are not allowed to manipulate thread groups outside
     * applet thread groups.
     */
    public synchronized void checkAccess(ThreadGroup g) {
        if (inThreadGroupCheck) {
            // if we are in a recursive check, it is because
            // inThreadGroup is calling appletLoader.getThreadGroup
            // in that case, only do the super check, as appletLoader
            // has a begin/endPrivileged
            checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        } else {
            try {
                inThreadGroupCheck = true;
                if (!inThreadGroup(g)) {
                    checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
                }
            } finally {
                inThreadGroupCheck = false;
            }
        }
    }


    /**
     * Throws a <code>SecurityException</code> if the
     * calling thread is not allowed to access the package specified by
     * the argument.
     * <p>
     * This method is used by the <code>loadClass</code> method of class
     * loaders.
     * <p>
     * The <code>checkPackageAccess</code> method for class
     * <code>SecurityManager</code>  calls
     * <code>checkPermission</code> with the
     * <code>RuntimePermission("accessClassInPackage."+pkg)</code>
     * permission.
     *
     * @param      pkg   the package name.
     * @exception  SecurityException  if the caller does not have
     *             permission to access the specified package.
     * @see        java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    public void checkPackageAccess(final String pkgname) {

        // first see if the VM-wide policy allows access to this package
        super.checkPackageAccess(pkgname);

        // now check the list of restricted packages
        for (Iterator iter = restrictedPackages.iterator(); iter.hasNext();)
        {
            String pkg = (String) iter.next();

            // Prevent matching "sun" and "sunir" even if they
            // starts with similar beginning characters
            //
            if (pkgname.equals(pkg) || pkgname.startsWith(pkg + "."))
            {
                checkPermission(new java.lang.RuntimePermission
                            ("accessClassInPackage." + pkgname));
            }
        }
    }

    /**
     * Tests if a client can get access to the AWT event queue.
     * <p>
     * This method calls <code>checkPermission</code> with the
     * <code>AWTPermission("accessEventQueue")</code> permission.
     *
     * @since   JDK1.1
     * @exception  SecurityException  if the caller does not have
     *             permission to accesss the AWT event queue.
     */
    public void checkAwtEventQueueAccess() {
        AppContext appContext = AppContext.getAppContext();
        AppletClassLoader appletClassLoader = currentAppletClassLoader();

        if ((appContext == mainAppContext) && (appletClassLoader != null)) {
            // If we're about to allow access to the main EventQueue,
            // and anything untrusted is on the class context stack,
            // disallow access.
            super.checkAwtEventQueueAccess();
        }
    } // checkAwtEventQueueAccess()

    /**
     * Returns the thread group of the applet. We consult the classloader
     * if there is one.
     */
    public ThreadGroup getThreadGroup() {
        /* If any applet code is on the execution stack, we return
           that applet's ThreadGroup.  Otherwise, we use the default
           behavior. */
        AppletClassLoader appletLoader = currentAppletClassLoader();
        ThreadGroup loaderGroup = (appletLoader == null) ? null
                                          : appletLoader.getThreadGroup();
        if (loaderGroup != null) {
            return loaderGroup;
        } else {
            return super.getThreadGroup();
        }
    } // getThreadGroup()

    /**
      * Get the AppContext corresponding to the current context.
      * The default implementation returns null, but this method
      * may be overridden by various SecurityManagers
      * (e.g. AppletSecurity) to index AppContext objects by the
      * calling context.
      *
      * @return  the AppContext corresponding to the current context.
      * @see     sun.awt.AppContext
      * @see     java.lang.SecurityManager
      * @since   JDK1.2.1
      */
    public AppContext getAppContext() {
        AppletClassLoader appletLoader = currentAppletClassLoader();
        if (appletLoader == null)
            return null;
        AppContext ac = appletLoader.getAppContext();
        if (ac == null)
            throw new SecurityException(
                        "Applet classloader has invalid AppContext");
        return ac;
    }

} // class AppletSecurity

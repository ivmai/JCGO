/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)TransferableProxy.java   1.4 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.awt.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Proxies for another Transferable so that Serializable objects are never
 * returned directly by DnD or the Clipboard. Instead, a new instance of the
 * object is returned.
 *
 * @author Lawrence P.G. Cable
 * @author David Mendenhall
 * @version 1.4, 01/23/03
 *
 * @since 1.4
 */
public class TransferableProxy implements Transferable {
    public TransferableProxy(Transferable t, boolean local) {
        transferable = t;
        isLocal = local;
    }
    public DataFlavor[] getTransferDataFlavors() {
        return transferable.getTransferDataFlavors();
    }
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferable.isDataFlavorSupported(flavor);
    }
    public Object getTransferData(DataFlavor df)
        throws UnsupportedFlavorException, IOException
    {
        Object data = transferable.getTransferData(df);

        // If the data is a Serializable object, then create a new instance
        // before returning it. This insulates applications sharing DnD and
        // Clipboard data from each other.
        /* if (data != null && isLocal && df.isFlavorSerializedObjectType()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ClassLoaderObjectOutputStream oos =
                new ClassLoaderObjectOutputStream(baos);
            oos.writeObject(data);

            ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());

            try {
                data = new ClassLoaderObjectInputStream(bais,
                        oos.getClassLoaderMap()).readObject();

                data = new ObjectInputStream(bais).readObject();
            } catch (ClassNotFoundException cnfe) {
                throw (IOException) new IOException().initCause(cnfe);
            }
        } */

        return data;
    }

    protected final Transferable transferable;
    protected final boolean isLocal;
}

class ClassLoaderObjectInputStream extends ObjectInputStream {

    private final Map map;

    public ClassLoaderObjectInputStream(InputStream inputstream, Map map)
        throws IOException {
        super(inputstream);
        if (map == null)
            throw new NullPointerException("Null map");
        this.map = map;
    }

    protected Class resolveClass(ObjectStreamClass objectstreamclass)
        throws IOException, ClassNotFoundException {
        String name = objectstreamclass.getName();
        HashSet set = new HashSet(1);
        set.add(name);
        return Class.forName(name, false, (ClassLoader)map.get(set));
    }

    protected Class resolveProxyClass(String names[])
        throws IOException, ClassNotFoundException {
        HashSet set = new HashSet(names.length);
        for (int i = 0; i < names.length; i++) {
            set.add(names[i]);
        }

        ClassLoader loader = (ClassLoader)map.get(set);
        ClassLoader loader2 = null;
        boolean flag = false;
        Class interfaces[] = new Class[names.length];
        for (int j = 0; j < names.length; j++) {
            Class class1 = Class.forName(names[j], false, loader);
            if ((class1.getModifiers() & Modifier.PUBLIC) == 0) {
                if (flag) {
                    if (loader2 != class1.getClassLoader())
                        throw new IllegalAccessError(
                            "conflicting non-public interface class loaders");
                } else {
                    loader2 = class1.getClassLoader();
                    flag = true;
                }
            }
            interfaces[j] = class1;
        }

        try {
            return Proxy.getProxyClass(flag ? loader2 : loader, interfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}

class ClassLoaderObjectOutputStream extends ObjectOutputStream {

    private final Map map = new HashMap();

    public ClassLoaderObjectOutputStream(OutputStream outputstream)
        throws IOException {
        super(outputstream);
    }

    protected void annotateClass(final Class cl) throws IOException {
        ClassLoader loader =
          (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {

              public Object run() {
                  return cl.getClassLoader();
              }
          });
        HashSet set = new HashSet(1);
        set.add(cl.getName());
        map.put(set, loader);
    }

    protected void annotateProxyClass(final Class cl) throws IOException {
        ClassLoader loader =
          (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {

              public Object run() {
                  return cl.getClassLoader();
              }
          });
        Class interfaces[] = cl.getInterfaces();
        HashSet set = new HashSet(interfaces.length);
        for (int i = 0; i < interfaces.length; i++)
            set.add(interfaces[i].getName());
        map.put(set, loader);
    }

    public Map getClassLoaderMap() {
        return new HashMap(map);
    }
}

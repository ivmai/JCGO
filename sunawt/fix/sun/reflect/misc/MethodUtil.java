/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MethodUtil.java
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.reflect.misc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class MethodUtil extends SecureClassLoader
{

    private static final class Signature
    {

        private final String methodName;
        private final Class[] argClasses;
        private volatile int hashCode;

        Signature(Method method) {
            methodName = method.getName();
            argClasses = method.getParameterTypes();
        }

        public int hashCode() {
            if (hashCode == 0) {
                int v = 17;
                v = 37 * v + methodName.hashCode();
                if (argClasses != null) {
                    for (int j = 0; j < argClasses.length; j++) {
                        v = 37 * v + (argClasses[j] != null ?
                                argClasses[j].hashCode() : 0);
                    }
                }
                hashCode = v;
            }
            return hashCode;
        }

        public boolean equals(Object obj) {
            if (this == obj)
             return true;
            Signature signature = (Signature)obj;
            if (!methodName.equals(signature.methodName))
             return false;
            if (argClasses.length != signature.argClasses.length)
             return false;
            for (int i = 0; i < argClasses.length; i++) {
                if (argClasses[i] != signature.argClasses[i])
                 return false;
            }
            return true;
        }
    }


    private static /* final */ String MISC_PKG = "sun.reflect.misc.";
    private static final String TRAMPOLINE = MISC_PKG + "Trampoline";

    private static final Method bounce = getTrampoline();

    private MethodUtil() {
    }

    private static Class getTrampolineClass() {
        try {
            return Class.forName(TRAMPOLINE, true, new MethodUtil());
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Method getTrampoline() {
        Method method = null;
        try {
            method = (Method)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {

                   public Object run() throws Exception {
                    Class klass = Trampoline.class; // MethodUtil.getTrampolineClass();
                    Class parameterTypes[] = {
                        java.lang.reflect.Method.class,
                        Object.class,
                        Object[].class
                    };
                    Method m = klass.getDeclaredMethod("invoke", parameterTypes);
                    m.setAccessible(true);
                    return m;
                   }

            });
        }
        catch (Exception e) {
            throw new InternalError("bouncer cannot be found");
        }
        return method;
    }

    private static byte[] getBytes(URL url) throws IOException {
        URLConnection urlconnection = url.openConnection();
        if (urlconnection instanceof HttpURLConnection) {
            if (((HttpURLConnection)urlconnection).getResponseCode() >= 400)
                throw new IOException("open HTTP connection failed.");
        }
        int i = urlconnection.getContentLength();
        BufferedInputStream bufferedinputstream =
                new BufferedInputStream(urlconnection.getInputStream());
        byte abyte0[];
        try {
            if (i != -1) {
                abyte0 = new byte[i];
                int l;
                for (; i > 0; i -= l) {
                    l = bufferedinputstream.read(abyte0, abyte0.length - i, i);
                    if (l == -1)
                        throw new IOException("unexpected EOF");
                }
            } else {
                abyte0 = new byte[8192];
                int i1 = 0;
                do {
                    int j;
                    if ((j = bufferedinputstream.read(abyte0, i1,
                        abyte0.length - i1)) == -1)
                        break;
                    i1 += j;
                    if (i1 >= abyte0.length) {
                        byte abyte1[] = new byte[i1 * 2];
                        System.arraycopy(abyte0, 0, abyte1, 0, i1);
                        abyte0 = abyte1;
                    }
                } while (true);
                if (i1 != abyte0.length) {
                    byte abyte2[] = new byte[i1];
                    System.arraycopy(abyte0, 0, abyte2, 0, i1);
                    abyte0 = abyte2;
                }
            }
        } finally {
            bufferedinputstream.close();
        }
        return abyte0;
    }

    protected Class findClass(String name)
        throws ClassNotFoundException {
        if (!name.startsWith(MISC_PKG))
            throw new ClassNotFoundException(name);
        String s1 = name.replace('.', '/').concat(".class");
        URL url = getResource(s1);
        if (url != null) {
            try {
                return defineClass(name, url);
            }
            catch (IOException ioexception) {
                throw new ClassNotFoundException(name, ioexception);
            }
        }
        else {
            throw new ClassNotFoundException(name);
        }
    }

    protected synchronized Class loadClass(String name, boolean flag)
        throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(name);
        Class klass = findLoadedClass(name);
        if (klass == null) {
            try {
                klass = findClass(name);
            }
            catch (ClassNotFoundException classnotfoundexception) {
            }
            if (klass == null)
                klass = getParent().loadClass(name);
        }
        if (flag)
            resolveClass(klass);
        return klass;
    }

    public static Method[] getMethods(Class klass) {
        ReflectUtil.checkPackageAccess(klass);
        return klass.getMethods();
    }

    public static Method[] getPublicMethods(Class klass) {
        if (System.getSecurityManager() == null)
            return klass.getMethods();
        HashMap hashmap = new HashMap();
        do {
            if (klass == null)
                break;
            boolean flag = getInternalPublicMethods(klass, hashmap);
            if (flag)
                break;
            getInterfaceMethods(klass, hashmap);
            klass = klass.getSuperclass();
        } while (true);
        Collection collection = hashmap.values();
        return (Method[])collection.toArray(new Method[collection.size()]);
    }

    private static void addMethod(Map map, Method method) {
        Signature signature = new Signature(method);
        if (!map.containsKey(signature)) {
            map.put(signature, method);
        } else {
           if (!method.getDeclaringClass().isInterface()) {
               Method m = (Method)map.get(signature);
               if (m.getDeclaringClass().isInterface())
                   map.put(signature, method);
           }
        }
    }

    protected PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection permissioncollection =
                super.getPermissions(codesource);
        permissioncollection.add(new AllPermission());
        return permissioncollection;
    }

    private static void getInterfaceMethods(Class klass, Map map) {
        Class interfaces[] = klass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class class2 = interfaces[i];
            if (!getInternalPublicMethods(class2, map))
                getInterfaceMethods(class2, map);
        }

    }

    private static boolean getInternalPublicMethods(Class class1, Map map) {
        Method amethod[] = null;
        try {
            if (!Modifier.isPublic(class1.getModifiers()))
                return false;
            if (!ReflectUtil.isPackageAccessible(class1))
                return false;
            amethod = class1.getMethods();
        }
        catch (SecurityException securityexception) {
            return false;
        }
        boolean flag = true;
        int i = 0;
        do {
            if (i >= amethod.length)
                break;
            Class class2 = amethod[i].getDeclaringClass();
            if (!Modifier.isPublic(class2.getModifiers())) {
                flag = false;
                break;
            }
            i++;
        } while (true);
        if (flag) {
            for (int j = 0; j < amethod.length; j++)
            addMethod(map, amethod[j]);
        } else {
            for (int k = 0; k < amethod.length; k++) {
                Class class3 = amethod[k].getDeclaringClass();
                if (class1.equals(class3))
                    addMethod(map, amethod[k]);
            }
        }
        return flag;
    }

    private Class defineClass(String name, URL url) throws IOException {
        byte abyte0[] = getBytes(url);
        CodeSource codesource = new CodeSource(null,
                                 (java.security.cert.Certificate[])null);
        if (!name.equals(TRAMPOLINE)) {
            throw new IOException("MethodUtil: bad name " + name);
        }
        return defineClass(name, abyte0, 0, abyte0.length, codesource);
    }

    public static Object invoke(Method method, Object obj, Object aobj[])
     throws InvocationTargetException, IllegalAccessException {
        if (method.getDeclaringClass().equals(
            java.security.AccessController.class) ||
            method.getDeclaringClass().equals(java.lang.reflect.Method.class))
            throw new InvocationTargetException(
                new UnsupportedOperationException("invocation not supported"));
        try {
            return bounce.invoke(null, new Object[] { method, obj, aobj });
        }
        catch (InvocationTargetException invocationtargetexception) {
            Throwable throwable = invocationtargetexception.getCause();
            if (throwable instanceof InvocationTargetException)
                throw (InvocationTargetException)throwable;
            if (throwable instanceof IllegalAccessException)
                throw (IllegalAccessException)throwable;
            if (throwable instanceof RuntimeException)
                throw (RuntimeException)throwable;
            if (throwable instanceof Error)
                throw (Error)throwable;
            throw new Error("Unexpected invocation error", throwable);
        }
        catch (IllegalAccessException illegalaccessexception) {
            throw new Error("Unexpected invocation error",
                        illegalaccessexception);
        }
    }

    public static Method getMethod(Class class1, String name,
                                Class parameterTypes[])
        throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(class1);
        return class1.getMethod(name, parameterTypes);
    }
}

class Trampoline
{

    Trampoline() {
    }

    private static Object invoke(Method method, Object obj, Object aobj[])
        throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, aobj);
    }
}

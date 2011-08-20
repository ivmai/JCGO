/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/VMProxy.java --
 * VM specific methods for Java class "Proxy" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
 */

/*
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 **
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License (GPL) for more details.
 **
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 **
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module. An independent module is a module which is not derived from
 * or based on this library. If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package java.lang.reflect;

final class VMProxy
{

 static final boolean HAVE_NATIVE_GET_PROXY_CLASS = true;

 static boolean HAVE_NATIVE_GET_PROXY_DATA = false;

 static boolean HAVE_NATIVE_GENERATE_PROXY_CLASS = true;

 private VMProxy() {}

 static Class getProxyClass(ClassLoader loader, Class[] interfaces)
 {
  return generateProxyClass(loader,
          checkAndComputePkgName(loader, interfaces), interfaces);
 }

 static Proxy.ProxyData getProxyData(ClassLoader loader, Class[] interfaces)
 {
  /* dummy */
  throw new InternalError();
 }

 static Class generateProxyClass(ClassLoader loader, Proxy.ProxyData data)
 {
  return generateProxyClass(loader, data.pack, data.interfaces);
 }

 static final Object invokeProxyHandler0X(Object proxyObj,
   Class declaringClass, int[] intArgs, long[] longArgs, float[] floatArgs,
   double[] doubleArgs, Object[] objectArgs, int slot, int proxySlot)
  throws Throwable /* hard-coded method signature */
 { /* called from native code */
  Proxy proxy = (Proxy) proxyObj;
  InvocationHandler handler = proxy.h;
  if (handler == null)
   throw new NullPointerException();
  return VMMethod.invokeProxyHandler(handler, proxy, declaringClass, intArgs,
          longArgs, floatArgs, doubleArgs, objectArgs, slot, proxySlot);
 }

 private static String checkAndComputePkgName(ClassLoader loader,
   Class[] interfaces)
 {
  String packageName = null;
  int i = interfaces.length;
  while (i-- > 0)
  {
   Class iface = interfaces[i];
   String ifaceName = iface.getName();
   if (!iface.isInterface())
    throw new IllegalArgumentException("not an interface: " + ifaceName);
   Class aclass = null;
   try
   {
    aclass = Class.forName(ifaceName, false, loader);
   }
   catch (ClassNotFoundException e) {}
   if (aclass != iface)
    throw new IllegalArgumentException(
           "interface not accessible in classloader: " + ifaceName);
   if ((iface.getModifiers() & Modifier.PUBLIC) == 0)
   {
    String pkgName = ifaceName.substring(0, ifaceName.lastIndexOf('.') + 1);
    if (packageName != null)
    {
     if (!packageName.equals(pkgName))
      throw new IllegalArgumentException(
       "non-public interfaces from different packages: " + ifaceName);
    }
     else packageName = pkgName;
   }
   for (int j = 0; j < i; j++)
    if (interfaces[j] == iface)
     throw new IllegalArgumentException("duplicate interface: " + ifaceName);
   /* check for methods return type not implemented */
  }
  return packageName != null ? packageName : "";
 }

 private static Class generateProxyClass(ClassLoader loader,
   String packageName, Class[] interfaces)
 {
  if (interfaces == null) /* hack */
  {
   try
   {
    invokeProxyHandler0X(new Proxy(null), null, null, null, null, null, null,
     -1, -1); /* hack */
   }
   catch (Throwable e) {}
  }
  String proxyClassName = makeProxyClassName(packageName, interfaces);
  if (loader == null)
   loader = ClassLoader.getSystemClassLoader();
  try
  {
   return loader.loadClass(proxyClassName);
  }
  catch (ClassNotFoundException e) {}
  throw new OutOfMemoryError("cannot create proxy class for: " +
         ifacesToString(interfaces));
 }

 private static String makeProxyClassName(String packageName,
   Class[] interfaces)
 {
  StringBuilder sb = new StringBuilder();
  sb.append(packageName).append("$Proxy");
  int count = interfaces.length;
  for (int i = 0; i < count; i++)
  {
   String name = interfaces[i].getName();
   int pos = -1;
   while ((pos = name.indexOf('$', pos + 1) + 1) > 0)
    name = name.substring(0, pos) + "$" + name.substring(pos);
   sb.append("$00");
   int next;
   while ((next = name.indexOf('.', pos)) >= 0)
   {
    sb.append(name.substring(pos, next)).append("$0");
    pos = next + 1;
   }
   sb.append(name.substring(pos));
  }
  return sb.toString();
 }

 private static String ifacesToString(Class[] interfaces)
 {
  int count = interfaces.length;
  if (count == 0)
   return "<no interfaces>";
  StringBuilder sb = new StringBuilder();
  sb.append(interfaces[0].getName());
  for (int i = 1; i < count; i++)
   sb.append(", ").append(interfaces[i].getName());
  return sb.toString();
 }
}

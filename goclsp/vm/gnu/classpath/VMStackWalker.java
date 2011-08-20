/*
 * @(#) $(JCGO)/goclsp/vm/gnu/classpath/VMStackWalker.java --
 * VM specific methods for Java stack access.
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

package gnu.classpath;

public final class VMStackWalker
{

 private VMStackWalker() {}

 public static Class[] getClassContext()
 {
  Class caller = getCallingClassAt0(1);
  if (caller == null)
   return new Class[] { Object.class }; /* hack */
  Class[] context = new Class[10];
  int count = 0;
  do
  {
   if (context.length <= count)
   {
    Class[] newContext = new Class[(count >> 1) + count];
    System.arraycopy(context, 0, newContext, 0, count);
    context = newContext;
   }
   context[count++] = caller;
  } while ((caller = getCallingClassAt0(count + 1)) != null);
  if (context.length > count)
  {
   Class[] newContext = new Class[count];
   System.arraycopy(context, 0, newContext, 0, count);
   context = newContext;
  }
  return context;
 }

 public static Class getCallingClass()
 {
  return getCallingClassAt0(2);
 }

 public static ClassLoader getCallingClassLoader()
 {
  Class caller = getCallingClassAt0(2);
  return caller != null ? getClassLoader(caller) : null;
 }

 public static ClassLoader getClassLoader(Class klass)
 {
  if (klass == null)
   throw new NullPointerException();
  return VMAccessorJavaLang.getClassLoaderVMClass(klass);
 }

 public static ClassLoader firstNonNullClassLoader()
 {
  ClassLoader loader = null;
  Class caller;
  for (int depth = 2; (caller = getCallingClassAt0(depth)) != null; depth++)
   if ((loader = getClassLoader(caller)) != null)
    break;
  return loader;
 }

 private static native Class getCallingClassAt0(int depth); /* JVM-core */
}

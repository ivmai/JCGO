/*
 * @(#) $(JCGO)/goclsp/vm/sun/reflect/misc/ReflectUtil.java --
 * JSR 166 reflection hook utils.
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

package sun.reflect.misc;

import java.lang.reflect.Modifier;

import sun.reflect.Reflection;

public final class ReflectUtil
{ /* VM class */

 private ReflectUtil() {}

 public static void checkPackageAccess(Class declaringClass)
 {
  checkPackageAccess(declaringClass.getName());
 }

 public static void checkPackageAccess(String className)
 {
  SecurityManager sm = System.getSecurityManager();
  if (sm != null)
  {
   className = className.replace('/', '.');
   int i;
   if (className.length() > 0 && className.charAt(0) == '[')
   {
    if ((i = className.lastIndexOf('[') + 2) > 1 && i < className.length())
     className = className.substring(i);
   }
   if ((i = className.lastIndexOf('.')) >= 0)
    sm.checkPackageAccess(className.substring(0, i));
  }
 }

 public static boolean isPackageAccessible(Class aclass)
 {
  try
  {
   checkPackageAccess(aclass);
  }
  catch (SecurityException e)
  {
   return false;
  }
  return true;
 }

 public static Class forName(String className)
  throws ClassNotFoundException
 {
  checkPackageAccess(className);
  return Class.forName(className);
 }

 public static Object newInstance(Class aclass)
  throws InstantiationException, IllegalAccessException
 {
  checkPackageAccess(aclass);
  return aclass.newInstance();
 }

 public static void ensureMemberAccess(Class callerClass, Class declarerClass,
   Object obj, int modifiers)
  throws IllegalAccessException
 {
  if (obj == null && (modifiers & Modifier.PROTECTED) != 0)
  {
   Reflection.ensureMemberAccess(callerClass, declarerClass, obj,
    (modifiers & ~Modifier.PROTECTED) | Modifier.PUBLIC);
   try
   {
    Reflection.ensureMemberAccess(callerClass, declarerClass, obj,
     modifiers & ~(Modifier.PUBLIC | Modifier.PROTECTED));
   }
   catch (IllegalAccessException e)
   {
    if (!isSubclassOf(callerClass, declarerClass))
     throw e;
   }
  }
   else Reflection.ensureMemberAccess(callerClass, declarerClass, obj,
         modifiers);
 }

 private static boolean isSubclassOf(Class queryClass, Class ofClass)
 {
  while (queryClass != null)
  {
   if (queryClass == ofClass)
    return true;
   queryClass = queryClass.getSuperclass();
  }
  return false;
 }
}

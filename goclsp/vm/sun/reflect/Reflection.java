/*
 * @(#) $(JCGO)/goclsp/vm/sun/reflect/Reflection.java --
 * JSR 166 reflection hooks.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package sun.reflect;

import gnu.classpath.VMStackWalker;

import java.lang.reflect.Modifier;

public class Reflection
{ /* VM class */

 public Reflection() {}

 public static Class getCallerClass(int depth)
 {
  Class[] ctx = VMStackWalker.getClassContext();
  return depth >= 0 && ctx.length > depth ? ctx[depth] : null;
 }

 public static boolean quickCheckMemberAccess(Class memberClass,
   int modifiers)
 {
  return Modifier.isPublic(getClassAccessFlags(memberClass) & modifiers);
 }

 public static void ensureMemberAccess(Class callerClass, Class declarerClass,
   Object obj, int modifiers)
  throws IllegalAccessException
 {
  if (callerClass == null || declarerClass == null)
   throw new InternalError("ensureMemberAccess");
  if (!verifyMemberAccess(callerClass, declarerClass, obj, modifiers))
   throw new IllegalAccessException("class " + callerClass.getName() +
          " can not access a member of class " + declarerClass.getName() +
          " with modifiers \"" + Modifier.toString(modifiers) + "\"");
 }

 public static boolean verifyMemberAccess(Class callerClass,
   Class declarerClass, Object obj, int modifiers)
 {
  if (callerClass != declarerClass)
  {
   boolean isPkgChecked = false;
   if (!Modifier.isPublic(getClassAccessFlags(declarerClass)))
   {
    if (!isSameClassPackage(callerClass, declarerClass))
     return false;
    isPkgChecked = true;
   }
   if (!Modifier.isPublic(modifiers))
   {
    if (Modifier.isProtected(modifiers))
    {
     if (!isPkgChecked && !isSubclassOf(callerClass, declarerClass))
     {
      if (!isSameClassPackage(callerClass, declarerClass))
       return false;
      isPkgChecked = true;
     }
     Class aclass = obj != null ? obj.getClass() : declarerClass;
     if (aclass != callerClass && !isPkgChecked &&
         !isSameClassPackage(callerClass, declarerClass) &&
         !isSubclassOf(aclass, callerClass))
      return false;
    }
     else if (Modifier.isPrivate(modifiers) || (!isPkgChecked &&
              !isSameClassPackage(callerClass, declarerClass)))
      return false;
   }
  }
  return true;
 }

 static boolean isSubclassOf(Class queryClass, Class ofClass)
 {
  while (queryClass != null)
  {
   if (queryClass == ofClass)
    return true;
   queryClass = queryClass.getSuperclass();
  }
  return false;
 }

 private static boolean isSameClassPackage(Class aclass1, Class aclass2)
 {
  return isSameClassPackage(aclass1.getClassLoader(), aclass1.getName(),
          aclass2.getClassLoader(), aclass2.getName());
 }

 private static boolean isSameClassPackage(ClassLoader loader1,
   String className1, ClassLoader loader2, String className2)
 {
  if (loader1 != loader2)
   return false;
  int i1 = className1.lastIndexOf('.');
  int i2 = className2.lastIndexOf('.');
  if (i1 == -1 || i2 == -1)
   return i1 == i2;
  int k1 = 0;
  if (className1.charAt(0) == '[')
  {
   do
   {
    k1++;
   } while (className1.charAt(k1) == '[');
   if (className1.charAt(k1) != 'L')
    throw new InternalError("illegal class name " + className1);
  }
  int k2 = 0;
  if (className2.charAt(0) == '[')
  {
   do
   {
    k2++;
   } while (className2.charAt(k2) == '[');
   if (className2.charAt(k2) != 'L')
    throw new InternalError("illegal class name " + className2);
  }
  int len = i1 - k1;
  return i2 - k2 == len && className1.regionMatches(k1, className2, k2, len);
 }

 private static int getClassAccessFlags(Class aclass)
 {
  return VMAccessorJavaLang.getModifiersVMClass(aclass, true);
 }
}

/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/VMAccessorJavaLangReflect.java --
 * VM cross-package access helper for "java.lang.reflect".
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

public final class VMAccessorJavaLangReflect
{ /* used by VM classes only */

 private VMAccessorJavaLangReflect() {}

 public static final Object allocateObjectVMConstructor(
   Constructor constructor, Class objClass)
  throws InstantiationException
 {
  return VMConstructor.allocateObject(constructor, objClass);
 }

 public static Field createNonFinalAccessibleVMField(Field field)
 {
  return VMField.createNonFinalAccessible(field);
 }

 public static long objectFieldOffsetVMField(Field field)
 {
  return VMField.objectFieldOffset(field);
 }

 public static Constructor getDeclaredConstructorVMMethod(Class klass,
   Class[] types)
 {
  return VMMethod.getDeclaredConstructor(klass, types);
 }

 public static Constructor[] getDeclaredConstructorsVMMethod(Class klass,
   boolean publicOnly)
 {
  return VMMethod.getDeclaredConstructors(klass, publicOnly);
 }

 public static Field getDeclaredFieldVMField(Class klass, String fieldName)
 {
  return VMField.getDeclaredField(klass, fieldName);
 }

 public static Field[] getDeclaredFieldsVMField(Class klass,
   boolean publicOnly)
 {
  return VMField.getDeclaredFields(klass, publicOnly);
 }

 public static Method getDeclaredMethodVMMethod(Class klass,
   String methodName, Class[] types)
 {
  return VMMethod.getDeclaredMethod(klass, methodName, types);
 }

 public static Method[] getDeclaredMethodsVMMethod(Class klass,
   boolean publicOnly)
 {
  return VMMethod.getDeclaredMethods(klass, publicOnly);
 }
}

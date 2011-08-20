/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMObject.java --
 * VM specific methods for Java "Object" class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package java.lang;

final class VMObject /* hard-coded class name */
{

 private static final int MODIFIER_PUBLIC = 0x1;
 private static final int MODIFIER_FINAL = 0x10;
 private static final int MODIFIER_ABSTRACT = 0x400;

 private VMObject() {}

 static Class getClass(Object obj)
 {
  /* throw new InternalError(); */
  return VMClass.arrayClassOf0X(getClass0(obj), getObjArrayDims0(obj));
 }

 static Object clone(Cloneable obj)
 {
  Object clonedObj = clone0(obj);
  if (clonedObj == null) /* hack */
   throw new InternalError();
  return clonedObj;
 }

 static void notify(Object obj)
 {
  VMThread.notify(obj, false);
 }

 static void notifyAll(Object obj)
 {
  VMThread.notify(obj, true);
 }

 static void wait(Object obj, long ms, int ns)
  throws InterruptedException
 {
  VMThread.wait(obj, ms, ns);
 }

 static final void appendClassName(Object obj, StringBuilder sb)
 { /* used by VM classes only */
  int dims = getObjArrayDims0(obj); /* hack */
  Class aclass = getClass0(obj);
  if (dims > 0)
  {
   do
   {
    sb.append('[');
   } while (--dims > 0);
   if (aclass.modifiers != (MODIFIER_PUBLIC | MODIFIER_FINAL |
       MODIFIER_ABSTRACT)) /* hack */
   {
    sb.append('L');
    dims = 1;
   }
  }
  sb.append(aclass.getName());
  if (dims > 0)
   sb.append(';');
 }

 static final native int getObjArrayDims0(Object obj); /* JVM-core */

 static final native Class getClass0(Object obj); /* JVM-core */

 private static native Object clone0(
   Object obj); /* JVM-core */ /* hard-coded method signature */
}

/*
 * @(#) $(JCGO)/goclsp/vm/sun/misc/Unsafe.java --
 * VM unsafe operations needed for concurrency.
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

package sun.misc;

import java.lang.reflect.Field;
import java.lang.reflect.VMAccessorJavaLangReflect;

public final class Unsafe
{ /* VM class */

 public static final int INVALID_FIELD_OFFSET = -1;

 private static final int CONSISTENCY_DEFAULT = 0;

 private static final int CONSISTENCY_ORDERED = 1;

 private static final int CONSISTENCY_VOLATILE = 2;

 private static final int TYPECODE_INTARRAY = 0;

 private static final int TYPECODE_LONGARRAY = 1;

 private static final int TYPECODE_OBJECTARRAY = 2;

 private static final int[] arrayBaseOffsets =
  new int[TYPECODE_OBJECTARRAY + 1];

 private static final int[] arrayIndexScales =
  new int[TYPECODE_OBJECTARRAY + 1];

 static
 {
  initArrayOffsets0(arrayBaseOffsets, arrayIndexScales);
 }

 private static final Unsafe unsafe = new Unsafe();

 private Unsafe() {}

 public static Unsafe getUnsafe()
 {
  SecurityManager sm = System.getSecurityManager();
  if (sm != null)
   sm.checkPropertiesAccess();
  return unsafe;
 }

 public long objectFieldOffset(Field field)
 {
  return VMAccessorJavaLangReflect.objectFieldOffsetVMField(field);
 }

 public boolean compareAndSwapInt(Object obj, long offset, int expect,
   int update)
 {
  return compareAndSwapInt0(obj, offset, expect, update) != 0;
 }

 public boolean compareAndSwapLong(Object obj, long offset, long expect,
   long update)
 {
  return compareAndSwapLong0(obj, offset, expect, update) != 0;
 }

 public boolean compareAndSwapObject(Object obj, long offset, Object expect,
   Object update)
 {
  return compareAndSwapObject0(obj, expect, update, offset) != 0;
 }

 public void putOrderedInt(Object obj, long offset, int value)
 {
  putInt0(obj, offset, value, CONSISTENCY_ORDERED);
 }

 public void putOrderedLong(Object obj, long offset, long value)
 {
  putLong0(obj, offset, value, CONSISTENCY_ORDERED);
 }

 public void putOrderedObject(Object obj, long offset, Object value)
 {
  putObject0(obj, value, offset, CONSISTENCY_ORDERED);
 }

 public void putIntVolatile(Object obj, long offset, int value)
 {
  putInt0(obj, offset, value, CONSISTENCY_VOLATILE);
 }

 public int getIntVolatile(Object obj, long offset)
 {
  return getInt0(obj, offset, CONSISTENCY_VOLATILE);
 }

 public void putLongVolatile(Object obj, long offset, long value)
 {
  putLong0(obj, offset, value, CONSISTENCY_VOLATILE);
 }

 public void putLong(Object obj, long offset, long value)
 {
  putLong0(obj, offset, value, CONSISTENCY_DEFAULT);
 }

 public long getLongVolatile(Object obj, long offset)
 {
  return getLong0(obj, offset, CONSISTENCY_VOLATILE);
 }

 public long getLong(Object obj, long offset)
 {
  return getLong0(obj, offset, CONSISTENCY_DEFAULT);
 }

 public void putObjectVolatile(Object obj, long offset, Object value)
 {
  putObject0(obj, value, offset, CONSISTENCY_VOLATILE);
 }

 public void putObject(Object obj, long offset, Object value)
 {
  putObject0(obj, value, offset, CONSISTENCY_DEFAULT);
 }

 public Object getObjectVolatile(Object obj, long offset)
 {
  return getObject0(obj, offset, CONSISTENCY_VOLATILE);
 }

 public int arrayBaseOffset(Class arrayClass)
 {
  Class componentType = arrayClass.getComponentType();
  return componentType == int.class ? arrayBaseOffsets[TYPECODE_INTARRAY] :
          componentType == long.class ? arrayBaseOffsets[TYPECODE_LONGARRAY] :
          componentType != null && !componentType.isPrimitive() ?
          arrayBaseOffsets[TYPECODE_OBJECTARRAY] : 0;
 }

 public int arrayIndexScale(Class arrayClass)
 {
  Class componentType = arrayClass.getComponentType();
  return componentType == int.class ? arrayIndexScales[TYPECODE_INTARRAY] :
          componentType == long.class ? arrayIndexScales[TYPECODE_LONGARRAY] :
          componentType != null && !componentType.isPrimitive() ?
          arrayIndexScales[TYPECODE_OBJECTARRAY] : 0;
 }

 public void unpark(Thread /* Object */ thread)
 {
  VMAccessorJavaLang.unparkVMThread((Thread) thread);
 }

 public void park(boolean isAbsolute, long time)
 {
  VMAccessorJavaLang.parkVMThread(isAbsolute, time);
 }

 private static native int initArrayOffsets0(int[] arrayBaseOffsets,
   int[] arrayIndexScales); /* JVM-core */

 private static native int compareAndSwapInt0(Object obj, long offset,
   int expect, int value); /* JVM-core */

 private static native int compareAndSwapLong0(Object obj, long offset,
   long expect, long value); /* JVM-core */

 private static native int compareAndSwapObject0(Object obj, Object expect,
   Object value, long offset); /* JVM-core */

 private static native int putInt0(Object obj, long offset, int value,
   int consistency); /* JVM-core */

 private static native int putLong0(Object obj, long offset, long value,
   int consistency); /* JVM-core */

 private static native int putObject0(Object obj, Object value, long offset,
   int consistency); /* JVM-core */

 private static native int getInt0(Object obj, long offset,
   int consistency); /* JVM-core */

 private static native long getLong0(Object obj, long offset,
   int consistency); /* JVM-core */

 private static native Object getObject0(Object obj, long offset,
   int consistency); /* JVM-core */
}

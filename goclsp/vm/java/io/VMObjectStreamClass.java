/*
 * @(#) $(JCGO)/goclsp/vm/java/io/VMObjectStreamClass.java --
 * VM specific methods for ObjectStreamClass.
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

package java.io;

import java.lang.reflect.Field;
import java.lang.reflect.VMAccessorJavaLangReflect;

import java.security.AccessController;
import java.security.PrivilegedAction;

final class VMObjectStreamClass
{

 private VMObjectStreamClass() {}

 static boolean hasClassInitializer(Class clazz)
 {
  return VMAccessorJavaLang.hasClassInitializerVMClass(clazz);
 }

 static void setDoubleNative(Field field, Object obj, double val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setDouble(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setFloatNative(Field field, Object obj, float val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setFloat(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setLongNative(Field field, Object obj, long val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setLong(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setIntNative(Field field, Object obj, int val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setInt(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setShortNative(Field field, Object obj, short val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setShort(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setCharNative(Field field, Object obj, char val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setChar(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setByteNative(Field field, Object obj, byte val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setByte(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setBooleanNative(Field field, Object obj, boolean val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).setBoolean(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 static void setObjectNative(Field field, Object obj, Object val)
 {
  try
  {
   privilegedCreateNonFinalAccessible(field).set(obj, val);
  }
  catch (Exception e)
  {
   throw (Error) (new InternalError("VMObjectStreamClass")).initCause(e);
  }
 }

 private static Field privilegedCreateNonFinalAccessible(final Field field)
 {
  return (Field) AccessController.doPrivileged(
   new PrivilegedAction()
   {

    public Object run()
    {
     return VMAccessorJavaLangReflect.createNonFinalAccessibleVMField(field);
    }
   });
 }
}

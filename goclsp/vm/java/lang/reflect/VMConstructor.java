/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/VMConstructor.java --
 * VM specific methods for Java "Constructor" implementation.
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

final class VMConstructor /* hard-coded class name */
{ /* VM class */ /* used by VM classes only */

 private static final int TYPECODE_BOOLEAN = 1;

 private static final int TYPECODE_BYTE = 2;

 private static final int TYPECODE_CHAR = 3;

 private static final int TYPECODE_SHORT = 4;

 private static final int TYPECODE_INT = 5;

 private static final int TYPECODE_LONG = 6;

 private static final int TYPECODE_FLOAT = 7;

 private static final int TYPECODE_DOUBLE = 8;

 private static final int TYPECODE_OBJECT = 9;

 private static final byte[] EMPTY_BYTES = {};

 private static final int[] EMPTY_INTS = {};

 private static final long[] EMPTY_LONGS = {};

 private static final float[] EMPTY_FLOATS = {};

 private static final double[] EMPTY_DOUBLES = {};

 private static final Object[] EMPTY_OBJECTS = {};

 private VMConstructor() {}

 static final int getModifiersInternal(Constructor constructor)
 {
  return constructor.modifiers;
 }

 static final Class[] getParameterTypesInternal(Constructor constructor)
 {
  return constructor.parameterTypes;
 }

 static final Class[] getExceptionTypesInternal(Constructor constructor)
 {
  return constructor.exceptionTypes;
 }

 static final String getSignature(Constructor constructor)
 {
  return constructor.signature;
 }

 static final Object newInstance(Constructor constructor, Object[] args,
   Class caller)
  throws InstantiationException, IllegalAccessException,
   InvocationTargetException
 {
  Class declaringClass = constructor.getDeclaringClass();
  if ((declaringClass.getModifiers() & Modifier.ABSTRACT) != 0)
   throw new InstantiationException("cannot instantiate an abstract class: " +
          declaringClass.getName());
  int modifiers = getModifiersInternal(constructor);
  if ((modifiers & Modifier.PUBLIC) == 0 && caller != declaringClass &&
      caller != null && ((modifiers & Modifier.PRIVATE) != 0 ||
      (((modifiers & Modifier.PROTECTED) == 0 ||
      !declaringClass.isAssignableFrom(caller)) &&
      !packageNameOf(declaringClass).equals(packageNameOf(caller)))))
   throw new IllegalAccessException("constructor not accessible: " +
          declaringClass.getName());
  Class[] parameterTypes = getParameterTypesInternal(constructor);
  int argsCnt = parameterTypes.length;
  if ((args != null ? args.length : 0) != argsCnt)
   throw new IllegalArgumentException(
          "constructor arguments number mismatch");
  byte[] argsTypecodes = argsCnt > 0 ? new byte[argsCnt] : EMPTY_BYTES;
  int longArgsLen = 0;
  int floatArgsLen = 0;
  int doubleArgsLen = 0;
  int objectArgsLen = 0;
  for (int i = 0; i < argsCnt; i++)
  {
   Class type = parameterTypes[i];
   int typecode;
   if (type == double.class)
   {
    typecode = TYPECODE_DOUBLE;
    doubleArgsLen++;
   }
    else if (type == float.class)
    {
     typecode = TYPECODE_FLOAT;
     floatArgsLen++;
    }
     else if (type == long.class)
     {
      typecode = TYPECODE_LONG;
      longArgsLen++;
     }
      else if (type == int.class)
       typecode = TYPECODE_INT;
       else if (type == short.class)
        typecode = TYPECODE_SHORT;
        else if (type == char.class)
         typecode = TYPECODE_CHAR;
         else if (type == byte.class)
          typecode = TYPECODE_BYTE;
          else if (type == boolean.class)
           typecode = TYPECODE_BOOLEAN;
           else
           {
            typecode = TYPECODE_OBJECT;
            objectArgsLen++;
           }
   argsTypecodes[i] = (byte) typecode;
  }
  int intArgsLen = argsCnt - (longArgsLen + floatArgsLen + doubleArgsLen +
                    objectArgsLen);
  int[] intArgs = EMPTY_INTS;
  if (intArgsLen > 0)
  {
   intArgs = new int[intArgsLen];
   intArgsLen = 0;
  }
  long[] longArgs = EMPTY_LONGS;
  if (longArgsLen > 0)
  {
   longArgs = new long[longArgsLen];
   longArgsLen = 0;
  }
  float[] floatArgs = EMPTY_FLOATS;
  if (floatArgsLen > 0)
  {
   floatArgs = new float[floatArgsLen];
   floatArgsLen = 0;
  }
  double[] doubleArgs = EMPTY_DOUBLES;
  if (doubleArgsLen > 0)
  {
   doubleArgs = new double[doubleArgsLen];
   doubleArgsLen = 0;
  }
  Object[] objectArgs = EMPTY_OBJECTS;
  if (objectArgsLen > 0)
  {
   objectArgs = new Object[objectArgsLen];
   objectArgsLen = 0;
  }
  for (int i = 0; i < argsCnt; i++)
  {
   int typecode = argsTypecodes[i];
   Object value = args[i];
   switch (typecode)
   {
   case TYPECODE_BOOLEAN:
    if (!(value instanceof Boolean))
     throw new IllegalArgumentException("constructor argument type mismatch");
    intArgs[intArgsLen++] = ((Boolean) value).booleanValue() ? 1 : 0;
    break;
   case TYPECODE_BYTE:
    if (!(value instanceof Byte))
     throw new IllegalArgumentException("constructor argument type mismatch");
    intArgs[intArgsLen++] = ((Byte) value).intValue();
    break;
   case TYPECODE_CHAR:
    if (!(value instanceof Character))
     throw new IllegalArgumentException("constructor argument type mismatch");
    intArgs[intArgsLen++] = ((Character) value).charValue();
    break;
   case TYPECODE_SHORT:
    int intValue;
    if (value instanceof Short)
     intValue = ((Short) value).intValue();
     else if (value instanceof Byte)
      intValue = ((Byte) value).intValue();
      else throw new IllegalArgumentException(
                  "constructor argument type mismatch");
    intArgs[intArgsLen++] = intValue;
    break;
   case TYPECODE_INT:
    intArgs[intArgsLen++] = unwrapIntValue(value);
    break;
   case TYPECODE_LONG:
    long longValue;
    if (value instanceof Long)
     longValue = ((Long) value).longValue();
     else longValue = unwrapIntValue(value);
    longArgs[longArgsLen++] = longValue;
    break;
   case TYPECODE_FLOAT:
    floatArgs[floatArgsLen++] =
     value instanceof Float || value instanceof Long ?
     ((Number) value).floatValue() : unwrapIntValue(value);
    break;
   case TYPECODE_DOUBLE:
    doubleArgs[doubleArgsLen++] =
     value instanceof Double || value instanceof Float ||
     value instanceof Long ? ((Number) value).doubleValue() :
     unwrapIntValue(value);
    break;
   default:
    if (value != null && !parameterTypes[i].isInstance(value))
     throw new IllegalArgumentException(
            "constructor argument reference type mismatch");
    objectArgs[objectArgsLen++] = value;
    break;
   }
  }
  VMAccessorJavaLang.initializeVMClass(declaringClass);
  Object obj;
  try
  {
   obj = constructNative0(declaringClass, declaringClass, argsTypecodes,
          intArgs, longArgs, floatArgs, doubleArgs, objectArgs, argsCnt,
          constructor.slot);
  }
  catch (Error e)
  {
   throw e;
  }
  catch (Throwable e)
  {
   throw new InvocationTargetException(e);
  }
  if (obj == null)
   throw new OutOfMemoryError();
  return obj;
 }

 static final Object allocateObject(Constructor constructor, Class objClass)
  throws InstantiationException
 { /* hack */ /* used for object de-serialization and by "Class" only */
  Class declaringClass = constructor.getDeclaringClass();
  if (!declaringClass.isAssignableFrom(objClass))
   throw new InstantiationException(
          "constructor declaring class not assignable from given class: " +
          objClass.getName());
  if (objClass.isInterface() ||
      (objClass.getModifiers() & Modifier.ABSTRACT) != 0 ||
      objClass == Class.class)
   throw new InstantiationException("cannot instantiate class: " +
          objClass.getName());
  int modifiers = getModifiersInternal(constructor);
  if ((modifiers & Modifier.PUBLIC) == 0 && !constructor.isAccessible() &&
      (declaringClass == objClass || (modifiers & Modifier.PRIVATE) != 0 ||
      ((modifiers & Modifier.PROTECTED) == 0 &&
      !packageNameOf(declaringClass).equals(packageNameOf(objClass)))))
   throw new InstantiationException("constructor not accessible: " +
          declaringClass.getName());
  if (getParameterTypesInternal(constructor).length != 0)
   throw new InstantiationException("constructor arguments number mismatch");
  VMAccessorJavaLang.initializeVMClass(objClass);
  Object obj;
  try
  {
   obj = constructNative0(objClass, declaringClass, EMPTY_BYTES, EMPTY_INTS,
          EMPTY_LONGS, EMPTY_FLOATS, EMPTY_DOUBLES, EMPTY_OBJECTS, 0,
          constructor.slot);
  }
  catch (Error e)
  {
   throw e;
  }
  catch (Throwable e)
  {
   VMAccessorJavaLang.throwExceptionVMClass(e);
   throw new InternalError("VMClass.throwException() returned");
  }
  if (obj == null)
   throw new OutOfMemoryError();
  return obj;
 }

 private static String packageNameOf(Class aclass)
 {
  String name = aclass.getName();
  int lastInd = name.lastIndexOf('.');
  return lastInd > 0 ? name.substring(0, lastInd) : "";
 }

 private static int unwrapIntValue(Object value)
 {
  int intValue;
  if (value instanceof Integer)
   intValue = ((Integer) value).intValue();
   else if (value instanceof Short)
    intValue = ((Short) value).intValue();
    else if (value instanceof Character)
     intValue = ((Character) value).charValue();
     else if (value instanceof Byte)
      intValue = ((Byte) value).intValue();
      else throw new IllegalArgumentException(
                  "constructor argument type mismatch");
  return intValue;
 }

 private static native Object constructNative0(Class objClass,
   Class declaringClass, byte[] argsTypecodes, int[] intArgs, long[] longArgs,
   float[] floatArgs, double[] doubleArgs, Object[] objectArgs, int argsCnt,
   int slot)
  throws Throwable; /* JVM-core */
}

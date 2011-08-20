/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/VMMethod.java --
 * VM specific methods for Java "Method" implementation.
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

final class VMMethod /* hard-coded class name */
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

 private static final Class[] EMPTY_TYPES = {};

 static
 {
  if (!"".equals("")) /* hack */
  {
   getMethodDeclClass0X(null); /* hack */
   getMethodSlot0X(null); /* hack */
   getMethodByName0X(void.class, "", "", 0); /* hack */
   getMethodBySlot0X(void.class, 0); /* hack */
  }
 }

 private VMMethod() {}

 static final int getModifiersInternal(Method method)
 {
  return method.modifiers;
 }

 static final Class getReturnType(Method method)
 {
  Class type = method.returnType;
  if (type == null) /* hack */
   throw new InternalError();
  return type;
 }

 static final Class[] getParameterTypesInternal(Method method)
 {
  return method.parameterTypes;
 }

 static final Class[] getExceptionTypesInternal(Method method)
 {
  return method.exceptionTypes;
 }

 static final String getSignature(Method method)
 {
  return method.signature;
 }

 static final Object invoke(Method method, Object obj, Object[] args,
   Class caller)
  throws IllegalAccessException, InvocationTargetException
 {
  int modifiers = getModifiersInternal(method);
  Class declaringClass = method.getDeclaringClass();
  if ((modifiers & Modifier.STATIC) == 0)
  {
   if (obj == null)
    throw new NullPointerException();
   if (!declaringClass.isInstance(obj))
    throw new IllegalArgumentException(
           "not an instance of a method declaring class: " +
           declaringClass.getName() + "." + method.getName());
  }
  if (caller != declaringClass && ((modifiers & Modifier.PUBLIC) == 0 ||
      (declaringClass.getModifiers() & Modifier.PUBLIC) == 0) &&
      caller != null && ((modifiers & Modifier.PRIVATE) != 0 ||
      (((modifiers & Modifier.PROTECTED) == 0 ||
      !declaringClass.isAssignableFrom(caller)) &&
      !packageNameOf(declaringClass).equals(packageNameOf(caller)))))
   throw new IllegalAccessException("method not accessible: " +
          declaringClass.getName() + "." + method.getName());
  Class[] parameterTypes = getParameterTypesInternal(method);
  int argsCnt = parameterTypes.length;
  if ((args != null ? args.length : 0) != argsCnt)
   throw new IllegalArgumentException("method arguments number mismatch");
  byte[] argsTypecodes = new byte[argsCnt];
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
  int[] intArgs = new int[intArgsLen > 0 ? intArgsLen : 1];
  long[] longArgs = new long[longArgsLen > 0 ? longArgsLen : 1];
  float[] floatArgs = new float[floatArgsLen > 0 ? floatArgsLen : 1];
  double[] doubleArgs = new double[doubleArgsLen > 0 ? doubleArgsLen : 1];
  Object[] objectArgs = new Object[objectArgsLen];
  intArgsLen = 0;
  longArgsLen = 0;
  floatArgsLen = 0;
  doubleArgsLen = 0;
  objectArgsLen = 0;
  for (int i = 0; i < argsCnt; i++)
  {
   int typecode = argsTypecodes[i];
   Object value = args[i];
   switch (typecode)
   {
   case TYPECODE_BOOLEAN:
    if (!(value instanceof Boolean))
     throw new IllegalArgumentException("method argument type mismatch");
    intArgs[intArgsLen++] = ((Boolean) value).booleanValue() ? 1 : 0;
    break;
   case TYPECODE_BYTE:
    if (!(value instanceof Byte))
     throw new IllegalArgumentException("method argument type mismatch");
    intArgs[intArgsLen++] = ((Byte) value).intValue();
    break;
   case TYPECODE_CHAR:
    if (!(value instanceof Character))
     throw new IllegalArgumentException("method argument type mismatch");
    intArgs[intArgsLen++] = ((Character) value).charValue();
    break;
   case TYPECODE_SHORT:
    int intValue;
    if (value instanceof Short)
     intValue = ((Short) value).intValue();
     else if (value instanceof Byte)
      intValue = ((Byte) value).intValue();
      else throw new IllegalArgumentException(
                  "method argument type mismatch");
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
    float floatValue;
    if (value instanceof Float)
     floatValue = ((Float) value).floatValue();
     else if (value instanceof Long)
      floatValue = ((Long) value).floatValue();
      else floatValue = unwrapIntValue(value);
    floatArgs[floatArgsLen++] = floatValue;
    break;
   case TYPECODE_DOUBLE:
    double doubleValue;
    if (value instanceof Double)
     doubleValue = ((Double) value).doubleValue();
     else if (value instanceof Float)
      doubleValue = ((Float) value).doubleValue();
      else if (value instanceof Long)
       doubleValue = ((Long) value).doubleValue();
       else doubleValue = unwrapIntValue(value);
    doubleArgs[doubleArgsLen++] = doubleValue;
    break;
   default:
    if (value != null && !parameterTypes[i].isInstance(value))
     throw new IllegalArgumentException(
            "method argument reference type mismatch");
    objectArgs[objectArgsLen++] = value;
    break;
   }
  }
  int allowOverride = 0;
  if ((modifiers & Modifier.STATIC) != 0)
  {
   obj = null;
   VMAccessorJavaLang.initializeVMClass(declaringClass);
  }
   else if ((modifiers & (Modifier.PRIVATE | Modifier.FINAL)) == 0 &&
            (declaringClass.getModifiers() & Modifier.FINAL) == 0)
    allowOverride = 1;
  Object value;
  try
  {
   value = invokeNative0(declaringClass, obj, argsTypecodes, intArgs,
            longArgs, floatArgs, doubleArgs, objectArgs, argsCnt, method.slot,
            allowOverride);
  }
  catch (Error e)
  {
   throw e;
  }
  catch (Throwable e)
  {
   throw new InvocationTargetException(e);
  }
  Class type = getReturnType(method);
  if (type == double.class)
   value = new Double(doubleArgs[0]);
   else if (type == float.class)
    value = new Float(floatArgs[0]);
    else if (type == long.class)
     value = new Long(longArgs[0]);
     else if (type == int.class)
      value = new Integer(intArgs[0]);
      else if (type == short.class)
       value = new Short((short) intArgs[0]);
       else if (type == char.class)
        value = new Character((char) intArgs[0]);
        else if (type == byte.class)
         value = new Byte((byte) intArgs[0]);
         else if (type == boolean.class)
          value = intArgs[0] != 0 ? Boolean.TRUE : Boolean.FALSE;
  return value;
 }

 static final Object invokeProxyHandler(InvocationHandler handler,
   Proxy proxy, Class declaringClass, int[] intArgs, long[] longArgs,
   float[] floatArgs, double[] doubleArgs, Object[] objectArgs, int slot,
   int proxySlot)
  throws Throwable
 {
  Method method = null;
  Class returnType = void.class;
  Class[] parameterTypes = EMPTY_TYPES;
  int argsCnt = 0;
  if (declaringClass != null)
  {
   Class[] methTypes = getMethodsTypes0(declaringClass)[slot];
   if (methTypes != null)
   {
    byte[][] methodsDims;
    byte[] dims = (methodsDims = getMethodsDims0(declaringClass)) != null ?
                   methodsDims[slot] : null;
    argsCnt = methTypes.length - 1;
    returnType = makeArrayClass(methTypes, dims, argsCnt);
    if (argsCnt > 0)
     parameterTypes = dims != null ? makeParameterTypes(argsCnt, methTypes,
                       dims) : copyTypes(methTypes, argsCnt);
   }
   Class[][] methodsThrows = getMethodsThrows0(declaringClass);
   Class[] exceptionTypes;
   short[] modifiers = getMethodsModifiers0(declaringClass);
   String[] signatures = getMethodsSignature0(declaringClass);
   method = new Method(declaringClass, getMethodsName0(declaringClass)[slot],
             slot, returnType, parameterTypes, methodsThrows != null &&
             (exceptionTypes = methodsThrows[slot]) != null ? exceptionTypes :
             EMPTY_TYPES, modifiers != null ? modifiers[slot] & 0xffff :
             declaringClass.isInterface() ? Modifier.PUBLIC |
             Modifier.ABSTRACT : Modifier.PUBLIC, signatures != null ?
             signatures[slot] : null);
  }
  Object[] args = null;
  if (argsCnt > 0)
  {
   args = new Object[argsCnt];
   int intArgsPos = 0;
   int longArgsPos = 0;
   int floatArgsPos = 0;
   int doubleArgsPos = 0;
   int objectArgsPos = 0;
   for (int i = 0; i < argsCnt; i++)
   {
    Class type = parameterTypes[i];
    args[i] =
     type == double.class ? new Double(doubleArgs[doubleArgsPos++]) :
     type == float.class ? new Float(floatArgs[floatArgsPos++]) :
     type == long.class ? new Long(longArgs[longArgsPos++]) :
     type == int.class ? new Integer(intArgs[intArgsPos++]) :
     type == short.class ? new Short((short) intArgs[intArgsPos++]) :
     type == char.class ? new Character((char) intArgs[intArgsPos++]) :
     type == byte.class ? new Byte((byte) intArgs[intArgsPos++]) :
     type == boolean.class ? (intArgs[intArgsPos++] != 0 ?
     Boolean.TRUE : Boolean.FALSE) : objectArgs[objectArgsPos++];
   }
  }
  Object value;
  try
  {
   value = handler.invoke(proxy, method, args);
  }
  catch (Error e)
  {
   throw e;
  }
  catch (RuntimeException e)
  {
   throw e;
  }
  catch (Throwable e)
  {
   Class[] exceptionTypes = method.exceptionTypes;
   if (proxySlot >= 0)
   {
    Class[][] methodsThrows = getMethodsThrows0(proxy.getClass());
    if (methodsThrows == null ||
        (exceptionTypes = methodsThrows[proxySlot]) == null)
     exceptionTypes = EMPTY_TYPES;
   }
   int cnt = exceptionTypes.length;
   if (cnt != 0)
   {
    Class excClass = e.getClass();
    for (int i = 0; i < cnt; i++)
     if (exceptionTypes[i].isAssignableFrom(excClass))
      throw e;
   }
   throw new UndeclaredThrowableException(e);
  }
  if (returnType == void.class)
   value = null;
   else
   {
    Class wrapperClass = returnType;
    if (returnType == double.class)
     wrapperClass = Double.class;
     else if (returnType == float.class)
      wrapperClass = Float.class;
      else if (returnType == long.class)
       wrapperClass = Long.class;
       else if (returnType == int.class)
        wrapperClass = Integer.class;
        else if (returnType == short.class)
         wrapperClass = Short.class;
         else if (returnType == char.class)
          wrapperClass = Character.class;
          else if (returnType == byte.class)
           wrapperClass = Byte.class;
           else if (returnType == boolean.class)
            wrapperClass = Boolean.class;
    if (value != null && !wrapperClass.isInstance(value))
     throw new ClassCastException(value.getClass().getName());
    if (wrapperClass != returnType)
    {
     if (value == null)
      throw new NullPointerException();
     if (returnType == double.class)
      doubleArgs[0] = ((Double) value).doubleValue();
      else if (returnType == float.class)
       floatArgs[0] = ((Float) value).floatValue();
       else if (returnType == long.class)
        longArgs[0] = ((Long) value).longValue();
        else intArgs[0] =
              returnType == int.class ? ((Integer) value).intValue() :
              returnType == short.class ? ((Short) value).intValue() :
              returnType == char.class ? ((Character) value).charValue() :
              returnType == byte.class ? ((Byte) value).intValue() :
              ((Boolean) value).booleanValue() ? 1 : 0;
     value = null;
    }
   }
  return value;
 }

 static final Constructor[] getDeclaredConstructors(Class klass,
   boolean publicOnly)
 {
  Class[][] methodsTypes;
  int count;
  if (klass.isInterface() || klass.isPrimitive() || klass.isArray() ||
      (methodsTypes = getMethodsTypes0(klass)) == null ||
      (count = methodsTypes.length) == 0)
   return new Constructor[0];
  String[] names = getMethodsName0(klass);
  short[] modifiers = getMethodsModifiers0(klass);
  if (modifiers == null)
   publicOnly = false;
  int cnt = count;
  if (names != null)
  {
   for (int i = 0; i < count; i++)
    if ((names[i] != null && names[i].length() > 0) ||
        (publicOnly && (modifiers[i] & Modifier.PUBLIC) == 0))
     cnt--;
  }
   else
   {
    if (publicOnly)
     for (int i = 0; i < count; i++)
      if ((modifiers[i] & Modifier.PUBLIC) == 0)
       cnt--;
   }
  Constructor[] constructors = new Constructor[cnt];
  if (cnt > 0)
  {
   byte[][] methodsDims = getMethodsDims0(klass);
   Class[][] methodsThrows = getMethodsThrows0(klass);
   String[] signatures = getMethodsSignature0(klass);
   cnt = 0;
   for (int i = 0; i < count; i++)
    if ((names == null || names[i] == null || names[i].length() == 0) &&
        (!publicOnly || (modifiers[i] & Modifier.PUBLIC) != 0))
    {
     Class[] ctorTypes = methodsTypes[i];
     Class[] parameterTypes = EMPTY_TYPES;
     if (ctorTypes != null)
     {
      byte[] dims = methodsDims != null ? methodsDims[i] : null;
      parameterTypes = ctorTypes;
      if (dims != null)
       parameterTypes = makeParameterTypes(ctorTypes.length, ctorTypes, dims);
     }
     Class[] exceptionTypes = methodsThrows != null ? methodsThrows[i] : null;
     constructors[cnt++] = new Constructor(klass, i, parameterTypes,
                            exceptionTypes != null ? exceptionTypes :
                            EMPTY_TYPES, modifiers != null ?
                            modifiers[i] & 0xffff : Modifier.PUBLIC,
                            signatures != null ? signatures[i] : null);
    }
  }
  return constructors;
 }

 static final Method[] getDeclaredMethods(Class klass, boolean publicOnly)
 {
  String[] names;
  int count;
  if (klass.isPrimitive() || klass.isArray() ||
      (names = getMethodsName0(klass)) == null || (count = names.length) == 0)
   return new Method[0];
  short[] modifiers = getMethodsModifiers0(klass);
  if (modifiers == null)
   publicOnly = false;
  int cnt = 0;
  for (int i = 0; i < count; i++)
   if (names[i] != null && names[i].length() != 0 &&
       (!publicOnly || (modifiers[i] & Modifier.PUBLIC) != 0))
    cnt++;
  Method[] methods = new Method[cnt];
  if (cnt != 0)
  {
   Class[][] methodsTypes = getMethodsTypes0(klass);
   byte[][] methodsDims = getMethodsDims0(klass);
   Class[][] methodsThrows = getMethodsThrows0(klass);
   String[] signatures = getMethodsSignature0(klass);
   int mods = klass.isInterface() ? Modifier.PUBLIC | Modifier.ABSTRACT :
               Modifier.PUBLIC;
   cnt = 0;
   for (int i = 0; i < count; i++)
    if (!publicOnly || (modifiers[i] & Modifier.PUBLIC) != 0)
    {
     String name = names[i];
     if (name != null && name.length() != 0)
     {
      Class[] methTypes = methodsTypes[i];
      Class returnType = void.class;
      Class[] parameterTypes = EMPTY_TYPES;
      if (methTypes != null)
      {
       byte[] dims = methodsDims != null ? methodsDims[i] : null;
       int argsCnt = methTypes.length - 1;
       returnType = makeArrayClass(methTypes, dims, argsCnt);
       if (argsCnt > 0)
        parameterTypes = dims != null ? makeParameterTypes(argsCnt, methTypes,
                          dims) : copyTypes(methTypes, argsCnt);
      }
      Class[] exceptionTypes = methodsThrows != null ?
                                methodsThrows[i] : null;
      methods[cnt++] = new Method(klass, name, i, returnType, parameterTypes,
                        exceptionTypes != null ? exceptionTypes : EMPTY_TYPES,
                        modifiers != null ? modifiers[i] & 0xffff : mods,
                        signatures != null ? signatures[i] : null);
     }
    }
  }
  return methods;
 }

 static final Constructor getDeclaredConstructor(Class klass, Class[] types)
 { /* used by VM classes only */
  Class[][] methodsTypes;
  int count;
  if (!klass.isInterface() && !klass.isPrimitive() && !klass.isArray() &&
      (methodsTypes = getMethodsTypes0(klass)) != null &&
      (count = methodsTypes.length) > 0)
  {
   String[] names = getMethodsName0(klass);
   byte[][] methodsDims = null;
   if (types != null)
    methodsDims = getMethodsDims0(klass);
    else types = EMPTY_TYPES;
   for (int i = 0; i < count; i++)
    if (names == null || names[i] == null || names[i].length() == 0)
    {
     Class[] ctorTypes;
     if ((ctorTypes = methodsTypes[i]) == null)
      ctorTypes = EMPTY_TYPES;
     byte[] dims;
     int argsCnt = ctorTypes.length;
     if (types.length == argsCnt && matchMethodTypes(argsCnt, ctorTypes,
         dims = methodsDims != null ? methodsDims[i] : null, types))
     {
      Class[] parameterTypes = ctorTypes;
      if (dims != null)
       parameterTypes = makeParameterTypes(argsCnt, ctorTypes, dims);
      Class[][] methodsThrows = getMethodsThrows0(klass);
      Class[] exceptionTypes = methodsThrows != null ?
                                methodsThrows[i] : null;
      short[] modifiers;
      String[] signatures;
      return new Constructor(klass, i, parameterTypes,
              exceptionTypes != null ? exceptionTypes : EMPTY_TYPES,
              (modifiers = getMethodsModifiers0(klass)) != null ?
              modifiers[i] & 0xffff : Modifier.PUBLIC,
              (signatures = getMethodsSignature0(klass)) != null ?
              signatures[i] : null);
     }
    }
  }
  return null;
 }

 static final Method getDeclaredMethod(Class klass, String methodName,
   Class[] types)
 { /* used by VM classes only */
  String[] names;
  int count;
  if (methodName.length() > 0 && !klass.isPrimitive() && !klass.isArray() &&
      (names = getMethodsName0(klass)) != null && (count = names.length) > 0)
  {
   Class[][] methodsTypes = getMethodsTypes0(klass);
   byte[][] methodsDims = getMethodsDims0(klass);
   if (types == null)
    types = EMPTY_TYPES;
   for (int i = 0; i < count; i++)
    if (methodName.equals(names[i]))
    {
     Class[] methTypes = methodsTypes[i];
     int argsCnt = 0;
     if (methTypes != null)
      argsCnt = methTypes.length - 1;
     byte[] dims;
     if (types.length == argsCnt && matchMethodTypes(argsCnt, methTypes,
         dims = methodsDims != null ? methodsDims[i] : null, types))
     {
      Class returnType = void.class;
      Class[] parameterTypes = EMPTY_TYPES;
      if (methTypes != null)
      {
       returnType = makeArrayClass(methTypes, dims, argsCnt);
       if (argsCnt > 0)
        parameterTypes = dims != null ? makeParameterTypes(argsCnt, methTypes,
                          dims) : copyTypes(methTypes, argsCnt);
      }
      Class[][] methodsThrows = getMethodsThrows0(klass);
      Class[] exceptionTypes = methodsThrows != null ?
                                methodsThrows[i] : null;
      short[] modifiers;
      String[] signatures;
      return new Method(klass, methodName, i, returnType, parameterTypes,
              exceptionTypes != null ? exceptionTypes : EMPTY_TYPES,
              (modifiers = getMethodsModifiers0(klass)) != null ?
              modifiers[i] & 0xffff : klass.isInterface() ?
              Modifier.PUBLIC | Modifier.ABSTRACT : Modifier.PUBLIC,
              (signatures = getMethodsSignature0(klass)) != null ?
              signatures[i] : null);
     }
    }
  }
  return null;
 }

 static final Class getMethodDeclClass0X(Object methodObj)
  throws ClassCastException
 { /* called from native code */
  Class declaringClass = methodObj instanceof Constructor ?
                          ((Constructor) methodObj).getDeclaringClass() :
                          ((Method) methodObj).getDeclaringClass();
  VMAccessorJavaLang.initializeVMClass(declaringClass);
  return declaringClass;
 }

 static final int getMethodSlot0X(Object methodObj)
 { /* called from native code */
  return methodObj instanceof Constructor ? ((Constructor) methodObj).slot :
          ((Method) methodObj).slot;
 }

 static final Object getMethodByName0X(Class klass, String name,
   String paramSig, int isStatic)
 { /* called from native code */
  Class[] sigTypes;
  if (klass.isPrimitive() || klass.isArray() || name.length() == 0 ||
      (sigTypes = decodeMethodSig(paramSig, klass)) == null)
   return null;
  if (name.equals("<init>"))
   return isStatic == 0 && !klass.isInterface() &&
           sigTypes[sigTypes.length - 1] == void.class ?
           getMethodByNameInner(klass, null, sigTypes, false) : null;
  if (name.equals("<clinit>"))
  {
   if (isStatic != 0 && sigTypes.length == 1 && sigTypes[0] == void.class)
    do
    {
     if (VMAccessorJavaLang.hasClassInitializerVMClass(klass))
      return new Method(klass, "<clinit>", -1, void.class, EMPTY_TYPES,
              EMPTY_TYPES, Modifier.STATIC | Modifier.SYNTHETIC, null);
    } while ((klass = klass.getSuperclass()) != null);
   return null;
  }
  return isStatic == 0 || !klass.isInterface() ?
          getMethodByNameInner(klass, name, sigTypes, isStatic != 0) : null;
 }

 static final Object getMethodBySlot0X(Class klass, long slot)
 { /* called from native code */
  Class[][] methodsTypes;
  int count;
  if (slot < 0 || klass.isPrimitive() || klass.isArray() ||
      (methodsTypes = getMethodsTypes0(klass)) == null ||
      (count = methodsTypes.length) <= slot)
   return null;
  String[] names = getMethodsName0(klass);
  String name = null;
  int intSlot = (int) slot;
  if (names != null && (name = names[intSlot]) != null && name.length() == 0)
   name = null;
  Class[] methTypes = methodsTypes[intSlot];
  Class returnType = void.class;
  Class[] parameterTypes = EMPTY_TYPES;
  if (methTypes != null)
  {
   int argsCnt = methTypes.length;
   byte[][] methodsDims;
   byte[] dims = (methodsDims = getMethodsDims0(klass)) != null ?
                  methodsDims[intSlot] : null;
   if (name != null)
   {
    argsCnt--;
    returnType = makeArrayClass(methTypes, dims, argsCnt);
   }
   if (argsCnt > 0)
    parameterTypes = dims != null ? makeParameterTypes(argsCnt, methTypes,
                      dims) : name != null ? copyTypes(methTypes, argsCnt) :
                      methTypes;
  }
  Class[][] methodsThrows = getMethodsThrows0(klass);
  Class[] exceptionTypes = methodsThrows != null ?
                            methodsThrows[intSlot] : null;
  short[] modifiers = getMethodsModifiers0(klass);
  String[] signatures = getMethodsSignature0(klass);
  String signature = signatures != null ? signatures[intSlot] : null;
  if (exceptionTypes == null)
   exceptionTypes = EMPTY_TYPES;
  if (name != null)
   return new Method(klass, name, intSlot, returnType, parameterTypes,
           exceptionTypes, modifiers != null ? modifiers[intSlot] & 0xffff :
           klass.isInterface() ? Modifier.PUBLIC | Modifier.ABSTRACT :
           Modifier.PUBLIC, signature);
  return new Constructor(klass, intSlot, parameterTypes, exceptionTypes,
          modifiers != null ? modifiers[intSlot] & 0xffff : Modifier.PUBLIC,
          signature);
 }

 private static Object getMethodByNameInner(Class klass, String name,
   Class[] sigTypes, boolean isStatic)
 {
  int argsCnt = sigTypes.length - 1;
  Class returnType = sigTypes[argsCnt];
  Class aclass = klass;
  Object method = null;
  do
  {
   Class[][] methodsTypes;
   String[] names;
   int count;
   if ((methodsTypes = getMethodsTypes0(aclass)) != null &&
       (count = methodsTypes.length) != 0 &&
       ((names = getMethodsName0(aclass)) != null || name == null))
   {
    byte[][] methodsDims = getMethodsDims0(aclass);
    short[] modifiers = getMethodsModifiers0(aclass);
    Class[] methTypes;
    int mods = aclass.isInterface() ? Modifier.PUBLIC | Modifier.ABSTRACT :
                Modifier.PUBLIC;
    for (int i = 0; i < count; i++)
     if ((name != null ? name.equals(names[i]) :
         names == null || names[i] == null || names[i].length() == 0) &&
         ((methTypes = methodsTypes[i]) != null ? methTypes.length -
         (name != null ? 1 : 0) == argsCnt : argsCnt == 0) && (name != null &&
         methTypes != null ? makeArrayClass(methTypes, methodsDims != null ?
         methodsDims[i] : null, argsCnt) : void.class) == returnType &&
         (((modifiers != null ? (mods = modifiers[i] & 0xffff) :
         mods) & Modifier.STATIC) != 0) == isStatic)
     {
      byte[] dims = methodsDims != null && argsCnt > 0 ?
                     methodsDims[i] : null;
      if (matchMethodTypes(argsCnt, methTypes, dims, sigTypes))
      {
       Class[] parameterTypes = EMPTY_TYPES;
       if (argsCnt > 0 && methTypes != null)
        parameterTypes = dims != null || name != null ?
                          copyTypes(sigTypes, argsCnt) : methTypes;
       Class[][] methodsThrows;
       Class[] exceptionTypes;
       if ((methodsThrows = getMethodsThrows0(aclass)) == null ||
           (exceptionTypes = methodsThrows[i]) == null)
        exceptionTypes = EMPTY_TYPES;
       String[] signatures = getMethodsSignature0(aclass);
       String signature = signatures != null ? signatures[i] : null;
       Object m;
       if (name != null)
        m = new Method(aclass, names[i], i, returnType, parameterTypes,
             exceptionTypes, mods, signature);
        else m = new Constructor(aclass, i, parameterTypes, exceptionTypes,
                  mods, signature);
       if (aclass == klass ||
           (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0 ||
           ((mods & Modifier.PRIVATE) == 0 &&
           packageNameOf(aclass).equals(packageNameOf(klass))))
        return m;
       if (method == null)
        method = m;
       break;
      }
     }
   }
   if (name == null)
    break;
   if (!isStatic)
   {
    Class[] interfaces = VMAccessorJavaLang.getInterfacesInnerVMClass(aclass);
    int len = interfaces.length;
    for (int i = 0; i < len; i++)
    {
     Object m = getMethodByNameInner(interfaces[i], name, sigTypes, false);
     if (m != null)
      return m;
    }
   }
   aclass = aclass.getSuperclass();
  } while (aclass != null);
  return method;
 }

 private static Class[] decodeMethodSig(String paramSig, Class klass)
 {
  int len = paramSig.length();
  if (len <= 1 || paramSig.charAt(0) != '(')
   return null;
  int pos = 1;
  int count = 0;
  char ch;
  while ((ch = paramSig.charAt(pos)) != ')')
  {
   if (ch != '[')
   {
    if (ch == 'L' && (pos = paramSig.indexOf(';', pos + 1)) < 0)
     return null;
    count++;
   }
   if (++pos >= len)
    return null;
  }
  Class[] sigTypes = new Class[count + 1];
  pos = 1;
  for (int i = 0; i < count; i++)
  {
   int next = pos;
   while ((ch = paramSig.charAt(next)) == '[')
    next++;
   next = (ch == 'L' ? paramSig.indexOf(';', next + 1) : next) + 1;
   Class type = VMAccessorJavaLang.classForSigVMClass(paramSig.substring(pos,
                 next), klass);
   if (type == null || type == void.class)
    return null;
   sigTypes[i] = type;
   pos = next;
  }
  return (sigTypes[count] = VMAccessorJavaLang.classForSigVMClass(
          paramSig.substring(pos + 1), klass)) != null ? sigTypes : null;
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
                  "method argument type mismatch");
  return intValue;
 }

 private static String packageNameOf(Class aclass)
 {
  String name = aclass.getName();
  int lastInd = name.lastIndexOf('.');
  return lastInd > 0 ? name.substring(0, lastInd) : "";
 }

 private static Class[] copyTypes(Class[] types, int count)
 {
  Class[] destTypes = new Class[count];
  for (int i = 0; i < count; i++) /* hack */
   destTypes[i] = types[i];
  return destTypes;
 }

 private static boolean matchMethodTypes(int argsCnt, Class[] methTypes,
   byte[] dims, Class[] types)
 {
  for (int i = 0; i < argsCnt; i++)
   if (makeArrayClass(methTypes, dims, i) != types[i])
    return false;
  return true;
 }

 private static Class[] makeParameterTypes(int argsCnt, Class[] methTypes,
   byte[] dims)
 {
  Class[] parameterTypes = new Class[argsCnt];
  for (int i = 0; i < argsCnt; i++)
   parameterTypes[i] = VMAccessorJavaLang.arrayClassOfVMClass(methTypes[i],
                        dims[i] & 0xff);
  return parameterTypes;
 }

 private static Class makeArrayClass(Class[] types, byte[] dims, int index)
 {
  return dims != null ? VMAccessorJavaLang.arrayClassOfVMClass(types[index],
          dims[index] & 0xff) : types[index];
 }

 private static native String[] getMethodsName0(
   Class klass); /* JVM-core */ /* const data */

 private static native byte[][] getMethodsDims0(
   Class klass); /* JVM-core */ /* const data of const data */

 private static native Class[][] getMethodsThrows0(
   Class klass); /* JVM-core */ /* const data of const data */

 private static native short[] getMethodsModifiers0(
   Class klass); /* JVM-core */ /* const data */

 private static native String[] getMethodsSignature0(
   Class klass); /* JVM-core */ /* const data */

 private static native Class[][] getMethodsTypes0(
   Class klass); /* JVM-core */ /* const data of const data */

 private static native Object invokeNative0(Class declaringClass, Object obj,
   byte[] argsTypecodes, int[] intArgs, long[] longArgs, float[] floatArgs,
   double[] doubleArgs, Object[] objectArgs, int argsCnt, int slot,
   int allowOverride)
  throws Throwable; /* JVM-core */
}

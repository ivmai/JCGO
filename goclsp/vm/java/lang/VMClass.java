/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMClass.java --
 * VM specific methods for Java "Class" class.
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

import java.lang.annotation.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.VMAccessorJavaLangReflect;

final class VMClass /* hard-coded class name */
{

 final static class IdentityHashMap
 { /* used by VM classes only */

  private Object[] table = new Object[13 << 1];

  private int size;

  IdentityHashMap() {}

  Object get(Object key)
  {
   Object[] table = this.table;
   int i = indexFor(key, table);
   return table[i] == key ? table[i + 1] : null;
  }

  Object put(Object key, Object value)
  {
   Object[] table = this.table;
   int i = indexFor(key, table);
   if (table[i] == key)
   {
    Object oldValue = table[i + 1];
    table[i + 1] = value;
    return oldValue;
   }
   if (value != null)
   {
    if ((table.length >>> 3) * 3 < size)
    {
     Object[] oldTable = table;
     int j = oldTable.length;
     table = new Object[(j + 1) << 1];
     int count = 0;
     Object oldKey;
     Object oldValue;
     while ((j -= 2) >= 0)
      if ((oldKey = oldTable[j]) != null &&
          (oldValue = oldTable[j + 1]) != null)
      {
       i = indexFor(oldKey, table);
       table[i] = oldKey;
       table[i + 1] = oldValue;
       count++;
      }
     this.table = table;
     size = count;
     i = indexFor(key, table);
    }
    size++;
    table[i] = key;
    table[i + 1] = value;
   }
   return null;
  }

  private static int indexFor(Object key, Object[] table)
  {
   int i = VMSystem.identityHashCode(key);
   i += ~(i << 9);
   if ((i = (((i >>> 14) ^ i) % (table.length >> 1)) << 1) < 0)
    i = -i;
   Object oldKey;
   while ((oldKey = table[i]) != key && oldKey != null)
    if ((i -= 2) < 0)
     i = table.length - 2;
   return i;
  }
 }

 private static final class StaticData
 {

  static final IdentityHashMap classBasicCtors = new IdentityHashMap();

  static final IdentityHashMap classSigners = new IdentityHashMap();

  static final IdentityHashMap arrayClasses = createArrayClassesMap();

  private static IdentityHashMap createArrayClassesMap()
  {
   IdentityHashMap arrayClasses = new IdentityHashMap();
   arrayClasses.put(boolean.class, boolean[].class); /* hack */
   arrayClasses.put(byte.class, byte[].class);
   arrayClasses.put(char.class, char[].class);
   arrayClasses.put(short.class, short[].class);
   arrayClasses.put(int.class, int[].class);
   arrayClasses.put(long.class, long[].class);
   arrayClasses.put(float.class, float[].class);
   arrayClasses.put(double.class, double[].class);
   return arrayClasses;
  }
 }

 private static final int MODIFIER_PUBLIC = 0x1;
 private static final int MODIFIER_PRIVATE = 0x2;
 private static final int MODIFIER_PROTECTED = 0x4;
 private static final int MODIFIER_FINAL = 0x10;

 private static final int MODIFIER_VOLATILE = 0x40;
 private static final int MODIFIER_TRANSIENT = 0x80;
 private static final int MODIFIER_NATIVE = 0x100;
 private static final int MODIFIER_INTERFACE = 0x200;
 private static final int MODIFIER_ABSTRACT = 0x400;

 private VMClass() {}

 static boolean isInstance(Class klass, Object obj)
 {
  return obj != null && isAssignableFrom(klass, obj.getClass());
 }

 static boolean isAssignableFrom(Class klass, Class aclass)
 {
  if (aclass == null)
   throw new NullPointerException();
  if (klass == aclass)
   return true;
  Class clazz;
  while ((clazz = getComponentType(klass)) != null)
  {
   klass = clazz;
   if ((aclass = getComponentType(aclass)) == null)
    return false;
  }
  if (getSuperclass(klass) != null)
  {
   do
   {
    aclass = getSuperclass(aclass);
    if (klass == aclass)
     return true;
   } while (aclass != null);
  }
   else
   {
    if (isInterface(klass))
    {
     do
     {
      if (isImplementedBy(klass, aclass))
       return true;
     } while ((aclass = getSuperclass(aclass)) != null);
    }
     else if (!isPrimitive(klass) && !isPrimitive(aclass))
      return true;
   }
  return false;
 }

 static boolean isInterface(Class klass)
 {
  return (klass.modifiers & MODIFIER_INTERFACE) != 0;
 }

 static String getName(Class klass)
 {
  /* return klass.name; */
  throw new InternalError(); /* hack */
 }

 static boolean isPrimitive(Class klass)
 {
  return klass.modifiers == (MODIFIER_PUBLIC | MODIFIER_FINAL |
          MODIFIER_ABSTRACT) && klass.superclass == null; /* hack */
 }

 static Class getSuperclass(Class klass)
 {
  return (klass.modifiers & ~(MODIFIER_PUBLIC | MODIFIER_PRIVATE | /* hack */
          MODIFIER_PROTECTED)) == (MODIFIER_FINAL | MODIFIER_ABSTRACT) ?
          (klass.superclass != null ? Object.class : null) : klass.superclass;
 }

 static Class[] getInterfaces(Class klass)
 {
  /* return klass.interfaces.clone(); */
  throw new InternalError(); /* hack */
 }

 static boolean isArray(Class klass)
 {
  return (klass.modifiers & ~(MODIFIER_PUBLIC | MODIFIER_PRIVATE |
          MODIFIER_PROTECTED)) == (MODIFIER_FINAL | MODIFIER_ABSTRACT) &&
          klass.superclass != null; /* hack */
 }

 static Class getComponentType(Class klass)
 {
  return (klass.modifiers & ~(MODIFIER_PUBLIC | MODIFIER_PRIVATE |
          MODIFIER_PROTECTED)) == (MODIFIER_FINAL | MODIFIER_ABSTRACT) ?
          klass.superclass : null; /* hack */
 }

 static int getModifiers(Class klass, boolean ignoreInnerClassesAttrib)
 {
  int modifiers = klass.modifiers;
  if (ignoreInnerClassesAttrib && (modifiers & MODIFIER_PRIVATE) == 0)
   while ((klass = getDeclaringClass(klass)) != null)
   {
    int outerMods = klass.modifiers;
    if ((outerMods & MODIFIER_PRIVATE) != 0)
    {
     modifiers = (modifiers & ~(MODIFIER_PUBLIC | MODIFIER_PROTECTED)) |
                  MODIFIER_PRIVATE;
     break;
    }
     else if ((outerMods & MODIFIER_PROTECTED) != 0)
      modifiers &= ~MODIFIER_PUBLIC;
      else if ((outerMods & MODIFIER_PUBLIC) == 0)
       modifiers &= ~(MODIFIER_PUBLIC | MODIFIER_PROTECTED);
   }
  return modifiers & ~(MODIFIER_VOLATILE | MODIFIER_TRANSIENT |
          MODIFIER_NATIVE);
 }

 static Class getDeclaringClass(Class klass)
 {
  String name = klass.getName();
  int ofs = name.lastIndexOf('$');
  return ofs > 0 && name.charAt(0) != '[' && name.length() - 1 > ofs &&
          !isAsciiDigit(name.charAt(ofs + 1)) ? loadClassResolve(
          name.substring(0, ofs), getClassLoaderInner(klass)) : null;
 }

 static Class[] getDeclaredClasses(Class klass, boolean publicOnly)
 {
  String name = klass.getName();
  String baseName = name.concat("$");
  Class[] classes = new Class[0];
  Class aclass;
  ClassLoader loader = getClassLoader(klass);
  int count = 0;
  while ((aclass = VMClassLoader.getNextInnerClass(name)) != null)
  {
   name = aclass.getName();
   if (!name.regionMatches(0, baseName, 0, baseName.length()))
    break;
   if (getDeclaringClass(aclass) == klass &&
       (!publicOnly || (aclass.modifiers & MODIFIER_PUBLIC) != 0))
   {
    if (loader != null)
     loader.resolveClass(aclass);
     else VMClassLoader.resolveClass(aclass);
    if (classes.length <= count)
    {
     Class[] newClasses = new Class[(count >> 1) + count + 3];
     if (count != 0)
      VMSystem.arraycopy(classes, 0, newClasses, 0, count);
     classes = newClasses;
    }
    classes[count++] = aclass;
   }
  }
  if (classes.length > count)
  {
   Class[] newClasses = new Class[count];
   VMSystem.arraycopy(classes, 0, newClasses, 0, count);
   classes = newClasses;
  }
  return classes;
 }

 static Field[] getDeclaredFields(Class klass, boolean publicOnly)
 {
  return VMAccessorJavaLangReflect.getDeclaredFieldsVMField(klass,
          publicOnly);
 }

 static Method[] getDeclaredMethods(Class klass, boolean publicOnly)
 {
  return VMAccessorJavaLangReflect.getDeclaredMethodsVMMethod(klass,
          publicOnly);
 }

 static Constructor[] getDeclaredConstructors(Class klass, boolean publicOnly)
 {
  return VMAccessorJavaLangReflect.getDeclaredConstructorsVMMethod(klass,
          publicOnly);
 }

 static ClassLoader getClassLoader(Class klass)
 {
  if (klass == null && /* hack */
      ClassLoader.StaticData.systemClassLoader == null) /* hack */
   return null;
  return getClassLoaderInner(klass);
 }

 static Class forName(String name, boolean initialize, ClassLoader loader)
  throws ClassNotFoundException
 {
  if (name == null)
   throw new NullPointerException();
  Class aclass;
  if (name.length() > 0 && name.charAt(0) == '[')
   aclass = loadArrayClass(name, loader);
   else
   {
    if (loader != null)
    {
     aclass = loader.loadClass(name);
     if (aclass != null)
      loader.resolveClass(aclass);
    }
     else aclass = VMClassLoader.loadClass(name, true);
    if (aclass == null)
     throw new ClassNotFoundException(name);
    if (initialize)
     initialize(aclass);
   }
  return aclass;
 }

 static void throwException(Throwable throwable)
 {
  if (throwable instanceof Error)
   throw (Error) throwable;
  if (throwable instanceof RuntimeException)
   throw (RuntimeException) throwable;
  if (throwable == null)
   throw new NullPointerException();
  throwException0(throwable);
 }

 static String getSimpleName(Class klass)
 {
  Class aclass = getComponentType(klass);
  if (aclass != null)
   return getSimpleName(aclass).concat("[]");
  String name = klass.getName();
  name = name.substring(name.lastIndexOf('.') + 1);
  int ofs = name.lastIndexOf('$') + 1;
  while (name.length() > ofs && isAsciiDigit(name.charAt(ofs)))
   ofs++;
  return name.substring(ofs);
 }

 static String getCanonicalName(Class klass)
 {
  Class aclass = getComponentType(klass);
  if (aclass != null)
  {
   String name = getCanonicalName(aclass);
   return name != null ? name.concat("[]") : null;
  }
  aclass = getDeclaringClass(klass);
  if (aclass != null)
  {
   String name = getCanonicalName(aclass);
   return name != null ? name.concat(".").concat(getSimpleName(klass)) : null;
  }
  return isLocalClass(klass) || isAnonymousClass(klass) ? null :
          klass.getName();
 }

 static Annotation[] getDeclaredAnnotations(Class klass)
 {
  /* not implemented */
  return new Annotation[0];
 }

 static Class getEnclosingClass(Class klass)
 {
  /* not implemented correctly for anonymous and local classes */
  String name = klass.getName();
  int ofs = name.lastIndexOf('$');
  return ofs > 0 && name.charAt(0) != '[' && name.length() - 1 > ofs ?
          loadClassResolve(name.substring(0, ofs),
          getClassLoaderInner(klass)) : null;
 }

 static Constructor getEnclosingConstructor(Class klass)
 {
  /* not implemented */
  return null;
 }

 static Method getEnclosingMethod(Class klass)
 {
  /* not implemented */
  return null;
 }

 static native String getClassSignature(Class klass); /* JVM-core */

 static boolean isAnonymousClass(Class klass)
 {
  return getSimpleName(klass).length() == 0;
 }

 static boolean isLocalClass(Class klass)
 {
  int ofs;
  String name = klass.getName();
  return !isArray(klass) && (ofs = getSimpleName(klass).length()) > 0 &&
          (ofs = name.length() - ofs - 1) > 1 &&
          isAsciiDigit(name.charAt(ofs));
 }

 static boolean isMemberClass(Class klass)
 {
  return getDeclaringClass(klass) != null;
 }

 static final Class arrayClassOf0X(Class klass,
   int dims) /* hard-coded method signature */
 { /* called from native code */
  if (dims > 0)
  {
   if (klass == void.class)
    throw new InternalError();
   Class origClass = klass;
   int absDims = dims;
   synchronized (StaticData.arrayClasses)
   {
    do
    {
     Class aclass = (Class) StaticData.arrayClasses.get(klass);
     if (aclass == null)
      break;
     klass = aclass;
    } while (--dims > 0);
    if (dims > 0)
    {
     absDims -= dims;
     while ((origClass = getComponentType(origClass)) != null)
      absDims++;
     String className;
     if ((className = klass.name) == null) /* hack */
      className = "<UnknownClass>";
     origClass = boolean[].class; /* hack */
     Class[] interfaces = origClass.interfaces;
     int modifiers =
      (klass.modifiers & (MODIFIER_PUBLIC | MODIFIER_PRIVATE |
      MODIFIER_PROTECTED)) | (MODIFIER_FINAL | MODIFIER_ABSTRACT);
     do
     {
      Object vmdata = vmdataForObjArray0(++absDims);
      if (vmdata == null)
       throw new OutOfMemoryError("array class dimensions limit exceeded");
      className = absDims > 1 ? "[".concat(className) :
                   "[L".concat(className).concat(";");
      Class aclass = new Class(vmdata, className, klass, interfaces,
                      modifiers);
      StaticData.arrayClasses.put(klass, aclass);
      klass = aclass;
     } while (--dims > 0);
    }
   }
  }
  if (klass == null) /* hack */
   throw new InternalError();
  return klass;
 }

 static final Class classForSig(String sig, Class klass)
 { /* used by VM classes only */
  int nameLen = sig.length();
  Class aclass = null;
  if (nameLen > 0)
  {
   char ch = sig.charAt(0);
   if (nameLen == 1)
    return VMClassLoader.getPrimitiveClass(ch);
   if (sig.indexOf('.', 0) < 0)
   {
    if (ch == 'L')
    {
     if (sig.charAt(nameLen - 1) != ';')
      return null;
     sig = sig.substring(1, nameLen - 1);
    }
     else if (ch != '[')
      return null;
    try
    {
     aclass = forName(sig.replace('/', '.'), false,
               klass != null ? getClassLoaderInner(klass) : null);
    }
    catch (ClassNotFoundException e) {}
   }
  }
  return aclass;
 }

 static final Class[] getInterfacesInner(Class klass)
 { /* used by VM classes only */
  return klass.interfaces;
 }

 static final void initialize(Class klass)
 { /* used by VM classes only */
  initialize0(klass);
 }

 static final boolean hasClassInitializer(Class klass)
 { /* used by VM classes only */
  return (klass.modifiers & MODIFIER_NATIVE) != 0;
 }

 static final String getClassStatus(Class klass)
 { /* used by VM classes only */
  int flags = klass.modifiers & (MODIFIER_VOLATILE | MODIFIER_TRANSIENT);
  return flags == 0 ? "INITIALIZED" : flags == MODIFIER_TRANSIENT ?
          "PREPARED" : flags == (MODIFIER_VOLATILE | MODIFIER_TRANSIENT) ?
          "VERIFIED" : "ERROR";
 }

 static final String getSourceFilename(Class klass)
 { /* used by VM classes only */
  Class aclass;
  while ((aclass = getComponentType(klass)) != null)
   klass = aclass;
  String filename = null;
  if (!isPrimitive(klass))
  {
   while ((aclass = getEnclosingClass(klass)) != null)
    klass = aclass;
   if ((klass.modifiers & MODIFIER_PUBLIC) != 0 ||
       (klass = getSourceHeadClass0(klass)) != null)
    filename = klass.getName().replace('.', '/').concat(".java");
  }
  return filename;
 }

 static final void setBasicConstructorOf(Class klass, Constructor constructor)
 { /* used by VM classes only */
  synchronized (StaticData.classBasicCtors)
  {
   StaticData.classBasicCtors.put(klass, constructor);
  }
 }

 static final Constructor getBasicConstructorOf(Class klass)
 { /* used by VM classes only */
  synchronized (StaticData.classBasicCtors)
  {
   return (Constructor) StaticData.classBasicCtors.get(klass);
  }
 }

 static final void setClassSignersOf(Class klass, Object[] signers)
 { /* used by VM classes only */
  synchronized (StaticData.classSigners)
  {
   StaticData.classSigners.put(klass, signers);
  }
 }

 static final Object[] getClassSignersOf(Class klass)
 { /* used by VM classes only */
  synchronized (StaticData.classSigners)
  {
   return (Object[]) StaticData.classSigners.get(klass);
  }
 }

 private static boolean isAsciiDigit(char ch)
 {
  return ch >= '0' && ch <= '9';
 }

 private static ClassLoader getClassLoaderInner(Class klass)
 {
  Class aclass;
  while ((aclass = getComponentType(klass)) != null)
   klass = aclass;
  ClassLoader loader = VMClassLoader.getLoaderOfDefinedClass(klass);
  if (loader != null)
   return loader;
  String name = klass.getName();
  return name.startsWith("java.") || name.startsWith("javax.") ||
          name.startsWith("gnu.java.") || name.startsWith("gnu.javax.") ||
          name.startsWith("gnu.classpath.") ? null :
          (ClassLoader) getClassLoader0(klass);
 }

 private static Class loadClassResolve(String name, ClassLoader loader)
 {
  Class aclass = null;
  try
  {
   aclass = loader != null ? loader.loadClass(name, true) :
             VMClassLoader.loadClass(name, true);
  }
  catch (ClassNotFoundException e) {}
  if (aclass != null)
  {
   if (getClassLoaderInner(aclass) != loader)
    aclass = null;
    else if (loader != null)
     loader.resolveClass(aclass);
     else VMClassLoader.resolveClass(aclass);
  }
  return aclass;
 }

 private static Class loadArrayClass(String name, ClassLoader loader)
  throws ClassNotFoundException
 {
  int nameLen = name.length();
  int dims = 0;
  while (dims < nameLen && name.charAt(dims) == '[')
   dims++;
  Class aclass = null;
  if (dims > 0 && dims < nameLen)
  {
   char ch = name.charAt(dims);
   if (ch == 'L')
   {
    if (nameLen - 1 > dims && name.charAt(nameLen - 1) == ';')
    {
     String className = name.substring(dims + 1, nameLen - 1);
     aclass = loader != null ? loader.loadClass(className) :
               VMClassLoader.loadClass(className, false);
    }
   }
    else if (nameLen - 1 == dims && ch != 'V')
     aclass = VMClassLoader.getPrimitiveClass(ch);
  }
  if (aclass == null)
   throw new ClassNotFoundException(name);
  return arrayClassOf0X(aclass, dims);
 }

 private static boolean isImplementedBy(Class klass, Class aclass)
 {
  do
  {
   Class[] interfaces = aclass.interfaces;
   int i;
   if ((i = interfaces.length) == 0)
    break;
   do
   {
    aclass = interfaces[--i];
    if (klass == aclass)
     return true;
    if (i == 0)
     break;
    if (isImplementedBy(klass, aclass))
     return true;
   } while (true);
  } while (true);
  return false;
 }

 private static native int initialize0(Class klass); /* JVM-core */

 private static native Object getClassLoader0(Class klass); /* JVM-core */

 private static native void throwException0(Object throwable); /* JVM-core */

 private static native Object vmdataForObjArray0(int dims); /* JVM-core */

 private static native Class getSourceHeadClass0(Class klass); /* JVM-core */
}

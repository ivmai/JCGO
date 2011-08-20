/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/Constructor.java --
 * VM specific Java "Constructor" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
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

/* java.lang.reflect.Constructor - reflection of Java constructors
   Copyright (C) 1998, 2001, 2004, 2005 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang.reflect;

import gnu.classpath.VMStackWalker;

import gnu.java.lang.reflect.MethodSignatureParser;

import java.lang.annotation.Annotation;

public final class Constructor extends AccessibleObject
 implements GenericDeclaration, Member /* hard-coded class name */
{ /* VM class */

 private static final int CONSTRUCTOR_MODIFIERS =
  Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STRICT;

 private final Class clazz;

 final int slot;

 final Class[] parameterTypes; /* const data */

 final Class[] exceptionTypes; /* const data */

 final int modifiers;

 final String signature;

 Constructor(Class declaringClass, int slot, Class[] parameterTypes,
   Class[] exceptionTypes, int modifiers, String signature)
 { /* used by VM classes only */
  if (declaringClass == null || parameterTypes == null ||
      exceptionTypes == null)
   throw new NullPointerException();
  this.clazz = declaringClass;
  this.slot = slot;
  this.parameterTypes = parameterTypes;
  this.exceptionTypes = exceptionTypes;
  this.modifiers = modifiers;
  this.signature = signature;
 }

 public Class getDeclaringClass()
 {
  if (clazz == null) /* hack */
   throw new InternalError();
  return clazz;
 }

 public String getName()
 {
  return getDeclaringClass().getName();
 }

 public int getModifiers()
 {
  return VMConstructor.getModifiersInternal(this) & CONSTRUCTOR_MODIFIERS;
 }

 public boolean isSynthetic()
 {
  return (VMConstructor.getModifiersInternal(this) & Modifier.SYNTHETIC) != 0;
 }

 public boolean isVarArgs()
 {
  return (VMConstructor.getModifiersInternal(this) & Modifier.VARARGS) != 0;
 }

 public Class[] getParameterTypes()
 {
  return copyClassArray(VMConstructor.getParameterTypesInternal(this));
 }

 public Class[] getExceptionTypes()
 {
  return copyClassArray(VMConstructor.getExceptionTypesInternal(this));
 }

 public boolean equals(Object obj)
 {
  if (obj == this)
   return true;
  if (!(obj instanceof Constructor))
   return false;
  Constructor constructor = (Constructor) obj;
  return getDeclaringClass() == constructor.getDeclaringClass() &&
          equalTypes(VMConstructor.getParameterTypesInternal(this),
          VMConstructor.getParameterTypesInternal(constructor));
 }

 public int hashCode()
 {
  return getDeclaringClass().getName().hashCode();
 }

 public String toString()
 {
  StringBuilder sb = new StringBuilder(128);
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  sb.append(getDeclaringClass().getName()).append('(');
  Class[] types = VMConstructor.getParameterTypesInternal(this);
  for (int i = 0; i < types.length; i++)
  {
   if (i > 0)
    sb.append(',');
   appendTypeName(types[i], sb);
  }
  sb.append(')');
  types = VMConstructor.getExceptionTypesInternal(this);
  if (types.length != 0)
  {
   sb.append(" throws ");
   for (int i = 0; i < types.length; i++)
   {
    if (i > 0)
     sb.append(',');
    sb.append(types[i].getName());
   }
  }
  return sb.toString();
 }

 public String toGenericString()
 {
  StringBuilder sb = new StringBuilder(128);
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  addTypeParameters(sb, getTypeParameters());
  sb.append(getDeclaringClass().getName()).append('(');
  Type[] types = getGenericParameterTypes();
  if (types.length != 0)
  {
   sb.append(types[0]);
   for (int i = 1; i < types.length; i++)
    sb.append(',').append(types[i]);
  }
  sb.append(')');
  types = getGenericExceptionTypes();
  if (types.length != 0)
  {
   sb.append(" throws ").append(types[0]);
   for (int i = 1; i < types.length; i++)
    sb.append(',').append(types[i]);
  }
  return sb.toString();
 }

 public Object newInstance(Object[] args)
  throws InstantiationException, IllegalAccessException,
   InvocationTargetException /* hard-coded method signature */
 {
  return VMConstructor.newInstance(this, args, isAccessible() ?
          getDeclaringClass() : VMStackWalker.getCallingClass());
 }

 public TypeVariable[] getTypeParameters()
 {
  String signature = VMConstructor.getSignature(this);
  return signature != null ? (new MethodSignatureParser(this,
          signature)).getTypeParameters() : new TypeVariable[0];
 }

 public Type[] getGenericExceptionTypes()
 {
  String signature = VMConstructor.getSignature(this);
  if (signature == null)
   return getExceptionTypes();
  return (new MethodSignatureParser(this,
          signature)).getGenericExceptionTypes();
 }

 public Type[] getGenericParameterTypes()
 {
  String signature = VMConstructor.getSignature(this);
  if (signature == null)
   return getParameterTypes();
  return (new MethodSignatureParser(this,
          signature)).getGenericParameterTypes();
 }

 public Annotation[][] getParameterAnnotations()
 {
  return new Annotation[0][];
 }

 static void addTypeParameters(StringBuilder sb, TypeVariable[] typeArgs)
 {
  if (typeArgs.length != 0)
  {
   sb.append('<');
   for (int i = 0; i < typeArgs.length; i++)
   {
    if (i > 0)
     sb.append(',');
    sb.append(typeArgs[i]);
   }
   sb.append("> ");
  }
 }

 static final StringBuilder appendTypeName(Class aclass, StringBuilder sb)
 { /* used by VM classes only */
  int dims;
  for (dims = 0; aclass.isArray(); dims++)
   aclass = aclass.getComponentType();
  sb.append(aclass.getName());
  while (dims-- > 0)
   sb.append("[]");
  return sb;
 }

 static final Class[] copyClassArray(Class[] types)
 { /* used by VM classes only */
  int count = types.length;
  Class[] newTypes = new Class[count];
  for (int i = 0; i < count; i++)
   newTypes[i] = types[i];
  return newTypes;
 }

 static final boolean equalTypes(Class[] types, Class[] types2)
 { /* used by VM classes only */
  int count = types.length;
  if (types2.length != count)
   return false;
  for (int i = 0; i < count; i++)
   if (types[i] != types2[i])
    return false;
  return true;
 }
}

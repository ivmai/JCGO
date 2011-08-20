/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/Method.java --
 * VM specific Java "Method" implementation.
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

/* java.lang.reflect.Method - reflection of Java methods
   Copyright (C) 1998, 2001, 2002, 2005 Free Software Foundation, Inc.

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

public final class Method extends AccessibleObject
 implements Member, GenericDeclaration /* hard-coded class name */
{ /* VM class */

 private static final int METHOD_MODIFIERS =
  Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC |
  Modifier.FINAL | Modifier.BRIDGE | Modifier.SYNCHRONIZED | Modifier.NATIVE |
  Modifier.ABSTRACT | Modifier.STRICT;

 private final Class declaringClass;

 private final String name;

 final int slot;

 final Class returnType;

 final Class[] parameterTypes; /* const data */

 final Class[] exceptionTypes; /* const data */

 final int modifiers;

 final String signature;

 Method(Class declaringClass, String name, int slot, Class returnType,
   Class[] parameterTypes, Class[] exceptionTypes, int modifiers,
   String signature)
 { /* used by VM classes only */
  if (declaringClass == null || name == null || returnType == null ||
      parameterTypes == null || exceptionTypes == null)
   throw new NullPointerException();
  this.declaringClass = declaringClass;
  this.name = name;
  this.slot = slot;
  this.returnType = returnType;
  this.parameterTypes = parameterTypes;
  this.exceptionTypes = exceptionTypes;
  this.modifiers = modifiers;
  this.signature = signature;
 }

 public Class getDeclaringClass()
 {
  if (declaringClass == null) /* hack */
   throw new InternalError();
  return declaringClass;
 }

 public String getName()
 {
  if (name == null) /* hack */
   throw new InternalError();
  return name;
 }

 public int getModifiers()
 {
  return VMMethod.getModifiersInternal(this) & METHOD_MODIFIERS;
 }

 public boolean isBridge()
 {
  return (VMMethod.getModifiersInternal(this) & Modifier.BRIDGE) != 0;
 }

 public boolean isSynthetic()
 {
  return (VMMethod.getModifiersInternal(this) & Modifier.SYNTHETIC) != 0;
 }

 public boolean isVarArgs()
 {
  return (VMMethod.getModifiersInternal(this) & Modifier.VARARGS) != 0;
 }

 public Class getReturnType()
 {
  return VMMethod.getReturnType(this);
 }

 public Class[] getParameterTypes()
 {
  return Constructor.copyClassArray(VMMethod.getParameterTypesInternal(this));
 }

 public Class[] getExceptionTypes()
 {
  return Constructor.copyClassArray(VMMethod.getExceptionTypesInternal(this));
 }

 public boolean equals(Object obj)
 {
  if (obj == this)
   return true;
  if (!(obj instanceof Method))
   return false;
  Method method = (Method) obj;
  return getDeclaringClass() == method.getDeclaringClass() &&
          getName().equals(method.getName()) &&
          getReturnType() == method.getReturnType() &&
          Constructor.equalTypes(VMMethod.getParameterTypesInternal(this),
          VMMethod.getParameterTypesInternal(method));
 }

 public int hashCode()
 {
  return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
 }

 public String toString()
 {
  StringBuilder sb = new StringBuilder(128);
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  Constructor.appendTypeName(getReturnType(), sb).append(' ');
  sb.append(getDeclaringClass().getName()).append('.');
  sb.append(getName()).append('(');
  Class[] types = VMMethod.getParameterTypesInternal(this);
  for (int i = 0; i < types.length; i++)
  {
   if (i != 0)
    sb.append(',');
   Constructor.appendTypeName(types[i], sb);
  }
  sb.append(')');
  types = VMMethod.getExceptionTypesInternal(this);
  if (types.length != 0)
  {
   sb.append(" throws ").append(types[0].getName());
   for (int i = 1; i < types.length; i++)
    sb.append(',').append(types[i].getName());
  }
  return sb.toString();
 }

 public String toGenericString()
 {
  StringBuilder sb = new StringBuilder(128);
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  Constructor.addTypeParameters(sb, getTypeParameters());
  sb.append(getGenericReturnType()).append(' ');
  sb.append(getDeclaringClass().getName()).append('.');
  sb.append(getName()).append('(');
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

 public Object invoke(Object obj, Object[] args)
  throws IllegalAccessException, InvocationTargetException
 {
  return VMMethod.invoke(this, obj, args, isAccessible() ?
          getDeclaringClass() : VMStackWalker.getCallingClass());
 }

 public TypeVariable[] getTypeParameters()
 {
  String signature = VMMethod.getSignature(this);
  return signature != null ? (new MethodSignatureParser(this,
          signature)).getTypeParameters() : new TypeVariable[0];
 }

 public Type[] getGenericExceptionTypes()
 {
  String signature = VMMethod.getSignature(this);
  if (signature == null)
   return getExceptionTypes();
  return (new MethodSignatureParser(this,
          signature)).getGenericExceptionTypes();
 }

 public Type[] getGenericParameterTypes()
 {
  String signature = VMMethod.getSignature(this);
  if (signature == null)
   return getParameterTypes();
  return (new MethodSignatureParser(this,
          signature)).getGenericParameterTypes();
 }

 public Type getGenericReturnType()
 {
  String signature = VMMethod.getSignature(this);
  if (signature == null)
   return getReturnType();
  return (new MethodSignatureParser(this, signature)).getGenericReturnType();
 }

 public Object getDefaultValue()
 {
  return null;
 }

 public Annotation[][] getParameterAnnotations()
 {
  return new Annotation[0][];
 }
}

/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/Field.java --
 * VM specific Java "Field" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
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

package java.lang.reflect;

import gnu.classpath.VMStackWalker;

import gnu.java.lang.reflect.FieldSignatureParser;

public final class Field extends AccessibleObject
 implements Member /* hard-coded class name */
{ /* VM class */

 private static final int FIELD_MODIFIERS =
  Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC |
  Modifier.FINAL | Modifier.VOLATILE | Modifier.TRANSIENT;

 private final Class declaringClass;

 private final String name;

 final int slot;

 final Class type;

 final int modifiers;

 final String signature;

 Field(Class declaringClass, String name, int slot, Class type,
   int modifiers, String signature)
 { /* used by VM classes only */
  if (declaringClass == null || name == null || type == null)
   throw new NullPointerException();
  this.declaringClass = declaringClass;
  this.name = name;
  this.slot = slot;
  this.type = type;
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
  return VMField.getModifiersInternal(this) & FIELD_MODIFIERS;
 }

 public boolean isSynthetic()
 {
  return (VMField.getModifiersInternal(this) & Modifier.SYNTHETIC) != 0;
 }

 public boolean isEnumConstant()
 {
  return (VMField.getModifiersInternal(this) & Modifier.ENUM) != 0;
 }

 public Class getType()
 {
  return VMField.getType(this);
 }

 public boolean equals(Object obj)
 {
  if (obj == this)
   return true;
  if (!(obj instanceof Field))
   return false;
  Field field = (Field) obj;
  return getDeclaringClass() == field.getDeclaringClass() &&
          getName().equals(field.getName()) && getType() == field.getType();
 }

 public int hashCode()
 {
  return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
 }

 public String toString()
 {
  StringBuilder sb = new StringBuilder();
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  Constructor.appendTypeName(getType(), sb).append(' ');
  sb.append(getDeclaringClass().getName()).append('.');
  sb.append(getName());
  return sb.toString();
 }

 public String toGenericString()
 {
  StringBuilder sb = new StringBuilder();
  int modifiers = getModifiers();
  if (modifiers != 0)
   Modifier.toString(modifiers, sb).append(' ');
  sb.append(getGenericType()).append(' ');
  sb.append(getDeclaringClass().getName()).append('.');
  sb.append(getName());
  return sb.toString();
 }

 public Object get(Object obj)
  throws IllegalAccessException
 {
  return VMField.get(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public boolean getBoolean(Object obj)
  throws IllegalAccessException
 {
  return VMField.getBoolean(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public byte getByte(Object obj)
  throws IllegalAccessException
 {
  return VMField.getByte(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public char getChar(Object obj)
  throws IllegalAccessException
 {
  return VMField.getChar(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public short getShort(Object obj)
  throws IllegalAccessException
 {
  return VMField.getShort(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public int getInt(Object obj)
  throws IllegalAccessException
 {
  return VMField.getInt(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public long getLong(Object obj)
  throws IllegalAccessException
 {
  return VMField.getLong(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public float getFloat(Object obj)
  throws IllegalAccessException
 {
  return VMField.getFloat(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public double getDouble(Object obj)
  throws IllegalAccessException
 {
  return VMField.getDouble(this, obj, isAccessible() ? getDeclaringClass() :
          VMStackWalker.getCallingClass());
 }

 public void set(Object obj, Object value)
  throws IllegalAccessException
 {
  VMField.set(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setBoolean(Object obj, boolean value)
  throws IllegalAccessException
 {
  VMField.setBoolean(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setByte(Object obj, byte value)
  throws IllegalAccessException
 {
  VMField.setByte(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setChar(Object obj, char value)
  throws IllegalAccessException
 {
  VMField.setChar(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setShort(Object obj, short value)
  throws IllegalAccessException
 {
  VMField.setShort(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setInt(Object obj, int value)
  throws IllegalAccessException
 {
  VMField.setInt(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setLong(Object obj, long value)
  throws IllegalAccessException
 {
  VMField.setLong(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setFloat(Object obj, float value)
  throws IllegalAccessException
 {
  VMField.setFloat(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public void setDouble(Object obj, double value)
  throws IllegalAccessException
 {
  VMField.setDouble(this, obj, value, isAccessible() ? getDeclaringClass() :
   VMStackWalker.getCallingClass());
 }

 public Type getGenericType()
 {
  String signature = VMField.getSignature(this);
  if (signature == null)
   return getType();
  return (new FieldSignatureParser(getDeclaringClass(),
          signature)).getFieldType();
 }
}

/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/reflect/VMField.java --
 * VM specific methods for Java "Field" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
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

final class VMField /* hard-coded class name */
{ /* VM class */ /* used by VM classes only */

 private static final int TYPECODE_BOOLEAN = 1;

 private static final int TYPECODE_BYTE = 2;

 private static final int TYPECODE_CHAR = 3;

 private static final int TYPECODE_SHORT = 4;

 private static final int TYPECODE_INT = 5;

 static
 {
  if (!"".equals("")) /* hack */
  {
   getStaticFieldClass0X(null); /* hack */
   getSlotOfField0X(null); /* hack */
   getFieldByName0X(void.class, "", "", 0); /* hack */
   getFieldBySlot0X(void.class, 0); /* hack */
  }
 }

 private VMField() {}

 static final int getModifiersInternal(Field field)
 {
  return field.modifiers;
 }

 static final Class getType(Field field)
 {
  Class type = field.type;
  if (type == null) /* hack */
   throw new InternalError();
  return type;
 }

 static final String getSignature(Field field)
 {
  return field.signature;
 }

 static final Object get(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  Class type = getType(field);
  int mods = getModifiersInternal(field);
  if (type == double.class)
   return new Double(getDouble0(fieldObjectOrClass(field, obj), field.slot,
           mods));
  if (type == float.class)
   return new Float(getFloat0(fieldObjectOrClass(field, obj), field.slot,
           mods));
  if (type == long.class)
   return new Long(getLong0(fieldObjectOrClass(field, obj), field.slot,
           mods));
  if (type == int.class)
   return new Integer(getInt0(fieldObjectOrClass(field, obj), field.slot,
           mods, TYPECODE_INT));
  if (type == short.class)
   return new Short((short) getInt0(fieldObjectOrClass(field, obj),
           field.slot, mods, TYPECODE_SHORT));
  if (type == char.class)
   return new Character((char) getInt0(fieldObjectOrClass(field, obj),
           field.slot, mods, TYPECODE_CHAR));
  if (type == byte.class)
   return new Byte((byte) getInt0(fieldObjectOrClass(field, obj), field.slot,
           mods, TYPECODE_BYTE));
  if (type == boolean.class)
   return getInt0(fieldObjectOrClass(field, obj), field.slot, mods,
           TYPECODE_BOOLEAN) != 0 ? Boolean.TRUE : Boolean.FALSE;
  return get0(fieldObjectOrClass(field, obj), field.slot, mods);
 }

 static final boolean getBoolean(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  if (getType(field) != boolean.class)
   throw new IllegalArgumentException("field type mismatch");
  return getInt0(fieldObjectOrClass(field, obj), field.slot,
          getModifiersInternal(field), TYPECODE_BOOLEAN) != 0;
 }

 static final byte getByte(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  if (getType(field) != byte.class)
   throw new IllegalArgumentException("field type mismatch");
  return (byte) getInt0(fieldObjectOrClass(field, obj), field.slot,
          getModifiersInternal(field), TYPECODE_BYTE);
 }

 static final char getChar(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  if (getType(field) != char.class)
   throw new IllegalArgumentException("field type mismatch");
  return (char) getInt0(fieldObjectOrClass(field, obj), field.slot,
          getModifiersInternal(field), TYPECODE_CHAR);
 }

 static final short getShort(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  Class type = getType(field);
  int typecode = TYPECODE_SHORT;
  if (type != short.class)
  {
   if (type == byte.class)
    typecode = TYPECODE_BYTE;
    else throw new IllegalArgumentException("field type mismatch");
  }
  return (short) getInt0(fieldObjectOrClass(field, obj), field.slot,
          getModifiersInternal(field), typecode);
 }

 static final int getInt(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  return getIntInner(field, obj);
 }

 static final long getLong(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  if (getType(field) == long.class)
   return getLong0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  return getIntInner(field, obj);
 }

 static final float getFloat(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  Class type = getType(field);
  if (type == float.class)
   return getFloat0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  if (type == long.class)
   return getLong0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  return getIntInner(field, obj);
 }

 static final double getDouble(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  Class type = getType(field);
  if (type == double.class)
   return getDouble0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  if (type == float.class)
   return getFloat0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  if (type == long.class)
   return getLong0(fieldObjectOrClass(field, obj), field.slot,
           getModifiersInternal(field));
  return getIntInner(field, obj);
 }

 static final void set(Field field, Object obj, Object value, Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  Class type = getType(field);
  int mods = getModifiersInternal(field);
  if (type == boolean.class)
  {
   if (!(value instanceof Boolean))
    throw new IllegalArgumentException("field type mismatch");
   int intValue = ((Boolean) value).booleanValue() ? 1 : 0;
   setInt0(fieldObjectOrClass(field, obj), field.slot, mods, TYPECODE_BOOLEAN,
    intValue);
  }
   else if (type == byte.class)
   {
    if (!(value instanceof Byte))
     throw new IllegalArgumentException("field type mismatch");
    int intValue = ((Byte) value).intValue();
    setInt0(fieldObjectOrClass(field, obj), field.slot, mods, TYPECODE_BYTE,
     intValue);
   }
    else if (type == char.class)
    {
     if (!(value instanceof Character))
      throw new IllegalArgumentException("field type mismatch");
     int intValue = ((Character) value).charValue();
     setInt0(fieldObjectOrClass(field, obj), field.slot, mods, TYPECODE_CHAR,
      intValue);
    }
     else if (type == short.class)
     {
      int intValue;
      if (value instanceof Short)
       intValue = ((Short) value).intValue();
       else if (value instanceof Byte)
        intValue = ((Byte) value).intValue();
        else throw new IllegalArgumentException("field type mismatch");
      setInt0(fieldObjectOrClass(field, obj), field.slot, mods,
       TYPECODE_SHORT, intValue);
     }
      else if (type == int.class)
      {
       int intValue = unwrapIntValue(value);
       setInt0(fieldObjectOrClass(field, obj), field.slot, mods, TYPECODE_INT,
        intValue);
      }
       else if (type == long.class)
       {
        long longValue;
        if (value instanceof Long)
         longValue = ((Long) value).longValue();
         else longValue = unwrapIntValue(value);
        setLong0(fieldObjectOrClass(field, obj), longValue, field.slot, mods);
       }
        else if (type == float.class)
         setFloat0(fieldObjectOrClass(field, obj), value instanceof Float ||
          value instanceof Long ? ((Number) value).floatValue() :
          unwrapIntValue(value), field.slot, mods);
         else if (type == double.class)
          setDouble0(fieldObjectOrClass(field, obj),
           value instanceof Double || value instanceof Float ||
           value instanceof Long ? ((Number) value).doubleValue() :
           unwrapIntValue(value), field.slot, mods);
          else
          {
           if (value != null && !type.isInstance(value))
            throw new IllegalArgumentException(
                   "field reference type mismatch");
           set0(fieldObjectOrClass(field, obj), value, field.slot, mods);
          }
 }

 static final void setBoolean(Field field, Object obj, boolean value,
   Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  if (getType(field) != boolean.class)
   throw new IllegalArgumentException("field type mismatch");
  setInt0(fieldObjectOrClass(field, obj), field.slot,
   getModifiersInternal(field), TYPECODE_BOOLEAN, value ? 1 : 0);
 }

 static final void setByte(Field field, Object obj, byte value, Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  Class type = getType(field);
  if (type == byte.class)
   setInt0(fieldObjectOrClass(field, obj), field.slot,
    getModifiersInternal(field), TYPECODE_BYTE, value);
   else if (type == short.class)
    setInt0(fieldObjectOrClass(field, obj), field.slot,
     getModifiersInternal(field), TYPECODE_SHORT, value);
    else setIntInner(field, obj, value);
 }

 static final void setChar(Field field, Object obj, char value, Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  if (getType(field) == char.class)
   setInt0(fieldObjectOrClass(field, obj), field.slot,
    getModifiersInternal(field), TYPECODE_CHAR, value);
   else setIntInner(field, obj, value);
 }

 static final void setShort(Field field, Object obj, short value,
   Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  if (getType(field) == short.class)
   setInt0(fieldObjectOrClass(field, obj), field.slot,
    getModifiersInternal(field), TYPECODE_SHORT, value);
   else setIntInner(field, obj, value);
 }

 static final void setInt(Field field, Object obj, int value, Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  setIntInner(field, obj, value);
 }

 static final void setLong(Field field, Object obj, long value, Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  Class type = getType(field);
  int mods = getModifiersInternal(field);
  if (type == long.class)
   setLong0(fieldObjectOrClass(field, obj), value, field.slot, mods);
   else if (type == float.class)
    setFloat0(fieldObjectOrClass(field, obj), value, field.slot, mods);
    else
    {
     if (type != double.class)
      throw new IllegalArgumentException("field type mismatch");
     setDouble0(fieldObjectOrClass(field, obj), value, field.slot, mods);
    }
 }

 static final void setFloat(Field field, Object obj, float value,
   Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  Class type = getType(field);
  int mods = getModifiersInternal(field);
  if (type == float.class)
   setFloat0(fieldObjectOrClass(field, obj), value, field.slot, mods);
   else
   {
    if (type != double.class)
     throw new IllegalArgumentException("field type mismatch");
    setDouble0(fieldObjectOrClass(field, obj), value, field.slot, mods);
   }
 }

 static final void setDouble(Field field, Object obj, double value,
   Class caller)
  throws IllegalAccessException
 {
  checkAllowWrite(field, obj, caller);
  if (getType(field) != double.class)
   throw new IllegalArgumentException("field type mismatch");
  setDouble0(fieldObjectOrClass(field, obj), value, field.slot,
   getModifiersInternal(field));
 }

 static final Field createNonFinalAccessible(Field field)
 {
  field = new Field(field.getDeclaringClass(), field.getName(), field.slot,
           getType(field), getModifiersInternal(field) & ~Modifier.FINAL,
           getSignature(field));
  field.setAccessible(true);
  return field;
 }

 static final long objectFieldOffset(Field field)
 {
  Class aclass = null;
  if ((getModifiersInternal(field) & Modifier.STATIC) != 0)
  {
   aclass = field.getDeclaringClass();
   VMAccessorJavaLang.initializeVMClass(aclass);
  }
  return objectFieldOffset0(aclass, field.slot);
 }

 static final Field[] getDeclaredFields(Class klass, boolean publicOnly)
 {
  String[] names;
  int count;
  if (klass.isPrimitive() || klass.isArray() ||
      (names = getFieldsName0(klass)) == null || (count = names.length) == 0)
   return new Field[0];
  short[] modifiers = getFieldsModifiers0(klass);
  int mods;
  int cnt = count;
  if (modifiers == null && klass.isInterface())
  {
   publicOnly = false;
   mods = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
  }
   else
   {
    mods = 0;
    if (publicOnly)
    {
     cnt = 0;
     if (modifiers != null)
      for (int i = 0; i < count; i++)
       if ((modifiers[i] & Modifier.PUBLIC) != 0)
        cnt++;
    }
   }
  Field[] fields = new Field[cnt];
  if (cnt > 0)
  {
   int[] slots = getFieldsSlot0(klass);
   Class[] types = getFieldsType0(klass);
   byte[] dims = getFieldsDims0(klass);
   String[] signatures = getFieldsSignature0(klass);
   cnt = 0;
   for (int i = 0; i < count; i++)
    if (!publicOnly || (modifiers[i] & Modifier.PUBLIC) != 0)
     fields[cnt++] = new Field(klass, names[i], slots != null ? slots[i] : i,
                      makeArrayClass(types, dims, i),
                      modifiers != null ? modifiers[i] & 0xffff : mods,
                      signatures != null ? signatures[i] : null);
  }
  return fields;
 }

 static final Field getDeclaredField(Class klass, String fieldName)
 { /* used by VM classes only */
  String[] names;
  if (!klass.isPrimitive() && !klass.isArray() &&
      (names = getFieldsName0(klass)) != null)
  {
   short[] modifiers;
   int[] slots;
   String[] signatures;
   int count = names.length;
   for (int i = 0; i < count; i++)
    if (fieldName.equals(names[i]))
     return new Field(klass, fieldName,
             (slots = getFieldsSlot0(klass)) != null ? slots[i] : i,
             makeArrayClass(getFieldsType0(klass), getFieldsDims0(klass), i),
             (modifiers = getFieldsModifiers0(klass)) != null ?
             modifiers[i] & 0xffff : klass.isInterface() ?
             Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL : 0,
             (signatures = getFieldsSignature0(klass)) != null ?
             signatures[i] : null);
  }
  return null;
 }

 static final Class getStaticFieldClass0X(Object fieldObj)
 { /* called from native code */
  Field field = (Field) fieldObj;
  return (getModifiersInternal(field) & Modifier.STATIC) != 0 ?
          field.getDeclaringClass() : null;
 }

 static final int getSlotOfField0X(Object fieldObj)
  throws ClassCastException
 { /* called from native code */
  Field field = (Field) fieldObj;
  VMAccessorJavaLang.initializeVMClass(field.getDeclaringClass());
  return field.slot;
 }

 static final Object getFieldByName0X(Class klass, String name,
   String typeSig, int isStatic)
 { /* called from native code */
  Class type = null;
  return !klass.isPrimitive() && !klass.isArray() && (typeSig == null ||
          (type = VMAccessorJavaLang.classForSigVMClass(typeSig,
          klass)) != null) ? getFieldByNameInner(klass, name, type,
          isStatic != 0) : null;
 }

 static final Object getFieldBySlot0X(Class klass, long slot)
 { /* called from native code */
  String[] names;
  int intSlot = (int) slot;
  int count;
  if (intSlot >= 0 && !klass.isPrimitive() && !klass.isArray() &&
      (long) intSlot == slot && (names = getFieldsName0(klass)) != null &&
      (count = names.length) != 0)
  {
   int[] slots = getFieldsSlot0(klass);
   for (int i = 0; i < count; i++)
    if ((slots != null ? slots[i] : i) == intSlot)
    {
     Class[] types = getFieldsType0(klass);
     byte[] dims = getFieldsDims0(klass);
     short[] modifiers = getFieldsModifiers0(klass);
     String[] signatures = getFieldsSignature0(klass);
     return new Field(klass, names[i], intSlot, makeArrayClass(types,
             dims, i), modifiers != null ? modifiers[i] & 0xffff :
             klass.isInterface() ? Modifier.PUBLIC | Modifier.STATIC |
             Modifier.FINAL : 0, signatures != null ? signatures[i] : null);
    }
  }
  return null;
 }

 private static void checkAllowGet(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  int modifiers = getModifiersInternal(field);
  Class declaringClass = field.getDeclaringClass();
  if ((modifiers & Modifier.STATIC) == 0)
  {
   if (obj == null)
    throw new NullPointerException();
   if (!declaringClass.isInstance(obj))
    throw new IllegalArgumentException(
           "not an instance of a field declaring class: " +
           declaringClass.getName());
  }
  if ((modifiers & Modifier.PUBLIC) == 0 && caller != declaringClass &&
      caller != null && ((modifiers & Modifier.PRIVATE) != 0 ||
      (((modifiers & Modifier.PROTECTED) == 0 ||
      !declaringClass.isAssignableFrom(caller)) &&
      !packageNameOf(declaringClass).equals(packageNameOf(caller)))))
   throw new IllegalAccessException("field not accessible: " +
              declaringClass.getName() + "." + field.getName());
 }

 private static void checkAllowWrite(Field field, Object obj, Class caller)
  throws IllegalAccessException
 {
  checkAllowGet(field, obj, caller);
  if ((getModifiersInternal(field) & Modifier.FINAL) != 0 &&
      !field.isAccessible())
   throw new IllegalAccessException("cannot set final field: " +
              field.getDeclaringClass().getName() + "." + field.getName());
 }

 private static Object fieldObjectOrClass(Field field, Object obj)
 {
  if ((getModifiersInternal(field) & Modifier.STATIC) == 0)
   return obj;
  Class declaringClass = field.getDeclaringClass();
  VMAccessorJavaLang.initializeVMClass(declaringClass);
  return declaringClass;
 }

 private static int getIntInner(Field field, Object obj)
 {
  Class type = getType(field);
  int typecode = TYPECODE_INT;
  if (type != int.class)
  {
   if (type == short.class)
    typecode = TYPECODE_SHORT;
    else if (type == char.class)
     typecode = TYPECODE_CHAR;
     else if (type == byte.class)
      typecode = TYPECODE_BYTE;
      else throw new IllegalArgumentException("field type mismatch");
  }
  return getInt0(fieldObjectOrClass(field, obj), field.slot,
          getModifiersInternal(field), typecode);
 }

 private static void setIntInner(Field field, Object obj, int value)
 {
  Class type = getType(field);
  int mods = getModifiersInternal(field);
  if (type == int.class)
   setInt0(fieldObjectOrClass(field, obj), field.slot, mods, TYPECODE_INT,
    value);
   else if (type == long.class)
    setLong0(fieldObjectOrClass(field, obj), value, field.slot, mods);
    else if (type == float.class)
     setFloat0(fieldObjectOrClass(field, obj), value, field.slot, mods);
     else
     {
      if (type != double.class)
       throw new IllegalArgumentException("field type mismatch");
      setDouble0(fieldObjectOrClass(field, obj), value, field.slot, mods);
     }
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
      else throw new IllegalArgumentException("field type mismatch");
  return intValue;
 }

 private static Field getFieldByNameInner(Class klass, String name,
   Class type, boolean isStatic)
 {
  Class aclass = klass;
  Field field = null;
  do
  {
   String[] names = getFieldsName0(aclass);
   int count;
   if (names != null && (count = names.length) != 0)
   {
    Class[] types = getFieldsType0(aclass);
    byte[] dims = getFieldsDims0(aclass);
    short[] modifiers = getFieldsModifiers0(aclass);
    int mods = aclass.isInterface() ?
                Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL : 0;
    for (int i = 0; i < count; i++)
     if (name.equals(names[i]))
     {
      if (modifiers != null)
       mods = modifiers[i] & 0xffff;
      if (((mods & Modifier.STATIC) != 0) == isStatic)
      {
       Class fieldType = makeArrayClass(types, dims, i);
       if (type == null || type == fieldType)
       {
        int[] slots = getFieldsSlot0(aclass);
        String[] signatures = getFieldsSignature0(klass);
        Field f = new Field(aclass, names[i], slots != null ? slots[i] : i,
                   fieldType, mods, signatures != null ? signatures[i] :
                   null);
        if (aclass == klass ||
            (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0 ||
            ((mods & Modifier.PRIVATE) == 0 &&
            packageNameOf(aclass).equals(packageNameOf(klass))))
         return f;
        if (field == null)
         field = f;
        break;
       }
      }
     }
   }
   if (isStatic)
   {
    Class[] interfaces = VMAccessorJavaLang.getInterfacesInnerVMClass(aclass);
    int len = interfaces.length;
    for (int i = 0; i < len; i++)
    {
     Field f = getFieldByNameInner(interfaces[i], name, type, true);
     if (f != null)
      return f;
    }
   }
   aclass = aclass.getSuperclass();
  } while (aclass != null);
  return field;
 }

 private static Class makeArrayClass(Class[] types, byte[] dims, int i)
 {
  return dims != null ? VMAccessorJavaLang.arrayClassOfVMClass(types[i],
          dims[i] & 0xff) : types[i];
 }

 private static native String[] getFieldsName0(
   Class klass); /* JVM-core */ /* const data */

 private static native int[] getFieldsSlot0(
   Class klass); /* JVM-core */ /* const data */

 private static native Class[] getFieldsType0(
   Class klass); /* JVM-core */ /* const data */

 private static native byte[] getFieldsDims0(
   Class klass); /* JVM-core */ /* const data */

 private static native short[] getFieldsModifiers0(
   Class klass); /* JVM-core */ /* const data */

 private static native String[] getFieldsSignature0(
   Class klass); /* JVM-core */ /* const data */

 private static native int getInt0(Object objOrClass, int slot, int mods,
   int typecode); /* JVM-core */

 private static native int setInt0(Object objOrClass, int slot, int mods,
   int typecode, int value); /* JVM-core */

 private static native long getLong0(Object objOrClass,
   int slot, int mods); /* JVM-core */

 private static native int setLong0(Object objOrClass, long value,
   int slot, int mods); /* JVM-core */

 private static native float getFloat0(Object objOrClass,
   int slot, int mods); /* JVM-core */

 private static native int setFloat0(Object objOrClass, float value,
   int slot, int mods); /* JVM-core */

 private static native double getDouble0(Object objOrClass,
   int slot, int mods); /* JVM-core */

 private static native int setDouble0(Object objOrClass, double value,
   int slot, int mods); /* JVM-core */

 private static native Object get0(Object objOrClass,
   int slot, int mods); /* JVM-core */

 private static native int set0(Object objOrClass, Object value,
   int slot, int mods); /* JVM-core */

 private static native long objectFieldOffset0(Class aclass,
   int slot); /* JVM-core */
}

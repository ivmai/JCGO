/*
 * @(#) $(JCGO)/include/jcgorfld.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
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

#ifdef JCGO_VER

#define JCGO_FIELDTYPECODE_BOOLEAN 1
#define JCGO_FIELDTYPECODE_BYTE 2
#define JCGO_FIELDTYPECODE_CHAR 3
#define JCGO_FIELDTYPECODE_SHORT 4
#define JCGO_FIELDTYPECODE_INT 5

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMField__getFieldsName0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->fieldsName : jnull;
}

JCGO_NOSEP_STATIC jintArr CFASTCALL
java_lang_reflect_VMField__getFieldsSlot0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->fieldsSlot : jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMField__getFieldsType0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->fieldsType : jnull;
}

JCGO_NOSEP_STATIC jbyteArr CFASTCALL
java_lang_reflect_VMField__getFieldsDims0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->fieldsDims : jnull;
}

JCGO_NOSEP_STATIC jshortArr CFASTCALL
java_lang_reflect_VMField__getFieldsModifiers0__Lc( java_lang_Class klass )
{
 CONST struct jcgo_reflect_s *jcgo_reflect =
  ((jvtable)&JCGO_METHODS_OF(
  JCGO_FIELD_NZACCESS(klass, vmdata)))->jcgo_reflect;
 return JCGO_EXPECT_TRUE(jcgo_reflect != NULL) ?
         jcgo_reflect->fieldsModifiers : jnull;
}

JCGO_NOSEP_STATIC jObjectArr CFASTCALL
java_lang_reflect_VMField__getFieldsSignature0__Lc( java_lang_Class klass )
{
 /* not implemented */
 return jnull;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__getInt0__LoIII( java_lang_Object objOrClass,
 jint slot, jint mods, jint typecode )
{
 jint value = 0;
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & JCGO_ACCMOD_VOLATILE) != 0)
 {
  switch ((int)typecode)
  {
  case JCGO_FIELDTYPECODE_BOOLEAN:
   value = jcgo_AO_fetchZ((volatile jboolean *)
            ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
   break;
  case JCGO_FIELDTYPECODE_BYTE:
   value = jcgo_AO_fetchB((volatile jbyte *)
            ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
   break;
  case JCGO_FIELDTYPECODE_CHAR:
   value = (jint)jcgo_AO_fetchC((volatile jchar *)
            ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
   break;
  case JCGO_FIELDTYPECODE_SHORT:
   value = jcgo_AO_fetchS((volatile jshort *)
            ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
   break;
  case JCGO_FIELDTYPECODE_INT:
   value = jcgo_AO_fetchI((volatile jint *)
            ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
   break;
  }
  return value;
 }
#endif
#endif
 switch ((int)typecode)
 {
 case JCGO_FIELDTYPECODE_BOOLEAN:
  value = *(volatile jboolean *)((char *)&JCGO_METHODS_OF(objOrClass) +
           (unsigned)slot);
  break;
 case JCGO_FIELDTYPECODE_BYTE:
  value = *(volatile jbyte *)((char *)&JCGO_METHODS_OF(objOrClass) +
           (unsigned)slot);
  break;
 case JCGO_FIELDTYPECODE_CHAR:
  value = (jint)(*(volatile jchar *)((char *)&JCGO_METHODS_OF(objOrClass) +
           (unsigned)slot));
  break;
 case JCGO_FIELDTYPECODE_SHORT:
  value = *(volatile jshort *)((char *)&JCGO_METHODS_OF(objOrClass) +
           (unsigned)slot);
  break;
 case JCGO_FIELDTYPECODE_INT:
  value = *(volatile jint *)((char *)&JCGO_METHODS_OF(objOrClass) +
           (unsigned)slot);
  break;
 }
 return value;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__setInt0__LoIIII( java_lang_Object objOrClass,
 jint slot, jint mods, jint typecode, jint value )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_FINAL)) != 0)
 {
  switch ((int)typecode)
  {
  case JCGO_FIELDTYPECODE_BOOLEAN:
   jcgo_AO_storeZ((volatile jboolean *)((char *)&JCGO_METHODS_OF(objOrClass) +
    (unsigned)slot), (jboolean)(value != 0));
   break;
  case JCGO_FIELDTYPECODE_BYTE:
   jcgo_AO_storeB((volatile jbyte *)((char *)&JCGO_METHODS_OF(objOrClass) +
    (unsigned)slot), (jbyte)value);
   break;
  case JCGO_FIELDTYPECODE_CHAR:
   jcgo_AO_storeC((volatile jchar *)((char *)&JCGO_METHODS_OF(objOrClass) +
    (unsigned)slot), (jchar)value);
   break;
  case JCGO_FIELDTYPECODE_SHORT:
   jcgo_AO_storeS((volatile jshort *)((char *)&JCGO_METHODS_OF(objOrClass) +
    (unsigned)slot), (jshort)value);
   break;
  case JCGO_FIELDTYPECODE_INT:
   jcgo_AO_storeI((volatile jint *)((char *)&JCGO_METHODS_OF(objOrClass) +
    (unsigned)slot), value);
   break;
  }
  return 0;
 }
#endif
#endif
 switch ((int)typecode)
 {
 case JCGO_FIELDTYPECODE_BOOLEAN:
  *(volatile jboolean *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot) = (jboolean)(value != 0);
  break;
 case JCGO_FIELDTYPECODE_BYTE:
  *(volatile jbyte *)((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot) =
   (jbyte)value;
  break;
 case JCGO_FIELDTYPECODE_CHAR:
  *(volatile jchar *)((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot) =
   (jchar)value;
  break;
 case JCGO_FIELDTYPECODE_SHORT:
  *(volatile jshort *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot) = (jshort)value;
  break;
 case JCGO_FIELDTYPECODE_INT:
  *(volatile jint *)((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot) =
   value;
  break;
 }
 return 0;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_reflect_VMField__getLong0__LoII( java_lang_Object objOrClass,
 jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & JCGO_ACCMOD_VOLATILE) != 0)
  return jcgo_AO_fetchJ((volatile jlong *)
          ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
#endif
#endif
 return *(volatile jlong *)((char *)&JCGO_METHODS_OF(objOrClass) +
         (unsigned)slot);
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__setLong0__LoJII( java_lang_Object objOrClass,
 jlong value, jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_FINAL)) != 0)
  jcgo_AO_storeJ((volatile jlong *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot), value);
  else
#endif
#endif
  /* else */ *(volatile jlong *)((char *)&JCGO_METHODS_OF(objOrClass) +
              (unsigned)slot) = value;
 return 0;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL
java_lang_reflect_VMField__getFloat0__LoII( java_lang_Object objOrClass,
 jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & JCGO_ACCMOD_VOLATILE) != 0)
  return jcgo_AO_fetchF((volatile jfloat *)
          ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
#endif
#endif
 return *(volatile jfloat *)((char *)&JCGO_METHODS_OF(objOrClass) +
         (unsigned)slot);
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__setFloat0__LoFII( java_lang_Object objOrClass,
 jfloat value, jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_FINAL)) != 0)
  jcgo_AO_storeF((volatile jfloat *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot), value);
  else
#endif
#endif
  /* else */ *(volatile jfloat *)((char *)&JCGO_METHODS_OF(objOrClass) +
              (unsigned)slot) = value;
 return 0;
}

JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_reflect_VMField__getDouble0__LoII( java_lang_Object objOrClass,
 jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & JCGO_ACCMOD_VOLATILE) != 0)
  return jcgo_AO_fetchD((volatile jdouble *)
          ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
#endif
#endif
 return *(volatile jdouble *)((char *)&JCGO_METHODS_OF(objOrClass) +
         (unsigned)slot);
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__setDouble0__LoDII( java_lang_Object objOrClass,
 jdouble value, jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_FINAL)) != 0)
  jcgo_AO_storeD((volatile jdouble *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot), value);
  else
#endif
#endif
  /* else */ *(volatile jdouble *)((char *)&JCGO_METHODS_OF(objOrClass) +
              (unsigned)slot) = value;
 return 0;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_lang_reflect_VMField__get0__LoII( java_lang_Object objOrClass,
 jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & JCGO_ACCMOD_VOLATILE) != 0)
  return (java_lang_Object)jcgo_AO_fetchL((jObject volatile *)(
         (char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
#endif
#endif
 return (java_lang_Object)(*(java_lang_Object volatile *)(
         (char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot));
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_reflect_VMField__set0__LoLoII( java_lang_Object objOrClass,
 java_lang_Object value, jint slot, jint mods )
{
#ifdef JCGO_THREADS
#ifdef JCGO_PARALLEL
 if (((int)mods & (JCGO_ACCMOD_VOLATILE | JCGO_ACCMOD_FINAL)) != 0)
  jcgo_AO_storeL((jObject volatile *)((char *)&JCGO_METHODS_OF(objOrClass) +
   (unsigned)slot), (jObject)value);
  else
#endif
#endif
  /* else */ *(java_lang_Object volatile *)
              ((char *)&JCGO_METHODS_OF(objOrClass) + (unsigned)slot) = value;
 return 0;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_reflect_VMField__objectFieldOffset0__LcI( java_lang_Class aclass,
 jint slot )
{
 return aclass != jnull ?
         (jlong)JCGO_CAST_PTRTONUM((char *)&JCGO_METHODS_OF(aclass) +
         (unsigned)slot) : (jlong)((unsigned)slot);
}

#endif

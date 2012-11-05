/*
 * @(#) $(JCGO)/include/jcgojnif.c --
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

#ifdef OBJT_java_lang_reflect_VMField

STATIC java_lang_Object CFASTCALL jcgo_fieldFindById( java_lang_Class aclass,
 jfieldID fieldID, int isStatic )
{
 java_lang_Object obj;
 jObjectArr interfaces;
 int i;
 do
 {
  obj = java_lang_reflect_VMField__getFieldBySlot0X__LcJ(aclass, isStatic ?
         (jlong)((char *)fieldID - (char *)&JCGO_METHODS_OF(aclass)) :
         (jlong)JCGO_CAST_PTRTONUM(fieldID));
  if (JCGO_EXPECT_TRUE(obj != jnull &&
      (java_lang_reflect_VMField__getStaticFieldClass0X__Lo(obj) != jnull ?
      isStatic : !isStatic)))
   return obj;
  if (isStatic)
  {
   interfaces = JCGO_FIELD_NZACCESS(aclass, interfaces);
   for (i = 0; (int)JCGO_ARRAY_NZLENGTH(interfaces) > i; i++)
    if ((obj = jcgo_fieldFindById(
        (java_lang_Class)JCGO_ARR_INTERNALACC(jObject, interfaces, i),
        fieldID, 1)) != jnull)
     return obj;
  }
 } while ((aclass = JCGO_FIELD_NZACCESS(aclass, superclass)) != jnull);
 return jnull;
}

#else /* OBJT_java_lang_reflect_VMField */

JCGO_NOSEP_INLINE void CFASTCALL jcgo_abortOnFieldNotFound( void )
{
 JCGO_FATAL_ABORT("Cannot find java.lang.reflect.VMField!");
}

#endif /* ! OBJT_java_lang_reflect_VMField */

STATIC jfieldID JNICALL
jcgo_JniFromReflectedField( JNIEnv *pJniEnv, jobject field )
{
 java_lang_Object jobj;
 jfieldID JCGO_TRY_VOLATILE fieldID;
#ifdef OBJT_java_lang_reflect_VMField
 java_lang_Class aclass;
 jint slot;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jfieldID)0;
 jobj = (java_lang_Object)jcgo_jniDeRef(field);
 if (JCGO_EXPECT_FALSE(jobj == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jfieldID)0;
 }
 fieldID = (jfieldID)0;
#ifdef OBJT_java_lang_reflect_VMField
 JCGO_NATCBACK_BEGIN(pJniEnv)
 slot = java_lang_reflect_VMField__getSlotOfField0X__Lo(jobj);
 aclass = java_lang_reflect_VMField__getStaticFieldClass0X__Lo(jobj);
 fieldID = aclass != jnull ? (jfieldID)((char *)&JCGO_METHODS_OF(aclass) +
            (unsigned)slot) : (jfieldID)JCGO_CAST_NUMTOPTR(slot);
 JCGO_NATCBACK_END(pJniEnv)
#endif
 return (jfieldID)fieldID;
}

STATIC jobject JNICALL
jcgo_JniToReflectedField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jboolean isStatic )
{
 java_lang_Class aclass;
 java_lang_Object JCGO_TRY_VOLATILE jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 jobj = jnull;
#ifdef OBJT_java_lang_reflect_VMField
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jobj = jcgo_fieldFindById(aclass, fieldID, isStatic ? 1 : 0);
 if (jobj == jnull)
  JCGO_FATAL_ABORT("Invalid JNI fieldID!");
 JCGO_NATCBACK_END(pJniEnv)
#endif
 return jcgo_jniToLocalRef(pJniEnv, (jObject)jobj);
}

STATIC jfieldID JNICALL
jcgo_JniGetFieldID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 java_lang_Class aclass;
 jfieldID JCGO_TRY_VOLATILE fieldID;
#ifdef OBJT_java_lang_reflect_VMField
 java_lang_String str;
 java_lang_String sigstr;
 java_lang_Object jobj;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jfieldID)0;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || name == NULL || sig == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jfieldID)0;
 }
 fieldID = (jfieldID)0;
#ifdef OBJT_java_lang_reflect_VMField
 JCGO_NATCBACK_BEGIN(pJniEnv)
 str = jcgo_utfMakeString(name);
 sigstr = jcgo_utfMakeString(sig);
 jobj = java_lang_reflect_VMField__getFieldByName0X__LcLsLsI(aclass, str,
         sigstr, 0);
 if (JCGO_EXPECT_TRUE(jobj != jnull))
  fieldID = (jfieldID)JCGO_CAST_NUMTOPTR(
             java_lang_reflect_VMField__getSlotOfField0X__Lo(jobj));
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createNoSuchFieldError0X__LcLsLsI(aclass, str,
        sigstr, 0));
#endif
 JCGO_NATCBACK_END(pJniEnv)
#else
 jcgo_abortOnFieldNotFound();
#endif
 return (jfieldID)fieldID;
}

STATIC jobject JNICALL
jcgo_JniGetObjectField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 return jcgo_jniToLocalRef(pJniEnv,
         (jObject)(*(jObject volatile *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID))));
}

STATIC jboolean JNICALL
jcgo_JniGetBooleanField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jboolean)JNI_FALSE;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jboolean)JNI_FALSE;
 }
 return *(volatile jboolean *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jbyte JNICALL
jcgo_JniGetByteField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jbyte)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jbyte)0;
 }
 return *(volatile jbyte *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jchar JNICALL
jcgo_JniGetCharField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jchar)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jchar)0;
 }
 return *(volatile jchar *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jshort JNICALL
jcgo_JniGetShortField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jshort)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jshort)0;
 }
 return *(volatile jshort *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jint JNICALL
jcgo_JniGetIntField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jint)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)0;
 }
 return *(volatile jint *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jlong JNICALL
jcgo_JniGetLongField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jlong)0L;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jlong)0L;
 }
 return *(volatile jlong *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jfloat JNICALL
jcgo_JniGetFloatField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jfloat)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jfloat)0;
 }
 return *(volatile jfloat *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC jdouble JNICALL
jcgo_JniGetDoubleField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jdouble)0;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jdouble)0;
 }
 return *(volatile jdouble *)((char *)&JCGO_METHODS_OF(jobj) +
         (unsigned)JCGO_CAST_PTRTONUM(fieldID));
}

STATIC void JNICALL
jcgo_JniSetObjectField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jobject value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(jObject volatile *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = jcgo_jniDeRef(value);
}

STATIC void JNICALL
jcgo_JniSetBooleanField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jboolean value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jboolean *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetByteField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jbyte value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jbyte *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetCharField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jchar value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jchar *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetShortField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jshort value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jshort *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetIntField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jint value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jint *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetLongField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jlong value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jlong *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetFloatField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jfloat value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jfloat *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC void JNICALL
jcgo_JniSetDoubleField( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
 jdouble value )
{
 jObject jobj;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jobj = jcgo_jniDeRef(obj);
 if (JCGO_EXPECT_FALSE(jobj == jnull || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jdouble *)((char *)&JCGO_METHODS_OF(jobj) +
  (unsigned)JCGO_CAST_PTRTONUM(fieldID)) = value;
}

STATIC jfieldID JNICALL
jcgo_JniGetStaticFieldID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 java_lang_Class aclass;
 jfieldID JCGO_TRY_VOLATILE fieldID;
#ifdef OBJT_java_lang_reflect_VMField
 java_lang_String str;
 java_lang_String sigstr;
 java_lang_Object jobj;
#endif
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jfieldID)0;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull || name == NULL || sig == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jfieldID)0;
 }
 fieldID = (jfieldID)0;
#ifdef OBJT_java_lang_reflect_VMField
 JCGO_NATCBACK_BEGIN(pJniEnv)
 str = jcgo_utfMakeString(name);
 sigstr = jcgo_utfMakeString(sig);
 jobj = java_lang_reflect_VMField__getFieldByName0X__LcLsLsI(aclass, str,
         sigstr, 1);
 if (JCGO_EXPECT_TRUE(jobj != jnull) && (aclass =
     java_lang_reflect_VMField__getStaticFieldClass0X__Lo(jobj)) != jnull)
  fieldID = (jfieldID)((char *)&JCGO_METHODS_OF(aclass) +
             (unsigned)java_lang_reflect_VMField__getSlotOfField0X__Lo(jobj));
#ifdef OBJT_java_lang_VMThrowable
  else JCGO_THROW_EXC(
        java_lang_VMThrowable__createNoSuchFieldError0X__LcLsLsI(aclass, str,
        sigstr, 1));
#endif
 JCGO_NATCBACK_END(pJniEnv)
#else
 jcgo_abortOnFieldNotFound();
#endif
 return (jfieldID)fieldID;
}

STATIC jobject JNICALL
jcgo_JniGetStaticObjectField( JNIEnv *pJniEnv, jclass clazz,
 jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 return jcgo_jniToLocalRef(pJniEnv, (jObject)(*(jObject volatile *)fieldID));
}

STATIC jboolean JNICALL
jcgo_JniGetStaticBooleanField( JNIEnv *pJniEnv, jclass clazz,
 jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jboolean)JNI_FALSE;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jboolean)JNI_FALSE;
 }
 return *(volatile jboolean *)fieldID;
}

STATIC jbyte JNICALL
jcgo_JniGetStaticByteField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jbyte)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jbyte)0;
 }
 return *(volatile jbyte *)fieldID;
}

STATIC jchar JNICALL
jcgo_JniGetStaticCharField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jchar)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jchar)0;
 }
 return *(volatile jchar *)fieldID;
}

STATIC jshort JNICALL
jcgo_JniGetStaticShortField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jshort)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jshort)0;
 }
 return *(volatile jshort *)fieldID;
}

STATIC jint JNICALL
jcgo_JniGetStaticIntField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jint)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jint)0;
 }
 return *(volatile jint *)fieldID;
}

STATIC jlong JNICALL
jcgo_JniGetStaticLongField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jlong)0L;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jlong)0L;
 }
 return *(volatile jlong *)fieldID;
}

STATIC jfloat JNICALL
jcgo_JniGetStaticFloatField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jfloat)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jfloat)0;
 }
 return *(volatile jfloat *)fieldID;
}

STATIC jdouble JNICALL
jcgo_JniGetStaticDoubleField( JNIEnv *pJniEnv, jclass clazz,
 jfieldID fieldID )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jdouble)0;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jdouble)0;
 }
 return *(volatile jdouble *)fieldID;
}

STATIC void JNICALL
jcgo_JniSetStaticObjectField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jobject value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(jObject volatile *)fieldID = jcgo_jniDeRef(value);
}

STATIC void JNICALL
jcgo_JniSetStaticBooleanField( JNIEnv *pJniEnv, jclass clazz,
 jfieldID fieldID, jboolean value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jboolean *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticByteField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jbyte value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jbyte *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticCharField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jchar value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jchar *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticShortField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jshort value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jshort *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticIntField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jint value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jint *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticLongField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jlong value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jlong *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticFloatField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jfloat value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jfloat *)fieldID = value;
}

STATIC void JNICALL
jcgo_JniSetStaticDoubleField( JNIEnv *pJniEnv, jclass clazz, jfieldID fieldID,
 jdouble value )
{
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 if (JCGO_EXPECT_FALSE(clazz == NULL || !fieldID))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 *(volatile jdouble *)fieldID = value;
}

#endif

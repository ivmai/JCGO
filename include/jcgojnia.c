/*
 * @(#) $(JCGO)/include/jcgojnia.c --
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

STATIC void *CFASTCALL jcgo_jniAllocData( JNIEnv *pJniEnv,
 JCGO_ALLOCSIZE_T size )
{
 void *JCGO_TRY_VOLATILE vptr = NULL;
 void *ptr;
 jObjectArr listEntry;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 ptr = jcgo_memAlloc(size ? size : (JCGO_ALLOCSIZE_T)1L, NULL);
 if (JCGO_EXPECT_TRUE(ptr != NULL))
 {
  listEntry = (jObjectArr)jcgo_newArray(
               JCGO_CLASSREF_OF(java_lang_Object__class), 0, 2);
  JCGO_ARR_INTERNALACC(jObject, listEntry, 0) = (jObject)ptr;
  JCGO_CRITMOD_BEGIN(jcgo_jniAllocDataMutex)
  JCGO_ARR_INTERNALACC(jObject, listEntry, 1) =
   (jObject)jcgo_globData.jniAllocatedDataList;
  jcgo_globData.jniAllocatedDataList = listEntry;
  JCGO_CRITMOD_END(jcgo_jniAllocDataMutex)
 }
#ifdef OBJT_java_lang_VMThrowable
  else java_lang_VMThrowable__throwOutOfMemoryError0X__();
#endif
 vptr = ptr;
 JCGO_NATCBACK_END(pJniEnv)
 return (void *)vptr;
}

STATIC void CFASTCALL jcgo_jniReleaseData( JNIEnv *pJniEnv, void *ptr )
{
 jObjectArr listEntry;
 jObjectArr prevEntry;
#ifndef JCGO_PARALLEL
 JCGO_NATCBACK_BEGIN(pJniEnv)
#endif
 prevEntry = jnull;
 JCGO_CRITMOD_BEGIN(jcgo_jniAllocDataMutex)
 listEntry = jcgo_globData.jniAllocatedDataList;
 while (listEntry != jnull &&
        JCGO_ARR_INTERNALACC(jObject, listEntry, 0) != (jObject)ptr)
 {
  prevEntry = listEntry;
  listEntry = (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
 }
 if (JCGO_EXPECT_TRUE(listEntry != jnull))
 {
  if (JCGO_EXPECT_TRUE(prevEntry != jnull))
   JCGO_ARR_INTERNALACC(jObject, prevEntry, 1) =
    JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
   else jcgo_globData.jniAllocatedDataList =
         (jObjectArr)JCGO_ARR_INTERNALACC(jObject, listEntry, 1);
 }
 JCGO_CRITMOD_END(jcgo_jniAllocDataMutex)
 if (listEntry == jnull)
  JCGO_FATAL_ABORT("Invalid JNI allocated data pointer!");
#ifndef JCGO_PARALLEL
 JCGO_NATCBACK_END(pJniEnv)
#endif
}

STATIC void CFASTCALL jcgo_jniThrowArrayStoreException( JNIEnv *pJniEnv )
{
#ifdef OBJT_java_lang_VMThrowable
 JCGO_NATCBACK_BEGIN(pJniEnv)
 java_lang_VMThrowable__throwArrayStoreException0X__();
 JCGO_NATCBACK_END(pJniEnv)
#endif
}

STATIC void CFASTCALL jcgo_jniThrowArrayIndexOutOfBoundsException(
 JNIEnv *pJniEnv )
{
#ifdef OBJT_java_lang_VMThrowable
 JCGO_NATCBACK_BEGIN(pJniEnv)
 java_lang_VMThrowable__throwArrayIndexOutOfBoundsException0X__();
 JCGO_NATCBACK_END(pJniEnv)
#endif
}

STATIC void CFASTCALL jcgo_jniThrowStringIndexOutOfBoundsException(
 JNIEnv *pJniEnv )
{
#ifdef OBJT_java_lang_VMThrowable
 JCGO_NATCBACK_BEGIN(pJniEnv)
 java_lang_VMThrowable__throwStringIndexOutOfBoundsException0X__();
 JCGO_NATCBACK_END(pJniEnv)
#endif
}

STATIC jstring JNICALL
jcgo_JniNewString( JNIEnv *pJniEnv, CONST jchar *chars, jsize len )
{
 java_lang_String JCGO_TRY_VOLATILE jStr;
 jcharArr JCGO_TRY_VOLATILE jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull ||
     chars == NULL))
  return NULL;
 jStr = jnull;
 jArr = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jcharArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jchar), 0, (jint)len);
 jStr = (java_lang_String)jcgo_newObject((jvtable)&java_lang_String_methods);
 JCGO_NATCBACK_END(pJniEnv)
 if (JCGO_EXPECT_TRUE(jStr != jnull))
 {
  JCGO_FIELD_NZACCESS(jStr, value) = (void *)jArr;
  JCGO_FIELD_NZACCESS(jStr, count) = (jint)len;
  if (JCGO_EXPECT_TRUE(len != 0))
   JCGO_MEM_CPY(&JCGO_ARR_INTERNALACC(jchar, jArr, 0), (void *)chars,
    (unsigned)len * sizeof(jchar));
 }
 return (jstring)jcgo_jniToLocalRef(pJniEnv, (jObject)jStr);
}

STATIC jsize JNICALL
jcgo_JniGetStringLength( JNIEnv *pJniEnv, jstring str )
{
 java_lang_String jStr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jsize)JNI_ERR;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jsize)JNI_ERR;
 }
 return (jsize)JCGO_FIELD_NZACCESS(jStr, count);
}

STATIC CONST jchar *JNICALL
jcgo_JniGetStringChars( JNIEnv *pJniEnv, jstring str, jboolean *isCopy )
{
 java_lang_String jStr;
 jchar *chars;
 jObject value;
 jint ofs;
 jint count;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 value = (jObject)JCGO_FIELD_NZACCESS(jStr, value);
 ofs = JCGO_FIELD_NZACCESS(jStr, offset);
 if (JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray + OBJT_jbyte)
 {
  count = JCGO_FIELD_NZACCESS(jStr, count);
  chars = (jchar *)jcgo_jniAllocData(pJniEnv,
           (JCGO_ALLOCSIZE_T)count * sizeof(jchar));
  if (JCGO_EXPECT_TRUE(chars != NULL))
  {
   if (isCopy != NULL)
    *isCopy = (jboolean)JNI_TRUE;
   while (count-- > 0)
    *(chars + (unsigned)count) =
     (jchar)((unsigned char)JCGO_ARR_INTERNALACC(jbyte, (jbyteArr)value,
     ofs + count));
  }
  return chars;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jchar, (jcharArr)value, ofs);
}

STATIC void JNICALL
jcgo_JniReleaseStringChars( JNIEnv *pJniEnv, jstring str, CONST jchar *chars )
{
 java_lang_String jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull))
 {
  if (JCGO_JNI_GETTCB(pJniEnv)->nativeExc == jnull)
   jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (chars != NULL &&
     JCGO_METHODS_OF(JCGO_FIELD_NZACCESS(jStr, value))->jcgo_typeid ==
     OBJT_jarray + OBJT_jbyte)
  jcgo_jniReleaseData(pJniEnv, (void *)chars);
}

STATIC jstring JNICALL
jcgo_JniNewStringUTF( JNIEnv *pJniEnv, CONST char *chars )
{
 java_lang_String JCGO_TRY_VOLATILE jStr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull ||
     chars == NULL))
  return NULL;
 jStr = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jStr = jcgo_utfMakeString(chars);
 JCGO_NATCBACK_END(pJniEnv)
 return (jstring)jcgo_jniToLocalRef(pJniEnv, (jObject)jStr);
}

STATIC jsize JNICALL
jcgo_JniGetStringUTFLength( JNIEnv *pJniEnv, jstring str )
{
 java_lang_String jStr;
 jObject value;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jsize)JNI_ERR;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jsize)JNI_ERR;
 }
 value = (jObject)JCGO_FIELD_NZACCESS(jStr, value);
 return (jsize)(JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray +
         OBJT_jbyte ? jcgo_utfLenOfBytes((jbyteArr)value,
         JCGO_FIELD_NZACCESS(jStr, offset),
         JCGO_FIELD_NZACCESS(jStr, count)) :
         jcgo_utfLenOfChars((jcharArr)value,
         JCGO_FIELD_NZACCESS(jStr, offset),
         JCGO_FIELD_NZACCESS(jStr, count)));
}

STATIC CONST char *JNICALL
jcgo_JniGetStringUTFChars( JNIEnv *pJniEnv, jstring str, jboolean *isCopy )
{
 char *chars;
 java_lang_String jStr;
 jObject value;
 jint ofs;
 jint count;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 value = (jObject)JCGO_FIELD_NZACCESS(jStr, value);
 ofs = JCGO_FIELD_NZACCESS(jStr, offset);
 count = JCGO_FIELD_NZACCESS(jStr, count);
 chars = (char *)jcgo_jniAllocData(pJniEnv,
          (JCGO_ALLOCSIZE_T)(JCGO_METHODS_OF(value)->jcgo_typeid ==
          OBJT_jarray + OBJT_jbyte ? jcgo_utfLenOfBytes((jbyteArr)value,
          ofs, count) : jcgo_utfLenOfChars((jcharArr)value, ofs, count)) +
          (JCGO_ALLOCSIZE_T)1L);
 if (JCGO_EXPECT_TRUE(chars != NULL))
 {
  if (JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray + OBJT_jbyte)
   jcgo_utfFillFromBytes(chars, (jbyteArr)value, ofs, count);
   else jcgo_utfFillFromChars(chars, (jcharArr)value, ofs, count);
  if (isCopy != NULL)
   *isCopy = (jboolean)JNI_TRUE;
 }
 return chars;
}

STATIC void JNICALL
jcgo_JniReleaseStringUTFChars( JNIEnv *pJniEnv, jstring str,
 CONST char *chars )
{
 if (JCGO_EXPECT_FALSE(str == NULL))
 {
  if (JCGO_JNI_GETTCB(pJniEnv)->nativeExc == jnull)
   jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(chars != NULL))
  jcgo_jniReleaseData(pJniEnv, (void *)chars);
}

STATIC jsize JNICALL
jcgo_JniGetArrayLength( JNIEnv *pJniEnv, jarray arr )
{
 jbyteArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jsize)JNI_ERR;
 jArr = (jbyteArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jsize)JNI_ERR;
 }
 return JCGO_ARRAY_NZLENGTH(jArr);
}

STATIC jobjectArray JNICALL
jcgo_JniNewObjectArray( JNIEnv *pJniEnv, jsize len, jclass clazz,
 jobject value )
{
 java_lang_Class aclass;
 jObject jobj;
 jObjectArr JCGO_TRY_VOLATILE jArr;
 java_lang_Class srcClass;
 int typenum;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 aclass = (java_lang_Class)jcgo_jniDeRef((jobject)clazz);
 if (JCGO_EXPECT_FALSE(aclass == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if ((jobj = jcgo_jniDeRef(value)) != jnull && JCGO_EXPECT_TRUE(len != 0))
 {
  typenum = JCGO_METHODS_OF(jobj)->jcgo_typeid;
  srcClass = JCGO_METHODS_OF(jobj)->jcgo_class;
  if (typenum >= OBJT_jarray + OBJT_void &&
      typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
   srcClass = JCGO_OBJARR_COMPCLASS((jObjectArr)jobj);
   else typenum = OBJT_jarray + OBJT_void - 1;
  if (JCGO_EXPECT_FALSE(!jcgo_isAssignable(srcClass, aclass,
      typenum - (OBJT_jarray + OBJT_void - 1), 0)))
  {
   jcgo_jniThrowArrayStoreException(pJniEnv);
   return NULL;
  }
 }
 jArr = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jObjectArr)jcgo_newArray(aclass, 0, (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 if (jobj != jnull && JCGO_EXPECT_TRUE(jArr != jnull))
  while (len-- > 0)
   JCGO_ARR_INTERNALACC(jObject, jArr, (jint)len) = jobj;
 return (jobjectArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jobject JNICALL
jcgo_JniGetObjectArrayElement( JNIEnv *pJniEnv, jobjectArray arr,
 jsize index )
{
 jObjectArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jObjectArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (JCGO_EXPECT_FALSE((jint)index < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) <= (jint)index))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return NULL;
 }
 return jcgo_jniToLocalRef(pJniEnv,
         JCGO_ARR_INTERNALACC(jObject, jArr, (jint)index));
}

STATIC void JNICALL
jcgo_JniSetObjectArrayElement( JNIEnv *pJniEnv, jobjectArray arr, jsize index,
 jobject value )
{
 jObjectArr jArr;
 jObject jobj;
 java_lang_Class srcClass;
 java_lang_Class destClass;
 int typenum;
 int destDims;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jObjectArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)index < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) <= (jint)index))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if ((jobj = jcgo_jniDeRef(value)) != jnull)
 {
  destClass = JCGO_OBJARR_COMPCLASS(jArr);
  srcClass = JCGO_METHODS_OF(jobj)->jcgo_class;
  destDims = JCGO_METHODS_OF(jArr)->jcgo_typeid - (OBJT_jarray + OBJT_void);
  if (destDims || srcClass != destClass)
  {
   typenum = JCGO_METHODS_OF(jobj)->jcgo_typeid;
   if (typenum >= OBJT_jarray + OBJT_void &&
       typenum < OBJT_jarray + OBJT_void + JCGO_DIMS_MAX)
    srcClass = JCGO_OBJARR_COMPCLASS((jObjectArr)jobj);
    else typenum = OBJT_jarray + OBJT_void - 1;
   if (JCGO_EXPECT_FALSE(!jcgo_isAssignable(srcClass, destClass,
       typenum - (OBJT_jarray + OBJT_void - 1), destDims)))
   {
    jcgo_jniThrowArrayStoreException(pJniEnv);
    return;
   }
  }
 }
 JCGO_ARR_INTERNALACC(jObject, jArr, (jint)index) = jobj;
}

STATIC jbooleanArray JNICALL
jcgo_JniNewBooleanArray( JNIEnv *pJniEnv, jsize len )
{
 jbooleanArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jbooleanArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jboolean), 0,
         (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jbooleanArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jbyteArray JNICALL
jcgo_JniNewByteArray( JNIEnv *pJniEnv, jsize len )
{
 jbyteArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jbyteArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jbyte), 0, (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jbyteArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jcharArray JNICALL
jcgo_JniNewCharArray( JNIEnv *pJniEnv, jsize len )
{
 jcharArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jcharArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jchar), 0, (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jcharArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jshortArray JNICALL
jcgo_JniNewShortArray( JNIEnv *pJniEnv, jsize len )
{
 jshortArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jshortArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jshort), 0,
         (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jshortArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jintArray JNICALL
jcgo_JniNewIntArray( JNIEnv *pJniEnv, jsize len )
{
 jintArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jintArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jint), 0, (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jintArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jlongArray JNICALL
jcgo_JniNewLongArray( JNIEnv *pJniEnv, jsize len )
{
 jlongArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jlongArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jlong), 0, (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jlongArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jfloatArray JNICALL
jcgo_JniNewFloatArray( JNIEnv *pJniEnv, jsize len )
{
 jfloatArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jfloatArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jfloat), 0,
         (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jfloatArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jdoubleArray JNICALL
jcgo_JniNewDoubleArray( JNIEnv *pJniEnv, jsize len )
{
 jdoubleArr JCGO_TRY_VOLATILE jArr = jnull;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 jArr = (jdoubleArr)jcgo_newArray(JCGO_CORECLASS_FOR(OBJT_jdouble), 0,
         (jint)len);
 JCGO_NATCBACK_END(pJniEnv)
 return (jdoubleArray)jcgo_jniToLocalRef(pJniEnv, (jObject)jArr);
}

STATIC jboolean *JNICALL
jcgo_JniGetBooleanArrayElements( JNIEnv *pJniEnv, jbooleanArray arr,
 jboolean *isCopy )
{
 jbooleanArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jbooleanArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jboolean, jArr, 0);
}

STATIC jbyte *JNICALL
jcgo_JniGetByteArrayElements( JNIEnv *pJniEnv, jbyteArray arr,
 jboolean *isCopy )
{
 jbyteArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jbyteArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jbyte, jArr, 0);
}

STATIC jchar *JNICALL
jcgo_JniGetCharArrayElements( JNIEnv *pJniEnv, jcharArray arr,
 jboolean *isCopy )
{
 jcharArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jcharArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jchar, jArr, 0);
}

STATIC jshort *JNICALL
jcgo_JniGetShortArrayElements( JNIEnv *pJniEnv, jshortArray arr,
 jboolean *isCopy )
{
 jshortArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jshortArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jshort, jArr, 0);
}

STATIC jint *JNICALL
jcgo_JniGetIntArrayElements( JNIEnv *pJniEnv, jintArray arr,
 jboolean *isCopy )
{
 jintArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jintArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jint, jArr, 0);
}

STATIC jlong *JNICALL
jcgo_JniGetLongArrayElements( JNIEnv *pJniEnv, jlongArray arr,
 jboolean *isCopy )
{
 jlongArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jlongArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jlong, jArr, 0);
}

STATIC jfloat *JNICALL
jcgo_JniGetFloatArrayElements( JNIEnv *pJniEnv, jfloatArray arr,
 jboolean *isCopy )
{
 jfloatArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jfloatArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jfloat, jArr, 0);
}

STATIC jdouble *JNICALL
jcgo_JniGetDoubleArrayElements( JNIEnv *pJniEnv, jdoubleArray arr,
 jboolean *isCopy )
{
 jdoubleArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jdoubleArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return &JCGO_ARR_INTERNALACC(jdouble, jArr, 0);
}

STATIC void JNICALL
jcgo_JniReleaseBooleanArrayElements( JNIEnv *pJniEnv, jbooleanArray arr,
 jboolean *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseByteArrayElements( JNIEnv *pJniEnv, jbyteArray arr,
 jbyte *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseCharArrayElements( JNIEnv *pJniEnv, jcharArray arr,
 jchar *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseShortArrayElements( JNIEnv *pJniEnv, jshortArray arr,
 jshort *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseIntArrayElements( JNIEnv *pJniEnv, jintArray arr, jint *elems,
 jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseLongArrayElements( JNIEnv *pJniEnv, jlongArray arr,
 jlong *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseFloatArrayElements( JNIEnv *pJniEnv, jfloatArray arr,
 jfloat *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniReleaseDoubleArrayElements( JNIEnv *pJniEnv, jdoubleArray arr,
 jdouble *elems, jint mode )
{
 /* dummy */
}

STATIC void JNICALL
jcgo_JniGetBooleanArrayRegion( JNIEnv *pJniEnv, jbooleanArray arr,
 jsize start, jsize len, jboolean *buf )
{
 jbooleanArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jbooleanArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jboolean, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jboolean));
}

STATIC void JNICALL
jcgo_JniGetByteArrayRegion( JNIEnv *pJniEnv, jbyteArray arr, jsize start,
 jsize len, jbyte *buf )
{
 jbyteArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jbyteArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jbyte, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len);
}

STATIC void JNICALL
jcgo_JniGetCharArrayRegion( JNIEnv *pJniEnv, jcharArray arr, jsize start,
 jsize len, jchar *buf )
{
 jcharArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jcharArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jchar, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jchar));
}

STATIC void JNICALL
jcgo_JniGetShortArrayRegion( JNIEnv *pJniEnv, jshortArray arr, jsize start,
 jsize len, jshort *buf )
{
 jshortArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jshortArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jshort, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jshort));
}

STATIC void JNICALL
jcgo_JniGetIntArrayRegion( JNIEnv *pJniEnv, jintArray arr, jsize start,
 jsize len, jint *buf )
{
 jintArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jintArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jint, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jint));
}

STATIC void JNICALL
jcgo_JniGetLongArrayRegion( JNIEnv *pJniEnv, jlongArray arr, jsize start,
 jsize len, jlong *buf )
{
 jlongArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jlongArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jlong, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jlong));
}

STATIC void JNICALL
jcgo_JniGetFloatArrayRegion( JNIEnv *pJniEnv, jfloatArray arr, jsize start,
 jsize len, jfloat *buf )
{
 jfloatArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jfloatArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jfloat, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jfloat));
}

STATIC void JNICALL
jcgo_JniGetDoubleArrayRegion( JNIEnv *pJniEnv, jdoubleArray arr,
 jsize start, jsize len, jdouble *buf )
{
 jdoubleArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jdoubleArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(buf, &JCGO_ARR_INTERNALACC(jdouble, jArr, (jint)start),
   (JCGO_ALLOCSIZE_T)len * sizeof(jdouble));
}

STATIC void JNICALL
jcgo_JniSetBooleanArrayRegion( JNIEnv *pJniEnv, jbooleanArray arr,
 jsize start, jsize len, CONST jboolean *buf )
{
 jbooleanArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jbooleanArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jboolean, jArr, (jint)start),
   (void *)buf, (JCGO_ALLOCSIZE_T)len * sizeof(jboolean));
}

STATIC void JNICALL
jcgo_JniSetByteArrayRegion( JNIEnv *pJniEnv, jbyteArray arr, jsize start,
 jsize len, CONST jbyte *buf )
{
 jbyteArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jbyteArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jbyte, jArr, (jint)start), (void *)buf,
   (JCGO_ALLOCSIZE_T)len);
}

STATIC void JNICALL
jcgo_JniSetCharArrayRegion( JNIEnv *pJniEnv, jcharArray arr, jsize start,
 jsize len, CONST jchar *buf )
{
 jcharArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jcharArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jchar, jArr, (jint)start), (void *)buf,
   (JCGO_ALLOCSIZE_T)len * sizeof(jchar));
}

STATIC void JNICALL
jcgo_JniSetShortArrayRegion( JNIEnv *pJniEnv, jshortArray arr, jsize start,
 jsize len, CONST jshort *buf )
{
 jshortArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jshortArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jshort, jArr, (jint)start),
   (void *)buf, (JCGO_ALLOCSIZE_T)len * sizeof(jshort));
}

STATIC void JNICALL
jcgo_JniSetIntArrayRegion( JNIEnv *pJniEnv, jintArray arr, jsize start,
 jsize len, CONST jint *buf )
{
 jintArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jintArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jint, jArr, (jint)start), (void *)buf,
   (JCGO_ALLOCSIZE_T)len * sizeof(jint));
}

STATIC void JNICALL
jcgo_JniSetLongArrayRegion( JNIEnv *pJniEnv, jlongArray arr, jsize start,
 jsize len, CONST jlong *buf )
{
 jlongArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jlongArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jlong, jArr, (jint)start), (void *)buf,
   (JCGO_ALLOCSIZE_T)len * sizeof(jlong));
}

STATIC void JNICALL
jcgo_JniSetFloatArrayRegion( JNIEnv *pJniEnv, jfloatArray arr, jsize start,
 jsize len, CONST jfloat *buf )
{
 jfloatArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jfloatArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jfloat, jArr, (jint)start),
   (void *)buf, (JCGO_ALLOCSIZE_T)len * sizeof(jfloat));
}

STATIC void JNICALL
jcgo_JniSetDoubleArrayRegion( JNIEnv *pJniEnv, jdoubleArray arr, jsize start,
 jsize len, CONST jdouble *buf )
{
 jdoubleArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jArr = (jdoubleArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_ARRAY_NZLENGTH(jArr) - (jint)start < (jint)len))
 {
  jcgo_jniThrowArrayIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
  JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jdouble, jArr, (jint)start),
   (void *)buf, (JCGO_ALLOCSIZE_T)len * sizeof(jdouble));
}

STATIC void JNICALL
jcgo_JniGetStringRegion( JNIEnv *pJniEnv, jstring str, jsize start, jsize len,
 jchar *buf )
{
 java_lang_String jStr;
 jObject value;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_FIELD_NZACCESS(jStr, count) - (jint)start < (jint)len))
 {
  jcgo_jniThrowStringIndexOutOfBoundsException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_TRUE(len != 0))
 {
  value = (jObject)JCGO_FIELD_NZACCESS(jStr, value);
  start = (jsize)JCGO_FIELD_NZACCESS(jStr, offset) + start;
  if (JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray + OBJT_jbyte)
  {
   while (len-- > 0)
    *(buf + (unsigned)len) =
     (jchar)((unsigned char)JCGO_ARR_INTERNALACC(jbyte, (jbyteArr)value,
     (jint)(start + len)));
  }
   else JCGO_MEM_CPY(buf, &JCGO_ARR_INTERNALACC(jchar, (jcharArr)value,
         (jint)start), (unsigned)len * sizeof(jchar));
 }
}

STATIC void JNICALL
jcgo_JniGetStringUTFRegion( JNIEnv *pJniEnv, jstring str, jsize start,
 jsize len, char *buf )
{
 java_lang_String jStr;
 jObject value;
 unsigned pos;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return;
 jStr = (java_lang_String)jcgo_jniDeRef((jobject)str);
 if (JCGO_EXPECT_FALSE(jStr == jnull || buf == NULL))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return;
 }
 if (JCGO_EXPECT_FALSE((jint)(start | len) < 0 ||
     JCGO_FIELD_NZACCESS(jStr, count) - (jint)start < (jint)len))
 {
  jcgo_jniThrowStringIndexOutOfBoundsException(pJniEnv);
  return;
 }
 pos = 0;
 if (JCGO_EXPECT_TRUE(len != 0))
 {
  value = (jObject)JCGO_FIELD_NZACCESS(jStr, value);
  start = (jsize)JCGO_FIELD_NZACCESS(jStr, offset) + start;
  pos = JCGO_METHODS_OF(value)->jcgo_typeid == OBJT_jarray + OBJT_jbyte ?
         jcgo_utfFillFromBytes(buf, (jbyteArr)value, (jint)start, (jint)len) :
         jcgo_utfFillFromChars(buf, (jcharArr)value, (jint)start, (jint)len);
 }
 *(buf + pos) = '\0';
}

STATIC void *JNICALL
jcgo_JniGetPrimitiveArrayCritical( JNIEnv *pJniEnv, jarray arr,
 jboolean *isCopy )
{
 jbyteArr jArr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 jArr = (jbyteArr)jcgo_jniDeRef((jobject)arr);
 if (JCGO_EXPECT_FALSE(jArr == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 if (JCGO_METHODS_OF(jArr)->jcgo_typeid >= OBJT_jarray + OBJT_void)
  JCGO_FATAL_ABORT("Cannot get JNI non-primitive critical array elements!");
 if (isCopy != NULL)
  *isCopy = (jboolean)JNI_FALSE;
 return (void *)((volatile char JCGO_HPTR_MOD *)jArr + jcgo_primitiveOffset[
         JCGO_METHODS_OF(jArr)->jcgo_typeid - OBJT_jarray]);
}

STATIC void JNICALL
jcgo_JniReleasePrimitiveArrayCritical( JNIEnv *pJniEnv, jarray arr,
 void *elems, jint mode )
{
 /* dummy */
}

STATIC CONST jchar *JNICALL
jcgo_JniGetStringCritical( JNIEnv *pJniEnv, jstring str, jboolean *isCopy )
{
 return jcgo_JniGetStringChars(pJniEnv, str, isCopy);
}

STATIC void JNICALL
jcgo_JniReleaseStringCritical( JNIEnv *pJniEnv, jstring str,
 CONST jchar *chars )
{
 jcgo_JniReleaseStringChars(pJniEnv, str, chars);
}

STATIC jobject JNICALL
jcgo_JniNewDirectByteBuffer( JNIEnv *pJniEnv, void *address, jlong capacity )
{
#ifdef OBJT_java_nio_VMDirectByteBuffer
 jObject JCGO_TRY_VOLATILE buffer;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 buffer = jnull;
 JCGO_NATCBACK_BEGIN(pJniEnv)
 buffer = (jObject)java_nio_VMDirectByteBuffer__newDirectByteBuffer0X__LoJ(
           (java_lang_Object)address, capacity);
 JCGO_NATCBACK_END(pJniEnv)
 return jcgo_jniToLocalRef(pJniEnv, (jObject)buffer);
#else
 if (*(void *volatile *)&jcgo_noTypesClassArr.jcgo_methods != NULL)
  JCGO_FATAL_ABORT("Cannot find java.nio.VMDirectByteBuffer!");
 return NULL;
#endif
}

STATIC void *JNICALL
jcgo_JniGetDirectBufferAddress( JNIEnv *pJniEnv, jobject buf )
{
 jObject buffer;
 void *JCGO_TRY_VOLATILE addr;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return NULL;
 buffer = jcgo_jniDeRef(buf);
 if (JCGO_EXPECT_FALSE(buffer == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return NULL;
 }
 addr = NULL;
#ifdef OBJT_java_nio_VMDirectByteBuffer
 JCGO_NATCBACK_BEGIN(pJniEnv)
 addr =
  (void *)java_nio_VMDirectByteBuffer__getDirectBufferAddressVmData0X__Lo(
  (java_lang_Object)buffer);
 if (addr != NULL)
  addr = (char JCGO_HPTR_MOD *)addr + (JCGO_ALLOCSIZE_T)
          java_nio_VMDirectByteBuffer__getDirectBufferAddressOffset0X__Lo(
          (java_lang_Object)buffer);
 JCGO_NATCBACK_END(pJniEnv)
#endif
 return (void *)addr;
}

STATIC jlong JNICALL
jcgo_JniGetDirectBufferCapacity( JNIEnv *pJniEnv, jobject buf )
{
 jObject buffer;
 JCGO_TRY_VOLATILE jlong capacity;
 if (JCGO_EXPECT_FALSE(JCGO_JNI_GETTCB(pJniEnv)->nativeExc != jnull))
  return (jlong)-1L;
 buffer = jcgo_jniDeRef(buf);
 if (JCGO_EXPECT_FALSE(buffer == jnull))
 {
  jcgo_jniThrowNullPointerException(pJniEnv);
  return (jlong)-1L;
 }
 capacity = (jlong)-1L;
#ifdef OBJT_java_nio_VMDirectByteBuffer
 JCGO_NATCBACK_BEGIN(pJniEnv)
 capacity = java_nio_VMDirectByteBuffer__getDirectBufferCapacity0X__Lo(
             (java_lang_Object)buffer);
 JCGO_NATCBACK_END(pJniEnv)
#endif
 return capacity;
}

#endif

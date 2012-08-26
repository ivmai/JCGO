/*
 * @(#) $(JCGO)/include/jcgojni.h --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not compile this file directly).
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

#ifndef JCGO_VER

#include "jcgover.h"

#include "jcgojnmd.h"

#ifdef JCGO_VER

/* #include <stdarg.h> */
/* typedef va_list; */

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef CONST
#define CONST const
#endif

#define JNI_FALSE 0
#define JNI_TRUE 1

#define JNI_VERSION_1_1 0x10001
#define JNI_VERSION_1_2 0x10002
#define JNI_VERSION_1_4 0x10004
#define JNI_VERSION_1_6 0x10006

#define JNI_COMMIT 1
#define JNI_ABORT 2

#define JNI_OK 0
#define JNI_ERR (-1)
#define JNI_EDETACHED (-2)
#define JNI_EVERSION (-3)
#define JNI_ENOMEM (-4)
#define JNI_EEXIST (-5)
#define JNI_EINVAL (-6)

typedef struct { int jobjectOpaque; } *jobject;
typedef struct { int jclassOpaque; } *jclass;
typedef struct { int jstringOpaque; } *jstring;
typedef struct { int jthrowableOpaque; } *jthrowable;
typedef struct { int jweakOpaque; } *jweak;

typedef struct { int jarrayOpaque; } *jarray;
typedef struct { int jobjectArrayOpaque; } *jobjectArray;
typedef struct { int jbooleanArrayOpaque; } *jbooleanArray;
typedef struct { int jbyteArrayOpaque; } *jbyteArray;
typedef struct { int jcharArrayOpaque; } *jcharArray;
typedef struct { int jshortArrayOpaque; } *jshortArray;
typedef struct { int jintArrayOpaque; } *jintArray;
typedef struct { int jlongArrayOpaque; } *jlongArray;
typedef struct { int jfloatArrayOpaque; } *jfloatArray;
typedef struct { int jdoubleArrayOpaque; } *jdoubleArray;

struct JNINativeInterface_;
struct JNIInvokeInterface_;

typedef CONST struct JNINativeInterface_ *JNIEnv;
typedef CONST struct JNIInvokeInterface_ *JavaVM;

struct _jfieldID { int jfieldOpaque; };
struct _jmethodID { int jmethodOpaque; };

typedef struct _jfieldID *jfieldID;
typedef struct _jmethodID *jmethodID;

union jvalue
{
 jboolean z;
 jbyte b;
 jchar c;
 jshort s;
 jint i;
 jlong j;
 jfloat f;
 jdouble d;
 jobject l;
};

typedef union jvalue jvalue;

typedef struct
{
 char *name;
 char *signature;
 void *fnPtr;
} JNINativeMethod;

#define JNIInvalidRefType 0
#define JNILocalRefType 1
#define JNIGlobalRefType 2
#define JNIWeakGlobalRefType 3
typedef int jobjectRefType;
/*typedef enum _jobjectType {
 JNIInvalidRefType = 0,
 JNILocalRefType = 1,
 JNIGlobalRefType = 2,
 JNIWeakGlobalRefType = 3
} jobjectRefType;*/

struct JNINativeInterface_
{
 void (JNICALL *reserved0)( void );
#ifndef JCGO_NOJNI
 void (JNICALL *reserved1)( void );
 void (JNICALL *reserved2)( void );
 void (JNICALL *reserved3)( void );

 jint (JNICALL *GetVersion)( JNIEnv *pJniEnv );

 jclass (JNICALL *DefineClass)( JNIEnv *pJniEnv, CONST char *name,
  jobject loader, CONST jbyte *buf, jsize bufLen );
 jclass (JNICALL *FindClass)( JNIEnv *pJniEnv, CONST char *name );

 jmethodID (JNICALL *FromReflectedMethod)( JNIEnv *pJniEnv, jobject method );
 jfieldID (JNICALL *FromReflectedField)( JNIEnv *pJniEnv, jobject field );
 jobject (JNICALL *ToReflectedMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, jboolean isStatic );

 jclass (JNICALL *GetSuperclass)( JNIEnv *pJniEnv, jclass clazz );
 jboolean (JNICALL *IsAssignableFrom)( JNIEnv *pJniEnv, jclass subclass,
  jclass clazz );

 jobject (JNICALL *ToReflectedField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jboolean isStatic );

 jint (JNICALL *Throw)( JNIEnv *pJniEnv, jthrowable obj );
 jint (JNICALL *ThrowNew)( JNIEnv *pJniEnv, jclass clazz, CONST char *msg );
 jthrowable (JNICALL *ExceptionOccurred)( JNIEnv *pJniEnv );
 void (JNICALL *ExceptionDescribe)( JNIEnv *pJniEnv );
 void (JNICALL *ExceptionClear)( JNIEnv *pJniEnv );
 void (JNICALL *FatalError)( JNIEnv *pJniEnv, CONST char *msg );

 jint (JNICALL *PushLocalFrame)( JNIEnv *pJniEnv, jint capacity );
 jobject (JNICALL *PopLocalFrame)( JNIEnv *pJniEnv, jobject obj );
 jobject (JNICALL *NewGlobalRef)( JNIEnv *pJniEnv, jobject obj );
 void (JNICALL *DeleteGlobalRef)( JNIEnv *pJniEnv, jobject globalref );
 void (JNICALL *DeleteLocalRef)( JNIEnv *pJniEnv, jobject localref );
 jboolean (JNICALL *IsSameObject)( JNIEnv *pJniEnv, jobject obj1,
  jobject obj2 );
 jobject (JNICALL *NewLocalRef)( JNIEnv *pJniEnv, jobject obj );
 jint (JNICALL *EnsureLocalCapacity)( JNIEnv *pJniEnv, jint capacity );

 jobject (JNICALL *AllocObject)( JNIEnv *pJniEnv, jclass clazz );
 jobject (JNICALL *NewObject)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jobject (JNICALL *NewObjectV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jobject (JNICALL *NewObjectA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );

 jclass (JNICALL *GetObjectClass)( JNIEnv *pJniEnv, jobject obj );
 jboolean (JNICALL *IsInstanceOf)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz );

 jmethodID (JNICALL *GetMethodID)( JNIEnv *pJniEnv, jclass clazz,
  CONST char *name, CONST char *sig );

 jobject (JNICALL *CallObjectMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jobject (JNICALL *CallObjectMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jobject (JNICALL *CallObjectMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jboolean (JNICALL *CallBooleanMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jboolean (JNICALL *CallBooleanMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jboolean (JNICALL *CallBooleanMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jbyte (JNICALL *CallByteMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jbyte (JNICALL *CallByteMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jbyte (JNICALL *CallByteMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jchar (JNICALL *CallCharMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jchar (JNICALL *CallCharMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jchar (JNICALL *CallCharMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jshort (JNICALL *CallShortMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jshort (JNICALL *CallShortMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jshort (JNICALL *CallShortMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jint (JNICALL *CallIntMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jint (JNICALL *CallIntMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jint (JNICALL *CallIntMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jlong (JNICALL *CallLongMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jlong (JNICALL *CallLongMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jlong (JNICALL *CallLongMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jfloat (JNICALL *CallFloatMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jfloat (JNICALL *CallFloatMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jfloat (JNICALL *CallFloatMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 jdouble (JNICALL *CallDoubleMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 jdouble (JNICALL *CallDoubleMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 jdouble (JNICALL *CallDoubleMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );
 void (JNICALL *CallVoidMethod)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, ... );
 void (JNICALL *CallVoidMethodV)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, va_list args );
 void (JNICALL *CallVoidMethodA)( JNIEnv *pJniEnv, jobject obj,
  jmethodID methodID, CONST jvalue *args );

 jobject (JNICALL *CallNonvirtualObjectMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jobject (JNICALL *CallNonvirtualObjectMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jobject (JNICALL *CallNonvirtualObjectMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jboolean (JNICALL *CallNonvirtualBooleanMethod)( JNIEnv *pJniEnv,
  jobject obj, jclass clazz, jmethodID methodID, ... );
 jboolean (JNICALL *CallNonvirtualBooleanMethodV)( JNIEnv *pJniEnv,
  jobject obj, jclass clazz, jmethodID methodID, va_list args );
 jboolean (JNICALL *CallNonvirtualBooleanMethodA)( JNIEnv *pJniEnv,
  jobject obj, jclass clazz, jmethodID methodID, CONST jvalue *args );
 jbyte (JNICALL *CallNonvirtualByteMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jbyte (JNICALL *CallNonvirtualByteMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jbyte (JNICALL *CallNonvirtualByteMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jchar (JNICALL *CallNonvirtualCharMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jchar (JNICALL *CallNonvirtualCharMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jchar (JNICALL *CallNonvirtualCharMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jshort (JNICALL *CallNonvirtualShortMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jshort (JNICALL *CallNonvirtualShortMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jshort (JNICALL *CallNonvirtualShortMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jint (JNICALL *CallNonvirtualIntMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jint (JNICALL *CallNonvirtualIntMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jint (JNICALL *CallNonvirtualIntMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jlong (JNICALL *CallNonvirtualLongMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jlong (JNICALL *CallNonvirtualLongMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jlong (JNICALL *CallNonvirtualLongMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jfloat (JNICALL *CallNonvirtualFloatMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jfloat (JNICALL *CallNonvirtualFloatMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jfloat (JNICALL *CallNonvirtualFloatMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 jdouble (JNICALL *CallNonvirtualDoubleMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 jdouble (JNICALL *CallNonvirtualDoubleMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 jdouble (JNICALL *CallNonvirtualDoubleMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );
 void (JNICALL *CallNonvirtualVoidMethod)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, ... );
 void (JNICALL *CallNonvirtualVoidMethodV)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, va_list args );
 void (JNICALL *CallNonvirtualVoidMethodA)( JNIEnv *pJniEnv, jobject obj,
  jclass clazz, jmethodID methodID, CONST jvalue *args );

 jfieldID (JNICALL *GetFieldID)( JNIEnv *pJniEnv, jclass clazz,
  CONST char *name, CONST char *sig );

 jobject (JNICALL *GetObjectField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jboolean (JNICALL *GetBooleanField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jbyte (JNICALL *GetByteField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jchar (JNICALL *GetCharField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jshort (JNICALL *GetShortField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jint (JNICALL *GetIntField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jlong (JNICALL *GetLongField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jfloat (JNICALL *GetFloatField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );
 jdouble (JNICALL *GetDoubleField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID );

 void (JNICALL *SetObjectField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID, jobject value );
 void (JNICALL *SetBooleanField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID, jboolean value );
 void (JNICALL *SetByteField)( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
  jbyte value );
 void (JNICALL *SetCharField)( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
  jchar value );
 void (JNICALL *SetShortField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID, jshort value );
 void (JNICALL *SetIntField)( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
  jint value );
 void (JNICALL *SetLongField)( JNIEnv *pJniEnv, jobject obj, jfieldID fieldID,
  jlong value );
 void (JNICALL *SetFloatField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID, jfloat value );
 void (JNICALL *SetDoubleField)( JNIEnv *pJniEnv, jobject obj,
  jfieldID fieldID, jdouble value );

 jmethodID (JNICALL *GetStaticMethodID)( JNIEnv *pJniEnv, jclass clazz,
  CONST char *name, CONST char *sig );

 jobject (JNICALL *CallStaticObjectMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jobject (JNICALL *CallStaticObjectMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jobject (JNICALL *CallStaticObjectMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jboolean (JNICALL *CallStaticBooleanMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jboolean (JNICALL *CallStaticBooleanMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jboolean (JNICALL *CallStaticBooleanMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jbyte (JNICALL *CallStaticByteMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jbyte (JNICALL *CallStaticByteMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jbyte (JNICALL *CallStaticByteMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jchar (JNICALL *CallStaticCharMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jchar (JNICALL *CallStaticCharMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jchar (JNICALL *CallStaticCharMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jshort (JNICALL *CallStaticShortMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jshort (JNICALL *CallStaticShortMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jshort (JNICALL *CallStaticShortMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jint (JNICALL *CallStaticIntMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jint (JNICALL *CallStaticIntMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jint (JNICALL *CallStaticIntMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jlong (JNICALL *CallStaticLongMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jlong (JNICALL *CallStaticLongMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jlong (JNICALL *CallStaticLongMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jfloat (JNICALL *CallStaticFloatMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jfloat (JNICALL *CallStaticFloatMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jfloat (JNICALL *CallStaticFloatMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 jdouble (JNICALL *CallStaticDoubleMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 jdouble (JNICALL *CallStaticDoubleMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 jdouble (JNICALL *CallStaticDoubleMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );
 void (JNICALL *CallStaticVoidMethod)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, ... );
 void (JNICALL *CallStaticVoidMethodV)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, va_list args );
 void (JNICALL *CallStaticVoidMethodA)( JNIEnv *pJniEnv, jclass clazz,
  jmethodID methodID, CONST jvalue *args );

 jfieldID (JNICALL *GetStaticFieldID)( JNIEnv *pJniEnv, jclass clazz,
  CONST char *name, CONST char *sig );

 jobject (JNICALL *GetStaticObjectField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jboolean (JNICALL *GetStaticBooleanField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jbyte (JNICALL *GetStaticByteField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jchar (JNICALL *GetStaticCharField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jshort (JNICALL *GetStaticShortField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jint (JNICALL *GetStaticIntField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jlong (JNICALL *GetStaticLongField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jfloat (JNICALL *GetStaticFloatField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );
 jdouble (JNICALL *GetStaticDoubleField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID );

 void (JNICALL *SetStaticObjectField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jobject value );
 void (JNICALL *SetStaticBooleanField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jboolean value );
 void (JNICALL *SetStaticByteField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jbyte value );
 void (JNICALL *SetStaticCharField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jchar value );
 void (JNICALL *SetStaticShortField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jshort value );
 void (JNICALL *SetStaticIntField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jint value );
 void (JNICALL *SetStaticLongField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jlong value );
 void (JNICALL *SetStaticFloatField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jfloat value );
 void (JNICALL *SetStaticDoubleField)( JNIEnv *pJniEnv, jclass clazz,
  jfieldID fieldID, jdouble value );

 jstring (JNICALL *NewString)( JNIEnv *pJniEnv, CONST jchar *chars,
  jsize len );
 jsize (JNICALL *GetStringLength)( JNIEnv *pJniEnv, jstring str );
 CONST jchar *(JNICALL *GetStringChars)( JNIEnv *pJniEnv, jstring str,
  jboolean *isCopy );
 void (JNICALL *ReleaseStringChars)( JNIEnv *pJniEnv, jstring str,
  CONST jchar *chars );
 jstring (JNICALL *NewStringUTF)( JNIEnv *pJniEnv, CONST char *chars );
 jsize (JNICALL *GetStringUTFLength)( JNIEnv *pJniEnv, jstring str );
 CONST char *(JNICALL *GetStringUTFChars)( JNIEnv *pJniEnv, jstring str,
  jboolean *isCopy );
 void (JNICALL *ReleaseStringUTFChars)( JNIEnv *pJniEnv, jstring str,
  CONST char *chars );

 jsize (JNICALL *GetArrayLength)( JNIEnv *pJniEnv, jarray arr );

 jobjectArray (JNICALL *NewObjectArray)( JNIEnv *pJniEnv, jsize len,
  jclass clazz, jobject value );
 jobject (JNICALL *GetObjectArrayElement)( JNIEnv *pJniEnv, jobjectArray arr,
  jsize index );
 void (JNICALL *SetObjectArrayElement)( JNIEnv *pJniEnv, jobjectArray arr,
  jsize index, jobject value );

 jbooleanArray (JNICALL *NewBooleanArray)( JNIEnv *pJniEnv, jsize len );
 jbyteArray (JNICALL *NewByteArray)( JNIEnv *pJniEnv, jsize len );
 jcharArray (JNICALL *NewCharArray)( JNIEnv *pJniEnv, jsize len );
 jshortArray (JNICALL *NewShortArray)( JNIEnv *pJniEnv, jsize len );
 jintArray (JNICALL *NewIntArray)( JNIEnv *pJniEnv, jsize len );
 jlongArray (JNICALL *NewLongArray)( JNIEnv *pJniEnv, jsize len );
 jfloatArray (JNICALL *NewFloatArray)( JNIEnv *pJniEnv, jsize len );
 jdoubleArray (JNICALL *NewDoubleArray)( JNIEnv *pJniEnv, jsize len );

 jboolean *(JNICALL *GetBooleanArrayElements)( JNIEnv *pJniEnv,
  jbooleanArray arr, jboolean *isCopy );
 jbyte *(JNICALL *GetByteArrayElements)( JNIEnv *pJniEnv, jbyteArray arr,
  jboolean *isCopy );
 jchar *(JNICALL *GetCharArrayElements)( JNIEnv *pJniEnv, jcharArray arr,
  jboolean *isCopy );
 jshort *(JNICALL *GetShortArrayElements)( JNIEnv *pJniEnv, jshortArray arr,
  jboolean *isCopy );
 jint *(JNICALL *GetIntArrayElements)( JNIEnv *pJniEnv, jintArray arr,
  jboolean *isCopy );
 jlong *(JNICALL *GetLongArrayElements)( JNIEnv *pJniEnv, jlongArray arr,
  jboolean *isCopy );
 jfloat *(JNICALL *GetFloatArrayElements)( JNIEnv *pJniEnv, jfloatArray arr,
  jboolean *isCopy );
 jdouble *(JNICALL *GetDoubleArrayElements)( JNIEnv *pJniEnv,
  jdoubleArray arr, jboolean *isCopy );

 void (JNICALL *ReleaseBooleanArrayElements)( JNIEnv *pJniEnv,
  jbooleanArray arr, jboolean *elems, jint mode );
 void (JNICALL *ReleaseByteArrayElements)( JNIEnv *pJniEnv, jbyteArray arr,
  jbyte *elems, jint mode );
 void (JNICALL *ReleaseCharArrayElements)( JNIEnv *pJniEnv, jcharArray arr,
  jchar *elems, jint mode );
 void (JNICALL *ReleaseShortArrayElements)( JNIEnv *pJniEnv, jshortArray arr,
  jshort *elems, jint mode );
 void (JNICALL *ReleaseIntArrayElements)( JNIEnv *pJniEnv, jintArray arr,
  jint *elems, jint mode );
 void (JNICALL *ReleaseLongArrayElements)( JNIEnv *pJniEnv, jlongArray arr,
  jlong *elems, jint mode );
 void (JNICALL *ReleaseFloatArrayElements)( JNIEnv *pJniEnv, jfloatArray arr,
  jfloat *elems, jint mode );
 void (JNICALL *ReleaseDoubleArrayElements)( JNIEnv *pJniEnv,
  jdoubleArray arr, jdouble *elems, jint mode );

 void (JNICALL *GetBooleanArrayRegion)( JNIEnv *pJniEnv, jbooleanArray arr,
  jsize start, jsize len, jboolean *buf );
 void (JNICALL *GetByteArrayRegion)( JNIEnv *pJniEnv, jbyteArray arr,
  jsize start, jsize len, jbyte *buf );
 void (JNICALL *GetCharArrayRegion)( JNIEnv *pJniEnv, jcharArray arr,
  jsize start, jsize len, jchar *buf );
 void (JNICALL *GetShortArrayRegion)( JNIEnv *pJniEnv, jshortArray arr,
  jsize start, jsize len, jshort *buf );
 void (JNICALL *GetIntArrayRegion)( JNIEnv *pJniEnv, jintArray arr,
  jsize start, jsize len, jint *buf );
 void (JNICALL *GetLongArrayRegion)( JNIEnv *pJniEnv, jlongArray arr,
  jsize start, jsize len, jlong *buf );
 void (JNICALL *GetFloatArrayRegion)( JNIEnv *pJniEnv, jfloatArray arr,
  jsize start, jsize len, jfloat *buf );
 void (JNICALL *GetDoubleArrayRegion)( JNIEnv *pJniEnv, jdoubleArray arr,
  jsize start, jsize len, jdouble *buf );

 void (JNICALL *SetBooleanArrayRegion)( JNIEnv *pJniEnv, jbooleanArray arr,
  jsize start, jsize len, CONST jboolean *buf );
 void (JNICALL *SetByteArrayRegion)( JNIEnv *pJniEnv, jbyteArray arr,
  jsize start, jsize len, CONST jbyte *buf );
 void (JNICALL *SetCharArrayRegion)( JNIEnv *pJniEnv, jcharArray arr,
  jsize start, jsize len, CONST jchar *buf );
 void (JNICALL *SetShortArrayRegion)( JNIEnv *pJniEnv, jshortArray arr,
  jsize start, jsize len, CONST jshort *buf );
 void (JNICALL *SetIntArrayRegion)( JNIEnv *pJniEnv, jintArray arr,
  jsize start, jsize len, CONST jint *buf );
 void (JNICALL *SetLongArrayRegion)( JNIEnv *pJniEnv, jlongArray arr,
  jsize start, jsize len, CONST jlong *buf );
 void (JNICALL *SetFloatArrayRegion)( JNIEnv *pJniEnv, jfloatArray arr,
  jsize start, jsize len, CONST jfloat *buf );
 void (JNICALL *SetDoubleArrayRegion)( JNIEnv *pJniEnv, jdoubleArray arr,
  jsize start, jsize len, CONST jdouble *buf );

 jint (JNICALL *RegisterNatives)( JNIEnv *pJniEnv, jclass clazz,
  CONST JNINativeMethod *methods, jint nMethods );
 jint (JNICALL *UnregisterNatives)( JNIEnv *pJniEnv, jclass clazz );

 jint (JNICALL *MonitorEnter)( JNIEnv *pJniEnv, jobject obj );
 jint (JNICALL *MonitorExit)( JNIEnv *pJniEnv, jobject obj );

 jint (JNICALL *GetJavaVM)( JNIEnv *pJniEnv, JavaVM **pvm );

 void (JNICALL *GetStringRegion)( JNIEnv *pJniEnv, jstring str, jsize start,
  jsize len, jchar *buf );
 void (JNICALL *GetStringUTFRegion)( JNIEnv *pJniEnv, jstring str,
  jsize start, jsize len, char *buf );

 void *(JNICALL *GetPrimitiveArrayCritical)( JNIEnv *pJniEnv, jarray arr,
  jboolean *isCopy );
 void (JNICALL *ReleasePrimitiveArrayCritical)( JNIEnv *pJniEnv, jarray arr,
  void *elems, jint mode );
 CONST jchar *(JNICALL *GetStringCritical)( JNIEnv *pJniEnv, jstring str,
  jboolean *isCopy );
 void (JNICALL *ReleaseStringCritical)( JNIEnv *pJniEnv, jstring str,
  CONST jchar *chars );

 jweak (JNICALL *NewWeakGlobalRef)( JNIEnv *pJniEnv, jobject obj );
 void (JNICALL *DeleteWeakGlobalRef)( JNIEnv *pJniEnv, jweak weakref );

 jboolean (JNICALL *ExceptionCheck)( JNIEnv *pJniEnv );

 jobject (JNICALL *NewDirectByteBuffer)( JNIEnv *pJniEnv, void *address,
  jlong capacity );
 void *(JNICALL *GetDirectBufferAddress)( JNIEnv *pJniEnv, jobject buf );
 jlong (JNICALL *GetDirectBufferCapacity)( JNIEnv *pJniEnv, jobject buf );

 jobjectRefType (JNICALL *GetObjectRefType)( JNIEnv *pJniEnv, jobject obj );
#endif
};

struct JNIEnv_ {
 CONST struct JNINativeInterface_ *functions;
};

struct JNIInvokeInterface_
{
 void (JNICALL *reserved0)( void );
#ifndef JCGO_NOJNI
 void (JNICALL *reserved1)( void );
 void (JNICALL *reserved2)( void );
 jint (JNICALL *DestroyJavaVM)( JavaVM *vm );
 jint (JNICALL *AttachCurrentThread)( JavaVM *vm, void **penv, void *args );
 jint (JNICALL *DetachCurrentThread)( JavaVM *vm );
 jint (JNICALL *GetEnv)( JavaVM *vm, void **penv, jint version );
 jint (JNICALL *AttachCurrentThreadAsDaemon)( JavaVM *vm, void **penv,
  void *args );
#endif
};

struct JavaVM_ {
 CONST struct JNIInvokeInterface_ *functions;
};

struct JavaVMAttachArgs
{
 jint version;
 char *name;
 jobject group;
};

typedef struct JavaVMAttachArgs JavaVMAttachArgs;

struct JavaVMOption
{
 char *optionString;
 void *extraInfo;
};

typedef struct JavaVMOption JavaVMOption;

struct JavaVMInitArgs
{
 jint version;
 jint nOptions;
 JavaVMOption *options;
 jboolean ignoreUnrecognized;
};

typedef struct JavaVMInitArgs JavaVMInitArgs;

#ifndef JCGO_BUILDING_JNIVM

JNIIMPORT jint JNICALL JNI_GetDefaultJavaVMInitArgs( void *args );
JNIIMPORT jint JNICALL JNI_CreateJavaVM( JavaVM **pvm, void **penv,
 void *args );
JNIIMPORT jint JNICALL JNI_GetCreatedJavaVMs( JavaVM **vmBuf, jsize bufLen,
 jsize *nVMs );

/* JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *reserved ); */
/* JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *reserved ); */

#endif /* ! JCGO_BUILDING_JNIVM */

#ifdef __cplusplus
}
#endif

#endif /* JCGO_VER */

#endif

/*
 * @(#) $(JCGO)/reflgen/trjnic.c --
 * native back-end for "TraceJni" utility (part of JCGO).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_UNIX, JCGO_WIN32.
 */

/* Intercepted JNI calls: ThrowNew, Get[Static]MethodID, Get[Static]FieldID */

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

#ifndef _JNI_H
#include "jni.h"
#endif

#ifndef _STDLIB_H
#include <stdlib.h>
#endif

#ifndef _STDIO_H
#include <stdio.h>
#endif

#ifdef JCGO_WIN32

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL VirtualProtect(void *, DWORD, DWORD, DWORD *); */
#endif

#else

#ifdef JCGO_UNIX

#ifndef _SYS_MMAN_H
#include <sys/mman.h>
/* int mprotect(void *, size_t, int); */
#endif

#endif

#endif

#ifndef CONST
#define CONST const
#endif

#ifndef STATIC
#define STATIC static
#endif

#ifndef STATICDATA
#define STATICDATA /* empty */
#endif

struct traceJniData_s
{
 jclass procClass;
 jmethodID procMethod;
 jint (JNICALL *origThrowNew)(JNIEnv *, jclass, CONST char *);
 jmethodID (JNICALL *origGetMethodID)(JNIEnv *, jclass, CONST char *,
  CONST char *);
 jmethodID (JNICALL *origGetStaticMethodID)(JNIEnv *, jclass, CONST char *,
  CONST char *);
 jfieldID (JNICALL *origGetFieldID)(JNIEnv *, jclass, CONST char *,
  CONST char *);
 jfieldID (JNICALL *origGetStaticFieldID)(JNIEnv *, jclass, CONST char *,
  CONST char *);
};

STATICDATA struct traceJniData_s traceJniData;

STATIC void interceptProcess( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 jstring jname;
 jstring jsig;
 if ((jname = (*pJniEnv)->NewStringUTF(pJniEnv, name)) != (jstring)0)
 {
  if ((jsig = (*pJniEnv)->NewStringUTF(pJniEnv, sig)) != (jstring)0)
  {
   (*pJniEnv)->CallStaticVoidMethod(pJniEnv, traceJniData.procClass,
    traceJniData.procMethod, clazz, jname, jsig);
   (*pJniEnv)->DeleteLocalRef(pJniEnv, (jobject)jsig);
  }
  (*pJniEnv)->DeleteLocalRef(pJniEnv, (jobject)jname);
 }
}

STATIC jint JNICALL
interceptThrowNew( JNIEnv *pJniEnv, jclass clazz, CONST char *msg )
{
 interceptProcess(pJniEnv, clazz, "<init>", "(Ljava/lang/String;)V");
 return (*traceJniData.origThrowNew)(pJniEnv, clazz, msg);
}

STATIC jmethodID JNICALL
interceptGetMethodID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 interceptProcess(pJniEnv, clazz, name, sig);
 return (*traceJniData.origGetMethodID)(pJniEnv, clazz, name, sig);
}

STATIC jmethodID JNICALL
interceptGetStaticMethodID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 interceptProcess(pJniEnv, clazz, name, sig);
 return (*traceJniData.origGetStaticMethodID)(pJniEnv, clazz, name, sig);
}

STATIC jfieldID JNICALL
interceptGetFieldID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 interceptProcess(pJniEnv, clazz, name, sig);
 return (*traceJniData.origGetFieldID)(pJniEnv, clazz, name, sig);
}

STATIC jfieldID JNICALL
interceptGetStaticFieldID( JNIEnv *pJniEnv, jclass clazz, CONST char *name,
 CONST char *sig )
{
 interceptProcess(pJniEnv, clazz, name, sig);
 return (*traceJniData.origGetStaticFieldID)(pJniEnv, clazz, name, sig);
}

JNIEXPORT void JNICALL
Java_com_ivmaisoft_jcgorefl_TraceJni_initIntercept( JNIEnv *pJniEnv,
 jclass This )
{
#ifdef JCGO_WIN32
 DWORD oldProt;
 VirtualProtect((void *)(*pJniEnv), sizeof(JNIEnv), PAGE_READWRITE, &oldProt);
#else
#ifdef JCGO_UNIX
 mprotect((void *)(*pJniEnv), sizeof(JNIEnv), PROT_READ | PROT_WRITE);
#endif
#endif
 if ((traceJniData.procMethod = (*pJniEnv)->GetStaticMethodID(pJniEnv,
     This, "processIntercept",
     "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)V")) !=
     (jmethodID)0 && (traceJniData.procClass =
     (jclass)(*pJniEnv)->NewGlobalRef(pJniEnv, This)) != (void *)0)
 {
  traceJniData.origThrowNew = (*pJniEnv)->ThrowNew;
  *(jint (JNICALL **)(JNIEnv *, jclass,
   CONST char *))&(*pJniEnv)->ThrowNew = interceptThrowNew;
  traceJniData.origGetMethodID = (*pJniEnv)->GetMethodID;
  *(jmethodID (JNICALL **)(JNIEnv *, jclass, CONST char *,
   CONST char *))&(*pJniEnv)->GetMethodID = interceptGetMethodID;
  traceJniData.origGetStaticMethodID = (*pJniEnv)->GetStaticMethodID;
  *(jmethodID (JNICALL **)(JNIEnv *, jclass, CONST char *,
   CONST char *))&(*pJniEnv)->GetStaticMethodID = interceptGetStaticMethodID;
  traceJniData.origGetFieldID = (*pJniEnv)->GetFieldID;
  *(jfieldID (JNICALL **)(JNIEnv *, jclass, CONST char *,
   CONST char *))&(*pJniEnv)->GetFieldID = interceptGetFieldID;
  traceJniData.origGetStaticFieldID = (*pJniEnv)->GetStaticFieldID;
  *(jfieldID (JNICALL **)(JNIEnv *, jclass, CONST char *,
   CONST char *))&(*pJniEnv)->GetStaticFieldID = interceptGetStaticFieldID;
 }
}

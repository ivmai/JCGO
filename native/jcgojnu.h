/*
 * @(#) $(JCGO)/native/jcgojnu.h --
 * a part of the JCGO native layer library (native layer API definition).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file may be included from a third-party software.
 */

/*
 * Used control macros: JCGO_INTFIT, JCGO_JNIUSCORE, JCGO_SYSDUALW,
 * JCGO_SYSWCHAR, JCGO_USELONG, JCGO_WMAIN.
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
#define JCGO_BUILDING_NATIVE
#include "jcgojni.h"
#endif

#ifdef JCGO_VER

#ifndef _STDLIB_H
#include <stdlib.h>
#endif

#ifndef NULL
#define NULL ((void *)0)
#endif

#ifndef JCGO_UNUSED_VAR
#define JCGO_UNUSED_VAR(var) (void)var
#endif

#ifdef JCGO_JNIUSCORE
#define JCGO_JNI_EXPF(jnitype, func) JNIEXPORT jnitype JNICALL _##func
#define jcgo_JnuGetByteArrayElemsRegion _jcgo_JnuGetByteArrayElemsRegion
#define jcgo_JnuGetIntArrayElement _jcgo_JnuGetIntArrayElement
#define jcgo_JnuNewStringPlatform _jcgo_JnuNewStringPlatform
#define jcgo_JnuNewStringWide _jcgo_JnuNewStringWide
#define jcgo_JnuReleaseByteArrayElemsRegion _jcgo_JnuReleaseByteArrayElemsRegion
#define jcgo_JnuSetIntArrayElement _jcgo_JnuSetIntArrayElement
#define jcgo_JnuSetLongArrayElement _jcgo_JnuSetLongArrayElement
#define jcgo_JnuStringSizeOfPlatform _jcgo_JnuStringSizeOfPlatform
#define jcgo_JnuStringSizeOfWide _jcgo_JnuStringSizeOfWide
#define jcgo_JnuStringToPlatformChars _jcgo_JnuStringToPlatformChars
#define jcgo_JnuStringToWideChars _jcgo_JnuStringToWideChars
#else
#define JCGO_JNI_EXPF(jnitype, func) JNIEXPORT jnitype JNICALL func
#endif

#ifdef JCGO_SYSWCHAR

#define JCGO_JNUTCHAR_T wchar_t

#ifdef JCGO_SYSDUALW
#define JCGO_JNUTCHAR_R(rtype, cexpr) ((rtype *)((volatile void *)(cexpr)))
#define JCGO_JNUTCHAR_C(tstr) ((char *)(tstr))
#define JCGO_JNUTCHAR_E(cexpr, wexpr) (jcgo_sysWCharOn ? (wexpr) : (cexpr))
#else
/* #define JCGO_JNUTCHAR_R(rtype, cexpr) */
/* #define JCGO_JNUTCHAR_C(tstr) */
#define JCGO_JNUTCHAR_E(cexpr, wexpr) (wexpr)
#endif

#else /* JCGO_SYSWCHAR */

#define JCGO_JNUTCHAR_T char

#define JCGO_JNUTCHAR_R(rtype, cexpr) (cexpr)
#define JCGO_JNUTCHAR_C(tstr) (tstr)
#define JCGO_JNUTCHAR_E(cexpr, wexpr) (cexpr)

#endif /* ! JCGO_SYSWCHAR */

#define JCGO_JNU_TSTRINGTOCHARS(pJniEnv, str, tbuf) JCGO_JNUTCHAR_E(jcgo_JnuStringToPlatformChars(pJniEnv, str, JCGO_JNUTCHAR_C(tbuf), sizeof(tbuf)), jcgo_JnuStringToWideChars(pJniEnv, str, tbuf, sizeof(tbuf)))
#define JCGO_JNU_TNEWSTRING(pJniEnv, tstr) JCGO_JNUTCHAR_E(jcgo_JnuNewStringPlatform(pJniEnv, JCGO_JNUTCHAR_C(tstr)), jcgo_JnuNewStringWide(pJniEnv, tstr))

#ifndef JCGO_BUILDING_JNU

#ifdef __cplusplus
extern "C"
{
#endif

JNIIMPORT jbyte *JNICALL jcgo_JnuGetByteArrayElemsRegion( JNIEnv *pJniEnv,
 jbyteArray arr, jint offset, jint len );
JNIIMPORT void JNICALL jcgo_JnuReleaseByteArrayElemsRegion( JNIEnv *pJniEnv,
 jbyteArray arr, jbyte *bytes, jint offset );
JNIIMPORT jint JNICALL jcgo_JnuGetIntArrayElement( JNIEnv *pJniEnv,
 jintArray arr, jint index );
JNIIMPORT void JNICALL jcgo_JnuSetIntArrayElement( JNIEnv *pJniEnv,
 jintArray arr, jint index, jint value );
JNIIMPORT void JNICALL jcgo_JnuSetLongArrayElement( JNIEnv *pJniEnv,
 jlongArray arr, jint index, jlong value );

JNIIMPORT unsigned JNICALL jcgo_JnuStringSizeOfPlatform( JNIEnv *pJniEnv,
 jstring str );
JNIIMPORT int JNICALL jcgo_JnuStringToPlatformChars( JNIEnv *pJniEnv,
 jstring str, char *cbuf, unsigned size );
JNIIMPORT jstring JNICALL jcgo_JnuNewStringPlatform( JNIEnv *pJniEnv,
 CONST char *cstr );

#ifdef JCGO_SYSWCHAR

JNIIMPORT unsigned JNICALL jcgo_JnuStringSizeOfWide( JNIEnv *pJniEnv,
 jstring str );
JNIIMPORT int JNICALL jcgo_JnuStringToWideChars( JNIEnv *pJniEnv, jstring str,
 wchar_t *wbuf, unsigned size );
JNIIMPORT jstring JNICALL jcgo_JnuNewStringWide( JNIEnv *pJniEnv,
 CONST wchar_t *wstr );

#ifdef JCGO_SYSDUALW
#ifndef JCGO_BUILDING_FILEAPI
#ifndef JCGO_USEWCHAR_VARIMPORT
#ifdef JNUBIGEXPORT
#define JCGO_USEWCHAR_VARIMPORT extern
#else
#define JCGO_USEWCHAR_VARIMPORT JNIIMPORT
#endif
#endif
JCGO_USEWCHAR_VARIMPORT int jcgo_sysWCharOn;
#endif
#endif

#else /* JCGO_SYSWCHAR */

#ifdef JCGO_WMAIN

JNIIMPORT jstring JNICALL jcgo_JnuNewStringWide( JNIEnv *pJniEnv,
 CONST wchar_t *wstr );

#endif /* JCGO_WMAIN */

#endif /* ! JCGO_SYSWCHAR */

#ifdef __cplusplus
}
#endif

#endif /* ! JCGO_BUILDING_JNU */

#endif

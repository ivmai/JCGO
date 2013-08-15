/*
 * @(#) $(JCGO)/native/jcgoprop.c --
 * a part of the JCGO native layer library (get system props impl).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_SYSDUALW, JCGO_SYSWCHAR, JCGO_WINEXINFO.
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

#ifndef JCGO_BUILDING_JNU
#include "jcgojnu.h"
#endif

#ifdef JCGO_VER

#include "jcgoprop.h"

#ifdef JCGO_SYSDUALW
#ifdef JCGO_SYSWCHAR
#ifdef JCGO_WINEXINFO
#ifdef JCGO_BUILDING_JNU
#ifndef JCGO_USEWCHAR_VAREXPORT
#ifdef JNUBIGEXPORT
#define JCGO_USEWCHAR_VAREXPORT JNUBIGEXPORT
#else
#define JCGO_USEWCHAR_VAREXPORT JNIEXPORT
#endif
#endif
JCGO_USEWCHAR_VAREXPORT int jcgo_sysWCharOn;
#endif
#endif
#endif
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_isCpuUnicodeEndianLittle0
JCGO_JNI_EXPF(jint,
Java_gnu_classpath_VMSystemProperties_isCpuUnicodeEndianLittle0)(
 JNIEnv *pJniEnv, jclass This, jint isUnicode )
{
 int value = 1;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (int)isUnicode ? (jint)JCGO_OSUNICODE_ISLITTLE :
         (jint)(*(volatile char *)&value);
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getArchDataModel0
JCGO_JNI_EXPF(jint,
Java_gnu_classpath_VMSystemProperties_getArchDataModel0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return (jint)(sizeof(void *) * 8);
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getOsArch0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getOsArch0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_OSARCH_STR);
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getOsNameVersion0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getOsNameVersion0)( JNIEnv *pJniEnv,
 jclass This, jint isVersion )
{
 JCGO_OSNAME_T data;
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, (int)isVersion ?
         JCGO_OSNAME_GETRELEASE(&data) : JCGO_OSNAME_GETNAME(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0
JCGO_JNI_EXPF(jint,
Java_gnu_classpath_VMSystemProperties_getOsVerMajorMinor0)( JNIEnv *pJniEnv,
 jclass This, jint isMinor )
{
 JCGO_OSVER_T data;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_OSVER_GET(&data);
 return (jint)((int)isMinor ? JCGO_OSVER_MINOR(&data) :
         JCGO_OSVER_MAJOR(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getCTypeLocale0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getCTypeLocale0)( JNIEnv *pJniEnv,
 jclass This )
{
#ifdef LC_CTYPE
 char *cstr = setlocale(LC_CTYPE, NULL);
 if (cstr != NULL)
  return jcgo_JnuNewStringPlatform(pJniEnv, cstr);
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getUserLanguage0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getUserLanguage0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_USERLANG_T data;
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_USERLANG_GETABBR(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getUserCountry0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getUserCountry0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_USERCNTRY_T data;
 JCGO_USERCNTRY_PREPARESTMT(&data);
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_USERCNTRY_GETABBR(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getUserVariant0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getUserVariant0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getFileConsoleEncoding0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getFileConsoleEncoding0)(
 JNIEnv *pJniEnv, jclass This, jint isConsole )
{
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(isConsole);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getFileConsoleCodePage0
JCGO_JNI_EXPF(jint,
Java_gnu_classpath_VMSystemProperties_getFileConsoleCodePage0)(
 JNIEnv *pJniEnv, jclass This, jint isConsole )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 if ((int)isConsole)
  return (jint)JCGO_OSCONSOLE_CODEPAGE;
 return (jint)JCGO_OSFILE_CODEPAGE;
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getUserName0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getUserName0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_USERINFO_T data;
#ifdef JCGO_WINUSERNAME_BUFSIZE
 DWORD bufSize = JCGO_WINUSERNAME_BUFSIZE;
 JCGO_JNUTCHAR_T tbuf[JCGO_WINUSERNAME_BUFSIZE];
 if (JCGO_JNUTCHAR_E(GetUserNameA(JCGO_JNUTCHAR_C(tbuf), &bufSize),
     GetUserNameW(tbuf, &bufSize)))
  return JCGO_JNU_TNEWSTRING(pJniEnv, tbuf);
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_USERINFO_GETNAME(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getUserHome0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getUserHome0)( JNIEnv *pJniEnv,
 jclass This )
{
 JCGO_USERINFO_T data;
#ifdef JCGO_WINEXINFO
#ifdef GetUserHomeFolder
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNUTCHAR_E(GetUserHomeFolderA((DWORD)JCGO_PATH_MAXSIZE,
     JCGO_JNUTCHAR_C(tbuf)), GetUserHomeFolderW((DWORD)JCGO_PATH_MAXSIZE,
     tbuf)) - (DWORD)1 < (DWORD)JCGO_PATH_MAXSIZE)
  return JCGO_JNU_TNEWSTRING(pJniEnv, tbuf);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_USERINFO_GETHOME(&data));
}
#endif

#ifndef NOJAVA_gnu_classpath_VMSystemProperties_getJavaIoTmpdir0
JCGO_JNI_EXPF(jstring,
Java_gnu_classpath_VMSystemProperties_getJavaIoTmpdir0)( JNIEnv *pJniEnv,
 jclass This )
{
#ifdef JCGO_WINEXINFO
#ifdef GetTempPath
 JCGO_JNUTCHAR_T tbuf[JCGO_PATH_MAXSIZE];
 if (JCGO_JNUTCHAR_E(GetTempPathA(JCGO_PATH_MAXSIZE, JCGO_JNUTCHAR_C(tbuf)),
     GetTempPathW(JCGO_PATH_MAXSIZE, tbuf)) - (DWORD)1 <
     (DWORD)JCGO_PATH_MAXSIZE)
  return JCGO_JNU_TNEWSTRING(pJniEnv, tbuf);
#endif
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, JCGO_TMPDIR_PATH);
}
#endif

#endif

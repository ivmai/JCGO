/*
 * @(#) $(JCGO)/jnidload/jnidload.c --
 * lazy trampoline for "JNI_OnLoad" or "JNI_OnUnload" function (part of JCGO).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_UNIX, JCGO_WIN32, JNIDLOAD_ISUNLOAD,
 * JNIDLOAD_TRYMSNAME, UNDER_CE.
 * Macros for tuning: JNIDLOAD_EXPNAME, JNIDLOAD_QLIBNAME.
 */

/*
 * Comment: this source code is for creating JCGO-specific static trampolines
 * with distinctive names for multiple dynamically-loaded "JNI_OnLoad" and
 * "JNI_OnUnload" functions (for the case when these functions are residing in
 * multiple dynamic libraries but used together).
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

#ifndef _JNI_H
#include "jni.h"
#endif

#ifndef _STDLIB_H
#include <stdlib.h>
#endif

#ifdef JCGO_WIN32

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
/* BOOL FreeLibrary(HMODULE); */
/* FARPROC GetProcAddress(HMODULE, LPCSTR); */
/* HINSTANCE LoadLibrary(LPCTSTR); */
#endif

#else /* JCGO_WIN32 */

#ifdef JCGO_UNIX

#ifndef _DLFCN_H
#include <dlfcn.h>
/* int dlclose(void *); */
/* void *dlopen(const char *, int); */
/* void *dlsym(void *, const char *); */
#endif

#endif /* JCGO_UNIX */

#endif /* ! JCGO_WIN32 */

#ifdef JNIDLOAD_ISUNLOAD
/* JNIEXPORT void JNICALL JNI_OnUnload( JavaVM *vm, void *reserved ); */
#define JNIDLOAD_RETTYPE void
#ifdef UNDER_CE
#define JNIDLOAD_QIMPMSNAME L"_JNI_OnUnload@8"
#define JNIDLOAD_QIMPNAME L"JNI_OnUnload"
#else
#define JNIDLOAD_QIMPMSNAME "_JNI_OnUnload@8"
#define JNIDLOAD_QIMPNAME "JNI_OnUnload"
#endif
#else
/* JNIEXPORT jint JNICALL JNI_OnLoad( JavaVM *vm, void *reserved ); */
#define JNIDLOAD_RETTYPE jint
#ifdef UNDER_CE
#define JNIDLOAD_QIMPMSNAME L"_JNI_OnLoad@8"
#define JNIDLOAD_QIMPNAME L"JNI_OnLoad"
#else
#define JNIDLOAD_QIMPMSNAME "_JNI_OnLoad@8"
#define JNIDLOAD_QIMPNAME "JNI_OnLoad"
#endif
#endif

JNIEXPORT JNIDLOAD_RETTYPE JNICALL
JNIDLOAD_EXPNAME( JavaVM *vm, void *reserved )
{
#ifndef JNIDLOAD_ISUNLOAD
 JNIDLOAD_RETTYPE res = -1;
#endif
#ifdef JCGO_WIN32
 HINSTANCE handle = LoadLibrary(JNIDLOAD_QLIBNAME);
 FARPROC sym;
 if (handle)
 {
  if (
#ifdef JNIDLOAD_TRYMSNAME
      (sym = GetProcAddress(handle, JNIDLOAD_QIMPMSNAME)) != 0 ||
#endif
      (sym = GetProcAddress(handle, JNIDLOAD_QIMPNAME)) != 0)
  {
#ifndef JNIDLOAD_ISUNLOAD
   res =
#endif
    ((JNIDLOAD_RETTYPE (JNICALL *)(JavaVM *, void *))sym)(vm, reserved);
  }
  (void)FreeLibrary(handle);
 }
#else
#ifdef JCGO_UNIX
 void *handle = dlopen(JNIDLOAD_QLIBNAME, RTLD_LAZY);
 void *sym;
 if (handle != NULL)
 {
  if ((sym = dlsym(handle, JNIDLOAD_QIMPNAME)) != NULL)
  {
#ifndef JNIDLOAD_ISUNLOAD
   res =
#endif
    ((JNIDLOAD_RETTYPE (JNICALL *)(JavaVM *, void *))sym)(vm, reserved);
  }
  (void)dlclose(handle);
 }
#endif
#endif
#ifndef JNIDLOAD_ISUNLOAD
 return res;
#endif
}

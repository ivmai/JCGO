/*
 * @(#) $(JCGO)/minihdr/win/windef.h--
 * a part of the minimalist "Win32" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2014 Ivan Maidanski <ivmai@mail.ru>
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

#ifndef _WINDEF_H
#define _WINDEF_H

#ifndef FAR
#define FAR /* empty */
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef BYTE
typedef unsigned char BYTE;
#endif

#ifndef WORD
typedef unsigned short WORD;
#endif

#ifndef DWORD
#ifdef _WIN64
typedef unsigned DWORD; /* in case of LP64 */
#else
typedef unsigned long DWORD;
#endif
#endif

#ifndef MAX_PATH
#define MAX_PATH 260
#endif

#ifndef FALSE
#define FALSE 0
#endif

#ifndef TRUE
#define TRUE 1
#endif

#ifndef LONG
#ifdef _WIN64
typedef int LONG; /* in case of LP64 */
typedef unsigned ULONG;
#else
typedef long LONG;
typedef unsigned long ULONG;
#endif
#endif

#ifdef __cplusplus
}
#endif

#ifndef _WINNT_H
#include <winnt.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef WINAPI
#define WINAPI __stdcall
#endif

#ifndef BOOL
typedef int BOOL;
#endif

#ifndef HINSTANCE
typedef struct HINSTANCE__ { int unused; } FAR *HINSTANCE;
#endif

typedef BYTE FAR *LPBYTE;
typedef LONG FAR *LPLONG;
typedef DWORD FAR *LPDWORD;
typedef void FAR *LPVOID;
typedef const void FAR *LPCVOID;

typedef unsigned UINT;

/* from basetsd.h */
#ifndef DWORD_PTR
#ifdef _WIN64
typedef unsigned __int64 DWORD_PTR;
#else
typedef unsigned long DWORD_PTR;
#endif
#endif

#ifndef UINT_PTR
#ifdef _WIN64
typedef DWORD_PTR UINT_PTR;
#else
typedef unsigned UINT_PTR;
#endif
#endif

#ifdef __cplusplus
}
#endif

#endif

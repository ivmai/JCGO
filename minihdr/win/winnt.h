/*
 * @(#) $(JCGO)/minihdr/win/winnt.h --
 * a part of the minimalist "Win32" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef _WINNT_H
#define _WINNT_H

#ifdef _WINDEF_H

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef __int64
#define __int64 long long /* or __int64 */
#endif

#ifndef DUPLICATE_SAME_ACCESS
#define DUPLICATE_SAME_ACCESS 0x2
#endif

#ifndef HANDLE
typedef void FAR *HANDLE;
#endif

typedef HANDLE FAR *LPHANDLE;

typedef char FAR *LPSTR;
typedef const char FAR *LPCSTR;

#ifndef WCHAR
typedef unsigned short WCHAR;
#endif

typedef WCHAR FAR *LPWSTR;
typedef const WCHAR FAR *LPCWSTR;

typedef DWORD LCID;
typedef WORD LANGID;

#ifndef LONGLONG
typedef __int64 LONGLONG;
#endif

typedef union _LARGE_INTEGER
{
 struct
 {
  DWORD LowPart; /* unused */
  LONG HighPart; /* unused */
 };
 struct
 {
  DWORD LowPart; /* unused */
  LONG  HighPart; /* unused */
 } u; /* unused */
 LONGLONG QuadPart;
} LARGE_INTEGER, FAR *PLARGE_INTEGER;

#ifndef PRIMARYLANGID
#define PRIMARYLANGID(langid) ((WORD)(langid) & 0x3ff)
#endif

#ifndef LANGIDFROMLCID
#define LANGIDFROMLCID(lcid) ((WORD)(lcid))
#endif

#define GENERIC_WRITE ((DWORD)0x40000000L)
#define GENERIC_READ ((DWORD)0x80000000L)

#define FILE_SHARE_READ 0x1
#define FILE_SHARE_WRITE 0x2

#define FILE_ATTRIBUTE_READONLY 0x1
#define FILE_ATTRIBUTE_HIDDEN 0x2
#define FILE_ATTRIBUTE_DIRECTORY 0x10
#define FILE_ATTRIBUTE_NORMAL 0x80

typedef struct _OSVERSIONINFOA
{
 DWORD dwOSVersionInfoSize;
 DWORD dwMajorVersion;
 DWORD dwMinorVersion;
 DWORD dwBuildNumber; /* unused */
 DWORD dwPlatformId; /* unused */
 char szCSDVersion[128]; /* unused */
} OSVERSIONINFOA, FAR *LPOSVERSIONINFOA;

typedef struct _OSVERSIONINFOW
{
 DWORD dwOSVersionInfoSize;
 DWORD dwMajorVersion;
 DWORD dwMinorVersion;
 DWORD dwBuildNumber; /* unused */
 DWORD dwPlatformId; /* unused */
 WCHAR szCSDVersion[128]; /* unused */
} OSVERSIONINFOW, FAR *LPOSVERSIONINFOW;

#ifdef UNICODE
typedef OSVERSIONINFOW OSVERSIONINFO;
typedef LPOSVERSIONINFOW LPOSVERSIONINFO;
#else
typedef OSVERSIONINFOA OSVERSIONINFO;
typedef LPOSVERSIONINFOA LPOSVERSIONINFO;
#endif

#ifndef _CONTEXT_DEFINED
#define _CONTEXT_DEFINED /* CPU-specific */
#define CONTEXT_CONTROL (0x10000 | 0x1)
#define CONTEXT_INTEGER (0x10000 | 0x2)
typedef struct _FLOATING_SAVE_AREA
{
 DWORD ControlWord; /* unused */
 DWORD StatusWord; /* unused */
 DWORD TagWord; /* unused */
 DWORD ErrorOffset; /* unused */
 DWORD ErrorSelector; /* unused */
 DWORD DataOffset; /* unused */
 DWORD DataSelector; /* unused */
 BYTE RegisterArea[80]; /* unused */
 DWORD Cr0NpxState; /* unused */
} FLOATING_SAVE_AREA;
typedef struct _CONTEXT
{
 DWORD ContextFlags;
 DWORD Dr0; /* unused */
 DWORD Dr1; /* unused */
 DWORD Dr2; /* unused */
 DWORD Dr3; /* unused */
 DWORD Dr6; /* unused */
 DWORD Dr7; /* unused */
 FLOATING_SAVE_AREA FloatSave; /* unused */
 DWORD SegGs; /* unused */
 DWORD SegFs; /* unused */
 DWORD SegEs; /* unused */
 DWORD SegDs; /* unused */
 DWORD Edi;
 DWORD Esi;
 DWORD Ebx;
 DWORD Edx;
 DWORD Ecx;
 DWORD Eax;
 DWORD Ebp;
 DWORD Eip; /* unused */
 DWORD SegCs; /* unused */
 DWORD EFlags; /* unused */
 DWORD Esp;
 DWORD SegSs; /* unused */
 BYTE ExtendedRegisters[512]; /* unused */
} CONTEXT;
#endif

#ifdef __cplusplus
}
#endif

#endif

#endif

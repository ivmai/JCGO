/*
 * @(#) $(JCGO)/minihdr/common/stddef.h --
 * a part of the minimalist "libc" headers for JCGO (core definitions).
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

#ifndef _STDDEF_H
#define _STDDEF_H

#ifdef _WITH_CONFIG
#include "config.h"
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef __int64
#define __int64 long long /* or __int64 */
#endif

#ifndef _EXPFUNC
#define _EXPFUNC /* empty */
#endif

#ifndef _EXPFUNC_BUILTIN
#define _EXPFUNC_BUILTIN _EXPFUNC
#endif

#ifndef _RTLENTRY
#define _RTLENTRY /* empty */
#endif

#ifndef _RTLENTRY_BUILTIN
#define _RTLENTRY_BUILTIN _RTLENTRY
#endif

#ifndef _USERENTRY
#define _USERENTRY /* empty */
#endif

#ifndef _ATTRIBUTE_NORETURN
#define _ATTRIBUTE_NORETURN /* __declspec(noreturn) */
#endif

#ifndef _ATTRIBUTE_NONNULL
#define _ATTRIBUTE_NONNULL(argnum) /* __attribute__((__nonnull__(argnum))) */
#endif

#ifndef _ATTRIBUTE_PURE
#define _ATTRIBUTE_PURE /* __attribute__((__pure__)) */
#endif

#ifndef NULL
#ifdef __cplusplus
#define NULL 0
#else
#define NULL ((void *)0)
#endif
#endif

#ifndef __SSIZE_TYPE__
#define __SSIZE_TYPE__ int /* or long, or __int64 */
#endif

#ifndef _SIZE_T_DEFINED
#define _SIZE_T_DEFINED
typedef unsigned __SSIZE_TYPE__ size_t;
#endif

#ifndef _PTRDIFF_T_DEFINED
#define _PTRDIFF_T_DEFINED
#ifdef _LLP64
typedef __int64 ptrdiff_t;
#else
typedef long ptrdiff_t; /* or int */
#endif
#endif

#ifndef _WCHAR_T_DEFINED
#define _WCHAR_T_DEFINED
#ifndef __WCHAR_TYPE__
#define __WCHAR_TYPE__ unsigned short /* or unsigned, or int, or unsigned char */
#endif
typedef __WCHAR_TYPE__ wchar_t;
#endif

/* OS/2-specific */
#ifndef _threadid
_EXPFUNC unsigned *_RTLENTRY __threadid(void);
#define _threadid __threadid()
#endif

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/dos/dir.h --
 * a part of the minimalist "libc" headers for JCGO (PC-specific).
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

#ifndef _DIR_H
#define _DIR_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef MAXPATH
#define MAXPATH 260 /* or 80 */
#endif

#ifndef FA_HIDDEN
#define FA_HIDDEN 0x2
#define FA_LABEL 0x8
#endif

#ifndef _FFBLK_DEFINED
#define _FFBLK_DEFINED
#ifndef _FFBLK_NAME_LEN
#define _FFBLK_NAME_LEN MAXPATH /* or 13 */
#endif
struct ffblk
{
#ifdef _FFBLK_DOS_LIKE
 char ff_reserved[21]; /* unused */
 unsigned char ff_attrib;
#else
 long ff_reserved; /* unused */
 unsigned long ff_fsize; /* unused */
 unsigned long ff_attrib;
#endif
 unsigned short ff_ftime; /* unused */
 unsigned short ff_fdate; /* unused */
#ifdef _FFBLK_DOS_LIKE
 unsigned long ff_fsize; /* unused */
#endif
 char ff_name[_FFBLK_NAME_LEN];
};
struct _wffblk
{
#ifdef _FFBLK_DOS_LIKE
 char ff_reserved[21]; /* unused */
 unsigned char ff_attrib;
#else
 long ff_reserved; /* unused */
 unsigned long ff_fsize; /* unused */
 unsigned long ff_attrib;
#endif
 unsigned short ff_ftime; /* unused */
 unsigned short ff_fdate; /* unused */
#ifdef _FFBLK_DOS_LIKE
 unsigned long ff_fsize; /* unused */
#endif
 wchar_t ff_name[_FFBLK_NAME_LEN];
};
#endif

_EXPFUNC int _RTLENTRY findfirst(const char *, struct ffblk *, int)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY findnext(struct ffblk *) _ATTRIBUTE_NONNULL(1);

#ifdef _DIR_HAS_WFINDFIRST
_EXPFUNC int _RTLENTRY _wfindfirst(const wchar_t *, struct _wffblk *, int)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY _wfindnext(struct _wffblk *) _ATTRIBUTE_NONNULL(1);
#endif

#ifdef __cplusplus
}
#endif

#endif

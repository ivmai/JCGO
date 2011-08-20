/*
 * @(#) $(JCGO)/minihdr/dos/dos.h --
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

#ifndef _DOS_H
#define _DOS_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _A_HIDDEN
#define _A_HIDDEN 0x2
#define _A_VOLID 0x8
#endif

#ifndef _FIND_T_DEFINED
#define _FIND_T_DEFINED
#ifndef _DOS_FIND_NAME_LEN
#define _DOS_FIND_NAME_LEN 256 /* or 13 */
#endif
struct find_t
{
 char reserved[21]; /* unused */
 unsigned char attrib;
 unsigned short wr_time; /* unused */
 unsigned short wr_date; /* unused */
 unsigned long size; /* unused */
 char name[_DOS_FIND_NAME_LEN];
};
struct _wfind_t
{
 char reserved[21]; /* unused */
 unsigned char attrib;
 unsigned short wr_time; /* unused */
 unsigned short wr_date; /* unused */
 unsigned long size; /* unused */
 wchar_t name[_DOS_FIND_NAME_LEN];
};
#endif

_EXPFUNC void _RTLENTRY _dos_setdrive(unsigned, unsigned *)
 _ATTRIBUTE_NONNULL(2);

_EXPFUNC unsigned _RTLENTRY _dos_findfirst(const char *, unsigned,
 struct find_t *) _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(3);
_EXPFUNC unsigned _RTLENTRY _dos_findnext(struct find_t *)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC unsigned _RTLENTRY _wdos_findfirst(const wchar_t *, unsigned,
 struct _wfind_t *) _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(3);
_EXPFUNC unsigned _RTLENTRY _wdos_findnext(struct _wfind_t *)
 _ATTRIBUTE_NONNULL(1);

#ifdef _DOS_HAS_FINDCLOSE
_EXPFUNC unsigned _RTLENTRY _dos_findclose(struct find_t *)
 _ATTRIBUTE_NONNULL(1);
_EXPFUNC unsigned _RTLENTRY _wdos_findclose(struct _wfind_t *)
 _ATTRIBUTE_NONNULL(1);
#endif

#ifdef __cplusplus
}
#endif

#endif

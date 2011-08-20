/*
 * @(#) $(JCGO)/minihdr/common/dirent.h --
 * a part of the minimalist "libc" headers for JCGO.
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

#ifndef _DIRENT_H
#define _DIRENT_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _DIR_DEFINED
#define _DIR_DEFINED
typedef struct
{
 int opaque; /* unused */
} DIR;
typedef struct
{
 int opaque; /* unused */
} _WDIR;
#ifndef wDIR
#define wDIR _WDIR
#endif
#endif

#ifndef _DIRENT_DEFINED
#define _DIRENT_DEFINED
#ifndef _DIRENT_NO_DRECLEN
#define _DIRENT_DRECLEN_TYPE unsigned short /* or int */
#endif
#ifndef _DIRENT_NO_DTYPE
#define _DIRENT_DTYPE_TYPE unsigned char /* or unsigned */
#endif
#ifdef _DIRENT_HAS_DNAMLEN
#define _DIRENT_DNAMLEN_TYPE unsigned char /* or unsigned short, or int */
#endif
#ifndef _DIRENT_NAME_LEN
#define _DIRENT_NAME_LEN 256 /* or 1 */
#endif
struct dirent
{
#ifndef _DIRENT_NO_DINO
 ino_t d_ino; /* unused */
#endif
#ifndef _DIRENT_NO_DOFF
 off_t d_off; /* unused */
#endif
#ifndef _DIRENT_NO_DRECLEN
 _DIRENT_DRECLEN_TYPE d_reclen; /* unused */
#endif
#ifndef _DIRENT_NO_DTYPE
 _DIRENT_DTYPE_TYPE d_type; /* unused */
#endif
#ifdef _DIRENT_HAS_DNAMLEN
 _DIRENT_DNAMLEN_TYPE d_namlen; /* unused */
#endif
 char d_name[_DIRENT_NAME_LEN];
};
struct _wdirent
{
#ifndef _DIRENT_NO_DINO
 ino_t d_ino; /* unused */
#endif
#ifndef _DIRENT_NO_DOFF
 off_t d_off; /* unused */
#endif
#ifndef _DIRENT_NO_DRECLEN
 _DIRENT_DRECLEN_TYPE d_reclen; /* unused */
#endif
#ifndef _DIRENT_NO_DTYPE
 _DIRENT_DTYPE_TYPE d_type; /* unused */
#endif
#ifdef _DIRENT_HAS_DNAMLEN
 _DIRENT_DNAMLEN_TYPE d_namlen; /* unused */
#endif
 wchar_t d_name[_DIRENT_NAME_LEN];
};
#endif

_EXPFUNC DIR *_RTLENTRY opendir(const char *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC struct dirent *_RTLENTRY readdir(DIR *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY closedir(DIR *) _ATTRIBUTE_NONNULL(1);

#ifndef wdirent
#define wdirent _wdirent
#endif

#ifndef wopendir
#define wopendir _wopendir
#endif

#ifndef wreaddir
#define wreaddir _wreaddir
#endif

#ifndef wclosedir
#define wclosedir _wclosedir
#endif

_EXPFUNC _WDIR *_RTLENTRY _wopendir(const wchar_t *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC struct _wdirent *_RTLENTRY _wreaddir(_WDIR *) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wclosedir(_WDIR *) _ATTRIBUTE_NONNULL(1);

#ifdef __cplusplus
}
#endif

#endif

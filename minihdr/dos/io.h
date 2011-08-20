/*
 * @(#) $(JCGO)/minihdr/dos/io.h --
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

#ifndef _IO_H
#define _IO_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifndef _STDINT_H
#include <stdint.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _A_HIDDEN
#define _A_HIDDEN 0x2
#define _A_VOLID 0x8
#endif

#ifndef SEEK_SET
#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#endif

#ifndef F_OK
#define F_OK 0
#endif

#ifndef R_OK
#define R_OK 0x4
#define W_OK 0x2
#define X_OK 0x1
#endif

#ifndef _IO_OSFHANDLE_TYPE
#define _IO_OSFHANDLE_TYPE intptr_t /* or long */
#endif

#ifndef _FINDDATA_T_DEFINED
#define _FINDDATA_T_DEFINED
#ifndef _FINDDATA_FSIZE_TYPE
#define _FINDDATA_FSIZE_TYPE unsigned long /* or off_t */
#endif
#ifndef _FINDDATA_NAME_LEN
#define _FINDDATA_NAME_LEN 260
#endif
struct _finddata_t
{
 unsigned attrib;
 time_t time_create; /* unused */
 time_t time_access; /* unused */
 time_t time_write; /* unused */
 _FINDDATA_FSIZE_TYPE size; /* unused */
 char name[_FINDDATA_NAME_LEN];
};
struct _wfinddata_t
{
 unsigned attrib;
 time_t time_create; /* unused */
 time_t time_access; /* unused */
 time_t time_write; /* unused */
 _FINDDATA_FSIZE_TYPE size; /* unused */
 wchar_t name[_FINDDATA_NAME_LEN];
};
#endif

#ifndef _ERRNO_T_DEFINED
#define _ERRNO_T_DEFINED
typedef int errno_t;
#endif

_EXPFUNC int _RTLENTRY close(int);
_EXPFUNC int _RTLENTRY read(int, void *, unsigned) _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY write(int, const void *, unsigned)
 _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY dup(int);
_EXPFUNC int _RTLENTRY dup2(int, int);
_EXPFUNC long _RTLENTRY lseek(int, long, int);

_EXPFUNC int _RTLENTRY setmode(int, int);
_EXPFUNC int _RTLENTRY chsize(int, long);

_EXPFUNC errno_t _RTLENTRY _chsize_s(int, __OFF64_TYPE__);

#ifndef _IO_NO_GET_OSFHANDLE
_EXPFUNC _IO_OSFHANDLE_TYPE _RTLENTRY _get_osfhandle(int);
#endif

#ifndef _IO_NO_COMMIT
#ifndef fsync
#define fsync _commit
#endif
_EXPFUNC int _RTLENTRY _commit(int);
#endif

_EXPFUNC int _RTLENTRY _locking(int, int, long);

_EXPFUNC int _RTLENTRY _pipe(int *, unsigned, int) _ATTRIBUTE_NONNULL(1);

_EXPFUNC __OFF64_TYPE__ _RTLENTRY _lseeki64(int, __OFF64_TYPE__, int);

_EXPFUNC int _RTLENTRY access(const char *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY chmod(const char *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY open(const char *, int, ...) _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY _findclose(long);

_EXPFUNC long _RTLENTRY _findfirst(const char *, struct _finddata_t *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY _findnext(long, struct _finddata_t *)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY _waccess(const wchar_t *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wchmod(const wchar_t *, int) _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wopen(const wchar_t *, int, ...)
 _ATTRIBUTE_NONNULL(1);

#ifndef _DIR_HAS_WFINDFIRST
_EXPFUNC long _RTLENTRY _wfindfirst(const wchar_t *, struct _wfinddata_t *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);
_EXPFUNC int _RTLENTRY _wfindnext(long, struct _wfinddata_t *)
 _ATTRIBUTE_NONNULL(1);
#endif

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/dos/sys/stat.h --
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

#ifndef _SYS_STAT_H
#define _SYS_STAT_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef S_IFMT
#define S_IFMT 0xf000
#endif

#ifndef S_IFREG
#define S_IFREG 0x8000
#define S_IFDIR 0x4000
#endif

#ifndef S_IRUSR
#define S_IRUSR 0x100
#define S_IWUSR 0x80
#define S_IXUSR 0x40
#endif

#ifndef S_IREAD
#define S_IREAD S_IRUSR
#define S_IWRITE S_IWUSR
#define S_IEXEC S_IXUSR
#endif

#ifndef _STAT_STINO_TYPE
#define _STAT_STINO_TYPE ino_t
#endif

#ifndef _STAT_STNLINK_TYPE
#define _STAT_STNLINK_TYPE short /* or int */
#endif

#ifndef _STAT_STGID_TYPE
#define _STAT_STGID_TYPE short /* or int */
#endif

#ifndef _STAT_STUID_TYPE
#define _STAT_STUID_TYPE _STAT_STGID_TYPE /* or unsigned long */
#endif

/* #define _STAT_RESERVED_SIZE 20 */

#ifndef _STAT_DEFINED
#define _STAT_DEFINED
struct stat
{
 dev_t st_dev;
 _STAT_STINO_TYPE st_ino;
 unsigned short st_mode;
 _STAT_STNLINK_TYPE st_nlink; /* unused */
 _STAT_STUID_TYPE st_uid; /* unused */
 _STAT_STGID_TYPE st_gid; /* unused */
 dev_t st_rdev; /* unused */
 off_t st_size;
 time_t st_atime;
 time_t st_mtime;
 time_t st_ctime; /* unused */
#ifdef _STAT_RESERVED_SIZE
 char _st_reserved[_STAT_RESERVED_SIZE]; /* unused */
#endif
};
#endif

#ifndef _STATI64_DEFINED
#define _STATI64_DEFINED
struct _stati64
{
 dev_t st_dev;
 _STAT_STINO_TYPE st_ino;
 unsigned short st_mode;
 _STAT_STNLINK_TYPE st_nlink; /* unused */
 _STAT_STUID_TYPE st_uid; /* unused */
 _STAT_STGID_TYPE st_gid; /* unused */
 dev_t st_rdev; /* unused */
 __OFF64_TYPE__ st_size;
 time_t st_atime;
 time_t st_mtime;
 time_t st_ctime; /* unused */
#ifdef _STAT_RESERVED_SIZE
 char _st_reserved[_STAT_RESERVED_SIZE]; /* unused */
#endif
};
#ifndef stati64
#define stati64 _stati64
#endif
#endif

_EXPFUNC int _RTLENTRY stat(const char *, struct stat *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY _stati64(const char *, struct _stati64 *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY _wstat(const wchar_t *, struct stat *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY _wstati64(const wchar_t *, struct _stati64 *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/unix/sys/stat.h --
 * a part of the minimalist "libc" headers for JCGO (Unix-specific).
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
#define S_IFREG 0x8000 /* or 0 */
#define S_IFDIR 0x4000 /* or 0x3000 */
#endif

#ifndef S_IFLNK
#define S_IFLNK 0xa000 /* or 0x8000 */
#endif

#ifndef S_IRUSR
#define S_IRUSR 0x100
#define S_IWUSR 0x80
#define S_IXUSR 0x40
#endif

#ifndef S_IRGRP
#define S_IRGRP 0x20
#define S_IWGRP 0x10
#define S_IXGRP 0x8
#endif

#ifndef S_IROTH
#define S_IROTH 0x4
#define S_IWOTH 0x2
#define S_IXOTH 0x1
#endif

#ifndef _STAT_DEFINED
#define _STAT_DEFINED
struct stat /* ABI-specific structure */
{
 dev_t st_dev;
 ino_t st_ino;
 mode_t st_mode;
 nlink_t st_nlink; /* unused */
 uid_t st_uid;
 gid_t st_gid;
 dev_t st_rdev; /* unused */
 off_t st_size;
 time_t st_atime;
 time_t st_mtime;
 time_t st_ctime; /* unused */
};
#endif

#ifndef _STAT64_DEFINED
#define _STAT64_DEFINED
struct stat64 /* ABI-specific structure */
{
 dev_t st_dev;
 ino_t st_ino;
 mode_t st_mode;
 nlink_t st_nlink; /* unused */
 uid_t st_uid;
 gid_t st_gid;
 dev_t st_rdev; /* unused */
 off64_t st_size;
 time_t st_atime;
 time_t st_mtime;
 time_t st_ctime; /* unused */
};
#endif

#ifndef _STAT_ARG_MODET
#define _STAT_ARG_MODET mode_t /* or int */
#endif

_EXPFUNC int _RTLENTRY stat(const char *, struct stat *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY lstat(const char *, struct stat *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

#ifndef _SYS_STAT_NO_CHMOD
_EXPFUNC int _RTLENTRY chmod(const char *, _STAT_ARG_MODET)
 _ATTRIBUTE_NONNULL(1);
_EXPFUNC int _RTLENTRY _wchmod(const wchar_t *, _STAT_ARG_MODET)
 _ATTRIBUTE_NONNULL(1);
#endif

_EXPFUNC int _RTLENTRY mkdir(const char *, _STAT_ARG_MODET)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY stat64(const char *, struct stat64 *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY _wstat(const wchar_t *, struct stat *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

_EXPFUNC int _RTLENTRY _wmkdir(const wchar_t *, _STAT_ARG_MODET)
 _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY _wstat64(const wchar_t *, struct stat64 *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2);

#ifdef __cplusplus
}
#endif

#endif

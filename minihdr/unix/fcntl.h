/*
 * @(#) $(JCGO)/minihdr/unix/fcntl.h --
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

#ifndef _FCNTL_H
#define _FCNTL_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef SEEK_SET
#define SEEK_SET 0
#define SEEK_CUR 1
#define SEEK_END 2
#endif

#ifndef O_RDONLY
#define O_RDONLY 0
#define O_WRONLY 0x1
#define O_RDWR 0x2
#endif

#ifndef O_APPEND
#define O_APPEND 0x8 /* or 0x400, or 0x1000 */
#endif

#ifndef O_CREAT
#define O_CREAT 0x100 /* or 0x40, or 0x200 */
#define O_TRUNC 0x200 /* or 0x400, or 0x800 */
#endif

#ifndef O_EXCL
#define O_EXCL 0x400 /* or 0x80, or 0x200, or 0x800 */
#endif

#ifndef O_SYNC
#define O_SYNC 0x10 /* or 0x80, or 0x1000, or 0x2000, or 0x4000, or 0x8000 */
#endif

#ifndef _FCNTL_NO_DSYNC
#ifndef O_DSYNC
#define O_DSYNC 0x40 /* or 0x40000 */
#endif
#endif

#ifdef _FCNTL_HAS_OTEXT
#ifndef O_TEXT
#define O_BINARY 0x4 /* or 0x100, or 0x10000 */
#define O_TEXT 0x20 /* or 0x8, or 0x10, or 0x20000 */
#endif
#ifndef O_NOINHERIT
#define O_NOINHERIT 0x80 /* or 0x1000, or 0x40000 */
#endif
#endif

#ifndef F_GETFD
#define F_GETFD 1 /* or 2, or 3 */
#define F_SETFD 2 /* or 4, or 5 */
#define F_SETLK 8 /* or 6, or 7, or 13, or 34 */
#define F_SETLKW 9 /* or 7, or 8, or 14, or 35 */
#endif

#ifndef F_SETLK64
#define F_SETLK64 13 /* or 34, or F_SETLK */
#define F_SETLKW64 14 /* or 35, or F_SETLKW */
#endif

#ifndef F_RDLCK
#define F_RDLCK 1 /* or 0 */
#define F_WRLCK 2 /* or 1, or 3 */
#define F_UNLCK 3 /* or 0, or 2, or 8 */
#endif

#ifndef FD_CLOEXEC
#define FD_CLOEXEC 0x1
#endif

#ifndef _FLOCK_DEFINED
#define _FLOCK_DEFINED
#ifndef _FLOCK_RESERVED_SIZE
#ifndef _FLOCK_LSTART_FIRST
#define _FLOCK_RESERVED_SIZE (sizeof(long) * 4) /* or sizeof(short) */
#endif
#endif
struct flock
{
#ifndef _FLOCK_LSTART_FIRST
 short l_type;
 short l_whence;
#endif
 off_t l_start;
 off_t l_len;
#ifdef _FLOCK_HAS_LSYSID
 int l_sysid; /* unused */
#endif
 pid_t l_pid;
#ifdef _FLOCK_LSTART_FIRST
 short l_type;
 short l_whence;
#endif
#ifdef _FLOCK_RESERVED_SIZE
 char l_pad[_FLOCK_RESERVED_SIZE]; /* unused */
#endif
};
struct flock64
{
#ifndef _FLOCK_LSTART_FIRST
 short l_type;
 short l_whence;
#endif
 off64_t l_start;
 off64_t l_len;
#ifdef _FLOCK_HAS_LSYSID
 int l_sysid; /* unused */
#endif
 pid_t l_pid;
#ifdef _FLOCK_LSTART_FIRST
 short l_type;
 short l_whence;
#endif
#ifdef _FLOCK_RESERVED_SIZE
 char l_pad[_FLOCK_RESERVED_SIZE]; /* unused */
#endif
};
#endif

_EXPFUNC int _RTLENTRY fcntl(int, int, ...);

_EXPFUNC int _RTLENTRY open(const char *, int, ...) _ATTRIBUTE_NONNULL(1);

_EXPFUNC int _RTLENTRY _wopen(const wchar_t *, int, ...)
 _ATTRIBUTE_NONNULL(1);

#ifdef __cplusplus
}
#endif

#endif

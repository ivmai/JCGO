/*
 * @(#) $(JCGO)/minihdr/unix/errno.h --
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

#ifndef _ERRNO_H
#define _ERRNO_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef EACCES
#define EACCES 13 /* or 4 */
#define EBADF 9 /* or 6 */
#define EBUSY 16 /* or 7 */
#define EEXIST 17 /* or 10 */
#define EINTR 4 /* or 13 */
#define EINVAL 22 /* or 14 */
#define EMFILE 24 /* or 17 */
#define ENFILE 23 /* or 20 */
#define ENOENT 2 /* or 22 */
#define ENOEXEC 8 /* or 23 */
#define ENOMEM 12 /* or 25 */
#define ENOSPC 28 /* or 26 */
#define ENOTDIR 20 /* or 28 */
#define EPIPE 32 /* or 33 */
#define ERANGE 34 /* or 2 */
#define EROFS 30 /* or 34 */
#define ESPIPE 29 /* or 35 */
#endif

#ifndef EAGAIN
#define EAGAIN 11 /* or 5, or 35 */
#endif

#ifndef EADDRINUSE
#define EADDRINUSE 98 /* or 48, or 125, or 226, or 112 */
#define EADDRNOTAVAIL 99 /* or 49, or 126, or 227, or 125 */
#define EAFNOSUPPORT 97 /* or 47, or 124, or 225, or 106 */
#define EALREADY 114 /* or 37, or 149, or 244, or 120, or 66 */
#define ECONNABORTED 103 /* or 53, or 130, or 231, or 113 */
#define ECONNREFUSED 111 /* or 61, or 146, or 239 */
#define ECONNRESET 104 /* or 54, or 131, or 232 */
#define EDESTADDRREQ 89 /* or 39, or 96, or 217, or 121 */
#define EHOSTDOWN 112 /* or 64, or 147, or 241, or 117 */
#define EHOSTUNREACH 113 /* or 65, or 148, or 242, or 118 */
#define EINPROGRESS 115 /* or 36, or 150, or 245, or 119 */
#define EISCONN 106 /* or 56, or 133, or 234, or 127 */
#define EMSGSIZE 90 /* or 40, or 97, or 218, or 122 */
#define ENETDOWN 100 /* or 50, or 127, or 228, or 115 */
#define ENETRESET 102 /* or 52, or 129, or 230, or 126 */
#define ENETUNREACH 101 /* or 51, or 128, or 229, or 114 */
#define ENOBUFS 105 /* or 55, or 132, or 233 */
#define ENOPROTOOPT 92 /* or 42, or 99, or 220, or 109 */
#define ENOTCONN 107 /* or 57, or 134, or 235, or 128 */
#define ENOTSOCK 88 /* or 38, or 95, or 216, or 108, or 63 */
#define EOPNOTSUPP 95 /* or 45, or 122, or 223 */
#define EPFNOSUPPORT 96 /* or 46, or 123, or 224 */
#define EPROTONOSUPPORT 93 /* or 43, or 120, or 221, or 123 */
#define EPROTOTYPE 91 /* or 41, or 98, or 219, or 107 */
#define ESHUTDOWN 108 /* or 58, or 143, or 236, or 110 */
#define ESOCKTNOSUPPORT 94 /* or 44, or 121, or 222, or 124 */
#define ETIMEDOUT 110 /* or 60, or 145, or 238, or 116 */
#define ETOOMANYREFS 109 /* or 59, or 144, or 237, or 129 */
#endif

#ifndef EWOULDBLOCK
#define EWOULDBLOCK EAGAIN /* or 246 */
#endif

#ifndef errno
_EXPFUNC int *_RTLENTRY _errno(void);
#define errno (*_errno())
#endif

#ifdef __cplusplus
}
#endif

#endif

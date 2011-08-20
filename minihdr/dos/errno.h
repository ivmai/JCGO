/*
 * @(#) $(JCGO)/minihdr/dos/errno.h --
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
#define EACCES 13 /* or 5, or 6, or 24 */
#define EBADF 9 /* or 4, or 6, or 43 */
#define EBUSY 16 /* or 19, or 44 */
#define EEXIST 17 /* or 7, or 16, or 35 */
#define EINTR 4 /* or 16, or 35, or 39 */
#define EINVAL 22 /* or 9, or 19, or 36 */
#define EMFILE 24 /* or 4, or 11, or 25 */
#define ENFILE 23 /* or 10 */
#define ENOENT 2 /* or 1, or 23 */
#define ENOEXEC 8 /* or 3, or 21 */
#define ENOMEM 12 /* or 5, or 8 */
#define ENOSPC 28 /* or 12, or 45 */
#define ENOTDIR 20 /* or 23, or 45 */
#define EPIPE 32 /* or 29 */
#define ERANGE 34 /* or 2, or 14 */
#define EROFS 30
#define ESPIPE 29 /* or 31 */
#endif

#ifndef EAGAIN
#define EAGAIN 11 /* or 18, or 38, or 42 */
#endif

#ifndef errno
_EXPFUNC int *_RTLENTRY _errno(void);
#define errno (*_errno())
#endif

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/unix/sys/select.h --
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

#ifndef _SYS_SELECT_H
#define _SYS_SELECT_H

#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef FD_SETSIZE
#define FD_SETSIZE 256 /* or 64, or 1024, or 65536 */
#endif

#ifndef _FD_MASK_DEFINED
#define _FD_MASK_DEFINED
typedef long fd_mask; /* or int */
#endif

#ifndef CHAR_BIT
#define CHAR_BIT 8
#endif

#ifndef FD_SET
#ifndef _NFDBITS
#define _NFDBITS (sizeof(fd_mask) * CHAR_BIT)
#endif
typedef struct fd_set
{
 fd_mask fds_bits[(FD_SETSIZE + (_NFDBITS - 1)) / _NFDBITS];
} fd_set;
#define FD_SET(fd, set) ((void)((fd) < FD_SETSIZE ? ((set)->fds_bits[(fd) / _NFDBITS] |= (fd_mask)1 << ((fd) % _NFDBITS)) : 0))
#define FD_ISSET(fd, set) ((fd) < FD_SETSIZE && ((set)->fds_bits[(fd) / _NFDBITS] & ((fd_mask)1 << ((fd) % _NFDBITS))) != 0)
#endif

#ifndef FD_ZERO
_EXPFUNC_BUILTIN void *_RTLENTRY_BUILTIN memset(void *, int, size_t)
 _ATTRIBUTE_NONNULL(1);
#define FD_ZERO(set) ((void)memset(set, 0, sizeof(fd_set)))
#endif

_EXPFUNC int _RTLENTRY select(int, fd_set *, fd_set *, fd_set *,
 struct timeval *);

#ifdef __cplusplus
}
#endif

#endif

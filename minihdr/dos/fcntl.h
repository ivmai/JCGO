/*
 * @(#) $(JCGO)/minihdr/dos/fcntl.h --
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

#ifndef _FCNTL_H
#define _FCNTL_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef O_RDONLY
#define O_RDONLY 0 /* or 0x1 */
#define O_WRONLY 0x1 /* or 0x2 */
#define O_RDWR 0x2 /* or 0x4 */
#endif

#ifndef O_APPEND
#define O_APPEND 0x8 /* or 0x10, or 0x800 */
#endif

#ifndef O_CREAT
#define O_CREAT 0x100 /* or 0x20 */
#define O_TRUNC 0x200 /* or 0x40 */
#endif

#ifndef O_EXCL
#define O_EXCL 0x400
#endif

#ifndef O_TEXT
#define O_BINARY ((int)0x8000L) /* or 0x200 */
#define O_TEXT 0x4000 /* or 0x100 */
#endif

#ifndef O_NOINHERIT
#define O_NOINHERIT 0x80
#endif

_EXPFUNC int _RTLENTRY _pipe(int *, unsigned, int) _ATTRIBUTE_NONNULL(1);

#ifdef __cplusplus
}
#endif

#endif

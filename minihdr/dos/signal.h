/*
 * @(#) $(JCGO)/minihdr/dos/signal.h --
 * a part of the minimalist "libc" headers for JCGO (PC-specific).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef _SIGNAL_H
#define _SIGNAL_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

typedef void (_USERENTRY *__sighandler_t)(int);

#ifndef SIG_DFL
#define SIG_DFL ((__sighandler_t)0) /* or ((__sighandler_t)(ptrdiff_t)2) */
#endif

#ifndef SIG_IGN
#define SIG_IGN ((__sighandler_t)(ptrdiff_t)1)
#endif

#ifndef _SIGNAL_NO_SIGACK
#ifndef SIG_ACK
#define SIG_ACK ((__sighandler_t)(ptrdiff_t)4)
#endif
#endif

#ifndef SIGINT
#define SIGINT 2 /* or 4, or 6 */
#endif

#ifndef SIGILL
#define SIGILL 4 /* or 1, or 3 */
#endif

#ifndef SIGSEGV
#define SIGSEGV 11 /* or 2, or 4, or 5, or 9 */
#endif

#ifndef SIGTERM
#define SIGTERM 15 /* or 4, or 5, or 6 */
#endif

_EXPFUNC __sighandler_t _RTLENTRY signal(int, __sighandler_t);
_EXPFUNC int _RTLENTRY raise(int);

#ifdef __cplusplus
}
#endif

#endif

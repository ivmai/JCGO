/*
 * @(#) $(JCGO)/minihdr/unix/sys/time.h --
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

#ifndef _SYS_TIME_H
#define _SYS_TIME_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _SUSECONDS_T_DEFINED
#define _SUSECONDS_T_DEFINED
typedef long suseconds_t;
#endif

#ifndef _TIMEVAL_DEFINED
#define _TIMEVAL_DEFINED
struct timeval
{
 time_t tv_sec;
 suseconds_t tv_usec;
};
#endif

#ifndef _TIMESPEC_DEFINED
#define _TIMESPEC_DEFINED
typedef struct timespec
{
 time_t tv_sec;
 long tv_nsec;
} timespec_t;
#endif

#ifndef _TIMESTRUC_T_DEFINED
#define _TIMESTRUC_T_DEFINED
typedef struct timespec timestruc_t;
#endif

#ifdef _SVID_GETTOD
_EXPFUNC int _RTLENTRY gettimeofday(struct timeval *) _ATTRIBUTE_NONNULL(1);
#else
_EXPFUNC int _RTLENTRY gettimeofday(struct timeval *, void *)
 _ATTRIBUTE_NONNULL(1);
#endif

#ifdef __cplusplus
}
#endif

#endif

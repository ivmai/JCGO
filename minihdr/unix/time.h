/*
 * @(#) $(JCGO)/minihdr/unix/time.h --
 * a part of the minimalist "libc" headers for JCGO (Unix-specific).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef _TIME_H
#define _TIME_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _TIME_NO_CLOCK_MONOTONIC
#define _POSIX_MONOTONIC_CLOCK 0
#ifndef CLOCK_MONOTONIC
#define CLOCK_MONOTONIC 1
#endif
#endif

#ifndef _CLOCKID_T_DEFINED
#define _CLOCKID_T_DEFINED
typedef int clockid_t;
#endif

#ifndef _TIMESPEC_DEFINED
#define _TIMESPEC_DEFINED
typedef struct timespec
{
 time_t tv_sec;
 long tv_nsec;
} timespec_t;
#endif

#ifndef _TM_DEFINED
#define _TM_DEFINED
struct tm
{
 int tm_sec;
 int tm_min;
 int tm_hour;
 int tm_mday;
 int tm_mon; /* unused */
 int tm_year; /* unused */
 int tm_wday; /* unused */
 int tm_yday; /* unused */
 int tm_isdst; /* unused */
#ifndef _TM_NO_GMTOFF
 long tm_gmtoff; /* unused */
 const char *tm_zone; /* unused */
#endif
};
#ifndef _TM_NO_GMTOFF
#define __tm_gmtoff tm_gmtoff
#define __tm_zone tm_zone
#endif
#endif

#ifndef _TIME_NO_TZSET
_EXPFUNC void _RTLENTRY tzset(void);
#endif

_EXPFUNC struct tm *_RTLENTRY localtime(const time_t *) _ATTRIBUTE_NONNULL(1);

#ifndef _TIME_NO_CLOCK_GETTIME
_EXPFUNC int _RTLENTRY clock_gettime(clockid_t, struct timespec *)
 _ATTRIBUTE_NONNULL(2);
#endif

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/unix/sys/types.h --
 * a part of the minimalist "libc" headers for JCGO (Unix-specific).
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

#ifndef _SYS_TYPES_H
#define _SYS_TYPES_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _UINT16_T_DEFINED
#define _UINT16_T_DEFINED
typedef unsigned short uint16_t;
#endif

#ifndef _UINT32_T_DEFINED
#define _UINT32_T_DEFINED
typedef unsigned long uint32_t; /* or unsigned */
#endif

#ifndef _TIME_T_DEFINED
#define _TIME_T_DEFINED
typedef long time_t;
#endif

#ifndef _PID_T_DEFINED
#define _PID_T_DEFINED
typedef int pid_t; /* or long */
#endif

#ifndef _DEV_T_DEFINED
#define _DEV_T_DEFINED
typedef long dev_t; /* or long long */
#endif

#ifndef _INO_T_DEFINED
#define _INO_T_DEFINED
typedef unsigned ino_t; /* or unsigned short, or unsigned long long */
#endif

#ifndef _MODE_T_DEFINED
#define _MODE_T_DEFINED
typedef unsigned short mode_t; /* or unsigned long, or int */
#endif

#ifndef _NLINK_T_DEFINED
#define _NLINK_T_DEFINED
typedef unsigned nlink_t; /* or unsigned short */
#endif

#ifndef _GID_T_DEFINED
#define _GID_T_DEFINED
typedef int gid_t; /* or unsigned short */
#endif

#ifndef _UID_T_DEFINED
#define _UID_T_DEFINED
typedef gid_t uid_t;
#endif

#ifndef _SSIZE_T_DEFINED
#define _SSIZE_T_DEFINED
typedef __SSIZE_TYPE__ ssize_t;
#endif

#ifndef _OFF_T_DEFINED
#define _OFF_T_DEFINED
typedef long off_t; /* or long long */
#endif

#ifndef __OFF64_TYPE__
#define __OFF64_TYPE__ __int64 /* or long */
#endif

#ifndef _OFF64_T_DEFINED
#define _OFF64_T_DEFINED
typedef __OFF64_TYPE__ off64_t;
#endif

#ifdef __cplusplus
}
#endif

#endif

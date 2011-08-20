/*
 * @(#) $(JCGO)/minihdr/unix/sys/utsname.h --
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

#ifndef _SYS_UTSNAME_H
#define _SYS_UTSNAME_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _SYS_NMLN
#define _SYS_NMLN 257 /* or 256, or 65, or 20 */
#endif

#ifndef _UTSNAME_DEFINED
#define _UTSNAME_DEFINED
struct utsname
{
 char sysname[_SYS_NMLN];
 char nodename[_SYS_NMLN]; /* unused */
 char release[_SYS_NMLN];
 char version[_SYS_NMLN];
 char machine[_SYS_NMLN]; /* unused */
#ifndef _UTSNAME_NO_DOMAINNAME
 char domainname[_SYS_NMLN]; /* unused */
#endif
};
#endif

_EXPFUNC int _RTLENTRY uname(struct utsname *) _ATTRIBUTE_NONNULL(1);

#ifdef __cplusplus
}
#endif

#endif

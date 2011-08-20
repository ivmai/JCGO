/*
 * @(#) $(JCGO)/minihdr/unix/netdb.h --
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

#ifndef _NETDB_H
#define _NETDB_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef MAXHOSTNAMELEN
#define MAXHOSTNAMELEN 256
#endif

#ifndef _HOSTENT_DEFINED
#define _HOSTENT_DEFINED
struct hostent
{
 char *h_name;
 char **h_aliases; /* unused */
 int h_addrtype;
 int h_length;
 char **h_addr_list;
};
#endif

_EXPFUNC struct hostent *_RTLENTRY gethostbyaddr(const char *, int, int)
 _ATTRIBUTE_NONNULL(1);
_EXPFUNC struct hostent *_RTLENTRY gethostbyname(const char *)
 _ATTRIBUTE_NONNULL(1);

#ifdef _NETDB_SYSV_REENT
_EXPFUNC struct hostent *_RTLENTRY gethostbyaddr_r(const char *, int, int,
 struct hostent *, char *, int, int *) _ATTRIBUTE_NONNULL(1)
 _ATTRIBUTE_NONNULL(4) _ATTRIBUTE_NONNULL(5);
_EXPFUNC struct hostent *_RTLENTRY gethostbyname_r(const char *,
 struct hostent *, char *, int, int *) _ATTRIBUTE_NONNULL(1)
 _ATTRIBUTE_NONNULL(2) _ATTRIBUTE_NONNULL(3);
#else
_EXPFUNC int _RTLENTRY gethostbyaddr_r(const char *, int, int,
 struct hostent *, char *, size_t, struct hostent **, int *)
 _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(4) _ATTRIBUTE_NONNULL(5)
 _ATTRIBUTE_NONNULL(7);
_EXPFUNC int _RTLENTRY gethostbyname_r(const char *, struct hostent *, char *,
 size_t, struct hostent **, int *) _ATTRIBUTE_NONNULL(1) _ATTRIBUTE_NONNULL(2)
 _ATTRIBUTE_NONNULL(3) _ATTRIBUTE_NONNULL(5);
#endif

#ifndef _UNISTD_NETDB_NO_GETHOSTNAME
_EXPFUNC int _RTLENTRY gethostname(char *, size_t) _ATTRIBUTE_NONNULL(1);
#endif

#ifdef __cplusplus
}
#endif

#endif

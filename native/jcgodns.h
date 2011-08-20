/*
 * @(#) $(JCGO)/native/jcgodns.h --
 * a part of the JCGO native layer library (inet gethostby defs).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_GNUNETDB, JCGO_OLDWSOCK, JCGO_SYSVNETDB,
 * JCGO_UNIX, JCGO_WIN32.
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

#ifdef JCGO_VER

/* #include <stdlib.h> */

#ifdef JCGO_WIN32

#ifndef WIN32
#define WIN32
#endif

#ifndef __CYGWIN__
#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifdef JCGO_OLDWSOCK

#ifndef _WINSOCK_H
#include <winsock.h>
/* struct hostent FAR *gethostbyaddr(const char *, int, int); */
/* struct hostent FAR *gethostbyname(const char *); */
#endif

#else /* JCGO_OLDWSOCK */

#ifndef _WINSOCK2_H
#include <winsock2.h>
/* struct hostent FAR *gethostbyaddr(const char *, int, int); */
/* struct hostent FAR *gethostbyname(const char *); */
#endif

#endif /* ! JCGO_OLDWSOCK */

#else /* JCGO_WIN32 */

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef JCGO_UNIX
#ifndef _UNISTD_H
#include <unistd.h>
#endif
#endif

#ifndef BSD_COMP
#define BSD_COMP 1
#endif

#ifndef _NETDB_H
#include <netdb.h>
/* struct hostent *gethostbyaddr(const char *, int, int); */
/* struct hostent *gethostbyname(const char *); */
#endif

#ifndef _SYS_SOCKET_H
#include <sys/socket.h>
#endif

#ifndef _ERRNO_H
#include <errno.h>
/* int errno; */
#endif

#ifndef ERANGE
#define ERANGE 12344
#endif

#endif /* ! JCGO_WIN32 */

#ifndef JCGO_GETHOSTBY_FAR
#ifdef FAR
#define JCGO_GETHOSTBY_FAR FAR
#else
#define JCGO_GETHOSTBY_FAR /* empty */
#endif
#endif

#ifndef MAXHOSTNAMELEN
#define MAXHOSTNAMELEN 256
#endif

#ifndef JCGO_WIN32

#ifndef JCGO_GETHOSTBY_REENTBUFSIZE
#define JCGO_GETHOSTBY_REENTBUFSIZE 0x400
#define JCGO_GETHOSTBY_BIGREENTBUFSIZE 0x3000
#endif

#ifdef JCGO_GNUNETDB

/* #include <netdb.h> */
/* int gethostbyaddr_r(const char *, int, int, struct hostent *, char *, size_t, struct hostent FAR **, int *); */
/* int gethostbyname_r(const char *, struct hostent *, char *, size_t, struct hostent FAR **, int *); */

/* #include <stdlib.h> */
/* void free(void *); */
/* void *malloc(size_t); */

#define JCGO_GETHOSTBY_T struct { struct hostent hent; int herrcode; struct hostent JCGO_GETHOSTBY_FAR *phent; void *pbigbuf; char buf[JCGO_GETHOSTBY_REENTBUFSIZE]; }
#define JCGO_GETHOSTBY_ADDR(address, len, family, pdata) ((pdata)->phent = NULL, gethostbyaddr_r(address, len, family, &(pdata)->hent, (pdata)->buf, sizeof((pdata)->buf), &(pdata)->phent, &(pdata)->herrcode), (pdata)->phent != NULL || errno != ERANGE ? ((pdata)->pbigbuf = NULL, (pdata)->phent) : ((pdata)->pbigbuf = malloc(JCGO_GETHOSTBY_BIGREENTBUFSIZE)) != NULL ? (gethostbyaddr_r(address, len, family, &(pdata)->hent, (pdata)->pbigbuf, JCGO_GETHOSTBY_BIGREENTBUFSIZE, &(pdata)->phent, &(pdata)->herrcode), (pdata)->phent) : NULL)
#define JCGO_GETHOSTBY_NAME(name, pdata) ((pdata)->phent = NULL, gethostbyname_r(name, &(pdata)->hent, (pdata)->buf, sizeof((pdata)->buf), &(pdata)->phent, &(pdata)->herrcode), (pdata)->phent != NULL || errno != ERANGE ? ((pdata)->pbigbuf = NULL, (pdata)->phent) : ((pdata)->pbigbuf = malloc(JCGO_GETHOSTBY_BIGREENTBUFSIZE)) != NULL ? (gethostbyname_r(name, &(pdata)->hent, (pdata)->pbigbuf, JCGO_GETHOSTBY_BIGREENTBUFSIZE, &(pdata)->phent, &(pdata)->herrcode), (pdata)->phent) : NULL)
#define JCGO_GETHOSTBY_FREE(pdata) ((pdata)->pbigbuf != NULL ? (free((pdata)->pbigbuf), 0) : 0)

#else /* JCGO_GNUNETDB */

#ifdef JCGO_SYSVNETDB

/* #include <netdb.h> */
/* struct hostent *gethostbyaddr_r(const char *, int, int, struct hostent *, char *, int, int *); */
/* struct hostent *gethostbyname_r(const char *, struct hostent *, char *, int, int *); */

/* #include <stdlib.h> */
/* void free(void *); */
/* void *malloc(size_t); */

#define JCGO_GETHOSTBY_T struct { struct hostent hent; int herrcode; struct hostent JCGO_GETHOSTBY_FAR *phent; void *pbigbuf; char buf[JCGO_GETHOSTBY_REENTBUFSIZE]; }
#define JCGO_GETHOSTBY_ADDR(address, len, family, pdata) (((pdata)->phent = gethostbyaddr_r(address, len, family, &(pdata)->hent, (pdata)->buf, sizeof((pdata)->buf), &(pdata)->herrcode)) != NULL || errno != ERANGE ? ((pdata)->pbigbuf = NULL, (pdata)->phent) : ((pdata)->pbigbuf = malloc(JCGO_GETHOSTBY_BIGREENTBUFSIZE)) != NULL ? gethostbyaddr_r(address, len, family, &(pdata)->hent, (pdata)->pbigbuf, JCGO_GETHOSTBY_BIGREENTBUFSIZE, &(pdata)->herrcode) : NULL)
#define JCGO_GETHOSTBY_NAME(name, pdata) (((pdata)->phent = gethostbyname_r(name, &(pdata)->hent, (pdata)->buf, sizeof((pdata)->buf), &(pdata)->herrcode)) != NULL || errno != ERANGE ? ((pdata)->pbigbuf = NULL, (pdata)->phent) : ((pdata)->pbigbuf = malloc(JCGO_GETHOSTBY_BIGREENTBUFSIZE)) != NULL ? gethostbyname_r(name, &(pdata)->hent, (pdata)->pbigbuf, JCGO_GETHOSTBY_BIGREENTBUFSIZE, &(pdata)->herrcode) : NULL)
#define JCGO_GETHOSTBY_FREE(pdata) ((pdata)->pbigbuf != NULL ? (free((pdata)->pbigbuf), 0) : 0)

#else /* JCGO_SYSVNETDB */

#ifdef _HOSTBUFSIZE
/* #include <netdb.h> */
/* int gethostbyaddr_r(char *, int, int, struct hostent *, struct hostent_data *); */
/* int gethostbyname_r(char *, struct hostent *, struct hostent_data *); */
#define JCGO_GETHOSTBY_T struct { struct hostent hent; struct hostent_data buf; }
#define JCGO_GETHOSTBY_ADDR(address, len, family, pdata) (gethostbyaddr_r(address, len, family, &(pdata)->hent, &(pdata)->buf) != -1 ? &(pdata)->hent : NULL)
#define JCGO_GETHOSTBY_NAME(name, pdata) (gethostbyname_r((char *)(name), &(pdata)->hent, &(pdata)->buf) != -1 ? &(pdata)->hent : NULL)
#define JCGO_GETHOSTBY_FREE(pdata) 0
#endif

#endif /* ! JCGO_SYSVNETDB */

#endif /* ! JCGO_GNUNETDB */

#endif /* ! JCGO_WIN32 */

#ifndef JCGO_GETHOSTBY_T
#define JCGO_GETHOSTBY_T char*
#define JCGO_GETHOSTBY_ADDR(address, len, family, pdata) (*(pdata) = address, gethostbyaddr(*(pdata), len, family))
#define JCGO_GETHOSTBY_NAME(name, pdata) (*(pdata) = (char *)(name), gethostbyname(*(pdata)))
#define JCGO_GETHOSTBY_FREE(pdata) 0
#ifndef JCGO_WIN32
#ifndef __CYGWIN__
#ifndef h_errno
#define JCGO_GETHOSTBY_NEEDSYNC 1
#endif
#endif
#endif
#endif

#ifndef JCGO_GETHOSTBY_NEEDSYNC
#define JCGO_GETHOSTBY_NEEDSYNC 0
#endif

#ifndef AF_INET
#define AF_INET 2
#endif

#endif

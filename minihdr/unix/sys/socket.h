/*
 * @(#) $(JCGO)/minihdr/unix/sys/socket.h --
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

#ifndef _SYS_SOCKET_H
#define _SYS_SOCKET_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#define AF_UNSPEC 0
#define AF_INET 2

#ifndef AF_INET6
#define AF_INET6 26 /* or 10, or 23, or 28, or 30 */
#endif

#ifndef SOCK_STREAM
#define SOCK_STREAM 1
#define SOCK_DGRAM 2
#endif

#ifndef SOL_SOCKET
#define SOL_SOCKET 0xffff /* or 1 */
#endif

#ifndef SO_REUSEADDR
#define SO_REUSEADDR 0x4 /* or 2 */
#define SO_KEEPALIVE 0x8 /* or 9 */
#define SO_BROADCAST 0x20 /* or 6 */
#define SO_LINGER 0x80 /* or 13 */
#define SO_OOBINLINE 0x100 /* or 10 */
#define SO_SNDBUF 0x1001 /* or 7 */
#define SO_RCVBUF 0x1002 /* or 8 */
#endif

#ifdef _SOCKET_HAS_NOSIGPIPE
#ifndef SO_NOSIGPIPE
#define SO_NOSIGPIPE 0x800
#endif
#endif

#define MSG_OOB 0x1
#define MSG_PEEK 0x2

#define SHUT_RD 0
#define SHUT_WR 1
#define SHUT_RDWR 2

#ifndef _LINGER_DEFINED
#define _LINGER_DEFINED
#ifndef _LINGER_LONOFF_TYPE
#define _LINGER_LONOFF_TYPE int /* or unsigned short */
#endif
#ifndef _LINGER_LLINGER_TYPE
#define _LINGER_LLINGER_TYPE _LINGER_LONOFF_TYPE
#endif
struct linger
{
 _LINGER_LONOFF_TYPE l_onoff;
 _LINGER_LLINGER_TYPE l_linger;
};
#endif

#ifndef _SOCKADDR_DEFINED
#define _SOCKADDR_DEFINED
struct sockaddr
{
 uint16_t sa_family;
 char sa_data[14];
};
#endif

#ifndef _SOCKLEN_T_DEFINED
#define _SOCKLEN_T_DEFINED
typedef unsigned socklen_t;
#endif

_EXPFUNC int _RTLENTRY accept(int, struct sockaddr *, socklen_t *);
_EXPFUNC int _RTLENTRY bind(int, const struct sockaddr *, socklen_t);
_EXPFUNC int _RTLENTRY connect(int, const struct sockaddr *, socklen_t);
_EXPFUNC int _RTLENTRY getsockname(int, struct sockaddr *, socklen_t *);
_EXPFUNC int _RTLENTRY getsockopt(int, int, int, void *, socklen_t *);
_EXPFUNC int _RTLENTRY listen(int, int);
_EXPFUNC ssize_t _RTLENTRY recvfrom(int, void *, size_t, int,
 struct sockaddr *, socklen_t *);
_EXPFUNC ssize_t _RTLENTRY sendto(int, const void *, size_t, int,
 const struct sockaddr *, socklen_t);
_EXPFUNC int _RTLENTRY setsockopt(int, int, int, const void *, socklen_t);
_EXPFUNC int _RTLENTRY shutdown(int, int);
_EXPFUNC int _RTLENTRY socket(int, int, int);

#ifndef _SOCKET_CLOSESOCKET
#define _SOCKET_CLOSESOCKET close /* or closesocket, or so_close */
#endif

_EXPFUNC int _RTLENTRY _SOCKET_CLOSESOCKET(int);

#ifdef __cplusplus
}
#endif

#endif

/*
 * @(#) $(JCGO)/minihdr/win/winsock2.h --
 * a part of the minimalist "Win32" headers for JCGO.
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

#ifndef _WINSOCK2_H
#define _WINSOCK2_H

#define _WINSOCK_H

#ifndef _WINDEF_H
#include <windef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef WINSOCK_API_LINKAGE
#define WINSOCK_API_LINKAGE __declspec(dllimport)
#endif

#define WSAAPI WINAPI

typedef UINT_PTR SOCKET;

#ifndef FD_SETSIZE
#define FD_SETSIZE 64
#endif

#define SD_RECEIVE 0
#define SD_SEND 1
#define SD_BOTH 2

#define IPPROTO_IP 0
#define IPPROTO_TCP 6

#define IPPROTO_IPV6 41

#define INADDR_ANY (ULONG)0

#define SO_REUSEADDR 0x4
#define SO_KEEPALIVE 0x8
#define SO_BROADCAST 0x20
#define SO_LINGER 0x80
#define SO_OOBINLINE 0x100
#define SO_SNDBUF 0x1001
#define SO_RCVBUF 0x1002

#define SOCK_STREAM 1
#define SOCK_DGRAM 2

#define TCP_NODELAY 1

#define AF_UNSPEC 0
#define AF_INET 2
#define AF_INET6 23

#define SOL_SOCKET 0xffff

#define MSG_OOB 1
#define MSG_PEEK 2

#define h_errno WSAGetLastError()

#ifndef WSABASEERR
#define WSABASEERR 10000
#define WSAEINTR (WSABASEERR + 4)
#define WSAEBADF (WSABASEERR + 9)
#define WSAEACCES (WSABASEERR + 13)
#define WSAEFAULT (WSABASEERR + 14)
#define WSAEINVAL (WSABASEERR + 22)
#define WSAEMFILE (WSABASEERR + 24)
#define WSAEWOULDBLOCK (WSABASEERR + 35)
#define WSAEINPROGRESS (WSABASEERR + 36)
#define WSAEALREADY (WSABASEERR + 37)
#define WSAENOTSOCK (WSABASEERR + 38)
#define WSAEDESTADDRREQ (WSABASEERR + 39)
#define WSAEMSGSIZE (WSABASEERR + 40)
#define WSAEPROTOTYPE (WSABASEERR + 41)
#define WSAENOPROTOOPT (WSABASEERR + 42)
#define WSAEPROTONOSUPPORT (WSABASEERR + 43)
#define WSAESOCKTNOSUPPORT (WSABASEERR + 44)
#define WSAEOPNOTSUPP (WSABASEERR + 45)
#define WSAEPFNOSUPPORT (WSABASEERR + 46)
#define WSAEAFNOSUPPORT (WSABASEERR + 47)
#define WSAEADDRINUSE (WSABASEERR + 48)
#define WSAEADDRNOTAVAIL (WSABASEERR + 49)
#define WSAENETDOWN (WSABASEERR + 50)
#define WSAENETUNREACH (WSABASEERR + 51)
#define WSAENETRESET (WSABASEERR + 52)
#define WSAECONNABORTED (WSABASEERR + 53)
#define WSAECONNRESET (WSABASEERR + 54)
#define WSAENOBUFS (WSABASEERR + 55)
#define WSAEISCONN (WSABASEERR + 56)
#define WSAENOTCONN (WSABASEERR + 57)
#define WSAESHUTDOWN (WSABASEERR + 58)
#define WSAETOOMANYREFS (WSABASEERR + 59)
#define WSAETIMEDOUT (WSABASEERR + 60)
#define WSAECONNREFUSED (WSABASEERR + 61)
#define WSAELOOP (WSABASEERR + 62)
#define WSAENAMETOOLONG (WSABASEERR + 63)
#define WSAEHOSTDOWN (WSABASEERR + 64)
#define WSAEHOSTUNREACH (WSABASEERR + 65)
#endif

#define WSADESCRIPTION_LEN 256
#define WSASYS_STATUS_LEN 128

#ifndef _IOR
#define IOCPARM_MASK 0x7f
#define IOC_OUT (LONG)0x40000000L
#define IOC_IN (LONG)0x80000000L
#define _IOR(x, y, t) (IOC_OUT | (((LONG)sizeof(t) & IOCPARM_MASK) << 16) | ((x) << 8) | (y))
#define _IOW(x, y, t) (IOC_IN | (((LONG)sizeof(t) & IOCPARM_MASK) << 16) | ((x) << 8) | (y))
#endif

#ifndef _IOCTLSOCKET_ARG_T
#define _IOCTLSOCKET_ARG_T unsigned long
#endif

#ifndef FIONBIO
#define FIONBIO _IOW('f', 126, _IOCTLSOCKET_ARG_T)
#endif

#ifndef FIONREAD
#define FIONREAD _IOR('f', 127, _IOCTLSOCKET_ARG_T)
#endif

#ifndef FD_ZERO
typedef struct fd_set
{
 unsigned fd_count;
 SOCKET fd_array[FD_SETSIZE];
} fd_set;
#define FD_ZERO(set) (((fd_set FAR *)(set))->fd_count = 0)
#endif

#ifndef FD_SET
#define FD_SET(fd, set) do { unsigned __i; for (__i = 0; __i < ((fd_set FAR *)(set))->fd_count; __i++) { if (((fd_set FAR *)(set))->fd_array[__i] == (fd)) break; } if (((fd_set FAR *)(set))->fd_count == __i && ((fd_set FAR *)(set))->fd_count < FD_SETSIZE) { ((fd_set FAR *)(set))->fd_array[__i] = (fd); ((fd_set FAR *)(set))->fd_count++; } } while(0)
#endif

#ifndef FD_ISSET
#define FD_ISSET(fd, set) __WSAFDIsSet((SOCKET)(fd), (fd_set FAR *)(set))
#endif

#ifndef _TIMEVAL_DEFINED
#define _TIMEVAL_DEFINED
struct timeval
{
 LONG tv_sec;
 LONG tv_usec;
};
#endif

struct hostent
{
 char FAR *h_name;
 char FAR *FAR *h_aliases; /* unused */
 short h_addrtype;
 short h_length;
#ifdef _WIN64
#ifndef _NO_MANUAL_ALIGNMENT
 short _pad1, _pad2; /* unused */
#endif
#endif
 char FAR *FAR *h_addr_list;
};

struct linger
{
 unsigned short l_onoff;
 unsigned short l_linger;
};

struct in_addr
{
 union
 {
  struct
  {
   unsigned char s_b1, s_b2, s_b3, s_b4;
  } S_un_b;
  struct
  {
   unsigned short s_w1, s_w2;
  } S_un_w;
  ULONG S_addr;
 } S_un;
};

struct sockaddr_in
{
 short sin_family;
 unsigned short sin_port;
 struct in_addr sin_addr;
 char sin_zero[8]; /* unused */
};

typedef struct WSAData
{
 unsigned short wVersion;
 unsigned short wHighVersion; /* unused */
#ifdef _WIN64
 unsigned short iMaxSockets; /* unused */
 unsigned short iMaxUdpDg; /* unused */
 char FAR *lpVendorInfo; /* unused */
 char szDescription[WSADESCRIPTION_LEN + 1]; /* unused */
 char szSystemStatus[WSASYS_STATUS_LEN + 1]; /* unused */
#else
 char szDescription[WSADESCRIPTION_LEN + 1]; /* unused */
 char szSystemStatus[WSASYS_STATUS_LEN + 1]; /* unused */
 unsigned short iMaxSockets; /* unused */
 unsigned short iMaxUdpDg; /* unused */
 char FAR *lpVendorInfo; /* unused */
#endif
} WSADATA;

struct sockaddr
{
 unsigned short sa_family;
 char sa_data[14];
};

WINSOCK_API_LINKAGE SOCKET WSAAPI accept(SOCKET, struct sockaddr FAR *,
 int FAR *);
WINSOCK_API_LINKAGE int WSAAPI bind(SOCKET, const struct sockaddr FAR *, int);

WINSOCK_API_LINKAGE int WSAAPI closesocket(SOCKET);

WINSOCK_API_LINKAGE int WSAAPI connect(SOCKET, const struct sockaddr FAR *,
 int);

WINSOCK_API_LINKAGE int WSAAPI getsockname(SOCKET, struct sockaddr FAR *,
 int FAR *);
WINSOCK_API_LINKAGE int WSAAPI getsockopt(SOCKET, int, int, char FAR *,
 int FAR *);

WINSOCK_API_LINKAGE unsigned short WSAAPI htons(unsigned short);

WINSOCK_API_LINKAGE int WSAAPI ioctlsocket(SOCKET, LONG,
 _IOCTLSOCKET_ARG_T FAR *);

WINSOCK_API_LINKAGE int WSAAPI listen(SOCKET, int);

WINSOCK_API_LINKAGE unsigned short WSAAPI ntohs(unsigned short);

WINSOCK_API_LINKAGE int WSAAPI recvfrom(SOCKET, char FAR *, int, int,
 struct sockaddr FAR *, int FAR *);

WINSOCK_API_LINKAGE int WSAAPI select(int, fd_set FAR *, fd_set FAR *,
 fd_set FAR *, const struct timeval FAR *);

WINSOCK_API_LINKAGE int WSAAPI sendto(SOCKET, const char FAR *, int, int,
 const struct sockaddr FAR *, int);
WINSOCK_API_LINKAGE int WSAAPI setsockopt(SOCKET, int, int, const char FAR *,
 int);
WINSOCK_API_LINKAGE int WSAAPI shutdown(SOCKET, int);
WINSOCK_API_LINKAGE SOCKET WSAAPI socket(int, int, int);

WINSOCK_API_LINKAGE struct hostent FAR *WSAAPI gethostbyaddr(const char FAR *,
 int, int);
WINSOCK_API_LINKAGE struct hostent FAR *WSAAPI gethostbyname(
 const char FAR *);

WINSOCK_API_LINKAGE int WSAAPI gethostname(char FAR *, int);

WINSOCK_API_LINKAGE int WSAAPI WSAGetLastError(void);

WINSOCK_API_LINKAGE int WSAAPI WSAStartup(unsigned short, WSADATA FAR *);
WINSOCK_API_LINKAGE int WSAAPI WSACleanup(void);

WINSOCK_API_LINKAGE int WSAAPI __WSAFDIsSet(SOCKET, fd_set FAR *);

#ifdef __cplusplus
}
#endif

#endif

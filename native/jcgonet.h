/*
 * @(#) $(JCGO)/native/jcgonet.h --
 * a part of the JCGO native layer library (network I/O defs).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_EXEC, JCGO_OLDWSOCK, JCGO_OS2, JCGO_UNIX,
 * JCGO_WIN32.
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

#ifdef JCGO_UNIX
#define STRICT
#endif

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN 1
#endif

#ifndef _WINDOWS_H
#include <windows.h>
#endif

#ifndef __CYGWIN__
#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif
#endif

#ifdef JCGO_OLDWSOCK

#ifndef _WINSOCK_H
#include <winsock.h>
#endif

#else /* JCGO_OLDWSOCK */

#ifndef _WINSOCK2_H
#include <winsock2.h>
#endif

#ifndef _WS2TCPIP_H
#include <ws2tcpip.h>
#endif

#endif /* ! JCGO_OLDWSOCK */

/* #include <winsock.h> */
/* int WSACleanup(void); */
/* int WSAGetLastError(void); */
/* int WSAStartup(unsigned short, WSADATA *); */
/* SOCKET accept(SOCKET, struct sockaddr *, int *); */
/* int bind(SOCKET, const struct sockaddr *, int); */
/* int closesocket(SOCKET); */
/* int connect(SOCKET, const struct sockaddr *, int); */
/* int gethostname(char *, int); */
/* int getsockname(SOCKET, struct sockaddr *, int *); */
/* int getsockopt(SOCKET, int, int, char *, int *); */
/* unsigned short htons(unsigned short); */
/* int ioctlsocket(SOCKET, long, unsigned long *); */
/* int listen(SOCKET, int); */
/* unsigned short ntohs(unsigned short); */
/* int recvfrom(SOCKET, char *, int, int, struct sockaddr *, int *); */
/* int select(int, fd_set *, fd_set *, fd_set *, const struct timeval *); */
/* int sendto(SOCKET, const char *, int, int, const struct sockaddr *, int); */
/* int setsockopt(SOCKET, int, int, const char *, int); */
/* int shutdown(SOCKET, int); */
/* SOCKET socket(int, int, int); */

#ifdef JCGO_UNIX

#define select _unix_select

#ifndef _STRUCT_TIMEVAL
#define _STRUCT_TIMEVAL 1
#endif

#ifndef _TIMEVAL_DEFINED
#define _TIMEVAL_DEFINED 1
#endif

#ifndef _TIMEVAL
#define _TIMEVAL 1
#endif

#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif

#ifndef FD_SET
#ifndef _SYS_SELECT_H
#include <sys/select.h>
#endif
#endif

#ifndef _UNISTD_H
#include <unistd.h>
#endif

#undef select

#endif /* JCGO_UNIX */

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#define JCGO_SOCKGETERR_NOTERRNO
#define JCGO_SOCK_GETERROR(x) WSAGetLastError()

#ifndef JCGO_SOCKINIT_VERWORD
#ifdef JCGO_OLDWSOCK
#define JCGO_SOCKINIT_VERWORD 0x101
#else
#define JCGO_SOCKINIT_VERWORD 0x202 /* minor:major */
#ifndef JCGO_SOCKINIT_REQMAJORVER
#define JCGO_SOCKINIT_REQMAJORVER 2
#endif
#endif
#endif

/* #include <stdlib.h> */
/* int atexit(void (*)(void)); */

#ifdef JCGO_SOCKINIT_REQMAJORVER
#define JCGO_SOCK_INIT(pdata) (WSAStartup(JCGO_SOCKINIT_VERWORD, pdata) ? -1 : ((pdata)->wVersion & 0xff) < JCGO_SOCKINIT_REQMAJORVER ? (WSACleanup(), -1) : (atexit(jcgo_sockCleanup), 0))
#else
#define JCGO_SOCK_INIT(pdata) (WSAStartup(JCGO_SOCKINIT_VERWORD, pdata) ? -1 : (atexit(jcgo_sockCleanup), 0))
#endif

#define JCGO_SOCKINIT_T WSADATA
#define JCGO_SOCK_CLEANUP(x) WSACleanup()
#define JCGO_SOCK_CLOSE(fd, perrcode) (closesocket(fd) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_IOCTL(fd, cmd, val, perrcode) (ioctlsocket(fd, cmd, (void *)&(val)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SELECT(nfds, readfds, writefds, exceptfds, ptv, perrcode) ((*(perrcode) = select(nfds, readfds, writefds, exceptfds, ptv)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#ifndef JCGO_SOCKFIONBIO_T
#define JCGO_SOCKFIONBIO_T unsigned long
#endif

#else /* JCGO_WIN32 */

#ifndef _ERRNO_H
#include <errno.h>
/* int errno; */
#endif

#ifndef EPROTONOSUPPORT
#ifndef _INC_STDLIB
#define _INC_STDLIB 1
#endif
#ifndef JCGO_OS2
#ifndef _TCP_H
#include "tcp.h"
#endif
#endif
#endif

#ifdef JCGO_UNIX

#ifndef BSD_COMP
#define BSD_COMP 1
#endif

#ifndef _SYS_TIME_H
#include <sys/time.h>
/* int select(int, fd_set *, fd_set *, fd_set *, struct timeval *); */
#endif

#ifndef FD_SET
#ifndef _SYS_SELECT_H
#include <sys/select.h>
/* int select(int, fd_set *, fd_set *, fd_set *, struct timeval *); */
#endif
#endif

#ifndef _UNISTD_H
#include <unistd.h>
/* int close(int); */
/* int gethostname(char *, size_t); */
#endif

#else /* JCGO_UNIX */

#ifdef JCGO_OS2
#ifndef _ARPA_INET_H
#include <arpa/inet.h>
/* unsigned short htons(unsigned short); */
/* unsigned short ntohs(unsigned short); */
#endif
#endif

#endif /* ! JCGO_UNIX */

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifndef _TIME_H
#include <time.h>
#endif

#ifndef _STRING_H
#include <string.h>
/* char *strerror(int); */
#endif

#ifndef _SYS_SOCKET_H
#include <sys/socket.h>
/* int accept(int, struct sockaddr *, unsigned *); */
/* int bind(int, const struct sockaddr *, unsigned); */
/* int connect(int, const struct sockaddr *, unsigned); */
/* int getsockname(int, struct sockaddr *, unsigned *); */
/* int getsockopt(int, int, int, void *, unsigned *); */
/* int listen(int, int); */
/* ssize_t recvfrom(int, void *, size_t, int, struct sockaddr *, unsigned *); */
/* ssize_t sendto(int, const void *, size_t, int, const struct sockaddr *, unsigned); */
/* int setsockopt(int, int, int, const void *, unsigned); */
/* int shutdown(int, int); */
/* int socket(int, int, int); */
#endif

#ifndef _SYS_IOCTL_H
#include <sys/ioctl.h>
/* int ioctl(int, int, ...); */
#endif

#ifndef _NETINET_IN_H
#include <netinet/in.h>
/* unsigned short htons(unsigned short); */
/* unsigned short ntohs(unsigned short); */
#endif

#ifndef TCP_NODELAY
#ifndef _NETINET_TCP_H
#include <netinet/tcp.h>
#endif
#endif

#ifndef _NETDB_H
#include <netdb.h>
#endif

#ifndef ESOCKTNOSUPPORT
#ifdef JCGO_OS2
#ifndef _NERRNO_H
#include <nerrno.h>
#endif
#endif
#endif

#ifdef JCGO_UNIX

#ifndef WATTCP_VER

#define JCGO_SOCK_GETERROR(x) (errno + 0)

#define JCGO_SOCK_IOCTL(fd, cmd, val, perrcode) (ioctl(fd, cmd, (void *)&(val)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SELECT(nfds, readfds, writefds, exceptfds, ptv, perrcode) ((*(perrcode) = select(nfds, readfds, writefds, exceptfds, ptv)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#ifdef closesocket

#define JCGO_SOCK_CLOSE(fd, perrcode) (closesocket(fd) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#else /* closesocket */

#ifndef _FCNTL_H
#include <fcntl.h>
#endif

#define JCGO_SOCK_CLOSE(fd, perrcode) (close(fd) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#ifndef JCGO_SOCK_SETCLOEXEC
#ifdef JCGO_EXEC
#ifndef JCGO_FD_SETCLOEXEC
#ifdef FD_CLOEXEC
#ifdef F_GETFD
#ifdef F_SETFD
/* #include <fcntl.h> */
/* int fcntl(int, int, ...); */
#define JCGO_FD_SETCLOEXEC(fd) ((fd) != -1 ? fcntl(fd, F_SETFD, fcntl(fd, F_GETFD, 0) | FD_CLOEXEC) : -1)
#endif
#endif
#endif
#endif
#endif
#ifdef JCGO_FD_SETCLOEXEC
#define JCGO_SOCK_SETCLOEXEC(fd) JCGO_FD_SETCLOEXEC(fd)
#endif
#endif

#endif /* ! closesocket */

#endif /* ! WATTCP_VER */

#else /* JCGO_UNIX */

#ifdef JCGO_OS2

#ifndef _SYS_TIME_H
#include <sys/time.h>
#endif

#ifndef _UNISTD_H
#include <unistd.h>
/* int gethostname(char *, size_t); */
/* int soclose(int); */
/* int select(int, fd_set *, fd_set *, fd_set *, struct timeval *); */
#endif

/* #include <sys/socket.h> */
/* int sock_errno(void); */
/* int sock_init(void); */

#ifdef SOCESOCKTNOSUPPORT
#define JCGO_SOCKGETERR_NOTERRNO
#define JCGO_SOCK_GETERROR(x) sock_errno()
#else
#define JCGO_SOCK_GETERROR(x) (errno + 0)
#endif

#define JCGO_SOCKINIT_T int
#define JCGO_SOCK_INIT(pdata) ((*(pdata) = sock_init()) != 0 ? -1 : 0)
#define JCGO_SOCK_CLOSE(fd, perrcode) (soclose(fd) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_IOCTL(fd, cmd, val, perrcode) (ioctl(fd, cmd, (void *)&(val), sizeof(val)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SELECT(nfds, readfds, writefds, exceptfds, ptv, perrcode) ((*(perrcode) = select(nfds, readfds, writefds, exceptfds, ptv)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#else /* JCGO_OS2 */

#ifndef _SYS_SELECT_H
#include <sys/select.h>
#endif

#endif /* ! JCGO_OS2 */

#endif /* ! JCGO_UNIX */

#ifndef JCGO_SOCK_GETERROR

/* #include <sys/socket.h> */
/* int closesocket(int); */
/* int ioctlsocket(int, long, char *); */

/* #include <sys/select.h> */
/* int _multicast_on; */
/* int select_s(int, fd_set *, fd_set *, fd_set *, struct timeval *); */
/* void sock_exit(void); */
/* int sock_init(void); */

/* #include <stdlib.h> */
/* int atexit(void (*)(void)); */

#define JCGO_SOCKGETERR_NOTERRNO
#define JCGO_SOCK_GETERROR(x) (errno + 0)

#define JCGO_SOCKINIT_T int
#define JCGO_SOCK_INIT(pdata) ((*(pdata) = (_multicast_on = 1, sock_init())) != 0 ? -1 : (atexit(jcgo_sockCleanup), 0))
#define JCGO_SOCK_CLEANUP(x) sock_exit()
#define JCGO_SOCK_CLOSE(fd, perrcode) (closesocket(fd) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_IOCTL(fd, cmd, val, perrcode) (ioctlsocket(fd, cmd, (void *)&(val)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SELECT(nfds, readfds, writefds, exceptfds, ptv, perrcode) ((*(perrcode) = select_s(nfds, readfds, writefds, exceptfds, ptv)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#endif /* ! JCGO_SOCK_GETERROR */

#ifndef JCGO_SOCKFIONBIO_T
#define JCGO_SOCKFIONBIO_T unsigned
#endif

#endif /* ! JCGO_WIN32 */

#define JCGO_SOCKCALL_BEGIN(pJniEnv) {
#define JCGO_SOCKCALL_END(pJniEnv) }

#ifndef JCGO_SOCKHOSTNAME_MAXSIZE
#define JCGO_SOCKHOSTNAME_MAXSIZE 0x100
#endif

#ifndef JCGO_SOCKFIONREAD_T
#define JCGO_SOCKFIONREAD_T JCGO_SOCKFIONBIO_T /* or size_t */
#endif

#ifndef JCGO_SOCK_SETCLOEXEC
#define JCGO_SOCK_SETCLOEXEC(fd) 0
#endif

#ifdef _UNISTD_NETDB_NO_GETHOSTNAME
#define JCGO_SOCK_GETHOSTNAME(name, len) -1
#else
#define JCGO_SOCK_GETHOSTNAME(name, len) gethostname(name, len)
#endif

#define JCGO_SOCK_SOCKET(family, type, proto, perrcode) ((*(perrcode) = (int)socket(family, type, proto)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#define JCGO_SOCK_ACCEPT(fd, saddr, perrcode) (*(perrcode) = (int)sizeof(saddr), (*(perrcode) = (int)accept(fd, (void *)&(saddr), (void *)(perrcode))) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_BIND(fd, psaddr, saddrsize, perrcode) (bind(fd, (struct sockaddr *)(psaddr), saddrsize) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_CONNECT(fd, psaddr, saddrsize, perrcode) (connect(fd, (struct sockaddr *)(psaddr), saddrsize) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_GETSOCKNAME(fd, saddr, perrcode) (*(perrcode) = (int)sizeof(saddr), getsockname(fd, (void *)&(saddr), (void *)(perrcode)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_LISTEN(fd, backlog, perrcode) (listen(fd, backlog) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SHUTDOWN(fd, howto, perrcode) (shutdown(fd, howto) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#define JCGO_SOCK_RECVFROM(fd, buf, len, flags, psaddr, saddrsize, perrcode) (*(perrcode) = (int)(saddrsize), (*(perrcode) = (int)recvfrom(fd, buf, len, flags, (struct sockaddr *)(psaddr), (void *)(perrcode))) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SENDTO(fd, buf, len, flags, psaddr, saddrsize, perrcode) ((*(perrcode) = (int)sendto(fd, buf, len, flags, (struct sockaddr *)(psaddr), saddrsize)) != -1 ? *(perrcode) : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_GETSOCKOPT(fd, level, optid, pval, valsize, perrcode) (*(perrcode) = (int)(valsize), getsockopt(fd, level, optid, (void *)(pval), (void *)(perrcode)) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))
#define JCGO_SOCK_SETSOCKOPT(fd, level, optid, pval, valsize, perrcode) (setsockopt(fd, level, optid, (void *)(pval), valsize) != -1 ? 0 : (*(perrcode) = JCGO_SOCK_GETERROR(0), -1))

#ifndef JCGO_SOCKLLINGER_T
#define JCGO_SOCKLLINGER_T unsigned short
#endif

#ifndef JCGO_SOCKTVSEC_T
#ifdef JCGO_WIN32
#define JCGO_SOCKTVSEC_T long
#else
#define JCGO_SOCKTVSEC_T time_t
#endif
#endif

#ifndef JCGO_TVSUSECONDS_T
#ifdef _SUSECONDS_T
#define JCGO_TVSUSECONDS_T suseconds_t
#else
#ifdef _SUSECONDS_T_DECLARED
#define JCGO_TVSUSECONDS_T suseconds_t
#else
#ifdef _SUSECONDS_T_DEFINED
#define JCGO_TVSUSECONDS_T suseconds_t
#else
#ifdef __suseconds_t_defined
#define JCGO_TVSUSECONDS_T suseconds_t
#else
#define JCGO_TVSUSECONDS_T long
#endif
#endif
#endif
#endif
#endif

#ifndef SOCESOCKTNOSUPPORT

#ifdef WSAESOCKTNOSUPPORT

#define SOCEACCES WSAEACCES
#define SOCEADDRINUSE WSAEADDRINUSE
#define SOCEADDRNOTAVAIL WSAEADDRNOTAVAIL
#define SOCEAFNOSUPPORT WSAEAFNOSUPPORT
#define SOCEALREADY WSAEALREADY
#define SOCEBADF WSAEBADF
#define SOCECONNABORTED WSAECONNABORTED
#define SOCECONNREFUSED WSAECONNREFUSED
#define SOCECONNRESET WSAECONNRESET
#define SOCEDESTADDRREQ WSAEDESTADDRREQ
#define SOCEHOSTDOWN WSAEHOSTDOWN
#define SOCEHOSTUNREACH WSAEHOSTUNREACH
#define SOCEINPROGRESS WSAEINPROGRESS
#define SOCEINTR WSAEINTR
#define SOCEINVAL WSAEINVAL
#define SOCEISCONN WSAEISCONN
#define SOCEMFILE WSAEMFILE
#define SOCEMSGSIZE WSAEMSGSIZE
#define SOCENETDOWN WSAENETDOWN
#define SOCENETRESET WSAENETRESET
#define SOCENETUNREACH WSAENETUNREACH
#define SOCENOBUFS WSAENOBUFS
#define SOCENOPROTOOPT WSAENOPROTOOPT
#define SOCENOTCONN WSAENOTCONN
#define SOCENOTSOCK WSAENOTSOCK
#define SOCEOPNOTSUPP WSAEOPNOTSUPP
#define SOCEPFNOSUPPORT WSAEPFNOSUPPORT
#define SOCEPROTONOSUPPORT WSAEPROTONOSUPPORT
#define SOCEPROTOTYPE WSAEPROTOTYPE
#define SOCESHUTDOWN WSAESHUTDOWN
#define SOCESOCKTNOSUPPORT WSAESOCKTNOSUPPORT
#define SOCETIMEDOUT WSAETIMEDOUT
#define SOCETOOMANYREFS WSAETOOMANYREFS
#define SOCEWOULDBLOCK WSAEWOULDBLOCK

#else /* WSAESOCKTNOSUPPORT */

#ifndef EACCES
#define EACCES 12345
#endif
#ifndef EAGAIN
#define EAGAIN 12346
#endif
#ifndef EBADF
#define EBADF 12347
#endif
#ifndef EINTR
#define EINTR 12350
#endif
#ifndef EINVAL
#define EINVAL 12351
#endif
#ifndef EMFILE
#define EMFILE 12352
#endif

#ifndef EADDRINUSE
#define EADDRINUSE 12456
#endif
#ifndef EADDRNOTAVAIL
#define EADDRNOTAVAIL 12457
#endif
#ifndef EAFNOSUPPORT
#define EAFNOSUPPORT 12458
#endif
#ifndef EALREADY
#define EALREADY 12459
#endif
#ifndef ECONNABORTED
#define ECONNABORTED 12460
#endif
#ifndef ECONNREFUSED
#define ECONNREFUSED 12461
#endif
#ifndef ECONNRESET
#define ECONNRESET 12462
#endif
#ifndef EDESTADDRREQ
#define EDESTADDRREQ 12463
#endif
#ifndef EHOSTDOWN
#define EHOSTDOWN 12464
#endif
#ifndef EHOSTUNREACH
#define EHOSTUNREACH 12465
#endif
#ifndef EINPROGRESS
#define EINPROGRESS 12466
#endif
#ifndef EISCONN
#define EISCONN 12467
#endif
#ifndef EMSGSIZE
#define EMSGSIZE 12468
#endif
#ifndef ENETDOWN
#define ENETDOWN 12469
#endif
#ifndef ENETRESET
#define ENETRESET 12470
#endif
#ifndef ENETUNREACH
#define ENETUNREACH 12471
#endif
#ifndef ENOBUFS
#define ENOBUFS 12472
#endif
#ifndef ENOPROTOOPT
#define ENOPROTOOPT 12473
#endif
#ifndef ENOTCONN
#define ENOTCONN 12474
#endif
#ifndef ENOTSOCK
#define ENOTSOCK 12475
#endif
#ifndef EOPNOTSUPP
#define EOPNOTSUPP 12476
#endif
#ifndef EPFNOSUPPORT
#define EPFNOSUPPORT 12477
#endif
#ifndef EPROTONOSUPPORT
#define EPROTONOSUPPORT 12478
#endif
#ifndef EPROTOTYPE
#define EPROTOTYPE 12479
#endif
#ifndef ESHUTDOWN
#define ESHUTDOWN 12480
#endif
#ifndef ESOCKTNOSUPPORT
#define ESOCKTNOSUPPORT 12481
#endif
#ifndef ETIMEDOUT
#define ETIMEDOUT 12482
#endif
#ifndef ETOOMANYREFS
#define ETOOMANYREFS 12483
#endif

#ifndef EWOULDBLOCK
#define EWOULDBLOCK EAGAIN
#endif

#define SOCEACCES EACCES
#define SOCEADDRINUSE EADDRINUSE
#define SOCEADDRNOTAVAIL EADDRNOTAVAIL
#define SOCEAFNOSUPPORT EAFNOSUPPORT
#define SOCEALREADY EALREADY
#define SOCEBADF EBADF
#define SOCECONNABORTED ECONNABORTED
#define SOCECONNREFUSED ECONNREFUSED
#define SOCECONNRESET ECONNRESET
#define SOCEDESTADDRREQ EDESTADDRREQ
#define SOCEHOSTDOWN EHOSTDOWN
#define SOCEHOSTUNREACH EHOSTUNREACH
#define SOCEINPROGRESS EINPROGRESS
#define SOCEINTR EINTR
#define SOCEINVAL EINVAL
#define SOCEISCONN EISCONN
#define SOCEMFILE EMFILE
#define SOCEMSGSIZE EMSGSIZE
#define SOCENETDOWN ENETDOWN
#define SOCENETRESET ENETRESET
#define SOCENETUNREACH ENETUNREACH
#define SOCENOBUFS ENOBUFS
#define SOCENOPROTOOPT ENOPROTOOPT
#define SOCENOTCONN ENOTCONN
#define SOCENOTSOCK ENOTSOCK
#define SOCEOPNOTSUPP EOPNOTSUPP
#define SOCEPFNOSUPPORT EPFNOSUPPORT
#define SOCEPROTONOSUPPORT EPROTONOSUPPORT
#define SOCEPROTOTYPE EPROTOTYPE
#define SOCESHUTDOWN ESHUTDOWN
#define SOCESOCKTNOSUPPORT ESOCKTNOSUPPORT
#define SOCETIMEDOUT ETIMEDOUT
#define SOCETOOMANYREFS ETOOMANYREFS
#define SOCEWOULDBLOCK EWOULDBLOCK

#endif /* ! WSAESOCKTNOSUPPORT */

#endif /* ! SOCESOCKTNOSUPPORT */

#ifndef AF_UNSPEC
#define AF_UNSPEC 0
#endif

#ifndef AF_INET
#define AF_INET 2
#endif

#ifndef INADDR_ANY
#define INADDR_ANY ((unsigned)0)
#endif

#ifndef SOCK_STREAM
#define SOCK_STREAM 1
#endif

#ifndef SOCK_DGRAM
#define SOCK_DGRAM 2
#endif

#ifndef IPPROTO_IP
#define IPPROTO_IP 0
#endif

#ifndef IPPROTO_TCP
#define IPPROTO_TCP 6
#endif

#ifndef IP_TOS
#ifdef JCGO_OLDWSOCK
#define IP_TOS 8
#else
#define IP_TOS 3 /* or 1 */
#endif
#endif

#ifndef IP_MULTICAST_IF
#ifdef JCGO_OLDWSOCK
#define IP_MULTICAST_IF 2
#else
#define IP_MULTICAST_IF 9 /* or 16, or 32 */
#endif
#endif

#ifndef IP_MULTICAST_TTL
#define IP_MULTICAST_TTL (IP_MULTICAST_IF + 1)
#endif

#ifndef IP_MULTICAST_LOOP
#define IP_MULTICAST_LOOP (IP_MULTICAST_IF + 2)
#endif

#ifndef IP_ADD_MEMBERSHIP
struct ip_mreq
{
 struct in_addr imr_multiaddr;
 struct in_addr imr_interface;
};
#define IP_ADD_MEMBERSHIP (IP_MULTICAST_IF + 3)
#endif

#ifndef IP_DROP_MEMBERSHIP
#define IP_DROP_MEMBERSHIP (IP_MULTICAST_IF + 4)
#endif

#ifndef TCP_NODELAY
#define TCP_NODELAY 0x1
#endif

#ifndef SOL_SOCKET
#define SOL_SOCKET 0xffff /* or 1 */
#endif

#ifndef SO_REUSEADDR
#define SO_REUSEADDR 0x4 /* or 2 */
#endif

#ifndef SO_KEEPALIVE
#define SO_KEEPALIVE 0x8 /* or 9 */
#endif

#ifndef SO_BROADCAST
#define SO_BROADCAST 0x20 /* or 6 */
#endif

#ifndef SO_LINGER
#define SO_LINGER 0x80 /* or 13 */
#endif

#ifndef SO_OOBINLINE
#define SO_OOBINLINE 0x100 /* or 10 */
#endif

#ifndef SO_SNDBUF
#define SO_SNDBUF 0x1001 /* or 7 */
#endif

#ifndef SO_RCVBUF
#define SO_RCVBUF 0x1002 /* or 8 */
#endif

#ifndef MSG_OOB
#define MSG_OOB 0x1
#endif

#ifndef MSG_PEEK
#define MSG_PEEK 0x2
#endif

#ifndef SD_RECEIVE
#ifdef SHUT_RD
#define SD_RECEIVE SHUT_RD
#else
#define SD_RECEIVE 0
#endif
#endif

#ifndef SD_SEND
#ifdef SHUT_WR
#define SD_SEND SHUT_WR
#else
#define SD_SEND 1
#endif
#endif

#ifndef SD_BOTH
#ifdef SHUT_RDWR
#define SD_BOTH SHUT_RDWR
#else
#define SD_BOTH 2
#endif
#endif

#ifdef AF_INET6
#ifdef IN6ADDR_ANY_INIT
#define JCGO_IPV6_PRESENT
#else
#ifdef USE_IPV6
#define JCGO_IPV6_PRESENT
#else
#ifdef s6_addr
#define JCGO_IPV6_PRESENT
#endif
#endif
#endif
#endif

#ifdef JCGO_IPV6_PRESENT

#ifndef IPPROTO_IPV6
#define IPPROTO_IPV6 41
#endif

#ifndef IPV6_MULTICAST_IF
#define IPV6_MULTICAST_IF IP_MULTICAST_IF /* or 6, or 17 */
#endif

#ifndef IPV6_JOIN_GROUP
#ifdef IPV6_ADD_MEMBERSHIP
#define IPV6_JOIN_GROUP IPV6_ADD_MEMBERSHIP
#else
#define IPV6_JOIN_GROUP (IPV6_MULTICAST_IF + 3)
#endif
#endif

#ifndef IPV6_LEAVE_GROUP
#ifdef IPV6_DROP_MEMBERSHIP
#define IPV6_LEAVE_GROUP IPV6_DROP_MEMBERSHIP
#else
#define IPV6_LEAVE_GROUP (IPV6_MULTICAST_IF + 4)
#endif
#endif

#endif /* JCGO_IPV6_PRESENT */

#endif

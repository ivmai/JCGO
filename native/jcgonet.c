/*
 * @(#) $(JCGO)/native/jcgonet.c --
 * a part of the JCGO native layer library (network I/O impl).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_INET, JCGO_UNIX.
 * Macros for tuning: CLIBDECL, STATIC.
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

#ifndef JCGO_BUILDING_JNU
#include "jcgojnu.h"
#endif

#ifdef JCGO_VER

#ifdef JCGO_INET

#include "jcgonet.h"

#define JCGO_JSOCKOPT_NODELAY 0x1
#define JCGO_JSOCKOPT_TOS 0x3
#define JCGO_JSOCKOPT_REUSEADDR 0x4
#define JCGO_JSOCKOPT_KEEPALIVE 0x8
#define JCGO_JSOCKOPT_LOOP 0x12
#define JCGO_JSOCKOPT_BROADCAST 0x20
#define JCGO_JSOCKOPT_LINGER 0x80
#define JCGO_JSOCKOPT_SNDBUF 0x1001
#define JCGO_JSOCKOPT_RCVBUF 0x1002
#define JCGO_JSOCKOPT_OOBINLINE 0x1003
#define JCGO_JSOCKOPT_TTL 0x1e61

#ifndef STATIC
#define STATIC static
#endif

union jcgo_sockaddr_u
{
 struct sockaddr_in in;
#ifdef JCGO_IPV6_PRESENT
 struct sockaddr_in6 in6;
#endif
};

#ifdef JCGO_SOCK_CLEANUP

#ifndef CLIBDECL
#ifdef __CLIB
#define CLIBDECL __CLIB
#else
#ifdef _USERENTRY
#define CLIBDECL _USERENTRY
#else
#ifdef _RTL_FUNC
#define CLIBDECL _RTL_FUNC
#else
#ifdef JCGO_UNIX
#define CLIBDECL /* empty */
#else
#define CLIBDECL __cdecl
#endif
#endif
#endif
#endif
#endif

STATIC void CLIBDECL jcgo_sockCleanup(void)
{
 JCGO_SOCK_CLEANUP(0);
}

#endif /* JCGO_SOCK_CLEANUP */

STATIC int jcgo_sockAddrFill( union jcgo_sockaddr_u *psaddr, char *address,
 int iplen, unsigned short port )
{
 int i;
 if ((unsigned)iplen == sizeof(psaddr->in.sin_addr))
 {
  i = (int)sizeof(psaddr->in);
  while (i-- > 0)
   *((volatile char *)psaddr + i) = '\0';
  psaddr->in.sin_family = AF_INET;
  psaddr->in.sin_port = htons(port);
  i = (int)sizeof(psaddr->in.sin_addr);
  while (i-- > 0)
   *((volatile char *)&psaddr->in.sin_addr + i) = *(address + i);
  return (int)sizeof(psaddr->in);
 }
#ifdef JCGO_IPV6_PRESENT
 if ((unsigned)iplen == sizeof(psaddr->in6.sin6_addr))
 {
  i = (int)sizeof(psaddr->in6);
  while (i-- > 0)
   *((volatile char *)psaddr + i) = '\0';
  psaddr->in6.sin6_family = AF_INET6;
  psaddr->in6.sin6_port = htons(port);
  i = (int)sizeof(psaddr->in6.sin6_addr);
  while (i-- > 0)
   *((volatile char *)&psaddr->in6.sin6_addr + i) = *(address + i);
  return (int)sizeof(psaddr->in6);
 }
#endif
 return 0;
}

STATIC int jcgo_sockAddrDecode( union jcgo_sockaddr_u *psaddr, char *address,
 int ipmaxlen, unsigned short *pport )
{
 int i;
 if (psaddr->in.sin_family == AF_INET)
 {
  *pport = (unsigned short)ntohs(psaddr->in.sin_port);
  i = (int)sizeof(psaddr->in.sin_addr);
  if (i >= ipmaxlen)
   i = ipmaxlen;
  while (i-- > 0)
   *(address + i) = *((volatile char *)&psaddr->in.sin_addr + i);
  return (int)sizeof(psaddr->in.sin_addr);
 }
#ifdef JCGO_IPV6_PRESENT
 if (psaddr->in6.sin6_family == AF_INET6)
 {
  *pport = (unsigned short)ntohs(psaddr->in6.sin6_port);
  i = (int)sizeof(psaddr->in6.sin6_addr);
  if (i >= ipmaxlen)
   i = ipmaxlen;
  while (i-- > 0)
   *(address + i) = *((volatile char *)&psaddr->in6.sin6_addr + i);
  return (int)sizeof(psaddr->in6.sin6_addr);
 }
#endif
 return 0;
}

#endif /* JCGO_INET */

#ifndef NOJAVA_java_net_VMInetAddress_lookupInaddrAny0
JCGO_JNI_EXPF(jint,
Java_java_net_VMInetAddress_lookupInaddrAny0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (jint)INADDR_ANY;
#else
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_net_VMInetAddress_getLocalHostname0
JCGO_JNI_EXPF(jstring,
Java_java_net_VMInetAddress_getLocalHostname0)( JNIEnv *pJniEnv, jclass This )
{
#ifdef JCGO_INET
 int res;
 char cbuf[JCGO_SOCKHOSTNAME_MAXSIZE];
 JCGO_UNUSED_VAR(This);
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_GETHOSTNAME(cbuf, sizeof(cbuf) - 1);
 JCGO_SOCKCALL_END(pJniEnv)
 cbuf[res != -1 ? sizeof(cbuf) - 1 : 0] = '\0';
 return jcgo_JnuNewStringPlatform(pJniEnv, cbuf);
#else
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketsInit0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketsInit0)( JNIEnv *pJniEnv,
 jclass This )
{
#ifdef JCGO_INET
#ifdef JCGO_SOCK_INIT
 int res;
 JCGO_SOCKINIT_T data;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_INIT(&data);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res)
  return -1;
#endif
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return 0;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrAddrNotAvail0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrAddrNotAvail0)(
 JNIEnv *pJniEnv, jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEACCES || (int)res == -SOCEADDRINUSE ||
         (int)res == -SOCEADDRNOTAVAIL ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrConnInProgress0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrConnInProgress0)(
 JNIEnv *pJniEnv, jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEALREADY || (int)res == -SOCEINPROGRESS ||
         (int)res == -SOCEWOULDBLOCK ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrConnRefused0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrConnRefused0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCECONNREFUSED || (int)res == -SOCEHOSTDOWN ||
         (int)res == -SOCETIMEDOUT ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrConnected0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrConnected0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEISCONN ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrHostUnreach0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrHostUnreach0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEHOSTUNREACH || (int)res == -SOCENETDOWN ||
         (int)res == -SOCENETUNREACH ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrInterrupted0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrInterrupted0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEINTR || (int)res == -SOCEINPROGRESS ||
         (int)res == -SOCEWOULDBLOCK ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrNoResources0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrNoResources0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCEADDRINUSE || (int)res == -SOCEMFILE ||
         (int)res == -SOCENOBUFS || (int)res == -SOCETOOMANYREFS ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_isSocketErrResetConn0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_isSocketErrResetConn0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return (int)res == -SOCECONNRESET ? 1 : 0;
#else
 JCGO_UNUSED_VAR(res);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_getSocketErrorMsg0
JCGO_JNI_EXPF(jstring,
Java_gnu_java_net_VMPlainSocketImpl_getSocketErrorMsg0)( JNIEnv *pJniEnv,
 jclass This, jint res )
{
#ifdef JCGO_INET
#ifdef JCGO_SOCKGETERR_NOTERRNO
 char *cstr = NULL;
 switch (-(int)res)
 {
 case SOCEACCES:
  cstr = "Permission denied";
  break;
 case SOCEADDRINUSE:
  cstr = "Address already in use";
  break;
 case SOCEADDRNOTAVAIL:
  cstr = "Cannot assign requested address";
  break;
 case SOCEAFNOSUPPORT:
  cstr = "Address family not supported by protocol family";
  break;
 case SOCEALREADY:
  cstr = "Connection operation already in progress";
  break;
 case SOCEBADF:
  cstr = "Socket closed";
  break;
 case SOCECONNABORTED:
  cstr = "Software caused connection abort";
  break;
 case SOCECONNREFUSED:
  cstr = "Connection refused";
  break;
 case SOCECONNRESET:
  cstr = "Connection reset by peer";
  break;
 case SOCEDESTADDRREQ:
  cstr = "Destination address required";
  break;
 case SOCEHOSTDOWN:
  cstr = "Host is down";
  break;
 case SOCEHOSTUNREACH:
  cstr = "No route to host";
  break;
 case SOCEINPROGRESS:
  cstr = "Blocking operation is in progress now";
  break;
 case SOCEINTR:
  cstr = "Operation is canceled";
  break;
 case SOCEINVAL:
  cstr = "Invalid function argument";
  break;
 case SOCEISCONN:
  cstr = "Socket is already connected";
  break;
 case SOCEMFILE:
  cstr = "Too many open sockets";
  break;
 case SOCEMSGSIZE:
  cstr = "Message too long";
  break;
 case SOCENETDOWN:
  cstr = "Network interface is not configured";
  break;
 case SOCENETRESET:
  cstr = "Network dropped connection on reset";
  break;
 case SOCENETUNREACH:
  cstr = "Network is unreachable";
  break;
 case SOCENOBUFS:
  cstr = "No buffer space available";
  break;
 case SOCENOPROTOOPT:
  cstr = "Bad protocol option";
  break;
 case SOCENOTCONN:
  cstr = "Socket is not connected";
  break;
 case SOCENOTSOCK:
  cstr = "Not a socket";
  break;
 case SOCEOPNOTSUPP:
  cstr = "Operation not supported on transport endpoint";
  break;
 case SOCEPFNOSUPPORT:
  cstr = "Protocol family not supported";
  break;
 case SOCEPROTONOSUPPORT:
  cstr = "Protocol not supported";
  break;
 case SOCEPROTOTYPE:
  cstr = "Protocol wrong type for socket";
  break;
 case SOCESHUTDOWN:
  cstr = "Cannot send after socket shutdown";
  break;
 case SOCESOCKTNOSUPPORT:
  cstr = "Socket type not supported";
  break;
 case SOCETIMEDOUT:
  cstr = "Connection timed out";
  break;
 case SOCETOOMANYREFS:
  cstr = "Too many references to some kernel resource";
  break;
 case SOCEWOULDBLOCK:
  cstr = "Operation would block";
  break;
 }
#else
 char *cstr = (int)res != -SOCEBADF ? strerror(-(int)res) : "Socket closed";
#endif
 JCGO_UNUSED_VAR(This);
 return jcgo_JnuNewStringPlatform(pJniEnv, cstr != NULL ? cstr : "");
#else
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(res);
 return jcgo_JnuNewStringPlatform(pJniEnv, "Sockets not supported");
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketCreate0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketCreate0)( JNIEnv *pJniEnv,
 jclass This, jintArray resArr, jint stream )
{
#ifdef JCGO_INET
 int res;
 int errcode;
#ifdef SO_NOSIGPIPE
 int optval = 1;
#endif
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_SOCKET(AF_INET, (int)stream ? SOCK_STREAM : SOCK_DGRAM, 0,
        &errcode);
#ifdef SO_NOSIGPIPE
 if (res != -1)
  (void)JCGO_SOCK_SETSOCKOPT(res, SOL_SOCKET, SO_NOSIGPIPE, &optval,
   sizeof(optval), &errcode);
#endif
 (void)JCGO_SOCK_SETCLOEXEC(res);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res != -1)
 {
  jcgo_JnuSetIntArrayElement(pJniEnv, resArr, 0, 0);
  return (jint)((unsigned)res);
 }
 jcgo_JnuSetIntArrayElement(pJniEnv, resArr, 0, (jint)-errcode);
#else
 JCGO_UNUSED_VAR(stream);
 jcgo_JnuSetIntArrayElement(pJniEnv, resArr, 0, -1);
#endif
 JCGO_UNUSED_VAR(This);
 return -1;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketBind0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketBind0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint iplen, jint port )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 char *address;
 union jcgo_sockaddr_u saddr;
 JCGO_UNUSED_VAR(This);
 address = (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
 if (address == NULL)
  return 0;
 res = 0;
 if ((int)iplen != 4 || (*(unsigned char *)address & 0x7f) != 0x7f ||
     *((unsigned char *)address + 3) != 0xff)
  res = jcgo_sockAddrFill(&saddr, address, (int)iplen, (unsigned short)port);
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 if (!res)
  return -SOCEAFNOSUPPORT;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_BIND((int)fd, &saddr, (unsigned)res, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 JCGO_UNUSED_VAR(port);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketDisconnect0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketDisconnect0)( JNIEnv *pJniEnv,
 jclass This, jint fd )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 struct sockaddr_in saddr;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 saddr.sin_family = AF_UNSPEC;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_CONNECT((int)fd, &saddr, sizeof(saddr), &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : errcode != SOCEAFNOSUPPORT ? -errcode : 0);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketConnect0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketConnect0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint iplen, jint port )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 char *address;
 union jcgo_sockaddr_u saddr;
 JCGO_UNUSED_VAR(This);
 address = (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
 if (address == NULL)
  return 0;
 res = jcgo_sockAddrFill(&saddr, address, (int)iplen, (unsigned short)port);
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 if (!res)
  return -SOCEAFNOSUPPORT;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_CONNECT((int)fd, &saddr, (unsigned)res, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 JCGO_UNUSED_VAR(port);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketSendTo0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketSendTo0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray buffer, jint off, jint len, jint urgent,
 jbyteArray ip, jint iplen, jint port )
{
#ifdef JCGO_INET
 int res;
 int errcode;
 unsigned buflen;
 char *buf =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, buffer, off, len);
 char *address;
 union jcgo_sockaddr_u saddr;
 JCGO_UNUSED_VAR(This);
 if (buf == NULL)
  return 0;
 res = 0;
 if (iplen > 0)
 {
  address = (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
  if (address == NULL)
  {
   jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
   return 0;
  }
  res = jcgo_sockAddrFill(&saddr, address, (int)iplen, (unsigned short)port);
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
  if (!res)
  {
   jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
   return -SOCEAFNOSUPPORT;
  }
 }
 buflen = ((unsigned)-1) >> 1;
 if ((jint)buflen >= len)
  buflen = (unsigned)len;
 errcode = -(int)((volatile char *)buf - (volatile char *)NULL);
 if (errcode > 0 && (int)buflen > errcode)
 {
  if (off > 0)
  {
   jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
   return 0;
  }
  buflen = (unsigned)errcode;
 }
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_SENDTO((int)fd, buf, buflen, (int)urgent ? MSG_OOB : 0,
        iplen > 0 ? &saddr : NULL, (unsigned)res, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(buffer);
 JCGO_UNUSED_VAR(off);
 JCGO_UNUSED_VAR(len);
 JCGO_UNUSED_VAR(urgent);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 JCGO_UNUSED_VAR(port);
 return -1;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketRecvFrom0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketRecvFrom0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray buffer, jint off, jint len, jint urgent,
 jint peek, jbyteArray ip, jint ipmaxlen, jintArray iplenPortArr )
{
#ifdef JCGO_INET
 int res = (int)(((unsigned)-1) >> 1);
 int errcode;
 unsigned short port = 0;
 char *buf =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, buffer, off, len);
 char *address;
 union jcgo_sockaddr_u saddr;
 JCGO_UNUSED_VAR(This);
 if (buf == NULL)
  return 0;
 if ((jint)res >= len)
  res = (int)len;
 errcode = -(int)((volatile char *)buf - (volatile char *)NULL);
 if (errcode > 0 && res > errcode)
 {
  if (off > 0 && (int)ipmaxlen)
  {
   jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
   return 0;
  }
  res = errcode;
 }
 address = (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0,
            (int)ipmaxlen > 0 ? ipmaxlen : 0);
 if (address == NULL)
 {
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
  return 0;
 }
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_RECVFROM((int)fd, buf, (unsigned)res,
        ((int)urgent ? MSG_OOB : 0) | ((int)peek ? MSG_PEEK : 0),
        (int)ipmaxlen ? &saddr : NULL, (int)ipmaxlen ? sizeof(saddr) : 0,
        &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res == -1 && errcode == SOCEMSGSIZE)
  res = (int)len;
 if (res >= 0)
 {
  if ((int)ipmaxlen > 0)
  {
   jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 0,
    (jint)jcgo_sockAddrDecode(&saddr, address, (int)ipmaxlen, &port));
   jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 1, (jint)port);
  }
 }
  else res = -errcode;
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, buffer, (jbyte *)buf, off);
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(buffer);
 JCGO_UNUSED_VAR(off);
 JCGO_UNUSED_VAR(len);
 JCGO_UNUSED_VAR(urgent);
 JCGO_UNUSED_VAR(peek);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(ipmaxlen);
 JCGO_UNUSED_VAR(iplenPortArr);
 return -1;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketAccept0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketAccept0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint ipmaxlen, jintArray iplenPortArr )
{
#ifdef JCGO_INET
 int res;
 int errcode;
#ifdef SO_NOSIGPIPE
 int optval = 1;
#endif
 unsigned short port = 0;
 char *address =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, ipmaxlen);
 union jcgo_sockaddr_u saddr;
 if (address == NULL)
  return -1;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_ACCEPT((int)fd, saddr, &errcode);
#ifdef SO_NOSIGPIPE
 if (res != -1)
  (void)JCGO_SOCK_SETSOCKOPT(res, SOL_SOCKET, SO_NOSIGPIPE, &optval,
   sizeof(optval), &errcode);
#endif
 (void)JCGO_SOCK_SETCLOEXEC(res);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res != -1)
 {
  jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 0,
   (jint)jcgo_sockAddrDecode(&saddr, address, (int)ipmaxlen, &port));
  jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 1, (jint)port);
 }
  else jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 0, (jint)-errcode);
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 if (res != -1)
  return (jint)((unsigned)res);
#else
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(ipmaxlen);
 JCGO_UNUSED_VAR(iplenPortArr);
 jcgo_JnuSetIntArrayElement(pJniEnv, iplenPortArr, 0, -1);
#endif
 JCGO_UNUSED_VAR(This);
 return -1;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketGetLocalAddrPort0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketGetLocalAddrPort0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint ipmaxlen, jintArray portArr )
{
#ifdef JCGO_INET
 int res;
 int errcode;
 unsigned short port = 0;
 char *address =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, ipmaxlen);
 union jcgo_sockaddr_u saddr;
 JCGO_UNUSED_VAR(This);
 if (address == NULL)
  return 0;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_GETSOCKNAME((int)fd, saddr, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res >= 0)
 {
  res = jcgo_sockAddrDecode(&saddr, address, (int)ipmaxlen, &port);
  jcgo_JnuSetIntArrayElement(pJniEnv, portArr, 0, (jint)port);
 }
  else res = -errcode;
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(ipmaxlen);
 JCGO_UNUSED_VAR(portArr);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketSelect0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketSelect0)( JNIEnv *pJniEnv,
 jclass This, jintArray readFDs, jint readFDsLen, jintArray writeFDs,
 jint writeFDsLen, jintArray exceptFDs, jint exceptFDsLen, jint timeout )
{
#ifdef JCGO_INET
#ifdef FD_SET
 jint fd;
 int res;
 int errcode;
 int i;
 int maxfd = 0;
 struct timeval tv;
 fd_set fdsread;
 fd_set fdswrite;
 fd_set fdsexcept;
 FD_ZERO(&fdsread);
 FD_ZERO(&fdswrite);
 FD_ZERO(&fdsexcept);
 tv.tv_sec = 0;
 tv.tv_usec = 0;
 for (i = 0; i < (int)readFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, readFDs, i);
  if (fd != -1)
  {
   FD_SET((unsigned)fd, &fdsread);
   if (maxfd <= (int)fd)
    maxfd = (int)fd;
  }
 }
 for (i = 0; i < (int)writeFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, writeFDs, i);
  if (fd != -1)
  {
   FD_SET((unsigned)fd, &fdswrite);
   if (maxfd <= (int)fd)
    maxfd = (int)fd;
  }
 }
 for (i = 0; i < (int)exceptFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, exceptFDs, i);
  if (fd != -1)
  {
   FD_SET((unsigned)fd, &fdsexcept);
   if (maxfd <= (int)fd)
    maxfd = (int)fd;
  }
 }
 if (timeout > 0)
 {
  tv.tv_sec = (JCGO_SOCKTVSEC_T)(timeout / (jint)1000L);
  tv.tv_usec = (JCGO_TVSUSECONDS_T)(timeout % (jint)1000L) *
                (JCGO_TVSUSECONDS_T)1000L;
 }
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_SELECT(maxfd + 1, &fdsread, &fdswrite, &fdsexcept,
        timeout >= 0 ? (void *)&tv : NULL, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res <= 0)
  return (jint)(res < 0 ? -errcode : 0);
 for (i = 0; i < (int)readFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, readFDs, i);
  if (fd != -1 && !FD_ISSET((unsigned)fd, &fdsread))
   jcgo_JnuSetIntArrayElement(pJniEnv, readFDs, i, -1);
 }
 for (i = 0; i < (int)writeFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, writeFDs, i);
  if (fd != -1 && !FD_ISSET((unsigned)fd, &fdswrite))
   jcgo_JnuSetIntArrayElement(pJniEnv, writeFDs, i, -1);
 }
 for (i = 0; i < (int)exceptFDsLen; i++)
 {
  fd = jcgo_JnuGetIntArrayElement(pJniEnv, exceptFDs, i);
  if (fd != -1 && !FD_ISSET((unsigned)fd, &fdsexcept))
   jcgo_JnuSetIntArrayElement(pJniEnv, exceptFDs, i, -1);
 }
#endif
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(readFDs);
 JCGO_UNUSED_VAR(readFDsLen);
 JCGO_UNUSED_VAR(writeFDs);
 JCGO_UNUSED_VAR(writeFDsLen);
 JCGO_UNUSED_VAR(exceptFDs);
 JCGO_UNUSED_VAR(exceptFDsLen);
 JCGO_UNUSED_VAR(timeout);
 return 1;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketSetNonBlocking0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketSetNonBlocking0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jint on )
{
#ifdef JCGO_INET
#ifdef FIONBIO
 JCGO_SOCKFIONBIO_T value = 0;
 int res;
 int errcode = 0;
 if ((int)on)
  value++;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_IOCTL((int)fd, FIONBIO, value, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res)
  return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(on);
#endif
#else
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(on);
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return 0;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketAvailable0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketAvailable0)( JNIEnv *pJniEnv,
 jclass This, jint fd )
{
#ifdef JCGO_INET
#ifdef FIONREAD
 JCGO_SOCKFIONREAD_T value = 0;
 int res;
 int errcode = 0;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_IOCTL((int)fd, FIONREAD, value, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 if (res < 0)
  return (jint)-errcode;
 if ((jint)value > 0)
  return (jint)value;
#else
 JCGO_UNUSED_VAR(fd);
#endif
#else
 JCGO_UNUSED_VAR(fd);
#endif
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 return 0;
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketListen0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketListen0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jint backlog )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_LISTEN((int)fd, backlog < (jint)(((unsigned)-1) >> 1) ?
        (int)backlog : (int)(((unsigned)-1) >> 1) - 1, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(backlog);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketGetSetOption0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketGetSetOption0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jint optionId, jint optval )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 int optionid;
 int level = (int)SOL_SOCKET;
 struct linger lingval;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 switch ((int)optionId)
 {
 case JCGO_JSOCKOPT_NODELAY:
  level = IPPROTO_TCP;
  optionid = TCP_NODELAY;
  break;
 case JCGO_JSOCKOPT_TOS:
  level = IPPROTO_IP;
  optionid = IP_TOS;
  break;
 case JCGO_JSOCKOPT_REUSEADDR:
  optionid = SO_REUSEADDR;
  break;
 case JCGO_JSOCKOPT_KEEPALIVE:
  optionid = SO_KEEPALIVE;
  break;
 case JCGO_JSOCKOPT_LOOP:
  level = IPPROTO_IP;
  optionid = IP_MULTICAST_LOOP;
  break;
 case JCGO_JSOCKOPT_BROADCAST:
  optionid = SO_BROADCAST;
  break;
 case JCGO_JSOCKOPT_LINGER:
  optionid = SO_LINGER;
  break;
 case JCGO_JSOCKOPT_SNDBUF:
  optionid = SO_SNDBUF;
  break;
 case JCGO_JSOCKOPT_RCVBUF:
  optionid = SO_RCVBUF;
  break;
 case JCGO_JSOCKOPT_OOBINLINE:
  optionid = SO_OOBINLINE;
  break;
 case JCGO_JSOCKOPT_TTL:
  level = IPPROTO_IP;
  optionid = IP_MULTICAST_TTL;
  break;
 default:
  return -SOCEINVAL;
 }
 if (optval >= 0)
 {
  if ((int)optionId == JCGO_JSOCKOPT_LINGER)
  {
   res = (int)sizeof(lingval);
   while (res-- > 0)
    *((volatile char *)&lingval + res) = '\0';
   if (optval > 0)
   {
    lingval.l_onoff = (unsigned char)1;
    lingval.l_linger = (JCGO_SOCKLLINGER_T)(optval - 1);
   }
   JCGO_SOCKCALL_BEGIN(pJniEnv)
   res = JCGO_SOCK_SETSOCKOPT((int)fd, level, optionid, &lingval,
          sizeof(lingval), &errcode);
   JCGO_SOCKCALL_END(pJniEnv)
  }
   else
   {
    JCGO_SOCKCALL_BEGIN(pJniEnv)
    res = JCGO_SOCK_SETSOCKOPT((int)fd, level, optionid, &optval,
           sizeof(optval), &errcode);
    JCGO_SOCKCALL_END(pJniEnv)
    if (res < 0 && ((errcode == SOCEINVAL &&
        (int)optionId == JCGO_JSOCKOPT_TOS) ||
        (errcode == SOCENOPROTOOPT && ((int)optionId == JCGO_JSOCKOPT_TOS ||
        (int)optionId == JCGO_JSOCKOPT_LOOP))))
     res = 0;
   }
  optval = (jint)res;
 }
  else
  {
   optval = 0;
   JCGO_SOCKCALL_BEGIN(pJniEnv)
   if ((int)optionId == JCGO_JSOCKOPT_LINGER)
   {
    res = JCGO_SOCK_GETSOCKOPT((int)fd, level, optionid, &lingval,
           sizeof(lingval), &errcode);
    if (lingval.l_onoff)
     optval = (jint)lingval.l_linger + 1;
   }
    else res = JCGO_SOCK_GETSOCKOPT((int)fd, level, optionid, &optval,
                sizeof(optval), &errcode);
   JCGO_SOCKCALL_END(pJniEnv)
   if (res < 0 && errcode == SOCENOPROTOOPT &&
       (int)optionId == JCGO_JSOCKOPT_TOS)
    res = 0;
   if (optval <= 0)
    optval = 0;
  }
 return res >= 0 ? optval : (jint)-errcode;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(optionId);
 JCGO_UNUSED_VAR(optval);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketSetMulticastAddr0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketSetMulticastAddr0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint iplen )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 int level = IPPROTO_IP;
 int optionid = IP_MULTICAST_IF;
 char *address =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
 JCGO_UNUSED_VAR(This);
 if (address == NULL)
  return 0;
#ifdef JCGO_IPV6_PRESENT
 if ((int)iplen == 16)
 {
  level = IPPROTO_IPV6;
  optionid = IPV6_MULTICAST_IF;
 }
#endif
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_SETSOCKOPT((int)fd, level, optionid, address,
        (unsigned)iplen, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketGetMulticastAddr0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketGetMulticastAddr0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint ipmaxlen )
{
#ifdef JCGO_INET
 int res;
 int errcode;
 char *address =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, ipmaxlen);
 JCGO_UNUSED_VAR(This);
 if (address == NULL)
  return 0;
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_GETSOCKOPT((int)fd, IPPROTO_IP, IP_MULTICAST_IF, address,
        (unsigned)ipmaxlen, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 return (jint)(res >= 0 ? errcode : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(ipmaxlen);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketMulticastGroup0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketMulticastGroup0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jbyteArray ip, jint iplen, jbyteArray ipNetIf,
 jint iplenNetIf, jint join )
{
#ifdef JCGO_INET
 int res = -SOCEAFNOSUPPORT;
 int errcode = 0;
 int level = IPPROTO_IP;
 int optionid;
 int i;
 char *address;
 struct ip_mreq mreq;
 JCGO_UNUSED_VAR(This);
 if (iplen <= (jint)sizeof(mreq.imr_multiaddr) &&
     iplenNetIf <= (jint)sizeof(mreq.imr_interface))
 {
  address = (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
  if (address == NULL)
   return 0;
  i = (int)iplen;
  while (i-- > 0)
   *((volatile char *)&mreq.imr_multiaddr + i) = *(address + i);
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
  address =
   (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ipNetIf, 0, iplenNetIf);
  if (address == NULL)
   return 0;
  i = (int)iplenNetIf;
  while (i-- > 0)
   *((volatile char *)&mreq.imr_interface + i) = *(address + i);
  jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ipNetIf, (jbyte *)address, 0);
  optionid = (int)join ? IP_ADD_MEMBERSHIP : IP_DROP_MEMBERSHIP;
#ifdef JCGO_IPV6_PRESENT
  if ((int)iplen == 16)
  {
   level = IPPROTO_IPV6;
   optionid = (int)join ? IPV6_JOIN_GROUP : IPV6_LEAVE_GROUP;
  }
#endif
  JCGO_SOCKCALL_BEGIN(pJniEnv)
  res = JCGO_SOCK_SETSOCKOPT((int)fd, level, optionid, &mreq, sizeof(mreq),
         &errcode);
  JCGO_SOCKCALL_END(pJniEnv)
  if (res < 0)
   res = -errcode;
 }
 return (jint)res;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 JCGO_UNUSED_VAR(ipNetIf);
 JCGO_UNUSED_VAR(iplenNetIf);
 JCGO_UNUSED_VAR(join);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketShutdown0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketShutdown0)( JNIEnv *pJniEnv,
 jclass This, jint fd, jint input, jint output )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_SHUTDOWN((int)fd, (int)input ?
        ((int)output ? SD_BOTH : SD_RECEIVE) : SD_SEND, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res >= 0 ? res : -errcode);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 JCGO_UNUSED_VAR(input);
 JCGO_UNUSED_VAR(output);
 return 0;
#endif
}
#endif

#ifndef NOJAVA_gnu_java_net_VMPlainSocketImpl_socketClose0
JCGO_JNI_EXPF(jint,
Java_gnu_java_net_VMPlainSocketImpl_socketClose0)( JNIEnv *pJniEnv,
 jclass This, jint fd )
{
#ifdef JCGO_INET
 int res;
 int errcode = 0;
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_SOCKCALL_BEGIN(pJniEnv)
 res = JCGO_SOCK_CLOSE((int)fd, &errcode);
 JCGO_SOCKCALL_END(pJniEnv)
 return (jint)(res < 0 && errcode != SOCEBADF && errcode != SOCECONNRESET &&
         errcode != SOCENOTCONN ? -errcode : 0);
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(fd);
 return 0;
#endif
}
#endif

#endif

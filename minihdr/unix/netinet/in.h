/*
 * @(#) $(JCGO)/minihdr/unix/netinet/in.h --
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

#ifndef _NETINET_IN_H
#define _NETINET_IN_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#define IPPROTO_IP 0
#define IPPROTO_TCP 6

#define INADDR_ANY ((unsigned)0)

#ifndef IP_TOS
#define IP_TOS 3 /* or 1 */
#endif

#ifndef IP_MULTICAST_IF
#define IP_MULTICAST_IF 9 /* or 16, or 32 */
#endif

#define IP_MULTICAST_TTL (IP_MULTICAST_IF + 1)
#define IP_MULTICAST_LOOP (IP_MULTICAST_IF + 2)
#define IP_ADD_MEMBERSHIP (IP_MULTICAST_IF + 3)
#define IP_DROP_MEMBERSHIP (IP_MULTICAST_IF + 4)

#ifndef _IN_ADDR_DEFINED
#define _IN_ADDR_DEFINED
struct in_addr
{
 uint32_t s_addr;
};
#define s_addr s_addr
#endif

struct sockaddr_in
{
 unsigned char sin_len; /* unused */
 uint16_t sin_family;
 uint16_t sin_port;
 struct in_addr sin_addr;
 char sin_zero[8]; /* unused */
};

struct ip_mreq
{
 struct in_addr imr_multiaddr;
 struct in_addr imr_interface;
};

#ifndef IPPROTO_IPV6
#define IPPROTO_IPV6 41
#endif

#ifndef IPV6_MULTICAST_IF
#define IPV6_MULTICAST_IF IP_MULTICAST_IF /* or 6, or 17 */
#endif

#define IPV6_JOIN_GROUP (IPV6_MULTICAST_IF + 3)
#define IPV6_LEAVE_GROUP (IPV6_MULTICAST_IF + 4)

#ifndef _IN6_ADDR_DEFINED
#define _IN6_ADDR_DEFINED
struct in6_addr
{
 union
 {
  unsigned char _S6_u8[16];
  uint16_t _S6_u16[8];
  uint32_t _S6_u32[4];
  uint32_t __S6_align;
 } _S6_un;
};
#define s6_addr _S6_un._S6_u8
#endif

#define IN6ADDR_ANY_INIT { { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } } }

struct sockaddr_in6
{
 unsigned char sin6_len; /* unused */
 uint16_t sin6_family;
 uint16_t sin6_port;
 uint32_t sin6_flowinfo; /* unused */
 struct in6_addr sin6_addr;
 uint32_t sin6_scope_id; /* unused */
};

_EXPFUNC uint16_t _RTLENTRY htons(uint16_t) _ATTRIBUTE_PURE;
_EXPFUNC uint16_t _RTLENTRY ntohs(uint16_t) _ATTRIBUTE_PURE;

#ifdef __cplusplus
}
#endif

#endif

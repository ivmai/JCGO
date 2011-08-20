/*
 * @(#) $(JCGO)/minihdr/win/ws2tcpip.h --
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

#ifndef _WS2TCPIP_H
#define _WS2TCPIP_H

#ifndef _WINSOCK2_H
#include <winsock2.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef IP_TOS
#define IP_TOS 3 /* 8 for old winsock */
#endif

#ifndef IP_MULTICAST_IF
#define IP_MULTICAST_IF 9 /* 2 for old winsock */
#endif

#define IP_MULTICAST_TTL (IP_MULTICAST_IF + 1)
#define IP_MULTICAST_LOOP (IP_MULTICAST_IF + 2)
#define IP_ADD_MEMBERSHIP (IP_MULTICAST_IF + 3)
#define IP_DROP_MEMBERSHIP (IP_MULTICAST_IF + 4)

#ifndef IPV6_MULTICAST_IF
#define IPV6_MULTICAST_IF IP_MULTICAST_IF
#endif

#define IPV6_ADD_MEMBERSHIP (IPV6_MULTICAST_IF + 3)
#define IPV6_DROP_MEMBERSHIP (IPV6_MULTICAST_IF + 4)

#define s6_addr _S6_un._S6_u8
#define IN6ADDR_ANY_INIT { 0 }

struct ip_mreq
{
 struct in_addr imr_multiaddr;
 struct in_addr imr_interface;
};

struct in6_addr
{
 union
 {
  unsigned char _S6_u8[16];
  unsigned short _S6_u16[8];
  ULONG _S6_u32[4];
 } _S6_un;
};

struct sockaddr_in6
{
 short sin6_family;
 unsigned short sin6_port;
 ULONG sin6_flowinfo; /* unused */
 struct in6_addr sin6_addr;
 ULONG sin6_scope_id; /* unused */
};

#ifdef __cplusplus
}
#endif

#endif

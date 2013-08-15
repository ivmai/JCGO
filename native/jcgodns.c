/*
 * @(#) $(JCGO)/native/jcgodns.c --
 * a part of the JCGO native layer library (inet gethostby impl).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
 */

/*
 * Used control macros: JCGO_INET.
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
#include "jcgodns.h"
#endif

#ifndef NOJAVA_java_net_VMInetAddress_getHostNeedsSync0
JCGO_JNI_EXPF(jint,
Java_java_net_VMInetAddress_getHostNeedsSync0)( JNIEnv *pJniEnv, jclass This )
{
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
#ifdef JCGO_INET
 return JCGO_GETHOSTBY_NEEDSYNC;
#else
 return 0;
#endif
}
#endif

#ifndef NOJAVA_java_net_VMInetAddress_getHostByAddr0
JCGO_JNI_EXPF(jstring,
Java_java_net_VMInetAddress_getHostByAddr0)( JNIEnv *pJniEnv, jclass This,
 jbyteArray ip, jint iplen )
{
#ifdef JCGO_INET
 int family = AF_INET;
 jstring hostname;
 struct hostent JCGO_GETHOSTBY_FAR *phent;
 char *address =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ip, 0, iplen);
 JCGO_GETHOSTBY_T data;
 JCGO_UNUSED_VAR(This);
 if (address == NULL)
  return NULL;
#ifdef AF_INET6
 if ((int)iplen == 16)
  family = AF_INET6;
#endif
 phent = JCGO_GETHOSTBY_ADDR(address, (unsigned)iplen, family, &data);
 hostname = jcgo_JnuNewStringPlatform(pJniEnv, phent != NULL &&
             phent->h_name != NULL ? (char *)phent->h_name : "");
 (void)JCGO_GETHOSTBY_FREE(&data);
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ip, (jbyte *)address, 0);
 return hostname;
#else
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(ip);
 JCGO_UNUSED_VAR(iplen);
 return jcgo_JnuNewStringPlatform(pJniEnv, "");
#endif
}
#endif

#ifndef NOJAVA_java_net_VMInetAddress_getHostByName0
JCGO_JNI_EXPF(jint,
Java_java_net_VMInetAddress_getHostByName0)( JNIEnv *pJniEnv, jclass This,
 jstring hostname, jbyteArray ipAddrsBuf, jint bufSize )
{
#ifdef JCGO_INET
 int i;
 int j;
 unsigned pos;
 unsigned ipsize;
 int cnt = 0;
 struct hostent JCGO_GETHOSTBY_FAR *phent;
 char *paddr;
 char *addrsbuf =
  (char *)jcgo_JnuGetByteArrayElemsRegion(pJniEnv, ipAddrsBuf, 0, bufSize);
 char cbuf[MAXHOSTNAMELEN + 1];
 JCGO_GETHOSTBY_T data;
 JCGO_UNUSED_VAR(This);
 if (addrsbuf == NULL)
  return 0;
 if (jcgo_JnuStringToPlatformChars(pJniEnv, hostname, cbuf, sizeof(cbuf)) > 0)
 {
  if ((phent = JCGO_GETHOSTBY_NAME(cbuf, &data)) != NULL &&
      (phent->h_addrtype == AF_INET
#ifdef AF_INET6
      || phent->h_addrtype == AF_INET6
#endif
      ))
  {
   while (phent->h_addr_list[cnt] != NULL)
    cnt++;
   ipsize = (unsigned)phent->h_length;
   pos = 0;
   for (i = 0; i < cnt && pos < (unsigned)bufSize; i++)
   {
    *(addrsbuf + pos) = (char)ipsize;
    if ((unsigned)bufSize - pos <= ipsize)
     break;
    paddr = (char *)phent->h_addr_list[i];
    for (j = 0; j < (int)ipsize; j++)
     *(addrsbuf + (++pos)) = *(paddr + j);
    pos++;
   }
  }
  (void)JCGO_GETHOSTBY_FREE(&data);
 }
 jcgo_JnuReleaseByteArrayElemsRegion(pJniEnv, ipAddrsBuf,
  (jbyte *)addrsbuf, 0);
 return cnt;
#else
 JCGO_UNUSED_VAR(pJniEnv);
 JCGO_UNUSED_VAR(This);
 JCGO_UNUSED_VAR(hostname);
 JCGO_UNUSED_VAR(ipAddrsBuf);
 JCGO_UNUSED_VAR(bufSize);
 return 0;
#endif
}
#endif

#endif

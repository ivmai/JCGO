/*
 * @(#) $(JCGO)/goclsp/vm/java/net/VMInetAddress.java --
 * VM specific methods for "InetAddress" implementation.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
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

package java.net;

import gnu.java.net.VMAccessorGnuJavaNet;

final class VMInetAddress
{

 private static volatile boolean initialized;

 private static Object getHostLock;

 private static volatile String localHostname;

 private VMInetAddress() {}

 static String getLocalHostname()
 {
  String hostname = localHostname;
  if (hostname == null)
  {
   initialize();
   if ((hostname = getLocalHostname0()) != null && hostname.length() > 0)
    localHostname = hostname;
    else hostname = "localhost";
  }
  return hostname;
 }

 static byte[] lookupInaddrAny()
  throws UnknownHostException
 {
  int address = lookupInaddrAny0();
  return new byte[] { (byte) (address >> 24), (byte) (address >> 16),
          (byte) (address >> 8), (byte) address };
 }

 static String getHostByAddr(byte[] ip)
  throws UnknownHostException
 {
  if (ip == null)
   throw new NullPointerException();
  initialize();
  String hostname;
  if (getHostLock != null)
  {
   synchronized (getHostLock)
   {
    hostname = getHostByAddr0(ip, ip.length);
   }
  }
   else hostname = getHostByAddr0(ip, ip.length);
  if (hostname == null || hostname.length() == 0)
   throw new UnknownHostException();
  return hostname;
 }

 static byte[][] getHostByName(String hostname)
  throws UnknownHostException
 {
  initialize();
  if (hostname.length() == 0 || hostname.charAt(0) <= ' ')
   throw new UnknownHostException(hostname);
  int count;
  int pos;
  byte[] ipAddrsBuf = new byte[40];
  do
  {
   int res;
   if (getHostLock != null)
   {
    synchronized (getHostLock)
    {
     res = getHostByName0(hostname, ipAddrsBuf, ipAddrsBuf.length);
    }
   }
    else res = getHostByName0(hostname, ipAddrsBuf, ipAddrsBuf.length);
   if (res <= 0)
    throw new UnknownHostException(hostname);
   pos = 0;
   for (count = 0; count < res; count++)
   {
    if (ipAddrsBuf.length <= pos)
     break;
    pos = (ipAddrsBuf[pos] & 0xff) + pos + 1;
   }
   if (count == res && ipAddrsBuf.length >= pos)
    break;
   ipAddrsBuf = new byte[ipAddrsBuf.length << 1];
  } while (true);
  byte[][] addrs = new byte[count][];
  pos = 0;
  for (int i = 0; i < count; i++)
  {
   int iplen = ipAddrsBuf[pos++] & 0xff;
   byte[] ip = new byte[iplen];
   for (int j = 0; j < iplen; j++)
    ip[j] = ipAddrsBuf[pos++];
   addrs[i] = ip;
  }
  return addrs;
 }

 static byte[] aton(String address)
 {
  byte[] addr = null;
  int len = address.length();
  if (len > 0)
  {
   boolean isIPv6 = false;
   if (address.charAt(0) == '[')
   {
    if (address.charAt(len - 1) != ']')
     return null;
    address = address.substring(1, len - 1);
    isIPv6 = true;
   }
   if (isIPv6 || (addr = atonIPv4(address, 0, len)) == null)
    addr = atonIPv6(address);
  }
  return addr;
 }

 private static byte[] atonIPv4(String address, int pos, int endPos)
 {
  int cnt = 0;
  for (int i = pos - 1; (i = address.indexOf('.', i + 2)) >= 0; cnt++)
   if (i >= endPos)
    break;
  if (cnt <= 3)
  {
   byte[] addr = new byte[4];
   try
   {
    for (int i = 0; i < cnt; i++)
    {
     int nextPos = address.indexOf('.', pos + 1);
     int value = parseUInt(address, pos, nextPos);
     if ((value >>> 1) > 0x7f)
      return null;
     addr[i] = (byte) value;
     pos = nextPos + 1;
    }
    if (pos < endPos)
    {
     int value = parseUInt(address, pos, endPos);
     if ((-1 >>> ((cnt << 3) + 1)) >= (value >>> 1))
     {
      while (cnt < 3)
      {
       addr[cnt] = (byte) (value >> ((3 - cnt) << 3));
       cnt++;
      }
      addr[3] = (byte) value;
      return addr;
     }
    }
   }
   catch (NumberFormatException e) {}
  }
  return null;
 }

 private static int parseUInt(String str, int pos, int endPos)
  throws NumberFormatException
 {
  int value = 0;
  do
  {
   char ch = str.charAt(pos);
   if (ch < '0' || ch > '9' || value > (-1 >>> 1) / 5 ||
       ((value = value * 10 + (ch - '0')) >= 0 && ch - '0' > value))
    throw new NumberFormatException();
  } while (++pos < endPos);
  return value;
 }

 private static byte[] atonIPv6(String address)
 {
  int len = address.length();
  if (len <= 1)
   return null;
  int pos = address.indexOf('%', 0);
  if (pos >= 0)
  {
   if (len - 1 == pos)
    return null;
   len = pos;
  }
  pos = 0;
  if (address.charAt(0) == ':')
  {
   if (address.charAt(1) != ':')
    return null;
   pos = 1;
  }
  byte[] addr = new byte[16];
  int firstPos = pos;
  int j = 0;
  int value = 0;
  int zerosIndex = -1;
  boolean hasDigits = false;
  while (pos < len)
  {
   char ch = address.charAt(pos++);
   int digit =
    ch >= '0' && ch <= '9' ? ch - '0' : ch >= 'A' && ch <= 'F' ?
    ch - ('A' - 10) : ch >= 'a' && ch <= 'f' ? ch - ('a' - 10) : -1;
   if (digit != -1)
   {
    if (value > 0xfff)
     return null;
    value = (value << 4) | digit;
    hasDigits = true;
   }
    else
    {
     if (ch != ':')
     {
      if (ch != '.' || j > 12)
       return null;
      pos = address.indexOf('.', pos + 1);
      if (pos < 0 || (pos = address.indexOf('.', pos + 2)) < 0 || pos >= len)
       return null;
      byte[] addrIPv4 = atonIPv4(address, firstPos, len);
      if (addrIPv4 == null)
       return null;
      System.arraycopy(addrIPv4, 0, addr, j, 4);
      hasDigits = false;
      j += 4;
      break;
     }
     firstPos = pos;
     if (hasDigits)
     {
      if (pos == len || j > 14)
       return null;
      addr[j] = (byte) (value >> 8);
      addr[j + 1] = (byte) value;
      hasDigits = false;
      j += 2;
      value = 0;
     }
      else
      {
       if (zerosIndex >= 0)
        return null;
       zerosIndex = j;
      }
    }
  }
  if (hasDigits)
  {
   if (j > 14)
    return null;
   addr[j] = (byte) (value >> 8);
   addr[j + 1] = (byte) value;
   j += 2;
  }
  if (zerosIndex >= 0)
  {
   if (j == 16)
    return null;
   j -= zerosIndex;
   if (j > 0)
   {
    System.arraycopy(addr, zerosIndex, addr, 16 - j, j);
    if (j > 8)
     j = 16 - j;
    while (j-- > 0)
     addr[zerosIndex++] = 0;
   }
  }
   else if (j != 16)
    return null;
  return addr;
 }

 private static void initialize()
 {
  if (!initialized)
  {
   VMAccessorGnuJavaNet.socketsInitVMPlainSocketImpl();
   if (getHostNeedsSync0() != 0)
   {
    Object obj = new Object();
    if (getHostLock == null)
     getHostLock = obj;
   }
   initialized = true;
  }
 }

 private static native int lookupInaddrAny0();

 private static native String getLocalHostname0();

 private static native int getHostNeedsSync0();

 private static native String getHostByAddr0(byte[] ip,
   int iplen); /* blocking syscall */

 private static native int getHostByName0(String hostname, byte[] ipAddrsBuf,
   int bufSize); /* blocking syscall */
}

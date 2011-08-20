/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/net/VMAccessorGnuJavaNet.java --
 * VM cross-package access helper for "gnu.java.net".
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

package gnu.java.net;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.SocketException;

public final class VMAccessorGnuJavaNet
{ /* used by VM classes only */

 private VMAccessorGnuJavaNet() {}

 public static InetSocketAddress socketAcceptVMPlainSocketImpl(int[] fdArr,
   boolean isNonBlocking)
  throws IOException
 {
  return VMPlainSocketImpl.socketAccept(fdArr, isNonBlocking);
 }

 public static int socketAvailableVMPlainSocketImpl(int fd)
  throws IOException
 {
  return VMPlainSocketImpl.socketAvailable(fd);
 }

 public static void socketCloseVMPlainSocketImpl(int fd)
  throws IOException
 {
  VMPlainSocketImpl.socketClose(fd);
 }

 public static boolean socketConnectVMPlainSocketImpl(int fd,
   InetSocketAddress address, int timeout)
  throws IOException
 {
  return VMPlainSocketImpl.socketConnect(fd, address, timeout);
 }

 public static int socketCreateVMPlainSocketImpl(boolean stream)
  throws SocketException
 {
  return VMPlainSocketImpl.socketCreate(stream);
 }

 public static void socketDisconnectVMPlainSocketImpl(int fd)
  throws IOException
 {
  VMPlainSocketImpl.socketDisconnect(fd);
 }

 public static InetSocketAddress socketGetLocalAddrPortVMPlainSocketImpl(
   int fd)
  throws SocketException
 {
  return VMPlainSocketImpl.socketGetLocalAddrPort(fd);
 }

 public static void socketsInitVMPlainSocketImpl()
 {
  VMPlainSocketImpl.socketsInit();
 }

 public static InetSocketAddress socketRecvFromVMPlainSocketImpl(int fd,
   byte[] buffer, int[] offArr, int len, boolean urgent, boolean peek,
   boolean fillAddress, boolean stream, boolean isNonBlocking)
  throws IOException
 {
  return VMPlainSocketImpl.socketRecvFrom(fd, buffer, offArr, len, urgent,
          peek, fillAddress, stream, isNonBlocking);
 }

 public static int socketSelectVMPlainSocketImpl(int[] readFDs,
   int[] writeFDs, int[] exceptFDs, int timeout)
  throws IOException
 {
  return VMPlainSocketImpl.socketSelect(readFDs, writeFDs, exceptFDs,
          timeout);
 }

 public static void socketSendToVMPlainSocketImpl(int fd, byte[] buffer,
   int[] offArr, int len, InetSocketAddress address, boolean urgent,
   boolean isNonBlocking)
  throws IOException
 {
  VMPlainSocketImpl.socketSendTo(fd, buffer, offArr, len, address, urgent,
   isNonBlocking);
 }

 public static void socketSetNonBlockingVMPlainSocketImpl(int fd, boolean on)
  throws IOException
 {
  VMPlainSocketImpl.socketSetNonBlocking(fd, on);
 }
}

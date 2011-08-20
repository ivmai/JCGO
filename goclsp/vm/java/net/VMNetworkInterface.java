/*
 * @(#) $(JCGO)/goclsp/vm/java/net/VMNetworkInterface.java --
 * VM specific methods for "NetworkInterface" implementation.
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

import java.util.HashSet;
import java.util.Set;

final class VMNetworkInterface
{

 private static final int IFF_UP = 0x1;
 private static final int IFF_LOOPBACK = 0x8;
 private static final int IFF_POINTOPOINT = 0x10;
 private static final int IFF_RUNNING = 0x40;
 private static final int IFF_MULTICAST = 0x800;

 final String name;

 final Set addresses = new HashSet();

 VMNetworkInterface()
 {
  name = "anyif";
  addresses.add(InetAddress.ANY_IF);
 }

 private VMNetworkInterface(String name)
 {
  this.name = name;
 }

 static VMNetworkInterface[] getVMInterfaces()
  throws SocketException
 {
  /* not implemented */
  return new VMNetworkInterface[0];
 }

 static boolean isUp(String name)
  throws SocketException
 {
  return (getIffFlags(name) & (IFF_UP | IFF_RUNNING)) != 0;
 }

 static boolean isLoopback(String name)
  throws SocketException
 {
  return (getIffFlags(name) & IFF_LOOPBACK) != 0;
 }

 static boolean isPointToPoint(String name)
  throws SocketException
 {
  return (getIffFlags(name) & IFF_POINTOPOINT) != 0;
 }

 static boolean supportsMulticast(String name)
  throws SocketException
 {
  return (getIffFlags(name) & IFF_MULTICAST) != 0;
 }

 private static int getIffFlags(String name)
  throws SocketException
 {
  /* not implemented */
  throw new SocketException("not implemented");
 }
}

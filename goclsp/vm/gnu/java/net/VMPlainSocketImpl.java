/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/net/VMPlainSocketImpl.java --
 * VM interface for default socket implementation.
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

package gnu.java.net;

import gnu.java.nio.VMChannel;

import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.SocketOptions;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.Enumeration;
import java.util.HashMap;

public final class VMPlainSocketImpl
{

 public final class State
 {

  private VMChannel.State chState;

  State() {}

  public void setChannelFD(VMChannel.State newChState)
   throws IOException
  {
   VMChannel.State chState = this.chState;
   if (chState != null && chState.isValid())
    throw new IOException("file descriptor already initialized");
   this.chState = newChState;
  }

  boolean isValid()
  {
   VMChannel.State chState = this.chState;
   return chState != null && chState.isValid();
  }

  int getNativeFD()
   throws IOException
  {
   VMChannel.State chState = this.chState;
   if (chState == null)
    throw new IOException("invalid file descriptor");
   return chState.getNativeFD();
  }

  void close()
   throws IOException
  {
   VMChannel.State chState = this.chState;
   if (chState == null)
    throw new IOException("invalid file descriptor");
   chState.close();
  }
 }

 private static final int CP_IP_TTL = 0x1E61;

 private static final int MAX_IP_SIZE = 16;

 private static final int DEFAULT_TIMEOUT = 30 * 1000;

 private static final int DEFAULT_BACKLOG = 5;

 private static final int[] EMPTY_FDS = {};

 private static final byte[] EMPTY_BUF = {};

 private static boolean initialized;

 private static boolean preventBlocking;

 private static HashMap timeouts;

 private final State state = new State();

 VMPlainSocketImpl() {}

 VMPlainSocketImpl(VMChannel channel)
  throws IOException
 {
  state.setChannelFD(channel.getState());
 }

 public State getState()
 {
  return state;
 }

 void setTimeToLive(int ttl)
  throws SocketException
 {
  if (ttl <= 0)
   ttl = 0;
  int res;
  do
  {
   res = socketGetSetOption0(getNativeSocketFD(), CP_IP_TTL, ttl);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
 }

 int getTimeToLive()
  throws SocketException
 {
  int res;
  do
  {
   res = socketGetSetOption0(getNativeSocketFD(), CP_IP_TTL, -1);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
  return res & 0xff;
 }

 void setOption(int optionId, Object value)
  throws SocketException
 {
  int optval = -1;
  if (value instanceof Integer)
  {
   if (optionId == SocketOptions.SO_TIMEOUT)
   {
    setTimeoutFor(getNativeSocketFD(), ((Integer) value).intValue());
    return;
   }
   if (optionId == SocketOptions.SO_LINGER ||
       optionId == SocketOptions.SO_SNDBUF ||
       optionId == SocketOptions.SO_RCVBUF ||
       optionId == SocketOptions.IP_TOS)
   {
    optval = ((Integer) value).intValue();
    if (optval < 0)
     optval = 0;
     else if (optionId == SocketOptions.SO_LINGER)
      optval++;
   }
  }
   else
   {
    if (value instanceof Boolean)
    {
     if (optionId != SocketOptions.SO_TIMEOUT &&
         optionId != SocketOptions.SO_SNDBUF &&
         optionId != SocketOptions.SO_RCVBUF &&
         optionId != SocketOptions.IP_TOS)
     {
      optval = 0;
      if (((Boolean) value).booleanValue())
      {
       optval = 1;
       if (optionId == SocketOptions.SO_LINGER)
        optval = -1;
      }
     }
    }
     else
     {
      if (value == null)
       throw new NullPointerException();
     }
   }
  if (optval < 0)
   throw new IllegalArgumentException("invalid option value");
  int res;
  do
  {
   res = socketGetSetOption0(getNativeSocketFD(), optionId, optval);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
 }

 void setMulticastInterface(int optionId, InetAddress addr)
  throws SocketException
 {
  socketSetMulticastAddr(getNativeSocketFD(), addr);
 }

 Object getOption(int optionId)
  throws SocketException
 {
  if (optionId == SocketOptions.SO_TIMEOUT)
   return new Integer(getTimeoutFor(getNativeSocketFD()));
  int res;
  do
  {
   res = socketGetSetOption0(getNativeSocketFD(), optionId, -1);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
  if (optionId == SocketOptions.SO_LINGER && res > 0)
   return new Integer(res - 1);
  if (optionId == SocketOptions.SO_SNDBUF ||
      optionId == SocketOptions.SO_RCVBUF ||
      optionId == SocketOptions.IP_TOS)
   return new Integer(res);
  return res != 0 ? Boolean.TRUE : Boolean.FALSE;
 }

 InetAddress getMulticastInterface(int optionId)
  throws SocketException
 {
  return socketGetMulticastAddr(getNativeSocketFD());
 }

 void bind(InetSocketAddress address)
  throws IOException
 {
  InetAddress addr = address.getAddress();
  int fd = state.getNativeFD();
  if (addr == null)
   throw new SocketException("unresolved address: " + address.toString());
  socketBind(fd, addr, address.getPort());
 }

 void listen(int backlog)
  throws IOException
 {
  if (backlog <= 0)
   backlog = DEFAULT_BACKLOG;
  int res;
  do
  {
   res = socketListen0(state.getNativeFD(), backlog);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkIOSockResCode(res);
 }

 void join(InetAddress group)
  throws IOException
 {
  int fd = state.getNativeFD();
  InetAddress addrNetIf = socketGetMulticastAddr(fd);
  try
  {
   socketMulticastGroup(fd, group, addrNetIf, true);
  }
  finally
  {
   socketSetMulticastAddr(fd, addrNetIf);
  }
 }

 void leave(InetAddress group)
  throws IOException
 {
  int fd = state.getNativeFD();
  InetAddress addrNetIf = socketGetMulticastAddr(fd);
  try
  {
   socketMulticastGroup(fd, group, addrNetIf, false);
  }
  finally
  {
   socketSetMulticastAddr(fd, addrNetIf);
  }
 }

 void joinGroup(InetSocketAddress address, NetworkInterface netIf)
  throws IOException
 {
  multicastGroup(state.getNativeFD(), address, netIf, true);
 }

 void leaveGroup(InetSocketAddress address, NetworkInterface netIf)
  throws IOException
 {
  multicastGroup(state.getNativeFD(), address, netIf, false);
 }

 void shutdownInput()
  throws IOException
 {
  socketShutdown(state.getNativeFD(), true, false);
 }

 void shutdownOutput()
  throws IOException
 {
  socketShutdown(state.getNativeFD(), false, true);
 }

 void sendUrgentData(int b)
  throws IOException
 {
  socketSendTo(state.getNativeFD(), new byte[] { (byte) b }, new int[1], 1,
   null, true, false);
 }

 void close()
  throws IOException
 {
  state.close();
 }

 static final void socketsInit()
 { /* used by VM classes only */
  if (!initialized)
   synchronized (EMPTY_FDS)
   {
    if (!initialized)
    {
     if (socketsInit0() < 0)
      throw new InternalError("cannot initialize sockets");
     preventBlocking = VMAccessorJavaLang.preventIOBlockingVMRuntime();
     initialized = true;
    }
   }
 }

 static final int socketSelect(int[] readFDs, int[] writeFDs, int[] exceptFDs,
   int timeout)
  throws IOException
 { /* used by VM classes only */
  long deadline = timeout > 0 ? getDeadlineTime(timeout) : 0L;
  int res;
  int readFDsLen = readFDs.length;
  int writeFDsLen = writeFDs.length;
  int exceptFDsLen = exceptFDs.length;
  do
  {
   res = socketSelect0(readFDs, readFDsLen, writeFDs, writeFDsLen, exceptFDs,
          exceptFDsLen, preventBlocking ? 0 : timeout);
   if (res >= 0)
   {
    if (res != 0 || !preventBlocking)
     break;
   }
    else if (isSocketErrInterrupted0(res) == 0)
     throw new IOException(getSocketErrorMsg0(res));
   Thread.yield();
   res = 0;
  } while (!Thread.currentThread().isInterrupted() && (timeout <= 0 ||
           (timeout = getTimeoutOfDeadline(deadline)) > 0 ||
           !preventBlocking));
  int i;
  if (res != 0)
  {
   res = 0;
   for (i = 0; i < readFDsLen; i++)
    if (readFDs[i] != -1)
     res++;
   for (i = 0; i < writeFDsLen; i++)
    if (writeFDs[i] != -1)
     res++;
   for (i = 0; i < exceptFDsLen; i++)
    if (exceptFDs[i] != -1)
     res++;
  }
   else
   {
    for (i = 0; i < readFDsLen; i++)
     readFDs[i] = -1;
    for (i = 0; i < writeFDsLen; i++)
     writeFDs[i] = -1;
    for (i = 0; i < exceptFDsLen; i++)
     exceptFDs[i] = -1;
   }
  return res;
 }

 static final int socketCreate(boolean stream)
  throws SocketException
 { /* used by VM classes only */
  socketsInit();
  int fd;
  int res = 0;
  int[] resArr = new int[1];
  boolean retrying = false;
  do
  {
   fd = socketCreate0(resArr, stream ? 1 : 0);
   if (fd != -1)
    break;
   res = resArr[0];
   if (!retrying && isSocketRetryNeededOnce(res))
    retrying = true;
    else if (isSocketErrInterrupted0(res) == 0)
     break;
  } while (true);
  checkSocketResCode(res);
  setTimeoutFor(fd, 0);
  if (preventBlocking)
   socketSetNonBlocking0(fd, 1);
  int optionId = stream ? SocketOptions.SO_REUSEADDR :
                  SocketOptions.SO_BROADCAST;
  do
  {
   res = socketGetSetOption0(fd, optionId, 1);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  return fd;
 }

 static final InetSocketAddress socketGetLocalAddrPort(int fd)
  throws SocketException
 { /* used by VM classes only */
  int res;
  byte[] ip = new byte[MAX_IP_SIZE];
  int[] portArr = new int[1];
  do
  {
   res = socketGetLocalAddrPort0(fd, ip, ip.length, portArr);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
  return new InetSocketAddress(toInetAddress(ip, res), portArr[0]);
 }

 static final void socketSetNonBlocking(int fd, boolean on)
  throws IOException
 { /* used by VM classes only */
  if (!preventBlocking)
   checkIOSockResCode(socketSetNonBlocking0(fd, on ? 1 : 0));
 }

 static final int socketAvailable(int fd)
  throws IOException
 { /* used by VM classes only */
  int res = socketAvailable0(fd);
  if (res <= 0)
  {
   int[] readFDs = new int[1];
   do
   {
    res = 0;
    if ((readFDs[0] = fd) == -1)
     break;
    res = socketSelect0(readFDs, 1, EMPTY_FDS, 0, EMPTY_FDS, 0, 0);
   } while (res < 0 && isSocketErrInterrupted0(res) != 0);
   checkIOSockResCode(res);
   if (res > 0)
    res = 1;
  }
  return res;
 }

 static final boolean socketConnect(int fd, InetSocketAddress address,
   int timeout)
  throws IOException
 { /* used by VM classes only */
  InetAddress addr = address.getAddress();
  if (addr == null)
   throw new SocketException("unresolved address: " + address.toString());
  int port = address.getPort();
  byte[] ip = addr.getAddress();
  if (timeout < 0 && preventBlocking)
   timeout = DEFAULT_TIMEOUT;
  if (timeout != 0)
   checkThreadInterrupted();
  int res;
  boolean isConnected = true;
  boolean retrying = false;
  if (timeout >= 0)
  {
   long deadline = timeout > 0 ? getDeadlineTime(timeout) : 0L;
   boolean connecting = false;
   try
   {
    if (timeout > 0 && !preventBlocking)
     socketSetNonBlocking0(fd, 1);
    do
    {
     boolean oldConnecting = connecting;
     res = socketConnect0(fd, ip, ip.length, port);
     connecting = true;
     if (!retrying && isSocketRetryNeededOnce(res))
     {
      connecting = oldConnecting;
      retrying = true;
     }
      else
      {
       if (res >= 0)
        break;
       if (timeout == 0)
       {
        if (isSocketErrConnected0(res) == 0)
        {
         if (isSocketErrInterrupted0(res) == 0 &&
             isSocketErrConnInProgress0(res) == 0)
          break;
         isConnected = false;
        }
        res = 0;
        break;
       }
       if (isSocketErrConnInProgress0(res) != 0)
       {
        if (!oldConnecting)
         socketSelectSingle(fd, true, true, false, deadline, false);
       }
        else
        {
         if (oldConnecting && isSocketErrConnected0(res) != 0)
         {
          res = 0;
          break;
         }
         connecting = oldConnecting;
         if (isSocketErrInterrupted0(res) == 0)
          break;
        }
      }
     Thread.yield();
     if (timeout > 0)
     {
      if (getTimeoutOfDeadline(deadline) <= 0)
       throw new SocketTimeoutException();
      checkThreadInterrupted();
     }
    } while (true);
    if (res >= 0)
     connecting = false;
   }
   finally
   {
    if (timeout > 0 && !preventBlocking)
     socketSetNonBlocking0(fd, 0);
    if (connecting)
     socketShutdown(fd, true, true);
   }
  }
   else
   {
    do
    {
     res = socketConnect0(fd, ip, ip.length, port);
     if (!retrying && isSocketRetryNeededOnce(res))
      retrying = true;
      else if (res >= 0 || isSocketErrInterrupted0(res) == 0)
       break;
     Thread.yield();
     checkThreadInterrupted();
    } while (true);
   }
  if (res < 0)
  {
   if (isSocketErrAddrNotAvail0(res) != 0)
    throw new ConnectException("Invalid destination port");
   String message = getSocketErrorMsg0(res);
   if (isSocketErrConnRefused0(res) != 0)
    throw new ConnectException(message);
   if (isSocketErrHostUnreach0(res) != 0)
    throw new NoRouteToHostException(message);
   throw new SocketException(message);
  }
  return isConnected;
 }

 static final void socketDisconnect(int fd)
  throws IOException
 { /* used by VM classes only */
  int res;
  do
  {
   res = socketDisconnect0(fd);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkIOSockResCode(res);
 }

 static final InetSocketAddress socketAccept(int[] fdArr,
   boolean isNonBlocking)
  throws IOException
 { /* used by VM classes only */
  InetSocketAddress addr = null;
  int fd = fdArr[0];
  byte[] ip = new byte[MAX_IP_SIZE];
  int[] iplenPortArr = new int[2];
  try
  {
   boolean retrying = false;
   do
   {
    fdArr[0] = socketAccept0(fd, ip, ip.length, iplenPortArr);
    int res = iplenPortArr[0];
    if (!retrying && isSocketRetryNeededOnce(res))
     retrying = true;
     else
     {
      if (res >= 0)
      {
       addr = new InetSocketAddress(toInetAddress(ip, res), iplenPortArr[1]);
       break;
      }
      if (isSocketErrInterrupted0(res) == 0)
       checkIOSockResCode(res);
      if (isNonBlocking)
       break;
     }
    Thread.yield();
    if (!isNonBlocking)
     checkThreadInterrupted();
   } while (true);
  }
  finally
  {
   int newfd = fdArr[0];
   if (newfd != fd && newfd != -1)
   {
    if (addr == null)
     socketShutdown(newfd, true, true);
     else if (preventBlocking)
      socketSetNonBlocking0(newfd, 1);
      else if (isNonBlocking)
       socketSetNonBlocking0(newfd, 0);
   }
  }
  return addr;
 }

 static final InetSocketAddress socketRecvFrom(int fd, byte[] buffer,
   int[] offArr, int len, boolean urgent, boolean peek, boolean fillAddress,
   boolean stream, boolean isNonBlocking)
  throws IOException
 { /* used by VM classes only */
  int off = offArr[0];
  if ((off | len) < 0 || buffer.length - off < len)
   throw new ArrayIndexOutOfBoundsException();
  byte[] ip = EMPTY_BUF;
  if (fillAddress)
  {
   ip = new byte[MAX_IP_SIZE];
   stream = true;
  }
  int[] iplenPortArr = new int[2];
  byte[] origBuffer = buffer;
  int timeout = 0;
  long deadline = 0L;
  if (!isNonBlocking)
  {
   timeout = getTimeoutFor(fd);
   if (timeout == 0 && preventBlocking)
    timeout = DEFAULT_TIMEOUT;
   deadline = socketSelectSingle(fd, !urgent, false, urgent, timeout, true);
  }
  do
  {
   int res = socketRecvFrom0(fd, buffer, off, len, urgent ? 1 : 0,
              peek ? 1 : 0, ip, stream ? ip.length : -1, iplenPortArr);
   if (res > 0)
   {
    if (res >= len)
     res = len;
    if (buffer != origBuffer)
    {
     off = offArr[0];
     System.arraycopy(buffer, 0, origBuffer, off, res);
    }
    offArr[0] = off + res;
    break;
   }
   if (res == 0)
   {
    if (!fillAddress)
    {
     offArr[0]--;
     break;
    }
    if (off == 0)
     break;
    buffer = new byte[len];
    off = 0;
   }
    else
    {
     if (isSocketErrInterrupted0(res) == 0)
      checkIOSockResCode(res);
     if (isNonBlocking)
     {
      fillAddress = false;
      break;
     }
    }
   Thread.yield();
   if (!isNonBlocking)
   {
    if (timeout > 0 && getTimeoutOfDeadline(deadline) <= 0)
     throw new SocketTimeoutException();
    checkThreadInterrupted();
   }
  } while (true);
  return fillAddress ? new InetSocketAddress(toInetAddress(ip,
          iplenPortArr[0]), iplenPortArr[1]) : null;
 }

 static final void socketSendTo(int fd, byte[] buffer, int[] offArr,
   int len, InetSocketAddress address, boolean urgent, boolean isNonBlocking)
  throws IOException
 { /* used by VM classes only */
  int off = offArr[0];
  if ((off | len) < 0 || buffer.length - off < len)
   throw new ArrayIndexOutOfBoundsException();
  byte[] ip = EMPTY_BUF;
  int port = 0;
  if (address != null)
  {
   InetAddress addr = address.getAddress();
   if (addr == null)
    throw new SocketException("unresolved address: " + address.toString());
   ip = addr.getAddress();
   port = address.getPort();
  }
  int remain = len;
  int timeout = 0;
  long deadline = 0L;
  if (!isNonBlocking)
  {
   timeout = getTimeoutFor(fd);
   if (timeout == 0 && preventBlocking)
    timeout = DEFAULT_TIMEOUT;
   deadline = socketSelectSingle(fd, false, true, false, timeout, true);
  }
  do
  {
   int res = socketSendTo0(fd, buffer, off, remain, urgent ? 1 : 0, ip,
              ip.length, port);
   if (res >= 0)
   {
    offArr[0] += res;
    if ((remain -= res) <= 0)
     break;
    if (res == 0 && off > 0)
    {
     byte[] newBuffer = new byte[remain];
     System.arraycopy(buffer, off, newBuffer, 0, remain);
     buffer = newBuffer;
     off = 0;
    }
     else
     {
      if (address != null)
       break;
      off += res;
     }
   }
    else
    {
     if (isSocketErrInterrupted0(res) == 0)
      checkIOSockResCode(res);
     if (isNonBlocking)
      break;
    }
   Thread.yield();
   if (!isNonBlocking)
   {
    if (res > 0)
     deadline = socketSelectSingle(fd, false, true, false, timeout, true);
     else
     {
      if (timeout > 0 && getTimeoutOfDeadline(deadline) <= 0)
       throw new SocketTimeoutException();
      if (remain == len)
       checkThreadInterrupted();
     }
   }
  } while (true);
 }

 static final void socketClose(int fd)
  throws IOException
 { /* used by VM classes only */
  int res;
  do
  {
   res = socketClose0(fd);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkIOSockResCode(res);
  setTimeoutFor(fd, 0);
 }

 private int getNativeSocketFD()
  throws SocketException
 {
  try
  {
   return state.getNativeFD();
  }
  catch (SocketException e)
  {
   throw e;
  }
  catch (IOException e)
  {
   throw (SocketException) (new SocketException()).initCause(e);
  }
 }

 private static long socketSelectSingle(int fd, boolean input, boolean output,
   boolean urgent, long deadline, boolean isTimeout)
  throws IOException
 {
  int timeout = 0;
  if (isTimeout)
  {
   checkThreadInterrupted();
   timeout = (int) deadline;
   if (timeout <= 0)
    return 0L;
   deadline = getDeadlineTime(timeout);
  }
  if (fd != -1)
  {
   int res;
   int[] readFDs = input ? new int[1] : EMPTY_FDS;
   int[] writeFDs = output ? new int[1] : EMPTY_FDS;
   int[] exceptFDs = urgent ? new int[1] : EMPTY_FDS;
   do
   {
    if (input)
     readFDs[0] = fd;
    if (output)
     writeFDs[0] = fd;
    if (urgent)
     exceptFDs[0] = fd;
    if (!isTimeout)
    {
     timeout = getTimeoutOfDeadline(deadline);
     res = 0;
     if (timeout <= 0)
      break;
     checkThreadInterrupted();
    }
    res = socketSelect0(readFDs, readFDs.length, writeFDs, writeFDs.length,
           exceptFDs, exceptFDs.length, preventBlocking ? 0 : timeout);
    if (res >= 0)
    {
     if (res != 0 || !preventBlocking)
      break;
    }
     else if (isSocketErrInterrupted0(res) == 0)
      break;
    isTimeout = false;
    Thread.yield();
   } while (true);
   if (res == 0)
    throw new SocketTimeoutException();
  }
  return deadline;
 }

 private static void socketBind(int fd, InetAddress addr, int port)
  throws SocketException
 {
  int res;
  byte[] ip = addr.getAddress();
  boolean retrying = false;
  do
  {
   res = socketBind0(fd, ip, ip.length, port);
   if (!retrying && isSocketRetryNeededOnce(res))
    retrying = true;
    else if (res >= 0 || isSocketErrInterrupted0(res) == 0)
     break;
  } while (true);
  checkSocketResCode(res);
 }

 private static void multicastGroup(int fd, InetSocketAddress address,
   NetworkInterface netIf, boolean join)
  throws IOException
 {
  InetAddress addr = address.getAddress();
  if (addr == null)
   throw new SocketException("unresolved address: " + address.toString());
  InetAddress addrNetIf = socketGetMulticastAddr(fd);
  try
  {
   if (netIf != null)
   {
    Enumeration en = netIf.getInetAddresses();
    boolean isSet = false;
    IOException exception = null;
    while (en.hasMoreElements())
    {
     try
     {
      socketMulticastGroup(fd, addr, (InetAddress) en.nextElement(), join);
      isSet = true;
     }
     catch (IOException e)
     {
      exception = e;
     }
    }
    if (!isSet)
    {
     if (exception == null)
      exception = new SocketException();
     throw exception;
    }
   }
    else socketMulticastGroup(fd, addr, addrNetIf, join);
  }
  finally
  {
   socketSetMulticastAddr(fd, addrNetIf);
  }
 }

 private static void setTimeoutFor(int fd, int timeout)
 {
  synchronized (EMPTY_FDS)
  {
   if (timeouts == null)
   {
    if (timeout == 0)
     return;
    timeouts = new HashMap();
   }
   if (timeout != 0)
    timeouts.put(new Integer(fd), new Integer(timeout));
    else timeouts.remove(new Integer(fd));
  }
 }

 private static int getTimeoutFor(int fd)
 {
  int timeout = 0;
  if (timeouts != null)
   synchronized (EMPTY_FDS)
   {
    if (timeouts != null)
    {
     Object value = timeouts.get(new Integer(fd));
     if (value != null)
      timeout = ((Integer) value).intValue();
    }
   }
  return timeout;
 }

 private static void socketSetMulticastAddr(int fd, InetAddress addr)
  throws SocketException
 {
  int res;
  byte[] ip = addr.getAddress();
  do
  {
   res = socketSetMulticastAddr0(fd, ip, ip.length);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
 }

 private static InetAddress socketGetMulticastAddr(int fd)
  throws SocketException
 {
  int res;
  byte[] ip = new byte[MAX_IP_SIZE];
  do
  {
   res = socketGetMulticastAddr0(fd, ip, ip.length);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkSocketResCode(res);
  return toInetAddress(ip, res);
 }

 private static void socketMulticastGroup(int fd, InetAddress addr,
   InetAddress addrNetIf, boolean join)
  throws IOException
 {
  byte[] ip = addr.getAddress();
  byte[] ipNetIf = addrNetIf.getAddress();
  int res;
  do
  {
   res = socketMulticastGroup0(fd, ip, ip.length, ipNetIf, ipNetIf.length,
          join ? 1 : 0);
  } while (res < 0 && isSocketErrInterrupted0(res) != 0);
  checkIOSockResCode(res);
 }

 private static void socketShutdown(int fd, boolean input, boolean output)
  throws IOException
 {
  if (input || output)
  {
   int res;
   do
   {
    res = socketShutdown0(fd, input ? 1 : 0, output ? 1 : 0);
   } while (res < 0 && isSocketErrInterrupted0(res) != 0);
   checkIOSockResCode(res);
  }
 }

 private static InetAddress toInetAddress(byte[] ip, int iplen)
  throws SocketException
 {
  try
  {
   if (ip.length != iplen)
   {
    if (ip.length < iplen)
     throw new UnknownHostException();
    byte[] newip = new byte[iplen];
    System.arraycopy(ip, 0, newip, 0, iplen);
    ip = newip;
   }
   return InetAddress.getByAddress(ip);
  }
  catch (UnknownHostException e)
  {
   throw new SocketException("invalid address family");
  }
 }

 private static long getDeadlineTime(int timeout)
 {
  return System.currentTimeMillis() + timeout;
 }

 private static int getTimeoutOfDeadline(long deadline)
 {
  long timeleft = deadline - System.currentTimeMillis();
  return timeleft > 0L ? (timeleft < (long) (-1 >>> 1) ?
          (int) timeleft : -1 >>> 1) : 0;
 }

 private static void checkThreadInterrupted()
  throws IOException
 {
  if (Thread.interrupted())
   throw new InterruptedIOException();
 }

 private static boolean isSocketRetryNeededOnce(int res)
 {
  if (res >= 0 || isSocketErrNoResources0(res) == 0)
   return false;
  Runtime runtime = Runtime.getRuntime();
  runtime.gc();
  runtime.runFinalization();
  return true;
 }

 private static void checkIOSockResCode(int res)
  throws IOException
 {
  checkSocketResCode(res);
 }

 private static void checkSocketResCode(int res)
  throws SocketException
 {
  if (res < 0)
  {
   String message = getSocketErrorMsg0(res);
   if (isSocketErrAddrNotAvail0(res) != 0)
    throw new BindException(message);
   if (isSocketErrHostUnreach0(res) != 0)
    throw new NoRouteToHostException(message);
   if (isSocketErrResetConn0(res) != 0)
    throw new PortUnreachableException(message);
   throw new SocketException(message);
  }
 }

 private static native int socketsInit0();

 private static native int isSocketErrAddrNotAvail0(int res);

 private static native int isSocketErrConnInProgress0(int res);

 private static native int isSocketErrConnRefused0(int res);

 private static native int isSocketErrConnected0(int res);

 private static native int isSocketErrHostUnreach0(int res);

 private static native int isSocketErrInterrupted0(int res);

 private static native int isSocketErrNoResources0(int res);

 private static native int isSocketErrResetConn0(int res);

 private static native String getSocketErrorMsg0(int res);

 private static native int socketCreate0(int[] resArr, int stream);

 private static native int socketBind0(int fd, byte[] ip, int iplen,
   int port);

 private static native int socketDisconnect0(int fd);

 private static native int socketConnect0(int fd, byte[] ip, int iplen,
   int port); /* blocking syscall */

 private static native int socketSendTo0(int fd, byte[] buffer, int off,
   int len, int urgent, byte[] ip, int iplen,
   int port); /* blocking syscall */

 private static native int socketRecvFrom0(int fd, byte[] buffer, int off,
   int len, int urgent, int peek, byte[] ip, int ipmaxlen,
   int[] iplenPortArr); /* blocking syscall */

 private static native int socketAccept0(int fd, byte[] ip, int ipmaxlen,
   int[] iplenPortArr); /* blocking syscall */

 private static native int socketGetLocalAddrPort0(int fd, byte[] ip,
   int ipmaxlen, int[] portArr);

 private static native int socketSelect0(int[] readFDs, int readFDsLen,
   int[] writeFDs, int writeFDsLen, int[] exceptFDs, int exceptFDsLen,
   int timeout); /* blocking syscall */

 private static native int socketSetNonBlocking0(int fd, int on);

 private static native int socketAvailable0(int fd);

 private static native int socketListen0(int fd, int backlog);

 private static native int socketGetSetOption0(int fd, int optionId,
   int optval);

 private static native int socketSetMulticastAddr0(int fd, byte[] ip,
   int iplen);

 private static native int socketGetMulticastAddr0(int fd, byte[] ip,
   int ipmaxlen);

 private static native int socketMulticastGroup0(int fd, byte[] ip, int iplen,
   byte[] ipNetIf, int iplenNetIf, int join);

 private static native int socketShutdown0(int fd, int input, int output);

 private static native int socketClose0(int fd);
}

/*
 * @(#) $(JCGO)/goclsp/vm/gnu/java/nio/VMChannel.java --
 * VM specific channel operations implementation.
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

package gnu.java.nio;

import gnu.java.net.VMAccessorGnuJavaNet;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.SyncFailedException;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public final class VMChannel
{

 private static abstract class Handle
 { /* used by VM classes only */

  Handle() {}

  abstract int getNativeFD()
   throws IOException;

  abstract void setNonBlocking(boolean on)
   throws IOException;

  abstract int available()
   throws IOException;

  abstract int read(byte[] buffer, int off, int len)
   throws IOException;

  abstract void write(byte[] buffer, int[] offArr, int len)
   throws IOException;

  abstract void flush(boolean metadata)
   throws IOException;

  abstract void close()
   throws IOException;

  abstract boolean needsCloseOnFinalize();
 }

 private static final class FileHandle extends Handle
 { /* used by VM classes only */

  private int fd = -1;

  private final int mode;

  private boolean isNonBlocking;

  FileHandle(int mode)
  {
   this.mode = mode;
  }

  int getNativeFD()
   throws IOException
  {
   int fd = this.fd;
   if (fd == -1)
    throw new IOException("invalid file descriptor");
   return fd;
  }

  void setNonBlocking(boolean on)
   throws IOException
  {
   getNativeFD();
   isNonBlocking = on;
  }

  synchronized int available()
   throws IOException
  {
   int fd = getNativeFD();
   return (mode & FileChannelImpl.READ) != 0 ? fileAvailable(fd) : 0;
  }

  synchronized int read(byte[] buffer, int off, int len)
   throws IOException
  {
   int fd = getNativeFD();
   if ((mode & FileChannelImpl.READ) == 0)
    throw new NonReadableChannelException();
   return fileRead(fd, buffer, off, len, isNonBlocking);
  }

  synchronized void write(byte[] buffer, int[] offArr, int len)
   throws IOException
  {
   int fd = getNativeFD();
   if ((mode & FileChannelImpl.WRITE) == 0)
    throw new NonWritableChannelException();
   fileWrite(fd, buffer, offArr, len, isNonBlocking);
  }

  void flush(boolean metadata)
   throws IOException
  {
   int fd = getNativeFD();
   if ((mode & FileChannelImpl.WRITE) != 0)
    fileFlush(fd, metadata);
  }

  void close()
   throws IOException
  {
   int fd = this.fd;
   this.fd = -1;
   VMChannel.close(fd);
  }

  boolean needsCloseOnFinalize()
  {
   return !isStdInOutErr(fd);
  }

  void setNativeFD(int fd)
   throws IOException
  {
   if (this.fd != -1)
    throw new IOException("file descriptor already initialized");
   this.fd = fd;
  }

  synchronized long position()
   throws IOException
  {
   return filePosition(getNativeFD());
  }

  synchronized void seek(long newPosition)
   throws IOException
  {
   fileSeek(getNativeFD(), newPosition);
  }

  synchronized void truncate(long size)
   throws IOException
  {
   fileTruncate(getNativeFD(), size);
  }

  synchronized long size()
   throws IOException
  {
   return fileSize(getNativeFD());
  }

  synchronized boolean lock(long pos, long len, boolean shared, boolean wait)
   throws IOException
  {
   return fileLock(getNativeFD(), pos, len, shared, wait);
  }

  synchronized void unlock(long pos, long len)
   throws IOException
  {
   fileUnlock(getNativeFD(), pos, len);
  }
 }

 private static final class SocketHandle extends Handle
 { /* used by VM classes only */

  private int fd = -1;

  private final boolean stream;

  private boolean isNonBlocking;

  private boolean isConnectPending;

  private InetSocketAddress peerSocketAddress;

  SocketHandle(boolean stream)
  {
   this.stream = stream;
  }

  int getNativeFD()
   throws IOException
  {
   int fd = this.fd;
   if (fd == -1)
    throw new IOException("invalid file descriptor");
   return fd;
  }

  void setNonBlocking(boolean on)
   throws IOException
  {
   int fd = getNativeFD();
   if (isNonBlocking != on)
   {
    VMAccessorGnuJavaNet.socketSetNonBlockingVMPlainSocketImpl(fd, on);
    isNonBlocking = on;
   }
  }

  int available()
   throws IOException
  {
   return VMAccessorGnuJavaNet.socketAvailableVMPlainSocketImpl(
           getNativeFD());
  }

  int read(byte[] buffer, int off, int len)
   throws IOException
  {
   int fd = getNativeFD();
   if (peerSocketAddress == null || isConnectPending)
    throw new SocketException("not connected");
   int[] offArr = { off };
   VMAccessorGnuJavaNet.socketRecvFromVMPlainSocketImpl(fd, buffer, offArr,
    len, false, false, false, stream, isNonBlocking);
   return offArr[0] - off;
  }

  void write(byte[] buffer, int[] offArr, int len)
   throws IOException
  {
   int fd = getNativeFD();
   if (peerSocketAddress == null || isConnectPending)
    throw new SocketException("not connected");
   VMAccessorGnuJavaNet.socketSendToVMPlainSocketImpl(fd, buffer, offArr, len,
    null, false, isNonBlocking);
  }

  void flush(boolean metadata)
   throws IOException
  {
   throw new IOException("not a file");
  }

  void close()
   throws IOException
  {
   int fd = this.fd;
   this.fd = -1;
   if (fd != -1)
    VMAccessorGnuJavaNet.socketCloseVMPlainSocketImpl(fd);
  }

  boolean needsCloseOnFinalize()
  {
   return true;
  }

  void setNativeFD(int fd)
   throws IOException
  {
   if (this.fd != -1)
    throw new IOException("file descriptor already initialized");
   this.fd = fd;
  }

  InetSocketAddress receive(byte[] buffer, int[] offArr, int len)
   throws IOException
  {
   return VMAccessorGnuJavaNet.socketRecvFromVMPlainSocketImpl(
           getDatagramSocketFD(), buffer, offArr, len, false, false, true,
           stream, isNonBlocking);
  }

  void send(byte[] buffer, int[] offArr, int len, InetSocketAddress address)
   throws IOException
  {
   VMAccessorGnuJavaNet.socketSendToVMPlainSocketImpl(getDatagramSocketFD(),
    buffer, offArr, len, address, false, isNonBlocking);
  }

  VMChannel accept()
   throws IOException
  {
   int[] fdArr = { getNativeFD() };
   if (!stream)
    throw new SocketException("not a stream socket");
   InetSocketAddress address =
    VMAccessorGnuJavaNet.socketAcceptVMPlainSocketImpl(fdArr, isNonBlocking);
   VMChannel ch = null;
   if (address != null)
   {
    try
    {
     ch = new VMChannel();
    }
    catch (OutOfMemoryError e)
    {
     if (fdArr[0] != -1)
     {
      try
      {
       VMAccessorGnuJavaNet.socketCloseVMPlainSocketImpl(fdArr[0]);
      }
      catch (IOException ex) {}
     }
     throw e;
    }
    State chState = ch.getState();
    chState.setNativeSocketFD(fdArr[0], true);
    chState.getSocketHandle().peerSocketAddress = address;
   }
   return ch;
  }

  boolean connect(InetSocketAddress address, int timeout)
   throws IOException
  {
   if (address == null)
    throw new NullPointerException();
   int fd = getNativeFD();
   if (peerSocketAddress != null)
    throw new SocketException(isConnectPending ? "connection pending" :
           "already connected");
   boolean completed = false;
   try
   {
    isConnectPending = true;
    peerSocketAddress = address;
    if (VMAccessorGnuJavaNet.socketConnectVMPlainSocketImpl(fd, address,
        isNonBlocking ? 0 : timeout != 0 ? timeout : -1))
     isConnectPending = false;
    completed = true;
   }
   finally
   {
    if (!completed)
    {
     peerSocketAddress = null;
     isConnectPending = false;
    }
   }
   return !isConnectPending;
  }

  InetSocketAddress getPeerSocketAddress()
   throws IOException
  {
   InetSocketAddress address = peerSocketAddress;
   if (address != null && isConnectPending)
   {
    try
    {
     if (!VMAccessorGnuJavaNet.socketConnectVMPlainSocketImpl(getNativeFD(),
         address, 0))
      return null;
    }
    catch (IOException e)
    {
     peerSocketAddress = null;
     isConnectPending = false;
     throw e;
    }
    isConnectPending = false;
   }
   return address;
  }

  void disconnect()
   throws IOException
  {
   VMAccessorGnuJavaNet.socketDisconnectVMPlainSocketImpl(
    getDatagramSocketFD());
   peerSocketAddress = null;
  }

  private int getDatagramSocketFD()
   throws IOException
  {
   int fd = this.fd;
   if (fd == -1)
    throw new IOException("invalid file descriptor");
   if (stream)
    throw new SocketException("not a datagram socket");
   return fd;
  }
 }

 public final class State
 {

  private Handle handle;

  private boolean closed;

  State() {}

  public boolean isValid()
  {
   return handle != null;
  }

  public boolean isClosed()
  {
   return closed;
  }

  public int getNativeFD()
   throws IOException
  {
   return getHandle().getNativeFD();
  }

  void setNativeFD(int fileFd)
   throws IOException
  {
   setNativeFileFD(fileFd, FileChannelImpl.READ | FileChannelImpl.WRITE);
  }

  public void close()
   throws IOException
  {
   Handle handle = getHandle();
   this.handle = null;
   closed = true;
   handle.close();
  }

  public String toString()
  {
   if (closed)
    return "<<closed>>";
   Handle handle = this.handle;
   if (handle != null)
   {
    try
    {
     return String.valueOf(handle.getNativeFD());
    }
    catch (IOException e) {}
   }
   return "<<invalid>>";
  }

  final Handle getHandle()
   throws IOException
  { /* used by VM classes only */
   Handle handle = this.handle;
   if (handle == null)
    throw new IOException("invalid file descriptor");
   return handle;
  }

  final void setNativeFileFD(int fd, int mode)
   throws IOException
  { /* used by VM classes only */
   checkUnset();
   FileHandle handle;
   try
   {
    handle = new FileHandle(mode);
   }
   catch (OutOfMemoryError e)
   {
    try
    {
     VMChannel.close(fd);
    }
    catch (IOException ex) {}
    throw e;
   }
   handle.setNativeFD(fd);
   this.handle = handle;
  }

  final FileHandle getFileHandle()
   throws IOException
  { /* used by VM classes only */
   Handle handle = this.handle;
   if (!(handle instanceof FileHandle))
    throw new IOException(handle != null ? "not a file" :
           "invalid file descriptor");
   return (FileHandle) handle;
  }

  final void setNativeSocketFD(int fd, boolean stream)
   throws IOException
  { /* used by VM classes only */
   checkUnset();
   SocketHandle handle;
   try
   {
    handle = new SocketHandle(stream);
   }
   catch (OutOfMemoryError e)
   {
    if (fd != -1)
    {
     try
     {
      VMAccessorGnuJavaNet.socketCloseVMPlainSocketImpl(fd);
     }
     catch (IOException ex) {}
    }
    throw e;
   }
   handle.setNativeFD(fd);
   this.handle = handle;
  }

  final SocketHandle getSocketHandle()
   throws IOException
  { /* used by VM classes only */
   Handle handle = this.handle;
   if (!(handle instanceof SocketHandle))
    throw new IOException(handle != null ? "not a socket" :
           "invalid file descriptor");
   return (SocketHandle) handle;
  }

  private void checkUnset()
   throws IOException
  {
   if (handle != null || closed)
    throw new IOException("file descriptor already initialized");
  }

  protected void finalize()
   throws Throwable
  {
   Handle handle = this.handle;
   if (handle != null && handle.needsCloseOnFinalize())
    close();
  }
 }

 private static final byte[] EMPTY_BUF = {};

 private static final int stdin_fd = getStdinFD0();

 private static final int stdout_fd = getStdoutFD0();

 private static final int stderr_fd = getStderrFD0();

 private static final boolean lockingOpHasPos = lockingOpHasPos0() != 0;

 private static final boolean preventBlocking =
  VMAccessorJavaLang.preventIOBlockingVMRuntime();

 private final State state = new State();

 public VMChannel() {}

 VMChannel(int fileFd)
  throws IOException
 {
  state.setNativeFD(fileFd);
 }

 public State getState()
 {
  return state;
 }

 public static VMChannel getStdin()
  throws IOException
 {
  VMChannel ch = new VMChannel();
  ch.state.setNativeFileFD(stdin_fd, FileChannelImpl.READ);
  return ch;
 }

 public static VMChannel getStdout()
  throws IOException
 {
  VMChannel ch = new VMChannel();
  ch.state.setNativeFileFD(stdout_fd, FileChannelImpl.WRITE);
  return ch;
 }

 public static VMChannel getStderr()
  throws IOException
 {
  VMChannel ch = new VMChannel();
  ch.state.setNativeFileFD(stderr_fd, FileChannelImpl.WRITE);
  return ch;
 }

 public void setBlocking(boolean blocking)
  throws IOException
 {
  state.getHandle().setNonBlocking(!blocking);
 }

 public int available()
  throws IOException
 {
  return state.getHandle().available();
 }

 public int read(ByteBuffer dst)
  throws IOException
 {
  int len = dst.remaining();
  byte[] buffer;
  int off;
  boolean hasArray = false;
  if (dst.hasArray())
  {
   buffer = dst.array();
   off = dst.arrayOffset() + dst.position();
   hasArray = true;
  }
   else
   {
    buffer = len > 0 ? new byte[len] : EMPTY_BUF;
    off = 0;
   }
  int res = state.getHandle().read(buffer, off, len);
  if (res > 0)
  {
   if (hasArray)
    dst.position(dst.position() + res);
    else dst.put(buffer, 0, res);
  }
  return res;
 }

 public long readScattering(ByteBuffer[] dsts, int offset, int length)
  throws IOException
 {
  if ((offset | length) < 0 || dsts.length - offset < length)
   throw new IndexOutOfBoundsException();
  long result = 0L;
  while (length-- > 0)
  {
   ByteBuffer dst = dsts[offset++];
   int res = read(dst);
   if (result == 0L || res >= 0)
    result += res;
   if (res <= 0 && (res != 0 || dst.remaining() > 0))
    break;
  }
  return result;
 }

 public int read()
  throws IOException
 {
  byte[] buffer = new byte[1];
  int res;
  while ((res = state.getHandle().read(buffer, 0, 1)) == 0)
  {
   threadYield();
   checkThreadInterrupted();
  }
  return res > 0 ? buffer[0] & 0xff : -1;
 }

 public int write(ByteBuffer src)
  throws IOException
 {
  int len = src.remaining();
  byte[] buffer;
  int off;
  if (src.hasArray())
  {
   buffer = src.array();
   off = src.arrayOffset() + src.position();
  }
   else
   {
    buffer = len > 0 ? new byte[len] : EMPTY_BUF;
    src.get(buffer, 0, len);
    src.position(src.position() - len);
    off = 0;
   }
  int[] offArr = { off };
  try
  {
   state.getHandle().write(buffer, offArr, len);
  }
  finally
  {
   if ((len = offArr[0] - off) > 0)
    src.position(src.position() + len);
  }
  return len;
 }

 public long writeGathering(ByteBuffer[] srcs, int offset, int length)
  throws IOException
 {
  if ((offset | length) < 0 || srcs.length - offset < length)
   throw new IndexOutOfBoundsException();
  long result = 0L;
  while (length-- > 0)
  {
   ByteBuffer src = srcs[offset++];
   int res = write(src);
   if (res <= 0 && src.remaining() > 0)
    break;
   result += res;
  }
  return result;
 }

 public void write(int b)
  throws IOException
 {
  byte[] buffer = { (byte) b };
  int[] offArr = new int[1];
  do
  {
   state.getHandle().write(buffer, offArr, 1);
   if (offArr[0] > 0)
    break;
   threadYield();
   checkThreadInterrupted();
  } while (true);
 }

 public SocketAddress receive(ByteBuffer dst)
  throws IOException
 {
  int len = dst.remaining();
  byte[] buffer;
  int off;
  boolean hasArray = false;
  if (dst.hasArray())
  {
   buffer = dst.array();
   off = dst.arrayOffset() + dst.position();
   hasArray = true;
  }
   else
   {
    buffer = len > 0 ? new byte[len] : EMPTY_BUF;
    off = 0;
   }
  int[] offArr = { off };
  InetSocketAddress address;
  try
  {
   address = state.getSocketHandle().receive(buffer, offArr, len);
  }
  finally
  {
   if ((len = offArr[0] - off) > 0)
   {
    if (hasArray)
     dst.position(dst.position() + len);
     else dst.put(buffer, 0, len);
   }
  }
  return address;
 }

 public int send(ByteBuffer src, InetSocketAddress address)
  throws IOException
 {
  int len = src.remaining();
  if (address == null)
   throw new NullPointerException();
  byte[] buffer;
  int off;
  if (src.hasArray())
  {
   buffer = src.array();
   off = src.arrayOffset() + src.position();
  }
   else
   {
    buffer = len > 0 ? new byte[len] : EMPTY_BUF;
    src.get(buffer, 0, len);
    src.position(src.position() - len);
    off = 0;
   }
  int[] offArr = { off };
  try
  {
   state.getSocketHandle().send(buffer, offArr, len, address);
  }
  finally
  {
   if ((len = offArr[0] - off) > 0)
    src.position(src.position() + len);
  }
  return len;
 }

 public void initSocket(boolean stream)
  throws IOException
 {
  if (state.isValid() || state.isClosed())
   throw new IOException("cannot reinitialize this channel");
  state.setNativeSocketFD(VMAccessorGnuJavaNet.socketCreateVMPlainSocketImpl(
   stream), stream);
 }

 public boolean connect(InetSocketAddress address, int timeout)
  throws SocketException
 {
  try
  {
   return state.getSocketHandle().connect(address, timeout);
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

 public void disconnect()
  throws IOException
 {
  state.getSocketHandle().disconnect();
 }

 public InetSocketAddress getLocalAddress()
  throws IOException
 {
  return state.isValid() ?
          VMAccessorGnuJavaNet.socketGetLocalAddrPortVMPlainSocketImpl(
          state.getSocketHandle().getNativeFD()) : null;
 }

 public InetSocketAddress getPeerAddress()
  throws IOException
 {
  return state.isValid() ? state.getSocketHandle().getPeerSocketAddress() :
          null;
 }

 public VMChannel accept()
  throws IOException
 {
  return state.getSocketHandle().accept();
 }

 public void openFile(String path, int mode)
  throws IOException
 {
  if (state.isValid() || state.isClosed())
   throw new IOException("cannot reinitialize this channel");
  int[] fdArr = new int[1];
  checkIOResCode(fileOpen(path, mode, fdArr));
  state.setNativeFileFD(fdArr[0], mode);
 }

 public long position()
  throws IOException
 {
  return state.getFileHandle().position();
 }

 public void seek(long newPosition)
  throws IOException
 {
  state.getFileHandle().seek(newPosition);
 }

 public void truncate(long size)
  throws IOException
 {
  state.getFileHandle().truncate(size);
 }

 public long size()
  throws IOException
 {
  return state.getFileHandle().size();
 }

 public boolean lock(long pos, long len, boolean shared, boolean wait)
  throws IOException
 {
  return wait && lockingOpHasPos ?
          fileLock(state.getFileHandle().getNativeFD(), pos, len, shared,
          true) : state.getFileHandle().lock(pos, len, shared, wait);
 }

 public void unlock(long pos, long len)
  throws IOException
 {
  state.getFileHandle().unlock(pos, len);
 }

 public MappedByteBuffer map(char mapMode, long pos, int len)
   throws IOException
 {
  /* not implemented */
  state.getFileHandle();
  throw new IOException("VMChannel.map() not implemented");
 }

 public boolean flush(boolean metadata)
  throws IOException
 {
  state.getHandle().flush(metadata);
  return true;
 }

 public void close()
  throws IOException
 {
  state.close();
 }

 static final void close(int fileFd)
  throws IOException
 { /* used by VM classes only */
  if (fileFd != -1 && !isStdInOutErr(fileFd))
  {
   int res;
   do
   {
    res = fileClose0(fileFd);
   } while (res < 0 && isIOErrorInterrupted0(res) != 0);
   checkIOResCode(res);
  }
 }

 static final boolean isStdInOutErr(int fileFd)
 { /* used by VM classes only */
  return fileFd == stdin_fd || fileFd == stdout_fd || fileFd == stderr_fd;
 }

 static final int fileAvailable(int fd)
  throws IOException
 { /* used by VM classes only */
  int res;
  do
  {
   res = fileAvailable0(fd);
   if (res >= 0)
    return res;
  } while (isIOErrorInterrupted0(res) != 0);
  long position;
  do
  {
   position = fileSeek0(0L, fd, 0);
  } while (position < 0L && isIOErrorInterrupted0((int) position) != 0);
  if (position >= 0L)
  {
   long size;
   do
   {
    size = fileSeek0(0L, fd, -1);
   } while (size < 0L && isIOErrorInterrupted0((int) size) != 0);
   if (position != size)
    fileSeek(fd, position);
   if (size < 0L)
    checkIOResCode((int) size);
   position = size - position;
   return position > 0L ? (position < (long) (-1 >>> 1) ?
           (int) position : -1 >>> 1) : 0;
  }
  do
  {
   res = fileSelect0(fd, 0);
  } while (res < 0 && isIOErrorInterrupted0(res) != 0);
  return res > 0 ? 1 : 0;
 }

 static final long filePosition(int fd)
  throws IOException
 { /* used by VM classes only */
  long position;
  do
  {
   position = fileSeek0(0L, fd, 0);
  } while (position < 0L && isIOErrorInterrupted0((int) position) != 0);
  if (position < 0L)
   checkIOResCode((int) position);
  return position;
 }

 static final void fileSeek(int fd, long newPosition)
  throws IOException
 { /* used by VM classes only */
  long position;
  do
  {
   position = fileSeek0(newPosition, fd, 1);
  } while (position < 0L && isIOErrorInterrupted0((int) position) != 0);
  if (position < 0L)
   checkIOResCode((int) position);
  if (position != newPosition)
   throw new IOException("seek() failed");
 }

 static final void fileTruncate(int fd, long size)
  throws IOException
 { /* used by VM classes only */
  long position = filePosition(fd);
  try
  {
   if (position != size)
    fileSeek(fd, size);
   checkIOResCode(fileWrite0(EMPTY_BUF, 0, 0, fd));
   if (position >= size)
    position = size;
  }
  finally
  {
   if (position != size)
    fileSeek(fd, position);
  }
 }

 static final long fileSize(int fd)
  throws IOException
 { /* used by VM classes only */
  long position = filePosition(fd);
  long size;
  do
  {
   size = fileSeek0(0L, fd, -1);
  } while (size < 0L && isIOErrorInterrupted0((int) size) != 0);
  if (position != size)
   fileSeek(fd, position);
  if (size < 0L)
   checkIOResCode((int) size);
  return size;
 }

 static final boolean fileLock(int fd, long pos, long len, boolean shared,
   boolean wait)
  throws IOException
 { /* used by VM classes only */
  long position;
  if (lockingOpHasPos || (position = filePosition(fd)) == pos)
   return fileLockInner(pos, len, fd, shared, wait);
  boolean res;
  try
  {
   fileSeek(fd, pos);
   res = fileLockInner(pos, len, fd, shared, wait);
  }
  finally
  {
   fileSeek(fd, position);
  }
  return res;
 }

 static final void fileUnlock(int fd, long pos, long len)
  throws IOException
 { /* used by VM classes only */
  long position;
  if (lockingOpHasPos || (position = filePosition(fd)) == pos)
   fileUnlockInner(pos, len, fd);
   else
   {
    try
    {
     fileSeek(fd, pos);
     fileUnlockInner(pos, len, fd);
    }
    finally
    {
     fileSeek(fd, position);
    }
   }
 }

 static final int fileRead(int fd, byte[] buffer, int off, int len,
   boolean isNonBlocking)
  throws IOException
 { /* used by VM classes only */
  if ((off | len) < 0 || buffer.length - off < len)
   throw new ArrayIndexOutOfBoundsException();
  int res = 0;
  if (len > 0 && (!isNonBlocking || fileSelect0(fd, 0) != 0))
  {
   if (isNonBlocking)
   {
    res = fileRead0(buffer, off, len, fd);
    if (res < 0)
    {
     if (isIOErrorInterrupted0(res) == 0)
      checkIOResCode(res);
     res = 0;
    }
     else if (res == 0)
      res = -1;
   }
    else
    {
     checkThreadInterrupted();
     do
     {
      if (!preventBlocking || fileSelect0(fd, 0) != 0)
      {
       res = fileRead0(buffer, off, len, fd);
       if (res >= 0 || isIOErrorInterrupted0(res) == 0)
        break;
      }
      threadYield();
      checkThreadInterrupted();
     } while (true);
     checkIOResCode(res);
     if (res == 0)
      res = -1;
    }
   if (res > len)
    throw new InternalError("read() fault");
  }
  return res;
 }

 static final void fileWrite(int fd, byte[] buffer, int[] offArr, int len,
   boolean isNonBlocking)
  throws IOException
 { /* used by VM classes only */
  int off = offArr[0];
  if ((off | len) < 0 || buffer.length - off < len)
   throw new ArrayIndexOutOfBoundsException();
  if (len > 0 && (!isNonBlocking || fileSelect0(fd, 1) != 0))
  {
   int remain = len;
   if (!isNonBlocking)
    checkThreadInterrupted();
   do
   {
    if (isNonBlocking || !preventBlocking || fileSelect0(fd, 1) != 0)
    {
     int res = fileWrite0(buffer, off, remain, fd);
     if (res > 0)
     {
      off += res;
      offArr[0] = off;
      if ((remain -= res) <= 0)
       break;
     }
      else
      {
       if (res == 0)
        throw new IOException("no space left on device");
       if (isIOErrorInterrupted0(res) == 0)
        checkIOResCode(res);
      }
     if (isNonBlocking)
      break;
    }
    threadYield();
    if (remain == len)
     checkThreadInterrupted();
   } while (true);
   if (remain < 0)
    throw new InternalError("write() fault");
  }
 }

 static final void fileFlush(int fd, boolean metadata)
  throws IOException
 { /* used by VM classes only */
  do
  {
   int res = fileFlush0(fd, metadata ? 1 : 0);
   if (res >= 0)
    break;
   if (isIOErrorInterrupted0(res) == 0)
   {
    if (isStdInOutErr(fd))
     break;
    throw new SyncFailedException(getIOErrorMsg0(res));
   }
   threadYield();
  } while (true);
 }

 final void setNativeFileFD(int fd, int mode)
  throws IOException
 { /* used by VM classes only */
  state.setNativeFileFD(fd, mode);
 }

 static final VMChannel createUnlessExists(File file)
  throws IOException
 { /* used by VM classes only */
  VMChannel ch = new VMChannel();
  int mode = FileChannelImpl.WRITE | FileChannelImpl.EXCL;
  int[] fdArr = new int[1];
  int res = fileOpen(file.getPath(), mode, fdArr);
  if (res < 0)
  {
   if (isIOErrorFileExists0(res) == 0)
    checkIOResCode(res);
   return null;
  }
  ch.setNativeFileFD(fdArr[0], mode);
  return ch;
 }

 static final void checkIOResCode(int res)
  throws IOException
 { /* used by VM classes only */
  if (res < 0)
   throw new IOException(getIOErrorMsg0(res));
 }

 static final boolean isIORetryNeededOnce(int res)
 { /* used by VM classes only */
  if (res >= 0 || isIOErrorNoResources0(res) == 0)
   return false;
  VMAccessorJavaLang.gcOnNoResourcesVMRuntime();
  Runtime.getRuntime().runFinalization();
  return true;
 }

 static boolean isThreadInterrupted()
 {
  return Thread.currentThread().isInterrupted();
 }

 private static void checkThreadInterrupted()
  throws IOException
 {
  if (Thread.interrupted())
   throw new InterruptedIOException();
 }

 private static void threadYield()
 {
  Thread.yield();
 }

 private static int fileOpen(String path, int mode, int[] fdArr)
 {
  if (path == null)
   throw new NullPointerException();
  int res;
  boolean retrying = false;
  do
  {
   res = fileOpen0(fdArr, path, mode);
   if (!retrying && isIORetryNeededOnce(res))
    retrying = true;
    else if (res >= 0 || isIOErrorInterrupted0(res) == 0)
     break;
  } while (true);
  return res;
 }

 private static boolean fileLockInner(long pos, long len, int fd,
   boolean shared, boolean wait)
  throws IOException
 {
  boolean retrying = false;
  do
  {
   int res = fileLock0(pos, len, fd, shared ? 1 : 0, wait ? 1 : 0);
   if (!retrying && isIORetryNeededOnce(res))
    retrying = true;
    else
    {
     if (res >= 0)
      break;
     if (isIOErrorInterrupted0(res) == 0)
      checkIOResCode(res);
     if (!wait)
      return false;
     threadYield();
    }
  } while (true);
  return true;
 }

 private static void fileUnlockInner(long pos, long len, int fd)
  throws IOException
 {
  int res;
  while ((res = fileLock0(pos, len, fd, -1, 0)) < 0)
  {
   if (isIOErrorInterrupted0(res) == 0)
    checkIOResCode(res);
   threadYield();
  }
 }

 private static native int getStdinFD0();

 private static native int getStdoutFD0();

 private static native int getStderrFD0();

 private static native int isIOErrorFileExists0(int res);

 private static native int isIOErrorNoResources0(int res);

 private static native int isIOErrorInterrupted0(int res);

 private static native String getIOErrorMsg0(int res);

 private static native int fileOpen0(int[] fdArr, String path, int mode);

 private static native int fileRead0(byte[] buffer, int off, int len,
  int fd); /* blocking syscall */

 private static native int fileWrite0(byte[] buffer, int off, int len,
  int fd); /* blocking syscall */

 private static native int fileAvailable0(int fd);

 private static native int fileSelect0(int fd, int iswrite);

 private static native long fileSeek0(long ofs, int fd, int direction);

 private static native int lockingOpHasPos0();

 private static native int fileLock0(long pos, long len, int fd,
   int sharedOrUnlock, int doWait); /* blocking syscall */

 private static native int fileFlush0(int fd,
   int metadata); /* blocking syscall */

 private static native int fileClose0(int fd);
}

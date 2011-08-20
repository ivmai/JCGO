/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Class root location: $(JCGO)/goclsp/clsp_fix
 * Origin: GNU Classpath v0.93
 */

/* FileChannelImpl.java --
   Copyright (C) 2002, 2004, 2005, 2006  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.java.nio;

import gnu.classpath.Configuration;
import gnu.java.nio.FileLockImpl;
import gnu.java.nio.VMChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This file is not user visible !
 * But alas, Java does not have a concept of friendly packages
 * so this class is public.
 * Instances of this class are created by invoking getChannel
 * Upon a Input/Output/RandomAccessFile object.
 */
public final class FileChannelImpl extends FileChannel
{

  private static final class FileLockTable
  {

    private final ArrayList lockList = new ArrayList();

    FileLockTable()
    {
    }

    synchronized boolean overlaps(long pos, long len)
    {
      Iterator it = lockList.iterator();
      while (it.hasNext())
        if (lockOverlaps((long[])it.next(), pos, len))
          return true;
      return false;
    }

    private static boolean lockOverlaps(long[] posLenArr, long pos, long len)
    {
      long lockPos = posLenArr[0];
      return pos < lockPos + posLenArr[1] && pos + len > lockPos;
    }

    synchronized void add(long pos, long len)
    {
      lockList.add(new long[] { pos, len });
    }

    synchronized boolean remove(long pos)
    {
      int i = lockList.size();
      while (i-- > 0)
        if (((long[])lockList.get(i))[0] == pos)
          {
            lockList.remove(i);
            return true;
          }
      return false;
    }

    synchronized void unlockAll(VMChannel ch)
    {
      int i = lockList.size();
      while (i-- > 0)
        {
          long[] posLenArr = (long[])lockList.get(i);
          try
            {
              ch.unlock(posLenArr[0], posLenArr[1]);
            }
          catch (IOException e)
            {
            }
        }
      lockList.clear();
    }
  }

  // These are mode values for open().
  public static final int READ   = 1;
  public static final int WRITE  = 2;
  public static final int APPEND = 4;

  // EXCL is used only when making a temp file.
  public static final int EXCL   = 8;
  public static final int SYNC   = 16;
  public static final int DSYNC  = 32;

  //private static native void init();

  static
  {
    if (Configuration.INIT_LOAD_LIBRARY)
      {
        System.loadLibrary("javanio");
      }

    //init();
  }

  public static final FileChannelImpl err = makeStdOutErrChannel(true);
  public static final FileChannelImpl out = makeStdOutErrChannel(false);
  public static final FileChannelImpl in = makeStdinChannel();

  private static FileChannelImpl makeStdinChannel()
  {
    try
      {
        return new FileChannelImpl(VMChannel.getStdin(), READ);
      }
    catch (IOException ioe)
      {
        throw (InternalError) (new InternalError()).initCause(ioe);
      }
  }

  private static FileChannelImpl makeStdOutErrChannel(boolean isStdErr)
  {
    try
      {
        return new FileChannelImpl(isStdErr ? VMChannel.getStderr() :
                VMChannel.getStdout(), WRITE);
      }
    catch (IOException ioe)
      {
        throw (InternalError) (new InternalError()).initCause(ioe);
      }
  }

  /**
   * This is the actual native file descriptor value
   */
  private final VMChannel ch;

  private final int mode;

  final String description;

  private volatile FileLockTable lockTable;

  /* Open a file.  MODE is a combination of the above mode flags. */
  /* This is a static factory method, so that VM implementors can decide
   * substitute subclasses of FileChannelImpl. */
  public static FileChannelImpl create(File file, int mode)
    throws IOException
  {
    return new FileChannelImpl(file, mode);
  }

  private FileChannelImpl(File file, int mode)
    throws IOException
  {
    String path = file.getPath();
    description = path;
    this.mode = mode;
    this.ch = new VMChannel();
    ch.openFile(path, mode);

    // First open the file and then check if it is a directory
    // to avoid race condition.
    if ((mode & WRITE) == 0 && file.isDirectory())
      {
        try
          {
            close();
          }
        catch (IOException e)
          {
            /* ignore it */
          }

        throw new FileNotFoundException(path + " is a directory");
      }
  }

  /**
   * Constructor for default channels in, out and err.
   *
   * Used by init() (native code).
   *
   * @param fd the file descriptor (0, 1, 2 for stdin, stdout, stderr).
   *
   * @param mode READ or WRITE
   */
  FileChannelImpl (VMChannel ch, int mode)
  {
    this.mode = mode;
    this.ch = ch;
    description = "descriptor#" + ch.getState();
  }

  public int available() throws IOException
  {
    return ch.available();
  }

  private long implPosition() throws IOException
  {
    return ch.position();
  }

  private void seek(long newPosition) throws IOException
  {
    ch.seek(newPosition);
  }

  private void implTruncate(long size) throws IOException
  {
    ch.truncate(size);
  }

  /* public */ void unlock(long pos, long len) throws IOException
  {
    if (lockTable != null && lockTable.remove(pos))
      ch.unlock(pos, len);
  }

  public long size () throws IOException
  {
    return ch.size();
  }

  protected void implCloseChannel() throws IOException
  {
    if (lockTable != null)
      lockTable.unlockAll(ch);
    ch.close();
  }

  /**
   * Makes sure the Channel is properly closed.
   */
  /* protected void finalize() throws IOException
  {
    if (ch.getState().isValid())
      close();
  } */

  public int read (ByteBuffer dst) throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & READ) == 0)
       throw new NonReadableChannelException ();

    boolean completed = false;
    int result;
    try
      {
        begin();
        result = ch.read(dst);
        completed = true;
      }
    finally
      {
        end(completed);
      }

    return result;
  }

  public int read (ByteBuffer dst, long position)
    throws IOException
  {
    if (position < 0)
      throw new IllegalArgumentException ("position: " + position);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & READ) == 0)
       throw new NonReadableChannelException ();

    boolean completed = false;
    int result;
    try
      {
        begin();
        long oldPosition = implPosition ();
        position (position);
        result = ch.read(dst);
        position (oldPosition);
        completed = true;
      }
    finally
      {
        end(completed);
      }

    return result;
  }

  public int read() throws IOException
  {
    return ch.read();
  }

  public long read (ByteBuffer[] dsts, int offset, int length)
    throws IOException
  {
    return ch.readScattering(dsts, offset, length);
  }

  public int write (ByteBuffer src) throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    boolean completed = false;
    int result;
    try
      {
        begin();
        result = ch.write(src);
        completed = true;
      }
    finally
      {
        end(completed);
      }

    return result;
  }

  public int write (ByteBuffer src, long position)
    throws IOException
  {
    if (position < 0)
      throw new IllegalArgumentException ("position: " + position);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    boolean completed = false;
    int result;
    try
      {
        begin();
        long oldPosition = implPosition ();
        seek (position);
        result = ch.write(src);
        seek (oldPosition);
        completed = true;
      }
    finally
      {
        end(completed);
      }

    return result;
  }

  public void write (int b) throws IOException
  {
    ch.write(b);
  }

  public long write(ByteBuffer[] srcs, int offset, int length)
    throws IOException
  {
    return ch.writeGathering(srcs, offset, length);
  }

  public MappedByteBuffer map (FileChannel.MapMode mode,
                               long position, long size)
    throws IOException
  {
    char nmode = 0;
    if (mode == MapMode.READ_ONLY)
      {
        nmode = 'r';
        if ((this.mode & READ) == 0)
          throw new NonReadableChannelException();
      }
    else if (mode == MapMode.READ_WRITE || mode == MapMode.PRIVATE)
      {
        nmode = mode == MapMode.READ_WRITE ? '+' : 'c';
        if ((this.mode & WRITE) != WRITE)
          throw new NonWritableChannelException();
        if ((this.mode & READ) != READ)
          throw new NonReadableChannelException();
      }
    else
      throw new IllegalArgumentException ("mode: " + mode);

    if (((position + size) | position | size) < 0 || size > Integer.MAX_VALUE)
      throw new IllegalArgumentException ("position: " + position
                                          + ", size: " + size);
    return ch.map(nmode, position, (int) size);
  }

  /**
   * msync with the disk
   */
  public void force (boolean metaData) throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();
    boolean completed = false;
    try
      {
        begin();
        ch.flush(metaData);
        completed = true;
      }
    finally
      {
        end(completed);
      }
  }

  // like transferTo, but with a count of less than 2Gbytes
  private int smallTransferTo (long position, int count,
                               WritableByteChannel target)
    throws IOException
  {
    ByteBuffer buffer;
    try
      {
        // Try to use a mapped buffer if we can.  If this fails for
        // any reason we'll fall back to using a ByteBuffer.
        buffer = map (MapMode.READ_ONLY, position, count);
      }
    catch (IOException e)
      {
        buffer = ByteBuffer.allocate (count);
        read (buffer, position);
        buffer.flip();
      }

    return target.write (buffer);
  }

  public long transferTo (long position, long count,
                          WritableByteChannel target)
    throws IOException
  {
    if (((position + count) | position | count) < 0)
      throw new IllegalArgumentException ("position: " + position
                                          + ", count: " + count);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & READ) == 0)
       throw new NonReadableChannelException ();

    final int pageSize = 65536;
    long total = 0;

    while (count > 0)
      {
        int transferred
          = smallTransferTo (position, (int)Math.min (count, pageSize),
                             target);
        if (transferred < 0)
          break;
        total += transferred;
        position += transferred;
        count -= transferred;
      }

    return total;
  }

  // like transferFrom, but with a count of less than 2Gbytes
  private int smallTransferFrom (ReadableByteChannel src, long position,
                                 int count)
    throws IOException
  {
    ByteBuffer buffer = null;

    if (src instanceof FileChannel)
      {
        try
          {
            // Try to use a mapped buffer if we can.  If this fails
            // for any reason we'll fall back to using a ByteBuffer.
            buffer = ((FileChannel)src).map (MapMode.READ_ONLY, position,
                                             count);
          }
        catch (IOException e)
          {
          }
      }

    if (buffer == null)
      {
        buffer = ByteBuffer.allocate (count);
        src.read (buffer);
        buffer.flip();
      }

    return write (buffer, position);
  }

  public long transferFrom (ReadableByteChannel src, long position,
                            long count)
    throws IOException
  {
    if (((position + count) | position | count) < 0)
      throw new IllegalArgumentException ("position: " + position
                                          + ", count: " + count);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    final int pageSize = 65536;
    long total = 0;

    while (count > 0)
      {
        int transferred = smallTransferFrom (src, position,
                                             (int)Math.min (count, pageSize));
        if (transferred < 0)
          break;
        total += transferred;
        position += transferred;
        count -= transferred;
      }

    return total;
  }

  // Shared sanity checks between lock and tryLock methods.
  private void lockCheck(long position, long size, boolean shared)
    throws IOException
  {
    if (((position + size) | position) < 0 || size <= 0L)
      throw new IllegalArgumentException ("position: " + position
                                          + ", size: " + size);

    if (!isOpen ())
      throw new ClosedChannelException();

    if (shared && ((mode & READ) == 0))
      throw new NonReadableChannelException();

    if (!shared && ((mode & WRITE) == 0))
      throw new NonWritableChannelException();

    if (lockTable == null)
      synchronized (ch)
        {
          if (lockTable == null)
            lockTable = new FileLockTable();
        }
    if (lockTable.overlaps(position, size))
      throw new OverlappingFileLockException();
  }

  public FileLock tryLock (long position, long size, boolean shared)
    throws IOException
  {
    lockCheck(position, size, shared);

    if (!ch.lock(position, size, shared, false))
      {
        if (!isOpen ())
          throw new AsynchronousCloseException();
        return null;
      }

    FileLock lock;
    try
      {
        lockTable.add(position, size);
        lock = new FileLockImpl(this, position, size, shared);
      }
    catch (Error e)
      {
        lockTable.remove(position);
        ch.unlock(position, size);
        throw e;
      }
    return lock;
  }

  public FileLock lock (long position, long size, boolean shared)
    throws IOException
  {
    lockCheck(position, size, shared);

    FileLock lock;
    boolean completed = false;
    try
      {
        begin();
        if (!ch.lock(position, size, shared, true))
          {
            if (!isOpen ())
              throw new AsynchronousCloseException();
            throw new FileLockInterruptionException();
          }
        try
          {
            lockTable.add(position, size);
            lock = new FileLockImpl(this, position, size, shared);
          }
        catch (Error e)
          {
            lockTable.remove(position);
            ch.unlock(position, size);
            throw e;
          }
        completed = true;
      }
    finally
      {
        end(completed);
      }

    return lock;
  }

  public long position ()
    throws IOException
  {
    if (!isOpen ())
      throw new ClosedChannelException ();

    return implPosition ();
  }

  public FileChannel position (long newPosition)
    throws IOException
  {
    if (newPosition < 0)
      throw new IllegalArgumentException ("newPosition: " + newPosition);

    if (!isOpen ())
      throw new ClosedChannelException ();

    // FIXME note semantics if seeking beyond eof.
    // We should seek lazily - only on a write.
    seek (newPosition);
    return this;
  }

  public FileChannel truncate (long size)
    throws IOException
  {
    if (size < 0)
      throw new IllegalArgumentException ("size: " + size);

    if (!isOpen ())
      throw new ClosedChannelException ();

    if ((mode & WRITE) == 0)
       throw new NonWritableChannelException ();

    if (size < size ())
      implTruncate (size);

    return this;
  }

  public String toString()
  {
    return (super.toString()
            + "[ fd: " + ch.getState()
            + "; mode: 0" + (mode != 0 ? Integer.toOctalString(mode) : "")
            + "; " + description + " ]");
  }
}

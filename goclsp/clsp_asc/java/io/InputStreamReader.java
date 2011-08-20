/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Class root location: $(JCGO)/goclsp/clsp_asc
 * Origin: GNU Classpath v0.93
 */

/* InputStreamReader.java -- Reader than transforms bytes to chars
   Copyright (C) 1998, 1999, 2001, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.

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


package java.io;

import gnu.classpath.SystemProperties;
import gnu.java.nio.charset.EncodingHelper;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import java.nio.charset.UnsupportedCharsetException;

/**
 * This class reads characters from a byte input stream.   The characters
 * read are converted from bytes in the underlying stream by a
 * decoding layer.  The decoding layer transforms bytes to chars according
 * to an encoding standard.  There are many available encodings to choose
 * from.  The desired encoding can either be specified by name, or if no
 * encoding is selected, the system default encoding will be used.  The
 * system default encoding name is determined from the system property
 * <code>file.encoding</code>.  The only encodings that are guaranteed to
 * be availalbe are "8859_1" (the Latin-1 character set) and "UTF8".
 * Unforunately, Java does not provide a mechanism for listing the
 * ecodings that are supported in a given implementation.
 * <p>
 * Here is a list of standard encoding names that may be available:
 * <p>
 * <ul>
 * <li>8859_1 (ISO-8859-1/Latin-1)</li>
 * <li>8859_2 (ISO-8859-2/Latin-2)</li>
 * <li>8859_3 (ISO-8859-3/Latin-3)</li>
 * <li>8859_4 (ISO-8859-4/Latin-4)</li>
 * <li>8859_5 (ISO-8859-5/Latin-5)</li>
 * <li>8859_6 (ISO-8859-6/Latin-6)</li>
 * <li>8859_7 (ISO-8859-7/Latin-7)</li>
 * <li>8859_8 (ISO-8859-8/Latin-8)</li>
 * <li>8859_9 (ISO-8859-9/Latin-9)</li>
 * <li>ASCII (7-bit ASCII)</li>
 * <li>UTF8 (UCS Transformation Format-8)</li>
 * <li>More later</li>
 * </ul>
 * <p>
 * It is recommended that applications do not use
 * <code>InputStreamReader</code>'s
 * directly.  Rather, for efficiency purposes, an object of this class
 * should be wrapped by a <code>BufferedReader</code>.
 * <p>
 * Due to a deficiency the Java class library design, there is no standard
 * way for an application to install its own byte-character encoding.
 *
 * @see BufferedReader
 * @see InputStream
 *
 * @author Robert Schuster
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @author Per Bothner (bothner@cygnus.com)
 * @date April 22, 1998.
 */
public class InputStreamReader extends Reader
{
  /**
   * The input stream.
   */
  private InputStream in;

  /**
   * The charset decoder.
   */
  /* private CharsetDecoder decoder; */

  /**
   * End of stream reached.
   */
  /* private boolean isDone = false; */

  /**
   * Need this.
   */
  /* private float maxBytesPerChar; */

  /**
   * Buffer holding surplus loaded bytes (if any)
   */
  /* private ByteBuffer byteBuffer; */

  /**
   * java.io canonical name of the encoding.
   */
  /* private String encoding; */
  private int encType;
  private static final int ASCII7 = 1;
  private static final int UTF8 = 2;

  private byte savedMultibyte[];

  /**
   * We might decode to a 2-char UTF-16 surrogate, which won't fit in the
   * output buffer. In this case we need to save the surrogate char.
   */
  /* private char savedSurrogate;
  private boolean hasSavedSurrogate = false; */

  /**
   * A byte array to be reused in read(byte[], int, int).
   */
  private byte[] bytesCache;

  /**
   * Locks the bytesCache above in read(byte[], int, int).
   */
  private Object cacheLock = new Object();

  /**
   * This method initializes a new instance of <code>InputStreamReader</code>
   * to read from the specified stream using the default encoding.
   *
   * @param in The <code>InputStream</code> to read from
   */
  public InputStreamReader(InputStream in)
  {
    if (in == null)
      throw new NullPointerException();
    this.in = in;
    /* try
        {
          encoding = SystemProperties.getProperty("file.encoding");
          // Don't use NIO if avoidable
          if(EncodingHelper.isISOLatin1(encoding))
            {
              encoding = "ISO8859_1";
              maxBytesPerChar = 1f;
              decoder = null;
              return;
            }
          Charset cs = EncodingHelper.getCharset(encoding);
          decoder = cs.newDecoder();
          encoding = EncodingHelper.getOldCanonical(cs.name());
          try {
              maxBytesPerChar = cs.newEncoder().maxBytesPerChar();
          } catch(UnsupportedOperationException _){
              maxBytesPerChar = 1f;
          }
          decoder.onMalformedInput(CodingErrorAction.REPLACE);
          decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
          decoder.reset();
        } catch(RuntimeException e) {
          encoding = "ISO8859_1";
          maxBytesPerChar = 1f;
          decoder = null;
        } catch(UnsupportedEncodingException e) {
          encoding = "ISO8859_1";
          maxBytesPerChar = 1f;
          decoder = null;
        } */
    setEncType(
     /* SystemProperties.getProperty("file.encoding", "") */
     gnu.classpath.VMAccessorGnuClasspath.getFileEncodingVMSystemProperties());
  }

  private boolean setEncType (String encoding)
  {
    if (encoding.equals("ISO8859_1") || encoding.equals("819") ||
        encoding.equals("8859_1") || encoding.equals("CP819") ||
        encoding.equals("IBM819") || encoding.equals("ISO-8859-1") ||
        encoding.equals("ISO_8859-1") || encoding.equals("ISO_8859-1:1987") ||
        encoding.equals("ISO_8859_1") || encoding.equals("csISOLatin1") ||
        encoding.equals("ibm-819") || encoding.equals("iso-ir-100") ||
        encoding.equals("l1") || encoding.equals("latin1")) {
      encType = 0;
      return true;
    }
    if (encoding.equals("US-ASCII") || encoding.equals("646") ||
        encoding.equals("ANSI_X3.4-1968") ||
        encoding.equals("ANSI_X3.4-1986") || encoding.equals("ASCII") ||
        encoding.equals("IBM367") || encoding.equals("ISO646-US") ||
        encoding.equals("ISO_646.irv:1991") || encoding.equals("ascii7") ||
        encoding.equals("cp367") || encoding.equals("csASCII") ||
        encoding.equals("iso-ir-6") || encoding.equals("iso_646.irv:1983") ||
        encoding.equals("us") || encoding.equals("windows-20127")) {
      encType = ASCII7;
      return true;
    }
    if (encoding.equals("UTF-8") || encoding.equals("UTF8") ||
        encoding.equals("cp1208") || encoding.equals("ibm-1208") ||
        encoding.equals("ibm-1209") || encoding.equals("ibm-5304") ||
        encoding.equals("ibm-5305") || encoding.equals("windows-65001")) {
      encType = UTF8;
      return true;
    }
    return false;
  }

  /**
   * This method initializes a new instance of <code>InputStreamReader</code>
   * to read from the specified stream using a caller supplied character
   * encoding scheme.  Note that due to a deficiency in the Java language
   * design, there is no way to determine which encodings are supported.
   *
   * @param in The <code>InputStream</code> to read from
   * @param encoding_name The name of the encoding scheme to use
   *
   * @exception UnsupportedEncodingException If the encoding scheme
   * requested is not available.
   */
  public InputStreamReader(InputStream in, String encoding_name)
    throws UnsupportedEncodingException
  {
    if (in == null
        || encoding_name == null)
      throw new NullPointerException();

    this.in = in;
    // Don't use NIO if avoidable
    /* if(EncodingHelper.isISOLatin1(encoding_name))
      {
        encoding = "ISO8859_1";
        maxBytesPerChar = 1f;
        decoder = null;
        return;
      }

    try {
      Charset cs = EncodingHelper.getCharset(encoding_name);
      try {
        maxBytesPerChar = cs.newEncoder().maxBytesPerChar();
      } catch(UnsupportedOperationException _){
        maxBytesPerChar = 1f;
      }

      decoder = cs.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPLACE);
      decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
      decoder.reset();

      // The encoding should be the old name, if such exists.
      encoding = EncodingHelper.getOldCanonical(cs.name());
    } catch(RuntimeException e) {
      encoding = "ISO8859_1";
      maxBytesPerChar = 1f;
      decoder = null;
    } */
    if (!setEncType(encoding_name))
      throw new UnsupportedEncodingException(encoding_name);
  }

  /**
   * Creates an InputStreamReader that uses a decoder of the given
   * charset to decode the bytes in the InputStream into
   * characters.
   *
   * @since 1.4
   */
  public InputStreamReader(InputStream in, Charset charset) {
    if (in == null)
      throw new NullPointerException();
    this.in = in;
    /* decoder = charset.newDecoder();

    try {
      maxBytesPerChar = charset.newEncoder().maxBytesPerChar();
    } catch(UnsupportedOperationException _){
      maxBytesPerChar = 1f;
    }

    decoder.onMalformedInput(CodingErrorAction.REPLACE);
    decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    decoder.reset();
    encoding = EncodingHelper.getOldCanonical(charset.name()); */
    String encoding = charset.name();
    if (!setEncType(encoding))
      throw new UnsupportedCharsetException(encoding);
  }

  /**
   * Creates an InputStreamReader that uses the given charset decoder
   * to decode the bytes in the InputStream into characters.
   *
   * @since 1.4
   */
  public InputStreamReader(InputStream in, CharsetDecoder decoder) {
    if (in == null)
      throw new NullPointerException();
    this.in = in;
    /* this.decoder = decoder;

    Charset charset = decoder.charset();
    try {
      if (charset == null)
        maxBytesPerChar = 1f;
      else
        maxBytesPerChar = charset.newEncoder().maxBytesPerChar();
    } catch(UnsupportedOperationException _){
        maxBytesPerChar = 1f;
    }

    decoder.onMalformedInput(CodingErrorAction.REPLACE);
    decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    decoder.reset();
    if (charset == null)
      encoding = "US-ASCII";
    else
      encoding = EncodingHelper.getOldCanonical(decoder.charset().name()); */
    String encoding = decoder.charset().name();
    if (!setEncType(encoding))
      throw new UnsupportedCharsetException(encoding);
  }

  /**
   * This method closes this stream, as well as the underlying
   * <code>InputStream</code>.
   *
   * @exception IOException If an error occurs
   */
  public void close() throws IOException
  {
    synchronized (lock)
      {
        // Makes sure all intermediate data is released by the decoder.
        /* if (decoder != null)
           decoder.reset(); */
        if (in != null)
           in.close();
        in = null;
        /* isDone = true;
        decoder = null; */
      }
  }

  /**
   * This method returns the name of the encoding that is currently in use
   * by this object.  If the stream has been closed, this method is allowed
   * to return <code>null</code>.
   *
   * @return The current encoding name
   */
  public String getEncoding()
  {
    /* return in != null ? encoding : null; */
    return in != null ? encType == ASCII7 ? "ASCII" :
            encType == UTF8 ? "UTF-8" : "ISO8859_1" : null;
  }

  /**
   * This method checks to see if the stream is ready to be read.  It
   * will return <code>true</code> if is, or <code>false</code> if it is not.
   * If the stream is not ready to be read, it could (although is not required
   * to) block on the next read attempt.
   *
   * @return <code>true</code> if the stream is ready to be read,
   * <code>false</code> otherwise
   *
   * @exception IOException If an error occurs
   */
  public boolean ready() throws IOException
  {
    if (in == null)
      throw new IOException("Stream closed.");

    return in.available() != 0;
  }

  /**
   * This method reads up to <code>length</code> characters from the stream into
   * the specified array starting at index <code>offset</code> into the
   * array.
   *
   * @param buf The character array to recieve the data read
   * @param offset The offset into the array to start storing characters
   * @param length The requested number of characters to read.
   *
   * @return The actual number of characters read, or -1 if end of stream.
   *
   * @exception IOException If an error occurs
   */
  public int read(char[] buf, int offset, int length) throws IOException
  {
    if (in == null)
      throw new IOException("Stream closed.");
    /* if (isDone)
      return -1;
    if(decoder != null)
      {
        int totalBytes = (int)((double) length * maxBytesPerChar);
        if (byteBuffer != null)
          totalBytes = Math.max(totalBytes, byteBuffer.remaining());
        byte[] bytes;
        // Fetch cached bytes array if available and big enough.
        synchronized(cacheLock)
          {
            bytes = bytesCache;
            if (bytes == null || bytes.length < totalBytes)
              bytes = new byte[totalBytes];
            else
              bytesCache = null;
          }

        int remaining = 0;
        if(byteBuffer != null)
        {
            remaining = byteBuffer.remaining();
            byteBuffer.get(bytes, 0, remaining);
        }
        int read;
        if(totalBytes - remaining > 0)
          {
            read = in.read(bytes, remaining, totalBytes - remaining);
            if(read == -1){
              read = remaining;
              isDone = true;
            } else
              read += remaining;
          } else
            read = remaining;
        byteBuffer = ByteBuffer.wrap(bytes, 0, read);
        CharBuffer cb = CharBuffer.wrap(buf, offset, length);
        int startPos = cb.position();

        if(hasSavedSurrogate){
            hasSavedSurrogate = false;
            cb.put(savedSurrogate);
            read++;
        }

        CoderResult cr = decoder.decode(byteBuffer, cb, isDone);
        decoder.reset();
        // 1 char remains which is the first half of a surrogate pair.
        if(cr.isOverflow() && cb.hasRemaining()){
            CharBuffer overflowbuf = CharBuffer.allocate(2);
            cr = decoder.decode(byteBuffer, overflowbuf, isDone);
            overflowbuf.flip();
            if(overflowbuf.hasRemaining())
            {
              cb.put(overflowbuf.get());
              savedSurrogate = overflowbuf.get();
              hasSavedSurrogate = true;
              isDone = false;
            }
        }

        if(byteBuffer.hasRemaining()) {
            byteBuffer.compact();
            byteBuffer.flip();
            isDone = false;
        } else
            byteBuffer = null;

        read = cb.position() - startPos;

        // Put cached bytes array back if we are finished and the cache
        // is null or smaller than the used bytes array.
        synchronized (cacheLock)
          {
            if (byteBuffer == null
                && (bytesCache == null || bytesCache.length < bytes.length))
              bytesCache = bytes;
          }
        return (read <= 0) ? -1 : read;
      }
    else */
      {
        byte[] bytes;
        // Fetch cached bytes array if available and big enough.
        synchronized (cacheLock)
          {
            bytes = bytesCache;
            if (bytes == null || bytes.length < length)
              bytes = new byte[length];
            else
              bytesCache = null;
          }

        int read = in.read(bytes, 0, length);

        if (encType == UTF8)
          {
            if (read > 0)
              {
                if (savedMultibyte != null)
                  {
                    byte newBytes[] = new byte[savedMultibyte.length + read];
                    System.arraycopy(savedMultibyte, 0, newBytes, 0,
                     savedMultibyte.length);
                    System.arraycopy(bytes, 0, newBytes,
                     savedMultibyte.length, read);
                    savedMultibyte = null;
                    bytes = newBytes;
                    read = newBytes.length;
                  }
                int ofsRef[] = new int[1];
                int count = decodeUTF8(bytes, ofsRef, read, buf, offset);
                int ofs = ofsRef[0];
                if (ofs < read)
                  {
                    byte newBytes[] = new byte[read - ofs];
                    System.arraycopy(bytes, ofs, newBytes, 0, read - ofs);
                    savedMultibyte = newBytes;
                  }
                read = count;
              }
            else
              {
                if (savedMultibyte != null && read < 0 && length > 0)
                  {
                    savedMultibyte = null;
                    buf[offset] = '?';
                    read = 1;
                  }
              }
          }
        else
          {
            if (encType == ASCII7)
              {
                for(int i=0;i<read;i++)
                  buf[offset+i] = (char)(bytes[i] >= 0 ? bytes[i] : '?');
              }
            else
              {
                for(int i=0;i<read;i++)
                  buf[offset+i] = (char)(bytes[i] & 0xFF);
              }
          }

        // Put back byte array into cache if appropriate.
        synchronized (cacheLock)
          {
            if (bytesCache == null || bytesCache.length < bytes.length)
              bytesCache = bytes;
          }
        return read;
      }
  }

  private static int decodeUTF8(byte b[], int ofsRef[], int len, char buf[],
   int offset)
  {
    int oldOffset = offset;
    int ofs;
    for (ofs = ofsRef[0]; ofs < len; ofs++)
      {
        int c = b[ofs];
        if (c >= 0)
          buf[offset] = (char)c;
        else
          {
            if (ofs + 1 >= len)
              break;
            int c2 = b[++ofs];
            if ((c2 & 0xc0) != 0x80)
              buf[offset] = '?';
            else
              {
                if ((c & 0xe0) == 0xc0)
                  buf[offset] = (char)(((c & 0x1f) << 6) | (c2 & 0x3f));
                else
                  {
                    if (++ofs >= len)
                      {
                        ofs -= 2;
                        break;
                      }
                    int c3 = b[ofs];
                    if ((c3 & 0xc0) != 0x80)
                      buf[offset] = '?';
                    else
                      {
                        if ((c & 0xf0) == 0xe0)
                          buf[offset] =
                           (char)(((c & 0xf) << 12) | ((c2 & 0x3f) << 6) |
                           (c3 & 0x3f));
                        else
                          {
                            if ((c & 0xf8) != 0xf0)
                              buf[offset] = '?';
                            else
                              {
                                if (++ofs >= len)
                                  {
                                    ofs -= 3;
                                    break;
                                  }
                                int c4 = b[ofs];
                                if ((c4 & 0xc0) != 0x80)
                                  buf[offset] = '?';
                                else
                                  {
                                    c = ((c & 0x7) << 8) |
                                         ((c2 & 0x3f) << 2) |
                                         ((c3 & 0x30) >> 4);
                                    if (c > 0x43f)
                                      buf[offset] = '?';
                                    else
                                      {
                                        c2 = ((c3 & 0xf) << 6) | (c4 & 0x3f);
                                        if (c <= 0x3f)
                                          buf[offset] = (char)((c << 10) | c2);
                                        else
                                          {
                                            buf[offset] = (char)((c - 0x40) |
                                                                0xd800);
                                            buf[offset + 1] = (char)(c2 |
                                                                0xdc00);
                                            offset++;
                                          }
                                      }
                                  }
                              }
                          }
                      }
                  }
              }
          }
        offset++;
      }
    ofsRef[0] = ofs;
    return offset - oldOffset;
  }

  /**
   * Reads an char from the input stream and returns it
   * as an int in the range of 0-65535.  This method also will return -1 if
   * the end of the stream has been reached.
   * <p>
   * This method will block until the char can be read.
   *
   * @return The char read or -1 if end of stream
   *
   * @exception IOException If an error occurs
   */
  public int read() throws IOException
  {
    char[] buf = new char[1];
    int count = read(buf, 0, 1);
    return count > 0 ? buf[0] : -1;
  }

  /**
   * Skips the specified number of chars in the stream.  It
   * returns the actual number of chars skipped, which may be less than the
   * requested amount.
   *
   * @param count The requested number of chars to skip
   *
   * @return The actual number of chars skipped.
   *
   * @exception IOException If an error occurs
   */
   public long skip(long count) throws IOException
   {
     if (in == null)
       throw new IOException("Stream closed.");

     return super.skip(count);
   }
}

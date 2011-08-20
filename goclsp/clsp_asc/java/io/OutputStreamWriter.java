/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Class root location: $(JCGO)/goclsp/clsp_asc
 * Origin: GNU Classpath v0.93
 */

/* OutputStreamWriter.java -- Writer that converts chars to bytes
   Copyright (C) 1998, 1999, 2000, 2001, 2003, 2005  Free Software Foundation, Inc.

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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;

import java.nio.charset.UnsupportedCharsetException;

/**
 * This class writes characters to an output stream that is byte oriented
 * It converts the chars that are written to bytes using an encoding layer,
 * which is specific to a particular encoding standard.  The desired
 * encoding can either be specified by name, or if no encoding is specified,
 * the system default encoding will be used.  The system default encoding
 * name is determined from the system property <code>file.encoding</code>.
 * The only encodings that are guaranteed to be available are "8859_1"
 * (the Latin-1 character set) and "UTF8".  Unfortunately, Java does not
 * provide a mechanism for listing the encodings that are supported in
 * a given implementation.
 * <p>
 * Here is a list of standard encoding names that may be available:
 * <p>
 * <ul>
 * <li>8859_1 (ISO-8859-1/Latin-1)
 * <li>8859_2 (ISO-8859-2/Latin-2)
 * <li>8859_3 (ISO-8859-3/Latin-3)
 * <li>8859_4 (ISO-8859-4/Latin-4)
 * <li>8859_5 (ISO-8859-5/Latin-5)
 * <li>8859_6 (ISO-8859-6/Latin-6)
 * <li>8859_7 (ISO-8859-7/Latin-7)
 * <li>8859_8 (ISO-8859-8/Latin-8)
 * <li>8859_9 (ISO-8859-9/Latin-9)
 * <li>ASCII (7-bit ASCII)
 * <li>UTF8 (UCS Transformation Format-8)
 * <li>More Later
 * </ul>
 *
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @author Per Bothner (bothner@cygnus.com)
 * @date April 17, 1998.
 */
public class OutputStreamWriter extends Writer
{
  /**
   * The output stream.
   */
  private OutputStream out;

  /**
   * The charset encoder.
   */
  /* private CharsetEncoder encoder; */

  /**
   * java.io canonical name of the encoding.
   */
  /* private String encodingName; */
  private int encType;
  private static final int ASCII7 = 1;
  private static final int UTF8 = 2;

  private char savedSurrogate;

  /**
   * Buffer output before character conversion as it has costly overhead.
   */
  /* private CharBuffer outputBuffer;
  private final static int BUFFER_SIZE = 1024; */

  /**
   * This method initializes a new instance of <code>OutputStreamWriter</code>
   * to write to the specified stream using a caller supplied character
   * encoding scheme.  Note that due to a deficiency in the Java language
   * design, there is no way to determine which encodings are supported.
   *
   * @param out The <code>OutputStream</code> to write to
   * @param encoding_scheme The name of the encoding scheme to use for
   * character to byte translation
   *
   * @exception UnsupportedEncodingException If the named encoding is
   * not available.
   */
  public OutputStreamWriter (OutputStream out, String encoding_scheme)
    throws UnsupportedEncodingException
  {
    this.out = out;
    /* try
      {
        // Don't use NIO if avoidable
        if(EncodingHelper.isISOLatin1(encoding_scheme))
          {
            encodingName = "ISO8859_1";
            encoder = null;
            return;
          } */

        /*
         * Workraround for encodings with a byte-order-mark.
         * We only want to write it once per stream.
         */
        /* try
          {
            if(encoding_scheme.equalsIgnoreCase("UnicodeBig") ||
               encoding_scheme.equalsIgnoreCase("UTF-16") ||
               encoding_scheme.equalsIgnoreCase("UTF16"))
              {
                encoding_scheme = "UTF-16BE";
                out.write((byte)0xFE);
                out.write((byte)0xFF);
              }
            else if(encoding_scheme.equalsIgnoreCase("UnicodeLittle")){
              encoding_scheme = "UTF-16LE";
              out.write((byte)0xFF);
              out.write((byte)0xFE);
            }
          }
        catch(IOException ioe)
          {
          }

        outputBuffer = CharBuffer.allocate(BUFFER_SIZE);

        Charset cs = EncodingHelper.getCharset(encoding_scheme);
        if(cs == null)
          throw new UnsupportedEncodingException("Encoding "+encoding_scheme+
                                                 " unknown");
        encoder = cs.newEncoder();
        encodingName = EncodingHelper.getOldCanonical(cs.name());

        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
      }
    catch(RuntimeException e)
      {
        // Default to ISO Latin-1, will happen if this is called, for instance,
        //  before the NIO provider is loadable.
        encoder = null;
        encodingName = "ISO8859_1";
      } */

    if (!setEncType(encoding_scheme))
      throw new UnsupportedEncodingException(encoding_scheme);
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
   * This method initializes a new instance of <code>OutputStreamWriter</code>
   * to write to the specified stream using the default encoding.
   *
   * @param out The <code>OutputStream</code> to write to
   */
  public OutputStreamWriter (OutputStream out)
  {
    this.out = out;
    /* outputBuffer = null;
    try
      {
        String encoding = SystemProperties.getProperty("file.encoding");
        Charset cs = Charset.forName(encoding);
        encoder = cs.newEncoder();
        encodingName =  EncodingHelper.getOldCanonical(cs.name());
      }
    catch(RuntimeException e)
      {
        encoder = null;
        encodingName = "ISO8859_1";
      }

    if(encoder != null)
      {
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        outputBuffer = CharBuffer.allocate(BUFFER_SIZE);
      } */
    setEncType(
     /* SystemProperties.getProperty("file.encoding", "") */
     gnu.classpath.VMAccessorGnuClasspath.getFileEncodingVMSystemProperties());
  }

  /**
   * This method initializes a new instance of <code>OutputStreamWriter</code>
   * to write to the specified stream using a given <code>Charset</code>.
   *
   * @param out The <code>OutputStream</code> to write to
   * @param cs The <code>Charset</code> of the encoding to use
   *
   * @since 1.5
   */
  public OutputStreamWriter(OutputStream out, Charset cs)
  {
    this.out = out;
    /* encoder = cs.newEncoder();
    encoder.onMalformedInput(CodingErrorAction.REPLACE);
    encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    outputBuffer = CharBuffer.allocate(BUFFER_SIZE);
    encodingName = EncodingHelper.getOldCanonical(cs.name()); */
    String encoding = cs.name();
    if (!setEncType(encoding))
      throw new UnsupportedCharsetException(encoding);
  }

  /**
   * This method initializes a new instance of <code>OutputStreamWriter</code>
   * to write to the specified stream using a given
   * <code>CharsetEncoder</code>.
   *
   * @param out The <code>OutputStream</code> to write to
   * @param enc The <code>CharsetEncoder</code> to encode the output with
   *
   * @since 1.5
   */
  public OutputStreamWriter(OutputStream out, CharsetEncoder enc)
  {
    this.out = out;
    /* encoder = enc;
    outputBuffer = CharBuffer.allocate(BUFFER_SIZE);
    Charset cs = enc.charset();
    if (cs == null)
      encodingName = "US-ASCII";
    else
      encodingName = EncodingHelper.getOldCanonical(cs.name()); */
    String encoding = enc.charset().name();
    if (!setEncType(encoding))
      throw new UnsupportedCharsetException(encoding);
  }

  /**
   * This method closes this stream, and the underlying
   * <code>OutputStream</code>
   *
   * @exception IOException If an error occurs
   */
  public void close () throws IOException
  {
    OutputStream out = this.out;
    if(out == null)
      return;
    try
      {
        flush();
      }
    catch (IOException e)
      {
        // ignore
      }
    out.close ();
    this.out = null;
  }

  /**
   * This method returns the name of the character encoding scheme currently
   * in use by this stream.  If the stream has been closed, then this method
   * may return <code>null</code>.
   *
   * @return The encoding scheme name
   */
  public String getEncoding ()
  {
    /* return out != null ? encodingName : null; */
    return out != null ? encType == ASCII7 ? "ASCII" :
            encType == UTF8 ? "UTF-8" : "ISO8859_1" : null;
  }

  /**
   * This method flushes any buffered bytes to the underlying output sink.
   *
   * @exception IOException If an error occurs
   */
  public void flush () throws IOException
  {
      OutputStream out = this.out;
      if (out != null) {
          /* if(outputBuffer != null){
              char[] buf = new char[outputBuffer.position()];
              if(buf.length > 0){
                  outputBuffer.flip();
                  outputBuffer.get(buf);
                  writeConvert(buf, 0, buf.length);
                  outputBuffer.clear();
              }
          } */
          out.flush ();
      }
  }

  void flushBuffer() throws IOException {}

  /**
   * This method writes <code>count</code> characters from the specified
   * array to the output stream starting at position <code>offset</code>
   * into the array.
   *
   * @param buf The array of character to write from
   * @param offset The offset into the array to start writing chars from
   * @param count The number of chars to write.
   *
   * @exception IOException If an error occurs
   */
  public void write (char[] buf, int offset, int count) throws IOException
  {
    if(out == null)
      throw new IOException("Stream closed.");

    /* if(outputBuffer != null)
        {
            if(count >= outputBuffer.remaining())
                {
                    int r = outputBuffer.remaining();
                    outputBuffer.put(buf, offset, r);
                    writeConvert(outputBuffer.array(), 0, BUFFER_SIZE);
                    outputBuffer.clear();
                    offset += r;
                    count -= r;
                    // if the remaining bytes is larger than the whole buffer,
                    // just don't buffer.
                    if(count >= outputBuffer.remaining()){
                      writeConvert(buf, offset, count);
                      return;
                    }
                }
            outputBuffer.put(buf, offset, count);
        } else */ writeConvert(buf, offset, count);
  }

 /**
  * Converts and writes characters.
  */
  private void writeConvert (char[] buf, int offset, int count)
      throws IOException
  {
    /* if(encoder == null) */
    {
       if (encType == ASCII7)
         {
           byte b[] = new byte[count];
           for(int i=0;i<count;i++)
             b[i] = (byte)(buf[offset+i] <= 0x7f ? buf[offset+i] : '?');
           out.write(b);
         }
       else
         {
           if (encType == UTF8)
             {
               if (savedSurrogate != '\0')
                 {
                   char newBuf[] = new char[count + 1];
                   newBuf[0] = savedSurrogate;
                   System.arraycopy(buf, offset, newBuf, 1, count);
                   savedSurrogate = '\0';
                   buf = newBuf;
                   offset = 0;
                   count++;
                 }
               if (count > 0)
                 {
                   char c = buf[offset + count - 1];
                   if (c >= 0xd800 && c < 0xdc00)
                     {
                       savedSurrogate = c;
                       count--;
                     }
                 }
               byte b[] = new byte[count * 3];
               out.write(b, 0, encodeUTF8(b, buf, offset, count));
             }
           else
             {
               byte[] b = new byte[count];
               for(int i=0;i<count;i++)
                 b[i] = (byte)((buf[offset+i] <= 0xff)?buf[offset+i]:'?');
               out.write(b);
             }
         }
    } /* else {
      try  {
        ByteBuffer output = encoder.encode(CharBuffer.wrap(buf,offset,count));
        encoder.reset();
        if(output.hasArray())
          out.write(output.array());
        else
          {
            byte[] outbytes = new byte[output.remaining()];
            output.get(outbytes);
            out.write(outbytes);
          }
      } catch(IllegalStateException e) {
        throw new IOException("Internal error.");
      } catch(MalformedInputException e) {
        throw new IOException("Invalid character sequence.");
      } catch(CharacterCodingException e) {
        throw new IOException("Unmappable character.");
      }
    } */
  }

  private static int encodeUTF8(byte b[], char buf[], int offset, int count)
    {
      int ofs = 0;
      for (int i = 0; i < count; i++)
        {
          char c = buf[offset + i];
          if (c <= 0x7f)
            b[ofs++] = (byte)c;
          else
            {
              if (c <= 0x7ff)
                {
                  b[ofs] = (byte)((c >> 6) | 0xc0);
                  b[ofs + 1] = (byte)((c & 0x3f) | 0x80);
                  ofs += 2;
                }
              else
                {
                  if (c < 0xd800 || c > 0xdfff)
                    {
                      b[ofs] = (byte)((c >> 12) | 0xe0);
                      b[ofs + 1] = (byte)(((c >> 6) & 0x3f) | 0x80);
                      b[ofs + 2] = (byte)((c & 0x3f) | 0x80);
                      ofs += 3;
                    }
                  else
                    {
                      if (c >= 0xdc00 || i >= count)
                        b[ofs++] = (byte)'?';
                      else
                        {
                          i++;
                          char c2 = buf[offset + i];
                          if (c2 < 0xdc00 || c2 > 0xdfff)
                            b[ofs++] = (byte)'?';
                          else
                            {
                              c = (char)((c & 0x3ff) + 0x40);
                              b[ofs] = (byte)((c >> 8) | 0xf0);
                              b[ofs + 1] = (byte)(((c >> 2) & 0x3f) | 0x80);
                              b[ofs + 2] = (byte)(((c & 0x3) << 4) |
                                                ((c2 >> 6) & 0xf) | 0x80);
                              b[ofs + 3] = (byte)((c2 & 0x3f) | 0x80);
                              ofs += 4;
                            }
                        }
                    }
                }
            }
        }
      return ofs;
    }

  /**
   * This method writes <code>count</code> bytes from the specified
   * <code>String</code> starting at position <code>offset</code> into the
   * <code>String</code>.
   *
   * @param str The <code>String</code> to write chars from
   * @param offset The position in the <code>String</code> to start
   * writing chars from
   * @param count The number of chars to write
   *
   * @exception IOException If an error occurs
   */
  public void write (String str, int offset, int count) throws IOException
  {
    write(str.toCharArray(), offset, count);
  }

  /**
   * This method writes a single character to the output stream.
   *
   * @param ch The char to write, passed as an int.
   *
   * @exception IOException If an error occurs
   */
  public void write (int ch) throws IOException
  {
    if (encType == ASCII7)
      out.write(ch <= 0x7f ? ch : '?');
    else
     {
       if (encType == UTF8)
         write(new char[]{ (char)ch }, 0, 1);
       else
         out.write(ch <= 0xff ? ch : '?');
     }
  }
} // class OutputStreamWriter

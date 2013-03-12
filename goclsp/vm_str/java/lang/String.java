/*
 * @(#) $(JCGO)/goclsp/vm_str/java/lang/String.java --
 * Space-optimized implementation of the standard String class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93
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

package java.lang;

import gnu.classpath.SystemProperties;

import gnu.java.lang.CharData;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import java.util.Comparator;
import java.util.Locale;

import java.util.regex.Pattern;

public final class String /* hard-coded class name */ /* const data */
 implements Serializable, Comparable, CharSequence
{

 private static final class CaseInsensitiveComparator
  implements Comparator, Serializable
 {

  private static final long serialVersionUID = 8575799808933029326L;

  CaseInsensitiveComparator() {}

  public int compare(Object o1, Object o2)
  {
   return ((String) o1).compareToIgnoreCase((String) o2);
  }
 }

 private static final long serialVersionUID = -6849794470754667710L;

 private static final char[] EMPTY_CHARS = {};

 private static final char[] upperExpand =
  zeroBasedStringValue(CharData.UPPER_EXPAND);

 private static final char[] upperSpecial =
  zeroBasedStringValue(CharData.UPPER_SPECIAL);

 public static final Comparator CASE_INSENSITIVE_ORDER =
  new CaseInsensitiveComparator();

 final Object value; /* hard-coded type and name */ /* const data */

 final int offset; /* hard-coded type and name */

 final int count; /* hard-coded type and name */

 private int cachedHashCode; /* hard-coded type and name */

 public String()
 {
  value = "".value;
  offset = 0;
  count = 0;
 }

 public String(String str)
 {
  value = str.value;
  offset = str.offset;
  count = str.count;
  cachedHashCode = str.cachedHashCode;
 }

 public String(char[] data)
 {
  int count = data.length;
  offset = 0;
  if (hasHighByte(data, 0, count))
   VMSystem.arraycopy(data, 0, value = new char[count], 0, count);
   else
   {
    byte[] newBytes = new byte[count];
    copyCharsToBytes(data, 0, newBytes, 0, count);
    value = newBytes;
   }
  this.count = count;
 }

 public String(char[] data, int offset, int count)
 {
  if ((offset | count) < 0 || data.length - offset < count)
   throw new StringIndexOutOfBoundsException();
  this.offset = 0;
  if (hasHighByte(data, offset, count))
   VMSystem.arraycopy(data, offset, value = new char[count], 0, count);
   else
   {
    byte[] newBytes = new byte[count];
    copyCharsToBytes(data, offset, newBytes, 0, count);
    value = newBytes;
   }
  this.count = count;
 }

/**
 * @deprecated
 */
 public String(byte[] data, int hibyte, int offset, int count)
 {
  if ((offset | count) < 0 || data.length - offset < count)
   throw new StringIndexOutOfBoundsException();
  this.offset = 0;
  this.count = count;
  if (hibyte != 0)
   value = createCharsFromBytes(data, offset, count, hibyte << 8);
   else
   {
    byte[] newBytes = new byte[count];
    VMSystem.arraycopy(data, offset, newBytes, 0, count);
    value = newBytes;
   }
 }

/**
 * @deprecated
 */
 public String(byte[] data, int hibyte)
 {
  int count = data.length;
  offset = 0;
  this.count = count;
  if (hibyte != 0)
   value = createCharsFromBytes(data, 0, count, hibyte << 8);
   else
   {
    byte[] newBytes = new byte[count];
    VMSystem.arraycopy(data, 0, newBytes, 0, count);
    value = newBytes;
   }
 }

 public String(byte[] data, int offset, int count, String encoding)
  throws UnsupportedEncodingException
 {
  CharBuffer cbuf = null;
  try
  {
   cbuf = decodeBytes(data, offset, count, encoding);
  }
  catch (CharacterCodingException e) {}
  catch (IllegalCharsetNameException e) {}
  catch (UnsupportedCharsetException e) {}
  if (cbuf == null)
   throw new UnsupportedEncodingException("Encoding not found: " + encoding);
  if (cbuf.hasArray())
  {
   value = cbuf.array();
   this.offset = cbuf.position();
   this.count = cbuf.remaining();
  }
   else
   {
    char[] newChars = new char[cbuf.remaining()];
    cbuf.get(newChars);
    value = newChars;
    this.offset = 0;
    this.count = newChars.length;
   }
 }

 public String(byte[] data, String encoding)
  throws UnsupportedEncodingException
 {
  this(data, 0, data.length, encoding);
 }

 public String(byte[] data, int offset, int count)
 {
  CharBuffer cbuf = null;
  try
  {
   cbuf = decodeBytes(data, offset, count,
           SystemProperties.getProperty("file.encoding"));
  }
  catch (CharacterCodingException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  catch (IllegalCharsetNameException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  catch (UnsupportedCharsetException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  if (cbuf.hasArray())
  {
   value = cbuf.array();
   this.offset = cbuf.position();
   this.count = cbuf.remaining();
  }
   else
   {
    char[] newChars = new char[cbuf.remaining()];
    cbuf.get(newChars);
    value = newChars;
    this.offset = 0;
    this.count = newChars.length;
   }
 }

 public String(byte[] data)
 {
  this(data, 0, data.length);
 }

 public String(StringBuffer buffer)
 {
  offset = 0;
  synchronized (buffer)
  {
   buffer.setShared();
   value = buffer.value();
   count = buffer.length();
  }
 }

 public String(StringBuilder buffer) /* hard-coded method signature */
 {
  offset = 0;
  buffer.setShared();
  value = buffer.value();
  count = buffer.length();
 }

 public String(int[] codePoints, int offset, int count)
 {
  if ((offset | count) < 0 || codePoints.length - offset < count)
   throw new IndexOutOfBoundsException();
  StringBuilder buffer = new StringBuilder(count);
  while (count-- > 0)
   buffer.appendCodePoint(codePoints[offset++]);
  this.offset = 0;
  buffer.setShared();
  value = buffer.value();
  this.count = buffer.length();
 }

 String(char[] data, int offset, int count, boolean dontCopy)
 {
  if (offset <= 0)
   offset = 0;
  if (count <= 0)
   count = 0;
  if (data.length - offset < count) /* hack */
  {
   offset = 0;
   count = data.length;
  }
  if (dontCopy)
  {
   value = data;
   this.offset = offset;
  }
   else
   {
    this.offset = 0;
    if (hasHighByte(data, offset, count))
     VMSystem.arraycopy(data, offset, value = new char[count], 0, count);
     else
     {
      byte[] newBytes = new byte[count];
      copyCharsToBytes(data, offset, newBytes, 0, count);
      value = newBytes;
     }
   }
  this.count = count;
 }

 public int length()
 {
  return count;
 }

 public char charAt(int index)
 {
  if (index < 0 || index >= count)
   throw new StringIndexOutOfBoundsException(index); /* hack */
  Object value = this.value;
  return value instanceof byte[] ?
          (char) (((byte[]) value)[offset + index] & 0xff) :
          ((char[]) value)[offset + index];
 }

 public void getChars(int srcBegin, int srcEnd, char[] dest, int destBegin)
 {
  if (((count - srcEnd) | srcBegin) < 0 || srcEnd < srcBegin)
   throw new StringIndexOutOfBoundsException();
  copyToChars(value, offset + srcBegin, dest, destBegin, srcEnd - srcBegin);
 }

/**
 * @deprecated
 */
 public void getBytes(int srcBegin, int srcEnd, byte[] dest, int destBegin)
 {
  if (((count - srcEnd) | srcBegin) < 0 || srcEnd < srcBegin)
   throw new StringIndexOutOfBoundsException();
  Object value = this.value;
  if (value instanceof byte[])
   VMSystem.arraycopy(value, offset + srcBegin, dest, destBegin,
    srcEnd - srcBegin);
   else copyCharsToBytes((char[]) value, offset + srcBegin, dest, destBegin,
         srcEnd - srcBegin);
 }

 public byte[] getBytes(String encoding)
  throws UnsupportedEncodingException
 {
  try
  {
   return encodeValue(value, offset, count, encoding);
  }
  catch (IllegalCharsetNameException e) {}
  catch (UnsupportedCharsetException e) {}
  catch (CharacterCodingException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  throw new UnsupportedEncodingException("Encoding not found: " + encoding);
 }

 public byte[] getBytes()
 {
  try
  {
   return encodeValue(value, offset, count,
           SystemProperties.getProperty("file.encoding"));
  }
  catch (CharacterCodingException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  catch (IllegalCharsetNameException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
  catch (UnsupportedCharsetException e)
  {
   throw (Error) (new InternalError()).initCause(e);
  }
 }

 public boolean equals(Object obj)
 {
  if (obj == this)
   return true;
  if (!(obj instanceof String))
   return false;
  String anotherString = (String) obj;
  int count = this.count;
  return anotherString.count == count && compareValues(value, offset,
          anotherString.value, anotherString.offset, count) == 0;
 }

 public boolean contentEquals(StringBuffer buffer)
 {
  int count = this.count;
  synchronized (buffer)
  {
   return buffer.length() == count &&
           compareValues(value, offset, buffer.value(), 0, count) == 0;
  }
 }

 public boolean contentEquals(CharSequence seq)
 {
  if (seq != this)
  {
   int count = this.count;
   if (seq.length() != count)
    return false;
   int offset = this.offset;
   Object value = this.value;
   if (value instanceof byte[])
   {
    byte[] bytes = (byte[]) value;
    for (int i = 0; i < count; i++)
     if ((bytes[offset + i] & 0xff) != seq.charAt(i))
      return false;
   }
    else
    {
     char[] chars = (char[]) value;
     for (int i = 0; i < count; i++)
      if (chars[offset + i] != seq.charAt(i))
       return false;
    }
  }
  return true;
 }

 public boolean equalsIgnoreCase(String anotherString)
 {
  int count = this.count;
  return anotherString != null && anotherString.count == count &&
          compareValuesIgnoreCase(value, offset, anotherString.value,
          anotherString.offset, count) == 0;
 }

 public int compareTo(String anotherString)
 {
  int count2 = anotherString.count;
  int count = this.count;
  int cmp = compareValues(value, offset, anotherString.value,
             anotherString.offset, count < count2 ? count : count2);
  if (cmp == 0)
   cmp = count - count2;
  return cmp;
 }

 public int compareTo(Object obj)
 {
  return compareTo((String) obj);
 }

 public int compareToIgnoreCase(String anotherString)
 {
  int count2 = anotherString.count;
  int count = this.count;
  int cmp = compareValuesIgnoreCase(value, offset, anotherString.value,
             anotherString.offset, count < count2 ? count : count2);
  if (cmp == 0)
   cmp = count - count2;
  return cmp;
 }

 public boolean regionMatches(int toffset, String other, int ooffset, int len)
 {
  return other.count - ooffset >= len && count - toffset >= len &&
          (toffset | ooffset) >= 0 && compareValues(value, offset + toffset,
          other.value, other.offset + ooffset, len) == 0;
 }

 public boolean regionMatches(boolean ignoreCase, int toffset, String other,
   int ooffset, int len)
 {
  return other.count - ooffset >= len && count - toffset >= len &&
          (toffset | ooffset) >= 0 && (ignoreCase ?
          compareValuesIgnoreCase(value, offset + toffset,
          other.value, other.offset + ooffset, len) :
          compareValues(value, offset + toffset,
          other.value, other.offset + ooffset, len)) == 0;
 }

 public boolean startsWith(String prefix, int toffset)
 {
  int count2 = prefix.count;
  return count - toffset >= count2 && toffset >= 0 && compareValues(value,
          offset + toffset, prefix.value, prefix.offset, count2) == 0;
 }

 public boolean startsWith(String prefix)
 {
  int count2 = prefix.count;
  return count >= count2 && compareValues(value, offset, prefix.value,
          prefix.offset, count2) == 0;
 }

 public boolean endsWith(String suffix)
 {
  int count2 = suffix.count;
  int toffset = count - count2;
  return toffset >= 0 && compareValues(value, offset + toffset, suffix.value,
          suffix.offset, count2) == 0;
 }

 public int hashCode()
 {
  int hashCode = cachedHashCode;
  if (hashCode == 0 &&
      (hashCode = hashCodeOfValue(value, offset, count)) != 0) /* hack */
   cachedHashCode = hashCode;
  return hashCode;
 }

 public int indexOf(int ch)
 {
  return indexOf(ch, 0);
 }

 public int indexOf(int ch, int fromIndex)
 {
  if (((char) ch) == ch)
  {
   if (fromIndex <= 0)
    fromIndex = 0;
   int offset = this.offset;
   int count = this.count;
   fromIndex = indexValueOf(ch, value, offset + fromIndex,
                count - fromIndex) - offset;
   if (fromIndex < count)
    return fromIndex;
  }
  return -1;
 }

 public int lastIndexOf(int ch)
 {
  return lastIndexOf(ch, count - 1);
 }

 public int lastIndexOf(int ch, int fromIndex)
 {
  if (((char) ch) != ch || fromIndex < 0)
   return -1;
  if (fromIndex >= count)
   fromIndex = count - 1;
  int offset = this.offset;
  return lastIndexValueOf(ch, value, offset + fromIndex, fromIndex + 1) -
          offset;
 }

 public int indexOf(String str)
 {
  return indexOf(str, 0);
 }

 public int indexOf(String str, int fromIndex)
 {
  int lastInd2 = str.count - 1;
  if (fromIndex <= 0)
   fromIndex = 0;
  int limit = count - lastInd2;
  if (fromIndex >= limit)
   return -1;
  if (lastInd2 >= 0)
  {
   Object value2 = str.value;
   int offset2 = str.offset;
   int ch = getValueAt(value2, offset2);
   Object value = this.value;
   int offset = this.offset;
   offset2++;
   do
   {
    fromIndex = indexValueOf(ch, value, offset + fromIndex,
                 limit - fromIndex) - offset;
    if (fromIndex >= limit)
     return -1;
    if (compareValues(value, offset + fromIndex + 1, value2, offset2,
        lastInd2) == 0)
     break;
    fromIndex++;
   } while (true);
  }
  return fromIndex;
 }

 public int lastIndexOf(String str)
 {
  return lastIndexOf(str, count - str.count);
 }

 public int lastIndexOf(String str, int fromIndex)
 {
  int lastInd2 = str.count - 1;
  if (count - lastInd2 <= fromIndex)
   fromIndex = count - lastInd2 - 1;
  if (fromIndex < 0)
   return -1;
  if (lastInd2 >= 0)
  {
   Object value2 = str.value;
   int offset2 = str.offset;
   int ch = getValueAt(value2, offset2);
   Object value = this.value;
   int offset = this.offset;
   offset2++;
   do
   {
    fromIndex = lastIndexValueOf(ch, value, offset + fromIndex,
                 fromIndex + 1) - offset;
    if (fromIndex < 0 || compareValues(value, offset + fromIndex + 1, value2,
        offset2, lastInd2) == 0)
     break;
    fromIndex--;
   } while (true);
  }
  return fromIndex;
 }

 public String substring(int beginIndex)
 {
  return substring(beginIndex, count);
 }

 public String substring(int beginIndex, int endIndex)
 {
  int margins = (count - endIndex) | beginIndex;
  if (margins == 0)
   return this;
  if (margins < 0 || endIndex < beginIndex)
   throw new StringIndexOutOfBoundsException();
  int len = endIndex - beginIndex;
  return len > 0 ? new String(offset + beginIndex, len, value) : "";
 }

 public CharSequence subSequence(int beginIndex, int endIndex)
 {
  return substring(beginIndex, endIndex);
 }

 public String concat(String str)
 {
  int count2 = str.count;
  int count = this.count;
  if (count2 == 0)
   return this;
  if (count == 0)
   return str;
  Object value = this.value;
  Object value2 = str.value;
  int offset = this.offset;
  int offset2 = str.offset;
  if (getValueLength(value) - offset - count >= count2 &&
      compareValues(value, offset + count, value2, offset2, count2) == 0)
   return new String(offset, count + count2, value);
  if (offset2 >= count &&
      compareValues(value, offset, value2, offset2 - count, count) == 0)
   return new String(offset2 - count, count + count2, value2);
  if (value instanceof byte[] && value2 instanceof byte[])
  {
   byte[] newBytes = new byte[count + count2];
   VMSystem.arraycopy(value, offset, newBytes, 0, count);
   VMSystem.arraycopy(value2, offset2, newBytes, count, count2);
   return new String(0, newBytes.length, newBytes);
  }
  char[] newChars = new char[count + count2];
  copyToChars(value, offset, newChars, 0, count);
  copyToChars(value2, offset2, newChars, count, count2);
  return new String(0, newChars.length, newChars);
 }

 public String replace(char oldChar, char newChar)
 {
  int pos;
  if (oldChar == newChar || (pos = indexOf(oldChar, 0)) < 0)
   return this;
  int count = this.count;
  Object value = this.value;
  if (newChar <= 0xff && value instanceof byte[])
  {
   byte[] newBytes = new byte[count];
   VMSystem.arraycopy(value, offset, newBytes, 0, count);
   newBytes[pos] = (byte) newChar;
   while (++pos < count)
    if ((newBytes[pos] & 0xff) == oldChar)
     newBytes[pos] = (byte) newChar;
   return new String(0, count, newBytes);
  }
  char[] newChars = new char[count];
  copyToChars(value, offset, newChars, 0, count);
  newChars[pos] = newChar;
  while (++pos < count)
   if (newChars[pos] == oldChar)
    newChars[pos] = newChar;
  return new String(0, count, newChars);
 }

 public boolean matches(String regex)
 {
  return Pattern.matches(regex, this);
 }

 public String replaceFirst(String regex, String replacement)
 {
  return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
 }

 public String replaceAll(String regex, String replacement)
 {
  return Pattern.compile(regex).matcher(this).replaceAll(replacement);
 }

 public String[] split(String regex, int limit)
 {
  return Pattern.compile(regex).split(this, limit);
 }

 public String[] split(String regex)
 {
  return split(regex, 0);
 }

 public String toLowerCase(Locale loc)
 {
  return toLowerCase("tr".equals(loc.getLanguage()));
 }

 public String toLowerCase()
 {
  return toLowerCase(false);
 }

 public String toUpperCase(Locale loc)
 {
  return toUpperCase("tr".equals(loc.getLanguage()));
 }

 public String toUpperCase()
 {
  return toUpperCase(false);
 }

 public String trim()
 {
  int limit = count;
  int pos = offset;
  if (limit == 0)
   return this;
  Object value = this.value;
  limit = pos + limit - 1;
  if (value instanceof byte[])
  {
   byte[] bytes = (byte[]) value;
   while ((bytes[pos] & 0xff) <= ' ')
    if (++pos > limit)
     return "";
   while ((bytes[limit] & 0xff) <= ' ')
    limit--;
  }
   else
   {
    char[] chars = (char[]) value;
    while (chars[pos] <= ' ')
     if (++pos > limit)
      return "";
    while (chars[limit] <= ' ')
     limit--;
   }
  limit = limit - pos + 1;
  return ((pos - offset) | (count - limit)) > 0 ?
          new String(pos, limit, value) : this;
 }

 public String toString()
 {
  return this;
 }

 public char[] toCharArray()
 {
  char[] newChars = new char[count];
  copyToChars(value, offset, newChars, 0, newChars.length);
  return newChars;
 }

 public static String valueOf(Object obj) /* hard-coded method signature */
 {
  return obj != null ? obj.toString() : "null";
 }

 public static String valueOf(char[] data)
 {
  return valueOf(data, 0, data.length);
 }

 public static String valueOf(char[] data, int offset, int count)
 {
  if ((offset | count) < 0 || data.length - offset < count)
   throw new StringIndexOutOfBoundsException();
  return new String(data, offset, count, false);
 }

 public static String copyValueOf(char[] data, int offset, int count)
 {
  if ((offset | count) < 0 || data.length - offset < count)
   throw new StringIndexOutOfBoundsException();
  return new String(data, offset, count, false);
 }

 public static String copyValueOf(char[] data)
 {
  return copyValueOf(data, 0, data.length);
 }

 public static String valueOf(boolean b) /* hard-coded method signature */
 {
  return b ? "true" : "false";
 }

 public static String valueOf(char c) /* hard-coded method signature */
 {
  return new String(0, 1, c > 0xff ? (Object) new char[] { c } :
          new byte[] { (byte) c });
 }

 public static String valueOf(int v) /* hard-coded method signature */
 {
  boolean isNeg = true;
  if (v >= 0)
  {
   isNeg = false;
   v = -v;
  }
  int offset = 11;
  if (v > -10000)
  {
   if (v > -100)
   {
    offset = 1;
    if (v <= -10)
     offset = 2;
   }
    else
    {
     offset = 3;
     if (v <= -1000)
      offset++;
    }
   if (isNeg)
    offset++;
  }
  byte[] newBytes = new byte[offset];
  do
  {
   newBytes[--offset] = (byte) ('0' - (v % 10));
   v /= 10;
  } while (v < 0);
  if (isNeg)
   newBytes[--offset] = '-';
  return new String(offset, newBytes.length - offset, newBytes);
 }

 public static String valueOf(long l) /* hard-coded method signature */
 {
  return Long.toString(l);
 }

 public static String valueOf(float f) /* hard-coded method signature */
 {
  return Float.toString(f);
 }

 public static String valueOf(double d) /* hard-coded method signature */
 {
  return Double.toString(d);
 }

 public String intern()
 {
  return VMString.intern(this);
 }

 public int codePointAt(int index)
 {
  return Character.codePointAt(this, index);
 }

 public int codePointBefore(int index)
 {
  return Character.codePointBefore(this, index);
 }

 public boolean contains(CharSequence seq)
 {
  return indexOf(seq.toString(), 0) >= 0;
 }

 public int codePointCount(int beginIndex, int endIndex)
 {
  if (((count - endIndex) | beginIndex) < 0 || endIndex < beginIndex)
   throw new StringIndexOutOfBoundsException();
  Object value = this.value;
  return value instanceof byte[] ? endIndex - beginIndex :
          Character.codePointCount((char[]) value, offset + beginIndex,
          endIndex - beginIndex);
 }

 public int offsetByCodePoints(int index, int codePointOffset)
 {
  int count = this.count;
  if (((count - index) | index) < 0)
   throw new StringIndexOutOfBoundsException(index);
  Object value = this.value;
  if (value instanceof byte[])
  {
   index = index + codePointOffset;
   if (((count - index) | index) < 0)
    throw new IndexOutOfBoundsException();
   return index;
  }
  int offset = this.offset;
  return Character.offsetByCodePoints((char[]) value, offset, count,
          offset + index, codePointOffset) - offset;
 }

 public String replace(CharSequence oldSeq, CharSequence newSeq)
 {
  int oldLen = oldSeq.length();
  int newLen = newSeq.length();
  if (oldLen == 1 && newLen == 1)
   return replace(oldSeq.charAt(0), newSeq.charAt(0));
  String oldStr = oldSeq.toString();
  int pos = indexOf(oldStr, 0);
  if (pos < 0 || oldSeq.equals(newSeq))
   return this;
  String newStr = newSeq.toString();
  StringBuilder buffer = new StringBuilder(this);
  do
  {
   buffer.replace(pos, pos + oldLen, newStr);
   pos = buffer.indexOf(oldStr, pos + newLen);
  } while (pos >= 0);
  return buffer.toString();
 }

 public boolean isEmpty()
 {
  return count == 0;
 }

 private static int upperCaseExpansion(char ch)
 {
  return Character.direction[0][Character.readCodePoint((int) ch) >> 7] & 0x3;
 }

 private static int upperCaseIndex(char ch)
 {
  int low = 0;
  char[] upperSpecialArr = upperSpecial;
  int hi = upperSpecialArr.length - 2;
  int mid;
  char c;
  while ((c = upperSpecialArr[mid = ((low + hi) >> 2) << 1]) != ch)
   if (ch < c)
    hi = mid - 2;
    else low = mid + 2;
  return upperSpecialArr[mid + 1];
 }

 static char[] zeroBasedStringValue(String str)
 {
  Object value = str.value;
  int offset = str.offset;
  int count = str.count;
  if (value instanceof byte[] ||
      ((((char[]) value).length - count) | offset) > 0)
  {
   if (count == 0)
    return EMPTY_CHARS;
   char[] newChars = new char[count];
   copyToChars(value, offset, newChars, 0, count);
   return newChars;
  }
  return (char[]) value;
 }

 String(int offset, byte[] bytes, int count)
 {
  value = bytes;
  this.offset = offset;
  this.count = count;
 }

 private String(int offset, int count, Object value)
 {
  this.value = value;
  this.offset = offset;
  this.count = count;
 }

 private static CharBuffer decodeBytes(byte[] bytes, int offset, int count,
   String encoding)
  throws CharacterCodingException, IllegalCharsetNameException,
   UnsupportedCharsetException
 {
  if ((offset | count) < 0 || bytes.length - offset < count)
   throw new StringIndexOutOfBoundsException();
  CharsetDecoder csd = Charset.forName(encoding).newDecoder();
  csd.onMalformedInput(CodingErrorAction.REPLACE);
  csd.onUnmappableCharacter(CodingErrorAction.REPLACE);
  return csd.decode(ByteBuffer.wrap(bytes, offset, count));
 }

 private static byte[] encodeValue(Object value, int offset, int count,
   String encoding)
  throws CharacterCodingException, IllegalCharsetNameException,
   UnsupportedCharsetException
 {
  CharsetEncoder cse = Charset.forName(encoding).newEncoder();
  cse.onMalformedInput(CodingErrorAction.REPLACE);
  cse.onUnmappableCharacter(CodingErrorAction.REPLACE);
  char[] chars;
  if (value instanceof byte[])
  {
   chars = new char[count];
   copyToChars(value, offset, chars, 0, count);
   offset = 0;
  }
   else chars = (char[]) value;
  ByteBuffer bbuf = cse.encode(CharBuffer.wrap(chars, offset, count));
  int bytesLen = bbuf.remaining();
  if (bbuf.hasArray() && bbuf.capacity() == bytesLen)
   return bbuf.array();
  byte[] newBytes = new byte[bytesLen];
  bbuf.get(newBytes);
  return newBytes;
 }

 private static boolean hasHighByte(char[] chars, int offset, int count)
 {
  count += offset;
  while (offset < count)
   if (chars[offset++] > 0xff)
    return true;
  return false;
 }

 private String toLowerCase(boolean isTurkish)
 {
  int offset = this.offset;
  int count = this.count;
  Object value = this.value;
  char[] chars;
  char[] newChars;
  int index;
  if (value instanceof byte[])
  {
   byte[] bytes = (byte[]) value;
   index = searchForNonLower(bytes, offset, count) - offset;
   if (index >= count)
    return this;
   if (!isTurkish &&
       Character.toLowerCase((char) (bytes[offset + index] & 0xff)) <= 0xff)
   {
    byte[] newBytes = new byte[count];
    if (index > 0)
     VMSystem.arraycopy(bytes, offset, newBytes, 0, index);
    index = convertToLower(bytes, offset + index, newBytes, index,
             count - index);
    if (index <= 0)
     return new String(0, count, newBytes);
    index = count - index;
    newChars = new char[count];
    copyToChars(newBytes, 0, newChars, 0, index);
    copyToChars(bytes, offset + index, newChars, index, count - index);
   }
    else
    {
     newChars = new char[count];
     copyToChars(bytes, offset, newChars, 0, count);
    }
   chars = newChars;
   offset = 0;
  }
   else
   {
    chars = (char[]) value;
    index = searchForNonLower(chars, offset, count) - offset;
    if (index >= count)
     return this;
    newChars = new char[count];
    if (index > 0)
     VMSystem.arraycopy(chars, offset, newChars, 0, index);
   }
  convertToLower(chars, offset + index, newChars, index, count - index,
   isTurkish);
  return new String(0, count, newChars);
 }

 private static int searchForNonLower(byte[] bytes, int offset, int count)
 {
  while (count-- > 0)
  {
   char ch = (char) (bytes[offset] & 0xff);
   if (Character.toLowerCase(ch) != ch)
    break;
   offset++;
  }
  return offset;
 }

 private static int searchForNonLower(char[] chars, int offset, int count)
 {
  while (count-- > 0)
  {
   char ch = chars[offset];
   if (Character.toLowerCase(ch) != ch)
    break;
   offset++;
  }
  return offset;
 }

 private static int convertToLower(byte[] bytes, int offset, byte[] bytes2,
   int offset2, int count)
 {
  if (count > 0)
  {
   do
   {
    char ch = Character.toLowerCase((char) (bytes[offset++] & 0xff));
    if (ch > 0xff)
     break;
    bytes2[offset2++] = (byte) ch;
   } while (--count > 0);
  }
  return count;
 }

 private static void convertToLower(char[] chars, int offset, char[] chars2,
   int offset2, int count, boolean isTurkish)
 {
  if (isTurkish)
  {
   while (count-- > 0)
   {
    char ch = chars[offset++];
    if (ch == 'I')
     ch = '\u0131';
     else if (ch == '\u0130')
      ch = 'i';
      else ch = Character.toLowerCase(ch);
    chars2[offset2++] = ch;
   }
  }
   else
   {
    while (count-- > 0)
     chars2[offset2++] = Character.toLowerCase(chars[offset++]);
   }
 }

 private String toUpperCase(boolean isTurkish)
 {
  int offset = this.offset;
  int count = this.count;
  Object value = this.value;
  char[] chars;
  char[] newChars = null;
  int index;
  if (value instanceof byte[])
  {
   byte[] bytes = (byte[]) value;
   index = searchForNonUpper(bytes, offset, count) - offset;
   if (index >= count)
    return this;
   if (!isTurkish &&
       Character.toUpperCase((char) (bytes[offset + index] & 0xff)) <= 0xff)
   {
    byte[] newBytes = new byte[count];
    if (index > 0)
     VMSystem.arraycopy(bytes, offset, newBytes, 0, index);
    index = convertToUpper(bytes, offset + index, newBytes, index,
             count - index);
    if (index <= 0)
     return new String(0, count, newBytes);
    index = count - index;
    newChars = new char[count];
    copyToChars(newBytes, 0, newChars, 0, index);
    copyToChars(bytes, offset + index, newChars, index, count - index);
   }
    else
    {
     newChars = new char[count];
     copyToChars(bytes, offset, newChars, 0, count);
    }
   chars = newChars;
   offset = 0;
  }
   else
   {
    chars = (char[]) value;
    index = searchForNonUpper(chars, offset, count) - offset;
    if (index >= count)
     return this;
   }
  int expand = getUpperExpansion(chars, offset + index, count - index);
  if (expand > 0 || chars != newChars)
  {
   newChars = new char[count + expand];
   if (index > 0)
    VMSystem.arraycopy(chars, offset, newChars, 0, index);
  }
  if (isTurkish || expand > 0)
   convertToUpper(chars, offset + index, newChars, index, count - index,
    isTurkish);
   else convertToUpper(chars, offset + index, newChars, index, count - index);
  return new String(0, count, newChars);
 }

 private static int searchForNonUpper(byte[] bytes, int offset, int count)
 {
  while (count-- > 0)
  {
   char ch = (char) (bytes[offset] & 0xff);
   if (Character.toUpperCase(ch) != ch || upperCaseExpansion(ch) > 0)
    break;
   offset++;
  }
  return offset;
 }

 private static int searchForNonUpper(char[] chars, int offset, int count)
 {
  while (count-- > 0)
  {
   char ch = chars[offset];
   if (Character.toUpperCase(ch) != ch || upperCaseExpansion(ch) > 0)
    break;
   offset++;
  }
  return offset;
 }

 private static int getUpperExpansion(char[] chars, int offset, int count)
 {
  int expand = 0;
  while (count-- > 0)
   expand += upperCaseExpansion(chars[offset++]);
  return expand;
 }

 private static int convertToUpper(byte[] bytes, int offset, byte[] bytes2,
   int offset2, int count)
 {
  if (count > 0)
  {
   do
   {
    char ch = (char) (bytes[offset++] & 0xff);
    if (upperCaseExpansion(ch) > 0)
     break;
    ch = Character.toUpperCase(ch);
    if (ch > 0xff)
     break;
    bytes2[offset2++] = (byte) ch;
   } while (--count > 0);
  }
  return count;
 }

 private static void convertToUpper(char[] chars, int offset, char[] chars2,
   int offset2, int count)
 {
  while (count-- > 0)
   chars2[offset2++] = Character.toUpperCase(chars[offset++]);
 }

 private static void convertToUpper(char[] chars, int offset, char[] chars2,
   int offset2, int count, boolean isTurkish)
 {
  while (count-- > 0)
  {
   char ch = chars[offset++];
   if (ch == '\u0131' && isTurkish)
    ch = 'I';
    else if (ch == 'i' && isTurkish)
     ch = '\u0130';
     else
     {
      int expand = upperCaseExpansion(ch);
      if (expand > 0)
      {
       char[] upperExpandArr = upperExpand;
       int index = upperCaseIndex(ch);
       do
       {
        chars2[offset2++] = upperExpandArr[index++];
       } while (--expand > 0);
       ch = upperExpandArr[index];
      }
       else ch = Character.toUpperCase(ch);
     }
   chars2[offset2++] = ch;
  }
 }

 private static char[] createCharsFromBytes(byte[] bytes, int offset,
   int count, int orval)
 {
  char[] newChars = new char[count];
  for (int i = 0; i < count; i++)
   newChars[i] = (char) ((bytes[offset++] & 0xff) | orval);
  return newChars;
 }

 private static void copyCharsToBytes(char[] chars, int offset, byte[] bytes2,
   int offset2, int count)
 {
  while (count-- > 0)
   bytes2[offset2++] = (byte) chars[offset++];
 }

 private static void copyToChars(Object value, int offset, char[] chars2,
   int offset2, int count)
 {
  if (count > 0)
  {
   if (value instanceof byte[])
   {
    byte[] bytes = (byte[]) value;
    do
    {
     chars2[offset2++] = (char) (bytes[offset++] & 0xff);
    } while (--count > 0);
   }
    else VMSystem.arraycopy(value, offset, chars2, offset2, count);
  }
 }

 private static int hashCodeOfValue(Object value, int offset, int count)
 {
  int hashCode = 0;
  if (value instanceof byte[])
  {
   byte[] bytes = (byte[]) value;
   while (count-- > 0)
   {
    hashCode = (bytes[offset] & 0xff) + (hashCode << 5) - hashCode;
    offset++;
   }
  }
   else
   {
    char[] chars = (char[]) value;
    while (count-- > 0)
     hashCode = chars[offset++] + (hashCode << 5) - hashCode;
   }
  return hashCode;
 }

 private static int indexValueOf(int ch, Object value, int pos, int count)
 {
  if (value instanceof byte[])
  {
   if (ch > 0xff)
    return pos + count;
   byte[] bytes = (byte[]) value;
   while (count-- > 0 && (bytes[pos] & 0xff) != ch)
    pos++;
  }
   else
   {
    char[] chars = (char[]) value;
    while (count-- > 0 && chars[pos] != ch)
     pos++;
   }
  return pos;
 }

 private static int lastIndexValueOf(int ch, Object value, int pos, int count)
 {
  if (value instanceof byte[])
  {
   if (ch > 0xff)
    return pos - count;
   byte[] bytes = (byte[]) value;
   while (count-- > 0 && (bytes[pos] & 0xff) != ch)
    pos--;
  }
   else
   {
    char[] chars = (char[]) value;
    while (count-- > 0 && chars[pos] != ch)
     pos--;
   }
  return pos;
 }

 private static int compareValues(Object value, int offset, Object value2,
   int offset2, int count)
 {
  int cmp = 0;
  if (offset != offset2 || value != value2)
  {
   if (value instanceof byte[])
   {
    byte[] bytes = (byte[]) value;
    if (value2 instanceof byte[])
    {
     byte[] bytes2 = (byte[]) value2;
     while (count-- > 0)
     {
      cmp = (bytes[offset] & 0xff) - (bytes2[offset2] & 0xff);
      if (cmp != 0)
       break;
      offset++;
      offset2++;
     }
    }
     else
     {
      char[] chars2 = (char[]) value2;
      while (count-- > 0)
       if ((cmp = (bytes[offset++] & 0xff) - chars2[offset2++]) != 0)
        break;
     }
   }
    else
    {
     char[] chars = (char[]) value;
     if (value2 instanceof byte[])
     {
      byte[] bytes2 = (byte[]) value2;
      while (count-- > 0)
       if ((cmp = chars[offset++] - (bytes2[offset2++] & 0xff)) != 0)
        break;
     }
      else
      {
       char[] chars2 = (char[]) value2;
       while (count-- > 0)
        if ((cmp = chars[offset++] - chars2[offset2++]) != 0)
         break;
      }
    }
  }
  return cmp;
 }

 private static int compareValuesIgnoreCase(Object value, int offset,
   Object value2, int offset2, int count)
 {
  int cmp = 0;
  if (offset != offset2 || value != value2)
  {
   if (value instanceof byte[])
   {
    byte[] bytes = (byte[]) value;
    if (value2 instanceof byte[])
    {
     byte[] bytes2 = (byte[]) value2;
     while (count-- > 0)
      if ((cmp = compareCharsIgnoreCase(bytes[offset++] & 0xff,
          bytes2[offset2++] & 0xff)) != 0)
       break;
    }
     else
     {
      char[] chars2 = (char[]) value2;
      while (count-- > 0)
       if ((cmp = compareCharsIgnoreCase(bytes[offset++] & 0xff,
           chars2[offset2++])) != 0)
        break;
     }
   }
    else
    {
     char[] chars = (char[]) value;
     if (value2 instanceof byte[])
     {
      byte[] bytes2 = (byte[]) value2;
      while (count-- > 0)
       if ((cmp = compareCharsIgnoreCase(chars[offset++],
           bytes2[offset2++] & 0xff)) != 0)
        break;
     }
      else
      {
       char[] chars2 = (char[]) value2;
       while (count-- > 0)
        if ((cmp = compareCharsIgnoreCase(chars[offset++],
            chars2[offset2++])) != 0)
         break;
      }
    }
  }
  return cmp;
 }

 private static int compareCharsIgnoreCase(int ch, int ch2)
 {
  int cmp = 0;
  if (ch != ch2)
  {
   char c = Character.toUpperCase((char) ch);
   char c2 = Character.toUpperCase((char) ch2);
   if (c != c2)
    cmp = Character.toLowerCase(c) - Character.toLowerCase(c2);
  }
  return cmp;
 }

 private static int getValueLength(Object value)
 {
  return value instanceof byte[] ? ((byte[]) value).length :
          ((char[]) value).length;
 }

 private static char getValueAt(Object value, int index)
 {
  return value instanceof byte[] ? (char) (((byte[]) value)[index] & 0xff) :
          ((char[]) value)[index];
 }
}

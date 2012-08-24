/*
 * @(#) $(JCGO)/goclsp/vm_str/java/lang/StringBuilder.java --
 * Space-optimized implementation of the standard StringBuilder class.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class StringBuilder /* hard-coded class name */
 implements Serializable, CharSequence /* , Appendable */
{

 private static final long serialVersionUID = 4383685877147921099L;

 private static final int DEFAULT_CAPACITY = 16;

 int count;

 char[] value;

 boolean shared;

 private transient byte[] bytes;

 public StringBuilder() /* hard-coded method signature */
 {
  this(DEFAULT_CAPACITY);
 }

 public StringBuilder(int capacity)
 {
  bytes = new byte[capacity];
 }

 public StringBuilder(String str)
 {
  int count = str.length();
  Object strValue = str.value;
  if (strValue instanceof byte[])
   VMSystem.arraycopy((byte[]) strValue, str.offset,
    bytes = new byte[count + DEFAULT_CAPACITY], 0, count);
   else VMSystem.arraycopy((char[]) strValue, str.offset,
         value = new char[count + DEFAULT_CAPACITY], 0, count);
  this.count = count;
 }

 public StringBuilder(CharSequence seq)
 {
  int len = seq.length();
  if (len > 0)
  {
   char ch = seq.charAt(0);
   int i = 0;
   byte[] newBytes = null;
   if (ch <= 0xff)
   {
    newBytes = new byte[len + DEFAULT_CAPACITY];
    do
    {
     newBytes[i++] = (byte) ch;
     if (i >= len)
      break;
     ch = seq.charAt(i);
    } while (ch <= 0xff);
   }
   if (i < len)
   {
    char[] newChars = new char[len + DEFAULT_CAPACITY];
    if (i > 0)
     copyBytesToChars(newBytes, 0, newChars, 0, i);
    do
    {
     newChars[i++] = ch;
     if (i >= len)
      break;
     ch = seq.charAt(i);
    } while (true);
    value = newChars;
   }
    else bytes = newBytes;
   count = len;
  }
   else bytes = new byte[DEFAULT_CAPACITY];
 }

 public int length()
 {
  return count;
 }

 public int capacity()
 {
  byte[] bytes = this.bytes;
  return bytes != null ? bytes.length : value.length;
 }

 public void ensureCapacity(int minimumCapacity)
 {
  if (minimumCapacity <= 0)
   minimumCapacity = 0;
  ensureCapacityInner(minimumCapacity, false);
 }

 public void setLength(int newLength)
 {
  if (newLength < 0)
   throw new StringIndexOutOfBoundsException(newLength);
  byte[] bytes = this.bytes;
  char[] chars = value;
  ensureCapacityInner(newLength, false);
  int count = this.count;
  if (newLength > count && this.bytes == bytes && value == chars)
  {
   if (bytes != null)
   {
    do
    {
     bytes[count++] = 0;
    } while (count < newLength);
   }
    else
    {
     do
     {
      chars[count++] = 0;
     } while (count < newLength);
    }
  }
  this.count = newLength;
 }

 public char charAt(int index)
 {
  if (index < 0 || index >= count)
   throw new StringIndexOutOfBoundsException(index);
  byte[] bytes = this.bytes;
  return bytes != null ? (char) (bytes[index] & 0xff) : value[index];
 }

 public int codePointAt(int index)
 {
  int count = this.count;
  if (index < 0 || index >= count)
   throw new StringIndexOutOfBoundsException(index);
  byte[] bytes = this.bytes;
  return bytes != null ? bytes[index] & 0xff :
          Character.codePointAt(value, index, count);
 }

 public int codePointBefore(int index)
 {
  if (index <= 0 || index > count)
   throw new StringIndexOutOfBoundsException(index);
  byte[] bytes = this.bytes;
  return bytes != null ? bytes[index - 1] & 0xff :
          Character.codePointBefore(value, index, 1);
 }

 public void getChars(int srcOffset, int srcEnd, char[] dest,
   int destOffset)
 {
  if (((count - srcEnd) | srcOffset) < 0 || srcEnd < srcOffset)
   throw new StringIndexOutOfBoundsException();
  byte[] bytes = this.bytes;
  if (bytes != null)
   copyBytesToChars(bytes, srcOffset, dest, destOffset, srcEnd - srcOffset);
   else VMSystem.arraycopy(value, srcOffset, dest, destOffset,
         srcEnd - srcOffset);
 }

 public void setCharAt(int index, char ch)
 {
  if (index < 0 || index >= count)
   throw new StringIndexOutOfBoundsException(index);
  ensureCapacityInner(count, ch > 0xff);
  byte[] bytes = this.bytes;
  if (bytes != null)
   bytes[index] = (byte) ch;
   else value[index] = ch;
 }

 public StringBuilder append(Object obj)
 {
  return append(obj != null ? obj.toString() : "null");
 }

 public StringBuilder append(
   String str) /* hard-coded method signature */
 {
  if (str == null)
   str = "null";
  int count = this.count;
  int len = str.length();
  Object strValue = str.value;
  int strOffset = str.offset;
  byte[] strBytes = null;
  boolean outOfByte = false;
  if (len > 0)
  {
   if (strValue instanceof byte[])
    strBytes = (byte[]) strValue;
    else if (this.bytes != null &&
             hasHighByte((char[]) strValue, strOffset, len))
     outOfByte = true;
  }
  ensureCapacityInner(count + len, outOfByte);
  if (len > 0)
  {
   byte[] bytes = this.bytes;
   if (bytes != null)
   {
    if (strBytes != null)
     VMSystem.arraycopy(strBytes, strOffset, bytes, count, len);
     else copyCharsToBytes((char[]) strValue, strOffset, bytes, count, len);
   }
    else
    {
     if (strBytes != null)
      copyBytesToChars(strBytes, strOffset, value, count, len);
      else VMSystem.arraycopy(strValue, strOffset, value, count, len);
    }
   this.count = count + len;
  }
  return this;
 }

 public StringBuilder append(StringBuffer sBuf)
 {
  if (sBuf == null)
   return append("null");
  int count = this.count;
  synchronized (sBuf)
  {
   int len = sBuf.length();
   ensureCapacityInner(count + len, len > 0 &&
    !(sBuf.value() instanceof byte[]));
   if (len > 0)
   {
    byte[] bytes = this.bytes;
    Object sBufValue = sBuf.value();
    if (bytes != null)
     VMSystem.arraycopy(sBufValue, 0, bytes, count, len);
     else
     {
      if (sBufValue instanceof byte[])
       copyBytesToChars((byte[]) sBufValue, 0, value, count, len);
       else VMSystem.arraycopy(sBufValue, 0, value, count, len);
     }
    this.count = count + len;
   }
  }
  return this;
 }

 public StringBuilder append(CharSequence seq)
 {
  if (seq == null)
   seq = "null";
  return append(seq, 0, seq.length());
 }

 public StringBuilder append(CharSequence seq, int start, int end)
 {
  if (seq == null)
   return append("null");
  if (end - start > 0)
  {
   ensureCapacityInner(count + end - start, false);
   while (start < end)
   {
    char ch = seq.charAt(start++);
    int count = this.count;
    ensureCapacityInner(count + 1, ch > 0xff);
    byte[] bytes = this.bytes;
    if (bytes != null)
     bytes[count] = (byte) ch;
     else value[count] = ch;
    this.count = count + 1;
   }
  }
  return this;
 }

 public StringBuilder append(char[] data)
 {
  int count = data.length;
  int oldCount = this.count;
  ensureCapacityInner(oldCount + count, hasHighByte(data, 0, count));
  byte[] bytes = this.bytes;
  if (bytes != null)
   copyCharsToBytes(data, 0, bytes, oldCount, count);
   else VMSystem.arraycopy(data, 0, value, oldCount, count);
  this.count = oldCount + count;
  return this;
 }

 public StringBuilder append(char[] data, int offset, int count)
 {
  if (data.length - count < offset || (offset | count) < 0)
   throw new StringIndexOutOfBoundsException();
  int oldCount = this.count;
  ensureCapacityInner(oldCount + count, hasHighByte(data, offset, count));
  byte[] bytes = this.bytes;
  if (bytes != null)
   copyCharsToBytes(data, offset, bytes, oldCount, count);
   else VMSystem.arraycopy(data, offset, value, oldCount, count);
  this.count = oldCount + count;
  return this;
 }

 public StringBuilder append(boolean bool)
 {
  return append(bool ? "true" : "false");
 }

 public StringBuilder append(char ch)
 {
  int count = this.count;
  ensureCapacityInner(count + 1, ch > 0xff);
  byte[] bytes = this.bytes;
  if (bytes != null)
   bytes[count] = (byte) ch;
   else value[count] = ch;
  this.count = count + 1;
  return this;
 }

 public StringBuilder append(int v)
 {
  return append(String.valueOf(v));
 }

 public StringBuilder append(long v)
 {
  return append(Long.toString(v, 10));
 }

 public StringBuilder append(float v)
 {
  return append(Float.toString(v));
 }

 public StringBuilder append(double v)
 {
  return append(Double.toString(v));
 }

 public StringBuilder appendCodePoint(int code)
 {
  int len = Character.charCount(code);
  int count = this.count;
  ensureCapacityInner(count + len, code > 0xff);
  byte[] bytes = this.bytes;
  if (bytes != null)
   bytes[count] = (byte) code;
   else Character.toChars(code, value, count);
  this.count = count + len;
  return this;
 }

 public StringBuilder delete(int start, int end)
 {
  int count = this.count;
  if (((count - start) | start) < 0 || start > end)
   throw new StringIndexOutOfBoundsException(start);
  ensureCapacityInner(count, false);
  int len = 0;
  if (count > end)
  {
   len = count - end;
   byte[] bytes = this.bytes;
   if (bytes != null)
    VMSystem.arraycopy(bytes, end, bytes, start, len);
    else
    {
     char[] chars = value;
     VMSystem.arraycopy(chars, end, chars, start, len);
    }
  }
  this.count = start + len;
  return this;
 }

 public StringBuilder deleteCharAt(int index)
 {
  return delete(index, index + 1);
 }

 public StringBuilder replace(int start, int end, String str)
 {
  int len = str.length();
  int count = this.count;
  if (((count - start) | start) < 0 || start > end)
   throw new StringIndexOutOfBoundsException(start);
  int delta = len - (end < count ? end : count) + start;
  Object strValue = str.value;
  byte[] strBytes = null;
  if (strValue instanceof byte[])
   strBytes = (byte[]) strValue;
  ensureCapacityInner(count + delta, len > 0 && strBytes == null);
  int strOffset = str.offset;
  byte[] bytes = this.bytes;
  if (bytes != null)
  {
   if (delta != 0 && end < count)
    VMSystem.arraycopy(bytes, end, bytes, end + delta, count - end);
   if (len > 0)
    VMSystem.arraycopy(strBytes, strOffset, bytes, start, len);
  }
   else
   {
    char[] chars = value;
    if (delta != 0 && end < count)
     VMSystem.arraycopy(chars, end, chars, end + delta, count - end);
    if (len > 0)
    {
     if (strBytes != null)
      copyBytesToChars(strBytes, strOffset, chars, start, len);
      else VMSystem.arraycopy(strValue, strOffset, chars, start, len);
    }
   }
  this.count = count + delta;
  return this;
 }

 public String substring(int beginIndex)
 {
  return substring(beginIndex, count);
 }

 public CharSequence subSequence(int beginIndex, int endIndex)
 {
  return substring(beginIndex, endIndex);
 }

 public String substring(int beginIndex, int endIndex)
 {
  if (((count - endIndex) | beginIndex) < 0 || endIndex < beginIndex)
   throw new StringIndexOutOfBoundsException();
  int len = endIndex - beginIndex;
  if (len == 0)
   return "";
  byte[] bytes = this.bytes;
  shared = true;
  return bytes != null ? new String(beginIndex, bytes, len) :
          new String(value, beginIndex, len, true);
 }

 public StringBuilder insert(int offset, char[] data,
   int dataOffset, int count)
 {
  int oldCount = this.count;
  if (data.length - count < dataOffset ||
      ((oldCount - offset) | offset | dataOffset | count) < 0)
   throw new StringIndexOutOfBoundsException();
  ensureCapacityInner(oldCount + count, hasHighByte(data, dataOffset, count));
  byte[] bytes = this.bytes;
  if (bytes != null)
  {
   VMSystem.arraycopy(bytes, offset, bytes, offset + count,
    oldCount - offset);
   copyCharsToBytes(data, dataOffset, bytes, offset, count);
  }
   else
   {
    char[] chars = value;
    VMSystem.arraycopy(chars, offset, chars, offset + count,
     oldCount - offset);
    VMSystem.arraycopy(data, dataOffset, chars, offset, count);
   }
  this.count = oldCount + count;
  return this;
 }

 public StringBuilder insert(int offset, Object obj)
 {
  return insert(offset, obj != null ? obj.toString() : "null");
 }

 public StringBuilder insert(int offset, String str)
 {
  int count = this.count;
  if (((count - offset) | offset) < 0)
   throw new StringIndexOutOfBoundsException(offset);
  if (str == null)
   str = "null";
  int len = str.length();
  Object strValue = str.value;
  byte[] strBytes = null;
  if (strValue instanceof byte[])
   strBytes = (byte[]) strValue;
  ensureCapacityInner(count + len, len > 0 && strBytes == null);
  if (len > 0)
  {
   int strOffset = str.offset;
   byte[] bytes = this.bytes;
   if (bytes != null)
   {
    if (offset < count)
     VMSystem.arraycopy(bytes, offset, bytes, offset + len, count - offset);
    VMSystem.arraycopy(strBytes, strOffset, bytes, offset, len);
   }
    else
    {
     char[] chars = value;
     if (offset < count)
      VMSystem.arraycopy(chars, offset, chars, offset + len, count - offset);
     if (strBytes != null)
      copyBytesToChars(strBytes, strOffset, chars, offset, len);
      else VMSystem.arraycopy(strValue, strOffset, chars, offset, len);
    }
   this.count = count + len;
  }
  return this;
 }

 public StringBuilder insert(int offset, CharSequence seq)
 {
  if (seq == null)
   seq = "null";
  return insert(offset, seq, 0, seq.length());
 }

 public StringBuilder insert(int offset, CharSequence seq, int start, int end)
 {
  if (seq == null)
   seq = "null";
  return insert(offset, seq.subSequence(start, end).toString());
 }

 public StringBuilder insert(int offset, char[] data)
 {
  return insert(offset, data, 0, data.length);
 }

 public StringBuilder insert(int offset, boolean bool)
 {
  return insert(offset, bool ? "true" : "false");
 }

 public StringBuilder insert(int offset, char ch)
 {
  int count = this.count;
  if (((count - offset) | offset) < 0)
   throw new StringIndexOutOfBoundsException(offset);
  ensureCapacityInner(count + 1, ch > 0xff);
  byte[] bytes = this.bytes;
  if (bytes != null)
  {
   VMSystem.arraycopy(bytes, offset, bytes, offset + 1, count - offset);
   bytes[offset] = (byte) ch;
  }
   else
   {
    char[] chars = value;
    VMSystem.arraycopy(chars, offset, chars, offset + 1, count - offset);
    chars[offset] = ch;
   }
  this.count = count + 1;
  return this;
 }

 public StringBuilder insert(int offset, int v)
 {
  return insert(offset, String.valueOf(v));
 }

 public StringBuilder insert(int offset, long v)
 {
  return insert(offset, Long.toString(v, 10));
 }

 public StringBuilder insert(int offset, float v)
 {
  return insert(offset, Float.toString(v));
 }

 public StringBuilder insert(int offset, double v)
 {
  return insert(offset, Double.toString(v));
 }

 public int indexOf(String str)
 {
  return indexOf(str, 0);
 }

 public int indexOf(String str, int fromIndex)
 {
  if (fromIndex <= 0)
   fromIndex = 0;
  byte[] bytes = this.bytes;
  int lastInd = str.length() - 1;
  int limit = count - lastInd;
  if (fromIndex < limit)
  {
   if (lastInd < 0)
    return fromIndex;
   Object strValue = str.value;
   int strOffset = str.offset;
   if (bytes != null)
   {
    if (strValue instanceof byte[])
    {
     byte[] strBytes = (byte[]) strValue;
     byte strCh = strBytes[strOffset++];
     do
     {
      while (bytes[fromIndex] != strCh)
       if (++fromIndex >= limit)
        break;
      if (fromIndex >= limit)
       break;
      if (regionMatches(bytes, fromIndex + 1, strBytes, strOffset, lastInd))
       return fromIndex;
     } while (++fromIndex < limit);
    }
     else
     {
      char[] strChars = (char[]) strValue;
      char ch = strChars[strOffset++];
      if (ch <= 0xff)
      {
       byte strCh = (byte)ch;
       do
       {
        while (bytes[fromIndex] != strCh)
         if (++fromIndex >= limit)
          break;
        if (fromIndex >= limit)
         break;
        if (regionMatches(bytes, fromIndex + 1, strChars, strOffset, lastInd))
         return fromIndex;
       } while (++fromIndex < limit);
      }
     }
   }
    else
    {
     char[] chars = value;
     if (strValue instanceof byte[])
     {
      byte[] strBytes = (byte[]) strValue;
      char strCh = (char)(strBytes[strOffset++] & 0xff);
      do
      {
       while (chars[fromIndex] != strCh)
        if (++fromIndex >= limit)
         break;
       if (fromIndex >= limit)
        break;
       if (regionMatches(strBytes, strOffset, chars, fromIndex + 1, lastInd))
        return fromIndex;
      } while (++fromIndex < limit);
     }
      else
      {
       char[] strChars = (char[]) strValue;
       char strCh = strChars[strOffset++];
       do
       {
        while (chars[fromIndex] != strCh)
         if (++fromIndex >= limit)
          break;
        if (fromIndex >= limit)
         break;
        if (regionMatches(chars, fromIndex + 1, strChars, strOffset, lastInd))
         return fromIndex;
       } while (++fromIndex < limit);
      }
    }
  }
  return -1;
 }

 public int lastIndexOf(String str)
 {
  return lastIndexOf(str, count - str.length());
 }

 public int lastIndexOf(String str, int fromIndex)
 {
  byte[] bytes = this.bytes;
  int lastInd = str.length() - 1;
  if (count - lastInd <= fromIndex)
   fromIndex = count - lastInd - 1;
  if (fromIndex < 0)
   return -1;
  if (lastInd >= 0)
  {
   Object strValue = str.value;
   int strOffset = str.offset;
   if (bytes != null)
   {
    if (strValue instanceof byte[])
    {
     byte[] strBytes = (byte[]) strValue;
     byte strCh = strBytes[strOffset++];
     do
     {
      while (bytes[fromIndex] != strCh)
       if (--fromIndex < 0)
        break;
      if (fromIndex < 0)
       break;
     } while (!regionMatches(bytes, fromIndex + 1, strBytes, strOffset,
              lastInd) && --fromIndex >= 0);
    }
     else
     {
      char[] strChars = (char[]) strValue;
      char ch = strChars[strOffset++];
      if (ch <= 0xff)
      {
       byte strCh = (byte)ch;
       do
       {
        while (bytes[fromIndex] != strCh)
         if (--fromIndex < 0)
          break;
        if (fromIndex < 0)
         break;
       } while (!regionMatches(bytes, fromIndex + 1, strChars, strOffset,
                lastInd) && --fromIndex >= 0);
      }
     }
   }
    else
    {
     char[] chars = value;
     if (strValue instanceof byte[])
     {
      byte[] strBytes = (byte[]) strValue;
      char strCh = (char)(strBytes[strOffset++] & 0xff);
      do
      {
       while (chars[fromIndex] != strCh)
        if (--fromIndex < 0)
         break;
       if (fromIndex < 0)
        break;
      } while (!regionMatches(strBytes, strOffset, chars, fromIndex + 1,
               lastInd) && --fromIndex >= 0);
     }
      else
      {
       char[] strChars = (char[]) strValue;
       char strCh = strChars[strOffset++];
       do
       {
        while (chars[fromIndex] != strCh)
         if (--fromIndex < 0)
          break;
        if (fromIndex < 0)
         break;
       } while (!regionMatches(chars, fromIndex + 1, strChars, strOffset,
                lastInd) && --fromIndex >= 0);
      }
    }
  }
  return fromIndex;
 }

 public StringBuilder reverse()
 {
  int j = count;
  ensureCapacityInner(j, false);
  byte[] bytes = this.bytes;
  int i = 0;
  if (bytes != null)
  {
   while (--j > i)
   {
    byte c = bytes[i];
    bytes[i++] = bytes[j];
    bytes[j] = c;
   }
  }
   else
   {
    char[] chars = value;
    while (--j > i)
    {
     char c = chars[i];
     chars[i++] = chars[j];
     chars[j] = c;
    }
   }
  return this;
 }

 public void trimToSize()
 {
  int count = this.count;
  if (count >= 0x20)
  {
   byte[] bytes = this.bytes;
   if (bytes != null)
   {
    int capacity = bytes.length;
    int remain = capacity - count;
    if (remain > 0x200 || (remain >= 0x20 && (capacity >> 2) < remain))
    {
     byte[] newBytes = new byte[count];
     VMSystem.arraycopy(bytes, 0, newBytes, 0, count);
     this.bytes = newBytes;
     shared = false;
    }
   }
    else
    {
     char[] chars = value;
     int capacity = chars.length;
     if (hasHighByte(chars, 0, count))
     {
      int remain = capacity - count;
      if (remain > 0x100 || (remain >= 0x20 && (capacity >> 2) < remain))
      {
       char[] newChars = new char[count];
       VMSystem.arraycopy(chars, 0, newChars, 0, count);
       value = newChars;
       shared = false;
      }
     }
      else
      {
       byte[] newBytes = new byte[count];
       copyCharsToBytes(chars, 0, newBytes, 0, count);
       this.bytes = newBytes;
       value = null;
       shared = false;
      }
    }
  }
 }

 public int codePointCount(int beginIndex, int endIndex)
 {
  if (((count - endIndex) | beginIndex) < 0 || endIndex < beginIndex)
   throw new StringIndexOutOfBoundsException();
  return bytes != null ? endIndex - beginIndex :
          Character.codePointCount(value, beginIndex, endIndex - beginIndex);
 }

 public int offsetByCodePoints(int index, int codePointOffset)
 {
  int count = this.count;
  if (((count - index) | index) < 0)
   throw new StringIndexOutOfBoundsException(index);
  if (bytes != null)
  {
   index = index + codePointOffset;
   if (((count - index) | index) < 0)
    throw new IndexOutOfBoundsException();
   return index;
  }
  return Character.offsetByCodePoints(value, 0, count, index,
          codePointOffset);
 }

 public String toString() /* hard-coded method signature */
 {
  return new String(this);
 }

 final Object value()
 {
  byte[] bytes = this.bytes;
  return bytes != null ? (Object) bytes : value;
 }

 final void setShared()
 {
  shared = true;
 }

 private void writeObject(ObjectOutputStream stream)
  throws IOException
 {
  byte[] bytes = this.bytes;
  if (bytes != null)
  {
   char[] newChars = new char[bytes.length];
   copyBytesToChars(bytes, 0, newChars, 0, count);
   value = newChars;
   this.bytes = null;
  }
  stream.defaultWriteObject();
  if (bytes != null)
  {
   this.bytes = bytes;
   value = null;
  }
 }

 private void readObject(ObjectInputStream stream)
  throws IOException, ClassNotFoundException
 {
  stream.defaultReadObject();
  char[] chars = value;
  int count = this.count;
  if (chars == null || count < 0 || chars.length < count)
   throw new InvalidObjectException("data corrupted");
  bytes = null;
  shared = true;
  if (!hasHighByte(chars, 0, count))
  {
   byte[] newBytes = new byte[chars.length];
   copyCharsToBytes(chars, 0, newBytes, 0, count);
   bytes = newBytes;
   value = null;
   shared = false;
  }
 }

 private void ensureCapacityInner(int minimumCapacity, boolean outOfByte)
 {
  byte[] bytes = this.bytes;
  char[] chars = null;
  if (minimumCapacity < 0)
   minimumCapacity = -1 >>> 1;
  int capacity = bytes != null ? bytes.length : (chars = value).length;
  if (capacity < minimumCapacity || shared || (outOfByte && bytes != null))
  {
   int newCapacity = capacity;
   if (capacity < minimumCapacity)
   {
    newCapacity = (capacity + 1) << 1;
    if (newCapacity <= minimumCapacity)
     newCapacity = minimumCapacity;
   }
   int count = this.count;
   if (bytes != null && !outOfByte)
   {
    byte[] newBytes = new byte[newCapacity];
    if (count > 0)
     VMSystem.arraycopy(bytes, 0, newBytes, 0, count);
    this.bytes = newBytes;
   }
    else
    {
     char[] newChars = new char[newCapacity];
     if (count > 0)
     {
      if (bytes != null)
      {
       for (int i = 0; i < count; i++)
        newChars[i] = (char) (bytes[i] & 0xff);
      }
       else VMSystem.arraycopy(chars, 0, newChars, 0, count);
     }
     value = newChars;
     this.bytes = null;
    }
   shared = false;
  }
 }

 private static boolean regionMatches(byte[] bytes, int offset, byte[] bytes2,
   int offset2, int count)
 {
  while (count-- > 0)
   if (bytes[offset++] != bytes2[offset2++])
    return false;
  return true;
 }

 private static boolean regionMatches(byte[] bytes, int offset, char[] chars2,
   int offset2, int count)
 {
  while (count-- > 0)
   if ((bytes[offset++] & 0xff) != chars2[offset2++])
    return false;
  return true;
 }

 private static boolean regionMatches(char[] chars, int offset, char[] chars2,
   int offset2, int count)
 {
  while (count-- > 0)
   if (chars[offset++] != chars2[offset2++])
    return false;
  return true;
 }

 private static void copyBytesToChars(byte[] bytes, int offset, char[] chars2,
   int offset2, int count)
 {
  while (count-- > 0)
   chars2[offset2++] = (char) (bytes[offset++] & 0xff);
 }

 private static void copyCharsToBytes(char[] chars, int offset, byte[] bytes2,
   int offset2, int count)
 {
  while (count-- > 0)
   bytes2[offset2++] = (byte) chars[offset++];
 }

 private static boolean hasHighByte(char[] chars, int offset, int count)
 {
  count += offset;
  while (offset < count)
   if (chars[offset++] > 0xff)
    return true;
  return false;
 }
}

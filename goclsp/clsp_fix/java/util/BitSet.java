/*
 * @(#) $(JCGO)/goclsp/clsp_fix/java/util/BitSet.java --
 * Int-based implementation of the standard BitSet class.
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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class BitSet
 implements Cloneable, Serializable
{

 private static final long serialVersionUID = 7997698588986878753L;

 private static final int INDEX_SHIFT = 5;

 private long[] bits;

 private transient int unitsInUse;

 private transient int[] intBits;

 public BitSet()
 {
  this(64);
 }

 public BitSet(int nbits)
 {
  if (nbits < 0)
   throw new NegativeArraySizeException("" + nbits);
  intBits = new int[(((nbits - 1) >> INDEX_SHIFT) + 2) & ~1];
 }

 public int length()
 {
  recalculateUnitsInUse();
  int i = unitsInUse;
  return i > 0 ? (i << INDEX_SHIFT) -
          numberOfLeadingZeros(intBits[i - 1]) : 0;
 }

 public boolean isEmpty()
 {
  recalculateUnitsInUse();
  return unitsInUse == 0;
 }

 public void set(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  if (unitsInUse <= i)
   ensureCapacity(i + 1);
  intBits[i] |= 1 << bitIndex;
 }

 public void set(int fromIndex, int toIndex)
 {
  if (fromIndex < 0 || toIndex < fromIndex)
   throw new IndexOutOfBoundsException("" + fromIndex + ", " + toIndex);
  if (fromIndex < toIndex)
  {
   int i = fromIndex >> INDEX_SHIFT;
   int j = (toIndex - 1) >> INDEX_SHIFT;
   if (unitsInUse <= j)
    ensureCapacity(j + 1);
   int[] intBits = this.intBits;
   if (i < j)
   {
    intBits[i] |= -1 << fromIndex;
    intBits[j] |= (2 << (toIndex - 1)) - 1;
    while (++i < j)
     intBits[i] = -1;
   }
    else intBits[i] |= (-1 << fromIndex) & ((2 << (toIndex - 1)) - 1);
  }
 }

 public void set(int bitIndex, boolean value)
 {
  if (value)
   set(bitIndex);
   else clear(bitIndex);
 }

 public void set(int fromIndex, int toIndex, boolean value)
 {
  if (value)
   set(fromIndex, toIndex);
   else clear(fromIndex, toIndex);
 }

 public void clear()
 {
  int i = unitsInUse;
  if (i > 0)
  {
   int[] intBits = this.intBits;
   unitsInUse = 0;
   do
   {
    intBits[--i] = 0;
   } while (i > 0);
  }
 }

 public void clear(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  if (unitsInUse > i)
   intBits[i] &= ~(1 << bitIndex);
 }

 public void clear(int fromIndex, int toIndex)
 {
  if (fromIndex < 0 || toIndex < fromIndex)
   throw new IndexOutOfBoundsException("" + fromIndex + ", " + toIndex);
  if (fromIndex < toIndex)
  {
   int i = fromIndex >> INDEX_SHIFT;
   int j = unitsInUse - 1;
   if (i <= j)
   {
    int[] intBits = this.intBits;
    int k = (toIndex - 1) >> INDEX_SHIFT;
    if (j >= k)
     j = k;
    if (i < j)
    {
     intBits[i] &= (1 << fromIndex) - 1;
     intBits[j] &= -2 << (toIndex - 1);
     while (++i < j)
      intBits[i] = 0;
    }
     else intBits[i] &= ((1 << fromIndex) - 1) | (-2 << (toIndex - 1));
   }
  }
 }

 public boolean get(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  return unitsInUse > i && (intBits[i] & (1 << bitIndex)) != 0;
 }

 public BitSet get(int fromIndex, int toIndex)
 {
  if (fromIndex < 0 || toIndex < fromIndex)
   throw new IndexOutOfBoundsException("" + fromIndex + ", " + toIndex);
  BitSet set = new BitSet(toIndex - fromIndex);
  if (fromIndex < toIndex)
  {
   int i = fromIndex >> INDEX_SHIFT;
   int j = unitsInUse;
   if (i < j)
   {
    int[] intBits = this.intBits;
    int[] intBits2 = set.intBits;
    int k = (toIndex - 1) >> INDEX_SHIFT;
    if (j >= k)
     j = k;
    k = j - i;
    set.unitsInUse = k + 1;
    if ((fromIndex & ((1 << INDEX_SHIFT) - 1)) != 0)
    {
     int v = intBits[i];
     if (k > 0)
     {
      int revShift = -fromIndex;
      k = 0;
      do
      {
       int w = intBits[++i];
       intBits2[k++] = (v >>> fromIndex) | (w << revShift);
       v = w;
      } while (i < j);
     }
     intBits2[k] = (((2 << (toIndex - 1)) - 1) & v) >>> fromIndex;
    }
     else
     {
      if (k > 0)
       System.arraycopy(intBits, i, intBits2, 0, k);
      intBits2[k] = intBits[j] & ((2 << (toIndex - 1)) - 1);
     }
   }
  }
  return set;
 }

 public void flip(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  if (unitsInUse <= i)
   ensureCapacity(i + 1);
  intBits[i] ^= 1 << bitIndex;
 }

 public void flip(int fromIndex, int toIndex)
 {
  if (fromIndex < 0 || toIndex < fromIndex)
   throw new IndexOutOfBoundsException("" + fromIndex + ", " + toIndex);
  if (fromIndex < toIndex)
  {
   int i = fromIndex >> INDEX_SHIFT;
   int j = (toIndex - 1) >> INDEX_SHIFT;
   if (unitsInUse <= j)
    ensureCapacity(j + 1);
   int[] intBits = this.intBits;
   if (i < j)
   {
    intBits[i] ^= -1 << fromIndex;
    intBits[j] ^= (2 << (toIndex - 1)) - 1;
    while (++i < j)
     intBits[i] ^= -1;
   }
    else intBits[i] ^= (-1 << fromIndex) & ((2 << (toIndex - 1)) - 1);
  }
 }

 public void and(BitSet set)
 {
  if (set != this)
  {
   set.recalculateUnitsInUse();
   recalculateUnitsInUse();
   int i = unitsInUse;
   int j = set.unitsInUse;
   int[] intBits = this.intBits;
   int[] intBits2 = set.intBits;
   if (i > j)
   {
    unitsInUse = j;
    do
    {
     intBits[--i] = 0;
    } while (i > j);
   }
   while (i-- > 0)
    intBits[i] &= intBits2[i];
  }
 }

 public void andNot(BitSet set)
 {
  if (set != this)
  {
   set.recalculateUnitsInUse();
   recalculateUnitsInUse();
   int i = unitsInUse;
   int[] intBits = this.intBits;
   int[] intBits2 = set.intBits;
   int j = set.unitsInUse;
   if (i >= j)
    i = j;
   while (i-- > 0)
    intBits[i] &= ~intBits2[i];
  }
   else clear();
 }

 public void or(BitSet set)
 {
  if (set != this)
  {
   set.recalculateUnitsInUse();
   recalculateUnitsInUse();
   int i = unitsInUse;
   int j = set.unitsInUse;
   if (i < j)
    ensureCapacity(j);
   int[] intBits = this.intBits;
   int[] intBits2 = set.intBits;
   while (i < j)
   {
    j--;
    intBits[j] = intBits2[j];
   }
   while (j-- > 0)
    intBits[j] |= intBits2[j];
  }
 }

 public void xor(BitSet set)
 {
  if (set != this)
  {
   set.recalculateUnitsInUse();
   recalculateUnitsInUse();
   int i = unitsInUse;
   int j = set.unitsInUse;
   if (i < j)
    ensureCapacity(j);
   int[] intBits = this.intBits;
   int[] intBits2 = set.intBits;
   while (i < j)
   {
    j--;
    intBits[j] = intBits2[j];
   }
   while (j-- > 0)
    intBits[j] ^= intBits2[j];
  }
   else clear();
 }

 public boolean intersects(BitSet set)
 {
  if (set == this)
   return true;
  set.recalculateUnitsInUse();
  recalculateUnitsInUse();
  int i = unitsInUse;
  int[] intBits = this.intBits;
  int[] intBits2 = set.intBits;
  int j = set.unitsInUse;
  if (i >= j)
   i = j;
  while (i-- > 0)
   if ((bits[i] & intBits2[i]) != 0)
    return true;
  return false;
 }

 public int nextClearBit(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  int j = unitsInUse;
  if (i < j)
  {
   int[] intBits = this.intBits;
   int v = intBits[i] | ((1 << bitIndex) - 1);
   while (++i < j && v == -1)
    v = intBits[i];
   bitIndex = numberOfTrailingZeros(~v) + ((i - 1) << INDEX_SHIFT);
  }
  return bitIndex;
 }

 public int nextSetBit(int bitIndex)
 {
  if (bitIndex < 0)
   throw new IndexOutOfBoundsException("" + bitIndex);
  int i = bitIndex >> INDEX_SHIFT;
  int j = unitsInUse;
  if (i < j)
  {
   int[] intBits = this.intBits;
   int v = intBits[i] & (-1 << bitIndex);
   while (v == 0 && ++i < j)
    v = intBits[i];
   if (i < j)
    return numberOfTrailingZeros(v) + (i << INDEX_SHIFT);
  }
  return -1;
 }

 public int size()
 {
  return intBits.length << INDEX_SHIFT;
 }

 public int cardinality()
 {
  recalculateUnitsInUse();
  int i = unitsInUse;
  int count = 0;
  int[] intBits = this.intBits;
  while (i-- > 0)
   count += bitCount(intBits[i]);
  return count;
 }

 public int hashCode()
 {
  recalculateUnitsInUse();
  int h = 1234;
  int[] intBits = this.intBits;
  for (int i = (unitsInUse + 1) >> 1; i > 0; i--)
  {
   int v = intBits[(i - 1) << 1];
   h ^= (mulHigh(v, i) + intBits[(i << 1) - 1] * i) ^ (v * i);
  }
  return h;
 }

 public boolean equals(Object obj)
 {
  if (obj != this)
  {
   if (!(obj instanceof BitSet))
    return false;
   BitSet set = (BitSet) obj;
   recalculateUnitsInUse();
   set.recalculateUnitsInUse();
   int i = unitsInUse;
   if (set.unitsInUse != i)
    return false;
   int[] intBits = this.intBits;
   int[] intBits2 = set.intBits;
   while (i-- > 0)
    if (intBits[i] != intBits2[i])
     return false;
  }
  return true;
 }

 public Object clone()
 {
  BitSet result;
  try
  {
   result = (BitSet) super.clone();
  }
  catch (CloneNotSupportedException e)
  {
   throw new InternalError();
  }
  int[] intBits = this.intBits;
  System.arraycopy(intBits, 0, result.intBits = new int[intBits.length], 0,
   unitsInUse);
  return result;
 }

 private void writeObject(ObjectOutputStream output)
  throws IOException
 {
  int[] intBits = this.intBits;
  long[] bits = new long[intBits.length >> 1];
  int i = (unitsInUse + 1) >> 1;
  while (i-- > 0)
   bits[i] = ((long) intBits[(i << 1) + 1] << 32) |
              (long) intBits[i << 1] & 0xffffffffL;
  this.bits = bits;
  output.defaultWriteObject();
  this.bits = null;
 }

 private void readObject(ObjectInputStream input)
  throws IOException, ClassNotFoundException
 {
  input.defaultReadObject();
  long[] bits = this.bits;
  int i = bits.length;
  this.bits = null;
  int[] intBits = new int[i << 1];
  unitsInUse = i << 1;
  while (i-- > 0)
  {
   long v = bits[i];
   intBits[i << 1] = (int) v;
   intBits[(i << 1) + 1] = (int) (v >>> 32);
  }
  this.intBits = intBits;
  recalculateUnitsInUse();
 }

 public String toString()
 {
  StringBuilder buffer = new StringBuilder();
  buffer.append('{');
  int[] intBits = this.intBits;
  int len = unitsInUse;
  boolean next = false;
  for (int i = 0; i < len; i++)
  {
   int v = intBits[i];
   for (int j = 0; v != 0; j++)
   {
    if ((v & 1) != 0)
    {
     if (next)
      buffer.append(',').append(' ');
     buffer.append((i << INDEX_SHIFT) | j);
     next = true;
    }
    v >>>= 1;
   }
  }
  return buffer.append('}').toString();
 }

 private void ensureCapacity(int unitsRequired)
 {
  int[] intBits = this.intBits;
  if (intBits.length < unitsRequired)
  {
   int newLen = intBits.length << 1;
   if (newLen < 0)
    newLen = -1 >>> 1;
   if (unitsRequired + 1 > newLen)
    newLen = (unitsRequired + 1) & ~1;
   int[] newBits = new int[newLen];
   System.arraycopy(intBits, 0, newBits, 0, unitsInUse);
   this.intBits = newBits;
  }
  unitsInUse = unitsRequired;
 }

 private void recalculateUnitsInUse()
 {
  int i = unitsInUse - 1;
  if (i >= 0)
  {
   int[] intBits = this.intBits;
   if (intBits[i] == 0)
   {
    while (i-- > 0)
     if (intBits[i] != 0)
      break;
    unitsInUse = i + 1;
   }
  }
 }

 private static int numberOfLeadingZeros(int value)
 {
  value |= value >>> 1;
  value |= value >>> 2;
  value |= value >>> 4;
  value |= value >>> 8;
  return bitCount(~((value >>> 16) | value));
 }

 private static int numberOfTrailingZeros(int value)
 {
  return bitCount((-value & value) - 1);
 }

 private static int bitCount(int value)
 {
  if (((value + 1) >> 1) == 0)
   return value != 0 ? 32 : 0;
  value = ((value >> 1) & 0x55555555) + (value & 0x55555555);
  value = ((value >> 2) & 0x33333333) + (value & 0x33333333);
  value = ((value >> 4) & 0xf0f0f0f) + (value & 0xf0f0f0f);
  value = ((value >> 8) & 0xff00ff) + (value & 0xff00ff);
  return ((value >> 16) & 0xffff) + (value & 0xffff);
 }

 private static int mulHigh(int valueA, int valueB)
 {
  int highA = valueA >>> 16;
  int highB = valueB >>> 16;
  valueA &= 0xffff;
  int lowB = valueB & 0xffff;
  valueA = ((valueA * lowB) >>> 16) + valueA * highB;
  lowB *= highA;
  valueA += lowB;
  highA = highA * highB + (valueA >>> 16);
  if ((valueA ^ lowB) >= 0)
   lowB = valueA - lowB;
  if (lowB < 0)
   highA += 0x10000;
  return highA;
 }
}

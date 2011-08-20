/*
 * @(#) $(JCGO)/goclsp/vm/java/lang/VMDouble.java --
 * VM specific methods for Java "Double" class.
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

package java.lang;

final class VMDouble
{

 private static final class FDBigInt
 {

  private int nWords;

  private int[] data;

  FDBigInt(int v)
  {
   nWords = 1;
   data = new int[1];
   data[0] = v;
  }

  FDBigInt(long v)
  {
   data = new int[2];
   data[0] = (int) v;
   data[1] = (int) (v >>> 32);
   nWords = data[1] != 0 ? 2 : 1;
  }

  FDBigInt(FDBigInt other)
  {
   nWords = other.nWords;
   data = new int[nWords];
   VMSystem.arraycopy(other.data, 0, data, 0, nWords);
  }

  FDBigInt(long l, char[] ac, int i, int j)
  {
   int k = (j + 8) / 9;
   if (k < 2)
    k = 2;
   data = new int[k];
   data[0] = (int) l;
   data[1] = (int) (l >>> 32);
   nWords = 1;
   if (data[1] != 0)
    nWords = 2;
   int i1 = i;
   int l1 = j - 5;
   while (i1 < l1)
   {
    int i2 = i1 + 5;
    int j1 = ac[i1++] - '0';
    while (i1 < i2)
     j1 = 10 * j1 + ac[i1++] - '0';
    multaddMe(100000, j1);
   }
   int j2 = 1;
   int k1 = 0;
   while (i1 < j)
   {
    k1 = 10 * k1 + ac[i1++] - '0';
    j2 *= 10;
   }
   if (j2 != 1)
    multaddMe(j2, k1);
  }

  private FDBigInt(int[] ai, int i)
  {
   data = ai;
   nWords = i;
  }

  void lshiftMe(int i)
   throws IllegalArgumentException
  {
   if (i <= 0)
   {
    if (i == 0)
     return;
    throw new IllegalArgumentException("negative shift count");
   }
   int j = i >> 5;
   int k = i & 0x1f;
   int l = 32 - k;
   int[] ai = data;
   int[] ai1 = data;
   if (nWords + j >= ai.length)
    ai = new int[nWords + j + 1];
   int i1 = nWords + j;
   int j1 = nWords - 1;
   if (k == 0)
   {
    VMSystem.arraycopy(ai1, 0, ai, j, nWords);
    i1 = j - 1;
   }
    else
    {
     for (ai[i1--] = ai1[j1] >>> l; j1 >= 1; ai[i1--] |= ai1[--j1] >>> l)
      ai[i1] = ai1[j1] << k;
     ai[i1--] = ai1[j1] << k;
    }
   while (i1 >= 0)
    ai[i1--] = 0;
   data = ai;
   nWords += j + 1;
   while (nWords > 1 && data[nWords - 1] == 0)
    nWords--;
  }

  int normalizeMe()
   throws IllegalArgumentException
  {
   int i = 0;
   int j = 0;
   int k = 0;
   int l;
   for (l = nWords - 1; l >= 0 && (k = data[l]) == 0; l--)
    i++;
   if (l < 0)
    throw new IllegalArgumentException("zero value");
   nWords -= i;
   if ((k & 0xf0000000) != 0)
   {
    for (j = 32; (k & 0xf0000000) != 0; j--)
     k >>>= 1;
   }
    else
    {
     while (k <= 0xfffff)
     {
      k <<= 8;
      j += 8;
     }
     while (k <= 0x7ffffff)
     {
      k <<= 1;
      j++;
     }
    }
   if (j != 0)
    lshiftMe(j);
   return j;
  }

  FDBigInt mult(int i)
  {
   long l = i;
   int[] ai = new int[l * ((long) data[nWords - 1] & 0xffffffffL) >
               0xfffffffL ? nWords + 1 : nWords];
   long l1 = 0L;
   for (int j = 0; j < nWords; l1 >>>= 32)
   {
    l1 += ((long) data[j] & 0xffffffffL) * l;
    ai[j++] = (int) l1;
   }
   if (l1 == 0L)
    return new FDBigInt(ai, nWords);
   ai[nWords] = (int) l1;
   return new FDBigInt(ai, nWords + 1);
  }

  private void multaddMe(int i, int j)
  {
   long l = i;
   long l1 = ((long) data[0] & 0xffffffffL) * l + ((long) j & 0xffffffffL);
   data[0] = (int) l1;
   l1 >>>= 32;
   for (int k = 1; k < nWords; l1 >>>= 32)
   {
    l1 += l * ((long) data[k] & 0xffffffffL);
    data[k++] = (int) l1;
   }
   if (l1 != 0L)
    data[nWords++] = (int) l1;
  }

  FDBigInt mult(FDBigInt other)
  {
   int[] ai = new int[nWords + other.nWords];
   for (int i = 0; i < nWords; i++)
   {
    long l = (long) data[i] & 0xffffffffL;
    long l1 = 0L;
    int k;
    for (k = 0; k < other.nWords; k++)
    {
     l1 += ((long) ai[i + k] & 0xffffffffL) +
            ((long) other.data[k] & 0xffffffffL) * l;
     ai[i + k] = (int) l1;
     l1 >>>= 32;
    }
    ai[i + k] = (int) l1;
   }
   int j = ai.length - 1;
   while (j > 0 && ai[j] == 0)
    j--;
   return new FDBigInt(ai, j + 1);
  }

  FDBigInt add(FDBigInt other)
  {
   long l = 0L;
   int[] ai;
   int[] ai1;
   int i;
   int j;
   if (nWords >= other.nWords)
   {
    ai = data;
    i = nWords;
    ai1 = other.data;
    j = other.nWords;
   }
    else
    {
     ai = other.data;
     i = other.nWords;
     ai1 = data;
     j = nWords;
    }
   int[] ai2 = new int[i];
   int k;
   for (k = 0; k < i; l >>= 32)
   {
    l += (long) ai[k] & 0xffffffffL;
    if (k < j)
     l += (long) ai1[k] & 0xffffffffL;
    ai2[k++] = (int) l;
   }
   if (l != 0L)
   {
    int[] ai3 = new int[i + 1];
    VMSystem.arraycopy(ai2, 0, ai3, 0, i);
    ai3[k++] = (int) l;
    ai2 = ai3;
   }
   return new FDBigInt(ai2, k);
  }

  FDBigInt sub(FDBigInt other)
  {
   int[] ai = new int[nWords];
   int i = nWords;
   int j = other.nWords;
   int k = 0;
   long l = 0L;
   int i1;
   for (i1 = 0; i1 < i; l >>= 32)
   {
    l += (long) data[i1] & 0xffffffffL;
    if (i1 < j)
     l -= (long) other.data[i1] & 0xffffffffL;
    if ((ai[i1++] = (int) l) == 0)
     k++;
     else k = 0;
   }
   if (l != 0L)
    throw new InternalError("FP assertion: borrow out of subtract");
   while (i1 < j)
    if (other.data[i1++] != 0)
     throw new InternalError("FP assertion: negative result of subtract");
   return new FDBigInt(ai, i - k);
  }

  int cmp(FDBigInt other)
  {
   int i;
   if (nWords > other.nWords)
   {
    int j = other.nWords - 1;
    for (i = nWords - 1; i > j; i--)
     if (data[i] != 0)
      return 1;
   }
    else
    {
     if (nWords < other.nWords)
     {
      for (i = other.nWords - 1; i >= nWords; i--)
       if (other.data[i] != 0)
        return -1;
     }
      else i = nWords - 1;
    }
   while (i > 0 && data[i] == other.data[i])
    i--;
   int k = data[i];
   int l = other.data[i];
   return k < 0 ? (l < 0 ? k - l : 1) : l < 0 ? -1 : k - l;
  }

  int quoRemIteration(FDBigInt other)
   throws IllegalArgumentException
  {
   if (nWords != other.nWords)
    throw new IllegalArgumentException("disparate values");
   int i = nWords - 1;
   long l = ((long) data[i] & 0xffffffffL) / (long) other.data[i];
   long l1 = 0L;
   for (int j = 0; j <= i; l1 >>= 32)
   {
    l1 += ((long) data[j] & 0xffffffffL) -
           l * ((long) other.data[j] & 0xffffffffL);
    data[j++] = (int) l1;
   }
   long l3;
   if (l1 != 0L)
    do
    {
     l3 = 0L;
     for (int k = 0; k <= i; l3 >>= 32)
     {
      l3 += ((long) data[k] & 0xffffffffL) +
             ((long) other.data[k] & 0xffffffffL);
      data[k++] = (int) l3;
     }
     if (l3 != 0L && l3 != 1L)
      throw new InternalError("FP assertion: ".concat(
             Long.toString(l3)).concat(" carry out of division correction"));
     l--;
    } while (l3 == 0L);
   l3 = 0L;
   for (int i1 = 0; i1 <= i; l3 >>= 32)
   {
    l3 += ((long) data[i1] & 0xffffffffL) * 10L;
    data[i1++] = (int) l3;
   }
   if (l3 != 0L)
    throw new InternalError("FP assertion: carry out of *10");
   return (int) l;
  }
 }

 private static final class FloatingDecimal
 {

  private static final long signMask = 0x8000000000000000L;

  private static final long expMask = 0x7ff0000000000000L;

  private static final long fractMask = 0xfffffffffffffL;

  private static final int expShift = 52;

  private static final int expBias = 0x3ff;

  private static final long fractHOB = 0x10000000000000L;

  private static final int maxSmallBinExp = 62;

  private static final int minSmallBinExp = -(63 / 3);

  private static final int maxDecimalDigits = 15;

  private static final int maxDecimalExponent = 308;

  private static final int minDecimalExponent = -324;

  private static final int bigDecimalExponent = 324;

  private static final int singleSignMask = 0x80000000;

  private static final int singleExpMask = 0x7f800000;

  private static final int singleFractMask = ~(singleSignMask | singleExpMask);

  private static final int singleExpShift = 23;

  private static final int singleFractHOB = 1 << singleExpShift;

  private static final int singleExpBias = 127;

  private static final int singleMaxDecimalDigits = 7;

  private static final int singleMaxDecimalExponent = 38;

  private static final int singleMinDecimalExponent = -45;

  private static volatile FDBigInt[] b5p = new FDBigInt[8];

  private static final char[] infinity =
  {
   'I', 'n', 'f', 'i', 'n', 'i', 't', 'y'
  };

  private static final char[] notANumber =
  {
   'N', 'a', 'N'
  };

  private static final char[] zero =
  {
   '0', '0', '0', '0', '0', '0', '0', '0'
  };

  private static final double[] small10pow =
  {
   1.0, 10.0, 100.0, 1000.0, 1.0e4, 1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
   1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19,
   1.0e20, 1.0e21, 1.0e22
  };

  private static final int maxSmallTen = 22 /* small10pow.length - 1 */;

  private static final float[] singleSmall10pow =
  {
   1.0F, 10.0F, 100.0F, 1000.0F, 1.0e4F, 1.0e5F, 1.0e6F, 1.0e7F, 1.0e8F,
   1.0e9F, 1.0e10F
  };

  private static final int singleMaxSmallTen =
   10 /* singleSmall10pow.length - 1 */;

  private static final double[] big10pow =
  {
   1e16, 1e32, 1e64, 1e128, 1e256
  };

  private static final double[] tiny10pow =
  {
   1e-16, 1e-32, 1e-64, 1e-128, 1e-256
  };

  private static final int[] n5bits =
  {
   0, 3, 5, 7, 10, 12, 14, 17, 19, 21, 24, 26, 28, 31, 33, 35, 38, 40, 42,
   45, 47, 49, 52, 54, 56, 59, 61
  };

  private static final int[] small5pow =
  {
   1,
   5,
   5*5,
   5*5*5,
   5*5*5*5,
   5*5*5*5*5,
   5*5*5*5*5*5,
   5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5*5*5*5*5,
   5*5*5*5*5*5*5*5*5*5*5*5*5
  };

  private static final long[] long5pow =
  {
   1L,
   5L,
   5L*5,
   5L*5*5,
   5L*5*5*5,
   5L*5*5*5*5,
   5L*5*5*5*5*5,
   5L*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
   5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5
  };

  private boolean isExceptional;

  private boolean isNegative;

  private int decExponent;

  private char[] digits;

  private int nDigits;

  private int bigIntExp;

  private int bigIntNBits;

  private boolean mustSetRoundDir;

  private int roundDir;

  FloatingDecimal(double d)
  {
   long l = doubleToRawLongBits(d);
   if ((l & signMask) != 0L)
   {
    isNegative = true;
    l ^= signMask;
   }
    else isNegative = false;
   int i = (int) ((l & expMask) >> expShift);
   long l1 = l & fractMask;
   if (i == 0x7ff)
   {
    isExceptional = true;
    if (l1 == 0L)
     digits = infinity;
     else
     {
      digits = notANumber;
      isNegative = false;
     }
    nDigits = digits.length;
   }
    else
    {
     isExceptional = false;
     int j;
     if (i == 0)
     {
      if (l1 == 0L)
      {
       decExponent = 0;
       digits = zero;
       nDigits = 1;
       return;
      }
      while ((~l1 & fractHOB) != 0L)
      {
       l1 <<= 1;
       i--;
      }
      j = expShift + i + 1;
      i++;
     }
      else
      {
       l1 |= fractHOB;
       j = expShift + 1;
      }
     i -= expBias;
     dtoa(i, l1, j);
    }
  }

  FloatingDecimal(float f)
  {
   int i = VMFloat.floatToRawIntBits(f);
   if ((i & singleSignMask) != 0)
   {
    isNegative = true;
    i ^= singleSignMask;
   }
    else isNegative = false;
   int j = (i & singleExpMask) >> singleExpShift;
   int k = i & singleFractMask;
   if (j == 0xff)
   {
    isExceptional = true;
    if (k == 0)
     digits = infinity;
     else
     {
      digits = notANumber;
      isNegative = false;
     }
    nDigits = digits.length;
   }
    else
    {
     isExceptional = false;
     int l;
     if (j == 0)
     {
      if (k == 0)
      {
       decExponent = 0;
       digits = zero;
       nDigits = 1;
       return;
      }
      while ((~k & singleFractHOB) != 0)
      {
       k <<= 1;
       j--;
      }
      l = singleExpShift + j + 1;
      j++;
     }
      else
      {
       k |= singleFractHOB;
       l = singleExpShift + 1;
      }
     j -= singleExpBias;
     dtoa(j, (long) k << (expShift - singleExpShift), l);
    }
  }

  private FloatingDecimal(boolean negSign, int i, char[] ac, int j,
    boolean isExc)
  {
   isNegative = negSign;
   decExponent = i;
   digits = ac;
   nDigits = j;
   isExceptional = isExc;
  }

  private static int countBits(long l)
  {
   int i = 0;
   if (l != 0L)
   {
    if (((int) l) == 0)
     l >>= 32;
    if (((short) l) == 0)
     l >>= 16;
    if (((byte) l) == 0)
     l >>= 8;
    while (((int) l & 1) == 0)
     l >>= 1;
    while ((l & 0xffL) != l)
    {
     l >>= 8;
     i += 8;
    }
    int j = (int) l;
    while (j != 0)
    {
     j >>= 1;
     i++;
    }
   }
   return i;
  }

  private static FDBigInt big5pow(int i)
  {
   if (i < 0)
    throw new InternalError("FP assertion: negative power of 5");
   if (i < b5p.length && b5p[i] != null)
    return b5p[i];
   synchronized (FloatingDecimal.class)
   {
    if (b5p.length <= i)
    {
     FDBigInt[] afdbigint = new FDBigInt[(i >> 1) + i + 1];
     VMSystem.arraycopy(b5p, 0, afdbigint, 0, b5p.length);
     b5p = afdbigint;
    }
    if (b5p[i] != null)
     return b5p[i];
    if (i < small5pow.length)
     return b5p[i] = new FDBigInt(small5pow[i]);
    if (i < long5pow.length)
     return b5p[i] = new FDBigInt(long5pow[i]);
    int j = i >> 1;
    int k = i - j;
    FDBigInt fdbigint = b5p[j];
    if (fdbigint == null)
     fdbigint = big5pow(j);
    if (k < small5pow.length)
     return b5p[i] = fdbigint.mult(small5pow[k]);
    FDBigInt fdbigint1 = b5p[k];
    if (fdbigint1 == null)
     fdbigint1 = big5pow(k);
    return b5p[i] = fdbigint.mult(fdbigint1);
   }
  }

  private static FDBigInt multPow52(FDBigInt fdbigint, int i, int j)
  {
   if (i != 0)
    fdbigint = i < small5pow.length ? fdbigint.mult(small5pow[i]) :
                fdbigint.mult(big5pow(i));
   if (j != 0)
    fdbigint.lshiftMe(j);
   return fdbigint;
  }

  private static FDBigInt constructPow52(int i, int j)
  {
   FDBigInt fdbigint = new FDBigInt(big5pow(i));
   if (j != 0)
    fdbigint.lshiftMe(j);
   return fdbigint;
  }

  private FDBigInt doubleToBigInt(double d)
  {
   long l = doubleToRawLongBits(d) & ~signMask;
   int i = (int) (l >>> expShift);
   l &= fractMask;
   if (i > 0)
    l |= fractHOB;
    else
    {
     if (l == 0L)
      throw new InternalError("FP assertion: doubleToBigInt(0)");
     for (i++; (~l & fractHOB) != 0L; i--)
      l <<= 1;
    }
   i -= expBias;
   int j = countBits(l);
   int k = expShift - j + 1;
   bigIntExp = i - j + 1;
   bigIntNBits = j;
   return new FDBigInt(l >>> k);
  }

  private static double ulp(double d, boolean flag)
  {
   long l = doubleToRawLongBits(d) & ~signMask;
   int i = (int) (l >>> expShift);
   if (flag && i >= expShift && (l & fractMask) == 0L)
    i--;
   double d1 = i > expShift ?
                longBitsToDouble((long) (i - expShift) << expShift) :
                i != 0 ? longBitsToDouble(1L << (i - 1)) : Double.MIN_VALUE;
   if (flag)
    d1 = -d1;
   return d1;
  }

  private float stickyRound(double d)
  {
   long l = doubleToRawLongBits(d);
   long l1 = l & expMask;
   return l1 == 0L || l1 == expMask ? (float) d :
           (float) longBitsToDouble(l + (long) roundDir);
  }

  private void developLongDigits(int i, long l, long l1)
  {
   int j;
   for (j = 0; l1 >= 10L; j++)
    l1 /= 10L;
   if (j != 0)
   {
    long l2 = long5pow[j] << j;
    long l3 = l % l2;
    l /= l2;
    i += j;
    if (l3 >= l2 >> 1)
     l++;
   }
   char[] ac;
   int k;
   int i1;
   if (l < 0x7fffffffL)
   {
    if (l <= 0L)
     throw new InternalError("FP assertion: non-positive value ".concat(
            Long.toString(l)));
    int j1 = (int) l;
    k = 10;
    ac = new char[10];
    i1 = k - 1;
    int i2 = j1 % 10;
    for (j1 /= 10; i2 == 0; i++)
    {
     i2 = j1 % 10;
     j1 /= 10;
    }
    while (j1 != 0)
    {
     ac[i1--] = (char) (i2 + '0');
     i++;
     i2 = j1 % 10;
     j1 /= 10;
    }
    ac[i1] = (char) (i2 + '0');
   }
    else
    {
     k = 20;
     ac = new char[20];
     i1 = k - 1;
     int k1 = (int) (l % 10L);
     for (l /= 10L; k1 == 0; i++)
     {
      k1 = (int) (l % 10L);
      l /= 10L;
     }
     while (l != 0L)
     {
      ac[i1--] = (char) (k1 + '0');
      i++;
      k1 = (int) (l % 10L);
      l /= 10L;
     }
     ac[i1] = (char) (k1 + '0');
    }
   k -= i1;
   char[] ac1 = ac;
   if (i1 != 0)
   {
    ac1 = new char[k];
    VMSystem.arraycopy(ac, i1, ac1, 0, k);
   }
   digits = ac1;
   decExponent = i + 1;
   nDigits = k;
  }

  private void roundup()
  {
   int i = nDigits - 1;
   char c = digits[i];
   if (c == '9')
   {
    while (c == '9' && i > 0)
    {
     digits[i] = '0';
     c = digits[--i];
    }
    if (c == '9')
    {
     decExponent++;
     digits[0] = '1';
     return;
    }
   }
   digits[i] = (char) (c + 1);
  }

  private void dtoa(int i, long l, int j)
  {
   int k = countBits(l);
   int i1 = k - i - 1;
   if (i1 <= 0)
    i1 = 0;
   if (i <= maxSmallBinExp && i >= minSmallBinExp && i1 < long5pow.length &&
       k + n5bits[i1] < 64 && i1 == 0)
   {
    long l1 = i > j ? 1L << (i - j - 1) : 0L;
    if (i >= expShift)
     l <<= i - expShift;
     else l >>>= expShift - i;
    developLongDigits(0, l, l1);
    return;
   }
   int j1 = (int) (((((((l & ((1L << (expShift >> 1)) - 1L)) *
             0x1287A762C9L) >>> (expShift >> 1)) + ((l & ~fractHOB) >>
             (expShift >> 1)) * 0x1287A762C9L) >>> (64 - expShift)) +
             (long) i * 0x4D104D427DE7EL + 0x8050250F0BBFL) >> expShift);
   int k1 = -j1;
   if (k1 <= 0)
    k1 = 0;
   int i2 = k1 + i1 + i;
   int j2 = j1;
   if (j2 <= 0)
    j2 = 0;
   int k2 = j2 + i1;
   int l2 = k1;
   int i3 = i2 - j;
   l >>>= expShift - k + 1;
   i2 -= k - 1;
   int j3 = i2;
   if (i2 >= k2)
    j3 = k2;
   i2 -= j3;
   k2 -= j3;
   i3 -= j3;
   if (k == 1)
    i3--;
   if (i3 < 0)
   {
    i2 -= i3;
    k2 -= i3;
    i3 = 0;
   }
   char[] ac = new char[18];
   int k3 = 0;
   int l3 = (k1 < n5bits.length ? n5bits[k1] : k1 * 3) + k + i2;
   int i4 = (j2 + 1 < n5bits.length ? n5bits[j2 + 1] : (j2 + 1) * 3) + k2 + 1;
   digits = ac;
   boolean flag;
   boolean flag1;
   long l4;
   if (l3 < 64 && i4 < 64)
   {
    if (l3 < 32 && i4 < 32)
    {
     int j4 = ((int) l * small5pow[k1]) << i2;
     int k4 = small5pow[j2] << k2;
     int i5 = small5pow[l2] << i3;
     int j5 = k4 * 10;
     k3 = 0;
     int i6 = j4 / k4;
     j4 = 10 * (j4 % k4);
     i5 *= 10;
     flag = j4 < i5;
     flag1 = j4 + i5 > j5;
     if (i6 >= 10)
      throw new InternalError("FP assertion: excessivly large digit ".concat(
             String.valueOf(i6)));
     if (i6 == 0 && !flag1)
      j1--;
      else ac[k3++] = (char) (i6 + '0');
     if (j1 <= -3 || j1 >= 8)
     {
      flag = false;
      flag1 = false;
     }
     while (!flag && !flag1)
     {
      int j6 = j4 / k4;
      j4 = 10 * (j4 % k4);
      i5 *= 10;
      if (j6 >= 10)
       throw new InternalError("FP assertion: excessivly large digit ".concat(
              String.valueOf(j6)));
      if (i5 > 0)
      {
       flag = j4 < i5;
       flag1 = j4 + i5 > j5;
      }
       else
       {
        flag = true;
        flag1 = true;
       }
      ac[k3++] = (char) (j6 + '0');
     }
     l4 = (j4 << 1) - j5;
    }
     else
     {
      long l5 = (l * long5pow[k1]) << i2;
      long l6 = long5pow[j2] << k2;
      long l7 = long5pow[l2] << i3;
      long l8 = l6 * 10L;
      k3 = 0;
      int j7 = (int) (l5 / l6);
      l5 = 10L * (l5 % l6);
      l7 *= 10L;
      flag = l5 < l7;
      flag1 = l5 + l7 > l8;
      if (j7 >= 10)
       throw new InternalError("FP assertion: excessivly large digit ".concat(
              String.valueOf(j7)));
      if (j7 == 0 && !flag1)
       j1--;
       else ac[k3++] = (char) (j7 + '0');
      if (j1 <= -3 || j1 >= 8)
      {
       flag = false;
       flag1 = false;
      }
      while (!flag && !flag1)
      {
       int k7 = (int) (l5 / l6);
       l5 = 10L * (l5 % l6);
       l7 *= 10L;
       if (k7 >= 10)
        throw new InternalError(
               "FP assertion: excessivly large digit ".concat(
               String.valueOf(k7)));
       if (l7 > 0L)
       {
        flag = l5 < l7;
        flag1 = l5 + l7 > l8;
       }
        else
        {
         flag = true;
         flag1 = true;
        }
       ac[k3++] = (char) (k7 + '0');
      }
      l4 = (l5 << 1) - l8;
     }
   }
    else
    {
     FDBigInt fdbigint = multPow52(new FDBigInt(l), k1, i2);
     FDBigInt fdbigint1 = constructPow52(j2, k2);
     FDBigInt fdbigint2 = constructPow52(l2, i3);
     int k5;
     fdbigint.lshiftMe(k5 = fdbigint1.normalizeMe());
     fdbigint2.lshiftMe(k5);
     FDBigInt fdbigint3 = fdbigint1.mult(10);
     k3 = 0;
     int k6 = fdbigint.quoRemIteration(fdbigint1);
     fdbigint2 = fdbigint2.mult(10);
     flag = fdbigint.cmp(fdbigint2) < 0;
     flag1 = fdbigint.add(fdbigint2).cmp(fdbigint3) > 0;
     if (k6 >= 10)
      throw new InternalError("FP assertion: excessivly large digit ".concat(
             String.valueOf(k6)));
     if (k6 == 0 && !flag1)
      j1--;
      else ac[k3++] = (char) (k6 + '0');
     if (j1 <= -3 || j1 >= 8)
     {
      flag = false;
      flag1 = false;
     }
     while (!flag && !flag1)
     {
      int i7 = fdbigint.quoRemIteration(fdbigint1);
      fdbigint2 = fdbigint2.mult(10);
      if (i7 >= 10)
       throw new InternalError("FP assertion: excessivly large digit ".concat(
              String.valueOf(i7)));
      flag = fdbigint.cmp(fdbigint2) < 0;
      flag1 = fdbigint.add(fdbigint2).cmp(fdbigint3) > 0;
      ac[k3++] = (char) (i7 + '0');
     }
     if (flag1 && flag)
     {
      fdbigint.lshiftMe(1);
      l4 = fdbigint.cmp(fdbigint3);
     }
      else l4 = 0L;
    }
   decExponent = j1 + 1;
   digits = ac;
   nDigits = k3;
   if (flag1)
   {
    if (flag)
    {
     if (l4 == 0L)
     {
      if ((ac[nDigits - 1] & 1) != 0)
       roundup();
     }
      else
      {
       if (l4 > 0L)
        roundup();
      }
    }
     else roundup();
   }
  }

  private static NumberFormatException numberFormatExceptionForInputString(
    String s)
  {
   return new NumberFormatException("for input string: \"" + s + "\"");
  }

  String toJavaFormatString()
  {
   char[] ac = new char[nDigits + 10];
   int i = 0;
   if (isNegative)
   {
    ac[0] = '-';
    i = 1;
   }
   if (isExceptional)
   {
    VMSystem.arraycopy(digits, 0, ac, i, nDigits);
    i += nDigits;
   }
    else
    {
     if (decExponent > 0 && decExponent <= 7)
     {
      int j = nDigits;
      if (j >= decExponent)
       j = decExponent;
      VMSystem.arraycopy(digits, 0, ac, i, j);
      i += j;
      if (j < decExponent)
      {
       j = decExponent - j;
       VMSystem.arraycopy(zero, 0, ac, i, j);
       i += j;
       ac[i] = '.';
       ac[i + 1] = '0';
       i += 2;
      }
       else
       {
        ac[i++] = '.';
        if (j < nDigits)
        {
         int l = nDigits - j;
         VMSystem.arraycopy(digits, j, ac, i, l);
         i += l;
        }
         else ac[i++] = '0';
       }
     }
      else
      {
       if (decExponent <= 0 && decExponent >= -2)
       {
        ac[i] = '0';
        ac[i + 1] = '.';
        i += 2;
        if (decExponent != 0)
        {
         VMSystem.arraycopy(zero, 0, ac, i, -decExponent);
         i -= decExponent;
        }
        VMSystem.arraycopy(digits, 0, ac, i, nDigits);
        i += nDigits;
        if (nDigits > 1 && ac[i - 1] == '0')
         i--;
       }
        else
        {
         ac[i] = digits[0];
         ac[i + 1] = '.';
         i += 2;
         if (nDigits > 1)
         {
          VMSystem.arraycopy(digits, 1, ac, i, nDigits - 1);
          i += nDigits - 1;
         }
          else ac[i++] = '0';
         ac[i++] = 'E';
         int k;
         if (decExponent <= 0)
         {
          ac[i++] = '-';
          k = 1 - decExponent;
         }
          else k = decExponent - 1;
         if (k <= 9)
          ac[i++] = (char) (k + '0');
          else
          {
           if (k <= 99)
           {
            ac[i] = (char) (k / 10 + '0');
            ac[i + 1] = (char) (k % 10 + '0');
            i += 2;
           }
            else
            {
             ac[i++] = (char) (k / 100 + '0');
             k %= 100;
             ac[i] = (char) (k / 10 + '0');
             ac[i + 1] = (char) (k % 10 + '0');
             i += 2;
            }
          }
        }
      }
    }
   return new String(ac, 0, i);
  }

  static FloatingDecimal readJavaFormatString(String s)
   throws NumberFormatException
  {
   boolean flag = false;
   boolean flag1 = false;
   try
   {
    s = s.trim();
    int i = s.length();
    if (i == 0)
     throw numberFormatExceptionForInputString(s);
    int j = 0;
    char c;
    if ((c = s.charAt(j)) == '+' || c == '-')
    {
     j++;
     flag1 = true;
     if (c == '-')
      flag = true;
    }
    c = s.charAt(j);
    if (c == 'N' || c == 'I')
    {
     boolean flag2 = false;
     char[] ac1 = null;
     if (c == 'N')
     {
      ac1 = notANumber;
      flag2 = true;
     }
      else ac1 = infinity;
     int l;
     for (l = 0; j < i && l < ac1.length; l++)
      if (s.charAt(j++) != ac1[l])
       break;
     if (l != ac1.length || j != i)
      throw numberFormatExceptionForInputString(s);
     return flag2 ? new FloatingDecimal(Double.NaN) :
             new FloatingDecimal(flag ? -Double.POSITIVE_INFINITY :
             Double.POSITIVE_INFINITY);
    }
    char[] ac = new char[i];
    boolean flag3 = false;
    int i1 = 0;
    int j1 = 0;
    int k1 = 0;
    int k;
    for (k = 0; j < i; j++)
    {
     if ((char) ((c = s.charAt(j)) - '1') <= (char) ('9' - '1'))
     {
      while (k1 > 0)
      {
       ac[k++] = '0';
       k1--;
      }
      ac[k++] = c;
     }
      else
      {
       if (c == '0')
       {
        if (k > 0)
         k1++;
         else j1++;
       }
        else
        {
         if (c != '.')
          break;
         if (flag3)
          throw numberFormatExceptionForInputString(s);
         i1 = j;
         if (flag1)
          i1--;
         flag3 = true;
        }
      }
    }
    if (k == 0)
    {
     ac = zero;
     k = 1;
     if (j1 == 0)
      throw numberFormatExceptionForInputString(s);
    }
    int l1 = flag3 ? i1 - j1 : k + k1;
    if (j < i && ((c = s.charAt(j)) == 'e' || c == 'E'))
    {
     int sign = 1;
     int i2 = 0;
     int j2 = 0xccccccc;
     boolean flag4 = false;
     char c2 = s.charAt(++j);
     if (c2 == '-')
     {
      sign = -1;
      j++;
     }
     if (c2 == '+')
      j++;
     int k2;
     for (k2 = j; j < i; j++)
     {
      if (i2 >= j2)
       flag4 = true;
      char c1;
      if ((char) ((c1 = s.charAt(j)) - '0') > (char) ('9' - '0'))
       break;
      i2 = i2 * 10 + c1 - '0';
     }
     if (j == k2)
      throw numberFormatExceptionForInputString(s);
     int l2 = bigDecimalExponent + k + k1;
     l1 = flag4 || i2 > l2 ? sign * l2 : sign * i2 + l1;
    }
    char c3;
    if (j >= i || (j == i - 1 && ((c3 = s.charAt(j)) == 'f' || c3 == 'F' ||
        c3 == 'd' || c3 == 'D')))
     return new FloatingDecimal(flag, l1, ac, k, false);
   }
   catch (StringIndexOutOfBoundsException e) {}
   throw numberFormatExceptionForInputString(s);
  }

  float floatValue()
  {
   int i = nDigits;
   if (i >= 8)
    i = 8;
   if (digits == infinity)
    return isNegative ? -Float.POSITIVE_INFINITY : Float.POSITIVE_INFINITY;
   if (digits == notANumber)
    return Float.NaN;
   int j = digits[0] - '0';
   for (int k = 1; k < i; k++)
    j = j * 10 + digits[k] - '0';
   float f = j;
   int l = decExponent - i;
   if (nDigits <= singleMaxDecimalDigits)
   {
    if (l == 0 || f == 0.0F)
     return isNegative ? -f : f;
    if (l >= 0)
    {
     if (l <= singleMaxSmallTen)
     {
      f *= singleSmall10pow[l];
      return isNegative ? -f : f;
     }
     int j1 = singleMaxDecimalDigits - i;
     if (l <= singleMaxSmallTen + j1)
     {
      f = (f * singleSmall10pow[j1]) * singleSmall10pow[l - j1];
      return isNegative ? -f : f;
     }
    }
     else
     {
      if (l >= -singleMaxSmallTen)
      {
       f /= singleSmall10pow[-l];
       return isNegative ? -f : f;
      }
     }
   }
    else
    {
     if (decExponent >= nDigits && nDigits + decExponent <= maxDecimalDigits)
     {
      long l1 = (long) j;
      for (int k1 = i; k1 < nDigits; k1++)
       l1 = l1 * 10L + (long) (digits[k1] - '0');
      float f1 = (float) ((double) l1 * small10pow[decExponent - nDigits]);
      return isNegative ? -f1 : f1;
     }
    }
   if (decExponent > singleMaxDecimalExponent + 1)
    return isNegative ? -Float.POSITIVE_INFINITY : Float.POSITIVE_INFINITY;
   if (decExponent < singleMinDecimalExponent - 1)
   {
    float f3 = 0.0F;
    if (isNegative)
     f3 = -f3;
    return f3;
   }
   mustSetRoundDir = true;
   return stickyRound(doubleValue());
  }

  double doubleValue()
  {
   int i = nDigits;
   if (i >= 16)
    i = 16;
   if (digits == infinity)
    return isNegative ? -Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
   if (digits == notANumber)
    return Double.NaN;
   roundDir = 0;
   int j = digits[0] - '0';
   int k = i;
   if (k >= 9)
    k = 9;
   for (int l = 1; l < k; l++)
    j = (j * 10 + digits[l]) - '0';
   long l1 = j;
   for (int i1 = k; i1 < i; i1++)
    l1 = l1 * 10L + (long) (digits[i1] - '0');
   double d = l1;
   int j1 = decExponent - i;
   if (nDigits <= maxDecimalDigits)
   {
    if (j1 == 0 || d == 0.0)
     return isNegative ? -d : d;
    if (j1 >= 0)
    {
     if (j1 <= maxSmallTen)
     {
      double d1 = d * small10pow[j1];
      if (mustSetRoundDir)
      {
       double d4 = d1 / small10pow[j1];
       if (d4 != d)
        roundDir = d4 >= d ? -1 : 1;
      }
      return isNegative ? -d1 : d1;
     }
     int k1 = maxDecimalDigits - i;
     if (j1 <= maxSmallTen + k1)
     {
      d *= small10pow[k1];
      double d4 = d * small10pow[j1 - k1];
      if (mustSetRoundDir)
      {
       double d5 = d4 / small10pow[j1 - k1];
       if (d5 != d)
        roundDir = d5 >= d ? -1 : 1;
      }
      return isNegative ? -d4 : d4;
     }
    }
     else
     {
      if (j1 >= -maxSmallTen)
      {
       double d2 = d / small10pow[-j1];
       double d6;
       if (mustSetRoundDir && (d6 = d2 * small10pow[-j1]) != d)
        roundDir = d6 >= d ? -1 : 1;
       return isNegative ? -d2 : d2;
      }
     }
   }
   if (j1 > 0)
   {
    if (decExponent > maxDecimalExponent + 1)
     return isNegative ? -Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
    if ((j1 & 0xf) != 0)
     d *= small10pow[j1 & 0xf];
    if ((j1 >>= 4) != 0)
    {
     int i2;
     for (i2 = 0; j1 > 1; j1 >>= 1)
     {
      if ((j1 & 1) != 0)
       d *= big10pow[i2];
      i2++;
     }
     double d5 = d * big10pow[i2];
     if (d5 == Double.POSITIVE_INFINITY)
     {
      d5 = (d / 2.0) * big10pow[i2];
      if (d5 == Double.POSITIVE_INFINITY)
       return isNegative ? -d5 : d5;
      d5 = Double.MAX_VALUE;
     }
     d = d5;
    }
   }
    else
    {
     if (j1 < 0)
     {
      j1 = -j1;
      if (decExponent < minDecimalExponent - 1)
      {
       double d3 = 0.0;
       if (isNegative)
        d3 = -d3;
       return d3;
      }
      if ((j1 & 0xf) != 0)
       d /= small10pow[j1 & 0xf];
      if ((j1 >>= 4) != 0)
      {
       int j2;
       for (j2 = 0; j1 > 1; j1 >>= 1)
       {
        if ((j1 & 1) != 0)
         d *= tiny10pow[j2];
        j2++;
       }
       double d6 = d * tiny10pow[j2];
       if (d6 == 0.0)
       {
        d6 = d * 2.0;
        d6 *= tiny10pow[j2];
        if (d6 == 0.0)
        {
         double d8 = 0.0;
         if (isNegative)
          d8 = -d8;
         return d8;
        }
        d6 = Double.MIN_VALUE;
       }
       d = d6;
      }
     }
    }
   FDBigInt fdbigint = new FDBigInt(l1, digits, i, nDigits);
   j1 = decExponent - nDigits;
   int k2 = 0;
   do
   {
    FDBigInt fdbigint1 = doubleToBigInt(d);
    int l2;
    int i3;
    int j3;
    int k3;
    if (j1 >= 0)
    {
     l2 = 0;
     i3 = 0;
     j3 = j1;
     k3 = j1;
    }
     else
     {
      l2 = -j1;
      j3 = 0;
      i3 = l2;
      k3 = 0;
     }
    if (bigIntExp >= 0)
     l2 += bigIntExp;
     else j3 -= bigIntExp;
    int l3 = l2;
    int i4 = bigIntExp + bigIntNBits <= -1022 ?
              bigIntExp + expBias + expShift : 54 - bigIntNBits;
    l2 += i4;
    j3 += i4;
    int j4 = l2;
    if (l2 >= j3)
     j4 = j3;
    if (j4 >= l3)
     j4 = l3;
    l2 -= j4;
    j3 -= j4;
    l3 -= j4;
    fdbigint1 = multPow52(fdbigint1, i3, l2);
    FDBigInt fdbigint2 = multPow52(new FDBigInt(fdbigint), k3, j3);
    FDBigInt fdbigint3;
    int k4;
    boolean flag;
    if ((k4 = fdbigint1.cmp(fdbigint2)) > 0)
    {
     flag = true;
     fdbigint3 = fdbigint1.sub(fdbigint2);
     if (bigIntNBits == 1 && bigIntExp > -expBias && --l3 < 0)
     {
      l3 = 0;
      fdbigint3.lshiftMe(1);
     }
    }
     else
     {
      if (k4 >= 0)
       break;
      flag = false;
      fdbigint3 = fdbigint2.sub(fdbigint1);
     }
    FDBigInt fdbigint4 = constructPow52(i3, l3);
    if ((k4 = fdbigint3.cmp(fdbigint4)) < 0)
    {
     roundDir = flag ? -1 : 1;
     break;
    }
    if (k4 == 0)
    {
     d += ulp(d, flag) / 2.0;
     roundDir = flag ? -1 : 1;
     break;
    }
    d += ulp(d, flag);
   } while (d != 0.0 && d != Double.POSITIVE_INFINITY && ++k2 < 64);
   return isNegative ? -d : d;
  }
 }

 private static final long NAN_BITS;

 static
 {
  long bits = -1L;
  bits >>>= 1; /* hack */
  NAN_BITS = bits - (bits >> 12); /* hack */
 }

 private VMDouble() {}

 static long doubleToLongBits(double value)
 {
  return value != value ? NAN_BITS : doubleToRawLongBits(value);
 }

 static native long doubleToRawLongBits(double value); /* JVM-core */

 static native double longBitsToDouble(long bits); /* JVM-core */

 static String toString(double d, boolean isFloat)
 {
  return (isFloat ? new FloatingDecimal((float) d) :
          new FloatingDecimal(d)).toJavaFormatString();
 }

 static double parseDouble(String str)
  throws NumberFormatException
 {
  if (str == null) /* hack */
   throw new NullPointerException();
  return FloatingDecimal.readJavaFormatString(str).doubleValue();
 }

 static float parseFloat(String str)
  throws NumberFormatException
 { /* used by VM classes only */
  if (str == null) /* hack */
   throw new NullPointerException();
  return FloatingDecimal.readJavaFormatString(str).floatValue();
 }
}

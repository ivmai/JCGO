/*
 * @(#) $(JCGO)/include/jcgovdbl.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
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

#ifdef JCGO_VER

#define JCGO_JDOUBLE_EXPLEN 11

JCGO_NOSEP_STATIC jlong CFASTCALL
java_lang_VMDouble__doubleToRawLongBits__D( jdouble d )
{
 jlong v;
 int expn;
#ifdef JCGO_NOFP
 if (d == (jdouble)0)
  return (jlong)0L;
 v = (jlong)d;
 if (v < (jlong)0L)
  v = -v;
 expn = (1 << (JCGO_JDOUBLE_EXPLEN - 1)) + (int)sizeof(jlong) * 8 - 2;
 while ((v = v + v) > (jlong)0L)
  expn--;
 if (v != (jlong)0L)
 {
  expn--;
  v = v + v;
 }
 if (d < (jdouble)0)
  expn = expn | (1 << JCGO_JDOUBLE_EXPLEN);
 return ((jlong)expn << ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1)) |
         (jlong)(((u_jlong)v) >> (JCGO_JDOUBLE_EXPLEN + 1));
#else
 char bytes[sizeof(jlong) > sizeof(double) ? sizeof(jlong) : sizeof(double)];
 int i = 0;
 int pos = 0;
#ifdef JCGO_REVFLOAT
 char ch;
#endif
 if (sizeof(jlong) != sizeof(double))
 {
  *(volatile int *)&bytes[0] = 1;
  if (sizeof(jlong) > sizeof(double))
  {
   if (bytes[0])
    i = (int)(sizeof(jlong) - sizeof(double));
   *(volatile jlong *)&bytes[0] = (jlong)0L;
  }
   else if (bytes[0])
    pos = (int)(sizeof(double) - sizeof(jlong));
 }
 *(volatile double *)&bytes[i] = (double)d;
#ifdef JCGO_REVFLOAT
 expn = (int)sizeof(double) / 2 - 1;
 do
 {
  ch = bytes[i + expn];
  bytes[i + expn] = bytes[i - expn + ((int)sizeof(double) - 1)];
  bytes[i - expn + ((int)sizeof(double) - 1)] = ch;
 } while (expn-- > 0);
#endif
 if (jcgo_dblWordSwap)
 {
  expn = *(volatile int *)&bytes[i];
  *(volatile int *)&bytes[i] = *((volatile int *)&bytes[i] + 1);
  *((volatile int *)&bytes[i] + 1) = expn;
 }
 if (sizeof(double) * 8 - DBL_MANT_DIG != JCGO_JDOUBLE_EXPLEN)
 {
  expn = (int)(*(volatile jlong *)&bytes[pos] >> (DBL_MANT_DIG -
          (int)(sizeof(double) - sizeof(jlong)) * 8 - 1)) &
          ((1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG)) - 1);
  v = *(volatile jlong *)&bytes[pos] & (((jlong)1L << (DBL_MANT_DIG -
       (int)(sizeof(double) - sizeof(jlong)) * 8 - 1)) - (jlong)1L);
  if ((int)sizeof(double) * 8 - DBL_MANT_DIG < JCGO_JDOUBLE_EXPLEN)
  {
   if (expn)
   {
    v = v >> (JCGO_JDOUBLE_EXPLEN + DBL_MANT_DIG - (int)sizeof(double) * 8);
    if (expn != (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG)) - 1)
     expn = expn + ((1 << (JCGO_JDOUBLE_EXPLEN - 1)) -
             (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)));
     else expn = (1 << JCGO_JDOUBLE_EXPLEN) - 1;
   }
    else if (v != (jlong)0L)
    {
     expn = (1 << (JCGO_JDOUBLE_EXPLEN - 1)) -
             (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1));
     v = v << ((int)sizeof(double) * 8 - DBL_MANT_DIG + 1);
     while (v > (jlong)0L && expn)
     {
      expn--;
      v = v + v;
     }
     if (expn)
      v = v + v;
     v = (jlong)(((u_jlong)v) >> (JCGO_JDOUBLE_EXPLEN + 1));
    }
  }
   else
   {
    v = v << ((int)sizeof(double) * 8 - DBL_MANT_DIG - JCGO_JDOUBLE_EXPLEN);
    if ((unsigned)expn >= (unsigned)((1 << (JCGO_JDOUBLE_EXPLEN - 1)) +
        (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)) - 1))
    {
     if (expn != (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG)) - 1)
      v = (jlong)0L;
     expn = (1 << JCGO_JDOUBLE_EXPLEN) - 1;
    }
     else if ((expn || v != (jlong)0L) &&
              (expn = expn - ((1 << ((int)sizeof(double) * 8 -
              DBL_MANT_DIG - 1)) - (1 << (JCGO_JDOUBLE_EXPLEN - 1)))) <= 0)
     {
      if (expn != -((1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)) -
          (1 << (JCGO_JDOUBLE_EXPLEN - 1))))
       v = (v >> 1) |
            ((jlong)1L << ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 2));
      v = expn > -((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1) ?
           v >> (-expn) : (jlong)0L;
      expn = 0;
     }
   }
  if (*(volatile jlong *)&bytes[pos] < (jlong)0L)
   expn = expn | (1 << JCGO_JDOUBLE_EXPLEN);
  *(volatile jlong *)&bytes[pos] = ((jlong)expn << ((int)sizeof(jlong) * 8 -
                                    JCGO_JDOUBLE_EXPLEN - 1)) | v;
 }
 if (((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN < DBL_MANT_DIG ||
     (int)sizeof(double) * 8 - DBL_MANT_DIG > JCGO_JDOUBLE_EXPLEN) &&
     (*(volatile jlong *)&bytes[pos] & (((jlong)1L <<
     ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1)) - (jlong)1L)) ==
     (jlong)0L && !JCGO_FP_NOTNAN(d))
  (*(volatile jlong *)&bytes[pos])++;
 return *(volatile jlong *)&bytes[pos];
#endif
}

JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMDouble__longBitsToDouble__J( jlong bits )
{
#ifdef JCGO_NOFP
 jlong v = (jlong)0L;
 int expn = (int)(bits >> ((int)sizeof(jlong) * 8 -
             JCGO_JDOUBLE_EXPLEN - 1)) & ((1 << JCGO_JDOUBLE_EXPLEN) - 1);
 if (expn >= (1 << (JCGO_JDOUBLE_EXPLEN - 1)) - 1)
 {
  v = bits & (((jlong)1L << ((int)sizeof(jlong) * 8 -
       JCGO_JDOUBLE_EXPLEN - 1)) - (jlong)1L);
  if (v != (jlong)0L && expn == (1 << JCGO_JDOUBLE_EXPLEN) - 1)
   return (jdouble)0;
  expn = expn - ((1 << (JCGO_JDOUBLE_EXPLEN - 1)) + (int)sizeof(jlong) * 8 -
          JCGO_JDOUBLE_EXPLEN - 2);
  if (expn >= JCGO_JDOUBLE_EXPLEN)
   return bits < (jlong)0L ? (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))) :
           (jdouble)(jlong)(((u_jlong)-1L) >> 1);
  v = v | ((jlong)1L << ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1));
  v = expn < 0 ? v >> (-expn) : v << expn;
  if (bits < (jlong)0L)
   v = -v;
 }
 return (jdouble)v;
#else
 jlong v;
 int expn;
 char bytes[sizeof(jlong) > sizeof(double) ? sizeof(jlong) : sizeof(double)];
 int i = 0;
 int pos = 0;
#ifdef JCGO_REVFLOAT
 char ch;
#endif
 if (sizeof(jlong) != sizeof(double))
 {
  *(volatile int *)&bytes[0] = 1;
  if (sizeof(jlong) < sizeof(double))
  {
   if (bytes[0])
    pos = (int)(sizeof(double) - sizeof(jlong));
   *(volatile double *)&bytes[i] = 0.0;
  }
   else if (bytes[0])
    i = (int)(sizeof(jlong) - sizeof(double));
 }
 if ((int)(sizeof(double) > sizeof(jlong) ? sizeof(double) :
     sizeof(jlong)) * 8 > DBL_MANT_DIG + JCGO_JDOUBLE_EXPLEN &&
     (bits & ((jlong)(((u_jlong)1L << (DBL_MANT_DIG +
     JCGO_JDOUBLE_EXPLEN - (sizeof(double) > sizeof(jlong) ?
     (int)(sizeof(double) - sizeof(jlong)) * 8 : 0) - 1)) -
     (u_jlong)1L) << ((int)(sizeof(double) > sizeof(jlong) ? sizeof(double) :
     sizeof(jlong)) * 8 - DBL_MANT_DIG - JCGO_JDOUBLE_EXPLEN))) ==
     ((jlong)((1 << JCGO_JDOUBLE_EXPLEN) - 1) <<
     ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1)) &&
     (bits & (((jlong)1L << ((int)(sizeof(double) > sizeof(jlong) ?
     sizeof(double) : sizeof(jlong)) * 8 - DBL_MANT_DIG -
     JCGO_JDOUBLE_EXPLEN + 1)) - (jlong)1L)) != (jlong)0L)
  bits = bits | ((jlong)1L << ((int)(sizeof(double) > sizeof(jlong) ?
          sizeof(double) : sizeof(jlong)) * 8 - DBL_MANT_DIG -
          JCGO_JDOUBLE_EXPLEN));
 if (sizeof(double) * 8 - DBL_MANT_DIG != JCGO_JDOUBLE_EXPLEN)
 {
  expn = (int)(bits >> ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1)) &
          ((1 << JCGO_JDOUBLE_EXPLEN) - 1);
  v = bits & (((jlong)1L <<
       ((int)sizeof(jlong) * 8 - JCGO_JDOUBLE_EXPLEN - 1)) - (jlong)1L);
  if ((int)sizeof(double) * 8 - DBL_MANT_DIG > JCGO_JDOUBLE_EXPLEN)
  {
   if (expn)
   {
    v = v >> ((int)sizeof(double) * 8 - DBL_MANT_DIG - JCGO_JDOUBLE_EXPLEN);
    if (expn != (1 << JCGO_JDOUBLE_EXPLEN) - 1)
     expn = expn + ((1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)) -
             (1 << (JCGO_JDOUBLE_EXPLEN - 1)));
     else expn = (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG)) - 1;
   }
    else if (v != (jlong)0L)
    {
     expn = (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)) -
             (1 << (JCGO_JDOUBLE_EXPLEN - 1));
     v = v << (JCGO_JDOUBLE_EXPLEN + 1);
     while (v > (jlong)0L && expn)
     {
      expn--;
      v = v + v;
     }
     if (expn)
      v = v + v;
     v = (jlong)(((u_jlong)v) >> ((int)sizeof(double) * 8 -
          DBL_MANT_DIG + 1));
    }
  }
   else
   {
    v = v << (JCGO_JDOUBLE_EXPLEN + DBL_MANT_DIG - (int)sizeof(double) * 8);
    if ((unsigned)expn >= (unsigned)((1 << (JCGO_JDOUBLE_EXPLEN - 1)) +
        (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)) - 1))
    {
     if (expn != (1 << JCGO_JDOUBLE_EXPLEN) - 1)
      v = (jlong)0L;
     expn = (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG)) - 1;
    }
     else if ((expn || v != (jlong)0L) &&
              (expn = expn - ((1 << (JCGO_JDOUBLE_EXPLEN - 1)) -
              (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1)))) <= 0)
     {
      if (expn != -((1 << (JCGO_JDOUBLE_EXPLEN - 1)) -
          (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1))))
       v = (v >> 1) | ((jlong)1L <<
            (DBL_MANT_DIG - (int)(sizeof(double) - sizeof(jlong)) * 8 - 2));
      v = expn > -(DBL_MANT_DIG - (int)(sizeof(double) -
           sizeof(jlong)) * 8 - 1) ? v >> (-expn) : (jlong)0L;
      expn = 0;
     }
   }
  if (bits < (jlong)0L)
   expn = expn | (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG));
  bits = ((jlong)expn << (DBL_MANT_DIG -
          (int)(sizeof(double) - sizeof(jlong)) * 8 - 1)) | v;
 }
 *(volatile jlong *)&bytes[pos] = bits;
 if (jcgo_dblWordSwap)
 {
  pos = *((volatile int *)&bytes[i] + 1);
  *((volatile int *)&bytes[i] + 1) = *(volatile int *)&bytes[i];
  *(volatile int *)&bytes[i] = pos;
 }
#ifdef JCGO_REVFLOAT
 pos = (int)sizeof(double) / 2 - 1;
 do
 {
  ch = bytes[i + pos];
  bytes[i + pos] = bytes[i - pos + ((int)sizeof(double) - 1)];
  bytes[i - pos + ((int)sizeof(double) - 1)] = ch;
 } while (pos-- > 0);
#endif
 return (jdouble)(*(volatile double *)&bytes[i]);
#endif
}

#endif

/*
 * @(#) $(JCGO)/include/jcgovflt.c --
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

#define JCGO_JFLOAT_EXPLEN 8

JCGO_NOSEP_STATIC jint CFASTCALL
java_lang_VMFloat__floatToRawIntBits__F( jfloat f )
{
 jint v;
 int expn;
#ifdef JCGO_NOFP
 if (f == (jfloat)0)
  return 0;
 v = (jint)f;
 if (v < 0)
  v = -v;
 expn = (1 << (JCGO_JFLOAT_EXPLEN - 1)) + (int)sizeof(jint) * 8 - 2;
 while ((v <<= 1) > 0)
  expn--;
 if (v)
 {
  expn--;
  v <<= 1;
 }
 if (f < (jfloat)0)
  expn = expn | (1 << JCGO_JFLOAT_EXPLEN);
 return ((jint)expn << ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1)) |
         (jint)(((u_jint)v) >> (JCGO_JFLOAT_EXPLEN + 1));
#else
 char bytes[sizeof(jint) > sizeof(float) ? sizeof(jint) : sizeof(float)];
 int i = 0;
 int pos = 0;
#ifdef JCGO_REVFLOAT
 char ch;
#endif
 if (sizeof(jint) != sizeof(float))
 {
  *(volatile int *)&bytes[0] = 1;
  if (sizeof(jint) > sizeof(float))
  {
   if (bytes[0])
    i = (int)(sizeof(jint) - sizeof(float));
   *(volatile jint *)&bytes[0] = 0;
  }
   else if (bytes[0])
    pos = (int)(sizeof(float) - sizeof(jint));
 }
 *(volatile float *)&bytes[i] = (float)f;
#ifdef JCGO_REVFLOAT
 expn = (int)sizeof(float) / 2 - 1;
 do
 {
  ch = bytes[i + expn];
  bytes[i + expn] = bytes[i - expn + ((int)sizeof(float) - 1)];
  bytes[i - expn + ((int)sizeof(float) - 1)] = ch;
 } while (expn-- > 0);
#endif
 if (sizeof(float) * 8 - FLT_MANT_DIG != JCGO_JFLOAT_EXPLEN)
 {
  expn = (int)(*(volatile jint *)&bytes[pos] >> (FLT_MANT_DIG -
          (int)(sizeof(float) - sizeof(jint)) * 8 - 1)) &
          ((1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG)) - 1);
  v = *(volatile jint *)&bytes[pos] & (((jint)1 << (FLT_MANT_DIG -
       (int)(sizeof(float) - sizeof(jint)) * 8 - 1)) - (jint)1);
  if ((int)sizeof(float) * 8 - FLT_MANT_DIG < JCGO_JFLOAT_EXPLEN)
  {
   if (expn)
   {
    v = v >> (JCGO_JFLOAT_EXPLEN + FLT_MANT_DIG - (int)sizeof(float) * 8);
    if (expn != (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG)) - 1)
     expn = expn + ((1 << (JCGO_JFLOAT_EXPLEN - 1)) -
             (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)));
     else expn = (1 << JCGO_JFLOAT_EXPLEN) - 1;
   }
    else if (v)
    {
     expn = (1 << (JCGO_JFLOAT_EXPLEN - 1)) -
             (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1));
     v <<= (int)sizeof(float) * 8 - FLT_MANT_DIG + 1;
     while (v > 0 && expn)
     {
      expn--;
      v <<= 1;
     }
     if (expn)
      v <<= 1;
     v = (jint)(((u_jint)v) >> (JCGO_JFLOAT_EXPLEN + 1));
    }
  }
   else
   {
    v <<= (int)sizeof(float) * 8 - FLT_MANT_DIG - JCGO_JFLOAT_EXPLEN;
    if ((unsigned)expn >= (unsigned)((1 << (JCGO_JFLOAT_EXPLEN - 1)) +
        (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)) - 1))
    {
     if (expn != (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG)) - 1)
      v = 0;
     expn = (1 << JCGO_JFLOAT_EXPLEN) - 1;
    }
     else if ((expn || v) && (expn = expn - ((1 << ((int)sizeof(float) * 8 -
              FLT_MANT_DIG - 1)) - (1 << (JCGO_JFLOAT_EXPLEN - 1)))) <= 0)
     {
      if (expn != -((1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)) -
          (1 << (JCGO_JFLOAT_EXPLEN - 1))))
       v = (v >> 1) |
            ((jint)1 << ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 2));
      v = expn > -((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1) ?
           v >> (-expn) : 0;
      expn = 0;
     }
   }
  if (*(volatile jint *)&bytes[pos] < 0)
   expn = expn | (1 << JCGO_JFLOAT_EXPLEN);
  *(volatile jint *)&bytes[pos] = ((jint)expn << ((int)sizeof(jint) * 8 -
                                   JCGO_JFLOAT_EXPLEN - 1)) | v;
 }
 if (((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN < FLT_MANT_DIG ||
     (int)sizeof(float) * 8 - FLT_MANT_DIG > JCGO_JFLOAT_EXPLEN) &&
     (*(volatile jint *)&bytes[pos] & (((jint)1 << ((int)sizeof(jint) * 8 -
     JCGO_JFLOAT_EXPLEN - 1)) - (jint)1)) == 0 && !JCGO_FP_NOTNANF(f))
  (*(volatile jint *)&bytes[pos])++;
 return *(volatile jint *)&bytes[pos];
#endif
}

JCGO_NOSEP_STATIC jfloat CFASTCALL
java_lang_VMFloat__intBitsToFloat__I( jint bits )
{
#ifdef JCGO_NOFP
 jint v = 0;
 int expn = (int)(bits >> ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1)) &
             ((1 << JCGO_JFLOAT_EXPLEN) - 1);
 if (expn >= (1 << (JCGO_JFLOAT_EXPLEN - 1)) - 1)
 {
  v = bits & (((jint)1 << ((int)sizeof(jint) * 8 -
       JCGO_JFLOAT_EXPLEN - 1)) - (jint)1);
  if (v != 0 && expn == (1 << JCGO_JFLOAT_EXPLEN) - 1)
   return (jfloat)0;
  expn = expn - ((1 << (JCGO_JFLOAT_EXPLEN - 1)) + (int)sizeof(jint) * 8 -
          JCGO_JFLOAT_EXPLEN - 2);
  if (expn >= JCGO_JFLOAT_EXPLEN)
   return bits < 0 ? (jfloat)((jint)(~(((u_jint)-1) >> 1))) :
           (jfloat)(((u_jint)-1) >> 1);
  v = v | ((jint)1 << ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1));
  v = expn < 0 ? v >> (-expn) : v << expn;
  if (bits < 0)
   v = -v;
 }
 return (jfloat)v;
#else
 jint v;
 int expn;
 char bytes[sizeof(jint) > sizeof(float) ? sizeof(jint) : sizeof(float)];
 int i = 0;
 int pos = 0;
#ifdef JCGO_REVFLOAT
 char ch;
#endif
 if (sizeof(jint) != sizeof(float))
 {
  *(volatile int *)&bytes[0] = 1;
  if (sizeof(jint) < sizeof(float))
  {
   if (bytes[0])
    pos = (int)(sizeof(float) - sizeof(jint));
   *(volatile float *)&bytes[i] = (float)0.0;
  }
   else if (bytes[0])
    i = (int)(sizeof(jint) - sizeof(float));
 }
 if ((int)(sizeof(float) > sizeof(jint) ? sizeof(float) :
     sizeof(jint)) * 8 > FLT_MANT_DIG + JCGO_JFLOAT_EXPLEN &&
     (bits & ((jint)(((u_jint)1 << (FLT_MANT_DIG + JCGO_JFLOAT_EXPLEN -
     (sizeof(float) > sizeof(jint) ?
     (int)(sizeof(float) - sizeof(jint)) * 8 : 0) - 1)) -
     (u_jint)1) << ((int)(sizeof(float) > sizeof(jint) ? sizeof(float) :
     sizeof(jint)) * 8 - FLT_MANT_DIG - JCGO_JFLOAT_EXPLEN))) ==
     ((jint)((1 << JCGO_JFLOAT_EXPLEN) - 1) <<
     ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1)) &&
     (bits & (((jint)1 << ((int)(sizeof(float) > sizeof(jint) ?
     sizeof(float) : sizeof(jint)) * 8 - FLT_MANT_DIG -
     JCGO_JFLOAT_EXPLEN + 1)) - (jint)1)) != 0)
  bits = bits | ((jint)1 << ((int)(sizeof(float) > sizeof(jint) ?
          sizeof(float) : sizeof(jint)) * 8 - FLT_MANT_DIG -
          JCGO_JFLOAT_EXPLEN));
 if (sizeof(float) * 8 - FLT_MANT_DIG != JCGO_JFLOAT_EXPLEN)
 {
  expn = (int)(bits >> ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1)) &
          ((1 << JCGO_JFLOAT_EXPLEN) - 1);
  v = bits & (((jint)1 << ((int)sizeof(jint) * 8 - JCGO_JFLOAT_EXPLEN - 1)) -
       (jint)1);
  if ((int)sizeof(float) * 8 - FLT_MANT_DIG > JCGO_JFLOAT_EXPLEN)
  {
   if (expn)
   {
    v = v >> ((int)sizeof(float) * 8 - FLT_MANT_DIG - JCGO_JFLOAT_EXPLEN);
    if (expn != (1 << JCGO_JFLOAT_EXPLEN) - 1)
     expn = expn + ((1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)) -
             (1 << (JCGO_JFLOAT_EXPLEN - 1)));
     else expn = (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG)) - 1;
   }
    else if (v)
    {
     expn = (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)) -
             (1 << (JCGO_JFLOAT_EXPLEN - 1));
     v <<= JCGO_JFLOAT_EXPLEN + 1;
     while (v > 0 && expn)
     {
      expn--;
      v <<= 1;
     }
     if (expn)
      v <<= 1;
     v = (jint)(((u_jint)v) >> ((int)sizeof(float) * 8 - FLT_MANT_DIG + 1));
    }
  }
   else
   {
    v <<= JCGO_JFLOAT_EXPLEN + FLT_MANT_DIG - (int)sizeof(float) * 8;
    if ((unsigned)expn >= (unsigned)((1 << (JCGO_JFLOAT_EXPLEN - 1)) +
        (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)) - 1))
    {
     if (expn != (1 << JCGO_JFLOAT_EXPLEN) - 1)
      v = 0;
     expn = (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG)) - 1;
    }
     else if ((expn || v) && (expn = expn - ((1 << (JCGO_JFLOAT_EXPLEN - 1)) -
              (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1)))) <= 0)
     {
      if (expn != -((1 << (JCGO_JFLOAT_EXPLEN - 1)) -
          (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG - 1))))
       v = (v >> 1) | ((jint)1 << (FLT_MANT_DIG -
            (int)(sizeof(float) - sizeof(jint)) * 8 - 2));
      v = expn > -(FLT_MANT_DIG - (int)(sizeof(float) -
           sizeof(jint)) * 8 - 1) ? v >> (-expn) : 0;
      expn = 0;
     }
   }
  if (bits < 0)
   expn = expn | (1 << ((int)sizeof(float) * 8 - FLT_MANT_DIG));
  bits = ((jint)expn << (FLT_MANT_DIG -
          (int)(sizeof(float) - sizeof(jint)) * 8 - 1)) | v;
 }
 *(volatile jint *)&bytes[pos] = bits;
#ifdef JCGO_REVFLOAT
 pos = (int)sizeof(float) / 2 - 1;
 do
 {
  ch = bytes[i + pos];
  bytes[i + pos] = bytes[i - pos + ((int)sizeof(float) - 1)];
  bytes[i - pos + ((int)sizeof(float) - 1)] = ch;
 } while (pos-- > 0);
#endif
 return (jfloat)(*(volatile float *)&bytes[i]);
#endif
}

#endif

/*
 * @(#) $(JCGO)/include/jcgomath.c --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef JCGO_NOFP
#include "jcgomath.h"
#endif

#ifdef JCGO_NATMATH_sin__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__sin__D( jdouble a )
{
#ifdef JCGO_NOFP
 return (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return sinl(a);
#else
 return sin(a);
#endif
#else
 jdouble v;
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  {
   v = sinl(a);
   if (JCGO_FP_NOTNAN(v) &&
       JCGO_EXPECT_TRUE(v >= (jdouble)-1.0 && v <= (jdouble)1.0))
    return v;
   a = sinl(fmodl(a, (jdouble)2.0 * M_PIl));
  }
#else
  if (JCGO_EXPECT_TRUE(a <= -DBL_MIN || a >= DBL_MIN))
  {
   v = sin(a);
   if (JCGO_FP_NOTNAN(v) && JCGO_EXPECT_TRUE(v >= -1.0 && v <= 1.0))
    return v;
   a = sin(fmod(a, 2.0 * M_PI));
  }
#endif
  return a;
 }
 return jcgo_fpNaN;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_cos__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__cos__D( jdouble a )
{
#ifdef JCGO_NOFP
 return a == (jdouble)0 ? (jdouble)1 : (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return cosl(a);
#else
 return cos(a);
#endif
#else
 jdouble v;
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  v = cosl(a);
  if (JCGO_FP_NOTNAN(v) &&
      JCGO_EXPECT_TRUE(v >= (jdouble)-1.0 && v <= (jdouble)1.0))
   return v;
  return cosl(fmodl(a, (jdouble)2.0 * M_PIl));
#else
  v = cos(a);
  if (JCGO_FP_NOTNAN(v) && JCGO_EXPECT_TRUE(v >= -1.0 && v <= 1.0))
   return v;
  return cos(fmod(a, 2.0 * M_PI));
#endif
 }
 return jcgo_fpNaN;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_tan__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__tan__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble vf;
 jdouble dd;
 jdouble df;
 jdouble c;
 jdouble p;
 int sign;
 int j;
 int n;
 int k = 0;
 v = (jdouble)0;
 if (a != (jdouble)0)
 {
  sign = 1;
  if (a < (jdouble)0)
  {
   sign = 0;
   a = -a;
  }
  j = (int)sizeof(jlong) * 8;
  c = (jdouble)((((u_jlong)0xC90FDAA2L) << ((int)sizeof(jlong) * 8 - 32)) |
       (j > 32 ? ((u_jlong)0x2168C235L) >> ((32 << 1) - j) : (u_jlong)0L));
  n = j;
  do
  {
   dd = a;
   k = k << 1;
   a = a + a;
   if (dd < (jdouble)0 || (u_jlong)a >= (u_jlong)c)
   {
    k++;
    a = a - c;
   }
  } while (--n > 0);
  if ((((k & 0x3) - 1) >> 1) == 0)
  {
   v = (jdouble)1;
   if ((k & 0x3) == 0x1)
   {
    a = c - a;
    sign = 1 - sign;
   }
   if (a >= (jdouble)0)
   {
    v = a;
    if (a != (jdouble)0)
    {
     dd = a;
     k = 0;
     for (;;)
     {
      df = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) - (jdouble)1;
      vf = (jdouble)((jlong)v & (jlong)df);
      df = (jdouble)((jlong)dd & (jlong)df);
      v = (jdouble)(((u_jlong)v) >> ((int)sizeof(jlong) * 4));
      p = (jdouble)(((u_jlong)dd) >> ((int)sizeof(jlong) * 4));
      vf = (jdouble)(((u_jlong)(vf * df)) >> ((int)sizeof(jlong) * 4)) +
            vf * p;
      df = v * df;
      vf = df + vf;
      v = (jdouble)(((u_jlong)vf) >> ((int)sizeof(jlong) * 4)) + v * p;
      if ((u_jlong)vf < (u_jlong)df)
       v = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) + v;
      if (c == (jdouble)0)
       break;
      switch (n)
      {
      case 0:
       p = (jdouble)((jlong)0x1B2A7L);
       c = (jdouble)((u_jlong)0x74BF7ADL);
       break;
      case 1:
       p = (jdouble)((jlong)0xFFFEC8A3L);
       c = (jdouble)((u_jlong)0x4249FACAL);
       break;
      case 2:
       p = (jdouble)((jlong)0x4AE03L);
       c = (jdouble)((u_jlong)0xCCBC29FAL);
       break;
      case 3:
       p = (jdouble)((jlong)0x51FA2L);
       c = (jdouble)((u_jlong)0x280DE4A9L);
       break;
      case 4:
       p = (jdouble)((jlong)0x1026F7L);
       c = (jdouble)((u_jlong)0x1A8D1068L);
       break;
      case 5:
       p = (jdouble)((jlong)0x2689B1L);
       c = (jdouble)((u_jlong)0xE5E4CA02L);
       break;
      case 6:
       p = (jdouble)((jlong)0x5F6F23L);
       c = (jdouble)((u_jlong)0xFB820C54L);
       break;
      case 7:
       p = (jdouble)((jlong)0xEB6916L);
       c = (jdouble)((u_jlong)0x4AB01940L);
       break;
      case 8:
       p = (jdouble)((jlong)0x244DC7DL);
       c = (jdouble)((u_jlong)0x2DD09260L);
       break;
      case 9:
       p = (jdouble)((jlong)0x5993D21L);
       c = (jdouble)((u_jlong)0x1B58DC0L);
       break;
      case 10:
       p = (jdouble)((jlong)0xDD0DD0DL);
       c = (jdouble)((u_jlong)0xD9A0FF00L);
       break;
      case 11:
       p = (jdouble)((jlong)0x22222222L);
       c = (jdouble)((u_jlong)0x21FCF400L);
       break;
      case 12:
       p = (jdouble)((jlong)0x55555555L);
       c = (jdouble)((u_jlong)0x55558C00L);
       break;
      default:
       c = (jdouble)0;
       break;
      }
      if (c != (jdouble)0)
      {
       c = (jdouble)(((u_jlong)p << ((int)sizeof(jlong) * 8 - 32)) |
            (j > 32 ? ((u_jlong)c) >> ((32 << 1) - j) : (u_jlong)0L));
       if (n == 0)
       {
        dd = v;
        v = (jdouble)0;
       }
       if (k != 0)
        c = -c;
       v = v + c;
       if (c < (jdouble)0 && (u_jlong)v >= (u_jlong)c)
       {
        v = -v;
        k = 1 - k;
       }
      }
       else dd = a;
      n++;
     }
     if (k != 0)
      v = -v;
     v = a + v;
    }
    if ((u_jlong)v <= (u_jlong)1L)
     return sign != 0 ? (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))) :
             (jdouble)(jlong)(((u_jlong)-1L) >> 1);
    v = (jdouble)((u_jlong)(-v) / (u_jlong)v) + (jdouble)1;
   }
   if (sign != 0)
    v = -v;
  }
 }
 return v;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return tanl(a);
#else
 return tan(a);
#endif
#else
 jdouble v;
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  {
   v = tanl(a);
   if (JCGO_FP_FINITE(v))
    return v;
   a = tanl(fmodl(a, M_PIl));
  }
#else
  if (JCGO_EXPECT_TRUE(a <= -DBL_MIN || a >= DBL_MIN))
  {
   v = tan(a);
   if (JCGO_FP_FINITE(v))
    return v;
   a = tan(fmod(a, M_PI));
  }
#endif
  return a;
 }
 return jcgo_fpNaN;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_asin__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__asin__D( jdouble a )
{
#ifdef JCGO_NOFP
 return a == (jdouble)-1 || a == (jdouble)1 ? a : (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return asinl(a);
#else
 return asin(a);
#endif
#else
 if (JCGO_FP_FINITE(a) &&
     JCGO_EXPECT_TRUE(a >= (jdouble)-1.0 && a <= (jdouble)1.0))
 {
#ifdef JCGO_LONGDBL
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
   a = asinl(a);
#else
  if (JCGO_EXPECT_TRUE(a <= -DBL_MIN || a >= DBL_MIN))
   a = asin(a);
#endif
  return a;
 }
 return jcgo_fpNaN;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_acos__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__acos__D( jdouble a )
{
#ifdef JCGO_NOFP
 return a == (jdouble)0 ? (jdouble)1 : a == (jdouble)-1 ? (jdouble)3 :
         (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return acosl(a);
#else
 return acos(a);
#endif
#else
 if (JCGO_FP_FINITE(a) &&
     JCGO_EXPECT_TRUE(a >= (jdouble)-1.0 && a <= (jdouble)1.0))
 {
#ifdef JCGO_LONGDBL
  return acosl(a);
#else
  return acos(a);
#endif
 }
 return jcgo_fpNaN;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_atan__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__atan__D( jdouble a )
{
#ifdef JCGO_NOFP
 return a >= (jdouble)2 ? (jdouble)1 : a <= (jdouble)-2 ? (jdouble)-1 :
         (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return atanl(a);
#else
 return atan(a);
#endif
#else
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
   a = atanl(a);
#else
  if (JCGO_EXPECT_TRUE(a <= -DBL_MIN || a >= DBL_MIN))
   a = atan(a);
#endif
  return a;
 }
 if (JCGO_FP_NOTNAN(a))
 {
#ifdef JCGO_LONGDBL
  a = a < (jdouble)0.0 ? -M_PIl / (jdouble)2.0 : M_PIl / (jdouble)2.0;
#else
  a = a < 0.0 ? -M_PI / 2.0 : M_PI / 2.0;
#endif
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_atan2__DD
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__atan2__DD( jdouble y, jdouble x )
{
#ifdef JCGO_NOFP
 jdouble v;
 if (y == (jdouble)0)
  return x < (jdouble)0 ? (jdouble)3 : (jdouble)0;
 if (x == (jdouble)0)
  return y < (jdouble)0 ? (jdouble)-1 : (jdouble)1;
 v = y;
 if (y < (jdouble)0)
  y = -y;
 if (x < (jdouble)0)
 {
  x = -x;
  y = (u_jlong)y > (u_jlong)x && (jdouble)((((u_jlong)x & (((u_jlong)1L <<
       ((int)sizeof(jlong) * 4)) - (u_jlong)1L)) * (((u_jlong)0x2F5EC5C1L) >>
       (32 - (int)sizeof(jlong) * 4))) >> ((int)sizeof(jlong) * 4)) +
       (jdouble)(((u_jlong)x) >> ((int)sizeof(jlong) * 4)) *
       (jdouble)(((u_jlong)0x2F5EC5C1L) >> (32 - (int)sizeof(jlong) * 4)) <
       (y - x) - x ? (jdouble)1 : ((((u_jlong)x & (((u_jlong)1L <<
       ((int)sizeof(jlong) * 4)) - (u_jlong)1L)) * (((u_jlong)0x247DEE24L) >>
       (32 - (int)sizeof(jlong) * 4))) >> ((int)sizeof(jlong) * 4)) +
       (((u_jlong)x) >> ((int)sizeof(jlong) * 4)) * (((u_jlong)0x247DEE24L) >>
       (32 - (int)sizeof(jlong) * 4)) < (u_jlong)y ? (jdouble)2 : (jdouble)3;
 }
  else y = (jdouble)(((((u_jlong)x & (((u_jlong)1L <<
            ((int)sizeof(jlong) * 4)) - (u_jlong)1L)) *
            (((u_jlong)0x8EB245CBL) >> (32 - (int)sizeof(jlong) * 4))) >>
            ((int)sizeof(jlong) * 4)) + (((u_jlong)x) >>
            ((int)sizeof(jlong) * 4)) * (((u_jlong)0x8EB245CBL) >>
            (32 - (int)sizeof(jlong) * 4))) < y - x ? (jdouble)1 : (jdouble)0;
 if (v < (jdouble)0)
  y = -y;
 return y;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return atan2l(y, x);
#else
 return atan2(y, x);
#endif
#else
 jdouble v;
 if (JCGO_FP_FINITE(y))
 {
  if (JCGO_FP_FINITE(x))
  {
   if (JCGO_EXPECT_TRUE(y != (jdouble)0.0))
   {
#ifdef JCGO_LONGDBL
    if (JCGO_EXPECT_TRUE(x != (jdouble)0.0))
    {
     v = y;
     if (y < (jdouble)0.0)
      y = -y;
     y = atan2l(y, x);
     if (v < (jdouble)0.0)
      y = -y;
     return y;
    }
    return y < (jdouble)0.0 ? -M_PIl / (jdouble)2.0 : M_PIl / (jdouble)2.0;
#else
    if (JCGO_EXPECT_TRUE(x != 0.0))
    {
     if (JCGO_EXPECT_FALSE((y > -DBL_MIN && y < DBL_MIN) ||
         (x > -DBL_MIN && x < DBL_MIN)))
     {
      v = y * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
           (1L << (DBL_MANT_DIG / 2)));
      if (JCGO_FP_FINITE(v))
       y = v;
      v = x * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
           (1L << (DBL_MANT_DIG / 2)));
      if (JCGO_FP_FINITE(v))
       x = v;
     }
     return y < 0.0 ? -atan2(-y, x) : atan2(y, x);
    }
    return y < 0.0 ? -M_PI / 2.0 : M_PI / 2.0;
#endif
   }
   if (x <= (jdouble)0.0 && (x != (jdouble)0.0 || JCGO_FP_ZEROISNEG(x)))
   {
#ifdef JCGO_LONGDBL
    x = M_PIl;
#else
    x = M_PI;
#endif
    if (JCGO_FP_ZEROISNEG(y))
     x = -x;
    return x;
   }
   return y;
  }
  if (JCGO_FP_NOTNAN(x))
  {
   if (y != (jdouble)0.0)
   {
#ifdef JCGO_LONGDBL
    x = x < (jdouble)0.0 ? M_PIl : (jdouble)0.0;
#else
    x = x < 0.0 ? M_PI : 0.0;
#endif
    if (y < (jdouble)0.0)
     x = -x;
    return x;
   }
   if (x < (jdouble)0.0)
   {
#ifdef JCGO_LONGDBL
    x = M_PIl;
#else
    x = M_PI;
#endif
    if (JCGO_FP_ZEROISNEG(y))
     x = -x;
    return x;
   }
   return y;
  }
  return x;
 }
 if (JCGO_FP_NOTNAN(y))
 {
  if (JCGO_FP_FINITE(x))
  {
#ifdef JCGO_LONGDBL
   return y < (jdouble)0.0 ? -M_PIl / (jdouble)2.0 : M_PIl / (jdouble)2.0;
#else
   return y < 0.0 ? -M_PI / 2.0 : M_PI / 2.0;
#endif
  }
  if (JCGO_FP_NOTNAN(x))
  {
#ifdef JCGO_LONGDBL
   x = x < (jdouble)0.0 ? M_PIl - M_PIl / (jdouble)4.0 :
        M_PIl / (jdouble)4.0;
#else
   x = x < 0.0 ? M_PI - M_PI / 4.0 : M_PI / 4.0;
#endif
   if (y < (jdouble)0.0)
    x = -x;
  }
  return x;
 }
 return y;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_exp__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__exp__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble vf;
 jdouble m;
 jdouble mf;
 jdouble p;
 int j = 0;
 if (a <= (jdouble)0)
  return a == (jdouble)0 ? (jdouble)1 : (jdouble)0;
 if (a > (jdouble)(((u_jint)0x2C5L *
     (u_jint)((int)sizeof(jlong) * 8 - 1)) >> 10))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 v = (jdouble)1;
 m = (jdouble)1;
 vf = (jdouble)0;
 mf = (jdouble)0;
 for (;;)
 {
  m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 8 - 12)) + a * m;
  if (++j <= 0)
   break;
  p = (jdouble)((u_jlong)m / (u_jlong)j);
  mf = (jdouble)((((u_jlong)(m - p * j) << ((int)sizeof(jlong) * 8 - 12)) +
        ((u_jlong)mf & (((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)) -
        (u_jlong)1L))) / (u_jlong)j);
  m = p;
  vf = vf + mf;
  v = v + m;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
  {
   v++;
   vf = vf - (jdouble)((jlong)1L << ((int)sizeof(jlong) * 8 - 12));
  }
   else if (m == (jdouble)0 && (((u_jlong)mf) >> 1) + (u_jlong)vf <=
            ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
    break;
  mf = a * mf;
 }
 m = (jdouble)(((u_jlong)v) >> ((int)sizeof(jlong) * 8 - 11));
 if (m > (jdouble)0)
 {
  j = 0;
  do
  {
   j++;
  } while ((m = (jdouble)((jlong)m >> 1)) > (jdouble)0);
  m = (jdouble)((jlong)1L << (j - 1));
  p = (m + m) + m;
  if ((jdouble)((u_jlong)v & (u_jlong)p) == p)
   v = m + v;
  v = (jdouble)((u_jlong)(-(m + m)) & (u_jlong)v);
 }
 return v;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return expl(a);
#else
 return exp(a);
#endif
#else
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  return JCGO_EXPECT_TRUE(a < (jdouble)(LN_MAXLDOUBLE - 0.1)) ? expl(a) :
          a < (jdouble)(LN_MAXLDOUBLE + 0.9) ? expl(a - (jdouble)1.0) * M_El :
          jcgo_fpInf;
#else
  return JCGO_EXPECT_TRUE(a < LN_MAXDOUBLE - 0.1) ?
          (JCGO_EXPECT_FALSE(a > -LN_MINDOUBLE - 0.1 &&
          a < LN_MINDOUBLE + 0.1) ? (a >= 0.0 ? exp(a +
          DBL_MANT_DIG * M_LN2) / ((double)(1L << (DBL_MANT_DIG / 2)) *
          (1L << ((DBL_MANT_DIG + 1) / 2))) : exp(a - DBL_MANT_DIG * M_LN2) *
          ((double)(1L << (DBL_MANT_DIG / 2)) *
          (1L << ((DBL_MANT_DIG + 1) / 2)))) : exp(a)) :
          a < LN_MAXDOUBLE + 0.9 ? exp(a - 1.0) * M_E : jcgo_fpInf;
#endif
 }
 if (JCGO_FP_NOTNAN(a) && a < (jdouble)0.0)
  a = (jdouble)0.0;
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_log__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__log__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble m;
 jdouble p;
 jdouble r;
 jdouble mf;
 jdouble df;
 int j;
 int k = 0;
 if (a <= (jdouble)0)
  return a < (jdouble)0 ? (jdouble)0 :
          (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)));
 if (a > (jdouble)1)
 {
  v = a;
  k = (int)sizeof(jlong) * 8 - 2;
  while ((a = a + a) >= (jdouble)0)
   k--;
  if (k >= (int)sizeof(jlong) * 8 - 11)
  {
   a = (jdouble)(((jlong)v - (jlong)1L) >>
        (k - ((int)sizeof(jlong) * 8 - 11)));
   if (((int)a & 0x3) != 0x3)
    a = a + (jdouble)2;
   a = (jdouble)(((jlong)a >> 1) << 11);
  }
  j = (int)sizeof(jlong) * 8;
  r = (jdouble)((((u_jlong)0xB17217F7L) << ((int)sizeof(jlong) * 8 - 32)) |
       (j > 32 ? ((u_jlong)0xD1CF79ABL) >> ((32 << 1) - j) : (u_jlong)0L)) *
       (jdouble)k;
  a = a + a;
  k = (int)(((u_jint)k * (u_jint)0x2C5L) >> 10);
  j = 1;
  v = a;
  m = a;
  for (;;)
  {
   df = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) - (jdouble)1;
   mf = (jdouble)((jlong)m & (jlong)df);
   df = (jdouble)((jlong)a & (jlong)df);
   m = (jdouble)(((u_jlong)m) >> ((int)sizeof(jlong) * 4));
   p = (jdouble)(((u_jlong)a) >> ((int)sizeof(jlong) * 4));
   mf = (jdouble)(((u_jlong)(mf * df)) >> ((int)sizeof(jlong) * 4)) + mf * p;
   df = m * df;
   mf = df + mf;
   m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 4)) + m * p;
   if ((u_jlong)mf < (u_jlong)df)
    m = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) + m;
   if (++j <= 0)
    break;
   p = (jdouble)((u_jlong)m / (u_jlong)j);
   if ((j & 1) == 0)
   {
    v = v - p;
    if ((u_jlong)(v + r) < (u_jlong)v)
    {
     k++;
     break;
    }
   }
    else if ((u_jlong)((v = v + p) + r) >= (u_jlong)r)
     break;
  }
 }
 return (jdouble)k;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return logl(a);
#else
 return log(a);
#endif
#else
 if (a < (jdouble)-0.0)
  return jcgo_fpNaN;
 if (JCGO_FP_FINITE(a))
 {
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  {
#ifdef JCGO_LONGDBL
   a = logl(a);
#else
   a = JCGO_EXPECT_FALSE(a < DBL_MIN) ?
        log(a * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
        (1L << (DBL_MANT_DIG / 2)))) - (DBL_MANT_DIG - 1) * M_LN2 : log(a);
#endif
  }
   else a = -jcgo_fpInf;
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_sqrt__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__sqrt__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble r;
 jdouble t;
 jdouble q = (jdouble)0;
 jdouble s = (jdouble)0;
 int k;
 int carry;
 if (a <= (jdouble)0)
  return (jdouble)0;
 r = a;
 for (k = 0; r > (jdouble)3; k++)
  r = (jdouble)((jlong)r >> 2);
 carry = 0;
 r = (jdouble)((jlong)1L << (k << 1));
 do
 {
  t = s + r;
  if (carry || (u_jlong)t <= (u_jlong)a)
  {
   q = q + r;
   a = a - t;
   if (a == (jdouble)0)
    break;
   carry = 0;
   s = t + r;
  }
  if (a < (jdouble)0)
   carry = 1;
  a = a + a;
 } while ((r = (jdouble)((jlong)r >> 1)) > (jdouble)0);
 return (jdouble)((jlong)q >> k);
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return sqrtl(a);
#else
 return sqrt(a);
#endif
#else
 if (a < (jdouble)-0.0)
  return jcgo_fpNaN;
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)0.0))
 {
#ifdef JCGO_LONGDBL
  a = sqrtl(a);
#else
  a = JCGO_EXPECT_FALSE(a < DBL_MIN) ?
       sqrt(a * ((double)(1L << (DBL_MANT_DIG / 2)) *
       (1L << (DBL_MANT_DIG / 2)))) / (double)(1L << (DBL_MANT_DIG / 2)) :
       sqrt(a);
#endif
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_pow__DD
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__pow__DD( jdouble a, jdouble b )
{
#ifdef JCGO_NOFP
 jdouble v;
 int sign = 0;
 if (b <= (jdouble)0)
 {
  if (b == (jdouble)0)
   return (jdouble)1;
  if (a == (jdouble)0)
   return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 }
 if (a < (jdouble)0)
 {
  a = -a;
  if (((int)b & 1) != 0)
   sign = 1;
 }
 v = a;
 if ((u_jlong)a > (u_jlong)1L)
 {
  if (b < (jdouble)0)
   return (jdouble)0;
  if (b >= (jdouble)(sizeof(jlong) * 8 - 1))
   return sign ? (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))) :
           (jdouble)(jlong)(((u_jlong)-1L) >> 1);
  if (a != (jdouble)2)
  {
   if (((int)b & 1) == 0)
    v = (jdouble)1;
   while ((b = (jdouble)(((unsigned)b) >> 1)) > (jdouble)0)
   {
    a = a * a;
    if (((int)b & 1) != 0)
     v = v * a;
   }
  }
   else v = (jdouble)((jlong)1L << (int)b);
 }
 if (sign)
  v = -v;
 return v;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return powl(a, b);
#else
 return pow(a, b);
#endif
#else
 jdouble v;
 jdouble d;
 if (JCGO_FP_FINITE(b))
 {
  if (JCGO_EXPECT_FALSE(b == (jdouble)0.0))
   return (jdouble)1.0;
  if (JCGO_EXPECT_FALSE(b == (jdouble)1.0))
   return a;
  if (JCGO_FP_FINITE(a))
  {
   if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
   {
    v = (jdouble)0.0;
    if (a < (jdouble)0.0)
    {
     v = b - JCGO_FP_FLOOR(b / (jdouble)2.0) * (jdouble)2.0;
     if (JCGO_EXPECT_FALSE(v != (jdouble)0.0 && v != (jdouble)1.0))
      return jcgo_fpNaN;
     a = -a;
    }
    if (JCGO_EXPECT_TRUE(a != (jdouble)1.0))
    {
#ifdef JCGO_LONGDBL
     a = (a > (jdouble)1.0 && b < (jdouble)0.0) ||
          (a < (jdouble)1.0 && b > (jdouble)0.0) ||
          (d = logl(a) * b) <= (jdouble)(LN_MAXLDOUBLE - 0.1) ?
          powl(a, b) : d < (jdouble)(LN_MAXLDOUBLE + 0.9) ?
          expl(d - (jdouble)1.0) * M_El : jcgo_fpInf;
#else
     d = (JCGO_EXPECT_FALSE(a < DBL_MIN) ?
          log(a * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
          (1L << (DBL_MANT_DIG / 2)))) - (DBL_MANT_DIG - 1) * M_LN2 :
          log(a)) * b;
     a = JCGO_EXPECT_TRUE(d < LN_MAXDOUBLE - 0.1) ?
          (JCGO_EXPECT_FALSE(d > -LN_MINDOUBLE - 0.1 &&
          d < LN_MINDOUBLE + 0.1) ? (d >= 0.0 ?
          exp(d + DBL_MANT_DIG * M_LN2) /
          ((double)(1L << (DBL_MANT_DIG / 2)) *
          (1L << ((DBL_MANT_DIG + 1) / 2))) : exp(d - DBL_MANT_DIG * M_LN2) *
          ((double)(1L << (DBL_MANT_DIG / 2)) *
          (1L << ((DBL_MANT_DIG + 1) / 2)))) :
          JCGO_EXPECT_FALSE(a < DBL_MIN) ? exp(d) : pow(a, b)) :
          d < LN_MAXDOUBLE + 0.9 ? exp(d - 1.0) * M_E : jcgo_fpInf;
#endif
    }
    if (v != (jdouble)0.0)
     a = -a;
    return a;
   }
   if (JCGO_FP_ZEROISNEG(a))
    a = b - JCGO_FP_FLOOR(b / (jdouble)2.0) * (jdouble)2.0;
   b = b < (jdouble)0.0 ? jcgo_fpInf : (jdouble)0.0;
   if (a == (jdouble)1.0)
    b = -b;
   return b;
  }
  if (JCGO_FP_NOTNAN(a))
  {
   if (a < (jdouble)0.0)
   {
    a = -a;
    if (b < (jdouble)0.0)
     a = (jdouble)0.0;
    if (b - JCGO_FP_FLOOR(b / (jdouble)2.0) * (jdouble)2.0 == (jdouble)1.0)
     a = -a;
   }
    else if (b < (jdouble)0.0)
     a = (jdouble)0.0;
  }
  return a;
 }
 if (JCGO_FP_NOTNAN(b))
 {
  if (JCGO_FP_FINITE(a))
  {
   if (a < (jdouble)0.0)
    a = -a;
   if (a != (jdouble)1.0)
   {
    if (a < (jdouble)1.0)
     b = -b;
    if (b < (jdouble)0.0)
     b = (jdouble)0.0;
    return b;
   }
   return jcgo_fpNaN;
  }
  if (JCGO_FP_NOTNAN(a))
  {
   if (a < (jdouble)0.0)
    a = -a;
   if (b < (jdouble)0.0)
    a = (jdouble)0.0;
  }
  return a;
 }
 return b;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_IEEEremainder__DD
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__IEEEremainder__DD( jdouble x, jdouble y )
{
 jdouble v;
 jdouble d;
#ifdef JCGO_NOFP
 if (y == (jdouble)0)
  return (jdouble)0;
 d = x;
 if (x < (jdouble)0)
  x = -x;
 if (y < (jdouble)0)
  y = -y;
 if ((((u_jlong)x) >> 1) >= (u_jlong)y)
  x = (jdouble)((u_jlong)x % (u_jlong)(y + y));
 v = x - y;
 if (v >= (jdouble)0)
 {
  y = y - v;
  x = v;
  if ((u_jlong)y <= (u_jlong)v)
   x = -y;
 }
  else if (-x < v)
   x = v;
 if (d < (jdouble)0)
  x = -x;
 return x;
#else
 if (JCGO_EXPECT_TRUE(y != (jdouble)0.0) && JCGO_FP_FINITE(x))
 {
  if (JCGO_FP_FINITE(y))
  {
   d = x;
   if (x < (jdouble)0.0)
    x = -x;
   if (y < (jdouble)0.0)
    y = -y;
   if (x / (jdouble)2.0 >= y)
   {
#ifdef JCGO_LONGDBL
    x = fmodl(x, y * (jdouble)2.0);
#else
    x = JCGO_EXPECT_FALSE(x < DBL_MIN) ||
         (JCGO_EXPECT_FALSE(y <= DBL_MIN / 2.0) && x <= 1.0) ?
         fmod(x * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
         (1L << (DBL_MANT_DIG / 2))),
         y * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
         (1L << (DBL_MANT_DIG / 2)) * 2.0)) /
         ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
         (1L << (DBL_MANT_DIG / 2))) : fmod(x, y * 2.0);
#endif
   }
   v = x - y;
   if (v >= (jdouble)0.0)
   {
    y = y - v;
    x = v;
    if (y <= v)
     x = -y;
   }
    else if (-v < x)
     x = v;
   if (d < (jdouble)0.0)
    x = -x;
   return x;
  }
  if (JCGO_FP_NOTNAN(y))
   return x;
 }
 return jcgo_fpNaN;
#endif
}
#endif

#ifdef JCGO_NATMATH_ceil__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__ceil__D( jdouble a )
{
#ifndef JCGO_NOFP
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  a = -JCGO_FP_FLOOR(-a);
#endif
 return a;
}
#endif

#ifdef JCGO_NATMATH_floor__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__floor__D( jdouble a )
{
#ifndef JCGO_NOFP
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  a = JCGO_FP_FLOOR(a);
#endif
 return a;
}
#endif

#ifdef JCGO_NATMATH_rint__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__rint__D( jdouble a )
{
#ifndef JCGO_NOFP
 jdouble v;
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)0.0))
 {
  v = a;
  a = JCGO_FP_FLOOR(((jdouble)0.5 + a) / (jdouble)2.0) * (jdouble)2.0;
  if (v - a > (jdouble)0.5)
   a = a + (jdouble)1.0;
   else if (JCGO_EXPECT_FALSE(v < (jdouble)0.0 && v >= (jdouble)-0.5))
    a = -a;
 }
#endif
 return a;
}
#endif

#ifdef JCGO_NATMATH_cbrt__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__cbrt__D( jdouble a )
{
 int sign;
#ifdef JCGO_NOFP
 jdouble v;
 jdouble s;
 jdouble t;
 int j;
 if (a == (jdouble)0)
  return (jdouble)0;
 sign = 0;
 if (a < (jdouble)0)
 {
  a = -a;
  sign = 1;
 }
 v = (jdouble)((jlong)a >> ((int)sizeof(jlong) * 8 - 12));
 if (v > (jdouble)0)
 {
  j = 0;
  do
  {
   j++;
  } while ((v = (jdouble)((jlong)v >> 1)) > (jdouble)0);
  a = (jdouble)((u_jlong)(((jlong)1L << j) - (jlong)1L) | (u_jlong)a) +
       (jdouble)1;
 }
 s = a;
 v = (jdouble)1;
 while ((u_jlong)s >= (u_jlong)8L)
 {
  v = v + v;
  s = (jdouble)(((u_jlong)s) >> 3);
 }
 if (v > (jdouble)1)
 {
  t = v;
  for (;;)
  {
   s = v * v;
   if (s == (jdouble)0)
    break;
   s = ((jdouble)((u_jlong)a / (u_jlong)s) + (v + v)) / (jdouble)3;
   if (s == t || s == v)
    break;
   t = v;
   v = s;
  }
  if (v >= s)
   v = s;
 }
 return sign ? -v : v;
#else
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)-0.0))
 {
  sign = 0;
  if (a < (jdouble)0.0)
  {
   sign = 1;
   a = -a;
  }
#ifdef JCGO_MATHEXT
#ifdef JCGO_LONGDBL
  a = cbrtl(a);
#else
  a = JCGO_EXPECT_FALSE(a < DBL_MIN) ?
       cbrt(a * ((double)(1L << ((DBL_MANT_DIG + 1) / 3)) *
       (double)(1L << ((DBL_MANT_DIG + 1) / 3)) *
       (1L << ((DBL_MANT_DIG + 1) / 3)))) /
       (double)(1L << ((DBL_MANT_DIG + 1) / 3)) : cbrt(a);
#endif
#else
#ifdef JCGO_LONGDBL
  a = powl(a, (jdouble)1.0 / (jdouble)3.0);
#else
  a = JCGO_EXPECT_FALSE(a < DBL_MIN) ?
       pow(a * ((double)(1L << ((DBL_MANT_DIG + 1) / 3)) *
       (double)(1L << ((DBL_MANT_DIG + 1) / 3)) *
       (1L << ((DBL_MANT_DIG + 1) / 3))), 1.0 / 3.0) /
       (double)(1L << ((DBL_MANT_DIG + 1) / 3)) : pow(a, 1.0 / 3.0);
#endif
#endif
  if (sign)
   a = -a;
 }
 return a;
#endif
}
#endif

#ifdef JCGO_NATMATH_hypot__DD
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__hypot__DD( jdouble a, jdouble b )
{
#ifdef JCGO_NOFP
 jdouble r;
 jdouble ql;
 jdouble qh;
 jdouble sl;
 jdouble sh;
 jdouble vl;
 jdouble vh;
 int k;
 int isbig;
 if (a == (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))) ||
     b == (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 if (a < (jdouble)0)
  a = -a;
 if (b < (jdouble)0)
  b = -b;
 ql = a;
 if (b != (jdouble)0)
 {
  ql = b;
  if (a != (jdouble)0)
  {
   ql = (jdouble)((u_jlong)1L << ((int)sizeof(jlong) * 4)) - (jdouble)1;
   vl = (jdouble)((jlong)a & (jlong)ql);
   ql = (jdouble)((jlong)b & (jlong)ql);
   vh = (jdouble)(((u_jlong)a) >> ((int)sizeof(jlong) * 4));
   qh = (jdouble)(((u_jlong)b) >> ((int)sizeof(jlong) * 4));
   vh = (jdouble)(((((u_jlong)vl * (u_jlong)vl) >>
         ((int)sizeof(jlong) * 4 + 1)) +
         (u_jlong)vl * (u_jlong)vh) >> ((int)sizeof(jlong) * 4 - 1)) +
         vh * vh + qh * qh + (jdouble)(((((u_jlong)ql * (u_jlong)ql) >>
         ((int)sizeof(jlong) * 4 + 1)) +
         (u_jlong)ql * (u_jlong)qh) >> ((int)sizeof(jlong) * 4 - 1));
   ql = (jdouble)((u_jlong)b * (u_jlong)b);
   vl = (jdouble)((u_jlong)a * (u_jlong)a) + ql;
   if ((u_jlong)vl < (u_jlong)ql)
    vh++;
   if ((u_jlong)vh > (((u_jlong)-1L) >> 2))
    return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
   isbig = 0;
   r = vl;
   if (vh != (jdouble)0)
   {
    r = vh;
    isbig = 1;
   }
   for (k = 0; r >= (jdouble)3; k++)
    r = (jdouble)((jlong)r >> 2);
   if (r < (jdouble)0)
   {
    k = (int)sizeof(jlong) * 4 - 1;
    if (r >= (jdouble)((jlong)(~(((u_jlong)-1L) >> 2))))
    {
     k = 0;
     isbig = 1;
    }
   }
   ql = (jdouble)0;
   qh = (jdouble)0;
   sl = (jdouble)0;
   sh = (jdouble)0;
   r = (jdouble)((jlong)1L << (k << 1));
   if (isbig)
    k = k + (int)sizeof(jlong) * 4;
   for (;;)
   {
    if (isbig)
    {
     a = sl;
     b = sh + r;
    }
     else
     {
      a = sl + r;
      b = sh;
      if ((u_jlong)a < (u_jlong)r)
       b++;
     }
    if ((u_jlong)b < (u_jlong)vh ||
        ((u_jlong)b == (u_jlong)vh && (u_jlong)a <= (u_jlong)vl))
    {
     if (isbig)
      qh = qh + r;
      else
      {
       ql = ql + r;
       if ((u_jlong)ql < (u_jlong)r)
        qh++;
      }
     vh = vh - b;
     if ((u_jlong)vl < (u_jlong)a)
      vh--;
     vl = vl - a;
     if (vl == (jdouble)0 && vh == (jdouble)0)
      break;
     if (isbig)
     {
      sl = a;
      sh = b + r;
     }
      else
      {
       sl = a + r;
       sh = b;
       if ((u_jlong)sl < (u_jlong)r)
        sh++;
      }
    }
    vh = vh + vh;
    if (vl < (jdouble)0)
     vh++;
    vl = vl + vl;
    r = (jdouble)(((u_jlong)r) >> 1);
    if (r == (jdouble)0)
    {
     if (!isbig)
      break;
     r = (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)));
     isbig = 0;
    }
   }
   if (k > 0)
    ql = (jdouble)((jlong)qh << ((int)sizeof(jlong) * 8 - k)) +
          (jdouble)(((u_jlong)ql) >> k);
  }
 }
 r = (jdouble)(((u_jlong)ql) >> ((int)sizeof(jlong) * 8 - 11));
 if (r > (jdouble)0)
 {
  k = 0;
  do
  {
   k++;
  } while ((r = (jdouble)((jlong)r >> 1)) > (jdouble)0);
  r = (jdouble)((jlong)1L << (k - 1));
  b = r + r;
  if ((jdouble)((u_jlong)((b + b - (jdouble)1) & (u_jlong)ql)) > r)
   ql = r + ql;
  ql = (jdouble)((u_jlong)(-b) & (u_jlong)ql);
  if (ql < (jdouble)0)
   ql = (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 }
 return ql;
#else
 jdouble v;
 if (a < (jdouble)0.0)
  a = -a;
 if (JCGO_FP_FINITE(a))
 {
  if (b < (jdouble)0.0)
   b = -b;
  v = b;
  if (JCGO_FP_FINITE(b))
  {
   if (a < b)
   {
    b = a;
    a = v;
   }
   if (a <= (jdouble)0.0)
    return (jdouble)0.0;
#ifdef JCGO_FP_HYPOT
#ifdef JCGO_LONGDBL
   v = a * M_SQRT2l;
#else
   v = a * M_SQRT2;
#endif
   if (JCGO_EXPECT_TRUE(a != b))
   {
    if (JCGO_FP_FINITE(v))
    {
     v = JCGO_FP_HYPOT(a, b);
     if (JCGO_FP_FINITE(v) && JCGO_EXPECT_TRUE(v != (jdouble)0.0))
      return v;
    }
    v = JCGO_FP_HYPOT(b / a, (jdouble)1.0) * a;
   }
#else
   v = b / a;
   v = v * v + (jdouble)1.0;
   if (JCGO_FP_FINITE(v))
   {
#ifdef JCGO_LONGDBL
    v = sqrtl(v) * a;
#else
    v = sqrt(v) * a;
#endif
   }
#endif
  }
  return v;
 }
 if (JCGO_FP_NOTNAN(b))
  return a;
 return b;
#endif
}
#endif

#ifdef JCGO_NATMATH_expm1__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__expm1__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble vf;
 jdouble m;
 jdouble mf;
 jdouble p;
 int j = 0;
 if (a <= (jdouble)0)
  return a == (jdouble)0 ? (jdouble)0 : (jdouble)-1;
 if (a > (jdouble)(((u_jint)0x2C5L *
     (u_jint)((int)sizeof(jlong) * 8 - 1)) >> 10))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 v = (jdouble)1;
 m = (jdouble)1;
 vf = (jdouble)0;
 mf = (jdouble)0;
 for (;;)
 {
  m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 8 - 12)) + a * m;
  if (++j <= 0)
   break;
  p = (jdouble)((u_jlong)m / (u_jlong)j);
  mf = (jdouble)((((u_jlong)(m - p * j) << ((int)sizeof(jlong) * 8 - 12)) +
        ((u_jlong)mf & (((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)) -
        (u_jlong)1L))) / (u_jlong)j);
  m = p;
  vf = vf + mf;
  v = v + m;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
  {
   v++;
   vf = vf - (jdouble)((jlong)1L << ((int)sizeof(jlong) * 8 - 12));
  }
   else if (m == (jdouble)0 && (((u_jlong)mf) >> 1) + (u_jlong)vf <=
            ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
    break;
  mf = a * mf;
 }
 m = (jdouble)(((u_jlong)v) >> ((int)sizeof(jlong) * 8 - 11));
 if (m > (jdouble)0)
 {
  j = 0;
  do
  {
   j++;
  } while ((m = (jdouble)((jlong)m >> 1)) > (jdouble)0);
  m = (jdouble)((jlong)1L << (j - 1));
  p = (m + m) + m;
  if ((jdouble)((u_jlong)v & (u_jlong)p) == p)
   v = m + v;
  v = (jdouble)((u_jlong)(-(m + m)) & (u_jlong)v);
 }
 return v - (jdouble)1;
#else
 jdouble v;
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_MATHEXT
#ifdef JCGO_LONGDBL
  v = JCGO_EXPECT_TRUE(a < (jdouble)(LN_MAXLDOUBLE - 0.1)) ?
       (JCGO_EXPECT_TRUE(a != (jdouble)-0.0) ? expm1l(a) : a) :
       a < (jdouble)(LN_MAXLDOUBLE + 0.9) ?
       expm1l(a - (jdouble)1.0) * M_El : jcgo_fpInf;
#else
  if (JCGO_EXPECT_TRUE(a < LN_MAXDOUBLE - 0.1))
  {
   v = expm1(a);
   if (JCGO_EXPECT_FALSE(v >= -0.0 && v <= 0.0))
    v = a;
  }
   else v = a < LN_MAXDOUBLE + 0.9 ? expm1(a - 1.0) * M_E : jcgo_fpInf;
#endif
  return v;
#else
#ifdef JCGO_LONGDBL
  v = JCGO_EXPECT_TRUE(a < (jdouble)(LN_MAXLDOUBLE - 0.1)) ? expl(a) :
       a < (jdouble)(LN_MAXLDOUBLE + 0.9) ? expl(a - (jdouble)1.0) * M_El :
       jcgo_fpInf;
#else
  v = JCGO_EXPECT_TRUE(a < LN_MAXDOUBLE - 0.1) ?
       (JCGO_EXPECT_FALSE(a > -LN_MINDOUBLE - 0.1 && a < LN_MINDOUBLE + 0.1) ?
       (a >= 0.0 ? exp(a + DBL_MANT_DIG * M_LN2) /
       ((double)(1L << (DBL_MANT_DIG / 2)) *
       (1L << ((DBL_MANT_DIG + 1) / 2))) : exp(a - DBL_MANT_DIG * M_LN2) *
       ((double)(1L << (DBL_MANT_DIG / 2)) *
       (1L << ((DBL_MANT_DIG + 1) / 2)))) : exp(a)) :
       a < LN_MAXDOUBLE + 0.9 ? exp(a - 1.0) * M_E : jcgo_fpInf;
#endif
  return JCGO_EXPECT_TRUE(v != (jdouble)1.0) ? v - (jdouble)1.0 : a;
#endif
 }
 if (JCGO_FP_NOTNAN(a) && a < (jdouble)0.0)
  a = (jdouble)-1.0;
 return a;
#endif
}
#endif

#ifdef JCGO_NATMATH_log10__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__log10__D( jdouble a )
{
#ifdef JCGO_NOFP
 int k;
 if (a == (jdouble)0)
  return (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)));
 for (k = 0; a >= (jdouble)10; k++)
  a = a / (jdouble)10;
 return (jdouble)k;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return log10l(a);
#else
 return log10(a);
#endif
#else
 if (a < (jdouble)-0.0)
  return jcgo_fpNaN;
 if (JCGO_FP_FINITE(a))
 {
  if (JCGO_EXPECT_TRUE(a != (jdouble)0.0))
  {
#ifdef JCGO_LONGDBL
   a = log10l(a);
#else
   a = JCGO_EXPECT_FALSE(a < DBL_MIN) ?
        log10(a * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
        (1L << (DBL_MANT_DIG / 2)))) - (DBL_MANT_DIG - 1) * M_LN2 / M_LN10 :
        log10(a);
#endif
  }
   else a = -jcgo_fpInf;
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_log1p__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__log1p__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble m;
 jdouble p;
 jdouble r;
 jdouble mf;
 jdouble df;
 int j;
 int k = 0;
 if (a == (jdouble)-1)
  return (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)));
 if (a > (jdouble)0)
 {
  if ((jlong)a != (jlong)(((u_jlong)-1L) >> 1))
   a++;
  v = a;
  k = (int)sizeof(jlong) * 8 - 2;
  while ((a = a + a) >= (jdouble)0)
   k--;
  if (k >= (int)sizeof(jlong) * 8 - 11)
  {
   a = (jdouble)(((jlong)v - (jlong)1L) >>
        (k - ((int)sizeof(jlong) * 8 - 11)));
   if (((int)a & 0x3) != 0x3)
    a = a + (jdouble)2;
   a = (jdouble)(((jlong)a >> 1) << 11);
  }
  j = (int)sizeof(jlong) * 8;
  r = (jdouble)((((u_jlong)0xB17217F7L) << ((int)sizeof(jlong) * 8 - 32)) |
       (j > 32 ? ((u_jlong)0xD1CF79ABL) >> ((32 << 1) - j) : (u_jlong)0L)) *
       (jdouble)k;
  a = a + a;
  k = (int)(((u_jint)k * (u_jint)0x2C5L) >> 10);
  j = 1;
  v = a;
  m = a;
  for (;;)
  {
   df = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) - (jdouble)1;
   mf = (jdouble)((jlong)m & (jlong)df);
   df = (jdouble)((jlong)a & (jlong)df);
   m = (jdouble)(((u_jlong)m) >> ((int)sizeof(jlong) * 4));
   p = (jdouble)(((u_jlong)a) >> ((int)sizeof(jlong) * 4));
   mf = (jdouble)(((u_jlong)(mf * df)) >> ((int)sizeof(jlong) * 4)) + mf * p;
   df = m * df;
   mf = df + mf;
   m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 4)) + m * p;
   if ((u_jlong)mf < (u_jlong)df)
    m = (jdouble)((jlong)1L << ((int)sizeof(jlong) * 4)) + m;
   if (++j <= 0)
    break;
   p = (jdouble)((u_jlong)m / (u_jlong)j);
   if ((j & 1) == 0)
   {
    v = v - p;
    if ((u_jlong)(v + r) < (u_jlong)v)
    {
     k++;
     break;
    }
   }
    else if ((u_jlong)((v = v + p) + r) >= (u_jlong)r)
     break;
  }
 }
 return (jdouble)k;
#else
#ifndef JCGO_MATHEXT
 jdouble v;
#endif
 if (a < (jdouble)-1.0)
  return jcgo_fpNaN;
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)-0.0))
 {
  if (JCGO_EXPECT_TRUE(a != (jdouble)-1.0))
  {
#ifdef JCGO_MATHEXT
#ifdef JCGO_LONGDBL
   a = log1pl(a);
#else
   if (JCGO_EXPECT_TRUE(a <= -DBL_MIN || a >= DBL_MIN))
    a = log1p(a);
#endif
#else
   v = a + (jdouble)1.0;
   if (JCGO_EXPECT_TRUE(v != (jdouble)1.0))
   {
#ifdef JCGO_LONGDBL
    a = a / (v - (jdouble)1.0) * logl(v);
#else
    a = a / (v - 1.0) * log(v);
#endif
   }
#endif
  }
   else a = -jcgo_fpInf;
 }
 return a;
#endif
}
#endif

#ifdef JCGO_NATMATH_sinh__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__sinh__D( jdouble a )
{
#ifdef JCGO_NOFP
 int sign;
 jdouble v;
 jdouble vf;
 jdouble m;
 jdouble mf;
 jdouble p;
 int j = 0;
 if (a == (jdouble)0)
  return (jdouble)0;
 if (a > (jdouble)(((u_jint)0x2C5L *
     (u_jint)((int)sizeof(jlong) * 8)) >> 10))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 if (a < -(jdouble)(((u_jint)0x2C5L *
     (u_jint)((int)sizeof(jlong) * 8)) >> 10))
  return (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)));
 sign = 0;
 if (a < (jdouble)0)
 {
  sign = 1;
  a = -a;
 }
 vf = (jdouble)((u_jlong)1L << ((int)sizeof(jlong) * 8 - 13));
 v = (jdouble)0;
 mf = vf;
 m = (jdouble)0;
 for (;;)
 {
  mf = a * mf;
  m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 8 - 12)) + a * m;
  if (++j <= 0)
   break;
  p = (jdouble)((u_jlong)m / (u_jlong)j);
  mf = (jdouble)((((u_jlong)(m - p * j) << ((int)sizeof(jlong) * 8 - 12)) +
        ((u_jlong)mf & (((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)) -
        (u_jlong)1L))) / (u_jlong)j);
  m = p;
  vf = vf + mf;
  v = v + m;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
  {
   v++;
   vf = vf - (jdouble)((jlong)1L << ((int)sizeof(jlong) * 8 - 12));
  }
   else if (m == (jdouble)0 && (u_jlong)mf + (u_jlong)vf <=
            ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
    break;
 }
 m = (jdouble)(((u_jlong)v) >> ((int)sizeof(jlong) * 8 - 12));
 if (m > (jdouble)0)
 {
  j = 0;
  do
  {
   j++;
  } while ((m = (jdouble)((jlong)m >> 1)) > (jdouble)0);
  m = (jdouble)((jlong)1L << (j - 1));
  p = (m + m) + m;
  v = v + v;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 13)))
   v++;
  if ((jdouble)((u_jlong)v & (u_jlong)p) == p)
   v = m + v;
  v = (jdouble)(((u_jlong)(-(m + m)) & (u_jlong)v) >> 1);
 }
 return sign ? -v : v;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return sinhl(a);
#else
 return sinh(a);
#endif
#else
 int sign;
 if (JCGO_FP_FINITE(a) && JCGO_EXPECT_TRUE(a != (jdouble)-0.0))
 {
  sign = 0;
  if (a < (jdouble)0.0)
  {
   sign = 1;
   a = -a;
  }
#ifdef JCGO_LONGDBL
  a = JCGO_EXPECT_TRUE(a < (jdouble)(LN_MAXLDOUBLE + M_LN2l - 0.1)) ?
       sinhl(a) : a < (jdouble)(LN_MAXLDOUBLE + M_LN2l + 0.9) ?
       sinhl(a - (jdouble)1.0) * M_El : jcgo_fpInf;
#else
  a = JCGO_EXPECT_TRUE(a < LN_MAXDOUBLE + M_LN2 - 0.1) ? sinh(a) :
       a < LN_MAXDOUBLE + M_LN2 + 0.9 ? sinh(a - 1.0) * M_E : jcgo_fpInf;
#endif
  if (sign)
   a = -a;
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_cosh__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__cosh__D( jdouble a )
{
#ifdef JCGO_NOFP
 jdouble v;
 jdouble vf;
 jdouble m;
 jdouble mf;
 jdouble p;
 int j = 0;
 if (a == (jdouble)0)
  return (jdouble)1;
 if (a < (jdouble)0)
  a = -a;
 if ((u_jlong)a > (u_jlong)(((u_jint)0x2C5L *
     (u_jint)((int)sizeof(jlong) * 8)) >> 10))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 vf = (jdouble)((u_jlong)1L << ((int)sizeof(jlong) * 8 - 13));
 v = (jdouble)0;
 mf = vf;
 m = (jdouble)0;
 for (;;)
 {
  mf = a * mf;
  m = (jdouble)(((u_jlong)mf) >> ((int)sizeof(jlong) * 8 - 12)) + a * m;
  if (++j <= 0)
   break;
  p = (jdouble)((u_jlong)m / (u_jlong)j);
  mf = (jdouble)((((u_jlong)(m - p * j) << ((int)sizeof(jlong) * 8 - 12)) +
        ((u_jlong)mf & (((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)) -
        (u_jlong)1L))) / (u_jlong)j);
  m = p;
  vf = vf + mf;
  v = v + m;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
  {
   v++;
   vf = vf - (jdouble)((jlong)1L << ((int)sizeof(jlong) * 8 - 12));
  }
   else if (m == (jdouble)0 && (u_jlong)mf + (u_jlong)vf <=
            ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 12)))
    break;
 }
 m = (jdouble)(((u_jlong)v) >> ((int)sizeof(jlong) * 8 - 12));
 if (m > (jdouble)0)
 {
  j = 0;
  do
  {
   j++;
  } while ((m = (jdouble)((jlong)m >> 1)) > (jdouble)0);
  m = (jdouble)((jlong)1L << (j - 1));
  p = (m + m) + m;
  v = v + v;
  if ((u_jlong)vf >= ((u_jlong)1L << ((int)sizeof(jlong) * 8 - 13)))
   v++;
  if ((jdouble)((u_jlong)v & (u_jlong)p) == p)
   v = m + v;
  v = (jdouble)(((u_jlong)(-(m + m)) & (u_jlong)v) >> 1);
 }
 return v;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return coshl(a);
#else
 return cosh(a);
#endif
#else
 if (a < (jdouble)0.0)
  a = -a;
 if (JCGO_FP_FINITE(a))
 {
#ifdef JCGO_LONGDBL
  a = JCGO_EXPECT_TRUE(a < (jdouble)(LN_MAXLDOUBLE + M_LN2l - 0.1)) ?
       coshl(a) : a < (jdouble)(LN_MAXLDOUBLE + M_LN2l + 0.9) ?
       coshl(a - (jdouble)1.0) * M_El : jcgo_fpInf;
#else
  a = JCGO_EXPECT_TRUE(a < LN_MAXDOUBLE + M_LN2 - 0.1) ? cosh(a) :
       a < LN_MAXDOUBLE + M_LN2 + 0.9 ? cosh(a - 1.0) * M_E : jcgo_fpInf;
#endif
 }
 return a;
#endif
#endif
}
#endif

#ifdef JCGO_NATMATH_tanh__D
JCGO_NOSEP_STATIC jdouble CFASTCALL
java_lang_VMMath__tanh__D( jdouble a )
{
#ifdef JCGO_NOFP
 return a >= (jdouble)20 ? (jdouble)1 : a <= (jdouble)-20 ?
         (jdouble)-1 : (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return tanhl(a);
#else
 return tanh(a);
#endif
#else
 if (JCGO_FP_FINITE(a))
 {
  if (JCGO_EXPECT_TRUE(a != (jdouble)-0.0))
  {
#ifdef JCGO_LONGDBL
   a = tanhl(a);
#else
   a = tanh(a);
#endif
  }
  return a;
 }
 if (JCGO_FP_NOTNAN(a))
  a = a > (jdouble)0.0 ? (jdouble)1.0 : (jdouble)-1.0;
 return a;
#endif
#endif
}
#endif

#endif

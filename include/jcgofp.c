/*
 * @(#) $(JCGO)/include/jcgofp.c --
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
 * Used control macros: FPINIT, JCGO_FASTMATH, JCGO_FPFAST, JCGO_LONGDBL,
 * JCGO_MATHEXT, JCGO_NOFP, JCGO_REVFLOAT.
 * Macros for tuning: FPINIT.
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

STATICDATA jdouble jcgo_fpInf;

STATICDATA jdouble jcgo_fpNaN;

#ifdef JCGO_SEPARATED

jdouble jcgo_fpZero;

jfloat jcgo_fpZeroF;

#endif /* JCGO_SEPARATED */

#ifndef jcgo_dblWordSwap
#ifdef JCGO_NATCLASS_java_lang_VMDouble
STATICDATA int jcgo_dblWordSwap = 0;
#else
#define jcgo_dblWordSwap 0
#endif
#endif

#endif /* ! JCGO_NOFP */

JCGO_NOSEP_INLINE void JCGO_INLFRW_FASTCALL jcgo_initFP( void )
{
#ifdef FPINIT
 FPINIT;
#endif
#ifndef JCGO_NOFP
 jcgo_fpZero = (jdouble)0.0;
 jcgo_fpZeroF = (jfloat)0.0;
#ifdef INFINITY
 jcgo_fpInf = (jdouble)INFINITY;
#else
#ifdef HUGE_VAL
 jcgo_fpInf = (jdouble)HUGE_VAL;
#else
 jcgo_fpInf = (jdouble)1.0 / *(volatile jdouble *)&jcgo_fpZero;
#endif
#endif
#ifdef NAN
 jcgo_fpNaN = (jdouble)NAN;
#else
 jcgo_fpNaN = *(volatile jdouble *)&jcgo_fpInf *
               *(volatile jdouble *)&jcgo_fpZero;
#endif
#ifndef jcgo_dblWordSwap
 if ((sizeof(int) << 1) <= sizeof(double))
 {
  double minusOne = -1.0;
  if ((*((volatile signed char *)&minusOne + ((sizeof(double) >> 1) - 1)) |
      *((volatile signed char *)&minusOne + (sizeof(double) >> 1))) < 0)
   jcgo_dblWordSwap = 1;
 }
#endif
#endif
}

#ifdef JCGO_NOFP

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_fdiv( jdouble d1, jdouble d2 )
{
 if (JCGO_EXPECT_FALSE(d2 == (jdouble)0))
  return d1 < (jdouble)0 ? (jdouble)((jlong)(~(((u_jlong)-1L) >> 1))) :
          d1 > (jdouble)0 ? (jdouble)(jlong)(((u_jlong)-1L) >> 1) : d2;
 if (JCGO_EXPECT_FALSE(d2 == (jdouble)-1) &&
     JCGO_EXPECT_FALSE(d1 == (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)))))
  return (jdouble)(jlong)(((u_jlong)-1L) >> 1);
 return d1 / d2;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_fdivf( jfloat f1, jfloat f2 )
{
 if (JCGO_EXPECT_FALSE(f2 == (jfloat)0))
  return f1 < (jfloat)0 ? (jfloat)((jint)(~(((u_jint)-1) >> 1))) :
          f1 > (jfloat)0 ? (jfloat)(((u_jint)-1) >> 1) : f2;
 if (JCGO_EXPECT_FALSE(f2 == (jfloat)-1) &&
     JCGO_EXPECT_FALSE(f1 == (jfloat)((jint)(~(((u_jint)-1) >> 1)))))
  return (jfloat)(((u_jint)-1) >> 1);
 return f1 / f2;
}

#else /* JCGO_NOFP */

JCGO_NOSEP_EXTRASTATIC jint CFASTCALL jcgo_jfloat2jint( jfloat f )
{
 if (JCGO_FP_NOTNANF(f))
  return f < (jfloat)0.0 ?
          (JCGO_EXPECT_FALSE(f <= (jfloat)((jint)(~(((u_jint)-1) >> 1)))) ?
          (jint)(~(((u_jint)-1) >> 1)) : (jint)(-JCGO_FP_FLOORF(-f))) :
          JCGO_EXPECT_TRUE(f < -(jfloat)(-(jint)(((u_jint)-1) >> 1))) ?
          (jint)JCGO_FP_FLOORF(f) : (jint)(((u_jint)-1) >> 1);
 return 0;
}

JCGO_NOSEP_EXTRASTATIC jlong CFASTCALL jcgo_jfloat2jlong( jfloat f )
{
 if (JCGO_FP_NOTNANF(f))
  return f < (jfloat)0.0 ?
          (JCGO_EXPECT_FALSE(f <= (jfloat)((jlong)(~(((u_jlong)-1L) >> 1)))) ?
          (jlong)(~(((u_jlong)-1L) >> 1)) : (jlong)(-JCGO_FP_FLOORF(-f))) :
          JCGO_EXPECT_TRUE(f < -(jfloat)(-(jlong)(((u_jlong)-1L) >> 1))) ?
          (jlong)JCGO_FP_FLOORF(f) : (jlong)(((u_jlong)-1L) >> 1);
 return (jlong)0L;
}

JCGO_NOSEP_EXTRASTATIC jint CFASTCALL jcgo_jdouble2jint( jdouble d )
{
 if (JCGO_FP_NOTNAN(d))
  return d < (jdouble)0.0 ?
          (JCGO_EXPECT_FALSE(d <= (jdouble)((jint)(~(((u_jint)-1) >> 1)))) ?
          (jint)(~(((u_jint)-1) >> 1)) : (jint)(-JCGO_FP_FLOOR(-d))) :
          JCGO_EXPECT_TRUE(d < (jdouble)(jint)(((u_jint)-1) >> 1)) ?
          (jint)JCGO_FP_FLOOR(d) : (jint)(((u_jint)-1) >> 1);
 return 0;
}

JCGO_NOSEP_EXTRASTATIC jlong CFASTCALL jcgo_jdouble2jlong( jdouble d )
{
 if (JCGO_FP_NOTNAN(d))
  return d < (jdouble)0.0 ? (JCGO_EXPECT_FALSE(d <=
          (jdouble)((jlong)(~(((u_jlong)-1L) >> 1)))) ?
          (jlong)(~(((u_jlong)-1L) >> 1)) : (jlong)(-JCGO_FP_FLOOR(-d))) :
          JCGO_EXPECT_TRUE(d < -(jdouble)(-(jlong)(((u_jlong)-1L) >> 1))) ?
          (jlong)JCGO_FP_FLOOR(d) : (jlong)(((u_jlong)-1L) >> 1);
 return (jlong)0L;
}

JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL jcgo_jdouble2jfloat( jdouble d )
{
 jfloat f;
 f = (jfloat)d;
 return f;
}

#ifndef JCGO_FPFAST

JCGO_NOSEP_STATIC int CFASTCALL jcgo_fequal( jdouble d1, jdouble d2 )
{
 return d1 == d2 && JCGO_FP_NOTNAN(d1) && JCGO_FP_NOTNAN(d2);
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_fequalf( jfloat f1, jfloat f2 )
{
 return f1 == f2 && JCGO_FP_NOTNANF(f1) && JCGO_FP_NOTNANF(f2);
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessequ( jdouble d1, jdouble d2 )
{
 return d1 <= d2 && JCGO_FP_NOTNAN(d1) && JCGO_FP_NOTNAN(d2);
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessequf( jfloat f1, jfloat f2 )
{
 return f1 <= f2 && JCGO_FP_NOTNANF(f1) && JCGO_FP_NOTNANF(f2);
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessthan( jdouble d1, jdouble d2 )
{
 return d1 < d2 && JCGO_FP_NOTNAN(d1) && JCGO_FP_NOTNAN(d2);
}

JCGO_NOSEP_STATIC int CFASTCALL jcgo_flessthanf( jfloat f1, jfloat f2 )
{
 return f1 < f2 && JCGO_FP_NOTNANF(f1) && JCGO_FP_NOTNANF(f2);
}

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_fdiv( jdouble d1, jdouble d2 )
{
 if (JCGO_EXPECT_FALSE(d2 >= (jdouble)-0.0 && d2 <= (jdouble)0.0))
 {
  if (d1 == (jdouble)0.0)
   return jcgo_fpNaN;
  if (JCGO_FP_ZEROISNEG(d2))
   d1 = -d1;
  return jcgo_fpInf * d1;
 }
 return d1 / d2;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_fdivf( jfloat f1, jfloat f2 )
{
 if (JCGO_EXPECT_FALSE(f2 >= (jfloat)-0.0 && f2 <= (jfloat)0.0))
 {
  if (f1 == (jfloat)0.0)
   return (jfloat)jcgo_fpNaN;
  if (JCGO_FP_ZEROISNEG(f2))
   f1 = -f1;
  return (jfloat)jcgo_fpInf * f1;
 }
 return f1 / f2;
}

#endif /* ! JCGO_FPFAST */

#endif /* ! JCGO_NOFP */

#ifdef JCGO_NOFP
JCGO_NOSEP_INLINE jdouble JCGO_INLFRW_FASTCALL
#else
#ifdef JCGO_FASTMATH
JCGO_NOSEP_INLINE jdouble JCGO_INLFRW_FASTCALL
#else
JCGO_NOSEP_STATIC jdouble CFASTCALL
#endif
#endif
jcgo_fmod( jdouble d1, jdouble d2 )
{
#ifdef JCGO_NOFP
 return (u_jlong)(d2 + (jdouble)1) > (u_jlong)1L ?
         (jdouble)((jlong)d1 % (jlong)d2) : (jdouble)0;
#else
#ifdef JCGO_FASTMATH
#ifdef JCGO_LONGDBL
 return fmodl(d1, d2);
#else
 return fmod(d1, d2);
#endif
#else
 jdouble d;
 if (JCGO_EXPECT_TRUE(d2 != (jdouble)0.0) && JCGO_FP_FINITE(d1))
 {
  if (JCGO_FP_FINITE(d2))
  {
   d = d1;
   if (d1 < (jdouble)0.0)
    d1 = -d1;
   if (d2 < (jdouble)0.0)
    d2 = -d2;
   if (JCGO_EXPECT_TRUE(d1 >= d2))
   {
#ifdef JCGO_LONGDBL
    d1 = fmodl(d1, d2);
#else
    d1 = JCGO_EXPECT_FALSE(d1 < DBL_MIN) ||
          (JCGO_EXPECT_FALSE(d2 < DBL_MIN) && d1 <= 1.0) ?
          fmod(d1 * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
          (1L << (DBL_MANT_DIG / 2))),
          d2 * ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
          (1L << (DBL_MANT_DIG / 2)))) /
          ((double)(1L << ((DBL_MANT_DIG - 1) / 2)) *
          (1L << (DBL_MANT_DIG / 2))) : fmod(d1, d2);
#endif
   }
   if (d < (jdouble)0.0)
    d1 = -d1;
   return d1;
  }
  if (JCGO_FP_NOTNAN(d2))
   return d1;
 }
 return jcgo_fpNaN;
#endif
#endif
}

#ifdef JCGO_NOFP
JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL
#else
#ifdef JCGO_FASTMATH
JCGO_NOSEP_INLINE jfloat JCGO_INLFRW_FASTCALL
#else
JCGO_NOSEP_STATIC jfloat CFASTCALL
#endif
#endif
jcgo_fmodf( jfloat f1, jfloat f2 )
{
#ifdef JCGO_NOFP
 return (u_jint)(f2 + (jfloat)1) > (u_jint)1 ?
         (jfloat)((jint)f1 % (jint)f2) : (jfloat)0;
#else
#ifdef JCGO_FASTMATH
 return (jfloat)JCGO_FP_FMODF(f1, f2);
#else
 jfloat f;
 if (JCGO_EXPECT_TRUE(f2 != (jfloat)0.0) && JCGO_FP_FINITEF(f1))
 {
  if (JCGO_FP_FINITEF(f2))
  {
   f = f1;
   if (f1 < (jfloat)0.0)
    f1 = -f1;
   if (f2 < (jfloat)0.0)
    f2 = -f2;
   if (JCGO_EXPECT_TRUE(f1 >= f2))
    f1 = (jfloat)JCGO_FP_FMODF(f1, f2);
   if (f < (jfloat)0.0)
    f1 = -f1;
   return f1;
  }
  if (JCGO_FP_NOTNANF(f2))
   return f1;
 }
 return (jfloat)jcgo_fpNaN;
#endif
#endif
}

#endif

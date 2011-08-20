/*
 * @(#) $(JCGO)/include/jcgomath.h --
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

#ifdef JCGO_LONGDBL

/* #include <math.h> */
/* long double acosl(long double); */
/* long double asinl(long double); */
/* long double atan2l(long double, long double); */
/* long double atanl(long double); */
/* long double coshl(long double); */
/* long double cosl(long double); */
/* long double expl(long double); */
/* long double floorl(long double); */
/* long double fmodl(long double, long double); */
/* long double log10l(long double); */
/* long double logl(long double); */
/* long double powl(long double, long double); */
/* long double sinhl(long double); */
/* long double sinl(long double); */
/* long double sqrtl(long double); */
/* long double tanhl(long double); */
/* long double tanl(long double); */

#ifndef _MATH_NO_HYPOT
/* #include <math.h> */
/* long double hypotl(long double, long double); */
#ifdef _hypotl
#ifdef hypotl
#define JCGO_FP_HYPOT(d1, d2) hypotl(d1, d2)
#else
#define JCGO_FP_HYPOT(d1, d2) _hypotl(d1, d2)
#endif
#else
#define JCGO_FP_HYPOT(d1, d2) hypotl(d1, d2)
#endif
#endif

#ifndef M_El
#define M_El 2.71828182845904523536L
#endif

#ifndef M_LN2l
#define M_LN2l 0.69314718055994530942L
#endif

#ifndef M_PIl
#define M_PIl 3.14159265358979323846L
#endif

#ifndef M_SQRT2l
#define M_SQRT2l 1.41421356237309504880L
#endif

#ifndef LDBL_MAX_EXP
#define LDBL_MAX_EXP (1 << 14)
#endif

#ifndef LN_MAXLDOUBLE
#define LN_MAXLDOUBLE (M_LN2l * LDBL_MAX_EXP)
#endif

#else /* JCGO_LONGDBL */

/* #include <math.h> */
/* double acos(double); */
/* double asin(double); */
/* double atan(double); */
/* double atan2(double, double); */
/* double cos(double); */
/* double cosh(double); */
/* double exp(double); */
/* double floor(double); */
/* double fmod(double, double); */
/* double log(double); */
/* double log10(double); */
/* double pow(double, double); */
/* double sin(double); */
/* double sinh(double); */
/* double sqrt(double); */
/* double tan(double); */
/* double tanh(double); */

#ifndef _MATH_NO_HYPOT
/* #include <math.h> */
/* double hypot(double, double); */
#ifdef _MSC_VER
#define JCGO_FP_HYPOT(d1, d2) _hypot(d1, d2)
#else
#ifdef __MINGW32__
#define JCGO_FP_HYPOT(d1, d2) _hypot(d1, d2)
#else
#define JCGO_FP_HYPOT(d1, d2) hypot(d1, d2)
#endif
#endif
#endif

#ifndef M_E
#define M_E 2.71828182845904523536
#endif

#ifndef M_LN2
#define M_LN2 0.69314718055994530942
#endif

#ifndef M_LN10
#define M_LN10 2.30258509299404568402
#endif

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#ifndef M_SQRT2
#define M_SQRT2 1.41421356237309504880
#endif

#ifndef DBL_MAX_EXP
#define DBL_MAX_EXP (1 << ((int)sizeof(double) * 8 - DBL_MANT_DIG - 1))
#endif

#ifndef DBL_MIN_EXP
#define DBL_MIN_EXP (3 - DBL_MAX_EXP)
#endif

#ifndef LN_MAXDOUBLE
#define LN_MAXDOUBLE (M_LN2 * DBL_MAX_EXP)
#endif

#ifndef LN_MINDOUBLE
#define LN_MINDOUBLE (M_LN2 * (DBL_MIN_EXP - 1))
#endif

#endif /* ! JCGO_LONGDBL */

#ifdef JCGO_MATHEXT

#ifdef JCGO_LONGDBL
/* #include <math.h> */
/* long double cbrtl(long double); */
/* long double expm1l(long double); */
/* long double log1pl(long double); */
#else
/* #include <math.h> */
/* double cbrt(double); */
/* double expm1(double); */
/* double log1p(double); */
#endif

#endif /* JCGO_MATHEXT */

#endif

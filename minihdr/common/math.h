/*
 * @(#) $(JCGO)/minihdr/common/math.h --
 * a part of the minimalist "libc" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
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

#ifndef _MATH_H
#define _MATH_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _EXPFUNC_MATH
#define _EXPFUNC_MATH _EXPFUNC_BUILTIN
#endif

#ifndef _RTLENTRY_MATH
#define _RTLENTRY_MATH _RTLENTRY_BUILTIN
#endif

#ifndef INFINITY
_EXPFUNC_MATH float _RTLENTRY_MATH __f_infinity_func(void) _ATTRIBUTE_PURE;
#define INFINITY __f_infinity_func() /* or (1f / 0) */
#endif

#ifndef HUGE_VAL
#define HUGE_VAL ((double)INFINITY)
#endif

#ifndef NAN
_EXPFUNC_MATH float _RTLENTRY_MATH __f_posqnan_func(void) _ATTRIBUTE_PURE;
#define NAN __f_posqnan_func() /* or (0f / 0) */
#endif

#ifndef M_E
#define M_E 2.71828182845904523536
#define M_LN2 0.69314718055994530942
#define M_LN10 2.30258509299404568402
#define M_PI 3.14159265358979323846
#define M_SQRT2 1.41421356237309504880
#endif

_EXPFUNC_MATH double _RTLENTRY_MATH acos(double);
_EXPFUNC_MATH double _RTLENTRY_MATH asin(double);
_EXPFUNC_MATH double _RTLENTRY_MATH atan(double);
_EXPFUNC_MATH double _RTLENTRY_MATH atan2(double, double);
_EXPFUNC_MATH double _RTLENTRY_MATH cos(double);
_EXPFUNC_MATH double _RTLENTRY_MATH cosh(double);
_EXPFUNC_MATH double _RTLENTRY_MATH exp(double);
_EXPFUNC_MATH double _RTLENTRY_MATH floor(double) _ATTRIBUTE_PURE;
_EXPFUNC_MATH double _RTLENTRY_MATH fmod(double, double);
_EXPFUNC_MATH double _RTLENTRY_MATH log(double);
_EXPFUNC_MATH double _RTLENTRY_MATH log10(double);
_EXPFUNC_MATH double _RTLENTRY_MATH pow(double, double);
_EXPFUNC_MATH double _RTLENTRY_MATH sin(double);
_EXPFUNC_MATH double _RTLENTRY_MATH sinh(double);
_EXPFUNC_MATH double _RTLENTRY_MATH sqrt(double);
_EXPFUNC_MATH double _RTLENTRY_MATH tan(double);
_EXPFUNC_MATH double _RTLENTRY_MATH tanh(double);

#ifndef _MATH_NO_HYPOT
_EXPFUNC_MATH double _RTLENTRY_MATH hypot(double, double);
#endif

_EXPFUNC_MATH double _RTLENTRY_MATH cbrt(double);
_EXPFUNC_MATH double _RTLENTRY_MATH expm1(double);
_EXPFUNC_MATH double _RTLENTRY_MATH log1p(double);

_EXPFUNC_MATH float _RTLENTRY_MATH floorf(float) _ATTRIBUTE_PURE;
_EXPFUNC_MATH float _RTLENTRY_MATH fmodf(float, float);

_EXPFUNC_MATH int _RTLENTRY_MATH finitef(float) _ATTRIBUTE_PURE;
_EXPFUNC_MATH int _RTLENTRY_MATH isnanf(float) _ATTRIBUTE_PURE;

#ifndef _isnan
#define _isnan isnan
#endif

_EXPFUNC_MATH int _RTLENTRY_MATH finite(double) _ATTRIBUTE_PURE;
_EXPFUNC_MATH int _RTLENTRY_MATH _isnan(double) _ATTRIBUTE_PURE;

#ifndef _MATH_NO_SIGNBIT
_EXPFUNC_MATH int _RTLENTRY_MATH __signbit(double) _ATTRIBUTE_PURE;
_EXPFUNC_MATH int _RTLENTRY_MATH __signbitf(float) _ATTRIBUTE_PURE;
#endif

#ifdef _MATH_HAS_LONGDOUBLE

#ifndef M_El
#define M_El 2.7182818284590452353602874713526625L
#define M_LN2l 0.6931471805599453094172321214581766L
#define M_LN10l 2.3025850929940456840179914546843642L
#define M_PIl 3.1415926535897932384626433832795029L
#define M_SQRT2l 1.4142135623730950488016887242096981L
#endif

_EXPFUNC_MATH long double _RTLENTRY_MATH acosl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH asinl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH atan2l(long double, long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH atanl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH coshl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH cosl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH expl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH floorl(long double) _ATTRIBUTE_PURE;
_EXPFUNC_MATH long double _RTLENTRY_MATH fmodl(long double, long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH log10l(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH logl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH powl(long double, long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH sinhl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH sinl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH sqrtl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH tanhl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH tanl(long double);

#ifndef _MATH_NO_HYPOT
_EXPFUNC_MATH long double _RTLENTRY_MATH hypotl(long double, long double);
#endif

_EXPFUNC_MATH long double _RTLENTRY_MATH cbrtl(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH expm1l(long double);
_EXPFUNC_MATH long double _RTLENTRY_MATH log1pl(long double);

_EXPFUNC_MATH int _RTLENTRY_MATH finitel(long double) _ATTRIBUTE_PURE;
_EXPFUNC_MATH int _RTLENTRY_MATH isnanl(long double) _ATTRIBUTE_PURE;

#ifndef _MATH_NO_SIGNBIT
_EXPFUNC_MATH int _RTLENTRY_MATH __signbitl(long double) _ATTRIBUTE_PURE;
#ifndef signbit
#define signbit(v) (sizeof(v) == sizeof(float) ? __signbitf((float)(v)) : sizeof(v) == sizeof(double) ? __signbit((double)(v)) : __signbitl(v))
#endif
#endif

#ifndef isfinite
#define isfinite(v) (sizeof(v) == sizeof(float) ? finitef((float)(v)) : sizeof(v) == sizeof(double) ? finite((double)(v)) : finitel(v))
#endif

#ifndef isnan
#define isnan(v) (sizeof(v) == sizeof(float) ? isnanf((float)(v)) : sizeof(v) == sizeof(double) ? _isnan((double)(v)) : isnanl(v))
#endif

#else

#ifndef _MATH_NO_SIGNBIT
#ifndef signbit
#define signbit(v) (sizeof(v) == sizeof(float) ? __signbitf((float)(v)) : __signbit(v))
#endif
#endif

#ifndef isfinite
#define isfinite(v) (sizeof(v) == sizeof(float) ? finitef((float)(v)) : finite(v))
#endif

#ifndef isnan
#define isnan(v) (sizeof(v) == sizeof(float) ? isnanf((float)(v)) : _isnan(v))
#endif

#endif

#ifdef __cplusplus
}
#endif

#endif

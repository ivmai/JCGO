/*
 * @(#) $(JCGO)/include/jcgofp.h --
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

#ifndef _MATH_H
#include <math.h>
/* const float INFINITY; */
/* const float NAN; */
#endif

#ifndef _FLOAT_H
#include <float.h>
/* const double DBL_MIN; */
#endif

#ifndef FPINIT
#ifdef _MCW_EM
/* #include <float.h> */
/* unsigned _control87(unsigned, unsigned); */
#ifdef _status87
#ifndef _control87
#define _control87 _controlfp
#endif
#endif
#ifdef JCGO_LONGDBL
#ifdef _PC_64
#define FPINIT _control87(_MCW_EM | _PC_64, _MCW_EM | _MCW_PC)
#endif
#else
#ifdef _PC_53
#define FPINIT _control87(_MCW_EM | _PC_53, _MCW_EM | _MCW_PC)
#endif
#endif
#else
#ifdef MCW_EM
/* #include <float.h> */
/* unsigned _control87(unsigned, unsigned); */
#ifdef _status87
#ifndef _control87
#define _control87 _controlfp
#endif
#endif
#ifdef JCGO_LONGDBL
#ifdef PC_64
#define FPINIT _control87(MCW_EM | PC_64, MCW_EM | MCW_PC)
#endif
#else
#ifdef PC_53
#define FPINIT _control87(MCW_EM | PC_53, MCW_EM | MCW_PC)
#endif
#endif
#else
#ifndef _IEEEFP_H
#include <ieeefp.h>
/* fp_prec_t fpsetprec(fp_prec_t); */
#endif
#ifdef fpsetprec
#ifdef JCGO_LONGDBL
#define FPINIT (void)fpsetprec(FP_PE)
#else
#define FPINIT (void)fpsetprec(FP_PD)
#endif
#else
#ifndef _FPU_CONTROL_H
#include <fpu_control.h>
#endif
#ifdef _FPU_EXTENDED
#ifdef _FPU_DOUBLE
#ifdef JCGO_LONGDBL
#define FPINIT { fpu_control_t cw; _FPU_GETCW(cw); cw = (cw & ~_FPU_DOUBLE) | _FPU_EXTENDED; _FPU_SETCW(cw); }
#else
#define FPINIT { fpu_control_t cw; _FPU_GETCW(cw); cw = (cw & ~_FPU_EXTENDED) | _FPU_DOUBLE; _FPU_SETCW(cw); }
#endif
#endif
#endif
#endif
#endif
#endif
#endif

#ifndef FLT_MANT_DIG
#ifdef FSIGNIF
#define FLT_MANT_DIG FSIGNIF
#else
#define FLT_MANT_DIG ((int)sizeof(float) * 8 - 8)
#endif
#endif

#ifndef DBL_MANT_DIG
#ifdef DSIGNIF
#define DBL_MANT_DIG DSIGNIF
#else
#define DBL_MANT_DIG ((int)sizeof(double) * 8 - 11)
#endif
#endif

#ifndef _MATH_NO_SIGNBIT
#ifdef signbit
/* #include <math.h> */
/* int signbit(<fptype>); */
#define JCGO_FP_ZEROISNEG(v) signbit(v)
#endif
#endif

#ifndef JCGO_FP_ZEROISNEG
#define JCGO_FP_ZEROISNEG(v) ((*((volatile signed char *)&(v) + (sizeof(v) - 1)) | *((volatile signed char *)&(v) + (sizeof(v) >> 1)) | *((volatile signed char *)&(v) + ((sizeof(v) >> 1) - 1)) | *(volatile signed char *)&(v)) < 0)
#endif

#ifdef isnan
/* #include <math.h> */
/* int isnan(<fptype>); */
#define JCGO_FPI_NOTNAN(d) (!isnan(d))
#define JCGO_FPI_NOTNANF(f) JCGO_FPI_NOTNAN(f)
#else
#ifdef fpclassify
/* #include <math.h> */
/* int fpclassify(<fptype>); */
#define JCGO_FPI_NOTNAN(d) (fpclassify(d) != FP_NAN)
#define JCGO_FPI_NOTNANF(f) JCGO_FPI_NOTNAN(f)
#else
#ifdef JCGO_MATHEXT
#ifdef JCGO_LONGDBL
/* #include <math.h> */
/* int isnanl(long double); */
#define JCGO_FPI_NOTNAN(d) (!isnanl(d))
#else
/* #include <math.h> */
/* int isnan(double); */
#define JCGO_FPI_NOTNAN(d) (!isnan(d))
#endif
/* #include <math.h> */
/* int isnanf(float); */
#define JCGO_FPI_NOTNANF(f) (!isnanf(f))
#else
#define JCGO_FPI_NOTNAN(d) ((d) + jcgo_fpZero >= (d) && (d) - jcgo_fpZero <= (d))
#define JCGO_FPI_NOTNANF(f) ((f) + jcgo_fpZeroF >= (f) && (f) - jcgo_fpZeroF <= (f))
#endif
#endif
#endif

#ifdef isfinite
/* #include <math.h> */
/* int isfinite(<fptype>); */
#define JCGO_FPI_FINITE(d) (isfinite(d) != 0)
#define JCGO_FPI_FINITEF(f) JCGO_FPI_FINITE(f)
#else
#ifdef JCGO_MATHEXT
#ifdef JCGO_LONGDBL
/* #include <math.h> */
/* int finitel(long double); */
#define JCGO_FPI_FINITE(d) (finitel(d) != 0)
#else
/* #include <math.h> */
/* int finite(double); */
#define JCGO_FPI_FINITE(d) (finite(d) != 0)
#endif
/* #include <math.h> */
/* int finitef(float); */
#define JCGO_FPI_FINITEF(f) (finitef(f) != 0)
#else
#define JCGO_FPI_FINITE(d) JCGO_FPI_NOTNAN((d) * jcgo_fpZero)
#define JCGO_FPI_FINITEF(f) JCGO_FPI_NOTNANF((f) * jcgo_fpZeroF)
#endif
#endif

#define JCGO_FP_FINITE(d) JCGO_EXPECT_TRUE(JCGO_FPI_FINITE(d))
#define JCGO_FP_FINITEF(f) JCGO_EXPECT_TRUE(JCGO_FPI_FINITEF(f))
#define JCGO_FP_NOTNAN(d) JCGO_EXPECT_TRUE(JCGO_FPI_NOTNAN(d))
#define JCGO_FP_NOTNANF(f) JCGO_EXPECT_TRUE(JCGO_FPI_NOTNANF(f))

#ifdef JCGO_MATHEXT

/* #include <math.h> */
/* float floorf(float); */
/* float fmodf(float, float); */

#define JCGO_FP_FLOORF(f) floorf(f)
#define JCGO_FP_FMODF(f1, f2) fmodf(f1, f2)

#endif /* JCGO_MATHEXT */

#ifdef JCGO_LONGDBL

/* #include <math.h> */
/* long double floorl(long double); */
/* long double fmodl(long double, long double); */

#define JCGO_FP_FLOOR(d) floorl(d)

#ifndef JCGO_MATHEXT
#define JCGO_FP_FLOORF(f) ((float)floorl((double)(f)))
#define JCGO_FP_FMODF(f1, f2) ((float)fmodl((double)(f1), (double)(f2)))
#endif

#else /* JCGO_LONGDBL */

/* #include <math.h> */
/* double floor(double); */
/* double fmod(double, double); */

#ifndef DBL_MIN
#define DBL_MIN 2.2250738585072014E-308
#endif

#define JCGO_FP_FLOOR(d) floor(d)

#ifndef JCGO_MATHEXT
#define JCGO_FP_FLOORF(f) ((float)floor((double)(f)))
#define JCGO_FP_FMODF(f1, f2) ((float)fmod((double)(f1), (double)(f2)))
#endif

#endif /* ! JCGO_LONGDBL */

#endif

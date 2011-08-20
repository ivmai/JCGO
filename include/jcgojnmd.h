/*
 * @(#) $(JCGO)/include/jcgojnmd.h --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2010 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
 */

/*
 * Used control macros: HAVE_CONFIG_H, JCGO_BOOLINT, JCGO_INTFIT, JCGO_INTNN,
 * JCGO_INVLL, JCGO_LONGDBL, JCGO_NOFP, JCGO_NOJNI, JCGO_USELONG.
 * Macros for tuning: CONST, JNICALL, JNIEXPORT, JNIIMPORT.
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

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#ifndef JCGO_NOJNI
#ifndef va_arg
#ifndef _STDARG_H
#include <stdarg.h>
#endif
#endif
#endif

#ifndef JNICALL
#define JNICALL /* empty */
#endif

#ifndef JNIEXPORT
#define JNIEXPORT /* empty */
#endif

#ifndef JNIIMPORT
#define JNIIMPORT /* empty */
#endif

#ifdef JCGO_BOOLINT
typedef int jboolean;
#else
typedef unsigned char jboolean;
#endif

typedef signed char jbyte;

#ifdef JCGO_INTNN
typedef __int16 jshort;
typedef unsigned __int16 jchar;
#else
typedef short jshort;
typedef unsigned short jchar;
#endif

#ifdef JCGO_INTFIT
typedef int jint;
typedef unsigned u_jint;
#else
#ifdef JCGO_INTNN
typedef __int32 jint;
typedef unsigned __int32 u_jint;
#else
typedef long jint;
typedef unsigned long u_jint;
#endif
#endif

#ifdef JCGO_USELONG
typedef long jlong;
typedef unsigned long u_jlong;
#define JLONG_C(n) (jlong)n##L
#else
#ifdef JCGO_INTNN
typedef __int64 jlong;
typedef unsigned __int64 u_jlong;
#ifdef JCGO_INVLL
#define JLONG_C(n) (jlong)n##LL
#else
#define JLONG_C(n) (jlong)n##i64
#endif
#else
#ifdef __int64
typedef __int64 jlong;
typedef unsigned __int64 u_jlong;
#else
typedef long long jlong;
typedef unsigned long long u_jlong;
#endif
#ifdef JCGO_INVLL
#define JLONG_C(n) (jlong)n##i64
#else
#define JLONG_C(n) (jlong)n##LL
#endif
#endif
#endif

#ifdef JCGO_NOFP
typedef jint jfloat;
#define JCGO_VAARG_JFLOAT jfloat
typedef jlong jdouble;
#else
typedef float jfloat;
#define JCGO_VAARG_JFLOAT double
#ifdef JCGO_LONGDBL
typedef long double jdouble;
#else
typedef double jdouble;
#endif
#endif

typedef jint jsize;

#endif

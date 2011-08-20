/*
 * @(#) $(JCGO)/minihdr/common/float.h --
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

#ifndef _FLOAT_H
#define _FLOAT_H

#ifndef _STDDEF_H
#include <stddef.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef CHAR_BIT
#define CHAR_BIT 8
#endif

#ifndef FLT_MANT_DIG
#define FLT_MANT_DIG ((int)sizeof(float) * CHAR_BIT - 8)
#endif

#ifndef DBL_MANT_DIG
#define DBL_MANT_DIG ((int)sizeof(double) * CHAR_BIT - 11)
#endif

#ifndef DBL_MAX_EXP
#define DBL_MAX_EXP (1 << ((int)sizeof(double) * CHAR_BIT - DBL_MANT_DIG - 1))
#endif

#ifndef DBL_MIN_EXP
#define DBL_MIN_EXP (3 - DBL_MAX_EXP)
#endif

#ifndef DBL_MIN
#define DBL_MIN 2.2250738585072014E-308
#endif

#ifndef LDBL_MAX_EXP
#define LDBL_MAX_EXP (1 << 14)
#endif

#ifndef _FLOAT_NO_CONTROL87

#ifndef _MCW_EM
#define _MCW_EM 0x3f /* or 0x1f, or 0x8001f */
#endif

#ifndef _MCW_PC
#define _MCW_PC 0x300 /* or 0x30000 */
#define _PC_53 0x200 /* or 0, or 0x10000 */
#define _PC_64 0x300 /* or 0, or 0x20000 */
#endif

_EXPFUNC unsigned _RTLENTRY _control87(unsigned, unsigned);

#endif

#ifdef __cplusplus
}
#endif

#endif

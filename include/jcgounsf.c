/*
 * @(#) $(JCGO)/include/jcgounsf.c --
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

#ifndef JCGO_ATOMICOP_END
#define JCGO_ATOMICOP_BEGIN(addr) {
#define JCGO_ATOMICOP_END }
#endif

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__initArrayOffsets0__IAIA( jintArr arrayBaseOffsets,
 jintArr arrayIndexScales )
{
 JCGO_ARR_INTERNALACC(jint, arrayBaseOffsets, 0) =
  (jint)jcgo_primitiveOffset[OBJT_jint];
 JCGO_ARR_INTERNALACC(jint, arrayBaseOffsets, 1) =
  (jint)jcgo_primitiveOffset[OBJT_jlong];
 JCGO_ARR_INTERNALACC(jint, arrayBaseOffsets, 2) =
  (jint)jcgo_primitiveOffset[0];
 JCGO_ARR_INTERNALACC(jint, arrayIndexScales, 0) =
  (jint)jcgo_primitiveSize[OBJT_jint];
 JCGO_ARR_INTERNALACC(jint, arrayIndexScales, 1) =
  (jint)jcgo_primitiveSize[OBJT_jlong];
 JCGO_ARR_INTERNALACC(jint, arrayIndexScales, 2) =
  (jint)jcgo_primitiveSize[0];
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__compareAndSwapInt0__LoJII( java_lang_Object obj,
 jlong offset, jint expect, jint value )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 int res = 0;
 JCGO_ATOMICOP_BEGIN(addr)
 if (JCGO_EXPECT_TRUE(*(volatile jint *)addr == expect))
 {
  *(volatile jint *)addr = value;
  res = 1;
 }
 JCGO_ATOMICOP_END
 return (jint)res;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__compareAndSwapLong0__LoJJJ( java_lang_Object obj,
 jlong offset, jlong expect, jlong value )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 int res = 0;
 JCGO_ATOMICOP_BEGIN(addr)
 if (JCGO_EXPECT_TRUE(*(volatile jlong *)addr == expect))
 {
  *(volatile jlong *)addr = value;
  res = 1;
 }
 JCGO_ATOMICOP_END
 return (jint)res;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__compareAndSwapObject0__LoLoLoJ( java_lang_Object obj,
 java_lang_Object expect, java_lang_Object value, jlong offset )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 int res = 0;
 JCGO_ATOMICOP_BEGIN(addr)
 if (JCGO_EXPECT_TRUE(*(java_lang_Object volatile *)addr == expect))
 {
  *(java_lang_Object volatile *)addr = value;
  res = 1;
 }
 JCGO_ATOMICOP_END
 return (jint)res;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__putInt0__LoJII( java_lang_Object obj, jlong offset,
 jint value, jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 JCGO_ATOMICOP_BEGIN(addr)
 *(volatile jint *)addr = value;
 JCGO_ATOMICOP_END
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__putLong0__LoJJI( java_lang_Object obj, jlong offset,
 jlong value, jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 JCGO_ATOMICOP_BEGIN(addr)
 *(volatile jlong *)addr = value;
 JCGO_ATOMICOP_END
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__putObject0__LoLoJI( java_lang_Object obj,
 java_lang_Object value, jlong offset, jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 JCGO_ATOMICOP_BEGIN(addr)
 *(java_lang_Object volatile *)addr = value;
 JCGO_ATOMICOP_END
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
sun_misc_Unsafe__getInt0__LoJI( java_lang_Object obj, jlong offset,
 jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 jint value;
 JCGO_ATOMICOP_BEGIN(addr)
 value = *(volatile jint *)addr;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jlong CFASTCALL
sun_misc_Unsafe__getLong0__LoJI( java_lang_Object obj, jlong offset,
 jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 jlong value;
 JCGO_ATOMICOP_BEGIN(addr)
 value = *(volatile jlong *)addr;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
sun_misc_Unsafe__getObject0__LoJI( java_lang_Object obj, jlong offset,
 jint consistency )
{
 void *addr = obj != jnull ? (char JCGO_HPTR_MOD *)&JCGO_METHODS_OF(obj) +
               (JCGO_ALLOCSIZE_T)offset : JCGO_CAST_NUMTOPTR(offset);
 java_lang_Object value;
 JCGO_ATOMICOP_BEGIN(addr)
 value = (java_lang_Object)(*(java_lang_Object volatile *)addr);
 JCGO_ATOMICOP_END
 return value;
}

#endif

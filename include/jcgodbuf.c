/*
 * @(#) $(JCGO)/include/jcgodbuf.c --
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

JCGO_NOSEP_STATIC java_lang_Object CFASTCALL
java_nio_VMDirectByteBuffer__allocate0__I( jint capacity )
{
 return (java_lang_Object)jcgo_memAlloc((JCGO_ALLOCSIZE_T)capacity, NULL);
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_nio_VMDirectByteBuffer__getRegion0__LoBAIII( java_lang_Object vmdata,
 jbyteArr dst, jint offset, jint len, jint index )
{
 JCGO_MEM_HCOPY(&JCGO_ARR_INTERNALACC(jbyte, dst, offset),
  (void *)((volatile char JCGO_HPTR_MOD *)vmdata + (JCGO_ALLOCSIZE_T)index),
  (JCGO_ALLOCSIZE_T)len);
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_nio_VMDirectByteBuffer__putRegion0__LoBAIII( java_lang_Object vmdata,
 jbyteArr src, jint offset, jint len, jint index )
{
 JCGO_MEM_HCOPY((void *)((volatile char JCGO_HPTR_MOD *)vmdata +
  (JCGO_ALLOCSIZE_T)index), &JCGO_ARR_INTERNALACC(jbyte, src, offset),
  (JCGO_ALLOCSIZE_T)len);
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_nio_VMDirectByteBuffer__moveRegion0__LoIII( java_lang_Object vmdata,
 jint dstIndex, jint srcIndex, jint len )
{
 JCGO_MEM_HMOVE((void *)((volatile char JCGO_HPTR_MOD *)vmdata +
  (JCGO_ALLOCSIZE_T)dstIndex),
  (void *)((volatile char JCGO_HPTR_MOD *)vmdata +
  (JCGO_ALLOCSIZE_T)srcIndex), (JCGO_ALLOCSIZE_T)len);
 return 0;
}

JCGO_NOSEP_STATIC jint CFASTCALL
java_nio_VMDirectByteBuffer__free0__Lo( java_lang_Object vmdata )
{
 /* dummy */
 return 0;
}

#endif

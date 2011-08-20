/*
 * @(#) $(JCGO)/include/jcgoaop.c --
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

#ifndef JCGO_ATOMICOP_HASHLOGSZ
#define JCGO_ATOMICOP_HASHLOGSZ 4
#endif

#define JCGO_ATOMICOP_BEGIN(addr) { int jcgo_atomicMutexInd = JCGO_MUTEXIND_HASHF(JCGO_CAST_PTRTONUM(addr), JCGO_ATOMICOP_HASHLOGSZ); (void)JCGO_MUTEX_LOCK(&jcgo_atomicOpsMutexes[jcgo_atomicMutexInd]); {
#define JCGO_ATOMICOP_END } (void)JCGO_MUTEX_UNLOCK(&jcgo_atomicOpsMutexes[jcgo_atomicMutexInd]); }

JCGO_MUTEX_T jcgo_atomicOpsMutexes[1 << JCGO_ATOMICOP_HASHLOGSZ];

JCGO_NOSEP_INLINE int JCGO_INLFRW_FASTCALL jcgo_atomicOpsInit( void )
{
 int i = 1 << JCGO_ATOMICOP_HASHLOGSZ;
 while (i-- > 0)
  if (JCGO_MUTEX_INIT(&jcgo_atomicOpsMutexes[i]))
   return -1;
 return 0;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_AO_fetchL(
 jObject JCGO_THRD_VOLATILE *pfield )
{
 jObject obj;
 JCGO_ATOMICOP_BEGIN(pfield)
 obj = (jObject)(*pfield);
 JCGO_ATOMICOP_END
 return obj;
}

JCGO_NOSEP_STATIC jObject CFASTCALL jcgo_AO_storeL(
 jObject JCGO_THRD_VOLATILE *pfield, jObject obj )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = obj;
 JCGO_ATOMICOP_END
 return obj;
}

JCGO_NOSEP_STATIC jboolean CFASTCALL jcgo_AO_fetchZ(
 JCGO_THRD_VOLATILE jboolean *pfield )
{
 jboolean value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_fetchB(
 JCGO_THRD_VOLATILE jbyte *pfield )
{
 jbyte value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_fetchC(
 JCGO_THRD_VOLATILE jchar *pfield )
{
 jchar value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_fetchS(
 JCGO_THRD_VOLATILE jshort *pfield )
{
 jshort value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_fetchI(
 JCGO_THRD_VOLATILE jint *pfield )
{
 jint value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_fetchJ(
 JCGO_THRD_VOLATILE jlong *pfield )
{
 jlong value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jboolean CFASTCALL jcgo_AO_storeZ(
 JCGO_THRD_VOLATILE jboolean *pfield, jboolean value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_storeB(
 JCGO_THRD_VOLATILE jbyte *pfield, jbyte value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_storeC(
 JCGO_THRD_VOLATILE jchar *pfield, jchar value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_storeS(
 JCGO_THRD_VOLATILE jshort *pfield, jshort value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_storeI(
 JCGO_THRD_VOLATILE jint *pfield, jint value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_storeJ(
 JCGO_THRD_VOLATILE jlong *pfield, jlong value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_preIncB(
 JCGO_THRD_VOLATILE jbyte *pfield )
{
 jbyte value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_preIncC(
 JCGO_THRD_VOLATILE jchar *pfield )
{
 jchar value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_preIncS(
 JCGO_THRD_VOLATILE jshort *pfield )
{
 jshort value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_preIncI(
 JCGO_THRD_VOLATILE jint *pfield )
{
 jint value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_preIncJ(
 JCGO_THRD_VOLATILE jlong *pfield )
{
 jlong value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jbyte CFASTCALL jcgo_AO_preDecrB(
 JCGO_THRD_VOLATILE jbyte *pfield )
{
 jbyte value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jchar CFASTCALL jcgo_AO_preDecrC(
 JCGO_THRD_VOLATILE jchar *pfield )
{
 jchar value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jshort CFASTCALL jcgo_AO_preDecrS(
 JCGO_THRD_VOLATILE jshort *pfield )
{
 jshort value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jint CFASTCALL jcgo_AO_preDecrI(
 JCGO_THRD_VOLATILE jint *pfield )
{
 jint value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jlong CFASTCALL jcgo_AO_preDecrJ(
 JCGO_THRD_VOLATILE jlong *pfield )
{
 jlong value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

#ifndef JCGO_NOFP

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_fetchF(
 JCGO_THRD_VOLATILE jfloat *pfield )
{
 jfloat value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_fetchD(
 JCGO_THRD_VOLATILE jdouble *pfield )
{
 jdouble value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = *pfield;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_storeF(
 JCGO_THRD_VOLATILE jfloat *pfield, jfloat value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_storeD(
 JCGO_THRD_VOLATILE jdouble *pfield, jdouble value )
{
 JCGO_ATOMICOP_BEGIN(pfield)
 *pfield = value;
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_preIncF(
 JCGO_THRD_VOLATILE jfloat *pfield )
{
 jfloat value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_preIncD(
 JCGO_THRD_VOLATILE jdouble *pfield )
{
 jdouble value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = ++(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jfloat CFASTCALL jcgo_AO_preDecrF(
 JCGO_THRD_VOLATILE jfloat *pfield )
{
 jfloat value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

JCGO_NOSEP_STATIC jdouble CFASTCALL jcgo_AO_preDecrD(
 JCGO_THRD_VOLATILE jdouble *pfield )
{
 jdouble value;
 JCGO_ATOMICOP_BEGIN(pfield)
 value = --(*pfield);
 JCGO_ATOMICOP_END
 return value;
}

#endif /* ! JCGO_NOFP */

#endif
